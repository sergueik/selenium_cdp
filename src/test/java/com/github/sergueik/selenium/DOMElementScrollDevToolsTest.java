package com.github.sergueik.selenium;
/**
 * Copyright 2024 Serguei Kouzmine
 */

import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.devtools.v134.dom.DOM;
import org.openqa.selenium.devtools.v134.dom.model.Node;
import org.openqa.selenium.devtools.v134.dom.model.NodeId;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-disable
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-focus
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-scrollIntoViewIfNeeded
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getContentQuads
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-performSearch
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

// see also:
// https://github.com/rkeeves/selenium-tricks/blob/main/src/test/java/io/github/rkeeves/interoperability/CDPHighlightByNodeIdTest.java
public class DOMElementScrollDevToolsTest extends BaseDevToolsTest {
	private static String url = "https://formy-project.herokuapp.com/form";
	private static final String selector = "input";

	@Before
	public void before() {
		chromeDevTools
				.send(DOM.enable(Optional.of(DOM.EnableIncludeWhitespace.ALL)));
		driver.get(url);
	}

	@After
	public void after() {
		chromeDevTools.send(DOM.disable());
	}

	@Test
	public void test() {
		Node result = chromeDevTools
				.send(DOM.getDocument(Optional.of(1), Optional.of(true)));

		List<NodeId> results = chromeDevTools
				.send(DOM.querySelectorAll(result.getNodeId(), selector));
		results.forEach((NodeId nodeId) -> {
		// @formatter:off
			chromeDevTools.send(DOM.scrollIntoViewIfNeeded(
					Optional.of(nodeId), //
					Optional.empty(),  // backendNodeId
					Optional.empty(),  // objectId
					Optional.empty() //rect
					));
		// @formatter:on
			Utils.sleep(500);
		});
	}
}
