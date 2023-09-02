package com.github.sergueik.selenium;
/**
 * Copyright 2021,2023 Serguei Kouzmine
 */

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v116.page.Page;
import org.openqa.selenium.devtools.v116.page.model.FrameAttached;
import org.openqa.selenium.devtools.v116.page.model.FrameNavigated;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/1-2/Page/#event-frameAttached
 * https://chromedevtools.github.io/devtools-protocol/1-2/Page/#event-frameNavigated
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

// public class FrameDevToolsTest extends EventSubscriptionCommonTest {
public class FrameDevToolsTest extends BaseDevToolsTest {
	private static int cnt = 0;
	private static WebElement element;
	private final static int maxcnt = 10;
	private final static String baseURL = "https://www.w3schools.com/html/tryit.asp?filename=tryhtml_iframe_target";

	@Before
	public void before() throws Exception {
		chromeDevTools.createSession();
		chromeDevTools.send(Page.enable());

		// register to frame events
		chromeDevTools.addListener(Page.frameAttached(), (FrameAttached event) -> {
			if (cnt++ < maxcnt)
				System.err.println(
						String.format("Page has frame %s attached: ", event.getFrameId()));
		});
		chromeDevTools.addListener(Page.frameNavigated(),
				(FrameNavigated event) -> System.err.println(String.format(
						"Page has frame %s navigated: ", event.getFrame().getId())));
		driver.get(baseURL);
	}

	@After
	public void afterTest() throws Exception {
		chromeDevTools.clearListeners();
		chromeDevTools.send(Page.disable());
	}


	// @Ignore
	@Test
	public void Test() {
		element = driver.findElement(By.tagName("iframe"));
		driver.switchTo().frame(element);
		driver.switchTo().defaultContent();
		chromeDevTools.send(Page.reload(Optional.of(true), Optional.empty()));
	}

}
