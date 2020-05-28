package com.github.sergueik.selenium;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chromium.ChromiumDriver;

public class Utils {
	private static String osName;
	private static ChromiumDriver driver;
	private static JavascriptExecutor js;
	private static long highlightInterval = 100;
	private static boolean debug = false;

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
		highlight(element, highlightInterval, "solid yellow");
	}

	public static void highlight(WebElement element, long highlightInterval) {
		highlight(element, highlightInterval, "solid yellow");
	}

	public static void highlight(WebElement element, long highlightInterval, String color) {
		try {
			js.executeScript(String.format("arguments[0].style.border='3px %s'", color), element);
			Thread.sleep(highlightInterval);
			js.executeScript("arguments[0].style.border=''", element);
		} catch (InterruptedException e) {
			// err.println("Exception (ignored): " + e.toString());
		}
	}

	public static String getPageContent(String pagename) {
		try {
			URI uri = Utils.class.getClassLoader().getResource(pagename).toURI();
			System.err.println("Testing local file: " + uri.toString());
			return uri.toString();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	// http://www.javawithus.com/tutorial/using-ellipsis-to-accept-variable-number-of-arguments
	public static Object executeScript(String script, Object... arguments) {
		return js.executeScript(script, arguments);
	}

	public static String processExceptionMessage(String message) {
		return processExceptionMessage(message, false);
	}

	public static String processExceptionMessage(String message, boolean flag) {
		Pattern p = Pattern.compile("(\\{.*\\})", Pattern.MULTILINE);
		Matcher m = p.matcher(message);
		List<String> messages = new ArrayList<>();
		while (m.find()) {
			messages.add(m.group(1));
		}
		return flag ? messages.get(0) : String.join("\n", messages);
	}

	// based on: https://stackoverflow.com/questions/672916/how-to-get-image
	// https://stackoverflow.com/questions/1780385/java-hashmapstring-int-not-working
	public static Map<String, Integer> getImageDimension(String filename) {
		if (debug) {
			System.err.println("get image dimentions for: " + filename);
		}
		ImageReader reader = null;
		Map<String, Integer> result = new HashMap<>();
		try {
			reader = ImageIO.getImageReadersBySuffix(filename.replaceFirst(".*\\.", "")).next();
			reader.setInput(new FileImageInputStream(new File(filename)));
			int index = reader.getMinIndex();
			result.put("width", reader.getWidth(index));
			result.put("height", reader.getHeight(index));
		} catch (IOException e) {
			System.err.println("Error (ignored): " + e.toString());
			// e.g. javax.imageio.IIOException: Not a JPEG file: starts with 0x89 0x50
		} finally {
			reader.dispose();
		}
		return result;
	}

}
