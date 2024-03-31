package com.github.sergueik.selenium;
/**
 * Copyright 2023,2024 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
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
public class AuthHeadersFailingCdpTest extends BaseCdpTest {

	private static String baseURL = "https://jigsaw.w3.org/HTTP/";
	private static String url = "https://jigsaw.w3.org/HTTP/Basic/";

	private final String username = "guest";
	private final String password = "wrong password";
	// NOTE: cannot initialize authString here:
	// Default constructor cannot handle exception type
	// UnsupportedEncodingException thrown by implicit super constructor. Must
	// define an explicit constructor
	// private byte[] input = String.format("%s:%s", username,
	// password).getBytes("UTF-8");
	private static String authString = null;
	private static WebElement element;

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
		authString = new String(Base64.encodeBase64(
				String.format("%s:%s", username, password).getBytes("UTF-8")));
		driver.get(baseURL);
	}

	@Test
	public void test1() throws UnsupportedEncodingException {
		if (runHeadless)
			return;
		headers = new HashMap<>();
		params = new HashMap<>();
		headers.put("authorization", "Basic " + authString);
		params.put("headers", headers);

		command = "Network.setExtraHTTPHeaders";
		driver.executeCdpCommand(command, params);

		driver.get(url);

		element = driver.findElement(By.xpath("//body"));
		assertThat(element, notNullValue());
		assertThat(element.getText(), is(""));
	}

	// NOTE: occasionally in headless test, the body is
	// "Unauthorized access\nYou are denied access to this resource."
	@Test
	public void test2() {
		if (!runHeadless)
			return;
		command = "Network.setExtraHTTPHeaders";
		headers = new HashMap<>();
		params = new HashMap<>();
		headers.put("authorization", "Basic " + authString);
		params.put("headers", headers);
		driver.executeCdpCommand(command, params);

		driver.get(url);
		element = driver.findElement(By.xpath("//body"));
		assertThat(element, notNullValue());
		assertThat(element.getText(), is(""));
		// assertThat(driver.getTitle(), is("Unauthorized access"));
		System.err.println(driver.getPageSource());
	}

}
