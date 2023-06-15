package com.github.sergueik.selenium;

/**
 * Copyright 2020,2021 Serguei Kouzmine
 */
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.ExpectedConditions;

import org.openqa.selenium.devtools.v113.page.Page;
import org.openqa.selenium.devtools.v113.page.model.JavascriptDialogClosed;
import org.openqa.selenium.devtools.v113.page.model.JavascriptDialogOpening;

/**
 * 
 * 
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/1-2/Page/#event-javascriptDialogOpening
 * https://chromedevtools.github.io/devtools-protocol/1-3/Page/#event-javascriptDialogClosed
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class AlertDevToolsTest extends EventSubscriptionCommonTest {

	// NOTE: fragile w.r. org.openqa.selenium.NoAlertPresentException and slowly
	// loading
	private final String text = "Lorem ipsum";

	@Before
	public void before() {
		chromeDevTools.createSession();
		chromeDevTools.send(Page.enable());
	}

	@After
	public void after() {
		driver.switchTo().defaultContent();
		chromeDevTools.clearListeners();
		driver.get("about:blank");
	}


	@Test
	public void acceptTest() {
		// Arrange
		// register to alert events
		chromeDevTools.addListener(Page.javascriptDialogOpening(),
				(JavascriptDialogOpening event) -> System.err
						.println(String.format("Page has dialog %s of type %s opening (%s)", event.getMessage(),
								event.getType(), event.getHasBrowserHandler().booleanValue())));
		// assert
		// Whether dialog was confirmed
		chromeDevTools.addListener(Page.javascriptDialogClosed(),
				(JavascriptDialogClosed event) -> assertThat(event.getResult(), is(true)));
		// Act
		driver.get("https://www.w3schools.com/js/tryit.asp?filename=tryjs_alert");
		element = findButton();
		element.sendKeys(Keys.ENTER);
		//
		// assertThat(element.getDomAttribute("id"), notNullValue());
		sleep(100);
		alert = driver.switchTo().alert();
		// Assert alert displayed
		assertThat(alert, notNullValue());
		// https://github.com/SeleniumHQ/selenium/blob/trunk/common/devtools/chromium.v99/browser_protocol.pdl#L7715
		if (debug)
			System.err.println("Selenium accepting alert.");
		alert.accept();
	}

	@Test
	public void dismissTest() {
		// Arrange
		// register to alert events
		chromeDevTools.addListener(Page.javascriptDialogOpening(),
				(JavascriptDialogOpening event) -> System.err
						.println(String.format("Page has dialog %s of type %s opening (%s)", event.getMessage(),
								event.getType(), event.getHasBrowserHandler().booleanValue())));
		chromeDevTools.addListener(Page.javascriptDialogClosed(),
				// NOTE: fragile. Also, The result is true for this alert, despite that
				// "dismiss" is called
				(JavascriptDialogClosed event) -> assertThat(event.getResult(), is(false)));
		// Act
		driver.get("https://www.w3schools.com/js/tryit.asp?filename=tryjs_alert");
		// chromeDevTools.send(Page.reload(Optional.of(true), Optional.empty()));
		element = findButton();
		// assertThat(element.getDomAttribute("id"), notNullValue());
		element.click();
		sleep(100);
		alert = wait.until(ExpectedConditions.alertIsPresent());
		// Assert alert displayed
		assertThat(alert, notNullValue());
		sleep(3000);
		if (debug)
			System.err.println("Selenium dismissing alert.");
		// assert that dialog was canceled
		alert.dismiss();
	}
	@Test
	public void promptTest() {
		// Arrange
		// register to dialog events
		chromeDevTools.addListener(Page.javascriptDialogOpening(),
				(JavascriptDialogOpening event) -> System.err.println(String
						.format("Dialog of type: %s opening with message: %s", event.getType(), event.getMessage())));
		chromeDevTools.addListener(Page.javascriptDialogClosed(), (JavascriptDialogClosed event) -> {
			assertThat(event.getUserInput(), notNullValue());
			assertThat(event.getUserInput(), is(text));
			System.err.println("Dialog user input: " + event.getUserInput());
		});
		// assert that dialog was accepted
		chromeDevTools.addListener(Page.javascriptDialogClosed(),
				(JavascriptDialogClosed event) -> assertThat(event.getResult(), is(true)));

		driver.get("https://www.w3schools.com/jsref/tryit.asp?filename=tryjsref_prompt");

		element = findButton();
		// Act
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
