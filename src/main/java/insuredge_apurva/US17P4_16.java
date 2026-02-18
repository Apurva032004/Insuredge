package insuredge_apurva;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;

public class US17P4_16 {

    private WebDriver driver;
    private WebDriverWait wait;

    // Locators (kept very close to your original)
    private final By username = By.id("txtUsername");
    private final By password = By.name("txtPassword");

    private final By policyHolderMenuIcon = By.xpath("//*[@id='sidebar-nav']/li[5]/a/i[2]");
    private final By policyHolderParent = By.xpath("//*[@id='sidebar-nav']/li[5]/a");
    private final By rejectedPolicyHoldersMenu = By.xpath("//*[@id='policyHolder-nav']/li[4]/a/span");

    private final By titleRejected = By.xpath("//h1[normalize-space()='Rejected Policy Holders']");

    private final By breadcrumbDashboard =
            By.xpath("//nav//a[normalize-space()='Dashboard'] | //div[contains(@class,'breadcrumb')]//a[normalize-space()='Dashboard']");
    private final By breadcrumbCurrentRejected =
            By.xpath(
                    "//nav//*[normalize-space()='Rejected Policy Holders' and (self::span or self::li or self::a or self::div)] | " +
                    "//div[contains(@class,'breadcrumb')]//*[normalize-space()='Rejected Policy Holders']"
            );

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        // If chromedriver is not on PATH, set it via:
        // System.setProperty("webdriver.chrome.driver", "C:\\drivers\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();

        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    // --- Helpers ---
    private void jsHighlight(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String originalStyle = element.getAttribute("style");
        try {
            js.executeScript(
                    "arguments[0].setAttribute('style', arguments[1]);",
                    element,
                    "outline: 3px solid #ff5252; outline-offset: 2px; background: rgba(255,82,82,0.15); border-radius: 4px; " +
                            (originalStyle != null ? originalStyle : "")
            );
        } catch (Exception ignored) {}
    }

    private void clickPolicyHolderParentIfNeeded() {
        // Click the policy holder parent (expand/collapse); ensure child becomes visible
        try {
            wait.until(ExpectedConditions.elementToBeClickable(policyHolderParent)).click();
        } catch (Exception ignored) {}
    }

    // ==========================================================
    // TC1: Login and open Rejected Policy Holders page
    // ==========================================================
    @Test(priority = 1)
    public void tc01_loginAndOpenRejectedPage() {
        driver.get("https://qeaskillhub.cognizant.com/LoginPage");

        // Login
        wait.until(ExpectedConditions.visibilityOfElementLocated(username)).sendKeys("admin_user");
        driver.findElement(password).sendKeys("testadmin", Keys.ENTER);

        // Expand menu and click "Rejected Policy Holders"
        wait.until(ExpectedConditions.elementToBeClickable(policyHolderMenuIcon)).click();
        WebElement rejectedMenu = wait.until(ExpectedConditions.elementToBeClickable(rejectedPolicyHoldersMenu));
        rejectedMenu.click();

        // Expect the page title to be present
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(titleRejected));
        Assert.assertTrue(title.isDisplayed(), "TC1 FAIL: 'Rejected Policy Holders' title not visible after navigation.");
        System.out.println("TC01: Rejected Policy Holders' title not visible after navigation.");
    }

    // ==========================================================
    // TC2: Validate page title is exactly "Rejected Policy Holders"
    // ==========================================================
    @Test(priority = 2, dependsOnMethods = "tc01_loginAndOpenRejectedPage")
    public void tc02_verifyPageTitle() {
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(titleRejected));
        String actual = title.getText().trim();
        Assert.assertEquals(actual, "Rejected Policy Holders",
                "TC2 FAIL: Unexpected page title.");
        System.out.println("TC02:Validate page title is exactly \"Rejected Policy Holders .");
    }

    // ==========================================================
    // TC3: Validate breadcrumb text "Dashboard / Rejected Policy Holders"
    // ==========================================================
    @Test(priority = 3, dependsOnMethods = "tc02_verifyPageTitle")
    public void tc03_verifyBreadcrumb() {
        WebElement dash = wait.until(ExpectedConditions.visibilityOfElementLocated(breadcrumbDashboard));
        WebElement current = wait.until(ExpectedConditions.visibilityOfElementLocated(breadcrumbCurrentRejected));

        String crumb = (dash.getText().trim() + " / " + current.getText().trim()).replaceAll("\\s+", " ");
        Assert.assertEquals(crumb, "Dashboard / Rejected Policy Holders",
                "TC3 FAIL: Breadcrumb mismatch. Found: " + crumb);
        System.out.println("TC03: Validate breadcrumb text \"Dashboard / Rejected Policy Holders");
    }

    // ==========================================================
    // TC4: Click 'Dashboard' breadcrumb and verify navigation, then return
    // ==========================================================
    @Test(priority = 4, dependsOnMethods = "tc03_verifyBreadcrumb")
    public void tc04_breadcrumbDashboardNavigates() {
        WebElement dash = wait.until(ExpectedConditions.elementToBeClickable(breadcrumbDashboard));
        dash.click();

        // Consider URL or dashboard title present
        boolean onDashboard;
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/Dashboard"),
                    ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[normalize-space()='Dashboard']"))
            ));
            onDashboard = true;
        } catch (TimeoutException te) {
            onDashboard = false;
        }
        Assert.assertTrue(onDashboard, "TC4 FAIL: Clicking 'Dashboard' did not navigate to Dashboard.");

        // Navigate back to Rejected Policy Holders for the next test
        clickPolicyHolderParentIfNeeded();
        WebElement rejectedMenu = wait.until(ExpectedConditions.elementToBeClickable(rejectedPolicyHoldersMenu));
        rejectedMenu.click();

        // Ensure we're back
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(titleRejected));
        Assert.assertTrue(title.isDisplayed(), "TC4 FAIL: Did not return to Rejected Policy Holders page.");
        
        System.out.println("TC04: Click 'Dashboard' breadcrumb and verify navigation, then return");
    }

    // ==========================================================
    // TC5: Highlight 'Rejected Policy Holders' menu item and verify presence
    // ==========================================================
    @Test(priority = 5, dependsOnMethods = "tc04_breadcrumbDashboardNavigates")
    public void tc05_highlightLeftMenuItem() {
        // Make sure the parent is expanded and the item is visible
        clickPolicyHolderParentIfNeeded();
        WebElement rejectedMenuItem = wait.until(ExpectedConditions.visibilityOfElementLocated(rejectedPolicyHoldersMenu));
        Assert.assertTrue(rejectedMenuItem.isDisplayed(), "TC5 FAIL: 'Rejected Policy Holders' menu item not visible.");

        // Highlight (visual aid)
        jsHighlight(rejectedMenuItem);

        // Optional: assert that style got applied (best-effort)
        String style = rejectedMenuItem.getAttribute("style");
        Assert.assertTrue(style == null || style.contains("outline") || style.contains("background"),
                "TC5 WARN: Could not confirm highlight style on the element.");
        
        System.out.println("TC05: Highlight 'Rejected Policy Holders' menu item and verify presence");
        System.out.println();
        System.out.println("US17P4_16: PASSED");
        System.out.println();
    }
}