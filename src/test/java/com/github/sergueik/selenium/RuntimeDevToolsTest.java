package com.github.sergueik.selenium;

/**
 * Copyright 2023,2024 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.Paths;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
// import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v124.runtime.Runtime;
import org.openqa.selenium.devtools.v124.runtime.Runtime.EvaluateResponse;
import org.openqa.selenium.devtools.v124.runtime.model.ExecutionContextId;
import org.openqa.selenium.devtools.v124.runtime.model.RemoteObject;
import org.openqa.selenium.devtools.v124.runtime.model.TimeDelta;
import org.openqa.selenium.json.JsonException;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/1-3/Runtime/#method-evaluate
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class RuntimeDevToolsTest {

	private static boolean runHeadless = false;
	private static String osName = Utils.getOSName();
	private static ChromiumDriver driver;
	private static DevTools chromeDevTools;
	private static String expression = null;

	private final static String baseURL = "https://www.google.com";

	@BeforeClass
	public static void setUp() throws Exception {

		if (System.getenv().containsKey("HEADLESS")
				&& System.getenv("HEADLESS").matches("(?:true|yes|1)")) {
			runHeadless = true;
		}
		// force the headless flag to be true to support Unix console execution
		if (!(Utils.getOSName().equals("windows"))
				&& !(System.getenv().containsKey("DISPLAY"))) {
			runHeadless = true;
		}
		System
				.setProperty("webdriver.chrome.driver",
						Paths.get(System.getProperty("user.home"))
								.resolve("Downloads").resolve(osName.equals("windows")
										? "chromedriver.exe" : "chromedriver")
								.toAbsolutePath().toString());

		if (runHeadless) {
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--headless", "--disable-gpu");
			driver = new ChromeDriver(options);
		} else {
			driver = new ChromeDriver();
		}
		Utils.setDriver(driver);
		chromeDevTools = ((HasDevTools) driver).getDevTools();
		// compiles but fails in runtime
		chromeDevTools.createSession();
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		driver.get(baseURL);
	}

	@AfterClass
	public static void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	// NOTE: some arguments *must* be empty
	@Test(expected = DevToolsException.class)
	public void test1() {
		// evaluate
		chromeDevTools.send(Runtime.enable());
		try {
			expression = "var y = 123; y;";

			EvaluateResponse response = chromeDevTools
					.send(Runtime.evaluate(expression, Optional.ofNullable(null), // objectGroup
							Optional.of(false), // includeCommandLineAPI
							Optional.of(false), // silent
							Optional.of(new ExecutionContextId(100)), // contextId
							Optional.of(false), // returnByValue
							Optional.of(false), // generatePreview
							Optional.of(false), // userGesture
							Optional.of(false), // awaitPromise
							Optional.of(false), // throwOnSideEffect
							Optional.of(new TimeDelta(new Double(1000))), // timeout
							Optional.of(false), // disableBreaks
							Optional.of(false), // replMode
							Optional.of(false), // allowUnsafeEvalBlockedByCSP
							Optional.ofNullable(null), // uniqueContextId
							// NOTE: removed in v119
							// Optional.ofNullable(null), // generateWebDriverValue
							Optional.empty() // serializationOptions
			));
			// org.openqa.selenium.devtools.DevToolsException:
			// {"id":6,"error":{"code":-32602,"message":"Invalid
			// parameters","data":"Failed to deserialize params.ti
			RemoteObject result = response.getResult();
			assertThat(result, notNullValue());
			System.err.println(String.format("test 1 Result type: %s Value: %s",
					result.getType(), result.getValue()));
		} catch (JsonException e) {
			System.err.println(
					"Exception in test 1 reading result (ignored): " + e.toString());
		}

	}

	@Test
	public void test2() {
		chromeDevTools.send(Runtime.enable());
		try {
			expression = "var y = 456; y;";

			EvaluateResponse response = chromeDevTools.send(Runtime.evaluate(
					expression, Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty()));

			Object result = response.getResult();
			System.err
					.println(String.format("test 2 Result raw %s:", result.toString()));
		} catch (JsonException e) {
			System.err.println(
					"Exception in test 2 reading result (ignored): " + e.toString());
		}
	}

	@Test
	public void test3() {
		// evaluate
		chromeDevTools.send(Runtime.enable());
		try {
			expression = "var y = 456; y;";

			Object response = chromeDevTools.send(Runtime.evaluate(expression,
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty()));
			assertThat(response, notNullValue());
			System.err.println(
					String.format("test 3 Response type is %s", response.getClass()));
		} catch (JsonException e) {
			System.err.println(
					"Exception in test 3 reading result (ignored): " + e.toString());
		} catch (DevToolsException e) {
			// Caused by: org.openqa.selenium.json.JsonException: Unable to create
			// instance of class
			// org.openqa.selenium.devtools.runtime.model.RemoteObject
			System.err.println(
					"Exception in test 3 generating result (ignored): " + e.toString());
			throw e;
		}

	}

	@Test(expected = java.lang.NullPointerException.class)
	public void test4() {
		// evaluate
		chromeDevTools.send(Runtime.enable());
		expression = "var y = 42; y;";
		chromeDevTools.send(Runtime.evaluate(expression, null, null, null, null,
				null, null, null, null, null, null, null, null, null, null, null));
	}

	@Test(expected = org.openqa.selenium.devtools.DevToolsException.class)
	public void test5() {
		// evaluate
		chromeDevTools.send(Runtime.enable());
		try {
			expression = "var y = 123; y;";

			EvaluateResponse response = chromeDevTools
					.send(Runtime.evaluate(expression, Optional.ofNullable(null), // objectGroup
							Optional.of(false), // includeCommandLineAPI
							Optional.of(false), // silent
							Optional.of(new ExecutionContextId(0)), // contextId
							Optional.of(false), // returnByValue
							Optional.of(false), // generatePreview
							Optional.of(false), // userGesture
							Optional.of(false), // awaitPromise
							Optional.of(false), // throwOnSideEffect
							Optional.of(new TimeDelta(new Double(0))), // timeout
							Optional.of(false), // disableBreaks
							Optional.of(false), // replMode
							Optional.of(false), // allowUnsafeEvalBlockedByCSP
							Optional.empty(), // uniqueContextId
							// NOTE: removed in v119
							// Optional.ofNullable(null), // generateWebDriverValue
							Optional.empty() // serializationOptions
			));

			response.getResult();
		} catch (JsonException e) {
			System.err.println("Exception reading result (ignored): " + e.toString());
		}
	}

	@Test(expected = org.openqa.selenium.devtools.DevToolsException.class)
	public void test6() {
		// evaluate
		chromeDevTools.send(Runtime.enable());
		try {
			expression = "var y = 123; y;";

			EvaluateResponse response = chromeDevTools
					.send(Runtime.evaluate(expression, Optional.ofNullable(null), // objectGroup
							Optional.of(false), // includeCommandLineAPI
							Optional.of(false), // silent
							Optional.of(new ExecutionContextId(0)), // contextId,
							// can also pass Optional.empty() here
							Optional.of(false), // returnByValue
							Optional.of(false), // generatePreview
							Optional.of(false), // userGesture
							Optional.of(false), // awaitPromise
							Optional.of(false), // throwOnSideEffect
							Optional.of(new TimeDelta(new Double(100))), // timeout
							Optional.of(false), // disableBreaks
							Optional.of(false), // replMode
							Optional.of(false), // allowUnsafeEvalBlockedByCSP
							Optional.empty(), // uniqueContextId
							// NOTE: removed in v119
							// Optional.ofNullable(null), // generateWebDriverValue
							Optional.empty() // serializationOptions
			));

			response.getResult();
		} catch (JsonException e) {
			System.err.println("Exception reading result (ignored): " + e.toString());
		}
		// {"code":-32602,"message":"Invalid parameters","data":"Failed to
		// deserialize params.timeout - BINDINGS: double value expected at position
		// 175"},"sessionId":"91EA16790E5732D1D741B5996757CE2"}(..)

	}

	// NOTE: replacing Optiona.empty() with nulls would lead to NPE
	@Test
	public void test7() {
		chromeDevTools.send(Runtime.enable());
		try {
			expression = "const letters = ['a', 'b', 'c']; letters.push('d'); letters";

			EvaluateResponse response = chromeDevTools.send(Runtime.evaluate(
					expression, Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(), Optional.empty()));

			Object rawResult = response.getResult();
			System.err.println(String.format("Result raw %s:", rawResult.toString()));
			RemoteObject result = response.getResult();
			assertThat(result.getType().toString(), is("object"));
			assertThat(result.getSubtype().toString(), is("Optional[array]"));
			assertThat(result.getDescription().toString(), is("Optional[Array(4)]"));
		} catch (JsonException e) {
			System.err.println("Exception reading result (ignored): " + e.toString());
		} catch (DevToolsException e) {
			// Caused by: org.openqa.selenium.json.JsonException: Unable to create
			// instance of class
			// org.openqa.selenium.devtools.runtime.model.RemoteObject
			System.err
					.println("Exception generating result (ignored): " + e.toString());
			throw (e);
		}

	}

}
