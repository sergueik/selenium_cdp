package com.github.sergueik.selenium;

/**
 * Copyright 2020 Serguei Kouzmine
 */
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.devtools.page.Page;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/1-2/Page/#event-javascriptDialogOpening
 * https://chromedevtools.github.io/devtools-protocol/1-3/Page/#event-javascriptDialogClosed 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class AlertDevToolsTest extends EventSubscriptionCommonTest {

	private final static String baseURL = "https://www.w3schools.com/js/tryit.asp?filename=tryjs_alert";
	// NOTE: fragile w.r. org.openqa.selenium.NoAlertPresentException

	@Before
	public void before() {
		chromeDevTools.createSession();
		chromeDevTools.send(Page.enable());

		// register to alert events
		chromeDevTools.addListener(Page.javascriptDialogOpening(),
				o -> System.err
						.println(String.format("Page has dialog %s of type %s opening",
								o.getMessage(), o.getType())));
		driver.get(baseURL);
	}

	@Test
	public void acceptTest() {
		element = findButton();
		element.sendKeys(Keys.ENTER);
		sleep(100);
		alert = driver.switchTo().alert();
		// Assert alert displayed
		assertThat(alert, notNullValue());
		sleep(1000);
		if (debug)
			System.err.println("Selenium accepting alert:");
		// assert that dialog was accepted
		chromeDevTools.addListener(Page.javascriptDialogClosed(),
				o -> assertThat(o.getResult(), is(true)));

		alert.accept();
	}

	@Test
	public void dismissTest() {
		chromeDevTools.send(Page.reload(Optional.of(true), Optional.empty()));
		element = findButton();
		element.click();
		sleep(100);
		alert = wait.until(ExpectedConditions.alertIsPresent());
		// Assert alert displayed
		assertThat(alert, notNullValue());
		sleep(1000);
		if (debug)
			System.err.println("Selenium dismissing alert.");
		// assert that dialog was canceled
		chromeDevTools.addListener(Page.javascriptDialogClosed(),
				o -> assertThat(o.getResult(), is(false)));

		alert.dismiss();
	}
}
