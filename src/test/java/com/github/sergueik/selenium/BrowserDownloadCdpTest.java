package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge inspired
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-setDownloadBehavior
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
public class BrowserDownloadCdpTest extends BaseCdpTest {

	// NOTE: not using "http://www.africau.edu/images/default/sample.pdf"
	// because of the already provided
	// "browser.helperApps.neverAsk.saveToDisk" argument
	// including "application/pdf"
	private final static String url = "https://scholar.harvard.edu/files/torman_personal/files/samplepptx.pptx";
	private final static String filename = url.replaceAll("^.*/", "");
	private static String tempDownloadDirectory = null;

	private static String command = null;
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();

	@After
	public void after() {
		// Arrange
		params = new HashMap<>();
		command = "Browser.setDownloadBehavior";
		params.put("behavior", "default");
		result = driver.executeCdpCommand(command, params);
		assertThat(result, notNullValue());
		driver.get("about:blank");
	}

	@Test
	public void test1() {
		tempDownloadDirectory = getTempDownloadDirectory();
		// Arrange
		command = "Browser.setDownloadBehavior";
		params = new HashMap<>();
		params.put("behavior", "allow");
		// NOTE: the "allowAndName" will randomly name the downloaded file
		params.put("downloadPath", tempDownloadDirectory);

		params.put("eventsEnabled", true);

		result = driver.executeCdpCommand(command, params);

		// NOTE: the return value of "Browser.setDownloadBehavior" is not described
		// in
		// the spec
		assertThat(result, notNullValue());
		try {
			// Act
			driver.get(url);
			Utils.sleep(3000);
			assertThat(new File(Paths.get(tempDownloadDirectory).resolve(filename)
					.toAbsolutePath().toString()).exists(), is(true));
			System.err.println(String.format("Verified downloaded file: %s in %s",
					filename, tempDownloadDirectory));
			// remove the file
			new File(Paths.get(tempDownloadDirectory).resolve(filename)
					.toAbsolutePath().toString()).delete();
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	@Test
	public void test2() {
		final String downloadPath = createTempDownloadDirectory();
		// Arrange
		params = new HashMap<>();
		// Browser will randomly name the downloaded file
		command = "Browser.setDownloadBehavior";
		params.put("behavior", "allowAndName");
		params.put("downloadPath", downloadPath);

		params.put("eventsEnabled", true);

		result = driver.executeCdpCommand(command, params);
		assertThat(result, notNullValue());
		try {
			// Act
			driver.get(url);
			Utils.sleep(3000);
			final Path tmpdirPath = Paths.get(downloadPath);
			List<Path> files = Files.list(tmpdirPath).collect(Collectors.toList());
			assertThat(files.size(), is(1));
			Path filePath = files.get(0);
			System.err.println(String.format("Verified downloaded file: %s in %s",
					filePath.getFileName().toString(), downloadPath));
			// remove the file
			Files.delete(filePath);
			// remove the dir
			Files.delete(tmpdirPath);
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
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

	// http://www.java2s.com/Code/Java/JDK-7/Createtempfileanddirectory.htm
	public static String createTempDownloadDirectory() {

		String tempDownloadDirectory = null;

		try {
			Path tempDownloadDirectoryPath = Files.createTempDirectory(
					FileSystems.getDefault().getPath(getTempDownloadDirectory()), "");
			System.err.println("Temporary Download directory created");
			tempDownloadDirectory = tempDownloadDirectoryPath.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return tempDownloadDirectory;

	}

}
