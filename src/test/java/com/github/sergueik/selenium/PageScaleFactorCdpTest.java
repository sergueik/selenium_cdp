package com.github.sergueik.selenium;

import static java.lang.System.err;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-resetPageScaleFactor
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setPageScaleFactor
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class PageScaleFactorCdpTest extends BaseCdpTest {

	private static String command = "Browser.setDownloadBehavior";
	private static Map<String, Object> params = new HashMap<>();

	@Test
	// based on:
	// see also:
	// https://github.com/sahajamit/chrome-devtools-webdriver-integration/blob/master/src/test/java/com/sahajamit/DemoTests.java
	// https://github.com/sahajamit/chrome-devtools-webdriver-integration/blob/master/src/main/java/com/sahajamit/messaging/MessageBuilder.java
	public void test() {

		// Arrange
		baseURL = "https://www.wikipedia.org";
		driver.get(baseURL);

		command = "Emulation.resetPageScaleFactor";

		try {
			// Act
			driver.executeCdpCommand(command, new HashMap<>());
			// returns empty JSON
			Utils.sleep(1000);

			command = "Emulation.setPageScaleFactor";
			// Act
			params.clear();
			for (float scale : Arrays
					.asList(new Float[] { (float) 1.25, (float) 1.5, (float) 2 })) {

				params.put("pageScaleFactor", scale);
				driver.executeCdpCommand(command, params);
				Utils.sleep(1000);
			}

			// Act
			params.clear();
			params.put("pageScaleFactor", 1);
			driver.executeCdpCommand(command, params);
			Utils.sleep(1000);
		} catch (WebDriverException e) {
			err.println("Exception in command " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			err.println("Exception: in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	@After
	public void after() {
		command = "Emulation.resetPageScaleFactor";
		try {
			// Act
			driver.executeCdpCommand(command, new HashMap<>());
			// returns empty JSON
		} catch (Exception e) {
			err.println("Exception: in " + command + " (ignored): " + e.toString());
		}
	}

}
