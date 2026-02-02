package com.github.sergueik.selenium;

/**
 * Copyright 2023-2025 Serguei Kouzmine
 */
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
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
import org.openqa.selenium.devtools.v144.page.Page;
import org.openqa.selenium.devtools.v144.page.model.JavascriptDialogClosed;
import org.openqa.selenium.devtools.v144.page.model.JavascriptDialogOpening;
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

public class AlertDevToolsLocalHostTest extends BaseDevToolsTest {

	private final String text = "Lorem ipsum";
	protected static WebDriverWait wait;
	public final static int flexibleWait = 10;
	public final static Duration duration = Duration.ofSeconds(flexibleWait);
	public final static int implicitWait = 1;
	private final String page = "tryit1.html";
	public final static int pollingInterval = 500;
	protected Alert alert = null;
	protected static String name = null;
	protected static WebElement element;
	protected static List<WebElement> elements;

	@Before
	public void before() {
		wait = new WebDriverWait(driver, duration);
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
	}

	@After
	public void after() {
		chromeDevTools.clearListeners();
		Utils.stopLocalHttpServer(1);
		Utils.sleep(3000);
		driver.get("about:blank");
	}

	// @Ignore
	@Test
	public void test1() {
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
		System.err.println("Started local test 1 - accept alert.");
		driver.get(Utils.getLocallyHostedPageContent(page));
		// System.err.println(driver.getPageSource());
		element = driver.findElement(By.tagName("button"));

		element.sendKeys(Keys.ENTER);
		//
		// assertThat(element.getDomAttribute("id"), notNullValue());
		Utils.sleep(100);
		alert = driver.switchTo().alert();
		// Assert alert displayed
		assertThat("alert expected to be displayed", alert, notNullValue());
		// https://github.com/SeleniumHQ/selenium/blob/trunk/common/devtools/chromium.v99/browser_protocol.pdl#L7715
		System.err.println("accepting alert with Selenium accept().");
		alert.accept();
		element = driver.findElement(By.id("demo"));
		System.err.println(element.getAttribute("innerHTML"));
	}

	// @Ignore
	@Test
	public void test2() {
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
		System.err.println("Started local test 2 - dismiss alert.");
		driver.get(Utils.getLocallyHostedPageContent(page));
		// chromeDevTools.send(Page.reload(Optional.of(true), Optional.empty()));
		// System.err.println(driver.getPageSource());
		element = driver.findElement(By.tagName("button"));
		// assertThat(element.getDomAttribute("id"), notNullValue());
		element.click();
		Utils.sleep(100);
		alert = wait.until(ExpectedConditions.alertIsPresent());
		// Assert alert displayed
		assertThat(alert, notNullValue());
		Utils.sleep(3000);
		System.err.println("dismissing alert with Selenium dismiss().");
		alert.dismiss();
		// TODO: assert that dialog was canceled
		element = driver.findElement(By.id("demo"));
		System.err.println(element.getAttribute("innerHTML"));
		assertThat(element.getAttribute("innerHTML"), is(""));
	}

	@Test
	public void test3() {
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
					System.err.println("Dialog user input was: " + event.getUserInput());
				});
		// assert that dialog was accepted
		chromeDevTools.addListener(Page.javascriptDialogClosed(),
				(JavascriptDialogClosed event) -> assertThat(event.getResult(),
						is(true)));
		// Act
		System.err.println("Started local test 3 - javascript prompt.");
		driver.get(Utils.getLocallyHostedPageContent("tryjsref_prompt.html"));
		// System.err.println(driver.getPageSource());
		element = driver.findElement(By.tagName("button"));
		// Act
		element.click();
		Utils.sleep(100);
		alert = wait.until(ExpectedConditions.alertIsPresent());
		// Assert
		assertThat(alert, notNullValue());
		System.err.println("Selenium entering text: " + text);
		// Act
		alert.sendKeys(text);
		Utils.sleep(100);
		// NOTE: alert.sendKeys expects a String argument not Keys
		// alert.sendKeys(Keys.RETURN);
		System.err.println("accepting alert");
		alert.accept();
		element = driver.findElement(By.id("demo"));
		System.err.println(element.getAttribute("innerHTML"));
		assertThat(element.getAttribute("innerHTML"), containsString(text));
	}

}
