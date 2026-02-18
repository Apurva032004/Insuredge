package insuredge_apurva;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class US17P4_18 {

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    // Exact headers to validate (text must match UI exactly)
    private static final List<String> headers = Arrays.asList(
            "Customer Name",
            "Mobile Number",
            "Email",
            "Policy Name",
            "Main Category",
            "Sub Category",
            "Sum Assured",
            "Premium",
            "Tenure",
            "Applied On",
            "Status"
    );

    // Common locators / XPaths
    private static final String table_id = "ContentPlaceHolder_Admin_gvRejectedHolders";
    private static final By username = By.id("txtUsername");
    private static final By password = By.name("txtPassword");
    private static final By sidebar = By.id("sidebar-nav");
    private static final By menu_icon = By.xpath("//*[@id='sidebar-nav']/li[5]/a/i[2]");
    private static final By rejected_menu = By.xpath("//*[@id='policyHolder-nav']/li[4]/a/span");
    private static final By rejected_title = By.xpath("//h1[normalize-space()='Rejected Policy Holders']");
    private static final By table_row = By.xpath("//table[@id='" + table_id + "']//tr[1]");

    @BeforeClass(alwaysRun = true)
    public void setup() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(12));
        js = (JavascriptExecutor) driver;

        // ===== LOGIN =====
        driver.get("https://qeaskillhub.cognizant.com/LoginPage");
        wait.until(ExpectedConditions.visibilityOfElementLocated(username)).sendKeys("admin_user");
        driver.findElement(password).sendKeys("testadmin", Keys.ENTER);
        wait.until(ExpectedConditions.visibilityOfElementLocated(sidebar));

        // ===== NAVIGATE: Policy Holder's → Rejected Policy Holders =====
        wait.until(ExpectedConditions.elementToBeClickable(menu_icon)).click();
        wait.until(ExpectedConditions.elementToBeClickable(rejected_menu)).click();

        // Ensure page and table are ready
        wait.until(ExpectedConditions.visibilityOfElementLocated(rejected_title));
        wait.until(ExpectedConditions.presenceOfElementLocated(table_row));
    }

    @Test(priority = 1, description = "T01: Scroll RIGHT to reveal the 'Status' header (no vertical scroll)")
    public void T01_scrollRightToStatus() {
        // Prefer an XPath that matches either <th> or <td> in row 1 with text 'Status' — no try/catch needed
        By statusHeaderBy = By.xpath("//table[@id='" + table_id + "']//tr[1]/*[self::th or self::td][normalize-space()='Status']");

        WebElement statusHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(statusHeaderBy));

        // Horizontal-only reveal (keeps vertical position)
        js.executeScript(
                "arguments[0].scrollIntoView({behavior:'instant', block:'nearest', inline:'end'})",
                statusHeader
        );

        // Re-assert visible (in case of sticky headers / transforms)
        Assert.assertTrue(statusHeader.isDisplayed(), "'Status' header is not visible after horizontal scroll.");

        System.out.println("T01 PASS: Scrolled right and all the column is visible.");
    }

    @Test(priority = 2, dependsOnMethods = "T01_scrollRightToStatus",
          description = "T02: Verify all specified header names are present")
    public void T02_allSpecifiedHeadersPresent() {
        for (String headerText : headers) {
            By headerBy = By.xpath("//table[@id='" + table_id + "']//tr[1]/*[self::th or self::td][normalize-space()='" + headerText + "']");
            WebElement headerEl = wait.until(ExpectedConditions.visibilityOfElementLocated(headerBy));
            Assert.assertTrue(headerEl.isDisplayed(), "Header not displayed: " + headerText);
        }
        System.out.println("T02 PASS: All specified header names are present.");
    }

    @Test(priority = 3, dependsOnMethods = "T02_allSpecifiedHeadersPresent",
          description = "T03: Verify all specified header names are BOLD (font-weight >= 700 or 'bold')")
    public void T03_allSpecifiedHeadersAreBold() {
        boolean allBold = true;
        StringBuilder nonBoldList = new StringBuilder();

        for (String headerText : headers) {
            By headerBy = By.xpath("//table[@id='" + table_id + "']//tr[1]/*[self::th or self::td][normalize-space()='" + headerText + "']");
            WebElement headerEl = wait.until(ExpectedConditions.visibilityOfElementLocated(headerBy));

            String fw = headerEl.getCssValue("font-weight"); // e.g., "700" or "bold"
            boolean isBold;

            // No try/catch: handle numeric vs keyword paths branch-wise
            String digits = fw.replaceAll("\\D", "");
            if (!digits.isEmpty()) {
                int numeric = Integer.parseInt(digits);
                isBold = numeric >= 700;
            } else {
                isBold = "bold".equalsIgnoreCase(fw);
            }

            if (!isBold) {
                allBold = false;
                nonBoldList.append("Header '").append(headerText).append("' font-weight=").append(fw).append("\n");
            }
        }

        Assert.assertTrue(allBold, "Some headers are not bold:\n" + nonBoldList);
        System.out.println("T03 PASS: All specified headers are bold (font-weight >= 700 or 'bold').");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        // Final user story pass message
    	 System.out.println();
        System.out.println("US17P4_18 IS PASSED");
        System.out.println();
        if (driver != null) driver.quit();
    }
}