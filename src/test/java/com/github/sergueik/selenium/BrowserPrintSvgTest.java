package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.containsString;

import org.openqa.selenium.By;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.time.Duration;
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

public class BrowserPrintSvgTest extends BaseCdpTest {

	private static String cssSelector = null;
	private static String scriptFilename = null;
	private static String outputFilename = null;
	private static String testpageFilename = null;

	private final static String downloadDirectory = Paths.get(System.getProperty("user.home")).resolve("Downloads")
			.toAbsolutePath().toString();
	private static boolean noop = true;
	private static WebElement element;

	@Before
	public void before() {

	}

	@After
	public void after() {
		driver.get("about:blank");
		try {
			File outputFile = new File(
					Paths.get(downloadDirectory).resolve(outputFilename).toAbsolutePath().toString());
			if (outputFile.exists())
				outputFile.delete();
		} catch (NullPointerException e) {
		}
	}

	@Test
	public void test2() throws DownloadTimeoutException {
		// Arrange
		testpageFilename = "svg_test.html";
		outputFilename = "selenium_test.txt";
		noop = true;
		cssSelector = "svg#diagram";
		scriptFilename = "svg_to_png.js";

		driver.get(Utils.getPageContent(testpageFilename));
		// NOTE: not limited to local file - can test on web page
		// driver.get(String.format("http://192.168.12.122:8000/%s", testpageFilename));
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(cssSelector)));
		assertThat(element, notNullValue());
		assertThat(element.isDisplayed(), is(true));

		// Act
		Object result = Utils.executeAsyncScript(Utils.getScriptContent(scriptFilename),
				Utils.cssSelectorOfElement(element), outputFilename, noop);
		System.err.println("Script Console Log: " + result.toString());

		Path filePath = Path.of(Paths.get(downloadDirectory).resolve(outputFilename).toAbsolutePath().toString());
		waitDownloadFileExists(filePath);

		try {
			assertThat(
					new File(Paths.get(downloadDirectory).resolve(outputFilename).toAbsolutePath().toString()).exists(),
					is(true));
			String fileContent = Files.readString(filePath);
			assertThat(fileContent, containsString("HELLO_FROM_SELENIUM"));
		} catch (IOException e) {
			System.err.println("Error: " + e.toString());
		}
	}

	@Test
	public void test3() throws DownloadTimeoutException {
		// Arrange
		testpageFilename = "svg_test.html";
		outputFilename = "svg.png";
		noop = false;
		cssSelector = "svg#diagram";
		scriptFilename = "svg_to_png.js";
		driver.get(Utils.getPageContent(testpageFilename));
		// NOTE: not limited to local file - can test on web page
		// driver.get(String.format("http://192.168.12.122:8000/%s", testpageFilename));
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(cssSelector)));
		assertThat(element, notNullValue());
		assertThat(element.isDisplayed(), is(true));

		Path filePath = Path.of(Paths.get(downloadDirectory).resolve(outputFilename).toAbsolutePath().toString());

		// Act
		Object result = Utils.executeAsyncScript(Utils.getScriptContent(scriptFilename),
				Utils.cssSelectorOfElement(element), outputFilename, noop);
		System.err.println("Script Console Log: " + result.toString());
		waitDownloadFileExists(filePath);
		assertThat(new File(filePath.toString()).exists(), is(true));
		assertThat(PngVerifier.isValidPng(filePath), is(true));

	}

	@Test
	public void test4() {
		// Arrange
		testpageFilename = "mermaid_test.html";
		outputFilename = "graph.png";
		noop = true;
		cssSelector = "svg#graph1";
		scriptFilename = "svg_to_png.js";

		driver.get(Utils.getPageContent(testpageFilename));

		element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(cssSelector)));
		assertThat(element, notNullValue());
		assertThat(element.isDisplayed(), is(true));

		// Act
		Object result = Utils.executeAsyncScript(Utils.getScriptContent(scriptFilename),
				Utils.cssSelectorOfElement(element), outputFilename, noop);
		System.err.println("Script Console Log: " + result.toString());

		Path filePath = Path.of(Paths.get(downloadDirectory).resolve(outputFilename).toAbsolutePath().toString());
		DownloadTimeoutException exception = assertThrows(DownloadTimeoutException.class,
				() -> waitDownloadFileExists(filePath));

		assertThat(new File(Paths.get(downloadDirectory).resolve(outputFilename).toAbsolutePath().toString()).exists(),
				is(false));
	}

	@Ignore
	@Test
	public void test5() {
		// Arrange
		testpageFilename = "mermaid_test.html";
		outputFilename = "graph.png";
		noop = true;
		cssSelector = "svg#graph1";
		scriptFilename = "svg_to_png.js";

		driver.get(Utils.getPageContent(testpageFilename));

		element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(cssSelector)));
		assertThat(element, notNullValue());
		assertThat(element.isDisplayed(), is(true));

		// Act
		Object result = Utils.executeAsyncScript(Utils.getScriptContent(scriptFilename),
				Utils.cssSelectorOfElement(element), outputFilename, noop);
		System.err.println("Script Console Log: " + result.toString());

		Path filePath = Path.of(Paths.get(downloadDirectory).resolve(outputFilename).toAbsolutePath().toString());
		DownloadTimeoutException exception = assertThrows(DownloadTimeoutException.class,
				() -> waitDownloadFileExists(filePath));

		try {
			assertThat(
					new File(Paths.get(downloadDirectory).resolve(outputFilename).toAbsolutePath().toString()).exists(),
					is(true));
			String fileContent = Files.readString(filePath);
			assertThat(fileContent, containsString("HELLO_FROM_SELENIUM"));
		} catch (IOException e) {
			System.err.println("Error: " + e.toString());
		}
	}

	private void waitDownloadFileExists(final Path filePath) throws DownloadTimeoutException {
		long timeout = 30;
		waitDownloadFileExists(filePath, timeout);
	}

	private void waitDownloadFileExists(final Path filePath, long timeout) throws DownloadTimeoutException {

		Duration duration = Duration.ofSeconds(timeout);

		String formatted = String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(),
				duration.toSecondsPart());

		long deadline = System.currentTimeMillis() + timeout * 1000;
		long interval = (timeout * 1000) / 4;

		while (!Files.exists(filePath)) {

			if (System.currentTimeMillis() >= deadline) {
				System.err.println("Timed out waiting for file");
				throw new DownloadTimeoutException(
						String.format("Timed out waiting for file %s over %s", filePath, formatted));
			}

			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new AssertionError("Interrupted while waiting for file: " + filePath, e);
			}

			System.err.println("Continue waiting");
		}

		System.err.println("Done waiting");
	}

	private static class PngVerifier {

		// PNG magic number signature
		private static final byte[] PNG_SIGNATURE = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A,
				0x0A };

		public static boolean isValidPng(Path filePath) {

			try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath.toFile()))) {
				byte[] signature = new byte[8];
				int bytesRead = bis.read(signature);

				if (bytesRead != 8 || !Arrays.equals(PNG_SIGNATURE, signature)) {
					System.err.println("Failed signature check: Not a PNG file.");
					return false;
				}
			} catch (IOException e) {
				System.err.println("Error reading file signature: " + e.getMessage());
				return false;
			}

			try {
				// ImageIO.read returns null if no registered decoder handles it,
				// or throws an IOException if the file stream data is corrupt.
				BufferedImage image = ImageIO.read(filePath.toFile());
				if (image == null) {
					System.err.println("Failed decode check: Invalid or unsupported image data.");
					return false;
				}

				// Optional: Access a metadata property to force full data pixel processing
				image.getWidth();
				return true;

			} catch (IOException e) {
				System.err.println("Failed decode check: Image data is corrupted.");
				return false;
			}
		}

	}

	@SuppressWarnings("serial")
	public static class DownloadTimeoutException extends TimeoutException {

		// Constructor that accepts a custom error message
		public DownloadTimeoutException(String message) {
			super(message);
		}
	}
}
