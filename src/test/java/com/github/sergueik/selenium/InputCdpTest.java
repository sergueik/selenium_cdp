package com.github.sergueik.selenium;
/**
 * Copyright 2022 Serguei Kouzmine
 */

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Input/#method-dispatchKeyEvent
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class InputCdpTest extends BaseCdpTest {

	private final static String baseURL = "https://www.google.com";
	private static String command;
	private static Map<String, Object> params = new HashMap<>();
	
	@Before
	public void beforeTest() throws Exception {
		driver.get(baseURL);
	}

	@Test
	public void test1() {

		try {
			// Act
			command = "Input.dispatchKeyEvent";
			params.put("type", "keyDown");
			params.put("modifiers", 2);
			params.put("text", "a");
			params.put("isKeypad", true);

			driver.executeCdpCommand(command, params);
			Utils.sleep(4000);
		} catch (Exception e) {
			System.err.println("Exception in " + command + " " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	@Test
	public void test3() {

		try {
			// Act
			command = "Input.dispatchKeyEvent";
			params.put("type", "keyDown");
			params.put("commands", new String[] { "selectAll" });
			driver.executeCdpCommand(command, params);
			Utils.sleep(4000);
		} catch (Exception e) {
			System.err.println("Exception in " + command + " " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	@Test
	public void test2() {

		try {
			// Act
			command = "Input.insertText";
			params.put("text", "^_^");

			driver.executeCdpCommand(command, params);
			Utils.sleep(4000);
		} catch (Exception e) {
			System.err.println("Exception in " + command + " " + e.toString());
			throw (new RuntimeException(e));
		}
	}

}
