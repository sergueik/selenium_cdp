package com.github.sergueik.selenium;
/**
 * Copyright 2023 Serguei Kouzmine
 */

import org.junit.After;
import org.junit.Before;
// import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.v120.network.model.TimeSinceEpoch;
import org.openqa.selenium.devtools.v120.storage.Storage;
import org.openqa.selenium.devtools.v120.storage.model.SharedStorageMetadata;
import org.openqa.selenium.json.JsonException;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Storage/#method-getSharedStorageEntries
 * https://chromedevtools.github.io/devtools-protocol/tot/Storage/
 * https://developer.chrome.com/en/docs/privacy-sandbox/use-shared-storage/
 * based on: https://github.com/GoogleChromeLabs/shared-storage-demo
 * see also: https://github.com/aslushnikov/getting-started-with-cdp (NOTE: js)
 * https://dev.to/grouparoo/testing-sessionstorage-and-localstorage-with-selenium-node-2336  (NOTE: js)  
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class StorageDevToolsTest extends BaseDevToolsTest {

	private final static String baseURL = "https://www.google.com";

	@Before
	public void before() throws Exception {
		driver.get(baseURL);
	}

	@After
	public void clearPage() {
		try {
			driver.get("about:blank");
		} catch (Exception e) {

		}
	}

	// Tests in error:
	// {
	// "id":5,"error":{"code":-32000,
	// "message":"Origin not found."},
	// "sessionId":"9B2C2FC875140BF76750D29883144F10"
	// }(..)
	@Test(expected = DevToolsException.class)
	public void test1() {
		try {
			String ownerOrigin = "https://www.google.com";
			SharedStorageMetadata response = chromeDevTools
					.send(Storage.getSharedStorageMetadata(ownerOrigin));
			Integer length = response.getLength();
			TimeSinceEpoch creationTime = response.getCreationTime();
			// assertThat(result, notNullValue());
			System.err.println(
					String.format("Shared Storage Metadata length: %d creation time: %s",
							length, creationTime.toString()));
		} catch (JsonException e) {
			System.err.println(
					"Exception in test 1 reading result (ignored): " + e.toString());
		}

	}

	// Tests in error:
	// {
	// "id":6,"error":{
	// "code":-32602,
	// "message":"Invalid owner origin."},
	// "sessionId":"9B2C2FC875140BF76750D29883144F10"
	// }(..)
	@Test(expected = DevToolsException.class)
	public void test2() {
		try {
			String ownerOrigin = "www.google.com";
			SharedStorageMetadata response = chromeDevTools
					.send(Storage.getSharedStorageMetadata(ownerOrigin));
			Integer length = response.getLength();
			TimeSinceEpoch creationTime = response.getCreationTime();
			// assertThat(result, notNullValue());
			System.err.println(
					String.format("Shared Storage Metadata length: %d creation time: %s",
							length, creationTime.toString()));
		} catch (JsonException e) {
			System.err.println(
					"Exception in test 1 reading result (ignored): " + e.toString());
		}

	}
}
