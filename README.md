### Info

The project practices Java Selenium __4.0.x__ release
[ChromiumDriver](https://github.com/SeleniumHQ/selenium/blob/master/java/client/src/org/openqa/selenium/chromium/ChromiumDriver.java)
to execute the [Chrome DevTools Protocol](https://chromedevtools.github.io/devtools-protocol/) a.k.a.
__cdp__ commands - an entirely different set of API communicated to the Chrome browser family via `POST` [requests](https://github.com/SeleniumHQ/selenium/blob/master/java/client/src/org/openqa/selenium/chromium/ChromiumDriverCommandExecutor.java) to `/session/$sessionId/goog/cdp/execute` with API-specific payload) feature (many of the cdp methods e.g. the [DOM]](https://chromedevtools.github.io/devtools-protocol/tot/DOM) ones like

  * `performSearch`,
  * `getSearchResults`
  * `getNodeForLocation`
  * `getOuterHTML`
  * `querySelectorAll
  * `querySelector`
  * `getAttributes`
  * `addCustomHeaders`

overlap with classic Selenium in Classic Javascript
and there are few specific ones.The project also exercised other new Selenium 4 API e.g. [relative nearby locators](https://dzone.com/articles/how-selenium-4-relative-locator-can-change-the-way) whidh did not apear powerful enough yet.

For accessing the __Chrome Devtools API__ without upgrading the Selenium driver to alpha release __4.0.x__ see [cdp_webdriver](https://github.com/sergueik/cdp_webdriver) project


### Examples

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
  * Python Chrome Devtools Procotol [client](https://github.com/shish/devtools-py)
  * Selenium 3.x CDP Exender [clone project](https://github.com/sergueik/cdp_webdriver) - the original project [sahajamit/chrome-devtools-webdriver-integration](https://github.com/sahajamit/chrome-devtools-webdriver-integration) - is somewhat stale
  * Selenium 4.0x `WindowType` [feature](https://www.facebook.com/2162486093842847/posts/selenium-4-is-providing-a-new-feature-to-launch-and-switch-to-new-tab-or-window-/2253812251376897/)
  * use CDP to switch to winows [stackoverflow](https://stackoverflow.com/questions/48219637/use-devtools-protocol-to-open-new-tab-in-window)
  * JavaScript-style event callback design [jpuppeteer](https://github.com/sunshinex/jpuppeteer)
  * [library inspired by Puppeteer](https://github.com/fanyong920/jvppeteer) to facilitate the use of Chrome DevTools API to control Chrome or Chromium via Java
  * advanced [prevention headless browser detection](https://intoli.com/blog/making-chrome-headless-undetectable/)
  * [recaptcha-3 score](https://antcpt.com/rus/information/demo-form/recaptcha-3-test-score.html)
  * [Puppeteer Documentation](https://devdocs.io/puppeteer/)

### License
This project is licensed under the terms of the MIT license.

### Author
[Serguei Kouzmine](kouzmine_serguei@yahoo.com)


