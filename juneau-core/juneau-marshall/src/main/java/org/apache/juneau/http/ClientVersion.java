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

import org.apache.juneau.http.annotation.*;

/**
 * Represents a parsed <l>X-Client-Version</l> HTTP request header.
 */
@Header("X-Client-Version")
public final class ClientVersion extends BasicStringHeader {

	/**
	 * Returns a parsed <c>X-Client-Version</c> header.
	 *
	 * @param value The <c>X-Client-Version</c> header string.
	 * @return The parsed <c>X-Client-Version</c> header, or <jk>null</jk> if the string was null.
	 */
	public static ClientVersion forString(String value) {
		if (value == null)
			return null;
		return new ClientVersion(value);
	}

	/**
	 * Constructor.
	 *
	 * @param value The value for this header.
	 */
	public ClientVersion(String value) {
		super("X-Client-Version", value);
	}
}
