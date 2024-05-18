package com.github.sergueik.selenium;

/* Copyright 2023,2024 Serguei Kouzmine */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.idealized.Network.UserAgent;
import org.openqa.selenium.devtools.v125.emulation.Emulation;
import org.openqa.selenium.devtools.v125.emulation.model.UserAgentMetadata;
import org.openqa.selenium.devtools.v125.emulation.model.UserAgentBrandVersion;

import org.openqa.selenium.devtools.v125.network.Network;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setUserAgentOverride
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setUserAgentOverride
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#type-UserAgentMetadata
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#type-UserAgentBrandVersion
 * https://wicg.github.io/ua-client-hints/
 * https://javadoc.io/static/org.seleniumhq.selenium/selenium-devtools-v119/4.16.1/org/openqa/selenium/devtools/v119/emulation/Emulation.html#setUserAgentOverride(java.lang.String,java.util.Optional,java.util.Optional,java.util.Optional)
 * https://www.lambdatest.com/automation-testing-advisor/selenium/methods/org.openqa.selenium.devtools.idealized.Network.UserAgent.acceptLanguage
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class UserAgentOverrideDevToolsTest extends BaseDevToolsTest {

	private static WebElement element = null;
	private static List<WebElement> elements = new ArrayList<>();
	private static By locator;

	@Before
	public void before() throws Exception {
		// @formatter:off
		chromeDevTools.send(
				Emulation.setUserAgentOverride(
						"", // userAgent 
						Optional.empty(), // acceptLanguage
						Optional.empty(), // platform
						Optional.empty() // userAgentMetadata
				));
		// @formatter:on
		// @formatter:off
		chromeDevTools.send(
				Network.setUserAgentOverride(
						"", // userAgent 
						Optional.empty(), // acceptLanguage
						Optional.empty(), // platform
						Optional.empty()) // userAgentMetadatas
				);
		// @formatter:on
	}

	@After
	public void clearPage() {
		driver.get("about:blank");
	}

	// @Ignore
	@Test
	public void test1() {
		// the site may be down, and it can also reject autoated browsing
		// pingHost() does not work reliably yet
		// Assume.assumeTrue(pingHost("www.whoishostingthis.com", 443, 3));
		// Arrange
		driver.get("https://www.whoishostingthis.com/tools/user-agent/");

		locator = By.cssSelector("#content-base div.content-block-main");
		elements = driver.findElements(locator);

		if (elements.size() == 0) {
			// You have been blocked ?
			return;
		}

		locator = By.cssSelector("div.info-box.user-agent");
		element = driver.findElement(locator);
		Utils.highlight(element);
		Utils.sleep(100);
		assertThat(element.getAttribute("innerText"), containsString("Mozilla"));
		System.err
				.println("Vanilla USER-AGENT: " + element.getAttribute("innerText"));

		// Act
		try {
			UserAgent userAgent = new UserAgent("python 2.7");
			userAgent.platform("windows");
			// @formatter:off
			chromeDevTools.send(
					Network.setUserAgentOverride(
							userAgent.userAgent(), // userAgent 
							Optional.empty(), // acceptLanguage
							Optional.empty(), // platform
							Optional.empty()) // userAgentMetadatas
					);
			// @formatter:on
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
	public void test2() {
		// Arrange
		String brand = "Chrome";
		String version = "120";
		String platform = "windows";
		String platformVersion = "NT 6.0";
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
			UserAgentBrandVersion userAgentBrandVersion = new UserAgentBrandVersion(
					brand, version);
			// version is required
			List<UserAgentBrandVersion> brands = new ArrayList<>();
			brands.add(userAgentBrandVersion);
			// @formatter:off
			// Emulation.UserAgentMetadata
			// To be sent in Sec-CH-UA-* headers and returned in navigator.userAgentData
			// EXPERIMENTAL
			UserAgentMetadata userAgentMetadata = new UserAgentMetadata(
					Optional.of(brands), 
					Optional.empty(), // fullVersionList 
					Optional.empty(), // fullVersion 
					platform, 
					platformVersion, //  platformVersion 
					"amd64", // architecture 
					"", // model cannot be null (java.lang.NullPointerException)
					false, // mobile 
					Optional.empty(), // bitness, 
					Optional.of(false) // wow64
					);
			// @formatter:on

			UserAgent userAgent = new UserAgent(brand);
			userAgent.platform(platform);
			// @formatter:off
			chromeDevTools.send(
					Emulation.setUserAgentOverride(
							userAgent.userAgent(), // userAgent 
							Optional.empty(), // acceptLanguage
							Optional.empty(), // platform
							Optional.of(userAgentMetadata)) // userAgentMetadata
					);
			// @formatter:on
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception " + e.toString());
			throw (new RuntimeException(e));
		}
		driver.navigate().refresh();
		Utils.sleep(1000);
		/*
				for (String entry : Arrays.asList(new String[] { "USER-AGENT" })) {
					locator = By.xpath(String.format(
							"//*[@id=\"content-base\"]//table//th[contains(text(),\"%s\")]/../td",
							entry));
					element = driver.findElement(locator);
					assertThat(element.isDisplayed(), is(true));
					assertThat(element.getAttribute("innerText"), is(brand));
					System.err
							.println("Updated USER-AGENT: " + element.getAttribute("innerText"));
		
				}
		*/
		locator = By.xpath(
				"//*[@id=\"content-base\"]//table//th[contains(text(),\"USER-AGENT\")]/../td");

		element = driver.findElement(locator);
		assertThat(element.isDisplayed(), is(true));
		assertThat(element.getAttribute("innerText"), is(brand));
		System.err
				.println("Updated USER-AGENT: " + element.getAttribute("innerText"));
		locator = By.xpath(
				"//*[@id=\"content-base\"]//table//th[contains(text(),\"SEC-CH-UA-PLATFORM\")]/../td");
		element = driver.findElement(locator);
		assertThat(element.isDisplayed(), is(true));
		assertThat(element.getAttribute("innerText"),
				is(String.format("\"%s\"", platform)));
		System.err.println(
				"Updated SEC-CH-UA-PLATFORM: " + element.getAttribute("innerText"));
		locator = By.xpath(
				"//*[@id=\"content-base\"]//table//th[contains(text(),\"SEC-CH-UA-PLATFORM-VERSION\")]/../td");
		element = driver.findElement(locator);
		assertThat(element.isDisplayed(), is(true));
		assertThat(element.getAttribute("innerText"),
				containsString(platformVersion));
		System.err.println("Updated SEC-CH-UA-PLATFORM-VERSION: "
				+ element.getAttribute("innerText"));
	}

	// @Ignore
	@Test
	public void test3() {
		// Arrange
		String userAgentString = "python 3.8";
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
			UserAgent userAgent = new UserAgent(userAgentString);
			userAgent.platform("windows");
			// @formatter:off
			chromeDevTools.send(
					Emulation.setUserAgentOverride(
							userAgent.userAgent(), // userAgent 
							Optional.empty(), // acceptLanguage
							Optional.empty(), // platform
							Optional.empty()) // userAgentMetadatas
					);
			// @formatter:on
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
		assertThat(element.getAttribute("innerText"), is(userAgentString));
		System.err
				.println("Updated USER-AGENT: " + element.getAttribute("innerText"));
	}

}

