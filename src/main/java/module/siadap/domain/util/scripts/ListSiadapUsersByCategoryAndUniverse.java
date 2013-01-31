/*
 * @(#)ListSiadapUsersByCategoryAndUniverse.java
 *
 * Copyright 2011 Instituto Superior Tecnico
 * Founding Authors: Paulo Abrantes
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the SIADAP Module.
 *
 *   The SIADAP Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The SIADAP Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the SIADAP Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package module.siadap.domain.util.scripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcessStateEnum;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.util.SiadapStatisticsSummaryBoardUniversesEnum;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import pt.ist.bennu.core.domain.scheduler.ReadCustomTask;

/**
 * Class used to list the Siadap users [full name (istId)] separated by
 * competence type category and universe [counts for quote or not] for a given
 * year
 * 
 * @author João Antunes
 * 
 */
public class ListSiadapUsersByCategoryAndUniverse extends ReadCustomTask {

	private static final int SIADAP_YEAR_FOR_LISTS = 2011;

	private static HashMap<Boolean, HashMap<String, List<String>>> list = new HashMap<Boolean, HashMap<String, List<String>>>();
	private final LocalDate today = new LocalDate();

	private transient SiadapYearConfiguration configuration;
	private transient AccountabilityType unitRelations;
	private transient AccountabilityType evaluationRelation;
	private transient AccountabilityType workingUnitWithQuotaRelation;
	private transient AccountabilityType workingUnitWithoutQuotaRelation;

	/* (non-Javadoc)
	 * @see jvstm.TransactionalCommand#doIt()
	 */
	@Override
	public void doIt() {
		configuration = SiadapYearConfiguration.getSiadapYearConfiguration(SIADAP_YEAR_FOR_LISTS);

		unitRelations = configuration.getUnitRelations();
		evaluationRelation = configuration.getEvaluationRelation();
		workingUnitWithQuotaRelation = configuration.getWorkingRelation();
		workingUnitWithoutQuotaRelation = configuration.getWorkingRelationWithNoQuota();

		Unit unit = configuration.getSiadapStructureTopUnit();

		count(unit, true);

		//let's print the list

		out.println("Listagem tirada em " + new DateTime());

		for (Boolean bool : list.keySet()) {
			out.println("\n\n");
			if (bool) {
				out.println("Listagem de pessoas que contabilizam para as quotas:");
			} else {
				out.println("Listagem de pessoas que não contabilizam para as quotas:");
			}
			out.println("\n\n");
			HashMap<String, List<String>> categoryMap = list.get(bool);
			for (String categoryString : categoryMap.keySet()) {
				List<String> personList = categoryMap.get(categoryString);
				out.println(categoryString + " (total: " + personList.size() + ")\n");
				for (String personNameAndIstId : personList) {
					out.println(personNameAndIstId);
				}
				out.println();
			}
		}

	}

	private void count(Unit unit, boolean distinguishBetweenUniverses) {
		for (final Accountability accountability : unit.getChildAccountabilitiesSet()) {
			if (accountability.isActive(today)) {
				final AccountabilityType accountabilityType = accountability.getAccountabilityType();
				if (accountabilityType == unitRelations) {
					final Unit child = (Unit) accountability.getChild();
					count(child, distinguishBetweenUniverses);
				} else if (accountabilityType == workingUnitWithQuotaRelation) {
					final Person person = (Person) accountability.getChild();
					count(person, true);

				} else if (accountabilityType == workingUnitWithoutQuotaRelation) {
					final Person person = (Person) accountability.getChild();
					count(person, false);

				}

			}
		}

	}

	private void count(final Person person, boolean withQuota) {
		final Siadap siadap = configuration.getSiadapFor(person);
		final SiadapProcessStateEnum state =
				siadap == null ? SiadapProcessStateEnum.NOT_CREATED : SiadapProcessStateEnum.getState(siadap);

		//let's fill the complicated hashmap
		HashMap<String, List<String>> categoryHashMap = list.get(Boolean.valueOf(withQuota));
		if (categoryHashMap == null)
		//if it doesn't exist for this quotaaware/noquotaaware universe, let's create it
		{
			categoryHashMap = new HashMap<String, List<String>>();
			list.put(Boolean.valueOf(withQuota), categoryHashMap);
		}

		SiadapStatisticsSummaryBoardUniversesEnum universesEnum =
				SiadapStatisticsSummaryBoardUniversesEnum.getStatisticsUniverse(state);
		List<String> categoryList = categoryHashMap.get(universesEnum.getCategoryString(siadap));
		if (categoryList == null) {
			//do we already have a counter for this category?!, if not, we create it
			categoryList = new ArrayList<String>();
			categoryHashMap.put(universesEnum.getCategoryString(siadap), categoryList);
		}
		categoryList.add(person.getName() + "(" + person.getUser().getUsername() + ")");
	}

}
