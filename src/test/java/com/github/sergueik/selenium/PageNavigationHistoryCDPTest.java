package com.github.sergueik.selenium;
/**
 * Copyright 2022 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-getNavigationHistory
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-resetNavigationHistory
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-navigateToHistoryEntry
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
public class PageNavigationHistoryCDPTest extends BaseCdpTest {

	private final static List<String> urls = Arrays.asList(
			"https://www.wikipedia.org",
			"https://chromedevtools.github.io/devtools-protocol/",
			"https://www.selenium.dev");

	private static String command = "Page.getNavigationHistory";
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();

	@Before
	public void before() {
		// Arrange
		for (String url : urls)
			driver.get(url);

	}

	@After
	public void after() {
		// Arrange
		command = "Page.resetNavigationHistory";
		driver.executeCdpCommand(command, new HashMap<>());
		driver.get("about:blank");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test() {
		// Act
		result = driver.executeCdpCommand(command, new HashMap<>());
		System.err.println(result);
		// Assert
		assertThat(result, notNullValue());
		assertThat(result.containsKey("currentIndex"), is(true));
		assertThat(result.containsKey("entries"), is(true));
		assertThat(result.get("entries") instanceof List<?>, is(true));
		assertThat(((List<Object>) result.get("entries")).size(), greaterThan(1));
		Object result2 = ((List<Object>) result.get("entries")).get(1);
		assertThat(result2 instanceof Map<?, ?>, is(true));
		for (String key : Arrays.asList("id", "url", "title", "userTypedURL",
				"transitionType")) {
			assertThat(((Map<String, ?>) result2).containsKey(key), is(true));
			// TODO: Page.TransitionType
			// Allowed Values: link, typed, address_bar, auto_bookmark, auto_subframe,
			// manual_subframe, generated, auto_toplevel, form_submit, reload,
			// keyword, keyword_generated, other
		}
		// https://stackoverflow.com/questions/4355303/how-can-i-convert-a-long-to-int-in-java
		int entryId = Math.toIntExact(((Map<String, Long>) result2).get("id"));
		params.put("entryId", entryId);

		result = driver.executeCdpCommand("Page.navigateToHistoryEntry", params);
	}
}
