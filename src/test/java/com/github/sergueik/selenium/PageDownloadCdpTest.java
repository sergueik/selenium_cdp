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
import java.util.Map;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;

import com.google.gson.Gson;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge inspired
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-setDownloadBehavior
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
public class PageDownloadCdpTest extends BaseCdpTest {

	// NOTE: not using "http://www.africau.edu/images/default/sample.pdf"
	// because of the already provided
	// "browser.helperApps.neverAsk.saveToDisk" argument
	// including "application/pdf"
	private final static String url = "https://scholar.harvard.edu/files/torman_personal/files/samplepptx.pptx";
	private final static String filename = url.replaceAll("^.*/", "");
	private static String downloadPath = null;

	private static String command = "Page.setDownloadBehavior";
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();
	private static Gson gson = new Gson();

	@After
	public void after() {
		// Arrange
		params = new HashMap<>();
		params.put("behavior", "default");
		result = driver.executeCdpCommand(command, params);
		assertThat(result, notNullValue());
		driver.get("about:blank");
	}

	@Test
	public void test() {
		downloadPath = createTempDownloadDir();
		// Arrange
		params = new HashMap<>();
		params.put("behavior", "allow");
		// NOTE: the "allowAndName" is not available in Page domain
		params.put("downloadPath", downloadPath);
		System.err.println(String.format("Sent command: %s with params  %s",
				command, gson.toJson(params)));
		result = driver.executeCdpCommand(command, params);

		assertThat(result, notNullValue());
		try {
			// Act
			driver.get(url);
			Utils.sleep(3000);
			assertThat(new File(
					Paths.get(downloadPath).resolve(filename).toAbsolutePath().toString())
							.exists(),
					is(true));
			System.err.println(String.format("Verified downloaded file: %s in %s",
					filename, downloadPath));
			// remove the file
			new File(
					Paths.get(downloadPath).resolve(filename).toAbsolutePath().toString())
							.delete();
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
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
