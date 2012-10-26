package org.mt4j.input.inputSources;

import org.mt4j.MTApplication;
import org.mt4j.input.InputManager;

import basic.helloWorld.HelloWorldScene;

public class Test extends MTApplication {
	/** main */
	public static void main(String[] args) {
		initialize();
	}

	@Override
	public void startUp() {
		InputManager inputManager = this.getInputManager();
		inputManager.registerInputSource(new MTDevInputSource(this, "/dev/input/event4"));
//		inputManager.registerInputSource(new MTDevInputSource(this, "/dev/input/event25"));

		// build scene
		this.addScene(new HelloWorldScene(this, ""));
	}
}
