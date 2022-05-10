package com.github.sergueik.selenium;
/**
 * Copyright 2021,2022 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;

import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v101.emulation.Emulation;
import org.openqa.selenium.devtools.v101.page.Page;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see:
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class GeolocationOverrideDevToolsTest extends BaseDevToolsTest {

	private static WebElement element = null;
	private static WebDriverWait wait;
	private static int flexibleWait = 10;
	private static int pollingInterval = 500;

	private static By locator = By
			.cssSelector("div[class *='widget-mylocation-button-icon-common']");
	private static String data = null;
	private static Double latitude = 37.422290;
	private static Double longitude = -122.084057;
	private static long accuracy = 100;

	@Ignore
	@Test
	public void test1() {
		// Act
		setLocation();
		baseURL = "https://www.google.com/maps";
		driver.get(baseURL);
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
		locator = By
				.cssSelector("div[class *='widget-mylocation-button-icon-common']");
		element = wait
				.until(ExpectedConditions.visibilityOfElementLocated(locator));
		element.click();
		// unclear what event to wait for here
		Utils.sleep(5000);

		data = chromeDevTools.send(
				// @formatter:off
				Page.captureScreenshot(
						Optional.of(Page.CaptureScreenshotFormat.JPEG), 
						Optional.of(100),
						Optional.empty(), 
						Optional.of(true), 
						Optional.of(true)
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

	@Ignore
	@Test
	public void test3() {
		// Act
		setLocation();
		baseURL = "https://www.iplocation.net";
		driver.get(baseURL);
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		wait.pollingEvery(Duration.ofMillis(pollingInterval));

		locator = By
				.xpath("//table[@class= 'iptable']//*[contains(th, 'IP Location')]");
		element = wait
				.until(ExpectedConditions.visibilityOfElementLocated(locator));
		System.err.println(element.getText());
	}

	// @Ignore
	@Test
	public void test4() {
		// Act
		setLocation();
		baseURL = "https://mycurrentlocation.net";
		driver.get(baseURL);
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		wait.pollingEvery(Duration.ofMillis(pollingInterval));

		locator = By.cssSelector(".location-intro");
		element = wait
				.until(ExpectedConditions.visibilityOfElementLocated(locator));
		assertThat(element.getText(), containsString("Mountain View"));
		System.err.println(element.getText());
	}

	@Test
	public void test5() {
		// Act
		// this line will disappear from the log, at least on Windows host
		System.err.print("test 5 check");
	}

	private void setLocation() {
		chromeDevTools.send(Emulation.setGeolocationOverride(Optional.of(latitude),
				Optional.of(longitude), Optional.of(accuracy)));

	}

}
