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

import org.openqa.selenium.devtools.v94.page.Page;
import org.openqa.selenium.devtools.v94.page.model.JavascriptDialogClosed;
import org.openqa.selenium.devtools.v94.page.model.JavascriptDialogOpening;

/**

 * 
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/1-2/Page/#event-javascriptDialogOpening
 * https://chromedevtools.github.io/devtools-protocol/1-3/Page/#event-javascriptDialogClosed 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class AlertDevToolsTest extends EventSubscriptionCommonTest {

	private final static String baseURL = "https://www.w3schools.com/js/tryit.asp?filename=tryjs_alert";
	// NOTE: fragile w.r. org.openqa.selenium.NoAlertPresentException and slowly
	// loading

	@After
	public void after() {
		driver.switchTo().defaultContent();
		chromeDevTools.clearListeners();
		driver.get("about:blank");
	}

	@Before
	public void before() {
		chromeDevTools.createSession();
		chromeDevTools.send(Page.enable());

		// register to alert events

		chromeDevTools.addListener(Page.javascriptDialogOpening(),
				(JavascriptDialogOpening event) -> System.err.println(String.format(
						"Page has dialog %s of type %s opening (%s)", event.getMessage(),
						event.getType(), event.getHasBrowserHandler().booleanValue())));
		driver.get(baseURL);
	}

	@Test
	public void acceptTest() {
		element = findButton();
		element.sendKeys(Keys.ENTER);
		//
		// assertThat(element.getDomAttribute("id"), notNullValue());
		sleep(100);
		alert = driver.switchTo().alert();
		// Assert alert displayed
		assertThat(alert, notNullValue());
		// https://github.com/SeleniumHQ/selenium/blob/trunk/common/devtools/chromium/v94/browser_protocol.pdl#L7715
		// assert
		// Whether dialog was confirmed
		chromeDevTools.addListener(Page.javascriptDialogClosed(),
				(JavascriptDialogClosed event) -> assertThat(event.getResult(),
						is(true)));
		if (debug)
			System.err.println("Selenium accepting alert.");
		alert.accept();
	}

	@Test
	public void dismissTest() {
		// chromeDevTools.send(Page.reload(Optional.of(true), Optional.empty()));
		chromeDevTools.addListener(Page.javascriptDialogClosed(),
				// NOTE: fragile. Also, The result is true for this alert, despite that
				// "dismiss" is called
				(JavascriptDialogClosed event) -> assertThat(event.getResult(),
						is(false)));
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
}
