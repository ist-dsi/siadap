/*
 * @(#)CustomTestSuiteTest.java
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
package customTestSuites;

import junit.framework.TestCase;

import org.cubictest.exporters.selenium.SeleniumRunner;

/**
 * Custom Test Suite.
 * Test set up and tear down logic can be put here. See JUnit documentation.
 * 
 * @author João Antunes
 * 
 */
public class CustomTestSuiteTest extends TestCase {

	public void test() {
		
		SeleniumRunner runner = new SeleniumRunner();
		
		//run all tests in the "/tests" folder:
		runner.runTests("/tests");
		
		
		//alternatively, run single tests, e.g: 
		//runner.runTest("/tests/myTest.aat");
	}
}

