package org.mt4j.input.inputData;

import org.mt4j.input.inputSources.AbstractInputSource;

public class MTDevInputEvt  extends MTFingerInputEvt {

	public MTDevInputEvt(AbstractInputSource source, float positionX, float positionY, int id, InputCursor m) {
		super(source, positionX, positionY, id, m);
	}
}
