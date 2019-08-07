### Info

The project practices Java Selenium 4.0 beta release [ChromiumDriver](https://github.com/SeleniumHQ/selenium/blob/master/java/client/src/org/openqa/selenium/chromium/ChromiumDriver.java) for executing the [Chrome DevTools Protocol](https://chromedevtools.github.io/devtools-protocol/) (cdp) commands (a entirely different set of API communicated to the Chrome browser family via `POST` requests to `/session/$sessionId/goog/cdp/execute` with API-specific payload) feature.

### Examples

#### Override User Agent

One can call cdp protocol to invoke [setUserAgentOverride](https://chromedevtools.github.io/devtools-protocol/tot/Network#method-setUserAgentOverride) method and dynmically modify the `user-agent` header during the test:

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

### Selenum release dependency

It appears that the critical dependency jar of this project, [selenium-chromium-driver](https://jcenter.bintray.com/org/seleniumhq/selenium/selenium-chromium-driver/) only available for Selenum release 4.x.

### See Also

  * [chrome devtools](https://github.com/ChromeDevTools/awesome-chrome-devtools)
 project
  * [puppeteer online](https://try-puppeteer.appspot.com/)
  * [GoogleChrome/puppeteer](https://github.com/GoogleChrome/puppeteer)
  * [ChromeDevTools/debugger-protocol-viewer](https://github.com/ChromeDevTools/debugger-protocol-viewer)
  * [standalond java cdp clienti](https://github.com/webfolderio/cdp4j) (commecial)
  * [cdp4j/javadoc](https://webfolder.io/cdp4j/javadoc/index.html)
  * headless chrome devtools based testing [lecture](https://habr.com/ru/company/yandex/blog/353018/) and the [video](https://youtu.be/jUycQRFoOww)(in Russan)

  ### License
This project is licensed under the terms of the MIT license.

### Author
[Serguei Kouzmine](kouzmine_serguei@yahoo.com)
