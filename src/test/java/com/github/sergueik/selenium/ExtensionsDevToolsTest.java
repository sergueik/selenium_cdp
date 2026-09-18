package com.github.sergueik.selenium;

import java.util.List;
import java.util.Map;

/**
 * Copyright 2026 Serguei Kouzmine
 */
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.devtools.v153.extensions.Extensions;
import org.openqa.selenium.devtools.v153.extensions.model.ExtensionInfo;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.WebDriverException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see:
 * https://chromedevtools.github.io/devtools-protocol/#/Extensions.getExtensions
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class ExtensionsDevToolsTest extends BaseDevToolsTest {

	@Before
	public void before() throws Exception {
		// Arrange
	}

	private static Gson gson = new Gson();

	// invoking an innocent "getExtensions" method in experimental Extensions domain
	// raises Method Not Available exception
	// with Selenium 4.49, Chrome 153
	@Test(expected = DevToolsException.class)
	public void test1() {
		// Arrange
		try {
			List<ExtensionInfo> extensions = chromeDevTools.send(Extensions.getExtensions());
			System.err.println(String.format("Extensions: %d", extensions.size()));
		} catch (DevToolsException e) {
			String message = e.getCause().getMessage();
			String typeName = e.getCause().getClass().getTypeName();
			WebDriverException webDriverException = (WebDriverException) e.getCause();
			String rawMessage = webDriverException.getRawMessage();
			System.err.println("DevToolsException message: " + message);
			try {
				Map<String, Object> data = gson.fromJson(rawMessage, Map.class);
				System.err.println(String.format("WebDriverException message: %s", data.get("error")));
			} catch (JsonSyntaxException e2) {
				System.err.println(
						String.format("Could not parse WebDriverException raw message json: %s", e2.getMessage()));
			}
			throw e;
		}
	}
}
