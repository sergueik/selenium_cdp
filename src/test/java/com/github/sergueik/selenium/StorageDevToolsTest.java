package com.github.sergueik.selenium;

/**
 * Copyright 2023,2024,2026 Serguei Kouzmine
 */

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;


import org.junit.After;
import org.junit.Before;
// import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.v154.network.model.TimeSinceEpoch;
import org.openqa.selenium.devtools.v154.storage.Storage;
import org.openqa.selenium.devtools.v154.storage.Storage.GetUsageAndQuotaResponse;
import org.openqa.selenium.devtools.v154.storage.model.UsageForType;
// NOTE: removed in v154
// import org.openqa.selenium.devtools.v154.storage.model.SharedStorageMetadata;
import org.openqa.selenium.json.JsonException;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Storage/#method-getSharedStorageEntries
 * https://chromedevtools.github.io/devtools-protocol/tot/Storage/
 * https://developer.chrome.com/en/docs/privacy-sandbox/use-shared-storage/
 * based on: https://github.com/GoogleChromeLabs/shared-storage-demo see also:
 * https://github.com/aslushnikov/getting-started-with-cdp (NOTE: js)
 * https://dev.to/grouparoo/testing-sessionstorage-and-localstorage-with-selenium-node-2336
 * (NOTE: js)
 * 
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

	@Test(/*expected = DevToolsException.class*/)
	public void test1() {
		try {
			String origin = "https://www.google.com";
			GetUsageAndQuotaResponse response = chromeDevTools.send(Storage.getUsageAndQuota(origin));
			assertThat(response, notNullValue());
			Number quota = response.getQuota();
			Number usage = response.getUsage();
			List<UsageForType> usageBreakdown = response.getUsageBreakdown();
			assertThat(usageBreakdown, notNullValue());
			System.err.println(String.format("Usage: %d Quota: %d %s", usage, quota, 
					usageBreakdown.stream()
							.map(o -> String.format("%s: %d", o.getStorageType().toString(), o.getUsage()))
							.collect(Collectors.toList())));
		} catch (JsonException e) {
			System.err.println("Exception in test 1 reading result (ignored): " + e.toString());
		}

	}
}
