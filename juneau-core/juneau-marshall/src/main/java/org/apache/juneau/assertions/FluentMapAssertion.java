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

import java.io.*;
import java.util.*;

import org.apache.juneau.internal.*;
import org.apache.juneau.marshall.*;

/**
 * Used for fluent assertion calls against maps.
 *
 * @param <R> The return type.
 */
@FluentSetters(returns="FluentMapAssertion<R>")
@SuppressWarnings("rawtypes")
public class FluentMapAssertion<R> extends FluentBaseAssertion<Map,R>  {

	private Map value;

	/**
	 * Constructor.
	 *
	 * @param contents The byte array being tested.
	 * @param returns The object to return after the test.
	 */
	public FluentMapAssertion(Map contents, R returns) {
		this(null, contents, returns);
	}

	/**
	 * Constructor.
	 *
	 * @param creator The assertion that created this assertion.
	 * @param contents The byte array being tested.
	 * @param returns The object to return after the test.
	 */
	public FluentMapAssertion(Assertion creator, Map contents, R returns) {
		super(creator, contents, returns);
		this.value = contents;
	}

	/**
	 * Returns an object assertion on the value specified at the specified key.
	 *
	 * <p>
	 * If the map is <jk>null</jk> or the map doesn't contain the specified key, the returned assertion is a null assertion
	 * (meaning {@link FluentObjectAssertion#exists()} returns <jk>false</jk>).
	 *
	 * @param key The key of the item to retrieve from the map.
	 * @return A new assertion.
	 */
	public FluentObjectAssertion<Object,R> value(String key) {
		return value(Object.class, key);
	}

	/**
	 * Returns an object assertion on the value specified at the specified key.
	 *
	 * <p>
	 * If the map is <jk>null</jk> or the map doesn't contain the specified key, the returned assertion is a null assertion
	 * (meaning {@link FluentObjectAssertion#exists()} returns <jk>false</jk>).
	 *
	 * @param type The value type.
	 * @param key The key of the item to retrieve from the map.
	 * @return A new assertion.
	 */
	public <V> FluentObjectAssertion<Object,R> value(Class<V> type, String key) {
		Object v = getValue(key);
		if (v == null || type.isInstance(v))
			return new FluentObjectAssertion<>(this, v, returns());
		throw error("Map value not of expected type for key ''{0}''.\n\tExpected: {1}.\n\tActual: {2}", key, type, v.getClass());
	}

	private Object getValue(String key) {
		if (value != null)
			return value.get(key);
		return null;
	}

	/**
	 * Asserts that the map exists and is empty.
	 *
	 * @return The object to return after the test.
	 * @throws AssertionError If assertion failed.
	 */
	public R isEmpty() throws AssertionError {
		exists();
		if (! value.isEmpty())
			throw error("Map was not empty.");
		return returns();
	}

	/**
	 * Asserts that the map contains the expected key.
	 *
	 * @param value The value to check for.
	 * @return The object to return after the test.
	 * @throws AssertionError If assertion failed.
	 */
	public R containsKey(String value) throws AssertionError {
		exists();
		if (this.value.containsKey(value))
			return returns();
		throw error("Map did not contain expected key.\n\tContents: {0}\n\tExpected key: {1}", SimpleJson.DEFAULT.toString(this.value), value);
	}

	/**
	 * Asserts that the map contains the expected key.
	 *
	 * @param value The value to check for.
	 * @return The object to return after the test.
	 * @throws AssertionError If assertion failed.
	 */
	public R doesNotContainKey(String value) throws AssertionError {
		exists();
		if (! this.value.containsKey(value))
			return returns();
		throw error("Map contained unexpected key.\n\tContents: {0}\n\tUnexpected key: {1}", SimpleJson.DEFAULT.toString(this.value), value);
	}

	/**
	 * Asserts that the map exists and is not empty.
	 *
	 * @return The object to return after the test.
	 * @throws AssertionError If assertion failed.
	 */
	public R isNotEmpty() throws AssertionError {
		exists();
		if (value.isEmpty())
			throw error("Map was empty.");
		return returns();
	}

	/**
	 * Asserts that the map exists and is the specified size.
	 *
	 * @param size The expected size.
	 * @return The object to return after the test.
	 * @throws AssertionError If assertion failed.
	 */
	public R isSize(int size) throws AssertionError {
		exists();
		if (value.size() != size)
			throw error("Map did not have the expected size.  Expect={0}, Actual={1}.", size, value.size());
		return returns();
	}

	// <FluentSetters>

	@Override /* GENERATED - Assertion */
	public FluentMapAssertion<R> msg(String msg, Object...args) {
		super.msg(msg, args);
		return this;
	}

	@Override /* GENERATED - Assertion */
	public FluentMapAssertion<R> out(PrintStream value) {
		super.out(value);
		return this;
	}

	@Override /* GENERATED - Assertion */
	public FluentMapAssertion<R> silent() {
		super.silent();
		return this;
	}

	@Override /* GENERATED - Assertion */
	public FluentMapAssertion<R> stdout() {
		super.stdout();
		return this;
	}

	@Override /* GENERATED - Assertion */
	public FluentMapAssertion<R> throwable(Class<? extends java.lang.RuntimeException> value) {
		super.throwable(value);
		return this;
	}

	// </FluentSetters>
}
