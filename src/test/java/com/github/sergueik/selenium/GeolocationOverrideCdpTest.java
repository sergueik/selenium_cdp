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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;

import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v101.emulation.Emulation;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.google.gson.JsonSyntaxException;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class GeolocationOverrideCdpTest extends BaseCdpTest {

	private static String command = null;
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();

	private static WebElement element = null;
	private static By locator = By
			.cssSelector("div[class *='widget-mylocation-button-icon-common']");
	private static String data = null;
	private static Double latitude = 37.422290;
	private static Double longitude = -122.084057;
	private static long accuracy = 100;

	@Ignore
	@Test
	// based on:
	// https://chromedevtools.github.io/devtools-protocol/tot/Emulation#method-setGeolocationOverride
	// see also:
	// https://github.com/sahajamit/chrome-devtools-webdriver-integration/blob/master/src/test/java/com/sahajamit/DemoTests.java
	// https://github.com/sahajamit/chrome-devtools-webdriver-integration/blob/master/src/main/java/com/sahajamit/messaging/MessageBuilder.java
	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-getLayoutMetrics
	// https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setDeviceMetricsOverride
	// https://chromedevtools.github.io/devtools-protocol/tot/Page#method-captureScreenshot
	// https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-clearDeviceMetricsOverride
	public void test1() {

		// Arrange
		setLocation();
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
			data = (String) result.get("data");
			assertThat(data, notNullValue());

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
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
		// Assert
	}

	@Ignore
	@Test
	public void test2() {
		setLocation();
		baseURL = "https://www.iplocation.net";
		driver.get(baseURL);
		locator = By
				.xpath("//table[@class= 'iptable']//*[contains(th, 'IP Location')]");
		element = wait
				.until(ExpectedConditions.visibilityOfElementLocated(locator));
		System.err.println(element.getText());
	}

	@Test
	public void test3() {
		setLocation();
		baseURL = "https://mycurrentlocation.net";
		driver.get(baseURL);
		locator = By.cssSelector(".location-intro");
		element = wait
				.until(ExpectedConditions.visibilityOfElementLocated(locator));
		assertThat(element.getText(), containsString("Mountain View"));
		System.err.println(element.getText());
	}

	private void setLocation() {
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
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

}
