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
package org.apache.juneau.assertions;

import static org.apache.juneau.assertions.Assertions.*;
import static org.junit.runners.MethodSorters.*;

import java.util.*;

import static java.util.Optional.*;

import org.junit.*;

@FixMethodOrder(NAME_ASCENDING)
public class BooleanAssertion_Test {

	private BooleanAssertion test(Boolean value) {
		return assertBoolean(value).silent();
	}

	private BooleanAssertion test(Optional<Boolean> value) {
		return assertBoolean(value).silent();
	}

	@Test
	public void a01_basic() throws Exception {

		assertThrown(()->test((Boolean)null).exists()).is("Value was null.");
		test(true).exists();
		assertThrown(()->test(empty()).exists()).is("Value was null.");
		test(true).exists();

		test(empty()).doesNotExist();
		assertThrown(()->test(true).doesNotExist()).is("Value was not null.");

		test(empty()).isEqual(null);
		test(true).isEqual(true);
		test(of(true)).isEqual(true);

		test(true).isTrue();
		assertThrown(()->test(true).isFalse()).is("Value was true.");
		test(false).isFalse();
		assertThrown(()->test(false).isTrue()).is("Value was false.");

		assertThrown(()->test(true).isEqual(false)).contains("Unexpected value.");
		test(empty()).isEqual(null);

		test(true).isNot("true");
	}

	@Test
	public void a02_other() throws Exception {
		assertThrown(()->test((Boolean)null).msg("Foo {0}", 1).exists()).is("Foo 1");
		test((Boolean)null).stdout();
	}
}
