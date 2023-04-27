package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.v112.dom.DOM;
import org.openqa.selenium.devtools.v112.dom.DOM.EnableIncludeWhitespace;
import org.openqa.selenium.devtools.v112.dom.model.Node;
import org.openqa.selenium.devtools.v112.dom.model.NodeId;
import org.openqa.selenium.devtools.v112.security.Security;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Security/#method-setIgnoreCertificateErrors
 * https://chromedevtools.github.io/devtools-protocol/tot/Security/#event-certificateError
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

// based on:
// https://github.com/shsarkar08/PythonSeleniumCDP_APIs/blob/master/sel_py_cdp_ignore_cert_errors.py

public class IgnoreCertificateErrorsDevToolsTest extends BaseDevToolsTest {

	private static String baseURL = "https://untrusted-root.badssl.com/";
	private final String selector = "#footer";

	private static Node result;
	private static NodeId nodeId = null;
	private static WebDriverWait wait;
	private static int flexibleWait = 10;
	private static int pollingInterval = 500;

	private static WebElement element = null;

	@After
	public void afterTest() {
		chromeDevTools.send(Security.setIgnoreCertificateErrors(false));
		chromeDevTools.send(DOM.disable());
		driver.get("about:blank");
	}

	@Before
	public void beforeTest() {
		chromeDevTools.send(Security.setIgnoreCertificateErrors(true));
		driver.get(baseURL);
		EnableIncludeWhitespace enableIncludeWhitespace = EnableIncludeWhitespace.ALL;

		chromeDevTools.send(DOM.enable(Optional.of(enableIncludeWhitespace)));
	}

	@Test
	public void test1() {
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));

		wait.pollingEvery(Duration.ofMillis(pollingInterval));
		element = wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector(selector)));
		String data = element.getText();
		assertThat(data, containsString(
				"The certificate for this site is signed using an untrusted root."));
		System.err.println(data);
	}

	@Test
	public void test2() {
		try {
			result = chromeDevTools
					.send(DOM.getDocument(Optional.of(1), Optional.of(true)));

			nodeId = chromeDevTools
					.send(DOM.querySelector(result.getNodeId(), selector));
			String data = chromeDevTools.send(DOM.getOuterHTML(Optional.of(nodeId),
					Optional.empty(), Optional.empty()));
			assertThat(data, containsString(
					"The certificate for this site is signed using an untrusted root."));

			System.err.println(data);

		} catch (DevToolsException e) {
			System.err.println("Exception (rethrown) " + e.getMessage());
			throw e;
		}
	}

}
