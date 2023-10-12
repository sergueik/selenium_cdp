package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.devtools.v118.browser.Browser;
import org.openqa.selenium.devtools.v118.browser.model.DownloadProgress;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see:
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-setDownloadBehavior
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#event-downloadProgres
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class BrowserDownloadDevToolsTest extends BaseDevToolsTest {

	// NOTE: not using "http://www.africau.edu/images/default/sample.pdf"
	// because of the already provided
	// "browser.helperApps.neverAsk.saveToDisk" argument
	// including "application/pdf"
	private final static String url = "https://scholar.harvard.edu/files/torman_personal/files/samplepptx.pptx";
	private final static String filename = url.replaceAll("^.*/", "");
	private static String downloadPath = getTempDownloadDir();

	// private static BrowserContextID browserContextID = "";

	@Before
	public void beforeTest() throws Exception {
		new File(
				Paths.get(downloadPath).resolve(filename).toAbsolutePath().toString())
						.delete();
	}

	@After
	public void afterTest() throws Exception {
		chromeDevTools.send(
				Browser.setDownloadBehavior(Browser.SetDownloadBehaviorBehavior.DEFAULT,
						Optional.empty(), Optional.empty(), Optional.of(false)));
	}

	@Test
	public void test1() {
		// Arrange

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
		List<DownloadProgress.State> states = new ArrayList<>();
		// Arrange
		chromeDevTools.send(
				Browser.setDownloadBehavior(Browser.SetDownloadBehaviorBehavior.ALLOW,
						Optional.empty(), Optional.of(downloadPath), Optional.of(true)));

		// Act
		try {
			chromeDevTools.addListener(Browser.downloadWillBegin(), o -> {
				System.err.println("in Browser.downloadWillBegin listener. url: "
						+ o.getUrl() + "\tfilename: " + o.getSuggestedFilename());
			});
			chromeDevTools.addListener(Browser.downloadProgress(), o -> {
				DownloadProgress.State state = o.getState();
				System.err.println(
						"in Browser.downloadProgress listener. state: " + state.toString());
				states.add(state);
			});
			driver.get(url);
			Utils.sleep(3000);
			assertThat(states.indexOf(DownloadProgress.State.COMPLETED),
					greaterThan(-1));

		} catch (WebDriverException e) {
			System.err.println("Web Driver exception (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception: " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	@Test
	public void test3() {
		final List<String> filenames = new ArrayList<>();
		final List<DownloadProgress.State> states = new ArrayList<>();
		// Arrange
		chromeDevTools.send(Browser.setDownloadBehavior(
				Browser.SetDownloadBehaviorBehavior.ALLOWANDNAME, Optional.empty(),
				Optional.of(downloadPath), Optional.of(true)));

		// Act
		try {
			chromeDevTools.addListener(Browser.downloadWillBegin(), o -> {
				String filename = o.getSuggestedFilename();
				// NOTE: this is not what the downloaded file be named
				System.err.println("in Browser.downloadWillBegin listener. url: "
						+ o.getUrl() + "\tfilename: " + filename);
			});
			chromeDevTools.addListener(Browser.downloadProgress(), o -> {
				DownloadProgress.State state = o.getState();
				String filename = o.getGuid();
				System.err.println(
						"in Browser.downloadProgress listener. state: " + state.toString());
				states.add(state);
				if (state == DownloadProgress.State.COMPLETED)
					filenames.add(filename);
			});
			driver.get(url);
			Utils.sleep(3000);
			assertThat(states.indexOf(DownloadProgress.State.COMPLETED),
					greaterThan(-1));
			assertThat(filenames.size(), is(1));
			String downloadedFilename = filenames.get(0);
			System.err
					.println("Inspecting downloaded filename: " + downloadedFilename);
			assertThat(new File(Paths.get(downloadPath).resolve(downloadedFilename)
					.toAbsolutePath().toString()).exists(), is(true));
			System.err.println(String.format("Verified downloaded file: %s in %s",
					downloadedFilename, downloadPath));
			new File(Paths.get(downloadPath).resolve(downloadedFilename)
					.toAbsolutePath().toString()).delete();
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception: " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// see also:
	// https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-cancelDownload
	// Browser.cancelDownload

	@Test
	public void test4() {
		List<DownloadProgress.State> states = new ArrayList<>();
		// Arrange
		chromeDevTools.send(
				Browser.setDownloadBehavior(Browser.SetDownloadBehaviorBehavior.ALLOW,
						Optional.empty(), Optional.of(downloadPath), Optional.of(true)));

		// Act
		try {
			chromeDevTools.addListener(Browser.downloadWillBegin(), o -> {
				System.err.println("in Browser.downloadWillBegin listener. url: "
						+ o.getUrl() + "\tfilename: " + o.getSuggestedFilename());
			});
			chromeDevTools.addListener(Browser.downloadProgress(), o -> {
				DownloadProgress.State state = o.getState();
				System.err.println(
						"in Browser.downloadProgress listener. state: " + state.toString());
				states.add(state);
				System.err.println("Cancel download: " + o.getGuid());

				chromeDevTools
						.send(Browser.cancelDownload(o.getGuid(), Optional.empty()));

			});
			driver.get(url);
			Utils.sleep(3000);
			assertThat(states.indexOf(DownloadProgress.State.COMPLETED), is(-1));
			assertThat(states.indexOf(DownloadProgress.State.CANCELED),
					greaterThan(-1));

		} catch (WebDriverException e) {
			System.err.println("Web Driver exception (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception: " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// TODO: shadow DOM test
	// https://stackoverflow.com/questions/57780426/selenium-headless-chrome-how-to-query-status-of-downloads

	// http://www.java2s.com/example/java-utility-method/temp-directory-get/gettempdir-466ee.html
	public static String getTempDownloadDir() {
		String tmpdir = System.getProperty("java.io.tmpdir");
		return (tmpdir != null && new File(tmpdir).exists()) ? tmpdir
				: Paths.get(System.getProperty("user.home")).resolve("Downloads")
						.toAbsolutePath().toString();
	}

}
