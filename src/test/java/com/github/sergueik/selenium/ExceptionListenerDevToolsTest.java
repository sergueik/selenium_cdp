package com.github.sergueik.selenium;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.openqa.selenium.devtools.idealized.Domains;
import org.openqa.selenium.devtools.idealized.Events;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * based on https://www.selenium.dev/documentation/webdriver/bidirectional/bidi_api/#listen-to-js-exceptions
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class ExceptionListenerDevToolsTest extends BaseDevToolsTest {

	private static WebElement element;
	private static WebDriverWait wait;
	private List<JavascriptException> jsExceptions = new ArrayList<>();

	@After
	public void after() throws Exception {
		// NOTE:
		// catch and retry if getting ConcurrentModificationException
		boolean done = false;
		while (!done) {
			try {
				for (JavascriptException e : jsExceptions) {
					System.out.println("Javascript exception message: " + e.getMessage()
							+ "\n" + "System information: " + e.getSystemInformation() + "\n"
							+ "Stack trace:" + "\n");
					e.printStackTrace();
				}
				done = true;
			} catch (ConcurrentModificationException e) {
				done = false;
			}
		}
		chromeDevTools.getDomains().events().disable();
	}

	@Before
	public void before() throws Exception {
		Consumer<JavascriptException> addEntry = jsExceptions::add;
		chromeDevTools.getDomains().events()
				.addJavascriptExceptionListener(addEntry);
		driver.get("https://www.wikipedia.org");
	}

	@Test
	public void test1() {
		element = driver.findElement(By.tagName("img"));
		// NOTE: no semicolon at the end of the script argument is OK
		addOnClick(element, "throw new Error('test1')");
		try {
			element.click();
		} catch (ElementClickInterceptedException e) {
			// the "element is not clickable at point" exception
			// message will include the target
			// element HTML which will include the
			// onclick="throw new Error('test1')"
			// injected handler
			System.err.println("Exception(ignored) " + e.toString());
			assertThat(e.toString(), containsString("test1"));
		}
	}

	@Test
	public void test2() {
		element = driver.findElement(By.cssSelector("#js-link-box-en"));
		addOnClick(element, "throw new Error('test2');");
		try {
			element.click();
		} catch (ElementClickInterceptedException e) {
			System.err.println("Exception(ignored) " + e.toString());
		}
	}

	// NOTE: the var titleState is intentionally not declared
	// to trigger
	// java.util.ConcurrentModificationException
	// is the @After method printing exceptions
	// presumably because new exceptions are thrown
	// when setInterval is handler is called
	@Test
	public void test3() {
		element = driver.findElement(By.cssSelector("#js-link-box-en"));
		String blinkTitlescrpit = "setInterval(() => { "
				+ " document.title = titleState? \"blinking\" : \"title\"; titleState = "
				+ " titleState ? 0 : 1 ; } , 500 );";

		addOnClick(element, blinkTitlescrpit + "return false;");
		try {
			element.click();
		} catch (ElementClickInterceptedException e) {
			System.err.println("Exception(ignored) " + e.toString());
		}
		Utils.sleep(3000);
	}

	// see also: https://qna.habr.com/q/1089060
	@Test
	public void test4() {
		element = driver.findElement(By.cssSelector("#js-link-box-en"));
		String blinkTitlescrpit = "var titleState = 0; setInterval(() => { "
				+ " document.title = titleState ? \"blinking\" : \"title\"; titleState = "
				+ " titleState ? 0 : 1; } , 500 );";

		addOnClick(element, blinkTitlescrpit + "return false;");
		try {
			element.click();
		} catch (ElementClickInterceptedException e) {
			System.err.println("Exception(ignored) " + e.toString());
		}
		Utils.sleep(3000);
	}

	public Object executeScript(String script, Object... arguments) {
		if (driver instanceof JavascriptExecutor) {
			JavascriptExecutor javascriptExecutor = JavascriptExecutor.class
					.cast(driver);
			return javascriptExecutor.executeScript(script, arguments);
		} else {
			throw new RuntimeException("Script execution failed.");
		}
	}

	private void addOnClick(WebElement element, String script) {
		executeScript("arguments[0].setAttribute(arguments[1], arguments[2]);",
				element, "onclick", script);

	}
}
