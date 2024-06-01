import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


public class FETest {
    private static final Scanner scanner = new Scanner(System.in);
    public static WebDriver driver = new ChromeDriver();
    public static ObjectMapper mapper = new ObjectMapper();
    public static WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    public static JavascriptExecutor jse = (JavascriptExecutor) driver;
    public static JSONObject json = new JSONObject();
    public static JSONArray jsonArray = new JSONArray();


    public static void main(String[] args) {
        runFrontendTest();
    }

    /**
     * Runs the frontend test.
     */
    private static void runFrontendTest() {
        try {
            LoginInfo loginInfo = getLoginInfo();
            String USERNAME = loginInfo.getUsername();
            String PASSWORD = loginInfo.getPassword();
            String URL = loginInfo.getUrl();

            loginToLinkedin(USERNAME, PASSWORD, URL);
            if (!isOnLinkedInFeedPage()) {  // check if there is a verification page
                waitForUserVerification();
            }
            extractProfileInfo();
            writeToFile();

            ArrayList<String> connections = extractConnections();
            jsonArray.addAll(connections);
            json.put("connections", jsonArray);

            writeToFile();
        } catch (IOException | InterruptedException | WebDriverException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Exiting browser...");
            driver.quit();
        }
    }

    /**
     * Gets login information from JSON file.
     */
    private static LoginInfo getLoginInfo() throws IOException {
        JsonNode node = mapper.readTree(new File("./login_info.json"));
        String username = node.get("username").asText();
        String password = node.get("password").asText();
        String url = node.get("url").asText();
        return new LoginInfo(username, password, url);
    }

    /**
     * Logs into LinkedIn using provided credentials.
     */
    private static void loginToLinkedin(String USERNAME, String PASSWORD, String LINKEDIN_URL) {
        driver.get(LINKEDIN_URL);
        driver.findElement(By.className("nav__button-secondary")).click();
        driver.findElement(By.id("username")).sendKeys(USERNAME);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.className("btn__primary--large")).click();
        if (!driver.findElements(By.id("error-for-username")).isEmpty()) { // check if login failed
            retryLogin();
        }
    }

    private static boolean isOnLinkedInFeedPage() {
        return driver.getTitle().contains("Feed | LinkedIn");
    }

    /**
     * Retries login if invalid credentials are provided.
     */
    private static void retryLogin() {
        try {
            while (!driver.findElements(By.id("error-for-username")).isEmpty()) {
                System.out.println("Invalid username or password.\nPlease enter correct username: ");
                String newUsername = scanner.nextLine();
                System.out.println("Please enter correct password: ");
                String newPassword = scanner.nextLine();
                driver.findElement(By.id("username")).clear();
                driver.findElement(By.id("username")).sendKeys(newUsername);
                driver.findElement(By.id("password")).clear();
                driver.findElement(By.id("password")).sendKeys(newPassword);
                driver.findElement(By.className("btn__primary--large")).click();
                try {
                    Thread.sleep(1000); // wait for login to process
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Waits for user to verify the LinkedIn security page.
     */
    private static void waitForUserVerification() {
        System.out.println("Waiting for user verification...");
        scanner.nextLine();
        System.out.println("User verified.");
    }

    /**
     * Extract profile information from LinkedIn.
     */
    private static void extractProfileInfo() {
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

        json.put("myName", profileName);
        json.put("myWorkplace", profileJob);
        json.put("city", profileLocation);

        System.out.println("Profile information added.");
    }

    /**
     * Extracts connections from LinkedIn profile.
     *
     * @return List of connections
     * @throws InterruptedException if thread sleep is interrupted
     */
    private static ArrayList<String> extractConnections() throws InterruptedException {
        ArrayList<String> connections = new ArrayList<>();
        String connectionsXPath = "//main/section[1]/div[2]/ul/li/a/span";

        driver.findElement(By.xpath(connectionsXPath)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("mn-connection-card")));

        scrollToBottom();

        List<WebElement> connectionElements = driver.findElements(By.className("mn-connection-card"));
        System.out.println("Total number of connections read: " + connectionElements.size() + "\nSaving connections...");

        int i;
        for (i = 0; i < connectionElements.size(); i++) {
            String[] connectedUser = connectionElements.get(i).getText().split("\n");
            switch (connectedUser.length) { // Some lines have more information than others, the switch statement handles that
                case 5:
                    addConnections(connections, connectedUser, 1, 3);
                    break;
                case 6:
                    addConnections(connections, connectedUser, 1, 3, 4);
                    break;
                case 7:
                    addConnections(connections, connectedUser, 2, 4, 5);
                    break;
                default:
                    break;
            }
        }
        System.out.println("Total number of connections saved: " + i);
        return connections;
    }

    /**
     * Adds connections to the list according to the indices provided.
     *
     * @param connections   List of connections
     * @param connectedUser Array of connected user information
     * @param indices       Indices of the connected user information to add
     */
    private static void addConnections(ArrayList<String> connections, String[] connectedUser, int... indices) {
        for (int idx : indices) {
            connections.add(connectedUser[idx]);
        }
    }

    /**
     * Scrolls to the bottom of the page to load all connections.
     *
     * @throws NoSuchElementException if no such element is found
     * @throws InterruptedException   if thread sleep is interrupted
     */
    private static void scrollToBottom() {
        String loadMoreButtonXPath = "/html/body/div[5]/div[3]/div/div/div/div/div[2]/div/div/main/div/section/div[2]/div[2]/div/button";
        long totalHeight = (long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");
        try {
            while (true) {
                // Scroll down by a fixed amount
                jse.executeScript("window.scrollTo(0, document.body.scrollHeight);");

                // Random sleep time between 1 and 1.5 seconds to let the page load more connections
                double sleepTime = ThreadLocalRandom.current().nextDouble(1, 1.5) * 1000;
                Thread.sleep((long) sleepTime);

//                if (driver.findElement(By.xpath(loadMoreButtonXPath)).isDisplayed()) {
                /*
                 * for some reason, when clicking "load more connections" it doesn't load more,
                 * scrolling up and down seems to overcome this issue
                 */
                jse.executeScript("window.scrollBy(0,-50)"); // scroll up
                jse.executeScript("window.scrollBy(0,50)"); // scroll down

//                }

                // Get the new height of the page
                long newHeight = (long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");

                // Check if the height has changed
                if (newHeight == totalHeight) {
                    try {
                        if (driver.findElement(By.xpath(loadMoreButtonXPath)).isDisplayed()) {
                            driver.findElement(By.xpath(loadMoreButtonXPath)).click();
                        } else {
                            break;
                        }
                    } catch (NoSuchElementException e) {
                        System.out.println("No more connections to load.");
                        break;
                    }
                }
                // Update the height for the next iteration
                totalHeight = newHeight;
            }
        } catch (NoSuchElementException e) {
            System.err.println("No such element found: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    /**
     * Writes JSON object to file.
     *
     * @throws IOException if an I/O error occurs
     */
    private static void writeToFile() throws IOException {
        try (FileWriter fileWriter = new FileWriter("./Results.json", false)) {
            fileWriter.write(json.toString());
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

}