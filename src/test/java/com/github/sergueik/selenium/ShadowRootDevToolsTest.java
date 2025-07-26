package com.github.sergueik.selenium;

/**
 * Copyright 2023,2024 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
// import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.json.JsonException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.openqa.selenium.devtools.v134.runtime.Runtime;
import org.openqa.selenium.devtools.v134.runtime.Runtime.EvaluateResponse;
import org.openqa.selenium.devtools.v134.runtime.model.ExecutionContextId;
import org.openqa.selenium.devtools.v134.runtime.model.RemoteObject;
import org.openqa.selenium.devtools.v134.runtime.model.TimeDelta;

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

// Karate UI Api Testing Framework is likely to be calling CDP under the hood

@SuppressWarnings("deprecation")

public class ShadowRootDevToolsTest extends BaseDevToolsTest {

	private static String expression = null;
	private static String baseURL = null;
	private static WebElement element = null;
	private static List<WebElement> elements = new ArrayList<>();

	private static WebDriverWait wait;
	private static int flexibleWait = 60;
	private static int pollingInterval = 500;

	@Before
	public void before() throws Exception {
		chromeDevTools.send(Runtime.enable());
		driver.executeCdpCommand("Runtime.enable", new HashMap<String, Object>());
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
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
	// based on:
	// https://youtu.be/O76h9Hf9-Os?list=PLMd2VtYMV0OSv62KjzJ4TFGLDTVtTtQVr&t=527
	@Test
	public void test1() {
		// evaluate
		baseURL = "chrome://downloads/";
		driver.get(baseURL);

		try {
			// "Downloads" -> inspect -> Copy -> Copy JS Path
			expression = "document.querySelector('body > downloads-manager').shadowRoot.querySelector('#toolbar').shadowRoot.querySelector('#toolbar').shadowRoot.querySelector('#leftSpacer > h1').textContent";
			EvaluateResponse response = chromeDevTools
					.send(Runtime.evaluate(expression, Optional.ofNullable(null), // objectGroup
							Optional.of(false), // includeCommandLineAPI
							Optional.of(false), // silent
							Optional.empty(), // contextId
							Optional.of(true), // returnByValue
							Optional.of(false), // generatePreview
							Optional.of(false), // userGesture
							Optional.of(false), // awaitPromise
							Optional.of(false), // throwOnSideEffect
							Optional.of(new TimeDelta(new Double(2000))), // timeout
							Optional.of(false), // disableBreaks
							Optional.of(false), // replMode
							Optional.of(false), // allowUnsafeEvalBlockedByCSP
							Optional.ofNullable(null), // uniqueContextId
							// NOTE: removed in v119
							// Optional.ofNullable(null), // generateWebDriverValue
							Optional.empty() // serializationOptions
			));
			RemoteObject result = response.getResult();
			assertThat(result, notNullValue());
			System.err.println(String.format("test1 result type: %s Value: %s",
					result.getType(), result.getValue().get()));
			assertThat(result.getValue().get(), is("Downloads"));
		} catch (JsonException e) {
			System.err.println(
					"Exception in test1 reading result (ignored): " + e.toString());
		}

	}

	// based on: https://habr.com/ru/companies/simbirsoft/articles/598407/
	// https://github.com/titusfortner/website-examples/blob/shadow_dom/python/selenium4/tests/test_shadow_dom.py#L95-L98
	@Test
	public void test2() {
		baseURL = "http://watir.com/examples/shadow_dom.html";
		driver.get(baseURL);
		element = wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector("#shadow_host")));
		assertThat(element, notNullValue());

		try {
			expression = "document.querySelector('#shadow_host').shadowRoot.children";
			EvaluateResponse response = chromeDevTools
					.send(Runtime.evaluate(expression, Optional.ofNullable(null), // objectGroup
							Optional.of(false), // includeCommandLineAPI
							Optional.of(false), // silent
							Optional.empty(), // contextId
							Optional.of(true), // returnByValue
							Optional.of(false), // generatePreview
							Optional.of(false), // userGesture
							Optional.of(false), // awaitPromise
							Optional.of(false), // throwOnSideEffect
							Optional.of(new TimeDelta(new Double(2000))), // timeout
							Optional.of(false), // disableBreaks
							Optional.of(false), // replMode
							Optional.of(false), // allowUnsafeEvalBlockedByCSP
							Optional.ofNullable(null), // uniqueContextId							
							// NOTE: removed in v119
							// Optional.ofNullable(null), // generateWebDriverValue
							Optional.empty() // serializationOptions
			));
			RemoteObject result = response.getResult();
			assertThat(result, notNullValue());
			System.err.println(String.format("test2 result type: %s value: %s",
					result.getType(), result.getValue().get()));
		} catch (JsonException e) {
			System.err.println(
					"Exception in test2 reading result (ignored): " + e.toString());
		}

	}

	@Test
	public void test3() {
		baseURL = "http://watir.com/examples/shadow_dom.html";
		driver.get(baseURL);
		element = wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector("#shadow_host")));
		assertThat(element, notNullValue());

		try {
			// https://developer.mozilla.org/en-US/docs/Web/API/Node
			expression = "document.querySelector('#shadow_host').shadowRoot.nodeValue";
			EvaluateResponse response = chromeDevTools
					.send(Runtime.evaluate(expression, Optional.ofNullable(null), // objectGroup
							Optional.of(false), // includeCommandLineAPI
							Optional.of(false), // silent
							Optional.empty(), // contextId
							Optional.of(true), // returnByValue
							Optional.of(false), // generatePreview
							Optional.of(false), // userGesture
							Optional.of(false), // awaitPromise
							Optional.of(false), // throwOnSideEffect
							Optional.of(new TimeDelta(new Double(2000))), // timeout
							Optional.of(false), // disableBreaks
							Optional.of(false), // replMode
							Optional.of(false), // allowUnsafeEvalBlockedByCSP
							Optional.ofNullable(null), // uniqueContextId
							// NOTE: removed in v119
							// Optional.ofNullable(null), // generateWebDriverValue
							Optional.empty() // serializationOptions
			));
			RemoteObject result = response.getResult();
			assertThat(result, notNullValue());
			System.err.println(String.format("test3 result type: %s value: %s",
					result.getType(), result.getValue()));
		} catch (JsonException e) {
			System.err.println(
					"Exception in test3 reading result (ignored): " + e.toString());
		}
	}

	@Test
	public void test4() {
		baseURL = "http://watir.com/examples/shadow_dom.html";
		driver.get(baseURL);
		element = wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector("#shadow_host")));
		assertThat(element, notNullValue());

		try {
			expression = "document.querySelector('#shadow_host').shadowRoot.querySelector('#shadow_content').textContent";

			EvaluateResponse response = chromeDevTools
					.send(Runtime.evaluate(expression, Optional.ofNullable(null), // objectGroup
							Optional.of(false), // includeCommandLineAPI
							Optional.of(false), // silent
							Optional.empty(), // contextId
							Optional.of(true), // returnByValue
							Optional.of(false), // generatePreview
							Optional.of(false), // userGesture
							Optional.of(false), // awaitPromise
							Optional.of(false), // throwOnSideEffect
							Optional.of(new TimeDelta(new Double(2000))), // timeout
							Optional.of(false), // disableBreaks
							Optional.of(false), // replMode
							Optional.of(false), // allowUnsafeEvalBlockedByCSP
							Optional.ofNullable(null), // uniqueContextId
							// NOTE: removed in v119
							// Optional.ofNullable(null), // generateWebDriverValue
							Optional.empty() // serializationOptions
			));
			RemoteObject result = response.getResult();
			assertThat(result, notNullValue());
			System.err.println(String.format("test4 result type: %s value: %s",
					result.getType(), result.getValue().get()));
			assertThat(result.getValue().get(), is("some text"));
		} catch (JsonException e) {
			System.err.println(
					"Exception in test4 reading result (ignored): " + e.toString());
		}
	}

	// based on:
	// https://qna.habr.com/q/1310096?e=14038972#clarification_1798462
	// not working: - no shadow-root document is renderd by the page
	// when the test is run
	@Test
	public void test5() {
		// Arrange
		baseURL = "https://podoq.ru";
		driver.get(baseURL);

		element = wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.cssSelector("#header > a > div.logo__title")));
		assertThat(element, notNullValue());

		// System.err.println(driver.getPageSource());
		for (int cnt = 0; cnt != 3; cnt++) {
			Utils.executeScript("window.scrollBy(0,document.body.scrollHeight)");
			Utils.sleep(100);
		}

		elements = driver.findElements(By.xpath("//div[contains(@id, 'yandex')]"));
		elements.stream().forEach(o -> System.err
				.println(String.format("id:" + "\"%s\"", o.getAttribute("id"))));

		element = driver
				.findElement(By.xpath("//div[@id='yandex_rtb_R-A-2602770-3']"));
		assertThat(element, notNullValue());
		System.err.println("test3 (1): " + element.getAttribute("outerHTML"));

		element = driver.findElement(By.cssSelector("#yandex_rtb_R-A-2602770-3"));
		assertThat(element, notNullValue());
		// NOTE: element is not visible
		// assertThat(element.isDisplayed(), is(true));
		System.err.println("test3 (2): " + element.getAttribute("outerHTML"));

		elements = driver.findElements(By.xpath("//div[contains(@id, 'yandex')]"));
		elements.stream()
				.forEach(o -> System.err.println(o.getAttribute("outerHTML")));

		// This is occasionally failing with org.openqa.selenium.TimeoutException:
		// Expected condition failed: waiting for visibility of element located by
		// By.cssSelector: #yandex_rtb_R-A-2602770-3 (tried for 60 second(s) with
		// 500 milliseconds interval)
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.cssSelector("#yandex_rtb_R-A-2602770-3")));
		assertThat(element, notNullValue());

		try {
			expression = "document.querySelector('#yandex_rtb_R-A-2602770-3').shadowRoot.textContent";

			EvaluateResponse response = chromeDevTools
					.send(Runtime.evaluate(expression, Optional.ofNullable(null), // objectGroup
							Optional.of(false), // includeCommandLineAPI
							Optional.of(false), // silent
							Optional.empty(), // contextId
							Optional.of(true), // returnByValue
							Optional.of(false), // generatePreview
							Optional.of(false), // userGesture
							Optional.of(false), // awaitPromise
							Optional.of(false), // throwOnSideEffect
							Optional.of(new TimeDelta(new Double(2000))), // timeout
							Optional.of(false), // disableBreaks
							Optional.of(false), // replMode
							Optional.of(false), // allowUnsafeEvalBlockedByCSP
							Optional.ofNullable(null), // uniqueContextId
							// NOTE: removed in v119
							// Optional.ofNullable(null), // generateWebDriverValue
							Optional.empty() // serializationOptions
			));
			RemoteObject result = response.getResult();
			assertThat(result, notNullValue());
			System.err.println(String.format("test5 result type: %s value: %s",
					result.getType(), result.getValue()));

			// NOTE: With Java 1.8 will not compile
			// symbol: method isEmpty()
			// location: class java.util.Optional<java.lang.Object>
			// assertThat(result.getValue().isEmpty(), is(true));
			assertThat(result.getValue().isPresent(), is(false));

		} catch (JsonException e) {
			System.err.println(
					"Exception in test5 reading result (ignored): " + e.toString());
		}

	}
}
