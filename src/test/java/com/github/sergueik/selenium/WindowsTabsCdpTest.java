package com.github.sergueik.selenium;

/**
 * Copyright 2023,2024 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see:
 * https://chromedevtools.github.io/devtools-protocol/tot/Target/#method-createTarget
 * https://chromedevtools.github.io/devtools-protocol/tot/Target/#method-getTargets
 * https://chromedevtools.github.io/devtools-protocol/tot/Target/#method-attachToTarget
 * https://chromedevtools.github.io/devtools-protocol/tot/Target/#method-getTargetInfo
 * https://chromedevtools.github.io/devtools-protocol/tot/Target/#type-TargetInfo
 * https://chromedevtools.github.io/devtools-protocol/tot/Target/#type-SessionID
 * https://chromedevtools.github.io/devtools-protocol/tot/Target/#method-attachToBrowserTarget
 */

public class WindowsTabsCdpTest extends BaseCdpTest {
	private static String baseURL = "https://developers.google.com/speed/webp/gallery1";
	private Actions actions;
	private WebElement element;
	private static String command = null;
	private static Map<String, Object> params = new HashMap<>();
	private Map<String, Object> bounds = new HashMap<String, Object>();
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> data = new HashMap<>();
	private static boolean headless = true;
	private static String targetId = null;
	private static int delay = 3000;

	@SuppressWarnings("unchecked")
	@Test
	public void test1() throws UnsupportedEncodingException {
		// Arrange
		baseURL = "https://en.wikipedia.org/wiki/Main_Page";
		// Act
		params = new HashMap<>();
		params.put("url", baseURL);
		command = "Target.createTarget";
		result = driver.executeCdpCommand(command, params);
		assertThat(result, notNullValue());
		assertThat(result, hasKey("targetId"));

		targetId = (String) result.get("targetId");
		assertThat(targetId, notNullValue());
		System.err.println("TargetID: " + targetId);
		// Act
		params = new HashMap<>();
		params.put("targetId", targetId);
		command = "Target.getTargetInfo";
		// Act
		result = driver.executeCdpCommand(command, params);
		assertThat(result, notNullValue());
		assertThat(result, hasKey("targetInfo"));
		data = (Map<String, Object>) result.get("targetInfo");
		assertThat(data, notNullValue());
		// NOTE: optional fields will not be guaranteed to be present in the data
		for (String field : Arrays.asList(new String[] { "targetId", "attached",
				"type", "url", "title", "browserContextId", "canAccessOpener" })) {
			assertThat(data, hasKey(field));
		}

		System.err.println("TargetInfo: " + "\n" + "TargetId: "
				+ data.get("targetId") + "\n" + "Title: " + data.get("title") + "\n"
				+ "Type: " + data.get("type") + "\n" + "Url: " + data.get("url") + "\n"
				+ "Attached: " + data.get("attached"));

	}

	@Ignore
	// NOTE: occasionally timing out under CDP
	@Test
	public void test2() throws UnsupportedEncodingException {
		// Arrange
		baseURL = "https://en.wikipedia.org/wiki/Main_Page";
		params = new HashMap<>();
		params.put("url", baseURL);
		command = "Target.createTarget";
		result = driver.executeCdpCommand(command, params);
		assertThat(result, notNullValue());
		assertThat(result, hasKey("targetId"));

		targetId = (String) result.get("targetId");
		assertThat(targetId, notNullValue());
		params = new HashMap<>();
		params.put("targetId", targetId);
		command = "Target.attachToTarget";
		result = driver.executeCdpCommand(command, params);
		assertThat(result, notNullValue());
		assertThat(result, hasKey("sessionId"));

		String sessionId = (String) result.get("sessionId");
		assertThat(sessionId, notNullValue());
		System.err.println("SessionId: " + sessionId);
	}
}
