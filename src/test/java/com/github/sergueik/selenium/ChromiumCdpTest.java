package com.github.sergueik.selenium;

import static java.lang.System.err;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;

import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.time.Duration;
import java.time.Year;
import java.time.Month;
import java.time.MonthDay;
import java.time.LocalDate;

import javax.imageio.ImageIO;

import java.nio.file.Paths;

import java.util.Optional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.v94.network.Network;

import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.CapabilityType;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.github.sergueik.selenium.Utils;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge inspired
 * by https://toster.ru/q/653249?e=7897302#comment_1962398
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class ChromiumCdpTest {

	private static String osName = Utils.getOSName();

	private static ChromiumDriver driver;
	private static WebDriverWait wait;
	private static boolean runHeadless = false;

	private static int flexibleWait = 60;
	private static int pollingInterval = 500;

	private static Gson gson = new Gson();

	private static String command = null;
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();
	private static Map<String, Object> data = new HashMap<>();
	private static Map<String, Object> data2 = new HashMap<>();
	private static List<Object> data3 = new ArrayList<>();
	private static String dataString = null;
	private static List<Map<String, Object>> cookies = new ArrayList<>();
	public static Long nodeId = (long) -1;
	public static String isolationId = null;

	private static WebElement element = null;
	private static By locator = null;
	private static String baseURL = "about:blank";

	@BeforeClass
	public static void beforeClass() throws Exception {

		if ((System.getenv().containsKey("HEADLESS")
				&& System.getenv("HEADLESS").matches("(?:true|yes|1)"))
				|| (!(Utils.getOSName().equals("windows"))
						&& !(System.getenv().containsKey("DISPLAY")))) {
			runHeadless = true;
		}

		System
				.setProperty("webdriver.chrome.driver",
						Paths.get(System.getProperty("user.home"))
								.resolve("Downloads").resolve(osName.equals("windows")
										? "chromedriver.exe" : "chromedriver")
								.toAbsolutePath().toString());

		ChromeOptions options = new ChromeOptions();
		// see also:
		// https://ivanderevianko.com/2020/04/disable-logging-in-selenium-chromedriver
		// https://antoinevastel.com/bot%20detection/2017/08/05/detect-chrome-headless.html
		// @formatter:off
		for (String optionAgrument : (new String[] {
				"--allow-insecure-localhost",
				"--allow-running-insecure-content",
				"--browser.download.folderList=2",
				"--browser.helperApps.neverAsk.saveToDisk=image/jpg,text/csv,text/xml,application/xml,application/vnd.ms-excel,application/x-excel,application/x-msexcel,application/excel,application/pdf",
				"--disable-blink-features=AutomationControlled",
				"--disable-default-app",
				"--disable-dev-shm-usage",
				"--disable-extensions",
				"--disable-gpu",
				"--disable-infobars",
				"--disable-in-process-stack-traces",
				"--disable-logging",
				"--disable-notifications",
				"--disable-popup-blocking",
				"--disable-save-password-bubble",
				"--disable-translate",
				"--disable-web-security",
				"--enable-local-file-accesses",
				"--ignore-certificate-errors",
				"--ignore-certificate-errors",
				"--ignore-ssl-errors=true",
				"--log-level=3",
				"--no-proxy-server",
				"--no-sandbox",
				"--output=/dev/null",
				"--ssl-protocol=any",
				// "--start-fullscreen",
				// "--start-maximized" ,
				"--user-agent=Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20120101 Firefox/33.0",
				// String.format("--browser.download.dir=%s", downloadFilepath)
				/*
				 * "--user-data-dir=/path/to/your/custom/profile",
				 * "--profile-directory=name_of_custom_profile_directory",
				 */
		})) {
			options.addArguments(optionAgrument);
		}
		// @formatter:on
		// options for headless
		// NOTE: Deprecated chrome option is ignored: useAutomationExtension
		// options.setExperimentalOption("useAutomationExtension", false);
		if (runHeadless) {
			options.addArguments("--headless", "--disable-gpu");
		}

		driver = new ChromeDriver(options);
		// TODO:
		/*
		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		capabilities.setBrowserName(DesiredCapabilities.chrome().getBrowserName());
		capabilities.setCapability(
				org.openqa.selenium.chrome.ChromeOptions.CAPABILITY, options);
		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		driver = new ChromeDriver(capabilities);
		*/
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		Utils.setDriver(driver);
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
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

	@After
	public void clearPage() {
		driver.get("about:blank");
	}

	// https://stackoverflow.com/questions/60409219/how-do-you-disable-navigator-webdriver-in-chromedriver
	// https://intoli.com/blog/not-possible-to-block-chrome-headless/chrome-headless-test.js
	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-addScriptToEvaluateOnNewDocument
	@Test
	public void scriptOnNewDocumentTest() {
		try {
			// Arrange
			command = "Page.addScriptToEvaluateOnNewDocument";
			params = new HashMap<>();
			final String script = "Object.defineProperty(navigator, 'webdriver', { get: () => undefined })";
			params.put("source", script);
			result = driver.executeCdpCommand(command, params);
			System.err.println("Result: " + result);
			String dataString = (String) result.get("identifier");
			assertThat(dataString, notNullValue());
			System.err.println("Script injected: " + dataString);
			// Act
			driver.get(
					"https://intoli.com/blog/not-possible-to-block-chrome-headless/chrome-headless-test.html");
			Utils.sleep(4000);
			command = "Page.removeScriptToEvaluateOnNewDocument";
			params = new HashMap<>();
			params.put("identifier", dataString);
			driver.executeCdpCommand(command, params);
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-grantPermissions
	// https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-setPermission
	// https://chromedevtools.github.io/devtools-protocol/tot/Browser/#type-PermissionType
	// https://chromedevtools.github.io/devtools-protocol/tot/Browser/#type-PermissionSetting
	// https://source.chromium.org/chromium/chromium/src/+/master:third_party/blink/renderer/modules/permissions/permission_descriptor.idl
	@Test
	public void chromeHeadlessDetectionTest() {

		try {
			// Arrange
			command = "Browser.setPermission";
			params = new HashMap<>();
			data = new HashMap<>();
			data.put("name", "notifications");
			params.put("permission", data);
			params.put("setting", "prompt");
			result = driver.executeCdpCommand(command, params);
			System.err.println("Result: " + result);

			command = "Browser.grantPermissions";
			params = new HashMap<>();
			params.put("permissions", Arrays.asList("notifications"));
			result = driver.executeCdpCommand(command, params);
			System.err.println("Result: " + result);
			// Act
			driver.navigate().to(
					"https://intoli.com/blog/not-possible-to-block-chrome-headless/chrome-headless-test.html");

			List<WebElement> elements = new ArrayList<>();
			WebElement element = null;
			WebElement element2 = null;

			Map<String, String> statuses = new HashMap<>();
			elements.clear();
			elements = driver
					.findElements(By.xpath("//*[contains(@class, 'result')]"));
			assertTrue(elements.size() > 0);

			for (int cnt = 0; cnt != elements.size(); cnt++) {
				element = elements.get(cnt);
				String value = element.getAttribute("class").replaceAll("result", "")
						.replaceAll("\\s", "");
				Utils.highlight(element);

				element2 = element.findElement(By.xpath("preceding-sibling::td"));
				Utils.highlight(element);
				// NOTE: not descriptive
				String key = element.getAttribute("id");
				System.err
						.println("Collecting " + element2.getText().replaceAll("\\n", " "));
				statuses.put(key, value);
				Utils.sleep(500);
			}
			for (Entry<String, String> entry : statuses.entrySet()) {
				System.err.println(entry.getKey() + " = " + entry.getValue());
			}

		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}
	/*
	 *  headless mode:
	 *  permissions-result = failed
	 * 	chrome-result = failed
	 *  languages-result = passed
	 * 	webdriver-result = passed
	 * 	plugins-length-result = failed
	 * 	user-agent-result = passed
	 */

	/*
	 *  headless mode:
	 *  permissions-result = failed
	 * 	chrome-result = failed
	 *  languages-result = passed
	 * 	webdriver-result = passed
	 * 	plugins-length-result = failed
	 * 	user-agent-result = passed
	 */
	// NOTE: works fine alone, but not because of "Overlay.enable" only availavle
	// in tip-of-tree protocol
	// https://chromedevtools.github.io/devtools-protocol/tot/Overlay/#method-enable
	// https://chromedevtools.github.io/devtools-protocol/1-2/DOM/#type-RGBA
	// https://chromedevtools.github.io/devtools-protocol/1-2/DOM/#method-highlightNode
	// https://chromedevtools.github.io/devtools-protocol/1-2/DOM/#type-HighlightConfig
	// https://chromedevtools.github.io/devtools-protocol/1-2/DOM/#method-hideHighlight
	@SuppressWarnings("unchecked")
	@Test
	public void nodeHighlightTest() {
		// Arrange
		driver.get("http://www.wikipedia.org");
		String command = "DOM.getDocument";
		params = new HashMap<>();
		params.put("pierce", false);
		params.put("depth", 0);
		try {
			// Act
			result = driver.executeCdpCommand(command, params);
			nodeId = Long.parseLong(
					((Map<String, Object>) result.get("root")).get("nodeId").toString());
			command = "DOM.querySelector";
			params.clear();
			params.put("nodeId", nodeId);
			params.put("selector", "#js-link-box-en > strong");

			result = driver.executeCdpCommand(command, params);
			assertThat(result, hasKey("nodeId"));
			nodeId = (Long) result.get("nodeId");
			assertTrue(nodeId != 0);
			err.println("Command " + command + " returned nodeId: " + nodeId);

			//
			// DOM.HighlightConfig
			data.clear();
			data.put("showInfo", true);
			data.put("showRulers", true);
			// DOM.RGBA
			data2 = new HashMap<>();
			data2.put("r", "255");
			data2.put("g", "0");
			data2.put("b", "0");
			data.put("contentColor", data2);
			data2.clear();
			data2.put("r", 0);
			data2.put("g", 128);
			data2.put("b", 0);

			data.put("marginColor", data2);
			data2.clear();
			data2.put("r", 0);
			data2.put("g", 0);
			data2.put("b", 128);
			data.put("borderColor", data2);
			command = "Overlay.enable";
			result = driver.executeCdpCommand(command, new HashMap<>());
			// Highlights DOM node with given id
			command = "DOM.highlightNode";
			params.clear();
			params.put("nodeId", nodeId);
			params.put("highlightConfig", data);
			dataString = null;
			result = driver.executeCdpCommand(command, params);
			command = "DOM.getOuterHTML";
			params.clear();
			params.put("nodeId", nodeId);
			dataString = null;
			result = driver.executeCdpCommand(command, params);
			assertThat(result, notNullValue());
			assertThat(result, hasKey("outerHTML"));
			dataString = (String) result.get("outerHTML");
			assertThat(dataString, notNullValue());
			err.println("Command " + command + " return outerHTML: " + dataString);

			Utils.sleep(1000);

			command = "DOM.hideHighlight";
			params.clear();
			result = driver.executeCdpCommand(command, params);
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-setAttributesAsText
	// https://chromedevtools.github.io/devtools-protocol/1-2/DOM/#method-removeAttribute
	@SuppressWarnings("unchecked")
	@Test
	public void nodesetAttributesAsTextTest() {
		// Arrange
		driver.get("http://www.wikipedia.org");
		String command = "DOM.getDocument";
		params = new HashMap<>();
		params.put("pierce", false);
		params.put("depth", 0);
		try {
			// Act
			result = driver.executeCdpCommand(command, params);
			nodeId = Long.parseLong(
					((Map<String, Object>) result.get("root")).get("nodeId").toString());
			command = "DOM.querySelector";
			params.clear();
			params.put("nodeId", nodeId);
			params.put("selector", "#js-link-box-en > strong");

			result = driver.executeCdpCommand(command, params);
			assertThat(result, hasKey("nodeId"));
			nodeId = (Long) result.get("nodeId");
			assertTrue(nodeId != 0);
			err.println("Command " + command + " returned nodeId: " + nodeId);

			command = "DOM.setAttributesAsText";
			params.clear();
			params.put("nodeId", nodeId);
			params.put("text", "style = \"color:green\"");
			result = driver.executeCdpCommand(command, params);

			command = "DOM.getOuterHTML";
			params.clear();
			params.put("nodeId", nodeId);
			dataString = null;
			result = driver.executeCdpCommand(command, params);
			assertThat(result, notNullValue());
			assertThat(result, hasKey("outerHTML"));
			dataString = (String) result.get("outerHTML");
			assertThat(dataString, notNullValue());
			err.println("Command " + command + " return outerHTML: " + dataString);
			Utils.sleep(1000);
			command = "DOM.removeAttribute";
			params.clear();
			params.put("nodeId", nodeId);
			params.put("name", "style");
			result = driver.executeCdpCommand(command, params);

		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	} // https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-setAttributesAsText
	// https://chromedevtools.github.io/devtools-protocol/1-2/DOM/#method-removeAttribute

	@SuppressWarnings("unchecked")
	@Test
	public void nodesetAttributesAsTextBadFormatTest() {
		// Arrange
		driver.get("http://www.wikipedia.org");
		String command = "DOM.getDocument";
		params = new HashMap<>();
		params.put("pierce", false);
		params.put("depth", 0);
		try {
			// Act
			params.clear();
			result = driver.executeCdpCommand(command, params);
			nodeId = Long.parseLong(
					((Map<String, Object>) result.get("root")).get("nodeId").toString());
			command = "DOM.querySelector";
			params.clear();
			params.put("nodeId", nodeId);
			params.put("selector", "#js-link-box-en > strong");

			result = driver.executeCdpCommand(command, params);
			assertThat(result, hasKey("nodeId"));
			nodeId = (Long) result.get("nodeId");
			assertTrue(nodeId != 0);
			err.println("Command " + command + " returned nodeId: " + nodeId);

			command = "DOM.setAttributesAsText";
			params.clear();
			params.put("nodeId", nodeId);
			// fail
			params.put("text", "\"style\" = \"color:green\"");
			result = driver.executeCdpCommand(command, params);
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
		try {
			command = "DOM.getOuterHTML";
			params.clear();
			params.put("nodeId", nodeId);
			dataString = null;
			result = driver.executeCdpCommand(command, params);
			assertThat(result, notNullValue());
			assertThat(result, hasKey("outerHTML"));
			dataString = (String) result.get("outerHTML");
			assertThat(dataString, notNullValue());
			err.println("Command " + command + " return outerHTML: " + dataString);
			Utils.sleep(1000);
			command = "DOM.removeAttribute";
			params.clear();
			params.put("nodeId", nodeId);
			params.put("name", "style");
			result = driver.executeCdpCommand(command, params);

		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}

	}

	// @Ignore
	@Test
	// https://chromedevtools.github.io/devtools-protocol/1-2/Input/#method-dispatchKeyEvent
	public void zoomDefaultTest() {
		// Assert
		driver.get("https://ya.ru");
		command = "Input.dispatchKeyEvent";
		try {
			// Act
			for (int cnt = 0; cnt != 10; cnt++) {
				params.clear();
				params.put("type", "char");
				params.put("keyIdentifier", "U+002D"); // minus
				params.put("modifiers", 2);
				// params.put("text", "-");
				driver.executeCdpCommand(command, params);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			params.clear();
			params.put("type", "char");
			// keyDown, keyUp, rawKeyDown, char
			params.put("text", "0");
			params.put("modifiers", 2);
			// Alt=1, Ctrl=2, Meta/Command=4, Shift=8 (default: 0).
			driver.executeCdpCommand(command, params);
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}

	}

	// https://github.com/qtacore/chrome_master/blob/master/chrome_master/input_handler.py#L32
	@Test
	public void dispatchMouseEventTest() {
		// Arrange
		driver.get("https://www.wikipedia.org");
		element = wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector("#js-link-box-ru")));
		org.openqa.selenium.Rectangle rect = element.getRect();

		System.err.println(String.format(
				"dispatchMouseEventTes target x:%d y:%d width:%d height:%d",
				rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
		int x = rect.getX() + rect.getWidth() / 2;
		int y = rect.getY() + rect.getHeight() / 2;
		System.err
				.println(String.format("dispatchMouseEventTes point x:%d y:%d", x, y));
		// returns the root DOM node and subtree, default to depth 1
		command = "Input.dispatchMouseEvent";
		try {
			// Act
			params.clear();
			params.put("type", "mousePressed");
			// mousePressed, mouseReleased, mouseMoved, mouseWhneel
			params.put("x", x);
			params.put("y", y);
			params.put("button", "left");
			params.put("clickCount", 1);
			params.put("modifiers", 0);
			// Alt=1, Ctrl=2, Meta/Command=4, Shift=8 (default: 0).
			result = driver.executeCdpCommand(command, params);
			Utils.sleep(100);
			params.clear();
			params.put("type", "mouseReleased");
			// mousePressed, mouseReleased, mouseMoved, mouseWhneel
			params.put("x", x);
			params.put("y", y);
			params.put("button", "left");
			params.put("clickCount", 1);
			params.put("modifiers", 0);
			// Alt=1, Ctrl=2, Meta/Command=4, Shift=8 (default: 0).
			result = driver.executeCdpCommand(command, params);
			Utils.sleep(3000);
			// Assert
			assertThat(driver.getCurrentUrl(), containsString("ru"));
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
		}
	}

	// @Ignore
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocuments
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM#type-Node
	@SuppressWarnings("unchecked")
	@Test
	public void getDocumentTest() {
		// Arrange
		driver.get("https://www.google.com");
		// returns the root DOM node and subtree, default to depth 1
		command = "DOM.getDocument";
		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			// Assert
			assertThat(result, hasKey("root"));
			data.clear();
			data = (Map<String, Object>) result.get("root");
			assertThat(data, hasKey("nodeId"));
			assertTrue(Long.parseLong(data.get("nodeId").toString()) != 0);
			System.err.println("Command " + command + " return node: "
					+ new Gson().toJson(data, Map.class));
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}

	}

	// @Ignore
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocuments
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM#type-Node
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-describeNode
	@SuppressWarnings("unchecked")
	@Test
	public void describeNodeTest() {
		// Arrange
		driver.get("https://www.google.com");
		String command = "DOM.getDocument";
		params = new HashMap<>();
		params.put("pierce", false);
		params.put("depth", 0);
		try {
			// Act
			result = driver.executeCdpCommand(command, params);
			nodeId = (Long) ((Map<String, Object>) result.get("root")).get("nodeId");
			// Describes node given its id
			command = "DOM.describeNode";
			params.clear();
			params.put("nodeId", nodeId);
			params.put("depth", 0);
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, hasKey("node"));
			data = (Map<String, Object>) result.get("node");
			for (String field : Arrays.asList(new String[] { "baseURL", "localName",
					"nodeName", "nodeType", "nodeValue" })) {
				assertThat(data, hasKey(field));
			}
			assertThat(data.get("nodeName"), is("#document"));
			System.err.println("Command " + command + " returned node: "
					+ new Gson().toJson(data, Map.class));
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// @Ignore
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocuments
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM#type-Node
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-querySelector
	@SuppressWarnings("unchecked")
	@Test
	public void querySelectorTest() {
		// Arrange
		driver.get("https://www.google.com");
		String command = "DOM.getDocument";
		params = new HashMap<>();
		params.put("pierce", false);
		params.put("depth", 0);
		try {
			// Act
			result = driver.executeCdpCommand(command, params);
			nodeId = Long.parseLong(
					((Map<String, Object>) result.get("root")).get("nodeId").toString());
			command = "DOM.querySelector";
			params.clear();
			params.put("nodeId", nodeId);
			params.put("selector", "img#hplogo");

			result = driver.executeCdpCommand(command, params);
			assertThat(result, hasKey("nodeId"));
			nodeId = (Long) result.get("nodeId");
			assertTrue(nodeId != 0);
			err.println("Command " + command + " returned nodeId: " + nodeId);
			// Returns node's HTML markup
			command = "DOM.getOuterHTML";
			params.clear();
			params.put("nodeId", nodeId);
			dataString = null;
			result = driver.executeCdpCommand(command, params);
			assertThat(result, notNullValue());
			assertThat(result, hasKey("outerHTML"));
			dataString = (String) result.get("outerHTML");
			assertThat(dataString, notNullValue());
			err.println("Command " + command + " return outerHTML: " + dataString);
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// @Ignore
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocuments
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM#type-Node
	// https://chromedevtools.github.io/devtools-protocol/1-2/DOM/#method-querySelectorAll
	@SuppressWarnings("unchecked")
	@Test
	public void querySelectorAllTest() {
		// Arrange
		driver.get("https://www.google.com");
		String command = "DOM.getDocument";
		params = new HashMap<>();
		params.put("pierce", false);
		params.put("depth", 0);
		try {
			// Act
			result = driver.executeCdpCommand(command, params);
			nodeId = Long.parseLong(
					((Map<String, Object>) result.get("root")).get("nodeId").toString());
			command = "DOM.querySelectorAll";
			params.clear();
			params.put("nodeId", nodeId);
			params.put("selector", "input[type='submit']");

			result = driver.executeCdpCommand(command, params);
			assertThat(result, hasKey("nodeIds"));
			List<Long> nodeIds = (List<Long>) result.get("nodeIds");
			assertThat(nodeIds, notNullValue());
			assertTrue(nodeIds.size() != 0);
			err.println("Command " + command + " returned nodeIds: " + nodeIds);
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocument
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM#type-Node
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-resolveNode
	@SuppressWarnings("unchecked")
	@Test
	public void resolveNodTest() {
		// Arrange
		driver.get("https://www.google.com");
		String command = "DOM.getDocument";
		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			nodeId = (Long) ((Map<String, Object>) result.get("root")).get("nodeId");
			// select DOM node in #document
			command = "DOM.querySelector";
			params.clear();
			params.put("nodeId", nodeId);
			params.put("selector", "img#hplogo");
			result = driver.executeCdpCommand(command, params);
			nodeId = (Long) result.get("nodeId");

			// Resolves the JavaScript node object for a given NodeId or BackendNodeId
			command = "DOM.resolveNode";
			params.clear();
			params.put("nodeId", nodeId);

			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, hasKey("object"));
			data.clear();
			// JavaScript object wrapper for given node
			data = (Map<String, Object>) result.get("object");
			for (String field : Arrays.asList(new String[] { "type", "subtype",
					"className", "description", "objectId" })) {
				assertThat(data, hasKey(field));
			}
			dataString = (String) data.get("objectId");
			assertThat(dataString, notNullValue());
			// reuse data to peek into dataString
			data = (Map<String, Object>) new Gson().fromJson(dataString, Map.class);
			// Unique object identifier
			System.err
					.println("Command " + command + " returned objectId data: " + data);

		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocument
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM#type-Node
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-resolveNode
	@SuppressWarnings("unchecked")
	@Test
	// TODO: command = "Runtime.callFunctionOn";
	public void callFunctionOnTest() {
		// Arrange
		driver.get("https://www.google.com");
		String command = "DOM.getDocument";
		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			nodeId = (Long) ((Map<String, Object>) result.get("root")).get("nodeId");

			// select DOM node in #document
			command = "DOM.querySelector";
			params.clear();
			params.put("nodeId", nodeId);
			params.put("selector", "img#hplogo");

			result = driver.executeCdpCommand(command, params);
			nodeId = (Long) result.get("nodeId");

			// Resolves the JavaScript node object for a given NodeId or BackendNodeId
			command = "DOM.resolveNode";
			params.clear();
			params.put("nodeId", nodeId);
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, hasKey("object"));
			data.clear();
			// JavaScript object wrapper for given node
			data = (Map<String, Object>) result.get("object");
			// reuse data to peek into dataString
			dataString = (String) data.get("object");
			data = (Map<String, Object>) new Gson()
					.fromJson((String) data.get("objectId"), Map.class);
			// Unique object identifier
			System.err
					.println("Command " + command + " returned objectId data: " + data);

			command = "Runtime.callFunctionOn";
			params = new HashMap<>();
			params.put("functionDeclaration", "function() { this.value=''; }");
			params.put("objectId", dataString);
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, hasKey("result"));
			data.clear();
			data = (Map<String, Object>) result.get("result");
			for (String field : Arrays.asList(
					new String[] { "type", "subtype", "className", "objectId" })) {
				assertThat(data, hasKey(field));
			}
			String objectId = (String) data.get("objectId");
			assertThat(objectId, notNullValue());
			System.err
					.println("Command " + command + " returned objectId: " + objectId);
		} catch (WebDriverException e) {
			Throwable cause = e.getCause();
			System.err.println("WebDriverException in command " + command
					+ " (ignored): " + Utils.processExceptionMessage(e.getMessage())
					+ ((cause == null) ? "" : "cause: " + cause.getMessage()));
			/*
			 * StackTraceElement[] stackTraceElements = e.getStackTrace(); for (int cnt = 0;
			 * cnt != stackTraceElements.length; cnt++) {
			 * err.println(String.format("StackTrace %d: %s", cnt,
			 * stackTraceElements[cnt].toString())); }
			 */
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// @Ignore
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocument
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM#type-Node
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-querySelector
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-describeNode
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-focus
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-highlightNode
	// https://chromedevtools.github.io/devtools-protocol/tot/Runtime#type-RemoteObjectId
	@SuppressWarnings("unchecked")
	@Test
	public void multiCommandTest() {
		// Arrange
		driver.get("https://www.google.com");
		command = "DOM.getDocument";
		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			// Assert
			assertThat(result, hasKey("root"));
			Map<String, Object> node = (Map<String, Object>) result.get("root");
			assertThat(node, hasKey("nodeId"));
			nodeId = Long.parseLong(node.get("nodeId").toString());
			assertTrue(nodeId != 0);
			err.println("Command " + command + " returned nodeId: " + nodeId);
			command = "DOM.describeNode";
			params = new HashMap<>();
			params.put("nodeId", nodeId);
			params.put("depth", 1);
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, hasKey("node"));
			// reuse "params" variable
			params.clear();
			params = (Map<String, Object>) result.get("node");
			for (String field : Arrays.asList(
					new String[] { "nodeType", "nodeName", "localName", "nodeValue" })) {
				assertThat(params, hasKey(field));
			}

			System.err.println("Command " + command + " returned: "
					+ new Gson().toJson(params, Map.class));

			command = "DOM.querySelector";
			// params.clear();
			// causes java.lang.UnsupportedOperationException
			params = new HashMap<>();
			params.put("nodeId", nodeId);
			// params.put("selector", "img#hplogo");
			params.put("selector", "input[name='q']");

			result = driver.executeCdpCommand(command, params);
			// depth, 1
			// Assert
			assertThat(result, hasKey("nodeId"));
			// @SuppressWarnings("unchecked")
			nodeId = Long.parseLong(result.get("nodeId").toString());
			assertTrue(nodeId != 0);
			err.println("Command " + command + " returned  nodeId: " + nodeId);

			command = "DOM.resolveNode";
			params = new HashMap<>();
			params.put("nodeId", nodeId);

			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, hasKey("object"));
			data.clear();
			data = (Map<String, Object>) result.get("object");
			for (String field : Arrays.asList(
					new String[] { "type", "subtype", "className", "objectId" })) {
				assertThat(data, hasKey(field));
			}
			String objectId = (String) data.get("objectId");
			assertThat(objectId, notNullValue());
			System.err
					.println("Command " + command + " returned objectId: " + objectId);

			command = "DOM.something not defined";
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			// DOM.removeNode
			command = "DOM.focus";
			params = new HashMap<>();
			params.put("nodeId", nodeId);
			// Act
			result = driver.executeCdpCommand(command, params);
			command = "DOM.highlightNode";
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			Utils.sleep(1000);
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
		// TODO: command = "Runtime.callFunctionOn";
	}

	// @Ignore
	@Test
	public void getIsolatedIdTest() {
		// Arrange
		baseURL = "https://www.google.com";
		driver.get(baseURL);
		String command = "Runtime.getIsolateId";
		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			assertThat(result, hasKey("id"));
			isolationId = (String) result.get("id");
			assertThat(isolationId, notNullValue());
			// isolationId = Long.parseLong((String) result.get("id"));
			// assertTrue(isolationId != 0);
			// Assert ?
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// @Ignore
	@Test
	public void compileScriptTest() {
		// Arrange
		baseURL = "https://www.google.com";
		driver.get(baseURL);
		command = "Runtime.compileScript";
		// "Runtime.runScript"
		params = new HashMap<>();
		params.put("expression", "function() { alert('test'); }");
		params.put("persistScript", false);
		// params.put("sourceURL", null);

		// params.put("executionContextId", 0);
		try {
			result = driver.executeCdpCommand(command, params);
			System.err.println("Response to " + command + ": " + result);
		} catch (UnhandledAlertException e) {
			assertThat(e.toString(), containsString("unexpected alert open"));
			System.err.println("Exception (ignored): " + e.toString());
		} catch (WebDriverException e) {
			System.err.println("WebDriverException in command " + command
					+ " (ignored): " + Utils.processExceptionMessage(e.getMessage()));
			// assertThat(e.toString(), containsString("invalid argument: Invalid
			// parameters"));
		} catch (Exception e) {
			System.err.println("Exception: " + e.toString());
			throw (new RuntimeException(e));
		}

	}

	// @Ignore
	@Test
	public void evaluateTest() {
		// Arrange
		baseURL = "https://www.google.com";
		driver.get(baseURL);
		command = "Runtime.evaluate";
		params = new HashMap<>();
		params.put("expression", "var f = function() { alert('test'); }");
		// {result={type=undefined}}
		// params.put("expression", "alert('test');");
		// NPE in org.openqa.selenium.chromium.ChromiumDriver.executeCdpCommand
		try {
			result = driver.executeCdpCommand(command, params);
			System.err.println("Response to " + command + ": " + result);
		} catch (WebDriverException e) {
			System.err.println("WebDriverException (rethrown): "
					+ Utils.processExceptionMessage(e.getMessage()));
			throw new RuntimeException(e.toString());
		}

	}

	// @Ignore
	// see:
	// https://github.com/sergueik/powershell_selenium/blob/master/python/print_pdf.py
	// origin: https://habr.com/ru/post/459112/
	// NOTE: Python uses different REST-like route
	// "/session/$sessionId/chromium/send_command_and_get_result"
	// than Java
	// "/session/$sessionId/goog/cdp/execute"
	// https://chromedevtools.github.io/devtools-protocol/tot/Page#method-printToPDF
	@Test
	public void printToPDFTest() {
		driver.get("https://www.google.com");
		String command = "Page.printToPDF";
		params = new HashMap<>();
		params.put("landscape", false);
		params.put("displayHeaderFooter", false);
		params.put("printBackground", true);
		params.put("preferCSSPageSize", true);
		try {
			result = driver.executeCdpCommand(command, params);
			System.err.println("Response to " + command + ": " + result);
			// TODO: assert the response is a valid Base64-encoded pdf data.
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
			assertThat(e.toString(), containsString("PrintToPDF is not implemented"));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// @Ignore
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
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// @Ignore
	// https://chromedevtools.github.io/devtools-protocol/tot/Browser#method-getWindowForTarget
	// https://chromedevtools.github.io/devtools-protocol/tot/Browser#method-setWindowBounds
	// https://chromedevtools.github.io/devtools-protocol/tot/Browser#type-Bounds
	@Test
	public void setWindowBoundsTest() {
		command = "Browser.getWindowForTarget";
		Long windowId = (long) -1;
		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<String, Object>());
			// Assert
			assertThat(result, notNullValue());
			System.err.println("Command " + command + " result: " + result);
			assertThat(result, hasKey("windowId"));
			windowId = (long) result.get("windowId");
			command = "Browser.setWindowBounds";
			params = new HashMap<String, Object>();
			Map<String, Object> bounds = new HashMap<String, Object>();
			String windowState = "minimized";
			bounds.put("windowState", windowState);
			params.put("bounds", bounds);
			params.put("windowId", windowId);
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, notNullValue());
			System.err.println("Command " + command + " result: " + result);
			Utils.sleep(1000);
			command = "Browser.setWindowBounds";
			params = new HashMap<String, Object>();
			bounds = new HashMap<String, Object>();
			windowState = "normal";
			bounds.put("windowState", windowState);
			params.put("bounds", bounds);
			params.put("windowId", windowId);
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, notNullValue());
			System.err.println("Command " + command + " result: " + result);
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}

	}

	// @Ignore
	// https://chromedevtools.github.io/devtools-protocol/tot/Browser#method-getWindowForTarget
	// https://chromedevtools.github.io/devtools-protocol/tot/Browser#method-getWindowBounds
	// https://chromedevtools.github.io/devtools-protocol/tot/Browser#type-Bounds
	@Test
	public void getBrowserWindowDetailsTest() {
		command = "Browser.getWindowForTarget";
		Long windowId = (long) -1;
		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<String, Object>());
			// Assert
			assertThat(result, notNullValue());
			System.err.println("Command " + command + " result: " + result);
			assertThat(result, hasKey("windowId"));
			windowId = (long) result.get("windowId");
			command = "Browser.getWindowBounds";
			params = new HashMap<String, Object>();
			params.put("windowId", windowId);
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, notNullValue());
			System.err.println("Command " + command + " result: " + result);
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// @Ignore
	// https://chromedevtools.github.io/devtools-protocol/tot/Network#method-enable
	@Test
	public void manageNetworkTrackingTest() {
		baseURL = "http://www.example.com/";
		command = "Network.enable";
		try {
			// Act
			params = new HashMap<String, Object>();
			params.put("maxTotalBufferSize", 100000000);
			params.put("maxResourceBufferSize", null);
			params.put("maxPostDataSize", null);
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, notNullValue());
			System.err.println("Command " + command + " result: " + result);
			driver.get(baseURL);
			command = "Network.disable";
			// Act
			driver.executeCdpCommand(command, new HashMap<>());
		} catch (InvalidArgumentException e) {
			System.err.println("Argument exception (ignored): " + e.toString());
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// @Ignore
	// https://chromedevtools.github.io/devtools-protocol/tot/Network#method-getResponseBody
	@Test
	public void getResponseBodyTest() {
		baseURL = "http://www.example.com/";
		driver.get(baseURL);
		command = "Network.getResponseBody";
		try {
			// Act
			params = new HashMap<String, Object>();
			params.put("requestId", "");
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, notNullValue());
			System.err.println("Command " + command + " result: " + result);
		} catch (InvalidArgumentException e) {
			System.err.println("Argument exception (ignored): " + e.toString());
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// @Ignore
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
			 * (java.lang.ClassCastExceptin e) { err.println("Exception (ignored): " +
			 * e.toString()); } }) ;
			 */
			/*
			 * for (String cookie : cookies) { System.err.println("Cookie:" + cookie); }
			 */
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// @Ignore
	@Test
	public void clearBrowserCookiesTest() {
		baseURL = "https://www.google.com";
		driver.get(baseURL);
		command = "Network.clearBrowserCookies";
		try {
			// Act
			driver.executeCdpCommand(command, new HashMap<>());
			// Assert ?
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// @Ignore
	@Test
	public void deleteCookiesTest() {
		baseURL = "https://www.google.com";
		driver.get(baseURL);
		command = "Network.deleteCookies";
		try {
			// Act
			params = new HashMap<String, Object>();
			params.put("requestId", "");
			params = new HashMap<>();
			String name = "NID";
			params.put("name", name);
			String url = "";
			params.put("url", url);
			String domain = ".google.com";
			params.put("domain", domain);
			String path = "/";
			params.put("path", path);

			result = driver.executeCdpCommand(command, params);
			System.err.println("Result of command " + command + " = " + result);
			// Assert ?
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// @Ignore
	@Test
	// based on:
	// https://qna.habr.com/q/732307
	// https://github.com/sahajamit/chrome-devtools-webdriver-integration/blob/master/src/test/java/com/sahajamit/DemoTests.java
	// https://chromedevtools.github.io/devtools-protocol/tot/Page#method-captureScreenshot
	// https://chromedevtools.github.io/devtools-protocol/tot/Page#type-Viewport
	public void capturElementScreenshotTest() {

		// basic logo example
		// baseURL = "https://www.google.com/";
		// String xpath = "//img[@id = 'hplogo'][@alt='Google']";

		// schedule of classes for today
		LocalDate localDate = LocalDate.now();
		Year year = Year.from(localDate);
		Month month = Month.from(localDate);
		MonthDay monthDay = MonthDay.now();
		baseURL = String.format(
				"http://almetpt.ru/%s/site/schedulegroups/0/1/%s-%02d-%02d",
				year.toString(), year.toString(), month.getValue(),
				monthDay.getDayOfMonth());
		baseURL = "http://almetpt.ru/2020/site/schedulegroups/0/1/2020-03-02";
		String xpath = "//div[@class=\"card-columns\"]//div[contains(@class, \"card\")]"
				+ "[div[contains(@class, \"card-header\")]]";
		driver.get(baseURL);
		result = null;
		dataString = null;
		// not assigning the value returned
		wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.xpath("//div[@class=\"card-columns\"]")));
		List<WebElement> elements = driver.findElements(By.xpath(xpath));
		int cnt = 0;
		int maxCnt = 10;
		cards: for (WebElement element : elements) {
			if (null == element
					.findElement(By.xpath("div[contains(@class, \"card-body\")]"))) {
				continue;
			}
			cnt++;
			if (cnt >= maxCnt) {
				break cards;
			}
			Utils.highlight(element);
			int x = element.getLocation().getX();
			int y = element.getLocation().getY();
			int width = element.getSize().getWidth();
			int height = element.getSize().getHeight();
			int scale = 1;

			command = "Page.captureScreenshot";
			params = new HashMap<String, Object>();
			Map<String, Object> viewport = new HashMap<>();
			System.err.println("Specified viewport: " + String
					.format("x=%d, y=%d, width=%d, height=%d", x, y, width, height));
			viewport.put("x", (double) x);
			viewport.put("y", (double) y);
			viewport.put("width", (double) width);
			viewport.put("height", (double) height);
			viewport.put("scale", scale);
			params.put("clip", viewport);
			try {
				// Act
				result = driver.executeCdpCommand(command, params);
				// Assert
				assertThat(result, notNullValue());
				assertThat(result, hasKey("data"));
				dataString = (String) result.get("data");
				assertThat(dataString, notNullValue());
			} catch (WebDriverException e) {
				System.err.println("Web Driver exception in " + command + " (ignored): "
						+ Utils.processExceptionMessage(e.getMessage()));
			} catch (Exception e) {
				System.err.println("Exception in " + command + "  " + e.toString());
				throw (new RuntimeException(e));
			}

			Base64 base64 = new Base64();
			byte[] image = base64.decode(dataString);
			try {
				BufferedImage o = ImageIO.read(new ByteArrayInputStream(image));
				assertThat(o.getWidth(), greaterThan(0));
				assertThat(o.getHeight(), greaterThan(0));
			} catch (IOException e) {
				System.err
						.println("Exception loading image (	ignored): " + e.toString());
			}
			String screenshotFileName = String.format("card%02d.png", cnt);
			try {
				FileOutputStream fileOutputStream = new FileOutputStream(
						screenshotFileName);
				fileOutputStream.write(image);
				fileOutputStream.close();
			} catch (IOException e) {
				System.err.println("Exception saving image (ignored): " + e.toString());
			}
		}
	}

	// @Ignore
	@Test
	// https://chromedevtools.github.io/devtools-protocol/tot/Page#method-captureScreenshot
	public void captureScreenshotTest() {
		baseURL = "https://www.google.com";
		driver.get(baseURL);
		result = null;
		dataString = null;
		command = "Page.captureScreenshot";
		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			// Assert
			assertThat(result, notNullValue());
			assertThat(result, hasKey("data"));
			dataString = (String) result.get("data");
			assertThat(dataString, notNullValue());
		} catch (WebDriverException e) {
			err.println("Exception in command " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			err.println("Exception: in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}

		Base64 base64 = new Base64();
		byte[] image = base64.decode(dataString);
		try {
			BufferedImage o = ImageIO.read(new ByteArrayInputStream(image));
			assertThat(o.getWidth(), greaterThan(0));
			assertThat(o.getHeight(), greaterThan(0));
		} catch (IOException e) {
			System.err.println("Exception loading image (ignored): " + e.toString());
		}
		String screenshotFileName = "temp.png";
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(
					screenshotFileName);
			fileOutputStream.write(image);
			fileOutputStream.close();
		} catch (IOException e) {
			System.err.println("Exception saving image (ignored): " + e.toString());
		}
	}

	// @Ignore
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
			// Assert
			System.err.println("Cookies count for " + baseURL + ": "
					+ ((List<Object>) result.get("cookies")).size() + "...");
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// @Ignore
	@Test
	@SuppressWarnings("unchecked")
	public void getCookiesTest1() {
		// Arrange
		driver.get("https://www.google.com");
		command = "Page.getCookies";
		// Act
		try {
			result = driver.executeCdpCommand(command, new HashMap<String, Object>());
			System.err.println("Cookies: "
					+ result.get("cookies").toString().substring(0, 100) + "...");
			// Assert
			// de-serialiaze
			cookies = gson.fromJson(result.get("cookies").toString(),
					ArrayList.class);
			cookies.stream().limit(10).map(o -> o.keySet())
					.forEach(System.err::println);
			Set<String> cookieKeys = new HashSet<>();
			for (String key : new String[] { "domain", "expires", "httpOnly", "name",
					"path", "secure", "session", "size", "value" }) {
				cookieKeys.add(key);
			}
			assertTrue(cookies.get(0).keySet().containsAll(cookieKeys));
		} catch (JsonSyntaxException e) {
			System.err.println(
					"Exception deserializing cookies (ignored): " + e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
		}
	}

	// @Ignore
	@Test
	@SuppressWarnings("unchecked")
	public void getCookiesTest2() {
		// Arrange
		driver.get("https://www.google.com");
		command = "Page.getCookies";
		// Act
		try {
			result = driver.executeCdpCommand(command, new HashMap<String, Object>());
			err.println("Cookies: "
					+ result.get("cookies").toString().substring(0, 100) + "...");
			// Assert
			// direct cast
			cookies = (ArrayList<Map<String, Object>>) result.get("cookies");
			cookies.stream().limit(10).map(o -> o.keySet())
					.forEach(System.err::println);
			Set<String> cookieKeys = new HashSet<>();
			for (String key : new String[] { "domain", "expires", "httpOnly", "name",
					"path", "secure", "session", "size", "value" }) {
				cookieKeys.add(key);
			}
			assertTrue(cookies.get(0).keySet().containsAll(cookieKeys));
		} catch (JsonSyntaxException e) {
			System.err
					.println("Exception loading cookies (ignored): " + e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
		}
	}

	// @Ignore
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
		dataString = null;
		command = "DOM.performSearch";
		params = new HashMap<String, Object>();

		WebElement element = driver
				.findElement(By.xpath("//table[@id='example']/tbody/tr[1]/td[1]"));
		System.err.println("outerHTML: " + element.getAttribute("outerHTML"));

		params.put("query", "//table[@id='example']/tbody/tr[1]/td[1]");
		// Act
		try {
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, notNullValue());
			assertThat(result, hasKey("searchId"));
			dataString = (String) result.get("searchId");
			System.err.println("searchId: " + dataString);
			assertThat(data, notNullValue());
			command = "DOM.getSearchResults";
			params = new HashMap<String, Object>();
			params.put("searchId", dataString);
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
			System.err.println("nodeId: " + nodeId);

			command = "DOM.getOuterHTML";
			params = new HashMap<String, Object>();
			params.put("nodeId", nodeId);
			dataString = null;
			result = driver.executeCdpCommand(command, params);
			assertThat(result, notNullValue());
			assertThat(result, hasKey("outerHTML"));
			dataString = (String) result.get("outerHTML");
			assertThat(dataString, notNullValue());
			System.err.println("outerHTML: " + dataString);
			// TODO:
			// Exception in DOM.highlightNode (ignored):
			// org.openqa.selenium.InvalidArgumentException: invalid argument:
			// Invalid parameters
			command = "DOM.highlightNode";
			driver.executeCdpCommand(command, new HashMap<String, Object>());
		} catch (InvalidArgumentException e) {
			System.err.println("Argument exception (ignored): " + e.toString());
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// @Ignore
	@Test
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM#method-getNodeForLocation
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM#type-NodeId
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM#method-getOuterHTML
	public void getNodeForLocationTest() {

		baseURL = "https://datatables.net/examples/api/highlight.html";
		driver.get(baseURL);
		command = "DOM.getNodeForLocation";
		params = new HashMap<String, Object>();

		WebElement element = driver
				.findElement(By.xpath("//table[@id='example']/tbody/tr[1]/td[1]"));
		int x = element.getLocation().getX();
		int y = element.getLocation().getX();
		System.err.println(String.format("x = %d, y = %d", x, y));
		System.err.println("outerHTML: " + element.getAttribute("outerHTML"));
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
			command = "DOM.getOuterHTML";
			params = new HashMap<String, Object>();
			params.put("nodeId", nodeId);
			dataString = null;
			result = driver.executeCdpCommand(command, params);
			assertThat(result, notNullValue());
			assertThat(result, hasKey("outerHTML"));
			dataString = (String) result.get("outerHTML");
			assertThat(dataString, notNullValue());
			err.println("outerHTML: " + dataString);
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// @Ignore
	// based on a more advanced code found in
	// https://github.com/adiohana/selenium-chrome-devtools-examples/blob/master/src/test/java/ChromeDevToolsTest.java
	// https://chromedevtools.github.io/devtools-protocol/tot/Network#method-setCacheDisabled
	// https://chromedevtools.github.io/devtools-protocol/tot/Network#method-setBlockedURLs
	@SuppressWarnings("serial")
	@Test
	public void setBlockedURLsTest() {
		// Arrange
		command = "Network.setCacheDisabled";
		try {
			driver.executeCdpCommand(command, new HashMap<String, Object>() {
				{
					put("cacheDisabled", true);
					// NOTE: value has to be a boolean, otherwise
					// org.openqa.selenium.InvalidArgumentException: invalid argument
				}
			});
			baseURL = "https://www.wikipedia.org/";
			command = "Network.setBlockedURLs";
			// Act
			// NOTE: in-line hashmap initialization code looks rather ugly
			driver.executeCdpCommand(command, new HashMap<String, Object>() {
				{
					put("urls", Arrays.asList(new String[] { "*.css", "*.png" }));
				}
			});
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
		driver.get(baseURL);
		// driver.navigate().refresh();
		Utils.sleep(1000);
	}

	// @Ignore
	@Test
	// based on:
	// https://chromedevtools.github.io/devtools-protocol/tot/Emulation#method-setGeolocationOverride
	// see also:
	// https://github.com/sahajamit/chrome-devtools-webdriver-integration/blob/master/src/test/java/com/sahajamit/DemoTests.java
	// https://github.com/sahajamit/chrome-devtools-webdriver-integration/blob/master/src/main/java/com/sahajamit/messaging/MessageBuilder.java
	public void setGeoLocationTest() {

		// Arrange
		command = "Emulation.setGeolocationOverride";
		params = new HashMap<String, Object>();
		Double latitude = 37.422290;
		Double longitude = -122.084057;
		params.put("latitude", latitude);
		params.put("longitude", longitude);
		params.put("accuracy", 100);
		// Act
		try {
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, notNullValue());
			err.println("Response from " + command + ": " + result);
			// Act

			baseURL = "https://www.google.com/maps";
			driver.get(baseURL);

			// click "my location" button when drawn

			element = wait.until(ExpectedConditions.visibilityOfElementLocated(By
					.cssSelector("div[class *='widget-mylocation-button-icon-common']")));
			element.click();
			// unclear what event to wait for here
			Utils.sleep(5000);
			result = null;
			dataString = null;
			command = "Page.captureScreenshot";
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			// Assert
			assertThat(result, notNullValue());
			assertThat(result, hasKey("data"));
			dataString = (String) result.get("data");
			assertThat(dataString, notNullValue());

			Base64 base64 = new Base64();
			byte[] image = base64.decode(dataString);
			BufferedImage o = ImageIO.read(new ByteArrayInputStream(image));
			assertThat(o.getWidth(), greaterThan(0));
			assertThat(o.getHeight(), greaterThan(0));
			String screenshotFileName = "map.png";
			FileOutputStream fileOutputStream = new FileOutputStream(
					screenshotFileName);
			fileOutputStream.write(image);
			fileOutputStream.close();
		} catch (IOException e) {
			System.err
					.println("Exception saving image file (ignored): " + e.toString());
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
		// Assert
	}

	// @Ignore
	@Test
	// based on:
	// https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-resetPageScaleFactor
	// https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setPageScaleFactor
	// see also:
	// https://github.com/sahajamit/chrome-devtools-webdriver-integration/blob/master/src/test/java/com/sahajamit/DemoTests.java
	// https://github.com/sahajamit/chrome-devtools-webdriver-integration/blob/master/src/main/java/com/sahajamit/messaging/MessageBuilder.java
	public void setPageScaleFactorTest() {

		// Arrange
		baseURL = "https://www.google.com/maps";
		driver.get(baseURL);

		command = "Emulation.resetPageScaleFactor";

		try {
			// Act
			driver.executeCdpCommand(command, new HashMap<>());
			// returns empty JSON
			Utils.sleep(1000);

			command = "Emulation.setPageScaleFactor";
			// Act
			params.clear();
			params.put("pageScaleFactor", 1.5);
			driver.executeCdpCommand(command, params);
			// returns empty JSON
			Utils.sleep(1000);

			// Act
			params.clear();
			params.put("pageScaleFactor", 1);
			driver.executeCdpCommand(command, params);
			// returns empty JSON
			Utils.sleep(1000);
		} catch (WebDriverException e) {
			err.println("Exception in command " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			err.println("Exception: in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	@Test
	// https://chromedevtools.github.io/devtools-protocol/tot/Page#method-captureSnapshot
	public void captureSnapshotTest() {
		driver.get("https://developer.chrome.com/extensions/pageCapture");
		String command = "Page.captureSnapshot";
		dataString = null;
		params = new HashMap<>();
		params.put("format", "mhtml");
		try {
			result = driver.executeCdpCommand(command, params);
			assertThat(result, hasKey("data"));
			dataString = (String) result.get("data");
			// Assert
			// like an email, but the following is failing
			for (String field : Arrays.asList(new String[] {
					"Snapshot-Content-Location", "Subject", "Content-Type" })) {
				assertThat(dataString, containsString(String.format("%s:", field)));
			}
			// assertThat(data, containsString("\n\n"));
			String header = dataString.split("\n\n")[0];
			assertThat(header, notNullValue());
			// System.err.println("Response to " + command + ": header" + header);
		} catch (WebDriverException e) {
			err.println("Exception in command " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			err.println("Exception: in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// @Ignore
	// https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setExtraHTTPHeaders
	@Test
	public void addCustomHeadersTest() {
		// Arrange
		String command = "Network.enable";
		params = new HashMap<>();
		params.put("maxTotalBufferSize", 0);
		params.put("maxPostDataSize", 0);
		params.put("maxPostDataSize", 0);
		try {
			driver.executeCdpCommand(command, params);
			// ignore the result
			command = "Network.setExtraHTTPHeaders";
			params = new HashMap<>();
			Map<String, String> headers = new HashMap<>();
			headers.put("customHeaderName",
					this.getClass().getName() + " addCustomHeadersTest");
			params.put("headers", headers);
			driver.executeCdpCommand(command, params);
			// ignore the result
			// Act
			// to test with a dummy server fire on locally and inspect the headers
			// server-side
			driver.get("http://127.0.0.1:8080/demo/Demo");
			// Assert
			// done through console logs inspection of the server
			// otherwise just hit a generic web site
			// driver.get("https://apache.org");
		} catch (WebDriverException e) {
			err.println("Exception in command " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			err.println("Exception: in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// https://en.wikipedia.org/wiki/Basic_access_authentication
	// https://examples.javacodegeeks.com/core-java/apache/commons/codec/binary/base64-binary/org-apache-commons-codec-binary-base64-example/
	@Test
	public void basicAuthenticationTest() {

		final String username = "guest";
		final String password = "guest";
		String command = null;
		try {
			// Arrange
			command = "Network.enable";
			driver.get("https://jigsaw.w3.org/HTTP/");
			params = new HashMap<>();
			params.put("maxTotalBufferSize", 10000000);
			params.put("maxResourceBufferSize", 5000000);
			params.put("maxPostDataSize", 5000000);
			driver.executeCdpCommand(command, params);
			command = "Network.setExtraHTTPHeaders";
			params = new HashMap<>();
			Map<String, String> headers = new HashMap<>();
			Base64 base64 = new Base64();
			headers.put("Authorization", "Basic " + new String(base64
					.encode(String.format("%s:%s", username, password).getBytes())));
			params.put("headers", headers);
			driver.executeCdpCommand(command, params);
			// Act
			element = wait.until(ExpectedConditions.visibilityOf(
					driver.findElement(By.cssSelector("table td> a[href=\"Basic/\"]"))));
			element.click();
			wait.until(
					ExpectedConditions.urlToBe("https://jigsaw.w3.org/HTTP/Basic/"));

			element = driver.findElement(By.tagName("body"));
			assertThat("get past authentication", element.getAttribute("innerHTML"),
					containsString("Your browser made it!"));
			Utils.sleep(1000);
		} catch (WebDriverException e) {
			err.println("WebDriverException in command " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			err.println("Exception: in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	@Test
	// based on:
	// https://github.com/SrinivasanTarget/selenium4CDPsamples/blob/master/src/test/java/DevToolsTest.java
	// https://chromedevtools.github.io/devtools-protocol/tot/Performance#method-setTimeDomain
	// https://chromedevtools.github.io/devtools-protocol/tot/Performance#method-enable
	// https://chromedevtools.github.io/devtools-protocol/tot/Performance#method-getMetrics
	public void getPerformanceMetricsTest() {
		driver.get("about:blank");
		String command = "Performance.setTimeDomain";
		params = new HashMap<>();
		params.put("timeDomain", "timeTicks");
		try {
			driver.executeCdpCommand(command, params);
			// Act
			command = "Performance.enable";
			driver.executeCdpCommand(command, new HashMap<>());

			driver.get("https://www.wikipedia.org");
			// Act
			command = "Performance.getMetrics";
			result = driver.executeCdpCommand(command, new HashMap<>());
			// Assert
			assertThat(result, notNullValue());
			assertThat(result, hasKey("metrics"));
			ArrayList<Object> metrics = (ArrayList<Object>) result.get("metrics");
			assertThat(metrics, notNullValue());
			assertThat(metrics.size(), greaterThan(0));
			System.err.println("Metric dimension: " + metrics.size());
			// see also:
			// https://github.com/ShamaUgale/Selenium4Examples/blob/master/src/main/java/com/devtools/GetMetrics.java
			for (int cnt = 0; cnt != metrics.size(); cnt++) {
				Object metricEntry = metrics.get(cnt);
				assertThat(metricEntry, notNullValue());
				System.err
						.println(String.format("Metric Data[%d] => %s", cnt, metricEntry));
				@SuppressWarnings("unchecked")
				Map<String, Integer> metricData = (Map<String, Integer>) metricEntry;
				assertThat(metricData, hasKey("name"));
				assertThat(metricData, hasKey("value"));
			}
			// Act
			command = "Performance.disable";
			driver.executeCdpCommand(command, new HashMap<>());
		} catch (WebDriverException e) {
			err.println("Exception in command " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			err.println("Exception: in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-getFrameTree
	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#type-Frame
	@SuppressWarnings("unchecked")
	@Test
	public void frameTreeTest() {
		// Arrange
		command = "Page.getFrameTree";
		params = new HashMap<>();
		driver.get("https://cloud.google.com/products/calculator");
		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			System.err.println("Result: " + result);
			Map<String, Object> frameTree = (Map<String, Object>) result
					.get("frameTree");
			assertThat(frameTree, notNullValue());
			System.err.println("Frame tree: " + Arrays.asList(frameTree.keySet()));
			data = (Map<String, Object>) frameTree.get("frame");
			System.err.println("Frame frame keys: " + Arrays.asList(data.keySet()));
			assertThat(data, hasKey("url"));
			assertThat(data, hasKey("id"));
			System.err.println(String.format("Frame id: %s, url: %s", data.get("id"),
					data.get("url")));
			data3 = (List<Object>) frameTree.get("childFrames");
			assertThat(data3, notNullValue());
			assertThat(data3.size(), greaterThan(0));
			System.err.println(data3.size() + " child frames");
			for (Object childFrame : data3) {
				data = (Map<String, Object>) childFrame;
				assertThat(data, hasKey("frame"));
				data2 = (Map<String, Object>) data.get("frame");
				assertThat(data2, hasKey("url"));
				assertThat(data2, hasKey("id"));
				assertThat(data2, hasKey("parentId"));
				System.err.println(String.format("Child frame id: %s, url: %s",
						data2.get("id"), data2.get("url")));
				// Web Driver exception in Overlay.highlightFrame
				/*
				command = "Overlay.highlightFrame";
				params = new HashMap<>();
				params.put("frameId", data2.get("id"));
				data = new HashMap<>();
				data.put("r", "255");
				data.put("g", "0");
				data.put("b", "0");
				params.put("contentColor", data);
				params.put("contentOutlineColor", data);
				driver.executeCdpCommand(command, params);
				Utils.sleep(1000);
				*/
			}
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

}
