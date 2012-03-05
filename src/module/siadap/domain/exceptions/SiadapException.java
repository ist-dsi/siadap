/*
 * @(#)SiadapException.java
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
package module.siadap.domain.exceptions;

import java.util.ResourceBundle;

import myorg.domain.exceptions.DomainException;
import pt.utl.ist.fenix.tools.util.i18n.Language;

/**
 * 
 * @author João Antunes
 * 
 */
public class SiadapException extends DomainException {

    public SiadapException(String key, String... args) {
	super(key, args);
    }
    
    public SiadapException(String key, Throwable throwable, String... args)
    {
	super(key, throwable,args);
    }

    @Override
    public ResourceBundle getBundle() {
	return ResourceBundle.getBundle("resources/SiadapResources", Language.getLocale());
    }

}
