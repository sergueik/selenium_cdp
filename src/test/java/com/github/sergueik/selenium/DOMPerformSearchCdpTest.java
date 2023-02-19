package com.github.sergueik.selenium;

/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.devtools.v110.dom.DOM;

import com.google.gson.JsonSyntaxException;
import static org.junit.Assert.assertTrue;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocument
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-performSearch
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#type-Node
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class DOMPerformSearchCdpTest extends BaseCdpTest {

	private static String baseURL = "https://www.wikipedia.org";
	private final String selector = "a.other-project-link";

	private static String command = "DOM.getDocument";

	private static Map<String, Object> params = new HashMap<>();
	private static Map<String, Object> result = new HashMap<>();
	private static List<Long> results = new ArrayList<>();
	public static Long nodeId = (long) -1;

	@After
	public void afterTest() {
		driver.get("about:blank");
		command = "DOM.disable";
		result = driver.executeCdpCommand(command, new HashMap<String, Object>());
	}

	@Before
	public void beforeTest() {
		command = "DOM.enable";
		params = new HashMap<String, Object>();
		params.put("includeWhitespace", "all");
		result = driver.executeCdpCommand(command, params);
		driver.get(baseURL);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test1() {

		try {
			command = "DOM.getDocument";
			params = new HashMap<>();
			params.put("pierce", false);
			params.put("depth", 1);
			// Act
			result = driver.executeCdpCommand(command, params);
			nodeId = Long.parseLong(
					((Map<String, Object>) result.get("root")).get("nodeId").toString());
			command = "DOM.querySelectorAll";
			params.clear();
			params.put("nodeId", nodeId);
			params.put("selector", selector);

			result = driver.executeCdpCommand(command, params);
			assertThat(result, hasKey("nodeIds"));
			results = (List<Long>) result.get("nodeIds");
			// Assert
			assertThat(results, notNullValue());
			assertTrue(results.size() != 0);
			System.err.println(String.format("Found %d Node Ids:", results.size()));
			results.forEach(nodeId -> {
				command = "DOM.getOuterHTML";
				params.clear();
				params.put("nodeId", nodeId);
				result = driver.executeCdpCommand(command, params);
				assertThat(result, notNullValue());
				assertTrue(result.containsKey("outerHTML"));
				System.err.println(String.format("Id: %s\nHTML: %s", nodeId.toString(),
						result.get("outerHTML")));
			});

		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			e.printStackTrace();
			throw (new RuntimeException(e));
		}
	}

	@SuppressWarnings("unchecked")
	// @Ignore
	@Test
	public void test2() {

		try {
			command = "DOM.performSearch";
			params = new HashMap<>();
			params.put("query", selector);
			params.put("includeUserAgentShadowDOM", false);
			// Act
			result = driver.executeCdpCommand(command, params);
			assertThat(results, notNullValue());
			assertThat(result, hasKey("searchId"));
			assertThat(result, hasKey("resultCount"));
			int resultCount = Integer.parseInt(result.get("resultCount").toString());
			System.err.println(String.format("Search %s Has %d results:",
					result.get("searchId").toString(), resultCount));
			command = "DOM.getSearchResults";
			params.clear();
			params.put("searchId", result.get("searchId"));
			params.put("fromIndex", 0);
			params.put("toIndex", resultCount - 1);

			result = driver.executeCdpCommand(command, params);
			assertThat(result, hasKey("nodeIds"));
			results = (List<Long>) result.get("nodeIds");
			// Assert
			assertThat(results, notNullValue());
			assertTrue(results.size() != 0);
			System.err.println(
					String.format("Found %d Node Ids: %s", results.size(), results));
			/*
			results.forEach(nodeId -> {
				command = "DOM.getOuterHTML";
				params.clear();
				params.put("nodeId", nodeId);
				result = driver.executeCdpCommand(command, params);
				assertThat(result, notNullValue());
				assertTrue(result.containsKey("outerHTML"));
				System.err.println(String.format("Id: %s\nHTML: %s", nodeId.toString(),
						result.get("outerHTML")));
			});
			*/

		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			e.printStackTrace();
			throw (new RuntimeException(e));
		}
	}
}
