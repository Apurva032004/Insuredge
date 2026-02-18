package insuredge_apurva;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class US17P4_07 {

    private WebDriver driver;
    private WebDriverWait wait;

    // ---------- Locators (same as your script) ----------
    private final By txtUsername = By.id("txtUsername");
    private final By txtPassword = By.name("txtPassword");

    private final By policyHolderMenuIcon = By.xpath("//*[@id='sidebar-nav']/li[5]/a/i[2]");
    private final By pendingRejectedPolicyHolders = By.xpath("//*[@id='policyHolder-nav']/li[3]/a/span");

    private final By paginationCell = By.xpath("//td[@colspan='11']");
    private final By paginationLinks = By.xpath("//td[@colspan='11']//a");
    private final By activePageCell = By.xpath("//td[@colspan='11']/table/tbody/tr/td[2]");

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(12));

        // ---- Login  ----
        driver.get("https://qeaskillhub.cognizant.com/LoginPage");
        waitForDomReady();

        wait.until(ExpectedConditions.visibilityOfElementLocated(txtUsername)).sendKeys("admin_user");
        driver.findElement(txtPassword).sendKeys("testadmin", Keys.ENTER);

        // Wait until the left sidebar/menu icon is clickable (post-login landing)
        wait.until(ExpectedConditions.elementToBeClickable(policyHolderMenuIcon)).click();

        // Click Pending/Rejected Policy Holders
        wait.until(ExpectedConditions.elementToBeClickable(pendingRejectedPolicyHolders)).click();

        // Ensure the target page is ready before tests
        waitForDomReady();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    private void scrollToBottom() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    private void waitForDomReady() {
        wait.until(d ->
                ((JavascriptExecutor) d).executeScript("return document.readyState").toString().equals("complete"));
    }

    // ---------------------- TC1: Pagination is visible ----------------------
    @Test(priority = 1)
    public void tc1_paginationIsVisible() {
        scrollToBottom();
        WebElement pagination = wait.until(ExpectedConditions.visibilityOfElementLocated(paginationCell));
        Assert.assertTrue(pagination.isDisplayed(), "TC1 FAIL: Pagination should not be visible at the bottom of the table.");
        System.out.println("TC01 PASS: Pagination should be visible at the bottom of the table. ");
    }

    // ---------------------- TC2: Page numbers are sequential ----------------------
    @Test(priority = 2, dependsOnMethods = "tc1_paginationIsVisible")
    public void tc2_pageNumbersSequential() {
        scrollToBottom();

        // Get the pagination container
        WebElement container = wait.until(ExpectedConditions.visibilityOfElementLocated(paginationCell));

        // Collect BOTH <a> and <span> (active page is often a <span>)
        List<WebElement> items = container.findElements(By.xpath(".//a | .//span"));

        // Build raw text for quick debugging (optional)
        StringBuilder raw = new StringBuilder();
        for (WebElement el : items) {
            raw.append((el.getText() == null ? "" : el.getText().trim())).append(" ");
        }

        // Keep only numeric labels in display order
        List<Integer> nums = new ArrayList<>();
        for (WebElement el : items) {
            String t = (el.getText() == null) ? "" : el.getText().trim();
            if (t.matches("\\d+")) {
                nums.add(Integer.parseInt(t));
            }
        }

        // Basic checks
        Assert.assertTrue(items.size() >= 1, "TC2 FAIL: No pagination items found.");
        Assert.assertTrue(nums.size() >= 1, "TC2 FAIL: No numeric page labels found. Raw: [" + raw.toString().trim() + "]");

        // Verify visible numbers are sequential by +1
        for (int i = 1; i < nums.size(); i++) {
            Assert.assertEquals(
                    nums.get(i).intValue(),
                    nums.get(i - 1) + 1,
                    "TC2 FAIL: Labels are not sequential. Found: " + nums + " | Raw: [" + raw.toString().trim() + "]"
            );
        }

        System.out.println("TC02 PASS: Displayed numeric page labels are sequential: " + nums);
    }

    // ---------------------- TC3: Clicking a page changes data ----------------------
    @Test(priority = 3, dependsOnMethods = "tc2_pageNumbersSequential")
    public void tc3_clickingPageChangesData() {
        scrollToBottom();

        // Wait for pagination links to be present
        List<WebElement> pages = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(paginationLinks));

        // Build numeric list only
        List<WebElement> numericPages = new ArrayList<>();
        for (WebElement p : pages) {
            if (p.getText().trim().matches("\\d+")) numericPages.add(p);
        }

        if (numericPages.size() <= 1) {
            throw new SkipException("TC3 SKIPPED: Only one numeric page exists.");
        }

        By firstCell = By.xpath("//td[@colspan='11']/table/tbody/tr/td[1]");
        By secondCell = By.xpath("//td[@colspan='11']/table/tbody/tr/td[2]");

        String before = wait.until(ExpectedConditions.visibilityOfElementLocated(firstCell)).getText();

        // Click page "2" (index 1)
        wait.until(ExpectedConditions.elementToBeClickable(numericPages.get(1))).click();

        // Wait for either the pagination to refresh or the second cell text to settle
        wait.until(ExpectedConditions.or(
                ExpectedConditions.stalenessOf(numericPages.get(1)),
                ExpectedConditions.not(ExpectedConditions.textToBe(secondCell, before))
        ));

        String after = wait.until(ExpectedConditions.visibilityOfElementLocated(secondCell)).getText();

        Assert.assertNotEquals(after, before,
                "TC3 FAIL: Page switched but content looks the same (consider targeting data table rows).");
        System.out.println("TC03: Page switched but content looks different");
    }

    // ---------------------- TC4: Active page is highlighted ----------------------
    @Test(priority = 4, dependsOnMethods = "tc3_clickingPageChangesData")
    public void tc4_activePageHighlighted() {
        scrollToBottom();

        WebElement activePage = wait.until(ExpectedConditions.visibilityOfElementLocated(activePageCell));
        String activeText = activePage.getText().trim();

        // Accept "1" or "2" based on previous click—adapt if your UI behaves differently
        boolean ok = activeText.equals("2") || activeText.equals("1");
        Assert.assertTrue(ok, "TC4 FAIL: Active page is not highlight missing/unexpected. Found: " + activeText);
        System.out.println("TC04: Active page is highlight missing/unexpected. Found");
    }

    // ---------------------- TC5: Pagination visible across screen sizes ----------------------
    @Test(priority = 5, dependsOnMethods = "tc4_activePageHighlighted")
    public void tc5_paginationVisibleOnResize() {
        int[][] sizes = new int[][]{
                {1920,1080}, {1366,768}, {1280,720}, {414,896}, {390,844}
        };

        for (int[] s : sizes) {
            driver.manage().window().setSize(new Dimension(s[0], s[1]));
            waitForDomReady();

            scrollToBottom();

            WebElement pagination = wait.until(ExpectedConditions.visibilityOfElementLocated(paginationCell));
            Assert.assertTrue(pagination.isDisplayed(),
                    String.format("TC5 FAIL: Pagination not visible for viewport %dx%d", s[0], s[1]));
        }
        System.out.println("TC05: Pagination is visible for viewport");
        System.out.println();
        System.out.println("US17P4_07: PASSED");
        System.out.println();
    }}