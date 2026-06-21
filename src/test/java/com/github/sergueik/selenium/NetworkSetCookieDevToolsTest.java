package com.github.sergueik.selenium;

/**
 * Copyright 2025,2026 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.WebDriver.Options;

import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.HasDevTools;

import org.openqa.selenium.devtools.v149.network.Network;

// NOTE: the import org.openqa.selenium.devtools.v149.network.model.Cookie collides with another import statement
// import org.openqa.selenium.Cookie;
import org.openqa.selenium.devtools.v149.network.model.Cookie;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-deleteCookies
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-getCookies
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setCookie
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-clearBrowserCookies
 *
 * https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/WebDriver.Options.html
 * https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/class-use/Cookie.html
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class NetworkSetCookieDevToolsTest extends BaseDevToolsTest {

	private static String baseURL = "https://www.selenium.dev";

	private static final String domain = baseURL.replaceFirst("https://", "");

	private static final String name = "cheese"; // Cookie name
	private static final String value = "cheese"; // Cookie value

	@After
	public void after() {
		chromeDevTools.send(Network.deleteCookies(
				name, // name of the cookies to remove.
				Optional.of(baseURL), // url
				Optional.of(domain), // domain
				Optional.empty(), // path
				Optional.empty() // partitionKey

		));
		chromeDevTools.send(Network.clearBrowserCookies());

	}

	@Before
	public void before() throws Exception {
		chromeDevTools.send(Network.enable(Optional.of(100000000), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
		driver.get(baseURL);
		chromeDevTools.send(Network.setCookie(
				name, // name
				value, // value
				Optional.empty(), // path
				Optional.of(baseURL.replaceFirst("https://", "")), // domain
				Optional.empty(), // sameSite
				Optional.of(true), // secure
				Optional.empty(), // httpOnly
				Optional.empty(), // expires
				Optional.empty(), // sameParty
				Optional.empty(), // sourceScheme
				Optional.empty(), // sourcePort
				Optional.empty(), // priority
				Optional.empty()  // partitionKey
		));
		driver.get("about:blank");

	}


	@Test
	public void test1() {
		// Navigate to the page again to see the cookie applied
		driver.get(baseURL);
		org.openqa.selenium.Cookie cookie = driver.manage().getCookieNamed(name);
		assertThat(cookie.getValue(), is(value));
		assertThat(cookie.getDomain(), is(domain));
	}

	@Test
	public void test2() {

		// Navigate to the page again to see the cookie applied
		driver.get(baseURL);
		// Verify the cookie is set
		List<Cookie> cookies = chromeDevTools
				.send(Network.getCookies(
						Optional.empty() // urls
						));
		assertThat(cookies.size(), greaterThan(0));
		//System.err.println(cookies.size());

		Cookie cookie = cookies.stream()
				.filter((o) -> o.getDomain().equals(domain)).collect(Collectors.toList()).get(0);
		assertThat(cookie.getValue(), is(value));
		assertThat(cookie.getDomain(), is(domain));
	}

}
