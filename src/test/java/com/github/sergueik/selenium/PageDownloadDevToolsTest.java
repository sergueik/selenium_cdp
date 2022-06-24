package com.github.sergueik.selenium;

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
import org.openqa.selenium.devtools.v103.page.Page;
import org.openqa.selenium.devtools.v103.page.model.DownloadProgress;
import org.openqa.selenium.devtools.v103.page.model.DownloadWillBegin;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see:
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-setDownloadBehavior
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#event-downloadWillBegin
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#event-downloadProgres
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
@SuppressWarnings("deprecation")

public class PageDownloadDevToolsTest extends BaseDevToolsTest {

	private final static String url = "https://scholar.harvard.edu/files/torman_personal/files/samplepptx.pptx";
	private final static String filename = url.replaceAll("^.*/", "");
	private static String downloadPath = null;

	@Before
	public void beforeTest() throws Exception {
	}

	@After
	public void afterTest() throws Exception {
		chromeDevTools.send(Page.setDownloadBehavior(
				Page.SetDownloadBehaviorBehavior.DEFAULT, Optional.empty()));
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
		chromeDevTools.send(Page.setDownloadBehavior(
				Page.SetDownloadBehaviorBehavior.ALLOW, Optional.of(downloadPath)));
		driver.get(url);
		Utils.sleep(3000);
		assertThat(new File(
				Paths.get(downloadPath).resolve(filename).toAbsolutePath().toString())
						.exists(),
				is(true));
		System.err.println(String.format("Verified downloaded file: %s in %s",
				filename, downloadPath));

	}

	@Test(expected = java.lang.AssertionError.class)
	public void test2() {
		downloadPath = createTempDownloadDir();
		System.err.println("Downloading to " + downloadPath);
		List<DownloadProgress.State> states = new ArrayList<>();
		// Arrange
		chromeDevTools.send(Page.setDownloadBehavior(
				Page.SetDownloadBehaviorBehavior.ALLOW, Optional.of(downloadPath)));

		// Act
		try {
			chromeDevTools.addListener(Page.downloadWillBegin(),
					(DownloadWillBegin o) -> {
						System.err.println("in Page.downloadWillBegin listener. " + "\t"
								+ "guid: " + o.getGuid() + "\t" + "url: " + o.getUrl() + "\t"
								+ "filename: " + o.getSuggestedFilename());
					});
			chromeDevTools.addListener(Page.downloadProgress(),
					(DownloadProgress o) -> {
						DownloadProgress.State state = o.getState();
						System.err.println("in Page.downloadProgress listener. state: "
								+ state.toString());
						states.add(state);
					});
			driver.get(url);
			Utils.sleep(3000);
			assertThat(new File(
					Paths.get(downloadPath).resolve(filename).toAbsolutePath().toString())
							.exists(),
					is(true));
			// The events are not fired. Expect AssertionError
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

}
