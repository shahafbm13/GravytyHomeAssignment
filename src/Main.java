import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;





public class Main {
    public static void main(String[] args) {

        String USERNAME="shahafbenmoshe@gmail.com";
        String PASSWORD="13111997";

        WebDriver driver = new ChromeDriver();
        driver.get("https://www.linkedin.com/");
        driver.findElement(By.className("nav__button-secondary")).click(); // click sign in button
        driver.findElement(By.id("username")).sendKeys(USERNAME);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.className("btn__primary--large")).click(); // click credential enter button
        driver.findElement(By.className("global-nav__me")).click(); // open "Me" menu
        driver.findElement(By.id("ember1492")).click();
//        driver.findElement(By.xpath("/html/body/div[5]/header/div/nav/ul/li[6]/div/div/div/header/a[2]")).click();
//        driver.findElement(By.className("ember-view")).click(); // click on "Sign out" button
    }
}