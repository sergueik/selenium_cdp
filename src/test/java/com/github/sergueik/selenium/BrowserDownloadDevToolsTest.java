package com.github.sergueik.selenium;
/**
 * Copyright 2023-2024 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;
// NOTE: will need to switch to v113 for Java 1.8 testing (later releases of Selenium are compiled to a later jar format
// this will lead to ignorable warning:
// Unable to find an exact match for CDP version 119, so returning the closest version found: 115

import org.openqa.selenium.devtools.v123.browser.Browser;
import org.openqa.selenium.devtools.v123.browser.model.DownloadProgress;
import org.openqa.selenium.devtools.v123.browser.model.DownloadWillBegin;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see:
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-setDownloadBehavior
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#event-downloadWillBegin
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#event-downloadProgress
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class BrowserDownloadDevToolsTest extends BaseDevToolsTest {

	// NOTE: not using "http://www.africau.edu/images/default/sample.pdf"
	// because of the already provided
	// "browser.helperApps.neverAsk.saveToDisk" argument
	// including "application/pdf"

	private final static String url = "https://scholar.harvard.edu/files/torman_personal/files/samplepptx.pptx";
	private final static String filename = url.replaceAll("^.*/", "");
	private static String tempDownloadDirectory = getTempDownloadDirectory();
	// private static BrowserContextID browserContextID = "";

	private static String downloadPath = null;

	@Before
	public void beforeTest() throws Exception {
	}

	@After
	public void afterTest() throws Exception {
		// NOTE: Allowed Values: deny, allow, allowAndName, default
		chromeDevTools.send(
				Browser.setDownloadBehavior(Browser.SetDownloadBehaviorBehavior.DEFAULT,
						Optional.empty(), Optional.empty(), Optional.of(false)));
		try {
			Files.delete((Paths.get(Paths.get(downloadPath).resolve(filename)
					.toAbsolutePath().toString())));
			// Files.delete(Paths.get(downloadPath));
		} catch (IOException e) {
		}
	}

	@Test
	public void test1() {
		// Arrange
		downloadPath = createTempDownloadDir();
		System.err.println("Downloading to " + downloadPath);
		chromeDevTools.send(
				Browser.setDownloadBehavior(Browser.SetDownloadBehaviorBehavior.ALLOW,
						Optional.empty(), Optional.of(downloadPath), Optional.of(true)));
		driver.get(url);
		Utils.sleep(3000);
		assertThat(new File(
				Paths.get(downloadPath).resolve(filename).toAbsolutePath().toString())
						.exists(),
				is(true));
		System.err.println(String.format("Verified downloaded file: %s in %s",
				filename, downloadPath));

	}

	@Test
	public void test2() {
		downloadPath = createTempDownloadDir();
		System.err.println("Downloading to " + downloadPath);
		List<DownloadProgress.State> states = new ArrayList<>();
		// Arrange
		chromeDevTools.send(
				Browser.setDownloadBehavior(Browser.SetDownloadBehaviorBehavior.ALLOW,
						Optional.empty(), Optional.of(downloadPath), Optional.of(true)));

		// Act
		try {
			chromeDevTools.addListener(Browser.downloadWillBegin(),
					(DownloadWillBegin o) -> {
						System.err.println("in Browser.downloadWillBegin listener. " + "\t"
								+ "guid: " + o.getGuid() + "\t" + "url: " + o.getUrl() + "\t"
								+ "filename: " + o.getSuggestedFilename());
					});
			chromeDevTools.addListener(Browser.downloadProgress(),
					(DownloadProgress o) -> {
						DownloadProgress.State state = o.getState();
						System.err.println("in Browser.downloadProgress listener. state: "
								+ state.toString());
						states.add(state);
					});
			driver.get(url);
			Utils.sleep(3000);
			assertThat(new File(
					Paths.get(downloadPath).resolve(filename).toAbsolutePath().toString())
							.exists(),
					is(true));
			// If the event is not fired, will encounter AssertionError
			// Allowed Values: inProgress, completed, canceled
			assertThat("Looking for COMPLETED events",
					states.indexOf(DownloadProgress.State.COMPLETED), greaterThan(-1));
		} catch (AssertionError e) {
			System.err.println("The events were not fired: " + e.toString());
			throw (e);
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception: " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// http://www.java2s.com/example/java-utility-method/temp-directory-get/gettempdir-466ee.html
	public static String getTempDownloadDir() {
		String tmpdir = System.getProperty("java.io.tmpdir");
		return (tmpdir != null && new File(tmpdir).exists()) ? tmpdir
				: Paths.get(System.getProperty("user.home")).resolve("Downloads")
						.toAbsolutePath().toString();
	}

	// http://www.java2s.com/Code/Java/JDK-7/Createtempfileanddirectory.htm
	public static String createTempDownloadDir() {

		String tempDownloadDirPath = null;

		try {
			Path tempDirectory = Files.createTempDirectory(
					FileSystems.getDefault().getPath(getTempDownloadDir()), "");
			System.err.println("Temporary Download directory created");
			tempDownloadDirPath = tempDirectory.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return tempDownloadDirPath;

	}

	// TODO: shadow DOM test
	// https://stackoverflow.com/questions/57780426/selenium-headless-chrome-how-to-query-status-of-downloads
	// http://www.java2s.com/example/java-utility-method/temp-directory-get/gettempdir-466ee.html
	public static String getTempDownloadDirectory() {
		String tempDownloadDirectory = System.getProperty("java.io.tmpdir");
		return (tempDownloadDirectory != null
				&& new File(tempDownloadDirectory).exists()) ? tempDownloadDirectory
						: Paths.get(System.getProperty("user.home")).resolve("Downloads")
								.toAbsolutePath().toString();
	}

}
