package net.driftingsouls.ds2.server.modules.admin;

import net.driftingsouls.ds2.server.bases.Building;
import net.driftingsouls.ds2.server.framework.Common;
import net.driftingsouls.ds2.server.framework.Context;
import net.driftingsouls.ds2.server.framework.ContextMap;
import net.driftingsouls.ds2.server.framework.DynamicContent;
import net.driftingsouls.ds2.server.framework.DynamicContentManager;
import net.driftingsouls.ds2.server.framework.pipeline.Request;
import net.driftingsouls.ds2.server.modules.AdminController;
import org.apache.commons.fileupload.FileItem;
import org.hibernate.Session;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

/**
 * Bsisklasse fuer einfache Editor-Plugins (fuer einzelne Entities). Stellt
 * regelmaessig benoetigte Funktionen bereit.
 *
 * @author christopherjung
 */
public abstract class AbstractEditPlugin<T> implements AdminPlugin
{
	private Class<T> clazz;

	protected AbstractEditPlugin(Class<T> clazz)
	{
		this.clazz = clazz;
	}

	@Override
	public final void output(AdminController controller, String page, int action) throws IOException
	{
		Context context = ContextMap.getContext();
		org.hibernate.Session db = context.getDB();

		Writer echo = context.getResponse().getWriter();

		Request request = context.getRequest();
		int entityId = request.getParameterInt("entityId");

		if (this.isUpdateExecuted())
		{
			try
			{
				@SuppressWarnings("unchecked") T entity = (T) db.get(this.clazz, entityId);
				if (isUpdatePossible(entity))
				{
					update(new DefaultStatusWriter(echo), entity);
					echo.append("<p>Update abgeschlossen.</p>");
				}
			}
			catch (IOException | RuntimeException e)
			{
				echo.append("<p>Fehler bei Update: ").append(e.getMessage());
			}
		}
		else if (this.isResetExecuted())
		{
			try
			{
				@SuppressWarnings("unchecked") T entity = (T) db.get(this.clazz, entityId);
				reset(new DefaultStatusWriter(echo), entity);
				echo.append("<p>Update abgeschlossen.</p>");
			}
			catch (IOException | RuntimeException e)
			{
				echo.append("<p>Fehler bei Reset: ").append(e.getMessage());
			}
		}

		List<Building> entities = Common.cast(db.createCriteria(clazz).list());

		beginSelectionBox(echo, page, action);
		for (Object entity : entities)
		{
			addSelectionOption(echo, db.getIdentifier(entity), generateLabelFor(entity));
		}
		endSelectionBox(echo);

		if (entityId > 0)
		{
			@SuppressWarnings("unchecked") T entity = (T) db.get(clazz, entityId);
			if (entity == null)
			{
				return;
			}

			EditorForm form = beginEditorTable(echo, page, action, entityId);
			edit(form, entity);
			endEditorTable(echo);
		}
	}

	protected final Session getDB()
	{
		return ContextMap.getContext().getDB();
	}

	public interface StatusWriter
	{
		public StatusWriter append(String text);
	}

	public class DefaultStatusWriter implements StatusWriter
	{
		private Writer echo;

		public DefaultStatusWriter(Writer echo)
		{
			this.echo = echo;
		}

		@Override
		public StatusWriter append(String text)
		{
			try
			{
				this.echo.append(text);
			}
			catch (IOException e)
			{
				throw new IllegalStateException(e);
			}
			return this;
		}
	}

	protected abstract void update(StatusWriter writer, T entity) throws IOException;

	protected void reset(StatusWriter writer, T entity) throws IOException
	{
	}

	protected abstract void edit(EditorForm form, T entity);

	protected boolean isUpdatePossible(T entity)
	{
		return true;
	}

	protected static String generateLabelFor(Object entity)
	{
		Context context = ContextMap.getContext();
		org.hibernate.Session db = context.getDB();

		Class<?> type = entity.getClass();

		Method labelMethod;
		try
		{
			labelMethod = type.getMethod("getName");
		}
		catch (NoSuchMethodException e)
		{
			try
			{
				labelMethod = type.getMethod("toString");
			}
			catch (NoSuchMethodException e1)
			{
				throw new AssertionError("No toString");
			}
		}

		Serializable identifier = db.getIdentifier(entity);
		try
		{
			return labelMethod.invoke(entity).toString() + " (" + identifier + ")";
		}
		catch (IllegalAccessException | InvocationTargetException e)
		{
			throw new IllegalStateException(e);
		}
	}

	protected final String processDynamicContent(String name, String currentValue) throws IOException
	{
		Context context = ContextMap.getContext();

		for (FileItem file : context.getRequest().getUploadedFiles())
		{
			if (name.equals(file.getFieldName()) && file.getSize() > 0)
			{
				String id = DynamicContentManager.add(file);
				if (id != null)
				{
					processDynamicContentMetadata(name, id);
					return id;
				}
			}
		}
		if (currentValue != null)
		{
			processDynamicContentMetadata(name, currentValue);
		}
		return null;
	}

	private void processDynamicContentMetadata(String name, String id)
	{
		Context context = ContextMap.getContext();

		DynamicContent metadata = DynamicContentManager.lookupMetadata(id, true);
		String lizenzStr = context.getRequest().getParameterString(name + "_dc_lizenz");
		if (!lizenzStr.isEmpty())
		{
			try
			{
				metadata.setLizenz(DynamicContent.Lizenz.valueOf(lizenzStr));
			}
			catch (IllegalArgumentException e)
			{
				// EMPTY
			}
		}
		metadata.setLizenzdetails(context.getRequest().getParameterString(name + "_dc_lizenzdetails"));
		metadata.setAutor(context.getRequest().getParameterString(name + "_dc_autor"));
		metadata.setQuelle(context.getRequest().getParameterString(name + "_dc_quelle"));
		metadata.setAenderungsdatum(new Date());
		if (!context.getDB().contains(metadata))
		{
			context.getDB().persist(metadata);
		}
	}

	private void beginSelectionBox(Writer echo, String page, int action) throws IOException
	{
		echo.append("<div class='gfxbox adminSelection' style='width:390px'>");
		echo.append("<form action=\"./ds\" method=\"post\">");
		echo.append("<input type=\"hidden\" name=\"page\" value=\"").append(page).append("\" />\n");
		echo.append("<input type=\"hidden\" name=\"act\" value=\"").append(Integer.toString(action)).append("\" />\n");
		echo.append("<input type=\"hidden\" name=\"module\" value=\"admin\" />\n");
		echo.append("<select size=\"1\" name=\"entityId\">");
	}

	private void addSelectionOption(Writer echo, Object id, String label) throws IOException
	{
		Context context = ContextMap.getContext();
		String currentIdStr = context.getRequest().getParameter("entityId");
		String idStr = id.toString();

		echo.append("<option value=\"").append(idStr).append("\" ").append(idStr.equals(currentIdStr) ? "selected=\"selected\"" : "").append(">").append(label).append("</option>");
	}

	private void endSelectionBox(Writer echo) throws IOException
	{
		echo.append("</select>");
		echo.append("<input type=\"submit\" name=\"choose\" value=\"Ok\" />");
		echo.append("</form>");
		echo.append("</div>");
	}

	private EditorForm beginEditorTable(final Writer echo, String page, int action, Object entityId) throws IOException
	{
		echo.append("<div class='gfxbox adminEditor' style='width:700px'>");
		echo.append("<form action=\"./ds\" method=\"post\" enctype='multipart/form-data'>");
		echo.append("<input type=\"hidden\" name=\"page\" value=\"").append(page).append("\" />\n");
		echo.append("<input type=\"hidden\" name=\"act\" value=\"").append(Integer.toString(action)).append("\" />\n");
		echo.append("<input type=\"hidden\" name=\"module\" value=\"admin\" />\n");
		echo.append("<input type=\"hidden\" name=\"entityId\" value=\"").append(entityId != null ? entityId.toString() : "").append("\" />\n");

		echo.append("<table width=\"100%\">");

		return new EditorForm(action, page, echo);
	}

	private void endEditorTable(Writer echo) throws IOException
	{
		echo.append("<tr><td colspan='2'></td><td><input type=\"submit\" name=\"change\" value=\"Aktualisieren\"></td></tr>\n");
		echo.append("</table>");
		echo.append("</form>\n");
		echo.append("</div>");
	}

	protected final boolean isResetted(String name)
	{
		Context context = ContextMap.getContext();
		String reset = context.getRequest().getParameterString("reset");
		return name.equals(reset);
	}

	private boolean isResetExecuted()
	{
		Context context = ContextMap.getContext();
		String reset = context.getRequest().getParameterString("reset");
		return reset != null && !reset.trim().isEmpty();
	}

	private boolean isUpdateExecuted()
	{
		Context context = ContextMap.getContext();
		String change = context.getRequest().getParameterString("change");
		return "Aktualisieren".equals(change);
	}
}
