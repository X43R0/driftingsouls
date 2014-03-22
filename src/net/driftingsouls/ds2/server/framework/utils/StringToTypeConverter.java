package net.driftingsouls.ds2.server.framework.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Hilfsklasse zum Konvertieren von Strings in eine Reihe von bekannten Datentypen.
 */
public final class StringToTypeConverter
{
	private StringToTypeConverter()
	{
		// EMPTY
	}

	/**
	 * Konvertiert einen String in den angegebenen Zieldatentyp, sofern eine Konvertierung moeglich ist.
	 * @param type Der Zieldatentyp
	 * @param value Der Stringwert
	 * @param <T> Der Zieldatentyp
	 * @return Der konvertierte Wert
	 * @throws java.lang.IllegalArgumentException Falls der konkrete Wert nicht in den Zieldatentyp konvertiert werden kann
	 * @throws java.lang.UnsupportedOperationException Falls fuer den Datentyp keine Konvertierung unterstuetzt wird
	 */
	@SuppressWarnings("unchecked")
	public static <T> T convert(Class<T> type, String value) throws UnsupportedOperationException, IllegalArgumentException
	{
		if( value == null )
		{
			// Null kann immer konvertiert werden
			return null;
		}
		if( type == String.class )
		{
			return (T)value;
		}

		try
		{
			Method valueOfMethod = type.getMethod("valueOf", String.class);
			if( (valueOfMethod.getModifiers() & Modifier.STATIC) != 0 )
			{
				return (T)valueOfMethod.invoke(null, value);
			}
		}
		catch (NoSuchMethodException | IllegalAccessException e)
		{
			throw new UnsupportedOperationException("Der Datentyp "+type+" besitzt zwar eine valueOf-Methode, diese kann aber nicht aufgerufen werden", e);
		}
		catch (InvocationTargetException e)
		{
			throw new IllegalArgumentException("Der Wert '"+value+"' kann nicht in den Datentyp "+type+" konvertiert werden", e);
		}

		throw new IllegalArgumentException("Der Zieltyp "+type.getName()+" wird nicht unterstuetzt");
	}
}