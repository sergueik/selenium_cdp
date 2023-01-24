package com.github.sergueik.selenium;

/**
 * Copyright 2023 Serguei Kouzmine
 */
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.JsonSyntaxException;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * based on:
 * https://github.com/Sushmada/Selenium_CDP/blob/master/src/ChromeDevToolsSeleniumIntegration/LocalizationTesting_Geolocation.java
 * see also:
 * https://www.selenium.dev/documentation/webdriver/bidirectional/chrome_devtools/
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setGeolocationOverride
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-clearGeolocationOverride
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

// testing the browser locale guess based on calculated client location
// Mexico City location will make browser switch the Results to Spanish as there
// local language

public class IndirectGeolocationOverrideCdpTest extends BaseCdpTest {

	private static String command = null;
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();

	private static WebElement element = null;
	private static WebDriverWait wait;
	private static int flexibleWait = 60;
	private static int pollingInterval = 500;
	private static By locator = null;

	@Before
	public void beforeTest() {
		// Arrange
		setLocation();
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
	}

	@After
	public void afterTest() {
		driver.executeCdpCommand("Emulation.clearGeolocationOverride",
				new HashMap<>());
	}

	@Test
	public void test1() {
		try {

			// Act
			baseURL = "https://www.google.com/maps";
			driver.get(baseURL);

			locator = By.cssSelector("div[jsaction*='mouseover:mylocation.main']");
			element = wait
					.until(ExpectedConditions.visibilityOfElementLocated(locator));
			element.click();
			System.err.println("click on \"my location\" button.");
			// unclear what event to wait for here
			Utils.sleep(1000);
			baseURL = "https://www.google.com/";
			driver.get(baseURL);
			locator = By.cssSelector("*[name='q']");
			element = wait
					.until(ExpectedConditions.visibilityOfElementLocated(locator));

			element.sendKeys("Netflix", Keys.ENTER);
			// Utils.sleep(10000);
			locator = By.cssSelector("button[type='submit']");
			element = wait
					.until(ExpectedConditions.visibilityOfElementLocated(locator));
			setLocation();
			System.err.println(String.format("click on \"%s\" button.",
					element.getAttribute("type")));
			element.click();
			// <div class="BNeawe vvjwJb AP7Wnd">Netflix México: Ve series online, ve
			// películas online</div>
			locator = By.xpath("//*[contains(text(), 'netflix.com')]");
			element = wait
					.until(ExpectedConditions.visibilityOfElementLocated(locator));
			locator = By.cssSelector(".BNeawe");
			// element = driver.findElement(locator);
			//
			element = wait
					.until(ExpectedConditions.visibilityOfElementLocated(locator));
			System.err
					.println(String.format("click on \"%s\" link.", element.getText()));
			element.click();
			locator = By.cssSelector("*[name='selectLocale'] option[selected]");

			element = wait
					.until(ExpectedConditions.visibilityOfElementLocated(locator));
			String text = element.getText();
			assertThat(text, containsString("Español"));
			System.err.println("Locale selector: " + text);

		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ e.toString() + " " + "message: "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}

	}

	private void setLocation() {
		// Mexico City MDX
		final Double latitude = 19.44;
		final Double longitude = -99.14;
		final long accuracy = 100;
		setLocation(latitude, longitude, accuracy);
	}

	private void setLocation(Double latitude, Double longitude, long accuracy) {
		params.put("latitude", latitude);
		params.put("longitude", longitude);
		params.put("accuracy", accuracy);
		// Arrange
		command = "Emulation.setGeolocationOverride";
		// Act
		try {
			result = driver.executeCdpCommand(command, params);
			assertThat(result, notNullValue());
			System.err.println("Response from " + command + ": " + result);
			// Act
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ "message: " + Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

}
