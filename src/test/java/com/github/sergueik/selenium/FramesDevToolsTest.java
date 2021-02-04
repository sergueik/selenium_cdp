package com.github.sergueik.selenium;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.TimeoutException;
// need to use branch cdp_codegen of SeleniumHQ/selenium
// https://github.com/SeleniumHQ/selenium/tree/cdp_codegen/java/client/src/org/openqa/selenium/devtools
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.dom.model.RGBA;
import org.openqa.selenium.devtools.overlay.Overlay;
import org.openqa.selenium.devtools.page.Page;
import org.openqa.selenium.devtools.page.model.FrameTree;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see:
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-getFrameTree
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#type-Frame
 * https://chromedevtools.github.io/devtools-protocol/tot/Overlay/#method-highlightFrame
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#type-RGBA
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class FramesDevToolsTest {

	private static boolean runHeadless = false;
	private static String osName = Utils.getOSName();
	private static ChromiumDriver driver;
	private static DevTools chromeDevTools;

	private static String baseURL = "about:blank";

	private static Map<String, Object> headers = new HashMap<>();

	@BeforeClass
	public static void setUp() throws Exception {

		if (System.getenv().containsKey("HEADLESS")
				&& System.getenv("HEADLESS").matches("(?:true|yes|1)")) {
			runHeadless = true;
		}
		// force the headless flag to be true to support Unix console execution
		if (!(Utils.getOSName().equals("windows"))
				&& !(System.getenv().containsKey("DISPLAY"))) {
			runHeadless = true;
		}
		System
				.setProperty("webdriver.chrome.driver",
						Paths.get(System.getProperty("user.home"))
								.resolve("Downloads").resolve(osName.equals("windows")
										? "chromedriver.exe" : "chromedriver")
								.toAbsolutePath().toString());

		if (runHeadless) {
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--headless", "--disable-gpu");
			driver = new ChromeDriver(options);
		} else {
			driver = new ChromeDriver();
		}
		Utils.setDriver(driver);
		chromeDevTools = driver.getDevTools();
		chromeDevTools.createSession();
	}

	@BeforeClass
	// https://chromedevtools.github.io/devtools-protocol/tot/Console#method-enable
	// https://chromedevtools.github.io/devtools-protocol/tot/Log#method-enable
	public static void beforeClass() throws Exception {
		// NOTE:
		// the github location of package org.openqa.selenium.devtools.console
		// is uncertain
		// enable Console
		// chromeDevTools.send(Log.enable());
		// add event listener to show in host console the browser console message
		// chromeDevTools.addListener(Log.entryAdded(), System.err::println);
		driver.get(baseURL);
	}

	@AfterClass
	public static void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	@Ignore
	@Test
	public void test1() {
		// Arrange
		driver.get("https://cloud.google.com/products/calculator");
		FrameTree response = chromeDevTools.send(Page.getFrameTree());
		Optional<List<FrameTree>> frames = response.getChildFrames();
		if (frames.isPresent()) {
			frames.get().stream().map(o -> o.getFrame())
					.map(frame -> String.format("Frame %s id: %s url: %s",
							frame.getName().isPresent()
									? String.format("name: %s", frame.getName().get()) : "",
							frame.getId(), frame.getUrl()))
					.forEach(System.err::println);

			RGBA color = new RGBA(128, 0, 0, Optional.empty());
			frames.get().stream().map(o -> o.getFrame()).forEach(frame -> {
				try {
					chromeDevTools.send(Overlay.highlightFrame(frame.getId(),
							Optional.of(color), Optional.empty()));
				} catch (TimeoutException e) {
					// WARNING: Unhandled type:
					// {"id":9,"error":{"code":-32602,"message":"Invalid
					// parameters","data":"Failed to deserialize params.contentColor.a -
					// BINDINGS: double value expected at position
					// 71"},"sessionId":"02D4DB8D745FBC153C1753C69CB75C14"}
				}
			});
		}
	}

}

