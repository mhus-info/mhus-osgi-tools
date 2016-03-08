package de.mhus.osgi.webconsole;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.webconsole.SimpleWebConsolePlugin;
import org.osgi.service.component.ComponentContext;

import de.mhus.lib.core.MXml;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;

@Component(immediate=true,provide=DummyService.class,name="SystemEnvironment")
public class SystemEnvironment extends SimpleWebConsolePlugin implements DummyService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final static String PLUGIN = "mhusysenv";
	final static String TITLE = "System Environment";
	
	public SystemEnvironment() {
		super(PLUGIN,TITLE,"Main",new String[0]);
	}

	@Activate
	public void doActivate(ComponentContext ctx) {
		register(ctx.getBundleContext());
	}
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		unregister();
	}

	
	@Override
	protected void renderContent(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		
		PrintWriter w = res.getWriter();
		w.println("<br>");
		
		w.println("<table class='nicetable ui-widget'>");
		w.println("<tr><td colspan='2' class='ui-widget-header'>System Parameters</td></tr>");
		
		for (Object key : System.getProperties().keySet()) {
			w.println("<tr><td>" + MXml.encode(String.valueOf(key)) + "</td><td>" + MXml.encode(System.getProperty(String.valueOf(key))) + "</td></tr>");
		}
		
		w.println("</table>");

		w.println("<br>");

		w.println("<table class='nicetable ui-widget'>");
		w.println("<tr><td colspan='2' class='ui-widget-header'>Environment</td></tr>");
		
		for (String key : System.getenv().keySet()) {
			w.println("<tr><td>" + MXml.encode(key) + "</td><td>" + MXml.encode(System.getenv().get(key)) + "</td></tr>");
		}
		
		w.println("</table>");
		
	}


}