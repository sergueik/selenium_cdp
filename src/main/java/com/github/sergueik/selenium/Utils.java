package com.github.sergueik.selenium;

import static java.lang.System.err;

import java.time.Duration;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;

public class Utils {
	private static String osName;
	private static ChromiumDriver driver;
	private static JavascriptExecutor js;
	private static long highlightInterval = 100;

	public static void setDriver(ChromiumDriver data) {
		Utils.driver = data;
		if (driver instanceof JavascriptExecutor) {
			Utils.js = JavascriptExecutor.class.cast(driver);
		} else {
			throw new RuntimeException("Script executor initialization failed.");
		}
	}

	public static String getOSName() {
		if (osName == null) {
			osName = System.getProperty("os.name").toLowerCase();
			if (osName.startsWith("windows")) {
				osName = "windows";
			}
		}
		return osName;
	}

	// Utilities
	public static void sleep(Integer milliSeconds) {
		try {
			Thread.sleep((long) milliSeconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void highlight(WebElement element) {
		highlight(element, 100, "solid yellow");
	}

	public static void highlight(WebElement element, long highlightInterval) {
		highlight(element, highlightInterval, "solid yellow");
	}

	public static void highlight(WebElement element, long highlightInterval,
			String color) {
		try {
			js.executeScript(
					String.format("arguments[0].style.border='3px %s'", color), element);
			Thread.sleep(highlightInterval);
			js.executeScript("arguments[0].style.border=''", element);
		} catch (InterruptedException e) {
			// err.println("Exception (ignored): " + e.toString());
		}
	}

	// http://www.javawithus.com/tutorial/using-ellipsis-to-accept-variable-number-of-arguments
	public static Object executeScript(String script, Object... arguments) {
		return js.executeScript(script, arguments);
	}
}
