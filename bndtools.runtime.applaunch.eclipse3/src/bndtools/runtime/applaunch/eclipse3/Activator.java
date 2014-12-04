/*******************************************************************************
 * Copyright (c) 2010 Neil Bartlett.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Bartlett - initial API and implementation
 ******************************************************************************/
package bndtools.runtime.applaunch.eclipse3;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.adaptor.LocationManager;
import org.eclipse.core.runtime.internal.adaptor.EclipseEnvironmentInfo;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.eclipse.osgi.service.runnable.ApplicationLauncher;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

    public void start(final BundleContext context) throws Exception {
        new LauncherTracker(context).open(true);
    }

    public void stop(final BundleContext context) throws Exception {
    }
}

@SuppressWarnings("rawtypes")
class LauncherTracker extends ServiceTracker {

    private static final String CLEAN                   = "-clean";                                                //$NON-NLS-1$
    private static final String CONSOLE                 = "-console";                                              //$NON-NLS-1$
    private static final String CONSOLE_LOG             = "-consoleLog";                                           //$NON-NLS-1$
    private static final String DEBUG                   = "-debug";                                                //$NON-NLS-1$
    private static final String INITIALIZE              = "-initialize";                                           //$NON-NLS-1$
    private static final String DEV                     = "-dev";                                                  //$NON-NLS-1$
    private static final String WS                      = "-ws";                                                   //$NON-NLS-1$
    private static final String OS                      = "-os";                                                   //$NON-NLS-1$
    private static final String ARCH                    = "-arch";                                                 //$NON-NLS-1$
    private static final String NL                      = "-nl";                                                   //$NON-NLS-1$
    private static final String NL_EXTENSIONS           = "-nlExtensions";                                         //$NON-NLS-1$
    private static final String CONFIGURATION           = "-configuration";                                        //$NON-NLS-1$
    private static final String USER                    = "-user";                                                 //$NON-NLS-1$
    private static final String NOEXIT                  = "-noExit";                                               //$NON-NLS-1$
    private static final String LAUNCHER                = "-launcher";                                             //$NON-NLS-1$
    // this is more of an Eclipse argument but this OSGi implementation stores its
    // metadata alongside Eclipse's.
    private static final String DATA                    = "-data";                                                 //$NON-NLS-1$

    // System properties
    public static final String  PROP_BUNDLES            = "osgi.bundles";                                          //$NON-NLS-1$
    public static final String  PROP_BUNDLES_STARTLEVEL = "osgi.bundles.defaultStartLevel";                        //$NON-NLS-1$ //The start level used to install the bundles
    public static final String  PROP_EXTENSIONS         = "osgi.framework.extensions";                             //$NON-NLS-1$
    public static final String  PROP_INITIAL_STARTLEVEL = "osgi.startLevel";                                       //$NON-NLS-1$ //The start level when the fwl start
    public static final String  PROP_DEBUG              = "osgi.debug";                                            //$NON-NLS-1$
    public static final String  PROP_DEV                = "osgi.dev";                                              //$NON-NLS-1$
    public static final String  PROP_CLEAN              = "osgi.clean";                                            //$NON-NLS-1$
    public static final String  PROP_CONSOLE            = "osgi.console";                                          //$NON-NLS-1$
    public static final String  PROP_CONSOLE_CLASS      = "osgi.consoleClass";                                     //$NON-NLS-1$
    public static final String  PROP_CHECK_CONFIG       = "osgi.checkConfiguration";                               //$NON-NLS-1$
    public static final String  PROP_OS                 = "osgi.os";                                               //$NON-NLS-1$
    public static final String  PROP_WS                 = "osgi.ws";                                               //$NON-NLS-1$
    public static final String  PROP_NL                 = "osgi.nl";                                               //$NON-NLS-1$
    private static final String PROP_NL_EXTENSIONS      = "osgi.nl.extensions";                                    //$NON-NLS-1$
    public static final String  PROP_ARCH               = "osgi.arch";                                             //$NON-NLS-1$
    public static final String  PROP_ADAPTOR            = "osgi.adaptor";                                          //$NON-NLS-1$
    public static final String  PROP_SYSPATH            = "osgi.syspath";                                          //$NON-NLS-1$
    public static final String  PROP_LOGFILE            = "osgi.logfile";                                          //$NON-NLS-1$
    public static final String  PROP_FRAMEWORK          = "osgi.framework";                                        //$NON-NLS-1$
    public static final String  PROP_INSTALL_AREA       = "osgi.install.area";                                     //$NON-NLS-1$
    public static final String  PROP_FRAMEWORK_SHAPE    = "osgi.framework.shape";                                  //$NON-NLS-1$ //the shape of the fwk (jar, or folder)
    public static final String  PROP_NOSHUTDOWN         = "osgi.noShutdown";                                       //$NON-NLS-1$
    public static final String  PROP_EXITCODE           = "eclipse.exitcode";                                      //$NON-NLS-1$
    public static final String  PROP_EXITDATA           = "eclipse.exitdata";                                      //$NON-NLS-1$
    public static final String  PROP_CONSOLE_LOG        = "eclipse.consoleLog";                                    //$NON-NLS-1$
    public static final String  PROP_IGNOREAPP          = "eclipse.ignoreApp";                                     //$NON-NLS-1$
    public static final String  PROP_REFRESH_BUNDLES    = "eclipse.refreshBundles";                                //$NON-NLS-1$
    static final String         PROP_LAUNCHER           = "eclipse.launcher";                                      //$NON-NLS-1$

    public static boolean       debug                   = false;

    private final Logger        log                     = Logger.getLogger(Activator.class.getPackage().getName());

    @SuppressWarnings("unchecked")
    public LauncherTracker(final BundleContext context) {
        super(context, createFilter(), null);
    }

    private static Filter createFilter() {
        try {
            return FrameworkUtil.createFilter("(&(objectClass=aQute.launcher.Launcher)(launcher.ready=true))"); //$NON-NLS-1$
        }
        catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object addingService(final ServiceReference reference) {

        prepareEnvService(context);

        // Find and start the Equinox Application bundle
        boolean found = false;
        Bundle[] bundles = context.getBundles();
        for (Bundle bundle: bundles) {
            if ("org.eclipse.equinox.app".equals(getBsn(bundle))) {
                found = true;
                try {
                    bundle.start();
                }
                catch (BundleException e) {
                    log.log(Level.SEVERE,
                            "Unable to start bundle org.eclipse.equinox.app. Eclipse application cannot start.",
                            e);
                }
                break;
            }
        }
        if (!found) {
            log.warning("Unable to find bundle org.eclipse.equinox.app. Eclipse application will not start.");
        }

        // Register the ApplicationLauncher
        log.fine("Registering ApplicationLauncher service.");
        return context.registerService(ApplicationLauncher.class.getName(), new EclipseApplicationLauncher(context), null);
    }

    @Override
    public void removedService(final ServiceReference reference, final Object service) {
        ((ServiceRegistration) service).unregister();
    }

    private String getBsn(final Bundle bundle) {
        String bsn = bundle.getHeaders().get(Constants.BUNDLE_SYMBOLICNAME);
        int semiColonIndex = bsn.indexOf(';');
        if (semiColonIndex > -1) {
            bsn = bsn.substring(0, semiColonIndex);
        }
        return bsn;
    }

    private void prepareEnvService(final BundleContext context) {
        String[] parameters = null;
        try {
            ServiceReference<?>[] serviceReferences = context.getServiceReferences("aQute.launcher.Launcher",
                                                                                   "(launcher.arguments=*)");
            if ( (serviceReferences != null) && (serviceReferences.length > 0)) {
                Object property = serviceReferences[0].getProperty("launcher.arguments");

                if (property instanceof String[]) {
                    parameters = (String[]) property;
                    processCommandLine(parameters);
                    log.fine("configured program arguments " + parameters);
                }
            }
            else {
                log.log(Level.SEVERE,
                        "service aQute.launcher.Launcher with props launcher.arguments could not be retireved");
            }
        }
        catch (Exception e) {
            log.log(Level.SEVERE, "command line parameters could not be configured.");
        }

    }

    private String[] processCommandLine(final String[] args) throws Exception {
        EclipseEnvironmentInfo.setAllArgs(args);
        if (args.length == 0) {
            EclipseEnvironmentInfo.setFrameworkArgs(args);
            EclipseEnvironmentInfo.setAllArgs(args);
            return args;
        }
        int[] configArgs = new int[args.length];
        configArgs[0] = -1; // need to initialize the first element to something that could not be an index.
        int configArgIndex = 0;
        for (int i = 0; i < args.length; i++) {
            boolean found = false;
            // check for args without parameters (i.e., a flag arg)

            // check if debug should be enabled for the entire platform
            // If this is the last arg or there is a following arg (i.e., arg+1 has a leading -),
            // simply enable debug. Otherwise, assume that that the following arg is
            // actually the filename of an options file. This will be processed below.
            if (args[i].equalsIgnoreCase(DEBUG) && ( ( (i + 1) == args.length) || ( ( (i + 1) < args.length) && (args[i + 1].startsWith("-"))))) { //$NON-NLS-1$
                FrameworkProperties.setProperty(PROP_DEBUG, ""); //$NON-NLS-1$
                debug = true;
                found = true;
            }

            // check if development mode should be enabled for the entire platform
            // If this is the last arg or there is a following arg (i.e., arg+1 has a leading -),
            // simply enable development mode. Otherwise, assume that that the following arg is
            // actually some additional development time class path entries. This will be processed below.
            if (args[i].equalsIgnoreCase(DEV) && ( ( (i + 1) == args.length) || ( ( (i + 1) < args.length) && (args[i + 1].startsWith("-"))))) { //$NON-NLS-1$
                FrameworkProperties.setProperty(PROP_DEV, ""); //$NON-NLS-1$
                found = true;
            }

            // look for the initialization arg
            if (args[i].equalsIgnoreCase(INITIALIZE)) {
                found = true;
            }

            // look for the clean flag.
            if (args[i].equalsIgnoreCase(CLEAN)) {
                FrameworkProperties.setProperty(PROP_CLEAN, "true"); //$NON-NLS-1$
                found = true;
            }

            // look for the consoleLog flag
            if (args[i].equalsIgnoreCase(CONSOLE_LOG)) {
                FrameworkProperties.setProperty(PROP_CONSOLE_LOG, "true"); //$NON-NLS-1$
                found = true;
            }

            // look for the console with no port.
            if (args[i].equalsIgnoreCase(CONSOLE) && ( ( (i + 1) == args.length) || ( ( (i + 1) < args.length) && (args[i + 1].startsWith("-"))))) { //$NON-NLS-1$
                FrameworkProperties.setProperty(PROP_CONSOLE, ""); //$NON-NLS-1$
                found = true;
            }

            if (args[i].equalsIgnoreCase(NOEXIT)) {
                FrameworkProperties.setProperty(PROP_NOSHUTDOWN, "true"); //$NON-NLS-1$
                found = true;
            }

            if (found) {
                configArgs[configArgIndex++] = i;
                continue;
            }
            // check for args with parameters. If we are at the last argument or if the next one
            // has a '-' as the first character, then we can't have an arg with a parm so continue.
            if ( (i == (args.length - 1)) || args[i + 1].startsWith("-")) { //$NON-NLS-1$
                continue;
            }
            String arg = args[++i];

            // look for the console and port.
            if (args[i - 1].equalsIgnoreCase(CONSOLE)) {
                FrameworkProperties.setProperty(PROP_CONSOLE, arg);
                found = true;
            }

            // look for the configuration location .
            if (args[i - 1].equalsIgnoreCase(CONFIGURATION)) {
                FrameworkProperties.setProperty(LocationManager.PROP_CONFIG_AREA, arg);
                found = true;
            }

            // look for the data location for this instance.
            if (args[i - 1].equalsIgnoreCase(DATA)) {
                FrameworkProperties.setProperty(LocationManager.PROP_INSTANCE_AREA, arg);
                found = true;
            }

            // look for the user location for this instance.
            if (args[i - 1].equalsIgnoreCase(USER)) {
                FrameworkProperties.setProperty(LocationManager.PROP_USER_AREA, arg);
                found = true;
            }

            // look for the launcher location
            if (args[i - 1].equalsIgnoreCase(LAUNCHER)) {
                FrameworkProperties.setProperty(PROP_LAUNCHER, arg);
                found = true;
            }
            // look for the development mode and class path entries.
            if (args[i - 1].equalsIgnoreCase(DEV)) {
                FrameworkProperties.setProperty(PROP_DEV, arg);
                found = true;
            }

            // look for the debug mode and option file location.
            if (args[i - 1].equalsIgnoreCase(DEBUG)) {
                FrameworkProperties.setProperty(PROP_DEBUG, arg);
                debug = true;
                found = true;
            }

            // look for the window system.
            if (args[i - 1].equalsIgnoreCase(WS)) {
                FrameworkProperties.setProperty(PROP_WS, arg);
                found = true;
            }

            // look for the operating system
            if (args[i - 1].equalsIgnoreCase(OS)) {
                FrameworkProperties.setProperty(PROP_OS, arg);
                found = true;
            }

            // look for the system architecture
            if (args[i - 1].equalsIgnoreCase(ARCH)) {
                FrameworkProperties.setProperty(PROP_ARCH, arg);
                found = true;
            }

            // look for the nationality/language
            if (args[i - 1].equalsIgnoreCase(NL)) {
                FrameworkProperties.setProperty(PROP_NL, arg);
                found = true;
            }

            // look for the locale extensions
            if (args[i - 1].equalsIgnoreCase(NL_EXTENSIONS)) {
                FrameworkProperties.setProperty(PROP_NL_EXTENSIONS, arg);
                found = true;
            }

            // done checking for args. Remember where an arg was found
            if (found) {
                configArgs[configArgIndex++] = i - 1;
                configArgs[configArgIndex++] = i;
            }
        }

        // remove all the arguments consumed by this argument parsing
        if (configArgIndex == 0) {
            EclipseEnvironmentInfo.setFrameworkArgs(new String[0]);
            EclipseEnvironmentInfo.setAppArgs(args);
            return args;
        }
        String[] appArgs = new String[args.length - configArgIndex];
        String[] frameworkArgs = new String[configArgIndex];
        configArgIndex = 0;
        int j = 0;
        int k = 0;
        for (int i = 0; i < args.length; i++) {
            if (i == configArgs[configArgIndex]) {
                frameworkArgs[k++] = args[i];
                configArgIndex++;
            }
            else {
                appArgs[j++] = args[i];
            }
        }
        EclipseEnvironmentInfo.setFrameworkArgs(frameworkArgs);
        EclipseEnvironmentInfo.setAppArgs(appArgs);
        return appArgs;
    }

}
