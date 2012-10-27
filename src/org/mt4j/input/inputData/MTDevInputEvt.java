package org.mt4j.input.inputData;

import org.mt4j.input.inputSources.AbstractInputSource;

public class MTDevInputEvt  extends MTFingerInputEvt {
	
	/** Major axis of touching ellipse */
	private float majorTouch;
	/** Minor axis of touching ellipse */
	private float minorTouch;

	public MTDevInputEvt(AbstractInputSource source, float positionX, float positionY, int id, InputCursor m) {
		this(source, positionX, positionY, 15, 15, id, m);
	}
	
	public MTDevInputEvt(AbstractInputSource source, float positionX, float positionY, float majorTouch, float minorTouch, int id, InputCursor m) {
		super(source, positionX, positionY, id, m);

		this.majorTouch = majorTouch;
		this.minorTouch = minorTouch;
	}
	
	public void setMajorTouch(float majorTouch) {
		this.majorTouch = majorTouch;
	}
	
	/** Get major axis of touching ellipse */
	public float getMajorTouch() {
		return majorTouch;
	}
	
	public void setMinorTouch(float minorTouch) {
		this.minorTouch = minorTouch;
	}
	
	/** Get minor axis of touching ellipse */
	public float getMinorTouch() {
		return minorTouch;
	}
}
