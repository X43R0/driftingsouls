package net.driftingsouls.ds2.server.config;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import net.driftingsouls.ds2.server.cargo.Cargo;
import net.driftingsouls.ds2.server.ships.ShipType;

/**
 * <p>Eine Konfiguration eines konkreten Felsbrockentyps
 * fuer in einem System spawnende Felsbrocken.
 * Jeder Konfiguration ist ein Schiffstyp (der Typ des Felsbrockens),
 * der zu erzeugende Cargo und eine Wahrscheinlichkeit zugeordnet.</p>
 * <p>Die Wahrscheinlichkeit ist ein beliebiger positiver Integer-Wert.
 * Die Summe aller Wahrscheinlichkeiten in einem System sollte den Wert 100
 * jedoch nicht ueberschreiten. Beim spawnen wird dann eine Zufallszahl
 * zwischen 1 und 100 bestimmt. Die Konfiguration, in
 * dessen "Bereich" (Liste) die Zufallszahl liegt, wird genommen. Sollte
 * keine Konfiguration getroffen sein, so wird kein Felsbrocken erzeugt.</p>
 * @author christopherjung
 *
 */
@Entity
@Table(name="config_felsbrocken")
public class ConfigFelsbrocken implements Comparable<ConfigFelsbrocken>
{
	@Id
	@GeneratedValue
	private int id;
	@ManyToOne
	@JoinColumn(name="shiptype")
	private ShipType shiptype;
	@ManyToOne
	@JoinColumn(name="system")
	private ConfigFelsbrockenSystem system;
	private int chance;
	@Type(type="cargo")
	private Cargo cargo;

	protected ConfigFelsbrocken()
	{
		// EMPTY
	}

	/**
	 * Gibt den Schiffstyp des Felsbrockens zurueck.
	 * @return Der Schiffstyp
	 */
	public ShipType getShiptype()
	{
		return shiptype;
	}

	/**
	 * Setzt den Schiffstyp des Felsbrockens.
	 * @param shiptype Der Schiffstyp
	 */
	public void setShiptype(ShipType shiptype)
	{
		this.shiptype = shiptype;
	}

	/**
	 * Gibt die Konfigurationsdaten fuer Felsbrocken im gesamten
	 * System zurueck.
	 * @return Die Gesamtsystemkonfiguration
	 */
	public ConfigFelsbrockenSystem getSystem()
	{
		return system;
	}

	/**
	 * Setzt die Konfigurationsdaten fuer Felsbrocken im gesamten
	 * System.
	 * @param system Die Gesamtsystemkonfiguration
	 */
	public void setSystem(ConfigFelsbrockenSystem system)
	{
		this.system = system;
	}

	/**
	 * Gibt die Wahrscheinlichkeit zurueck, dass diese
	 * Felsbrocken-Konfiguration spawnt.
	 * @return Die Wahrscheinlichkeit
	 */
	public int getChance()
	{
		return chance;
	}

	/**
	 * Gibt die Wahrscheinlichkeit zurueck, dass diese
	 * Felsbrocken-Konfiguration spawnt.
	 * @return Die Wahrscheinlichkeit
	 */
	public void setChance(int chance)
	{
		this.chance = chance;
	}

	/**
	 * Gibt den Cargo des zu spawnenden Felsbrockens zurueck.
	 * @return Der Cargo
	 */
	public Cargo getCargo()
	{
		return new Cargo(this.cargo);
	}

	/**
	 * Setzt den Cargo des zu spawnenden Felsbrockens.
	 * @param cargo Der Cargo
	 */
	public void setCargo(Cargo cargo)
	{
		this.cargo = new Cargo(cargo);
	}

	/**
	 * Gibt die ID dieser Felsbrocken-Konfiguration zurueck.
	 * @return Die ID
	 */
	public int getId()
	{
		return id;
	}

	@Override
	public int compareTo(ConfigFelsbrocken other)
	{
		return id - other.id;
	}
}