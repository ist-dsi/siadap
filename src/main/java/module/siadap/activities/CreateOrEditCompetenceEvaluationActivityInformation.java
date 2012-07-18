/*
 * @(#)CreateOrEditCompetenceEvaluationActivityInformation.java
 *
 * Copyright 2010 Instituto Superior Tecnico
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
package module.siadap.activities;

import java.util.ArrayList;
import java.util.List;

import module.siadap.domain.Competence;
import module.siadap.domain.CompetenceType;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.presentationTier.renderers.providers.CompetencesForCompetenceType.ContainsCompetenceType;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author João Antunes
 * 
 */
public class CreateOrEditCompetenceEvaluationActivityInformation extends ActivityInformation<SiadapProcess> implements
	ContainsCompetenceType {

    private Siadap siadap;
    private CompetenceType competenceType;
    private List<Competence> competences;
    // Variable used to make sure that the JSP is displayed to the user upon
    // creation/edition of the competences
    private boolean inputDisplayed = false;

    private Boolean evaluatedOnlyByCompetences;

    public CreateOrEditCompetenceEvaluationActivityInformation(SiadapProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
	super(process, activity);
	setCompetences(new ArrayList<Competence>(getSiadap().getCompetences()));
	// setCompetences(process.getSiadap().getCompetences());
	this.setCompetenceType(process.getSiadap().getDefaultCompetenceType());
	if (process.getSiadap().getEvaluatedOnlyByCompetences() == null) {
	    setEvaluatedOnlyByCompetences(Boolean.FALSE);
	} else {
	    setEvaluatedOnlyByCompetences(process.getSiadap().getEvaluatedOnlyByCompetences());
	}
    }

    @Override
    public void setProcess(SiadapProcess process) {
	super.setProcess(process);
	setSiadap(process.getSiadap());
    }

    public Siadap getSiadap() {
	return siadap;
    }

    public void setSiadap(Siadap siadap) {
	this.siadap = siadap;
    }

    @Override
    public CompetenceType getCompetenceType() {
	return competenceType;
    }

    public void setCompetences(List<Competence> competences) {
	this.competences = competences;
    }

    public List<Competence> getCompetences() {
	return competences;
    }

    @Override
    public boolean hasAllneededInfo() {
	return isForwardedFromInput() && getInputDisplayed() && getSiadap() != null && getCompetences() != null
		&& getCompetenceType() != null && (evaluatedOnlyByCompetences != null);
    }

    public void setInputDisplayed(boolean inputDisplayed) {
	this.inputDisplayed = inputDisplayed;
    }

    public boolean getInputDisplayed() {
	return inputDisplayed;
    }

    public void setEvaluatedOnlyByCompetences(Boolean evaluatedOnlyByCompetences) {
	this.evaluatedOnlyByCompetences = evaluatedOnlyByCompetences;
    }

    /**
     * 
     * @param competence
     *            the competence to check if there's an equivalent set here
     * @return true if for the given competence we have in the
     *         {@link #getCompetences()} any competence with the same name and
     *         description (whitespace tolerant)
     */
    public boolean hasEquivalentCompetence(Competence competence) {
	String competenceProcessedDescription = StringUtils.deleteWhitespace(competence.getDescription());
	String competenceProcessedName = StringUtils.deleteWhitespace(competence.getName());
	for (Competence availableCompetence : getCompetences()) {
	    if (StringUtils.equalsIgnoreCase(StringUtils.deleteWhitespace(availableCompetence.getDescription()),
		    competenceProcessedDescription))
		return true;
	}
	return false;
    }

    @Override
    public Boolean getEvaluatedOnlyByCompetences() {
	return evaluatedOnlyByCompetences;
    }

    public void setCompetenceType(CompetenceType competenceType) {
	this.competenceType = competenceType;
    }

}
