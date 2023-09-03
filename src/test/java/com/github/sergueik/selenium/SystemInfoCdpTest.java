package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openqa.selenium.WebDriverException;

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
			// Ubuntu host:
			// Web Driver exception in SystemInfo.getProcessInfo:
			// org.openqa.selenium.UnsupportedCommandException: unknown command:
			// 'SystemInfo.getProcessInfo' wasn't found
			// Windows host:
			// Web Driver exception: org.openqa.selenium.devtools.DevToolsException:
			// {"id":5,"error":{"code":-32601,"message":"'SystemInfo.getInfo' wasn't
			// found"},"sessionId": "AF6B2ECF976CFDAEC1C7EB1534259EB7"}
			throw e;
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}
}
