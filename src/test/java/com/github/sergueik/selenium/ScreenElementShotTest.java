package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.interactions.Actions;

// https://toster.ru/q/653249?e=7897302#comment_1962398
// https://stackoverflow.com/questions/29916054/change-user-agent-for-selenium-driver

public class ScreenElementShotTest {

	private static ChromiumDriver driver;
	private static String osName = Utils.getOSName();
	// currently unused
	@SuppressWarnings("unused")
	private static Actions actions;
	private static String baseURL = "https://www.bing.com/";

	@BeforeClass
	public static void setUp() throws Exception {
		System
				.setProperty("webdriver.chrome.driver",
						Paths.get(System.getProperty("user.home"))
								.resolve("Downloads").resolve(osName.equals("windows")
										? "chromedriver.exe" : "chromedriver")
								.toAbsolutePath().toString());

		// NOTE: protected constructor method is not visible
		// driver = new ChromiumDriver((CommandExecutor) null, new
		// ImmutableCapabilities(),
		// null);
		driver = new ChromeDriver();
		actions = new Actions(driver);
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

	@SuppressWarnings("serial")
	@Test
	public void screenElementScreenShotTest() {
		By locator = By.cssSelector("div.sbox > svg.logo");
		WebElement element = driver.findElement(locator);
		assertThat(element.getAttribute("fill"), containsString("white"));

		// Act
		try {
			File file = element.getScreenshotAs(OutputType.FILE);
			FileUtils.copyFile(file, new File("logo.png"));
		} catch (IOException e) {
			System.err.println("Exception (ignored): " + e.toString());
		} catch (WebDriverException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

}

