package com.github.sergueik.selenium;
/**
 * Copyright 2022-2024 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v141.page.Page;
import org.openqa.selenium.devtools.v141.page.model.NavigationEntry;
import org.openqa.selenium.devtools.v141.page.model.TransitionType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.Gson;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-getNavigationHistory
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-resetNavigationHistory
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-navigateToHistoryEntry
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
public class PageNavigationHistoryDevToolsTest extends BaseDevToolsTest {

	private List<String> urls = new ArrayList<>();
	private static Gson gson = new Gson();
	private static WebDriverWait wait;
	private static int flexibleWait = 60;
	private static int pollingInterval = 500;
	private final String cssSelector = "#ca-nstab-main > a";
	private WebElement element;

	@Before
	public void before() {
		// Arrange
		urls.addAll(Arrays.asList(new String[] { "https://fr.wikipedia.org/wiki",
				"https://de.wikipedia.org/wiki", "https://es.wikipedia.org/wiki",
				"https://it.wikipedia.org/wiki", "https://ar.wikipedia.org/wiki",
				"https://en.wikipedia.org/wiki", "https://fi.wikipedia.org/wiki",
				"https://hu.wikipedia.org/wiki", "https://da.wikipedia.org/wiki",
				"https://pt.wikipedia.org/wiki" }));
		Collections.shuffle(urls);
		urls.forEach(url -> driver.get(url));

	}

	@After
	public void after() {
		chromeDevTools.send(Page.resetNavigationHistory());
	}

	@Test
	public void test1() {
		// Act
		Page.GetNavigationHistoryResponse result = chromeDevTools
				.send(Page.getNavigationHistory());
		// Assert
		assertThat(result, notNullValue());
		assertThat(result.getCurrentIndex(), greaterThan(0));
		System.err.println(String.format("currend index: %d, url: %s",
				result.getCurrentIndex(), driver.getCurrentUrl()));
		assertThat(result.getEntries() instanceof List<?>, is(true));
		List<NavigationEntry> navigationEntries = result.getEntries();
		System.err.println("History entries size: " + navigationEntries.size());
		// NOTE: index 0 will be special:
		// History entry :
		// {"id":2,"url":"data:,","userTypedURL":"data:,","title":"","transitionType":"AUTO_TOPLEVEL"}
		NavigationEntry navigationEntry = navigationEntries.get(1);

		// Assert
		assertThat(navigationEntry.getId(), notNullValue());
		assertThat(navigationEntry.getUrl(), notNullValue());
		assertThat(navigationEntry.getUserTypedURL(), notNullValue());
		assertThat(navigationEntry.getTitle(), notNullValue());
		assertThat(navigationEntry.getTransitionType() instanceof TransitionType,
				is(true));
		System.err.print("History entry : " + gson.toJson(navigationEntry));
	}

	@Test
	public void test2() {
		// Act
		Page.GetNavigationHistoryResponse result = chromeDevTools
				.send(Page.getNavigationHistory());
		List<NavigationEntry> navigationEntries = result.getEntries();

		// Act
		navigationEntries.stream().forEach((NavigationEntry o) -> {
			if (o.getUrl().indexOf("https://en.wikipedia.org/wiki", 0) == 0) {
				final int entryId = o.getId();
				System.err.println(
						String.format("Navigate to id: %d url: %s", entryId, o.getUrl()));
				try {
					chromeDevTools.send(Page.navigateToHistoryEntry(entryId));
					Utils.sleep(250);
				} catch (TimeoutException e) {
					System.err.println("Exception (ignored): " + e.toString());
				}
			}
		});

		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
		element = wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector(cssSelector)));
		assertThat(element.getText(), is("Main Page"));
		Utils.highlight(element);

	}
}
