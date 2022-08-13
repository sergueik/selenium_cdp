package com.github.sergueik.selenium;

/**
 * Copyright 2021,2022 Serguei Kouzmine
 */
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v103.emulation.Emulation;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.JsonSyntaxException;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * based on:
 * https://github.com/sahajamit/chrome-devtools-webdriver-integration/blob/master/src/test/java/com/sahajamit/DemoTests.java
 * https://github.com/sahajamit/chrome-devtools-webdriver-integration/blob/master/src/main/java/com/sahajamit/messaging/MessageBuilder.java
 * see also:
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setGeolocationOverride
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-clearGeolocationOverride
 * https://chromedevtools.github.io/devtools-protocol/tot/Page#method-captureScreenshot
 * https://chromedevtools.github.io/devtools-protocol/tot/Runtime/#method-getProperties
 * https://chromedevtools.github.io/devtools-protocol/tot/Runtime/#method-evaluate 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class GeolocationOverrideCdpTest extends BaseCdpTest {

	private static String command = null;
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> result2 = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();

	private static WebElement element = null;
	private static WebDriverWait wait;
	private static int flexibleWait = 60;
	private static int pollingInterval = 500;
	// private static By locator = By
	// .cssSelector("button[aria-label='Show Your Location']");
	private static By locator = By
			.cssSelector("div[jsaction*='mouseover:mylocation.main']");
	private static String data = null;

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

			// click "my location" button when drawn
			element = wait
					.until(ExpectedConditions.visibilityOfElementLocated(locator));
			element.click();
			// unclear what event to wait for here
			Utils.sleep(5000);
			result = null;
			data = null;
			command = "Page.captureScreenshot";
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			// Assert
			assertThat(result, notNullValue());
			assertThat(result, hasKey("data"));
			assertThat(result.get("data"), notNullValue());
			data = (String) result.get("data");

			Base64 base64 = new Base64();
			byte[] image = base64.decode(data);
			BufferedImage o = ImageIO.read(new ByteArrayInputStream(image));
			assertThat(o.getWidth(), greaterThan(0));
			assertThat(o.getHeight(), greaterThan(0));
			String screenshotFileName = "map.png";
			FileOutputStream fileOutputStream = new FileOutputStream(
					screenshotFileName);
			fileOutputStream.write(image);
			fileOutputStream.close();
		} catch (IOException e) {
			System.err
					.println("Exception saving image file (ignored): " + e.toString());
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
		// Assert
	}

	@Test
	public void test3() {
		baseURL = "https://mycurrentlocation.net";
		driver.get(baseURL);
		locator = By.cssSelector(".location-intro");
		element = wait
				.until(ExpectedConditions.visibilityOfElementLocated(locator));
		assertThat(element.getText(), containsString("Mountain View"));
		System.err.println("Location explained: " + element.getText());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test4() {
		// Arrange
		driver.executeCdpCommand("Runtime.enable", new HashMap<>());

		driver.get(baseURL);
		command = "Runtime.evaluate";
		params = new HashMap<>();
		params.put("expression",
				"function example(){ return navigator.geolocation;} example();");
		result = driver.executeCdpCommand(command, params);
		System.err.println("Response to " + command + ": " + result);
		command = "Runtime.getProperties";
		params = new HashMap<>();
		result2 = (Map<String, Object>) result.get("result");
		params.put("objectId", result2.get("objectId").toString());
		result = driver.executeCdpCommand(command, params);
		System.err.println("Response to " + command + ": " + result);

		// https://developer.mozilla.org/en-US/docs/Web/API/Geolocation/getCurrentPosition
		// https://w3c.github.io/geolocation-api/#getcurrentposition-method
		command = "Runtime.evaluate";
		params = new HashMap<>();
		// @formatter:off
		params.put("expression",
				"const options = {" 
						+ " enableHighAccuracy: true," 
						+ " timeout: 5000,"
						+ " maximumAge: 0" 
						+ "};" 
						+ " function success(pos) {"
						+ " const crd = pos.coords;"
						+ "	console.log('Your current position is:');"
						+ " console.log(`Latitude : ${crd.latitude}`);"
						+ " console.log(`Longitude: ${crd.longitude}`);"
						+ " console.log(`More or less ${crd.accuracy} meters.`);" + " }"
						+ " function error(err) {"
						+ " console.warn(`ERROR(${err.code}): ${err.message}`);" + " }"
						+ "function example(){"
						+ " return navigator.geolocation.getCurrentPosition(success, error, options);} "
						+ " example();"
						);
		// @formatter:on
		// NOTE: a shorter call, ( still async ) is
		// as shown in https://www.w3.org/TR/geolocation/
		/*
		navigator.geolocation.getCurrentPosition(position => {
		  const { latitude, longitude } = position.coords;
		 // provide feedback
		})
		*/
		result = driver.executeCdpCommand(command, params);
		System.err.println("Response to " + command + ": " + result);
		Utils.sleep(1000);
	}

	private void setLocation() {
		final Double latitude = 37.422290;
		final Double longitude = -122.084057;
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
