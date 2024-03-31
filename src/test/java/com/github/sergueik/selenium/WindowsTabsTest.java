package com.github.sergueik.selenium;

/**
 * Copyright 2022-2024 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WindowType;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge inspired
 * origin: https://github.com/sachinguptait/SeleniumAutomation
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

@SuppressWarnings("unchecked")

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

	
	@Test
	// https://github.com/qtacore/chrome_master/blob/master/chrome_master/input_handler.py#L32
	// https://www.javadoc.io/static/com.machinepublishers/jbrowserdriver/1.1.1/org/openqa/selenium/WindowType.html
	public void test4() {
		this.openNewTab(url1);
		System.err.println("WindowHandle: " + BaseCdpTest.driver.getWindowHandle());
		this.openNewTab(url2);
		System.err.println("WindowHandle: " + BaseCdpTest.driver.getWindowHandle());
		this.openNewTab(url3);
		System.err.println("WindowHandle: " + BaseCdpTest.driver.getWindowHandle());
		Utils.sleep(100);
		List<String> targetIds = new ArrayList<>();
		Map<String, Object> result = BaseCdpTest.driver
				.executeCdpCommand("Target.getTargets", new HashMap<>());
		// format:
		// CDwindow-F71A995BF9B0D0386CB08DD1488CED15
		assertThat(result, notNullValue());
		assertThat(result, hasKey("targetInfos"));
		System.err.println("Target.getTargets:");

		List<Object> targetInfos = (List<Object>) Arrays
				.asList(result.get("targetInfos")).get(0);
		assertThat(targetInfos, notNullValue());
		targetInfos.stream().forEach(o -> {
			Map<String, Object> data = (Map<String, Object>) o;
			assertThat(data, notNullValue());
			assertThat(data, hasKey("targetId"));
			assertThat(data.get("targetId"), notNullValue());
			assertThat(data, hasKey("type"));
			assertThat(data, hasKey("url"));

			System.err.println("targetInfo:");

			for (String k : data.keySet()) {
				System.err.println(k + ": " + data.get(k).toString());

			}
			if (data.get("type").equals("page"))
				targetIds.add(data.get("targetId").toString());
			// format: F71A995BF9B0D0386CB08DD1488CED15
		});
		System.err.println("targetIds: " + targetIds);
		assertThat(targetIds.size(), greaterThan(2));
		// at least 3 tabs

	}

	// utilities
	private void openNewTab(String url) {
		BaseCdpTest.driver.switchTo().newWindow(WindowType.TAB).get(url);
	}

	private void openNewWindow(String url) {
		BaseCdpTest.driver.switchTo().newWindow(WindowType.WINDOW).get(url);
	}

	public Set<String> getAllWindows() {
		return BaseCdpTest.driver.getWindowHandles();
	}

	private void switchToWindow(int windowNumber) {
		// not caching window handles
		ArrayList<String> windowHandles = new ArrayList<>(getAllWindows());
		driver.switchTo().window(windowHandles.get(windowNumber));
	}

	private void closeWindow(int windowNumber) {
		Set<String> allWindows = getAllWindows();
		if (!allWindows.isEmpty()) {
			switchToWindow(windowNumber);
			driver.close();
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
