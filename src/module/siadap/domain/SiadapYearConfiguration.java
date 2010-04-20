package module.siadap.domain;

import pt.ist.fenixWebFramework.services.Service;

public class SiadapYearConfiguration extends SiadapYearConfiguration_Base {

    private static final Double DEFAULT_OBJECTIVES_PONDERATION = 75.0;
    private static final Double DEFAULT_COMPETENCES_PONDERATION = 25.0;

    public SiadapYearConfiguration(Integer year, Double objectivesPonderation, Double competencesPonderation) {
	super();
	setYear(year);
	setObjectivesPonderation(objectivesPonderation);
	setCompetencesPonderation(competencesPonderation);
	setSiadapRootModule(SiadapRootModule.getInstance());
    }

    public static SiadapYearConfiguration getSiadapYearConfiguration(Integer year) {
	for (SiadapYearConfiguration configuration : SiadapRootModule.getInstance().getYearConfigurations()) {
	    if (configuration.getYear() == year) {
		return configuration;
	    }
	}
	return null;
    }

    @Service
    public static SiadapYearConfiguration createNewSiadapYearConfiguration(Integer year) {
	SiadapYearConfiguration configuration = getSiadapYearConfiguration(year);
	if (configuration != null) {
	    return configuration;
	}
	return new SiadapYearConfiguration(year, DEFAULT_OBJECTIVES_PONDERATION, DEFAULT_COMPETENCES_PONDERATION);
    }
}
