package net.driftingsouls.ds2.server.modules.admin.editoren;

import net.driftingsouls.ds2.server.framework.pipeline.Request;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Klasse zum Erstellen eines Eingabeformulars.
 */
public class EditorForm8<E>
{
	public static class Job<E,T>
	{
		public final String name;
		public final Function<E,? extends Collection<T>> supplier;
		public final Consumer<T> job;

		public Job(String name, Function<E, ? extends Collection<T>> supplier, Consumer<T> job)
		{
			this.name = name;
			this.supplier = supplier;
			this.job = job;
		}

		public static <E> Job<E,Boolean> forRunnable(String name, Runnable job)
		{
			return new Job<>(name, (entity) -> Arrays.asList(Boolean.TRUE), (b) -> job.run());
		}
	}

	private final EditorMode modus;
	private Writer echo;
	private int action;
	private String page;
	private List<CustomFieldGenerator<E>> fields = new ArrayList<>();
	private int counter;
	private boolean allowAdd;
	private List<Job<E,?>> updateTasks = new ArrayList<>();

	public EditorForm8(EditorMode modus, int action, String page, Writer echo)
	{
		this.modus = modus;
		this.echo = echo;
		this.action = action;
		this.page = page;
		this.counter = 0;
		this.allowAdd = false;
	}

	/**
	 * Aktiviert das Hinzufuegen von Entities.
	 */
	public void allowAdd()
	{
		this.allowAdd = true;
	}

	/**
	 * Gibt zurueck, ob das Hinzufuegen von Entities erlaubt ist.
	 * @return <code>true</code> falls dem so ist
	 */
	public boolean isAddAllowed()
	{
		return this.allowAdd;
	}

	public void addUpdateTask(String name, Runnable job)
	{
		updateTasks.add(Job.forRunnable(name, job));
	}

	public <T> void updateTask(String name, Function<E, ? extends Collection<T>> supplier, Consumer<T> job)
	{
		updateTasks.add(new Job<>(name, supplier, job));
	}

	public List<Job<E,?>> getUpdateTasks()
	{
		return this.updateTasks;
	}

	/**
	 * Fuegt einen Generator fuer ein Eingabefeld zum Form hinzu.
	 * @param generator Der Generator
	 * @param <T> Der Typ des Generators
	 * @return Der Generator
	 */
	public <T extends CustomFieldGenerator<E>> T custom(T generator)
	{
		fields.add(generator);
		return generator;
	}

	/**
	 * Generiert ein Eingabefeld (Editor) fuer ein via {@link net.driftingsouls.ds2.server.framework.DynamicContent}
	 * gemanagetes Bild.
	 * @param label Der Label fuer das Eingabefeld
	 * @param getter Der getter für das Feld
	 * @return Der erzeugte Generator
	 */
	public DynamicContentFieldGenerator<E> dynamicContentField(String label, Function<E,String> getter, BiConsumer<E,String> setter)
	{
		return custom(new DynamicContentFieldGenerator<>(action, page, label, generateName(getter.getClass().getSimpleName()), getter, setter));
	}

	/**
	 * Erzeugt ein Eingabefeld (Editor) in Form eines nicht editierbaren Werts.
	 * @param label Der Label zum Wert
	 * @param getter Der getter des Werts
	 */
	public <T> LabelGenerator<E,T> label(String label, Function<E,T> getter)
	{
		return custom(new LabelGenerator<>(label, getter));
	}

	/**
	 * Erzeugt ein Eingabefeld (Editor) in Form einer Textarea.
	 * @param label Der Label
	 * @param getter Der Getter fuer den momentanen Wert
	 * @param setter Der Setter fuer den momentanen Wert
	 */
	public TextAreaGenerator<E> textArea(String label, Function<E,String> getter, BiConsumer<E,String> setter)
	{
		return custom(new TextAreaGenerator<>(label, generateName(getter.getClass().getSimpleName()), getter, setter));
	}

	/**
	 * Erzeugt ein Eingabefeld (Editor) fuer einen bestimmten Datentyp. Das konkret erzeugte Eingabefeld
	 * kann von Datentyp zu Datentyp unterschiedlich sein.
	 * @param label Das Anzeigelabel
	 * @param viewType Der Datentyp des Views
	 * @param dataType Der Datentyp des Models
	 * @param getter Der getter fuer den momentanen Wert
     * @param setter Der setter fuer den momentanen Wert
	 */
	public <T> FieldGenerator<E,T> field(String label, Class<?> viewType, Class<T> dataType, Function<E,T> getter, BiConsumer<E,T> setter)
	{
		return custom(new FieldGenerator<>(label, generateName(getter.getClass().getSimpleName()), viewType, dataType, getter, setter));
	}

	/**
	 * Erzeugt ein Eingabefeld (Editor) fuer einen bestimmten Datentyp. Das konkret erzeugte Eingabefeld
	 * kann von Datentyp zu Datentyp unterschiedlich sein.
	 * @param label Das Anzeigelabel
	 * @param type Der Datentyp des Views und des Models
	 * @param getter Der getter fuer den momentanen Wert
	 * @param setter Der setter fuer den momentanen Wert
	 */
	public <T> FieldGenerator<E,T> field(String label, Class<T> type, Function<E,T> getter, BiConsumer<E,T> setter)
	{
		return field(label, type, type, getter, setter);
	}

	private String generateName(String suffix)
	{
		return "field"+(counter++)+"_"+suffix.replace('/', '_').replace('$', '_');
	}

	public void generateForm(E entity)
	{
		try
		{
			for (CustomFieldGenerator<E> field : fields)
			{
				field.generate(echo, entity);
			}

			if( modus == EditorMode.UPDATE && !updateTasks.isEmpty() )
			{
				StringBuilder str = new StringBuilder("<ul>");
				for (EditorForm8.Job<E, ?> tJob : updateTasks)
				{
					Collection<?> jobParams = tJob.supplier.apply(entity);
					str.append("<li>").append(jobParams.size()).append("x ").append(tJob.name).append("</li>");
				}
				str.append("</ul>");
				new LabelGenerator<>("Bei Aktualisierung", (e) -> str.toString()).generate(echo, entity);
			}

			String label = modus == EditorMode.UPDATE ? "Aktualisieren" : "Hinzufügen";
			echo.append("<tr><td colspan='2'></td><td><input type=\"submit\" name=\"change\" value=\"").append(label).append("\"></td></tr>\n");
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}
    
    public void applyRequestValues(Request request, E entity) throws IOException
	{
        for (CustomFieldGenerator<E> field : fields)
        {
            field.applyRequestValues(request, entity);
        }
    }

	public EditorForm8<E> ifAdding()
	{
		if( modus == EditorMode.CREATE )
		{
			return this;
		}
		return new EditorForm8<>(EditorMode.CREATE, action, page, new StringWriter());
	}

	public EditorForm8<E> ifUpdating()
	{
		if( modus == EditorMode.UPDATE )
		{
			return this;
		}
		return new EditorForm8<>(EditorMode.UPDATE, action, page, new StringWriter());
	}
}
