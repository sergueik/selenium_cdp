package com.github.sergueik.selenium;

/**
 * Copyright 2024 Serguei Kouzmine
 */

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v121.console.Console;
import org.openqa.selenium.devtools.v121.console.model.ConsoleMessage;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * 
 * https://chromedevtools.github.io/devtools-protocol/tot/Console/#method-disable
 * https://chromedevtools.github.io/devtools-protocol/tot/Console/#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/Console/#event-messageAdded
 * https://chromedevtools.github.io/devtools-protocol/tot/Console/#type-ConsoleMessage
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

@SuppressWarnings("deprecation")
public class ConsoleMessagesandDevToolsTest extends BaseDevToolsTest {

	private final static String baseURL = "https://rahulshettyacademy.com/angularAppdemo/";
	private WebElement element;

	@Before
	public void beforeTest() throws Exception {
		chromeDevTools.send(Console.enable());
		chromeDevTools.addListener(Console.messageAdded(),
				(ConsoleMessage message) -> System.err.println(String.format(
						"level: %s" + "\n" + "source: %s" + "\n" + "line number: %s" + "\n" + "column number: %s" + "\n"
								+ "url: \"%s\" text: %s",
						message.getLevel(), message.getSource(),
						(message.getLine().isPresent() ? message.getLine().get() : ""),
						(message.getColumn().isPresent() ? message.getColumn().get() : ""),
						(message.getUrl().isPresent() ? message.getUrl().get() : ""), message.getText())));
	}

	// based on:
	// https://github.com/SaraMohamed2022/Selenium4_CDPPracticing/blob/main/src/test/java/selenium4_SelfPracticing/GetJavaScriptLogs.java
	@Test
	public void test1() {
		driver.get(baseURL);
		element = driver.findElement(By.linkText("Browse Products"));
		element.click();
		element = driver.findElement(By.partialLinkText("Selenium"));
		element.click();
		element = driver.findElement(By.cssSelector(".add-to-cart"));
		element.click();
		element = driver.findElement(By.linkText("Cart"));
		element.click();
		element = driver.findElement(By.id("exampleInputEmail1"));
		element.clear();
		element.sendKeys("2");

	}

	@After
	public void afterTest() throws Exception {
		chromeDevTools.clearListeners();
		chromeDevTools.send(Console.disable());
	}

}
