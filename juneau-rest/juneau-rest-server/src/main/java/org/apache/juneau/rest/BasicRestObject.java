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

import java.util.*;

import javax.servlet.http.*;

import org.apache.juneau.dto.swagger.Swagger;
import org.apache.juneau.rest.annotation.*;
import org.apache.juneau.rest.config.*;
import org.apache.juneau.http.annotation.*;
import org.apache.juneau.http.entity.*;
import org.apache.juneau.http.response.*;

/**
 * Identical to {@link BasicRestServlet} but doesn't extend from {@link HttpServlet}.
 *
 * <p>
 * This is particularly useful in Spring Boot environments that auto-detect servlets to deploy in servlet containers,
 * but you want this resource to be deployed as a child instead.
 *
 * <ul class='seealso'>
 * 	<li class='link'>{@doc BasicRest}
 * </ul>
 */
@Rest
public abstract class BasicRestObject extends RestObject implements BasicUniversalRest, BasicRestOperations {

	//-----------------------------------------------------------------------------------------------------------------
	// BasicRestConfig methods
	//-----------------------------------------------------------------------------------------------------------------

	@Override /* BasicRestConfig */
	public Swagger getSwagger(RestRequest req) {
		return req.getSwagger().orElseThrow(NotFound::new);
	}

	@Override /* BasicRestConfig */
	public HttpResource getHtdoc(@Path("/*") String path, Locale locale) throws NotFound {
		return getContext().getStaticFiles().resolve(path, locale).orElseThrow(NotFound::new);
	}

	@Override /* BasicRestConfig */
	public HttpResource getFavIcon() {
		String favIcon = getContext().getConfig().getString("REST/favicon", "images/juneau.png");
		return getHtdoc(favIcon, null);
	}

	@Override /* BasicRestConfig */
	public void error() {}

	@Override /* BasicRestConfig */
	public RestContextStats getStats(RestRequest req) {
		return req.getContext().getStats();
	}
}
