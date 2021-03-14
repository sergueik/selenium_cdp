package com.github.sergueik.selenium;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.TimeoutException;
// need to use branch cdp_codegen of SeleniumHQ/selenium
// https://github.com/SeleniumHQ/selenium/tree/cdp_codegen/java/client/src/org/openqa/selenium/devtools
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.v89.dom.DOM;
import org.openqa.selenium.devtools.v89.dom.model.BackendNodeId;
import org.openqa.selenium.devtools.v89.dom.model.NodeId;
import org.openqa.selenium.devtools.v89.dom.model.RGBA;
import org.openqa.selenium.devtools.v89.overlay.Overlay;
import org.openqa.selenium.devtools.v89.page.Page;
import org.openqa.selenium.devtools.v89.page.model.FrameId;
import org.openqa.selenium.devtools.v89.page.model.FrameTree;
import org.openqa.selenium.remote.Command;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see:
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-getFrameTree
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#type-Frame
 * https://chromedevtools.github.io/devtools-protocol/tot/Overlay/#method-highlightFrame
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#type-RGBA
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class FramesDevToolsTest extends BaseDevToolsTest {

	private static Map<String, Object> headers = new HashMap<>();

	// @Ignore
	@Test
	public void test1() {
		// Arrange
		driver.get("https://cloud.google.com/products/calculator");
		FrameTree response = chromeDevTools.send(Page.getFrameTree());
		System.err.println("API response: " + response.getClass());
		Optional<List<FrameTree>> frames = response.getChildFrames();
		if (frames.isPresent()) {
			frames.get().stream().map(o -> o.getFrame())
					.map(frame -> String.format("Frame %s id: %s url: %s",
							frame.getName().isPresent()
									? String.format("name: %s", frame.getName().get()) : "",
							frame.getId(), frame.getUrl()))
					.forEach(System.err::println);

			RGBA color = new RGBA(128, 0, 0, Optional.empty());
			frames.get().stream().map(o -> o.getFrame()).forEach(frame -> {
				try {
					FrameId frameId = frame.getId();
					DOM.GetFrameOwnerResponse response2 = chromeDevTools
							.send(DOM.getFrameOwner(frameId));
					if (response2.getNodeId().isPresent()) {
						NodeId nodeId = response2.getNodeId().get();
						System.err.println("Frame owner node id: " + nodeId);
					}
					BackendNodeId backendNodeId = response2.getBackendNodeId();
					System.err.println("Frame owner backend node id: " + backendNodeId);

					String data = chromeDevTools
							.send(DOM.getOuterHTML(response2.getNodeId(),
									Optional.of(response2.getBackendNodeId()), Optional.empty()));
					System.err.println("API response: " + data);

					chromeDevTools.send(Overlay.highlightFrame(frame.getId(),
							Optional.of(color), Optional.empty()));
				} catch (TimeoutException e) {
					// WARNING: Unhandled type:
					// {"id":9,"error":{"code":-32602,"message":"Invalid
					// parameters","data":"Failed to deserialize params.contentColor.a -
					// BINDINGS: double value expected at position
					// 71"},"sessionId":"02D4DB8D745FBC153C1753C69CB75C14"}
				} catch (DevToolsException e) {
					System.err.println("Exception (ignored): " + e.toString());
				}
			});
		}
	}

}
