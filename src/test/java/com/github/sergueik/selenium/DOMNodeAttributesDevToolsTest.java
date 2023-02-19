package com.github.sergueik.selenium;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.v110.dom.DOM;
import org.openqa.selenium.devtools.v110.dom.DOM.EnableIncludeWhitespace;
import org.openqa.selenium.devtools.v110.dom.model.Node;
import org.openqa.selenium.devtools.v110.dom.model.NodeId;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocument
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getOuterHTML
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getAttributes
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#type-Node
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getContainerForNode
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class DOMNodeAttributesDevToolsTest extends BaseDevToolsTest {

	private static String baseURL = "https://www.wikipedia.org";
	private final String selector = "div.central-featured-lang[lang='ru']";

	private static String command = "DOM.getDocument";
	private static Node result;
	private static Map<String, Object> results = new HashMap<>();
	private static NodeId nodeId = null;
	private static List<String> attributes = new ArrayList<>();

	@After
	public void afterTest() {
		chromeDevTools.send(DOM.disable());
		driver.get("about:blank");
	}

	@Before
	public void beforeTest() {
		EnableIncludeWhitespace enableIncludeWhitespace = EnableIncludeWhitespace.ALL;
		driver.get(baseURL);
		chromeDevTools.send(DOM.enable(Optional.of(enableIncludeWhitespace)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test1() {
		try {
			result = chromeDevTools
					.send(DOM.getDocument(Optional.of(1), Optional.of(true)));

			nodeId = chromeDevTools
					.send(DOM.querySelector(result.getNodeId(), selector));

			attributes = chromeDevTools.send(DOM.getAttributes(nodeId));
			assertThat(attributes.size(), greaterThan(2));
			results = listToMap(attributes);
			assertThat(results, hasKey("lang"));
			String lang = (String) results.get("lang");
			System.err.println(String.format("lang: %s", lang));

			System.err.println(String.format("Id: %s", nodeId.toString()));
			System.err
					.println(chromeDevTools.send(DOM.getOuterHTML(Optional.of(nodeId),
							Optional.empty(), Optional.empty())));
		} catch (DevToolsException e) {
			System.err.println("Exception (rethrown) " + e.getMessage());
			throw e;
		}
	}

	// origin:
	// http://www.java2s.com/example/java/java.util/convert-array-to-map.html
	//
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <K, V> Map<K, V> listToMap(List<String> objects) {
		HashMap map = new HashMap();
		Object key = null;
		for (final String object : objects) {
			// System.err.println("processing " + object.toString());
			if (key == null) {
				key = object;
			} else {
				map.put(key, object);
				key = null;
			}
		}
		return map;
	}

}
