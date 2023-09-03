package com.github.sergueik.selenium;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocument
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-querySelectorAll
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-describeNode
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#type-Node
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class PierceCdpTest extends BaseCdpTest {

	private static final String page = "iframe_example.html";

	private static String command = null;
	private static String baseURL = "https://demoqa.com/frames";

	private static Map<String, Object> params = new HashMap<>();
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> result2 = new HashMap<>();
	public static Long nodeId = (long) -1;

	@After
	public void afterTest() {
		command = "DOM.disable";
		result = driver.executeCdpCommand(command, new HashMap<String, Object>());
		driver.get("about:blank");
	}

	@Before
	public void beforeTest() {
		command = "DOM.enable";
		params = new HashMap<String, Object>();
		params.put("includeWhitespace", "all");
		result = driver.executeCdpCommand(command, params);
		// driver.get(Utils.getPageContent(page));
		driver.get(baseURL);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void test1() {

		try {
			command = "DOM.getDocument";
			params = new HashMap<>();
			params.put("pierce", true);
			params.put("depth", -1);
			// Act
			result = driver.executeCdpCommand(command, params);
			assertThat(result, hasKey("root"));
			result2 = (Map<String, Object>) result.get("root");
			Writer out = new OutputStreamWriter(
					new FileOutputStream(new File("test1.json")), "UTF-8");
			try {
				out.write(new Gson().toJson(result2, Map.class));
			} finally {
				out.close();
			}
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			e.printStackTrace();
			throw (new RuntimeException(e));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test2() {

		try {
			command = "DOM.getDocument";
			params = new HashMap<>();
			params.put("pierce", false);
			params.put("depth", -1);
			// Act
			result = driver.executeCdpCommand(command, params);
			assertThat(result, hasKey("root"));
			result2 = (Map<String, Object>) result.get("root");
			Writer out = new OutputStreamWriter(
					new FileOutputStream(new File("test2.json")), "UTF-8");
			try {
				out.write(new Gson().toJson(result2, Map.class));
			} finally {
				out.close();
			}
		} catch (JsonSyntaxException e) {
			System.err.println("JSON Syntax exception in " + command + " (ignored): "
					+ e.toString());
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			e.printStackTrace();
			throw (new RuntimeException(e));
		}
	}
}
