package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.UnsupportedCommandException;

import com.google.gson.Gson;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge inspired
 *
 * https://github.com/SeleniumHQ/selenium/commit/edbebe0f63c9a4facdc467a99dcc999fd77645dc
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
public class BrowsingContextTraverseHistoryCdpTest extends BaseCdpTest {
	private static Gson gson = new Gson();

	private static String command = null;
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();
	private List<String> urls = new ArrayList<>();

	@Before
	public void beforeTest() throws UnsupportedEncodingException {

		command = "Page.resetNavigationHistory";
		driver.executeCdpCommand(command, new HashMap<String, Object>());
		urls.addAll(Arrays.asList(new String[] { "https://fr.wikipedia.org/wiki",
				"https://de.wikipedia.org/wiki", "https://es.wikipedia.org/wiki",
				"https://it.wikipedia.org/wiki", "https://ar.wikipedia.org/wiki",
				"https://en.wikipedia.org/wiki", "https://fi.wikipedia.org/wiki",
				"https://hu.wikipedia.org/wiki", "https://da.wikipedia.org/wiki",
				"https://pt.wikipedia.org/wiki" }));
		Collections.shuffle(urls);
		urls.forEach(url -> driver.get(url));
		Utils.sleep(100);
	}

	@Test(expected = UnsupportedCommandException.class)
	public void test1() {
		command = "BrowsingContext.traverseHistory";
		params.clear();
		params.put("delta", 5);
		result = driver.executeCdpCommand(command, params);
		System.err.println(
				command + " result: " + new ArrayList<String>(result.keySet()));
		assertThat(result, notNullValue());
	}

}
