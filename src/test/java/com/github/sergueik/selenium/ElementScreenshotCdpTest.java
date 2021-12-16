package com.github.sergueik.selenium;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge inspired
 * by https://toster.ru/q/c653249?e=7897302#comment_1962398
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class ElementScreenshotCdpTest extends BaseCdpTest {
	private static String command = null;
	private static Map<String, Object> result = new HashMap<>();
	private static Map<String, Object> params = new HashMap<>();
	private static String dataString = null;
	public static Long nodeId = (long) -1;
	public static String isolationId = null;

	@Test
	// based on:
	// https://qna.habr.com/q/732307
	// https://github.com/sahajamit/chrome-devtools-webdriver-integration/blob/master/src/test/java/com/sahajamit/DemoTests.java
	// https://chromedevtools.github.io/devtools-protocol/tot/Page#method-captureScreenshot
	// https://chromedevtools.github.io/devtools-protocol/tot/Page#type-Viewport
	public void test1() {

		// basic logo example
		// baseURL = "https://www.google.com/";
		// String xpath = "//img[@id = 'hplogo'][@alt='Google']";

		// schedule of classes for today
		LocalDate localDate = LocalDate.now();
		Year year = Year.from(localDate);
		Month month = Month.from(localDate);
		MonthDay monthDay = MonthDay.now();
		baseURL = String.format(
				"http://almetpt.ru/%s/site/schedulegroups/0/1/%s-%02d-%02d",
				year.toString(), year.toString(), month.getValue(),
				monthDay.getDayOfMonth());
		baseURL = "http://almetpt.ru/2020/site/schedulegroups/0/1/2020-03-02";
		String xpath = "//div[@class=\"card-columns\"]//div[contains(@class, \"card\")]"
				+ "[div[contains(@class, \"card-header\")]]";
		driver.get(baseURL);
		result = null;
		dataString = null;
		// not assigning the value returned
		wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.xpath("//div[@class=\"card-columns\"]")));
		List<WebElement> elements = driver.findElements(By.xpath(xpath));
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
			// TODO: devtools variant
			// aee also
			// https://stackoverflow.com/questions/1197172/how-can-i-take-a-screenshot-image-of-a-website-using-python
			command = "Page.captureScreenshot";
			params = new HashMap<String, Object>();
			Map<String, Object> viewport = new HashMap<>();
			System.err.println("Specified viewport: " + String
					.format("x=%d, y=%d, width=%d, height=%d", x, y, width, height));
			viewport.put("x", (double) x);
			viewport.put("y", (double) y);
			viewport.put("width", (double) width);
			viewport.put("height", (double) height);
			viewport.put("scale", scale);
			params.put("clip", viewport);
			try {
				// Act
				result = driver.executeCdpCommand(command, params);
				// Assert
				assertThat(result, notNullValue());
				assertThat(result, hasKey("data"));
				dataString = (String) result.get("data");
				assertThat(dataString, notNullValue());
			} catch (WebDriverException e) {
				System.err.println("Web Driver exception in " + command + " (ignored): "
						+ Utils.processExceptionMessage(e.getMessage()));
			} catch (Exception e) {
				System.err.println("Exception in " + command + "  " + e.toString());
				throw (new RuntimeException(e));
			}

			Base64 base64 = new Base64();
			byte[] image = base64.decode(dataString);
			try {
				BufferedImage o = ImageIO.read(new ByteArrayInputStream(image));
				assertThat(o.getWidth(), greaterThan(0));
				assertThat(o.getHeight(), greaterThan(0));
			} catch (IOException e) {
				System.err
						.println("Exception loading image (	ignored): " + e.toString());
			}
			String screenshotFileName = String.format("card%02d.png", cnt);
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
