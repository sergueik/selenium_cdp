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
  * BiDirectional WebDriver Protocol w3c [s[ec](https://w3c.github.io/webdriver-bidi/)
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

### License
This project is licensed under the terms of the MIT license.

### Author
[Serguei Kouzmine](kouzmine_serguei@yahoo.com)

