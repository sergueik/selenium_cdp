package com.github.sergueik.selenium;

import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.openqa.selenium.support.locators.RelativeLocator.withTagName;

/*
 *  Exploring Selenium 4 new Relative Locators API (do not appear to working smoothly yet) 
 *  see also: 
 *  https://automatorsworld.com/2019/10/05/selenium-webdriver-what-are-relative-locators-in-selenium/
 *  https://www.swtestacademy.com/selenium-relative-locators/
 *  https://angiejones.tech/selenium-4-relative-locators/
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
public class RelativeLocatorTest {

	private static WebDriver driver;
	private static WebDriverWait wait;
	private static String osName = Utils.getOSName();

	private static Actions actions;
	private final static int flexibleWait = 10;
	private final static int implicitWait = 1;
	private final static int pollingInterval = 500;
	private static long highlightInterval = 1000;

	@SuppressWarnings("unused")
	private static final String baseURL = "https://datatables.net/examples/api/highlight.html";

	@SuppressWarnings("deprecation")
	@BeforeClass
	public static void setUp() throws Exception {
		System.setProperty("webdriver.chrome.driver", Paths.get(System.getProperty("user.home")).resolve("Downloads")
				.resolve(osName.equals("windows") ? "chromedriver.exe" : "chromedriver").toAbsolutePath().toString());

		// NOTE: protected constructor method is not visible
		// driver = new ChromiumDriver((CommandExecutor) null, new
		// ImmutableCapabilities(),
		// null);
		driver = new ChromeDriver();
		actions = new Actions(driver);
		wait = new WebDriverWait(driver, flexibleWait);
	}

	@Before
	public void beforeTest() throws Exception {
		driver.get(baseURL);
		wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.id("example"))));
	}

	@AfterClass
	public static void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	@Test
	public void rightElementTest() {

		WebElement tableElement = driver.findElement(By.id("example"));
		WebElement rowElement = tableElement.findElements(By.xpath("tbody/tr")).get(0);
		// System.err.println(rowElement.getAttribute("outerHTML"));
		List<WebElement> elements = rowElement.findElements(By.xpath("td"));
		int num = new Random().nextInt(elements.size() - 1);
		WebElement element = elements.get(num);
		System.err.println("Cell element: " + element.getText());
		highlight(element, highlightInterval);
		WebElement rightElement = tableElement.findElement(withTagName("td").toRightOf(element));
		System.err.println("Cell to the right: " + rightElement.getText());
		highlight(rightElement, highlightInterval);
		Utils.sleep(1000);
	}

	// This cannot find the column below,
	// returns following row leftmost column instead
	// this ruins the whole idea
	// @Ignore
	@Test
	public void belowElementTest() {

		WebElement tableElement = driver.findElement(By.id("example"));
		WebElement rowElement = tableElement.findElements(By.xpath("tbody/tr")).get(0);
		// System.err.println(rowElement.getAttribute("outerHTML"));
		List<WebElement> columnElements = rowElement.findElements(By.xpath("td"));

		int num = new Random().nextInt(columnElements.size() - 1) + 1;
		WebElement columnElement = columnElements.get(num);
		System.err.println("Random cell: " + columnElement.getText());
		highlight(columnElement, highlightInterval);
		WebElement belowElement = tableElement.findElement(withTagName("td").below(columnElement));
		System.err.println("Cell Below: " + belowElement.getText());
		/*
		 * for (WebElement element : columnElements) {
		 * System.err.println("Column element: " + element.getText());
		 * highlight(element, highlightInterval); // NOTE: the next call leads to the
		 * following be logged to STDOUT // (repeated multiple times): //
		 * [org.openqa.selenium.remote.RemoteWebElement@3e62ec7 -> unknown // locator]
		 * belowElement = tableElement.findElement(withTagName("td").below(element));
		 * System.err.println("Below element: " + belowElement.getText()); //
		 * highlight(belowElement, highlightInterval); }
		 */
		Utils.sleep(1000);
	}

	public void highlight(WebElement element, long highlightInterval) {
		// WebElement element = driver.findElement(locator);
		String color = "solid yellow";
		try {
			executeScript(String.format("arguments[0].style.border='3px %s'", color), element);
			Thread.sleep(highlightInterval);
			executeScript("arguments[0].style.border=''", element);
		} catch (java.lang.InterruptedException e) {

		}
	}

	public Object executeScript(String script, Object... arguments) {
		if (driver instanceof JavascriptExecutor) {
			JavascriptExecutor javascriptExecutor = JavascriptExecutor.class.cast(driver);
			return javascriptExecutor.executeScript(script, arguments);
		} else {
			throw new RuntimeException("Script execution failed.");
		}
	}
}
