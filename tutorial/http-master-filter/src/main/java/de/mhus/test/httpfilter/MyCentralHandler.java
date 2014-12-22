package de.mhus.test.httpfilter;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ops4j.pax.web.service.jetty.CentralCallContext;
import org.ops4j.pax.web.service.jetty.CentralRequestHandler;
import org.ops4j.pax.web.service.jetty.ConfigurableHandler;

import aQute.bnd.annotation.component.Component;

@Component(immediate=true)
public class MyCentralHandler implements CentralRequestHandler,ConfigurableHandler {

	private boolean enabled = true;

	@Override
	public boolean doHandleBefore(CentralCallContext context) throws IOException, ServletException {
		
		System.out.println("CB," + context.getHost() + "," + context.getTarget());
		context.setAttribute("MyCentralHandlerTime", System.currentTimeMillis());
		context.setResponse(new StatusExposingServletResponse(context.getResponse()));
		return false;
	}

	@Override
	public boolean doHandleAfter(CentralCallContext context)
			throws IOException, ServletException {
		
		long cur = System.currentTimeMillis();
		Long start = (Long) context.getAttribute("MyCentralHandlerTime");
		long time = 0;
		if (start != null) {
			time = cur - start;
		}
		int rc = 0;
		if (context.getResponse() instanceof StatusExposingServletResponse) {
			rc = ((StatusExposingServletResponse)context.getResponse()).getStatus();
		}
		System.out.println("CA," + context.getBaseRequest().getHeader("host") + "," + context.getTarget() + "," + time + "," + rc);
		return false;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public double getSortHint() {
		return 0;
	}

	@Override
	public void configure(Properties rules) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled  = enabled;
	}

}
