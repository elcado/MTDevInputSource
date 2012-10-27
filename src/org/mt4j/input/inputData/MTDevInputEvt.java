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
package org.mt4j.input.inputData;

import org.mt4j.input.inputSources.AbstractInputSource;

/**
 * Linux native mtdev input event.
 * 
 * @author Frédéric Cadier
 */
public class MTDevInputEvt  extends MTFingerInputEvt {
	
	/** Orientation of touching ellipse */
	private float orientationTouch;
	
	/** Major axis of touching ellipse */
	private float majorTouch;
	/** Minor axis of touching ellipse */
	private float minorTouch;

	public MTDevInputEvt(AbstractInputSource source, float positionX, float positionY, int id, InputCursor m) {
		this(source, positionX, positionY, 0, 15, 15, id, m);
	}
	
	public MTDevInputEvt(AbstractInputSource source, float positionX, float orientationTouch, float positionY, float majorTouch, float minorTouch, int id, InputCursor m) {
		super(source, positionX, positionY, id, m);

		this.orientationTouch = orientationTouch;
		this.majorTouch = majorTouch;
		this.minorTouch = minorTouch;
	}
	
	public void setOrientationTouch(float orientationTouch) {
		this.orientationTouch = orientationTouch;
	}
	
	/** Get orientation of touching ellipse */
	public float getOrientationTouch() {
		return orientationTouch;
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
