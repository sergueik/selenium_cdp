package com.github.sergueik.selenium;
/**
 * Copyright 2022,2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Optional;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.commons.codec.binary.Base64;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.text.PDFTextStripper;

import com.github.sergueik.selenium.PrintToPDFCDPTest.PDF;
import com.google.common.collect.ImmutableMap;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;

import org.openqa.selenium.devtools.Command;
import org.openqa.selenium.devtools.ConverterFunctions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;

import org.openqa.selenium.devtools.v114.page.Page;
import org.openqa.selenium.devtools.v114.page.Page.PrintToPDFResponse;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge
 * 
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-printToPDF
 * for page dimensions see https://papersizes.io/a/a4
 * https://css.paperplaza.net/conferences/support/page.php
 * see also:
 * https://stackoverflow.com/questions/20904191/pdfbox-find-page-dimensions
 */
// NOTE: not extending BaseDevToolsTest

public class PrintToPDFDevToolsTest {

	private static boolean keepFile = true;
	private static String magic = null;
	private static boolean runHeadless = true;
	private static PrintToPDFResponse response;
	private static String body = null;
	private static boolean landscape = false;
	private static boolean displayHeaderFooter = false;
	private static boolean printBackground = false;
	private static Page.PrintToPDFTransferMode transferMode = Page.PrintToPDFTransferMode.RETURNASBASE64;
	private static int scale = 1;
	private static String filename = null;
	private static String osName = Utils.getOSName();
	private static ChromiumDriver driver;
	private static DevTools chromeDevTools;
	private static String baseURL = "https://www.wikipedia.org";

	@BeforeClass
	public static void setUp() throws Exception {

		if (System.getenv().containsKey("HEADLESS")
				&& System.getenv("HEADLESS").matches("(?:true|yes|1)")) {
			runHeadless = true;
		}
		// force the headless flag to be true to support Unix console execution
		if (!(Utils.getOSName().equals("windows"))
				&& !(System.getenv().containsKey("DISPLAY"))) {
			runHeadless = true;
		}
		System
				.setProperty("webdriver.chrome.driver",
						Paths.get(System.getProperty("user.home"))
								.resolve("Downloads").resolve(osName.equals("windows")
										? "chromedriver.exe" : "chromedriver")
								.toAbsolutePath().toString());

		if (runHeadless) {
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--headless", "--disable-gpu");
			driver = new ChromeDriver(options);
		} else {
			driver = new ChromeDriver();
		}
		Utils.setDriver(driver);

		chromeDevTools = ((HasDevTools) driver).getDevTools();

		chromeDevTools.createSession();
		// TODO: switch to
		// chromeDevTools.createSessionIfThereIsNotOne();
	}

	@AfterClass
	public static void afterClass() {
		if (driver != null) {
			driver.quit();
		}
	}

	@After
	public void afterTest() {
		if (!keepFile)
			new File(System.getProperty("user.dir") + "/" + filename).delete();
		driver.get("about:blank");
	}

	@Before
	public void beforeTest() {
		// Arrange
		driver.get(baseURL);
	}

	//
	@Test
	public void test2() {
		//
		transferMode = Page.PrintToPDFTransferMode.RETURNASBASE64;
		// Act
		response = chromeDevTools.send(Page.printToPDF(Optional.of(landscape),
				Optional.of(displayHeaderFooter), Optional.of(printBackground),
				Optional.of(scale), Optional.empty(/* paperWidth */),
				Optional.empty(/* paperHeight */), Optional.empty(/* marginTop	*/),
				Optional.empty(/* marginBottom */), Optional.empty(/* marginLeft */),
				Optional.empty(/* marginRight */), Optional.empty(/* pageRanges */),
				Optional.empty(/* headerTemplate */),
				Optional.empty(/* footerTemplate */),
				Optional.of(true /* preferCSSPageSize */), Optional.of(
						transferMode /* Allowed Values: ReturnAsBase64, ReturnAsStream */ )));
		assertThat(response, notNullValue());
		try {
			body = new String(
					Base64.decodeBase64(response.getData().getBytes("UTF8")));
			assertThat(body, notNullValue());
			magic = body.substring(0, 9);
			assertThat(magic, containsString("%PDF"));

			filename = "result3.pdf";
			writeToFile(Base64.decodeBase64(response.getData().getBytes("UTF8")),
					filename);
		} catch (UnsupportedEncodingException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}

	}

	// verify paper size is Letter
	// NOTE: top margin is different on the first page and the rest
	// (not attempted in this test)
	@Test
	public void test3() {
		// Act
		response = chromeDevTools.send(new Command<PrintToPDFResponse>(
				"Page.printToPDF", ImmutableMap.of("landscape", landscape),
				o -> o.read(PrintToPDFResponse.class)));
		assertThat(response, notNullValue());

		try {
			body = new String(
					Base64.decodeBase64(response.getData().getBytes("UTF8")));
			assertThat(body, notNullValue());
			magic = body.substring(0, 9);
			assertThat(magic, containsString("%PDF"));
			filename = "result4.pdf";
			writeToFile(Base64.decodeBase64(response.getData().getBytes("UTF8")),
					filename);

			PDF pdf = new PDF(
					new File(System.getProperty("user.dir") + "/" + filename));
			assertThat(pdf.text, containsString("The Free Encyclopedia"));
			// NOTE: locale UTF8
			assertThat(pdf.text, containsString("Русский"));
			assertThat(pdf.text, containsString("Français"));
			assertThat(pdf.encrypted, is(false));
			assertThat(pdf.numberOfPages, equalTo(2));
			assertThat(pdf.getHeight(), equalTo(11.0));
			assertThat(pdf.getWidth(), equalTo(8.5));

		} catch (UnsupportedEncodingException e) {
			System.err.println("Exception (ignored): " + e.toString());
		} catch (IOException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}

	}

	@Ignore
	// Unable to create instance of class
	// org.openqa.selenium.devtools.v114.page.Page$PrintToPDFResponse
	// Caused by: org.openqa.selenium.json.JsonException: Expected to read a
	// START_MAP
	// but instead have: STRING. Last 26 characters read:
	// {"id":11,"result":{"data":
	@Test
	public void test4() {
		// Act
		filename = "result5.pdf";
		response = chromeDevTools.send(new Command<PrintToPDFResponse>(
				"Page.printToPDF", ImmutableMap.of("landscape", landscape),
				ConverterFunctions.map("data", PrintToPDFResponse.class)));
		assertThat(response, notNullValue());
		try {
			body = new String(
					Base64.decodeBase64(response.getData().getBytes("UTF8")));
			assertThat(body, notNullValue());
			magic = body.substring(0, 9);
			assertThat(magic, containsString("%PDF"));
			writeToFile(Base64.decodeBase64(response.getData().getBytes("UTF8")),
					filename);

		} catch (UnsupportedEncodingException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}

	}

	// print A4 (default is Letter)
	// for page dimensions see https://papersizes.io/a/a4
	// https://css.paperplaza.net/conferences/support/page.php
	// NOTE: top margin is different on the first page and the rest
	// (not attempted in this test)
	@Test
	public void test5() {
		// Act
		response = chromeDevTools.send(new Command<PrintToPDFResponse>(
				"Page.printToPDF", ImmutableMap.of("landscape", landscape),
				o -> o.read(PrintToPDFResponse.class)));
		filename = "result6.pdf";
		response = chromeDevTools.send(Page.printToPDF(Optional.of(landscape),
				Optional.of(displayHeaderFooter), Optional.of(printBackground),
				Optional.of(scale), Optional.of(8.27), Optional.of(11.69),
				Optional.of(1.0), Optional.of(1.44), Optional.of(0.75),
				Optional.of(0.52), Optional.empty(/* pageRanges */),
				Optional.empty(/* headerTemplate */),
				Optional.empty(/* footerTemplate */),
				Optional.of(true /* preferCSSPageSize */), Optional.of(
						transferMode /* Allowed Values: ReturnAsBase64, ReturnAsStream */ )));

		assertThat(response, notNullValue());

		try {
			body = new String(
					Base64.decodeBase64(response.getData().getBytes("UTF8")));
			assertThat(body, notNullValue());
			magic = body.substring(0, 9);
			assertThat(magic, containsString("%PDF"));
			writeToFile(Base64.decodeBase64(response.getData().getBytes("UTF8")),
					filename);

			PDF pdf = new PDF(
					new File(System.getProperty("user.dir") + "/" + filename));
			assertThat(pdf.text, containsString("The Free Encyclopedia"));
			// NOTE: locale UTF8
			assertThat(pdf.text, containsString("Русский"));
			assertThat(pdf.text, containsString("Français"));
			assertThat(pdf.encrypted, is(false));
			assertThat(pdf.creator, is("Chromium"));
			assertThat(pdf.numberOfPages, equalTo(2));
			assertThat(pdf.getHeight(), equalTo(11.69));
			assertThat(pdf.getWidth(), equalTo(8.26));
		} catch (UnsupportedEncodingException e) {
			System.err.println("Exception (ignored): " + e.toString());
		} catch (IOException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}

	}
	/*
	 * response = chromeDevTools.send(new
	 * Command<PrintToPDFResponse>("Page.printToPDF",
	 * ImmutableMap.of("landscape", landscape),
	 * ImmutableMap.of("displayHeaderFooter", displayHeaderFooter),
	 * ImmutableMap.of("printBackground", printBackground),
	 * ImmutableMap.of("scale", scale), ImmutableMap.of("paperWidth", null),
	 * ImmutableMap.of("paperWidth", null), ImmutableMap.of("marginTop", null),
	 * ImmutableMap.of("marginBottom", null), ImmutableMap.of("marginLeft",
	 * null), ImmutableMap.of("marginRight", null),
	 * ImmutableMap.of("pageRanges", null),
	 * ImmutableMap.of("headerTemplate", null),
	 * ImmutableMap.of("footerTemplate", null),
	 * ImmutableMap.of("preferCSSPageSize", 0), ImmutableMap.of("transferMode",
	 * transferMode), ConverterFunctions.map("data",
	 * PrintToPDFResponse.class)));
	 */


	private void writeToFile(byte[] data, String fileName) {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(filename);
			DataOutputStream out = new DataOutputStream(fileOutputStream);
			out.write(data);
			out.close();
		} catch (Exception e) {
			System.err.println("Exception saving file " + e.toString());
			e.printStackTrace();
			throw (new RuntimeException(e));
		}
	}

	// add org.apache.pdfbox.text.PDFTextStripper
	// to inspect the PDF contents
	// origin: https://github.com/codeborne/pdf-test
	public static class PDF {
		public final byte[] content;

		public final String text;
		public final int numberOfPages;
		public final String author;
		public final String creator;
		public final String keywords;
		public final String producer;
		public final String subject;
		public final String title;

		public final boolean encrypted;
		public final boolean signed;
		public final String signerName;
		private double height;
		private double width;

		public double getHeight() {
			return height;
		}

		public double getWidth() {
			return width;
		}

		private PDF(String name, byte[] content) {
			this(name, content, 1, Integer.MAX_VALUE);
		}

		private PDF(String name, byte[] content, int startPage, int endPage) {
			this.content = content;

			try (InputStream inputStream = new ByteArrayInputStream(content)) {
				try (PDDocument pdf = PDDocument.load(inputStream)) {
					PDFTextStripper pdfTextStripper = new PDFTextStripper();
					pdfTextStripper.setStartPage(startPage);
					pdfTextStripper.setEndPage(endPage);
					this.text = pdfTextStripper.getText(pdf);
					this.numberOfPages = pdf.getNumberOfPages();
					this.author = pdf.getDocumentInformation().getAuthor();
					// this.creationDate = pdf.getDocumentInformation().getCreationDate();
					this.creator = pdf.getDocumentInformation().getCreator();
					this.keywords = pdf.getDocumentInformation().getKeywords();
					this.producer = pdf.getDocumentInformation().getProducer();
					this.subject = pdf.getDocumentInformation().getSubject();
					this.title = pdf.getDocumentInformation().getTitle();

					// find pdf page dimensions
					// https://stackoverflow.com/questions/20904191/pdfbox-find-page-dimensions
					float pageHeight = pdf.getPage(0).getMediaBox().getHeight() / 72;
					float pageWidth = pdf.getPage(0).getMediaBox().getWidth() / 72;

					// round down to 2 decimal places
					// http://www.java2s.com/example/java-utility-method/decimal-round-index-1.html
					NumberFormat format = NumberFormat.getInstance();
					format.setGroupingUsed(false);
					format.setMaximumFractionDigits(2);
					try {
						this.height = format.parse(format.format(pageHeight)).doubleValue();
					} catch (ParseException e) {
						this.height = pageHeight;
					}
					try {
						this.width = format.parse(format.format(pageWidth)).doubleValue();
					} catch (ParseException e) {
						this.width = pageWidth;
					}

					this.encrypted = pdf.isEncrypted();
					// https://pdfbox.apache.org/docs/2.0.3/javadocs/org/apache/pdfbox/pdmodel/common/PDRectangle.html

					PDSignature signature = pdf.getLastSignatureDictionary();
					this.signed = signature != null;
					this.signerName = signature == null ? null : signature.getName();
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid PDF file: " + name, e);
			}
		}

		public PDF(File pdfFile) throws IOException {
			this(pdfFile.getAbsolutePath(),
					Files.readAllBytes(Paths.get(pdfFile.getAbsolutePath())));
		}

		public PDF(URL url) throws IOException {
			this(url.toString(), readBytes(url));
		}

		public PDF(byte[] content) {
			this("", content);
		}

		public PDF(InputStream inputStream) throws IOException {
			this(readBytes(inputStream));
		}

		private static byte[] readBytes(URL url) throws IOException {
			try (InputStream inputStream = url.openStream()) {
				return readBytes(inputStream);
			}
		}

		private static byte[] readBytes(InputStream inputStream)
				throws IOException {
			ByteArrayOutputStream result = new ByteArrayOutputStream(2048);
			byte[] buffer = new byte[2048];

			int nRead;
			while ((nRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
				result.write(buffer, 0, nRead);
			}

			return result.toByteArray();
		}
	}
}
