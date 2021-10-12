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
	private static String downloadPath = Paths.get(System.getProperty("user.home")).resolve("Downloads")
			.toAbsolutePath().toString();
	private static String command = "Browser.setDownloadBehavior";
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();

	@Before
	public void beforeTest() throws Exception {
		new File(Paths.get(downloadPath).resolve(filename).toAbsolutePath().toString()).delete();
	}

	@After
	public void clearPage() {
		driver.get("about:blank");
	}

	@Test
	public void test1() {
		try {
			// Arrange
			params = new HashMap<>();
			params.put("behavior", "allow");
			// NOTE: the "allowAndName" will randomly name the downloaded file
			params.put("downloadPath", downloadPath);

			params.put("eventsEnabled", true);

			result = driver.executeCdpCommand(command, params);

			// NOTE: the return value of "Browser.setDownloadBehavior" is not described in
			// the spec
			assertThat(result, notNullValue());

			// Act
			driver.get(url);
			Utils.sleep(3000);
			assertThat(new File(Paths.get(downloadPath).resolve(filename).toAbsolutePath().toString()).exists(),
					is(true));
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}
}
