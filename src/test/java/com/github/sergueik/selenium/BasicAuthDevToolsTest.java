package com.github.sergueik.selenium;

/**
 * Copyright 2022 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.HasAuthentication;
import org.openqa.selenium.UsernameAndPassword;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setExtraHTTPHeaders
 * https://chromedevtools.github.io/devtools-protocol/tot/Network#method-enable
 * 
 * based on:
 * https://github.com/024RahulRaman/ChromeDevTools/blob/master/src/chromeDevTools/BasicAuthentication.java
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class BasicAuthDevToolsTest extends BaseDevToolsTest {

	private static Map<String, Object> headers = new HashMap<>();

	private static final String username = "guest";
	private static final String password = "guest";

	private static String baseURL = String
			.format("http://httpbin.org/basic-auth/%s/%s", username, password);

	@Test
	public void test1() throws UnsupportedEncodingException {
		Predicate<URI> uriPredicate = uri -> uri.getHost().contains("httpbin.org");
		((HasAuthentication) driver).register(uriPredicate,
				UsernameAndPassword.of(username, password));
		driver.get(baseURL);
		Utils.sleep(100);
		String pageSource = driver.getPageSource();
		assertThat(pageSource, containsString("\"authenticated\": true"));
		System.err.println(pageSource);

	}
}
