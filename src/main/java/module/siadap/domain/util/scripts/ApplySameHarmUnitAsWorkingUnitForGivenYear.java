/**
 * 
 */
package module.siadap.domain.util.scripts;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.jfree.data.time.Month;
import org.joda.time.LocalDate;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import module.organization.domain.Unit;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcessStateEnum;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapUniverse;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import pt.ist.bennu.core.domain.MyOrg;
import pt.ist.bennu.core.domain.scheduler.ReadCustomTask;
import pt.ist.bennu.core.domain.scheduler.TransactionalThread;
import pt.ist.fenixframework.pstm.AbstractDomainObject;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 25 de Jan de 2013
 * 
 *         This script is needed to resolve the changes that were made to the
 *         working unit but did not change the harmonization unit (as the
 *         interface wasn't contemplating this before)
 * 
 * 
 * 
 */
public class ApplySameHarmUnitAsWorkingUnitForGivenYear extends ReadCustomTask {

    public static final int year = 2012;

    public static final boolean DRY_RUN = true;
    static int totalSiadaps = 0;
    static int totalSiadaps2 = 0;
    static int siadapsReconnectedWithYear = 0;

    static final Multiset<Siadap> siadapsWithDifferentUniverses = HashMultiset.create();

    public static final List<String> siadaps = new ArrayList<String>();

    static class CorrectHarmRelationsForGivenYear extends TransactionalThread {
        private static final LocalDate DATE_OF_CHANGES = new LocalDate(year, Month.JUNE, 1);
        PrintWriter out;

        public CorrectHarmRelationsForGivenYear(PrintWriter out) {
            this.out = out;
        }

        @SuppressWarnings("boxing")
        @Override
        public void transactionalRun() {
            out.println("Cases corrected:");
            out.println("Tipo de caso|\t Nome|\t Unidade de trabalho|\t Unidade antiga de harmonização|\t Unidade nova de harmonização|\t ano do siadap |\t opcional - estado do siadap|\t");
            for (String siadapId : siadaps) {
                Siadap siadap = AbstractDomainObject.fromExternalId(siadapId);

                // let's register the SIADAPs whose siadapUniverse changed
                if (siadap.getYear() == 2012) {
                    try {

                        PersonSiadapWrapper previousSiadap = new PersonSiadapWrapper(siadap.getEvaluated(), 2011);
                        if (previousSiadap.getSiadap() != null
                                && !siadap.getDefaultSiadapUniverse().equals(previousSiadap.getDefaultSiadapUniverse())) {
                            if (siadap.getState().ordinal() > SiadapProcessStateEnum.NOT_CREATED.ordinal()
                                    && previousSiadap.getSiadap().getState().ordinal() > SiadapProcessStateEnum.NOT_CREATED
                                            .ordinal())
                                siadapsWithDifferentUniverses.add(siadap);
                        }
                    } catch (NullPointerException ex) {
                        // do nothing
                    }
                }

                boolean hasToChangeSIADAPAcc = false;

                PersonSiadapWrapper personSiadapWrapper = new PersonSiadapWrapper(siadap);
                SiadapUniverse defaultSiadapUniverse = personSiadapWrapper.getDefaultSiadapUniverse();
                Unit unitWhereIsHarmonized = null;
                try {

                    unitWhereIsHarmonized = personSiadapWrapper.getUnitWhereIsHarmonized(defaultSiadapUniverse);
                } catch (SiadapException ex) {
                    out.println("This fella has more than 1 harmonization unit!! siadap id: " + siadap.getExternalId());

                }
                UnitSiadapWrapper workingUnit = personSiadapWrapper.getWorkingUnit();

                if (personSiadapWrapper.getPerson() == null) {
                    out.println("dafuq?! got a process without a person!! Siadap externalID: " + siadap.getExternalId());
                } else {

                    if (workingUnit == null || workingUnit.getUnit() == null) {
                        out.println("Unidade de trabalho vazia |\t "
                                + personSiadapWrapper.getPerson().getPresentationName()
                                + " |\t não definida |\t não definida |\t não definida |\t " + siadap.getYear()
                                + " |\t" + siadap.getState().getLocalizedName());

                    } else if (unitWhereIsHarmonized == null) {
                        SiadapUniverse previousSiadapUniverse = new PersonSiadapWrapper(siadap.getEvaluated(),
                                Integer.valueOf((siadap.getYear().intValue() - 1))).getDefaultSiadapUniverse();

                        if (previousSiadapUniverse != null && !previousSiadapUniverse.equals(defaultSiadapUniverse)
                                && personSiadapWrapper.getUnitWhereIsHarmonized(previousSiadapUniverse) != null) {
                            hasToChangeSIADAPAcc = true;
                            unitWhereIsHarmonized = personSiadapWrapper
                                    .getUnitWhereIsHarmonized(previousSiadapUniverse);
                            out.println("Mudança de universo |\t "
                                    + personSiadapWrapper.getPerson().getPresentationName() + " |\t"
                                    + workingUnit.getUnit().getPresentationName() + "|\t não definida |\t "
                                    + unitWhereIsHarmonized.getPresentationName() + " |\t " + siadap.getYear() + " |\t"
                                    + siadap.getState().getLocalizedName());
                            if (!DRY_RUN) {
                                // TODO
                            }

                        } else {

                            try {

                                Unit newHarmonizationUnit = workingUnit.getHarmonizationUnit();

                                if (unitWhereIsHarmonized == null
                                        || !unitWhereIsHarmonized.equals(newHarmonizationUnit)) {
                                    String newHarmonizationUnitString;
                                    if (newHarmonizationUnit == null) {
                                        newHarmonizationUnitString = "NULL";

                                    } else {
                                        newHarmonizationUnitString = newHarmonizationUnit.getPresentationName();
                                    }
                                    String unitWhereIsHarmonizedString = unitWhereIsHarmonized == null ? "-" : unitWhereIsHarmonized.getPresentationName();
                                    out.print("Incoerência entre U.T e U.H |\t "
                                            + personSiadapWrapper.getPerson().getPresentationName() + " |\t "
                                            + workingUnit.getPresentationName() + " |\t "
                                            + unitWhereIsHarmonizedString + " |\t "
                                            + newHarmonizationUnitString + " |\t " + siadap.getYear() + " |\t"
                                            + siadap.getState().getLocalizedName());

                                    // let's make the change
                                    if (!DRY_RUN && workingUnit != null && workingUnit.getUnit() != null) {
                                        try {
                                            personSiadapWrapper
                                                    .changeHarmonizationUnitTo(workingUnit.getUnit(), DATE_OF_CHANGES,
                                                            "Correcção automática das unidades de harmonização para reflectirem as unidades de trabalho");
                                            out.println("|\tcorrigido");
                                        } catch (SiadapException ex) {
                                            out.println("|\t NÃO corrigido, razão: " + ex.getMessage());
                                        }
                                    } else {
                                        out.println();
                                    }
                                }
                            } catch (NullPointerException ex) {
                                out.println("NPE, moving on");
                                ex.printStackTrace();
                            }
                        }

                    }
                }
            }
        }
    }

    @Override
    public void doIt() {

        for (Siadap siadap : SiadapYearConfiguration.getSiadapYearConfiguration(year).getSiadaps()) {
            totalSiadaps++;
            siadaps.add(siadap.getExternalId());

        }
        // let's reconnect the disconnected ones
        CorrectHarmRelationsForGivenYear siadapReconnecter = new CorrectHarmRelationsForGivenYear(out);

        siadapReconnecter.start();

        try {
            siadapReconnecter.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace(out);
            throw new Error(ex);
        }

        out.println("Got " + siadapsWithDifferentUniverses.size()
                + " siadaps of people who changed universe.\n List of ISTIds who had a change of universe : ");
        for (Siadap siadap : siadapsWithDifferentUniverses) {
            out.println(siadap.getEvaluated().getPresentationName() + " ESTADO: "
                    + siadap.getState().getLocalizedName());

        }

    }

}
