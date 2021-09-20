package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.openqa.selenium.net.PortProber;

import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.NoSuchSessionException;
import java.util.Iterator;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge inspired
 * origin: https://github.com/sachinguptait/SeleniumAutomation
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class WindowsTabsTest extends BaseCdpTest {

	private static String url1 = "https://en.wikipedia.org/wiki/Main_Page";
	private static String url2 = "https://www.google.com";
	// NOTE: http://ww1.demoaut.com no longer exits, domain it for sale
	private static String url3 = "http://newtours.demoaut.com/";
	private static Map<Integer, String> data = new HashMap<>();
	private static String baseURL = "about:blank";

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
		// usually get "about:blank
		// driver.get("about:blank");
		// Utils.sleep(1000);
		closeAllWindows();
		driver.get("about:blank");
		Utils.sleep(100);
	}

	@Test
	// https://github.com/qtacore/chrome_master/blob/master/chrome_master/input_handler.py#L32
	// https://www.javadoc.io/static/com.machinepublishers/jbrowserdriver/1.1.1/org/openqa/selenium/WindowType.html
	public void test1() {
		this.openNewTab(url1);
		data.put(1, url1);
		Utils.sleep(100);
		this.openNewTab(url2);
		data.put(2, url2);
		Utils.sleep(100);
		this.openNewTab(url3);
		data.put(3, url3);
		Utils.sleep(100);
		switchToWindow(1);
		assertThat(driver.getTitle(), is("Wikipedia, the free encyclopedia"));
		Utils.sleep(100);
		switchToWindow(2);
		assertThat(driver.getTitle(), is("Google"));
		assertThat(driver.getCurrentUrl(),
				containsString("https://www.google.com/"));
		switchToWindow(3);
		Utils.sleep(100);
		System.err.println("Window handle: " + driver.getWindowHandle());
		assertThat(driver.getWindowHandle(), containsString("CDwindow-"));
		Utils.sleep(100);
		switchToWindow(0);
		// closeAllWindows();
	}

	@Test
	// https://github.com/qtacore/chrome_master/blob/master/chrome_master/input_handler.py#L32
	// https://www.javadoc.io/static/com.machinepublishers/jbrowserdriver/1.1.1/org/openqa/selenium/WindowType.html
	public void test2() {
		this.openNewTab(url1);
		data.put(1, url1);
		Utils.sleep(100);
		this.openNewTab(url2);
		data.put(2, url2);
		Utils.sleep(100);
		this.openNewTab(url3);
		data.put(3, url3);
		Utils.sleep(100);
		switchToWindow(0);
		closeWindow(1);
		Utils.sleep(1000);
		try {
			System.err.println("Available /remaining window handles:\n"
					+ String.join(",\n", getAllWindows()));
		} catch (NoSuchWindowException e) {
			System.err.println("Exception (ignored) in mini-step 1:" + e.toString());
		}
		Utils.sleep(1000);
		switchToWindow(0);
		closeWindow(1);
		try {
			System.err.println("Available /remaining window handles:\n"
					+ String.join(",\n", getAllWindows()));
		} catch (NoSuchWindowException e) {
			System.err.println("Exception (ignored) in mini-step 1:" + e.toString());
		}
		Utils.sleep(1000);
		switchToWindow(0);
		closeWindow(1);
		try {
			System.err.println("Available /remaining window handles:\n"
					+ String.join(",\n", getAllWindows()));
		} catch (NoSuchWindowException e) {
			System.err.println("Exception (ignored) in mini-step 1:" + e.toString());
		}
		Utils.sleep(1000);
		switchToWindow(0);
		// NOTE: unstable
		// assertThat(driver.getTitle(), is("about:blank"));
		// assertThat(driver.getTitle(), is("Wikipedia, the free encyclopedia"));
		assertThat(driver.getTitle(), is(""));
		Utils.sleep(1000);
	}

	@Test
	public void test3() {
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
		Utils.sleep(100);
		switchToWindow(2);
		// NOTE: unstable
		// assertThat(driver.getTitle(), is("Google"));
		assertThat(driver.getCurrentUrl(),
				containsString("https://www.google.com/"));
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
		return BaseCdpTest.driver.getWindowHandles();
	}

	private void switchToWindow(int windowNumber) {
		Set<String> allWindows = getAllWindows();
		ArrayList<String> windowHandles = new ArrayList<>(allWindows);
		driver.switchTo().window(windowHandles.get(windowNumber));
	}

	private void closeWindow(int windowNumber) {
		Set<String> allWindows = getAllWindows();
		if (!allWindows.isEmpty()) {
			ArrayList<String> windowHandles = new ArrayList<>(allWindows);
			driver.switchTo().window(windowHandles.get(windowNumber)).close();
		}
	}

	private void closeAllWindows() {
		boolean hasWindows = true;
		while (hasWindows) {
			try {
				Set<String> allWindows = getAllWindows();
				if (allWindows.isEmpty() || allWindows.size() == 1) {
					// keep 1 window open after tests run
					hasWindows = false;
				} else {
					System.err.println(
							String.format("closing: %d remaining", allWindows.size()));
					Iterator<String> windowIterator = allWindows.iterator();
					driver.switchTo().window(windowIterator.next()).close();
					driver.switchTo().window(windowIterator.next());
				}
			} catch (NoSuchWindowException | NoSuchSessionException e) {
				System.err.println(
						"Exception (ignored) while closing windows:" + e.toString());
				hasWindows = false;
			}
		}
	}
}
