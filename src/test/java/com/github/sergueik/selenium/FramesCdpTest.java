package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.Gson;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge inspired
 * https://the-internet.herokuapp.com/nested_frames
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-getFrameTree
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#type-Frame
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class FramesCdpTest {

	private static String osName = Utils.getOSName();

	private static ChromiumDriver driver;
	private static WebDriverWait wait;
	private static boolean runHeadless = false;

	private static int flexibleWait = 60;
	private static int pollingInterval = 500;

	private static Gson gson = new Gson();

	private static String command = null;
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();
	private static Map<String, Object> data = new HashMap<>();
	private static Map<String, Object> data2 = new HashMap<>();
	private static List<Object> data3 = new ArrayList<>();
	private static String dataString = null;
	private static List<Map<String, Object>> cookies = new ArrayList<>();
	public static Long nodeId = (long) -1;
	public static String isolationId = null;

	private static WebElement element = null;
	private static By locator = null;
	private static String baseURL = "https:// cloud.google.com/products/calculator";

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

	@Ignore
	@Test
	public void test1() {
		// Arrange
		command = "Page.getFrameTree";
		params = new HashMap<>();
		baseURL = "https:// cloud.google.com/products/calculator";
		driver.get(baseURL);
		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			System.err.println("Result: " + result);
			Map<String, Object> frameTree = (Map<String, Object>) result
					.get("frameTree");
			assertThat(frameTree, notNullValue());
			System.err.println("Frame tree: " + Arrays.asList(frameTree.keySet()));
			data = (Map<String, Object>) frameTree.get("frame");
			System.err.println("Frame frame keys: " + Arrays.asList(data.keySet()));
			assertThat(data, hasKey("url"));
			assertThat(data, hasKey("id"));
			System.err.println(String.format("Frame id: %s, url: %s", data.get("id"),
					data.get("url")));
			data3 = (List<Object>) frameTree.get("childFrames");
			assertThat(data3, notNullValue());
			assertThat(data3.size(), greaterThan(0));
			System.err.println(data3.size() + " child frames");
			for (Object childFrame : data3) {
				data = (Map<String, Object>) childFrame;
				assertThat(data, hasKey("frame"));
				data2 = (Map<String, Object>) data.get("frame");
				assertThat(data2, hasKey("url"));
				assertThat(data2, hasKey("id"));
				assertThat(data2, hasKey("parentId"));
				System.err.println(String.format("Child frame id: %s, url: %s",
						data2.get("id"), data2.get("url")));
			}
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	@Test
	public void test2() {
		// Arrange
		command = "Page.getFrameTree";
		params = new HashMap<>();
		// fails with the below
		// that uses the old style frameset and frame tags
		// baseURL =
		// "http://www.maths.surrey.ac.uk/explore/nigelspages/framenest.htm";
		// baseURL = "https://the-internet.herokuapp.com/nested_frames";
		// baseURL = "https://nunzioweb.com/iframes-example.htm";
		// https://jwcooney.com/2014/11/03/calling-page-elements-in-nested-iframes-with-javascript/
		// baseURL =
		// "https://www.sitepoint.com/community/t/can-i-use-iframe-inside-an-iframe/214310";
		baseURL = "https://www.javatpoint.com/oprweb/test.jsp?filename=htmliframes";
		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			System.err.println("Result: " + result);
			Map<String, Object> frameTree = (Map<String, Object>) result
					.get("frameTree");
			assertThat(frameTree, notNullValue());
			System.err.println("Frame tree: " + Arrays.asList(frameTree.keySet()));
			data = (Map<String, Object>) frameTree.get("frame");
			System.err.println("Frame frame keys: " + Arrays.asList(data.keySet()));
			assertThat(data, hasKey("url"));
			assertThat(data, hasKey("id"));
			System.err.println(String.format("Frame id: %s, url: %s", data.get("id"),
					data.get("url")));
			data3 = (List<Object>) frameTree.get("childFrames");
			assertThat(data3, notNullValue());
			assertThat(data3.size(), greaterThan(0));
			System.err.println(data3.size() + " child frames");
			for (Object childFrame : data3) {
				data = (Map<String, Object>) childFrame;
				assertThat(data, hasKey("frame"));
				data2 = (Map<String, Object>) data.get("frame");
				assertThat(data2, hasKey("url"));
				assertThat(data2, hasKey("id"));
				assertThat(data2, hasKey("parentId"));
				System.err.println(String.format("Child frame id: %s, url: %s",
						data2.get("id"), data2.get("url")));
			}
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + " " + e.toString());
			throw (new RuntimeException(e));
		}
	}
}

