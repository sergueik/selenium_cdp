package com.github.sergueik.selenium;

/**
 * Copyright 2022-2024 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.util.List;

import org.junit.Test;
import org.openqa.selenium.devtools.v134.browser.Browser;

/**
 * 
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-getBrowserCommandLine
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class BrowserCommandLineDevToolsTest extends BaseDevToolsTest {

	private static List<String> results;

	@Test
	public void test() {
		// Act
		results = chromeDevTools.send(Browser.getBrowserCommandLine());
		// Assert
		assertThat(results, notNullValue());
		assertThat(results.size(), greaterThan(0));
		results.stream().forEach(o -> System.err.println(o));

	}

}
