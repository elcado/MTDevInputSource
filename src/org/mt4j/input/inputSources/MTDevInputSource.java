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
		logger.setLevel(ILogger.INFO);
//		logger.setLevel(ILogger.DEBUG);
	}
	
	enum ABS_MT_CODE {
		/** Number of device's slots */
		ABS_MT_SLOT(0x2f),
		/** Major axis of touching ellipse */
		ABS_MT_TOUCH_MAJOR(0x30),
		/** Minor axis (omit if circular) */
		ABS_MT_TOUCH_MINOR(0x31),
		/** Major axis of approaching ellipse */
		ABS_MT_WIDTH_MAJOR(0x32),
		/** Minor axis (omit if circular) */
		ABS_MT_WIDTH_MINOR(0x33),
		/** Ellipse orientation */
		ABS_MT_ORIENTATION(0x34),
		/** Center X ellipse position */
		ABS_MT_POSITION_X(0x35),
		/** Center Y ellipse position */
		ABS_MT_POSITION_Y(0x36),
		/** Type of touching device */
		ABS_MT_TOOL_TYPE(0x37),
		/** Group a set of packets as a blob */
		ABS_MT_BLOB_ID(0x38),
		/** Unique ID of initiated contact */
		ABS_MT_TRACKING_ID(0x39),
		/** Pressure on contact area */
		ABS_MT_PRESSURE(0x3a),
		/** Contact hover distance */
		ABS_MT_DISTANCE(0x3b);

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
	 * Device name (DO NOT modify this field as it is directly accessed from native code)
	 */
	private String devName;

	/**
	 * Device caps
	 */
	private Map<ABS_MT_CODE, Interval<Integer>> abs_mt_caps = new HashMap<>();
	
	/**
	 * mtdev4j event callback (DO NOT modify this method as it is directly accessed from native code)
	 * 
	 * @param code {@link ABS_MT_CODE} capability code
	 * @param min capability min value
	 * @param max capability max value
	 */
	private void addCap(int code, int min, int max) {
		abs_mt_caps.put(ABS_MT_CODE.fromValue(code), new Interval<Integer>(min, max));
	}

	private AbstractMTApplication mtApp;
	private static boolean loaded = false;

	/**
	 * Build a mtdev on the supplied device.
	 * 
	 * @param devFileName
	 *            device events filename (smth like /dev/input/eventXX)
	 */
	public MTDevInputSource(AbstractMTApplication mtApp, String devFileName) {
		super(mtApp);

		this.mtApp = mtApp;

		if (!loaded){
			// load native lib
			System.loadLibrary("mtdev4j");

			// init/open device
			if (this.openDevice(devFileName)) {
				// get device capabilities
				this.loadDeviceCaps();

				// set as loaded to avoid multiple instances
				loaded = true;
			}
		}
		else {
			logger.error("MTDevInputSource may only be instantiated once.");
			return;
		}

	}

	@Override
	public void onRegistered() {
		// only register if correctly loaded
		if (!loaded) return;
		
		logger.info("Linux native mtdev device '" + devName + "'");
		for (Entry<ABS_MT_CODE, Interval<Integer>> cap : abs_mt_caps.entrySet()) {
			logger.debug(cap.getKey().name() + " " + cap.getValue().toString());
		}

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

	private Map<Integer, Long> slotIdToCursorID = new HashMap<>();
	private Map<Integer, MTDevInputEvt> slotIdToCurrentEvt = new HashMap<>();

	/**
	 * mtdev4j event callback (DO NOT modify this method as it is directly accessed from native code)
	 * 
	 * @param slotId slot id concerned by this event (in case evtType != SYN_REPORT, otherwise all slots are concerned)
	 * @param evtType event type (see {@link ABS_MT_TYPE})
	 * @param evtCode event code (see {@link ABS_MT_CODE})
	 * @param evtValue event value
	 */
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

		// handle ABS_MT_TRACKING_ID
		if (evtMTCode == ABS_MT_CODE.ABS_MT_TRACKING_ID) {
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
					return;

				// get MT4j cursor associated with this slot
				Long cursorId = slotIdToCursorID.get(slotId);
				if (cursorId == null)
					return;
				InputCursor inputCursor = ActiveCursorPool.getInstance().getActiveCursorByID(cursorId);

				// init an INPUT_ENDED event
				slotIdToCurrentEvt.put(slotId, new MTDevInputEvt(this, currentSlotEvt.getX(), currentSlotEvt.getY(),
					MTFingerInputEvt.INPUT_ENDED, inputCursor));
			}

		}
		else {
			bob.append("set " + evtMTCode.name());
			
			// cannot update if there is no current event
			if (currentSlotEvt == null) return;
			
			// check device capability
			Interval<Integer> capInterval = abs_mt_caps.get(evtMTCode);
			if (capInterval == null) return;
			
			// normalize value
			float normEvtValue = (float) evtValue;
			normEvtValue -= capInterval.getMin();
			normEvtValue /= capInterval.getLength();

			// switch on event code
			switch (evtMTCode) {
				case ABS_MT_TRACKING_ID:
					break;
				case ABS_MT_POSITION_X:
					// correct form device to screen coord
					float screenX = normEvtValue * this.mtApp.getWidth();
					currentSlotEvt.setScreenX(screenX);
	
					break;
				case ABS_MT_POSITION_Y:
					// correct form device to screen coord
					float screenY = normEvtValue * this.mtApp.getWidth();
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
					break;
			}
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

class Interval<T extends Number> {
	T min;
	T max;
	
	public Interval(T min,	T max) {
		this.min = min;
		this.max = max;
	}
	
	public T getMin() {
		return min;
	}
	
	public T getMax() {
		return max;
	}
	
	public double getLength() {
		return (getMax().doubleValue() - getMin().doubleValue());
	}
	
	@Override
	public String toString() {
		return "[" + min + ";" + max + "]";
	}
}
