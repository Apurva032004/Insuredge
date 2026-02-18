package insuredge_apurva;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;

public class US17P4_25 {

    WebDriver driver;
    JavascriptExecutor js;
    Actions actions;
    WebDriverWait wait;

    String tableId = "ContentPlaceHolder_Admin_gvRejectedHolders";

    @BeforeClass
    public void setup() {

        driver = new ChromeDriver();
        driver.manage().window().maximize();
        js = (JavascriptExecutor) driver;
        actions = new Actions(driver);
        wait = new WebDriverWait(driver, Duration.ofSeconds(12));

        // LOGIN
        driver.get("https://qeaskillhub.cognizant.com/LoginPage");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("txtUsername")))
            .sendKeys("admin_user");
        driver.findElement(By.name("txtPassword")).sendKeys("testadmin", Keys.ENTER);

        // Wait for sidebar as post-login signal
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("sidebar-nav")));

        // Navigate → Policy Holder's → Rejected Policy Holders
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='sidebar-nav']/li[5]/a/i[2]"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='policyHolder-nav']/li[4]/a/span"))).click();

        // Ensure table is present and visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table[@id='" + tableId + "']")
        ));

        // scroll down so table is visible
        js.executeScript("window.scrollBy(0,300)");

        // --- MUST: bring STATUS column into view (right scroll alternative) ---
        WebElement statusHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//th[normalize-space()='Status']")));
        js.executeScript(
            "arguments[0].scrollIntoView({behavior:'instant', block:'nearest', inline:'end'})",
            statusHeader
        );
        // Re-assert to ensure it’s in view before tests proceed
        wait.until(ExpectedConditions.visibilityOf(statusHeader));
    }

    // ================================
    // TC1: Status cannot be edited by mouse click
    // ================================
    @Test
    public void tc1_statusNotEditableOnClick() {
        WebElement cell = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table[@id='" + tableId + "']/tbody/tr[2]/td[11]")));

        String before = cell.getText().trim();
        cell.click();

        // Wait for stability (no change expected)
        wait.until(ExpectedConditions.textToBePresentInElement(cell, before));
        String after = cell.getText().trim();

        Assert.assertEquals(after, before, "Status changed after click!");
        System.out.println("TC01 PASS: Status cannot be edited by mouse click.");
    }

    // ================================
    // TC2: Status does not allow Copy/Paste
    // ================================
    @Test(dependsOnMethods = "tc1_statusNotEditableOnClick")
    public void tc2_statusNoCopyPaste() {
        WebElement cell = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table[@id='" + tableId + "']/tbody/tr[2]/td[11]")));

        String before = cell.getText().trim();
        actions.click(cell)
               .keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL)
               .keyDown(Keys.CONTROL).sendKeys("v").keyUp(Keys.CONTROL)
               .perform();

        // Wait expecting no change
        wait.until(ExpectedConditions.textToBePresentInElement(cell, before));

        String after = cell.getText().trim();
        Assert.assertEquals(after, before, "Status changed after copy/paste!");
        System.out.println("TC02 PASS: Status does not accept copy/paste.");
    }

    // ================================
    // TC3: Typing keys should NOT update Status
    // ================================
    @Test(dependsOnMethods = "tc2_statusNoCopyPaste")
    public void tc3_statusDoesNotChangeOnTyping() {
        WebElement cell = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table[@id='" + tableId + "']/tbody/tr[2]/td[11]")));

        String before = cell.getText().trim();

        actions.click(cell).sendKeys("APPROVED123").perform();

        // Wait expecting no change
        wait.until(ExpectedConditions.textToBePresentInElement(cell, before));

        String after = cell.getText().trim();
        Assert.assertEquals(after, before, "Status changed on typing!");
        System.out.println("TC03 PASS: Status does not change on typing.");
    }

    // ================================
    // TC4: Status must remain “Rejected”
    // ================================
    @Test(dependsOnMethods = "tc3_statusDoesNotChangeOnTyping")
    public void tc4_statusAlwaysRejected() {
        WebElement cell = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table[@id='" + tableId + "']/tbody/tr[2]/td[11]")));

        Assert.assertEquals(cell.getText().trim(), "Rejected", "Status is not 'Rejected'!");
        System.out.println("TC04 PASS: Status always remains 'Rejected'.");
    }

    // ================================
    // TC5: Hover does not change Status
    // ================================
    @Test(dependsOnMethods = "tc4_statusAlwaysRejected")
    public void tc5_statusDoesNotChangeOnHover() {
        WebElement cell = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table[@id='" + tableId + "']/tbody/tr[2]/td[11]")));

        String before = cell.getText().trim();
        actions.moveToElement(cell).pause(Duration.ofMillis(500)).perform();

        // No change expected after hover
        wait.until(ExpectedConditions.textToBePresentInElement(cell, before));
        String after = cell.getText().trim();

        Assert.assertEquals(after, before, "Status changed on hover!");
        System.out.println("TC05 PASS: Hover does not change Status.");
    }

    // ================================
    // TC6: No UI option to change Status icon/menu/button
    // ================================
    @Test(dependsOnMethods = "tc5_statusDoesNotChangeOnHover")
    public void tc6_noUiToChangeStatus() {

        // Check header for any icon/button
        int headerControls = driver.findElements(
                By.xpath("//th[normalize-space()='Status']//button | //th[normalize-space()='Status']//a | //th[normalize-space()='Status']//i")
        ).size();

        // Check cell for any UI element (edit icon/link)
        int cellControls = driver.findElements(
                By.xpath("//table[@id='" + tableId + "']//tbody/tr[2]/td[11]//a | //table[@id='" + tableId + "']//tbody/tr[2]/td[11]//button")
        ).size();

        Assert.assertEquals(headerControls, 0, "Editable UI element found in header!");
        Assert.assertEquals(cellControls, 0, "Editable UI element found in Status cell!");

        System.out.println("TC06 PASS: No UI element allows changing Status.");
        System.out.println();
        System.out.println("US17P4_25: PASSED");
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}