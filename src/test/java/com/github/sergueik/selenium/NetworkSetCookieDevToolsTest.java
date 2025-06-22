package com.github.sergueik.selenium;

/**
 * Copyright 2025 Serguei Kouzmine
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

import org.openqa.selenium.Cookie;

import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v109.network.Network;
// the import org.openqa.selenium.devtools.v127.network.model.Cookie collides with another import statement
// import org.openqa.selenium.devtools.v127.network.model.Cookie;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * 
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class NetworkSetCookieDevToolsTest extends BaseDevToolsTest {

	private static String baseURL = "https://www.selenium.dev";

	private static final String name = "cheese"; // Cookie name
	private static final String value = "cheese"; // Cookie value
	private static final String domain = baseURL.replaceFirst("https://", "");

	@After
	public void after() {
		chromeDevTools.send(Network.deleteCookies(name, // Cookie value
				Optional.of(baseURL), // Optional: URL
				Optional.of(domain), // Optional: Domain
				Optional.empty() // Optional: Path
				// NOTE: method signature change
				// Optional.empty(), // Optional: Path
				// Optional.empty() // Optional: Partition key
		));
		chromeDevTools.send(Network.clearBrowserCookies());

	}

	@Before
	public void before() throws Exception {
		chromeDevTools.send(Network.enable(Optional.of(100000000), Optional.empty(), Optional.empty()));
		driver.get(baseURL);
	}

	@Test
	public void test1() {

		chromeDevTools.send(Network.setCookie(name, // Cookie name
				value, // Cookie value
				Optional.empty(), // Optional: Path
				Optional.of(baseURL.replaceFirst("https://", "")), // Optional: Domain
				Optional.empty(), // Optional: SameSite
				Optional.of(true), // Optional: Secure
				Optional.empty(), // Optional: HttpOnly
				Optional.empty(), // Optional: Expiration date
				Optional.empty(), // Optional: SameParty
				Optional.empty(), // Optional: Source Scheme
				Optional.empty(), // Optional: Source Port
				Optional.empty(), // Optional: Priority
				Optional.empty(), // Optional: URL
				Optional.empty() // Optional: Expires time
		));

		// Verify the cookie is set
		driver.get(baseURL);
		// Navigate to the page again to see the cookie applied
		Cookie cookie = driver.manage().getCookieNamed(name);
		assertThat(cookie.getValue(), is(value));
		
				List<org.openqa.selenium.devtools.v109.network.model.Cookie>cookies = 
				chromeDevTools.send(Network.getCookies(Optional.empty()));
		// Navigate to the page again to see the cookie applied
		
		assertThat(cookies.size(), greaterThan(0));
		// System.err.println(cookies);
		System.err.println(cookies.size());

		org.openqa.selenium.devtools.v109.network.model.Cookie cookie2 = cookies.stream()
				.filter((o) -> o.getDomain().equals(domain)).collect(Collectors.toList()).get(0);
		assertThat(cookie2.getValue(), is(value));
		assertThat(cookie2.getDomain(), is(domain));

	}

}
