package com.github.sergueik.selenium;
/**
 * Copyright 2024 Serguei Kouzmine
 */

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

@SuppressWarnings("deprecation")
public class LegacyLoggingDevToolsTest extends BaseDevToolsTest {

	private final static String baseURL = "https://rahulshettyacademy.com/angularAppdemo/";
	private WebElement element;

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

		LogEntries entries = driver.manage().logs().get(LogType.BROWSER);
		for (LogEntry e : entries)
			System.out.println(e.getMessage());
	}

}
