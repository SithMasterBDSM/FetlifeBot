package sithmaster;


import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.LoggerFactory;

import java.util.logging.Level;

public class Util {
    private static final int DRIVER = 2;
    private static final String CHROME_PROFILE_PATH = "/Users/jedilink/Library/Application Support/Google/Chrome/SeleniumFetlife"; // From chrome://version/
    private static final String MONGO_SERVER = "127.0.0.1";
    private static final int MONGO_PORT = 27017;

    public static void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch ( Exception e ) {
            // Ignore
        }
    }

    public static WebDriver initWebDriver() {
        System.setProperty("webdriver.chrome.silentOutput", "true");
        System.setProperty(ChromeDriverService.CHROME_DRIVER_SILENT_OUTPUT_PROPERTY, "true");

        java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.INFO);
        java.util.logging.Logger.getLogger("org.asynchttpclient.netty.channel").setLevel(Level.INFO);

        WebDriver driver;
        if (DRIVER == 1) {
            driver = new FirefoxDriver();
        } else {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--user-data-dir=" + CHROME_PROFILE_PATH);
            options.addArguments("--log-level=3");
            options.addArguments("--silent");
            options.addArguments("----disable-logging");
            options.addArguments("--disable-in-process-stack-traces");
            //options.setHeadless(true);
            driver = new ChromeDriver(options);
        }
        return driver;
    }

    public static MongoConnection connectWithMongoDatabase() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(ch.qos.logback.classic.Level.OFF);
        MongoConnection mongoConnection = new MongoConnection();
        try {
            MongoClient mongoClient = MongoClients.create("mongodb://" + MONGO_SERVER + ":" + MONGO_PORT);
            MongoDatabase database = mongoClient.getDatabase("fetlife");
            mongoConnection.setUser(database.getCollection("user"));
        } catch (Exception e) {
            System.out.println("ERROR connecting to mongo database");
            return null;
        }
        return mongoConnection;
    }
}
