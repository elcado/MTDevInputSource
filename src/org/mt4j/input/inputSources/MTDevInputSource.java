package org.mt4j.input.inputSources;

public class MTDevInputSource {

	static {
		// load native lib
		System.loadLibrary("mtdev4j");
	}

	/*
	 * Native functions
	 */
	private native static MTDevInputSource openDevice(String dev);

	private native void closeDevice();

	public static MTDevInputSource openMTDevice(String devFileName) {
		MTDevInputSource device = MTDevInputSource.openDevice(devFileName);

		// try to open device
		if (device != null) {
			System.out.println(device);
		}

		return device;
	}

	/*
	 * Device caps
	 */
	private int ABS_MT_SLOT; /* MT slot being modified */
	private boolean is_ABS_MT_SLOT = false;
	private int ABS_MT_TOUCH_MAJOR; /* Major axis of touching ellipse */
	private boolean is_ABS_MT_TOUCH_MAJOR = false;
	private int ABS_MT_TOUCH_MINOR; /* Minor axis (omit if circular) */
	private boolean is_ABS_MT_TOUCH_MINOR = false;
	private int ABS_MT_WIDTH_MAJOR; /* Major axis of approaching ellipse */
	private boolean is_ABS_MT_WIDTH_MAJOR = false;
	private int ABS_MT_WIDTH_MINOR; /* Minor axis (omit if circular) */
	private boolean is_ABS_MT_WIDTH_MINOR = false;
	private int ABS_MT_ORIENTATION; /* Ellipse orientation */
	private boolean is_ABS_MT_ORIENTATION = false;
	private int ABS_MT_POSITION_X; /* Center X ellipse position */
	private boolean is_ABS_MT_POSITION_X = false;
	private int ABS_MT_POSITION_Y; /* Center Y ellipse position */
	private boolean is_ABS_MT_POSITION_Y = false;
	private int ABS_MT_TOOL_TYPE; /* Type of touching device */
	private boolean is_ABS_MT_TOOL_TYPE = false;
	private int ABS_MT_BLOB_ID; /* Group a set of packets as a blob */
	private boolean is_ABS_MT_BLOB_ID = false;
	private int ABS_MT_TRACKING_ID; /* Unique ID of initiated contact */
	private boolean is_ABS_MT_TRACKING_ID = false;
	private int ABS_MT_PRESSURE; /* Pressure on contact area */
	private boolean is_ABS_MT_PRESSURE = false;
	private int ABS_MT_DISTANCE; /* Contact hover distance */
	private boolean is_ABS_MT_DISTANCE = false;

	public void closeMTDevice() {
		this.closeDevice();
	}

	@Override
	public String toString() {
		StringBuilder bob = new StringBuilder();

		bob.append("Device caps:\n");
		if (this.is_ABS_MT_SLOT)
			bob.append("\tABS_MT_SLOT:" + this.ABS_MT_SLOT + "\n");
		if (this.is_ABS_MT_TOUCH_MAJOR)
			bob.append("\tABS_MT_TOUCH_MAJOR:" + this.ABS_MT_TOUCH_MAJOR + "\n");
		if (this.is_ABS_MT_TOUCH_MINOR)
			bob.append("\tABS_MT_TOUCH_MINOR:" + this.ABS_MT_TOUCH_MINOR + "\n");
		if (this.is_ABS_MT_WIDTH_MAJOR)
			bob.append("\tABS_MT_WIDTH_MAJOR:" + this.ABS_MT_WIDTH_MAJOR + "\n");
		if (this.is_ABS_MT_WIDTH_MINOR)
			bob.append("\tABS_MT_WIDTH_MINOR:" + this.ABS_MT_WIDTH_MINOR + "\n");
		if (this.is_ABS_MT_ORIENTATION)
			bob.append("\tABS_MT_ORIENTATION:" + this.ABS_MT_ORIENTATION + "\n");
		if (this.is_ABS_MT_POSITION_X)
			bob.append("\tABS_MT_POSITION_X:" + this.ABS_MT_POSITION_X + "\n");
		if (this.is_ABS_MT_POSITION_Y)
			bob.append("\tABS_MT_POSITION_Y:" + this.ABS_MT_POSITION_Y + "\n");
		if (this.is_ABS_MT_TOOL_TYPE)
			bob.append("\tABS_MT_TOOL_TYPE:" + this.ABS_MT_TOOL_TYPE + "\n");
		if (this.is_ABS_MT_BLOB_ID)
			bob.append("\tABS_MT_BLOB_ID:" + this.ABS_MT_BLOB_ID + "\n");
		if (this.is_ABS_MT_TRACKING_ID)
			bob.append("\tABS_MT_TRACKING_ID:" + this.ABS_MT_TRACKING_ID + "\n");
		if (this.is_ABS_MT_PRESSURE)
			bob.append("\tABS_MT_PRESSURE:" + this.ABS_MT_PRESSURE + "\n");
		if (this.is_ABS_MT_DISTANCE)
			bob.append("\tABS_MT_DISTANCE:" + this.ABS_MT_DISTANCE + "\n");

		return bob.toString();
	}
}
