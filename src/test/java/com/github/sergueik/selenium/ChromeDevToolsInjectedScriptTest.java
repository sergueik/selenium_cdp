package com.github.sergueik.selenium;

/**
 * Copyright 2022,2024 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.Paths;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
// need to use branch cdp_codegen of SeleniumHQ/selenium
// https://github.com/SeleniumHQ/selenium/tree/cdp_codegen/java/client/src/org/openqa/selenium/devtools
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v141.page.Page;
import org.openqa.selenium.devtools.v141.page.model.ScriptIdentifier;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-addScriptToEvaluateOnNewDocument
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class ChromeDevToolsInjectedScriptTest {

	private static boolean runHeadless = false;
	private static String osName = Utils.getOSName();
	private static ChromiumDriver driver;
	private static DevTools chromeDevTools;

	private static String script = null;
	private static ScriptIdentifier identifier = null;

	private static String baseURL = "https://www.wikipedia.org";

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

		chromeDevTools = ((HasDevTools) driver).getDevTools();

		chromeDevTools.createSession();
	}

	@AfterClass
	public static void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	// https://github.com/SeleniumHQ/selenium/blob/0f197cbd4fa9acdd2ac3ddebbe0cc9b4ca26bff8/rb/spec/integration/selenium/webdriver/chrome/driver_spec.rb
	// see also:
	// https://qna.habr.com/q/1324590 (in Russian)
	/*
	 driver.execute_cdp_cmd('Page.addScriptToEvaluateOnNewDocument', {
        'source': '''
            Object.defineProperty(navigator, 'webdriver', {
                get: () => undefined
            })
        '''
    })
	 */ 
	@SuppressWarnings("deprecation")
	@Ignore
	@Test
	public void scriptToEvaluateOnNewDocumentTest() {
		// Arrange
		script = "window.was_here='true';";
		identifier = chromeDevTools
				.send(Page.addScriptToEvaluateOnNewDocument(script, // source
						Optional.empty(), // worldName
						Optional.of(false), // includeCommandLineAPI
						Optional.of(false) // runImmediately
		));
		System.err.println(String.format(
				"Method Page.addScriptToEvaluateOnNewDocument result: %s", identifier));

		// chromeDevTools.send(Debugger.getScriptSource((ScriptId) response));
		// Cannot cast from ScriptIdentifier to ScriptId
		driver.get(baseURL);
		Utils.sleep(100);
		String data = (String) Utils.executeScript("return window.was_here");
		assertThat(data, is("true"));

		chromeDevTools.send(Page.removeScriptToEvaluateOnLoad(identifier));
	}

	// @Ignore
	// https://github.com/SeleniumHQ/selenium/blob/0f197cbd4fa9acdd2ac3ddebbe0cc9b4ca26bff8/rb/spec/integration/selenium/webdriver/chrome/driver_spec.rb
	@SuppressWarnings("deprecation")
	@Test(expected = java.lang.AssertionError.class)
	public void scriptToOnLoadTest() {
		// Arrange
		String script = "window.was_here=true;";
		identifier = chromeDevTools.send(Page.addScriptToEvaluateOnLoad(script));
		System.err.println(String.format(
				"Method Page.addScriptToEvaluateOnLoad result: %s", identifier));
		driver.get(baseURL);
		driver.navigate().refresh();
		Utils.sleep(100);
		Boolean data = (Boolean) Utils.executeScript("return window.was_here");
		assertThat(data, notNullValue());

		chromeDevTools.send(Page.removeScriptToEvaluateOnLoad(identifier));
	}

	// @Ignore
	// https://github.com/SeleniumHQ/selenium/blob/0f197cbd4fa9acdd2ac3ddebbe0cc9b4ca26bff8/rb/spec/integration/selenium/webdriver/chrome/driver_spec.rb
	@Ignore
	@Test
	public void scriptOnNewDocumentTest2() {
		// Arrange
		script = "window.was_here=true;";
		identifier = chromeDevTools
				.send(Page.addScriptToEvaluateOnNewDocument(script, // source
						Optional.empty(), // worldName
						Optional.of(false), // includeCommandLineAPI
						Optional.of(false) // runImmediately
		));

		System.err.println(String.format(
				"Method Page.addScriptToEvaluateOnNewDocument result: %s", identifier));

		driver.get(baseURL);
		Utils.sleep(100);
		Boolean data = (Boolean) Utils.executeScript("return window.was_here");
		assertThat(data, is(true));
		chromeDevTools.send(Page.removeScriptToEvaluateOnNewDocument(identifier));
	}

	@Ignore
	// https://stackoverflow.com/questions/60409219/how-do-you-disable-navigator-webdriver-in-chromedriver
	// https://intoli.com/blog/not-possible-to-block-chrome-headless/chrome-headless-test.js
	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-addScriptToEvaluateOnNewDocument
	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#type-ScriptIdentifier
	@Test
	public void scriptOnNewDocumentTest() {
		// Arrange
		script = "Object.defineProperty(navigator, 'webdriver', { get: () => undefined });";
		identifier = chromeDevTools
				.send(Page.addScriptToEvaluateOnNewDocument(script, // source
						Optional.empty(), // worldName
						Optional.of(false), // includeCommandLineAPI
						Optional.of(false) // runImmediately
		));

		System.err.println(String.format(
				"Method Page.addScriptToEvaluateOnNewDocument result: %s", identifier));

		// chromeDevTools.send(Debugger.getScriptSource((ScriptId) response));
		// Cannot cast from ScriptIdentifier to ScriptId
		driver.get(baseURL);
		Utils.sleep(100);
		chromeDevTools.send(Page.removeScriptToEvaluateOnNewDocument(identifier));
	}
}
