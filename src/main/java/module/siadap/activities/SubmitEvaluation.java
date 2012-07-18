/*
 * @(#)SubmitEvaluation.java
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
package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluationItem;
import module.siadap.domain.SiadapEvaluationUniverse;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.exceptions.SiadapException;
import module.workflow.activities.ActivityException;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.util.BundleUtil;

import org.joda.time.LocalDate;

/**
 * 
 *         This activity allows you to have intermediate states when doing the
 *         evaluation so that the evaluator can save intermediate states of his
 *         evaluation and iterate at will before making it available
 * 
 * @author João Antunes
 * 
 */
public class SubmitEvaluation extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	if (!process.isActive())
	    return false;
	Siadap siadap = process.getSiadap();
	if (siadap.getEvaluator() == null)
	    return false;
	return siadap.getEvaluator().getPerson().getUser().equals(user) && !siadap.isDefaultEvaluationDone()
		&& new Evaluation().isActive(process, user) && siadap.getEvaluationData2() != null;
    }

    @Override
    protected void process(ActivityInformation<SiadapProcess> activityInformation) {
	//validate the existing evaluation data
	Siadap siadap = activityInformation.getProcess().getSiadap();
	for (SiadapEvaluationItem item : siadap.getCurrentEvaluationItems()) {
	    if (item.getItemEvaluation() == null || item.getItemEvaluation().getPoints() == null) {
		throw new ActivityException(BundleUtil.getStringFromResourceBundle(getUsedBundle(),
			"error.siadapEvaluation.mustFillAllItems"), getLocalizedName());
	    }
	}

	//let's make some extra checks on the data inserted
	siadap.getEvaluationData2().validateData();

	activityInformation.getProcess().getSiadap().setEvaluationSealedDate(new LocalDate());

	//let's save that data
	SiadapEvaluationUniverse defaultSiadapEvalUniverse = siadap.getDefaultSiadapEvaluationUniverse();
	defaultSiadapEvalUniverse.setEvaluatorClassification(defaultSiadapEvalUniverse.getTotalEvaluationScoring());
	defaultSiadapEvalUniverse.setEvaluatorClassificationExcellencyAward(defaultSiadapEvalUniverse.getSiadapEvaluation()
		.getExcellencyAward());

    }

    @Override
    public boolean isConfirmationNeeded(SiadapProcess process) {
	Siadap siadap = process.getSiadap();
	return !siadap.isAutoEvaliationDone() && !siadap.isAutoEvaluationIntervalFinished();
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }

    protected static void revertProcess(ActivityInformation<SiadapProcess> activityInformation) {
	Siadap siadap = activityInformation.getProcess().getSiadap();
	if (siadap.isHarmonizationOfDefaultUniverseDone())
	    throw new SiadapException("error.cannot.revert.harmonized.to.no.evaluation");
	siadap.setEvaluationSealedDate(null);
	siadap.getDefaultSiadapEvaluationUniverse().removeHarmonizationAssessments();
    }


}
