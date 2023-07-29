package com.github.sergueik.selenium;

/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setExtraHTTPHeaders
 * https://chromedevtools.github.io/devtools-protocol/tot/Network#method-enable
 * see also: https://github.com/SeleniumHQ/selenium/issues/12162
 * https://stackoverflow.com/questions/71668952/how-to-set-user-agent-client-hint-sec-ch-ua-in-selenium-python
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class CustomHeadersCdpTest extends BaseCdpTest {

	private static String baseURL = "https://manytools.org/http-html-text/http-request-headers/";
	private static String text = null;
	private static Map<String, Object> headers = new HashMap<>();
	private static String command = null;
	private static Map<String, Object> params = new HashMap<>();

	@After
	public void afterTest() {
		command = "Network.disable";
		driver.executeCdpCommand(command, new HashMap<>());
		driver.get("about:blank");
	}

	@Before
	public void beforeTest() throws UnsupportedEncodingException {
		command = "Network.enable";
		driver.executeCdpCommand(command, new HashMap<>());
	}

	@Test
	public void test1() {
		headers = new HashMap<>();
		params = new HashMap<>();

		headers.put("Sec-Ch-Ua",
				"\"Not_A Brand\";v=\"42\", \"Google Chrome\";v=\"109\", \"Chromium\";v=\"109\"");
		headers.put("Sec-Ch-Ua-Arch", "x86");
		headers.put("Sec-Ch-Ua-Platform", "Windows");
		String[] headerNames = new String[headers.keySet().size()];
		headers.keySet().toArray(headerNames);
		String customHeaderName = headerNames[new Random((new Date()).getTime())
				.nextInt(headers.keySet().size())];

		params.put("headers", headers);

		command = "Network.setExtraHTTPHeaders";
		driver.executeCdpCommand(command, params);

		driver.get(baseURL);
		WebElement element = wait.until(ExpectedConditions.visibilityOf(driver
				.findElement(By.cssSelector("#maincontent > div.middlecol > table"))));
		text = element.getText();
		assertThat(text, containsString(customHeaderName));
		// System.err.println(text);
		System.err.println("Verified: " + customHeaderName);

	}

}
