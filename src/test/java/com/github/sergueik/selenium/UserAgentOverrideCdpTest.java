package com.github.sergueik.selenium;

/* Copyright 2023,2024 Serguei Kouzmine */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * 
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setUserAgentOverride
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setUserAgentOverride
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#type-UserAgentMetadata
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#type-UserAgentBrandVersion
 * https://wicg.github.io/ua-client-hints/
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class UserAgentOverrideCdpTest extends BaseCdpTest {

	private static WebElement element = null;
	private static List<WebElement> elements = new ArrayList<>();
	private static By locator = null;
	private static Map<String, Object> params = new HashMap<>();
	private static Map<String, Object> data = new HashMap<>();
	private static Map<String, Object> data2 = new HashMap<>();
	static {
		{
			params.put("userAgent", "python 2.7");
			params.put("platform", "Windows");
		}
	};

	@Before
	public void beforeTest() throws Exception {
		// new HashMap<>() => org.openqa.selenium.InvalidArgumentException
		params.put("userAgent", "");
		params.put("platform", "");

		driver.executeCdpCommand("Network.setUserAgentOverride", params);
		driver.executeCdpCommand("Emulation.setUserAgentOverride", params);
		params.clear();
		params.put("userAgent", "python 2.7");
		params.put("platform", "Windows");
	}

	@After
	public void clearPage() {
		driver.get("about:blank");
	}

	// NOTE:
	// the URL used in this test shows bogus information about
	// the sender ip address (not confirmed)
	// and does not reflect the change in the user agent made via CDP
	// https://chromedevtools.github.io/devtools-protocol/tot/Network#method-setUserAgentOverride
	// https://stackoverflow.com/questions/29916054/change-user-agent-for-selenium-driver
	// @Ignore

	@Test(/* expected = NoSuchElementException.class */)
	public void test1() {
		// the site may be down, and it can also reject automated browsing
		// pingHost() does not work reliably yet
		// Assume.assumeTrue(pingHost("www.whoishostingthis.com", 443, 3));
		// Assume.assumeTrue(pingHost("www.whoishostingthis.com", 80, 10));
		// Arrange
		driver.get("https://www.whoishostingthis.com/tools/user-agent/");
		locator = By.cssSelector("#content-base div.content-block-main");
		elements = driver.findElements(locator);
		if (elements.size() == 0) {
			// You have been blocked
			return;
		}
		locator = By.cssSelector("div.info-box.user-agent");
		element = driver.findElement(locator);
		Utils.highlight(element);
		Utils.sleep(100);
		assertThat(element.getAttribute("innerText"), containsString("Mozilla"));

		// Act
		try {
			driver.executeCdpCommand("Network.setUserAgentOverride", params);
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception " + e.toString());
			throw (new RuntimeException(e));
		}
		driver.navigate().refresh();
		Utils.sleep(2000);

		element = driver.findElement(locator);
		assertThat(element.isDisplayed(), is(true));
		assertThat(element.getAttribute("innerText"), is("python 2.7"));
	}

	@Test
	public void test2() {
		// Arrange
		driver.get(
				"https://www.whatismybrowser.com/detect/what-http-headers-is-my-browser-sending");
		locator = By.xpath(
				"//*[@id=\"content-base\"]//table//th[contains(text(),\"USER-AGENT\")]/../td");
		element = driver.findElement(locator);
		Utils.highlight(element);
		Utils.sleep(100);
		assertThat(element.getAttribute("innerText"), containsString("Mozilla"));
		System.err
				.println("Vanilla USER-AGENT: " + element.getAttribute("innerText"));

		// Act
		try {
			params.put("acceptLanguage", "en-US"); // optional
			driver.executeCdpCommand("Network.setUserAgentOverride", params);
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception " + e.toString());
			throw (new RuntimeException(e));
		}
		driver.navigate().refresh();
		Utils.sleep(1000);

		element = driver.findElement(locator);
		assertThat(element.isDisplayed(), is(true));
		assertThat(element.getAttribute("innerText"), is("python 2.7"));
		System.err
				.println("Updated USER-AGENT: " + element.getAttribute("innerText"));
	}

	@Test
	public void test3() {
		// Arrange
		driver.get(
				"https://www.whatismybrowser.com/detect/what-http-headers-is-my-browser-sending");
		locator = By.xpath(
				"//*[@id=\"content-base\"]//table//th[contains(text(),\"USER-AGENT\")]/../td");
		element = driver.findElement(locator);
		Utils.highlight(element);
		Utils.sleep(100);
		assertThat(element.getAttribute("innerText"), containsString("Mozilla"));
		System.err
				.println("Vanilla USER-AGENT: " + element.getAttribute("innerText"));

		// Act
		try {
			driver.executeCdpCommand("Emulation.setUserAgentOverride", params);
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception " + e.toString());
			throw (new RuntimeException(e));
		}
		driver.navigate().refresh();
		Utils.sleep(1000);

		element = driver.findElement(locator);
		assertThat(element.isDisplayed(), is(true));
		assertThat(element.getAttribute("innerText"), is("python 2.7"));
		System.err
				.println("Updated USER-AGENT: " + element.getAttribute("innerText"));
	}

	@Test
	public void test4() {
		// Arrange
		driver.get(
				"https://www.whatismybrowser.com/detect/what-http-headers-is-my-browser-sending");
		locator = By.xpath(
				"//*[@id=\"content-base\"]//table//th[contains(text(),\"USER-AGENT\")]/../td");
		element = driver.findElement(locator);
		Utils.highlight(element);
		Utils.sleep(100);
		assertThat(element.getAttribute("innerText"), containsString("Mozilla"));
		System.err
				.println("Vanilla USER-AGENT: " + element.getAttribute("innerText"));

		// Act
		try {
			driver.executeCdpCommand("Emulation.setUserAgentOverride", params);
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception " + e.toString());
			throw (new RuntimeException(e));
		}
		driver.navigate().refresh();
		Utils.sleep(1000);

		element = driver.findElement(locator);
		assertThat(element.isDisplayed(), is(true));
		assertThat(element.getAttribute("innerText"), is("python 2.7"));
		System.err
				.println("Updated USER-AGENT: " + element.getAttribute("innerText"));
	}

	@Test
	public void test5() {
		String brand = "Chrome";
		String version = "120";
		String platform = "windows";
		String platformVersion = "NT 6.0";

		// Arrange
		driver.get(
				"https://www.whatismybrowser.com/detect/what-http-headers-is-my-browser-sending");
		locator = By.xpath(
				"//*[@id=\"content-base\"]//table//th[contains(text(),\"USER-AGENT\")]/../td");
		element = driver.findElement(locator);
		Utils.highlight(element);
		Utils.sleep(100);
		assertThat(element.getAttribute("innerText"), containsString("Mozilla"));
		System.err
				.println("Vanilla USER-AGENT: " + element.getAttribute("innerText"));

		// Act
		try {
			data2.put("brand", brand);
			data2.put("version", version);
			List<Map<String, Object>> brands = new ArrayList<>();
			brands.add(data2);
			data.put("brands", brands);
			data.put("fullVersionList", new ArrayList<>());
			data.put("platform", platform);
			data.put("platformVersion", platformVersion);
			data.put("architecture", "amd64");
			data.put("model", "");
			data.put("mobile", false);
			data.put("bitness", "");
			params.put("userAgentMetadata", data);
			driver.executeCdpCommand("Emulation.setUserAgentOverride", params);
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception " + e.toString());
			throw (new RuntimeException(e));
		}
		driver.navigate().refresh();
		Utils.sleep(1000);

		element = driver.findElement(locator);
		assertThat(element.isDisplayed(), is(true));
		assertThat(element.getAttribute("innerText"), is("python 2.7"));
		System.err
				.println("Updated USER-AGENT: " + element.getAttribute("innerText"));
		locator = By.xpath(
				"//*[@id=\"content-base\"]//table//th[contains(text(),\"SEC-CH-UA-PLATFORM-VERSION\")]/../td");
		element = driver.findElement(locator);
		assertThat(element.isDisplayed(), is(true));
		assertThat(element.getAttribute("innerText"),
				containsString(platformVersion));
		System.err.println("Updated SEC-CH-UA-PLATFORM-VERSION: "
				+ element.getAttribute("innerText"));

	}

	//
}

