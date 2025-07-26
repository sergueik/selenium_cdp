package com.github.sergueik.selenium;

/**
 * Copyright 2024 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v134.network.Network;
import org.openqa.selenium.devtools.v134.network.model.Request;
import org.openqa.selenium.devtools.v134.network.model.Headers;
import org.openqa.selenium.devtools.v134.network.model.RequestWillBeSent;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.interactions.Actions;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/1-3/Network#method-enable
 * https://chromedevtools.github.io/devtools-protocol/1-3/Network/#event-requestWillBeSent
 * https://chromedevtools.github.io/devtools-protocol/1-3/Network/#type-Request
 * https://chromedevtools.github.io/devtools-protocol/1-3/Network/#type-Headers
 * https://chromedevtools.github.io/devtools-protocol/1-3/Network/#method-setCacheDisabled
 * https://chromedevtools.github.io/devtools-protocol/1-3/Network/#method-clearBrowserCache
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class FileUploadNetworkDevToolsTest extends BaseDevToolsTest {

	private int cnt = 0;
	private static String url = null;
	private static String url2 = null;
	private static final String filename = "temp.png";
	private static final File dummy = new File(System.getProperty("user.dir") + "/" + filename);

	private static WebElement element = null;
	private static WebDriverWait wait;
	private static int flexibleWait = 60;
	private static int pollingInterval = 500;

	public Actions actions;
	private Map<String, Map<String, Object>> capturedRequests = new HashMap<>();
	private final Gson gson = new Gson();

	@After
	public void after() {
		capturedRequests.clear();
		chromeDevTools.clearListeners();
	}

	@Before
	public void before() throws Exception {
		chromeDevTools.send(Network.enable(Optional.of(100000000), Optional.empty(), Optional.empty()));
		chromeDevTools.send(Network.clearBrowserCache());
		chromeDevTools.send(Network.setCacheDisabled(true));
		chromeDevTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

		chromeDevTools.addListener(Network.requestWillBeSent(), (RequestWillBeSent event) -> {
			capturedRequests.put(event.getRequest().getUrl(), event.getRequest().getHeaders().toJson());
		});
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
		actions = new Actions(driver);

	}


	@Ignore
	// based on:
	// https://www.browserstack.com/docs/automate/selenium/test-file-upload
	@Test
	public void test2() {
		url = "https://www.fileconvoy.com/";
		url2 = "https://www.fileconvoy.com/index.php?Section=25681";
		driver.get(url);
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("readTermsOfUse")));
		assertThat(element, notNullValue());
		actions.moveToElement(element).build().perform();
		assertThat(element.isDisplayed(), is(true));
		Utils.highlight(element);
		// element.click();
		element.sendKeys(Keys.SPACE);

		element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("upfile_0")));
		assertThat(element, notNullValue());
		assertThat(element.isDisplayed(), is(true));
		Utils.highlight(element);

		element.sendKeys(dummy.getAbsolutePath());

		element = driver.findElement(By.name("upload_button"));
		assertThat(element, notNullValue());
		Utils.highlight(element);
		capturedRequests.clear();
		element.submit();
		Utils.sleep(1000);

		System.err.println("Captured: ");
		capturedRequests.keySet().stream().forEach(System.err::println);

		assertThat(capturedRequests.keySet(), hasItems(new String[] { url2 }));
		String headers = capturedRequests.get(url2).toString();
		System.err.println("Headers: " + headers);
		assertThat(headers, containsString("Content-Type=multipart/form-data"));
	}
	// @Ignore
	@Test
	public void test1() {
		url = "https://ps.uci.edu/~franklin/doc/file_upload.html";
		url2 = "https://www.oac.uci.edu/indiv/franklin/cgi-bin/values";
		driver.get(url);
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='userfile']")));

		assertThat(element.isDisplayed(), is(true));
		Utils.highlight(element);
		element.sendKeys(dummy.getAbsolutePath());

		element = driver.findElement(By.tagName("form"));
		assertThat(element, notNullValue());
		assertThat(element.getAttribute("action"), notNullValue());
		// NOTE:
		// assertThat(element.getAttribute("action"), is(url2));
		// Expected: is "https://www.oac.uci.edu/indiv/franklin/cgi-bin/values"
		// but: was "http://www.oac.uci.edu/indiv/franklin/cgi-bin/values"
		// url2 = element.getAttribute("action");

		element = driver.findElement(By.cssSelector("input[type='submit']"));
		assertThat(element, notNullValue());
		Utils.highlight(element);
		element.submit();
		Utils.sleep(1000);
		assertThat(capturedRequests.size(), greaterThan(1));
		System.err.println("Captured: ");
		capturedRequests.keySet().stream().forEach(System.err::println);

		Pattern pattern = Pattern.compile("data:image/png;base64");
		System.err.println("Pattern:\n" + pattern.toString());
		cnt = 0;
		for (String x : capturedRequests.keySet()) {
			Matcher matcher = pattern.matcher(x);
			if (matcher.find()) {
				cnt++;
			}
		}
		assertThat(cnt, greaterThan(1));
		assertThat(capturedRequests.keySet(), hasItems(new String[] { url2 }));
		String data = capturedRequests.get(url2).toString();
		System.err.println("Headers: " + data);
		assertThat(data, containsString("Content-Type=multipart/form-data"));
		try {
			Headers headers = gson.fromJson(data, Headers.class);
			// Assert
			assertThat(headers, notNullValue());
			//
			System.err.println("Headers (2): ");
			headers.keySet().stream().forEach(System.err::println);
		} catch (JsonSyntaxException e) {
			System.err.println("Exception: " + e.toString());
			// com.google.gson.JsonSyntaxException:
			// com.google.gson.stream.MalformedJsonException:
			// Unterminated object at line 1 column 25 path $.
			// ignore
		}
		// assertThat(capturedRequests.containsKey(url2), is(true));

	}
}
