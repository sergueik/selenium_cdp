package com.github.sergueik.selenium;

/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import com.google.gson.JsonSyntaxException;
import static org.hamcrest.CoreMatchers.is;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocument
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-querySelectorAll
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-querySelector
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getContentQuads
 * https://chromedevtools.github.io/devtools-protocol/tot/Input/#method-dispatchMouseEvent
 *  * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

// based on:
// https://github.com/estromenko/driverless-selenium/blob/init-project/driverless_selenium/webdriver.py
public class DOMElementClickCdpTest extends BaseCdpTest {

	private static String baseURL = "https://www.wikipedia.org";

	private WebElement element;
	private String selector = null;

	private static String command = null;

	private static Map<String, Object> params = new HashMap<>();
	private static Map<String, Object> result = new HashMap<>();
	private static List<Long> results = new ArrayList<>();
	private List<List<Object>> data = new ArrayList<>();
	private List<Object> data1 = new ArrayList<>();
	private Double value;
	private static Long nodeId = -1l;

	@After
	public void afterTest() {
		Utils.sleep(3000);
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
			// Arrange
			selector = "#js-link-box-it > strong";
			element = driver.findElement(By.cssSelector(selector));
			Utils.highlight(element);
			Point point = element.getLocation();
			System.err.println(
					String.format("Element location x: %d y: %d", point.x, point.y));

			command = "DOM.getDocument";
			params = new HashMap<>();
			params.put("pierce", false);
			params.put("depth", 1);
			// Act
			result = driver.executeCdpCommand(command, params);
			nodeId = Long.parseLong(
					((Map<String, Object>) result.get("root")).get("nodeId").toString());
			System.err.println(String.format("Found Root Node Id: %d", nodeId));
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
			results.subList(0, 1).forEach(nodeId -> {
				command = "DOM.getOuterHTML";
				params.clear();
				params.put("nodeId", nodeId);
				result = driver.executeCdpCommand(command, params);
				assertThat(result, notNullValue());
				assertTrue(result.containsKey("outerHTML"));
				System.err.println(String.format("Id: %s\nHTML: %s", nodeId.toString(),
						result.get("outerHTML")));
				command = "DOM.getContentQuads";
				params.clear();
				params.put("nodeId", nodeId);
				result = driver.executeCdpCommand(command, params);
				assertThat(result, notNullValue());
				assertTrue(result.containsKey("quads"));
				System.err.println(String.format("Id: %s\nquads: %s", nodeId.toString(),
						result.get("quads")));
				data = (List<List<Object>>) result.get("quads");
				data1 = (List<Object>) data.get(0);
				value = Double.parseDouble(data1.get(0).toString());
				int x = Math.round(value.longValue());
				value = Double.parseDouble(data1.get(1).toString());
				int y = Math.round(value.longValue());
				System.err.println(String.format("Click at x: %d y: %d", x, y));
				command = "Input.dispatchMouseEvent";
				params.clear();
				params.put("x", x);
				params.put("y", y);
				params.put("button", "left");
				params.put("type", "mousePressed");
				params.put("clickCount", 1);
				driver.executeCdpCommand(command, params);
				Utils.sleep(100);
				command = "Input.dispatchMouseEvent";
				params.clear();
				params.put("x", x);
				params.put("y", y);
				params.put("button", "left");
				params.put("clickCount", 1);

				params.put("type", "mouseReleased");
				driver.executeCdpCommand(command, params);
				Utils.sleep(1000);
				System.err.println("Navigated to: " + driver.getTitle());
			});

		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println(
					"Web Driver exception in " + command + " (ignored): " + e.getMessage()
							+ " " + Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			e.printStackTrace();
			throw (new RuntimeException(e));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test2() {

		try {
			// Arrange
			selector = "#js-link-box-de > strong";
			element = driver.findElement(By.cssSelector(selector));
			Utils.highlight(element);
			Point point = element.getLocation();
			System.err.println(
					String.format("Element location x: %d y: %d", point.x, point.y));

			command = "DOM.getDocument";
			params = new HashMap<>();
			params.put("pierce", false);
			params.put("depth", 1);
			// Act
			result = driver.executeCdpCommand(command, params);
			nodeId = Long.parseLong(
					((Map<String, Object>) result.get("root")).get("nodeId").toString());
			System.err.println(String.format("Found Root Node Id: %d", nodeId));
			command = "DOM.querySelector";
			params.clear();
			params.put("nodeId", nodeId);
			params.put("selector", selector);
			nodeId = -1l;
			result = driver.executeCdpCommand(command, params);
			assertThat(result, hasKey("nodeId"));
			nodeId = (Long) result.get("nodeId");
			// Assert
			assertThat(nodeId != -1l, is(true));
			System.err.println(String.format("Found %d Node Id:", nodeId));
			command = "DOM.getOuterHTML";
			params.clear();
			params.put("nodeId", nodeId);
			result = driver.executeCdpCommand(command, params);
			assertThat(result, notNullValue());
			assertTrue(result.containsKey("outerHTML"));
			System.err.println(String.format("Id: %s\nHTML: %s", nodeId.toString(),
					result.get("outerHTML")));
			command = "DOM.getContentQuads";
			params.clear();
			params.put("nodeId", nodeId);
			result = driver.executeCdpCommand(command, params);
			assertThat(result, notNullValue());
			assertTrue(result.containsKey("quads"));
			System.err.println(String.format("Id: %s\nquads: %s", nodeId.toString(),
					result.get("quads")));
			data = (List<List<Object>>) result.get("quads");
			data1 = (List<Object>) data.get(0);
			value = Double.parseDouble(data1.get(0).toString());
			int x = Math.round(value.longValue());
			value = Double.parseDouble(data1.get(1).toString());
			int y = Math.round(value.longValue());
			System.err.println(String.format("Click at x: %d y: %d", x, y));
			command = "Input.dispatchMouseEvent";
			params.clear();
			params.put("x", x);
			params.put("y", y);
			params.put("button", "left");
			params.put("type", "mousePressed");
			params.put("clickCount", 1);
			driver.executeCdpCommand(command, params);
			Utils.sleep(100);
			command = "Input.dispatchMouseEvent";
			params.clear();
			params.put("x", x);
			params.put("y", y);
			params.put("button", "left");
			params.put("clickCount", 1);
			params.put("type", "mouseReleased");
			driver.executeCdpCommand(command, params);
			System.err.println("Navigated to: " + driver.getTitle());
			Utils.sleep(1000);

		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println(
					"Web Driver exception in " + command + " (ignored): " + e.getMessage()
							+ " " + Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			e.printStackTrace();
			throw (new RuntimeException(e));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test3() {

		try {
			// Arrange
			selector = selector = "a.other-project-link:nth-of-type(1)";
			element = driver.findElement(By.cssSelector(selector));
			Utils.highlight(element);
			Point point = element.getLocation();
			System.err.println(
					String.format("Element location x: %d y: %d", point.x, point.y));

			command = "DOM.getDocument";
			params = new HashMap<>();
			params.put("pierce", false);
			params.put("depth", 1);
			// Act
			result = driver.executeCdpCommand(command, params);
			nodeId = Long.parseLong(
					((Map<String, Object>) result.get("root")).get("nodeId").toString());
			System.err.println(String.format("Found Root Node Id: %d", nodeId));
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
			results.subList(0, 1).forEach(nodeId -> {
				command = "DOM.getOuterHTML";
				params.clear();
				params.put("nodeId", nodeId);
				result = driver.executeCdpCommand(command, params);
				assertThat(result, notNullValue());
				assertTrue(result.containsKey("outerHTML"));
				System.err.println(String.format("Id: %s\nHTML: %s", nodeId.toString(),
						result.get("outerHTML")));
				command = "DOM.getContentQuads";
				params.clear();
				params.put("nodeId", nodeId);
				result = driver.executeCdpCommand(command, params);
				assertThat(result, notNullValue());
				assertTrue(result.containsKey("quads"));
				System.err.println(String.format("Id: %s\nquads: %s", nodeId.toString(),
						result.get("quads")));
				data = (List<List<Object>>) result.get("quads");
				data1 = (List<Object>) data.get(0);
				value = Double.parseDouble(data1.get(0).toString());
				int x = Math.round(value.longValue());
				value = Double.parseDouble(data1.get(1).toString());
				int y = Math.round(value.longValue());
				System.err.println(String.format("Click at x: %d y: %d", x, y));
				command = "Input.dispatchMouseEvent";
				params.clear();
				params.put("x", x);
				params.put("y", y);
				params.put("button", "left");
				params.put("type", "mousePressed");
				params.put("clickCount", 1);
				driver.executeCdpCommand(command, params);
				Utils.sleep(100);
				command = "Input.dispatchMouseEvent";
				params.clear();
				params.put("x", x);
				params.put("y", y);
				params.put("button", "left");
				params.put("clickCount", 1);

				params.put("type", "mouseReleased");
				driver.executeCdpCommand(command, params);
				Utils.sleep(1000);
				System.err.println("Navigated to: " + driver.getTitle());
			});

		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println(
					"Web Driver exception in " + command + " (ignored): " + e.getMessage()
							+ " " + Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			e.printStackTrace();
			throw (new RuntimeException(e));
		}
	}

}
