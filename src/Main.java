import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.simple.JSONArray;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        String USERNAME = "shahafbenmoshe@gmail.com";
        String PASSWORD = "13111997";
        Scanner scanner = new Scanner(System.in);


        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayList<String> people = new ArrayList<>();
        JSONObject json = new JSONObject();

        FileWriter f = new FileWriter("./Results.json", false);

        driver.get("https://www.linkedin.com/");
        driver.findElement(By.className("nav__button-secondary")).click(); // click sign in button
        driver.findElement(By.id("username")).sendKeys(USERNAME);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.className("btn__primary--large")).click(); // click credential enter button

        System.out.println("Waiting on verification");
        String input = scanner.nextLine();

        String viewProfilePath = "/html/body/div[5]/header/div/nav/ul/li[6]/div/div/div/header/a[2]";
        String connectionsPath = "/html/body/div[5]/div[3]/div/div/div[2]/div/div/main/section[1]/div[2]/ul/li/a/span";



        driver.findElement(By.className("global-nav__me")).click(); // open "Me" menu
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(viewProfilePath))); // wait for "View Button" to appear
        driver.findElement(By.xpath(viewProfilePath)).click(); // click "View Profile" button

        String profileName=driver.findElement(By.xpath("/html/body/div[5]/div[3]/div/div/div[2]/div/div/main/section[1]/div[2]/div[2]/div[1]/div[1]/span/a/h1")).getText();
        String profileJob = driver.findElement(By.xpath("/html/body/div[5]/div[3]/div/div/div[2]/div/div/main/section[1]/div[2]/div[2]/div[1]/div[2]")).getText();
        String profileLocation = driver.findElement(By.xpath("/html/body/div[5]/div[3]/div/div/div[2]/div/div/main/section[1]/div[2]/div[2]/div[2]/span[1]")).getText();

        json.put("myName", profileName);
        json.put("myWorkplace", profileJob);
        json.put("city", profileLocation);

        System.out.println("profile name: " + profileName + "\nprofile job: " + profileJob + "\nprofile location: " + profileLocation);

        driver.findElement(By.xpath(connectionsPath)).click(); // click "Connections" button

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("mn-connection-card"))); // wait for connections to load

        scrollToBottom(driver);

        System.out.println("Reached the bottom of the page, extracting connections...");

        List<WebElement> connections = driver.findElements(By.className("mn-connection-card"));
        System.out.println("Total number of connections read: " + connections.size());


        for (int i=0; i<connections.size(); i++) {
            String[] lines = connections.get(i).getText().split("\n");
            people.add(lines[1]);
            people.add(lines[3]);
            people.add(lines[4]);
            System.out.println("added connection number " + i);
        }
        JSONArray jsonArray = new JSONArray();
        for (String person : people) {
            jsonArray.add(person);
        }


        json.put("connections", jsonArray);
        f.write(json.toJSONString());
        f.close();

    }

    public static int scrollToBottom(WebDriver driver) {
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        long totalHeight = (long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");
        try {
            while (true) {
                // Scroll down by a fixed amount
                jse.executeScript("window.scrollTo(0, document.body.scrollHeight);");

                // Random sleep time between 1 and 1.5 seconds
                double sleepTime = ThreadLocalRandom.current().nextDouble(1, 1.5) * 1000;
                Thread.sleep((long) sleepTime);

                if (driver.findElement(By.xpath("/html/body/div[5]/div[3]/div/div/div/div/div[2]/div/div/main/div/section/div[2]/div[2]/div/button")).isDisplayed()){
                    jse.executeScript("window.scrollBy(0,-50)"); // scroll up
                    jse.executeScript("window.scrollBy(0,50)"); // scroll down

                }

                // Get the new height of the page
                long newHeight = (long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");

                // Check if the height has changed
                if (newHeight == totalHeight) {
                    if (driver.findElement(By.xpath("/html/body/div[5]/div[3]/div/div/div/div/div[2]/div/div/main/div/section/div[2]/div[2]/div/button")).isDisplayed()) {
                        driver.findElement(By.xpath("/html/body/div[5]/div[3]/div/div/div/div/div[2]/div/div/main/div/section/div[2]/div[2]/div/button")).click();
                    }else{
                        break;
                    }
                }
                // Update the height for the next iteration
                totalHeight = newHeight;
            }
        } catch (Exception e) {
            return 1;
        }
        return 0;
    }
}
