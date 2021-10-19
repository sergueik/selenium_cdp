package com.github.sergueik.selenium;

import java.util.Optional;

import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.Command;
import org.openqa.selenium.devtools.ConverterFunctions;
import org.openqa.selenium.devtools.DevToolsException;

import org.openqa.selenium.devtools.v94.browser.Browser;
import org.openqa.selenium.devtools.v94.browser.Browser.GetWindowForTargetResponse;
import org.openqa.selenium.devtools.v94.browser.model.Bounds;
import org.openqa.selenium.devtools.v94.browser.model.WindowID;
import org.openqa.selenium.devtools.v94.browser.model.WindowState;

import com.google.common.collect.ImmutableMap;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see:
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-setWindowBounds
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-getWindowBounds
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-getWindowForTarget
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class WindowSizeDevToolsTest extends BaseDevToolsTest {

	private static boolean debug = false;
	private static WebElement element = null;

	private static WindowID windowId;
	private static Bounds bounds;

	@Test
	public void test1() {
		System.err.println("test1");
		// Act
		try {
			final GetWindowForTargetResponse result = chromeDevTools
					.send(Browser.getWindowForTarget(Optional.empty()));
			windowId = result.getWindowId();
			System.err.println(String.format(
					"top: %d left: %d width: %d height: %d state: %s",
					result.getBounds().getTop().get(), result.getBounds().getLeft().get(),
					result.getBounds().getWidth().get(),
					result.getBounds().getHeight().get(),
					result.getBounds().getWindowState().get()));
		} catch (DevToolsException e) {
			System.err.println("DevToolsException exception " + "in test2"
					+ " (ignored): " + Utils.processExceptionMessage(e.getMessage()));
		}
	}

	// origin:
	// https://github.com/rookieInTraining/selenium-cdp-examples/blob/main/src/test/java/com/rookieintraining/cdp/alternative_examples/Browser.java#L34
	@Test
	public void test2() {
		System.err.println("test2");
		// Act
		try {
			Bounds bounds2 = chromeDevTools.send(new Command<>(
					"Browser.getWindowBounds", ImmutableMap.of("windowId", windowId),
					ConverterFunctions.map("bounds", Bounds.class)));
			System.err.println(
					String.format("top: %d left: %d width: %d height: %d state: %s",
							bounds2.getTop().get(), bounds2.getLeft().get(),
							bounds2.getWidth().get(), bounds2.getHeight().get(),
							bounds2.getWindowState().get()));
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
			// java.lang.NullPointerException: windowId is required
			bounds = chromeDevTools.send(Browser.getWindowBounds(windowId));

			System.err.println(
					String.format("top: %d left: %d width: %d height: %d state: %s",
							bounds.getTop().get(), bounds.getLeft().get(),
							bounds.getWidth().get(), bounds.getHeight().get(),
							bounds.getWindowState().get()));
		} catch (DevToolsException e) {
			System.err.println("DevToolsException exception " + "in test3"
					+ " (ignored): " + Utils.processExceptionMessage(e.getMessage()));
		}
	}

	@Test
	public void test4() {
		System.err.println("test4");
		// Act
		try {
			Bounds bounds2 = new Bounds(Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.empty(),
					Optional.of(WindowState.FULLSCREEN));
			chromeDevTools.send(Browser.setWindowBounds(windowId, bounds2));
			bounds2 = chromeDevTools.send(Browser.getWindowBounds(windowId));

			System.err.println(
					String.format("top: %d left: %d width: %d height: %d state: %s",
							bounds2.getTop().get(), bounds2.getLeft().get(),
							bounds2.getWidth().get(), bounds2.getHeight().get(),
							bounds2.getWindowState().get()));
		} catch (DevToolsException e) {
			System.err.println("DevToolsException exception " + "in test4"
					+ " (ignored): " + Utils.processExceptionMessage(e.getMessage()));
		}

	}

	@Test
	public void test5() {
		System.err.println("test5");
		// Act
		try {
			Bounds bounds2 = new Bounds(Optional.of(bounds.getTop().get() + 100),
					bounds.getLeft(), bounds.getWidth(), bounds.getHeight(),
					Optional.of(WindowState.NORMAL));

			System.err.println(
					String.format("top: %d left: %d width: %d height: %d state: %s",
							bounds2.getTop().get(), bounds2.getLeft().get(),
							bounds2.getWidth().get(), bounds2.getHeight().get(),
							bounds2.getWindowState().get()));
			chromeDevTools.send(Browser.setWindowBounds(windowId, bounds2));

		} catch (DevToolsException e) {
			/*
			 * DevToolsException exception in test5 (ignored): {"id":11,"error":{"code":-32000,
			"message":"The 'minimized', 'maximized' and 'fullscreen' states cannot be combin
			ed with 'left', 'top', 'width' or 'height'"},"sessionId":"DBB8A2BC3A6EDBB228EEA5
			E26F34D42D"}
			 */
			System.err.println("DevToolsException exception " + "in test5"
					+ " (ignored): " + Utils.processExceptionMessage(e.getMessage()));
		}

	}
}
