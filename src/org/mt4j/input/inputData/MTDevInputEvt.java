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

	public MTDevInputEvt(AbstractInputSource source, float positionX, float positionY, int id, InputCursor m) {
		super(source, positionX, positionY, id, m);
	}
}
