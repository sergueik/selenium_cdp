package com.github.sergueik.selenium;

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
import java.util.Optional;

import javax.imageio.ImageIO;

import org.openqa.selenium.devtools.v89.dom.model.Rect;
import org.openqa.selenium.devtools.v89.emulation.Emulation;
import org.openqa.selenium.devtools.v89.page.Page;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see also:
 * https://github.com/rookieInTraining/selenium-cdp-examples/blob/main/src/test/java/com/rookieintraining/cdp/examples/Pages.java
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class ScreenShotDevToolsTest extends BaseDevToolsTest {
	private static Page.GetLayoutMetricsResponse layoutMetrics;
	private static Rect rect;
	private int width, height;
	private double deviceScaleFactor = 0.85;

	@Before
	public void before() throws Exception {
		baseURL = "https://www.wikipedia.org";
		driver.get(baseURL);
	}


	@Test
	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-getLayoutMetrics
	// https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setDeviceMetricsOverride
	// https://chromedevtools.github.io/devtools-protocol/tot/Page#method-captureScreenshot
	// https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-clearDeviceMetricsOverride
	public void test() {

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

		Base64 base64 = new Base64();
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
		String screenshotFileName = "temp.jpg";
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
