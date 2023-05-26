package com.github.sergueik.selenium;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.WheelInput;

//based on: https://github.com/fugazi/carbonfour-selenium-4/blob/main/src/test/java/Selenium_4_Tests/TestWheelScrolling.java
//see also:

public class WheelInputTest extends BaseCdpTest {

	private Actions actions;
	private static String baseURL = "https://www.wikipedia.org";

	@Before
	public void setUp() {
		driver.get(baseURL);
		actions = new Actions(driver);
		// driver.manage().window().maximize();
	}

	@Test
	public void testWheelScrolling() {

		actions
				.scrollToElement(
						driver.findElement(By.cssSelector("div.central-textlogo")))
				.perform();

		actions.scrollByAmount(0, 500).perform();
		actions.scrollByAmount(0, -500).perform();

		actions.scrollFromOrigin(
				WheelInput.ScrollOrigin.fromElement(driver.findElement(By.cssSelector(
						"div.footer > div.other-projects span[ data-jsl10n='metawiki.name']"))),
				0, 500).perform();
		actions.scrollFromOrigin(WheelInput.ScrollOrigin.fromViewport(), 0, 500)
				.perform();
		actions
				.scrollFromOrigin(WheelInput.ScrollOrigin.fromViewport(35, 35), 0, 500)
				.perform();
	}
}
