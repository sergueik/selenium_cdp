package com.github.sergueik.selenium;

/**
 * Copyright 2022-2024 Serguei Kouzmine
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.devtools.DevToolsException;

// NOTE letter case in the class name 
import org.openqa.selenium.devtools.v138.target.Target;
import org.openqa.selenium.devtools.v138.target.model.SessionID;
import org.openqa.selenium.devtools.v138.target.model.TargetID;
import org.openqa.selenium.devtools.v138.target.model.TargetInfo;

/**
 * Selected test scenarios for Selenium Chrome Developer Tools Selenium 4 bridge
 * see:
 * https://chromedevtools.github.io/devtools-protocol/tot/Target/#method-createTarget
 * https://chromedevtools.github.io/devtools-protocol/tot/Target/#method-attachToTarget
 * https://chromedevtools.github.io/devtools-protocol/tot/Target/#method-getTargets
 * https://chromedevtools.github.io/devtools-protocol/tot/Target/#method-getTargetInfo
 * https://chromedevtools.github.io/devtools-protocol/tot/Target/#type-TargetInfo
 * https://chromedevtools.github.io/devtools-protocol/tot/Target/#type-SessionID
 * https://chromedevtools.github.io/devtools-protocol/tot/Target/#method-attachToBrowserTarget
 */

// NOTE: the "Target.createTarget" method signature change between 109 and 119:
// 109:
// java.lang.String,java.util.Optional<java.lang.Integer>,java.util.Optional<java.lang.Integer>,java.util.Optional<org.openqa.selenium.devtools.v109.browser.model.BrowserContextID>,java.util.Optional<java.lang.Boolean>,java.util.Optional<java.lang.Boolean>,java.util.Optional<java.lang.Boolean>
// 119:
// java.lang.String,java.util.Optional<java.lang.Integer>,java.util.Optional<java.lang.Integer>,java.util.Optional<org.openqa.selenium.devtools.v138.browser.model.BrowserContextID>,java.util.Optional<java.lang.Boolean>,java.util.Optional<java.lang.Boolean>,java.util.Optiona<java.lang.Boolean>,java.util.Optional<java.lang.Boolean>

public class WindowsTabsDevToolsTest extends BaseDevToolsTest {

	private TargetID targetId = null;
	private SessionID sessionId = null;
	private TargetInfo targetInfo = null;
	private List<TargetInfo> targetInfos = new ArrayList<>();
	private final String baseURL = "https://en.wikipedia.org/wiki/Main_Page";

	@Before
	public void before() throws Exception {
	}

	@Test
	public void test1() {
		// Arrange

		// Act
		targetId = chromeDevTools.send(Target.createTarget(baseURL, Optional.of(0),
				Optional.of(0), Optional.empty(), null, null, null, Optional.of(false), Optional.of(true),
				Optional.of(false), Optional.of(false), null));
		Utils.sleep(1000);
		System.err.println("TargetID: " + targetId);
		sessionId = chromeDevTools
				.send(Target.attachToTarget(targetId, Optional.empty()));
		System.err.println("SessionId: " + sessionId);
		targetInfo = chromeDevTools
				.send(Target.getTargetInfo(Optional.of(targetId)));
		System.err.println("TargetInfo: " + "\n" + "TargetId: "
				+ targetInfo.getTargetId() + "\n" + "Title: " + targetInfo.getTitle()
				+ "\n" + "Type: " + targetInfo.getType() + "\n" + "Url: "
				+ targetInfo.getUrl() + "\n" + "Attached: " + targetInfo.getAttached());

	}

	@Test
	public void test2() {
		// Arrange
		targetInfo = chromeDevTools.send(Target.getTargetInfo(Optional.empty()));
		System.err.println("TargetInfo: " + "\n" + "TargetId: "
				+ targetInfo.getTargetId() + "\n" + "Title: " + targetInfo.getTitle()
				+ "\n" + "Type: " + targetInfo.getType() + "\n" + "Url: "
				+ targetInfo.getUrl() + "\n" + "Attached: " + targetInfo.getAttached());

		// Act
		targetInfos = chromeDevTools.send(Target.getTargets(Optional.empty()));
		System.err.println("TargetInfos: " + targetInfos.toString());
		targetInfos.stream()
				.forEach(o -> System.err.println("TargetInfo: " + "\n" + "TargetId: "
						+ o.getTargetId() + "\n" + "Title: " + o.getTitle() + "\n"
						+ "Type: " + o.getType() + "\n" + "Url: " + o.getUrl() + "\n"
						+ "Attached: " + o.getAttached() + "\n" + "\n"));
	}

	@Test
	public void test3() {
		// Arrange

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

	@Test()
	public void test4() {
		// Arrange
		chromeDevTools.addListener(Target.targetCreated(),
				(TargetInfo o) -> System.err.println("TargetInfo: " + "\n"
						+ "TargetId: " + o.getTargetId() + "\n" + "Title: " + o.getTitle()
						+ "\n" + "Type: " + o.getType() + "\n" + "Url: " + o.getUrl() + "\n"
						+ "Attached: " + o.getAttached() + "\n" + "\n"));

		// Act
		targetId = chromeDevTools.send(Target.createTarget(baseURL, Optional.of(0),
				Optional.of(0), Optional.empty(), null, null, null, Optional.of(false), Optional.of(true),
				Optional.of(false), Optional.of(false), null));
		Utils.sleep(1000);
	}

}

