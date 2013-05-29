/**
 * 
 */
package module.siadap.domain.util.scripts;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapYearConfiguration;

import org.joda.time.LocalDate;

import pt.ist.bennu.core.domain.scheduler.ReadCustomTask;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 19 de Abr de 2013
 * 
 *         List everybody with an active working accountability on a given date but no SIADAP
 * 
 */
public class ListWorkingPeopleNotInSiadap extends ReadCustomTask {

    private static LocalDate DATE_TO_CHECK = new LocalDate();

    private static SiadapYearConfiguration siadapToCheck;

    private static AccountabilityType ORGANIZATIONAL_ACC_TYPE;
    private static AccountabilityType PERSONNEL_ACC_TYPE;

    private static Set<Person> personsWithSiadapCreated;

    private static Set<Person> personsWorkingAtGivenDate;

    /* (non-Javadoc)
     * @see jvstm.TransactionalCommand#doIt()
     */
    @Override
    public void doIt() {
        PERSONNEL_ACC_TYPE = AccountabilityType.readBy("Personnel");
        ORGANIZATIONAL_ACC_TYPE = AccountabilityType.readBy("Organizational");
        siadapToCheck = SiadapYearConfiguration.getSiadapYearConfiguration(2013);
        personsWithSiadapCreated = new HashSet<Person>();
        personsWorkingAtGivenDate = new HashSet<Person>();

        personsWithSiadapCreated.addAll(Collections2.transform(siadapToCheck.getSiadapsSet(), new Function<Siadap, Person>() {

            @Override
            @Nullable
            public Person apply(@Nullable Siadap input) {
                if (input == null) {
                    return null;
                }
                return input.getEvaluated();
            }
        }));

        descendAndRegisterWorkingPeople(siadapToCheck.getSiadapStructureTopUnit());

        personsWorkingAtGivenDate.removeAll(personsWithSiadapCreated);

        out.println("Got " + personsWorkingAtGivenDate.size() + " persons working without SIADAP for Siadap "
                + siadapToCheck.getLabel());
        for (Person person : personsWorkingAtGivenDate) {
            out.println(person.getPresentationName());
        }

    }

    private void descendAndRegisterWorkingPeople(Unit unit) {
        for (Accountability acc : unit.getChildrenAccountabilities(ORGANIZATIONAL_ACC_TYPE, PERSONNEL_ACC_TYPE)) {

            if (acc.isActive(DATE_TO_CHECK)) {

                if (acc.getChild() instanceof Person && acc.getAccountabilityType().equals(PERSONNEL_ACC_TYPE)) {
                    personsWorkingAtGivenDate.add((Person) acc.getChild());

                } else if (acc.getChild() instanceof Unit) {
                    descendAndRegisterWorkingPeople((Unit) acc.getChild());

                }
            }

        }

    }

}
