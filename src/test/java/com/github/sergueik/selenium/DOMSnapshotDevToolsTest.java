package com.github.sergueik.selenium;

/**
 * Copyright 2022 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;

import com.google.gson.JsonSyntaxException;

import org.openqa.selenium.devtools.v105.domsnapshot.DOMSnapshot;
import org.openqa.selenium.devtools.v105.domsnapshot.model.DocumentSnapshot;
import org.openqa.selenium.devtools.v105.domsnapshot.model.StringIndex;
import org.openqa.selenium.devtools.v105.browser.model.Histogram;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/DOMSnapshot/#method-disable
 * https://chromedevtools.github.io/devtools-protocol/tot/DOMSnapshot/#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/DOMSnapshot/#method-captureSnapshot
 * https://chromedevtools.github.io/devtools-protocol/tot/DOMSnapshot/#type-DocumentSnapshot
 * https://chromedevtools.github.io/devtools-protocol/tot/DOMSnapshot/#type-NodeTreeSnapshot
 * https://chromedevtools.github.io/devtools-protocol/tot/DOMSnapshot/#type-LayoutTreeSnapshot
 * see also:
 * https://stackoverflow.com/questions/58099695/is-there-a-way-in-hamcrest-to-test-for-a-value-to-be-a-number
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
// https://www.logicalincrements.com/articles/resolution

public class DOMSnapshotDevToolsTest extends BaseDevToolsTest {

	private final static String baseURL = "https://www.wikipedia.org";

	@After
	public void afterTest() {
		chromeDevTools.send(DOMSnapshot.disable());
		driver.get("about:blank");
	}

	@Before
	public void beforeTest() {
		chromeDevTools.send(DOMSnapshot.enable());
		driver.get(baseURL);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Browser#method-getWindowBounds
	// @Ignore
	@SuppressWarnings("unchecked")
	@Test
	public void test() {
		DOMSnapshot.CaptureSnapshotResponse results = chromeDevTools
				.send(DOMSnapshot.captureSnapshot(new ArrayList<String>(), Optional.of(false), Optional.of(false),
						Optional.of(false), Optional.of(false)));
		List<DocumentSnapshot> documentSnapshots = results.getDocuments();
		DocumentSnapshot documentSnapshot = documentSnapshots.get(0);
		StringIndex index = documentSnapshot.getTitle();
		List<String> strings = results.getStrings();
		String title = strings.get((int) Long.parseLong(index.toString()));
		System.err.println(
				"Page Title index: " + index + " value: " + strings.get((int) Long.parseLong(index.toString())));

	}

}
