package com.github.sergueik.selenium;

/* Copyright 2023 Serguei Kouzmine */

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v109.network.model.TimeSinceEpoch;
import org.openqa.selenium.devtools.v109.page.Page;
import org.openqa.selenium.devtools.v109.page.Page.StartScreencastFormat;
import org.openqa.selenium.devtools.v109.page.model.ScreencastFrame;
import org.openqa.selenium.devtools.v109.page.model.ScreencastFrameMetadata;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Actions;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see also:
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#event-screencastFrame
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-startScreencast
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-reload
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-screencastFrameAck
 * https://javadoc.io/doc/org.seleniumhq.selenium/selenium-devtools-v109/latest/overview-summary.html
 *  
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class ScreenCastFramesDevToolsTest extends BaseDevToolsTest {
	public Actions actions;
	private int delay = 30000;
	private final int maxWidth = 640;
	private final int maxHeight = 480;
	private final int quality = 100;
	private final int everyNthFrame = 10;
	private final StartScreencastFormat format = Page.StartScreencastFormat.PNG;
	private String screencastFileName = null;
	private static long nowEpoch;
	private static long frameEpoch;
	private static String data;
	private static boolean debug = true;
	private static byte[] image;
	private static Integer sesionId;
	private final static Base64 base64 = new Base64();

	@Before
	public void beforeTest() throws Exception {
		baseURL = "https://www.youtube.com/watch?v=ik6jzbW2jOE";
		driver.get(baseURL);
	}

	@After
	public void afterTest() throws Exception {
		chromeDevTools.clearListeners();
	}

	@Test
	public void test() {
		// Arrange
		// add to the seconds since the start of the recorging to the screenshot
		// filename
		nowEpoch = System.currentTimeMillis() / 1000;
		chromeDevTools.addListener(Page.screencastFrame(),
				(ScreencastFrame screencastFrame) -> {

					try {
						ScreencastFrameMetadata metadata = screencastFrame.getMetadata();
						// Number deviceHeight = metadata.getDeviceHeight();
						// Number scaleFactor = metadata.getPageScaleFactor();
						// Number deviceWidth = metadata.getDeviceWidth();
						TimeSinceEpoch timestamp = metadata.getTimestamp().get();

						String data = screencastFrame.getData();
						frameEpoch = (long) Math.ceil((double) timestamp.toJson());

						sesionId = screencastFrame.getSessionId();
						image = base64.decode(data);
						screencastFileName = "temp." + (frameEpoch - nowEpoch) + "."
								+ Math.ceil(Math.random() * 100) + ".png";
						try {
							if (debug)
								System.err.println(String.format("Saving frame at: %s as %s",
										Long.toUnsignedString(frameEpoch), screencastFileName));
							FileOutputStream fileOutputStream = new FileOutputStream(
									screencastFileName);
							fileOutputStream.write(image);
							fileOutputStream.close();
							// send ack otherwise frames stop to arrive
							chromeDevTools.send(Page.screencastFrameAck(sesionId));
						} catch (IOException e) {
							System.err
									.println("Exception saving image (ignored): " + e.toString());
						}

					} catch (WebDriverException e) {
						System.err.println("Web Driver exception (ignored): "
								+ Utils.processExceptionMessage(e.getMessage()));
					} catch (Exception e) {
						System.err.println("Exception: " + e.toString());
						throw (new RuntimeException(e));
					}
				});

		// based on:
		// https://stackoverflow.com/questions/63599903/how-can-i-play-a-youtube-video-selenium
		Utils.sleep(5000);
		WebElement element = driver.findElement(By.id("movie_player"));
		actions = new Actions(driver);
		actions.moveToElement(element).click().build().perform();

		element.sendKeys(Keys.SPACE);
		Utils.sleep(1000);
		element.click();
		chromeDevTools.send(Page.startScreencast(Optional.of(format),
				Optional.of(quality), Optional.of(maxWidth), Optional.of(maxHeight),
				Optional.of(everyNthFrame)));
		// chromeDevTools.send(Page.reload(Optional.of(true), Optional.empty()));
		Utils.sleep(delay);

		chromeDevTools.send(Page.stopScreencast());
	}
}