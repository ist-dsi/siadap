package module.siadap.domain;

import java.util.HashSet;
import java.util.Set;

import module.organization.domain.Unit;
import module.workflow.presentationTier.ProcessNodeSelectionMapper;
import module.workflow.widgets.ProcessListWidget;
import myorg.domain.ModuleInitializer;
import myorg.domain.MyOrg;
import myorg.domain.contents.Node;
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

    private void addHarmonizationUnits(Set<Unit> set, SiadapYearConfiguration siadapYearConfiguration, Unit unit) {
	set.add(unit);
	for (Unit iteratingUnit : unit.getChildUnits(siadapYearConfiguration.getUnitRelations())) {
	    if (!iteratingUnit.getChildPersons(siadapYearConfiguration.getHarmonizationResponsibleRelation()).isEmpty()) {
		addHarmonizationUnits(set, siadapYearConfiguration, iteratingUnit);
	    }
	}
    }

    public Set<Unit> getHarmonizationUnits(Integer year) {
	SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	Unit topUnit = siadapYearConfiguration.getSiadapStructureTopUnit();
	Set<Unit> units = new HashSet<Unit>();
	addHarmonizationUnits(units, siadapYearConfiguration, topUnit);
	return units;
    }
}
