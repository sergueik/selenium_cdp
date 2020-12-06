package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.Event;
import org.openqa.selenium.devtools.page.Page;
import org.openqa.selenium.devtools.page.model.JavascriptDialogClosed;
import org.openqa.selenium.devtools.runtime.Runtime;
import org.openqa.selenium.devtools.runtime.Runtime.EvaluateResponse;
import org.openqa.selenium.devtools.runtime.model.ExecutionContextId;
import org.openqa.selenium.devtools.runtime.model.RemoteObject;
import org.openqa.selenium.devtools.runtime.model.TimeDelta;
import org.openqa.selenium.json.JsonException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/1-3/Runtime/#method-evaluate
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class AlertDevToolsTest {

	private static boolean debug = true;
	private static boolean runHeadless = false;
	private static String osName = Utils.getOSName();
	private static ChromiumDriver driver;
	private static DevTools chromeDevTools;
	public static WebDriverWait wait;
	public final static int flexibleWait = 60; // too long
	public final static int implicitWait = 1;
	public final static int pollingInterval = 500;
	private final static long highlightInterval = 100;
	private Alert alert = null;
	private static String name = null;
	private static WebElement element;
	private static List<WebElement> elements;
	private final static String baseURL = "https://www.w3schools.com/js/tryit.asp?filename=tryjs_alert";


	@SuppressWarnings("deprecation")
	@BeforeClass
	public static void setUp() throws Exception {

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
		wait = new WebDriverWait(driver, flexibleWait);

		// NOTE: constructor WebDriverWait(WebDriver, Duration) is undefined
		// with Selenium 3.x ?
		// wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));

		// Selenium Driver version sensitive code: 3.13.0 vs. 3.8.0 and older
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
		// wait.pollingEvery(pollingInterval, TimeUnit.MILLISECONDS);
		chromeDevTools = driver.getDevTools();
		chromeDevTools.createSession();
		chromeDevTools.send(Page.enable());

		// https://chromedevtools.github.io/devtools-protocol/1-2/Page/#event-frameAttached
		chromeDevTools.addListener(Page.frameAttached(), o -> System.err.println(
				String.format("Page has frame %s attached: ", o.getFrameId())));
		// https://chromedevtools.github.io/devtools-protocol/1-2/Page/#event-frameNavigated
		chromeDevTools.addListener(Page.frameNavigated(), o -> System.err
				.println(String.format("Page has frame %s navigated: ", o.getId())));

		// https://chromedevtools.github.io/devtools-protocol/1-2/Page/#event-javascriptDialogOpening
		chromeDevTools.addListener(Page.javascriptDialogOpening(),
				o -> System.err
						.println(String.format("Page has dialog %s of type %s opening",
								o.getMessage(), o.getType())));
		// TODO: prompt
		// chromeDevTools.addListener(Page.javascriptDialogClosed(),
		// o -> System.err.println("User input:" + o.getUserInput()));
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		driver.get(baseURL);
	}

	@AfterClass
	public static void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	private static WebElement findButton() {

		elements = driver.findElements(By.tagName("iframe"));
		if (debug) {
			// System.err.println("page: " + driver.getPageSource());
		}
		for (WebElement element : elements) {
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
		sleep(1000);
		WebElement element = iframe.findElement(By.tagName("button"));
		assertThat(element, notNullValue());
		if (debug)
			System.err.println(String.format("Selenium found button: %s\n",
					element.getAttribute("outerHTML")));
		highlight(element, 1000);
		return element;
	}

	// https://chromedevtools.github.io/devtools-protocol/1-3/Page/#event-javascriptDialogClosed
	@Test
	public void test1() {
		// register to alert events
		/*	
			chromeDevTools.addListener(new Event("Page.javascriptDialogClosed",
					JavascriptDialogClosed.class), new Consumer<Object>() {
						@Override
						public void accept(Object o) {
							// do something
						}
					});
					*/
		element = findButton();
		element.sendKeys(Keys.ENTER);
		sleep(100);
		// NOTE: fragile
		// org.openqa.selenium.NoAlertPresentException
		alert = driver.switchTo().alert();
		// Assert
		assertThat(alert, notNullValue());
		sleep(1000);
		if (debug)
			System.err.println("Selenium accepting alert:");
		// assert that dialog was accepted
		chromeDevTools.addListener(Page.javascriptDialogClosed(),
				o -> assertThat(o.getResult(), is(true)));

		alert.accept();
	}

	@Test
	public void test2() {
		// register to alert events
		/*	
			chromeDevTools.addListener(new Event("Page.javascriptDialogClosed",
					JavascriptDialogClosed.class), new Consumer<Object>() {
						@Override
						public void accept(Object o) {
							// do something
						}
					});
					*/
		chromeDevTools.send(Page.reload(Optional.of(true), Optional.empty()));
		element = findButton();
		element.click();
		sleep(100);
		// NOTE: fragile
		// org.openqa.selenium.NoAlertPresentException
		alert = wait.until(ExpectedConditions.alertIsPresent());
		// Assert
		assertThat(alert, notNullValue());
		sleep(1000);
		if (debug)
			System.err.println("Selenium accepting alert:");
		// assert that dialog was canceled
		chromeDevTools.addListener(Page.javascriptDialogClosed(),
				o -> assertThat(o.getResult(), is(false)));

		alert.dismiss();
		chromeDevTools.send(Page.reload(Optional.of(true), Optional.empty()));
	}

	public void highlight(WebElement element) {
		highlight(element, 100, "solid yellow");
	}

	public static void highlight(WebElement element, long highlightInterval) {
		highlight(element, highlightInterval, "solid yellow");
	}

	public static void highlight(WebElement element, long highlightInterval,
			String color) {
		// err.println("Color: " + color);
		if (wait == null) {
			wait = new WebDriverWait(driver, flexibleWait);
		}
		// Selenium Driver version sensitive code: 3.13.0 vs. 3.8.0 and older
		// https://stackoverflow.com/questions/49687699/how-to-remove-deprecation-warning-on-timeout-and-polling-in-selenium-java-client
		wait.pollingEvery(Duration.ofMillis((int) pollingInterval));

		// wait.pollingEvery(pollingInterval, TimeUnit.MILLISECONDS);

		try {
			wait.until(ExpectedConditions.visibilityOf(element));
			executeScript(String.format("arguments[0].style.border='3px %s'", color),
					element);
			Thread.sleep(highlightInterval);
			executeScript("arguments[0].style.border=''", element);
		} catch (InterruptedException e) {
			// err.println("Exception (ignored): " + e.toString());
		}
	}

	// http://www.javawithus.com/tutorial/using-ellipsis-to-accept-variable-number-of-arguments
	public static Object executeScript(String script, Object... arguments) {
		if (driver instanceof JavascriptExecutor) {
			JavascriptExecutor javascriptExecutor = JavascriptExecutor.class
					.cast(driver);
			return javascriptExecutor.executeScript(script, arguments);
		} else {
			throw new RuntimeException("Script execution failed.");
		}
	}

	public static void sleep(Integer milliSeconds) {
		try {
			Thread.sleep((long) milliSeconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}

