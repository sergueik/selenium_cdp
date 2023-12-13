package com.github.sergueik.selenium;

/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see:
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-setBypassCSP
 */

public class BypassCSPCdpTest extends BaseCdpTest {
	private WebElement element;
	private static String command = null;
	private static Map<String, Object> params = new HashMap<>();
	private static String page = null;
	private static int delay = 3000;

	@After
	public void afterTest() {
		command = "Page.setBypassCSP";
		params.clear();
		params.put("enabled", false);
		driver.get("about:blank");
	}

	// @Ignore
	@Test
	public void test1() {
		page = "test1.html";
		driver.get(Utils.getPageContent(page));
		element = wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.cssSelector("img")));
		assertThat(element, notNullValue());
		assertThat(element.isDisplayed(), is(true));
		// to take screen shot uncommend pause
		// Utils.sleep(delay);
		assertThat(element.getRect().getWidth(), is(16));
		// broken image size is different
	}

	// @Ignore
	@Test
	public void test2() {
		page = "test2.html";
		driver.get(Utils.getPageContent(page));
		element = wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.cssSelector("img")));
		assertThat(element, notNullValue());
		assertThat(element.isDisplayed(), is(true));
		assertThat(element.getRect().getWidth(), is(100));
	}

	@Test
	public void test3() {
		command = "Page.setBypassCSP";
		params.clear();
		params.put("enabled", true);
		driver.executeCdpCommand(command, params);
		page = "test1.html";
		driver.get(Utils.getPageContent(page));
		element = wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.cssSelector("img")));
		assertThat(element, notNullValue());
		assertThat(element.isDisplayed(), is(true));
		// to take screen shot uncommend pause
		// Utils.sleep(delay);
		assertThat(element.getRect().getWidth(), is(100));

	}

}
