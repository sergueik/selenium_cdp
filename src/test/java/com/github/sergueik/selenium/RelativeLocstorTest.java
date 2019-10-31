package com.github.sergueik.selenium;

import static java.lang.System.err;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.interactions.Actions;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.codec.binary.Base64;

import org.openqa.selenium.chromium.ChromiumDriver;
import com.github.sergueik.selenium.Utils;

// https://toster.ru/q/653249?e=7897302#comment_1962398
// https://stackoverflow.com/questions/29916054/change-user-agent-for-selenium-driver

public class RelativeLocstorTest {

	private static ChromiumDriver driver;
	private static String osName = Utils.getOSName();
	// currently unused
	@SuppressWarnings("unused")
	private static Actions actions;
	private static String baseURL = "about:blank";
	private static Gson gson = new Gson();

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
	public void setUserAgentOverrideTest() {
		// Arrange
		baseURL = "https://www.whoishostingthis.com/tools/user-agent/";
		driver.get(baseURL);
		By locator = By.cssSelector("div.info-box.user-agent");
		WebElement element = driver.findElement(locator);
		assertThat(element.getAttribute("innerText"), containsString("Mozilla"));
		// Act
		try {
			driver.executeCdpCommand("Network.setUserAgentOverride",
					new HashMap<String, Object>() {
						{
							put("userAgent", "python 2.7");
							put("platform", "Windows");
						}
					});
		} catch (WebDriverException e) {
			System.err.println("Exception (ignored): " + e.toString());
			// org.openqa.selenium.WebDriverException: unknown error: unhandled
			// inspector error :
			// {"code":-32601,"message":"'setUserAgentOverride'
			// wasn't found"}
		}
		driver.navigate().refresh();
		Utils.sleep(1000);

		element = driver.findElement(locator);
		assertThat(element.isDisplayed(), is(true));
		assertThat(element.getAttribute("innerText"), is("python 2.7"));
	}

}
