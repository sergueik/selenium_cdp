package com.github.sergueik.selenium;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Input/#method-dispatchKeyEvent
 */

public class ZoomCdpTest {

	private static String osName = Utils.getOSName();

	private static ChromiumDriver driver;
	private static WebDriverWait wait;
	private static boolean runHeadless = false;

	private static int flexibleWait = 60;
	private static int pollingInterval = 500;

	private static final int modifiers = 2;
	// Bit field representing pressed modifier keys. Alt=1, Ctrl=2,
	// Meta/Command=4, Shift=8 (default: 0).
	private final static int delay = 1000;
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
	private static String baseURL = "about:blank";

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
		// TODO:
		/*
		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		capabilities.setBrowserName(DesiredCapabilities.chrome().getBrowserName());
		capabilities.setCapability(
				org.openqa.selenium.chrome.ChromeOptions.CAPABILITY, options);
		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		driver = new ChromeDriver(capabilities);
		*/
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		Utils.setDriver(driver);
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
	}

	@Before
	public void beforeTest() throws Exception {
		driver.get(baseURL);
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

	@Test
	// https://chromedevtools.github.io/devtools-protocol/1-2/Input/#method-dispatchKeyEvent
	public void zoomDefaultTest() {
		// Assert
		driver.get("https://www.wikipedia.org");
		command = "Input.dispatchKeyEvent";
		try {
			// Act
			for (int cnt = 0; cnt != 3; cnt++) {
				params.clear();
				params.put("type", "char");
				params.put("keyIdentifier", "U+002D"); // minus
				params.put("modifiers", modifiers);
				// params.put("text", "-");
				driver.executeCdpCommand(command, params);
				Utils.sleep(delay);
			}
			params.clear();
			params.put("type", "rawKeyDown");
			// keyDown, keyUp, rawKeyDown, char
			params.put("text", "0");
			params.put("modifiers", modifiers);
			driver.executeCdpCommand(command, params);
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}

	}
}
