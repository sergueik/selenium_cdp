package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.containsString;
import org.openqa.selenium.By;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.concurrent.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Selected test scenarios for Selenium WebDriver
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo
 */
// NOTE: can run with set HEADLESS=true
public class BrowserPrintSvgTest extends BaseCdpTest {

	private final static String cssSelector = "svg#diagram";
	private static String filename = "selenium_test.txt"; // "diagram.png";
	private final static String downloadDirectory = Paths.get(System.getProperty("user.home")).resolve("Downloads")
			.toAbsolutePath().toString();
	private static boolean noop = true;
	private static WebElement element;

	@Before
	public void before() {
		// Arrange

		String page = "mermaid_test.html";
		driver.get(Utils.getPageContent(page));

		// driver.get("http://192.168.12.122:8000/mermaid_test.html");
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(cssSelector)));
		assertThat(element, notNullValue());
		assertThat(element.isDisplayed(), is(true));
		// TODO: computed size

		/*
		 * try { // Act assertThat(new
		 * File(Paths.get(downloadDirectory).resolve(filename).toAbsolutePath().toString
		 * ()).exists(), is(true));
		 * System.err.println(String.format("Verified downloaded file: %s in %s",
		 * filename, downloadDirectory)); // remove the file new
		 * File(Paths.get(downloadDirectory).resolve(filename).toAbsolutePath().toString
		 * ()).delete(); } catch (Exception e) { System.err.println("Exception:  " +
		 * e.toString()); throw (new RuntimeException(e)); }
		 */

	}

	@After
	public void afterMethod() {
		driver.get("about:blank");
	}

	@Ignore
	@Test
	public void test1() {

		// Act
		filename = "selenium_test.txt";
		// filename argument is ignored
		Object result = Utils.executeAsyncScript(Utils.getScriptContent("svg_to_png.js"),
				Utils.cssSelectorOfElement(element), filename, noop);
		System.err.println("SVG PNG RESULT: " + result.toString());
		// TODO: org.openqa.selenium.ScriptTimeoutException: script timeout
		// Assert
		// verify downloaded PNG
		while (new File(Paths.get(downloadDirectory).resolve(filename).toAbsolutePath().toString()).exists() != true) {

			Utils.sleep(60000);
			System.err.println("Waiting...");
		}
	}

	@Test
	public void test2() {
		// Arrange
		filename = "selenium_test.txt";
		noop = true;
		// NOTE filename argument is ignored when noop is true

		// Act
		Object result = Utils.executeAsyncScript(Utils.getScriptContent("svg_to_png.js"),
				Utils.cssSelectorOfElement(element), filename, noop);
		System.err.println("Script Console Log: " + result.toString());
		ExecutorService executor = Executors.newSingleThreadExecutor();
		boolean running = true;
		long maxIterationTimeMs = 60000; // 1 minute limit per iteration

		while (running) {
			Future<Void> future = executor.submit(() -> {
				// TODO: Put your actual loop iteration work here
				// NOTE: need synchronized ?
				Thread.sleep(1500); // hard work
				return null;
			});

			try {
				// Wait for the iteration to finish within the time limit
				future.get(maxIterationTimeMs, TimeUnit.MILLISECONDS);
				System.err.println("Iteration completed successfully.");
				if (new File(Paths.get(downloadDirectory).resolve(filename).toAbsolutePath().toString())
						.exists() == true) {
					running = false; // Stop the loop
					System.err.println("Done waiting.");
				} else
					System.err.println("Continue waiting.");
			} catch (TimeoutException e) {
				// Iteration took too long! Fail/abort it.
				future.cancel(true); // Attempt to interrupt the running task
				System.err.println("timed out!");
				running = false;
			} catch (InterruptedException | ExecutionException e) {
				System.err.println("Error: " + e.getCause());
				running = false;
			}
		}
		Path filePath = Path.of(Paths.get(downloadDirectory).resolve(filename).toAbsolutePath().toString());

		try {
			assertThat(new File(Paths.get(downloadDirectory).resolve(filename).toAbsolutePath().toString()).exists(),
					is(true));
			String fileContent = Files.readString(filePath);
			assertThat(fileContent, containsString("HELLO_FROM_SELENIUM"));
		} catch (IOException e) {
			System.err.println("Error: " + e.toString());
		}
	}

	@Test
	public void test3() {
		// Arrange
		filename = "diagram.png";
		noop = false;
		// NOTE filename argument is ignored when noop is true

		// Act
		Object result = Utils.executeAsyncScript(Utils.getScriptContent("svg_to_png.js"),
				Utils.cssSelectorOfElement(element), filename, noop);
		System.err.println("Script Console Log: " + result.toString());
		ExecutorService executor = Executors.newSingleThreadExecutor();
		boolean running = true;
		long maxIterationTimeMs = 60000; // 1 minute limit per iteration

		while (running) {
			Future<Void> future = executor.submit(() -> {
				// TODO: Put your actual loop iteration work here
				// NOTE: need synchronized ?
				Thread.sleep(1500); // hard work
				return null;
			});

			try {
				// Wait for the iteration to finish within the time limit
				future.get(maxIterationTimeMs, TimeUnit.MILLISECONDS);
				System.err.println("Iteration completed successfully.");
				if (new File(Paths.get(downloadDirectory).resolve(filename).toAbsolutePath().toString())
						.exists() == true) {
					running = false; // Stop the loop
					System.err.println("Done waiting.");
				} else
					System.err.println("Continue waiting.");
			} catch (TimeoutException e) {
				// Iteration took too long! Fail/abort it.
				future.cancel(true); // Attempt to interrupt the running task
				System.err.println("timed out!");
				running = false;
			} catch (InterruptedException | ExecutionException e) {
				System.err.println("Error: " + e.getCause());
				running = false;
			}
		}
	}
}
