// ***************************************************************************************************************************
// * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file *
// * distributed with this work for additional information regarding copyright ownership.  The ASF licenses this file        *
// * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance            *
// * with the License.  You may obtain a copy of the License at                                                              *
// *                                                                                                                         *
// *  http://www.apache.org/licenses/LICENSE-2.0                                                                             *
// *                                                                                                                         *
// * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an  *
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the        *
// * specific language governing permissions and limitations under the License.                                              *
// ***************************************************************************************************************************
package org.apache.juneau.http;

import static org.apache.juneau.assertions.Assertions.*;
import static org.apache.juneau.http.HttpHeaders.*;
import static org.junit.Assert.*;
import static org.junit.runners.MethodSorters.*;
import static org.apache.juneau.http.HttpParts.*;

import java.util.*;
import java.util.function.*;

import org.apache.http.*;
import org.apache.juneau.collections.*;
import org.apache.juneau.http.header.*;
import org.apache.juneau.http.part.*;
import org.apache.juneau.utils.*;
import org.junit.*;

@FixMethodOrder(NAME_ASCENDING)
public class BasicHeader_Test {

	@Test
	public void a01_ofPair() {
		BasicHeader x = basicHeader("Foo:bar");
		assertEquals("Foo", x.getName());
		assertEquals("bar", x.getValue());

		x = basicHeader(" Foo : bar ");
		assertEquals("Foo", x.getName());
		assertEquals("bar", x.getValue());

		x = basicHeader(" Foo : bar : baz ");
		assertEquals("Foo", x.getName());
		assertEquals("bar : baz", x.getValue());

		x = basicHeader("Foo");
		assertEquals("Foo", x.getName());
		assertEquals("", x.getValue());

		assertNull(basicHeader((String)null));
	}

	@Test
	public void a02_of() {
		BasicHeader x;
		x = header("Foo","bar");
		assertObject(x).asJson().is("'Foo: bar'");
		x = header("Foo",()->"bar");
		assertObject(x).asJson().is("'Foo: bar'");
	}

	@Test
	public void a03_cast() {
		BasicPart x1 = part("X1","1");
		SerializedPart x2 = serializedPart("X2","2");
		Header x3 = header("X3","3");
		SerializedHeader x4 = serializedHeader("X4","4");
		Map.Entry<String,Object> x5 = AMap.of("X5",(Object)"5").entrySet().iterator().next();
		org.apache.http.message.BasicNameValuePair x6 = new org.apache.http.message.BasicNameValuePair("X6","6");
		Partable x7 = new Partable() {
			@Override
			public Part asPart() {
				return part("X7","7");
			}
		};
		Headerable x8 = new Headerable() {
			@Override
			public Header asHeader() {
				return header("X8","8");
			}
		};
		SerializedPart x9 = serializedPart("X9",()->"9");

		assertObject(BasicHeader.cast(x1)).isType(Header.class).asJson().is("'X1: 1'");
		assertObject(BasicHeader.cast(x2)).isType(Header.class).asJson().is("'X2: 2'");
		assertObject(BasicHeader.cast(x3)).isType(Header.class).asJson().is("'X3: 3'");
		assertObject(BasicHeader.cast(x4)).isType(Header.class).asJson().is("'X4: 4'");
		assertObject(BasicHeader.cast(x5)).isType(Header.class).asJson().is("'X5: 5'");
		assertObject(BasicHeader.cast(x6)).isType(Header.class).asJson().is("'X6: 6'");
		assertObject(BasicHeader.cast(x7)).isType(Header.class).asJson().is("'X7: 7'");
		assertObject(BasicHeader.cast(x8)).isType(Header.class).asJson().is("'X8: 8'");
		assertObject(BasicHeader.cast(x9)).isType(Header.class).asJson().is("'X9: 9'");

		assertThrown(()->BasicHeader.cast("X")).is("Object of type java.lang.String could not be converted to a Header.");
		assertThrown(()->BasicHeader.cast(null)).is("Object of type null could not be converted to a Header.");

		assertTrue(BasicHeader.canCast(x1));
		assertTrue(BasicHeader.canCast(x2));
		assertTrue(BasicHeader.canCast(x3));
		assertTrue(BasicHeader.canCast(x4));
		assertTrue(BasicHeader.canCast(x5));
		assertTrue(BasicHeader.canCast(x6));
		assertTrue(BasicHeader.canCast(x7));
		assertTrue(BasicHeader.canCast(x8));
		assertTrue(BasicHeader.canCast(x9));

		assertFalse(BasicHeader.canCast("X"));
		assertFalse(BasicHeader.canCast(null));
	}

	@Test
	public void a05_assertions() {
		BasicHeader x = header("X1","1");
		x.assertName().is("X1").assertValue().is("1");
	}

	@Test
	public void a07_eqIC() {
		BasicHeader x = header("X1","1");
		assertTrue(x.equalsIgnoreCase("1"));
		assertFalse(x.equalsIgnoreCase("2"));
		assertFalse(x.equalsIgnoreCase(null));
	}

	@Test
	public void a08_getElements() {
		Mutable<Integer> m = Mutable.of(1);
		Header h1 = header("X1","1");
		Header h2 = header("X2",()->m);
		Header h3 = header("X3",null);

		HeaderElement[] x;

		x = h1.getElements();
		assertEquals(1, x.length);
		assertEquals("1", x[0].getName());
		x = h1.getElements();
		assertEquals(1, x.length);
		assertEquals("1", x[0].getName());

		x = h2.getElements();
		assertEquals(1, x.length);
		assertEquals("1", x[0].getName());
		m.set(2);
		x = h2.getElements();
		assertEquals(1, x.length);
		assertEquals("2", x[0].getName());

		x = h3.getElements();
		assertEquals(0, x.length);
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void a09_equals() {
		BasicHeader h1 = header("Foo","bar"), h2 = header("Foo","bar"), h3 = header("Bar","bar"), h4 = header("Foo","baz");
		assertInteger(h1.hashCode()).exists();
		assertBoolean(h1.equals(h2)).isTrue();
		assertBoolean(h1.equals(h3)).isFalse();
		assertBoolean(h1.equals(h4)).isFalse();
		assertBoolean(h1.equals("foo")).isFalse();

	}

	//------------------------------------------------------------------------------------------------------------------
	// Utility methods
	//------------------------------------------------------------------------------------------------------------------

	private BasicHeader header(String name, Object val) {
		return basicHeader(name, val);
	}

	private BasicHeader header(String name, Supplier<?> val) {
		return basicHeader(name, val);
	}

	private BasicPart part(String name, Object val) {
		return basicPart(name, val);
	}
}
