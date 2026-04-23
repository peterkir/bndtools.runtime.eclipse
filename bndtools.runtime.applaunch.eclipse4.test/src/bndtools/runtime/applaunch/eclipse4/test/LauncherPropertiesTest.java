package bndtools.runtime.applaunch.eclipse4.test;

import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * OSGi integration tests verifying that the bndtools.runtime.applaunch.eclipse4
 * plugin correctly processes all command-line arguments and VM arguments, and
 * makes them available as properties and services within the Eclipse OSGi
 * runtime.
 *
 * <p>
 * Each test method verifies one specific aspect of the launcher plugin's
 * behaviour. The required setup for each test (VM args, program args, run
 * properties) is documented in the test method's Javadoc and configured via the
 * corresponding <code>.bndrun</code> launch files.
 * </p>
 *
 * <p>
 * Required {@code .bndrun} configuration for all tests in this class:
 * <ul>
 * <li>{@code -runvm: -DtestVmArg1=eclipse4testvalue}</li>
 * <li>{@code -runprogramargs: -application example.equinox.headless.application
 *     -os linux -ws gtk -arch x86_64 -nl en_US -clean -consoleLog}</li>
 * </ul>
 * </p>
 */
public class LauncherPropertiesTest extends TestCase {

    private final BundleContext context = FrameworkUtil.getBundle(
            this.getClass()).getBundleContext();

    // -----------------------------------------------------------------------
    // VM argument tests
    // -----------------------------------------------------------------------

    /**
     * Verifies that JVM system properties set via {@code -runvm} in the
     * {@code .bndrun} file are accessible through {@link System#getProperty}.
     *
     * <p>
     * Required {@code .bndrun} setup:
     * {@code -runvm: -DtestVmArg1=eclipse4testvalue}
     * </p>
     */
    public void testVMArguments() throws Exception {
        assertEquals("eclipse4testvalue", System.getProperty("testVmArg1"));
    }

    // -----------------------------------------------------------------------
    // IApplicationContext service tests
    // -----------------------------------------------------------------------

    /**
     * Verifies that the {@link IApplicationContext} OSGi service is registered
     * and retrievable from the service registry after the Eclipse application
     * launcher has initialised.
     */
    public void testApplicationContextServiceAvailable() throws Exception {
        assertNotNull("BundleContext must not be null", context);
        ServiceReference<?> srv = context
                .getServiceReference(IApplicationContext.class.getName());
        assertNotNull("IApplicationContext service must be registered in the "
                + "OSGi service registry", srv);
    }

    /**
     * Verifies that the {@code eclipse.application} entry in the
     * {@link IApplicationContext} arguments map matches the application
     * identifier specified via {@code -application} in
     * {@code -runprogramargs}.
     *
     * <p>
     * Required {@code .bndrun} setup:
     * {@code -runprogramargs: -application example.equinox.headless.application}
     * </p>
     */
    @SuppressWarnings("rawtypes")
    public void testEclipseApplicationArgument() throws Exception {
        ServiceReference srv = context
                .getServiceReference(IApplicationContext.class.getName());
        assertNotNull(srv);
        @SuppressWarnings("unchecked")
		IApplicationContext iac = (IApplicationContext) context.getService(srv);
        assertNotNull("IApplicationContext service object must not be null", iac);
        Map arguments = iac.getArguments();
        assertNotNull("IApplicationContext arguments map must not be null",
                arguments);
        assertEquals("example.equinox.headless.application",
                arguments.get("eclipse.application"));
    }

    // -----------------------------------------------------------------------
    // Command-line argument → OSGi property tests
    // -----------------------------------------------------------------------

    /**
     * Verifies that the {@code -os} program argument is translated to the
     * {@code osgi.os} framework property.
     *
     * <p>
     * Required {@code .bndrun} setup:
     * {@code -runprogramargs: ... -os linux ...}
     * </p>
     */
    public void testOsArgument() throws Exception {
        String osProperty = context.getProperty("osgi.os");
        assertNotNull("osgi.os property must be set when -os argument is "
                + "passed as a program argument", osProperty);
        assertEquals("linux", osProperty);
    }

    /**
     * Verifies that the {@code -ws} program argument is translated to the
     * {@code osgi.ws} framework property.
     *
     * <p>
     * Required {@code .bndrun} setup:
     * {@code -runprogramargs: ... -ws gtk ...}
     * </p>
     */
    public void testWsArgument() throws Exception {
        String wsProperty = context.getProperty("osgi.ws");
        assertNotNull("osgi.ws property must be set when -ws argument is "
                + "passed as a program argument", wsProperty);
        assertEquals("gtk", wsProperty);
    }

    /**
     * Verifies that the {@code -arch} program argument is translated to the
     * {@code osgi.arch} framework property.
     *
     * <p>
     * Required {@code .bndrun} setup:
     * {@code -runprogramargs: ... -arch x86_64 ...}
     * </p>
     */
    public void testArchArgument() throws Exception {
        String archProperty = context.getProperty("osgi.arch");
        assertNotNull("osgi.arch property must be set when -arch argument is "
                + "passed as a program argument", archProperty);
        assertEquals("x86_64", archProperty);
    }

    /**
     * Verifies that the {@code -nl} program argument is translated to the
     * {@code osgi.nl} framework property.
     *
     * <p>
     * Required {@code .bndrun} setup:
     * {@code -runprogramargs: ... -nl en_US ...}
     * </p>
     */
    public void testNlArgument() throws Exception {
        String nlProperty = context.getProperty("osgi.nl");
        assertNotNull("osgi.nl property must be set when -nl argument is "
                + "passed as a program argument", nlProperty);
        assertEquals("en_US", nlProperty);
    }

    /**
     * Verifies that the {@code -clean} flag sets the {@code osgi.clean}
     * framework property to {@code "true"}.
     *
     * <p>
     * Required {@code .bndrun} setup:
     * {@code -runprogramargs: ... -clean ...}
     * </p>
     */
    public void testCleanArgument() throws Exception {
        assertEquals("osgi.clean must be \"true\" when -clean flag is present",
                "true", context.getProperty("osgi.clean"));
    }

    /**
     * Verifies that the {@code -consoleLog} flag sets the
     * {@code eclipse.consoleLog} framework property to {@code "true"}.
     *
     * <p>
     * Required {@code .bndrun} setup:
     * {@code -runprogramargs: ... -consoleLog ...}
     * </p>
     */
    public void testConsoleLogArgument() throws Exception {
        assertEquals("eclipse.consoleLog must be \"true\" when -consoleLog "
                + "flag is present",
                "true", context.getProperty("eclipse.consoleLog"));
    }

    // -----------------------------------------------------------------------
    // Run-properties tests
    // -----------------------------------------------------------------------

    /**
     * Verifies that OSGi framework properties set via {@code -runproperties}
     * in the {@code .bndrun} file are accessible through
     * {@link BundleContext#getProperty}.
     *
     * <p>
     * Required {@code .bndrun} setup:
     * {@code -runproperties: launch.keep=false}
     * </p>
     */
    public void testRunProperties() throws Exception {
        assertEquals("false", context.getProperty("launch.keep"));
    }
}
