package com.github.sergueik.selenium;

import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.v119.dom.DOM;
import org.openqa.selenium.devtools.v119.dom.DOM.EnableIncludeWhitespace;
import org.openqa.selenium.devtools.v119.dom.DOM.PerformSearchResponse;
import org.openqa.selenium.devtools.v119.dom.model.Node;
import org.openqa.selenium.devtools.v119.dom.model.NodeId;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocument
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-querySelectorAll
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-performSearch
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#type-Node
 * https://chromedevtools.github.io/devtools-protocol/1-3/DOM/#event-setChildNodes
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class DOMPerformSearchDevToolsTest extends BaseDevToolsTest {

	private static String baseURL = "https://www.wikipedia.org";
	private final String selector = "a.other-project-link";

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

	@Test
	public void test1() {
		try {
			Node result = chromeDevTools
					.send(DOM.getDocument(Optional.of(1), Optional.of(true)));

			List<NodeId> results = chromeDevTools.send(
					DOM.querySelectorAll(result.getNodeId(), "a.other-project-link"));
			System.err.println(String.format("Found %d Node Ids:", results.size()));
			results.forEach(nodeId -> System.err
					.println(String.format("Id: %s\nHTML: %s", nodeId.toString(),
							chromeDevTools.send(DOM.getOuterHTML(Optional.of(nodeId),
									Optional.empty(), Optional.empty())))));
		} catch (DevToolsException e) {
			System.err.println("Exception (rethrown) " + e.getMessage());
			throw e;
		}
	}

	// NOTE: "DOM.getSearchResults" is returning array of zero NodeIds
	@Test
	public void test2() {
		try {
			PerformSearchResponse searchResponse = chromeDevTools
					.send(DOM.performSearch(selector, Optional.of(true)));
			Utils.sleep(1000); // no effect
			System.err.println(
					String.format("returned %d nodes", searchResponse.getResultCount()));
			List<NodeId> results = chromeDevTools.send(DOM.getSearchResults(
					searchResponse.getSearchId(), 0, searchResponse.getResultCount()));
			System.err.println(String.format("Node Ids: %s", results.toString()));
		} catch (DevToolsException e) {
			System.err.println("Exception (rethrown) " + e.getMessage());
			throw e;
		}
	}
}
