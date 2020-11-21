package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.log.Log;
import org.openqa.selenium.devtools.page.Page;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Console#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/Log#method-enable
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class LoggingDevToolsTest {

	private static boolean runHeadless = false;
	private static String osName = Utils.getOSName();
	private static ChromiumDriver driver;
	private static DevTools chromeDevTools;

	private final static String baseURL = "https://www.google.com";

	@BeforeClass
	public static void setUp() throws Exception {

		if (System.getenv().containsKey("HEADLESS") && System.getenv("HEADLESS").matches("(?:true|yes|1)")) {
			runHeadless = true;
		}
		if (!(Utils.getOSName().equals("windows")) && !(System.getenv().containsKey("DISPLAY"))) {
			runHeadless = true;
		}
		System.setProperty("webdriver.chrome.driver", Paths.get(System.getProperty("user.home")).resolve("Downloads")
				.resolve(osName.equals("windows") ? "chromedriver.exe" : "chromedriver").toAbsolutePath().toString());

		if (runHeadless) {
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--headless", "--disable-gpu");
			driver = new ChromeDriver(options);
		} else {
			driver = new ChromeDriver();
		}
		Utils.setDriver(driver);
		chromeDevTools = driver.getDevTools();
		chromeDevTools.createSession();
	}

	@SuppressWarnings("deprecation")
	@BeforeClass
	public static void beforeClass() throws Exception {
		// enable Console
		chromeDevTools.send(Log.enable());
		// add event listener to show in host console the browser console message
		chromeDevTools.addListener(Log.entryAdded(), o -> {
			assertThat(o.getText(), notNullValue());
			assertThat(o.getLineNumber(), notNullValue());
			assertThat(o.getTimestamp(), notNullValue());
			assertThat(o.getSource(), notNullValue());

		});
		chromeDevTools.addListener(Log.entryAdded(), System.err::println);
		driver.get(baseURL);
	}

	@AfterClass
	public static void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	// https://chromedevtools.github.io/devtools-protocol/1-3/Page/#method-navigate
	@Test
	public void test1() {
		chromeDevTools
				.send(Page.navigate(baseURL, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
	}

}
