package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v94.log.Log;
import org.openqa.selenium.devtools.v94.log.model.LogEntry;
import org.openqa.selenium.devtools.v94.page.Page;
import org.openqa.selenium.devtools.v94.runtime.model.Timestamp;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge

 * https://chromedevtools.github.io/devtools-protocol/tot/Log#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/Log/#event-entryAdded
 * https://chromedevtools.github.io/devtools-protocol/1-3/Page/#method-navigate
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

@SuppressWarnings("deprecation")
public class LoggingDevToolsTest {

	private static boolean runHeadless = false;
	private static String osName = Utils.getOSName();
	private static ChromiumDriver driver;
	private static DevTools chromeDevTools;

	private final static String baseURL = "https://www.google.com";

	@BeforeClass
	public static void setUp() throws Exception {

		if (System.getenv().containsKey("HEADLESS")
				&& System.getenv("HEADLESS").matches("(?:true|yes|1)")) {
			runHeadless = true;
		}
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

		chromeDevTools = ((HasDevTools) driver).getDevTools();

		chromeDevTools.createSession();
	}

	@Before
	public void beforeTest() throws Exception {
		// enable Console
		chromeDevTools.send(Log.enable());
	}

	@After
	public void afterTest() throws Exception {
		chromeDevTools.clearListeners();
		chromeDevTools.send(Log.disable());
	}

	@AfterClass
	public static void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	@Test
	public void test1() {
		// add event listener to show in host console the browser console message
		chromeDevTools.addListener(Log.entryAdded(), event -> {
			assertThat(event.getText(), notNullValue());
			assertThat(event.getLineNumber(), notNullValue());
			assertThat(event.getTimestamp(), notNullValue());
			assertThat(event.getSource(), notNullValue());

		});
		// bad exmaple: would print:
		// org.openqa.selenium.devtools.v94.log.model.LogEntry@5e77d702
		// chromeDevTools.addListener(Log.entryAdded(), System.err::println);

		chromeDevTools.addListener(Log.entryAdded(),
				entry -> System.err.println(String.format(
						"time stamp: %s line number: %s url: \"%s\" text: %s",
						// formatted in unparsable "1.634098233101593E12"
						// intended to new Date(Long.parseUnsignedLong(...))
						entry.getTimestamp(),
						(entry.getLineNumber().isPresent() ? entry.getLineNumber().get()
								: ""),
						(entry.getUrl().isPresent() ? entry.getUrl().get() : ""),
						entry.getText())));
		driver.get(baseURL);
		chromeDevTools.send(Page.navigate(baseURL, Optional.empty(),
				Optional.empty(), Optional.empty(), Optional.empty()));
	}

}
