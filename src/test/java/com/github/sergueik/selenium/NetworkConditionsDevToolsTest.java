package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Optional;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chromium.ChromiumNetworkConditions;
import org.openqa.selenium.chromium.HasNetworkConditions;
import org.openqa.selenium.devtools.v121.network.Network;
import org.openqa.selenium.devtools.v121.network.model.ConnectionType;
import org.openqa.selenium.remote.Augmenter;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see also:
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-emulateNetworkConditions
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#type-ConnectionType
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class NetworkConditionsDevToolsTest extends BaseDevToolsTest {

	private static String baseURL = "https://www.wikipedia.org";

	private Boolean offline;

	@After
	public void afterTest() {
		offline = false;
		chromeDevTools
				.send(Network.emulateNetworkConditions(offline, new Long(100L),
						new Long(-1), new Long(-1), Optional.of(ConnectionType.ETHERNET)));

		driver.get("about:blank");
	}

	@SuppressWarnings("deprecation")
	@Test(expected = WebDriverException.class)
	public void test1() throws IOException {
		offline = true;
		chromeDevTools
				.send(Network.emulateNetworkConditions(offline, new Long(100L),
						new Long(-1), new Long(-1), Optional.of(ConnectionType.ETHERNET)));
		try {
			driver.get(baseURL);
		} catch (WebDriverException e) {
			assertThat(e.getMessage(), containsString("ERR_INTERNET_DISCONNECTED"));
			System.err.println("Exception (ignored): " + e.getMessage());
			// e.printStackTrace(System.err);
			throw (e);
		} finally {
			offline = false;
			chromeDevTools
					.send(Network.emulateNetworkConditions(offline, new Long(100L),
							new Long(-1), new Long(-1), Optional.of(ConnectionType.NONE)));

		}
	}

	// based on:
	// https://github.com/Bhakti-satalkar/junit-selenium-offline-web-cdp-se4/blob/master/src/test/java/com/lambdatest/toggleNetworkOffline.java
	@Test(expected = WebDriverException.class)
	public void test2() throws IOException {
		WebDriver augmentedDriver = new Augmenter().augment(driver);
		ChromiumNetworkConditions networkConditions = new ChromiumNetworkConditions();
		networkConditions.setOffline(true);
		((HasNetworkConditions) augmentedDriver)
				.setNetworkConditions(networkConditions);
		try {
			driver.get(baseURL);
		} catch (WebDriverException e) {
			assertThat(e.getMessage(), containsString("ERR_INTERNET_DISCONNECTED"));
			System.err.println("Exception (ignored): " + e.getMessage());
			// e.printStackTrace(System.err);
			throw (e);
		} finally {
			((HasNetworkConditions) augmentedDriver)
					.setNetworkConditions(new ChromiumNetworkConditions());
		}

	}

}

