package com.github.sergueik.selenium;
/**
 * Copyright 2023 Serguei Kouzmine
 */

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.JavascriptException;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

// based on:
// https://github.com/Shivshive/Selenium_CDP/blob/master/src/test/java/example/selenium_cdp/MainPageTest.java
// Domains
// see also:
// https://github.com/SeleniumHQ/selenium/issues/11573
// https://www.lambdatest.com/automation-testing-advisor/selenium/methods/org.openqa.selenium.devtools.idealized.Javascript.addJsBinding
public class DomainJavascriptDevToolsTest extends BaseDevToolsTest {
	private static String url = "https://www.wikipedia.org";

	@Before
	public void before() {
		driver.get(url);
		String script = "function showAlert(msg){ alert(msg); }";
		String alias = "example";
		chromeDevTools.getDomains().javascript().pin(alias, script);
	}

	@Test(expected = JavascriptException.class)
	public void test() {
		String msg = "test";
		Utils.executeScript("showAlert(arguments[0])", msg);
	}
}
