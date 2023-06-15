package com.github.sergueik.selenium;
/**
 * Copyright 2021,2022,2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v113.emulation.Emulation;
import org.openqa.selenium.devtools.v113.page.Page;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see:
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setGeolocationOverride
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-clearGeolocationOverride
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-captureScreenshot
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class GeolocationOverrideDevToolsTest extends BaseDevToolsTest {

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
		chromeDevTools.send(Emulation.clearGeolocationOverride());
	}

	@Test
	public void test1() {
		// Act
		baseURL = "https://www.google.com/maps";
		driver.get(baseURL);
		// click "my location" button when drawn

		// fails with timeout here, but not in CDP Test
		// element = driver.findElement(locator);

		element = wait
				.until(ExpectedConditions.visibilityOfElementLocated(locator));
		element.click();
		// unclear what event to wait for here
		Utils.sleep(5000);

		data = chromeDevTools.send(
				// @formatter:off
				Page.captureScreenshot(
						Optional.of(Page.CaptureScreenshotFormat.JPEG), // format
						Optional.of(100), // quality
						Optional.empty(),  // clip
						Optional.of(false),  // fromSurface
						Optional.of(false), // captureBeyondViewport
						Optional.of(false) // optimizeForSpeed
				)
				// @formatter:on
		);
		try {
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
		}
	}

	// @Ignore
	@Test
	public void test4() {
		// Act
		baseURL = "https://mycurrentlocation.net";
		driver.get(baseURL);
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		wait.pollingEvery(Duration.ofMillis(pollingInterval));

		locator = By.cssSelector(".location-intro");
		element = wait
				.until(ExpectedConditions.visibilityOfElementLocated(locator));
		assertThat(element.getText(), containsString("Mountain View"));
		System.err.println("Location explained: " + element.getText());
	}

	private void setLocation(Double latitude, Double longitude, long accuracy) {
		chromeDevTools.send(Emulation.setGeolocationOverride(Optional.of(latitude),
				Optional.of(longitude), Optional.of(accuracy)));
	}

	private void setLocation() {
		final Double latitude = 37.422290;
		final Double longitude = -122.084057;
		final long accuracy = 100;
		setLocation(latitude, longitude, accuracy);
	}
	// Warning: after:
	// WARNING: Failed to shutdown Driver Command Executor
	// org.openqa.selenium.WebDriverException: Timed out waiting for driver server
	// to stop.
}
