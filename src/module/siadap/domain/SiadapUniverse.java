/**
 * 
 */
package module.siadap.domain;

import java.util.List;
import java.util.ResourceBundle;

import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;
import pt.utl.ist.fenix.tools.util.i18n.Language;

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

}
