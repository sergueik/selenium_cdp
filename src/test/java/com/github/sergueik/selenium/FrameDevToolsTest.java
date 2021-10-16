package com.github.sergueik.selenium;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.devtools.v94.page.Page;
import org.openqa.selenium.devtools.v94.page.model.Frame;
import org.openqa.selenium.devtools.v94.page.model.FrameNavigated;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 *	 https://chromedevtools.github.io/devtools-protocol/1-2/Page/#event-frameAttached
 *	 https://chromedevtools.github.io/devtools-protocol/1-2/Page/#event-frameNavigated
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class FrameDevToolsTest extends EventSubscriptionCommonTest {
	private static int cnt = 0;
	private final static int maxcnt = 10;
	private final static String baseURL = "https://www.w3schools.com/html/tryit.asp?filename=tryhtml_iframe_target";

	@Before
	public void befores() throws Exception {
		chromeDevTools.createSession();
		chromeDevTools.send(Page.enable());

		// register to frame events
		chromeDevTools.addListener(Page.frameAttached(), o -> {
			if (cnt++ < maxcnt)
				System.err.println(
						String.format("Page has frame %s attached: ", o.getFrameId()));
		});
		chromeDevTools.addListener(Page.frameNavigated(), o -> System.err.println(
				String.format("Page has frame %s navigated: ", o.getFrame().getId())));
		driver.get(baseURL);
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
