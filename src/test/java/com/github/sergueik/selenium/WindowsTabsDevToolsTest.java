package com.github.sergueik.selenium;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.v92.dom.DOM;
import org.openqa.selenium.devtools.v92.dom.model.BackendNodeId;
import org.openqa.selenium.devtools.v92.dom.model.NodeId;
import org.openqa.selenium.devtools.v92.dom.model.RGBA;
import org.openqa.selenium.devtools.v92.overlay.Overlay;
import org.openqa.selenium.devtools.v92.page.Page;
import org.openqa.selenium.devtools.v92.page.model.FrameId;
// NOTE letter case in the class name 
import org.openqa.selenium.devtools.v92.target.model.TargetID;
import org.openqa.selenium.devtools.v92.target.model.TargetInfo;
import org.openqa.selenium.devtools.v92.target.Target;
import org.openqa.selenium.devtools.v92.target.model.SessionID;
import org.openqa.selenium.devtools.v92.page.model.FrameTree;

public class WindowsTabsDevToolsTest extends BaseDevToolsTest {

	private TargetID targetId = null;
	private SessionID sessionId = null;
	private TargetInfo targetInfo = null;
	private List<TargetInfo> targetInfos = new ArrayList<>();

	@Before
	public void before() throws Exception {
	}

	// @Ignore
	@Test
	public void test1() {
		// Arrange
		// Act
		baseURL = "https://en.wikipedia.org/wiki/Main_Page";
		targetId = chromeDevTools.send(Target.createTarget(baseURL,
				Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
				Optional.of(true), Optional.of(false)));
		Utils.sleep(1000);
		System.err.println("TargetID: " + targetId);
		sessionId = chromeDevTools
				.send(Target.attachToTarget(targetId, Optional.empty()));
		System.err.println("SessionId: " + sessionId);
		targetInfo = chromeDevTools
				.send(Target.getTargetInfo(Optional.of(targetId)));
		System.err.println("TargetInfo: ");
		System.err.println("TargetId: " + targetInfo.getTargetId());
		System.err.println("Title: " + targetInfo.getTitle());
		System.err.println("Type: " + targetInfo.getType());
		System.err.println("Url: " + targetInfo.getUrl());
		System.err.println("Attached: " + targetInfo.getAttached());

		targetInfos = chromeDevTools.send(Target.getTargets());
		System.err.println("TargetInfos: " + targetInfos.toString());
		targetInfos.stream()
				.forEach(o -> System.err.println("TargetInfo:" + "\n" + "TargetId: "
						+ o.getTargetId() + "\n" + "Title: " + o.getTitle() + "\n"
						+ "Type: " + o.getType() + "\n" + "Url: " + o.getUrl() + "\n"
						+ "Attached: " + o.getAttached()));

	}

	// @Ignore
	@Test
	public void test2() {
		// Arrange
		// Act
		try {
			targetInfo = chromeDevTools.send(Target.getTargetInfo(Optional.empty()));
			System.err.println("TargetInfo: ");
			System.err.println("TargetId: " + targetInfo.getTargetId());
			System.err.println("Title: " + targetInfo.getTitle());
			System.err.println("Type: " + targetInfo.getType());
			System.err.println("Url: " + targetInfo.getUrl());
			System.err.println("Attached: " + targetInfo.getAttached());
		} catch (DevToolsException e) {
			System.err.println("DevToolsException exception " + "in test2"
					+ " (ignored): " + Utils.processExceptionMessage(e.getMessage()));
		}

		targetInfos = chromeDevTools.send(Target.getTargets());
		System.err.println("TargetInfos: " + targetInfos.toString());
	}

	@Test
	public void test3() {
		// Arrange
		baseURL = "https://www.google.com";
		driver.get(baseURL);
		Utils.sleep(1000);
		// Act
		try {
			sessionId = chromeDevTools.send(Target.attachToBrowserTarget());
			System.err.println("SessionId: " + sessionId);
		} catch (DevToolsException e) {
			System.err.println("DevToolsException exception " + "in test3"
					+ " (ignored): " + Utils.processExceptionMessage(e.getMessage()));
		}

	}
}