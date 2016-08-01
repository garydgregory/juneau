/***************************************************************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 ***************************************************************************************************************************/
package org.apache.juneau.a.rttests;

import static org.apache.juneau.TestUtils.*;
import static org.junit.Assert.*;

import java.text.*;
import java.util.*;

import org.apache.juneau.dto.jsonschema.*;
import org.apache.juneau.parser.*;
import org.apache.juneau.serializer.*;
import org.junit.*;


/**
 * Tests designed to serialize and parse objects to make sure we end up
 * with the same objects for all serializers and parsers.
 */
@SuppressWarnings("hiding")
public class CT_RoundTripNumericConstructors extends RoundTripTest {

	public CT_RoundTripNumericConstructors(String label, Serializer s, Parser p, int flags) throws Exception {
		super(label, s, p, flags);
	}

	//====================================================================================================
	// Test parsing numbers to dates.
	//====================================================================================================
	@Test
	public void testParseNumberToDate() throws Exception {
		if (isValidationOnly())
			return;

		Serializer s = getSerializer();
		Parser p = getParser();
		Date d = new Date(100, 1, 1);

		Object r = s.serialize(d.getTime());
		Date d2 = p.parse(r, Date.class);
		assertEquals(d.getTime(), d2.getTime());
	}
}
