package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import myorg.domain.User;

public class AutoEvaluation extends GenericEvaluationActivity {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	return siadap.getEvaluated().getUser() == user && !siadap.isAutoEvaliationDone()
		&& siadap.isEvaluatedWithKnowledgeOfObjectives() && !siadap.getSiadapEvaluationItems().isEmpty();

	// Auto evaluation should only be done in the following here...commenting for dev porpose for now
	// && siadap.getYear() + 1 == new LocalDate().getYear();
    }

}
