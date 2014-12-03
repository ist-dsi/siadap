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

import org.fenixedu.bennu.core.domain.exceptions.DomainException;

/**
 * 
 * @author João Antunes
 * 
 */
public class SiadapException extends DomainException {

    private static final String BUNDLE = "resources/SiadapResources";

    public SiadapException(String key, String... args) {
        super(BUNDLE, key, args);
    }

//    public SiadapException(Status status, String key, String... args) {
//        super(status, BUNDLE, key, args);
//    }

    public SiadapException(Throwable cause, String key, String... args) {
        super(cause, BUNDLE, key, args);
    }

//    public SiadapException(Throwable cause, Status status, String key, String... args) {
//        super(cause, status, BUNDLE, key, args);
//    }

}
