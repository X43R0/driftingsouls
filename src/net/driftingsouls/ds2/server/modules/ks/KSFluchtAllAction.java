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
package net.driftingsouls.ds2.server.modules.ks;

import java.io.IOException;
import java.util.List;

import net.driftingsouls.ds2.server.battles.Battle;
import net.driftingsouls.ds2.server.battles.BattleShip;
import net.driftingsouls.ds2.server.framework.Context;
import net.driftingsouls.ds2.server.framework.ContextMap;
import net.driftingsouls.ds2.server.ships.ShipTypeData;
import net.driftingsouls.ds2.server.ships.ShipTypes;

/**
 * Laesst alle Schiffe einer Seite fliehen.
 * @author Christopher Jung
 *
 */
public class KSFluchtAllAction extends BasicKSAction {
	
	/**
	 * Prueft, ob das Schiff fliehen kann oder nicht.
	 * @param fluchtmode Der Zeitpunkt der Flucht (<code>current</code> oder <code>next</code>) - dieser Parameter ist nur noch aus historischen Gruenden vorhanden und hat keinen Effekt mehr
	 * @param ship Das Schiff
	 * @param shiptype Der Schiffstyp
	 * @return <code>true</code>, wenn das Schiff fliehen kann
	 */
	protected boolean validateShipExt( String fluchtmode, BattleShip ship, ShipTypeData shiptype ) {
		// Extension Point
		return true;
	}
	
	@Override
	public final int execute(Battle battle) throws IOException {
		int result = super.execute(battle);
		if( result != RESULT_OK ) {
			return result;
		}
		
		Context context = ContextMap.getContext();

		String fluchtmode = context.getRequest().getParameterString("fluchtmode");

		int shipcount = 0;
		Boolean gotone = null;
		
		int fluchtflag = Battle.BS_FLUCHTNEXT;
		
		List<BattleShip> ownShips = battle.getOwnShips();
		for( int i=0; i < ownShips.size(); i++ ) {
			BattleShip aship = ownShips.get(i);
			
			if( (aship.getAction() & Battle.BS_DESTROYED) != 0 ) {
				continue;
			}
			
			if( (aship.getAction() & Battle.BS_FLUCHT) != 0 ) {
				continue;
			}
			
			if( fluchtmode.equals("next") && (aship.getAction() & Battle.BS_FLUCHTNEXT) != 0 ) {
				continue;
			}
			
			if( (aship.getAction() & Battle.BS_JOIN) != 0 ) {
				continue;
			}
			 
			if( aship.getShip().getEngine() == 0 ) {
				continue;
			}
			
			if( aship.getDocked().length() > 0 ) {
				continue;
			}

			if( aship.getEngine() <= 0 ) {
				continue;
			} 
			
			ShipTypeData ashipType = aship.getTypeData();
			 
			if( (ashipType.getCrew() > 0) && (aship.getCrew() < (int)(ashipType.getCrew()/4d)) ) {
				continue;
			}
	
			if( (gotone == null) && ashipType.hasFlag(ShipTypes.SF_DROHNE) ) {
				gotone = Boolean.FALSE;
				for( int j=0; j < ownShips.size(); j++ ) {
					BattleShip as = ownShips.get(j);
					ShipTypeData ast = as.getTypeData();
					if( ast.hasFlag(ShipTypes.SF_DROHNEN_CONTROLLER) ) {
						gotone = Boolean.TRUE;
						break;	
					}
				}
			}
			if( (gotone == Boolean.FALSE) && ashipType.hasFlag(ShipTypes.SF_DROHNE) ) {
				continue;
			}
			
			if( !validateShipExt(fluchtmode, aship, ashipType) ) {
				continue;
			}

		
			battle.logme( aship.getName()+" flieht n&auml;chste Runde\n" );
			aship.setAction(aship.getAction() | Battle.BS_FLUCHTNEXT);
			
			int remove = 1;
			for( int j=0; j < ownShips.size(); j++ ) {
				BattleShip s = ownShips.get(j);
				if( (s.getDocked().length() > 0) && (s.getDocked().equals(""+aship.getId()) || s.getDocked().equals("l "+aship.getId()) ) ) {
					s.setAction(s.getAction() | fluchtflag);

					remove++;
				}
			}
			
			if( remove > 1 ) {
				battle.logme( (remove-1)+" an "+aship.getName()+" gedockte Schiffe fliehen n&auml;chste Runde\n" );
			}
			shipcount += remove;
		}
		
		if( shipcount > 0 ) {
			battle.resetInactivity();
		}
		
		return RESULT_OK;	
	}
}
