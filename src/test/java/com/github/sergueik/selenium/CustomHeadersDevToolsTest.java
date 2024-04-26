package com.github.sergueik.selenium;

/**
 * Copyright 2022-2024 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v124.network.Network;
import org.openqa.selenium.devtools.v124.network.model.Headers;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setExtraHTTPHeaders
 * https://chromedevtools.github.io/devtools-protocol/tot/Network#method-enable
 * see also: https://github.com/SeleniumHQ/selenium/issues/12162
 * https://stackoverflow.com/questions/71668952/how-to-set-user-agent-client-hint-sec-ch-ua-in-selenium-python
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class CustomHeadersDevToolsTest extends BaseDevToolsTest {

	private static String baseURL = "https://manytools.org/http-html-text/http-request-headers/";
	private static Map<String, Object> headers = new HashMap<>();
	private static String text = null;
	private static WebDriverWait wait;
	private static int flexibleWait = 60;
	private static int pollingInterval = 500;

	@After
	public void after() {
		chromeDevTools.clearListeners();
		chromeDevTools.send(Network.disable());
	}

	@Before
	public void before() throws UnsupportedEncodingException {
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
		chromeDevTools.send(Network.enable(Optional.of(100000000), Optional.empty(),
				Optional.empty()));

		// add event listener to log custom headers requests are sending with
		chromeDevTools.addListener(Network.requestWillBeSent(), o -> {
			headers.keySet().stream()
					.forEach(h -> System.err.println(
							String.format("request will be sent with extra header %s=%s", h,
									o.getRequest().getHeaders().get(h))));

		});
	}

	@Test
	public void test1() {

		headers = new HashMap<>();
		headers.put("Sec-Ch-Ua",
				"\"Not_A Brand\";v=\"42\", \"Google Chrome\";v=\"109\", \"Chromium\";v=\"109\"");
		headers.put("Sec-Ch-Ua-Arch", "x86");
		headers.put("Sec-Ch-Ua-Platform", "Windows");
		String[] headerNames = new String[headers.keySet().size()];
		headers.keySet().toArray(headerNames);
		String customHeaderName = headerNames[new Random((new Date()).getTime())
				.nextInt(headers.keySet().size())];
		chromeDevTools.send(Network.setExtraHTTPHeaders(new Headers(headers)));

		driver.get(baseURL);
		WebElement element = wait.until(ExpectedConditions.visibilityOf(driver
				.findElement(By.cssSelector("#maincontent > div.middlecol > table"))));
		text = element.getText();
		assertThat(text, containsString(customHeaderName));
		System.err.println("Verified: " + customHeaderName);

	}
}
