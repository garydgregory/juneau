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
package org.apache.juneau.http.header;

import static org.junit.runners.MethodSorters.*;

import java.io.*;
import java.util.function.*;

import org.apache.juneau.collections.*;
import org.apache.juneau.http.annotation.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.rest.annotation.*;
import org.apache.juneau.rest.client.*;
import org.apache.juneau.rest.mock.*;

import static org.apache.juneau.assertions.Assertions.*;
import static org.apache.juneau.http.HttpHeaders.*;

import org.junit.*;

@FixMethodOrder(NAME_ASCENDING)
public class BasicCsvArrayHeader_Test {


	private static final String HEADER = "Foo";

	@Rest
	public static class A {
		@RestOp
		public StringReader get(@Header(name=HEADER,multi=true) String[] h) {
			return new StringReader(h == null ? "null" : StringUtils.join(h, '|'));
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	// Method tests
	//------------------------------------------------------------------------------------------------------------------

	@Test
	public void a01_basic() throws Exception {
		RestClient c = client().build();

		c.get().header(csvArrayHeader(null,(Object)null)).run().assertBody().isEmpty();
		c.get().header(csvArrayHeader("","*")).run().assertBody().isEmpty();
		c.get().header(csvArrayHeader(HEADER,(Object)null)).run().assertBody().isEmpty();
		c.get().header(csvArrayHeader(null,"*")).run().assertBody().isEmpty();

		c.get().header(csvArrayHeader(null,()->null)).run().assertBody().isEmpty();
		c.get().header(csvArrayHeader(HEADER,(Supplier<?>)null)).run().assertBody().isEmpty();
		c.get().header(csvArrayHeader(null,(Supplier<?>)null)).run().assertBody().isEmpty();

		c.get().header(csvArrayHeader(HEADER,"foo")).run().assertBody().is("foo");
		c.get().header(csvArrayHeader(HEADER,()->"foo")).run().assertBody().is("foo");

		c.get().header(csvArrayHeader(HEADER,()->null)).run().assertBody().isEmpty();

		c.get().header(csvArrayHeader(HEADER,"foo,bar")).run().assertBody().is("foo,bar");
		c.get().header(csvArrayHeader(HEADER,()->"foo,bar")).run().assertBody().is("foo,bar");

		c.get().header(csvArrayHeader(HEADER,AList.of("foo","bar"))).run().assertBody().is("foo,bar");
		c.get().header(csvArrayHeader(HEADER,()->AList.of("foo","bar"))).run().assertBody().is("foo,bar");

		String[] x2 = {"foo","bar"};
		c.get().header(csvArrayHeader(HEADER,x2)).run().assertBody().is("foo,bar");
		c.get().header(csvArrayHeader(HEADER,()->x2)).run().assertBody().is("foo,bar");
	}

	@Test
	public void a02_contains() throws Exception {
		BasicCsvArrayHeader x = new BasicCsvArrayHeader("Foo", new String[]{null,"bar","baz"});
		assertBoolean(x.contains(null)).isFalse();
		assertBoolean(x.containsIgnoreCase(null)).isFalse();
		assertBoolean(x.contains("bar")).isTrue();
		assertBoolean(x.containsIgnoreCase("bar")).isTrue();
		assertBoolean(x.contains("qux")).isFalse();
		assertBoolean(x.containsIgnoreCase("qux")).isFalse();
		assertBoolean(x.contains("BAR")).isFalse();
		assertBoolean(x.containsIgnoreCase("BAR")).isTrue();

		BasicCsvArrayHeader x2 = csvArrayHeader("Foo",()->null);
		assertBoolean(x2.contains(null)).isFalse();
		assertBoolean(x2.containsIgnoreCase(null)).isFalse();
		assertBoolean(x2.contains("bar")).isFalse();
		assertBoolean(x2.containsIgnoreCase("bar")).isFalse();
	}

	@Test
	public void a03_assertList() throws Exception {
		csvArrayHeader("Foo", AList.of("bar")).assertList().contains("bar").assertList().doesNotContain("baz");
		new BasicCsvArrayHeader("Foo", null).assertList().doesNotExist();
	}

	//------------------------------------------------------------------------------------------------------------------
	// Helper methods.
	//------------------------------------------------------------------------------------------------------------------

	private static RestClientBuilder client() {
		return MockRestClient.create(A.class);
	}
}
