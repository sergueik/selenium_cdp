package com.github.sergueik.selenium;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.v89.dom.DOM;
import org.openqa.selenium.devtools.v89.dom.model.BackendNodeId;
import org.openqa.selenium.devtools.v89.dom.model.NodeId;
import org.openqa.selenium.devtools.v89.dom.model.RGBA;
import org.openqa.selenium.devtools.v89.overlay.Overlay;
import org.openqa.selenium.devtools.v89.page.Page;
import org.openqa.selenium.devtools.v89.page.model.FrameId;
import org.openqa.selenium.devtools.v89.page.model.FrameTree;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see:
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-getFrameTree
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getFrameOwner
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getOuterHTML
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#type-Frame
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/Overlay/#method-enable
 * https://chromedevtools.github.io/devtools-protocol/tot/Overlay/#method-highlightFrame
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#type-RGBA
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class FramesDevToolsTest extends BaseDevToolsTest {

	private FrameTree response = null;
	private FrameId frameId = null;
	private String html = null;
	Optional<List<FrameTree>> frames = null;

	// @Ignore

	@Before
	public void before() throws Exception {
		baseURL = "https://cloud.google.com/products/calculator";
		// Arrange
		driver.get(baseURL);
	}

	@Test
	public void test1() {
		// Act
		response = chromeDevTools.send(Page.getFrameTree());
		frames = response.getChildFrames();
		if (frames.isPresent()) {
			frames.get().stream().map(o -> o.getFrame())
					.map(frame -> String.format("Frame %s id: %s url: %s",
							frame.getName().isPresent()
									? String.format("name: %s", frame.getName().get()) : "",
							frame.getId(), frame.getUrl()))
					.forEach(System.err::println);

			frames.get().stream().map(o -> o.getFrame()).forEach(frame -> {
				try {
					frameId = frame.getId();
					DOM.GetFrameOwnerResponse response2 = chromeDevTools
							.send(DOM.getFrameOwner(frameId));
					if (response2.getNodeId().isPresent()) {
						NodeId nodeId = response2.getNodeId().get();
						System.err.println("Frame owner node id: " + nodeId);
					}
					BackendNodeId backendNodeId = response2.getBackendNodeId();
					System.err.println("Frame owner backend node id: " + backendNodeId);

					html = chromeDevTools.send(DOM.getOuterHTML(response2.getNodeId(),
							Optional.of(response2.getBackendNodeId()), Optional.empty()));
					System.err.println("Frame owner outer HTML: " + html);

				} catch (DevToolsException e) {
					System.err.println("Exception (ignored): " + e.toString());
				}
			});
		} else {
			System.err.println("No Frames found on " + baseURL);
		}
	}

	// @Ignore
	@Test
	public void test2() {
		// Act
		response = chromeDevTools.send(Page.getFrameTree());
		frames = response.getChildFrames();
		if (frames.isPresent()) {
			frames.get().stream().map(o -> o.getFrame())
					.map(frame -> String.format("Frame %s id: %s url: %s",
							frame.getName().isPresent()
									? String.format("name: %s", frame.getName().get()) : "",
							frame.getId(), frame.getUrl()))
					.forEach(System.err::println);

			frames.get().stream().map(o -> o.getFrame()).forEach(frame -> {
				try {
					chromeDevTools.send(DOM.enable());
					chromeDevTools.send(Overlay.enable());
					RGBA color = new RGBA(128, 0, 0, Optional.empty());
					FrameId frameId = frame.getId();
					chromeDevTools.send(Overlay.highlightFrame(frameId,
							Optional.of(color), Optional.empty()));
					System.err.println("Attempted to highlight frame " + frameId);
				} catch (DevToolsException e) {
					System.err.println("Exception (ignored): " + e.toString());
				}
			});
		} else {
			System.err.println("No Frames found on " + baseURL);
		}
	}
}
