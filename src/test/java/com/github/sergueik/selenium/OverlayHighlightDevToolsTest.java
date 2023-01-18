package com.github.sergueik.selenium;
/**
 * Copyright 2023 Serguei Kouzmine
 */

import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v108.dom.DOM;
import org.openqa.selenium.devtools.v108.dom.model.Node;
import org.openqa.selenium.devtools.v108.dom.model.NodeId;
import org.openqa.selenium.devtools.v108.dom.model.RGBA;
import org.openqa.selenium.devtools.v108.overlay.Overlay;
import org.openqa.selenium.devtools.v108.overlay.model.HighlightConfig;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/#method-failRequest
 * https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/devtools/NetworkInterceptor.html
 * https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/remote/http/Route.html
 * https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/remote/http/HttpRequest.html
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

// see also:
// https://github.com/rkeeves/selenium-tricks/blob/main/src/test/java/io/github/rkeeves/interoperability/CDPHighlightByNodeIdTest.java
public class OverlayHighlightDevToolsTest extends BaseDevToolsTest {
	private static String url = "https://www.wikipedia.org";
	private static final String selector = "*[id^='js-link-box'] > strong";

	@Before
	public void before() {
		chromeDevTools
				.send(DOM.enable(Optional.of(DOM.EnableIncludeWhitespace.ALL)));
		chromeDevTools.send(Overlay.enable());
		driver.get(url);
	}

	@After
	public void after() {
		chromeDevTools.send(Overlay.disable());
		chromeDevTools.send(DOM.disable());
	}

	@Test
	public void test() {
		Node result = chromeDevTools
				.send(DOM.getDocument(Optional.of(1), Optional.of(true)));

		List<NodeId> results = chromeDevTools
				.send(DOM.querySelectorAll(result.getNodeId(), selector));

		results.forEach((NodeId nodeId) -> {
			RGBA green = new RGBA(34, 177, 76, Optional.empty());
			HighlightConfig highlightConfig = highlightOfColor(green);
			chromeDevTools
					.send(Overlay.highlightNode(highlightConfig, Optional.of(nodeId),
							Optional.empty(), Optional.empty(), Optional.empty()));
			// Debug breakpoint the line below to see green
			System.err.println("It must be green");
			Utils.sleep(500);
		});
	}

	// origin:
	// https://github.com/rkeeves/selenium-tricks/blob/main/src/test/java/io/github/rkeeves/interoperability/CDPHighlightByNodeIdTest.java
	private static HighlightConfig highlightOfColor(RGBA green) {
		return new HighlightConfig(Optional.empty(), Optional.empty(),
				Optional.empty(), Optional.empty(), Optional.empty(),
				Optional.of(green), Optional.empty(), Optional.empty(),
				Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
				Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
				Optional.empty(), Optional.empty(), Optional.empty()
		// https://youtu.be/OrOYvVf6tIM?t=328
		);
	}
}
