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
package net.driftingsouls.ds2.server.modules.stats;

import java.io.IOException;

import net.driftingsouls.ds2.server.modules.StatsController;

/**
 * Interface fuer eine Statistik auf der Statistikseite.
 * @author Christopher Jung
 *
 */
public interface Statistic {
	/**
	 * Die Bevoelkerungs-/Crewdaten (Tabelle tmpbev).
	 */
	public static final int DATA_CREW = 1;
	/**
	 * Die Forschungsdaten (Tabelle tmpres).
	 */
	public static final int DATA_RESEARCH = 2;
	/**
	 * Die Schiffsdaten (Tabelle tmpships).
	 */
	public static final int DATA_SHIPS = 4;
	
	/**
	 * Zeigt die Statistik an.
	 * 
	 * @param contr Der Statistik-Controller
	 * @param size Die Anzahl der anzuzeigenden Eintraege
	 * @throws IOException 
	 */
	public void show(StatsController contr, int size) throws IOException;
	
	/**
	 * Sollen die durch den Controller zu generierenden Daten 
	 * Allianzdaten oder Userdaten sein?
	 * @return <code>true</code>, falls es Allianzdaten sein sollen
	 */
	public boolean generateAllyData();
	
	/**
	 * Gibt zurueck, welche benoetigten Daten generiert werden sollen.
	 * @return Die benoetigten Daten
	 * @see #DATA_CREW
	 * @see #DATA_RESEARCH
	 * @see #DATA_SHIPS
	 */
	public int getRequiredData();
}
