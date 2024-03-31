package com.github.sergueik.selenium;

/**
 * Copyright 2022-2024 Serguei Kouzmine
 */


import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openqa.selenium.WebDriverException;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see also:
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-emulateNetworkConditions
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#type-ConnectionType
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
@SuppressWarnings("deprecation")
public class NetworkConditionsCdpTest extends BaseCdpTest {

	private static String baseURL = "https://www.wikipedia.org";
	private static String command = "Network.emulateNetworkConditions";
	private static Map<String, Object> params = new HashMap<>();
	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();


	@Before
	public void beforeTest() {
		params = new HashMap<>();
		params.put("offline", false);
		params.put("latency", 100L);
		params.put("downloadThroughput", -1L);
		params.put("uploadThroughput", -1L);
	}

	@After
	public void afterTest() {
		params.put("offline", false);
		driver.executeCdpCommand(command, params);
		driver.get("about:blank");
	}

	// https://www.baeldung.com/junit-assert-exception
	@Test(expected = WebDriverException.class)
	public void test1() throws IOException {
		params.put("offline", true);
		driver.executeCdpCommand(command, params);
		try {
			driver.get(baseURL);
		} catch (WebDriverException e) {
			assertThat(e.getMessage(), containsString("ERR_INTERNET_DISCONNECTED"));
			System.err.println("Exception (ignored): " + e.getMessage());
			// e.printStackTrace(System.err);
			throw (e);
		} finally {
			params.put("offline", false);
			driver.executeCdpCommand(command, params);
		}
	}

	@Test
	public void test2() {
		exceptionRule.expect(WebDriverException.class);
		exceptionRule.expectMessage(containsString("ERR_INTERNET_DISCONNECTED"));
		params.put("offline", true);
		driver.executeCdpCommand(command, params);
		driver.get(baseURL);

	}

}

