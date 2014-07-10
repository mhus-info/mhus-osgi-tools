package de.mhus.osgi.vaadin_sample.impl;

import java.util.Date;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

import de.mhus.osgi.vaadin_sample.MyService;

@Theme("vaadinsample")
public class SampleUI extends UI {

	private static final long serialVersionUID = 1L;
	private VerticalLayout panelContnent;

	@Override
	protected void init(VaadinRequest request) {
		
		VerticalLayout content = new VerticalLayout();
		setContent(content);
		content.setSizeFull();
        content.addStyleName("view-content");
        content.setMargin(true);
        content.setSpacing(true);
        
        getPage().setTitle("Vaadin Sample");
        {
        	Label label = new Label("Vaadin Sample");
        	label.addStyleName("heading");
        	content.addComponent(label);
        }

        Button bSample = new Button("Click");
        bSample.addStyleName("icon-ok");
        content.addComponent(bSample);
        content.setExpandRatio(bSample, 0);
        bSample.addClickListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				doExecute();
			}
		});
        
        Panel panel = new Panel();
        panel.addStyleName("layout-panel");
        panel.setSizeFull();
        content.addComponent(panel);
        content.setExpandRatio(panel, 1);

        panelContnent = new VerticalLayout();
        // panelContnent.setWidth("100%");
        panelContnent.setSizeUndefined();
        panel.setContent(panelContnent);
        
		
	}

	protected void doExecute() {
		// load an service out of OSGi
		BundleContext context = ((SampleServlet)VaadinServlet.getCurrent()).getBundleContext();
		
		ServiceReference ref = context.getServiceReference(MyService.class.getName());
		MyService service = (MyService) context.getService(ref);
		
		panelContnent.removeAllComponents();

		{
			// Use the service
			Label label = new Label( service.echo( new Date().toString() ) );
			label.addStyleName("h1");
			panelContnent.addComponent(label);
		}
		
		// print all bundle names
		for (Bundle bundle : context.getBundles()) {
			Label label = new Label( bundle.getSymbolicName() + " : " + bundle.getVersion() );
			if (bundle.getState() != Bundle.ACTIVE)
				label.addStyleName("light");
			panelContnent.addComponent(label);
		}
		
		
	}

}