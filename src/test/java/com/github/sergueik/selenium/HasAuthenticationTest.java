package com.github.sergueik.selenium;
/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.HasAuthentication;
import org.openqa.selenium.UsernameAndPassword;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Selected test scenarios for Selenium WebDriver
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 * based on discussion https://automated-testing.info/t/pri-ozhidanii-alerta-poyavlyaetsya-oshibka-o-ego-otsutstvii/29094/3
 * https://www.selenium.dev/selenium/docs/api/java/index.html?org/openqa/selenium/HasAuthentication.html
 * see also https://stackoverflow.com/questions/42114940/how-to-handle-authentication-popup-in-chrome-with-selenium-webdriver-using-java
 * see also https://stackoverflow.com/questions/50834002/chrome-headless-browser-with-corporate-proxy-authetication-not-working/67321556#67321556

 */

public class HasAuthenticationTest extends BaseCdpTest {

	private static String command = null;
	private static String baseURL = "https://jigsaw.w3.org/HTTP/";
	private String username = null;
	private String password = null;
	private static WebElement element = null;
	private static List<WebElement> elements = new ArrayList<>();

	private static Map<String, Object> params = new HashMap<>();
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> result2 = new HashMap<>();
	private HasAuthentication authentication;

	@Before
	public void before() throws IOException {
		authentication = (HasAuthentication) driver;
	}

	@After
	public void afterMethod() {
		driver.get("about:blank");
	}

	// NOTE: following two tests
	// log numerous exceptions to console, both in success and failure case
	// at org.openqa.selenium.devtools.Connection.handle(Connection.java:260)
	// ...5 more
	// Exception in thread "CDP Connection"
	// org.openqa.selenium.devtools.DevToolsException:
	// {"id":69,"error":{"code":-32602,"message":"Invalid
	// InterceptionId."},"sessionId":"5370606E4AC1FB56C1199764C8CD5C34"}
	// but should not mark the test with expected = DevToolsException.class
	// or the test would fail
	@Test
	public void test1() {
		// Arrange
		username = "user";
		password = "unauthorized";
		authentication.register(() -> new UsernameAndPassword(username, password));
		// Act
		driver.get("https://the-internet.herokuapp.com/basic_auth");
		// Assert
		System.err.println("test2: " + driver.getCurrentUrl() + "\n");
		element = driver.findElement(By.tagName("body"));
		System.err.println(String.format("test2 %s %s %s\n", username, password,
				element.getText()));
		assertThat(element.getText(), is(""));

		Utils.sleep(1000);
	}

	// NOTE: if the test order is swapped the test 2 will be failing becuse the
	// username and password from test1 will apparently be used
	@Test
	public void test2() {
		// Arrange
		username = "admin";
		password = "admin";
		authentication.register(() -> new UsernameAndPassword(username, password));
		// Act
		driver.get("https://the-internet.herokuapp.com/basic_auth");
		// Assert
		System.err.println(driver.getCurrentUrl());
		element = driver.findElement(By.tagName("body"));
		assertThat(element.getText(),
				containsString("You must have the proper credentials."));
		System.err.println(
				String.format("test1 %s %s %s", username, password, element.getText()));

		Utils.sleep(1000);
	}

	@Test
	public void test3() {
		// Arrange

		driver.get("https://jigsaw.w3.org/HTTP/");

		element = wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.linkText("Basic Authentication test")));
		username = "guest";
		password = "guest";
		authentication.register(() -> new UsernameAndPassword(username, password));
		Utils.highlight(element);
		// Act
		element.click();
		// Assert
		element = driver.findElement(By.tagName("body"));
		assertThat(element.getText(), containsString("Your browser made it!"));
		Utils.highlight(element);
		element.click();
		Utils.sleep(1000);
	}
}
