/*
 * @(#)AlterCompetencesNamesAndCreateCopies.java
 *
 * Copyright 2011 Instituto Superior Tecnico
 * Founding Authors: Paulo Abrantes
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the SIADAP Module.
 *
 *   The SIADAP Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The SIADAP Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the SIADAP Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package module.siadap.domain.util.scripts;

import module.siadap.domain.Competence;
import module.siadap.domain.CompetenceType;
import module.siadap.domain.SiadapRootModule;
import pt.ist.bennu.core.domain.scheduler.WriteCustomTask;

/**
 * 
 * @author João Antunes
 * 
 */
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
