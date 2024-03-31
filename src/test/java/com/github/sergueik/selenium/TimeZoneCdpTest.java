package com.github.sergueik.selenium;

/**
 * Copyright 2023,2024 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge inspired
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setTimezoneOverride
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */
public class TimeZoneCdpTest extends BaseCdpTest {

	private static String command = "Emulation.setTimezoneOverride";
	private static Map<String, Object> params = new HashMap<>();

	private static final String timezoneId = "America/Lima";
	private static final String timezoneDescription = "Peru Standard Time";
	// https://docs.oracle.com/middleware/12211/wcs/tag-ref/MISC/TimeZones.html

	static {
		{
			params.put("timezoneId", timezoneId);
		}
	};
	private static String baseURL = "https://webbrowsertools.com/timezone/";

	// see also:
	// https://github.com/SrinivasanTarget/selenium4CDPsamples/blob/master/src/test/java/DevToolsTest.java#L169
	@Test
	public void test1() {
		// Arrange
		driver.executeCdpCommand(command, params);
		// Act
		driver.get(baseURL);
		WebElement element = driver.findElement(By.xpath(
				"//table[@id=\"timezone\"]/tbody/tr/td[contains(text(),\"Time on Local Machine\")]/../td[@id=\"toString\"]"));
		Utils.highlight(element);
		Utils.sleep(100);
		// Assert
		assertThat(element.getAttribute("innerText"),
				containsString(timezoneDescription));
		System.err
				.println("Time on Local Machine: " + element.getAttribute("innerText"));

	}

	@Test(expected = WebDriverException.class)
	public void test2() {
		try {
			// Arrange
			driver.executeCdpCommand(command, new HashMap<>());
			// Assert
		} catch (WebDriverException e) {
			System.err.println("Web Driver exception in " + command + " (expected): "
					+ Utils.processExceptionMessage(e.getMessage()));
			throw e;
		}
	}

	@After
	public void after() {

		TimeZone timeZone = Calendar.getInstance().getTimeZone();
		System.err.println(String.format("Current TimeZone is: %s (%s) ",
				timeZone.getID(), timeZone.getDisplayName()));
		params.put("timezoneId", timeZone.getID());
		driver.executeCdpCommand(command, params);
		driver.get("about:blank");
	}

}

