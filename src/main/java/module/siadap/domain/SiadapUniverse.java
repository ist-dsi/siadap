/*
 * @(#)SiadapUniverse.java
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
package module.siadap.domain;

import java.util.List;
import java.util.ResourceBundle;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Party;
import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;
import pt.utl.ist.fenix.tools.util.i18n.Language;
import dml.runtime.Relation;
import dml.runtime.RelationListener;

/**
 * 
 * @author João Antunes
 * 
 */
public enum SiadapUniverse implements IPresentableEnum {
	SIADAP2 {

		@Override
		public AccountabilityType getHarmonizationRelation(SiadapYearConfiguration siadapYearConfiguration) {
			return siadapYearConfiguration.getSiadap2HarmonizationRelation();
		}
	},
	SIADAP3 {

		@Override
		public AccountabilityType getHarmonizationRelation(SiadapYearConfiguration siadapYearConfiguration) {
			return siadapYearConfiguration.getSiadap3HarmonizationRelation();
		}
	};

	static public List<SiadapUniverse> getQuotasUniverse(Siadap siadap) {
		//TODO joantune: take into account the fact that some SIADAP2 users might also count as SIADAP3!!
		return null;
	}

	@Override
	public String getLocalizedName() {
		final ResourceBundle resourceBundle = ResourceBundle.getBundle("resources.SiadapResources", Language.getLocale());
		return resourceBundle.getString(SiadapUniverse.class.getSimpleName() + "." + name());
	}

	public AccountabilityType getHarmonizationRelation(int year) {
		return getHarmonizationRelation(SiadapYearConfiguration.getSiadapYearConfiguration(year));
	}

	public abstract AccountabilityType getHarmonizationRelation(SiadapYearConfiguration siadapYearConfiguration);

	//TODO joantune SIADAP-155
	public static final RelationListener<Accountability, Party> siadapHarmonizationRelationListener =
			new RelationListener<Accountability, Party>() {

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
