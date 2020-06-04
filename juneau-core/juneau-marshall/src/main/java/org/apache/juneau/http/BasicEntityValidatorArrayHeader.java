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

import static org.apache.juneau.internal.StringUtils.*;

/**
 * Category of headers that consist of a comma-delimited list of entity validator values.
 *
 * <p>
 * <h5 class='figure'>Example</h5>
 * <p class='bcode w800'>
 * 	If-Match: "xyzzy"
 * 	If-Match: "xyzzy", "r2d2xxxx", "c3piozzzz"
 * 	If-Match: *
 * </p>
 *
 * <ul class='seealso'>
 * 	<li class='extlink'>{@doc RFC2616}
 * </ul>
 */
public class BasicEntityValidatorArrayHeader extends BasicHeader {

	private static final long serialVersionUID = 1L;

	private final EntityValidator[] value;

	/**
	 * Convenience creator.
	 *
	 * @param name The parameter name.
	 * @param value The parameter value.
	 * @return A new {@link BasicEntityValidatorArrayHeader} object.
	 */
	public static BasicEntityValidatorArrayHeader of(String name, String value) {
		return new BasicEntityValidatorArrayHeader(name, value);
	}

	/**
	 * Constructor.
	 *
	 * @param name The HTTP header name.
	 * @param value The raw header value.
	 */
	public BasicEntityValidatorArrayHeader(String name, String value) {
		super(name, value);
		String[] s = value == null ? new String[0] : split(value);
		this.value = new EntityValidator[s.length];
		for (int i = 0; i < s.length; i++) {
			this.value[i] = new EntityValidator(s[i]);
		}
	}

	/**
	 * Returns this header value as an array of {@link EntityValidator} objects.
	 *
	 * @return this header value as an array of {@link EntityValidator} objects.
	 */
	public EntityValidator[] asValidators() {
		return value;
	}
}
