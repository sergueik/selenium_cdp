package com.github.sergueik.selenium;

/**
 * Copyright 2023 Serguei Kouzmine
 */

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.v144.dom.DOM;
import org.openqa.selenium.devtools.v144.input.Input;

import com.google.gson.Gson;

import org.openqa.selenium.devtools.v144.dom.DOM.EnableIncludeWhitespace;
import org.openqa.selenium.devtools.v144.dom.model.Node;
import org.openqa.selenium.devtools.v144.dom.model.NodeId;
import org.openqa.selenium.devtools.v144.dom.model.Quad;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocument
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-querySelectorAll
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-querySelector
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getContentQuads
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#type-Quadhttps://chromedevtools.github.io/devtools-protocol/tot/Input/#method-dispatchMouseEvent
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-performSearch
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#type-Node
 * https://chromedevtools.github.io/devtools-protocol/1-3/DOM/#event-setChildNodes
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class DOMElementClickDevToolsTest extends BaseDevToolsTest {

	private static String baseURL = "https://www.wikipedia.org";
	// private final String selector = "a.other-project-link:nth(0)";
	// NOTE:
	// Web Driver exception in DOM.querySelectorAll (ignored):
	// {"code":-32000,"message":"DOM Error while querying"}
	// {params={selector=a.other-project-link:nth(1), nodeId=1},
	// cmd=DOM.querySelectorAll}

	private final String selector = "a.other-project-link";
	private Gson gson = new Gson();

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
	// @Test(expected = DevToolsException.class)
	@Test
	public void test1() {
		try {
			Node result = chromeDevTools.send(DOM.getDocument(Optional.of(1), Optional.of(true)));

			// Limit example to just one node
			List<NodeId> results = chromeDevTools.send(DOM.querySelectorAll(result.getNodeId(), selector));
			System.err.println(String.format("Found %d Node Ids:", results.size()) + ", explore first one");

			results.subList(0, 1).forEach(nodeId -> {
				
				// @formatter:off
				System.err.println(String.format("Id: %s\nHTML: %s", nodeId.toString(), chromeDevTools.send(
								DOM.getOuterHTML(
									Optional.of(nodeId), // nodeId
									Optional.empty(),  // backendNodeId
									Optional.empty(),  // objectId
									Optional.of(false) // includeShadowDOM 
								))));
				// @formatter:on

				
				// NOTE: WARNING: Unable to map result for DOM.getContentQuads
				// org.openqa.selenium.json.JsonException:
				// Unable to create instance of class
				// org.openqa.selenium.devtools.v144.dom.model.Quad
				// WARNING: Unable to process:
				// {"id":9,"result":{"quads":[[360.5500183105469,661.1500244140625,578.3624877929688,661.1500244140625,578.3624877929688,747.1500244140625,360.5500183105469,747.1500244140625]]},"sessionId":"B816DDEDA9AEA89C97AF770D6885CD7E"}
				// org.openqa.selenium.json.JsonException: Expected to read a
				// END_MAP
				// but instead have: END_COLLECTION. Last 128 characters read:
				// 9,661.1500244140625,578.3624877929688,661.1500244140625,578.3624877929688,747.1500244140625,360.5500183105469,747.1500244140625]
				List<Quad> result2 = chromeDevTools
						.send(DOM.getContentQuads(Optional.of(nodeId), Optional.empty(), Optional.empty()));
				System.err.println(String.format("result2: " + result2));

				Quad result3 = result2.get(0);
				// https://www.javadoc.io/doc/org.seleniumhq.selenium/selenium-devtools-v126/latest/org/openqa/selenium/devtools/v126/dom/model/package-summary.html
				// An array of quad vertices, x immediately followed by y for
				// each point, points clock-wise
				System.err.println(String.format("result3: " + result3.toString()));

				List<Double> data = (List<Double>) gson.fromJson(result3.toString(), List.class);
				// java.lang.ClassCastException:
				// class org.openqa.selenium.devtools.v144.dom.model.Quad
				// cannot be cast to class java.util.List
				Double x = data.get(0);
				Double y = data.get(1);

				chromeDevTools.send(Input.dispatchMouseEvent(Input.DispatchMouseEventType.MOUSEPRESSED, x, y,
						Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(1), Optional.of(1),
						Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
						Optional.empty(), Optional.empty(), Optional.of(Input.DispatchMouseEventPointerType.MOUSE)));
				Utils.sleep(100);
				chromeDevTools.send(Input.dispatchMouseEvent(Input.DispatchMouseEventType.MOUSERELEASED, x, y,
						Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(1), Optional.of(1),
						Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
						Optional.empty(), Optional.empty(), Optional.of(Input.DispatchMouseEventPointerType.MOUSE)));
			});

		} catch (DevToolsException e) {
			System.err.println("Exception (rethrown) " + e.getMessage());
			throw e;
		}
	}
}

