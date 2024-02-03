package com.github.sergueik.selenium;
/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
// import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v121.fetch.Fetch;
import org.openqa.selenium.devtools.v121.fetch.model.RequestId;
import org.openqa.selenium.devtools.v121.fetch.model.RequestPattern;
import org.openqa.selenium.devtools.v121.fetch.model.RequestPaused;
import org.openqa.selenium.devtools.v121.network.Network;
import org.openqa.selenium.devtools.v121.network.model.ErrorReason;
import org.openqa.selenium.devtools.v121.network.model.Request;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/#method-failRequest
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/#method-fulfillRequest
 * https://chromedevtools.github.io/devtools-protocol/tot/Fetch/#method-continueRequest 
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#type-Request
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
// see also:
// https://github.com/Sushmada/Selenium_CDP/blob/master/src/ChromeDevToolsSeleniumIntegration/FailNetworkRequest.java
public class FetcFailRequestDevToolsTest extends BaseDevToolsTest {
	private static String url = "https://www.wikipedia.org";
	private static WebDriverWait wait;
	private static int flexibleWait = 10;
	private static int pollingInterval = 500;
	private static WebElement element = null;

	@Before
	public void before() throws TimeoutException {
		String urlPattern = "*assets*";

		RequestPattern requestPattern = new RequestPattern(Optional.of(urlPattern),
				Optional.empty(), Optional.empty());
		List<RequestPattern> arg = new ArrayList<>();
		arg.add(requestPattern);
		chromeDevTools.send(Fetch.enable(Optional.of(arg) /* Optional.empty()*/,
				Optional.of(false)));
		chromeDevTools.send(Network.clearBrowserCache());
		chromeDevTools.send(Network.setCacheDisabled(true));

		chromeDevTools.addListener(Fetch.requestPaused(), (RequestPaused event) -> {
			Request request = event.getRequest();
			RequestId requestId = event.getRequestId();
			System.err.println("About to handle the request: " + request.getUrl());
			if (request.getUrl().matches(".*\\.(?:png|jpg|jpeg)$")) {
				System.err.println("About to abort the request to " + request.getUrl());
				ErrorReason errorReason = ErrorReason.FAILED;
				Fetch.failRequest(requestId, errorReason);
			} else {
				// NOTE: setting HTTP response code by hand
				Fetch.continueRequest(requestId, Optional.of(request.getUrl()),
						Optional.of(request.getMethod()), request.getPostData(),
						/* Optional.of(request.getHeaders()) */ Optional.empty(),
						Optional.of(false));
			}
		});

	}

	@Test
	public void test2() {
		try {
			driver.navigate().to(url);
		} catch (TimeoutException e) {
			System.err.println("continue");
		}
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));

		wait.pollingEvery(Duration.ofMillis(pollingInterval));
		// Visibility or presence would time out
		// element = wait.until(ExpectedConditions.visibilityOfElementLocated(
		// By.cssSelector("img.central-featured-logo")));
		// element = wait.until(ExpectedConditions
		element = driver.findElement(By.cssSelector("img.central-featured-logo"));
		// .presenceOfElementLocated(By.cssSelector("img.central-featured-logo")));
		Long naturalWidth = (Long) driver
				.executeScript("return arguments[0].naturalWidth", element);
		Long naturalHeight = (Long) driver
				.executeScript("return arguments[0].naturalHeight", element);
		assertThat(naturalWidth, is(0L));
		assertThat(naturalHeight, is(0L));
	}

	@After
	public void after() {

		chromeDevTools.send(Fetch.disable());
		chromeDevTools.clearListeners();
	}

}

