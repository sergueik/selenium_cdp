package com.github.sergueik.selenium;

import static java.lang.System.err;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.support.locators.RelativeLocator.withTagName;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
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

// inspired by
// https://toster.ru/q/653249?e=7897302#comment_1962398
// https://stackoverflow.com/questions/29916054/change-user-agent-for-selenium-driver

public class ChromiumCdpTest {

	private static ChromiumDriver driver;
	private static String osName = Utils.getOSName();
	// currently unused
	@SuppressWarnings("unused")
	private static Actions actions;
	private static String baseURL = "about:blank";
	private static Gson gson = new Gson();

	private static String command = null;
	private static String data = null;
	private static Map<String, Object> result = null;
	private static Map<String, Object> params = null;
	public static Long nodeId = (long) -1;

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

	// see also: https://habr.com/ru/post/459112/
	/*
	 * import sys from selenium import webdriver from
	 * selenium.webdriver.chrome.options import Options import json, base64
	 * 
	 * def get_pdf_from_html(path, chromedriver='./chromedriver', print_options =
	 * {}): # запускаем Chrome webdriver_options = Options()
	 * webdriver_options.add_argument('--headless')
	 * webdriver_options.add_argument('--disable-gpu') driver =
	 * webdriver.Chrome(chromedriver, options=webdriver_options)
	 * 
	 * # открываем заданный url driver.get(path)
	 * 
	 * # задаем параметры печати calculated_print_options = { 'landscape': False,
	 * 'displayHeaderFooter': False, 'printBackground': True, 'preferCSSPageSize':
	 * True, } calculated_print_options.update(print_options)
	 * 
	 * # запускаем печать в pdf файл result = send_devtools(driver,
	 * "Page.printToPDF", calculated_print_options) driver.quit() # ответ приходит в
	 * base64 - декодируем return base64.b64decode(result['data'])
	 * 
	 * def send_devtools(driver, cmd, params={}): resource =
	 * "/session/%s/chromium/send_command_and_get_result" % driver.session_id url =
	 * driver.command_executor._url + resource body = json.dumps({'cmd': cmd,
	 * 'params': params}) response = driver.command_executor._request('POST', url,
	 * body) if response['status']: raise Exception(response.get('value')) return
	 * response.get('value')
	 * 
	 * if __name__ == "__main__": if len(sys.argv) != 3: print (
	 * "usage: converter.py <html_page_sourse> <filename_to_save>") exit()
	 * 
	 * result = get_pdf_from_html(sys.argv[1]) with open(sys.argv[2], 'wb') as file:
	 * file.write(result)
	 * 
	 */
	// NOTE: python uses different route than java
	// /session/$sessionId/chromium/send_command_and_get_result
	// vs.
	// /session/$sessionId/goog/cdp/execute
	@Test
	public void printToPDFTest() {
		baseURL = "https://www.google.com";
		driver.get(baseURL);
		String command = "Page.printToPDF";
		params = new HashMap<>();
		params.put("landscape", false);
		params.put("displayHeaderFooter", false);
		params.put("printBackground", true);
		params.put("preferCSSPageSize", true);
		try {
			result = driver.executeCdpCommand(command, params);
			err.println("Result: " + result.keySet());
			// TODO: assert the response is a valid Base64-encoded pdf data.
		} catch (org.openqa.selenium.WebDriverException e) {
			err.println("Exception (ignored): " + e.toString());
			assertThat(e.toString(), containsString("PrintToPDF is not implemented"));
			// printToPDFTest(com.github.sergueik.selenium.ChromiumCdpTest):
			// unknown
			// error: unhandled inspector error:
			// {
			// "code": -32000,
			// "message": "PrintToPDF is not implemented"
			// }
		}
	}

	@Test
	// https://chromedevtools.github.io/devtools-protocol/tot/Network#method-clearBrowserCache
	public void clearBrowserCacheTest() {
		baseURL = "https://www.google.com";
		driver.get(baseURL);
		String command = "Network.clearBrowserCache";
		try {
			// Act
			driver.executeCdpCommand(command, new HashMap<>());
			// Assert ?
		} catch (org.openqa.selenium.WebDriverException e) {
			err.println("Exception (ignored): " + e.toString());
		}
	}

	@Test
	// https://chromedevtools.github.io/devtools-protocol/tot/Network#method-getResponseBody
	public void getResponseBodyTest() {
		baseURL = "http://www.example.com/";
		driver.get(baseURL);
		command = "Network.getResponseBody";
		data = null;
		try {
			// Act
			params = new HashMap<String, Object>();
			params.put("requestId", "");
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, notNullValue());
			System.err.println(result);
		} catch (org.openqa.selenium.InvalidArgumentException e) {
			err.println("Exception (ignored): " + e.toString());
		} catch (com.google.gson.JsonSyntaxException e) {
			err.println("Exception (ignored): " + e.toString());
		} catch (org.openqa.selenium.WebDriverException e) {
			err.println("Exception (ignored): " + e.toString());
		} catch (Exception e) {
			err.println("Exception: " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	// https://chromedevtools.github.io/devtools-protocol/tot/Network#method-getAllCookies
	public void getAllCookiesTest() {
		baseURL = "https://www.google.com";
		driver.get(baseURL);
		command = "Network.getAllCookies";
		List<String> cookies = new ArrayList<>();

		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			// Assert
			assertThat(result, notNullValue());
			assertThat(result, hasKey("cookies"));
			cookies = (List<String>) result.get("cookies");
			System.err.println(cookies);
			// Assert
			assertThat(cookies, notNullValue());
			assertThat(cookies.size(), greaterThan(0));
			/*
			 * cookies.stream().limit(3).forEach(o -> { try { System.err.println(o); } catch
			 * (java.lang.ClassCastException e) { err.println("Exception (ignored): " +
			 * e.toString()); } }) ;
			 */
			/*
			 * for (String cookie : cookies) { System.err.println("Cookie:" + cookie); }
			 */
		} catch (com.google.gson.JsonSyntaxException e) {
			err.println("Exception (ignored): " + e.toString());
		} catch (org.openqa.selenium.WebDriverException e) {
			err.println("Exception (ignored): " + e.toString());
		}
	}

	@Ignore
	@Test
	public void clearBrowserCookiesTest() {
		baseURL = "https://www.google.com";
		driver.get(baseURL);
		command = "Network.clearBrowserCookies";
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
	// https://chromedevtools.github.io/devtools-protocol/tot/Page#method-captureScreenshot
	public void captureScreenshotTest() {
		baseURL = "https://www.google.com";
		driver.get(baseURL);
		result = null;
		data = null;
		command = "Page.captureScreenshot";
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

	@SuppressWarnings("unchecked")
	@Test
	public void getCookiesWithUrlsTest() {
		// Arrange
		baseURL = "https://www.google.com";
		driver.get(baseURL);
		command = "Page.getCookies";
		params = new HashMap<String, Object>();
		params.put("urls", new String[] { ".google.com" });
		// Act
		try {
			result = driver.executeCdpCommand(command, params);
			err.println("Cookies for www.google.com: "
					+ ((List<Object>) result.get("cookies")).size() + "...");
			// Assert
		} catch (com.google.gson.JsonSyntaxException e) {
			err.println("Exception (ignored): " + e.toString());

		} catch (Exception e) {
			err.println("Exception (ignored): " + e.toString());
		}
	}

	@Test
	public void getCookiesTest() {
		// Arrange
		baseURL = "https://www.google.com";
		driver.get(baseURL);
		command = "Page.getCookies";
		// Act
		try {
			result = driver.executeCdpCommand(command, new HashMap<String, Object>());
			err.println("Cookies: "
					+ result.get("cookies").toString().substring(0, 100) + "...");
			// Assert
			try {
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> cookies = gson
						.fromJson(result.get("cookies").toString(), ArrayList.class);
			} catch (JsonSyntaxException e) {
				err.println("Exception (ignored): " + e.toString());
			}
			// Assert
			try {
				@SuppressWarnings("unchecked")
				ArrayList<Map<String, Object>> cookies = (ArrayList<Map<String, Object>>) result
						.get("cookies");
				cookies.stream().limit(3).map(o -> o.keySet())
						.forEach(System.err::println);
				Set<String> cookieKeys = new HashSet<>();
				for (String key : new String[] { "domain", "expires", "httpOnly",
						"name", "path", "secure", "session", "size", "value" }) {
					cookieKeys.add(key);
				}
				/*
				 * cookieKeys.add("domain"); cookieKeys.add("expires");
				 * cookieKeys.add("httpOnly"); cookieKeys.add("name"); cookieKeys.add("path");
				 * cookieKeys.add("secure"); cookieKeys.add("session"); cookieKeys.add("size");
				 * cookieKeys.add("value");
				 */
				assertTrue(cookies.get(0).keySet().containsAll(cookieKeys));
			} catch (com.google.gson.JsonSyntaxException e) {
				err.println("Exception (ignored): " + e.toString());

			} catch (Exception e) {
				err.println("Exception (ignored): " + e.toString());
			}

		} catch (WebDriverException e) {
			err.println("Exception (ignored): " + e.toString());
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM#method-performSearch
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM#method-getSearchResults
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM#type-NodeId
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM#method-getOuterHTML
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM#method-highlightNode
	public void performSearchTest() {

		baseURL = "https://datatables.net/examples/api/highlight.html";
		driver.get(baseURL);
		data = null;
		command = "DOM.performSearch";
		params = new HashMap<String, Object>();

		WebElement element = driver
				.findElement(By.xpath("//table[@id='example']/tbody/tr[1]/td[1]"));
		err.println("outerHTML: " + element.getAttribute("outerHTML"));

		params.put("query", "//table[@id='example']/tbody/tr[1]/td[1]");
		// Act
		try {
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, notNullValue());
			assertThat(result, hasKey("searchId"));
			data = (String) result.get("searchId");
			err.println("searchId: " + data);
			assertThat(data, notNullValue());
			command = "DOM.getSearchResults";
			params = new HashMap<String, Object>();
			params.put("searchId", data);
			params.put("fromIndex", 0);
			params.put("toIndex", 1);
			nodeId = (long) -1;
			result = driver.executeCdpCommand(command, params);
			assertThat(result, notNullValue());
			assertThat(result, hasKey("nodeIds"));
			List<Long> nodes = (List<Long>) result.get("nodeIds");
			assertThat(nodes, notNullValue());
			assertThat(nodes.get(0), notNullValue());
			nodeId = nodes.get(0);
			err.println("nodeId: " + nodeId);
		} catch (com.google.gson.JsonSyntaxException e) {
			err.println(
					"JSON Exception in " + command + " (ignored): " + e.toString());
		} catch (Exception e) {
			err.println("Exception in " + command + " (ignored): " + e.toString());
		}
		try {

			command = "DOM.getOuterHTML";
			params = new HashMap<String, Object>();
			params.put("nodeId", nodeId);
			data = null;
			result = driver.executeCdpCommand(command, params);
			assertThat(result, notNullValue());
			assertThat(result, hasKey("outerHTML"));
			data = (String) result.get("outerHTML");
			assertThat(data, notNullValue());
			err.println("outerHTML: " + data);
		} catch (com.google.gson.JsonSyntaxException e) {
			err.println(
					"JSON Exception in " + command + " (ignored): " + e.toString());
		} catch (Exception e) {
			err.println("Exception in " + command + " (ignored): " + e.toString());
		}

		try {
			command = "DOM.highlightNode";
			driver.executeCdpCommand(command, new HashMap<String, Object>());
		} catch (com.google.gson.JsonSyntaxException e) {
			err.println(
					"JSON Exception in " + command + " (ignored): " + e.toString());
		} catch (Exception e) {
			err.println("Exception in " + command + " (ignored): " + e.toString());
		}
	}

	@Test
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM#method-getNodeForLocation
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM#type-NodeId
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM#method-getOuterHTML
	public void getNodeForLocationTest() {

		baseURL = "https://datatables.net/examples/api/highlight.html";
		driver.get(baseURL);
		data = null;
		command = "DOM.getNodeForLocation";
		params = new HashMap<String, Object>();

		WebElement element = driver
				.findElement(By.xpath("//table[@id='example']/tbody/tr[1]/td[1]"));
		int x = element.getLocation().getX();
		int y = element.getLocation().getX();
		err.println(String.format("x = %d, y = %d", x, y));
		err.println("outerHTML: " + element.getAttribute("outerHTML"));
		params.put("x", x);
		params.put("y", y);
		nodeId = (long) -1;
		// Act
		try {
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, notNullValue());
			assertThat(result, hasKey("backendNodeId"));
			nodeId = (Long) result.get("backendNodeId");
			err.println("backendNodeId: " + nodeId);
			assertThat(nodeId, notNullValue());
			// might not have frameId
			assertThat(result, hasKey("nodeId"));
			nodeId = (Long) result.get("nodeId");
			err.println("nodeId: " + nodeId);
			assertThat(nodeId, notNullValue());
		} catch (com.google.gson.JsonSyntaxException e) {
			err.println(
					"JSON Exception in " + command + " (ignored): " + e.toString());
		} catch (Exception e) {
			err.println("Exception in " + command + " (ignored): " + e.toString());
		}

		try {
			command = "DOM.getOuterHTML";
			params = new HashMap<String, Object>();
			params.put("nodeId", nodeId);
			data = null;
			result = driver.executeCdpCommand(command, params);
			assertThat(result, notNullValue());
			assertThat(result, hasKey("outerHTML"));
			data = (String) result.get("outerHTML");
			assertThat(data, notNullValue());
			err.println("outerHTML: " + data);
		} catch (com.google.gson.JsonSyntaxException e) {
			err.println(
					"JSON Exception in " + command + " (ignored): " + e.toString());
		} catch (Exception e) {
			err.println("Exception in " + command + " (ignored): " + e.toString());
		}
	}
}
