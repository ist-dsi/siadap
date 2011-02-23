package module.siadap.domain.util.scripts;

import module.siadap.domain.Competence;
import module.siadap.domain.CompetenceType;
import module.siadap.domain.SiadapRootModule;
import myorg.domain.scheduler.WriteCustomTask;

public class AlterCompetencesNamesAndCreateCopies extends WriteCustomTask {

    int nrOfEvalAccEnded = 0;

    /*
     * 1) Dirigente Intermédio;
     * 
     * 2) Assistente Operacional (lista de competências igual à que está no
     * pessoal operário e auxiliar);
     * 
     * 3) Encarregado Operacional (lista de competências igual à que está no
     * pessoal operário e auxiliar);
     * 
     * 4) Assistente Técnico (lista de competências igual à que está no pessoal
     * administrativo e técnico-profissional);
     * 
     * 5) Coordenador Técnico (lista de competências igual à que está no pessoal
     * administrativo e técnico-profissional);
     * 
     * 6) Técnico Superior (lista de competências igual à que está no pessoal
     * técnico superior e técnico).
     */
    @Override
    protected void doService() {
	//get the competences
	for (CompetenceType competenceType : SiadapRootModule.getInstance().getCompetenceTypes()) {
	    if (competenceType.getName().equalsIgnoreCase("Pessoal Operário e Auxiliar")) {
		competenceType.setName("Assistente Operacional");

		//copy the competences to the new type
		CompetenceType encOpCompType = CompetenceType.createNewCompetenceType("Encarregado Operacional");

		//it's created, let's copy the competences
		for (Competence competence : competenceType.getCompetences()) {
		    Competence.createNewCompetence(encOpCompType, competence.getName(), competence.getDescription());
		}
		out.println("Caught the pessoal Operário e Auxiliar");

	    } else if (competenceType.getName().equalsIgnoreCase("Dirigentes Intermédios")) {
		//change it to dirigente intermédio
		competenceType.setName("Dirigente Intermédio");
		out.println("Caught the pessoal Dirigente Intermédio");
	    } else if (competenceType.getName().equalsIgnoreCase("Técnico Superior e Técnico")) {
		//change the name to Técnico Superior
		competenceType.setName("Técnico Superior");
		out.println("Caught the Técnico Superior e Técnico");
	    } else if (competenceType.getName().equalsIgnoreCase("Técnico Profissional e Administrativo")) {
		//change its name to Assistente Técnico
		competenceType.setName("Assistente Técnico");

		//create the new one called Coordenador Técnico
		CompetenceType coordTecCompType = CompetenceType.createNewCompetenceType("Coordenador Técnico");

		out.println("Caught the Técnico Profissional e Administrativo");

		for (Competence competence : competenceType.getCompetences()) {
		    Competence.createNewCompetence(coordTecCompType, competence.getName(), competence.getDescription());
		}

	    }

	}
	throw new Error("Make sure everything is correct");
    }


}
