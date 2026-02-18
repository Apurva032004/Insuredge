package insuredge_apurva;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.List;

public class US17P4_20 {

    WebDriver driver;
    WebDriverWait wait;

    @BeforeClass
    public void setup() {

        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(12));

        // LOGIN
        driver.get("https://qeaskillhub.cognizant.com/LoginPage");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("txtUsername")))
                .sendKeys("admin_user");
        driver.findElement(By.name("txtPassword"))
                .sendKeys("testadmin", Keys.ENTER);

        // Wait for sidebar as a post-login signal
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("sidebar-nav")));

        // NAVIGATE TO REJECTED POLICY HOLDERS
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='sidebar-nav']/li[5]/a/i[2]"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='policyHolder-nav']/li[4]/a/span"))).click();

        // Ensure table is present before tests
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//table[@id='ContentPlaceHolder_Admin_gvRejectedHolders']")
        ));

        // Scroll down to pagination area (kept same behavior)
        ((JavascriptExecutor) driver).executeScript("window.scroll(0,600);");
    }

    // ---------------- TC1 ----------------
    @Test(priority = 1)
    public void testPaginationControlDisplayed() {

        WebElement pagination = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table[@id='ContentPlaceHolder_Admin_gvRejectedHolders']/tbody/tr[12]")
        ));

        Assert.assertTrue(pagination.isDisplayed(),
                "Pagination row is NOT displayed!");

        System.out.println("TC01 : Pagination control is visible.");
    }

    // ---------------- TC2 ----------------
    @Test(priority = 2, dependsOnMethods = "testPaginationControlDisplayed")
    public void testPageNumbersDisplayed() {

        List<WebElement> pages = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.xpath("//td[@colspan='11']/table/tbody/tr")
        ));

        Assert.assertTrue(pages.size() > 0,
                "Page number controls NOT found!");

        System.out.println("Page numbers:");
        for (WebElement p : pages) {
            System.out.println(" > " + p.getText().trim());
        }

        WebElement activePage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//td[@colspan='11']/table/tbody/tr/td[1]")
        ));

        Assert.assertTrue(activePage.isDisplayed(),
                "Active page highlight is missing!");

        System.out.println("TC02: Page numbers + active highlight detected.");
    }

    // ---------------- TC3 ----------------
    @Test(priority = 3, dependsOnMethods = "testPageNumbersDisplayed")
    public void testPageNavigation() {

        // First row before switching page
        By firstRowCellBy = By.xpath("//table[@id='ContentPlaceHolder_Admin_gvRejectedHolders']/tbody/tr[2]/td[1]");
        String before = wait.until(ExpectedConditions.visibilityOfElementLocated(firstRowCellBy)).getText();

        System.out.println("Before switching page → " + before);

        // Click page 2 (NO try-catch!)
        WebElement page2 = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//td[@colspan='11']/table/tbody/tr/td[2]")
        ));
        page2.click();

        // Wait until the first row cell text changes
        wait.until(ExpectedConditions.not(ExpectedConditions.textToBe(firstRowCellBy, before)));

        // First row after switching page
        String after = wait.until(ExpectedConditions.visibilityOfElementLocated(firstRowCellBy)).getText();

        System.out.println("After switching page → " + after);

        Assert.assertNotEquals(after, before,
                "Data did NOT change after clicking page 2!");

        System.out.println("TC03: Page Number is displayed.");
        System.out.println();
        System.out.println("US17P4_20: PASSED");
        System.out.println();
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}
