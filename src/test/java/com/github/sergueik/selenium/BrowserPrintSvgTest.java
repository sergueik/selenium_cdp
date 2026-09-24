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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

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

	private final static String cssSelector = "svg#diagram";
	private final static String scriptFilename = "svg_to_png.js";
	private static String filename = null;
	private final static String downloadDirectory = Paths.get(System.getProperty("user.home")).resolve("Downloads")
			.toAbsolutePath().toString();
	private static boolean noop = true;
	private static WebElement element;

	@Before
	public void before() {
		// Arrange

		String page = "mermaid_test.html";
		driver.get(Utils.getPageContent(page));
		// can test on web page - not limited to local file
		// driver.get("http://192.168.12.122:8000/mermaid_test.html");
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(cssSelector)));
		assertThat(element, notNullValue());
		assertThat(element.isDisplayed(), is(true));

	}

	@After
	public void after() {
		driver.get("about:blank");
		new File(Paths.get(downloadDirectory).resolve(filename).toAbsolutePath().toString()).delete();
	}

	@Test
	public void test2() {
		// Arrange
		filename = "selenium_test.txt";
		noop = true;
		// NOTE filename argument is ignored when noop is true

		// Act
		Object result = Utils.executeAsyncScript(Utils.getScriptContent(scriptFilename),
				Utils.cssSelectorOfElement(element), filename, noop);
		System.err.println("Script Console Log: " + result.toString());

		Path filePath = Path.of(Paths.get(downloadDirectory).resolve(filename).toAbsolutePath().toString());
		waitDownloadFileExists(filePath);

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

		// Act
		Object result = Utils.executeAsyncScript(Utils.getScriptContent(scriptFilename),
				Utils.cssSelectorOfElement(element), filename, noop);
		System.err.println("Script Console Log: " + result.toString());
		Path filePath = Path.of(Paths.get(downloadDirectory).resolve(filename).toAbsolutePath().toString());
		waitDownloadFileExists(filePath);

		assertThat(new File(Paths.get(downloadDirectory).resolve(filename).toAbsolutePath().toString()).exists(),
				is(true));
		assertThat(PngVerifier.isValidPng(filePath), is(true));

	}

	private void waitDownloadFileExists(final Path filePath) {

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
				if (new File(filePath.toString()).exists() == true) {
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

	private static class PngVerifier {

		// Standard 8-byte PNG magic number signature
		private static final byte[] PNG_SIGNATURE = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A,
				0x0A };

		/**
		 * Verifies if a file is a valid, uncorrupted PNG image.
		 */
		public static boolean isValidPng(Path filePath) {
			// Step 1: Fast check - Verify file signature (Magic Bytes)
			try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath.toFile()))) {
				byte[] signature = new byte[8];
				int bytesRead = bis.read(signature);

				if (bytesRead != 8 || !Arrays.equals(PNG_SIGNATURE, signature)) {
					System.out.println("Failed signature check: Not a PNG file.");
					return false;
				}
			} catch (IOException e) {
				System.err.println("Error reading file signature: " + e.getMessage());
				return false;
			}

			// Step 2: Deep check - Try decoding the image data to ensure it isn't corrupt
			try {
				// ImageIO.read returns null if no registered decoder handles it,
				// or throws an IOException if the file stream data is corrupt.
				BufferedImage image = ImageIO.read(filePath.toFile());
				if (image == null) {
					System.out.println("Failed decode check: Invalid or unsupported image data.");
					return false;
				}

				// Optional: Access a property to force full data pixel processing
				image.getWidth();
				return true;

			} catch (IOException e) {
				System.out.println("Failed decode check: Image data is corrupted.");
				return false;
			}
		}

	}
}
