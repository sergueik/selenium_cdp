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
import org.openqa.selenium.devtools.v115.dom.DOM;
import org.openqa.selenium.devtools.v115.dom.model.Node;
import org.openqa.selenium.devtools.v115.dom.model.NodeId;
import org.openqa.selenium.devtools.v115.dom.model.RGBA;
import org.openqa.selenium.devtools.v115.overlay.Overlay;
import org.openqa.selenium.devtools.v115.overlay.model.HighlightConfig;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Overlay/#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#type-RGBA
 * https://chromedevtools.github.io/devtools-protocol/tot/Overlay/#method-highlightNode
 * https://chromedevtools.github.io/devtools-protocol/tot/Overlay/#type-HighlightConfig
 * https://chromedevtools.github.io/devtools-protocol/tot/Overlay/#method-hideHighlight
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

		// based on:
		// https://github.com/rkeeves/selenium-tricks/blob/main/src/test/java/io/github/rkeeves/interoperability/CDPHighlightByNodeIdTest.java
		// https://youtu.be/OrOYvVf6tIM?t=328
		results.forEach((NodeId nodeId) -> {
			float alpha = (float) 0.5;
			RGBA contentColor = new RGBA(68, 255, 152, Optional.of(alpha));
			boolean showInfo = true;
			boolean showRulers = true;
			boolean showAccessibilityInfo = false;
			boolean showExtensionLines = true;
			boolean showStyles = false;
			HighlightConfig highlightConfig = new HighlightConfig(
					Optional.of(showInfo), Optional.of(showStyles),
					Optional.of(showRulers), Optional.of(showAccessibilityInfo),
					Optional.of(showExtensionLines), Optional.of(contentColor),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty());

			chromeDevTools
					.send(Overlay.highlightNode(highlightConfig, Optional.of(nodeId),
							Optional.empty(), Optional.empty(), Optional.empty()));
			Utils.sleep(500);
		});
	}
}
