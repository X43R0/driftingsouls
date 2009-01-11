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
package net.driftingsouls.ds2.server.ships;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.driftingsouls.ds2.server.framework.db.SQLResultRow;

/**
 * Findet den kuerzesten Weg zwischen zwei Systemen.
 * Jede Instanz der Klasse kann dabei nur einmal verwendet werden!
 * @author Christopher Jung
 *
 */
public class JumpNodeRouter {
	/**
	 * Eine Route zwischen zwei Systemen.
	 */
	public static class Result {
		/**
		 * Die Distanz.
		 */
		public int distance;
		/**
		 * Der Pfad. Elemente sind die zu benutzenden Jumpnodes.
		 */
		public List<SQLResultRow> path = new ArrayList<SQLResultRow>();
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder("[distance: "+distance+"\npath: ");
			for( int i=0; i < path.size(); i++ ) {
				SQLResultRow pathElem = path.get(i);
				builder.append(pathElem.getInt("system")+":"+pathElem.getInt("x")+"/"+pathElem.getInt("y")+" -> "+pathElem.getInt("systemout")+":"+pathElem.getInt("xout")+"/"+pathElem.getInt("yout")+"\n");
			}
			
			builder.append("]");
			return builder.toString();
		}
	}
	
	private Map<Integer,Integer> systemInterestLevel = new HashMap<Integer,Integer>();
	private Map<Integer,List<SQLResultRow>> jnlist = new HashMap<Integer,List<SQLResultRow>>();
	
	/**
	 * Konstruktor.
	 * @param jns Die benutzbaren Jumpnodes. Schluessel ist das Ausgangssystem der Jumpnode
	 */
	public JumpNodeRouter(Map<Integer,List<SQLResultRow>> jns) {
		for( Map.Entry<Integer, List<SQLResultRow>> entry: jns.entrySet()) {
			List<SQLResultRow> jnlist = entry.getValue();
			this.jnlist.put(entry.getKey(), new ArrayList<SQLResultRow>(jnlist));
		}
	}
	
	private int getMovementCost(int startX, int startY, int targetX, int targetY) {
		return Math.max(Math.abs(targetX-startX),Math.abs(targetY-startY));
	}
	
	/**
	 * Findet den kuerzesten Weg zwischen zwei Punkten. 
	 * @param currentsys Das Startsystem
	 * @param currentx Die Start-X-Koordinate
	 * @param currenty Die Start-Y-Koordinate
	 * @param targetsys Das Zielsystem
	 * @param targetx Die Ziel-X-Koordinate
	 * @param targety Die Ziel-Y-Koordinate
	 * @return Der Pfad oder <code>null</code>
	 */
	public Result locateShortestJNPath( int currentsys, int currentx, int currenty, int targetsys, int targetx, int targety) {
		if( currentsys == targetsys ) {
			Result res = new Result();
			res.distance = Math.max(Math.abs(targetx-currentx),Math.abs(targety-currenty));
			return res;	
		}
		
		if( !jnlist.containsKey(currentsys) ) {
			return null;	
		}
		
		Result shortestpath = null;
		List<SQLResultRow> sysJNList = jnlist.get(currentsys);
		for( int k=0; k < sysJNList.size(); k++ ) {
			SQLResultRow ajn = sysJNList.get(k);
			
			if( systemInterestLevel.containsKey(ajn.getInt("systemout")) &&
				systemInterestLevel.get(ajn.getInt("systemout")) < 0 ) {
				continue;
			}
			int pathcost = getMovementCost(currentx, currenty, ajn.getInt("x"), ajn.getInt("y"));
			
			// JN vorlaeufig aus der Liste entfernen um Endlosschleifen zu vermeiden
			sysJNList.remove(k);
			
			Result cost = locateShortestJNPath(ajn.getInt("systemout"),ajn.getInt("xout"),ajn.getInt("yout"), targetsys, targetx, targety );
			
			sysJNList.add(k, ajn);
			
			// Keine Route gefunden -> JN-Zielsystem ignorieren
			if( cost == null ) {
				if( !systemInterestLevel.containsKey(ajn.getInt("systemout")) ) {
					systemInterestLevel.put(ajn.getInt("systemout"), -1);
				}
				continue;
			}
			// Erste moegliche Route gefunden
			else if( shortestpath == null ) {
				shortestpath = cost;
				shortestpath.distance += pathcost;
				shortestpath.path.add(0, ajn);
			}
			// Neue Route mit der alten Route vergleichen
			else if( shortestpath.distance > cost.distance+pathcost ) {
				shortestpath = cost;
				shortestpath.distance += pathcost;
				shortestpath.path.add(0, ajn);
			}
			
			// JN-Zielsystem bietet gueltige Route -> merken!
			if( !systemInterestLevel.containsKey(ajn.getInt("systemout")) ) {
				systemInterestLevel.put(ajn.getInt("systemout"), 1);
			}
		}
		
		return shortestpath;
	}
}
