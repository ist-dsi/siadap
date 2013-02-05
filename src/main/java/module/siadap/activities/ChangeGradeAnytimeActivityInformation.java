/**
 * 
 */
package module.siadap.activities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import module.siadap.domain.SiadapEvaluationUniverse;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 12 de Abr de 2012
 * 
 * 
 */
public class ChangeGradeAnytimeActivityInformation extends ActivityInformation<SiadapProcess> {

    public static class GradePerUniverseBean implements Serializable {
        /**
         * Default serial version
         */
        private static final long serialVersionUID = 1L;
        private BigDecimal gradeToChangeTo;
        private boolean assignExcellency;
        private String justification;
        private final SiadapEvaluationUniverse siadapEvaluationUniverse;

        public GradePerUniverseBean(SiadapEvaluationUniverse evaluationUniverse) {
            this.siadapEvaluationUniverse = evaluationUniverse;
        }

        public boolean isAssignExcellency() {
            return assignExcellency;
        }

        public void setAssignExcellency(boolean assignExcellency) {
            this.assignExcellency = assignExcellency;
        }

        public BigDecimal getGradeToChangeTo() {
            return gradeToChangeTo;
        }

        public void setGradeToChangeTo(BigDecimal gradeToChangeTo) {
            this.gradeToChangeTo = gradeToChangeTo;
        }

        public SiadapEvaluationUniverse getSiadapEvaluationUniverse() {
            return siadapEvaluationUniverse;
        }

        public String getJustification() {
            return justification;
        }

        public void setJustification(String justification) {
            this.justification = justification;
        }

    }

    private List<GradePerUniverseBean> siadapEvaluationUniversesBeans = new ArrayList<GradePerUniverseBean>();

    public ChangeGradeAnytimeActivityInformation(SiadapProcess process,
            WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
        super(process, activity);
        for (SiadapEvaluationUniverse siadapEvaluationUniverse : process.getSiadap().getSiadapEvaluationUniverses()) {
            getSiadapEvaluationUniversesBeans().add(new GradePerUniverseBean(siadapEvaluationUniverse));
        }
    }

    public List<GradePerUniverseBean> getSiadapEvaluationUniversesBeans() {
        return siadapEvaluationUniversesBeans;
    }

    public void setSiadapEvaluationUniversesBeans(List<GradePerUniverseBean> siadapEvaluationUniversesBeans) {
        this.siadapEvaluationUniversesBeans = siadapEvaluationUniversesBeans;
    }

    /**
     * @return true if there is at least one non null and valid grade and the
     *         other is null or valid
     */
    @Override
    public boolean hasAllneededInfo() {
        return isForwardedFromInput();
    }

    /**
     * Default Serial
     */
    private static final long serialVersionUID = 1L;

}
