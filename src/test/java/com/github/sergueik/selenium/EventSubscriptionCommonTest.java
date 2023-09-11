package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/1-3/Runtime/#method-evaluate
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class EventSubscriptionCommonTest {

	protected static boolean debug = true;
	protected static boolean runHeadless = false;
	private static String osName = Utils.getOSName();
	protected static ChromiumDriver driver;
	protected static DevTools chromeDevTools;
	protected static WebDriverWait wait;
	public final static int flexibleWait = 60; // too long
	public final static Duration duration = Duration.ofSeconds(flexibleWait);;
	public final static int implicitWait = 1;
	public final static int pollingInterval = 500;
	protected Alert alert = null;
	protected static String name = null;
	protected static WebElement element;
	protected static List<WebElement> elements;

	@BeforeClass
	public static void setUp() {

		if (System.getenv().containsKey("HEADLESS")
				&& System.getenv("HEADLESS").matches("(?:true|yes|1)")) {
			runHeadless = true;
		}
		// force the headless flag to be true to support Unix console execution
		if (!(Utils.getOSName().equals("windows"))
				&& !(System.getenv().containsKey("DISPLAY"))) {
			runHeadless = true;
		}
		System
				.setProperty("webdriver.chrome.driver",
						Paths.get(System.getProperty("user.home"))
								.resolve("Downloads").resolve(osName.equals("windows")
										? "chromedriver.exe" : "chromedriver")
								.toAbsolutePath().toString());

		if (runHeadless) {
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--headless", "--disable-gpu");
			driver = new ChromeDriver(options);
		} else {
			driver = new ChromeDriver();
		}
		Utils.setDriver(driver);
		// Declare a wait time

		wait = new WebDriverWait(driver, duration);

		// NOTE: constructor WebDriverWait(WebDriver, Duration) is undefined
		// with Selenium 3.x ?
		// wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));

		// Selenium Driver version sensitive code: 3.13.0 vs. 3.8.0 and older
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
		// wait.pollingEvery(pollingInterval, TimeUnit.MILLISECONDS);

		chromeDevTools = ((HasDevTools) driver).getDevTools();
		// compiles but fails in runtime when version of selenium-java and
		// selenium-devtools are different
		// error is:
		// Method
		// org/openqa/selenium/chrome/ChromeDriver.getDevTools()Lorg/openqa/selenium/devtools/DevTools;
		// is abstract
	}

	@AfterClass
	public static void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	protected static WebElement findButton() {

		elements = driver.findElements(By.tagName("iframe"));
		if (debug) {
			// System.err.println("page: " + driver.getPageSource());
		}
		for (WebElement element : elements) {
			// TODO: org.openqa.selenium.StaleElementReferenceException
			// probably not navigating across frames
			if (!element.getAttribute("style").matches(".*display: none;.*")) {
				name = element.getAttribute("name");
				if (debug)
					System.err
							.println(String.format("Selenium found visible iframe : %s\n%s\n",
									name, element.getAttribute("outerHTML")));
			}
		}
		assertThat(name, notNullValue());
		WebElement frame = wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.cssSelector(String.format("iframe[name='%s']", name))));
		assertThat(frame, notNullValue());

		// Act
		WebDriver iframe = driver.switchTo().frame(frame);

		Utils.sleep(1000);
		WebElement element = iframe.findElement(By.tagName("button"));
		assertThat(element, notNullValue());
		if (debug)
			System.err.println(String.format("Selenium found button: %s\n",
					element.getAttribute("outerHTML")));
		Utils.highlight(element, 1000);
		return element;
	}

}
