package module.siadap.domain.util;

import java.util.function.Predicate;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;

public class AccountabilityPredicate {

    public static Predicate<Accountability> byType(final AccountabilityType type) {
        return (a) -> type == null || a.getAccountabilityType() == type;
    }

}
