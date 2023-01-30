package com.github.sergueik.selenium;
/**
 * Copyright 2022 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import org.openqa.selenium.TimeoutException;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openqa.selenium.devtools.v109.page.Page;
import org.openqa.selenium.devtools.v109.page.model.NavigationEntry;
import org.openqa.selenium.devtools.v109.page.model.TransitionType;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-getNavigationHistory
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-resetNavigationHistory
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-navigateToHistoryEntry
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
public class PageNavigationHistoryDevToolsTest extends BaseDevToolsTest {

	private final static List<String> urls = Arrays.asList(
			"https://www.wikipedia.org",
			"https://chromedevtools.github.io/devtools-protocol/",
			"https://www.selenium.dev");

	@Before
	public void before() {
		// Arrange
		for (String url : urls)
			driver.get(url);
	}

	@After
	public void after() {
		chromeDevTools.send(Page.resetNavigationHistory());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test() {
		// Arrange
		// Act
		// NavigationHistoryResponse result =
		Page.GetNavigationHistoryResponse result = chromeDevTools
				.send(Page.getNavigationHistory());
		assertThat(result, notNullValue());
		assertThat(result.getCurrentIndex(), greaterThan(0));
		// System.err.println(result.toString());
		long index = result.getCurrentIndex();
		System.err.println(String.format("currend index: %d, url: %s", index,
				driver.getCurrentUrl()));
		assertThat(result.getEntries() instanceof List<?>, is(true));
		List<NavigationEntry> navigationEntries = result.getEntries();
		NavigationEntry navigationEntry = navigationEntries.get(0);

		// Assert
		assertThat(navigationEntry.getId(), notNullValue());
		assertThat(navigationEntry.getUrl(), notNullValue());
		assertThat(navigationEntry.getUserTypedURL(), notNullValue());
		// NOTE: "title" not in CDP
		assertThat(navigationEntry.getTitle(), notNullValue());
		assertThat(navigationEntry.getTransitionType() instanceof TransitionType,
				is(true));

		// https://stackoverflow.com/questions/4355303/how-can-i-convert-a-long-to-int-in-java
		index = 2;
		navigationEntry = navigationEntries.get(Math.toIntExact(index));
		int entryId = navigationEntry.getId();
		System.err.println(String.format("Navigate to id: %d url: %s", entryId,
				navigationEntry.getUrl()));

		try {
			chromeDevTools.send(Page.navigateToHistoryEntry(entryId));
			Utils.sleep(250);
		} catch (TimeoutException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}

	}
}
