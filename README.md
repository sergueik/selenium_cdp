### Info

The project practices Java Selenium __4.0.x__ release
[ChromiumDriver](https://github.com/SeleniumHQ/selenium/blob/master/java/client/src/org/openqa/selenium/chromium/ChromiumDriver.java)
to execute the [Chrome DevTools Protocol](https://chromedevtools.github.io/devtools-protocol/) a.k.a.
__cdp__ commands - an entirely different set of API communicated to the Chrome browser family via `POST` [requests](https://github.com/SeleniumHQ/selenium/blob/master/java/client/src/org/openqa/selenium/chromium/ChromiumDriverCommandExecutor.java) to `/session/$sessionId/goog/cdp/execute` with API-specific payload) feature (many of the cdp methods e.g. the [DOM](https://chromedevtools.github.io/devtools-protocol/tot/DOM) ones like

  * `performSearch`,
  * `getSearchResults`
  * `getNodeForLocation`
  * `getOuterHTML`
  * `querySelectorAll`
  * `querySelector`
  * `getAttributes`

overlap with classic Selenium in Classic Javascript
and there are few specific ones like:
  * `addCustomHeaders`
  * `getFrameTree`
  * `setGeolocationOverride`
  * `setDownloadBehavior`

to name a few, and various event listeners

This functionality is named in official Selenium Developer Documentation as [BiDirectional functionality](https://www.selenium.dev/documentation/webdriver/bidirectional/)
and [BiDi API](https://www.selenium.dev/documentation/webdriver/bidirectional/bidi_api/)

The project also exercised other new Selenium 4 API e.g. [relative nearby locators](https://dzone.com/articles/how-selenium-4-relative-locator-can-change-the-way) whidh did not apear powerful enough yet.

For accessing the __Chrome Devtools API__ with Selenium driver __3.x__ see [cdp_webdriver](https://github.com/sergueik/cdp_webdriver) project


### Examples

#### Async Code Execution by XHR Fetch events in the Browser

![xhr_test_capture.png](https://github.com/sergueik/selenium_cdp/blob/master/screenshots/xhr_test_capture.png)

This test is opening Wikipedia page and hovers over few links using "classic" Selenium `Actions` class:
```java
driver.findElement(By.id("mw-content-text")).findElements(By.tagName("a")).stream().forEach( (WebElement element ) -> {
    new Actions(driver).moveToElement(element).build().perform();
  }
}
```
To emphacise that the lambda operates "classic" object,the `WebElement` type was entered explicitly.

In the `@Before` -annotated method in the test class, the `Fetch` API is enabled
for all requests
```
@Before
public void beforeTest() throws Exception {
chromeDevTools = ((HasDevTools) driver).getDevTools();

List<RequestPattern> reqPattern = new ArrayList<>();
reqPattern.add(new RequestPattern(Optional.of("*"), Optional.of(ResourceType.XHR), Optional.of(RequestStage.RESPONSE)));
chromeDevTools.send(Fetch.enable(Optional.of(reqPattern), Optional.of(false)));
```
(If necessary one can limit to subset of reuests via match pattern).
Then in the test method callback is set up:

```java
@Test
public void test() {
	chromeDevTools.addListener(Fetch.requestPaused(),
		(RequestPaused event) -> {
      event.getResponseHeaders().get().stream().map((HeaderEntry entry) -> String.format("%s: %s",
              entry.getName(), entry.getValue())).collect(Collectors.toList());
      Fetch.GetResponseBodyResponse response = chromeDevTools.send(Fetch.getResponseBody(event.getRequestId()));
        String body = new String(Base64.decodeBase64(response.getBody().getBytes("UTF8")));
	System.err.println("response body:\n" + body);
      }
});
// he mouse hover actions to follow
```
This allows capture every Ajax request response headers,
```java
List<HeaderEntry> headerEntries = event.getResponseHeaders().isPresent() ? event.getResponseHeaders().get() : new ArrayList<>();
List<String> headers = headerEntries.stream().map(entry -> String.format("%s: %s", entry.getName(), entry.getValue())) .collect(Collectors.toList());
```
along with response status
```java
event.getResponseStatusCode().get()
```
and body which is usually a base64 encoded JSON with multiple details, processed by browser
![xhr_logged_capture.png](https://github.com/sergueik/selenium_cdp/blob/master/screenshots/xhr_logged_capture.png)
```java
Fetch.GetResponseBodyResponse response = chromeDevTools.send(Fetch.getResponseBody(event.getRequestId()));
String body = null;
if (response.getBase64Encoded()) {
	try {
		body = new String( Base64.decodeBase64(response.getBody().getBytes("UTF8")));
	} catch (UnsupportedEncodingException e) {
		System.err.println("Exception (ignored): " + e.toString());
	}
} else {
	body = response.getBody();
}
```
finally the test continues default processing  of the request:
```java
chromeDevTools.send(Fetch.continueRequest(
	event.getRequestId(),
	Optional.empty(),
	Optional.empty(),
	Optional.empty(),
	Optional.empty(),
	Optional.empty()));
```

- the arguments to the Java adapter method match the Javascript `Fetch.continueResponse` [parameter definition](https://chromedevtools.github.io/devtools-protocol/tot/Fetch/#method-continueResponse):

```text
requestId
RequestId
An id the client received in requestPaused event.
responseCode
integer
An HTTP response code. If absent, original response code will be used.
responsePhrase
string
A textual representation of responseCode. If absent, a standard phrase matching responseCode is used.
responseHeaders
array[ HeaderEntry ]
Response headers. If absent, original response headers will be used.
binaryResponseHeaders
string
Alternative way of specifying response headers as a \0-separated series of name: value pairs. Prefer the above method unless you need to represent some non-UTF8 values that can't be transmitted over the protocol as text. (Encoded as a base64 string when passed over JSON)
```
#### Access Browser Console Logs
Browser console logs may accessed asynchronuosly in asimilar fashion:
```java
@Before
public void beforeTest() throws Exception {
	chromeDevTools.send(Log.enable());
	chromeDevTools.addListener(Log.entryAdded(),
		(LogEntry event) -> System.err.println(
			String.format( "time stamp: %s line number: %s url: \"%s\" text: %s",
	formatTimestamp(event.getTimestamp()),
	(event.getLineNumber().isPresent() ? event.getLineNumber().get() : ""),
	(event.getUrl().isPresent() ? event.getUrl().get() : ""),
	event.getText())));
}
```
The properties of the event are taken from `Log entry` object [specification](https://chromedevtools.github.io/devtools-protocol/tot/Log/#event-entryAdded)
One can also confirm the logging event to have expected properties, e.g. message:

```java

@Test
public void test() {
	final String consoleMessage = "Lorem ipsum";
	chromeDevTools.addListener(Log.entryAdded(),
		(LogEntry event) -> assertThat(event.getText(), containsString(consoleMessage)));
	if (driver instanceof JavascriptExecutor) {
		JavascriptExecutor executor = JavascriptExecutor.class.cast(driver);		
		executor.executeScript("console.log(arguments[0]);", consoleMessage);
	}
}
```
#### Print to PDF

This API uses CDP command:
```java
public void test1() {
	PrintToPDFResponse response;
	boolean landscape = false;
	boolean displayHeaderFooter = false;
	boolean printBackground = false;
	Page.PrintToPDFTransferMode transferMode = Page.PrintToPDFTransferMode.RETURNASBASE64;
	int scale = 1;

	// Act
	response = chromeDevTools.send(Page.printToPDF(
	Optional.of(landscape),
	Optional.of(displayHeaderFooter),
	Optional.of(printBackground),
	Optional.of(scale),
	Optional.empty(),
	Optional.empty(),
	Optional.empty(),
	Optional.empty(),
	Optional.empty(),
	Optional.empty(),
	Optional.empty(),
	Optional.empty(),
	Optional.empty(),
	Optional.empty(),
	Optional.empty(),
	Optional.of(transferMode)));
	assertThat(response, notNullValue());
	String body = new String(Base64.decodeBase64(response.getData().getBytes("UTF8")));
	assertThat(body, notNullValue());
	String magic = body.substring(0, 9);
	assertThat(magic, containsString("%PDF"));
```
the browser needs to run headless mode for the call to succeed
the alternative call signature is
```java
response = chromeDevTools.send(new Command<PrintToPDFResponse>("Page.printToPDF", ImmutableMap.of("landscape", landscape), o -> o.read(PrintToPDFResponse.class)));
assertThat(response, notNullValue());


```
for some calls (but not specifically for `Page.printToPDF`) yet anoher alternavie signature via static method exists

```java
response = chromeDevTools.send(new Command<PrintToPDFResponse>("Page.printToPDF", ImmutableMap.of("landscape", landscape), ConverterFunctions.map("data", PrintToPDFResponse.class)));

```
#### Zoom the Browser window

in additon to *legacy*-like keyboard zoom, the CDP supports `Page.setDeviceMetricsOverride` [method](https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-setDeviceMetricsOverride) and `Emulation.setDeviceMetricsOverride` [method](https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setDeviceMetricsOverride):

```java
  @Before
  public void before() throws Exception {
    baseURL = "https://www.wikipedia.org";
    driver.get(baseURL);
  }

  @Test
  public void test1() {
    for (int cnt = 0; cnt != deviceScaleFactors.length; cnt++) {
      double deviceScaleFactor = deviceScaleFactors[cnt];
      screenshotFileName = String.format("test1_%03d.jpg",
          (int) (100 * deviceScaleFactor));
      layoutMetrics = chromeDevTools.send(Page.getLayoutMetrics());
      rect = layoutMetrics.getContentSize();
      width = rect.getWidth().intValue();
      height = rect.getHeight().intValue();
      System.err.println(String.format("Content size: %dx%d", width, height));
      chromeDevTools.send(
        // @formatter:off
        Emulation.setDeviceMetricsOverride(
          rect.getWidth().intValue(),
          rect.getHeight().intValue(),
          deviceScaleFactor,
          false,
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty()
        )
        // @formatter:on
      );
      String dataString = chromeDevTools.send(
        // @formatter:off
        Page.captureScreenshot(
            Optional.of(Page.CaptureScreenshotFormat.JPEG),
            Optional.of(100),
            Optional.empty(),
            Optional.of(true),
            Optional.of(true)
        )
        // @formatter:off
    );
    chromeDevTools.send(Emulation.clearDeviceMetricsOverride());

    byte[] image = base64.decode(dataString);
    try {
      BufferedImage o = ImageIO.read(new ByteArrayInputStream(image));
      System.err.println(String.format("Screenshot dimensions: %dx%d",
          o.getWidth(), o.getHeight()));
      assertThat((int) (width * deviceScaleFactor) - o.getWidth(),
          not(greaterThan(2)));
      assertThat((int) (height * deviceScaleFactor) - o.getHeight(),
          not(greaterThan(2)));
    } catch (IOException e) {
      System.err.println("Exception loading image (ignored): " + e.toString());
    }
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


@After
public void clearPage() {
  chromeDevTools.send(CSS.disable());
  try {
    chromeDevTools.send(DOM.disable());
  } catch (DevToolsException e) {
    // DOM agent hasn't been enabled
  }
  driver.get("about:blank");
}

```
this test gets gradually magnified out page screen shots:

![capture-multi-zoom.png](https://github.com/sergueik/selenium_cdp/blob/master/screenshots/capture-multi-zoom.png)


alternatively use CDP commands for the same:
```java
  @SuppressWarnings("unchecked")
  @Test
  public void test() {
    // Assert
    params = new HashMap<>();
    for (int cnt = 0; cnt != deviceScaleFactors.length; cnt++) {
      double deviceScaleFactor = deviceScaleFactors[cnt];
      filename = String.format("test2_%03d.jpg",
          (int) (100 * deviceScaleFactor));

      try {
        command = "Page.getLayoutMetrics";
        result = driver.executeCdpCommand(command, new HashMap<>());
        System.err
            .println("Page.getLayoutMetrics: " + result.get("contentSize"));
        rect = (Map<String, Long>) result.get("contentSize");
        height = rect.get("height");
        width = rect.get("width");
        command = "Emulation.setDeviceMetricsOverride";
        // Act
        System.err.println(String.format("Scaling to %02d%% %s",
            (int) (100 * deviceScaleFactor), filename));
        params.clear();
        params.put("deviceScaleFactor", deviceScaleFactor);
        params.put("width", width);
        params.put("height", height);
        params.put("mobile", false);
        params.put("scale", 1);
        driver.executeCdpCommand(command, params);

        Utils.sleep(delay);
        command = "Page.captureScreenshot";
        // Act
        result = driver.executeCdpCommand(command,
            new HashMap<String, Object>());

        command = "Emulation.clearDeviceMetricsOverride";
        driver.executeCdpCommand(command, new HashMap<String, Object>());

        // Assert
        assertThat(result, notNullValue());
        assertThat(result, hasKey("data"));
        dataString = (String) result.get("data");
        assertThat(dataString, notNullValue());

        byte[] image = base64.decode(dataString);
        BufferedImage o = ImageIO.read(new ByteArrayInputStream(image));
        assertThat(o.getWidth(), greaterThan(0));
        assertThat(o.getHeight(), greaterThan(0));
        FileOutputStream fileOutputStream = new FileOutputStream(filename);
        fileOutputStream.write(image);
        fileOutputStream.close();
      } catch (IOException e) {
        System.err.println("Exception saving image (ignored): " + e.toString());
      } catch (JsonSyntaxException e) {
        System.err.println("JSON Syntax exception in " + command
            + " (ignored): " + e.toString());
      } catch (WebDriverException e) {
        // willbe thrown if the required arguments are not provided.
        // TODO: add failing test
        System.err.println(
            "Web Driver exception in " + command + " (ignored): " + Utils
                .processExceptionMessage(e.getMessage() + "  " + e.toString()));
      } catch (Exception e) {
        System.err.println("Exception in " + command + "  " + e.toString());
        throw (new RuntimeException(e));
      }
    }
  }

```
#### Filter URL
![xhr_logged_capture.png](https://github.com/sergueik/selenium_cdp/blob/master/screenshots/filtering-on_capture.jpg)
Bandwidth improving filtering of certain mask URLs
```java
chromeDevTools.send(Network.enable(Optional.of(100000000), Optional.empty(), Optional.empty()));
chromeDevTools.send(Network.setBlockedURLs(ImmutableList.of("*.css", "*.png", "*.jpg", "*.gif", "*favicon.ico")));
driver.get("http://arngren.net");
```

one can also log the `*.css`, `*.jpg` `*.png` and  `*.ico` blocking in action:
```java
// verify that
chromeDevTools.addListener(Network.loadingFailed(),
	(LoadingFailed event) -> {
		ResourceType resourceType = event.getType();
		if (resourceType.equals(ResourceType.STYLESHEET)
				|| resourceType.equals(ResourceType.IMAGE)
				|| resourceType.equals(ResourceType.OTHER)) {
			Optional<BlockedReason> blockedReason = event.getBlockedReason();
			assertThat(blockedReason.isPresent(), is(true));
			assertThat(blockedReason.get(), is(BlockedReason.INSPECTOR));
		}
	System.err.println("Blocked event: " + event.getType());
});


```
finally one can disable filtering:
```java
// set request interception only for css requests
RequestPattern requestPattern = new RequestPattern(Optional.of("*.gif"), Optional.of(ResourceType.IMAGE), Optional.of(InterceptionStage.HEADERSRECEIVED));
chromeDevTools.send(Network.setRequestInterception(ImmutableList.of(requestPattern)));
chromeDevTools.send(Page.navigate(baseURL, Optional.empty(),Optional.empty(), Optional.empty(), Optional.empty()));
```
![xhr_logged_capture.png](https://github.com/sergueik/selenium_cdp/blob/master/screenshots/filtering-off_capture.jpg)


#### Override User Agent

One can __call__ cdp protocol to invoke [setUserAgentOverride](https://chromedevtools.github.io/devtools-protocol/tot/Network#method-setUserAgentOverride) method and dynmically modify the `user-agent` header during the test:

```java
  import org.openqa.selenium.chrome.ChromeDriver;
  import org.openqa.selenium.chromium.ChromiumDriver;

  ChromiumDriver driver = new ChromeDriver();
  driver.get("https://www.whoishostingthis.com/tools/user-agent/");
  By locator = By.cssSelector(".user-agent");
  WebElement element = driver.findElement(locato);
  assertThat(element.getAttribute("innerText"), containsString("Mozilla"));
  Map<String, Object> params = new HashMap<String, Object>();
  params.put("userAgent", "python 2.7");
  params.put("platform", "Windows");
  driver.executeCdpCommand("Network.setUserAgentOverride", params);
  driver.navigate().refresh();
  sleep(100);

  element = driver.findElement(locator);
  assertThat(element.isDisplayed(), is(true));
  assertThat(element.getAttribute("innerText"), is("python 2.7"));

```
demonstrates that the user-agent is indeed changing
#### Cookies
The example shows alternative API to collect the cookies available to page Javascript
```java
  Map<String, Object> result = driver.executeCdpCommand("Page.getCookies", new HashMap<String, Object>());
  ArrayList<Map<String, Object>> cookies = (ArrayList<Map<String, Object>>) result.get("cookies");
  cookies.stream().limit(100).map(o -> o.keySet()).forEach(System.err::println);
```
#### Capture Screenshot
```java
  String result = driver.executeCdpCommand("Page.captureScreenshot", new HashMap<>());
  String data = (String) result.get("data");
  byte[] image = new (Base64()).decode(data);
  assertThat(ImageIO.read(new ByteArrayInputStream(image)).getWidth(), greaterThan(0));
  (new FileOutputStream("temp.png")).write(image);
```
#### Capture Element Screenshot
implements the clipping to viewport functioality
```java
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
result = driver.executeCdpCommand(command, params);
dataString = (String) result.get("data");
assertThat(dataString, notNullValue());
Base64 base64 = new Base64();
byte[] image = base64.decode(dataString);
String screenshotFileName = String.format("card%02d.png", cnt);
FileOutputStream fileOutputStream = new FileOutputStream( screenshotFileName);
fileOutputStream.write(image);
fileOutputStream.close();
```

Note: some CDP API notably `Page.printToPDF` are not curently implemented:
```sh
unhandled inspector error: {"code":-32000,"message":"PrintToPDF is not implemented"}(..)
```
### Custom Headers

This can be done both at the wrapper methods
```java

    // enable Network
    chromeDevTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
    headers = new HashMap<>();
    headers.put("customHeaderName", "customHeaderValue");
    Headers headersData = new Headers(headers);
    chromeDevTools.send(Network.setExtraHTTPHeaders(headersData));
```
The validation can be done through hooking assert and log message to the event:
```java
    // add event listener to log that requests are sending with the custom header
    chromeDevTools.addListener(Network.requestWillBeSent(),
        o -> Assert.assertEquals(o.getRequest().getHeaders().get("customHeaderName"), "customHeaderValue"));
    chromeDevTools.addListener(Network.requestWillBeSent(), o -> System.err.println(
        "addCustomHeaders Listener invoked with " + o.getRequest().getHeaders().get("customHeaderName")));
```
and low level "commands":
```java
    String command = "Network.enable";
    params = new HashMap<>();
    params.put("maxTotalBufferSize", 0);
    params.put("maxPostDataSize", 0);
    params.put("maxPostDataSize", 0);
    result = driver.executeCdpCommand(command, params);
    command = "Network.setExtraHTTPHeaders";

    params = new HashMap<>();
    Map<String, String> headers = new HashMap<>();
    headers.put("customHeaderName", this.getClass().getName() + " addCustomHeadersTest");
    params.put("headers", headers);
    result = driver.executeCdpCommand(command, params);
```

To test one can e.g. fire a tomcat server with request header logging and
send the `GET` request
```java
driver.get("http://127.0.0.1:8080/demo/Demo");
```
The actual validation will be done through console logs inspection of the server

#### DOM Node Navigation


The following somewhat long test exercises steps one has to perform with CDP to get a specific DOM Node focused and act upon:

It appears every node search starts with getting the [document](https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocument):

```java
	@SuppressWarnings("unchecked")
	@Test
	public void getDocumentTest() {
		// Arrange
		driver.get("https://www.google.com");
		String command = "DOM.getDocument";
		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			// Assert
			assertThat(result, hasKey("root"));
			Map<String, Object> data =  (Map<String, Object>) result.get("root");
			assertThat(data, hasKey("nodeId"));
			assertTrue(Long.parseLong(data.get("nodeId").toString()) != 0);
			err.println("Command " + command + " return node: "
					+ new Gson().toJson(data, Map.class));
		} catch (org.openqa.selenium.WebDriverException e) {
			err.println(
					"Exception in command " + command + " (ignored): " + e.toString());
		}
	}

```

This test logs:
```js
Command DOM.getDocument return node:
{
  "backendNodeId": 1,
  "baseURL": "https://www.google.com/",
  "childNodeCount": 2,
  "children": [
    {
      "backendNodeId": 2,
      "localName": "",
      "nodeId": 10,
      "nodeName": "html",
      "nodeType": 10,
      "nodeValue": "",
      "parentId": 9,
      "publicId": "",
      "systemId": ""
    },
    {
      "attributes": [
        "itemscope",
        "",
        "itemtype",
        "http://schema.org/WebPage",
        "lang",
        "en"
      ],
      "backendNodeId": 3,
      "childNodeCount": 2,
      "children": [
        {
          "attributes": [],
          "backendNodeId": 21,
          "childNodeCount": 12,
          "localName": "head",
          "nodeId": 12,
          "nodeName": "HEAD",
          "nodeType": 1,
          "nodeValue": "",
          "parentId": 11
        },
        {
          "attributes": [
            "jsmodel",
            " ",
            "class",
            "hp vasq",
            "id",
            "gsr"
          ],
          "backendNodeId": 22,
          "childNodeCount": 8,
          "localName": "body",
          "nodeId": 13,
          "nodeName": "BODY",
          "nodeType": 1,
          "nodeValue": "",
          "parentId": 11
        }
      ],
      "frameId": "C3CE739B971DD10AFECA84F6C1554308",
      "localName": "html",
      "nodeId": 11,
      "nodeName": "HTML",
      "nodeType": 1,
      "nodeValue": "",
      "parentId": 9
    }
  ],
  "documentURL": "https://www.google.com/",
  "localName": "",
  "nodeId": 9,
  "nodeName": "#document",
  "nodeType": 9,
  "nodeValue": "",
  "xmlVersion": ""
}
```

now one can

```java
		command = "DOM.querySelector";
		params.clear();
		params.put("nodeId", nodeId);
		params.put("selector", "img#hplogo");

		try {
			result = driver.executeCdpCommand(command, params);
			assertThat(result, hasKey("nodeId"));
			nodeId = (Long) result.get("nodeId");
			assertTrue(nodeId != 0);
			err.println("Command " + command + " returned  nodeId: " + nodeId);
		} catch (org.openqa.selenium.WebDriverException e) {
			err.println(
					"Exception in command " + command + " (ignored): " + e.toString());
		}
		command = "DOM.getOuterHTML";
		params.clear();
		params.put("nodeId", nodeId);
		
		try {
			result = driver.executeCdpCommand(command, params);
			assertThat(result, notNullValue());
			assertThat(result, hasKey("outerHTML"));
			String dataString = (String) result.get("outerHTML");
			assertThat(dataString, notNullValue());
			err.println("Command " + command + " return outerHTML: " + dataString);
		} catch (Exception e) {
			err.println("Exception in " + command + " (ignored): " + e.toString());
		}
	}

```

This will log:
```shell
Command DOM.querySelector returned  nodeId: 162
```
```html
Command DOM.getOuterHTML return outerHTML:
<img alt="Google" height="92" id="hplogo"  src="/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"  style="padding-top:109px" width="272" onload="typeof google==='object'&amp;&amp;google.aft&amp;&amp;google.aft(this)" data-iml="1576602836994" data-atf="1">
```

collapsing multiple command calls together will lead to somewhat bloated test method
```java
	@Test
	public void multiCommandTest() {
		// Arrange
		baseURL = "https://www.google.com";
		driver.get(baseURL);
		String command = "DOM.getDocument";
		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			// Assert
			assertThat(result, hasKey("root"));
			@SuppressWarnings("unchecked")
			Map<String, Object> node = (Map<String, Object>) result.get("root");
			assertThat(node, hasKey("nodeId"));
			nodeId = Long.parseLong(node.get("nodeId").toString());
			assertTrue(nodeId != 0);
			err.println("Command " + command + " returned nodeId: " + nodeId);
		} catch (org.openqa.selenium.WebDriverException e) {
			err.println(
					"Exception in command " + command + " (ignored): " + e.toString());
		}
		command = "DOM.describeNode";
		params = new HashMap<>();
		params.put("nodeId", nodeId);
		params.put("depth", 1);
		try {
			result = driver.executeCdpCommand(command, params);
			// Assert
			assertThat(result, hasKey("node"));
			@SuppressWarnings("unchecked")
			Map<String, Object> data = (Map<String, Object>) result.get("node");
			for (String field : Arrays.asList(
					new String[] { "nodeType", "nodeName", "localName", "nodeValue" })) {
				assertThat(data, hasKey(field));
			}
			System.err.println("Command " + command + " returned node: " + data);
		} catch (org.openqa.selenium.WebDriverException e) {
			err.println(
					"Exception in command " + command + " (ignored): " + e.toString());
		}

		command = "DOM.querySelector";
		params = new HashMap<>();
		params.put("nodeId", nodeId);
		// params.put("selector", "img#hplogo");
		params.put("selector", "input[name='q']");

		try {
			result = driver.executeCdpCommand(command, params);
			// depth, 1
			// Assert
			assertThat(result, hasKey("nodeId"));
			// @SuppressWarnings("unchecked")
			nodeId = Long.parseLong(result.get("nodeId").toString());
			assertTrue(nodeId != 0);
			err.println("Command " + command + " returned  nodeId: " + nodeId);
		} catch (org.openqa.selenium.WebDriverException e) {
			err.println(
					"Exception in command " + command + " (ignored): " + e.toString());
		}

		command = "DOM.resolveNode";
		params = new HashMap<>();
		params.put("nodeId", nodeId);

		try {
			result = driver.executeCdpCommand(command, params);
			// depth, 1
			// Assert
			assertThat(result, hasKey("object"));
			// object
			@SuppressWarnings("unchecked")
			Map<String, Object> data = (Map<String, Object>) result.get("object");
			for (String field : Arrays.asList(
					new String[] { "type", "subtype", "className", "objectId" })) {
				assertThat(data, hasKey(field));
			}
			String objectId = (String) data.get("objectId");
			assertThat(objectId, notNullValue());
			System.err
					.println("Command " + command + " returned objectId: " + objectId);
		} catch (org.openqa.selenium.WebDriverException e) {
			err.println(
					"Exception in command " + command + " (ignored): " + e.toString());
		}

		command = "DOM.something not defined";
		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
		} catch (org.openqa.selenium.WebDriverException e) {
			err.println(
					"Exception in command " + command + " (ignored): " + e.toString());
			// wasn't found
		}
		// DOM.removeNode
		command = "DOM.focus";
		params = new HashMap<>();
		params.put("nodeId", nodeId);
		try {
			// Act
			result = driver.executeCdpCommand(command, params);
		} catch (org.openqa.selenium.WebDriverException e) {
			err.println(
					"Exception in command " + command + " (ignored): " + e.toString());
			// : unknown error: unhandled inspector error:
			// {"code":-32000,"message":"Element is not focusable"}
		}
		command = "DOM.highlightNode";
		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			Utils.sleep(10000);
		} catch (org.openqa.selenium.WebDriverException e) {
			err.println(
					"Exception in command " + command + " (ignored): " + e.toString());
		}
		// TODO: command = "Runtime.callFunctionOn";
	}


```
### Relative Locators


### Selenum release dependency

The [selenium-chromium-driver](https://jcenter.bintray.com/org/seleniumhq/selenium/selenium-chromium-driver/)
that is only available for Selenum release 4 is the critical dependency jar of this project.
The
[selenium-chromium-driver](https://mvnrepository.com/artifact/org.seleniumhq.selenium/selenium-chromium-driver) repository search page.

The [devtools](https://github.com/SeleniumHQ/selenium/tree/master/java/client/src/org/openqa/selenium/devtools) and [chromium](https://github.com/SeleniumHQ/selenium/tree/master/java/client/src/org/openqa/selenium/chromium) subprojects of selenium client of official [seleniumhq/selenium](https://github.com/SeleniumHQ/selenium) project have no dependencies and can be cloned and built locally allowing one to use CDP API with Selenium __3.x__ e.g. Selenium __3.13.0__. This is currently attempted this way in this project. Moving away form default __4.0.0.alpha__ maven profiles is a work in progress.

### Breaking Changes in Selenium 4.0.0-alpha-7

With Selenium driver release __4.0.0-alpha-7__  just to make the project compile changes imported package names need to change all
`org.openqa.selenium.devtools.browser` references with `org.openqa.selenium.devtools.v87.browser` and similar to other packages inside `org.openqa.selenium.devtools` were requied. Without this multiple compile errors like:
```sh
package org.openqa.selenium.devtools.browser does not exist
```
are observed

Also the following run time errors indicate that `selenium-api-4.0.0-alpha-7.jar` was build on JDK 11 and is notloadable in JDK 8.

This manifests through the runtime exception
```sh
java.lang.NoClassDefFoundError: Could not initialize class org.openqa.selenium.net.PortProber
  at org.openqa.selenium.remote.service.DriverService$Builder.build(DriverService.java:401)
  at org.openqa.selenium.chrome.ChromeDriverService.createServiceWithConfig(ChromeDriverService.java:133)
```
the usual classpath scan reveals the jar containing the class in question, to be actually present in classpath
```sh
find ~/.m2/repository/ -iname 'selenium*jar' |xargs -IX sh -c "echo X; jar tvf X" | tee a
```
and method signature exception
```sh
java.lang.NoSuchMethodError: java.io.FileReader.<init>(Ljava/io/File;Ljava/nio/charset/Charset;)V
  at org.openqa.selenium.net.LinuxEphemeralPortRangeDetector.getInstance(LinuxEphemeralPortRangeDetector.java:36)
  at org.openqa.selenium.net.PortProber.<clinit>(PortProber.java:42)
```
the method the exception is complainign was [added](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/FileReader.html#%3Cinit%3E(java.io.File,java.nio.charset.Charset)) in Java 11

### Note

  * To get Google Chrome updates past version 108,  one needs Windows 10 or later. Some development environment computers are using Windows 8.1

### Note

* When Chromium browser installed via snapd on Ubuntu __20.04__, all tests are failing with

```text
org.openqa.selenium.SessionNotCreatedException: Could not start a new session. Response code 500. Message: unknown error: DevToolsActivePort file doesn't exist
Host info: host: 'lenovoy40-1', ip: '127.0.1.1'
Build info: version: '4.10.0', revision: 'c14d967899'
System info: os.name: 'Linux', os.arch: 'amd64', os.version: '5.4.0-150-generic', java.version: '1.8.0_161'
Driver info: org.openqa.selenium.chrome.ChromeDriver
Command: [null, newSession {capabilities=[Capabilities {browserName: chrome, goog:chromeOptions: {args: [--remote-allow-origins=*, --allow-insecure-localhost, --allow-running-insecure-co..., --browser.download.folderLi..., --browser.helperApps.neverA..., --disable-blink-features=Au..., --disable-default-app, --disable-dev-shm-usage, --disable-extensions, --disable-gpu, --disable-infobars, --disable-in-process-stack-..., --disable-logging, --disable-notifications, --disable-popup-blocking, --disable-save-password-bubble, --disable-translate, --disable-web-security, --enable-local-file-accesses, --ignore-certificate-errors, --ignore-certificate-errors, --ignore-ssl-errors=true, --log-level=3, --no-proxy-server, --no-sandbox, --output=/dev/null, --ssl-protocol=any, --user-agent=Mozilla/5.0 (W...], extensions: []}}]}]
	at org.openqa.selenium.remote.ProtocolHandshake.createSession(ProtocolHandshake.java:140)
	at org.openqa.selenium.remote.ProtocolHandshake.createSession(ProtocolHandshake.java:96)
	at org.openqa.selenium.remote.ProtocolHandshake.createSession(ProtocolHandshake.java:68)
	at org.openqa.selenium.remote.HttpCommandExecutor.execute(HttpCommandExecutor.java:163)
	at org.openqa.selenium.remote.service.DriverCommandExecutor.invokeExecute(DriverCommandExecutor.java:196)
	at org.openqa.selenium.remote.service.DriverCommandExecutor.execute(DriverCommandExecutor.java:171)
	at org.openqa.selenium.remote.RemoteWebDriver.execute(RemoteWebDriver.java:531)
	at org.openqa.selenium.remote.RemoteWebDriver.startSession(RemoteWebDriver.java:227)
	at org.openqa.selenium.remote.RemoteWebDriver.<init>(RemoteWebDriver.java:154)
	at org.openqa.selenium.chromium.ChromiumDriver.<init>(ChromiumDriver.java:107)
	at org.openqa.selenium.chrome.ChromeDriver.<init>(ChromeDriver.java:87)
	at org.openqa.selenium.chrome.ChromeDriver.<init>(ChromeDriver.java:82)
	at org.openqa.selenium.chrome.ChromeDriver.<init>(ChromeDriver.java:71)
	at com.github.sergueik.selenium.BaseCdpTest.beforeClass(BaseCdpTest.java:124)

```
* when Chromium browser installed [via apt](https://askubuntu.com/questions/1204571/how-to-install-chromium-without-snap), from
```sh
sudo add-apt-repository ppa:system76/pop
sudo apt update
sudo apt install -y -q chromium
```
the tests would work but the browser is quite old version __83__.

The latest available version of chromium-broser on `http://archive.ubuntu.com/ubuntu/pool/universe/c/chromium-browser/` is __112__

NOTE: will have to download a few packages to be able to install `chromium-browser`:

* `chromium-browser_112.0.5615.49-0ubuntu0.18.04.1_amd64.deb`
* `chromium-browser-l10n_112.0.5615.49-0ubuntu0.18.04.1_all.deb`
* `chromium-codecs-ffmpeg_112.0.5615.49-0ubuntu0.18.04.1_amd64.deb`
* `chromium-codecs-ffmpeg-extra_112.0.5615.49-0ubuntu0.18.04.1_amd64.deb`

and install them in specific order:
```sh
dpkg -i chromium-codecs-ffmpeg_112.0.5615.49-0ubuntu0.18.04.1_amd64.deb
dpkg -i chromium-codecs-ffmpeg-extra_112.0.5615.49-0ubuntu0.18.04.1_amd64.deb
dpkg -i chromium-browser_112.0.5615.49-0ubuntu0.18.04.1_amd64.deb
```

Since the version mismatch the test log will contain plenty of
```text
Match
WARNING: Unable to find CDP implementation matching 112
Jun 15, 2023 9:56:53 AM org.openqa.selenium.chromium.ChromiumDriver lambda$new$4
WARNING: Unable to find version of CDP to use for . You may need to include a dependency on a specific version of the CDP using something similar to `org.seleniumhq.selenium:selenium-devtools-v86:4.10.0` where the version ("v86") matches the version of the chromium-based browser you're using and the version number of the artifact is the same as Selenium's.

```

* when Chrome latest stable deb package is downloaded and chrome is installed via `dpkg`

```sh
cd ~/Downloads
wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
sudo apt install -y -q ./google-chrome-stable_current_amd64.deb
```
a longer version

```sh
cd ~/Downloads
wget -nv "https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb"
sudo apt-get install -qqy libxss1 libappindicator1 libindicator7
sudo dpkg -i google-chrome-stable_current_amd64.deb
rm google-chrome-stable_current_amd64.deb
sudo apt-get install -qqy -f google-chrome-stable
```
```sh
dpkg -l google-chrome-stable
```
```text
Desired=Unknown/Install/Remove/Purge/Hold
| Status=Not/Inst/Conf-files/Unpacked/halF-conf/Half-inst/trig-aWait/Trig-pend
|/ Err?=(none)/Reinst-required (Status,Err: uppercase=bad)
||/ Name           Version      Architecture Description
+++-==============-============-============-=================================
ii  google-chrome- 114.0.5735.1 amd64        The web browser from Google
```
Download latest Chromedriver
```sh
CHROMEDRIVER_VERSION=$(curl -s "http://chromedriver.storage.googleapis.com/LATEST_RELEASE")
PACKAGE_ARCHIVE='chromedriver_linux64.zip'
PLATFORM=linux64
URL="http://chromedriver.storage.googleapis.com/${CHROMEDRIVER_VERSION}/chromedriver_${PLATFORM}.zip"
wget -O $PACKAGE_ARCHIVE -nv $URL
unzip -o $PACKAGE_ARCHIVE
```
NOTE: the latest version of Chrome Driver that can be downloaded this way is `114.0.5735.90`


the tests work

NOTE: is is better to use

```sh
dpkg -i ./google-chrome-stable_current_amd64.deb
```
the `apt` has the warning:
```text
W: Repository is broken: google-chrome-stable:amd64 (= 114.0.5735.133-1) has no Size information
```
### File Download Browser Behavior

one may configure
``
and subscribe to events `Browser.downloadWillBegin` and `Browser.downloadProgress` via CDP:

```java
chromeDevTools.addListener(Browser.downloadWillBegin(),
	(DownloadWillBegin o) -> {
		System.err.println("in Browser.downloadWillBegin listener. url: " + o.getUrl() + "\tfilename: " + o.getSuggestedFilename());
});
List<DownloadProgress.State> states = new ArrayList<>();
chromeDevTools.addListener(Browser.downloadProgress(),
	(DownloadProgress o) -> {
		DownloadProgress.State state = o.getState();
		System.err.println("in Browser.downloadProgress listener. state: " + state.toString());
		states.add(state);
});

```
```text
in Browser.downloadWillBegin listener. url: https://scholar.harvard.edu/files/torman_personal/files/samplepptx.pptx     filename: samplepptx.pptx
in Browser.downloadProgress listener. state: inProgress
in Browser.downloadProgress listener. state: inProgress
in Browser.downloadProgress listener. state: inProgress
in Browser.downloadProgress listener. state: inProgress
in Browser.downloadProgress listener. state: inProgress
in Browser.downloadProgress listener. state: inProgress
in Browser.downloadProgress listener. state: inProgress
in Browser.downloadProgress listener. state: inProgress
in Browser.downloadProgress listener. state: inProgress
in Browser.downloadProgress listener. state: inProgress
in Browser.downloadProgress listener. state: completed
in Browser.downloadProgress listener. state: completed
Inspecting downloaded filename: f5a83cbd-97fb-451e-8651-618f63c1ec59
Verified downloaded file: f5a83cbd-97fb-451e-8651-618f63c1ec59 in /tmp

```
### Note

Starting with version __115__ the Chrome browser and ChromeDriver
information is located on
__Chrome for Testing availability__
[dashboard page](https://googlechromelabs.github.io/chrome-for-testing/).
A broader listing of Chrome versions can be found in `https://googlechromelabs.github.io/chrome-for-testing/known-good-versions-with-downloads.json`:
```sh

curl -k -O https://googlechromelabs.github.io/chrome-for-testing/known-good-versions-with-downloads.json
```
```sh

jq '.versions[] | select(.version | contains( "114.")) ' known-good-versions-with-downloads.json  | jq '.downloads[]|.[]|select(.platform |contains("linux64"))'
```
```JSON
{
  "platform": "linux64",
  "url": "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/114.0.5696.0/linux64/chrome-linux64.zip"
}
```
 - extract the `chrome` and `chromedriver` links via `jq` (unfinished for chromedriver, save the full JSON locally and finish the query)

The instructions of version lookup are provided on [version selection hint page](https://chromedriver.chromium.org/downloads/version-selection). Prior to that the `chromedriver` download links were posted on Chromedriver Downloads [page](https://chromedriver.chromium.org/downloads)

* With Selenium version __4.14.0__ onwards one has to build it on JDK __11___ or later, even if targeting Java __1.8__ in `pom.xml`  - `* Require Java 11 (#12843)` is noted in the [Selenium Changelog](https://github.com/SeleniumHQ/selenium/commit/a0e04e15f17ed3f12373f61c363a296d4e06a976#diff-44f582ea3c01561650edeea2771d241bf19ceb93eb8f96bc3d199bcd7ca30d3e).

The attempt to build with JDK __1.8__ fails with

```text
[ERROR] bad class file: .m2\repository\org\seleniumhq\selenium\selenium-api\4.14.0\selenium-api-4.14.0.jar
[ERROR] class file has wrong version 55.0, should be 52.0
```

### Note

if seeing the version mismatch error in every test:
```sh
mvn test
```
```text
org.openqa.selenium.SessionNotCreatedException: Could not start a new session. Response code 500. Message: session not created: This version of ChromeDriver only supports Chrome version 122
Current browser version is 121.0.6167.85 with binary path /opt/google/chrome/chrome
```


make sure to have the google repository  added:



```sh
echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" | sudo tee /etc/apt/sources.list.d/google-chrome.list
```

and the error is cleared:
```text
apt-get install google-chrome-stable
Reading package lists... Done
Building dependency tree
Reading state information... Done
Package google-chrome-stable is not available, but is referred to by another package.
This may mean that the package is missing, has been obsoleted, or
is only available from another source

E: Package 'google-chrome-stable' has no installation candidate
```
```text
dpkg-reconfigure google-chrome-stable
/usr/sbin/dpkg-reconfigure: google-chrome-stable is broken or not fully installed

```
the install the chrome:
```sh
apt-get install google-chrome-stable
```
and confirm the test to pass

### Debugging File Upload

A textbook [File Upload Form](https://cgi-lib.berkeley.edu/ex/fup.html) looks like below

![file upload page](https://github.com/sergueik/selenium_cdp/blob/master/screenshots/capture-file-upload-form.png)

```html
<html>
<head>
<title>File Upload Test</title>
</head>

<body>
<h1>File Upload Test</h1>
<form enctype = "multipart/form-data" action="upload endpoint url" method="POST">
upload file path: <input name="upload file path" type="file">
<input type="submit" value="send file">
</form>
</body>
```

The requirements for plain browser-driven file upload HTML page are:

  * form must specify the `POST` method
  * form must specify an enctype of `multipart/form-data`
  * form must contain an `<input type="file">` element

To examine the upload request genetated by the browser, subscribe to `` [event](https://chromedevtools.github.io/devtools-protocol/1-3/Network/#event-requestWillBeSent):
```java
Map<String, Map<String, Object>> requests = new HashMap<>();
chromeDevTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
chromeDevTools.addListener(Network.requestWillBeSent(),
  (RequestWillBeSent event) ->
    requests.put(event.getRequest().getUrl(), event.getRequest().getHeaders().toJson())
);
```
and perform the upload:

```java
wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
wait.pollingEvery(Duration.ofMillis(pollingInterval));
actions = new Actions(driver);
url = "https://ps.uci.edu/~franklin/doc/file_upload.html";
driver.get(url);
element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='userfile']")));

assertThat(element.isDisplayed(), is(true));
Utils.highlight(element);
element.sendKeys(dummy.getAbsolutePath());

element = driver.findElement(By.tagName("form"));
assertThat(element, notNullValue());
assertThat(element.getAttribute("action"), notNullValue());
url2 = element.getAttribute("action");

element = driver.findElement(By.cssSelector("input[type='submit']"));
assertThat(element, notNullValue());
Utils.highlight(element);
requests.clear();
element.submit();
try {
  Thread.sleep(1000);
} catch (InterruptedException e) {
  e.printStackTrace();
}

System.err.println("Captured: ");
requests.keySet().stream().forEach(System.err::println);

```

this will print:
```text
data:image/png;base64,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
data:image/png;base64,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
data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEgAAABIAQMAAABvIyEEAAAABlBMVEUAAABTU1OoaSf/AAAAAXRSTlMAQObYZgAAAENJREFUeF7tzbEJACEQRNGBLeAasBCza2lLEGx0CxFGG9hBMDDxRy/72O9FMnIFapGylsu1fgoBdkXfUHLrQgdfrlJN1BdYBjQQm3UAAAAASUVORK5CYII=
https://ps.uci.edu/favicon.ico
https://www.oac.uci.edu/indiv/franklin/cgi-bin/values
```
the following confirms there is more than one data chunk:
```java
Pattern pattern = Pattern.compile("data:image/png;base64");
System.err.println("Pattern:\n" + pattern.toString());
cnt = 0;
for (String x : requests.keySet()) {
  Matcher matcher = pattern.matcher(x);
  if (matcher.find()) {
    cnt++;
  }
}
assertThat(cnt, greaterThan(1));

```
the following prints the headers sent to file upload endpoint:
```java
System.err.println("Headers: " + requests.get(url2).toString());
```
```text
Headers: {
Content-Type=multipart/form-data;
boundary=----WebKitFormBoundaryhsNWaugzUXymNUzV,
Origin=https://ps.uci.edu,
Referer=https://ps.uci.edu/,
Upgrade-Insecure-Requests=1,
User-Agent=Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20120101 Firefox/33.0,
sec-ch-ua="Chromium";v="124",
"Google Chrome";v="124",
"Not-A.Brand";v="99",
sec-ch-ua-mobile=?0,
sec-ch-ua-platform="Linux"
}

```
### See Also

  * [chrome devtools](https://github.com/ChromeDevTools/awesome-chrome-devtools) project
  * [puppeteer online](https://try-puppeteer.appspot.com/)
  * [GoogleChrome/puppeteer](https://github.com/GoogleChrome/puppeteer)
  * [ChromeDevTools/debugger-protocol-viewer](https://github.com/ChromeDevTools/debugger-protocol-viewer)
  * [standalond java cdp client](https://github.com/webfolderio/cdp4j) (commecial)
  * [cdp4j/javadoc](https://webfolder.io/cdp4j/javadoc/index.html)
  * headless chrome devtools based testing [lecture](https://habr.com/ru/company/yandex/blog/353018/) and [video](https://youtu.be/jUycQRFoOww)(in Russan)
  * [HTML to PDF conversion with Chromium devtools and Selenium Python client](https://habr.com/ru/post/459112/) (in Russan)
  * [PDF configuration](https://github.com/GoogleChrome/puppeteer/blob/v1.11.0/docs/api.md#pagepdfoptions) with [Puppeteer](https://github.com/GoogleChrome/puppeteer)
  * Selenium 4 [Relative Locator](https://angiejones.tech/selenium-4-relative-locators/) DOM traversal DSL.
  * Selenium 4 [Relatve locator]( https://www.swtestacademy.com/selenium-relative-locators/) examples
  * alternative java websocket client [HubSpot/ChromeDevToolsClient](https://github.com/HubSpot/ChromeDevToolsClient) for the Chrome DevTools Protocol
  * another [chrome-devtools-java-client](https://github.com/kklisura/chrome-devtools-java-client) project featuring annotation-style [builder pattern](https://dzone.com/articles/design-patterns-the-builder-pattern) design for handling chrome commandline arguments.
  * [examples](https://github.com/adiohana/selenium-chrome-devtools-examples) with callback hook example
  * yet another project [chrome-devtools-webdriver-integration](https://github.com/sahajamit/chrome-devtools-webdriver-integration)
  * yet another framework project [sachinguptait/SeleniumAutomation](https://github.com/sachinguptait/SeleniumAutomation) demonstrating Selenium 4 and CDP features
  * yet another project [SrinivasanTarget/selenium4CDPsamples](https://github.com/SrinivasanTarget/selenium4CDPsamples)
  * Python Chrome Devtools Procotol [client](https://github.com/qtacore/chrome_master/blob/master/chrome_master/input_handler.py)
  * yet another [CDP Java client](https://github.com/Fathima704/sample-selenium-cdp) . Note: only works with Selenium 4.alpha-2
  * Python Chrome Devtools Procotol [client](https://github.com/shish/devtools-py)
  * Selenium 3.x CDP Exender [clone project](https://github.com/sergueik/cdp_webdriver) - the original project [sahajamit/chrome-devtools-webdriver-integration](https://github.com/sahajamit/chrome-devtools-webdriver-integration) - is somewhat stale
  * Selenium 4.0x `WindowType` [feature](https://www.facebook.com/2162486093842847/posts/selenium-4-is-providing-a-new-feature-to-launch-and-switch-to-new-tab-or-window-/2253812251376897/)
  * use CDP to switch to winows [stackoverflow](https://stackoverflow.com/questions/48219637/use-devtools-protocol-to-open-new-tab-in-window)
  * JavaScript-style event callback design [jpuppeteer](https://github.com/sunshinex/jpuppeteer)
  * [library inspired by Puppeteer](https://github.com/fanyong920/jvppeteer) to facilitate the use of Chrome DevTools API to control Chrome or Chromium via Java
  * advanced [prevention headless browser detection](https://intoli.com/blog/making-chrome-headless-undetectable/)
  * [recaptcha-3 score](https://antcpt.com/rus/information/demo-form/recaptcha-3-test-score.html)
  * [Puppeteer Documentation](https://devdocs.io/puppeteer/)
  * [difference between WebDriver and DevTool protocol](https://stackoverflow.com/questions/50939116/what-is-the-difference-between-webdriver-and-devtool-protocol/50942942#50942942) explained
  * [using Chrome DevTools Protocol](https://github.com/aslushnikov/getting-started-with-cdp)
  * overview of [DevTools access offered by Selenium 4](https://applitools.com/blog/selenium-4-chrome-devtools/)
  * [Libraries.io](https://libraries.io/maven/org.seleniumhq.selenium:selenium-devtools-v93) - monitors over 2 million open source libraries/packages from 36 package managers
  * [collection of Selenium 4 CDP-specific tests](https://github.com/rookieInTraining)
  * https://medium.com/codex/selenium4-a-peek-into-chrome-devtools-92bca6de55e0
  * BiDirectional WebDriver Protocol [w3c spec](https://w3c.github.io/webdriver-bidi/)
  * BiDi - The future of cross-browser automation [blog](https://developer.chrome.com/blog/webdriver-bidi/)
  * BiDirectional functionality [official documentation](https://www.selenium.dev/documentation/webdriver/bidirectional/)
  * list of bidi APIs [examples](https://www.selenium.dev/documentation/webdriver/bidirectional/bidi_api)
  * [guide To Java 8 Optional](https://www.baeldung.com/java-optional)
  * [translation of the guide To Java 8 Optional](https://habr.com/ru/post/658457/)(in Russian)
  * Ferrum - Ruby gem for "high-level" API for CDP backed Chrome browser automation [rubycdp/ferrum](https://github.com/rubycdp/ferrum) repository, [rubygems.org link](https://rubygems.org/gems/ferrum/versions/0.6.2) [documentation translation](https://habr.com/ru/post/681292/) (in Russian)
  * [serg-ty/selenium-tests-logger](https://github.com/serg-ty/selenium-tests-logger) project to enable listeners as part of the logging

  * [list of existing headless web browsers](https://github.com/dhamaniasad/HeadlessBrowsers)
  * [new headless mode](https://www.selenium.dev/blog/2023/headless-is-going-away/)

  * [interactive performance analysis with Chrome DevTools](https://www.thisdot.co/blog/performance-analysis-with-chrome-devtools)
  * [replacing request url, postdata and headers](https://github.com/premsundarraj/SeleniumCDP)
  * [getting connection information and cookies from chrome dev tools and use with curl](https://github.com/fipso/ccurl.sh)
  * [How to Install Google Chrome Web Browser on Ubuntu 18.04](https://linuxize.com/post/how-to-install-google-chrome-web-browser-on-ubuntu-18-04/)
  * [Content Security Policy (CSP) Evaluator](https://csp-evaluator.withgoogle.com/)
  * [Testing for Content Security Policy](https://owasp.org/www-project-web-security-testing-guide/latest/4-Web_Application_Security_Testing/02-Configuration_and_Deployment_Management_Testing/12-Test_for_Content_Security_Policy)
  * [Enable page Content Security Policy by-passing](https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-setBypassCSP)
  * [Content Security Policy Examples](https://content-security-policy.com/examples/)



### License
This project is licensed under the terms of the MIT license.

### Author
[Serguei Kouzmine](kouzmine_serguei@yahoo.com)

