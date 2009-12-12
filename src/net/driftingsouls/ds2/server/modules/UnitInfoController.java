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
package net.driftingsouls.ds2.server.modules;

import java.util.List;

import net.driftingsouls.ds2.server.cargo.ResourceEntry;
import net.driftingsouls.ds2.server.entities.Forschung;
import net.driftingsouls.ds2.server.entities.User;
import net.driftingsouls.ds2.server.framework.Common;
import net.driftingsouls.ds2.server.framework.Context;
import net.driftingsouls.ds2.server.framework.ContextMap;
import net.driftingsouls.ds2.server.framework.pipeline.generators.Action;
import net.driftingsouls.ds2.server.framework.pipeline.generators.ActionType;
import net.driftingsouls.ds2.server.framework.pipeline.generators.TemplateGenerator;
import net.driftingsouls.ds2.server.framework.templates.TemplateEngine;
import net.driftingsouls.ds2.server.units.UnitType;

import org.springframework.beans.factory.annotation.Configurable;

/**
 * Zeigt Informationen zu Einheiten an.
 *
 */
@Configurable
public class UnitInfoController extends TemplateGenerator {
	
	/**
	 * Konstruktor.
	 * @param context Der zu verwendende Kontext
	 */
	public UnitInfoController(Context context) {
		super(context);
		
		setTemplate("unitinfo.html");
		
		setPageTitle("Einheit");
	}
	
	@Override
	protected boolean validateAndPrepare(String action) {
		return true;
	}
	
	/**
	 * Zeigt die Einheitenliste an.
	 */
	@Action(ActionType.DEFAULT)
	public void listAction() {
		TemplateEngine t = getTemplateEngine();
		org.hibernate.Session db = getDB();
		List<UnitType> unitlist = Common.cast(db.createQuery("from UnitType").list());
		
		t.setVar( "unitinfo.list", 1);
		
		t.setBlock("_UNITINFO", "unitinfo.unitlist.listitem", "unitinfo.unitlist.list");
		
		for( UnitType unit : unitlist)
		{
			t.setVar(	"unit.id",		unit.getId(),
						"unit.name", 	unit.getName(),
						"unit.groesse",	unit.getSize(),
						"unit.picture",	unit.getPicture() );
			
			t.parse("unitinfo.unitlist.list", "unitinfo.unitlist.listitem", true);
		}
	}
	
	/**
	 * Zeigt Details zu einer Einheit an.
	 * @urlparam Integer unitid Die ID der anzuzeigenden Einheit
	 */
	@Action(ActionType.DEFAULT)
	public void defaultAction() {
		TemplateEngine t = getTemplateEngine();
		org.hibernate.Session db = getDB();

		parameterNumber("unit");
		int unitid = getInteger("unit");
		
		UnitType unittype = (UnitType)db.get(UnitType.class, unitid);
				
		if( unittype == null ) {
			t.setVar("unitinfo.message", "Es ist keine Einheit mit dieser Identifikationsnummer bekannt");
		
			return;
		}
		String buildcosts = "";
		buildcosts = buildcosts+"<img style=\"vertical-align:middle\" src=\"data/interface/time.gif\" alt=\"\" />"+unittype.getDauer();
		
		for(ResourceEntry res : unittype.getBuildCosts().getResourceList())
		{
			buildcosts = buildcosts+"<img style=\"vertical-align:middle\" src=\""+res.getImage()+"\" alt=\"\" />"+res.getCargo1();
		}
		
		User user = (User)ContextMap.getContext().getActiveUser();
		Forschung forschung = Forschung.getInstance(unittype.getRes());
		String forschungstring = "";
		
		if(forschung != null && forschung.isVisibile(user))
		{
			forschungstring = forschung.getName();
		}
		else if(forschung != null)
		{
			forschungstring = "Unbekannte Technologie";
			if(user.getAccessLevel() > 19)
			{
				forschungstring = forschungstring + " ["+forschung.getID()+"]";
			}
		}
				
		String name = Common._plaintitle(unittype.getName());
		
		t.setVar(	"unitinfo.details",	1,
					"unit.picture",		unittype.getPicture(),
					"unit.name",		name,
					"unit.size",		unittype.getSize(),
					"unit.nahrungcost",	unittype.getNahrungCost(),
					"unit.recost",		unittype.getReCost(),
					"unit.kapervalue",	unittype.getKaperValue(),
					"unit.description",	Common._text(unittype.getDescription()),
					"unit.baukosten",	buildcosts,
					"unit.forschung",	forschungstring );
	}
}