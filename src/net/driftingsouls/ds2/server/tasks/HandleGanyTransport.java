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
package net.driftingsouls.ds2.server.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.driftingsouls.ds2.server.Location;
import net.driftingsouls.ds2.server.comm.PM;
import net.driftingsouls.ds2.server.entities.User;
import net.driftingsouls.ds2.server.framework.Context;
import net.driftingsouls.ds2.server.framework.ContextMap;
import net.driftingsouls.ds2.server.framework.db.Database;
import net.driftingsouls.ds2.server.framework.db.SQLQuery;
import net.driftingsouls.ds2.server.framework.db.SQLResultRow;
import net.driftingsouls.ds2.server.ships.JumpNodeRouter;

import org.apache.commons.lang.StringUtils;

/**
 * TASK_GANY_TRANSPORT
 * 		Ein Gany-Transportauftrag.
 * 
 * 	- data1 -> die Order-ID des Auftrags
 *  - data2 -> Schiffs-ID [Wird von der Task selbst gesetzt!]
 *  - data3 -> Status [autom. gesetzt: Nichts = Warte auf Schiff od. flug zur Ganymede; 1 = Gany-Transport; 2 = Rueckweg]
 *   
 *  @author Christopher Jung
 */
class HandleGanyTransport implements TaskHandler {
	public void handleEvent(Task task, String event) {	
		Context context = ContextMap.getContext();
		Database db = context.getDatabase();
		Taskmanager tm = Taskmanager.getInstance();
		
		if( event.equals("tick_timeout") ) {		
			int orderid = Integer.parseInt(task.getData1());
			SQLResultRow order = db.first("SELECT * FROM factions_shop_orders WHERE id=",orderid);
			SQLResultRow entryowner = db.first("SELECT faction_id FROM factions_shop_entries WHERE id=",order.getInt("shopentry_id")," AND type='2'");
			if( entryowner.isEmpty() ) {
				order.clear();
			}
			
			if( !order.isEmpty() ) {
				String[] tmp = StringUtils.split(order.getString("adddata"), '@');
				int ganyid = Integer.parseInt(tmp[0]);
				tmp = StringUtils.split(tmp[1], "->");
				Location source = Location.fromString(tmp[0]);
				Location target = Location.fromString(tmp[1]);
					
				SQLResultRow shiptrans = db.first("SELECT * FROM ships WHERE owner=",entryowner.getInt("faction_id")," AND system=",source.getSystem()," AND script IS NULL AND scriptexedata IS NULL AND LOCATE('#!/tm gany_transport',destcom)");
				if( shiptrans.isEmpty() ) {
					shiptrans = db.first("SELECT * FROM ships WHERE owner=",entryowner.getInt("faction_id")," AND system=",target.getSystem()," AND script IS NULL AND scriptexedata IS NULL AND LOCATE('#!/tm gany_transport',destcom)");
				}			
				if( shiptrans.isEmpty() ) {
					shiptrans = db.first("SELECT * FROM ships WHERE owner=",entryowner.getInt("faction_id")," AND script IS NULL AND scriptexedata IS NULL AND LOCATE('#!/tm gany_transport',destcom)");
				}
			
				if( !shiptrans.isEmpty() ) {	
					Map<Integer,List<SQLResultRow>> jumpnodes = new HashMap<Integer,List<SQLResultRow>>();
					Map<Integer,SQLResultRow> jumpnodeindex = new HashMap<Integer,SQLResultRow>();
					SQLQuery jnRow = db.query("SELECT * FROM jumpnodes WHERE hidden=0");
					while( jnRow.next() ) {
						if( !jumpnodes.containsKey(jnRow.getInt("system")) ) {
							jumpnodes.put(jnRow.getInt("system"), new ArrayList<SQLResultRow>());
						}
						jumpnodes.get(jnRow.getInt("system")).add(jnRow.getRow());
						jumpnodeindex.put(jnRow.getInt("id"), jnRow.getRow());
					}
					jnRow.free();
					
					JumpNodeRouter.Result shortestpath = new JumpNodeRouter(jumpnodes)
						.locateShortestJNPath(source.getSystem(),source.getX(),source.getY(),
								target.getSystem(),target.getX(),target.getY());
					if( shortestpath == null ) {
						User sourceUser = (User)context.getDB().get(User.class, -1);
						
						String msg = "[color=orange]WARNUNG[/color]\nDer Taskmanager kann keinen Weg f&uuml;r die Gany-Transport-Order mit der ID "+orderid+" finden.";
						PM.sendToAdmins(sourceUser, "Taskmanager-Warnung", msg, 0);
						
						tm.incTimeout( task.getTaskID() );
						return;
					}
					
					JumpNodeRouter.Result pathtogany = new JumpNodeRouter(jumpnodes)
						.locateShortestJNPath(shiptrans.getInt("system"),shiptrans.getInt("x"),shiptrans.getInt("y"),
								source.getSystem(),source.getX(),source.getY());
					if( pathtogany == null ) {
						User sourceUser = (User)context.getDB().get(User.class, -1);
						
						// Eigenartig....es gibt kein Weg zur Gany. Pausieren wir besser mal
						String msg = "[color=orange]WARNUNG[/color]\nDer Taskmanager kann keinen Weg zur Ganymede f&uuml;r die Gany-Transport-Order mit der ID "+orderid+" finden.";
						
						PM.sendToAdmins(sourceUser, "Taskmanager-Warnung", msg, 0);
						
						tm.incTimeout( task.getTaskID() );
						return;
					}
					
					JumpNodeRouter.Result pathtohome = new JumpNodeRouter(jumpnodes)
						.locateShortestJNPath(target.getSystem(),target.getX(),target.getY(),
								shiptrans.getInt("system"),shiptrans.getInt("x"),shiptrans.getInt("y"));
					JumpNodeRouter.Result pathback = new JumpNodeRouter(jumpnodes)
						.locateShortestJNPath(source.getSystem(),source.getX(),source.getY(),
								shiptrans.getInt("system"),shiptrans.getInt("x"),shiptrans.getInt("y"));
					
					StringBuilder script = new StringBuilder(300);
					for( int i=0; i < pathtogany.path.size(); i++ ) {
						SQLResultRow jn = pathtogany.path.get(i);
						script.append("!SHIPMOVE "+jn.getInt("system")+":"+jn.getInt("x")+"/"+jn.getInt("y")+"\n");
						script.append("!NJUMP "+jn.getInt("id")+"\n");
					}
					script.append("!SHIPMOVE "+source.getSystem()+":"+source.getX()+"/"+source.getY()+"\n");

					script.append("!GETSHIPOWNER "+ganyid+"\n");
					script.append("!COMPARE #A "+order.getInt("user_id")+"\n");
					script.append("!JNE error\n");
					
					script.append("!DOCK "+ganyid+"\n");
					script.append("!EXECUTETASK "+task.getTaskID()+" start\n");
					
					for( int i=0; i < shortestpath.path.size(); i++ ) {
						SQLResultRow jn = shortestpath.path.get(i);
						script.append("!SHIPMOVE "+jn.getInt("system")+":"+jn.getInt("x")+"/"+jn.getInt("y")+"\n");
						script.append("!NJUMP "+jn.getInt("id")+"\n");
					}
					script.append("!SHIPMOVE "+target.getSystem()+":"+target.getX()+"/"+target.getY()+"\n");
					
					script.append("!UNDOCK "+ganyid+"\n");
					script.append("!EXECUTETASK "+task.getTaskID()+" completed\n");				
					script.append("!JUMP ende\n");
									
					script.append(":error\n");
					script.append("#title = \"Shop-Fehler: Order "+order.getInt("id")+"\"\n");
					script.append("#msg = \"Der Besitzer der Gany "+ganyid+" konnte nicht korrekt &uuml;berpr&uuml;ft werden. Erwartet wurde ID "+order.getInt("user_id")+"\"\n");
					script.append("!MSG "+entryowner.getInt("faction_id")+" #title #msg\n");
					script.append("!EXECUTETASK "+task.getTaskID()+" error\n");
					for( int i=0; i < pathback.path.size(); i++ ) {
						SQLResultRow jn = pathback.path.get(i);
						script.append("!SHIPMOVE "+jn.getInt("system")+":"+jn.getInt("x")+"/"+jn.getInt("y")+"\n");
						script.append("!NJUMP "+jn.getInt("id")+"\n");
					}
					
					script.append("!SHIPMOVE "+shiptrans.getInt("system")+":"+shiptrans.getInt("x")+"/"+shiptrans.getInt("y")+"\n");
					script.append("!WAIT\n");
					script.append("!WAIT\n");
					script.append("!WAIT\n");
					script.append("!WAIT\n");
					script.append("!EXECUTETASK "+task.getTaskID()+" end\n");
					script.append("!RESETSCRIPT\n");
					
					script.append(":ende\n");
					for( int i=0; i < pathtohome.path.size(); i++ ) {
						SQLResultRow jn = pathtohome.path.get(i);
						script.append("!SHIPMOVE "+jn.getInt("system")+":"+jn.getInt("x")+"/"+jn.getInt("y")+"\n");
						script.append("!NJUMP "+jn.getInt("id")+"\n");
					}
					script.append("!SHIPMOVE "+shiptrans.getInt("system")+":"+shiptrans.getInt("x")+"/"+shiptrans.getInt("y")+"\n");
					script.append("!WAIT\n");
					script.append("!WAIT\n");
					script.append("!WAIT\n");
					script.append("!WAIT\n");
					script.append("!EXECUTETASK "+task.getTaskID()+" end\n");
					script.append("!RESETSCRIPT\n");
					
					db.prepare("UPDATE ships SET script= ? WHERE id= ?")
						.update(script.toString(), shiptrans.getInt("id"));
					tm.modifyTask( task.getTaskID(), task.getData1(), Integer.toString(shiptrans.getInt("id")), task.getData3() );

					db.update("UPDATE factions_shop_orders SET status='2' WHERE id=",order.getInt("id"));
				}
				else {
					db.update("UPDATE factions_shop_orders SET status='1' WHERE id=",order.getInt("id"));
					tm.incTimeout( task.getTaskID() );
				}
			}
			else {
				User sourceUser = (User)context.getDB().get(User.class, -1);
				
				String msg = "[color=orange]WARNUNG[/color]\nDer Taskmanager kann die Gany-Transport-Order mit der ID "+orderid+" nicht finden.";
				PM.sendToAdmins(sourceUser, "Taskmanager-Warnung", msg, 0);
				tm.removeTask( task.getTaskID() );
			}
		}
		else if( event.equals("spa_start") ) {
			tm.modifyTask( task.getTaskID(), task.getData1(), task.getData2(), "1" );
		}
		else if( event.equals("spa_completed") ) {
			db.update("UPDATE factions_shop_orders SET status='4' WHERE id=",task.getData1());

			tm.modifyTask( task.getTaskID(), task.getData1(), task.getData2(), "2" );
		}
		else if( event.equals("spa_error") ) {
			db.update("UPDATE factions_shop_orders SET status='3' WHERE id=",task.getData1());

			tm.modifyTask( task.getTaskID(), task.getData1(), task.getData2(), "2" );
		}
		else if( event.equals("spa_end") ) {
			tm.removeTask( task.getTaskID() );
		}
	}

}
