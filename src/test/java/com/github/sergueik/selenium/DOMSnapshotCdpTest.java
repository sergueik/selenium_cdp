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
import java.util.List;
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

public class DOMSnapshotCdpTest extends BaseCdpTest {

	private static String page = null;
	private static String command = null;
	private static Map<String, Object> params = new HashMap<>();
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> data = new HashMap<>();
	private final static String baseURL = "https://www.wikipedia.org";

	@After
	public void afterTest() {
		command = "DOMSnapshot.disable";
		params = new HashMap<String, Object>();
		result = driver.executeCdpCommand(command, params);
		driver.get("about:blank");
	}

	@Before
	public void beforeTest() {
		command = "DOMSnapshot.enable";
		params = new HashMap<String, Object>();
		result = driver.executeCdpCommand(command, params);
		driver.get(baseURL);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Browser#method-getWindowBounds
	// @Ignore
	@SuppressWarnings("unchecked")
	@Test
	public void test() {

		try {
			// Act
			command = "DOMSnapshot.captureSnapshot";
			params = new HashMap<String, Object>();
			result = driver.executeCdpCommand(command, params);
			command = "DOMSnapshot.enable";
			params = new HashMap<String, Object>();
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, notNullValue());
			assertThat(result, hasKey("documents"));

			data = (Map<String, Object>) result.get("documents");
			// documents - the nodes in the DOM tree. The DOMNode at index 0 corresponds to
			// the root document.
			assertThat(data, notNullValue());

			for (String field : Arrays.asList(new String[] { "documentURL", "title", "baseURL", "contentLanguage",
					"NodeTreeSnapshot", "nodes", "layout", "textBoxes", "contentWidth" })) {
				assertThat(data, hasKey(field));
			}
			assertThat(result, hasKey("strings"));
			List<String>strings = (List<String>)result.get("strings");

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

}
