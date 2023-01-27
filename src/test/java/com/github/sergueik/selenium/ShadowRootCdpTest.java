package com.github.sergueik.selenium;

/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.v108.runtime.Runtime;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.Gson;
import static org.hamcrest.CoreMatchers.is;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Runtime/#method-evaluate
 * https://chromedevtools.github.io/devtools-protocol/tot/Runtime/#method-disable
 * https://chromedevtools.github.io/devtools-protocol/tot/Runtime/#method-enable
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

// based on:
// https://youtu.be/O76h9Hf9-Os?list=PLMd2VtYMV0OSv62KjzJ4TFGLDTVtTtQVr&t=527
// Karate UI Api Testing Framework is likely to be calling CDP under the hood

public class ShadowRootCdpTest extends BaseCdpTest {

	private static final String command = "Runtime.evaluate";
	private static String baseURL = "chrome://downloads/";
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();
	private static Map<String, Object> data = new HashMap<>();

	private static Gson gson = new Gson();
	// "Downloads" -> inspect -> Copy -> Copy JS Path
	private static final String expression = "document.querySelector('body > downloads-manager').shadowRoot.querySelector('#toolbar').shadowRoot.querySelector('#toolbar').shadowRoot.querySelector('#leftSpacer > h1').textContent";
	private static boolean returnByValue = false;

	@Before
	public void beforeTest() throws Exception {
		driver.get(baseURL);
		driver.executeCdpCommand("Runtime.enable", new HashMap<String, Object>());
	}

	@After
	public void clearPage() {
		driver.executeCdpCommand("Runtime.disable", new HashMap<String, Object>());
		driver.get("about:blank");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test1() {
		try {
			// Act
			params = new HashMap<>();
			returnByValue = false;
			// Whether the result is expected to be a JSON object that should be sent
			// by value
			// argument appears to be ignored
			params.put("expression", expression);
			params.put("returnByValue", returnByValue);
			params.put("timout", new Double(100));
			result = driver.executeCdpCommand(command, params);
			System.err.println(String.format("Command \"%s\" raw response: %s",
					command, result.toString()));
			assertThat(result, notNullValue());
			assertThat(result, hasKey("result"));
			data = (Map<String, Object>) result.get("result");
			for (String field : Arrays.asList(new String[] { "type", "value" })) {
				assertThat(data, hasKey(field));
			}
			assertThat((String) data.get("value"), is("Downloads"));
			System.err.println("Result value: " + (String) data.get("value"));
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + ": " + e.toString());
			throw (new RuntimeException(e));
		}
	}

}
