package com.github.sergueik.selenium;

/**
 * Copyright 2026 Serguei Kouzmine
 */

import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;


import java.util.ArrayList;
import java.util.List;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge See
 * also:
 * https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/devtools/CdpVersionFinder.html
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class CdpWarningHandlerTest {

	private static WarningHandler warningHandler;

	private static ChromiumDriver driver;
	private static final String LOGGER_NAME = "org.openqa.selenium.devtools.CdpVersionFinder";

	private static String osName = Utils.getOSName();
	private static boolean runHeadless = false;

	private static List<String> warnings = new ArrayList<String>();

	@BeforeClass
	public static void beforeClass() throws Exception {
		warningHandler = new WarningHandler();
		// NOTE: without setting Logger Name to filter - will collect all
		warningHandler.setFilterLoggerName(LOGGER_NAME);
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

		warnings = warningHandler.getWarnings();
		if (warnings != null && warnings.size() > 0) {
			System.out.println("CDP compatibility issue: " + String.join("\n", warnings));
		} else {
			System.out.println("No CDP warning observed.");
		}

		warningHandler.close();
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

	// https://docs.oracle.com/javase/8/docs/api/java/util/logging/Handler.html
	public static class WarningHandler extends Handler {

		private String filterLoggerName = null;
		private final List<String> warnings = new ArrayList<>();
		private LogRecord warningRecord;

		public void setFilterLoggerName(String value) {
			filterLoggerName = value;
		}

		public WarningHandler() {
			setLevel(Level.ALL);

			// Attach to the JUL root logger.
			Logger.getLogger("").addHandler(this);
		}

		@Override
		public void publish(LogRecord record) {
			if (record == null || !isLoggable(record)) {
				return;
			}

			// focus on specific logger messages
			if (filterLoggerName == null || filterLoggerName.equals(record.getLoggerName())
					&& record.getLevel().intValue() >= Level.WARNING.intValue()) {

				warningRecord = record;
				var warning = getFormatter() == null
						? java.text.MessageFormat.format(record.getMessage(), record.getParameters())
						: getFormatter().formatMessage(record);
				warnings.add(warning);
			}
		}

		@Override
		public void flush() {
		}

		@Override
		public void close() {
			Logger.getLogger("").removeHandler(this);
		}

		public List<String> getWarnings() {
			return warnings;
		}

		public LogRecord getWarningRecord() {
			return warningRecord;
		}
	}
}
