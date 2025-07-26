package com.github.sergueik.selenium;

/**
 * Copyright 2021,2022,2024,2025 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
// import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.v138.dom.DOM;
import org.openqa.selenium.devtools.v138.dom.model.BackendNodeId;
import org.openqa.selenium.devtools.v138.dom.model.NodeId;
import org.openqa.selenium.devtools.v138.dom.model.RGBA;
import org.openqa.selenium.devtools.v138.overlay.Overlay;
import org.openqa.selenium.devtools.v138.page.Page;
import org.openqa.selenium.devtools.v138.page.model.Frame;
import org.openqa.selenium.devtools.v138.page.model.FrameId;
import org.openqa.selenium.devtools.v138.page.model.FrameTree;

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

	private FrameTree frameTree = null;
	private FrameId frameId = null;
	private Frame frame = null;
	private RGBA color = null;
	private String html = null;
	Optional<List<FrameTree>> frames = null;
	private BackendNodeId backendNodeId = null;
	private NodeId nodeId = null;

	// @Ignore

	@Before
	public void before() throws Exception {
	}

	@Test
	public void test1() {
		// Arrange
		baseURL = "https://cloud.google.com/products/calculator";
		driver.get(baseURL);

		frameTree = chromeDevTools.send(Page.getFrameTree());
		assertThat(frameTree, notNullValue());
		if (frameTree != null) {
			// Act
			frame = frameTree.getFrame();
			System.err.println(
					String.format("id: %s" + "\t" + "url: %s" + "\t" + "mime-type: %s",
							frame.getId(), frame.getUrl(), frame.getMimeType()));
		} else {
			System.err.println("No Frames found on " + baseURL);
		}
	}

	// @Ignore
	@Test
	public void test2() {
		// Act
		// Arrange
		baseURL = "https://cloud.google.com/products/calculator";
		driver.get(baseURL);
		frameTree = chromeDevTools.send(Page.getFrameTree());
		frames = frameTree.getChildFrames();
		if (frames.isPresent()) {

			frames.get().stream().map((FrameTree frameTree) -> {
				Frame frame = frameTree.getFrame();
				return String.format("Frame %s id: %s url: %s",
						frame.getName().isPresent()
								? String.format("name: %s", frame.getName().get()) : "",
						frame.getId(), frame.getUrl());
			}).forEach(System.err::println);

			frames.get().stream().forEach((FrameTree frameTree) -> {
				Frame frame = frameTree.getFrame();
				try {
					frameId = frame.getId();
					DOM.GetFrameOwnerResponse response2 = chromeDevTools
							.send(DOM.getFrameOwner(frameId));
					if (response2.getNodeId().isPresent()) {
						nodeId = response2.getNodeId().get();
						System.err.println("Frame owner node id: " + nodeId);
					}
					backendNodeId = response2.getBackendNodeId();
					System.err.println("Frame owner backend node id: " + backendNodeId);

					html = chromeDevTools.send(DOM.getOuterHTML(response2.getNodeId(),
							Optional.of(response2.getBackendNodeId()), Optional.empty()));
					System.err.println("Frame owner outer HTML: " + html);

				} catch (DevToolsException e) {
					System.err.println("Exception (ignored): " + e.toString());
				}
			});
		} else

		{
			System.err.println("No Frames found on " + baseURL);
		}
	}

	// @Ignore
	@SuppressWarnings("deprecation")
	@Test
	public void test3() {
		// Act
		baseURL = "https://www.javatpoint.com/oprweb/test.jsp?filename=htmliframes";
		driver.get(baseURL);
		frames = chromeDevTools.send(Page.getFrameTree()).getChildFrames();
		// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-enable
		chromeDevTools
				.send(DOM.enable(Optional.of(DOM.EnableIncludeWhitespace.NONE)));

		chromeDevTools.send(Overlay.enable());
		frames.get().stream().forEach((FrameTree frameTree) -> {
			Frame frame = frameTree.getFrame();
			try {
				color = new RGBA(Utils.getRandomColor(), Utils.getRandomColor(),
						Utils.getRandomColor(), Optional.empty());
				frameId = frame.getId();
				chromeDevTools.send(Overlay.highlightFrame(frameId, Optional.of(color),
						Optional.empty()));
				System.err.println("Attempted to highlight frame " + frameId);
				Utils.sleep(1000);
			} catch (DevToolsException e) {
				System.err.println("Exception (ignored): " + e.toString());
			}
		});
	}
}
