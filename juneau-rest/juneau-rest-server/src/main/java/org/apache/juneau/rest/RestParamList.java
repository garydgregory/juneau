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
package org.apache.juneau.rest;

import org.apache.juneau.collections.*;

/**
 * A list of {@Link RestParam} classes.
 */
public class RestParamList extends AList<Class<? extends RestParam>> {

	private static final long serialVersionUID = 1L;

	/**
	 * Static creator.
	 *
	 * @return An empty list.
	 */
	@SuppressWarnings("unchecked")
	public static RestParamList create() {
		return new RestParamList();
	}

	/**
	 * Returns the contents of this list as a {@link Class} array.
	 *
	 * @return The contents of this list as a {@link Class} array.
	 */
	public Class<? extends RestParam>[] asArray() {
		return asArrayOf(Class.class);
	}
}
