package com.github.sergueik.selenium;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class FailWithSessionNotCreatedTest {

	// Junit 4 canonical - fail-flat setup only
	@BeforeClass
	public static void beforeClass() throws Exception {
		ChromeDriver driver = null; // local variable for safe release
		try {
			ChromeOptions options = new ChromeOptions();
			driver = new ChromeDriver(options);
			System.err.println("ChromeDriver instance created ");
			driver.quit();
		} catch (SessionNotCreatedException e) {
			// amend exception and propagate
			System.err.println("Fatal Selenium exception: " + e.getMessage());
			throw new RuntimeException("Fatal Selenium exception: " + e.getMessage(), e);
		}
	}

	// @Ignore
	@Test
	public void test() {
		System.err.println("test is run");
	}
}
