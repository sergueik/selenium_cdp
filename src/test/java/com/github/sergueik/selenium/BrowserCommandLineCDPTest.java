package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.is;

/**
 * Copyright 2022 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge inspired
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-getBrowserCommandLine
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class BrowserCommandLineCDPTest extends BaseCdpTest {

	private final static String url = "about:blank";

	private static String command = "Browser.getBrowserCommandLine";
	private static Map<String, Object> result = new HashMap<>();
	private static List<String> results;

	@Test
	public void test() {
		// Act
		result = driver.executeCdpCommand(command, new HashMap<>());
		// Assert
		assertThat(result, notNullValue());

		assertThat(result instanceof Map<?, ?>, is(true));
		assertThat(result.containsKey("arguments"), is(true));
		// histograms
		assertThat(result.get("arguments") instanceof List<?>, is(true));
		results = ((List<String>) result.get("arguments"));
		assertThat(results.size(), greaterThan(1));
		System.err.println(String.join("\n", results));
	}

}
