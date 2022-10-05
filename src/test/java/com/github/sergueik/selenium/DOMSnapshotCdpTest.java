package com.github.sergueik.selenium;

/**
 * Copyright 2022 Serguei Kouzmine
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

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;

import com.google.gson.JsonSyntaxException;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/DOMSnapshot/#method-disable
 * https://chromedevtools.github.io/devtools-protocol/tot/DOMSnapshot/#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/DOMSnapshot/#method-captureSnapshot
 * https://chromedevtools.github.io/devtools-protocol/tot/DOMSnapshot/#type-DocumentSnapshot
 * https://chromedevtools.github.io/devtools-protocol/tot/DOMSnapshot/#type-NodeTreeSnapshot
 * https://chromedevtools.github.io/devtools-protocol/tot/DOMSnapshot/#type-LayoutTreeSnapshot
 * https://chromedevtools.github.io/devtools-protocol/tot/DOMSnapshot/#type-TextBoxSnapshot
 * see also:
 * https://stackoverflow.com/questions/58099695/is-there-a-way-in-hamcrest-to-test-for-a-value-to-be-a-number
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class DOMSnapshotCdpTest extends BaseCdpTest {

	private static String command = null;
	private static List<Object> documents = new ArrayList<Object>();
	private static Map<String, Object> params = new HashMap<>();
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> data = new HashMap<>();
	private static Map<String, Object> data2 = new HashMap<>();
	private final static String baseURL = "https://www.wikipedia.org";

	@After
	public void afterTest() {
		command = "DOMSnapshot.disable";
		driver.executeCdpCommand(command, new HashMap<String, Object>());
		driver.get("about:blank");
	}

	@Before
	public void beforeTest() {
		command = "DOMSnapshot.enable";
		driver.executeCdpCommand(command, new HashMap<String, Object>());
		driver.get(baseURL);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test1() {

		try {
			// Act
			command = "DOMSnapshot.captureSnapshot";
			params = new HashMap<String, Object>();
			params.put("computedStyles", new ArrayList<String>());
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, notNullValue());
			assertThat(result, hasKey("documents"));
			// documents - the nodes in the DOM tree. The DOMNode at index 0 corresponds to
			// the root document.
			documents = (List<Object>) result.get("documents");

			data = (Map<String, Object>) documents.get(0);

			assertThat(data, notNullValue());

			for (String field : Arrays.asList(new String[] { "documentURL", "title", "baseURL", "contentLanguage",
					"encodingName", "publicId", "systemId", "frameId", "nodes", "layout", "textBoxes", "scrollOffsetX",
					"scrollOffsetY", "contentWidth", "contentHeight" })) {
				assertThat(data, hasKey(field));
			}
			data2 = (Map<String, Object>) data.get("nodes");
			for (String field : Arrays.asList(new String[] { "nodeName", "nodeValue", "nodeType", "attributes",
					"currentSourceURL", "originURL" })) {
				assertThat(data2, hasKey(field));
			}
			data2 = (Map<String, Object>) data.get("layout");
			for (String field : Arrays.asList(new String[] { "nodeIndex", "styles", "text",
					"bounds" /* , "offsetRects", "scrollRects", "clientRects", "stackingContexts" */ })) {
				assertThat(data2, hasKey(field));
			}
			data2 = (Map<String, Object>) data.get("textBoxes");
			for (String field : Arrays.asList(new String[] { "start", "length", "bounds", "layoutIndex" })) {
				assertThat(data2, hasKey(field));
			}
			long index = (long) data.get("title");
			assertThat(result, hasKey("strings"));
			List<String> strings = (List<String>) result.get("strings");

			assertThat(strings, notNullValue());
			assertThat(strings.size(), greaterThan((int) index));
			assertThat(strings.get((int) index), notNullValue());
			System.err.println("Page Title index: " + index + " value: " + strings.get((int) index));
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): " + e.toString());
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
	@Test
	public void test2() {

		try {
			// Act
			command = "DOMSnapshot.getSnapshot";
			params = new HashMap<String, Object>();
			params.put("computedStyles", new ArrayList<String>());
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, notNullValue());
			for (String field : Arrays.asList(new String[] { "domNodes", "layoutTreeNodes", "computedStyles" })) {
				assertThat(result, hasKey(field));
				documents = (List<Object>) result.get(field);
				assertThat(documents, notNullValue());
				data = (Map<String, Object>) documents.get(0);
				assertThat(data, notNullValue());
			}
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): " + e.toString());
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
