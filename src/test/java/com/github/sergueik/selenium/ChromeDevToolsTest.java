package com.github.sergueik.selenium;

import java.nio.file.Paths;
import java.time.Duration;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.Console;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.Gson;

// origin:
// https://github.com/adiohana/selenium-chrome-devtools-examples/blob/master/src/test/java/ChromeDevToolsTest.java

public class ChromeDevToolsTest {

	private static boolean runHeadless = false;
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

	private static Gson gson = new Gson();

	private final static int id = (int) (java.lang.Math.random() * 1_000_000);
	public final static String consoleMessage = "message from test id #" + id;

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

	@BeforeClass
	public static void beforeClass() throws Exception {
		// enable Console
		chromeDevTools.send(Console.enable());
		// listener to host console log the console message
		chromeDevTools.addListener(Console.messageAdded(), System.err::println);
		driver.get(baseURL);
	}

	@AfterClass
	public static void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	@Test
	public void consoleMessageAddTest() {
		// Assert
		// listener to verify the console message
		chromeDevTools.addListener(Console.messageAdded(),
				o -> Assert.assertEquals(true, o.getText().equals(consoleMessage)));

		// Act
		// write console message by executing Javascript
		Utils.executeScript("console.log('" + consoleMessage + "');");
	}

}
