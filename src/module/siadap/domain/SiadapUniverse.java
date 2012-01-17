/**
 * 
 */
package module.siadap.domain;

import java.util.List;
import java.util.ResourceBundle;

import module.organization.domain.Accountability;
import module.organization.domain.Party;
import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;
import pt.utl.ist.fenix.tools.util.i18n.Language;
import dml.runtime.Relation;
import dml.runtime.RelationListener;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 2 de Dez de 2011
 *
 * 
 */
public enum SiadapUniverse implements IPresentableEnum {
    SIADAP2, SIADAP3;

    static public List<SiadapUniverse> getQuotasUniverse(Siadap siadap) {
	//TODO joantune: take into account the fact that some SIADAP2 users might also count as SIADAP3!!
	return null;
    }

    @Override
    public String getLocalizedName() {
	final ResourceBundle resourceBundle = ResourceBundle.getBundle("resources.SiadapResources", Language.getLocale());
	return resourceBundle.getString(SiadapUniverse.class.getSimpleName() + "." + name());
    }
    
    //TODO joantune SIADAP-155
    public static final RelationListener<Accountability, Party> siadapHarmonizationRelationListener = new RelationListener<Accountability, Party>() {

	@Override
	public void afterAdd(Relation<Accountability, Party> arg0, Accountability arg1, Party arg2) {
	    // TODO Auto-generated method stub

	}

	@Override
	public void afterRemove(Relation<Accountability, Party> arg0, Accountability arg1, Party arg2) {
	    // TODO Auto-generated method stub

	}

	@Override
	public void beforeAdd(Relation<Accountability, Party> arg0, Accountability arg1, Party arg2) {
	    // TODO Auto-generated method stub

	}

	@Override
	public void beforeRemove(Relation<Accountability, Party> arg0, Accountability arg1, Party arg2) {
	    // TODO Auto-generated method stub

	}
    };
}
