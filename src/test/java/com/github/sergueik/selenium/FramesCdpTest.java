package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import org.openqa.selenium.WebDriverException;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge inspired
 * https://the-internet.herokuapp.com/nested_frames
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-getFrameTree
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getFrameOwner
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getOuterHTML
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#type-Frame
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#type-FrameId
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
@SuppressWarnings("unchecked")

public class FramesCdpTest extends BaseCdpTest {

	private static String command = null;
	private static String html = null;
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> data = new HashMap<>();
	private static Map<String, Object> data2 = new HashMap<>();
	private static Map<String, Integer> rgb_data = new HashMap<>();
	private static List<Object> data3 = new ArrayList<>();
	private String frameId = null;
	private static Map<String, Object> params = new HashMap<>();
	public static Long nodeId = (long) -1;
	private final List<String> frameKeys = Arrays
			.asList(new String[] { "domainAndRegistry", "securityOrigin",
					"secureContextType", "id", "url", "mimeType" });
	// note: "parentId" only is set for children frames

	@Before
	public void loadPage() {
	}

	@Test
	public void test1() {
		command = "Page.getFrameTree";
		// Arrange
		baseURL = "https://cloud.google.com/products/calculator";
		driver.get(baseURL);
		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			if (debug)
				System.err.println("Raw result: " + result);
			Map<String, Object> frameTree = (Map<String, Object>) result
					.get("frameTree");
			assertThat(frameTree, notNullValue());
			System.err
					.println("Frame tree keys: " + Arrays.asList(frameTree.keySet()));
			data = (Map<String, Object>) frameTree.get("frame");
			System.err.println("Frame keys: " + Arrays.asList(data.keySet()));
			for (String key : frameKeys) {
				assertThat(data, hasKey(key));
			}
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
				System.err
						.println("Child frame keys: " + Arrays.asList(data2.keySet()));
				for (String key : frameKeys) {
					assertThat(data2, hasKey(key));
				}
				assertThat(data2, hasKey("parentId"));
				System.err.println(String.format("Child frame id: %s, url: %s",
						data2.get("id"), data2.get("url")));
				command = "DOM.getFrameOwner";
				params = new HashMap<>();
				params.put("frameId", data2.get("id"));
				result = driver.executeCdpCommand(command, params);
				if (debug)
					System.err.println("DOM.getFrameOwner result: " + result);
				nodeId = Long.parseLong(result.get("nodeId").toString());

				command = "DOM.getOuterHTML";
				params.clear();
				params.put("nodeId", nodeId);
				result = driver.executeCdpCommand(command, params);
				assertThat(result, notNullValue());
				assertThat(result, hasKey("outerHTML"));
				html = (String) result.get("outerHTML");
				assertThat(html, notNullValue());
				System.err.println("Frame owner outer HTML: " + html);

			}
			for (Object childFrame : data3) {
				data = (Map<String, Object>) childFrame;
				assertThat(data, hasKey("frame"));
				data2 = (Map<String, Object>) data.get("frame");
				System.err
						.println("Child frame keys: " + Arrays.asList(data2.keySet()));
				for (String key : frameKeys) {
					assertThat(data2, hasKey(key));
				}
				assertThat(data2, hasKey("parentId"));
				System.err.println(String.format("Child frame id: %s, url: %s",
						data2.get("id"), data2.get("url")));
				command = "DOM.getFrameOwner";
				params = new HashMap<>();
				params.put("frameId", data2.get("id"));
				result = driver.executeCdpCommand(command, params);
				if (debug)
					System.err.println("DOM.getFrameOwner result: " + result);
				nodeId = Long.parseLong(result.get("nodeId").toString());

				command = "DOM.getOuterHTML";
				params.clear();
				params.put("nodeId", nodeId);
				result = driver.executeCdpCommand(command, params);
				assertThat(result, notNullValue());
				assertThat(result, hasKey("outerHTML"));
				html = (String) result.get("outerHTML");
				assertThat(html, notNullValue());
				System.err.println("Frame owner outer HTML: " + html);

			}

		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + "  " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	@Test
	public void test2() {
		// Arrange
		command = "Page.getFrameTree";
		// fails with the below
		// that uses the old style frameset and frame tags
		// baseURL =
		// "http://www.maths.surrey.ac.uk/explore/nigelspages/framenest.htm";
		// baseURL = "https://the-internet.herokuapp.com/nested_frames";
		// baseURL = "https://nunzioweb.com/iframes-example.htm";
		// https://jwcooney.com/2014/11/03/calling-page-elements-in-nested-iframes-with-javascript/
		// baseURL =
		// "https://www.sitepoint.com/community/t/can-i-use-iframe-inside-an-iframe/214310";
		baseURL = "https://www.javatpoint.com/oprweb/test.jsp?filename=htmliframes";
		driver.get(baseURL);
		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			if (debug)
				System.err.println("Page.getFrameTree result: " + result);
			Map<String, Object> frameTree = (Map<String, Object>) result
					.get("frameTree");
			assertThat(frameTree, notNullValue());
			System.err
					.println("Frame tree keys: " + Arrays.asList(frameTree.keySet()));
			data = (Map<String, Object>) frameTree.get("frame");
			System.err.println("Frame keys: " + Arrays.asList(data.keySet()));
			for (String key : frameKeys) {
				assertThat(data, hasKey(key));
			}
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
				System.err
						.println("Child frame keys: " + Arrays.asList(data2.keySet()));
				for (String key : frameKeys) {
					assertThat(data2, hasKey(key));
				}
				assertThat(data2, hasKey("parentId"));
				System.err.println(String.format("Child frame id: %s, url: %s",
						data2.get("id"), data2.get("url")));
			}
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + " " + e.toString());
			throw (new RuntimeException(e));
		}
	}

	@Test
	public void test3() {
		// Arrange
		command = "Page.getFrameTree";
		baseURL = "https://www.javatpoint.com/oprweb/test.jsp?filename=htmliframes";
		driver.get(baseURL);
		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			if (debug)
				System.err.println("Page.getFrameTree result: " + result);
			Map<String, Object> frameTree = (Map<String, Object>) result
					.get("frameTree");
			assertThat(frameTree, notNullValue());
			System.err
					.println("Frame tree keys: " + Arrays.asList(frameTree.keySet()));
			data = (Map<String, Object>) frameTree.get("frame");
			System.err.println("Frame keys: " + Arrays.asList(rgb_data.keySet()));
			for (String key : frameKeys) {
				assertThat(data, hasKey(key));
			}
			System.err.println(String.format("Frame id: %s, url: %s",
					rgb_data.get("id"), rgb_data.get("url")));
			data3 = (List<Object>) frameTree.get("childFrames");
			assertThat(data3, notNullValue());
			assertThat(data3.size(), greaterThan(0));
			System.err.println(data3.size() + " child frames");
			for (Object childFrame : data3) {
				data = (Map<String, Object>) childFrame;
				assertThat(data, hasKey("frame"));
				data2 = (Map<String, Object>) data.get("frame");
				System.err
						.println("Child frame keys: " + Arrays.asList(data2.keySet()));
				for (String key : frameKeys) {
					assertThat(data2, hasKey(key));
				}
				assertThat(data2, hasKey("parentId"));
				System.err.println(String.format("Child frame id: %s, url: %s",
						data2.get("id"), data2.get("url")));

				// THREE calls
				driver.executeCdpCommand("DOM.enable", new HashMap<>());
				driver.executeCdpCommand("Overlay.enable", new HashMap<>());
				rgb_data.clear();
				rgb_data.put("r", Utils.getRandomColor());
				rgb_data.put("g", Utils.getRandomColor());
				rgb_data.put("b", Utils.getRandomColor());
				rgb_data.put("a", 1);
				params.clear();
				frameId = data2.get("id").toString();
				params.put("frameId", frameId);
				params.put("contentColor", rgb_data);
				command = "Overlay.highlightFrame";
				System.err.println("Attempted to highlight frame " + frameId);
				driver.executeCdpCommand(command, params);
				Utils.sleep(1000);
			}
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (ignored): "
					+ Utils.processExceptionMessage(e.getMessage()));
		} catch (Exception e) {
			System.err.println("Exception in " + command + " " + e.toString());
			throw (new RuntimeException(e));
		}
	}
}
