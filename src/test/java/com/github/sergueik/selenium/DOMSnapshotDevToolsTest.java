package com.github.sergueik.selenium;

/**
 * Copyright 2022,2024 Serguei Kouzmine
 */


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.v142.domsnapshot.DOMSnapshot;
import org.openqa.selenium.devtools.v142.domsnapshot.model.DocumentSnapshot;
import org.openqa.selenium.devtools.v142.domsnapshot.model.StringIndex;

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

	private final static String baseURL = "http://www.java2s.com"; // "https://www.wikipedia.org";

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

	// @Ignore("Unable to create instance of class
	// org.openqa.selenium.devtools.v142.domsnapshot.DOMSnapshot$CaptureSnapshotResponse")
	// NOTE: pom.xml
	// org.openqa.selenium.devtools.DevToolsException: Unable to create instance
	// of
	// class
	// org.openqa.selenium.devtools.v142.domsnapshot.DOMSnapshot$CaptureSnapshotResponse
	// Exception in thread "CDP Connection"
	// org.openqa.selenium.devtools.DevToolsException:
	// Expected to read a NAME but instead have: START_COLLECTION. Last 128
	// characters read:
	// 0,1341,1342,1343,1344,1345,1346,1347,1348,1349,1350,1351,1352,1353,1354,1355,1356,1357,
	// 1358,94,1359,1360,1361],
	// "attributes":[[],

	// NOTE: on Ubuntu 18.04 the version 106 is not yet available.
	// This illustrates the error this leads to:
	// You are using a no-op implementation of the CDP. The most likely reason for
	// this is that Selenium was unable to find an implementation of the CDP
	// protocol that matches your browser. Please be sure to include an
	// implementation on the classpath, possibly by adding a new (maven)
	// dependency
	// of `org.seleniumhq.selenium:selenium-devtools-vNN:4.5.0` where `NN` matches
	// the major version of the browser you're using.(..)

	@Test(expected = DevToolsException.class)
	public void test() {
		try {
			DOMSnapshot.CaptureSnapshotResponse results = chromeDevTools
					.send(DOMSnapshot.captureSnapshot(new ArrayList<String>(),
							Optional.of(false), Optional.of(false), Optional.of(false),
							Optional.of(false)));
			List<DocumentSnapshot> documentSnapshots = results.getDocuments();
			DocumentSnapshot documentSnapshot = documentSnapshots.get(0);
			StringIndex index = documentSnapshot.getTitle();
			List<String> strings = results.getStrings();
			String title = strings.get((int) Long.parseLong(index.toString()));
			System.err.println("Page Title index: " + index + " value: " + title);
		} catch (DevToolsException e) {

			// the full exception is too long
			System.err.println("Exception (rethrown) " + e.getMessage());
			throw e;
		}
	}

}
