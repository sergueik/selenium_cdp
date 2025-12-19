package com.github.sergueik.selenium;

/**
 * Copyright 2023,2024 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v143.emulation.Emulation;
import org.openqa.selenium.devtools.v143.emulation.model.DisabledImageType;
import org.openqa.selenium.interactions.Actions;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setDisabledImageTypes
 * https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#type-DisabledImageType
 * see also:
 * https://www.lambdatest.com/blog/find-broken-images-using-selenium-webdriver/
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class WebpDevToolsTest extends BaseDevToolsTest {
	private static String baseURL = "https://developers.google.com/speed/webp/gallery1";
	private Actions actions;
	private WebElement element;
	private static int delay = 3000;

	@Test
	public void test1() throws UnsupportedEncodingException {
		// Arrange
		DisabledImageType type = DisabledImageType.WEBP;
		// @formatter:off
		chromeDevTools.send(
				Emulation.setDisabledImageTypes(
						Arrays.asList(type)) // imageTypes
				);
		// @formatter:on
		// Act
		driver.get(baseURL);
		element = driver.findElement(By.xpath("//img[@alt='WebP Image']"));
		assertThat(element, notNullValue());

		assertThat(element.isDisplayed(), is(true));
		actions = new Actions(driver);
		actions.moveToElement(element).build().perform();

		Utils.highlight(element);
		Utils.sleep(delay);
		// identify as Broken Image
		assertThat(element.getAttribute("naturalWidth"), is("0"));
		assertThat(element.getSize().width, greaterThan(0)); // 95
		assertThat(
				(int) Math.ceil(
						Float.parseFloat(element.getCssValue("width").replace("px", ""))),
				greaterThan(0)); // "94.75px"
	}

}
