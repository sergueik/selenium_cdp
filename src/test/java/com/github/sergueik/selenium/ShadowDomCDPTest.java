package com.github.sergueik.selenium;

/**
 * Copyright 2023,2024 Serguei Kouzmine
 */


import static java.lang.System.err;
import static org.hamcrest.CoreMatchers.is;

/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

// based on:
// https://github.com/diemol/selenium-4-demo/blob/master/src/test/java/com/saucelabs/demo/ShadowDomTest.java#L3

public class ShadowDomCDPTest extends BaseCdpTest {

	private WebElement element1;
	private WebElement element2;
	private WebElement element3;
	private SearchContext shadowRoot;
	private static String page = "inner_html_example.html";

	@Before
	public void beforeTest() throws Exception {
		driver.get(Utils.getPageContent(page));
		driver.executeCdpCommand("Runtime.enable", new HashMap<String, Object>());
	}

	@After
	public void clearPage() {
		driver.executeCdpCommand("Runtime.disable", new HashMap<String, Object>());
		driver.get("about:blank");
	}

	@Test
	public void test1() {
		// Act
		element1 = driver.findElement(By.cssSelector("body > div"));
		element2 = element1.findElement(By.tagName("h3"));
		assertThat(element2, notNullValue());
		err.println("Page outerHTML: " + element2.getAttribute("outerHTML"));
		// Assert
		assertThat(element2.getText(), is(""));
		err.println(String.format("Page text: \"%s\"", element2.getText()));
		// Act
		shadowRoot = element1.getShadowRoot();

		element3 = shadowRoot.findElement(By.cssSelector("h3"));

		assertThat(element3, notNullValue());
		err.println("Shadow DOM outerHTML: " + element3.getAttribute("outerHTML"));
		// Assert
		assertThat(element3.getText(), is("Shadow DOM"));
		err.println(String.format("Shadow DOM text: \"%s\"", element3.getText()));

	}

}
