package net.driftingsouls.ds2.server.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import net.driftingsouls.ds2.server.framework.JSONSupport;
import net.sf.json.JSONObject;

/**
 * Repraesentation einer Menge von vergebenen Loyalitaetspunkten durch einen NPC
 * an einen Nutzer. Der Vergabe ist jeweils ein Datum sowie ein Grund zugeordnet.
 * Optional koennen weitere Anmerkungen erfolgen.
 * @author christopherjung
 *
 */
@Entity
@Table(name="loyalitaetspunkte")
public class Loyalitaetspunkte implements Comparable<Loyalitaetspunkte>, JSONSupport
{
	@Id
	@GeneratedValue
	private int id;

	@ManyToOne
	@JoinColumn
	private User user;
	private String grund;
	private String anmerkungen;
	private int anzahlPunkte;
	private Date zeitpunkt;
	@ManyToOne
	@JoinColumn
	private User verliehenDurch;

	/**
	 * Konstruktor.
	 */
	protected Loyalitaetspunkte()
	{
		// EMPTY
	}

	/**
	 * Konstruktor.
	 * @param user Der User dem die Punkte verliehen werden
	 * @param verliehenDurch Der NPC, der die Punkte vergeben hat
	 * @param grund Der Grund
	 * @param anzahlPunkte Die Anzahl der Punkte
	 */
	public Loyalitaetspunkte(User user, User verliehenDurch, String grund, int anzahlPunkte)
	{
		this.user = user;
		this.verliehenDurch = verliehenDurch;
		this.grund = grund;
		this.anzahlPunkte = anzahlPunkte;
		this.zeitpunkt = new Date();
	}

	/**
	 * Gibt die ID des DB-Eintrags zurueck.
	 * @return Die ID
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * Gibt die weiteren Anmerkungen zur Punktevergabe zurueck.
	 * @return Die Anmerkungen
	 */
	public String getAnmerkungen()
	{
		return this.anmerkungen;
	}

	/**
	 * Setzt die weiteren Anmerkungen zur Punktevergabe.
	 * @param anmerkungen Die Anmerkungen
	 */
	public void setAnmerkungen(String anmerkungen)
	{
		this.anmerkungen = anmerkungen;
	}

	/**
	 * Gibt den Nutzer zurueck, dem die Punkte verliehen wurden.
	 * @return Der Nutzer
	 */
	public User getUser()
	{
		return this.user;
	}

	/**
	 * Gibt den Grund fuer die Punktevergabe zurueck.
	 * @return Der Grund
	 */
	public String getGrund()
	{
		return this.grund;
	}

	/**
	 * Gibt die Anzahl der verliehenen Punkte zurueck.
	 * @return Die Anzahl
	 */
	public int getAnzahlPunkte()
	{
		return this.anzahlPunkte;
	}

	/**
	 * Gibt den Zeitpunkt der Punktevergabe zurueck.
	 * @return Der Zeitpunkt
	 */
	public Date getZeitpunkt()
	{
		return new Date(this.zeitpunkt.getTime());
	}

	/**
	 * Gibt den Nutzer (NPC) zurueck, der die Punkte verliehen hat.
	 * @return Der Nutzer
	 */
	public User getVerliehenDurch()
	{
		return verliehenDurch;
	}

	@Override
	public int compareTo(Loyalitaetspunkte arg0)
	{
		int diff = this.zeitpunkt.compareTo(arg0.zeitpunkt);
		if( diff != 0 )
		{
			return -diff;
		}
		return this.grund.compareTo(arg0.grund);
	}

	@Override
	public JSONObject toJSON()
	{
		JSONObject result = new JSONObject()
			.accumulate("id", this.id)
			.accumulate("grund", this.grund)
			.accumulate("anmerkungen", this.anmerkungen)
			.accumulate("anzahlPunkte", this.anzahlPunkte)
			.accumulate("zeitpunkt", this.zeitpunkt.getTime());

		return result;
	}
}
