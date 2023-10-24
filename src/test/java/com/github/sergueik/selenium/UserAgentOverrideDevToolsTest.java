package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.idealized.Network.UserAgent;
import org.openqa.selenium.devtools.v118.emulation.Emulation;
//import org.openqa.selenium.devtools.Console;
// import org.openqa.selenium.devtools.Log;
import org.openqa.selenium.devtools.v118.network.Network;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setUserAgentOverride
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class UserAgentOverrideDevToolsTest extends BaseDevToolsTest {

	private static WebElement element = null;
	private static List<WebElement> elements = new ArrayList<>();
	private static By locator = By.xpath(
			"//*[@id=\"content-base\"]//table//th[contains(text(),\"USER-AGENT\")]/../td");

	private static String baseURL = "https://www.whatismybrowser.com/detect/what-http-headers-is-my-browser-sending";

	private final static int id = (int) (java.lang.Math.random() * 1_000_000);
	public final static String consoleMessage = "message from test id #" + id;

	@Test
	public void test1() {
		// the site may be down, and it can also reject autoated browsing
		// pingHost() does not work reliably yet
		// Assume.assumeTrue(pingHost("www.whoishostingthis.com", 443, 3));
		// Arrange
		driver.get(baseURL);
		locator = By.cssSelector("#content-base div.content-block-main");
		elements = driver.findElements(locator);
		if (elements.size() > 0) {
			// You have been blocked
			return;
		}

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
		// the site may be down, and it can also reject autoated browsing
		// pingHost() does not work reliably yet
		// Assume.assumeTrue(pingHost("www.whoishostingthis.com", 443, 3));
		// Arrange
		driver.get(baseURL);
		locator = By.cssSelector("#content-base div.content-block-main");
		elements = driver.findElements(locator);
		if (elements.size() > 0) {
			// You have been blocked
			return;
		}

		element = driver.findElement(locator);
		Utils.highlight(element);
		Utils.sleep(100);
		assertThat(element.getAttribute("innerText"), containsString("Mozilla"));
		System.err
				.println("Vanilla USER-AGENT: " + element.getAttribute("innerText"));

		// Act
		try {
			UserAgent userAgent = new UserAgent("python 3.8");
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
		assertThat(element.getAttribute("innerText"), is("python 3.8"));
		System.err
				.println("Updated USER-AGENT: " + element.getAttribute("innerText"));
	}

}

