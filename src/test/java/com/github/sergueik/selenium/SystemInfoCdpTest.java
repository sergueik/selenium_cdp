package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.Gson;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge inspired
 * by https://toster.ru/q/653249?e=7897302#comment_1962398
 * https://chromedevtools.github.io/devtools-protocol/tot/SystemInfo/#method-getProcessInfo
 * https://chromedevtools.github.io/devtools-protocol/tot/SystemInfo/#method-getInfo
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class SystemInfoCdpTest extends BaseCdpTest {

	private static String command;
	private static Map<String, Object> result = new HashMap<>();
	private static List<Map<String, Object>> results = new ArrayList<>();

	@Test(expected = WebDriverException.class)
	public void test1() {

		try {
			// Act
			command = "SystemInfo.getInfo";
			assertThat(result, notNullValue());
			result = driver.executeCdpCommand(command, new HashMap<>());
			System.err.println(
					command + " result: " + new ArrayList<String>(result.keySet()));
			for (String field : Arrays.asList("gpu", "modelName", "modelVersion",
					"commandLine")) {
				assertThat(result, hasKey(field));
				System.err.print(field + ": " + result.get(field) + "\t");
			}
		} catch (WebDriverException e) {
			System.err
			.println("Web Driver exception in " + command + ": " + e.toString());
	throw e;
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	@Test(expected = WebDriverException.class)
	public void test2() {

		try {
			// Act
			command = "SystemInfo.getProcessInfo";
			result = driver.executeCdpCommand(command, new HashMap<>());
			System.err.println(command + " result: " + result.keySet());
			assertThat(result, notNullValue());
		} catch (WebDriverException e) {
			System.err
					.println("Web Driver exception in " + command + ": " + e.toString());
			throw e;
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}
}
