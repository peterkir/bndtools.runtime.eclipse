package bndtools.runtime.applaunch.eclipse3.test;

import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

public class ExampleTest extends TestCase {

	private final BundleContext context = FrameworkUtil.getBundle(
			this.getClass()).getBundleContext();

	public void testVMarguments() throws Exception {

		assertEquals("vmArg1", System.getProperty("vmArg1"));
	}

	public void testArgumentsService() throws Exception {
		assertNotNull(context);
		@SuppressWarnings("rawtypes")
		ServiceTracker tracker = new ServiceTracker(context,
				IApplicationContext.class.getName(), null);
		tracker.open();
		IApplicationContext iac = (IApplicationContext) tracker.waitForService(5000);
		tracker.close();
		assertNotNull("IApplicationContext service was not registered within 5 s", iac);
		@SuppressWarnings("rawtypes")
		Map arguments = iac.getArguments();
		assertEquals("example.equinox.headless.application",
				arguments.get("eclipse.application"));
	}
}
