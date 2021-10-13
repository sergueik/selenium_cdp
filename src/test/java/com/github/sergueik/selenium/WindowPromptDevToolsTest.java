package com.github.sergueik.selenium;

/**
 * Copyright 2020 Serguei Kouzmine
 */
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.Event;
import org.openqa.selenium.devtools.v93.page.Page;
import org.openqa.selenium.devtools.v93.page.model.JavascriptDialogClosed;
import org.openqa.selenium.devtools.v93.runtime.Runtime;
import org.openqa.selenium.devtools.v93.runtime.Runtime.EvaluateResponse;
import org.openqa.selenium.devtools.v93.runtime.model.ExecutionContextId;
import org.openqa.selenium.devtools.v93.runtime.model.RemoteObject;
import org.openqa.selenium.devtools.v93.runtime.model.TimeDelta;
import org.openqa.selenium.json.JsonException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * https://chromedevtools.github.io/devtools-protocol/1-3/Runtime/#method-evaluate
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#event-javascriptDialogOpening
 * 
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class WindowPromptDevToolsTest extends EventSubscriptionCommonTest {
	private final String text = "Lorem ipsum";
	private final static String baseURL = "https://www.w3schools.com/jsref/tryit.asp?filename=tryjsref_prompt";
	// NOTE: fragile
	// org.openqa.selenium.NoAlertPresentException

	@Before
	public void before() {
		chromeDevTools.createSession();
		chromeDevTools.send(Page.enable());

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
	}

	@Test
	public void promptTest() {
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
		// NOTE: salert.sendKeys expects String not Keys
		// alert.sendKeys(Keys.RETURN);
		alert.accept();

	}

}
