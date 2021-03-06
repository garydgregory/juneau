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
package org.apache.juneau.microservice.jetty.resources;

import java.io.*;

import org.apache.juneau.html.annotation.HtmlDocConfig;
import org.apache.juneau.microservice.jetty.*;
import org.apache.juneau.rest.*;
import org.apache.juneau.rest.annotation.*;
import org.apache.juneau.rest.helper.*;

/**
 * Microservice debug utilities.
 */
@Rest(
	path="/debug",
	title="Debug",
	description="Debug Utilities.",
	allowedMethodParams="OPTIONS,POST"
)
@HtmlDocConfig(
	navlinks={
		"up: request:/..",
		"jetty-thread-dump: servlet:/jetty/dump?method=POST",
		"api: servlet:/api",
		"stats: servlet:/stats"
	}
)
@SuppressWarnings("javadoc")
public class DebugResource extends BasicRestServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * [GET /] - Shows child utilities.
	 *
	 * @return Child utility links.
	 * @throws Exception
	 */
	@RestGet(path="/", description="Show contents of config file.")
	public ResourceDescriptions getChildren() throws Exception {
		return new ResourceDescriptions()
			.append("jetty/dump", "Jetty thread dump")
		;
	}

	/**
	 * [GET /jetty/dump] - Generates and retrieves the jetty thread dump.
	 */
	@RestGet(path="/jetty/dump", description="Generates and retrieves the jetty thread dump.")
	public Reader getJettyDump(RestRequest req, RestResponse res) {
		res.setContentType("text/plain");
		return new StringReader(JettyMicroservice.getInstance().getServer().dump());
	}

	/**
	 * [POST /jetty/dump] - Generates and saves the jetty thread dump file to jetty-thread-dump.log.
	 */
	@RestPost(path="/jetty/dump", description="Generates and saves the jetty thread dump file to jetty-thread-dump.log.")
	public String createJettyDump(RestRequest req, RestResponse res) throws Exception {
		String dump = JettyMicroservice.getInstance().getServer().dump();
		try (FileWriter fw = new FileWriter(req.getConfig().getString("Logging/logDir") + "/jetty-thread-dump.log")) {
			fw.write(dump);
		}
		return "OK";
	}
}
