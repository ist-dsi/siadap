package module.siadap.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.joda.time.LocalDate;

import pt.ist.fenixWebFramework.services.ServiceManager;
import pt.ist.fenixWebFramework.services.ServicePredicate;
import pt.ist.fenixframework.pstm.AbstractDomainObject;
import module.organization.domain.AccountabilityType;
import module.organization.domain.OrganizationalModel;
import module.organization.domain.Party;
import module.organization.domain.Unit;
import myorg.domain.scheduler.ReadCustomTask;

public class ImportOrganizationalStructure extends ReadCustomTask {

    public static class UnitPair {
	Unit unit1;
	Unit unit2;

	public UnitPair(Unit unit1, Unit unit2) {
	    this.unit1 = unit1;
	    this.unit2 = unit2;
	}

	public Unit getUnit1() {
	    return unit1;
	}

	public Unit getUnit2() {
	    return unit2;
	}

	@Override
	public int hashCode() {
	    return unit1.hashCode() + unit2.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
	    if (obj instanceof UnitPair) {
		UnitPair u2 = (UnitPair) obj;
		return u2.unit1 == this.unit1 && u2.unit2 == this.unit2;
	    }
	    return false;
	}
    }

    private final HashSet<UnitPair> units = new HashSet<UnitPair>();
    private SiadapYearConfiguration configuration;
    private final LocalDate now = new LocalDate();
    private AccountabilityType type;

    @Override
    public void doIt() {

	configuration = SiadapYearConfiguration.getSiadapYearConfiguration(2010);
	type = AccountabilityType.readBy("Organizational");
	OrganizationalModel model = AbstractDomainObject.fromExternalId("545460846593");

	for (Party party : model.getParties()) {
	    if (party.isUnit()) {
		Unit unit = (Unit) party;
		doUnit(unit, units);
	    }
	}

	ServiceManager.execute(new ServicePredicate() {

	    @Override
	    public void execute() {
		for (UnitPair pair : units) {
		    pair.getUnit1().addChild(pair.getUnit2(), configuration.getUnitRelations(), now, null);
		}

	    }

	});
    }

    public void doUnit(Unit unit, HashSet<UnitPair> units) {
	for (Unit someUnit : unit.getChildUnits(type)) {
	    units.add(new UnitPair(unit, someUnit));
	    doUnit(someUnit, units);
	}
    }
}
