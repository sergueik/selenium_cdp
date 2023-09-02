package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.v116.dom.DOM;
import org.openqa.selenium.devtools.v116.dom.DOM.EnableIncludeWhitespace;
import org.openqa.selenium.devtools.v116.dom.model.Node;
import org.openqa.selenium.devtools.v116.dom.model.NodeId;

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
			Writer out = new OutputStreamWriter(
					new FileOutputStream(new File("test3.json")), "UTF-8");
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
			Writer out = new OutputStreamWriter(
					new FileOutputStream(new File("test4.json")), "UTF-8");
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
