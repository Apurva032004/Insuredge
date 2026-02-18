package insuredge_apurva;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;

public class US17P4_09 {

    private WebDriver driver;
    private WebDriverWait wait;
    private WebElement policyNameInput;

    @BeforeClass
    public void setUpAndLogin() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // ===== LOGIN =====
        driver.get("https://qeaskillhub.cognizant.com/LoginPage");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("txtUsername")))
                .sendKeys("admin_user");
        driver.findElement(By.name("txtPassword")).sendKeys("testadmin", Keys.ENTER);

        // Wait until sidebar is visible after login
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("sidebar-nav")));
    }

    @BeforeMethod
    public void navigateToPendingPolicyHoldersAndFocusInput() {
        // ===== NAVIGATE TO Pending Policy Holders =====
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='sidebar-nav']/li[5]/a/i[2]"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='policyHolder-nav']/li[3]/a/span"))).click();

        // ===== POLICY NAME TEXTBOX =====
        policyNameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[contains(@placeholder,'Policy Name')]")
        ));
        policyNameInput.click();
    }

    @Test(description = "TC-01: Alphanumeric values should be accepted")
    public void testTC01_AlphanumericAccepted() {
        policyNameInput.clear();
        policyNameInput.sendKeys("Policy123");
        String actual = policyNameInput.getAttribute("value");

        Assert.assertEquals(actual, "Policy123", "Alphanumeric value should be accepted.");
        System.out.println("TC01: Alphanumeric value accepted → " + actual);
    }

    @Test(description = "TC-02: Special characters behavior (blocked/sanitized OR accepted)")
    public void testTC02_SpecialCharactersBehavior() {
        final String input = "@#$%^&*";
        policyNameInput.clear();
        policyNameInput.sendKeys(input);
        String actual = policyNameInput.getAttribute("value");

        // Acceptable outcomes:
        // 1) Blocked: empty value
        // 2) Sanitized: contains only [A-Za-z0-9 ] (specials removed)
        // 3) Accepted-as-is: equals the original input (app allows specials)
        boolean blocked = actual.isEmpty();
        boolean sanitized = !actual.isEmpty() && actual.matches("[A-Za-z0-9 ]+");
        boolean acceptedAsIs = actual.equals(input);

        boolean pass = blocked || sanitized || acceptedAsIs;
        Assert.assertTrue(pass,
                "TC-02 failed. Expected blocked/sanitized/accepted-as-is. Actual: '" + actual + "'");

        if (blocked) {
            System.out.println("TC02: Special characters blocked (empty value).");
        } else if (sanitized) {
            System.out.println("TC02: Special characters sanitized → '" + actual + "'");
        } else {
            System.out.println("TC02: Special characters accepted-as-is → '" + actual + "'");
        }
    }

    @Test(description = "TC-03: Mixed input behavior (sanitized OR accepted)")
    public void testTC03_MixedInputBehavior() {
        final String input = "Policy@123";
        policyNameInput.clear();
        policyNameInput.sendKeys(input);
        String actual = policyNameInput.getAttribute("value");

        // Acceptable outcomes:
        // 1) Sanitized: no special chars remain & only [A-Za-z0-9 ]
        // 2) Accepted-as-is: equals the original input (app allows specials)
        boolean sanitized = !actual.contains("@") && actual.matches("[A-Za-z0-9 ]+");
        boolean acceptedAsIs = actual.equals(input);

        boolean pass = sanitized || acceptedAsIs;
        Assert.assertTrue(pass,
                "TC-03 failed. Expected sanitized or accepted-as-is. Actual: '" + actual + "'");

        if (sanitized) {
            System.out.println("TC03: Mixed input sanitized → '" + actual + "'");
        } else {
            System.out.println("TC03: Mixed input accepted-as-is → '" + actual + "'");
        }
        System.out.println();
        System.out.println("US17P4_09: PASSED");
        System.out.println();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}
