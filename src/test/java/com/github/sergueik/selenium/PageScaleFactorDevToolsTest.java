package com.github.sergueik.selenium;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.devtools.v141.emulation.Emulation;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 *
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-resetPageScaleFactor
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setPageScaleFactor
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
// https://www.logicalincrements.com/articles/resolution

public class PageScaleFactorDevToolsTest extends BaseDevToolsTest {

	private final static String baseURL = "http://www.wikipedia.org";

	@After
	public void afterTest() {
		chromeDevTools.send(Emulation.resetPageScaleFactor());
	}

	@Before
	public void beforeTest() {
		driver.get(baseURL);
	}

	@Test
	public void test() {
		for (float scale : Arrays
				.asList(new Float[] { (float) 1.25, (float) 1.5, (float) 2 })) {
			chromeDevTools.send(Emulation.setPageScaleFactor(scale));
			Utils.sleep(1000);
		}

	}

}
