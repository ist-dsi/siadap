/*
 * @(#)SiadapStatisticsSummaryBoardUniversesEnum.java
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
package module.siadap.domain.util;

import pt.ist.bennu.core.util.BundleUtil;
import pt.ist.bennu.core.util.ClassNameBundle;
import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcessStateEnum;

/**
 * 
 * @author João Antunes
 * 
 */
@ClassNameBundle(bundle = "resources/SiadapResources")
public enum SiadapStatisticsSummaryBoardUniversesEnum implements IPresentableEnum {
    KNOWN_CATEGORY {
	@Override
	public String getCategoryString(Siadap siadap) {
	    return siadap.getDefaultCompetenceType().getName();
	}

	@Override
	public int getNrOfSubCategories() {
	    return 1;
	    //it's the total sub-category
	}

	@Override
	public int getSubCategoryIndex(SiadapProcessStateEnum state) {
	    return 0;
	}


    }
,
    UNKNOWN_CATEGORY {
	@Override
	public String getCategoryString(Siadap siadap) {
	    return getLocalizedName();
	}

	@Override
	public int getNrOfSubCategories() {

	    //	    SiadapProcessStateEnum.NOT_CREATED;
	    //	    SiadapProcessStateEnum.INCOMPLETE_OBJ_OR_COMP;
	    //	    SiadapProcessStateEnum.NOT_SEALED;
	    //	    SiadapProcessStateEnum.EVALUATION_NOT_GOING_TO_BE_DONE;
	    //	    SiadapProcessStateEnum.NOT_YET_SUBMITTED_FOR_ACK;
	    //WAITING_EVAL_OBJ_ACK,
	    return SiadapProcessStateEnum.NOT_YET_SUBMITTED_FOR_ACK.ordinal() + 1;
	}

	@Override
	public int getSubCategoryIndex(SiadapProcessStateEnum state) {
	    //in this case, it translates directly to the ordinal, so let's get that
	    return state.ordinal();
	}

    };
    
    public abstract String getCategoryString(Siadap siadap);

    public abstract int getSubCategoryIndex(SiadapProcessStateEnum state);

    @Override
    public String getLocalizedName() {
	String key = "label." + name();
	String name = key;
	try {
	    name = BundleUtil.getStringFromResourceBundle("resources/SiadapResources", key);
	} catch (java.util.MissingResourceException ex) {
	}
	return name;
    }

    /**
     * 
     * @return the number of divisions inside the category e.g. acknowledeged
     *         proccess; proccess not created, etc.
     */
    public abstract int getNrOfSubCategories();

    public static SiadapStatisticsSummaryBoardUniversesEnum getStatisticsUniverse(SiadapProcessStateEnum processState) {
	if (processState.ordinal() < SiadapProcessStateEnum.WAITING_EVAL_OBJ_ACK.ordinal()) {
	    return UNKNOWN_CATEGORY;
	}
	return KNOWN_CATEGORY;
    }
    

}
