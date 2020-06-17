/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.lib.mutable;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MActivator;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MHousekeeper;
import de.mhus.lib.core.activator.DefaultActivator;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.core.logging.LogFactory;
import de.mhus.lib.core.logging.MLogFactory;
import de.mhus.lib.core.logging.MLogUtil;
import de.mhus.lib.core.mapi.ApiInitialize;
import de.mhus.lib.core.mapi.IApi;
import de.mhus.lib.core.mapi.IApiInternal;
import de.mhus.lib.core.mapi.MCfgManager;
import de.mhus.lib.core.mapi.SingleMLogInstanceFactory;
import de.mhus.lib.core.shiro.AccessUtil;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.logging.JavaLoggerFactory;
import de.mhus.osgi.api.cache.LocalCache;
import de.mhus.osgi.api.cache.LocalCacheService;
import de.mhus.osgi.api.services.MOsgi;

/**
 * TODO: Map config to service TODO: Add MActivator with mapper to OSGi Services
 *
 * @author mikehummel
 */
public class KarafMApiImpl implements IApi, ApiInitialize, IApiInternal {

    private LogFactory logFactory;
    private MCfgManager configProvider;
    private boolean fullTrace = false;
    private HashSet<String> logTrace = new HashSet<>();
    private DefaultActivator base = new DefaultActivator();

    private KarafHousekeeper housekeeper;

    private File baseDir = new File(System.getProperty("karaf.base"));

    private MLogFactory mlogFactory;
    //	{
    //		baseDir.mkdirs();
    //	}
    private LocalCache<String, Container> apiCache;
    
    @Override
    public MActivator createActivator() {
        //		return new DefaultActivator(new OsgiBundleClassLoader());
        return new DefaultActivator();
    }

    @Override
    public LogFactory getLogFactory() {
        return logFactory;
    }

    @Override
    public synchronized MCfgManager getCfgManager() {
        if (configProvider == null) {
            configProvider = new KarafCfgManager(this);
            configProvider.startInitiators();
        }
        return configProvider;
    }

    @Override
    public void doInitialize(ClassLoader coreLoader) {
        logFactory = new JavaLoggerFactory();
        mlogFactory = new SingleMLogInstanceFactory();
        base.addObject(MLogFactory.class, null, mlogFactory);

        getCfgManager(); // init
        //		TimerFactoryImpl.indoCheckTimers();

        try {
            housekeeper = new KarafHousekeeper();
            base.addObject(MHousekeeper.class, null, housekeeper);
        } catch (Throwable t) {
            System.out.println("Can't initialize housekeeper base: " + t);
        }

        getCfgManager().doRestart();

        // logFactory.setLevelMapper(new ThreadBasedMapper() );
    }

    @Override
    public boolean isTrace(String name) {
        return fullTrace || logTrace.contains(name);
    }

    public void setFullTrace(boolean trace) {
        fullTrace = trace;
    }

    public void setTrace(String name) {
        logTrace.add(name);
    }

    public void clearTrace() {
        logTrace.clear();
    }

    public String[] getTraceNames() {
        return logTrace.toArray(new String[logTrace.size()]);
    }

    public boolean isFullTrace() {
        return fullTrace;
    }

    @Override
    public void setLogFactory(LogFactory logFactory) {
        this.logFactory = logFactory;
    }

    @Override
    public Set<String> getLogTrace() {
        return logTrace;
    }

    @Override
    public void setBaseDir(File file) {
        baseDir = file;
        baseDir.mkdirs();
    }

    @Override
    public File getFile(MApi.SCOPE scope, String dir) {
        dir = MFile.normalizePath(dir);
        switch (scope) {
            case DATA:
                return new File(baseDir, "data/" + dir);
            case DEPLOY:
                return new File(baseDir, "deploy/" + dir);
            case ETC:
                return new File(baseDir, "etc/" + dir);
            case LOG:
                return new File(baseDir, "data/log/" + dir);
            case TMP:
                return new File(baseDir, "data/tmp" + dir);
            default:
                break;
        }
        return new File(baseDir, "data" + File.separator + "mhus" + File.separator + dir);
    }

    @Override
    public synchronized Log lookupLog(Object owner) {
        if (mlogFactory == null) mlogFactory = M.l(MLogFactory.class);
        return mlogFactory.lookup(owner);
    }

    @Override
    public void updateLog() {
        if (mlogFactory == null) return;
        mlogFactory.update();
    }

    @Override
    public void setMLogFactory(MLogFactory mlogFactory) {
        this.mlogFactory = mlogFactory;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T, D extends T> T lookup(Class<T> ifc, Class<D> def) {

        if (ifc == null) return null;

        T result = null;
        
        if (def == null && ifc.isInterface()) { // only interfaces can be OSGi services

            if (apiCache == null) {
                try {
                    LocalCacheService cacheService = MOsgi.getService(LocalCacheService.class);
                    apiCache = cacheService.createCache(
                            FrameworkUtil.getBundle(KarafMApiImpl.class).getBundleContext(),
                            "baseApi", 
                            String.class, Container.class, 
                            100
                            );
                } catch (NotFoundException e) {
                    MApi.dirtyLogTrace(e);
                }
            }

            Container cached = null;
            if (apiCache != null) {
                cached = apiCache.get(ifc.getCanonicalName());
                if (cached != null) {
                    Bundle bundle = MOsgi.getBundleOrNull(cached.bundleId);
                    if (bundle == null || bundle.getState() != Bundle.ACTIVE
                            || cached.modified != bundle.getLastModified()) {
                        apiCache.remove(cached.ifc.getCanonicalName());
                        cached = null;
                    }
                }
            }
            
            if (cached == null) {
                Bundle bundle = FrameworkUtil.getBundle(KarafMApiImpl.class);
                if (bundle != null) {
                    BundleContext context = bundle.getBundleContext();
                    if (context != null) {
                        String filter = null;
                        IConfig cfg = MApi.getCfg(ifc);
                        if (cfg != null) {
                            filter = cfg.getString("mhusApiBaseFilter", null);
                        }
                        ServiceReference<T> ref = null;
                        try {
                            Collection<ServiceReference<T>> refs = context.getServiceReferences(ifc, filter);
                            Iterator<ServiceReference<T>> refsIterator = refs.iterator();
                            
                            if (refsIterator.hasNext())
                                ref = refs.iterator().next();
                            if (refsIterator.hasNext())
                                MApi.dirtyLogDebug("more then one service found for singleton",ifc,filter);
                        } catch (InvalidSyntaxException e) {
                            MApi.dirtyLogError(ifc,filter,e);
                        }
                        if (ref != null) {
                            if (ref.getBundle().getState() != Bundle.ACTIVE) {
                                MLogUtil.log()
                                        .d(
                                                "KarafBase",
                                                "found in bundle but not jet active",
                                                ifc,
                                                bundle.getSymbolicName());
                                return null;
                            }
                            T obj = null;
                            try {
                                obj = ref.getBundle().getBundleContext().getService(ref);
                                //                              obj = context.getService(ref);
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                            if (obj != null) {
                                MApi.dirtyLogDebug("KarafBase", "loaded from OSGi", ifc);
                                cached = new Container();
                                cached.bundleId = ref.getBundle().getBundleId();
                                cached.bundleName = ref.getBundle().getSymbolicName();
                                cached.modified = ref.getBundle().getLastModified();
                                cached.api = obj;
                                cached.ifc = ifc;
                                cached.filter = filter;
                                if (apiCache != null)
                                    apiCache.put(ifc.getCanonicalName(), cached);
                            }
                        }
                    }
                }
            }
            if (cached != null)
                result = (T) cached.api;
        }
        
        if (result == null)
            result = base.lookup(ifc, def);
        
        if (result != null) {
            AccessUtil.checkPermission(result);
        }
            
        return result;
    }

    public static class Container implements Serializable {

        public String filter;
        private static final long serialVersionUID = 1L;
        public long modified;
        public Class<?> ifc;
        public Object api;
        public String bundleName;
        public long bundleId;

    }

    @Override
    public DefaultActivator getLookupActivator() {
        return base;
    }

}
