package com.github.sergueik.selenium;

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

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setBlockedURLs
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#event-loadingFailed
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#event-requestWillBeSent
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#event-responseReceived
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-continueInterceptedRequest
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#type-RequestPattern
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setCacheDisabled
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-clearBrowserCache
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class FilterUrlCdpTest extends BaseCdpTest {

	private final static String baseURL = "http://www.wikipedia.org";
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();

	private static String command = null;
	private static Map<String, Object> viewport = new HashMap<>();
	private static Base64 base64 = new Base64();
	private static String dataString = null;
	private static byte[] image;

	@Before
	public void before() throws Exception {
		command = "Network.enable";
		params = new HashMap<>();
		final long maxTotalBufferSize = 10000000;
		final long maxResourceBufferSize = 5000000;
		final long maxPostDataSize = 5000000;

		params.put("maxTotalBufferSize", maxTotalBufferSize);
		params.put("maxResourceBufferSize", maxResourceBufferSize);
		params.put("maxPostDataSize", maxPostDataSize);

		driver.executeCdpCommand(command, params);
		params = new HashMap<>();
		String[] urls = new String[] { "*.css", "*.png", "*.jpg", "*.gif",
				"*favicon.ico" };
		params.put("urls", urls);
		command = "Network.setBlockedURLs";
		driver.executeCdpCommand(command, params);
		command = "Network.clearBrowserCache";
		driver.executeCdpCommand(command, new HashMap<>());
		params = new HashMap<>();
		params.put("cacheDisabled", true);
		command = "Network.setCacheDisabled";
		driver.executeCdpCommand(command, params);
	}

	@After
	public void after() {
		Utils.sleep(1000);
		command = "Network.setBlockedURLs";
		params = new HashMap<>();
		String[] urls = new String[] {};
		params.put("urls", urls);
		driver.executeCdpCommand(command, params);
		command = "Network.disable";
		driver.executeCdpCommand(command, new HashMap<>());
		params = new HashMap<>();
		params.put("cacheDisabled", false);
		command = "Network.setCacheDisabled";
		driver.executeCdpCommand(command, params);
	}

	// @Ignore
	// see also:
	// https://github.com/adiohana/selenium-chrome-devtools-examples/blob/master/src/test/java/ChromeDevToolsTest.java
	@Test
	public void test1() {
		driver.get(baseURL);
		Utils.sleep(1000);
		WebElement element = driver.findElement(
				By.cssSelector("#www-wikipedia-org > div.central-textlogo > img"));
		Utils.highlight(element);
		int x = element.getLocation().getX();
		int y = element.getLocation().getY();
		int width = element.getSize().getWidth();
		int height = element.getSize().getHeight();
		int scale = 1;
		command = "Page.captureScreenshot";
		params = new HashMap<String, Object>();
		viewport = new HashMap<>();
		System.err.println("Specified viewport: " + String
				.format("x=%d, y=%d, width=%d, height=%d", x, y, width, height));
		viewport.put("x", (double) x);
		viewport.put("y", (double) y);
		viewport.put("width", (double) width);
		viewport.put("height", (double) height);
		viewport.put("scale", scale);
		params.put("clip", viewport);
		System.err
				.println(String.format("Viewport: %d %d %d", x, y, width, height));
		try {
			// Act
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, notNullValue());
			assertThat(result, hasKey("data"));
			dataString = (String) result.get("data");
			assertThat(dataString, notNullValue());
			image = base64.decode(dataString);
			try {
				BufferedImage o = ImageIO.read(new ByteArrayInputStream(image));
				assertThat(o.getWidth(), greaterThan(0));
				assertThat(o.getHeight(), greaterThan(0));
			} catch (IOException e) {
				System.err
						.println("Exception loading image (	ignored): " + e.toString());
			}
			String screenshotFileName = "logo.png";
			// will be an image of a broken image and a word "Wikipedia"
			// possibly from the "alt" attribute
			FileOutputStream fileOutputStream = new FileOutputStream(
					screenshotFileName);
			fileOutputStream.write(image);
			fileOutputStream.close();
		} catch (IOException e) {
			System.err.println("Exception saving image (ignored): " + e.toString());

		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}

	}

	@Test
	public void test2() {
		driver.get(Utils.getPageContent("ng_basic.htm"));
		WebElement element = wait
				.until(ExpectedConditions.visibilityOf(driver.findElement(
						By.cssSelector("body > table > tbody > tr > td:nth-child(1)"))));
		assertThat(element.getText(), containsString("A1"));
		System.err.println(element.getText());

	}

	@Test
	public void test3() {
		params = new HashMap<>();
		String[] urls = new String[] { "*.js" };
		params.put("urls", urls);
		command = "Network.setBlockedURLs";
		driver.executeCdpCommand(command, params);
		command = "Network.clearBrowserCache";
		driver.executeCdpCommand(command, new HashMap<>());
		params = new HashMap<>();
		params.put("cacheDisabled", true);
		command = "Network.setCacheDisabled";
		driver.executeCdpCommand(command, params);
		driver.get(Utils.getPageContent("ng_basic.htm"));
		WebElement element = wait
				.until(ExpectedConditions.visibilityOf(driver.findElement(
						By.cssSelector("body > table > tbody > tr > td:nth-child(1)"))));
		assertThat(element.getText(), containsString("{{item.a}}"));
		System.err.println(element.getText());
	}

}
