package com.github.sergueik.selenium;
/**
 * Copyright 2022,2024 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.devtools.v140.systeminfo.SystemInfo;
import org.openqa.selenium.devtools.v140.systeminfo.model.ProcessInfo;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge

 * https://chromedevtools.github.io/devtools-protocol/tot/Log#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/Log/#event-entryAdded
 * https://chromedevtools.github.io/devtools-protocol/1-3/Page/#method-navigate
 * https://chromedevtools.github.io/devtools-protocol/tot/SystemInfo/#method-getProcessInfo
 * https://chromedevtools.github.io/devtools-protocol/tot/SystemInfo/#method-getInfo
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
@SuppressWarnings("deprecation")
public class SystemInfoDevToolsTest extends BaseDevToolsTest {

	@Test(expected = WebDriverException.class)
	public void test1() {
		try {
			SystemInfo.GetInfoResponse response = chromeDevTools
					.send(SystemInfo.getInfo());

			assertThat(response, notNullValue());
			response.getCommandLine();
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception: " + e.toString());
			throw e;
		}
	}

	@Test(expected = WebDriverException.class)
	public void test2() {
		try {
			List<ProcessInfo> response = chromeDevTools
					.send(SystemInfo.getProcessInfo());
			assertThat(response, notNullValue());
			response.get(0);
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception: " + e.toString());
			throw e;
		}
	}

}