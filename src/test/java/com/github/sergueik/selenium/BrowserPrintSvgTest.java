package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import org.openqa.selenium.By;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Selected test scenarios for Selenium WebDriver
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo
 */
public class BrowserPrintSvgTest extends BaseCdpTest {

	private final static String cssSelector = "svg#graph1";
	private final static String filename = "diagram.png";

	private static WebElement element;

	@Before
	public void before() {
		// Arrange
		String page = "mermaid_test.html";
		driver.get(Utils.getPageContent(page));
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(cssSelector)));
		assertThat(element, notNullValue());
		assertThat(element.isDisplayed(), is(true));
		// TODO: computed size
	}

	@After
	public void afterMethod() {
		driver.get("about:blank");
	}

	@Test
	public void test1() {

		// Act

		// filename argument is ignored
		Utils.executeAsyncScript(Utils.getScriptContent("svg_to_png.js"), Utils.cssSelectorOfElement(element),
				filename);
		// TODO: org.openqa.selenium.ScriptTimeoutException: script timeout
		// Assert
		// verify downloaded PNG
	}

}
