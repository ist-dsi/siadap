/**
 * 
 */
package module.siadap.domain;

import module.organization.domain.AccountabilityType;
import module.organization.domain.AccountabilityType.AccountabilityTypeBean;
import myorg.domain.scheduler.WriteCustomTask;
import pt.utl.ist.fenix.tools.util.i18n.Language;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

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
	AccountabilityTypeBean accountabilityTypeBean = new AccountabilityTypeBean("unitRelations", new MultiLanguageString()
		.with(Language.pt, "Relações de unidades SIADAP").with(Language.en, "SIADAP unit relations"));
	AccountabilityType.create(accountabilityTypeBean);

	// relacao que liga uma Person e uma Unit e denota que a person e'
	// responsavel pela harmonizacao da unidade
	accountabilityTypeBean = new AccountabilityTypeBean("harmonizationResponsibleRelation", new MultiLanguageString().with(
		Language.pt, "Responsáveis por unidades de harmonização").with(Language.en, "Unit harmonization responsibles"));
	AccountabilityType.create(accountabilityTypeBean);

	// relacao entre uma Person e uma Unit e denota que a pessoa trabalha
	// nessa Unit e a sua avaliacao conta para as quotas (funcionarios IST)
	accountabilityTypeBean = new AccountabilityTypeBean("workingRelation", new MultiLanguageString().with(Language.pt,
		"Relação de trabalho com quota").with(Language.en, "Working relation with quota"));
	AccountabilityType.create(accountabilityTypeBean);

	// relacao entre Person e Unit e denota que a pessoa trabalha nesas Unit
	// mas a sua avaliacao n conta para as quotas (ADIST)
	accountabilityTypeBean = new AccountabilityTypeBean("workingRelationWithNoQuota", new MultiLanguageString().with(
		Language.pt, "Relação de trabalho sem quota").with(Language.en, "Working relation without quota"));
	AccountabilityType.create(accountabilityTypeBean);

	// relacao entre 2 Person e denota que uma Person e responsavel por
	// avaliar a outra (parent = evaluator, child = evaluated)
	accountabilityTypeBean = new AccountabilityTypeBean("evaluationRelation", new MultiLanguageString().with(Language.pt,
		"Relação de avaliação").with(Language.en, "Evaluation relation"));
	AccountabilityType.create(accountabilityTypeBean);
    }

}
