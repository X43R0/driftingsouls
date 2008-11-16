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
package net.driftingsouls.ds2.server;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.driftingsouls.ds2.server.framework.BasicContext;
import net.driftingsouls.ds2.server.framework.CmdLineRequest;
import net.driftingsouls.ds2.server.framework.SimpleResponse;
import net.driftingsouls.ds2.server.framework.db.HibernateFacade;

import org.junit.After;
import org.junit.Before;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Basisklasse fuer Datenbankbasierte Tests von DS
 * @author Christopher Jung
 *
 */
public abstract class DriftingSoulsDBTestCase implements DBTestable {
	protected BasicContext context;
	protected DBTestCaseAdapter dbTester;

	/**
	 * Konstruktor
	 */
	public DriftingSoulsDBTestCase() {
		dbTester = new DBTestCaseAdapter(this);
	}
	
	/**
	 * SetUp-Methode
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		new FileSystemXmlApplicationContext("test/cfg/spring.xml");
		
		SimpleResponse response = new SimpleResponse();
		CmdLineRequest request = new CmdLineRequest(new String[0]);
		this.context = new BasicContext(request, response);
		
		Connection con = this.dbTester.getConnection().getConnection();
		Statement stmt = con.createStatement();
		try {
			stmt.executeUpdate("INSERT INTO config VALUES ('ticks', '1', 'Der aktuelle Tick', 0)");
		}
		finally {
			stmt.close();
		}
		
		this.dbTester.setUp();
	}

	/**
	 * TearDown-Methode
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
		this.context.free();
		
		try {
			Connection con = this.dbTester.getConnection().getConnection();
			Statement stmt = con.createStatement();
			try {
				ResultSet result = stmt.executeQuery("SHOW TABLES");
				try {
					final Statement stmt2 = con.createStatement();
					stmt2.addBatch("SET FOREIGN_KEY_CHECKS=0");
					while( result.next() ) {
						String table = result.getString(1);
						stmt2.addBatch("DELETE FROM "+table);
					}
					stmt2.addBatch("SET FOREIGN_KEY_CHECKS=1");
					stmt2.executeBatch();
					stmt2.close();
				}
				finally {
					result.close();
				}
			}
			finally {
				stmt.close();
			}
			
			HibernateFacade.clearSecondLevelCache();
		}
		catch( SQLException e ) {
			e.printStackTrace();
		}
		
		this.dbTester.tearDown();
	}
}
