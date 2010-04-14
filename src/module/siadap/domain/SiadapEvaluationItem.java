package module.siadap.domain;

import java.util.Comparator;

import org.joda.time.DateTime;

public class SiadapEvaluationItem extends SiadapEvaluationItem_Base {

    public static Comparator<SiadapEvaluationItem> COMPARATOR_BY_DATE = new Comparator<SiadapEvaluationItem>() {

	@Override
	public int compare(SiadapEvaluationItem o1, SiadapEvaluationItem o2) {
	    return o1.getWhenCreated().compareTo(o2.getWhenCreated());
	}

    };

    public SiadapEvaluationItem() {
	super();
	setWhenCreated(new DateTime());
	setSiadapRootModule(SiadapRootModule.getInstance());
    }

}
