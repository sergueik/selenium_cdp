package com.github.sergueik.selenium;

/* Copyright 2020,2022 Serguei Kouzmine */

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.CoreMatchers.not;

import org.apache.commons.codec.binary.Base64;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.v107.css.CSS;
import org.openqa.selenium.devtools.v107.dom.DOM;
import org.openqa.selenium.devtools.v107.dom.model.Rect;
import org.openqa.selenium.devtools.v107.emulation.Emulation;
import org.openqa.selenium.devtools.v107.page.Page;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see also:
 * https://github.com/rookieInTraining/selenium-cdp-examples/blob/main/src/test/java/com/rookieintraining/cdp/examples/Pages.java
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-getLayoutMetrics
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setDeviceMetricsOverride
 * https://chromedevtools.github.io/devtools-protocol/tot/Page#method-captureScreenshot
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-clearDeviceMetricsOverride
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class ScreenShotDevToolsTest extends BaseDevToolsTest {
	private static Page.GetLayoutMetricsResponse layoutMetrics;
	private static Rect rect;
	private int width, height;
	private double[] deviceScaleFactors = { 0.85, 0.5, 0.35, 0.25 };
	private String screenshotFileName = "temp.jpg";
	private Base64 base64 = new Base64();

	@Before
	public void before() throws Exception {
		baseURL = "https://www.wikipedia.org";
		driver.get(baseURL);
	}

	@Test
	public void test1() {
		for (int cnt = 0; cnt != deviceScaleFactors.length; cnt++) {
			double deviceScaleFactor = deviceScaleFactors[cnt];
			screenshotFileName = String.format("test1_%03d.jpg",
					(int) (100 * deviceScaleFactor));
			layoutMetrics = chromeDevTools.send(Page.getLayoutMetrics());
			rect = layoutMetrics.getContentSize();
			width = rect.getWidth().intValue();
			height = rect.getHeight().intValue();
			System.err.println(String.format("Content size: %dx%d", width, height));
			chromeDevTools.send(
				// @formatter:off
				Emulation.setDeviceMetricsOverride(
					rect.getWidth().intValue(), 
					rect.getHeight().intValue(),
					deviceScaleFactor, 
					false, 
					Optional.empty(), 
					Optional.empty(),
					Optional.empty(), 
					Optional.empty(), 
					Optional.empty(), 
					Optional.empty(),
					Optional.empty(), 
					Optional.empty(), 
					Optional.empty()
				)
				// @formatter:on
			);
			String dataString = chromeDevTools.send(
				// @formatter:off
				Page.captureScreenshot(
						Optional.of(Page.CaptureScreenshotFormat.JPEG), 
						Optional.of(100),
						Optional.empty(), 
						Optional.of(true), 
						Optional.of(true)
				)
				// @formatter:off
		);
		chromeDevTools.send(Emulation.clearDeviceMetricsOverride());

		byte[] image = base64.decode(dataString);
		try {
			BufferedImage o = ImageIO.read(new ByteArrayInputStream(image));
			System.err.println(String.format("Screenshot dimensions: %dx%d",
					o.getWidth(), o.getHeight()));
			assertThat((int) (width * deviceScaleFactor) - o.getWidth(),
					not(greaterThan(2)));
			assertThat((int) (height * deviceScaleFactor) - o.getHeight(),
					not(greaterThan(2)));
		} catch (IOException e) {
			System.err.println("Exception loading image (ignored): " + e.toString());
		}
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(
					screenshotFileName);
			fileOutputStream.write(image);
			fileOutputStream.close();
		} catch (IOException e) {
			System.err.println("Exception saving image (ignored): " + e.toString());
		}
		}
	}


	@After
	public void clearPage() {
		chromeDevTools.send(CSS.disable());
		try {
			chromeDevTools.send(DOM.disable());
		} catch (DevToolsException e) {
			// DOM agent hasn't been enabled
		}
		driver.get("about:blank");
	}

	@Test
	public void test2() {

		screenshotFileName = "test1_100.jpg";
		chromeDevTools.send(CSS.disable());
		try {
			chromeDevTools.send(DOM.disable());
		} catch (DevToolsException e) {
			// DOM agent hasn't been enabled
		}
		layoutMetrics = chromeDevTools.send(Page.getLayoutMetrics());
		rect = layoutMetrics.getContentSize();
		width = rect.getWidth().intValue();
		height = rect.getHeight().intValue();
		System.err.println(String.format("Content size: %dx%d", width, height));
		chromeDevTools.send(
				// @formatter:off
				Emulation.setDeviceMetricsOverride(
					rect.getWidth().intValue(), 
					rect.getHeight().intValue(),
					1.0, 
					false, 
					Optional.empty(), 
					Optional.empty(),
					Optional.empty(), 
					Optional.empty(), 
					Optional.empty(), 
					Optional.empty(),
					Optional.empty(), 
					Optional.empty(), 
					Optional.empty()
				)
				// @formatter:on
		);
		// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-enable
		chromeDevTools
				.send(DOM.enable(Optional.of(DOM.EnableIncludeWhitespace.NONE)));
		chromeDevTools.send(CSS.enable());
		String dataString = chromeDevTools.send(
				// @formatter:off
				Page.captureScreenshot(
						Optional.of(Page.CaptureScreenshotFormat.JPEG), 
						Optional.of(100),
						Optional.empty(), 
						Optional.of(true), 
						Optional.of(true)
				)
				// @formatter:off
		);

		Base64 base64 = new Base64();
		byte[] image = base64.decode(dataString);
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(
					screenshotFileName);
			fileOutputStream.write(image);
			fileOutputStream.close();
		} catch (IOException e) {
			System.err.println("Exception saving image (ignored): " + e.toString());
		}
	}
}
