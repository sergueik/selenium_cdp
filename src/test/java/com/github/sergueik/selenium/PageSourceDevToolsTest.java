package com.github.sergueik.selenium;

/**
 * Copyright 2022,2024 Serguei Kouzmine
 */


import java.util.Arrays;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.devtools.v144.dom.DOM;
import org.openqa.selenium.devtools.v144.dom.model.Node;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocument 
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getOuterHTML
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-disable
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

// https://github.com/estromenko/driverless-selenium/blob/init-project/driverless_selenium/webdriver.py#L183
// see also: https://codepedia.info/angularjs-call-ajax-function-on-page-load
// https://angular.io/tutorial/tour-of-heroes/toh-pt6

public class PageSourceDevToolsTest extends BaseDevToolsTest {

	private String url = null;

	@Before
	public void before() {
		chromeDevTools
				.send(DOM.enable(Optional.of(DOM.EnableIncludeWhitespace.ALL)));

	}

	@After
	public void after() {
		chromeDevTools.send(DOM.disable());
	}

	@Test
	public void test1() {
		url = "http://www.wikipedia.org";
		driver.get(url);
		Node result = chromeDevTools
				.send(DOM.getDocument(Optional.of(1), Optional.of(false)));
		String pageSource = chromeDevTools.send(DOM.getOuterHTML(
				Optional.of(result.getNodeId()), Optional.empty(), Optional.empty(), Optional.of(false)));
		System.err.println("page source: " + pageSource);
	}

	@Test
	public void test2() {
		// Arrange
		String page = "call_ajax.html";
		driver.get(Utils.getPageContent(page));
		Node result = chromeDevTools
				.send(DOM.getDocument(Optional.of(1), Optional.of(false)));
		String pageSource = chromeDevTools.send(DOM.getOuterHTML(
				Optional.of(result.getNodeId()), Optional.empty(), Optional.empty(), Optional.of(false)));
		System.err.println("page source: " + pageSource);
	}

}
