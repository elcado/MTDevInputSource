package org.mt4j.input.inputSources;


public class MTDevInputSource {//extends AbstractInputSource {
	
	static {
		// load native lib
		System.loadLibrary("mtdev4j");
	}

	/*
	 * Native functions
	 */
	
	/** Open the device. */
	private native boolean openDevice(String devFileName);
	/** Load mtdev capabillties. */
	private native void loadDeviceCaps();
	/** Close the device. */
	private native void closeDevice();
	
	/*
	 * Device caps (DO NOT modify these fileds as they are directly accessed in native code)
	 */
	
	/** MT slot being modified */
	private int ABS_MT_SLOT;
	private boolean is_ABS_MT_SLOT = false;
	/** Major axis of touching ellipse */
	private int ABS_MT_TOUCH_MAJOR;
	private boolean is_ABS_MT_TOUCH_MAJOR = false;
	/** Minor axis (omit if circular) */
	private int ABS_MT_TOUCH_MINOR;
	private boolean is_ABS_MT_TOUCH_MINOR = false;
	/** Major axis of approaching ellipse */
	private int ABS_MT_WIDTH_MAJOR;
	private boolean is_ABS_MT_WIDTH_MAJOR = false;
	/** Minor axis (omit if circular) */
	private int ABS_MT_WIDTH_MINOR;
	private boolean is_ABS_MT_WIDTH_MINOR = false;
	/** Ellipse orientation */
	private int ABS_MT_ORIENTATION;
	private boolean is_ABS_MT_ORIENTATION = false;
	/** Center X ellipse position */
	private int ABS_MT_POSITION_X;
	private boolean is_ABS_MT_POSITION_X = false;
	/** Center Y ellipse position */
	private int ABS_MT_POSITION_Y;
	private boolean is_ABS_MT_POSITION_Y = false;
	/** Type of touching device */
	private int ABS_MT_TOOL_TYPE;
	private boolean is_ABS_MT_TOOL_TYPE = false;
	/** Group a set of packets as a blob */
	private int ABS_MT_BLOB_ID;
	private boolean is_ABS_MT_BLOB_ID = false;
	/** Unique ID of initiated contact */
	private int ABS_MT_TRACKING_ID;
	private boolean is_ABS_MT_TRACKING_ID = false;
	/** Pressure on contact area */
	private int ABS_MT_PRESSURE;
	private boolean is_ABS_MT_PRESSURE = false;
	/** Contact hover distance */
	private int ABS_MT_DISTANCE;
	private boolean is_ABS_MT_DISTANCE = false;

//	public MTDevInputSource(AbstractMTApplication mtApp) {
//		super(mtApp);
//	}

	/**
	 * Build a mtdev on the supplied device.
	 * 
	 * @param devFileName device events filename (smth like /dev/input/eventXX)
	 */
	public MTDevInputSource(String devFileName) {
		if (this.openDevice(devFileName)) {
			// get device capabilities
			this.loadDeviceCaps();
			System.out.println(this);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		this.closeMTDevice();

		super.finalize();
	}

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
