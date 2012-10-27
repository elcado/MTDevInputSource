package basic.linuxNativeHelloWorld;

import org.mt4j.MTApplication;
import org.mt4j.input.InputManager;
import org.mt4j.input.inputSources.AbstractInputSource;
import org.mt4j.input.inputSources.MTDevInputSource;
import org.mt4j.sceneManagement.AbstractScene;

public class LinuxNativeHelloWorld extends MTApplication {
	public static void main(String[] args) {
		initialize();
	}

	@Override
	public void exit() {
		AbstractInputSource mtdevInputSource = null;
		InputManager inputManager = this.getInputManager();
		
		// find mtdev input source
		for (AbstractInputSource inputSource : inputManager.getInputSources()) {
			if (inputSource instanceof MTDevInputSource) {
				mtdevInputSource = inputSource;
			}	
		}
		
		// unregister mtdev input source
		if (mtdevInputSource != null)
			inputManager.unregisterInputSource(mtdevInputSource);
		
		super.exit();
	}
	
	@Override
	public void startUp() {
		InputManager inputManager = this.getInputManager();
		inputManager.registerInputSource(new MTDevInputSource(this, "/dev/input/event4"));

		// build scene
		this.addScene(new AbstractScene(this, "Test - Linux native mtdev support") {
			@Override
			public void onEnter() {
//				this.registerGlobalInputProcessor(new CursorTracer(LinuxNativeHelloWorld.this, this));
				this.registerGlobalInputProcessor(new MTDevCursorTracer(LinuxNativeHelloWorld.this, this));
			}
		});
	}
}
