package com.github.sergueik.selenium;

/**
 * Copyright 2026 Serguei Kouzmine
 */


import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.v153.network.Network;
import org.openqa.selenium.devtools.v153.page.model.FrameId;
import org.openqa.selenium.devtools.v153.webmcp.WebMCP;
import org.openqa.selenium.devtools.v153.webmcp.model.Tool;
import org.openqa.selenium.devtools.v153.webmcp.model.ToolInvoked;
import org.openqa.selenium.devtools.v153.webmcp.model.ToolResponded;
import org.openqa.selenium.devtools.v153.webmcp.model.Annotation;
import com.google.gson.Gson;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/#/WebMCP.disable
 * https://chromedevtools.github.io/devtools-protocol/#/WebMCP.enable
 * https://chromedevtools.github.io/devtools-protocol/#/WebMCP.invokeTool
 * https://chromedevtools.github.io/devtools-protocol/#/WebMCP.toolInvoked
 *
 * see also:
 * https://medium.com/@LakshmiNarayana_U/i-built-a-six-tool-agent-ready-website-with-webmcp-heres-what-it-actually-changes-b98e6a2ec20a
 * (requires registration)
 * https://scrapfly.io/docs/cloud-browser-api/cdp-reference/WebMCP
 * [how to build and debug WebMCP tools for browser agents](https://www.youtube.com/watch?v=5lJ0a6tdj-4)
 * https://webmcp.dev/
 * https://github.com/cloudflare/agents/tree/main/examples/webmcp
 * https://medium.com/data-science-collective/moving-beyond-screen-scraping-creating-an-agent-native-web-app-with-webmcp-4818552e1e11
 * (recommends registration)
 * https://developer.chrome.com/blog/webmcp-epp#structured_interactions_for_the_agentic_web
 * https://github.com/hugozanini/air-bird-booking-web-mcp
 * https://developer.chrome.com/blog/webmcp-epp#structured_interactions_for_the_agentic_web
 * https://github.com/angiejones/mcp-selenium - An MCP implementation for Selenium WebDriver
 * https://github.com/aaif-goose/goose
 * https://github.com/pshivapr/selenium-mcp - Selenium Tools for MCP - presumably the two are the same
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
public class WebMCPDevToolsTest extends BaseDevToolsTest {

	private static String baseURL = "https://www.wikipedia.org";
	private final String selector = "div.central-featured-lang[lang='ru']";
	@After
	public void afterTest() {
		chromeDevTools.send(WebMCP.disable());
		driver.get("about:blank");
	}

	@Before
	public void beforeTest() {
		driver.get(baseURL);
		// Map<String, Object> params = new LinkedHashMap<>();
		chromeDevTools.send(WebMCP.enable());
	}

	@Test
	public void test1() {
		String toolName = null;
		String description = null;
		Map <String, Object> input = new HashMap<>();
		Optional<Map<String, Object>> inputSchema = Optional.of(new HashMap<String, Object>());
		Optional<Annotation> annotations = Optional.of(
			// @formatter:off
			new Annotation(
				Optional.of(false),  // readOnly
				Optional.of(false), // untrustedContent
				Optional.of(false)  // autosubmit
			)
			// @formatter:on
		);
		// @formatter:off
		// The constructor Tool() is undefined
		Tool tool = new Tool(
				toolName,
			description,
			inputSchema,
			annotations,
			null, // frameId
			Optional.empty(), // backendNodeId
			Optional.empty() // stackTrace
		);
		// @formatter:on

		// @formatter:off
		chromeDevTools.send(WebMCP.invokeTool(
				new FrameId(null), // frameId
				toolName, // toolName
				input // input
		));
		// @formatter:on

		
	}

}
