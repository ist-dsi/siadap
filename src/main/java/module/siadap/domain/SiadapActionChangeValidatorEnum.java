/**
 * 
 */
package module.siadap.domain;

import java.util.Collection;

import module.organization.domain.Unit;
import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 7 de Fev de 2013
 * 
 *         Enum used to validate atomic changes in the SIADAP structure structure
 * 
 * 
 */
public enum SiadapActionChangeValidatorEnum {
    WORKING_UNIT_CHANGE {
        @Override
        public void validate(PersonSiadapWrapper siadapWrapper, Object... arguments) {
            HARMONIZATION_UNIT_CHANGE.validate(siadapWrapper, arguments);
        }
    },
    HARMONIZATION_UNIT_CHANGE {
        @Override
        public void validate(PersonSiadapWrapper siadapWrapper, Object... arguments) {
            int currentStateOrdinal = siadapWrapper.getSiadap().getState().ordinal();
            if (arguments.length == 0) {

                if (currentStateOrdinal > SiadapProcessStateEnum.WAITING_HARMONIZATION.ordinal()
                        || (currentStateOrdinal == SiadapProcessStateEnum.WAITING_HARMONIZATION.ordinal() && siadapWrapper
                                .getConfiguration().isHarmonizationPeriodOpenNow())) {
                    throw new SiadapException("error.changing.working.unit.cant.change.quotas.on.user.waiting.to.be.harmonized");
                }
            }

            else {
                if (currentStateOrdinal > SiadapProcessStateEnum.WAITING_HARMONIZATION.ordinal()) {
                    throw new SiadapException("error.changing.harmonization.unit.user.has.harmonization.closed");

                }

                //the first and only argument shall be the unit to change to
                Unit newHarmonizationUnit = (Unit) arguments[0];
                UnitSiadapWrapper unitSiadapWrapper = new UnitSiadapWrapper(newHarmonizationUnit, siadapWrapper.getYear());

                //we must not have any harmonization assessment
                SiadapEvaluationUniverse defaultSiadapEvaluationUniverse =
                        siadapWrapper.getSiadap().getDefaultSiadapEvaluationUniverse();
                if (defaultSiadapEvaluationUniverse == null) {
                    return;
                }
                if (defaultSiadapEvaluationUniverse.getHarmonizationAssessment() != null
                        || (defaultSiadapEvaluationUniverse.getCurrentExcellencyAward() == true && defaultSiadapEvaluationUniverse
                                .getHarmonizationAssessmentForExcellencyAward() != null)) {
                    throw new SiadapException("error.changing.harmonization.unit.user.has.harmonization.assessment");
                }

                //let's check if the unit we are changing from/to have the harmonization closed or not
                Unit currentUnit = siadapWrapper.getUnitWhereIsHarmonized();
                Collection<Unit> harmonizationClosedUnits = siadapWrapper.getConfiguration().getHarmonizationClosedUnits();

                if (harmonizationClosedUnits.contains(newHarmonizationUnit) && harmonizationClosedUnits.contains(currentUnit)) {
                    throw new SiadapException("error.changing.harmonization.unit.both.are.closed",
                            currentUnit.getPresentationName(), newHarmonizationUnit.getPresentationName());
                }

                if (harmonizationClosedUnits.contains(newHarmonizationUnit)) {
                    throw new SiadapException("error.changing.harmonization.unit.destination.closed",
                            newHarmonizationUnit.getPresentationName());
                }

                if (harmonizationClosedUnits.contains(currentUnit)) {
                    throw new SiadapException("error.changing.harmonization.unit.origin.closed",
                            currentUnit.getPresentationName());
                }

            }

        }
    },
    EVALUATOR_CHANGE {
        @Override
        public void validate(PersonSiadapWrapper siadapWrapper, Object... arguments) {
            Siadap siadap = siadapWrapper.getSiadap();
            if (siadap.isDefaultEvaluationDone()) {
                throw new SiadapException("error.cannot.change.evaluator.evaluation.already.done");
            }

        }
    },
    UNIVERSE_CHANGE {
        @Override
        public void validate(PersonSiadapWrapper siadapWrapper, Object... arguments) {
            // TODO Auto-generated method stub

        }
    },
    CAREER_CHANGE {
        @Override
        public void validate(PersonSiadapWrapper siadapWrapper, Object... arguments) {
            // TODO Auto-generated method stub

        }
    };

    abstract public void validate(PersonSiadapWrapper siadapWrapper, Object... arguments) throws SiadapException;

}
