package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge inspired
 * origin: https://github.com/sachinguptait/SeleniumAutomation
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class WindowsTabsTest {

	private static String osName = Utils.getOSName();

	private static ChromiumDriver driver;
	private static WebDriverWait wait;
	private static boolean runHeadless = false;

	private static int flexibleWait = 60;
	private static int pollingInterval = 500;

	private static String url1 = "https://en.wikipedia.org/wiki/Main_Page";
	private static String url2 = "https://www.google.com";
	private static String url3 = "http://newtours.demoaut.com/";
	private static Map<Integer, String> data = new HashMap<>();
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
		options.addArguments("--ssl-protocol=any", "--ignore-ssl-errors=true",
				"--disable-extensions", "--ignore-certificate-errors");
		options.setExperimentalOption("useAutomationExtension", false);
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
		Utils.sleep(1000);
	}

	// https://github.com/qtacore/chrome_master/blob/master/chrome_master/input_handler.py#L32
	// https://www.javadoc.io/static/com.machinepublishers/jbrowserdriver/1.1.1/org/openqa/selenium/WindowType.html
	
	@Test
	public void tabTest() {
		this.openNewTab(url1);
		Utils.sleep(100);
		this.openNewWindow(url2);
		data.put(1, url2);
		Utils.sleep(100);
		this.openNewTab(url3);
		data.put(2, url3);
		Utils.sleep(100);
		this.openNewWindow(url1);
		Utils.sleep(100);
		switchToWindow(1);
		assertThat(driver.getTitle(), is("Wikipedia, the free encyclopedia"));
		// NOTE: unstable
		// assertThat(driver.getTitle(), is("Google"));
		Utils.sleep(100);
		switchToWindow(2);
		assertThat(driver.getCurrentUrl(), is("https://www.google.com/"));
		Utils.sleep(100);
		switchToWindow(3);
		System.err.println("Window handle: " + driver.getWindowHandle());
		assertThat(driver.getWindowHandle(), containsString("CDwindow-"));
		Utils.sleep(100);
		closeWindow(0);
		switchToWindow(1);
		// NOTE: unstable
		assertThat(driver.getTitle(), is("Google"));
		// assertThat(driver.getTitle(), is("Wikipedia, the free encyclopedia"));
		Utils.sleep(1000);
	}

	// utilities
	private void openNewTab(String url) {
		this.driver.switchTo().newWindow(WindowType.TAB).get(url);
	}

	private void openNewWindow(String url) {
		this.driver.switchTo().newWindow(WindowType.WINDOW).get(url);
	}

	public Set<String> getAllWindows() {
		return this.driver.getWindowHandles();
	}

	private void switchToWindow(int windowNumber) {
		Set<String> allWindows = getAllWindows();
		ArrayList<String> windowHandles = new ArrayList<>(allWindows);
		driver.switchTo().window(windowHandles.get(windowNumber));
	}

	private void closeWindow(int windowNumber) {
		Set<String> allWindows = getAllWindows();
		ArrayList<String> windowHandles = new ArrayList<>(allWindows);
		driver.switchTo().window(windowHandles.get(windowNumber)).close();
	}
}
