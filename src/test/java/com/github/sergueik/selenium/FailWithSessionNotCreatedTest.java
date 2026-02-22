package com.github.sergueik.selenium;

import org.junit.BeforeClass;
import org.junit.Test;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

// to see the intended behavior
// git show 9494f9cb45c87a596be1fe985bac7e5cccd0d00e:src/test/java/com/github/sergueik/selenium/FailWithSessionNotCreatedTest.java  > src/test/java/com/github/sergueik/selenium/FailWithSessionNotCreatedTest.java
// mvn test -Dtest=FailWithSessionNotCreatedTest
// Running com.github.sergueik.selenium.FailWithSessionNotCreatedTest
// ChromeDriver instance created
// test is run

public class FailWithSessionNotCreatedTest {


	// fail if major versions diverge this far
	private static final int MIN_VERSION_GAP = 2;

	// Junit 4 canonical - fail-flat setup only
	@BeforeClass
	public static void beforeClass() {

		// Cross-platform path logic
		System.setProperty("webdriver.chrome.driver",
				Paths.get(System.getProperty("user.home")).resolve("Downloads")
						.resolve(System.getProperty("os.name").toLowerCase().startsWith("windows") ? "chromedriver.exe"
								: "chromedriver")
						.toAbsolutePath().toString());

		ChromiumDriver driver = null;

		try {
			ChromeOptions options = new ChromeOptions();
			// options.addArguments("--headless=new");

			// Attempt driver construction
			try {
				driver = new ChromeDriver(options);
			} catch (SessionNotCreatedException e) {
				// amend exception and propagate
				System.err.println("Fatal Selenium exception: " + e.getMessage());
				throw new RuntimeException("Fatal Selenium exception: " + e.getMessage(), e);
			}

			// Query browser version via CDP
			Map<String, Object> result = driver.executeCdpCommand("Browser.getVersion", new HashMap<>());
			String browserProduct = (String) result.get("product"); // e.g., Chrome/145.0.7632.76

			// Query ChromeDriver version from capabilities
			String chromeDriverFull = ((Map<String, Object>) (driver.getCapabilities()).getCapability("chrome"))
					.get("chromedriverVersion").toString(); // e.g., 143.0.7570.10

			int browserMajor = Integer.parseInt(browserProduct.split("/")[1].split("\\.")[0]);
			int driverMajor = Integer.parseInt(chromeDriverFull.split("\\.")[0]);

			System.err.println(
					"Gatekeeper: Browser version " + browserProduct + " | ChromeDriver version " + chromeDriverFull);

			if (Math.abs(browserMajor - driverMajor) > MIN_VERSION_GAP) {
				throw new RuntimeException(
						"Chrome Browser / Chrome Driver version mismatch. " + browserMajor + " / " + driverMajor);
			}

		} catch (Exception e) {
			throw new RuntimeException("Gatekeeper fatal error: " + e.getMessage(), e);
		} finally {
			if (driver != null) {
				driver.quit();
			}
		}
	}

	// @Ignore
	@Test
	public void test() {
		System.err.println("test is run");
	}
}
