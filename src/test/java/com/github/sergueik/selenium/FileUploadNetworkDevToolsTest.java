package com.github.sergueik.selenium;

/**
 * Copyright 2020-2024 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v124.network.Network;
import org.openqa.selenium.devtools.v124.network.model.RequestWillBeSent;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-
 * setExtraHTTPHeaders
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-
 * getResponseBody
 * https://chromedevtools.github.io/devtools-protocol/tot/Network#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#event-
 * dataReceived
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-
 * setCacheDisabled
 * https://chromedevtools.github.io/devtools-protocol/tot/Console#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/Log#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#event-
 * responseReceived
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#type-
 * ResourceType
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#type-Response
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#type-
 * ResourceTiming
 * 
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class FileUploadNetworkDevToolsTest {

	private int cnt = 0;
	private static String baseURL = "about:blank";
	private static String url = null;
	private static String url2 = null;
	private static final String filename = "temp.png";
	private static final File dummy = new File(System.getProperty("user.dir") + "/" + filename);
	private static boolean runHeadless = false;
	private static String osName = Utils.getOSName();
	private static ChromiumDriver driver;
	private static DevTools chromeDevTools;


	private static WebElement element = null;

	private static WebDriverWait wait;
	private static int flexibleWait = 60;
	private static int pollingInterval = 500;
	private Map<String, Map<String, Object>> capturedRequests = new HashMap<>();
	private final Gson gson = new Gson();

	@BeforeClass
	public static void setUp() throws Exception {

		if (System.getenv().containsKey("HEADLESS") && System.getenv("HEADLESS").matches("(?:true|yes|1)")) {
			runHeadless = true;
		}
		// force the headless flag to be true to support Unix console execution
		if (!(Utils.getOSName().equals("windows")) && !(System.getenv().containsKey("DISPLAY"))) {
			runHeadless = true;
		}
		System.setProperty("webdriver.chrome.driver", Paths.get(System.getProperty("user.home")).resolve("Downloads")
				.resolve(osName.equals("windows") ? "chromedriver.exe" : "chromedriver").toAbsolutePath().toString());

		if (runHeadless) {
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--headless", "--disable-gpu");
			driver = new ChromeDriver(options);
		} else {
			driver = new ChromeDriver();
		}
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
		Utils.setDriver(driver);

		chromeDevTools = ((HasDevTools) driver).getDevTools();

		chromeDevTools.createSession();
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		driver.get(baseURL);
	}

	@AfterClass
	public static void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

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

		// NOTE: org.openqa.selenium.WebDriverException:
		// Setting the file detector only works on
		// remote webdriver instances obtained via RemoteWebDriver
		// driver.setFileDetector(new LocalFileDetector());

		chromeDevTools.addListener(Network.requestWillBeSent(), (RequestWillBeSent event) -> {
			capturedRequests.put(event.getRequest().getUrl(), event.getRequest().getHeaders().toJson());
		});
	}

	// based on: https://www.browserstack.com/docs/automate/selenium/test-file-upload
	@Ignore("Element <input type=\"checkbox\" id=\"readTermsOfUse\" is not clickable")
	@Test
	public void test2() {
		url = "https://www.fileconvoy.com/";

		driver.get(url);
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("upfile_0")));
		assertThat(element, notNullValue());
		assertThat(element.isDisplayed(), is(true));
		Utils.highlight(element);

		element.sendKeys(dummy.getAbsolutePath());
		element = driver.findElement(By.id("readTermsOfUse"));
		assertThat(element, notNullValue());
		Utils.highlight(element);
		element.click();
		element = driver.findElement(By.name("upload_button"));
		assertThat(element, notNullValue());
		Utils.highlight(element);
		capturedRequests.clear();
		element.submit();

	}

	@Test
	@SuppressWarnings("unchecked")
	public void test1() {
		url = "https://ps.uci.edu/~franklin/doc/file_upload.html";
		url2 = "https://www.oac.uci.edu/indiv/franklin/cgi-bin/values";
		driver.get(url);
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='userfile']")));
		assertThat(element.isDisplayed(), is(true));
		Utils.highlight(element);
		element.sendKeys(dummy.getAbsolutePath());
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
		String headers = capturedRequests.get(url2).toString();
		System.err.println("Headers: " + headers);
		assertThat(headers, containsString("Content-Type=multipart/form-data"));
		try {
			Map<String, Object> data = gson.fromJson(headers, Map.class);
			data.keySet().stream().forEach(System.err::println);
		} catch (JsonSyntaxException e) {
			// com.google.gson.JsonSyntaxException:
			// com.google.gson.stream.MalformedJsonException:
			// Unterminated object at line 1 column 25 path $.
			// ignore
		}
		// assertThat(capturedRequests.containsKey(url2), is(true));

	}
}
