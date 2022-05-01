package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.v100.page.Page;
import org.openqa.selenium.devtools.v100.page.model.Viewport;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge inspired
 * by https://toster.ru/q/c653249?e=7897302#comment_1962398
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class ElementScreenshotDevToolsTest extends BaseDevToolsTest {

	private static List<WebElement> elements = new ArrayList<>();
	private static String dataString = null;
	public static Long nodeId = (long) -1;
	public static String isolationId = null;
	private static WebDriverWait wait;
	private static int flexibleWait = 60;
	private static int pollingInterval = 500;
	private static Viewport viewport;
	private final String xpath = "//div[@class=\"card-columns\"]//div[contains(@class, \"card\")]"
			+ "[div[contains(@class, \"card-header\")]]";
	private static Base64 base64 = new Base64();
	private static byte[] image;

	@Before
	public void before() throws Exception {
		wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		Utils.setDriver(driver);
		wait.pollingEvery(Duration.ofMillis(pollingInterval));
		LocalDate localDate = LocalDate.now();
		Year year = Year.from(localDate);
		Month month = Month.from(localDate);
		MonthDay monthDay = MonthDay.now();
		baseURL = String.format(
				"http://almetpt.ru/%s/site/schedulegroups/0/1/%s-%02d-%02d",
				year.toString(), year.toString(), month.getValue(),
				monthDay.getDayOfMonth());
		baseURL = "http://almetpt.ru/2020/site/schedulegroups/0/1/2020-03-02";
		driver.get(baseURL);
	}

	@Test
	// https://chromedevtools.github.io/devtools-protocol/tot/Page#method-captureScreenshot
	// https://chromedevtools.github.io/devtools-protocol/tot/Page#type-Viewport
	// see also:
	// https://qna.habr.com/q/732307
	// https://github.com/sahajamit/chrome-devtools-webdriver-integration/blob/master/src/test/java/com/sahajamit/DemoTests.java
	// see also
	// https://stackoverflow.com/questions/1197172/how-can-i-take-a-screenshot-image-of-a-website-using-python
	public void test1() {

		dataString = null;
		// not assigning the value returned
		wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.xpath("//div[@class=\"card-columns\"]")));
		elements = driver.findElements(By.xpath(xpath));
		int cnt = 0;
		int maxCnt = 10;
		cards: for (WebElement element : elements) {
			if (null == element
					.findElement(By.xpath("div[contains(@class, \"card-body\")]"))) {
				continue;
			}
			cnt++;
			if (cnt >= maxCnt) {
				break cards;
			}
			Utils.highlight(element);
			int x = element.getLocation().getX();
			int y = element.getLocation().getY();
			int width = element.getSize().getWidth();
			int height = element.getSize().getHeight();
			int scale = 1;
			viewport = new Viewport(x, y, width, height, scale);
			dataString = chromeDevTools.send(
					// @formatter:off
					Page.captureScreenshot(
							Optional.of(Page.CaptureScreenshotFormat.JPEG), 
							Optional.of(100),
							Optional.of(viewport), 
							Optional.of(true), 
							Optional.of(true)
					)
					// @formatter:off
			);
			
			assertThat(dataString, notNullValue());


			image = base64.decode(dataString);
			try {
				BufferedImage o = ImageIO.read(new ByteArrayInputStream(image));
				assertThat(o.getWidth(), greaterThan(0));
				assertThat(o.getHeight(), greaterThan(0));
			} catch (IOException e) {
				System.err
						.println("Exception loading image (	ignored): " + e.toString());
			}
			String screenshotFileName = String.format("card_devtools_%02d.png", cnt);
			try {
				FileOutputStream fileOutputStream = new FileOutputStream(
						screenshotFileName);
				fileOutputStream.write(image);
				fileOutputStream.close();
			} catch (IOException e) {
				System.err.println("Exception saving image (ignored): " + e.toString());
			}
		}
	}
}
