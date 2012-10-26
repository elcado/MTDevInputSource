package org.mt4j.input.inputSources;

public class Test {

	/** main */
	public static void main(String[] args) {
		MTDevInputSource mtdevice = null;
		try {
			mtdevice = new MTDevInputSource("/dev/input/event25");
			
			// work with device
			;
		}
		finally {
			if (mtdevice != null)
				mtdevice.closeMTDevice();
		}
	}
}
