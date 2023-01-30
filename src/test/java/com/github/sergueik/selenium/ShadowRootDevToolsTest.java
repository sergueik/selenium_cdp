package com.github.sergueik.selenium;
/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Optional;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
// import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v109.runtime.Runtime;
import org.openqa.selenium.devtools.v109.runtime.Runtime.EvaluateResponse;
import org.openqa.selenium.devtools.v109.runtime.model.ExecutionContextId;
import org.openqa.selenium.devtools.v109.runtime.model.RemoteObject;
import org.openqa.selenium.devtools.v109.runtime.model.TimeDelta;
import org.openqa.selenium.json.JsonException;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Runtime/#method-evaluate
 * https://chromedevtools.github.io/devtools-protocol/tot/Runtime/#method-disable
 * https://chromedevtools.github.io/devtools-protocol/tot/Runtime/#method-enable
 * 
 * https://chromedevtools.github.io/devtools-protocol/tot/Runtime/#method-callFunctionOn
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

// based on:
// https://youtu.be/O76h9Hf9-Os?list=PLMd2VtYMV0OSv62KjzJ4TFGLDTVtTtQVr&t=527
// Karate UI Api Testing Framework is likely to be calling CDP under the hood

public class ShadowRootDevToolsTest extends BaseDevToolsTest {

	// "Downloads" -> inspect -> Copy -> Copy JS Path
	private final static String expression = "document.querySelector('body > downloads-manager').shadowRoot.querySelector('#toolbar').shadowRoot.querySelector('#toolbar').shadowRoot.querySelector('#leftSpacer > h1').textContent";
	private final static String baseURL = "chrome://downloads/";

	@Before
	public void before() throws Exception {
		chromeDevTools.send(Runtime.enable());
		driver.get(baseURL);
	}

	@After
	public void clearPage() {
		try {
			chromeDevTools.send(Runtime.disable());
			driver.get("about:blank");
		} catch (Exception e) {

		}
	}

	// NOTE: some arguments *must* be empty
	@Test
	// @Test(expected = DevToolsException.class)
	public void test1() {
		// evaluate
		chromeDevTools.send(Runtime.enable());
		try {

			EvaluateResponse response = chromeDevTools
					.send(Runtime.evaluate(expression, Optional.ofNullable(null), // objectGroup
							Optional.of(false), // includeCommandLineAPI
							Optional.of(false), // silent
							Optional.of(new ExecutionContextId(1000)), // contextId
							Optional.of(false), // returnByValue
							Optional.of(false), // generatePreview
							Optional.of(false), // userGesture
							Optional.of(false), // awaitPromise
							Optional.of(false), // throwOnSideEffect
							Optional.of(new TimeDelta(new Double(2000))), // timeout
							Optional.of(false), // disableBreaks
							Optional.of(false), // replMode
							Optional.of(false), // allowUnsafeEvalBlockedByCSP
							Optional.ofNullable(null), // uniqueContextId
							// Optional.of("42"),
							Optional.ofNullable(null) // generateWebDriverValue
			));
			RemoteObject result = response.getResult();
			assertThat(result, notNullValue());
			System.err.println(String.format("Result type: %s Value: %s",
					result.getType(), result.getValue()));
		} catch (JsonException e) {
			System.err.println(
					"Exception in test 1 reading result (ignored): " + e.toString());
		}

	}

}
