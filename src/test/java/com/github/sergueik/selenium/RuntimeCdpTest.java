package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.Gson;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge inspired
 * by https://toster.ru/q/653249?e=7897302#comment_1962398
 * https://chromedevtools.github.io/devtools-protocol/1-3/Runtime/#method-evaluate
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class RuntimeCdpTest {

	private static String osName = Utils.getOSName();

	private static ChromiumDriver driver;
	private static WebDriverWait wait;
	private static boolean runHeadless = false;

	private static int flexibleWait = 60;
	private static int pollingInterval = 500;

	private static final String command = "Runtime.evaluate";

	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();
	private static Map<String, Object> data = new HashMap<>();
	private static Map<String, Object> data2 = new HashMap<>();

	private static Gson gson = new Gson();
	private String expression;
	private static final boolean returnByValue = false;

	private static WebElement element = null;
	private static By locator = null;
	private final static String baseURL = "https://www.google.com";

	@BeforeClass
	public static void beforeClass() throws Exception {

		if ((System.getenv().containsKey("HEADLESS")
				&& System.getenv("HEADLESS").matches("(?:true|yes|1)"))
				|| (!(Utils.getOSName().equals("windows"))
						&& !(System.getenv().containsKey("DISPLAY")))) {
			runHeadless = true;
		}

		System
				.setProperty("webdriver.chrome.driver",
						Paths.get(System.getProperty("user.home"))
								.resolve("Downloads").resolve(osName.equals("windows")
										? "chromedriver.exe" : "chromedriver")
								.toAbsolutePath().toString());

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--ssl-protocol=any", "--ignore-ssl-errors=true",
				"--disable-extensions", "--ignore-certificate-errors");
		options.setExperimentalOption("useAutomationExtension", false);
		if (runHeadless) {
			options.addArguments("--headless", "--disable-gpu");
		}

		driver = new ChromeDriver(options);
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		Utils.setDriver(driver);
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
	}

	@Before
	public void beforeTest() throws Exception {
		driver.get(baseURL);
	}

	@AfterClass
	public static void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	@After
	public void clearPage() {
		driver.get("about:blank");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test1() {
		try {
			// Act
			params = new HashMap<>();
			expression = "var x = 42; x;";
			params.put("expression", expression);
			params.put("returnByValue", returnByValue);
			result = driver.executeCdpCommand(command, params);
			// smoke test call
			data = gson.fromJson(result.toString(), Map.class);
			System.err.println(String.format("Command \"%s\" raw response: %s",
					command, result.toString()));
			assertThat(result, notNullValue());
			assertThat(result, hasKey("result"));
			data = (Map<String, Object>) result.get("result");
			for (String field : Arrays
					.asList(new String[] { "description", "type", "value" })) {
				assertThat(data, hasKey(field));
			}
			System.err.println(
					"Command " + command + " result value: " + (Long) data.get("value"));
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + ": " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test2() {
		try {
			// Act
			params = new HashMap<>();

			// final
			String selector = "body";
			selector = "center > input.gNO89b";//
			selector = "input[value=\\\"Google Search\\\"]";
			expression = String.format("window.document.querySelector('%s')",
					selector);
			params.put("expression", expression);
			params.put("returnByValue", returnByValue);
			params.put("timout", new Double(100));
			result = driver.executeCdpCommand(command, params);
			System.err.println(String.format("Command \"%s\" raw response: %s",
					command, result.toString()));
			assertThat(result, notNullValue());
			assertThat(result, hasKey("result"));
			data = (Map<String, Object>) result.get("result");
			for (String field : Arrays.asList(
					new String[] { "className", "type", "subtype", "objectId" })) {
				assertThat(data, hasKey(field));
			}
			System.err.println(
					"Command " + command + " objectId: " + (String) data.get("objectId"));
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + ": " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// @Ignore
	@SuppressWarnings("unchecked")
	@Test
	public void test3() {
		try {
			// Act
			params = new HashMap<>();
			expression = "//body";
			// $x is not defined
			expression = String.format("$x(\"//body\")[0]", expression);
			params.put("expression", expression);
			params.put("returnByValue", returnByValue);
			result = driver.executeCdpCommand(command, params);
			// data = gson.fromJson(result.toString(), Map.class);
			// JsonException: Unterminated object at line 1 column 100 path
			// $..exception.description
			System.err.println(String.format("Command \"%s\" raw response: %s",
					command, result.toString()));
			assertThat(result, notNullValue());
			assertThat(result, hasKey("exceptionDetails"));
			data = (Map<String, Object>) result.get("exceptionDetails");
			assertThat(data, hasKey("exception"));
			data2 = (Map<String, Object>) data.get("exception");
			for (String field : Arrays
					.asList(new String[] { "className", "description" })) {
				assertThat(data2, hasKey(field));
			}
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + ": " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test4() {
		try {
			// Act
			params = new HashMap<>();
			expression = "const letters = ['a', 'b', 'c']; letters.push('d'); letters";
			params.put("expression", expression);
			params.put("returnByValue", returnByValue);
			result = driver.executeCdpCommand(command, params);
			System.err.println(String.format("Command \"%s\" raw response: %s",
					command, result.toString()));
			assertThat(result, notNullValue());
			assertThat(result, hasKey("result"));
			data = (Map<String, Object>) result.get("result");
			assertThat(result, hasKey("result"));
			for (String field : Arrays
					.asList(new String[] { "className", "description" })) {
				assertThat(data, hasKey(field));
			}
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + ": " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// @Ignore
	@SuppressWarnings("unchecked")
	@Test
	public void test5() {
		try {
			// Act
			params = new HashMap<>();
			final String selector = "center > input.gNO89b";// "input[value='Google
																											// Search']";
			expression = String.format("$('%s')", selector);
			params.put("expression", expression);
			params.put("returnByValue", returnByValue);
			result = driver.executeCdpCommand(command, params);
			System.err.println(String.format("Command \"%s\" raw response: %s",
					command, result.toString()));
			assertThat(result, notNullValue());
			assertThat(result, hasKey("result"));
			data = (Map<String, Object>) result.get("result");
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + ": " + e.toString());
			throw (new RuntimeException(e));
		}
	}

}
