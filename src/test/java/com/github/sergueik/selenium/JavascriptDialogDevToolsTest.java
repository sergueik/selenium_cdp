package com.github.sergueik.selenium;

/**
 * Copyright 2020,2021 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Before;
import org.junit.Test;

import org.openqa.selenium.support.ui.ExpectedConditions;

import org.openqa.selenium.devtools.v94.page.Page;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/1-3/Runtime/#method-evaluate
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#event-javascriptDialogOpening
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class JavascriptDialogDevToolsTest extends EventSubscriptionCommonTest {
	private final String text = "Lorem ipsum";
	private final static String baseURL = "https://www.w3schools.com/jsref/tryit.asp?filename=tryjsref_prompt";
	// NOTE: fragile
	// org.openqa.selenium.NoAlertPresentException

	@Before
	public void before() {
		chromeDevTools.createSession();
		chromeDevTools.send(Page.enable());

	}

	@Test
	public void promptTest() {
		// register to dialog events
		chromeDevTools.addListener(Page.javascriptDialogOpening(),
				event -> System.err.println(
						String.format("Dialog of type: %s opening with message: %s",
								event.getType(), event.getMessage())));
		chromeDevTools.addListener(Page.javascriptDialogClosed(), event -> {
			assertThat(event.getUserInput(), notNullValue());
			assertThat(event.getUserInput(), is(text));
			System.err.println("Dialog user input: " + event.getUserInput());
		});

		driver.get(baseURL);
		// assert that dialog was accepted
		chromeDevTools.addListener(Page.javascriptDialogClosed(),
				o -> assertThat(o.getResult(), is(true)));
		// Arrange
		element = findButton();
		element.click();
		sleep(100);
		alert = wait.until(ExpectedConditions.alertIsPresent());
		// Assert
		assertThat(alert, notNullValue());
		if (debug)
			System.err.println("Selenium entering text: " + text);
		// Act
		alert.sendKeys(text);
		sleep(100);
		// NOTE: alert.sendKeys expects a String argument not Keys
		// alert.sendKeys(Keys.RETURN);
		alert.accept();

	}

}
