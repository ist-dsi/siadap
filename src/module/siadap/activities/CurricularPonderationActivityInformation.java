/**
 * 
 */
package module.siadap.activities;

import java.math.BigDecimal;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapUniverse;
import module.siadap.domain.scoring.SiadapGlobalEvaluation;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 26 de Dez de 2011
 *
 * 
 */
public class CurricularPonderationActivityInformation extends ActivityInformation<SiadapProcess> {

    private SiadapUniverse siadapUniverseToApply;

    private String curricularPonderationRemarks;

    private Boolean assignExcellentGrade;

    private String excellentGradeJustification;

    private BigDecimal assignedGrade;

    private final Siadap siadap;

    public CurricularPonderationActivityInformation(SiadapProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
	super(process, activity);
	this.siadap = process.getSiadap();
    }

    @Override
    public boolean hasAllneededInfo() {
	if (siadapUniverseToApply != null && curricularPonderationRemarks != null && assignExcellentGrade != null
		&& assignedGrade != null
		&& SiadapGlobalEvaluation.isValidGrade(assignedGrade, assignExcellentGrade.booleanValue()))
	    return true;
	return false;
    }

    public SiadapUniverse getSiadapUniverseToApply() {
	return siadapUniverseToApply;
    }

    public void setSiadapUniverseToApply(SiadapUniverse siadapUniverseToApply) {
	this.siadapUniverseToApply = siadapUniverseToApply;
    }

    public String getCurricularPonderationRemarks() {
	return curricularPonderationRemarks;
    }

    public void setCurricularPonderationRemarks(String curricularPonderationRemarks) {
	this.curricularPonderationRemarks = curricularPonderationRemarks;
    }

    public Boolean getAssignExcellentGrade() {
	return assignExcellentGrade;
    }

    public void setAssignExcellentGrade(Boolean assignExcellentGrade) {
	this.assignExcellentGrade = assignExcellentGrade;
    }

    public BigDecimal getAssignedGrade() {
	return assignedGrade;
    }

    public void setAssignedGrade(BigDecimal assignedGrade) {
	this.assignedGrade = assignedGrade;
    }

    public Siadap getSiadap() {
	return siadap;
    }

    public String getExcellentGradeJustification() {
	return excellentGradeJustification;
    }

    public void setExcellentGradeJustification(String excellentGradeJustification) {
	this.excellentGradeJustification = excellentGradeJustification;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

}
