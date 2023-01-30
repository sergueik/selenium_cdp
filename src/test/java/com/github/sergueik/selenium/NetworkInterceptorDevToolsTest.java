package com.github.sergueik.selenium;

/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.NetworkInterceptor;
import org.openqa.selenium.devtools.v109.network.Network;
import org.openqa.selenium.remote.http.HttpHandler;
import org.openqa.selenium.remote.http.HttpMethod;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;
import org.openqa.selenium.remote.http.Route;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/devtools/NetworkInterceptor.html
 * https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/remote/http/Route.html
 * https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/remote/http/HttpRequest.html
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

// based on:
// https://github.com/rkeeves/selenium-tricks/blob/main/src/test/java/io/github/rkeeves/network/DoNotLoadEyecandyTest.java

public class NetworkInterceptorDevToolsTest extends BaseDevToolsTest {

	private static String url = null;
	private static NetworkInterceptor networkInterceptor;
	private static WebDriverWait wait;
	private static int flexibleWait = 10;
	private static int pollingInterval = 500;

	private static WebElement element = null;

	@After
	public void after() {
		chromeDevTools.clearListeners();
		chromeDevTools.send(Network.disable());
	}

	// NOTE: only available in Junit5
	// import org.junit.jupiter.api.AfterEach
	/*
	@AfterEach
	void afterEach() {
		
	}
	*/

	@Before
	public void before() throws UnsupportedEncodingException {

		chromeDevTools.send(Network.enable(Optional.of(100000000), Optional.empty(),
				Optional.empty()));

		// NOTE: use looe extension method-like syntax
		// the compact LINQ style syntax is causing the IDE challenge
		// and is too terse
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
		Supplier<HttpHandler> handler = () -> (
				HttpRequest request) -> new HttpResponse().setStatus(404);
		Route route = Route.matching(predicate).to(handler);
		networkInterceptor = new NetworkInterceptor(driver, route);
	}

	@Ignore("this site is occasionally hanging the browser, target directory remains locked after test is aborted")
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

	@Test
	public void test2() {
		driver.navigate().to("https://www.wikipedia.org");

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

}
