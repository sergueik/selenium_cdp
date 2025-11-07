package com.github.sergueik.selenium;

/**
 * Copyright 2021,2024 Serguei Kouzmine
 */


import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v139.page.Page;
import org.openqa.selenium.devtools.v139.page.model.FileChooserOpened;
import org.openqa.selenium.devtools.v139.page.model.FrameId;
import org.openqa.selenium.interactions.Actions;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * MOTE: does not work as documented:
 * Intercept file chooser requests and transfer control to protocol clients. 
 * When file chooser interception is enabled, native file chooser dialog is not shown. 
 * Instead, a protocol event Page.fileChooserOpened is emitted
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#event-fileChooserOpened
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-setInterceptFileChooserDialog
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class FileChooserDialogInterceptDevToolsTest extends BaseDevToolsTest {

	public String baseURL = "http://www.dev2qa.com/demo/upload/uploadFileTest.html";

	@Before
	public void before() throws Exception {
		driver.get(baseURL);
		chromeDevTools.send(Page.setInterceptFileChooserDialog(true, Optional.of(false)));
		// listen file chooser dialog events
		chromeDevTools.addListener(Page.fileChooserOpened(),
				(FileChooserOpened event) -> {
					FrameId frameId = event.getFrameId();
					System.err.println("Event from frame: " + frameId.toJson());
				});
	}

	@After
	public void after() {

		chromeDevTools.send(Page.setInterceptFileChooserDialog(false, Optional.of (false)));
		chromeDevTools.clearListeners();
	}

	@Test
	public void test() {
		// Arrange
		WebElement element = driver
				.findElement(By.cssSelector("input[name='uploadFileInputBox']"));
		assertThat(element, notNullValue());
		Utils.highlight(element, 1000);
		Utils.sleep(1000);
		Actions actions = new Actions(driver);
		actions.moveToElement(element).click(element).build().perform();
		// org.openqa.selenium.InvalidArgumentException: invalid argument
		// element.click();
		// org.openqa.selenium.InvalidArgumentException: invalid argument
		// element.sendKeys(Keys.ENTER);
		// Input the uploaded file's absolute file path to the upload file web
		// element as string use sendKeys() method.
		Utils.sleep(1000);
	}

}
