package com.github.sergueik.selenium;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge inspired
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
public class LocaleOverrideTest extends BaseCdpTest {

	private static String command = null;
	private static Map<String, Object> result = null;
	private static Map<String, Object> data = null;
	private static Map<String, Object> params = new HashMap<>();
	private static final String baseURL = "https://www.wikipedia.org";

	@SuppressWarnings("unchecked")
	@Test
	// https://phrase.com/blog/posts/detecting-a-users-locale/
	public void test3() {
		// Arrange
		command = "Emulation.setLocaleOverride";
		// Act
		params = new HashMap<>();
		params.put("locale", "ru-RU");
		driver.executeCdpCommand(command, params);
		driver.executeCdpCommand("Runtime.enable", new HashMap<>());

		driver.get(baseURL);
		command = "Runtime.evaluate";
		params = new HashMap<>();
		params.put("expression",
				"function example(){ return navigator.userLanguage || (navigator.languages && navigator.languages.length && navigator.languages[0]) || navigator.language || navigator.browserLanguage || navigator.systemLanguage || 'en';} example();");
		result = driver.executeCdpCommand(command, params);
		System.err.println("Response to " + command + ": " + result);
		command = "Runtime.evaluate";
		params = new HashMap<>();
		params.put("expression",
				"function example(){ return navigator.languages;} example();");
		result = driver.executeCdpCommand(command, params);
		System.err.println("Response to " + command + ": " + result);
		command = "Runtime.getProperties";
		params = new HashMap<>();
		data = (Map<String, Object>) result.get("result");
		params.put("objectId", data.get("objectId").toString());
		result = driver.executeCdpCommand(command, params);
		System.err.println("Response to " + command + ": " + result);
	}
}
