package com.github.sergueik.selenium;

/**
 * Copyright 2020,2021,2023,2024 Serguei Kouzmine
 */
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v123.page.Page;
import org.openqa.selenium.devtools.v123.page.model.JavascriptDialogClosed;
import org.openqa.selenium.devtools.v123.page.model.JavascriptDialogOpening;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * 
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/1-2/Page/#event-javascriptDialogOpening
 * https://chromedevtools.github.io/devtools-protocol/1-3/Page/#event-javascriptDialogClosed
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class AlertDevToolsTest extends EventSubscriptionCommonTest {

	private final String text = "Lorem ipsum";
	protected static boolean debug = true;

	// @Ignore
	@Test
	public void acceptTest() {
		// Arrange
		// register to alert events
		chromeDevTools.addListener(Page.javascriptDialogOpening(),
				(JavascriptDialogOpening event) -> System.err.println(String.format(
						"Page has dialog %s of type %s opening (%s)", event.getMessage(),
						event.getType(), event.getHasBrowserHandler().booleanValue())));
		// assert
		// Whether dialog was confirmed
		chromeDevTools.addListener(Page.javascriptDialogClosed(),
				(JavascriptDialogClosed event) -> assertThat(event.getResult(),
						is(true)));
		// Act
		driver.get("https://www.w3schools.com/js/tryit.asp?filename=tryjs_alert");
		element = findButton();
		element.sendKeys(Keys.ENTER);
		//
		// assertThat(element.getDomAttribute("id"), notNullValue());
		Utils.sleep(100);
		alert = driver.switchTo().alert();
		// Assert alert displayed
		assertThat(alert, notNullValue());
		// https://github.com/SeleniumHQ/selenium/blob/trunk/common/devtools/chromium.v99/browser_protocol.pdl#L7715
		if (debug)
			System.err.println("Selenium accepting alert.");
		alert.accept();
	}

	// @Ignore
	@Test
	public void dismissTest() {
		// Arrange
		// register to alert events
		chromeDevTools.addListener(Page.javascriptDialogOpening(),
				(JavascriptDialogOpening event) -> System.err.println(String.format(
						"Page has dialog %s of type %s opening (%s)", event.getMessage(),
						event.getType(), event.getHasBrowserHandler().booleanValue())));
		chromeDevTools.addListener(Page.javascriptDialogClosed(),
				// NOTE: fragile. Also, The result is true for this alert, despite that
				// "dismiss" is called
				(JavascriptDialogClosed event) -> assertThat(event.getResult(),
						is(false)));
		// Act
		driver.get("https://www.w3schools.com/js/tryit.asp?filename=tryjs_alert");
		// chromeDevTools.send(Page.reload(Optional.of(true), Optional.empty()));
		element = findButton();
		// assertThat(element.getDomAttribute("id"), notNullValue());
		element.click();
		Utils.sleep(100);
		alert = wait.until(ExpectedConditions.alertIsPresent());
		// Assert alert displayed
		assertThat(alert, notNullValue());
		Utils.sleep(3000);
		if (debug)
			System.err.println("Selenium dismissing alert.");
		// assert that dialog was canceled
		alert.dismiss();
	}

	// @Ignore
	@Test
	public void promptTest() {
		// Arrange
		// register to dialog events
		chromeDevTools.addListener(Page.javascriptDialogOpening(),
				(JavascriptDialogOpening event) -> System.err.println(
						String.format("Dialog of type: %s opening with message: %s",
								event.getType(), event.getMessage())));
		chromeDevTools.addListener(Page.javascriptDialogClosed(),
				(JavascriptDialogClosed event) -> {
					assertThat(event.getUserInput(), notNullValue());
					assertThat(event.getUserInput(), is(text));
					System.err.println("Dialog user input: " + event.getUserInput());
				});
		// assert that dialog was accepted
		chromeDevTools.addListener(Page.javascriptDialogClosed(),
				(JavascriptDialogClosed event) -> assertThat(event.getResult(),
						is(true)));

		driver.get(
				"https://www.w3schools.com/jsref/tryit.asp?filename=tryjsref_prompt");

		element = findButton();
		// Act
		element.click();
		Utils.sleep(100);
		alert = wait.until(ExpectedConditions.alertIsPresent());
		// Assert
		assertThat(alert, notNullValue());
		if (debug)
			System.err.println("Selenium entering text: " + text);
		// Act
		alert.sendKeys(text);
		Utils.sleep(100);
		// NOTE: alert.sendKeys expects a String argument not Keys
		// alert.sendKeys(Keys.RETURN);
		alert.accept();

	}

}
