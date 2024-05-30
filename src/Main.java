import org.json.simple.JSONArray;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import org.json.simple.JSONObject;


public class Main {
    private static final String USERNAME = "shahafbenmoshe@gmail.com";
    private static final String PASSWORD = "13111997";
    private static final String LINKEDIN_URL = "https://www.linkedin.com/";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        JSONObject json = new JSONObject();

        try {
            loginToLinkedin(driver, wait);
            waitForUserVerification(scanner);

            JSONObject profileJson = extractProfileInfo(driver, wait);
            json.put("myName", profileJson.get("myName"));
            json.put("myWorkplace", profileJson.get("myWorkplace"));
            json.put("city", profileJson.get("city"));


            ArrayList<String> connections = extractConnections(driver, wait, jsExecutor);
            JSONArray jsonArray = new JSONArray();
            jsonArray.addAll(connections);

            json.put("connections", jsonArray);


            writeToFile(json);
        } catch (IOException | InterruptedException | WebDriverException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    /**
     * Logs into LinkedIn using provided credentials.
     *
     * @param driver WebDriver instance
     * @param wait   WebDriverWait instance
     */
    private static void loginToLinkedin(WebDriver driver, WebDriverWait wait) {
        driver.get(LINKEDIN_URL);
        driver.findElement(By.className("nav__button-secondary")).click();
        driver.findElement(By.id("username")).sendKeys(USERNAME);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.className("btn__primary--large")).click();
        wait.until(ExpectedConditions.titleContains("LinkedIn"));
    }

    /**
     * Waits for user to verify the LinkedIn security page.
     *
     * @param scanner Scanner instance to read user input
     */
    private static void waitForUserVerification(Scanner scanner) {
        System.out.println("Waiting for user verification...");
        scanner.nextLine();
    }

    /**
     * Extracts profile information from LinkedIn.
     *
     * @param driver WebDriver instance
     * @param wait   WebDriverWait instance
     * @return JSONObject containing profile information
     */
    private static JSONObject extractProfileInfo(WebDriver driver, WebDriverWait wait) {
        JSONObject profileJson = new JSONObject();
        String viewProfileXPath = "//header/div/nav/ul/li[6]/div/div/div/header/a[2]";
        String profileNamePath = "//main/section[1]/div[2]/div[2]/div[1]/div[1]/span/a/h1";
        String profileJobPath = "//main/section[1]/div[2]/div[2]/div[1]/div[2]";
        String profileLocationPath = "//main/section[1]/div[2]/div[2]/div[2]/span[1]";

        driver.findElement(By.className("global-nav__me")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(viewProfileXPath)));
        driver.findElement(By.xpath(viewProfileXPath)).click();

        String profileName = driver.findElement(By.xpath(profileNamePath)).getText();
        String profileJob = driver.findElement(By.xpath(profileJobPath)).getText();
        String profileLocation = driver.findElement(By.xpath(profileLocationPath)).getText();

        profileJson.put("myName", profileName);
        profileJson.put("myWorkplace", profileJob);
        profileJson.put("city", profileLocation);

        System.out.println("Profile Name: " + profileName);
        System.out.println("Profile Job: " + profileJob);
        System.out.println("Profile Location: " + profileLocation);

        return profileJson;
    }

    /**
     * Extracts connections from LinkedIn profile.
     *
     * @param driver     WebDriver instance
     * @param wait       WebDriverWait instance
     * @param jsExecutor JavascriptExecutor instance
     * @return List of connections
     * @throws InterruptedException if thread sleep is interrupted
     */
    private static ArrayList<String> extractConnections(WebDriver driver, WebDriverWait wait, JavascriptExecutor jsExecutor) throws InterruptedException {
        ArrayList<String> connections = new ArrayList<>();
        String connectionsXPath = "//main/section[1]/div[2]/ul/li/a/span";

        driver.findElement(By.xpath(connectionsXPath)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("mn-connection-card")));

        scrollToBottom(driver, jsExecutor);

        List<WebElement> connectionElements = driver.findElements(By.className("mn-connection-card"));
        System.out.println("Total number of connections read: " + connectionElements.size());

        for (int i = 0; i < connectionElements.size(); i++) {
            String[] connectedUser = connectionElements.get(i).getText().split("\n");
            if (connectedUser.length == 7) {
                connections.add(connectedUser[2]);
                connections.add(connectedUser[4]);
                connections.add(connectedUser[5]);
            } else if (connectedUser.length == 6) {
                connections.add(connectedUser[1]);
                connections.add(connectedUser[3]);
                connections.add(connectedUser[4]);
            } else if (connectedUser.length == 5) {
                connections.add(connectedUser[1]);
                connections.add(connectedUser[3]);
            }
            System.out.println("Added connection number " + i);
        }

        return connections;
    }

    /**
     * Scrolls to the bottom of the page to load all connections.
     *
     * @param driver WebDriver instance
     * @param jse    JavascriptExecutor instance
     */
    private static void scrollToBottom(WebDriver driver, JavascriptExecutor jse) {

        long totalHeight = (long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");
        try {
            while (true) {
                // Scroll down by a fixed amount
                jse.executeScript("window.scrollTo(0, document.body.scrollHeight);");

                // Random sleep time between 1 and 1.5 seconds
                double sleepTime = ThreadLocalRandom.current().nextDouble(1, 1.5) * 1000;
                Thread.sleep((long) sleepTime);

                if (driver.findElement(By.xpath("/html/body/div[5]/div[3]/div/div/div/div/div[2]/div/div/main/div/section/div[2]/div[2]/div/button")).isDisplayed()) {
                    jse.executeScript("window.scrollBy(0,-50)"); // scroll up
                    jse.executeScript("window.scrollBy(0,50)"); // scroll down

                }

                // Get the new height of the page
                long newHeight = (long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");

                // Check if the height has changed
                if (newHeight == totalHeight) {
                    if (driver.findElement(By.xpath("/html/body/div[5]/div[3]/div/div/div/div/div[2]/div/div/main/div/section/div[2]/div[2]/div/button")).isDisplayed()) {
                        driver.findElement(By.xpath("/html/body/div[5]/div[3]/div/div/div/div/div[2]/div/div/main/div/section/div[2]/div[2]/div/button")).click();
                    } else {
                        break;
                    }
                }
                // Update the height for the next iteration
                totalHeight = newHeight;
            }
        } catch (Exception _) {
        }
    }

    /**
     * Writes JSON object to file.
     *
     * @param json JSON object to write
     * @throws IOException if an I/O error occurs
     */
    private static void writeToFile(JSONObject json) throws IOException {
        try (FileWriter fileWriter = new FileWriter("./Results.json", false)) {
            fileWriter.write(json.toJSONString());
        }
    }

}
