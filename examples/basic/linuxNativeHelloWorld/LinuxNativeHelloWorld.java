package basic.linuxNativeHelloWorld;

import org.mt4j.MTApplication;
import org.mt4j.input.InputManager;
import org.mt4j.input.inputSources.MTDevInputSource;

import basic.helloWorld.HelloWorldScene;

public class LinuxNativeHelloWorld extends MTApplication {
	public static void main(String[] args) {
		initialize();
	}

	@Override
	public void startUp() {
		InputManager inputManager = this.getInputManager();
		inputManager.registerInputSource(new MTDevInputSource(this, "/dev/input/event4"));

		// build scene
		this.addScene(new HelloWorldScene(this, "Test - Linux native mtdev support"));
	}
}
