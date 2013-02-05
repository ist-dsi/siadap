/**
 * 
 */
package module.siadap.presentationTier.vaadin;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.util.SiadapProcessCounter;
import pt.ist.bennu.core.util.BundleUtil;
import pt.ist.vaadinframework.annotation.EmbeddedComponent;
import pt.ist.vaadinframework.data.reflect.DomainContainer;
import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 4 de Dez de 2012
 * 
 * 
 */
@EmbeddedComponent(path = { "listSiadapsComponent" }, args = { "year", "typeOfList" })
public class ListSiadapsComponent extends CustomComponent implements EmbeddedComponentContainer {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static enum TypeOfList {
        SIADAPS_NOT_LISTED_IN_STATISTICS

        {
            @Override
            public Set<Siadap> getListOfSiadaps(SiadapProcessCounter siadapProcessCounter) {
                return siadapProcessCounter.getOrCreateSiadapsNotListed();
            }

            @Override
            public String getTitleOfTable() {
                try {
                    return BundleUtil.getStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
                            "ListSiadapsComponent.TypeOfList.title");

                } catch (MissingResourceException exception) {
                    return this.getClass().getSimpleName();
                }
            }
        };
        public abstract Set<Siadap> getListOfSiadaps(SiadapProcessCounter siadapProcessCounter);

        public abstract String getTitleOfTable();
    }

    public ListSiadapsComponent() {
        super();
    }

    /**
     * 
     */
    public ListSiadapsComponent(Set<Siadap> siadaps, TypeOfList typeOfList) {
        super();
        renderInterface(siadaps, typeOfList);
    }

    private void renderInterface(Set<Siadap> siadaps, TypeOfList typeOfList) {
        VerticalLayout rootVerticalLayout = new VerticalLayout();
        setCompositionRoot(rootVerticalLayout);

        DomainContainer<Siadap> siadapsContainer = new DomainContainer<Siadap>(siadaps, Siadap.class);

        Label listTitle = new Label(typeOfList.getTitleOfTable());
        rootVerticalLayout.addComponent(listTitle);

        siadapsContainer.setContainerProperties("evaluated.presentationName", "state");

        Table siadapsTable = new Table();
        siadapsTable.setContainerDataSource(siadapsContainer);
        //	siadapsTable.addGeneratedColumn("state", new ColumnGenerator() {
        //
        //	    @Override
        //	    public Object generateCell(Table source, Object itemId, Object columnId) {
        //		DomainItem<Siadap> siadap = (DomainItem<Siadap>) source.getContainerDataSource().getItem(itemId);
        //		return new Label(siadap.getValue().getState().getLocalizedName());
        //	    }
        //	});
        siadapsTable.setVisibleColumns(new String[] { "evaluated.presentationName", "state" });
        //	siadapsTable.setVisibleColumns(new String[] { "evaluated.presentationName", "state" });
        rootVerticalLayout.addComponent(siadapsTable);

    }

    /**
     * @param compositionRoot
     */
    public ListSiadapsComponent(Component compositionRoot) {
        super(compositionRoot);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void setArguments(Map<String, String> arguments) {
        String typeOfListString = arguments.get("typeOfList");
        TypeOfList typeOfList = TypeOfList.valueOf(typeOfListString);

        int year = Integer.valueOf(arguments.get("year"));

        SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);

        SiadapProcessCounter siadapProcessCounter =
                new SiadapProcessCounter(configuration.getSiadapStructureTopUnit(), false, configuration, true);

        renderInterface(siadapProcessCounter.getOrCreateSiadapsNotListed(), typeOfList);

    }

    @Override
    public boolean isAllowedToOpen(Map<String, String> parameters) {
        Integer year = Integer.valueOf(parameters.get("year"));
        SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);

        return siadapYearConfiguration.isCurrentUserMemberOfCCA()
                || siadapYearConfiguration.isCurrentUserMemberOfStructureManagementGroup();
    }

}
