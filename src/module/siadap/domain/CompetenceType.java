package module.siadap.domain;

import java.util.Collections;
import java.util.List;

import pt.ist.fenixWebFramework.services.Service;

public class CompetenceType extends CompetenceType_Base {

    public CompetenceType(String name) {
	super();
	setName(name);
	setSiadapRootModule(SiadapRootModule.getInstance());
    }

    @Service
    public static CompetenceType createNewCompetenceType(String name) {
	return new CompetenceType(name);
    }

    public Integer getNextCompetenceNumber() {

	List<Competence> competences = getCompetences();
	if (competences.isEmpty()) {
	    return 1;
	}
	Competence max = Collections.max(competences, Competence.COMPARATOR_BY_NUMBER);
	return max.getNumber() + 1;
    }
}
