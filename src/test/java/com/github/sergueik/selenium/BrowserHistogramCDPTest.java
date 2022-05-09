package com.github.sergueik.selenium;

/**
 * Copyright 2022 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.CoreMatchers.is;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openqa.selenium.WebDriverException;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-getHistograms
 * https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-getHistogram
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class BrowserHistogramCDPTest extends BaseCdpTest {

	private final static String url = "https://en.wikipedia.org/wiki/Main_Page";

	private static String command = null;
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();

	@Test
	public void test1() {
		// Arrange
		driver.get(url);
		// Act
		command = "Browser.getHistograms";
		params = new HashMap<>();
		params.put("query", "");
		params.put("delta", false);
		result = driver.executeCdpCommand(command, params);
		// Assert
		assertThat(result, notNullValue());
		assertThat(result instanceof Map<?, ?>, is(true));
		assertThat(result.containsKey("histograms"), is(true));
		// histograms
		assertThat(result.get("histograms") instanceof List<?>, is(true));
		assertThat(((List<Object>) result.get("histograms")).size(),
				greaterThan(1));
		Object result2 = ((List<Object>) result.get("histograms"));
		assertThat(result2 instanceof List<?>, is(true));
		// System.err.println(result2);
		// ...
		// {buckets=[{count=9, high=2, low=1}], count=9,
		// name=API.StorageAccess.AllowedRequests2, sum=9}
		Object result3 = ((List<Object>) result2).get(0);
		assertThat(result3 instanceof Map<?, ?>, is(true));
		assertThat(((Map<?, ?>) result3).containsKey("buckets"), is(true));
		Object result4 = ((Map<?, ?>) result3).get("buckets");
		// System.err.println(result4);
		assertThat(result4 instanceof List<?>, is(true));
		Object result5 = ((List<Object>) result4).get(0);
		assertThat(result5 instanceof Map<?, ?>, is(true));
		assertThat(((Map<?, ?>) result3).containsKey("name"), is(true));

		String name = (String) ((Map<?, ?>) result3).get("name");

		// Act
		command = "Browser.getHistogram";
		params = new HashMap<>();
		params.put("name", name);
		params.put("delta", false);
		result = driver.executeCdpCommand(command, params);
		// Assert
		assertThat(result, notNullValue());
		assertThat(result instanceof Map<?, ?>, is(true));
		assertThat(((Map<?, ?>) result).containsKey("histogram"), is(true));
		// System.err.println(result);
		// {
		// histogram={
		// buckets=[
		// {
		// count=12,
		// high=2,
		// low=1
		// }],
		// count=12,
		// name=API.StorageAccess.AllowedRequests2,
		// sum=12
		// }
		// }

		result2 = result.get("histogram");
		assertThat(result2 instanceof Map<?, ?>, is(true));
		assertThat(((Map<?, ?>) result2).containsKey("buckets"), is(true));
		result3 = ((Map<?, ?>) result2).get("buckets");
		// System.err.println(result3);
		assertThat(result3 instanceof List<?>, is(true));
		result4 = ((List<Object>) result3).get(0);
		assertThat(result4 instanceof Map<?, ?>, is(true));
		assertThat(((Map<?, ?>) result2).containsKey("name"), is(true));

	}

}
