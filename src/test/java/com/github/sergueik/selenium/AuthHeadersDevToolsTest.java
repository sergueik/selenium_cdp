package com.github.sergueik.selenium;

/**
 * Copyright 2022 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.devtools.v101.network.Network;
import org.openqa.selenium.devtools.v101.network.model.Headers;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setExtraHTTPHeaders
 * https://chromedevtools.github.io/devtools-protocol/tot/Network#method-enable
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class AuthHeadersDevToolsTest extends BaseDevToolsTest {

	private int cnt = 0;
	// to test locally fire a dummy server and monitor the ariving the headers
	// server-side
	// driver.get("http://127.0.0.1:8080/demo/Demo");
	// otherwise just hit a learning web site

	private static String baseURL = "https://jigsaw.w3.org/HTTP/";
	private static String url = null;
	private String authString = null;
	private static Map<String, Object> headers = new HashMap<>();

	private final String username = "guest";
	private final String password = "guest";

	// NOTE: Default constructor cannot handle exception type
	// UnsupportedEncodingException thrown by implicit super constructor.
	// Must define an explicit constructor
	// private byte[] input = String.format("%s:%s", username,
	// password).getBytes("UTF-8");

	@BeforeClass
	public static void beforeClass() throws Exception {

		driver.get(baseURL);
	}

	@AfterClass
	public static void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	@After
	public void after() {
		chromeDevTools.clearListeners();
		chromeDevTools.send(Network.disable());
	}

	@Before
	public void before() throws Exception {
		byte[] input = String.format("%s:%s", username, password).getBytes("UTF-8");
		authString = new String(Base64.encodeBase64(input));
		chromeDevTools.send(Network.enable(Optional.of(100000000), Optional.empty(),
				Optional.empty()));
		// add event listener to log custom headers requests are sending with
		chromeDevTools.addListener(Network.requestWillBeSent(), o -> {
			headers.keySet().stream()
					.forEach(h -> System.err.println(
							String.format("request will be sent with extra header %s=%s", h,
									o.getRequest().getHeaders().get(h))));

		});
	}

	@Test
	public void test1() throws UnsupportedEncodingException {

		headers = new HashMap<>();
		headers.put("authorization", "Basic " + authString);
		headers.put("dummy", this.getClass().getName() + " addCustomHeadersTest");
		chromeDevTools.send(Network.setExtraHTTPHeaders(new Headers(headers)));

		url = "https://jigsaw.w3.org/HTTP/Basic/";
		driver.get(url);
		assertThat(driver.getPageSource(), containsString("Your browser made it!"));
		System.err.println(driver.getPageSource());
	}

	@Test
	public void test2() {
		chromeDevTools
				.send(Network.setExtraHTTPHeaders(new Headers(new HashMap<>())));

		url = "https://jigsaw.w3.org/HTTP/Basic/";
		driver.get(url);
		assertThat(driver.getPageSource(),
				not(containsString("Your browser made it!")));
		System.err.println(driver.getPageSource());
	}

	// TODO: generate a valid digest authentication
	// see also:
	// https://stackoverflow.com/questions/22556713/nonce-in-digest-authentication-process
	// NOTE: unstable, passes only occassionally
	@Test(expected = AssertionError.class)
	// @Test
	public void test3() {

		headers = new HashMap<>();

		headers.put("authorization",
				"Digest username=\"guest\", realm=\"test\", nonce=\"64ef0c5ea8a859ce1f6840274f3dfce5\", uri=\"/HTTP/Digest/\", response=\"3b6fed767f9383cd04153a1a1e8d80a9\"");
		chromeDevTools.send(Network.setExtraHTTPHeaders(new Headers(headers)));
		url = "https://jigsaw.w3.org/HTTP/Digest/";
		driver.get(url);
		assertThat(driver.getPageSource(), containsString("Your browser made it!"));
		System.err.println(driver.getPageSource());
	}
}
