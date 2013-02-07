/**
 * 
 */
package module.siadap.domain;

import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.wrappers.PersonSiadapWrapper;

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
        public void validate(PersonSiadapWrapper siadapWrapper) {
            HARMONIZATION_UNIT_CHANGE.validate(siadapWrapper);
        }
    },
    HARMONIZATION_UNIT_CHANGE {
        @Override
        public void validate(PersonSiadapWrapper siadapWrapper) {
            int currentStateOrdinal = siadapWrapper.getSiadap().getState().ordinal();
            if (currentStateOrdinal > SiadapProcessStateEnum.WAITING_HARMONIZATION.ordinal()
                    || (currentStateOrdinal == SiadapProcessStateEnum.WAITING_HARMONIZATION.ordinal() && siadapWrapper
                    .getConfiguration().isHarmonizationPeriodOpenNow())) {
                throw new SiadapException("error.changing.working.unit.cant.change.quotas.on.user.waiting.to.be.harmonized");
            }

        }
    },
    EVALUATOR_CHANGE {
        @Override
        public void validate(PersonSiadapWrapper siadapWrapper) {
            Siadap siadap = siadapWrapper.getSiadap();
            if (siadap.isDefaultEvaluationDone()) {
                throw new SiadapException("error.cannot.change.evaluator.evaluation.already.done");
            }

        }
    },
    UNIVERSE_CHANGE {
        @Override
        public void validate(PersonSiadapWrapper siadapWrapper) {
            // TODO Auto-generated method stub

        }
    },
    CAREER_CHANGE {
        @Override
        public void validate(PersonSiadapWrapper siadapWrapper) {
            // TODO Auto-generated method stub

        }
    };

    abstract public void validate(PersonSiadapWrapper siadapWrapper) throws SiadapException;

}
