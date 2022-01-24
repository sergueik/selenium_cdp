package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
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
	private static String downloadPath = getTempDownloadDir();

	private static String command = "Browser.setDownloadBehavior";
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();

	@Before
	public void beforeTest() throws Exception {
		// Arrange
		params = new HashMap<>();
		params.put("behavior", "allow");
		// NOTE: the "allowAndName" will randomly name the downloaded file
		params.put("downloadPath", downloadPath);

		params.put("eventsEnabled", true);

		result = driver.executeCdpCommand(command, params);

		// NOTE: the return value of "Browser.setDownloadBehavior" is not described
		// in
		// the spec
		assertThat(result, notNullValue());

		new File(
				Paths.get(downloadPath).resolve(filename).toAbsolutePath().toString())
						.delete();
	}

	@After
	public void clearPage() {
		// Arrange
		params = new HashMap<>();
		params.put("behavior", "default");
		result = driver.executeCdpCommand(command, params);
		assertThat(result, notNullValue());
		driver.get("about:blank");
	}

	@Test
	public void test1() {
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

	// TODO: shadow DOM test
	// https://stackoverflow.com/questions/57780426/selenium-headless-chrome-how-to-query-status-of-downloads

}
