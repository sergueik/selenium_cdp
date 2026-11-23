package com.github.sergueik.selenium;

/**
 * Copyright 2026 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.Gson;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class CdpWarningHandlerTest {

	private static CdpWarningHandler handler;

	private static ChromiumDriver driver;

	private static String osName = Utils.getOSName();
	private static boolean runHeadless = false;

	private static String warning = null;

	@BeforeClass
	public static void beforeClass() throws Exception {
		handler = new CdpWarningHandler(); 
		if ((System.getenv().containsKey("HEADLESS") && System.getenv("HEADLESS").matches("(?:true|yes|1)"))
				|| (!(Utils.getOSName().equals("windows")) && !(System.getenv().containsKey("DISPLAY")))) {
			runHeadless = true;
		}
		System.setProperty("webdriver.chrome.driver", Paths.get(System.getProperty("user.home")).resolve("Downloads")
				.resolve(osName.equals("windows") ? "chromedriver.exe" : "chromedriver").toAbsolutePath().toString());

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--ssl-protocol=any", "--ignore-ssl-errors=true", "--disable-extensions",
				"--ignore-certificate-errors");
		options.setExperimentalOption("useAutomationExtension", false);
		if (runHeadless) {
			options.addArguments("--headless", "--disable-gpu");
		}

		driver = new ChromeDriver(options);
		Utils.setDriver(driver);

		warning = handler.getWarning();
		if (warning != null) {
			System.out.println("CDP compatibility issue: " + warning);
		} else {
			System.out.println("No CDP warning observed.");
		}

		handler.close();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test1() {

	}

	@AfterClass
	public static void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	public static class CdpWarningHandler extends Handler {

		private static final String LOGGER_NAME = "org.openqa.selenium.devtools.CdpVersionFinder";

		private String warning;

		public CdpWarningHandler() {
			setLevel(Level.WARNING);
			Logger.getLogger(LOGGER_NAME).addHandler(this);
		}

		@Override
		public void publish(LogRecord record) {
			if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
				warning = record.getMessage();
				// WARNING: Unable to find an exact match for CDP version 158, returning the
				// closest version; found: 157; Please update to a Selenium version that
				// supports CDP version 158

			}
		}

		@Override
		public void flush() {
		}

		@Override
		public void close() {
			Logger.getLogger(LOGGER_NAME).removeHandler(this);
		}

		public String getWarning() {
			return warning;
		}
	}
}
