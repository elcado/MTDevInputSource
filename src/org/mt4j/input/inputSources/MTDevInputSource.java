/**
 * Copyright 2012 Frédéric Cadier <f.cadier@free.fr>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see
 * <http://www.gnu.org/licenses/>.
 */
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
import org.mt4j.input.inputSources.MTDevInputSource.ABS_MT_CONSTANT;
import org.mt4j.input.inputSources.MTDevInputSource.SYN_CONSTANT;
import org.mt4j.util.logging.ILogger;
import org.mt4j.util.logging.MTLoggerFactory;

/**
 * Linux native mtdev input source.
 * 
 * @author Frédéric Cadier
 */
public class MTDevInputSource extends AbstractInputSource implements Cmtdev4j {
	/** The Constant logger. */
	private static final ILogger logger = MTLoggerFactory.getLogger(MTDevInputSource.class.getName());
	static {
		logger.setLevel(ILogger.ERROR);
		logger.setLevel(ILogger.INFO);
//		logger.setLevel(ILogger.DEBUG);
	}
	
	/**
	 * ABS_MT_* constants defined in /usr/include/linux/input.h
	 */
	enum ABS_MT_CONSTANT {
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

		private ABS_MT_CONSTANT(int numericValue) {
			this.numericValue = numericValue;
		}

		public static ABS_MT_CONSTANT fromValue(int numericValue) {
			for (ABS_MT_CONSTANT abs_mt_const : ABS_MT_CONSTANT.values())
				if (abs_mt_const.numericValue == numericValue)
					return abs_mt_const;
			return null;
		}
	}

	/**
	 * SYN_* constants defined in /usr/include/linux/input.h
	 */
	enum SYN_CONSTANT {
		SYN_REPORT(0x00);

		private int numericValue;

		private SYN_CONSTANT(int numericValue) {
			this.numericValue = numericValue;
		}

		public static SYN_CONSTANT fromValue(int numericValue) {
			for (SYN_CONSTANT abs_mt_type : SYN_CONSTANT.values())
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
	 * Device name
	 */
	private String devName;
	
	/* (non-Javadoc)
	 * @see org.mt4j.input.inputSources.Cmtdev4j#setDevName(java.lang.String)
	 */
	@Override
	public void setDevName(String devName) { this.devName = devName; }

	/**
	 * Device caps
	 */
	private Map<ABS_MT_CONSTANT, Interval<Integer>> abs_mt_caps = new HashMap<>();
	
	/* (non-Javadoc)
	 * @see org.mt4j.input.inputSources.Cmtdev4j#addCap(int, int, int)
	 */
	@Override
	public void addCap(int code, int min, int max) {
		abs_mt_caps.put(ABS_MT_CONSTANT.fromValue(code), new Interval<Integer>(min, max));
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
		for (Entry<ABS_MT_CONSTANT, Interval<Integer>> cap : abs_mt_caps.entrySet()) {
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

	/* (non-Javadoc)
	 * @see org.mt4j.input.inputSources.Cmtdev4j#onMTDevTouch(int, int, int, int)
	 */
	@Override
	public void onMTDevTouch(int slotId, int evtType, int evtCode, int evtValue) {
		// SYN_REPORT
		if (SYN_CONSTANT.fromValue(evtType) == SYN_CONSTANT.SYN_REPORT) {
			logger.debug("SYN_REPORT");

			// fire all built MTDevInputEvt events
			fireAllBuiltEvents();
			return;
		}

		// debug information
		StringBuilder bob = new StringBuilder();
		bob.append("[" + slotId + "] ");

		// parse evtCode and return if not handled
		ABS_MT_CONSTANT evtMTCode = ABS_MT_CONSTANT.fromValue(evtCode);
		if (evtMTCode == null)
			return;

		// get current slot event
		MTDevInputEvt currentSlotEvt = slotIdToCurrentEvt.get(slotId);

		// handle ABS_MT_TRACKING_ID
		if (evtMTCode == ABS_MT_CONSTANT.ABS_MT_TRACKING_ID) {
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
				slotIdToCurrentEvt.put(slotId, new MTDevInputEvt(this,
					currentSlotEvt.getX(), currentSlotEvt.getY(),
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
				case ABS_MT_TRACKING_ID: break;
				case ABS_MT_POSITION_X:
					// correct form device to screen coord
					float screenX = normEvtValue * this.mtApp.getWidth();
					currentSlotEvt.setScreenX(screenX);
	
					break;
				case ABS_MT_POSITION_Y:
					// correct form device to screen coord
					float screenY = normEvtValue * this.mtApp.getHeight();
					currentSlotEvt.setScreenY(screenY);
	
					break;
				case ABS_MT_BLOB_ID: break;
				case ABS_MT_DISTANCE: break;
				case ABS_MT_ORIENTATION: break;
				case ABS_MT_PRESSURE: break;
				case ABS_MT_SLOT: break;
				case ABS_MT_TOOL_TYPE: break;
				case ABS_MT_TOUCH_MAJOR: break;
				case ABS_MT_TOUCH_MINOR: break;
				case ABS_MT_WIDTH_MAJOR: break;
				case ABS_MT_WIDTH_MINOR: break;
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
						slotIdToCurrentEvt.put(slotId, new MTDevInputEvt(this,
							pendingEvent.getX(), pendingEvent.getY(),
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

		// clean internal maps
		for (Integer slotId : slotIdToFree) {
			slotIdToCurrentEvt.remove(slotId);
			slotIdToCursorID.remove(slotId);

		}

		return;
	}
}

/**
 * This interface describes methods called by the native mtdev4j library. DO NOT modify this interface, otherwise the native will fail and crash the
 * JVM.
 * 
 * @author Frédéric Cadier
 */
interface Cmtdev4j {

	/**
	 * Set the mtdev device's friendly name
	 * 
	 * @param devName device name
	 */
	public abstract void setDevName(String devName);

	/**
	 * Call this to add a mtdev capability.
	 * 
	 * @param code
	 *            {@link ABS_MT_CONSTANT} capability code
	 * @param min
	 *            capability min value
	 * @param max
	 *            capability max value
	 */
	public abstract void addCap(int code, int min, int max);

	/**
	 * Event callback: call this for each mtdev event.
	 * 
	 * @param slotId
	 *            slot id concerned by this event (in case evtType != SYN_REPORT, otherwise all slots are concerned)
	 * @param evtType
	 *            event type (see {@link SYN_CONSTANT})
	 * @param evtCode
	 *            event code (see {@link ABS_MT_CONSTANT})
	 * @param evtValue
	 *            event value
	 */
	public abstract void onMTDevTouch(int slotId, int evtType, int evtCode, int evtValue);

}

/**
 * Helper class for numeric interval handling.
 * 
 * @author Frédéric Cadier
 *
 * @param <T> interval's type: T must extends Number
 */
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
