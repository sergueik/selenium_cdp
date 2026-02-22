package com.github.sergueik.selenium;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;

// import static org.junit.jupiter.api.Assertions.assertThrows;
// This version of ChromeDriver only supports Chrome version 143

public class FailWithSessionNotCreatedAlternativeCdpTest extends BaseCdpTest {

	private static boolean force = false;

	// Junit 4 canonical - fail-flat setup only
	private static String command = "Browser.getVersion";
	private static Map<String, Object> result = new HashMap<>();

	@BeforeClass
	// NOTE: Superclass @BeforeClass runs first, then subclass @BeforeClass.
	// beware of Static hiding —
	// if the subclass defines a static method with the same name, it hides the superclass method.
	// too bad this is just how Java works
	public static void beforeClass() throws Exception {
		ChromeDriver dummyDriver = null; // local variable for safe release
		try {
			System.err.println("Custom BeforeClass: trying to create driver");
			System
			.setProperty("webdriver.chrome.driver",
					Paths.get(System.getProperty("user.home"))
							.resolve("Downloads").resolve(osName.equals("windows")
									? "chromedriver.exe" : "chromedriver")
							.toAbsolutePath().toString());

			System.err.println("The chromedriver version: " + getChromeDriverVersion());

			ChromeOptions options = new ChromeOptions();
			dummyDriver = new ChromeDriver(options);

			result = dummyDriver.executeCdpCommand(command, new HashMap<>());
			// System.err.println(command + " result: " + new
			// ArrayList<String>(result.keySet()));
			// TODO: reveal 143
			System.err.println("The driver info: " + result.get("product"));
			Capabilities caps = dummyDriver.getCapabilities();
			String chromeDriverVersion = ((Map<String, Object>) caps.getCapability("chrome")).get("chromedriverVersion")
					.toString();

			System.out.println("ChromeDriver: " + chromeDriverVersion);
			// let it fail let it fail let it fail
			// DevTools dummyDevTools = ((HasDevTools) dummyDriver).getDevTools();
			System.err.println("The driver was created: " + result.get("product"));
			// here, the driver was created
			dummyDriver.quit();
			// fail flat if configured so because we expected mismatch
			if (force)
				fail("Expected SessionNotCreatedException due to ChromeDriver mismatch");

		} catch (SessionNotCreatedException ex) {
			// amend exception and propagate
			System.err.println("Fatal Selenium exception: " + ex.getMessage());
			throw new RuntimeException("Fatal Selenium exception: " + ex.getMessage(), ex);
		}
	}

	// @Ignore
	@Test
	public void test1() {
	}

	private static String getChromeDriverVersion() throws IOException {
		String x = System.getProperty("webdriver.chrome.driver");
		System.err.println("examining " + x);
		Path driverPath = Paths.get(x);
		return getChromeDriverVersion(driverPath);
	}

	private static String getChromeDriverVersion(Path driverPath) throws IOException {
		Process process = new ProcessBuilder(driverPath.toString(), "--version").redirectErrorStream(true).start();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line = reader.readLine();
			if (line != null && line.startsWith("ChromeDriver")) {
				return line.split(" ")[1]; // returns e.g., "143.0.7570.10"
			}
		}
		return null;
	}
}
