package com.github.sergueik.selenium;

/**
 * Copyright 2021-2025 Serguei Kouzmine
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
// need to use branch cdp_codegen of SeleniumHQ/selenium
// https://github.com/SeleniumHQ/selenium/tree/cdp_codegen/java/client/src/org/openqa/selenium/devtools
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.devtools.DevToolsException;
/**
 * Base class for selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class BaseDevToolsTest {

	protected static boolean runHeadless = false;
	protected static String osName = Utils.getOSName();
	protected static ChromiumDriver driver;
	protected static DevTools chromeDevTools;
	protected static String baseURL = "about:blank";

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

		// see: http://barancev.github.io/slow-loading-pages/
		// https://stackoverflow.com/questions/43734797/page-load-strategy-for-chrome-driver-updated-till-selenium-v3-12-0
		DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
		desiredCapabilities.setCapability("pageLoadStrategy", "eager");
		ChromeOptions options = new ChromeOptions();
		for (String optionAgrument : (new String[] { "--allow-insecure-localhost",
				"--allow-running-insecure-content", "--browser.download.folderList=2",
				"--browser.helperApps.neverAsk.saveToDisk=image/jpg,text/csv,text/xml,application/xml,application/vnd.ms-excel,application/x-excel,application/x-msexcel,application/excel,application/pdf",
				"--disable-blink-features=AutomationControlled",
				"--disable-default-app", "--disable-dev-shm-usage",
				"--disable-extensions", "--disable-gpu", "--disable-infobars",
				"--disable-in-process-stack-traces", "--disable-logging",
				"--disable-notifications", "--disable-popup-blocking",
				"--disable-save-password-bubble", "--disable-translate",
				"--disable-web-security", "--enable-local-file-accesses",
				"--ignore-certificate-errors", "--ignore-certificate-errors",
				"--ignore-ssl-errors=true", "--log-level=3", "--no-proxy-server",
				"--no-sandbox", "--output=/dev/null", "--ssl-protocol=any",
				// "--start-fullscreen",
				// "--start-maximized" ,
				"--user-agent=Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20120101 Firefox/33.0",
				// To prevent exception in StorageDevToolsTest:
				// org.openqa.selenium.devtools.DevToolsException:
				// {"id":5,"error":{"code":-32000,"message":"Shared storage is
				// disabled"},"sessionId":"018E331E2BEBBD1860ECDAECAD6B59C1"}
				// Caused by: org.openqa.selenium.WebDriverException:
				// {"id":5,"error":{"code":-32000,"message":"Shared storage is
				// disabled"},"sessionId":"018E331E2BEBBD1860ECDAECAD6B59C1"}

				"--enable-features=PrivacySandboxAdsAPIsOverride,OverridePrivacySandboxSettingsLocalTesting,SharedStorageAPI,FencedFrames",
				// String.format("--browser.download.dir=%s", downloadFilepath)
				/*
				 * "--user-data-dir=/path/to/your/custom/profile",
				 * "--profile-directory=name_of_custom_profile_directory",
				 */
		})) {
			options.addArguments(optionAgrument);
		}

		options.merge(desiredCapabilities);

		if (runHeadless) {
			// see:
			// https://www.selenium.dev/blog/2023/headless-is-going-away/
			// for 109+ headless option changes
			// "--headless=new"

			options.addArguments("--headless=new" /* "--headless"*/, "--disable-gpu");
			// alternatively,
			// options.setHeadless(false)
		}
		// "--remote-allow-origins=*" ChromeDriver 111
		// see also: https://github.com/SeleniumHQ/selenium/issues/11750
		driver = new ChromeDriver(options);

		Utils.setDriver(driver);

		chromeDevTools = ((HasDevTools) driver).getDevTools();
		// TODO: catch org.openqa.selenium.devtools.DevToolsException and Assume
		// chromeDevTools.createSession();
		
		try {
			tryCreateSession();

		} catch (DevToolsException e) {
			Assume.assumeNoException(e);
		}


		// TODO: switch to
		// chromeDevTools.createSessionIfThereIsNotOne();
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
		chromeDevTools.close();
		if (driver != null) {
			driver.quit();
		}
	}
	// origin: 
	// https://www.baeldung.com/junit-conditional-assume
	public static void tryCreateSession() throws DevToolsException {
		try {
			// TODO: catch org.openqa.selenium.devtools.DevToolsException and Assume
			chromeDevTools.createSession();
		} catch (DevToolsException e) { 
			throw e;
		}
	}
	// origin:
	// https://stackoverflow.com/questions/3584210/preferred-java-way-to-ping-an-http-url-for-availability
	public boolean pingHost(String host, int port, int timeout) {
		try (Socket socket = new Socket()) {
			System.err.println(
					String.format("Trying to connect to host %s port %d", host, port));
			socket.connect(new InetSocketAddress(host, port), timeout);
			return true;
		} catch (IOException e) {
			System.err.println("timeout or unreachable or failed DNS lookup.");
			return false; // timeout or unreachable or failed DNS lookup.
		}
	}
}
