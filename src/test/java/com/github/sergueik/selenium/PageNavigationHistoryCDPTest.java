package com.github.sergueik.selenium;
/**
 * Copyright 2022,2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-getNavigationHistory
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-resetNavigationHistory
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-navigateToHistoryEntry
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
public class PageNavigationHistoryCDPTest extends BaseCdpTest {

	private List<String> urls = new ArrayList<>();

	private static Gson gson = new Gson();
	private static String command = "Page.getNavigationHistory";
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();

	@Before
	public void before() {
		// Arrange
		urls.addAll(Arrays.asList(new String[] { "https://fr.wikipedia.org/wiki",
				"https://de.wikipedia.org/wiki", "https://es.wikipedia.org/wiki",
				"https://it.wikipedia.org/wiki", "https://ar.wikipedia.org/wiki",
				"https://en.wikipedia.org/wiki", "https://fi.wikipedia.org/wiki",
				"https://hu.wikipedia.org/wiki", "https://da.wikipedia.org/wiki",
				"https://pt.wikipedia.org/wiki" }));
		Collections.shuffle(urls);
		urls.forEach(url -> driver.get(url));

	}

	@After
	public void after() {
		// Arrange
		command = "Page.resetNavigationHistory";
		driver.executeCdpCommand(command, new HashMap<>());
		driver.get("about:blank");
	}

	@Test
	public void test1() {
		command = "Page.getNavigationHistory";
		result = driver.executeCdpCommand(command, new HashMap<String, Object>());
		System.err.println(
				command + " result: " + new ArrayList<String>(result.keySet()));
		System.err.print(
				"History entries : " + gson.toJson(result.get("entries")).toString());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test2() {
		// Act
		command = "Page.getNavigationHistory";
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
		}
		int entryId = Math.toIntExact(((Map<String, Long>) result2).get("id"));

		((List<Object>) result.get("entries")).stream().forEach(o -> {
			Map<String, Object> entry = (Map<String, Object>) o;
			if (entry.get("url").toString().indexOf("https://en.wikipedia.org/wiki",
					0) == 0) {
				final int entryId2 = Math
						.toIntExact(Long.parseLong(entry.get("id").toString()));
				command = "Page.navigateToHistoryEntry";
				params.clear();
				params.put("entryId", entryId2);
				result = driver.executeCdpCommand(command, params);
			}
		});
		// https://stackoverflow.com/questions/4355303/how-can-i-convert-a-long-to-int-in-java

	}
}
