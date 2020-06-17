package de.mhus.karaf.commands.impl;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.services.IServiceManager;

@Command(scope = "service", name = "sb-create", description = "Create a service blueprint")
@Service
public class CmdServiceCreate extends AbstractCmd {

    @Argument(
            index = 0,
            name = "implementation",
            required = true,
            description = "Canonical name of implementation class",
            multiValued = false)
    String impl;

    @Argument(
            index = 0,
            name = "bundle",
            required = false,
            description = "Bundle name or id",
            multiValued = false)
    String bundleName;
    
    @Override
    public Object execute2() throws Exception {
        IServiceManager api = M.l(IServiceManager.class);
        boolean ret = api.create(impl, bundleName);
        if (ret)
            System.out.println("Created");
        else
            System.out.println("Skipped");
        return null;
    }

}
