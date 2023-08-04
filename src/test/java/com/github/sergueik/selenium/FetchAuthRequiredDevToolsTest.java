package com.github.sergueik.selenium;

/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.devtools.v115.fetch.Fetch;
import org.openqa.selenium.devtools.v115.fetch.model.AuthChallengeResponse;
import org.openqa.selenium.devtools.v115.fetch.model.AuthChallengeResponse.Response;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/#method-disable
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/#method-continueRequest
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/#method-continueWithAuth
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/#type-AuthChallengeResponse
 *  
 * based on:
 * https://stackoverflow.com/questions/50834002/chrome-headless-browser-with-corporate-proxy-authetication-not-working/67321556#67321556
 * see also:
 * https://www.programsbuzz.com/article/selenium-intercept-requests
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class FetchAuthRequiredDevToolsTest extends BaseDevToolsTest {

	private static Map<String, Object> headers = new HashMap<>();

	private static final String username = "user";
	private static final String password = "guest";

	private static String baseURL = String
			.format("http://httpbin.org/basic-auth/%s/%s", username, password);

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
		chromeDevTools.send(Fetch.disable());
	}

	@Before
	public void before() throws UnsupportedEncodingException {
		chromeDevTools.send(Fetch.enable(Optional.empty(), Optional.of(true)));
	}

	@Test
	public void test1() {
		// @formatter:off
		chromeDevTools.addListener(Fetch.requestPaused(),
			requestPaused -> chromeDevTools.send(
				Fetch.continueRequest(
					requestPaused.getRequestId(), // requestId 
					Optional.empty(), // url 
					Optional.empty(), // method
					Optional.empty(), // postData
					Optional.empty(), // headers
					Optional.empty() // interceptResponse
				)
			)
		);
		// @formatter:on
		// @formatter:off
		chromeDevTools.addListener(Fetch.authRequired(),
			authRequired -> chromeDevTools.send(
				Fetch.continueWithAuth(
					authRequired.getRequestId(),
					new AuthChallengeResponse(
						Response.PROVIDECREDENTIALS,
						Optional.of(username), 
						Optional.of(password)
					)
				)
			)
		);
		// @formatter:on
		driver.get(baseURL);
		Utils.sleep(100);
		String pageSource = driver.getPageSource();
		assertThat(pageSource, containsString("\"authenticated\": true"));
		// System.err.println(pageSource);

	}

}
