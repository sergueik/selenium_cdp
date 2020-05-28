package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.junit.Ignore;
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
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
// https://www.logicalincrements.com/articles/resolution

public class ChromiumWindowSizeTest {

	private static final int customWidth = 1366;
	private static final int customHeight = 768;
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

	private static String command = null;
	private static Map<String, Object> params = new HashMap<>();
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> data = new HashMap<>();
	private static boolean headless = true;

	@BeforeClass
	public static void beforeClass() throws Exception {
		System.setProperty("webdriver.chrome.driver", Paths.get(System.getProperty("user.home")).resolve("Downloads")
				.resolve(osName.equals("windows") ? "chromedriver.exe" : "chromedriver").toAbsolutePath().toString());

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--ssl-protocol=any", "--ignore-ssl-errors=true", "--disable-extensions",
				"--ignore-certificate-errors", String.format("window-size=%d,%d", customWidth, customHeight));
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

	// https://chromedevtools.github.io/devtools-protocol/tot/Browser#method-getWindowBounds
	// @Ignore
	@SuppressWarnings("unchecked")
	@Test
	public void measureScreenSizeTest() {

		command = "Browser.getWindowBounds";
		driver.get(Utils.getPageContent(imagePage));
		try {
			// Act
			params = new HashMap<String, Object>();
			params.put("windowId", windowId);
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, notNullValue());
			assertThat(result, hasKey("bounds"));

			data = (Map<String, Object>) result.get("bounds");
			assertThat(data, notNullValue());
			assertThat(data, hasKey("width"));
			assertThat(data, hasKey("height"));
			int width = Integer.parseInt(data.get("width").toString());
			assertThat(String.format("Expected screen width: %d", customWidth), width, is(customWidth));
			// when the browser window is visible test will likely fail
			// because dimensions will not match:
			// Expected: is <1366> but: was <1051>
			int height = Integer.parseInt(data.get("height").toString());
			assertThat(height, is(customHeight));
			if (debug) {
				System.err.println("Command " + command + " result: " + result);
			}

			if (debug) {
				System.err.println("Internal: result class: " + result.getClass().getName());
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
			System.err.println("JSON Syntax exception in " + command + " (ignored): " + e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			e.printStackTrace();
			throw (new RuntimeException(e));
		}
	}

	// @Ignore
	@Test(expected = org.openqa.selenium.WebDriverException.class)
	public void resizeBrowserWindowTest() {

		driver.get(Utils.getPageContent(imagePage));
		command = "Browser.setWindowBounds";
		try {
			// Act
			params = new HashMap<String, Object>();
			params.put("windowId", (Object) windowId);
			// java.lang.UnsupportedOperationException
			data = new HashMap<>();
			data.put("left", (Object) 0);
			data.put("top", (Object) 0);
			data.put("width", (Object) (customWidth / 2));
			data.put("height", (Object) (customHeight / 2));
			data.put("windowState", (Object) "normal");
			params.put("Bounds ", data);
			driver.executeCdpCommand(command, params);
			// TODO: Assert
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): " + e.toString());
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
	public void evaluateElementLocationTest() {
		driver.get(Utils.getPageContent(fixedSizePage));
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.absolute > span")));
		assertThat(element, notNullValue());
		assertThat(element.isDisplayed(), is(true));
		System.err.println("evaluateSizeTest element location: x=" + element.getLocation().x + "," + "y="
				+ element.getLocation().y);
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
			FileOutputStream fileOutputStream = new FileOutputStream(screenshotFileName);
			fileOutputStream.write(image);
			fileOutputStream.close();
			// read it back
			getScreenshotSize(screenshotFileName);
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): " + e.toString());
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
		Map<String, Integer> dimension = Utils.getImageDimension(screenshotFileName);
		System.err.println("Dimensions: " + dimension.get("width") + "," + dimension.get("height"));
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Browser#method-getWindowForTarget
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
			System.err.println("JSON Syntax exception in " + command + " (ignored): " + e.toString());
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
