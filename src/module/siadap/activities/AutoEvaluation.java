package module.siadap.activities;

import module.siadap.domain.SiadapProcess;
import myorg.domain.User;

public class AutoEvaluation extends GenericEvaluationActivity {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	return process.getSiadap().getEvaluated().getUser() == user;
    }

}
