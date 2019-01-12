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
package net.driftingsouls.ds2.server.entities.fraktionsgui;

import net.driftingsouls.ds2.server.comm.PM;
import net.driftingsouls.ds2.server.entities.User;
import net.driftingsouls.ds2.server.framework.Common;
import net.driftingsouls.ds2.server.framework.Context;
import net.driftingsouls.ds2.server.framework.ContextMap;
import net.driftingsouls.ds2.server.ships.ShipType;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.List;

/**
 * Eine Versteigerung fuer ein Schiff eines Schiffstyps.
 * @author Christopher Jung
 *
 */
@Entity
@DiscriminatorValue("1")
public class VersteigerungSchiff extends Versteigerung {
	private String type = "1";
	
	/**
	 * Konstruktor.
	 *
	 */
	public VersteigerungSchiff() {
		// EMPTY
	}

	/**
	 * Erstellt einen neuen Versteigerungseintrag fuer einen Schiffstyp.
	 * @param owner Der Besitzer und zugleich default-Bieter
	 * @param type Der Typ des Schiffes
	 * @param price Der Startpreis
	 */
	public VersteigerungSchiff(User owner, ShipType type, long price) {
		super(owner, price);
		
		this.type = Integer.toString(type.getTypeId());
		Context context = ContextMap.getContext();
		org.hibernate.Session db = context.getDB();
		List<User> users = Common.cast(db.createQuery("from User").list());
		User niemand = (User)db.get(User.class, -1);
		for(User auser : users)
		{
			PM.send(niemand, auser.getId(), "Neue Versteigerung eingestellt.", "Versteigert wird eine "+type+". Aktueller Preis: "+price);
			/*if(auser.getApiKey()!="") {
				new Notifier (auser.getApiKey()).sendMessage("Neue Versteigerung eingestellt", "Versteigert wird eine "+type+". Aktueller Preis: "+price);		
			}
			*/
		}
	}
	
	/**
	 * Gibt den Schiffstyp zurueck.
	 * @return Der Schiffstyp
	 */
	public ShipType getShipType() {
		org.hibernate.Session db = ContextMap.getContext().getDB();
		return (ShipType)db.get(ShipType.class, Integer.parseInt(type));
	}
	
	/**
	 * Setzt den Schiffstyp.
	 * @param type Der Schiffstyp
	 */
	public void setShipType(ShipType type) {
		this.type = Integer.toString(type.getTypeId());
	}

	@Override
	public long getObjectCount() {
		return 1;
	}

	@Override
	public String getObjectName() {
		return getShipType().getNickname();
	}

	@Override
	public String getObjectPicture() {
		return getShipType().getPicture();
	}

	@Override
	public String getObjectUrl() {
		return Common.buildUrl("default", "module", "schiffinfo", "ship", Integer.parseInt(type) );
	}

	@Override
	public boolean isObjectFixedImageSize() {
		return false;
	}
}
