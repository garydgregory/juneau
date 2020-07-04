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
import static org.junit.runners.MethodSorters.*;

import org.apache.http.*;
import org.apache.juneau.http.header.*;
import org.junit.*;

@FixMethodOrder(NAME_ASCENDING)
public class HeaderSupplierTest {

	@Test
	public void a01_basic() {
		HeaderSupplier h = HeaderSupplier.of();
		assertObject(h.iterator()).json().is("[]");
		h.add(header("Foo","bar"));
		assertObject(h.iterator()).json().is("['Foo: bar']");
		h.add(header("Foo","baz"));
		assertObject(h.iterator()).json().is("['Foo: bar','Foo: baz']");
		h.add(HeaderSupplier.of());
		assertObject(h.iterator()).json().is("['Foo: bar','Foo: baz']");
		h.add(HeaderSupplier.of(header("Foo","qux")));
		assertObject(h.iterator()).json().is("['Foo: bar','Foo: baz','Foo: qux']");
		h.add(HeaderSupplier.of(header("Foo","q2x"), header("Foo","q3x")));
		assertObject(h.iterator()).json().is("['Foo: bar','Foo: baz','Foo: qux','Foo: q2x','Foo: q3x']");
		h.add(HeaderSupplier.of(HeaderSupplier.of(header("Foo","q4x"),header("Foo","q5x"))));
		assertObject(h.iterator()).json().is("['Foo: bar','Foo: baz','Foo: qux','Foo: q2x','Foo: q3x','Foo: q4x','Foo: q5x']");
		h.add((Header)null);
		assertObject(h.iterator()).json().is("['Foo: bar','Foo: baz','Foo: qux','Foo: q2x','Foo: q3x','Foo: q4x','Foo: q5x']");
		h.add((HeaderSupplier)null);
		assertObject(h.iterator()).json().is("['Foo: bar','Foo: baz','Foo: qux','Foo: q2x','Foo: q3x','Foo: q4x','Foo: q5x']");
	}

	private static Header header(String name, Object val) {
		return BasicHeader.of(name, val);
	}
}