package com.github.sergueik.selenium;

/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.v109.css.CSS;
import org.openqa.selenium.devtools.v109.css.model.CSSComputedStyleProperty;
import org.openqa.selenium.devtools.v109.dom.DOM;
import org.openqa.selenium.devtools.v109.dom.DOM.EnableIncludeWhitespace;
import org.openqa.selenium.devtools.v109.dom.model.Node;
import org.openqa.selenium.devtools.v109.dom.model.NodeId;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocument
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getOuterHTML
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getAttributes
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#type-Node
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getContainerForNode
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
public class ComputedStyleDevToolsTest extends BaseDevToolsTest {

	private static String baseURL = "https://getbootstrap.com/docs/4.0/components/buttons/";
	private final ArrayList<String> classes = new ArrayList<String>(
			Arrays.asList("btn-primary", "btn-secondary", "btn-success", "btn-danger",
					"btn-warning", "btn-info", "btn-light", "btn-dark", "btn-link"));

	private static String selector = null;
	private WebElement element;
	private static Node result;

	private static NodeId nodeId = null;
	private static NodeId rootNodeId = null;
	private static boolean debug = false;

	@After
	public void afterTest() {
		chromeDevTools.send(DOM.disable());
		chromeDevTools.send(CSS.disable());
		driver.get("about:blank");
	}

	@Before
	public void beforeTest() {
		EnableIncludeWhitespace enableIncludeWhitespace = EnableIncludeWhitespace.ALL;
		driver.get(baseURL);
		chromeDevTools.send(DOM.enable(Optional.of(enableIncludeWhitespace)));
		chromeDevTools.send(CSS.enable());
	}

	@Test
	public void test1() {
		driver.get(baseURL);
		for (String data : classes) {
			selector = String.format("div.bd-example button.%s", data);
			element = driver.findElement(By.cssSelector(selector));
			assertThat(element, notNullValue());
			String value = styleOfElement(element, "background-color");

			System.err.println(
					element.getText() + " computed style: background-color: " + value);
		}
		try {
			result = chromeDevTools
					.send(DOM.getDocument(Optional.of(1), Optional.of(true)));
			rootNodeId = result.getNodeId();
			for (String data : classes) {
				selector = String.format("div.bd-example button.%s", data);

				nodeId = chromeDevTools.send(DOM.querySelector(rootNodeId, selector));

				List<CSSComputedStyleProperty> properties = chromeDevTools
						.send(CSS.getComputedStyleForNode(nodeId));
				assertThat(properties.size(), greaterThan(2));

				properties.stream().forEach((CSSComputedStyleProperty property) -> {
					if (debug)
						System.err
								.println(String.format("element: %s", property.getName()));
					if (property.getName().contains("background-color")) {

						System.err.println(
								String.format("computed style: %s", property.getValue()));

					}
				});
				System.err
						.println(chromeDevTools.send(DOM.getOuterHTML(Optional.of(nodeId),
								Optional.empty(), Optional.empty())));
			}
		} catch (DevToolsException e) {
			System.err.println("Exception (rethrown) " + e.getMessage());
			throw e;
		}
	}

	protected String styleOfElement(WebElement element, Object... arguments) {
		return (String) Utils.executeScript(Utils.getScriptContent("getStyle.js"),
				element, arguments);
	}

}
