package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.is;

/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Runtime/#method-evaluate
 * https://chromedevtools.github.io/devtools-protocol/tot/Runtime/#method-disable
 * https://chromedevtools.github.io/devtools-protocol/tot/Runtime/#method-enable
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class ShadowRoot2CdpTest extends BaseCdpTest {

	private static final String command = "Runtime.evaluate";
	private String baseURL = null;

	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();
	private static Map<String, Object> data = new HashMap<>();

	private static WebElement element = null;
	private static List<WebElement> elements = new ArrayList<>();

	private static WebDriverWait wait;
	private static int flexibleWait = 60;
	private static int pollingInterval = 500;

	private static String expression = null;
	private static boolean returnByValue = false;

	@Before
	public void beforeTest() throws Exception {
		driver.executeCdpCommand("Runtime.enable", new HashMap<String, Object>());
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
		Utils.setDriver(driver);
	}

	@After
	public void clearPage() {
		driver.executeCdpCommand("Runtime.disable", new HashMap<String, Object>());
		driver.get("about:blank");
	}

	// based on: https://habr.com/ru/companies/simbirsoft/articles/598407/
	// https://github.com/titusfortner/website-examples/blob/shadow_dom/python/selenium4/tests/test_shadow_dom.py#L95-L98
	@SuppressWarnings("unchecked")
	@Test
	public void test1() {
		// Arrange
		baseURL = "http://watir.com/examples/shadow_dom.html";
		driver.get(baseURL);

		element = wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector("#shadow_host")));
		assertThat(element, notNullValue());
		// Act
		try {

			params = new HashMap<>();
			returnByValue = false;
			// Whether the result is expected to be a JSON object that should be sent
			// by value
			// argument appears to be ignored
			expression = "document.querySelector('#shadow_host').shadowRoot.children";
			params.put("expression", expression);
			params.put("returnByValue", returnByValue);
			params.put("timout", new Double(100));
			result = driver.executeCdpCommand(command, params);
			System.err.println(String.format("Command \"%s\" raw response: %s",
					command, result.toString()));
			assertThat(result, notNullValue());
			assertThat(result, hasKey("result"));
			data = (Map<String, Object>) result.get("result");
			assertThat(data, hasKey("className"));
			assertThat((String) data.get("className"), is("HTMLCollection"));
			System.err.println("Result value: " + (String) data.get("className"));

			expression = "document.querySelector('#shadow_host').shadowRoot.textContent";
			params.put("expression", expression);
			params.put("returnByValue", returnByValue);
			params.put("timout", new Double(100));
			result = driver.executeCdpCommand(command, params);
			System.err.println(String.format("Command \"%s\" raw response: %s",
					command, result.toString()));
			assertThat(result, notNullValue());
			assertThat(result, hasKey("result"));
			data = (Map<String, Object>) result.get("result");
			System.err.println("Result: " + data);

			expression = "document.querySelector('#shadow_host').shadowRoot.querySelector('#shadow_content').textContent";
			params.put("expression", expression);
			params.put("returnByValue", returnByValue);
			params.put("timout", new Double(100));
			result = driver.executeCdpCommand(command, params);
			System.err.println(String.format("Command \"%s\" raw response: %s",
					command, result.toString()));
			assertThat(result, notNullValue());
			assertThat(result, hasKey("result"));
			data = (Map<String, Object>) result.get("result");
			System.err.println("Result: " + data);
			data = (Map<String, Object>) result.get("result");
			for (String field : Arrays.asList(new String[] { "type", "value" })) {
				assertThat(data, hasKey(field));
			}
			assertThat((String) data.get("value"), is("some text"));
			System.err.println("Result value: " + (String) data.get("value"));

		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + ": " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// based on:
	// https://qna.habr.com/q/1310096?e=14038972#clarification_1798462
	// not working: - no shadow-root hosting elements isrenderd by the page
	// when the test is run
	@SuppressWarnings("unchecked")
	@Ignore
	@Test
	public void test2() {
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
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.xpath("//div[contains(@id, 'yandex')]")));
		assertThat(element, notNullValue());
		assertThat(element.isDisplayed(), is(true));
		System.err.println(element.getAttribute("outerHTML"));
		elements = driver.findElements(By.xpath("//div[contains(@id, 'yandex')]"));
		elements.stream()
				.forEach(o -> System.err.println(o.getAttribute("outerHTML")));
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.cssSelector("#yandex_rtb_R-A-2602770-3")));
		assertThat(element, notNullValue());
		assertThat(element.isDisplayed(), is(true));

		// Act
		try {

			params = new HashMap<>();
			returnByValue = false;
			// Whether the result is expected to be a JSON object that should be sent
			// by value
			// NOTE: argument appears to be ignored
			expression = "document.querySelector('#yandex_rtb_R-A-2602770-3').shadowRoot";
			params.put("expression", expression);
			params.put("returnByValue", returnByValue);
			params.put("timout", new Double(100));
			result = driver.executeCdpCommand(command, params);
			System.err.println(String.format("Command \"%s\" raw response: %s",
					command, result.toString()));
			// expression =
			// "document.querySelector('#yandex_rtb_R-A-2602770-3').shadowRoot.querySelector('div[data-container=\"outer\"]').textContent"

		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + ": " + e.toString());
			throw (new RuntimeException(e));
		}
	}

}

