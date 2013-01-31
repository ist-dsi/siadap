/*
 * @(#)Competence.java
 *
 * Copyright 2010 Instituto Superior Tecnico
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

import java.util.Comparator;

import pt.ist.fenixWebFramework.services.Service;

/**
 * 
 * @author Paulo Abrantes
 * 
 */
public class Competence extends Competence_Base {

	public static Comparator<Competence> COMPARATOR_BY_NUMBER = new Comparator<Competence>() {

		@Override
		public int compare(Competence o1, Competence o2) {
			return o1.getNumber().compareTo(o2.getNumber());
		}

	};

	public Competence(CompetenceType type, String name, String description) {
		super();
		setSiadapRootModule(SiadapRootModule.getInstance());
		setName(name);
		setDescription(description);
		setNumber(type.getNextCompetenceNumber());
		setCompetenceType(type);

	}

	@Service
	public static Competence createNewCompetence(CompetenceType competenceType, String name, String description) {
		return new Competence(competenceType, name, description);
	}

}
