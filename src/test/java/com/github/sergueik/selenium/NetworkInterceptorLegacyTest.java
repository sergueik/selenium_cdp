package com.github.sergueik.selenium;

/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.NetworkInterceptor;
import org.openqa.selenium.remote.http.HttpHandler;
import org.openqa.selenium.remote.http.HttpMethod;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;
import org.openqa.selenium.remote.http.Route;
import org.openqa.selenium.remote.http.Route.PredicatedConfig;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/devtools/NetworkInterceptor.html
 * https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/remote/http/Route.html
 * https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/remote/http/Route.PreicatedConfig.html
 * https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/remote/http/HttpRequest.html
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

// based on:
// https://github.com/rkeeves/selenium-tricks/blob/main/src/test/java/io/github/rkeeves/network/DoNotLoadEyecandyTest.java

public class NetworkInterceptorLegacyTest extends BaseDevToolsTest {

	private static String url = null;
	private static NetworkInterceptor networkInterceptor;
	private static WebDriverWait wait;
	private static int flexibleWait = 10;
	private static int pollingInterval = 500;
	private static WebElement element = null;

	@Before
	public void before() throws UnsupportedEncodingException {

		// NOTE: use loose extension method-like syntax
		// overly terse LINQ style syntax is causing the IDE challenge in
		// parsing

		/* 
		networkInterceptor = new NetworkInterceptor(driver,
				Route
						.matching(req -> HttpMethod.GET.equals(req.getMethod())
								&& req.getUri().matches(".*\\.(?:png|jpg|jpeg)$"))
						.to(() -> req -> new HttpResponse().setStatus(404)));
						*/
		Predicate<HttpRequest> predicate = (HttpRequest request) -> {
			return HttpMethod.GET.equals(request.getMethod())
					&& request.getUri().matches(".*\\.(?:png|jpg|jpeg)$");
		};
		PredicatedConfig predicatedConfig = Route.matching(predicate);
		Supplier<HttpHandler> handler = () -> (
				HttpRequest request) -> new HttpResponse().setStatus(404);
		Route route = predicatedConfig.to(handler);
		networkInterceptor = new NetworkInterceptor(driver, route);
		// occasional exception in @Before
		// Caused by: org.openqa.selenium.devtools.DevToolsException:
		// {"id":326,"error":{"code":-32000,"message":"Fetch domain is not
		// enabled"},"sessionId":"C5E769148A05264AF964186945F3F943"}

	}

	@Test
	public void test1() {
		url = "https://demoqa.com/books";
		driver.navigate().to(url);
		element = driver.findElement(By.cssSelector("header img"));
		Long naturalWidth = (Long) driver
				.executeScript("return arguments[0].naturalWidth", element);
		Long naturalHeight = (Long) driver
				.executeScript("return arguments[0].naturalHeight", element);
		assertThat(naturalWidth, is(0L));
		assertThat(naturalHeight, is(0L));
	}

	// @Ignore
	@Test
	public void test2() {
		url = "https://www.wikipedia.org";
		driver.navigate().to(url);

		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));

		wait.pollingEvery(Duration.ofMillis(pollingInterval));
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.cssSelector("img.central-featured-logo")));

		Long naturalWidth = (Long) driver
				.executeScript("return arguments[0].naturalWidth", element);
		Long naturalHeight = (Long) driver
				.executeScript("return arguments[0].naturalHeight", element);
		assertThat(naturalWidth, is(0L));
		assertThat(naturalHeight, is(0L));
	}

	@After
	public void after() {
	}

	// NOTE: only available in Junit5
	// import org.junit.jupiter.api.AfterEach
	/*
	@AfterEach
	void afterEach() {
		
	}
	*/
}
