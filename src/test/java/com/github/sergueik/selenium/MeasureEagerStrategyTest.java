package com.github.sergueik.selenium;

/**
 * Copyright 2023 Serguei Kouzmine
 */

import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 */

// based on:
// https://github.com/debdutta-chatterjee/dc_selenium_4_x_features/blob/main/src/test/java/com/selenium4/newfeature/PageLoadStrategy_Test.java
public class MeasureEagerStrategyTest {

	private static String osName = Utils.getOSName();

	private static ChromiumDriver driver;
	private ChromeOptions options;
	private static WebDriverWait wait;
	private static boolean runHeadless = false;
	private static Map<String, Object> params = new HashMap<>();

	private static String command = null;

	private static int flexibleWait = 60;
	private static int pollingInterval = 500;

	// private static WebElement element = null;
	// private static By locator = By.xpath("//img[@alt='Carnival Home Page']");
	// private static String baseURL = "https://www.carnival.com/";
	// For an ad-heavy "https://www.carnival.com"
	// there is no sound difference
	// eager: 12.547
	// normal: 13.631

	private static String baseURL = "https://www.pluralsight.com/browse";
	private static By locator = By.xpath("//img[@alt='Pluralsight Skills']");

	// eager: 4052
	// normal:10.884
	// HEADLESS
	// eager: 5.101
	// normal: 14,556
	@Before
	public void setup() {
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
		options = new ChromeOptions();
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
	}

	@After
	public void tearDown() {
		driver.get("about:blank");
		if (driver != null) {
			driver.quit();
		}
	}

	@Test
	public void test1() {
		// Arrange

		options.setPageLoadStrategy(PageLoadStrategy.EAGER);
		DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
		desiredCapabilities.setCapability("pageLoadStrategy", "eager");
		options.merge(desiredCapabilities);
		driver = new ChromeDriver(options);

		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		Utils.setDriver(driver);
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		Utils.setDriver(driver);
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
		command = "Network.clearBrowserCache";
		driver.executeCdpCommand(command, new HashMap<>());
		params = new HashMap<>();
		params.put("cacheDisabled", true);
		command = "Network.setCacheDisabled";
		driver.executeCdpCommand(command, params);

		// Act
		long start = System.currentTimeMillis();
		driver.get(baseURL);
		wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
		// Assert
		long end = System.currentTimeMillis();

		System.out.println("Eager: " + (end - start));
	}

	@Test
	public void test2() {
		options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
		// DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
		// desiredCapabilities.setCapability("pageLoadStrategy", "eager");
		// options.merge(desiredCapabilities);
		driver = new ChromeDriver(options);
		// Arrange
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		Utils.setDriver(driver);
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		Utils.setDriver(driver);
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
		command = "Network.clearBrowserCache";
		driver.executeCdpCommand(command, new HashMap<>());
		params = new HashMap<>();
		params.put("cacheDisabled", true);
		command = "Network.setCacheDisabled";
		driver.executeCdpCommand(command, params);

		// Act
		long start = System.currentTimeMillis();
		driver.get(baseURL);

		wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
		// Assert
		long end = System.currentTimeMillis();

		System.out.println("Normal: " + (end - start));
	}
}
