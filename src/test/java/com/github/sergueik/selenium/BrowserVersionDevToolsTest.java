package com.github.sergueik.selenium;

import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.devtools.Command;
import org.openqa.selenium.devtools.ConverterFunctions;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.json.JsonInput;
import org.openqa.selenium.devtools.v112.browser.Browser;
import org.openqa.selenium.devtools.v112.browser.Browser.GetVersionResponse;

import com.google.common.collect.ImmutableMap;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see: https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-
 * getVersion
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class BrowserVersionDevToolsTest extends BaseDevToolsTest {

	private GetVersionResponse response = null;

	@Test
	public void test1() {
		// Act
		GetVersionResponse response = chromeDevTools.send(Browser.getVersion());
		assertThat(response, notNullValue());
		response.getUserAgent();
		System.err.println("Browser Version : " + response.getProduct() + "\t"
				+ "Browser User Agent : " + response.getUserAgent() + "\t"
				+ "Browser Protocol Version : " + response.getProtocolVersion() + "\t"
				+ "Browser JS Version : " + response.getJsVersion());
	}

	// based on: https://github.com/rookieInTraining/selenium-cdp-examples
	@Test
	public void test2() {
		// Act
		response = chromeDevTools
				.send(new Command<GetVersionResponse>("Browser.getVersion",
						ImmutableMap.of(), o -> o.read(GetVersionResponse.class)));

		assertThat(response, notNullValue());
		System.err.println("Browser Version : " + response.getProduct() + "\t"
				+ "Browser User Agent : " + response.getUserAgent() + "\t"
				+ "Browser Protocol Version : " + response.getProtocolVersion() + "\t"
				+ "Browser JS Version : " + response.getJsVersion());
	}

	@Test
	public void test3() {
		// Act
		try {
			response = chromeDevTools.send(new Command<GetVersionResponse>(
					"Browser.getVersion", ImmutableMap.of(), o -> {
						System.err
								.println("in callback: " + new Json().toJson((JsonInput) o));
						// difficult to parse, not attempted
						return ((GetVersionResponse) o.read(GetVersionResponse.class));
					}));

			assertThat(response, notNullValue());
			System.err.println("Browser Version : " + response.getProduct() + "\t"
					+ "Browser User Agent : " + response.getUserAgent() + "\t"
					+ "Browser Protocol Version : " + response.getProtocolVersion() + "\t"
					+ "Browser JS Version : " + response.getJsVersion());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception: " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	@Ignore
	@Test
	public void test4() {

		// Act
		response = chromeDevTools.send(new Command<GetVersionResponse>(
				"Browser.getVersion", ImmutableMap.of(), ConverterFunctions
						.map("getVersionResponse", GetVersionResponse.class)));
		assertThat(response, notNullValue());
		assertThat(response.getProduct(), notNullValue());
		assertThat(response.getRevision(), notNullValue());
		assertThat(response.getProtocolVersion(), notNullValue());
		assertThat(response.getUserAgent(), notNullValue());
		assertThat(response.getJsVersion(), notNullValue());

	}

	@Test
	public void test5() {
		// Act
		response = chromeDevTools.send(Browser.getVersion());

		assertThat(response, notNullValue());
		System.err.println("Browser Version : " + response.getProduct() + "\t"
				+ "Browser User Agent : " + response.getUserAgent() + "\t"
				+ "Browser Protocol Version : " + response.getProtocolVersion() + "\t"
				+ "Browser JS Version : " + response.getJsVersion());
	}

}
