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
// started with
// https://toster.ru/q/653249?e=7897302#comment_1962398
// https://stackoverflow.com/questions/29916054/change-user-agent-for-selenium-driver

public class ChromiumCdpTest {

	private static ChromiumDriver driver;
	// currently unused
	@SuppressWarnings("unused")
	private static Actions actions;
	private static String baseURL = "about:blank";
	private static Gson gson = new Gson();

	@BeforeClass
	public static void setUp() throws Exception {
		System.setProperty("webdriver.chrome.driver",
				(new File("c:/java/selenium/chromedriver.exe")).getAbsolutePath());
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
			// inspector error : {"code":-32601,"message":"'setUserAgentOverride'
			// wasn't found"}
		}
		driver.navigate().refresh();
		sleep(1000);

		element = driver.findElement(locator);
		assertThat(element.isDisplayed(), is(true));
		assertThat(element.getAttribute("innerText"), is("python 2.7"));
	}

	private static Map<String, Object> result = null;

	private static Map<String, Object> params = null;

	@Test
	public void printToPDFTest() {
		baseURL = "https://www.google.com";
		driver.get(baseURL);
		String command = "Page.printToPDF";
		params = new HashMap<>();
		try {
			result = driver.executeCdpCommand(command, params);
			err.println("Result: " + result.keySet());
			// TODO: assert the response is a valid Base64-encoded pdf data.
		} catch (org.openqa.selenium.WebDriverException e) {
			err.println("Exception (ignored): " + e.toString());
			assertThat(e.toString(), containsString("PrintToPDF is not implemented"));
			// printToPDFTest(com.github.sergueik.selenium.ChromiumCdpTest): unknown
			// error: unhandled inspector error:
			// {
			// "code": -32000,
			// "message": "PrintToPDF is not implemented"
			// }
		}
	}

	@Test
	public void clearBrowserCookiesTest() {
		baseURL = "https://www.google.com";
		driver.get(baseURL);
		String command = "Network.clearBrowserCookies";
		try {
			// Act
			driver.executeCdpCommand(command, new HashMap<>());
			// Assert ?
		} catch (org.openqa.selenium.WebDriverException e) {
			err.println("Exception (ignored): " + e.toString());
		}
	}

	@SuppressWarnings("serial")
	@Test
	public void captureScreenshotTest() {
		baseURL = "https://www.google.com";
		driver.get(baseURL);
		result = null;
		String data = null;
		String command = "Page.captureScreenshot";
		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			// Assert
			assertThat(result, notNullValue());
			assertThat(result, hasKey("data"));
			data = (String) result.get("data");
			assertThat(data, notNullValue());
		} catch (org.openqa.selenium.WebDriverException e) {
			err.println("Exception (ignored): " + e.toString());
		}

		Base64 base64 = new Base64();
		byte[] image = base64.decode(data);
		try {
			BufferedImage o = ImageIO.read(new ByteArrayInputStream(image));
			assertThat(o.getWidth(), greaterThan(0));
			assertThat(o.getHeight(), greaterThan(0));
		} catch (IOException e) {
			err.println("Exception (ignored): " + e.toString());
		}
		String tmpFilename = "temp.png";
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(tmpFilename);
			fileOutputStream.write(image);
			fileOutputStream.close();
		} catch (IOException e) {
			err.println("Exception (ignored): " + e.toString());
		}
	}

	@Test
	public void getCookiesTest() {
		// Arrange
		baseURL = "https://www.google.com";
		driver.get(baseURL);
		String command = "Page.getCookies";
		// Act
		try {
			Map<String, Object> result = driver.executeCdpCommand(command,
					new HashMap<String, Object>());
			err.println("Cookies: "
					+ result.get("cookies").toString().substring(0, 100) + "...");
			// Assert
			try {
				List<Map<String, Object>> cookies = gson
						.fromJson(result.get("cookies").toString(), ArrayList.class);

			} catch (JsonSyntaxException e) {
				err.println("Exception (ignored): " + e.toString());
			}
			// Assert
			try {
				ArrayList<Map<String, Object>> cookies = (ArrayList<Map<String, Object>>) result
						.get("cookies");
				cookies.stream().limit(3).map(o -> o.keySet())
						.forEach(System.err::println);
				Set<String> cookieKeys = new HashSet<>();
				cookieKeys.add("domain");
				cookieKeys.add("expires");
				cookieKeys.add("httpOnly");
				cookieKeys.add("name");
				cookieKeys.add("path");
				cookieKeys.add("secure");
				cookieKeys.add("session");
				cookieKeys.add("size");
				cookieKeys.add("value");

				assertTrue(cookies.get(0).keySet().containsAll(cookieKeys));

			} catch (Exception e) {
				err.println("Exception (ignored): " + e.toString());
			}

		} catch (WebDriverException e) {
			err.println("Exception (ignored): " + e.toString());
		}
	}

	public void sleep(Integer milliSeconds) {
		try {
			Thread.sleep((long) milliSeconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}