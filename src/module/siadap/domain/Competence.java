package module.siadap.domain;

import java.util.Comparator;

import pt.ist.fenixWebFramework.services.Service;

public class Competence extends Competence_Base {

    public static Comparator<Competence> COMPARATOR_BY_NUMBER = new Comparator<Competence>() {

	@Override
	public int compare(Competence o1, Competence o2) {
	    return o1.getNumber().compareTo(o2.getNumber());
	}

    };

    public Competence(CompetenceType type, String name, String description) {
	super();
	setSiadapRootModule(SiadapRootModule.getInstance());
	setName(name);
	setDescription(description);
	setNumber(type.getNextCompetenceNumber());
	setCompetenceType(type);

    }

    @Service
    public static Competence createNewCompetence(CompetenceType competenceType, String name, String description) {
	return new Competence(competenceType, name, description);
    }

}
