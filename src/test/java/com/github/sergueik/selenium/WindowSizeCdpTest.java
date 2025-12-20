package com.github.sergueik.selenium;
/**
 * Copyright 2022-2024 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.JsonSyntaxException;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge inspired
 * by https://toster.ru/q/653249?e=7897302#comment_1962398
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-getWindowBounds
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-setWindowBounds
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#type-WindowID
 * 
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser#method-getWindowForTarget
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

// https://www.logicalincrements.com/articles/resolution

public class WindowSizeCdpTest {

	private static final long customWidth = 1366;
	private static final long customHeight = 768;
	private static final String imagePage = "image_page.html";
	private static final String fixedSizePage = "fixed_size_page.html";

	private static boolean debug = false;
	private static ChromiumDriver driver;
	private static WebElement element = null;

	private static WebDriverWait wait;
	private static int flexibleWait = 60;
	private static int pollingInterval = 500;
	private static String osName = Utils.getOSName();
	private static String screenshotFileName = null;

	private static Long windowId;

	private static String page = null;
	private static String command = null;
	private static Map<String, Object> params = new HashMap<>();
	private Map<String, Object> bounds = new HashMap<String, Object>();
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> data = new HashMap<>();
	private static boolean headless = true;

	@BeforeClass
	public static void beforeClass() throws Exception {
		System
				.setProperty("webdriver.chrome.driver",
						Paths.get(System.getProperty("user.home"))
								.resolve("Downloads").resolve(osName.equals("windows")
										? "chromedriver.exe" : "chromedriver")
								.toAbsolutePath().toString());

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--ssl-protocol=any", "--ignore-ssl-errors=true",
				"--disable-extensions", "--ignore-certificate-errors",
				String.format("window-size=%d,%d", customWidth, customHeight));
		if (headless) {
			options.addArguments("--headless", "--disable-gpu");
		}
		options.setExperimentalOption("useAutomationExtension", false);

		driver = new ChromeDriver(options);
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
		Utils.setDriver(driver);
	}

	@AfterClass
	public static void afterClass() {
		if (driver != null) {
			driver.quit();
		}
	}

	@After
	public void afterTest() {
		driver.get("about:blank");
	}

	@Before
	public void beforeTest() {
		windowId = getBrowserWindowId();
	}

	//
	// @Ignore
	@SuppressWarnings("unchecked")
	@Test
	public void measureScreenSizeTest() {

		driver.get(Utils.getPageContent(imagePage));
		try {
			command = "Browser.getWindowBounds";
			// Act
			params = new HashMap<String, Object>();
			params.put("windowId", (long) windowId);
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, notNullValue());
			assertThat(result, hasKey("bounds"));

			data = (Map<String, Object>) result.get("bounds");
			assertThat(data, notNullValue());
			for (String field : Arrays.asList(
					new String[] { "left", "top", "width", "height", "windowState" })) {
				assertThat(data, hasKey(field));
			}

			int width = Integer.parseInt(data.get("width").toString());
			assertThat(String.format("Expected screen width: %d", customWidth), width,
					is((int) customWidth));
			// when the browser window is visible test will likely fail
			// because dimensions will not match:
			// Expected: is <1366> but: was <1051>
			int height = Integer.parseInt(data.get("height").toString());
			assertThat(height, is((int) customHeight));
			if (debug) {
				System.err.println("Command " + command + " result: " + result);
			}

			if (debug) {
				System.err
						.println("Internal: result class: " + result.getClass().getName());
				// com.google.common.collect.SingletonImmutableBiMap
			}
			try {
				result.clear();
			} catch (UnsupportedOperationException e) {
				result = null;
			}
			screenshotFileName = "image_page.png";
			getFullPageScreenShot();
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			e.printStackTrace();
			throw (new RuntimeException(e));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void minimizeBrowserWindowTest() {

		driver.get(Utils.getPageContent(imagePage));

		try {
			// Act
			command = "Browser.setWindowBounds";
			params = new HashMap<String, Object>();
			params.put("windowId", (long) windowId);
			bounds = new HashMap<String, Object>();
			bounds.put("windowState", "minimized");
			params.put("bounds", bounds);
			result = driver.executeCdpCommand(command, params);
			// the "Browser.setWindowBounds" result is empty JSON - nothing to verify
			assertThat(result, notNullValue());

			// Assert
			command = "Browser.getWindowBounds";
			params = new HashMap<String, Object>();
			params.put("windowId", windowId);
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, notNullValue());
			System.err.println(
					"resizeBrowserWindowTest Command " + command + " result: " + result);
			data = (Map<String, Object>) result.get("bounds");
			String windowState = data.get("windowState").toString();
			assertThat(windowState, is("minimized"));
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {

			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
			throw e;
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			e.printStackTrace();
			throw (new RuntimeException(e));
		}
	}

	@Test
	public void modifyBrowserWindowTest() {
		try {
			// Act
			command = "Browser.setWindowBounds";
			params = new HashMap<String, Object>();
			params.put("windowId", (long) windowId);
			bounds = new HashMap<String, Object>();
			bounds.put("windowState", "minimized");
			params.put("bounds", bounds);
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, notNullValue());

			// NOTE {"code":-32000,"message":"To resize minimized/maximized/fullscreen
			// window, restore it to normal state first."}

			command = "Browser.setWindowBounds";
			bounds = new HashMap<String, Object>();
			params.put("windowId", (long) windowId);
			bounds.put("windowState", "normal");
			params.put("bounds", bounds);
			result = driver.executeCdpCommand(command, params);

			command = "Browser.setWindowBounds";
			bounds = new HashMap<String, Object>();
			params.put("windowId", (long) windowId);
			bounds.put("left", 0L);
			bounds.put("top", 0L);
			bounds.put("width", customWidth / 2);
			bounds.put("height", customHeight / 2);
			bounds.put("windowState", "normal");
			params.put("bounds", bounds);
			result = driver.executeCdpCommand(command, params);
			assertThat(result, notNullValue());
			// the result is empty JSON - nothing to verify
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {

			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
			throw e;
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			e.printStackTrace();
			throw (new RuntimeException(e));
		}
	}

	@Test
	public void resizeBrowserWindowTest() {

		driver.get(Utils.getPageContent(imagePage));

		try {
			// Act
			command = "Browser.setWindowBounds";
			params = new HashMap<String, Object>();
			params.put("windowId", (long) windowId);
			bounds = new HashMap<String, Object>();
			bounds.put("left", 0L);
			bounds.put("top", 0L);
			bounds.put("width", customWidth / 2);
			bounds.put("height", customHeight / 2);
			bounds.put("windowState", "normal");
			params.put("bounds", bounds);
			result = driver.executeCdpCommand(command, params);
			// the result is empty JSON - nothing to verify
			assertThat(result, notNullValue());
			// Assert
			command = "Browser.getWindowBounds";
			params = new HashMap<String, Object>();
			params.put("windowId", windowId);
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, notNullValue());
			System.err.println(
					"resizeBrowserWindowTest Command " + command + " result: " + result);

			Utils.sleep(1000);
			// TODO: Assert
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {

			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
			throw e;
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			e.printStackTrace();
			throw (new RuntimeException(e));
		}
	}

	// TODO: for Java 11
	// sudo apt install openjdk-11-jdk
	// otherwise tests will fail with
	// java.lang.UnsatisfiedLinkError:
	// Can't load library: /usr/lib/jvm/java-11-openjdk-amd64/lib/libawt_xawt.so
	// and
	// java.lang.NoClassDefFoundError: Could not initialize class
	// javax.imageio.ImageIO
	@Test
	public void evaluateSizeTest() {
		page = "fixed_size_page.html";
		driver.get(Utils.getPageContent(page));
		element = wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector("div.absolute > span")));
		assertThat(element, notNullValue());
		assertThat(element.isDisplayed(), is(true));
		System.err.println("evaluateSizeTest element location: x="
				+ element.getLocation().x + "," + "y=" + element.getLocation().y);
		// TODO: rendering issue:
		// evaluateSizeTest element location: x=-39,y=721
		screenshotFileName = "fixed_size_page.png";
		getFullPageScreenShot();
	}

	@Test
	public void evaluateElementLocationTest() {
		driver.get(Utils.getPageContent(fixedSizePage));
		element = wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector("div.absolute > span")));
		assertThat(element, notNullValue());
		assertThat(element.isDisplayed(), is(true));
		System.err.println("evaluateSizeTest element location: x="
				+ element.getLocation().x + "," + "y=" + element.getLocation().y);
		// TODO: rendering issue:
		// evaluateSizeTest element location: x=-39,y=721
		// has to be
		screenshotFileName = "fixed_size_page.png";
		getFullPageScreenShot();
	}

	private void getFullPageScreenShot() {
		String dataString = null;
		try {

			command = "Page.captureScreenshot";
			result = driver.executeCdpCommand(command, new HashMap<>());
			assertThat(result, notNullValue());
			assertThat(result, hasKey("data"));
			dataString = (String) result.get("data");
			assertThat(dataString, notNullValue());
			Base64 base64 = new Base64();
			byte[] image = base64.decode(dataString);
			BufferedImage o = ImageIO.read(new ByteArrayInputStream(image));
			assertThat(o.getWidth(), greaterThan(0));
			assertThat(o.getHeight(), greaterThan(0));
			FileOutputStream fileOutputStream = new FileOutputStream(
					screenshotFileName);
			fileOutputStream.write(image);
			fileOutputStream.close();
			// read it back
			getScreenshotSize(screenshotFileName);
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (IOException e) {
			System.err.println("Exception saving image (ignored): " + e.toString());
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			e.printStackTrace();
			throw (new RuntimeException(e));
		}
	}

	private void getScreenshotSize(String screenshotFileName) {
		Map<String, Integer> dimension = Utils
				.getImageDimension(screenshotFileName);
		System.err.println("Dimensions: " + dimension.get("width") + ","
				+ dimension.get("height"));
	}

	private long getBrowserWindowId() {
		command = "Browser.getWindowForTarget";
		long windowId = (long) -1;
		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<String, Object>());
			// Assert
			assertThat(result, notNullValue());
			System.err.println("Command " + command + " result: " + result);
			assertThat(result, hasKey("windowId"));
			windowId = (long) result.get("windowId");
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			e.printStackTrace();
			throw (new RuntimeException(e));
		}

		return windowId;
	}

}
