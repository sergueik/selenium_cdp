package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.NoSuchElementException;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge inspired
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class ChromiumCdpInjectedScriptTest {

	private static String osName = Utils.getOSName();

	private static ChromiumDriver driver;
	private static WebDriverWait wait;
	private static boolean runHeadless = false;

	private static int flexibleWait = 60;
	private static int pollingInterval = 500;

	private static String command = null;
	private static String script = null;
	private static String identifier = null;
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();
	private static Map<String, Object> data = new HashMap<>();
	private static String stringData = null;
	private static Boolean booleanData = false;
	private static WebElement element = null;
	private static String baseURL = "https://www.wikipedia.org";

	@BeforeClass
	public static void beforeClass() throws Exception {

		if ((System.getenv().containsKey("HEADLESS")
				&& System.getenv("HEADLESS").matches("(?:true|yes|1)"))
				|| (!(Utils.getOSName().equals("windows"))
						&& !(System.getenv().containsKey("DISPLAY")))) {
			runHeadless = true;
		}

		System
				.setProperty("webdriver.chrome.driver",
						Paths.get(System.getProperty("user.home"))
								.resolve("Downloads").resolve(osName.equals("windows")
										? "chromedriver.exe" : "chromedriver")
								.toAbsolutePath().toString());

		ChromeOptions options = new ChromeOptions();
		// see also:
		// https://ivanderevianko.com/2020/04/disable-logging-in-selenium-chromedriver
		// https://antoinevastel.com/bot%20detection/2017/08/05/detect-chrome-headless.html
		// @formatter:off
		for (String optionAgrument : (new String[] {
				"--allow-insecure-localhost",
				"--allow-running-insecure-content",
				"--browser.download.folderList=2",
				"--browser.helperApps.neverAsk.saveToDisk=image/jpg,text/csv,text/xml,application/xml,application/vnd.ms-excel,application/x-excel,application/x-msexcel,application/excel,application/pdf",
				"--disable-blink-features=AutomationControlled",
				"--disable-default-app",
				"--disable-dev-shm-usage",
				"--disable-extensions",
				"--disable-gpu",
				"--disable-infobars",
				"--disable-in-process-stack-traces",
				"--disable-logging",
				"--disable-notifications",
				"--disable-popup-blocking",
				"--disable-save-password-bubble",
				"--disable-translate",
				"--disable-web-security",
				"--enable-local-file-accesses",
				"--ignore-certificate-errors",
				"--ignore-certificate-errors",
				"--ignore-ssl-errors=true",
				"--log-level=3",
				"--no-proxy-server",
				"--no-sandbox",
				"--output=/dev/null",
				"--ssl-protocol=any",
				// "--start-fullscreen",
				// "--start-maximized" ,
				"--user-agent=Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20120101 Firefox/33.0",
				// String.format("--browser.download.dir=%s", downloadFilepath)
				/*
				 * "--user-data-dir=/path/to/your/custom/profile",
				 * "--profile-directory=name_of_custom_profile_directory",
				 */
		})) {
			options.addArguments(optionAgrument);
		}
		// @formatter:on
		// options for headless
		// NOTE: Deprecated chrome option is ignored: useAutomationExtension
		// options.setExperimentalOption("useAutomationExtension", false);
		if (runHeadless) {
			options.addArguments("--headless", "--disable-gpu");
		}

		driver = new ChromeDriver(options);
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		Utils.setDriver(driver);
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
	}

	@Before
	public void beforeTest() throws Exception {

	}

	@AfterClass
	public static void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	@After
	public void clearPage() {
		driver.get("about:blank");
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-addScriptToEvaluateOnNewDocument
	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-removeScriptToEvaluateOnNewDocument
	@Test
	public void scriptOnNewDocumentTest() {
		try {
			// Arrange
			command = "Page.addScriptToEvaluateOnNewDocument";
			params = new HashMap<>();
			script = "window.was_here=true;";

			params.put("source", script);
			result = driver.executeCdpCommand(command, params);
			identifier = (String) result.get("identifier");
			assertThat(identifier, notNullValue());
			System.err.println("injected script identifier: " + identifier);
			// Act
			driver.get(baseURL);
			Utils.sleep(100);
			booleanData = (Boolean) Utils.executeScript("return window.was_here");
			// Assert
			assertThat(booleanData, is(true));

			command = "Page.removeScriptToEvaluateOnNewDocument";
			params = new HashMap<>();
			params.put("identifier", identifier);
			driver.executeCdpCommand(command, params);
		} catch (Exception e) {
			System.err
					.println("Exception in " + command + " (ignored): " + e.toString());
		}
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-addScriptToEvaluateOnLoad
	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-removeScriptToEvaluateOnLoad
	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#type-ScriptIdentifier
	@Test
	public void scriptToOnLoadTest() {
		try {
			// Arrange
			command = "Page.addScriptToEvaluateOnLoad";
			params = new HashMap<>();
			script = "window.was_here='true';";
			params.clear();
			params.put("scriptSource", script);
			result = driver.executeCdpCommand(command, params);
			identifier = (String) result.get("identifier");
			assertThat(identifier, notNullValue());
			System.err.println("injected script identifier: " + identifier);
			// Act
			driver.get(baseURL);
			Utils.sleep(100);
			stringData = (String) Utils.executeScript("return window.was_here");
			assertThat(stringData, is("true"));

			command = "Page.removeScriptToEvaluateOnNewDocument";
			params = new HashMap<>();
			params.put("identifier", identifier);
			driver.executeCdpCommand(command, params);
		} catch (Exception e) {
			System.err
					.println("Exception in " + command + " (ignored): " + e.toString());
		}
	}

	// @Ignore
	@Test(expected = java.lang.AssertionError.class)
	public void scriptToOnLoadTest2() {
		try {
			// Arrange
			command = "Page.addScriptToEvaluateOnLoad";
			params = new HashMap<>();
			script = "var e = document.createElement('div'); e.id = 'data'; e.setAttribute('class', 'democlass'); e.style.display = 'none';  if (document.body != null) { document.body.appendChild(e); }";
			params.clear();
			params.put("scriptSource", script);
			result = driver.executeCdpCommand(command, params);
			identifier = (String) result.get("identifier");
			assertThat(stringData, notNullValue());
			System.err.println("injected script identifier: " + identifier);
			// Act
			driver.get(baseURL);
			Utils.sleep(1000);
			element = driver.findElement(By.cssSelector("div.democlass"));
			assertThat(element, notNullValue());
			System.err.println("Test 2 found the dynamic element "
					+ element.getAttribute("outerHTML"));

			command = "Page.removeScriptToEvaluateOnNewDocument";
			params = new HashMap<>();
			params.put("identifier", identifier);
			driver.executeCdpCommand(command, params);
		} catch (NoSuchElementException e) {
			System.err.println("Exception in " + command + ": " + e.toString());
			throw (e);
		} catch (Exception e) {
			System.err
					.println("Exception in " + command + " (ignored): " + e.toString());
		}
	}

	// @Ignore
	@Test(expected = NoSuchElementException.class)
	public void scriptToNewDocumentest3() {
		try {
			// Arrange
			command = "Page.addScriptToEvaluateOnNewDocument";
			params = new HashMap<>();
			script = "var e = document.createElement('div');e.id = 'data'; e.setAttribute('class', 'democlass'); e.style.display = 'none'; if (document.body != null) { document.body.appendChild(e); }";
			// VM25:1 Uncaught TypeError: Cannot read property 'appendChild' of null
			// at <anonymous>:1:132
			params.clear();
			params.put("source", script);
			result = driver.executeCdpCommand(command, params);
			identifier = (String) result.get("identifier");
			assertThat(stringData, notNullValue());
			System.err.println("injected script identifier: " + identifier);
			// Act
			driver.get(baseURL);
			Utils.sleep(100);
			element = driver.findElement(By.cssSelector("div.democlass"));
			assertThat(element, notNullValue());
			System.err.println("Test 3 Found the dynamic element "
					+ element.getAttribute("outerHTML"));

			command = "Page.removeScriptToEvaluateOnNewDocument";
			params = new HashMap<>();
			params.put("identifier", identifier);
			driver.executeCdpCommand(command, params);
		} catch (NoSuchElementException e) {
			System.err.println("Exception in " + command + ": " + e.toString());
			throw (e);
		} catch (Exception e) {
			System.err
					.println("Exception in " + command + " (ignored): " + e.toString());
		}
	}

	// @Ignore
	@Test
	public void scriptToNewDocumentest4() {
		try {
			// Arrange
			command = "Page.addScriptToEvaluateOnNewDocument";
			params = new HashMap<>();
			script = "window.addEventListener('load', function(){ var e = document.createElement('div');e.id = 'data'; e.setAttribute('class', 'democlass'); e.style.display = 'none'; document.body.appendChild(e);});";
			params.clear();
			params.put("source", script);
			result = driver.executeCdpCommand(command, params);
			identifier = (String) result.get("identifier");
			assertThat(stringData, notNullValue());
			System.err.println("injected script identifier: " + identifier);
			// Act
			driver.get(baseURL);
			Utils.sleep(1000);
			element = driver.findElement(By.cssSelector("div.democlass"));
			assertThat(element, notNullValue());
			System.err.println(
					"Found the dynamic element " + element.getAttribute("outerHTML"));
			command = "Page.removeScriptToEvaluateOnNewDocument";
			params = new HashMap<>();
			params.put("identifier", identifier);
			driver.executeCdpCommand(command, params);
		} catch (Exception e) {
			System.err
					.println("Exception in " + command + " (ignored): " + e.toString());
		}
	}
}
