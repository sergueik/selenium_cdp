package com.github.sergueik.selenium;
/**
 * Copyright 2023,2024 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v141.emulation.Emulation;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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

public class IndirectGeolocationOverrideDevToolsTest extends BaseDevToolsTest {

	private static WebElement element = null;
	private static WebDriverWait wait;
	private static int flexibleWait = 60;
	private static int pollingInterval = 500;

	// private static By locator = By
	// .cssSelector("button[aria-label='Show Your Location']");
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
		chromeDevTools.send(Emulation.clearGeolocationOverride());
	}

	@Test
	public void test1() {
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
		System.err.println(
				String.format("click on \"%s\" button.", element.getAttribute("type")));
		element.click();
		// <div class="BNeawe vvjwJb AP7Wnd">Netflix México: Ve series online, ve
		// películas online</div>
		locator = By.xpath("//*[contains(text(), 'netflix.com')]");
		element = wait
				.until(ExpectedConditions.visibilityOfElementLocated(locator));
		// locator = By.cssSelector(".BNeawe");
		// element = driver.findElement(locator);
		//
		// element = wait
		// .until(ExpectedConditions.visibilityOfElementLocated(locator));
		System.err
				.println(String.format("click on \"%s\" link.", element.getText()));
		element.click();
		locator = By.cssSelector("*[name='selectLocale'] option[selected]");

		// <select data-uia="language-picker-header" class="ui-select medium"
		// id="lang-switcher-header-select" tabindex="0"
		// placeholder="lang-switcher"><option lang="en" value="/"
		// data-language="en" data-country="US">English</option><option selected=""
		// lang="es" value="/us-es/" data-language="es"
		// data-country="US">Español</option></select>
		locator = By.cssSelector("#lang-switcher-header-select  option[selected]");
		element = wait
				.until(ExpectedConditions.visibilityOfElementLocated(locator));
		String text = element.getText();
		assertThat(text, containsString("Español"));
		System.err.println("Locale selector: " + text);
	}

	private void setLocation(Double latitude, Double longitude, long accuracy) {
		long altitudeAccuracy =  0L;
		long altitude = 0;
		long heading = 0L;
		long speed = 0L;
		chromeDevTools.send(Emulation.setGeolocationOverride(
				Optional.of(latitude),
				Optional.of(longitude), 
				Optional.of(accuracy),
				Optional.of(altitude),
				Optional.of(altitudeAccuracy),
				Optional.of(heading),
				Optional.of(speed) ));
	}

	private void setLocation() {
		// Mexico City MDX
		final Double latitude = 19.44;
		final Double longitude = -99.14;
		final long accuracy = 100;
		setLocation(latitude, longitude, accuracy);
	}
}
