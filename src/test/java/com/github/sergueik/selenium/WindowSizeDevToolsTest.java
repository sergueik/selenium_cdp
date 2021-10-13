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
import java.util.Optional;

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
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.v93.browser.Browser;
import org.openqa.selenium.devtools.v93.browser.Browser.GetWindowForTargetResponse;
import org.openqa.selenium.devtools.v93.browser.model.Bounds;
import org.openqa.selenium.devtools.v93.browser.model.WindowState;
import org.openqa.selenium.devtools.v93.target.Target;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.JsonSyntaxException;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see:
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-setWindowBounds
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-getWindowBounds
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-getWindowForTarget
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class WindowSizeDevToolsTest extends BaseDevToolsTest {

	private static final int customWidth = 1366;
	private static final int customHeight = 768;
	private static final String imagePage = "image_page.html";
	private static final String fixedSizePage = "fixed_size_page.html";

	private static boolean debug = false;
	private static WebElement element = null;

	private static Long windowId;

	private static Bounds bounds;
	private static GetWindowForTargetResponse result;

	@Test
	public void test1() {
		System.err.println("test1");
		// Act
		try {
			result = chromeDevTools
					.send(Browser.getWindowForTarget(Optional.empty()));
			System.err.println(String.format(
					"Browser.getWindowForTarget result. top: %d\tleft: %d\twidth: %d\theight: %d\tstate: %s",
					result.getBounds().getTop().get(), result.getBounds().getLeft().get(),
					result.getBounds().getWidth().get(),
					result.getBounds().getHeight().get(),
					result.getBounds().getWindowState().get()));
		} catch (DevToolsException e) {
			System.err.println("DevToolsException exception " + "in test2"
					+ " (ignored): " + Utils.processExceptionMessage(e.getMessage()));
		}
	}

	@Test
	public void test2() {
		System.err.println("test2");
		// Act
		try {
			// java.lang.NullPointerException: windowId is required
			bounds = chromeDevTools
					.send(Browser.getWindowBounds(result.getWindowId()));

			System.err.println(String.format(
					"Browser Window bounds: top: %d\tleft: %d\twidth: %d\theight: %d\tstate: %s",
					bounds.getTop().get(), bounds.getLeft().get(),
					bounds.getWidth().get(), bounds.getHeight().get(),
					bounds.getWindowState().get()));
		} catch (DevToolsException e) {
			System.err.println("DevToolsException exception " + "in test2"
					+ " (ignored): " + Utils.processExceptionMessage(e.getMessage()));
		}
	}

	@Test
	public void test3() {
		System.err.println("test3");
		// Act
		try {
			Bounds bounds2 = new Bounds(Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(),
					Optional.of(WindowState.FULLSCREEN));
			chromeDevTools
					.send(Browser.setWindowBounds(result.getWindowId(), bounds2));
			bounds2 = chromeDevTools
					.send(Browser.getWindowBounds(result.getWindowId()));

			System.err.println(String.format(
					"New Browser Window bounds: top: %d\tleft: %d\twidth: %d\theight: %d\tstate: %s",
					bounds2.getTop().get(), bounds2.getLeft().get(),
					bounds2.getWidth().get(), bounds2.getHeight().get(),
					bounds2.getWindowState().get()));
		} catch (DevToolsException e) {
			System.err.println("DevToolsException exception " + "in test2"
					+ " (ignored): " + Utils.processExceptionMessage(e.getMessage()));
		}

	}

	@Test
	public void test4() {
		System.err.println("test4");
		// Act
		try {
			Bounds bounds2 = new Bounds(Optional.of(bounds.getTop().get() + 100),
					bounds.getLeft(), bounds.getWidth(), bounds.getHeight(),
					Optional.of(WindowState.NORMAL));
			bounds2 = chromeDevTools
					.send(Browser.getWindowBounds(result.getWindowId()));

			System.err.println(String.format(
					"New Browser Window bounds: top: %d\tleft: %d\twidth: %d\theight: %d\tstate: %s",
					bounds2.getTop().get(), bounds2.getLeft().get(),
					bounds2.getWidth().get(), bounds2.getHeight().get(),
					bounds2.getWindowState().get()));
			chromeDevTools
					.send(Browser.setWindowBounds(result.getWindowId(), bounds2));

		} catch (DevToolsException e) {
			System.err.println("DevToolsException exception " + "in test2"
					+ " (ignored): " + Utils.processExceptionMessage(e.getMessage()));
		}

	}
}
