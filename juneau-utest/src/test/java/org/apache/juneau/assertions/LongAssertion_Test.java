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
public class LongAssertion_Test {

	private LongAssertion test(Long value) {
		return assertLong(value).silent();
	}

	private LongAssertion test(Optional<Long> value) {
		return assertLong(value).silent();
	}

	@Test
	public void a01_basic() throws Exception {

		assertThrown(()->test((Long)null).exists()).is("Value was null.");
		test(1l).exists();
		assertThrown(()->test(empty()).exists()).is("Value was null.");
		test(1l).exists();

		test(empty()).doesNotExist();
		assertThrown(()->test(1l).doesNotExist()).is("Value was not null.");

		test(empty()).isEqual(null);
		test(of(1l)).isEqual(1l);
		test(1l).isEqual(1l);

		assertThrown(()->test(1l).isEqual(2)).contains("Unexpected value.");
		test(empty()).isEqual(null);

		assertThrown(()->test(empty()).isGreaterThan(1)).is("Value was null.");
		assertThrown(()->test(1l).isGreaterThan(null)).is("Parameter 'value' cannot be null.");
		test(2l).isGreaterThan(1);
		assertThrown(()->test(1l).isGreaterThan(2l)).is("Value was not greater than expected.\n\tExpect=[2]\n\tActual=[1]");
		assertThrown(()->test(1l).isGreaterThan(1l)).is("Value was not greater than expected.\n\tExpect=[1]\n\tActual=[1]");

		test(2l).asInteger().isGreaterThan(1);
		test(empty()).asInteger().isNull();

		assertThrown(()->test(empty()).isGt(1l)).is("Value was null.");
		assertThrown(()->test(1l).isGt(null)).is("Parameter 'value' cannot be null.");
		test(2l).isGt(1);
		assertThrown(()->test(1l).isGt(2)).is("Value was not greater than expected.\n\tExpect=[2]\n\tActual=[1]");
		assertThrown(()->test(1l).isGt(1)).is("Value was not greater than expected.\n\tExpect=[1]\n\tActual=[1]");

		assertThrown(()->test(empty()).isGreaterThanOrEqual(1)).is("Value was null.");
		assertThrown(()->test(1l).isGreaterThanOrEqual(null)).is("Parameter 'value' cannot be null.");
		test(2l).isGreaterThanOrEqual(1);
		assertThrown(()->test(1l).isGreaterThanOrEqual(2)).is("Value was not greater than or equals to expected.\n\tExpect=[2]\n\tActual=[1]");
		test(1l).isGreaterThanOrEqual(1);

		assertThrown(()->test(empty()).isGte(1)).is("Value was null.");
		assertThrown(()->test(1l).isGte(null)).is("Parameter 'value' cannot be null.");
		test(2l).isGte(1);
		assertThrown(()->test(1l).isGte(2l)).is("Value was not greater than or equals to expected.\n\tExpect=[2]\n\tActual=[1]");
		test(1l).isGte(1l);

		assertThrown(()->test(empty()).isLessThan(1)).is("Value was null.");
		assertThrown(()->test(1l).isLessThan(null)).is("Parameter 'value' cannot be null.");
		test(1l).isLessThan(2l);
		assertThrown(()->test(2l).isLessThan(1)).is("Value was not less than expected.\n\tExpect=[1]\n\tActual=[2]");
		assertThrown(()->test(1l).isLessThan(1)).is("Value was not less than expected.\n\tExpect=[1]\n\tActual=[1]");

		assertThrown(()->test(empty()).isLt(1)).is("Value was null.");
		assertThrown(()->test(1l).isLt(null)).is("Parameter 'value' cannot be null.");
		test(1l).isLt(2);
		assertThrown(()->test(2l).isLt(1)).is("Value was not less than expected.\n\tExpect=[1]\n\tActual=[2]");
		assertThrown(()->test(1l).isLt(1)).is("Value was not less than expected.\n\tExpect=[1]\n\tActual=[1]");

		assertThrown(()->test(empty()).isLessThanOrEqual(1)).is("Value was null.");
		assertThrown(()->test(1l).isLessThanOrEqual(null)).is("Parameter 'value' cannot be null.");
		test(1l).isLessThanOrEqual(2);
		assertThrown(()->test(2l).isLessThanOrEqual(1)).is("Value was not less than or equals to expected.\n\tExpect=[1]\n\tActual=[2]");
		test(1l).isLessThanOrEqual(1);

		assertThrown(()->test(empty()).isLte(1)).is("Value was null.");
		assertThrown(()->test(1l).isLte(null)).is("Parameter 'value' cannot be null.");
		test(1l).isLte(2);
		assertThrown(()->test(2l).isLte(1)).is("Value was not less than or equals to expected.\n\tExpect=[1]\n\tActual=[2]");
		test(1l).isLte(1);

		assertThrown(()->test(empty()).isBetween(1,3)).is("Value was null.");
		assertThrown(()->test(2l).isBetween(null,3)).is("Parameter 'lower' cannot be null.");
		assertThrown(()->test(2l).isBetween(1,null)).is("Parameter 'upper' cannot be null.");
		test(2l).isBetween(1,3);
		test(1l).isBetween(1,3);
		test(3l).isBetween(1,3);
		assertThrown(()->test(2l).isBetween(1,1)).is("Value was not less than or equals to expected.\n\tExpect=[1]\n\tActual=[2]");
		assertThrown(()->test(2l).isBetween(3,3)).is("Value was not greater than or equals to expected.\n\tExpect=[3]\n\tActual=[2]");

		test(2l).isNot("2");
	}

	@Test
	public void a02_other() throws Exception {
		assertThrown(()->test((Long)null).msg("Foo {0}", 1).exists()).is("Foo 1");
		test((Long)null).stdout();
	}
}
