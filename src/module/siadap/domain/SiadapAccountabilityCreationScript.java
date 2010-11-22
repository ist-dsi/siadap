/**
 * 
 */
package module.siadap.domain;

import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;
import module.organization.domain.AccountabilityType;
import module.organization.domain.AccountabilityType.AccountabilityTypeBean;
import myorg.domain.scheduler.WriteCustomTask;

/**
 * @author João André Pereira Antunes (joao.antunes@tagus.ist.utl.pt)
 * 
 */
public class SiadapAccountabilityCreationScript extends WriteCustomTask {

    /*
     * (non-Javadoc)
     * 
     * @see myorg.domain.scheduler.WriteCustomTask#doService()
     */
    @Override
    protected void doService() {
	// relacao que liga 2 Units que fazem parte da estrutura SIADAP
	AccountabilityTypeBean accountabilityTypeBean = new AccountabilityTypeBean("unitRelations", MultiLanguageString.i18n()
		.add("pt", "Relações de unidades SIADAP").add("en", "SIADAP unit relations").finish());
	AccountabilityType.create(accountabilityTypeBean);

	// relacao que liga uma Person e uma Unit e denota que a person e'
	// responsavel pela harmonizacao da unidade
	accountabilityTypeBean = new AccountabilityTypeBean("harmonizationResponsibleRelation", MultiLanguageString.i18n()
		.add("pt", "Responsáveis por unidades de harmonização").add("en", "Unit harmonization responsibles").finish());
	AccountabilityType.create(accountabilityTypeBean);

	// relacao entre uma Person e uma Unit e denota que a pessoa trabalha
	// nessa Unit e a sua avaliacao conta para as quotas (funcionarios IST)
	accountabilityTypeBean = new AccountabilityTypeBean("workingRelation", MultiLanguageString.i18n()
		.add("pt", "Relação de trabalho com quota").add("en", "Working relation with quota").finish());
	AccountabilityType.create(accountabilityTypeBean);

	// relacao entre Person e Unit e denota que a pessoa trabalha nesas Unit
	// mas a sua avaliacao n conta para as quotas (ADIST)
	accountabilityTypeBean = new AccountabilityTypeBean("workingRelationWithNoQuota", MultiLanguageString.i18n()
		.add("pt", "Relação de trabalho sem quota").add("en", "Working relation without quota").finish());
	AccountabilityType.create(accountabilityTypeBean);

	// relacao entre 2 Person e denota que uma Person e responsavel por
	// avaliar a outra (parent = evaluator, child = evaluated)
	accountabilityTypeBean = new AccountabilityTypeBean("evaluationRelation", MultiLanguageString.i18n()
		.add("pt", "Relação de avaliação").add("en", "Evaluation relation").finish());
	AccountabilityType.create(accountabilityTypeBean);
    }

}
