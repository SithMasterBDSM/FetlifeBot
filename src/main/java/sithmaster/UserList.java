package sithmaster;

import org.openqa.selenium.*;

import java.util.List;
import java.util.TreeSet;

public class UserList {
    private static final int TIME_TO_WAIT_BETWEEN_SCROLLS = 2500;
    private static final String[] blacklist = {
            "home", "explore", "notifications", "s", "messages",
            "tos", "privacy", "SegundaTempora3", "", "login", "settings"};

    private static boolean blacklisted(String e) {
        for (String l: blacklist) {
            if (e.equals(l)) {
                return true;
            }
        }
        return false;
    }

    private static void processLink(TreeSet<String> links, TreeSet<String> users, String val) {
        if (links.contains(val)) {
            return;
        }
        links.add(val);
        if (val.startsWith("https://twitter.com/") &&
                !val.contains("search?q=")) {
            String tail = val.substring(20);
            if (tail.indexOf('/') < 0 && tail.indexOf('?') < 0 && !blacklisted(tail)) {
                System.out.println("  - " + tail);
                users.add(tail);
            }
        }
    }

    public static TreeSet<String> processUserListWebPage(WebDriver d, String url) {
        TreeSet<String> links = new TreeSet<String>();
        TreeSet<String> users = new TreeSet<String>();

        JavascriptExecutor js;
        js = (JavascriptExecutor)d;
        d.get(url);
        Util.delay(2 * TIME_TO_WAIT_BETWEEN_SCROLLS); // Wait for page to be loaded!

        int newSize;
        do {
            newSize = links.size();
            List<WebElement> l = d.findElements(By.tagName("a"));

            for (WebElement e: l) {
                try {
                    String val = e.getAttribute("href");
                    processLink(links, users, val);
                } catch (StaleElementReferenceException se) {
                    // Just skipping now deleted <a> element
                }
            }
            System.out.println("Current users: " + users.size());

            js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
            Util.delay(TIME_TO_WAIT_BETWEEN_SCROLLS);
        } while (newSize != links.size());
        return users;
    }
}

