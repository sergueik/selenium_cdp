package com.github.sergueik.selenium;
/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringContains.containsString;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.locators.RelativeLocator;

// see also: https://github.com/fugazi/carbonfour-selenium-4/blob/main/src/test/java/Selenium_4_Tests/TestLoginRelativeLocators.java
public class RelativeLocatorTest extends BaseCdpTest {
	private static String baseURL = "https://demoqa.com/automation-practice-form";
	// https://opensource-demo.orangehrmlive.com/web/index.php/auth/login
	private final String header = "Student Registration Form";
	private final String xpath = String
			.format("//*[@id='app']//h5[contains(text(),'%s')]", header);
	private static Base64 base64 = new Base64();
	private static byte[] image;
	private WebElement element;

	@Before
	public void before() {
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		Utils.setDriver(driver);
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
		// Arrange
		driver.get(baseURL);

	}

	@After
	public void after() {
		// Arrange
		driver.get("about:blank");
	}

	@Test
	public void test1() {
		// Arrange
		WebElement headerElement = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
		assertThat(headerElement.getText(), containsString(header));
		Utils.highlight(headerElement);
		System.err.println(headerElement.getText());
		WebElement labelElement = driver.findElement(
				RelativeLocator.with(By.className("form-label")).below(headerElement));
		assertThat(labelElement.getText(), containsString("Name"));
		Utils.highlight(labelElement);
		System.err.println(labelElement.getText());
		// NOTE: search by tagName "input" to the right of "Name" will
		// surpsingly find the "gender-radio-1" and not the "firstName"
		// the "placeholder" attrbute assertion will reveal "placeholder" attrbute
		// to be blank
		WebElement firstNameElement = driver.findElement(RelativeLocator
				.with(By.cssSelector("input[type='text']")).toRightOf(labelElement));
		assertThat(firstNameElement.getAttribute("placeholder"), is("First Name"));
		assertThat(firstNameElement.getAttribute("id"), is("firstName"));
		System.err.println("id: " + firstNameElement.getAttribute("id"));
		System.err.println(
				"placeholder: " + firstNameElement.getAttribute("placeholder"));
		Utils.highlight(firstNameElement);
		WebElement lastNameElement = driver
				.findElement(RelativeLocator.with(By.cssSelector("input[type='text']"))
						.toRightOf(firstNameElement));
		assertThat(lastNameElement.getAttribute("placeholder"), is("Last Name"));
		assertThat(lastNameElement.getAttribute("id"), is("lastName"));
		System.err.println("id: " + lastNameElement.getAttribute("id"));
		System.err
				.println("placeholder: " + lastNameElement.getAttribute("placeholder"));

		element = driver.findElement(RelativeLocator
				.with(By.cssSelector("input[type='text']")).below(firstNameElement));
		assertThat(element.getAttribute("placeholder"), is("name@example.com"));
		assertThat(element.getAttribute("id"), is("userEmail"));
		System.err.println("id: " + element.getAttribute("id"));
		System.err.println("placeholder: " + element.getAttribute("placeholder"));

		// placeholder="name@example.com" type="text" id="userEmail"
	}
}
