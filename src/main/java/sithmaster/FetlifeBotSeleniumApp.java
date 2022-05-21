package sithmaster;

import java.io.PrintWriter;
import java.util.List;

import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static com.mongodb.client.model.Filters.eq;

public class FetlifeBotSeleniumApp {
    private static int count = 1;
    private static PrintWriter htmlWriter;

    private static void
    login(WebDriver d) {
        d.get("https://fetlife.com/home");
        Util.delay(5000);
    }

    private static void
    browserSession(MongoConnection mongoConnection) {
        WebDriver driver = Util.initWebDriver();

        try {
            login(driver);
            searchKinksters(driver, mongoConnection);
        } catch (Exception e) {
            System.out.println("Error!");
            e.printStackTrace();
        } finally {
            System.out.println("CLOSED");
            driver.quit();
        }
    }

    private static void searchKinksters(WebDriver driver, MongoConnection mongoConnection) {
            driver.get("https://fetlife.com/p/colombia/bogota/kinksters");
        Util.delay(1000);

        while ( dumpKinkstersResults(driver, mongoConnection) );
    }

    private static boolean dumpKinkstersResults(WebDriver driver, MongoConnection mongoConnection) {
        // Extract results
        List<WebElement> elements = driver.findElements(By.cssSelector("div.ph1"));
        for ( WebElement element: elements ) {
            try {
                WebElement a = element.findElement(By.cssSelector("a.secondary"));
                WebElement span = element.findElement(By.cssSelector("span"));
                if ( a != null && span != null ) {
                    String id = a.getAttribute("href").replace("https://fetlife.com/users/", "");
                    UserTypeInfo userTypeInfo = new UserTypeInfo(span.getText());
                    System.out.println("  - [" + count + "] " + a.getText() + " --> " + id + " -> " + userTypeInfo);

                    count++;
                    if ( !existInMongo(mongoConnection, id) ) {
                        persistToMongo(mongoConnection, a, id, userTypeInfo);
                        exportHtmlLine(a.getAttribute("href"), id, a.getText(), userTypeInfo);
                    }
                }
            } catch ( Exception e ) {
            }
        }

        // Advance page
        WebElement next = driver.findElement(By.cssSelector(".next_page"));
        if ( next == null ) {
            return false;
        }

        try {
            next.click();
        } catch (Exception e) {
            return false;
        }
        Util.delay(1000);
        return true;
    }

    private static boolean existInMongo(MongoConnection mongoConnection, String id) {
        Bson projectionFields = Projections.fields(
                Projections.include("_id"),
                Projections.excludeId());
        Document doc = mongoConnection.getUser().find(eq("_id", id)).projection(projectionFields).first();

        return doc != null;
    }

    private static void persistToMongo(MongoConnection mongoConnection, WebElement a, String id, UserTypeInfo userTypeInfo) {
        Document d = new Document();
        d.put("_id", id);
        d.put("login", a.getText());
        d.put("genre", userTypeInfo.getGenreHints());
        d.put("age", userTypeInfo.getAge());
        d.put("role", userTypeInfo.getRole());

        mongoConnection.getUser().insertOne(d);
    }

    private static void exportHtmlLine(String href, String id, String login, UserTypeInfo userTypeInfo) {
        htmlWriter.println("<tr><td><a href=\""
            + href
            + "\">"
            + login
            + "</a></td><td>"
            + userTypeInfo.getAge()
            + "</td><td>"
            + userTypeInfo.getGenreHints()
            + "</td><td>"
            + userTypeInfo.getRole()
            + "</td></tr>"
        );
        htmlWriter.flush();
    }

    public static void
    main(String[] args) {
        try {
            htmlWriter = new PrintWriter("index.html");
            htmlWriter.println("<html>");
            htmlWriter.println("<table width=\"100%\" borer=\"2\">");
            MongoConnection mongoConnection = Util.connectWithMongoDatabase();
            if (mongoConnection == null) {
                return;
            }
            browserSession(mongoConnection);
            htmlWriter.println("</table>");
            htmlWriter.println("</html>");
        } catch ( Exception e ) {
        }
    }
}
