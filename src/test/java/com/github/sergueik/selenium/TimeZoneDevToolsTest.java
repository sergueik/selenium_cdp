package com.github.sergueik.selenium;

/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v118.emulation.Emulation;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setUserAgentOverride
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class TimeZoneDevToolsTest extends BaseDevToolsTest {

	private static final String timezoneId = "America/Lima";
	private static final String timezoneDescription = "Peru Standard Time";

	private static String baseURL = "https://webbrowsertools.com/timezone/";

	@Test
	public void test1() {
		// does not work:
		// Assume.assumeTrue(pingHost("webbrowsertools.com", 443, 3));
		// NOTE: nc -z webbrowsertools.com 443
		// echo $?
		// 0

		// Arrange
		chromeDevTools.send(Emulation.setTimezoneOverride(timezoneId));
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

	@Test(expected = NullPointerException.class)
	// "If The timezone identifier is empty, disables the override and
	// restores default host system timezone."
	// NOTE: sending null leads to NPE
	public void test2() {
		// Arrange
		try {
			chromeDevTools.send(Emulation.setTimezoneOverride(null));
		} catch (NullPointerException e) {
			System.err.println("Null Pointer exception (expected): "
					+ Utils.processExceptionMessage(e.getMessage()));
			throw e;
		}
	}

	@After
	public void after() {

		TimeZone timeZone = Calendar.getInstance().getTimeZone();
		System.err.println(String.format("Current TimeZone is: %s (%s) ",
				timeZone.getID(), timeZone.getDisplayName()));

		chromeDevTools.send(Emulation.setTimezoneOverride(timeZone.getID()));
		driver.get("about:blank");
	}

}
