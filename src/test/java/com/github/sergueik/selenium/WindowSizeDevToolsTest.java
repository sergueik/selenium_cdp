package com.github.sergueik.selenium;

/**
 * Copyright 2022-2024 Serguei Kouzmine
 */


import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.Command;
import org.openqa.selenium.devtools.ConverterFunctions;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.v124.browser.Browser;
import org.openqa.selenium.devtools.v124.browser.Browser.GetWindowForTargetResponse;
import org.openqa.selenium.devtools.v124.browser.model.Bounds;
import org.openqa.selenium.devtools.v124.browser.model.WindowID;
import org.openqa.selenium.devtools.v124.browser.model.WindowState;

import com.google.common.collect.ImmutableMap;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see:
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-getWindowForTarget
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-getWindowBounds
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-setWindowBounds
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#type-WindowID
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class WindowSizeDevToolsTest extends BaseDevToolsTest {

	private static boolean debug = false;
	private static WebElement element = null;

	private static WindowID windowId;
	private static Bounds bounds;
	private GetWindowForTargetResponse result;

	@Before
	public void before() throws DevToolsException {
		result = chromeDevTools.send(Browser.getWindowForTarget(Optional.empty()));
		windowId = result.getWindowId();
		bounds = result.getBounds();
	}

	@Test
	public void test1() {
		// Act
		try {
			result = chromeDevTools
					.send(Browser.getWindowForTarget(Optional.empty()));
			assertThat(result, notNullValue());
			windowId = result.getWindowId();
			bounds = result.getBounds();
			assertThat(bounds, notNullValue());
			System.err.println(String.format(
					"test1: " + "top: %d left: %d width: %d height: %d state: %s",
					bounds.getTop().get(), bounds.getLeft().get(),
					bounds.getWidth().get(), bounds.getHeight().get(),
					bounds.getWindowState().get()));
		} catch (DevToolsException e) {
			System.err.println("DevToolsException exception " + "in test1"
					+ " (ignored): " + Utils.processExceptionMessage(e.getMessage()));
		}
	}

	// NOTE syntax
	// origin:
	// https://github.com/rookieInTraining/selenium-cdp-examples/blob/main/src/test/java/com/rookieintraining/cdp/alternative_examples/Browser.java#L34
	@Test
	public void test2() {
		// Act
		try {
			bounds = chromeDevTools.send(new Command<>("Browser.getWindowBounds",
					ImmutableMap.of("windowId", windowId),
					ConverterFunctions.map("bounds", Bounds.class)));
			assertThat(bounds, notNullValue());
			System.err.println(String.format(
					"test2: " + "top: %d left: %d width: %d height: %d state: %s",
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
		// Act
		try {
			// java.lang.NullPointerException: windowId is required
			bounds = chromeDevTools.send(Browser.getWindowBounds(windowId));
			assertThat(bounds, notNullValue());

			System.err.println(String.format(
					"test3: " + "top: %d left: %d width: %d height: %d state: %s",
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
		// Act
		try {
			bounds = new Bounds(Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.of(WindowState.FULLSCREEN));
			chromeDevTools.send(Browser.setWindowBounds(windowId, bounds));
			bounds = chromeDevTools.send(Browser.getWindowBounds(windowId));
			assertThat(bounds, notNullValue());
			System.err.println(String.format(
					"test4: " + "top: %d left: %d width: %d height: %d state: %s",
					bounds.getTop().get(), bounds.getLeft().get(),
					bounds.getWidth().get(), bounds.getHeight().get(),
					bounds.getWindowState().get()));
		} catch (DevToolsException e) {
			System.err.println("DevToolsException exception " + "in test4"
					+ " (ignored): " + Utils.processExceptionMessage(e.getMessage()));
		}

	}

	@Test
	public void test5() {
		// Act
		try {
			bounds = new Bounds(Optional.of(bounds.getTop().get() + 100),
					bounds.getLeft(), bounds.getWidth(), bounds.getHeight(),
					Optional.of(WindowState.NORMAL));

			chromeDevTools.send(Browser.setWindowBounds(windowId, bounds));
			bounds = chromeDevTools.send(Browser.getWindowBounds(windowId));
			System.err.println(String.format(
					"test5: " + "top: %d left: %d width: %d height: %d state: %s",
					bounds.getTop().get(), bounds.getLeft().get(),
					bounds.getWidth().get(), bounds.getHeight().get(),
					bounds.getWindowState().get()));

		} catch (DevToolsException e) {
			System.err.println("DevToolsException exception " + "in test5"
					+ " (ignored): " + Utils.processExceptionMessage(e.getMessage()));
		}
	}

	@Test
	public void test6() {
		// Act
		try {
			bounds = new Bounds(Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.of(WindowState.MINIMIZED));

			// No Value present
			System.err.println(String.format("test6: " + "state: %s",
					bounds.getWindowState().get()));
			chromeDevTools.send(Browser.setWindowBounds(windowId, bounds));
			bounds = chromeDevTools.send(Browser.getWindowBounds(windowId));
			System.err.println(String.format(
					"test6: " + "top: %d left: %d width: %d height: %d state: %s",
					bounds.getTop().get(), bounds.getLeft().get(),
					bounds.getWidth().get(), bounds.getHeight().get(),
					bounds.getWindowState().get()));

		} catch (DevToolsException e) {
			System.err.println("DevToolsException exception " + "in test6"
					+ " (ignored): " + Utils.processExceptionMessage(e.getMessage()));
		}
	}

	@Test
	public void test7() {
		// Act
		try {
			bounds = new Bounds(Optional.empty(), Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.of(WindowState.MINIMIZED));
			chromeDevTools.send(Browser.setWindowBounds(windowId, bounds));
			bounds = chromeDevTools.send(Browser.getWindowBounds(windowId));
			System.err.println(String.format("test7(1): " + "state: %s",
					bounds.getWindowState().get()));

			bounds = new Bounds(Optional.of(0), Optional.of(0), Optional.of(1024),
					Optional.of(768), Optional.of(WindowState.NORMAL));
			chromeDevTools.send(Browser.setWindowBounds(windowId, bounds));
			bounds = chromeDevTools.send(Browser.getWindowBounds(windowId));
			System.err.println(String.format(
					"test7 (2): " + "top: %d left: %d width: %d height: %d state: %s",
					bounds.getTop().get(), bounds.getLeft().get(),
					bounds.getWidth().get(), bounds.getHeight().get(),
					bounds.getWindowState().get()));
			bounds = new Bounds(Optional.of(0), Optional.of(0), Optional.of(1024),
					Optional.of(768), Optional.of(WindowState.NORMAL));
			chromeDevTools.send(Browser.setWindowBounds(windowId, bounds));
			bounds = chromeDevTools.send(Browser.getWindowBounds(windowId));
			System.err.println(String.format(
					"test7 (3): " + "top: %d left: %d width: %d height: %d state: %s",
					bounds.getTop().get(), bounds.getLeft().get(),
					bounds.getWidth().get(), bounds.getHeight().get(),
					bounds.getWindowState().get()));

		} catch (DevToolsException e) {
			/*
			 * DevToolsException exception in test5 (ignored): {"id":11,"error":{"code":-32000, "message":"The 'minimized', 'maximized' and 'fullscreen' states cannot be combined with 'left', 'top', 'width' or 'height'"},
			 * "sessionId":"DBB8A2BC3A6EDBB228EEA5E26F34D42D"}
			 */
			System.err.println("DevToolsException exception " + "in test7"
					+ " (ignored): " + Utils.processExceptionMessage(e.getMessage()));
		}
	}
}
