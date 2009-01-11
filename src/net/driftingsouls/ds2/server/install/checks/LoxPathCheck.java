/*
 *	Drifting Souls 2
 *	Copyright (c) 2007 Christopher Jung
 *
 *	This library is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU Lesser General Public
 *	License as published by the Free Software Foundation; either
 *	version 2.1 of the License, or (at your option) any later version.
 *
 *	This library is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *	Lesser General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public
 *	License along with this library; if not, write to the Free Software
 *	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.driftingsouls.ds2.server.install.checks;

import java.io.File;

import net.driftingsouls.ds2.server.framework.Configuration;

/**
 * Ueberprueft, ob die config.xml existiert.
 * @author Christopher Jung
 *
 */
public class LoxPathCheck implements Checkable {
	@Override
	public void doCheck() throws CheckFailedException {
		final String path = Configuration.getSetting("LOXPATH");
		if( !new File(path).isDirectory() ) {
			throw new CheckFailedException("LOXPATH ist ungueltig");
		}
		
		if( !path.endsWith("/") ) {
			throw new CheckFailedException("LOXPATH endet nicht auf /");
		}
		
		if( !new File(path+"battles").isDirectory() ) {
			throw new CheckFailedException("LOXPATH enthaelt kein Verzeichnis 'battles'");
		}
		
		if( !new File(path+"raretick").isDirectory() ) {
			throw new CheckFailedException("LOXPATH enthaelt kein Verzeichnis 'raretick'");
		}
		
		if( !new File(path+"tick").isDirectory() ) {
			throw new CheckFailedException("LOXPATH enthaelt kein Verzeichnis 'tick'");
		}
	}

	@Override
	public String getDescription() {
		return "config.xml:LOXPATH pruefen";
	}
}
