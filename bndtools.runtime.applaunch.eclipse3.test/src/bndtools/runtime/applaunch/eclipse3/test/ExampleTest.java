package bndtools.runtime.applaunch.eclipse3.test;

import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class ExampleTest extends TestCase {

	private final BundleContext context = FrameworkUtil.getBundle(
			this.getClass()).getBundleContext();

	public void testVMarguments() throws Exception {

		assertEquals("vmArg1", System.getProperty("vmArg1"));
	}

	public void testArgumentsService() throws Exception {
		assertNotNull(context);
		ServiceReference srv = context
				.getServiceReference(IApplicationContext.class.getName());
		assertNotNull(srv);
		IApplicationContext iac = (IApplicationContext) context.getService(srv);
		Map arguments = iac.getArguments();
		assertEquals("example.equinox.headless.application",
				arguments.get("eclipse.application"));
	}
}
