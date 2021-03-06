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

import static org.apache.juneau.http.HttpHeaders.*;
import static org.junit.runners.MethodSorters.*;

import java.io.*;
import java.util.function.*;

import org.apache.juneau.http.annotation.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.rest.annotation.*;
import org.apache.juneau.rest.client.*;
import org.apache.juneau.rest.mock.*;
import org.junit.*;

@FixMethodOrder(NAME_ASCENDING)
public class Date_Test {

	private static final String HEADER = "Date";
	private static final String VALUE = "Sat, 29 Oct 1994 19:43:31 GMT";

	@Rest
	public static class A {
		@RestOp
		public StringReader get(@Header(name=HEADER,multi=true) String[] h) {
			return new StringReader(h == null ? "null" : StringUtils.join(h, ','));
		}
	}

	//------------------------------------------------------------------------------------------------------------------
	// Method tests
	//------------------------------------------------------------------------------------------------------------------

	@Test
	public void a01_basic() throws Exception {
		RestClient c = client().build();

		c.get().header(date((String)null)).run().assertBody().isEmpty();
		c.get().header(date((Object)null)).run().assertBody().isEmpty();
		c.get().header(date((Supplier<?>)null)).run().assertBody().isEmpty();
		c.get().header(date(()->null)).run().assertBody().isEmpty();
		c.get().header(date(VALUE)).run().assertBody().is(VALUE);
		c.get().header(date(VALUE)).run().assertBody().is(VALUE);
		c.get().header(date(new StringBuilder(VALUE))).run().assertBody().is(VALUE);
		c.get().header(date(()->VALUE)).run().assertBody().is(VALUE);
		c.get().header(new Date(VALUE)).run().assertBody().is(VALUE);
	}

	//------------------------------------------------------------------------------------------------------------------
	// Helper methods.
	//------------------------------------------------------------------------------------------------------------------

	private static RestClientBuilder client() {
		return MockRestClient.create(A.class);
	}
}
