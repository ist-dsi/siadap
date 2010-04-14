package module.siadap.domain;

import myorg.domain.ModuleInitializer;
import myorg.domain.MyOrg;
import pt.ist.fenixWebFramework.services.Service;

public class SiadapRootModule extends SiadapRootModule_Base implements ModuleInitializer {

    private static boolean isInitialized = false;

    private static ThreadLocal<SiadapRootModule> init = null;

    private SiadapRootModule() {
	super();
	setMyOrg(MyOrg.getInstance());
	setNumber(0);
    }

    public static SiadapRootModule getInstance() {
	if (init != null) {
	    return init.get();
	}

	if (!isInitialized) {
	    initialize();
	}
	final MyOrg myOrg = MyOrg.getInstance();
	return myOrg.getSiadapRootModule();
    }

    @Override
    public void init(MyOrg root) {
	// TODO Auto-generated method stub

    }

    @Service
    public synchronized static void initialize() {
	if (!isInitialized) {
	    try {
		final MyOrg myOrg = MyOrg.getInstance();
		final SiadapRootModule system = myOrg.getSiadapRootModule();
		if (system == null) {
		    new SiadapRootModule();
		}
		init = new ThreadLocal<SiadapRootModule>();
		init.set(myOrg.getSiadapRootModule());

		isInitialized = true;
	    } finally {
		init = null;
	    }
	}
    }

    @Override
    public Integer getNumber() {
	throw new UnsupportedOperationException("Use getNumberAndIncrement instead");
    }

    public Integer getNumberAndIncrement() {
	Integer processNumber = super.getNumber();
	setNumber(processNumber + 1);
	return processNumber;
    }
}
