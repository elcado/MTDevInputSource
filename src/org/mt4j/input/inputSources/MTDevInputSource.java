package org.mt4j.input.inputSources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.mt4j.AbstractMTApplication;
import org.mt4j.input.inputData.ActiveCursorPool;
import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputData.MTDevInputEvt;
import org.mt4j.input.inputData.MTFingerInputEvt;
import org.mt4j.util.logging.ILogger;
import org.mt4j.util.logging.MTLoggerFactory;

public class MTDevInputSource extends AbstractInputSource {
	/** The Constant logger. */
	private static final ILogger logger = MTLoggerFactory.getLogger(MTDevInputSource.class.getName());
	static {
		logger.setLevel(ILogger.ERROR);
//		logger.setLevel(ILogger.DEBUG);
		logger.setLevel(ILogger.INFO);
	}

	static {
		// load native lib
		System.loadLibrary("mtdev4j");
	}

	/*
	 * Native functions
	 */

	/** Open the device. */
	private native boolean openDevice(String devFileName);
	/** Load mtdev capabilities. */
	private native void loadDeviceCaps();
	/** Starts the event loop. */
	private native void startEventLoop();
	/** Close the device. */
	private native void closeDevice();

	/**
	 * Device name (DO NOT modify this filed as it is directly accessed from native code)
	 */
	private String devName;

	/*
	 * Device caps (DO NOT modify these fileds as they are directly accessed from native code)
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

	private AbstractMTApplication mtApp;

	/**
	 * Build a mtdev on the supplied device.
	 * 
	 * @param devFileName
	 *            device events filename (smth like /dev/input/eventXX)
	 */
	public MTDevInputSource(AbstractMTApplication mtApp, String devFileName) {
		super(mtApp);

		this.mtApp = mtApp;

		// init/open device
		if (this.openDevice(devFileName))
			// get device capabilities
			this.loadDeviceCaps();
	}

	@Override
	public void onRegistered() {
		logger.info("Linux native mtdev device '" + devName + "'");
		if (this.is_ABS_MT_SLOT)
			logger.debug("ABS_MT_SLOT:" + this.ABS_MT_SLOT);
		if (this.is_ABS_MT_TOUCH_MAJOR)
			logger.debug("ABS_MT_TOUCH_MAJOR:" + this.ABS_MT_TOUCH_MAJOR);
		if (this.is_ABS_MT_TOUCH_MINOR)
			logger.debug("ABS_MT_TOUCH_MINOR:" + this.ABS_MT_TOUCH_MINOR);
		if (this.is_ABS_MT_WIDTH_MAJOR)
			logger.debug("ABS_MT_WIDTH_MAJOR:" + this.ABS_MT_WIDTH_MAJOR);
		if (this.is_ABS_MT_WIDTH_MINOR)
			logger.debug("ABS_MT_WIDTH_MINOR:" + this.ABS_MT_WIDTH_MINOR);
		if (this.is_ABS_MT_ORIENTATION)
			logger.debug("ABS_MT_ORIENTATION:" + this.ABS_MT_ORIENTATION);
		if (this.is_ABS_MT_POSITION_X)
			logger.debug("ABS_MT_POSITION_X:" + this.ABS_MT_POSITION_X);
		if (this.is_ABS_MT_POSITION_Y)
			logger.debug("ABS_MT_POSITION_Y:" + this.ABS_MT_POSITION_Y);
		if (this.is_ABS_MT_TOOL_TYPE)
			logger.debug("ABS_MT_TOOL_TYPE:" + this.ABS_MT_TOOL_TYPE);
		if (this.is_ABS_MT_BLOB_ID)
			logger.debug("ABS_MT_BLOB_ID:" + this.ABS_MT_BLOB_ID);
		if (this.is_ABS_MT_TRACKING_ID)
			logger.debug("ABS_MT_TRACKING_ID:" + this.ABS_MT_TRACKING_ID);
		if (this.is_ABS_MT_PRESSURE)
			logger.debug("ABS_MT_PRESSURE:" + this.ABS_MT_PRESSURE);
		if (this.is_ABS_MT_DISTANCE)
			logger.debug("ABS_MT_DISTANCE:" + this.ABS_MT_DISTANCE);

		new Thread(new Runnable() {
			@Override
			public void run() {
				// start getting touch event
				MTDevInputSource.this.startEventLoop();
			}
		}).start();

		super.onRegistered();
	}

	@Override
	public void onUnregistered() {
		// close mtdev device
		this.closeMTDevice();

		super.onUnregistered();
	}

	public void closeMTDevice() {
		logger.info("Closing Linux native mtdev device '" + devName + "'");

		this.closeDevice();
	}

	/*
	 * Event handling code
	 */

	enum ABS_MT_CODE {
		ABS_MT_SLOT(0x2f), /* MT slot being modified */
		ABS_MT_TOUCH_MAJOR(0x30), /* Major axis of touching ellipse */
		ABS_MT_TOUCH_MINOR(0x31), /* Minor axis (omit if circular) */
		ABS_MT_WIDTH_MAJOR(0x32), /* Major axis of approaching ellipse */
		ABS_MT_WIDTH_MINOR(0x33), /* Minor axis (omit if circular) */
		ABS_MT_ORIENTATION(0x34), /* Ellipse orientation */
		ABS_MT_POSITION_X(0x35), /* Center X ellipse position */
		ABS_MT_POSITION_Y(0x36), /* Center Y ellipse position */
		ABS_MT_TOOL_TYPE(0x37), /* Type of touching device */
		ABS_MT_BLOB_ID(0x38), /* Group a set of packets as a blob */
		ABS_MT_TRACKING_ID(0x39), /* Unique ID of initiated contact */
		ABS_MT_PRESSURE(0x3a), /* Pressure on contact area */
		ABS_MT_DISTANCE(0x3b); /* Contact hover distance */

		private int numericValue;

		private ABS_MT_CODE(int numericValue) {
			this.numericValue = numericValue;
		}

		public static ABS_MT_CODE fromValue(int numericValue) {
			for (ABS_MT_CODE abs_mt_const : ABS_MT_CODE.values())
				if (abs_mt_const.numericValue == numericValue)
					return abs_mt_const;
			return null;
		}
	}

	enum ABS_MT_TYPE {
		SYN_REPORT(0x00);

		private int numericValue;

		private ABS_MT_TYPE(int numericValue) {
			this.numericValue = numericValue;
		}

		public static ABS_MT_TYPE fromValue(int numericValue) {
			for (ABS_MT_TYPE abs_mt_type : ABS_MT_TYPE.values())
				if (abs_mt_type.numericValue == numericValue)
					return abs_mt_type;
			return null;
		}
	}

	private Map<Integer, Long> slotIdToCursorID = new HashMap<>();
	private Map<Integer, MTDevInputEvt> slotIdToCurrentEvt = new HashMap<>();

	private void onMTDevTouch(int slotId, int evtType, int evtCode, int evtValue) {
		// SYN_REPORT
		if (ABS_MT_TYPE.fromValue(evtType) == ABS_MT_TYPE.SYN_REPORT) {
			logger.debug("SYN_REPORT");

			// fire all built MTDevInputEvt events
			fireAllBuiltEvents();
			return;
		}

		// debug information
		StringBuilder bob = new StringBuilder();
		bob.append("[" + slotId + "] ");

		// parse evtCode and return if not handled
		ABS_MT_CODE evtMTCode = ABS_MT_CODE.fromValue(evtCode);
		if (evtMTCode == null)
			return;

		// get current slot event
		MTDevInputEvt currentSlotEvt = slotIdToCurrentEvt.get(slotId);

		// switch on event code
		switch (evtMTCode) {
			case ABS_MT_TRACKING_ID:
				// ABS_MT_TRACKING_ID:
				// - evtValue >= 0 -> starts MTDevInputEvt event
				if (evtValue >= 0) {
					bob.append("build INPUT_STARTED");

					// build MT4j cursor
					InputCursor inputCursor = new InputCursor();
					long cursorId = inputCursor.getId();
					ActiveCursorPool.getInstance().putActiveCursor(cursorId, inputCursor);
					slotIdToCursorID.put(slotId, cursorId);

					// init an INPUT_STARTED event
					slotIdToCurrentEvt.put(slotId, new MTDevInputEvt(this, 0, 0, MTFingerInputEvt.INPUT_STARTED, inputCursor));
				}

				// ABS_MT_TRACKING_ID:
				// - evtValue == -1 -> ends MTDevInputEvt event
				else {
					bob.append("build INPUT_ENDED");

					// cannot end if there is no current event
					if (currentSlotEvt == null)
						break;

					// get MT4j cursor associated with this slot
					Long cursorId = slotIdToCursorID.get(slotId);
					if (cursorId == null)
						break;
					InputCursor inputCursor = ActiveCursorPool.getInstance().getActiveCursorByID(cursorId);

					// init an INPUT_ENDED event
					slotIdToCurrentEvt.put(slotId, new MTDevInputEvt(this, currentSlotEvt.getX(), currentSlotEvt.getY(),
						MTFingerInputEvt.INPUT_ENDED, inputCursor));
				}

				break;
			case ABS_MT_POSITION_X:
				bob.append("set ABS_MT_POSITION_X");

				// cannot update if there is no current event
				if (currentSlotEvt == null)
					break;

				// correct form device to screen coord
				float screenX = ((float) evtValue / this.ABS_MT_POSITION_X) * this.mtApp.getWidth();

				// set x
				currentSlotEvt.setScreenX(screenX);

				break;
			case ABS_MT_POSITION_Y:
				bob.append("set ABS_MT_POSITION_Y");

				// cannot update if there is no current event
				if (currentSlotEvt == null)
					break;

				// correct form device to screen coord
				float screenY = ((float) evtValue / this.ABS_MT_POSITION_Y) * this.mtApp.getHeight();

				// set y
				currentSlotEvt.setScreenY(screenY);

				break;
			case ABS_MT_BLOB_ID:
			case ABS_MT_DISTANCE:
			case ABS_MT_ORIENTATION:
			case ABS_MT_PRESSURE:
			case ABS_MT_SLOT:
			case ABS_MT_TOOL_TYPE:
			case ABS_MT_TOUCH_MAJOR:
			case ABS_MT_TOUCH_MINOR:
			case ABS_MT_WIDTH_MAJOR:
			case ABS_MT_WIDTH_MINOR:
				bob.append(evtMTCode.name() + " -> " + evtValue);
				break;
		}

		logger.debug(bob);

		return;
	}

	private void fireAllBuiltEvents() {
		// handle all built MTDevInputEvt events
		List<Integer> slotIdToFree = new ArrayList<>();
		for (Entry<Integer, MTDevInputEvt> slottedEvt : slotIdToCurrentEvt.entrySet()) {
			Integer slotId = slottedEvt.getKey();
			MTDevInputEvt pendingEvent = slottedEvt.getValue();

			if (pendingEvent != null) {
				// fire event
				this.enqueueInputEvent(pendingEvent);

				// either prepare next event (for update), or clean resource
				switch (pendingEvent.getId()) {
					case MTFingerInputEvt.INPUT_STARTED:
					case MTFingerInputEvt.INPUT_UPDATED:
						// get MT4j cursor associated with this slot
						Long cursorId = slotIdToCursorID.get(slotId);
						if (cursorId == null)
							break;
						InputCursor inputCursor = ActiveCursorPool.getInstance().getActiveCursorByID(cursorId);

						// init an INPUT_UPDATED event for next mtdev events
						slotIdToCurrentEvt.put(slotId, new MTDevInputEvt(this, pendingEvent.getX(), pendingEvent.getY(),
							MTFingerInputEvt.INPUT_UPDATED, inputCursor));

						break;
					case MTFingerInputEvt.INPUT_ENDED:
						// clean MT4j cursor
						cursorId = slotIdToCursorID.get(slotId);
						if (cursorId == null)
							break;
						ActiveCursorPool.getInstance().removeCursor(cursorId);

						// (re)init current event
						slotIdToFree.add(slotId);

						break;
				}
			}
		}

		// clean maps
		for (Integer slotId : slotIdToFree) {
			slotIdToCurrentEvt.remove(slotId);
			slotIdToCursorID.remove(slotId);

		}

		return;
	}
}
