package com.github.sergueik.selenium;

import java.nio.file.Paths;
import java.time.Duration;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.Console;
// does not exist
// import org.openqa.selenium.devtools.Page;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * based on:
 * https://github.com/adiohana/selenium-chrome-devtools-examples/blob/master/src/test/java/ChromeDevToolsTest.java
 * https://chromedevtools.github.io/devtools-protocol/tot/Page#method-handleJavaScriptDialog 
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class JavaScriptDialogTest {

	private static int flexibleWait = 60;
	private static int pollingInterval = 500;
	private static ChromiumDriver driver;
	private static String osName = Utils.getOSName();
	private static DevTools chromeDevTools;

	// currently unused
	@SuppressWarnings("unused")
	private static WebDriverWait wait;
	private static Actions actions;
	private static String baseURL = "https://apache.org";

	@SuppressWarnings("deprecation")
	@BeforeClass
	public static void setUp() throws Exception {
		System
				.setProperty("webdriver.chrome.driver",
						Paths.get(System.getProperty("user.home"))
								.resolve("Downloads").resolve(osName.equals("windows")
										? "chromedriver.exe" : "chromedriver")
								.toAbsolutePath().toString());

		driver = new ChromeDriver();
		Utils.setDriver(driver);
		actions = new Actions(driver);
		wait = new WebDriverWait(driver, flexibleWait);

		// Selenium Driver version sensitive code: 3.13.0 vs. 3.8.0 and older
		wait.pollingEvery(Duration.ofMillis(pollingInterval));

		chromeDevTools = driver.getDevTools();

		chromeDevTools.createSession();
	}

	@AfterClass
	public static void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	// TODO: Page.setInterceptFileChooserDialog

	@Ignore
	@Test
	// https://chromedevtools.github.io/devtools-protocol/tot/Page#event-javascriptDialogOpening
	// https://chromedevtools.github.io/devtools-protocol/tot/Page#event-javascriptDialogOpening
	// https://chromedevtools.github.io/devtools-protocol/tot/Page#method-handleJavaScriptDialog
	public void handleJavaScriptDialogTest() {
		// Assert
		/*
		chromeDevTools.addListener(Page.javascriptDialogOpening(),
				o -> System.err.println("Alert opening " +  o.getText());
		chromeDevTools.addListener(Page.javascriptDialogOpening(),
				o -> System.err.println("Alert closed " +   o.getText());
			*/
		// Act
		// write console message by executing Javascript
	}

}
