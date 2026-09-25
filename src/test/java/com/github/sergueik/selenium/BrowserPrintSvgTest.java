package com.github.sergueik.selenium;

/**
 * Copyright 2026 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.containsString;

import org.openqa.selenium.By;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import org.junit.After;
import static org.junit.Assert.assertThrows;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.Duration;
import java.io.File;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

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

		try {
			File outputFile = new File(
					Paths.get(downloadDirectory).resolve(outputFilename).toAbsolutePath().toString());
			if (outputFile.exists())
				outputFile.delete();
		} catch (NullPointerException e) {
		}
	}

	@After
	public void after() {
		driver.get("about:blank");
	}

	@Ignore
	@Test
	public void test2() throws DownloadTimeoutException {
		// Arrange
		testpageFilename = "svg_test1.html";
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
		outputFilename = "svg.png";
		noop = false;
		cssSelector = "svg#diagram";
		scriptFilename = "svg_to_png.js";
		testpageFilename = "svg_test1.html";
		/*
		 * for (String filename : Arrays.asList("svg_test.html", "svg_test2.html")) {
		 * testpageFilename = filename;
		 */
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
		// TODO: expect
		// Failed to execute 'toDataURL' on 'HTMLCanvasElement': Tainted canvases may
		// not be exported.
		// in result
		waitDownloadFileExists(filePath);
		assertThat(new File(filePath.toString()).exists(), is(true));
		computeHash(filePath);
		assertThat(PngVerifier.isValidPng(filePath), is(true));
	}

	@Test
	public void test4() throws DownloadTimeoutException {
		// Arrange
		outputFilename = "svg.png";
		noop = false;
		cssSelector = "svg#diagram";
		scriptFilename = "svg_to_png.js";
		testpageFilename = "svg_test2.html";

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
		// Assert
		// optionally may store and inspect the thrown exception 
		/* DownloadTimeoutException exception = */ assertThrows(DownloadTimeoutException.class,
				() -> waitDownloadFileExists(filePath));

		assertThat(new File(filePath.toString()).exists(), is(false));
	}

	@Test(expected = BlankPngException.class)
	public void test5() throws DownloadTimeoutException, BlankPngException {
		// Arrange
		outputFilename = "svg.png";
		noop = false;
		cssSelector = "svg#diagram";
		scriptFilename = "svg_to_png.js";
		testpageFilename = "svg_test3.html";
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
		String fileHash = computeHash(filePath);
		/*
		 try { assertThat(fileHash, is(not(BLANK_PNG_HASH))); } catch (AssertionError e) { throw new BlankPngException("This is a blank PNG"); }
		 */
		if (BLANK_PNG_HASH.equals(fileHash)) {
			throw new BlankPngException("This is a blank PNG");
		}
	}

	private final static String BLANK_PNG_HASH = "e7Jn+2V6VQ+c791XFZelruhI56Zjl1xVFFrbPlLrT0E=";

	@Ignore
	@Test
	public void test6() {
		// Arrange
		testpageFilename = "mermaid_test.html";
		outputFilename = "graph.png";
		noop = false;
		cssSelector = "svg#graph1";
		scriptFilename = "svg_to_png.js";
		Path filePath = Path.of(Paths.get(downloadDirectory).resolve(outputFilename).toAbsolutePath().toString());

		driver.get(Utils.getPageContent(testpageFilename));

		element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(cssSelector)));
		assertThat(element, notNullValue());
		assertThat(element.isDisplayed(), is(true));

		// Act
		Object result = Utils.executeAsyncScript(Utils.getScriptContent(scriptFilename),
				Utils.cssSelectorOfElement(element), outputFilename, noop);
		System.err.println("Script Console Log: " + result.toString());

		// Assert
		DownloadTimeoutException exception = assertThrows(DownloadTimeoutException.class,
				() -> waitDownloadFileExists(filePath));

		assertThat(new File(filePath.toString()).exists(), is(false));
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

	private String computeHash(Path filePath) {

		String result = null;
		File outputFile = new File(filePath.toString());

		try {
			FileInputStream fileInputStream = new FileInputStream(outputFile);
			byte byteData[] = new byte[(int) outputFile.length()];
			fileInputStream.read(byteData);

			byte[] base64EncodedByteArray = Base64.encodeBase64(byteData);

			fileInputStream.close();

			byte[] outputFileHash = new byte[20];
			try {
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				outputFileHash = md.digest(base64EncodedByteArray);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			result = new String(Base64.encodeBase64(outputFileHash));
			System.err.println(String.format("output file encoded and added from %s: hash: %s",
					filePath.toString().replaceFirst("^.*[\\/]", ""), result));
		} catch (FileNotFoundException e) {
			System.err.println("Output file not found: " + filePath.toString() + " " + e);
		} catch (IOException e) {
			System.err.println("Problem with reading output file: " + e);
		}
		return result;
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

	@SuppressWarnings("serial")
	public static class BlankPngException extends Exception {

		// Constructor that accepts a custom error message
		public BlankPngException(String message) {
			super(message);
		}
	}
}
