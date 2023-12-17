package com.github.sergueik.selenium;

/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.devtools.v120.page.Page;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see:
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-setBypassCSP
 */

public class BypassCSPDevToolsTest extends BaseDevToolsTest {
	private WebElement element;
	private static String page = null;
	private static int delay = 3000;
	protected static WebDriverWait wait;
	public final static int flexibleWait = 10; // NOTE: 60 is quite long
	public final static Duration duration = Duration.ofSeconds(flexibleWait);
	public final static int implicitWait = 1;
	public final static int pollingInterval = 500;

	@Before
	public void before() throws Exception {
		chromeDevTools.send(Page.enable());
		wait = new WebDriverWait(driver, duration);
		wait.pollingEvery(Duration.ofMillis(pollingInterval));

	}

	@After
	public void afterTest() {
		chromeDevTools.send(Page.setBypassCSP(false));
		chromeDevTools.clearListeners();
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
		chromeDevTools.send(Page.setBypassCSP(true));
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
