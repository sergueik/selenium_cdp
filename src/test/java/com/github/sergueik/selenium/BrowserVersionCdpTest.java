package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import org.openqa.selenium.WebDriverException;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge inspired
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-getVersion
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
public class BrowserVersionCdpTest extends BaseCdpTest {

	private static String command = "Browser.getVersion";
	private static Map<String, Object> result = new HashMap<>();

	@Test
	public void test1() {

		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			System.err.println(command + " result: " + new ArrayList<String>(result.keySet()));
			assertThat(result, notNullValue());
			for (String field : Arrays.asList("jsVersion", "product", "protocolVersion", "revision", "userAgent")) {
				assertThat(result, hasKey(field));
				System.err.print(field + ": " + result.get(field) + "\t");
			}
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}
}
