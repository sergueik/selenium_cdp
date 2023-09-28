package com.github.sergueik.selenium;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.v117.dom.DOM;
import org.openqa.selenium.devtools.v117.dom.DOM.EnableIncludeWhitespace;
import org.openqa.selenium.devtools.v117.dom.model.Node;
import org.openqa.selenium.devtools.v117.dom.model.NodeId;

import com.google.gson.Gson;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocument
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-querySelectorAll
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-describeNode
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#type-Node
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
public class PierceDevToolsTest extends BaseDevToolsTest {

	private static final String page = "iframe_example.html";
	private static String baseURL = "https://demoqa.com/frames";

	private static Node result;
	private NodeId nodeId = null;

	@After
	public void afterTest() {
		chromeDevTools.send(DOM.disable());
		driver.get("about:blank");
	}

	@Before
	public void beforeTest() {
		chromeDevTools.send(DOM.enable(Optional.of(EnableIncludeWhitespace.ALL)));
		// driver.get(Utils.getPageContent(page));
		driver.get(baseURL);

	}

	@Test
	public void test3() throws IOException {
		try {
			result = chromeDevTools
					.send(DOM.getDocument(Optional.of(-1), Optional.of(true)));

			String resultFilepath = System.getProperty("user.dir") + System.getProperty("file.separator") + "target"  + System.getProperty("file.separator")  + "test3.json";
			Writer out = new OutputStreamWriter(new FileOutputStream(new File(resultFilepath)), "UTF-8");
			try {
				out.write(new Gson().toJson(result, Node.class));
			} finally {
				out.close();
			}

		} catch (DevToolsException e) {
			System.err.println("Exception (rethrown) " + e.getMessage());
			throw e;
		}
	}

	@Test
	public void test4() throws IOException {
		try {
			result = chromeDevTools
					.send(DOM.getDocument(Optional.of(-1), Optional.of(false)));
			String resultFilepath = System.getProperty("user.dir") + System.getProperty("file.separator") + "target"  + System.getProperty("file.separator")  + "test3.json";
			Writer out = new OutputStreamWriter(new FileOutputStream(new File(resultFilepath)), "UTF-8");
			try {
				out.write(new Gson().toJson(result, Node.class));
			} finally {
				out.close();
			}

		} catch (DevToolsException e) {
			System.err.println("Exception (rethrown) " + e.getMessage());
			throw e;
		}
	}
}
