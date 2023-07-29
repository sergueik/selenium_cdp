package com.github.sergueik.selenium;

/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.WebElement;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setExtraHTTPHeaders
 * https://chromedevtools.github.io/devtools-protocol/tot/Network#method-enable
 * see also: https://stackoverflow.com/questions/50834002/chrome-headless-browser-with-corporate-proxy-authetication-not-working/67321556#67321556
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

// see also:
// https://github.com/shsarkar08/PythonSeleniumCDP_APIs/blob/master/selenium_py_cdp_basic_auth.py
//
public class AuthHeadersCdpTest extends BaseCdpTest {

	private static String baseURL = "https://jigsaw.w3.org/HTTP/";
	private static String url = "https://jigsaw.w3.org/HTTP/Basic/";

	private final String username = "guest";
	private final String password = "guest";
	// NOTE: cannot initialize here:
	// Default constructor cannot handle exception type
	// UnsupportedEncodingException thrown by implicit super constructor. Must
	// define an explicit constructor
	// private byte[] input = String.format("%s:%s", username,
	// password).getBytes("UTF-8");
	private static String authString = null;

	private static Map<String, Object> headers = new HashMap<>();

	private static String command = null;

	private static Map<String, Object> params = new HashMap<>();

	@After
	public void afterTest() {
		command = "Network.disable";
		driver.executeCdpCommand(command, new HashMap<>());
		driver.get("about:blank");
	}

	@AfterClass
	public static void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		// NOTE: NPE without calling the superclass @BeforeClass method
		BaseCdpTest.beforeClass();
		// Cannot use super in a static context
		// super.beforeClass();
		driver.get(baseURL);
	}

	@Before
	public void beforeTest() throws UnsupportedEncodingException {
		// NOT passing any parameters
		command = "Network.enable";
		driver.executeCdpCommand(command, new HashMap<>());
		byte[] input = String.format("%s:%s", username, password).getBytes("UTF-8");
		authString = new String(Base64.encodeBase64(input));

		driver.get(baseURL);
	}

	@Test(expected = InvalidArgumentException.class)
	public void test3() throws UnsupportedEncodingException {
		params = new HashMap<>();
		params.put("authorization", "Basic " + authString);
		command = "Network.setExtraHTTPHeaders";
		// NOTE: org.openqa.selenium.InvalidArgumentException:
		// invalid argument: Invalid parameters
		driver.executeCdpCommand(command, params);
	}
	//

	@Test
	public void test1() throws UnsupportedEncodingException {
		headers = new HashMap<>();
		params = new HashMap<>();
		headers.put("authorization", "Basic " + authString);
		params.put("headers", headers);

		command = "Network.setExtraHTTPHeaders";
		driver.executeCdpCommand(command, params);

		driver.get(url);
		assertThat(driver.getPageSource(), containsString("Your browser made it!"));
		System.err.println(driver.getPageSource());
	}

	// NOTE: need to send extra HTTP Headers with blank value to fail to
	// authenticate ?
	@Ignore("failing to clear extra HTTP Headers")
	@Test
	public void test2() {
		params = new HashMap<>();
		params.put("headers", new HashMap<>());
		command = "Network.setExtraHTTPHeaders";
		driver.executeCdpCommand(command, params);

		driver.get(url);
		assertThat(driver.getPageSource(),
				not(containsString("Your browser made it!")));
		System.err.println(driver.getPageSource());
	}

}
