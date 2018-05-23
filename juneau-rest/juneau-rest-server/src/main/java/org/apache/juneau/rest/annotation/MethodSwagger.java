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
package org.apache.juneau.rest.annotation;

/**
 * Extended annotation for {@link RestMethod#swagger() RestMethod.swagger()}.
 * 
 * <h5 class='section'>See Also:</h5>
 * <ul>
 * 	<li class='link'><a class="doclink" href="../../../../../overview-summary.html#juneau-rest-server.OptionsPages">Overview &gt; juneau-rest-server &gt; OPTIONS Pages</a>
 * </ul>
 */
public @interface MethodSwagger {
	
	/**
	 * Defines the swagger field <code>/paths/{path}/{method}</code>.
	 * 
	 * <p>
	 * Used for free-form Swagger documentation of a REST Java method.
	 * 
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode'>
	 * 	<ja>@RestMethod</ja>(
	 * 		swagger=<ja>@MethodSwagger</ja>(
	 * 			<js>"tags:['pet'],"</js>,
	 * 			<js>"security:[ { petstore_auth:['write:pets','read:pets'] } ]"</js>
	 * 		)
	 * 	)
	 * </p>
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is a JSON object.
	 * 		<br>Multiple lines are concatenated with newlines.
	 * 		<br>Comments and whitespace are ignored.
	 * 		<br>The leading and trailing <js>'{'</js>/<js>'}'</js> characters are optional.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * 	<li>
	 * 		Values defined on this annotation override values defined for the method in the class swagger.
	 * 	<li>
	 * 		
	 * </ul>
	 */
	String[] value() default {};
	
	/**
	 * Defines the swagger field <code>/paths/{path}/{method}/summary</code>.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is plain text.
	 * 		<br>Multiple lines are concatenated with newlines.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * 	<li>
	 * 		Values defined on this annotation override values defined for the method in the class swagger.
	 * 	<li>
	 * 		If not specified, the value is pulled from {@link RestMethod#summary()}.
	 * </ul>
	 */
	String[] summary() default {};
	
	/**
	 * Defines the swagger field <code>/paths/{path}/{method}/description</code>.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is plain text.
	 * 		<br>Multiple lines are concatenated with newlines.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * 	<li>
	 * 		Values defined on this annotation override values defined for the method in the class swagger.
	 * 	<li>
	 * 		If not specified, the value is pulled from {@link RestMethod#description()}.
	 * </ul>
	 */
	String[] description() default {};

	/**
	 * Defines the swagger field <code>/paths/{path}/{method}/operationId</code>.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is plain text.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * 	<li>
	 * 		Values defined on this annotation override values defined for the method in the class swagger.
	 * 	<li>
	 * 		If not specified, the value used is the Java method name.
	 * </ul>
	 */
	String operationId() default "";
	
	/**
	 * Defines the swagger field <code>/paths/{path}/{method}/schemes</code>.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is either a comma-delimited list of simple strings or a JSON array.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * 	<li>
	 * 		Values defined on this annotation override values defined for the method in the class swagger.
	 * </ul>
	 */
	String[] schemes() default {};
	
	/**
	 * Defines the swagger field <code>/paths/{path}/{method}/deprecated</code>.
	 * 
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode'>
	 * 	<ja>@RestMethod</ja>(
	 * 		swagger=<ja>@MethodSwagger</ja>(
	 * 			deprecated=<jk>true</jk>
	 * 		)
	 * 	)
	 * </p>
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is boolean.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * 	<li>
	 * 		Values defined on this annotation override values defined for the method in the class swagger.
	 * 	<li>
	 * 		If not specified, set to <js>"true"</js> if the method is annotated with {@link Deprecated @Deprecated}
	 * </ul>
	 */
	String deprecated() default "";
	
	/**
	 * Defines the swagger field <code>/paths/{path}/{method}/consumes</code>.
	 * 
	 * <p>
	 * Use this value to override the supported <code>Content-Type</code> media types defined by the parsers defined via {@link RestMethod#parsers()}.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is either a comma-delimited list of simple strings or a JSON array.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * 	<li>
	 * 		Values defined on this annotation override values defined for the method in the class swagger.
	 * </ul>
	 */
	String[] consumes() default {};
	
	/**
	 * Defines the swagger field <code>/paths/{path}/{method}/consumes</code>.
	 * 
	 * <p>
	 * Use this value to override the supported <code>CAccept/code> media types defined by the serializers defined via {@link RestMethod#serializers()}.
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is either a comma-delimited list of simple strings or a JSON array.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * 	<li>
	 * 		Values defined on this annotation override values defined for the method in the class swagger.
	 * </ul>
	 */
	String[] produces() default {};

	/**
	 * Defines the swagger field <code>/paths/{path}/{method}/externalDocs</code>.
	 * 
	 * <p>
	 * A simplified JSON string with the following fields:
	 * <p class='bcode'>
	 * 	{
	 * 		description: string,
	 * 		url: string
	 * 	}
	 * </p>
	 * 
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode'>
	 * 	<ja>@RestMethod</ja>(
	 * 		swagger=<ja>@MethodSwagger</ja>(
	 * 			<js>"{url:'http://juneau.apache.org'}"</js>
	 * 		)
	 * 	)
	 * </p>
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		The format is a JSON object.
	 * 		<br>Multiple lines are concatenated with newlines.
	 * 		<br>Comments and whitespace are ignored.
	 * 		<br>The leading and trailing <js>'{'</js>/<js>'}'</js> characters are optional.
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * 	<li>
	 * 		Values defined on this annotation override values defined for the method in the class swagger.
	 * </ul>
	 */
	String[] externalDocs() default {};

	/**
	 * Defines the swagger field <code>/paths/{path}/{method}/parameters</code>.
	 * 
	 * <p>
	 * This annotation is provided for documentation purposes and is used to populate the method <js>"parameters"</js>
	 * column on the Swagger page.
	 * 
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode'>
	 * 	<ja>@RestMethod</ja>(
	 * 		name=<jsf>POST</jsf>, path=<js>"/{a}"</js>,
	 * 		description=<js>"This is my method."</js>,
	 * 		swagger=<ja>@MethodSwagger</ja>(
	 * 			parameters={
	 * 				<ja>@Parameter</ja>(in=<js>"path"</js>, name=<js>"a"</js>, description=<js>"The 'a' attribute"</js>),
	 * 				<ja>@Parameter</ja>(in=<js>"query"</js>, name=<js>"b"</js>, description=<js>"The 'b' parameter"</js>, required=<jk>true</jk>),
	 * 				<ja>@Parameter</ja>(in=<js>"body"</js>, description=<js>"The HTTP content"</js>),
	 * 				<ja>@Parameter</ja>(in=<js>"header"</js>, name=<js>"D"</js>, description=<js>"The 'D' header"</js>),
	 * 			}
	 * 		)
	 * 	)
	 * </p>
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * 	<li>
	 * </ul>
	 */
	String[] parameters() default {};

	/**
	 * Defines the swagger field <code>/paths/{path}/{method}/responses</code>.
	 * 
	 * <p>
	 * This annotation is provided for documentation purposes and is used to populate the method <js>"responses"</js>
	 * column on the Swagger page.
	 * 
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode'>
	 * 	<ja>@RestMethod</ja>(
	 * 		name=<jsf>GET</jsf>, path=<js>"/"</js>,
	 * 		swagger=<ja>@MethodSwagger</ja>(
	 * 			responses={
	 * 				<ja>@Response</ja>(200),
	 * 				<ja>@Response</ja>(
	 * 					value=302,
	 * 					description=<js>"Thing wasn't found here"</js>,
	 * 					headers={
	 * 						<ja>@Parameter</ja>(name=<js>"Location"</js>, description=<js>"The place to find the thing"</js>)
	 * 					}
	 * 				)
	 * 			}
	 * 		)
	 * 	)
	 * </p>
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * </ul>
	 */
	String[] responses() default {};

	/**
	 * Optional tagging information for the exposed API.
	 * 
	 * <p>
	 * Used to populate the Swagger tags field.
	 * 
	 * <p>
	 * A comma-delimited list of tags for API documentation control.
	 * <br>Tags can be used for logical grouping of operations by resources or any other qualifier.
	 * 
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode'>
	 * 	<ja>@RestMethod</ja>(
	 * 		swagger=<ja>@MethodSwagger</ja>(
	 * 			tags=<js>"foo,bar"</js>
	 * 		)
	 * 	)
	 * </p>
	 * 
	 * <h5 class='section'>Notes:</h5>
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		Supports <a class="doclink" href="../../../../../overview-summary.html#DefaultRestSvlVariables">initialization-time and request-time variables</a> 
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * 	<li>
	 * 		Corresponds to the swagger field <code>/paths/{path}/{method}/tags</code>.
	 * </ul>
	 */
	String[] tags() default {};
}
