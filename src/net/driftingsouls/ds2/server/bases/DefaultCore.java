/*
 *	Drifting Souls 2
 *	Copyright (c) 2006 Christopher Jung
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
package net.driftingsouls.ds2.server.bases;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.Immutable;

/**
 * <h1>Die Standardcore in DS.</h1>
 * Alle Cores, welche keine eigene Core-Klasse besitzen, werden von
 * dieser Klasse bearbeitet.
 * 
 * @author Christopher Jung
 *
 */
@Entity
@Immutable
@DiscriminatorValue("1")
public class DefaultCore extends Core {
	/**
	 * Erstellt eine neue Core-Instanz.
	 */
	public DefaultCore() {
		// EMPTY
	}

}
