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

import static javax.servlet.http.HttpServletResponse.*;
import static org.apache.juneau.internal.CollectionUtils.*;
import static org.apache.juneau.internal.ObjectUtils.*;
import static org.apache.juneau.internal.IOUtils.*;
import static org.apache.juneau.internal.StringUtils.*;
import static org.apache.juneau.rest.HttpRuntimeException.*;
import static org.apache.juneau.rest.logging.RestLoggingDetail.*;
import static org.apache.juneau.Enablement.*;
import static java.util.Collections.*;
import static java.util.logging.Level.*;
import static java.util.Arrays.*;

import java.io.*;
import java.lang.reflect.*;
import java.lang.reflect.Method;
import java.nio.charset.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.logging.*;
import java.util.stream.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.juneau.*;
import org.apache.juneau.annotation.*;
import org.apache.juneau.collections.*;
import org.apache.juneau.config.*;
import org.apache.juneau.cp.*;
import org.apache.juneau.dto.swagger.*;
import org.apache.juneau.encoders.*;
import org.apache.juneau.html.*;
import org.apache.juneau.html.annotation.*;
import org.apache.juneau.http.*;
import org.apache.juneau.http.annotation.Response;
import org.apache.juneau.httppart.*;
import org.apache.juneau.httppart.bean.*;
import org.apache.juneau.json.*;
import org.apache.juneau.jsonschema.*;
import org.apache.juneau.marshall.*;
import org.apache.juneau.msgpack.*;
import org.apache.juneau.mstat.*;
import org.apache.juneau.oapi.*;
import org.apache.juneau.parser.*;
import org.apache.juneau.plaintext.*;
import org.apache.juneau.reflect.*;
import org.apache.juneau.rest.annotation.*;
import org.apache.juneau.rest.converters.*;
import org.apache.juneau.rest.logging.*;
import org.apache.juneau.rest.params.*;
import org.apache.juneau.http.exception.*;
import org.apache.juneau.http.header.*;
import org.apache.juneau.rest.reshandlers.*;
import org.apache.juneau.rest.util.*;
import org.apache.juneau.rest.vars.*;
import org.apache.juneau.serializer.*;
import org.apache.juneau.soap.*;
import org.apache.juneau.svl.*;
import org.apache.juneau.uon.*;
import org.apache.juneau.urlencoding.*;
import org.apache.juneau.utils.*;
import org.apache.juneau.xml.*;

/**
 * Contains all the configuration on a REST resource and the entry points for handling REST calls.
 *
 * <ul class='seealso'>
 * 	<li class='link'>{@doc RestContext}
 * </ul>
 */
@ConfigurableContext(nocache=true)
public class RestContext extends BeanContext {

	/**
	 * Represents a null value for the {@link Rest#contextClass()} annotation.
	 */
	@SuppressWarnings("javadoc")
	public static final class Null extends RestContext {
		public Null(RestContextBuilder builder) throws Exception {
			super(builder);
		}
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Configurable properties
	//-------------------------------------------------------------------------------------------------------------------

	static final String PREFIX = "RestContext";

	/**
	 * Configuration property:  Allowed header URL parameters.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_allowedHeaderParams REST_allowedHeaderParams}
	 * 	<li><b>Name:</b>  <js>"RestContext.allowedHeaderParams.s"</js>
	 * 	<li><b>Data type:</b>  <c>String</c> (comma-delimited)
	 * 	<li><b>System property:</b>  <c>RestContext.allowedHeaderParams</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_ALLOWHEADERPARAMS</c>
	 * 	<li><b>Default:</b>  <js>"Accept,Content-Type"</js>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#allowedHeaderParams()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#allowedHeaderParams(String)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * When specified, allows headers such as <js>"Accept"</js> and <js>"Content-Type"</js> to be passed in as URL query
	 * parameters.
	 * <br>
	 * For example:
	 * <p class='bcode w800'>
	 *  ?Accept=text/json&amp;Content-Type=text/json
	 * </p>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation.</jc>
	 * 	<ja>@Rest</ja>(allowedHeaderParams=<js>"Accept,Content-Type"</js>)
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder <mv>builder</mv>) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			<mv>builder</mv>.allowedHeaderParams(<js>"Accept,Content-Type"</js>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			<mv>builder</mv>.set(<jsf>REST_allowedHeaderParams</jsf>, <js>"Accept,Content-Type"</js>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder <mv>builder</mv>) <jk>throws</jk> Exception {
	 * 			<mv>builder</mv>.allowedHeaderParams(<js>"Accept,Content-Type"</js>);
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		Useful for debugging REST interface using only a browser so that you can quickly simulate header values
	 * 		in the URL bar.
	 * 	<li>
	 * 		Header names are case-insensitive.
	 * 	<li>
	 * 		Use <js>"*"</js> to allow any headers to be specified as URL parameters.
	 * 	<li>
	 * 		Use <js>"NONE"</js> (case insensitive) to suppress inheriting a value from a parent class.
	 * </ul>
	 */
	public static final String REST_allowedHeaderParams = PREFIX + ".allowedHeaderParams.s";

	/**
	 * Configuration property:  Allowed method headers.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_allowedMethodHeaders REST_allowedMethodHeaders}
	 * 	<li><b>Name:</b>  <js>"RestContext.allowedMethodHeaders.s"</js>
	 * 	<li><b>Data type:</b>  <c>String</c> (comma-delimited)
	 * 	<li><b>System property:</b>  <c>RestContext.allowedMethodHeaders</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_ALLOWEDMETHODHEADERS</c>
	 * 	<li><b>Default:</b>  empty string
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#allowedMethodHeaders()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#allowedMethodHeaders(String)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * A comma-delimited list of HTTP method names that are allowed to be passed as values in an <c>X-Method</c> HTTP header
	 * to override the real HTTP method name.
	 * <p>
	 * Allows you to override the actual HTTP method with a simulated method.
	 * <br>For example, if an HTTP Client API doesn't support <c>PATCH</c> but does support <c>POST</c> (because
	 * <c>PATCH</c> is not part of the original HTTP spec), you can add a <c>X-Method: PATCH</c> header on a normal
	 * <c>HTTP POST /foo</c> request call which will make the HTTP call look like a <c>PATCH</c> request in any of the REST APIs.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation resolving to a config file setting with default value.</jc>
	 * 	<ja>@Rest</ja>(allowedMethodHeaders=<js>"PATCH"</js>)
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder <mv>builder</mv>) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			<mv>builder</mv>.allowedMethodHeaders(<js>"PATCH"</js>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			<mv>builder</mv>.set(<jsf>REST_allowedMethodHeaders</jsf>, <js>"PATCH"</js>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			<mv>builder</mv>.allowedMethodHeaders(<js>"PATCH"</js>);
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		Method names are case-insensitive.
	 * 	<li>
	 * 		Use <js>"*"</js> to represent all methods.
	 * 	<li>
	 * 		Use <js>"NONE"</js> (case insensitive) to suppress inheriting a value from a parent class.
	 * </ul>
	 */
	public static final String REST_allowedMethodHeaders = PREFIX + ".allowedMethodHeaders.s";

	/**
	 * Configuration property:  Allowed method URL parameters.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_allowedMethodParams REST_allowedMethodParams}
	 * 	<li><b>Name:</b>  <js>"RestContext.allowedMethodParams.s"</js>
	 * 	<li><b>Data type:</b>  <c>String</c> (comma-delimited)
	 * 	<li><b>System property:</b>  <c>RestContext.allowedMethodParams</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_ALLOWEDMETHODPARAMS</c>
	 * 	<li><b>Default:</b>  <js>"HEAD,OPTIONS"</js>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#allowedMethodParams()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#allowedMethodParams(String)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * When specified, the HTTP method can be overridden by passing in a <js>"method"</js> (case-insensitive) URL parameter on a regular
	 * GET request.
	 * <br>
	 * For example:
	 * <p class='bcode w800'>
	 *  /myservlet/myendpoint?method=OPTIONS
	 * </p>
	 * <p>
	 * 	Useful in cases where you want to simulate a non-GET request in a browser by simply adding a parameter.
	 * 	<br>Also useful if you want to construct hyperlinks to non-GET REST endpoints such as links to <c>OPTIONS</c>
	 * pages.
	 *
	 * <p>
	 * Note that per the {@doc ExtRFC2616.section9 HTTP specification}, special care should
	 * be taken when allowing non-safe (<c>POST</c>, <c>PUT</c>, <c>DELETE</c>) methods to be invoked through GET requests.
	 *
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation.</jc>
	 * 	<ja>@Rest</ja>(allowedMethodParams=<js>"HEAD,OPTIONS,PUT"</js>)
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder <mv>builder</mv>) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			<mv>builder</mv>.allowedMethodParams(<js>"HEAD,OPTIONS,PUT"</js>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			<mv>builder</mv>.set(<jsf>REST_allowedMethodParams</jsf>, <js>"HEAD,OPTIONS,PUT"</js>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			<mv>builder</mv>.allowedMethodParams(<js>"HEAD,OPTIONS,PUT"</js>);
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		Format is a comma-delimited list of HTTP method names that can be passed in as a method parameter.
	 * 	<li>
	 * 		<js>'method'</js> parameter name is case-insensitive.
	 * 	<li>
	 * 		Use <js>"*"</js> to represent all methods.
	 * 	<li>
	 * 		Use <js>"NONE"</js> (case insensitive) to suppress inheriting a value from a parent class.
	 * </ul>
	 */
	public static final String REST_allowedMethodParams = PREFIX + ".allowedMethodParams.s";

	/**
	 * Configuration property:  Bean factory.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_beanFactory REST_beanFactory}
	 * 	<li><b>Name:</b>  <js>"RestContext.beanFactory.o"</js>
	 * 	<li><b>Data type:</b>
	 * 		<ul>
	 * 			<li>{@link org.apache.juneau.cp.BeanFactory}
	 * 			<li><c>Class&lt;{@link org.apache.juneau.cp.BeanFactory}&gt;</c>
	 * 		</ul>
	 * 	<li><b>Default:</b>  {@link org.apache.juneau.cp.BeanFactory}
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#beanFactory()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#beanFactory(Class)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#beanFactory(BeanFactory)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * The resolver used for resolving instances of child resources and various other beans including:
	 * <ul>
	 * 	<li>{@link RestLogger}
	 * 	<li>{@link SwaggerProvider}
	 * 	<li>{@link FileFinder}
	 * 	<li>{@link StaticFiles}
	 * </ul>
	 *
	 * <p>
	 * Note that the <c>SpringRestServlet</c> classes uses the <c>SpringBeanFactory</c> class to allow for any
	 * Spring beans to be injected into your REST resources.
	 *
	 * <ul class='seealso'>
	 * 	<li class='link'>{@doc RestInjection}
	 * </ul>
	 */
	public static final String REST_beanFactory = PREFIX + ".beanFactory.o";

	/**
	 * Configuration property:  REST call logger.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_callLogger REST_callLogger}
	 * 	<li><b>Name:</b>  <js>"RestContext.callLogger.o"</js>
	 * 	<li><b>Data type:</b>
	 * 		<ul>
	 * 			<li>{@link org.apache.juneau.rest.logging.RestLogger}
	 * 			<li><c>Class&lt;{@link org.apache.juneau.rest.logging.RestLogger}&gt;</c>
	 * 		</ul>
	 * 	<li><b>Default:</b>  {@link #REST_callLoggerDefault}
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#callLogger()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#callLogger(Class)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#callLogger(RestLogger)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Specifies the logger to use for logging of HTTP requests and responses.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Our customized logger.</jc>
	 * 	<jk>public class</jk> MyLogger <jk>extends</jk> BasicRestLogger {
	 *
	 * 		<ja>@Override</ja>
	 * 			<jk>protected void</jk> log(Level <mv>level</mv>, String <mv>msg</mv>, Throwable <mv>e</mv>) {
	 * 			<jc>// Handle logging ourselves.</jc>
	 * 		}
	 * 	}
	 *
	 * 	<jc>// Option #1 - Registered via annotation resolving to a config file setting with default value.</jc>
	 * 	<ja>@Rest</ja>(callLogger=MyLogger.<jk>class</jk>)
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Registered via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder <mv>builder</mv>) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			<mv>builder</mv>.callLogger(MyLogger.<jk>class</jk>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			<mv>builder</mv>.set(<jsf>REST_callLogger</jsf>, MyLogger.<jk>class</jk>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Registered via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder <mv>builder</mv>) <jk>throws</jk> Exception {
	 * 			<mv>builder</mv>.callLogger(MyLogger.<jk>class</jk>);
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		The default call logger if not specified is {@link BasicRestLogger} unless overwritten by {@link #REST_callLoggerDefault}.
	 * 	<li>
	 * 		The resource class itself will be used if it implements the {@link RestLogger} interface and not
	 * 		explicitly overridden via this annotation.
	 * 	<li>
	 * 		When defined as a class, the implementation must have one of the following constructors:
	 * 		<ul>
	 * 			<li><code><jk>public</jk> T(RestContext)</code>
	 * 			<li><code><jk>public</jk> T()</code>
	 * 			<li><code><jk>public static</jk> T <jsm>create</jsm>(RestContext)</code>
	 * 			<li><code><jk>public static</jk> T <jsm>create</jsm>()</code>
	 * 		</ul>
	 * 	<li>
	 * 		Inner classes of the REST resource class are allowed.
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='link'>{@doc RestLoggingAndDebugging}
	 * </ul>
	 */
	public static final String REST_callLogger = PREFIX + ".callLogger.o";

	/**
	 * Configuration property:  Default REST call logger.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_callLoggerDefault REST_callLoggerDefault}
	 * 	<li><b>Name:</b>  <js>"RestContext.callLoggerDefault.o"</js>
	 * 	<li><b>Data type:</b>
	 * 		<ul>
	 * 			<li>{@link org.apache.juneau.rest.logging.RestLogger}
	 * 			<li><c>Class&lt;{@link org.apache.juneau.rest.logging.RestLogger}&gt;</c>
	 * 		</ul>
	 * 	<li><b>Default:</b>  {@link org.apache.juneau.rest.logging.BasicRestLogger}
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#callLoggerDefault(Class)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#callLoggerDefault(RestLogger)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * The default logger to use if one is not specified.
	 * <p>
	 * This setting is inherited from the parent context.
	 *
	 * <ul class='seealso'>
	 * 	<li class='link'>{@doc RestLoggingAndDebugging}
	 * </ul>
	 */
	public static final String REST_callLoggerDefault = PREFIX + ".callLoggerDefault.o";

	/**
	 * Configuration property:  Children.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_children REST_children}
	 * 	<li><b>Name:</b>  <js>"RestContext.children.lo"</js>
	 * 	<li><b>Data type:</b>  <c>List&lt;Class|Object|{@link org.apache.juneau.rest.RestChild}&gt;</c>
	 * 	<li><b>Default:</b>  empty list
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#children()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#child(String,Object)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#children(Class...)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#children(Object...)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Defines children of this resource.
	 *
	 * <p>
	 * A REST child resource is simply another servlet or object that is initialized as part of the ascendant resource and has a
	 * servlet path directly under the ascendant resource object path.
	 * <br>The main advantage to defining servlets as REST children is that you do not need to define them in the
	 * <c>web.xml</c> file of the web application.
	 * <br>This can cut down on the number of entries that show up in the <c>web.xml</c> file if you are defining
	 * large numbers of servlets.
	 *
	 * <p>
	 * Child resources must specify a value for {@link Rest#path() @Rest(path)} that identifies the subpath of the child resource
	 * relative to the ascendant path UNLESS you use the {@link RestContextBuilder#child(String, Object)} method to register it.
	 *
	 * <p>
	 * Child resources can be nested arbitrarily deep using this technique (i.e. children can also have children).
	 *
	 * <dl>
	 * 	<dt>Servlet initialization:</dt>
	 * 	<dd>
	 * 		<p>
	 * 			A child resource will be initialized immediately after the ascendant servlet/resource is initialized.
	 * 			<br>The child resource receives the same servlet config as the ascendant servlet/resource.
	 * 			<br>This allows configuration information such as servlet initialization parameters to filter to child
	 * 			resources.
	 * 		</p>
	 * 	</dd>
	 * 	<dt>Runtime behavior:</dt>
	 * 	<dd>
	 * 		<p>
	 * 			As a rule, methods defined on the <c>HttpServletRequest</c> object will behave as if the child
	 * 			servlet were deployed as a top-level resource under the child's servlet path.
	 * 			<br>For example, the <c>getServletPath()</c> and <c>getPathInfo()</c> methods on the
	 * 			<c>HttpServletRequest</c> object will behave as if the child resource were deployed using the
	 * 			child's servlet path.
	 * 			<br>Therefore, the runtime behavior should be equivalent to deploying the child servlet in the
	 * 			<c>web.xml</c> file of the web application.
	 * 		</p>
	 * 	</dd>
	 * </dl>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Our child resource.</jc>
	 * 	<ja>@Rest</ja>(path=<js>"/child"</js>)
	 * 	<jk>public class</jk> MyChildResource {...}
	 *
	 * 	<jc>// Option #1 - Registered via annotation.</jc>
	 * 	<ja>@Rest</ja>(children={MyChildResource.<jk>class</jk>})
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Registered via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder builder) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			builder.children(MyChildResource.<jk>class</jk>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			builder.addTo(<jsf>REST_children</jsf>, MyChildResource.<jk>class</jk>));
	 *
	 * 			<jc>// Use a pre-instantiated object instead.</jc>
	 * 			builder.child(<js>"/child"</js>, <jk>new</jk> MyChildResource());
	 * 		}
	 *
	 * 		<jc>// Option #3 - Registered via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			builder.children(MyChildResource.<jk>class</jk>);
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		When defined as classes, instances are resolved using the registered {@link #REST_beanFactory} which
	 * 		by default is {@link BeanFactory} which requires the class have one of the following
	 * 		constructors:
	 * 		<ul>
	 * 			<li><code><jk>public</jk> T(RestContextBuilder)</code>
	 * 			<li><code><jk>public</jk> T()</code>
	 * 		</ul>
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='link'>{@doc RestChildren}
	 * </ul>
	 */
	public static final String REST_children = PREFIX + ".children.lo";

	/**
	 * Configuration property:  Client version header.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_clientVersionHeader REST_clientVersionHeader}
	 * 	<li><b>Name:</b>  <js>"RestContext.clientVersionHeader.s"</js>
	 * 	<li><b>Data type:</b>  <c>String</c>
	 * 	<li><b>System property:</b>  <c>RestContext.clientVersionHeader</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_CLIENTVERSIONHEADER</c>
	 * 	<li><b>Default:</b>  <js>"X-Client-Version"</js>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#clientVersionHeader()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#clientVersionHeader(String)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Specifies the name of the header used to denote the client version on HTTP requests.
	 *
	 * <p>
	 * The client version is used to support backwards compatibility for breaking REST interface changes.
	 * <br>Used in conjunction with {@link RestMethod#clientVersion() @RestMethod(clientVersion)} annotation.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation resolving to a config file setting with default value.</jc>
	 * 	<ja>@Rest</ja>(clientVersionHeader=<js>"$C{REST/clientVersionHeader,Client-Version}"</js>)
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder builder) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			builder.clientVersionHeader(<js>"Client-Version"</js>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			builder.set(<jsf>REST_clientVersionHeader</jsf>, <js>"Client-Version"</js>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			builder.clientVersionHeader(<js>"Client-Version"</js>);
	 * 		}
	 * 	}
	 * </p>
	 * <p class='bcode w800'>
	 * 	<jc>// Call this method if Client-Version is at least 2.0.
	 * 	// Note that this also matches 2.0.1.</jc>
	 * 	<ja>@RestMethod</ja>(method=<jsf>GET</jsf>, path=<js>"/foobar"</js>, clientVersion=<js>"2.0"</js>)
	 * 	<jk>public</jk> Object method1() {
	 * 		...
	 * 	}
	 *
	 * 	<jc>// Call this method if Client-Version is at least 1.1, but less than 2.0.</jc>
	 * 	<ja>@RestMethod</ja>(method=<jsf>GET</jsf>, path=<js>"/foobar"</js>, clientVersion=<js>"[1.1,2.0)"</js>)
	 * 	<jk>public</jk> Object method2() {
	 * 		...
	 * 	}
	 *
	 * 	<jc>// Call this method if Client-Version is less than 1.1.</jc>
	 * 	<ja>@RestMethod</ja>(method=<jsf>GET</jsf>, path=<js>"/foobar"</js>, clientVersion=<js>"[0,1.1)"</js>)
	 * 	<jk>public</jk> Object method3() {
	 * 		...
	 * 	}
	 * </p>
	 */
	public static final String REST_clientVersionHeader = PREFIX + ".clientVersionHeader.s";

	/**
	 * Configuration property:  Supported content media types.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_consumes REST_consumes}
	 * 	<li><b>Name:</b>  <js>"RestContext.consumes.ls"</js>
	 * 	<li><b>Data type:</b>  <c>List&lt;String&gt;</c>
	 * 	<li><b>System property:</b>  <c>RestContext.consumes</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_CONSUMES</c>
	 * 	<li><b>Default:</b>  empty list
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#consumes()}
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.RestMethod#consumes()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#consumes(String...)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#consumes(MediaType...)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#consumesReplace(String...)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#consumesReplace(MediaType...)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Overrides the media types inferred from the parsers that identify what media types can be consumed by the resource.
	 * <br>An example where this might be useful if you have parsers registered that handle media types that you
	 * don't want exposed in the Swagger documentation.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation resolving to a config file setting with default value.</jc>
	 * 	<ja>@Rest</ja>(consumes={<js>"$C{REST/supportedConsumes,application/json}"</js>})
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder builder) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			builder.consumes(<jk>false</jk>, <js>"application/json"</js>)
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			builder.set(<jsf>REST_consumes</jsf>, <js>"application/json"</js>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			builder.consumes(<jk>false</jk>, <js>"application/json"</js>);
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <p>
	 * This affects the returned values from the following:
	 * <ul class='javatree'>
	 * 	<li class='jm'>{@link RestContext#getConsumes() RestContext.getConsumes()}
	 * </ul>
	 */
	public static final String REST_consumes = PREFIX + ".consumes.ls";

	/**
	 * Configuration property:  REST context class.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_contextClass REST_contextClass}
	 * 	<li><b>Name:</b>  <js>"RestContext.contextClass.c"</js>
	 * 	<li><b>Data type:</b>  <c>Class&lt;? extends {@link org.apache.juneau.rest.RestContext}&gt;</c>
	 * 	<li><b>Default:</b>  {@link org.apache.juneau.rest.RestContext}
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#contextClass()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#contextClass(Class)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Allows you to extend the {@link RestContext} class to modify how any of the methods are implemented.
	 *
	 * <p>
	 * The subclass must have a public constructor that takes in any of the following arguments:
	 * <ul>
	 * 	<li>{@link RestContextBuilder} - The builder for the object.
	 * 	<li>Any beans found in the specified {@link #REST_beanFactory bean factory}.
	 * 	<li>Any {@link Optional} beans that may or may not be found in the specified {@link #REST_beanFactory bean factory}.
	 * </ul>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Our extended context class that adds a request attribute to all requests.</jc>
	 * 	<jc>// The attribute value is provided by an injected spring bean.</jc>
	 * 	<jk>public</jk> MyRestContext <jk>extends</jk> RestContext {
	 *
	 * 		<jk>private final</jk> Optional&lt;? <jk>extends</jk> Supplier&lt;Object&gt;&gt; <jf>fooSupplier</jf>;
	 *
	 * 		<jc>// Constructor that takes in builder and optional injected attribute provider.</jc>
	 * 		<jk>public</jk> MyRestContext(RestMethodContextBuilder <jv>builder</jv>, Optional&lt;AnInjectedFooSupplier&gt; <jv>fooSupplier</jv>) {
	 * 			<jk>super</jk>(<jv>builder</jv>);
	 * 			<jk>this</jk>.<jf>fooSupplier</jf> = <jv>fooSupplier</jv>.orElseGet(()-><jk>null</jk>);
	 * 		}
	 *
	 * 		<jc>// Override the method used to create default request attributes.</jc>
	 * 		<ja>@Override</ja>
	 * 		<jk>protected</jk> NamedAttributeList createDefaultRequestAttributes(Object <jv>resource</jv>, BeanFactory <jv>beanFactory</jv>) <jk>throws</jk> Exception {
	 * 			<jk>return super</jk>
	 * 				.createDefaultRequestAttributes(<jv>resource</jv>, <jv>beanFactory</jv>)
	 * 				.append(NamedAttribute.<jsm>of</jsm>(<js>"foo"</js>, ()-><jf>fooSupplier</jf>.get());
	 * 		}
	 * 	}
	 * </p>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation.</jc>
	 * 	<ja>@Rest</ja>(contextClass=MyRestContext.<jk>class</jk>)
	 * 	<jk>public class</jk> MyResource {
	 * 		...
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder <jv>builder</jv>) <jk>throws</jk> Exception {
	 * 			<jv>builder</jv>.contextClass(MyRestContext.<jk>class</jk>);
	 * 		}
	 *
	 * 		<ja>@RestMethod</ja>
	 * 		<jk>public</jk> Object getFoo(RequestAttributes <jv>attributes</jv>) {
	 * 			<jk>return</jk> <jv>attributes</jv>.get(<js>"foo"</js>);
	 * 		}
	 * 	}
	 * </p>
	 */
	public static final String REST_contextClass = PREFIX + ".context.c";

	/**
	 * Configuration property:  Class-level response converters.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_converters REST_converters}
	 * 	<li><b>Name:</b>  <js>"RestContext.converters.lo"</js>
	 * 	<li><b>Data type:</b>  <c>List&lt;{@link org.apache.juneau.rest.RestConverter}|Class&lt;{@link org.apache.juneau.rest.RestConverter}&gt;&gt;</c>
	 * 	<li><b>Default:</b>  empty list
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#converters()}
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.RestMethod#converters()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#converters(Class...)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#converters(RestConverter...)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Associates one or more {@link RestConverter converters} with a resource class.
	 * <br>These converters get called immediately after execution of the REST method in the same order specified in the
	 * annotation.
	 * <br>The object passed into this converter is the object returned from the Java method or passed into
	 * the {@link RestResponse#setOutput(Object)} method.
	 *
	 * <p>
	 * Can be used for performing post-processing on the response object before serialization.
	 *
	 * <p>
	 * 	When multiple converters are specified, they're executed in the order they're specified in the annotation
	 * 	(e.g. first the results will be traversed, then the resulting node will be searched/sorted).
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Our converter.</jc>
	 * 	<jk>public class</jk> MyConverter <jk>implements</jk> RestConverter {
	 * 		<ja>@Override</ja>
	 * 		<jk>public</jk> Object convert(RestRequest req, Object o) {
	 * 			<jc>// Do something with object and return another object.</jc>
	 * 			<jc>// Or just return the same object for a no-op.</jc>
	 * 		}
	 * 	}
	 *
	 * 	<jc>// Option #1 - Registered via annotation resolving to a config file setting with default value.</jc>
	 * 	<ja>@Rest</ja>(converters={MyConverter.<jk>class</jk>})
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Registered via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder builder) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			builder.converters(MyConverter.<jk>class</jk>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			builder.set(<jsf>REST_converters</jsf>, MyConverter.<jk>class</jk>);
	 *
	 * 			<jc>// Pass in an instance instead.</jc>
	 * 			builder.converters(<jk>new</jk> MyConverter());
	 * 		}
	 *
	 * 		<jc>// Option #3 - Registered via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			builder.converters(MyConverter.<jk>class</jk>);
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jc'>{@link Traversable} - Allows URL additional path info to address individual elements in a POJO tree.
	 * 	<li class='jc'>{@link Queryable} - Allows query/view/sort functions to be performed on POJOs.
	 * 	<li class='jc'>{@link Introspectable} - Allows Java public methods to be invoked on the returned POJOs.
	 * 	<li class='link'>{@doc RestConverters}
	 * </ul>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		When defined as a class, the implementation must have one of the following constructors:
	 * 		<ul>
	 * 			<li><code><jk>public</jk> T(BeanContext)</code>
	 * 			<li><code><jk>public</jk> T()</code>
	 * 			<li><code><jk>public static</jk> T <jsm>create</jsm>(RestContext)</code>
	 * 			<li><code><jk>public static</jk> T <jsm>create</jsm>()</code>
	 * 		</ul>
	 * 	<li>
	 * 		Inner classes of the REST resource class are allowed.
	 * </ul>
	 */
	public static final String REST_converters = PREFIX + ".converters.lo";

	/**
	 * Configuration property:  Debug mode.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_debug REST_debug}
	 * 	<li><b>Name:</b>  <js>"RestContext.debug.s"</js>
	 * 	<li><b>Data type:</b>  {@link org.apache.juneau.Enablement}
	 * 	<li><b>System property:</b>  <c>RestContext.debug</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_DEBUG</c>
	 * 	<li><b>Default:</b>  {@link #REST_debugDefault}
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#debug()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#debug()}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Enables the following:
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		HTTP request/response bodies are cached in memory for logging purposes.
	 * 	<li>
	 * 		Request/response messages are automatically logged always or per request.
	 * 	<li>
	 * 		The default can be overwritten by {@link #REST_debugDefault}.
	 * </ul>
	 */
	public static final String REST_debug = PREFIX + ".debug.s";

	/**
	 * Configuration property:  Default debug mode.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_debugDefault REST_debugDefault}
	 * 	<li><b>Name:</b>  <js>"RestContext.debug.s"</js>
	 * 	<li><b>Data type:</b>  {@link org.apache.juneau.Enablement}
	 * 	<li><b>System property:</b>  <c>RestContext.debugDefault</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_DEBUGDEFAULT</c>
	 * 	<li><b>Default:</b>  {@link org.apache.juneau.Enablement#NEVER}
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#debugDefault(Enablement)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * The default value for the {@link #REST_debug} setting.
	 * <p>
	 * This setting is inherited from parent contexts.
	 */
	public static final String REST_debugDefault = PREFIX + ".debugDefault.s";

	/**
	 * Configuration property:  Debug mode on specified classes/methods.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_debugOn REST_debugOn}
	 * 	<li><b>Name:</b>  <js>"RestContext.debugOn.s"</js>
	 * 	<li><b>Data type:</b>  <c>String</c> (comma-delimited)
	 * 	<li><b>System property:</b>  <c>RestContext.debugOn</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_DEBUGON</c>
	 * 	<li><b>Default:</b>  Empty string
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#debugOn()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#debugOn(String)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Enables the following:
	 * <ul class='spaced-list'>
	 * 	<li>
	 * 		HTTP request/response bodies are cached in memory for logging purposes.
	 * 	<li>
	 * 		Request/response messages are automatically logged always or per request.
	 * </ul>
	 */
	public static final String REST_debugOn = PREFIX + ".debugOn.s";

	/**
	 * Configuration property:  Default character encoding.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_defaultCharset REST_defaultCharset}
	 * 	<li><b>Name:</b>  <js>"RestContext.defaultCharset.s"</js>
	 * 	<li><b>Data type:</b>  <c>String</c>
	 * 	<li><b>System property:</b>  <c>RestContext.defaultCharset</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_DEFAULTCHARSET</c>
	 * 	<li><b>Default:</b>  <js>"utf-8"</js>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#defaultCharset()}
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.RestMethod#defaultCharset()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#defaultCharset(String)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#defaultCharset(Charset)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * The default character encoding for the request and response if not specified on the request.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation resolving to a config file setting with default value.</jc>
	 * 	<ja>@Rest</ja>(defaultCharset=<js>"$C{REST/defaultCharset,US-ASCII}"</js>)
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder builder) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			builder.defaultCharset(<js>"US-ASCII"</js>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			builder.set(<jsf>REST_defaultCharset</jsf>, <js>"US-ASCII"</js>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			builder.defaultCharset(<js>"US-ASCII"</js>);
	 * 		}
	 *
	 * 		<jc>// Override at the method level.</jc>
	 * 		<ja>@RestMethod</ja>(defaultCharset=<js>"UTF-16"</js>)
	 * 		public Object myMethod() {...}
	 * 	}
	 * </p>
	 */
	public static final String REST_defaultCharset = PREFIX + ".defaultCharset.s";

	/**
	 * Configuration property:  Default request attributes.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_defaultRequestAttributes REST_defaultRequestAttributes}
	 * 	<li><b>Name:</b>  <js>"RestContext.defaultRequestAttributes.lo"</js>
	 * 	<li><b>Data type:</b>  <c>{@link NamedAttribute}[]</c>
	 * 	<li><b>System property:</b>  <c>RestContext.defaultRequestAttributes</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_DEFAULTREQUESTATTRIBUTES</c>
	 * 	<li><b>Default:</b>  empty list
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#defaultRequestAttributes()}
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.RestMethod#defaultRequestAttributes()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#defaultRequestAttribute(String,Object)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#defaultRequestAttribute(String,Supplier)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#defaultRequestAttributes(NamedAttribute...)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Specifies default values for request attributes if they're not already set on the request.
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		Affects values returned by the following methods:
	 * 		<ul>
	 * 			<li class='jm'>{@link RestRequest#getAttribute(String)}.
	 * 			<li class='jm'>{@link RestRequest#getAttributes()}.
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation resolving to a config file setting with default value.</jc>
	 * 	<ja>@Rest</ja>(defaultRequestAttributes={<js>"Foo=bar"</js>, <js>"Baz: $C{REST/myAttributeValue}"</js>})
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder <jv>builder</jv>) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			<jv>builder</jv>
	 * 				.defaultRequestAttributes(
	 * 					BasicNamedAttribute.<jsm>of</jsm>(<js>"Foo"</js>, <js>"bar"</js>),
	 * 					BasicNamedAttribute.<jsm>of</jsm>(<js>"Baz"</js>, <jk>true</jk>)
	 * 				);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			<jv>builder</jv>.appendTo(<jsf>REST_defaultRequestAttributes</jsf>, BasicNamedAttribute.<jsm>of</jsm>(<js>"Foo"</js>, <js>"bar"</js>));
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder <jv>builder</jv>) <jk>throws</jk> Exception {
	 * 			<jv>builder</jv>.defaultRequestAttribute(<js>"Foo"</js>, <js>"bar"</js>);
	 * 		}
	 *
	 * 		<jc>// Override at the method level.</jc>
	 * 		<ja>@RestMethod</ja>(defaultRequestAttributes={<js>"Foo: bar"</js>})
	 * 		<jk>public</jk> Object myMethod() {...}
	 * 	}
	 * </p>
	 */
	public static final String REST_defaultRequestAttributes = PREFIX + ".defaultRequestAttributes.lo";

	/**
	 * Configuration property:  Default request headers.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_defaultRequestHeaders REST_defaultRequestHeaders}
	 * 	<li><b>Name:</b>  <js>"RestContext.defaultRequestHeaders.lo"</js>
	 * 	<li><b>Data type:</b>  <c>{@link org.apache.http.Header}[]</c>
	 * 	<li><b>System property:</b>  <c>RestContext.defaultRequestHeaders</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_DEFAULTREQUESTHEADERS</c>
	 * 	<li><b>Default:</b>  empty list
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#defaultRequestHeaders()}
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.RestMethod#defaultRequestHeaders()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#defaultRequestHeader(String,Object)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#defaultRequestHeader(String,Supplier)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#defaultRequestHeaders(org.apache.http.Header...)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Specifies default values for request headers if they're not passed in through the request.
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		Affects values returned by {@link RestRequest#getHeader(String)} when the header is not present on the request.
	 * 	<li>
	 * 		The most useful reason for this annotation is to provide a default <c>Accept</c> header when one is not
	 * 		specified so that a particular default {@link Serializer} is picked.
	 * </ul>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation resolving to a config file setting with default value.</jc>
	 * 	<ja>@Rest</ja>(defaultRequestHeaders={<js>"Accept: application/json"</js>, <js>"My-Header=$C{REST/myHeaderValue}"</js>})
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder <jv>builder</jv>) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			<jv>builder</jv>
	 * 				.defaultRequestHeaders(
	 * 					Accept.<jsm>of</jsm>(<js>"application/json"</js>),
	 * 					BasicHeader.<jsm>of</jsm>(<js>"My-Header"</js>, <js>"foo"</js>)
	 * 				);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			<jv>builder</jv>.appendTo(<jsf>REST_defaultRequestHeaders</jsf>, Accept.<jsm>of</jsm>(<js>"application/json"</js>));
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder <jv>builder</jv>) <jk>throws</jk> Exception {
	 * 			<jv>builder</jv>.defaultRequestHeaders(Accept.<jsm>of</jsm>(<js>"application/json"</js>));
	 * 		}
	 *
	 * 		<jc>// Override at the method level.</jc>
	 * 		<ja>@RestMethod</ja>(defaultRequestHeaders={<js>"Accept: text/xml"</js>})
	 * 		<jk>public</jk> Object myMethod() {...}
	 * 	}
	 * </p>
	 */
	public static final String REST_defaultRequestHeaders = PREFIX + ".defaultRequestHeaders.lo";

	/**
	 * Configuration property:  Default response headers.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_defaultResponseHeaders REST_defaultResponseHeaders}
	 * 	<li><b>Name:</b>  <js>"RestContext.defaultResponseHeaders.lo"</js>
	 * 	<li><b>Data type:</b>  <c>{@link org.apache.http.Header}[]</c>
	 * 	<li><b>System property:</b>  <c>RestContext.defaultResponseHeaders</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_DEFAULTRESPONSEHEADERS</c>
	 * 	<li><b>Default:</b>  empty list
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#defaultResponseHeaders()}
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.RestMethod#defaultResponseHeaders()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#defaultResponseHeader(String,Object)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#defaultResponseHeader(String,Supplier)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#defaultResponseHeaders(org.apache.http.Header...)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Specifies default values for response headers if they're not set after the Java REST method is called.
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		This is equivalent to calling {@link RestResponse#setHeader(String, String)} programmatically in each of
	 * 		the Java methods.
	 * 	<li>
	 * 		The header value will not be set if the header value has already been specified (hence the 'default' in the name).
	 * </ul>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation resolving to a config file setting with default value.</jc>
	 * 	<ja>@Rest</ja>(defaultResponseHeaders={<js>"Content-Type: $C{REST/defaultContentType,text/plain}"</js>,<js>"My-Header: $C{REST/myHeaderValue}"</js>})
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder <jv>builder</jv>) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			<jv>builder</jv>
	 * 				.defaultResponseHeaders(
	 * 					ContentType.<jsm>of</jsm>(<js>"text/plain"</js>),
	 * 					BasicHeader.<jsm>ofPair</jsm>(<js>"My-Header: foo"</js>)
	 * 				);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			<jv>builder</jv>
	 * 				.appendTo(<jsf>REST_resHeaders</jsf>, ContentType.<jsm>of</jsm>(<js>"text/plain"</js>))
	 * 				.appendTo(<jsf>REST_resHeaders</jsf>, BasicHeader.<jsm>of</jsm>(<js>"My-Header"</js>, <js>"foo"</js>));
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder <jv>builder</jv>) <jk>throws</jk> Exception {
	 * 			<jv>builder</jv>.defaultResponseHeaders(ContentType.<jsm>of</jsm>(<js>"text/plain"</js>));
	 * 		}
	 * 	}
	 * </p>
	 */
	public static final String REST_defaultResponseHeaders = PREFIX + ".defaultResponseHeaders.lo";

	/**
	 * Configuration property:  Disable allow body URL parameter.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_disableAllowBodyParam REST_disableAllowBodyParam}
	 * 	<li><b>Name:</b>  <js>"RestContext.disableAllowBodyParam.b"</js>
	 * 	<li><b>Data type:</b>  <jk>boolean</jk>
	 * 	<li><b>System property:</b>  <c>RestContext.disableAllowBodyParam</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_DISABLEALLOWBODYPARAM</c>
	 * 	<li><b>Default:</b>  <jk>false</jk>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#disableAllowBodyParam()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#disableAllowBodyParam()}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * When enabled, the HTTP body content on PUT and POST requests can be passed in as text using the <js>"body"</js>
	 * URL parameter.
	 * <br>
	 * For example:
	 * <p class='bcode w800'>
	 *  ?body=(name='John%20Smith',age=45)
	 * </p>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation resolving to a config file setting with default value.</jc>
	 * 	<ja>@Rest</ja>(disableAllowBodyParam=<js>"$C{REST/disableAllowBodyParam,true}"</js>)
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder builder) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			builder.disableAllowBodyParam();
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			builder.set(<jsf>REST_disableAllowBodyParam</jsf>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			builder.disableAllowBodyParam();
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		<js>'body'</js> parameter name is case-insensitive.
	 * 	<li>
	 * 		Useful for debugging PUT and POST methods using only a browser.
	 * </ul>
	 */
	public static final String REST_disableAllowBodyParam = PREFIX + ".disableAllowBodyParam.b";

	/**
	 * Configuration property:  Compression encoders.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_encoders REST_encoders}
	 * 	<li><b>Name:</b>  <js>"RestContext.encoders.o"</js>
	 * 	<li><b>Data type:</b>  <c>List&lt;{@link org.apache.juneau.encoders.Encoder}|Class&lt;{@link org.apache.juneau.encoders.Encoder}&gt;&gt;</c>
	 * 	<li><b>Default:</b>  empty list
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#encoders()}
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.RestMethod#encoders()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#encoders(Class...)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#encoders(Encoder...)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * These can be used to enable various kinds of compression (e.g. <js>"gzip"</js>) on requests and responses.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Registered via annotation.</jc>
	 * 	<ja>@Rest</ja>(encoders={GzipEncoder.<jk>class</jk>})
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Registered via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder builder) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			builder.encoders(GzipEncoder.<jk>class</jk>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			builder.addTo(<jsf>REST_encoders</jsf>, GzipEncoder.<jk>class</jk>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Registered via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			builder.encoders(GzipEncoder.<jk>class</jk>);
	 * 		}
	 *
	 * 		<jc>// Override at the method level.</jc>
	 * 		<ja>@RestMethod</ja>(encoders={MySpecialEncoder.<jk>class</jk>}, inherit={<js>"ENCODERS"</js>})
	 * 		public Object myMethod() {...}
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		When defined as a class, the implementation must have one of the following constructors:
	 * 		<ul>
	 * 			<li><code><jk>public</jk> T(BeanContext)</code>
	 * 			<li><code><jk>public</jk> T()</code>
	 * 			<li><code><jk>public static</jk> T <jsm>create</jsm>(RestContext)</code>
	 * 			<li><code><jk>public static</jk> T <jsm>create</jsm>()</code>
	 * 		</ul>
	 * 	<li>
	 * 		Inner classes of the REST resource class are allowed.
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='link'>{@doc RestEncoders}
	 * </ul>
	 */
	public static final String REST_encoders = PREFIX + ".encoders.lo";

	/**
	 * Configuration property:  File finder.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_fileFinder REST_fileFinder}
	 * 	<li><b>Name:</b>  <js>"RestContext.fileFinder.o"</js>
	 * 	<li><b>Data type:</b>  {@link org.apache.juneau.cp.FileFinder}
	 * 	<li><b>Default:</b>  {@link #REST_fileFinderDefault}
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#fileFinder()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#fileFinder(Class)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#fileFinder(FileFinder)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContext#createFileFinder(Object,BeanFactory)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Used to retrieve localized files from the classpath for a variety of purposes including:
	 * <ul>
	 * 	<li>Resolution of {@link FileVar $F} variable contents.
	 * </ul>
	 *
	 * <p>
	 * The file finder can be accessed through the following methods:
	 * <ul class='javatree'>
	 * 	<li class='jm'>{@link RestContext#getFileFinder()}
	 * 	<li class='jm'>{@link RestRequest#getFileFinder()}
	 * </ul>
	 *
	 * <p>
	 * The file finder is instantiated via the {@link RestContext#createFileFinder(Object,BeanFactory)} method which in turn instantiates
	 * based on the following logic:
	 * <ul>
	 * 	<li>Returns the resource class itself if it's an instance of {@link FileFinder}.
	 * 	<li>Looks for {@link #REST_fileFinder} setting.
	 * 	<li>Looks for a public <c>createFileFinder()</> method on the resource class with an optional {@link RestContext} argument.
	 * 	<li>Instantiates the default file finder as specified via {@link #REST_fileFinderDefault}.
	 * 	<li>Instantiates a {@link BasicFileFinder} which provides basic support for finding localized
	 * 		resources on the classpath and JVM working directory.
	 * </ul>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Create a file finder that looks for files in the /files working subdirectory, but overrides the find()
	 * 	// method for special handling of special cases.</jc>
	 * 	<jk>public class</jk> MyFileFinder <jk>extends</jk> FileFinder {
	 *
	 * 		<jk>public</jk> MyFileFinder() {
	 * 			<jk>super</jk>(
	 * 				<jk>new</jk> FileFinderBuilder()
	 * 					.dir(<js>"/files"</js>)
	 *			);
	 * 		}
	 *
	 *		<ja>@Override</ja> <jc>// FileFinder</jc>
	 * 		<jk>protected</jk> Optional&lt;InputStream&gt; find(String <jv>name</jv>, Locale <jv>locale</jv>) <jk>throws</jk> IOException {
	 * 			<jc>// Do special handling or just call super.find().</jc>
	 * 			<jk>return super</jk>.find(<jv>name</jv>, <jv>locale</jv>);
	 * 		}
	 * 	}
	 * </p>
	 *
	 * 	<jc>// Option #1 - Registered via annotation.</jc>
	 * 	<ja>@Rest</ja>(fileFinder=MyFileFinder.<jk>class</jk>)
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Created via createFileFinder() method.</jc>
	 * 		<jk>public</jk> FileFinder createFileFinder(RestContext <jv>context</jv>) <jk>throws</jk> Exception {
	 * 			<jk>return new</jk> MyFileFinder();
	 * 		}
	 *
	 * 		<jc>// Option #3 - Registered via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder <jv>builder</jv>) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			<jv>builder</jv>.fileFinder(MyFileFinder.<jk>class</jk>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			<jv>builder</jv>.set(<jsf>REST_fileFinder</jsf>, MyFileFinder.<jk>class</jk>));
	 *
	 * 			<jc>// Use a pre-instantiated object instead.</jc>
	 * 			<jv>builder</jv>.fileFinder(<jk>new</jk> MyFileFinder());
	 * 		}
	 *
	 * 		<jc>// Option #4 - Registered via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder <jv>builder</jv>) <jk>throws</jk> Exception {
	 * 			<jv>builder</jv>.fileFinder(MyFileFinder.<jk>class</jk>);
	 * 		}
	 *
	 * 		<jc>// Create a REST method that uses the file finder.</jc>
	 * 		<ja>@RestMethod</ja>
	 * 		<jk>public</jk> InputStream getFoo(RestRequest <jv>req</jv>) {
	 * 			<jk>return</jk> <jv>req</jv>.getFileFinder().getStream(<js>"foo.json"</js>).orElseThrow(NotFound::<jk>new</jk>);
	 * 		}
	 * 	}
	 * </p>
	 */
	public static final String REST_fileFinder = PREFIX + ".fileFinder.o";

	/**
	 * Configuration property:  Default file finder.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_fileFinderDefault REST_fileFinderDefault}
	 * 	<li><b>Name:</b>  <js>"RestContext.fileFinderDefault.o"</js>
	 * 	<li><b>Data type:</b>  {@link org.apache.juneau.cp.FileFinder}
	 * 	<li><b>Default:</b>  {@link org.apache.juneau.rest.BasicFileFinder}
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#fileFinderDefault(Class)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#fileFinderDefault(FileFinder)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * The default file finder to use if not specified.
	 * <p>
	 * This setting is inherited from the parent context.
	 */
	public static final String REST_fileFinderDefault = PREFIX + ".fileFinderDefault.o";

	/**
	 * Configuration property:  Class-level guards.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_guards REST_guards}
	 * 	<li><b>Name:</b>  <js>"RestContext.guards.lo"</js>
	 * 	<li><b>Data type:</b>  <c>List&lt;{@link org.apache.juneau.rest.RestGuard}|Class&lt;{@link org.apache.juneau.rest.RestGuard}&gt;&gt;</c>
	 * 	<li><b>Default:</b>  empty list
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#guards()}
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.RestMethod#guards()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#guards(Class...)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#guards(RestGuard...)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Associates one or more {@link RestGuard RestGuards} with all REST methods defined in this class.
	 * <br>These guards get called immediately before execution of any REST method in this class.
	 *
	 * <p>
	 * If multiple guards are specified, <b>ALL</b> guards must pass.
	 * <br>Note that this is different than matchers where only ONE matcher needs to pass.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Define a guard that only lets Billy make a request.</jc>
	 * 	<jk>public</jk> BillyGuard <jk>extends</jk> RestGuard {
	 * 		<ja>@Override</ja>
	 * 		<jk>public boolean</jk> isRequestAllowed(RestRequest req) {
	 * 			<jk>return</jk> req.getUserPrincipal().getName().equals(<js>"Billy"</js>);
	 * 		}
	 * 	}
	 *
	 * 	<jc>// Option #1 - Registered via annotation.</jc>
	 * 	<ja>@Rest</ja>(guards={BillyGuard.<jk>class</jk>})
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Registered via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder builder) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			builder.guards(BillyGuard.<jk>class</jk>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			builder.addTo(<jsf>REST_guards</jsf>, BillyGuard.<jk>class</jk>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Registered via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			builder.guards(BillyGuard.<jk>class</jk>);
	 * 		}
	 *
	 * 		<jc>// Override at the method level.</jc>
	 * 		<ja>@RestMethod</ja>(guards={SomeOtherGuard.<jk>class</jk>})
	 * 		public Object myMethod() {...}
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		When defined as a class, the implementation must have one of the following constructors:
	 * 		<ul>
	 * 			<li><code><jk>public</jk> T(RestContext)</code>
	 * 			<li><code><jk>public</jk> T()</code>
	 * 			<li><code><jk>public static</jk> T <jsm>create</jsm>(RestContext)</code>
	 * 			<li><code><jk>public static</jk> T <jsm>create</jsm>()</code>
	 * 		</ul>
	 * 	<li>
	 * 		Inner classes of the REST resource class are allowed.
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='link'>{@doc RestGuards}
	 * </ul>
	 */
	public static final String REST_guards = PREFIX + ".guards.lo";

	/**
	 * Configuration property:  The maximum allowed input size (in bytes) on HTTP requests.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_maxInput REST_maxInput}
	 * 	<li><b>Name:</b>  <js>"RestContext.maxInput.s"</js>
	 * 	<li><b>Data type:</b>  <c>String</c>
	 * 	<li><b>System property:</b>  <c>RestContext.maxInput</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_MAXINPUT</c>
	 * 	<li><b>Default:</b>  <js>"100M"</js>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#maxInput()}
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.RestMethod#maxInput()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#maxInput(String)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Useful for alleviating DoS attacks by throwing an exception when too much input is received instead of resulting
	 * in out-of-memory errors which could affect system stability.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation resolving to a config file setting with default value.</jc>
	 * 	<ja>@Rest</ja>(maxInput=<js>"$C{REST/maxInput,10M}"</js>)
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder builder) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			builder.maxInput(<js>"10M"</js>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			builder.set(<jsf>REST_maxInput</jsf>, <js>"10M"</js>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			builder.maxInput(<js>"10M"</js>);
	 * 		}
	 *
	 * 		<jc>// Override at the method level.</jc>
	 * 		<ja>@RestMethod</ja>(maxInput=<js>"10M"</js>)
	 * 		public Object myMethod() {...}
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		String value that gets resolved to a <jk>long</jk>.
	 * 	<li>
	 * 		Can be suffixed with any of the following representing kilobytes, megabytes, and gigabytes:
	 * 		<js>'K'</js>, <js>'M'</js>, <js>'G'</js>.
	 * 	<li>
	 * 		A value of <js>"-1"</js> can be used to represent no limit.
	 * </ul>
	 */
	public static final String REST_maxInput = PREFIX + ".maxInput.s";

	/**
	 * Configuration property:  Messages.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_messages REST_messages}
	 * 	<li><b>Name:</b>  <js>"RestContext.messages.lo"</js>
	 * 	<li><b>Data type:</b>  <c>List&lt;{@link org.apache.juneau.utils.Tuple2}&lt;Class,String&gt;&gt;</c>
	 * 	<li><b>Default:</b>  <jk>null</jk>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#messages()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#messages(String)},
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Identifies the location of the resource bundle for this class if it's different from the class name.
	 *
	 * <p>
	 * By default, the resource bundle name is assumed to match the class name.  For example, given the class
	 * <c>MyClass.java</c>, the resource bundle is assumed to be <c>MyClass.properties</c>.  This property
	 * allows you to override this setting to specify a different location such as <c>MyMessages.properties</c> by
	 * specifying a value of <js>"MyMessages"</js>.
	 *
	 * <p>
	 * 	Resource bundles are searched using the following base name patterns:
	 * 	<ul>
	 * 		<li><js>"{package}.{name}"</js>
	 * 		<li><js>"{package}.i18n.{name}"</js>
	 * 		<li><js>"{package}.nls.{name}"</js>
	 * 		<li><js>"{package}.messages.{name}"</js>
	 * 	</ul>
	 *
	 * <p>
	 * This annotation is used to provide request-localized (based on <c>Accept-Language</c>) messages for the following methods:
	 * <ul class='javatree'>
	 * 	<li class='jm'>{@link RestRequest#getMessage(String, Object...)}
	 * 	<li class='jm'>{@link RestContext#getMessages() RestContext.getMessages()}
	 * </ul>
	 *
	 * <p>
	 * Request-localized messages are also available by passing either of the following parameter types into your Java method:
	 * <ul class='javatree'>
	 * 	<li class='jc'>{@link ResourceBundle} - Basic Java resource bundle.
	 * 	<li class='jc'>{@link Messages} - Extended resource bundle with several convenience methods.
	 * </ul>
	 *
	 * The value can be a relative path like <js>"nls/Messages"</js>, indicating to look for the resource bundle
	 * <js>"com.foo.sample.nls.Messages"</js> if the resource class is in <js>"com.foo.sample"</js>, or it can be an
	 * absolute path like <js>"com.foo.sample.nls.Messages"</js>
	 *
	 * <h5 class='section'>Examples:</h5>
	 * <p class='bcode w800'>
	 * 	<cc># Contents of org/apache/foo/nls/MyMessages.properties</cc>
	 *
	 * 	<ck>HelloMessage</ck> = <cv>Hello {0}!</cv>
	 * </p>
	 * <p class='bcode w800'>
	 * 	<jc>// Contents of org/apache/foo/MyResource.java</jc>
	 *
	 * 	<ja>@Rest</ja>(messages=<js>"nls/MyMessages"</js>)
	 * 	<jk>public class</jk> MyResource {...}
	 *
	 * 		<ja>@RestMethod</ja>(method=<js>"GET"</js>, path=<js>"/hello/{you}"</js>)
	 * 		<jk>public</jk> Object helloYou(RestRequest <jv>req</jv>, Messages <jv>messages</jv>, <ja>@Path</ja>(<js>"name"</js>) String <jv>you</jv>) {
	 * 			String <jv>s</jv>;
	 *
	 * 			<jc>// Get it from the RestRequest object.</jc>
	 * 			<jv>s</jv> = <jv>req</jv>.getMessage(<js>"HelloMessage"</js>, <jv>you</jv>);
	 *
	 * 			<jc>// Or get it from the method parameter.</jc>
	 * 			<jv>s</jv> = <jv>messages</jv>.getString(<js>"HelloMessage"</js>, <jv>you</jv>);
	 *
	 * 			<jc>// Or get the message in a locale different from the request.</jc>
	 * 			<jv>s</jv> = <jv>messages</jv>.forLocale(Locale.<jsf>UK</jsf>).getString(<js>"HelloMessage"</js>, <jv>you</jv>);
	 *
	 * 			<jk>return</jk> <jv>s</jv>;
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>Mappings are cumulative from super classes.
	 * 		<br>Therefore, you can find and retrieve messages up the class-hierarchy chain.
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jc'>{@link Messages}
	 * 	<li class='link'>{@doc RestMessages}
	 * </ul>
	 */
	public static final String REST_messages = PREFIX + ".messages.lo";

	/**
	 * Configuration property:  REST method context class.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_methodContextClass REST_methodContextClass}
	 * 	<li><b>Name:</b>  <js>"RestContext.methodContextClass.c"</js>
	 * 	<li><b>Data type:</b>  <c>Class&lt;? extends {@link org.apache.juneau.rest.RestMethodContext}&gt;</c>
	 * 	<li><b>Default:</b>  {@link org.apache.juneau.rest.RestMethodContext}
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#methodContextClass()}
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.RestMethod#contextClass()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#methodContextClass(Class)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestMethodContextBuilder#contextClass(Class)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Allows you to extend the {@link RestMethodContext} class to modify how any of the methods are implemented.
	 *
	 * <p>
	 * The subclass must have a public constructor that takes in any of the following arguments:
	 * <ul>
	 * 	<li>{@link RestMethodContextBuilder} - The builder for the object.
	 * 	<li>Any beans found in the specified {@link #REST_beanFactory bean factory}.
	 * 	<li>Any {@link Optional} beans that may or may not be found in the specified {@link #REST_beanFactory bean factory}.
	 * </ul>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Our extended context class that adds a request attribute to all requests.</jc>
	 * 	<jc>// The attribute value is provided by an injected spring bean.</jc>
	 * 	<jk>public</jk> MyRestMethodContext <jk>extends</jk> RestMethodContext {
	 *
	 * 		<jk>private final</jk> Optional&lt;? <jk>extends</jk> Supplier&lt;Object&gt;&gt; <jf>fooSupplier</jf>;
	 *
	 * 		<jc>// Constructor that takes in builder and optional injected attribute provider.</jc>
	 * 		<jk>public</jk> MyRestMethodContext(RestMethodContextBuilder <jv>builder</jv>, Optional&lt;AnInjectedFooSupplier&gt; <jv>fooSupplier</jv>) {
	 * 			<jk>super</jk>(<jv>builder</jv>);
	 * 			<jk>this</jk>.<jf>fooSupplier</jf> = <jv>fooSupplier</jv>.orElseGet(()-><jk>null</jk>);
	 * 		}
	 *
	 * 		<jc>// Override the method used to create default request attributes.</jc>
	 * 		<ja>@Override</ja>
	 * 		<jk>protected</jk> NamedAttributeList createDefaultRequestAttributes(Object <jv>resource</jv>, BeanFactory <jv>beanFactory</jv>, Method <jv>method</jv>, RestContext <jv>context</jv>) <jk>throws</jk> Exception {
	 * 			<jk>return super</jk>
	 * 				.createDefaultRequestAttributes(<jv>resource</jv>, <jv>beanFactory</jv>, <jv>method</jv>, <jv>context</jv>)
	 * 				.append(NamedAttribute.<jsm>of</jsm>(<js>"foo"</js>, ()-><jf>fooSupplier</jf>.get());
	 * 		}
	 * 	}
	 * </p>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation.</jc>
	 * 	<ja>@Rest</ja>(methodContextClass=MyRestMethodContext.<jk>class</jk>)
	 * 	<jk>public class</jk> MyResource {
	 * 		...
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder <jv>builder</jv>) <jk>throws</jk> Exception {
	 * 			<jv>builder</jv>.methodContextClass(MyRestMethodContext.<jk>class</jk>);
	 * 		}
	 *
	 * 		<ja>@RestMethod</ja>
	 * 		<jk>public</jk> Object getFoo(RequestAttributes <jv>attributes</jv>) {
	 * 			<jk>return</jk> <jv>attributes</jv>.get(<js>"foo"</js>);
	 * 		}
	 * 	}
	 * </p>
	 */
	public static final String REST_methodContextClass = PREFIX + ".methodContextClass.c";

	/**
	 * Configuration property:  Parsers.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_parsers REST_parsers}
	 * 	<li><b>Name:</b>  <js>"RestContext.parsers.lo"</js>
	 * 	<li><b>Data type:</b>  <c>List&lt;{@link org.apache.juneau.parser.Parser}|Class&lt;{@link org.apache.juneau.parser.Parser}&gt;&gt;</c>
	 * 	<li><b>Default:</b>  empty list
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#parsers()}
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.RestMethod#parsers()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#parsers(Object...)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#parsers(Class...)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#parsersReplace(Object...)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Adds class-level parsers to this resource.
	 *
	 * <p>
	 * Parsers are used to convert the body of HTTP requests into POJOs.
	 * <br>Any of the Juneau framework parsers can be used in this setting.
	 * <br>The parser selected is based on the request <c>Content-Type</c> header matched against the values returned by the following method
	 * using a best-match algorithm:
	 * <ul class='javatree'>
	 * 	<li class='jm'>{@link Parser#getMediaTypes()}
	 * </ul>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation.</jc>
	 * 	<ja>@Rest</ja>(parsers={JsonParser.<jk>class</jk>, XmlParser.<jk>class</jk>})
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder builder) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			builder.parsers(JsonParser.<jk>class</jk>, XmlParser.<jk>class</jk>);
	 *
	 * 			<jc>// Same, but use pre-instantiated parsers.</jc>
	 * 			builder.parsers(JsonParser.<jsf>DEFAULT</jsf>, XmlParser.<jsf>DEFAULT</jsf>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			builder.set(<jsf>REST_parsers</jsf>, JsonParser.<jk>class</jk>, XmlParser.<jk>class</jk>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			builder.parsers(JsonParser.<jk>class</jk>, XmlParser.<jk>class</jk>);
	 * 		}
	 *
	 * 		<jc>// Override at the method level.</jc>
	 * 		<ja>@RestMethod</ja>(parsers={HtmlParser.<jk>class</jk>})
	 * 		<jk>public</jk> Object myMethod(<ja>@Body</ja> MyPojo myPojo) {
	 * 			<jc>// Do something with your parsed POJO.</jc>
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		When defined as a class, properties/transforms defined on the resource/method are inherited.
	 * 	<li>
	 * 		When defined as an instance, properties/transforms defined on the resource/method are NOT inherited.
	 * 	<li>
	 * 		Typically, you'll want your resource to extend directly from {@link BasicRestServlet} which comes
	 * 		preconfigured with the following parsers:
	 * 		<ul>
	 * 			<li class='jc'>{@link JsonParser}
	 * 			<li class='jc'>{@link XmlParser}
	 * 			<li class='jc'>{@link HtmlParser}
	 * 			<li class='jc'>{@link UonParser}
	 * 			<li class='jc'>{@link UrlEncodingParser}
	 * 			<li class='jc'>{@link MsgPackParser}
	 * 			<li class='jc'>{@link PlainTextParser}
	 * 		</ul>
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='link'>{@doc RestParsers}
	 * </ul>
	 */
	public static final String REST_parsers = PREFIX + ".parsers.lo";

	/**
	 * Configuration property:  HTTP part parser.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_partParser REST_partParser}
	 * 	<li><b>Name:</b>  <js>"RestContext.partParser.o"</js>
	 * 	<li><b>Data type:</b>  <c>{@link org.apache.juneau.httppart.HttpPartParser}|Class&lt;{@link org.apache.juneau.httppart.HttpPartParser}&gt;</c>
	 * 	<li><b>Default:</b>  {@link org.apache.juneau.oapi.OpenApiParser}
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#partParser()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#partParser(Class)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#partParser(HttpPartParser)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Specifies the {@link HttpPartParser} to use for parsing headers, query/form parameters, and URI parts.
	 *
	 * <p>
	 * The default value is {@link OpenApiParser} which allows for both plain-text and URL-Encoded-Object-Notation values.
	 * <br>If your parts contain text that can be confused with UON (e.g. <js>"(foo)"</js>), you can switch to
	 * {@link SimplePartParser} which treats everything as plain text.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation.</jc>
	 * 	<ja>@Rest</ja>(partParser=SimplePartParser.<jk>class</jk>)
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder builder) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			builder.partParser(SimplePartParser.<jk>class</jk>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			builder.set(<jsf>REST_partParser</jsf>, SimplePartParser.<jk>class</jk>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			builder.partParser(SimplePartParser.<jk>class</jk>);
	 * 		}
	 *
	 * 		<ja>@RestMethod</ja>(...)
	 * 		<jk>public</jk> Object myMethod(<ja>@Header</ja>(<js>"My-Header"</js>) MyParsedHeader h, <ja>@Query</ja>(<js>"myquery"</js>) MyParsedQuery q) {
	 * 			<jc>// Do something with your parsed parts.</jc>
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		When defined as a class, properties/transforms defined on the resource/method are inherited.
	 * 	<li>
	 * 		When defined as an instance, properties/transforms defined on the resource/method are NOT inherited.
	 * </ul>
	 */
	public static final String REST_partParser = PREFIX + ".partParser.o";

	/**
	 * Configuration property:  HTTP part serializer.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_partSerializer REST_partSerializer}
	 * 	<li><b>Name:</b>  <js>"RestContext.partSerializer.o"</js>
	 * 	<li><b>Data type:</b>
	 * 		<ul>
	 * 			<li>{@link org.apache.juneau.httppart.HttpPartSerializer}
	 * 			<li><c>Class&lt;{@link org.apache.juneau.httppart.HttpPartSerializer}&gt;</c>
	 * 		</ul>
	 * 	<li><b>Default:</b>  {@link org.apache.juneau.oapi.OpenApiSerializer}
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#partSerializer()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#partSerializer(Class)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#partSerializer(HttpPartSerializer)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Specifies the {@link HttpPartSerializer} to use for serializing headers, query/form parameters, and URI parts.
	 *
	 * <p>
	 * The default value is {@link OpenApiSerializer} which serializes based on OpenAPI rules, but defaults to UON notation for beans and maps, and
	 * plain text for everything else.
	 * <br>Other options include:
	 * <ul>
	 * 	<li class='jc'>{@link SimplePartSerializer} - Always serializes to plain text.
	 * 	<li class='jc'>{@link UonSerializer} - Always serializers to UON.
	 * </ul>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation.</jc>
	 * 	<ja>@Rest</ja>(partSerializer=SimplePartSerializer.<jk>class</jk>)
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder builder) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			builder.partSerializer(SimplePartSerializer.<jk>class</jk>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			builder.set(<jsf>REST_partSerializer</jsf>, SimplePartSerializer.<jk>class</jk>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			builder.partSerializer(SimplePartSerializer.<jk>class</jk>);
	 * 		}
	 *
	 * 		<ja>@RestMethod</ja>(...)
	 * 		<jk>public</jk> Object myMethod(RestResponse res) {
	 * 			<jc>// Set a header to a POJO.</jc>
	 * 			res.setHeader(<js>"My-Header"</js>, <jk>new</jk> MyPojo());
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		When defined as a class, properties/transforms defined on the resource/method are inherited.
	 * 	<li>
	 * 		When defined as an instance, properties/transforms defined on the resource/method are NOT inherited.
	 * </ul>
	 */
	public static final String REST_partSerializer = PREFIX + ".partSerializer.o";

	/**
	 * Configuration property:  Resource path.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_path REST_path}
	 * 	<li><b>Name:</b>  <js>"RestContext.path.s"</js>
	 * 	<li><b>Data type:</b>  <c>String</c>
	 * 	<li><b>System property:</b>  <c>RestContext.path.</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_PATH</c>
	 * 	<li><b>Default:</b>  <jk>null</jk>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#path()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#path(String)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Identifies the URL subpath relative to the ascendant resource.
	 *
	 * <p>
	 * This setting is critical for the routing of HTTP requests from ascendant to child resources.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation.</jc>
	 * 	<ja>@Rest</ja>(path=<js>"/myResource"</js>)
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder builder) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			builder.path(<js>"/myResource"</js>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			builder.set(<jsf>REST_path</jsf>, <js>"/myResource"</js>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			builder.path(<js>"/myResource"</js>);
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <p>
	 * <ul class='notes'>
	 * 	<li>
	 * 		This annotation is ignored on top-level servlets (i.e. servlets defined in <c>web.xml</c> files).
	 * 		<br>Therefore, implementers can optionally specify a path value for documentation purposes.
	 * 	<li>
	 * 		Typically, this setting is only applicable to resources defined as children through the
	 * 		{@link Rest#children() @Rest(children)} annotation.
	 * 		<br>However, it may be used in other ways (e.g. defining paths for top-level resources in microservices).
	 * 	<li>
	 * 		Slashes are trimmed from the path ends.
	 * 		<br>As a convention, you may want to start your path with <js>'/'</js> simple because it make it easier to read.
	 * 	<li>
	 * 		This path is available through the following method:
	 * 		<ul>
	 * 			<li class='jm'>{@link RestContext#getPath() RestContext.getPath()}
	 * 		</ul>
	 * </ul>
	 */
	public static final String REST_path = PREFIX + ".path.s";

	/**
	 * Configuration property:  Supported accept media types.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_produces REST_produces}
	 * 	<li><b>Name:</b>  <js>"RestContext.produces.ls"</js>
	 * 	<li><b>Data type:</b>  <c>List&lt;String&gt;</c>
	 * 	<li><b>System property:</b>  <c>RestContext.produces</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_PRODUCES</c>
	 * 	<li><b>Default:</b>  empty list
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#produces()}
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.RestMethod#produces()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#produces(String...)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#produces(MediaType...)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#producesReplace(String...)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#producesReplace(MediaType...)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Overrides the media types inferred from the serializers that identify what media types can be produced by the resource.
	 * <br>An example where this might be useful if you have serializers registered that handle media types that you
	 * don't want exposed in the Swagger documentation.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation resolving to a config file setting with default value.</jc>
	 * 	<ja>@Rest</ja>(produces={<js>"$C{REST/supportedProduces,application/json}"</js>})
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder builder) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			builder.produces(<jk>false</jk>, <js>"application/json"</js>)
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			builder.set(<jsf>REST_produces</jsf>, <js>"application/json"</js>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			builder.produces(<jk>false</jk>, <js>"application/json"</js>);
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <p>
	 * This affects the returned values from the following:
	 * <ul class='javatree'>
	 * 	<li class='jm'>{@link RestContext#getProduces() RestContext.getProduces()}
	 * 	<li class='jm'>{@link SwaggerProvider#getSwagger(RestContext,Locale)} - Affects produces field.
	 * </ul>
	 */
	public static final String REST_produces = PREFIX + ".produces.ls";

	/**
	 * Configuration property:  Render response stack traces in responses.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_renderResponseStackTraces REST_renderResponseStackTraces}
	 * 	<li><b>Name:</b>  <js>"RestContext.renderResponseStackTraces.b"</js>
	 * 	<li><b>Data type:</b>  <jk>boolean</jk>
	 * 	<li><b>System property:</b>  <c>RestContext.renderResponseStackTraces</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_RENDERRESPONSESTACKTRACES</c>
	 * 	<li><b>Default:</b>  <jk>false</jk>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#renderResponseStackTraces()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#renderResponseStackTraces(boolean)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#renderResponseStackTraces()}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Render stack traces in HTTP response bodies when errors occur.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation.</jc>
	 * 	<ja>@Rest</ja>(renderResponseStackTraces=<jk>true</jk>)
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder builder) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			builder.renderResponseStackTraces();
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			builder.set(<jsf>REST_renderResponseStackTraces</jsf>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			builder.renderResponseStackTraces();
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		Useful for debugging, although allowing stack traces to be rendered may cause security concerns so use
	 * 		caution when enabling.
	 * 	<li>
	 * 		This setting is available through the following method:
	 * 		<ul>
	 * 			<li class='jm'>{@link RestContext#isRenderResponseStackTraces() RestContext.isRenderResponseStackTraces()}
	 * 		</ul>
	 * 		That method is used by {@link #handleError(RestCall, Throwable)}.
	 * </ul>
	 */
	public static final String REST_renderResponseStackTraces = PREFIX + ".renderResponseStackTraces.b";

	/**
	 * Configuration property:  Response handlers.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_responseHandlers REST_responseHandlers}
	 * 	<li><b>Name:</b>  <js>"RestContext.responseHandlers.lo"</js>
	 * 	<li><b>Data type:</b>  <c>List&lt;{@link org.apache.juneau.rest.ResponseHandler}|Class&lt;{@link org.apache.juneau.rest.ResponseHandler}&gt;&gt;</c>
	 * 	<li><b>Default:</b>  empty list
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#responseHandlers()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#responseHandlers(Class...)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#responseHandlers(ResponseHandler...)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Specifies a list of {@link ResponseHandler} classes that know how to convert POJOs returned by REST methods or
	 * set via {@link RestResponse#setOutput(Object)} into appropriate HTTP responses.
	 *
	 * <p>
	 * By default, the following response handlers are provided out-of-the-box:
	 * <ul>
	 * 	<li class='jc'>{@link ReaderHandler} - {@link Reader} objects.
	 * 	<li class='jc'>{@link InputStreamHandler} - {@link InputStream} objects.
	 * 	<li class='jc'>{@link DefaultHandler} - All other POJOs.
	 * </ul>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Our custom response handler for MySpecialObject objects. </jc>
	 * 	<jk>public class</jk> MyResponseHandler <jk>implements</jk> ResponseHandler {
	 *
	 * 		<ja>@Override</ja>
	 * 		<jk>public boolean</jk> handle(RestRequest req, RestResponse res, Object output) <jk>throws</jk> IOException, RestException {
	 * 			<jk>if</jk> (output <jk>instanceof</jk> MySpecialObject) {
	 * 				<jk>try</jk> (Writer w = res.getNegotiatedWriter()) {
	 * 					<jc>//Pipe it to the writer ourselves.</jc>
	 * 				}
	 * 				<jk>return true</jk>;  <jc>// We handled it.</jc>
	 * 			}
	 * 			<jk>return false</jk>; <jc>// We didn't handle it.</jc>
	 * 		}
	 * 	}
	 *
	 * 	<jc>// Option #1 - Defined via annotation.</jc>
	 * 	<ja>@Rest</ja>(responseHandlers=MyResponseHandler.<jk>class</jk>)
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder builder) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			builder.responseHandlers(MyResponseHandler.<jk>class</jk>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			builder.addTo(<jsf>REST_responseHandlers</jsf>, MyResponseHandler.<jk>class</jk>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			builder.responseHandlers(MyResponseHandler.<jk>class</jk>);
	 * 		}
	 *
	 * 		<ja>@RestMethod</ja>(...)
	 * 		<jk>public</jk> Object myMethod() {
	 * 			<jc>// Return a special object for our handler.</jc>
	 * 			<jk>return new</jk> MySpecialObject();
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		Response handlers resolvers are always inherited from ascendant resources.
	 * 	<li>
	 * 		When defined as a class, the implementation must have one of the following constructors:
	 * 		<ul>
	 * 			<li><code><jk>public</jk> T(RestContext)</code>
	 * 			<li><code><jk>public</jk> T()</code>
	 * 			<li><code><jk>public static</jk> T <jsm>create</jsm>(RestContext)</code>
	 * 			<li><code><jk>public static</jk> T <jsm>create</jsm>()</code>
	 * 		</ul>
	 * 	<li>
	 * 		Inner classes of the REST resource class are allowed.
	 * </ul>
	 */
	public static final String REST_responseHandlers = PREFIX + ".responseHandlers.lo";

	/**
	 * Configuration property:  REST children class.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_restChildrenClass REST_restChildrenClass}
	 * 	<li><b>Name:</b>  <js>"RestContext.restChildrenClass.c"</js>
	 * 	<li><b>Data type:</b>  <c>Class&lt;? extends {@link org.apache.juneau.rest.RestChildren}&gt;</c>
	 * 	<li><b>Default:</b>  {@link org.apache.juneau.rest.RestChildren}
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#restChildrenClass()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#restChildrenClass(Class)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Allows you to extend the {@link RestChildren} class to modify how any of the methods are implemented.
	 *
	 * <p>
	 * The subclass must have a public constructor that takes in any of the following arguments:
	 * <ul>
	 * 	<li>{@link RestChildrenBuilder} - The builder for the object.
	 * 	<li>Any beans found in the specified {@link #REST_beanFactory bean factory}.
	 * 	<li>Any {@link Optional} beans that may or may not be found in the specified {@link #REST_beanFactory bean factory}.
	 * </ul>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Our extended context class</jc>
	 * 	<jk>public</jk> MyRestChildren <jk>extends</jk> RestChildren {
	 * 		<jk>public</jk> MyRestChildren(RestChildrenBuilder <jv>builder</jv>, ARequiredSpringBean <jv>bean1</jv>, Optional&lt;AnOptionalSpringBean&gt; <jv>bean2</jv>) {
	 * 			<jk>super</jk>(<jv>builder</jv>);
	 * 		}
	 *
	 * 		<jc>// Override any methods.</jc>
	 *
	 * 		<ja>@Override</ja>
	 * 		<jk>public</jk> Optional&lt;RestChildMatch&gt; findMatch(RestCall <jv>call</jv>) {
	 * 			String <jv>path</jv> = <jv>call</jv>.getPathInfo();
	 * 			<jk>if</jk> (<jv>path</jv>.endsWith(<js>"/foo"</js>)) {
	 * 				<jc>// Do our own special handling.</jc>
	 * 			}
	 * 			<jk>return super</jk>.findMatch(<jv>call</jv>);
	 * 		}
	 * 	}
	 * </p>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation.</jc>
	 * 	<ja>@Rest</ja>(restChildrenClass=MyRestChildren.<jk>class</jk>)
	 * 	<jk>public class</jk> MyResource {
	 * 		...
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder <jv>builder</jv>) <jk>throws</jk> Exception {
	 * 			<jv>builder</jv>.restChildrenClass(MyRestChildren.<jk>class</jk>);
	 * 		}
	 * 	}
	 * </p>
	 */
	public static final String REST_restChildrenClass = PREFIX + ".restChildrenClass.c";

	/**
	 * Configuration property:  REST methods class.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_restMethodsClass REST_restMethodsClass}
	 * 	<li><b>Name:</b>  <js>"RestContext.restMethodsClass.c"</js>
	 * 	<li><b>Data type:</b>  <c>Class&lt;? extends {@link org.apache.juneau.rest.RestMethods}&gt;</c>
	 * 	<li><b>Default:</b>  {@link org.apache.juneau.rest.RestMethods}
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#restMethodsClass()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#restMethodsClass(Class)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Allows you to extend the {@link RestMethods} class to modify how any of the methods are implemented.
	 *
	 * <p>
	 * The subclass must have a public constructor that takes in any of the following arguments:
	 * <ul>
	 * 	<li>{@link RestMethodsBuilder} - The builder for the object.
	 * 	<li>Any beans found in the specified {@link #REST_beanFactory bean factory}.
	 * 	<li>Any {@link Optional} beans that may or may not be found in the specified {@link #REST_beanFactory bean factory}.
	 * </ul>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Our extended context class</jc>
	 * 	<jk>public</jk> MyRestMethods <jk>extends</jk> RestMethods {
	 * 		<jk>public</jk> MyRestMethods(RestMethodsBuilder <jv>builder</jv>, ARequiredSpringBean <jv>bean1</jv>, Optional&lt;AnOptionalSpringBean&gt; <jv>bean2</jv>) {
	 * 			<jk>super</jk>(<jv>builder</jv>);
	 * 		}
	 *
	 * 		<jc>// Override any methods.</jc>
	 *
	 * 		<ja>@Override</ja>
	 * 		<jk>public</jk> RestMethodContext findMethod(RestCall <jv>call</jv>) <jk>throws</jk> MethodNotAllowed, PreconditionFailed, NotFound {
	 * 			String <jv>path</jv> = <jv>call</jv>.getPathInfo();
	 * 			<jk>if</jk> (<jv>path</jv>.endsWith(<js>"/foo"</js>)) {
	 * 				<jc>// Do our own special handling.</jc>
	 * 			}
	 * 			<jk>return super</jk>.findMethod(<jv>call</jv>);
	 * 		}
	 * 	}
	 * </p>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation.</jc>
	 * 	<ja>@Rest</ja>(restMethodsClass=MyRestMethods.<jk>class</jk>)
	 * 	<jk>public class</jk> MyResource {
	 * 		...
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder <jv>builder</jv>) <jk>throws</jk> Exception {
	 * 			<jv>builder</jv>.restMethodsClass(MyRestMethods.<jk>class</jk>);
	 * 		}
	 * 	}
	 * </p>
	 */
	public static final String REST_restMethodsClass = PREFIX + ".restMethodsClass.c";

	/**
	 * Configuration property:  Java REST method parameter resolvers.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_restParams REST_restParams}
	 * 	<li><b>Name:</b>  <js>"RestContext.restParams.lo"</js>
	 * 	<li><b>Data type:</b>  <c>List&lt;Class&lt;{@link org.apache.juneau.rest.RestParam}&gt;&gt;</c>
	 * 	<li><b>Default:</b>  empty list
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#restParams()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#restParams(Class...)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * By default, the Juneau framework will automatically Java method parameters of various types (e.g.
	 * <c>RestRequest</c>, <c>Accept</c>, <c>Reader</c>).
	 * This setting allows you to provide your own resolvers for your own class types that you want resolved.
	 *
	 * <p>
	 * For example, if you want to pass in instances of <c>MySpecialObject</c> to your Java method, define
	 * the following resolver:
	 * <p class='bcode w800'>
	 * 	<jc>// Define a parameter resolver for resolving MySpecialObject objects.</jc>
	 * 	<jk>public class</jk> MyRestParam <jk>implements</jk> RestParam {
	 *
	 *		<jc>// Must implement a static creator method that takes in a ParamInfo that describes the parameter
	 *		// being checked.  If the parameter isn't of type MySpecialObject, then it should return null.</jc>
	 *		<jk>public static</jk> MyRestParam <jsm>create</jsm>(ParamInfo <jv>paramInfo</jv>) {
	 *			<jk>if</jk> (<jv>paramInfo</jv>.isType(MySpecialObject.<jk>class</jk>)
	 *				<jk>return new</jk> MyRestParam();
	 *			<jk>return null</jk>;
	 *		}
	 *
	 * 		<jk>public</jk> MyRestParam() {}
	 *
	 * 		<jc>// The method that creates our object.
	 * 		// In this case, we're taking in a query parameter and converting it to our object.</jc>
	 * 		<ja>@Override</ja>
	 * 		<jk>public</jk> Object resolve(RestCall <jv>call</jv>) <jk>throws</jk> Exception {
	 * 			<jk>return new</jk> MySpecialObject(<jv>call</jv>.getRestRequest().getQuery().get(<js>"myparam"</js>));
	 * 		}
	 * 	}
	 *
	 * 	<jc>// Option #1 - Registered via annotation.</jc>
	 * 	<ja>@Rest</ja>(restParams=MyRestParam.<jk>class</jk>)
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Registered via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder <jv>builder</jv>) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			<jv>builder</jv>.restParams(MyRestParam.<jk>class</jk>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			<jv>builder</jv>.addTo(<jsf>REST_restParams</jsf>, MyRestParam.<jk>class</jk>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Registered via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder <jv>builder</jv>) <jk>throws</jk> Exception {
	 * 			<jv>builder</jv>.restParams(MyRestParam.<jk>class</jk>);
	 * 		}
	 *
	 * 		<jc>// Now pass it into your method.</jc>
	 * 		<ja>@RestMethod</ja>(...)
	 * 		<jk>public</jk> Object doMyMethod(MySpecialObject <jv>mySpecialObject</jv>) {
	 * 			<jc>// Do something with it.</jc>
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		Inner classes of the REST resource class are allowed.
	 * 	<li>
	 * 		Refer to {@link RestParam} for the list of predefined parameter resolvers.
	 * </ul>
	 */
	public static final String REST_restParams = PREFIX + ".restParams.lo";

	/**
	 * Configuration property:  Role guard.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_roleGuard REST_roleGuard}
	 * 	<li><b>Name:</b>  <js>"RestContext.roleGuard.ss"</js>
	 * 	<li><b>Data type:</b>  <c>Set&lt;String&gt;</c>
	 * 	<li><b>System property:</b>  <c>RestContext.roleGuard</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_ROLEGUARD</c>
	 * 	<li><b>Default:</b>  empty set
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#roleGuard()}
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.RestMethod#roleGuard()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#roleGuard(String)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * An expression defining if a user with the specified roles are allowed to access methods on this class.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<ja>@Rest</ja>(
	 * 		path=<js>"/foo"</js>,
	 * 		roleGuard=<js>"ROLE_ADMIN || (ROLE_READ_WRITE &amp;&amp; ROLE_SPECIAL)"</js>
	 * 	)
	 * 	<jk>public class</jk> MyResource <jk>extends</jk> RestServlet {
	 * 		...
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		Supports any of the following expression constructs:
	 * 		<ul>
	 * 			<li><js>"foo"</js> - Single arguments.
	 * 			<li><js>"foo,bar,baz"</js> - Multiple OR'ed arguments.
	 * 			<li><js>"foo | bar | bqz"</js> - Multiple OR'ed arguments, pipe syntax.
	 * 			<li><js>"foo || bar || bqz"</js> - Multiple OR'ed arguments, Java-OR syntax.
	 * 			<li><js>"fo*"</js> - Patterns including <js>'*'</js> and <js>'?'</js>.
	 * 			<li><js>"fo* &amp; *oo"</js> - Multiple AND'ed arguments, ampersand syntax.
	 * 			<li><js>"fo* &amp;&amp; *oo"</js> - Multiple AND'ed arguments, Java-AND syntax.
	 * 			<li><js>"fo* || (*oo || bar)"</js> - Parenthesis.
	 * 		</ul>
	 * 	<li>
	 * 		AND operations take precedence over OR operations (as expected).
	 * 	<li>
	 * 		Whitespace is ignored.
	 * 	<li>
	 * 		<jk>null</jk> or empty expressions always match as <jk>false</jk>.
	 * 	<li>
	 * 		If patterns are used, you must specify the list of declared roles using {@link Rest#rolesDeclared()} or {@link RestContext#REST_rolesDeclared}.
	 * 	<li>
	 * 		Supports {@doc RestSvlVariables}
	 * 		(e.g. <js>"$L{my.localized.variable}"</js>).
	 * 	<li>
	 * 		Role guards defined at both the class and method level must both pass.
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestContext#REST_roleGuard}
	 * </ul>
	 */
	public static final String REST_roleGuard = PREFIX + ".roleGuard.ss";

	/**
	 * Configuration property:  Declared roles.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_rolesDeclared REST_rolesDeclared}
	 * 	<li><b>Name:</b>  <js>"RestContext.rolesDeclared.ss"</js>
	 * 	<li><b>Data type:</b>  <c>Set&lt;String&gt;</c>
	 * 	<li><b>System property:</b>  <c>RestContext.rolesDeclared</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_ROLESDECLARED</c>
	 * 	<li><b>Default:</b>  empty list
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#rolesDeclared()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#rolesDeclared(String...)}
	 * 		</ul>
	 * </ul>
	 *
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * A comma-delimited list of all possible user roles.
	 *
	 * <p>
	 * Used in conjunction with {@link RestContextBuilder#roleGuard(String)} is used with patterns.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<ja>@Rest</ja>(
	 * 		rolesDeclared=<js>"ROLE_ADMIN,ROLE_READ_WRITE,ROLE_READ_ONLY,ROLE_SPECIAL"</js>,
	 * 		roleGuard=<js>"ROLE_ADMIN || (ROLE_READ_WRITE &amp;&amp; ROLE_SPECIAL)"</js>
	 * 	)
	 * 	<jk>public class</jk> MyResource <jk>extends</jk> RestServlet {
	 * 		...
	 * 	}
	 * </p>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestContext#REST_rolesDeclared}
	 * </ul>
	 */
	public static final String REST_rolesDeclared = PREFIX + ".rolesDeclared.ss";

	/**
	 * Configuration property:  Serializers.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_serializers REST_serializers}
	 * 	<li><b>Name:</b>  <js>"RestContext.serializers.lo"</js>
	 * 	<li><b>Data type:</b>  <c>List&lt;{@link org.apache.juneau.serializer.Serializer}|Class&lt;{@link org.apache.juneau.serializer.Serializer}&gt;&gt;</c>
	 * 	<li><b>Default:</b>  empty list
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#serializers()}
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.RestMethod#serializers()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#serializers(Object...)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#serializers(Class...)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#serializersReplace(Object...)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#serializersReplace(Class...)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Adds class-level serializers to this resource.
	 *
	 * <p>
	 * Serializer are used to convert POJOs to HTTP response bodies.
	 * <br>Any of the Juneau framework serializers can be used in this setting.
	 * <br>The serializer selected is based on the request <c>Accept</c> header matched against the values returned by the following method
	 * using a best-match algorithm:
	 * <ul class='javatree'>
	 * 	<li class='jm'>{@link Serializer#getMediaTypeRanges()}
	 * </ul>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation.</jc>
	 * 	<ja>@Rest</ja>(serializers={JsonSerializer.<jk>class</jk>, XmlSerializer.<jk>class</jk>})
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder builder) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			builder.serializers(JsonSerializer.<jk>class</jk>, XmlSerializer.<jk>class</jk>);
	 *
	 * 			<jc>// Same, but use pre-instantiated parsers.</jc>
	 * 			builder.serializers(JsonSerializer.<jsf>DEFAULT</jsf>, XmlSerializer.<jsf>DEFAULT</jsf>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			builder.set(<jsf>REST_serializers</jsf>, JsonSerializer.<jk>class</jk>, XmlSerializer.<jk>class</jk>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			builder.serializers(JsonSerializer.<jk>class</jk>, XmlSerializer.<jk>class</jk>);
	 * 		}
	 *
	 * 		<jc>// Override at the method level.</jc>
	 * 		<ja>@RestMethod</ja>(serializers={HtmlSerializer.<jk>class</jk>})
	 * 		<jk>public</jk> MyPojo myMethod() {
	 * 			<jc>// Return a POJO to be serialized.</jc>
	 * 			<jk>return new</jk> MyPojo();
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <ul class='notes'>
	 * 	<li>
	 * 		When defined as a class, properties/transforms defined on the resource/method are inherited.
	 * 	<li>
	 * 		When defined as an instance, properties/transforms defined on the resource/method are NOT inherited.
	 * 	<li>
	 * 		Typically, you'll want your resource to extend directly from {@link BasicRestServlet} which comes
	 * 		preconfigured with the following serializers:
	 * 		<ul>
	 * 			<li class='jc'>{@link HtmlDocSerializer}
	 * 			<li class='jc'>{@link HtmlStrippedDocSerializer}
	 * 			<li class='jc'>{@link HtmlSchemaDocSerializer}
	 * 			<li class='jc'>{@link JsonSerializer}
	 * 			<li class='jc'>{@link SimpleJsonSerializer}
	 * 			<li class='jc'>{@link JsonSchemaSerializer}
	 * 			<li class='jc'>{@link XmlDocSerializer}
	 * 			<li class='jc'>{@link UonSerializer}
	 * 			<li class='jc'>{@link UrlEncodingSerializer}
	 * 			<li class='jc'>{@link MsgPackSerializer}
	 * 			<li class='jc'>{@link SoapXmlSerializer}
	 * 			<li class='jc'>{@link PlainTextSerializer}
	 * 		</ul>
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='link'>{@doc RestSerializers}
	 * </ul>
	 * <p>
	 */
	public static final String REST_serializers = PREFIX + ".serializers.lo";

	/**
	 * Configuration property:  Static file finder.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_staticFiles REST_staticFiles}
	 * 	<li><b>Name:</b>  <js>"RestContext.staticFiles.o"</js>
	 * 	<li><b>Data type:</b>  {@link org.apache.juneau.rest.StaticFiles}
	 * 	<li><b>Default:</b>  {@link #REST_staticFilesDefault}
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#staticFiles()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#staticFiles(Class)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#staticFiles(StaticFiles)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContext#createStaticFiles(Object,BeanFactory)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Used to retrieve localized files to be served up as static files through the REST API via the following
	 * predefined methods:
	 * <ul class='javatree'>
	 * 	<li class='jm'>{@link BasicRestObject#getHtdoc(String, Locale)}.
	 * 	<li class='jm'>{@link BasicRestServlet#getHtdoc(String, Locale)}.
	 * </ul>
	 *
	 * <p>
	 * The static file finder can be accessed through the following methods:
	 * <ul class='javatree'>
	 * 	<li class='jm'>{@link RestContext#getStaticFiles()}
	 * 	<li class='jm'>{@link RestRequest#getStaticFiles()}
	 * </ul>
	 *
	 * <p>
	 * The static file finder is instantiated via the {@link RestContext#createStaticFiles(Object,BeanFactory)} method which in turn instantiates
	 * based on the following logic:
	 * <ul>
	 * 	<li>Returns the resource class itself is an instance of {@link StaticFiles}.
	 * 	<li>Looks in {@link #REST_staticFiles} setting.
	 * 	<li>Looks for a public <c>createStaticFiles()</> method on the resource class with an optional {@link RestContext} argument.
	 * 	<li>Instantiates a {@link BasicStaticFiles} which provides basic support for finding localized
	 * 		resources on the classpath and JVM working directory..
	 * </ul>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Create a static file finder that looks for files in the /files working subdirectory, but overrides the find()
	 * 	// and resolve methods for special handling of special cases and adds a Foo header to all requests.</jc>
	 * 	<jk>public class</jk> MyStaticFiles <jk>extends</jk> StaticFiles {
	 *
	 * 		<jk>public</jk> MyStaticFiles() {
	 * 			<jk>super</jk>(
	 * 				<jk>new</jk> StaticFilesBuilder()
	 * 					.dir(<js>"/files"</js>)
	 * 					.headers(BasicStringHeader.<jsm>of</jsm>(<js>"Foo"</js>, <js>"bar"</js>))
	 * 			);
	 * 		}
	 *
	 *		<ja>@Override</ja> <jc>// FileFinder</jc>
	 * 		<jk>protected</jk> Optional&lt;InputStream&gt; find(String <jv>name</jv>, Locale <jv>locale</jv>) <jk>throws</jk> IOException {
	 * 			<jc>// Do special handling or just call super.find().</jc>
	 * 			<jk>return super</jk>.find(<jv>name</jv>, <jv>locale</jv>);
	 * 		}
	 *
	 *		<ja>@Override</ja> <jc>// staticFiles</jc>
	 * 		<jk>public</jk> Optional&lt;BasicHttpResource&gt; resolve(String <jv>path</jv>, Locale <jv>locale</jv>) {
	 * 			<jc>// Do special handling or just call super.resolve().</jc>
	 * 			<jk>return super</jk>.resolve(<jv>path</jv>, <jv>locale</jv>);
	 * 		}
	 * 	}
	 * </p>
	 *
	 * 	<jc>// Option #1 - Registered via annotation.</jc>
	 * 	<ja>@Rest</ja>(staticFiles=MyStaticFiles.<jk>class</jk>)
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Created via createStaticFiles() method.</jc>
	 * 		<jk>public</jk> StaticFiles createStaticFiles(RestContext <jv>context</jv>) <jk>throws</jk> Exception {
	 * 			<jk>return new</jk> MyStaticFiles();
	 * 		}
	 *
	 * 		<jc>// Option #3 - Registered via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder <jv>builder</jv>) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			<jv>builder</jv>.staticFiles(MyStaticFiles.<jk>class</jk>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			<jv>builder</jv>.set(<jsf>REST_staticFiles</jsf>, MyStaticFiles.<jk>class</jk>));
	 *
	 * 			<jc>// Use a pre-instantiated object instead.</jc>
	 * 			<jv>builder</jv>.staticFiles(<jk>new</jk> MyStaticFiles());
	 * 		}
	 *
	 * 		<jc>// Option #4 - Registered via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder <jv>builder</jv>) <jk>throws</jk> Exception {
	 * 			<jv>builder</jv>.staticFiles(MyStaticFiles.<jk>class</jk>);
	 * 		}
	 *
	 * 		<jc>// Create a REST method that uses the static files finder.</jc>
	 * 		<ja>@RestMethod<ja>(
	 * 			method=<jsf>GET</jsf>,
	 * 			path=<js>"/htdocs/*"</js>
	 * 		)
	 * 		<jk>public</jk> HttpResource getHtdoc(RestRequest <jv>req</jv>, <ja>@Path</ja>("/*") String <jv>path</jv>, Locale <jv>locale</jv>) <jk>throws</jk> NotFound {
	 * 			<jk>return</jk> <jv>req</jv>.getStaticFiles().resolve(<jv>path</jv>, <jv>locale</jv>).orElseThrow(NotFound::<jk>new</jk>);
	 * 		}
	 * 	}
	 * </p>
	 */
	public static final String REST_staticFiles = PREFIX + ".staticFiles.o";

	/**
	 * Configuration property:  Static file finder default.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_staticFilesDefault REST_staticFilesDefault}
	 * 	<li><b>Name:</b>  <js>"RestContext.staticFilesDefault.o"</js>
	 * 	<li><b>Data type:</b>  {@link org.apache.juneau.rest.StaticFiles}
	 * 	<li><b>Default:</b>  {@link org.apache.juneau.rest.BasicStaticFiles}
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#staticFilesDefault(Class)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#staticFilesDefault(StaticFiles)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * The default static file finder.
	 * <p>
	 * This setting is inherited from the parent context.
	 */
	public static final String REST_staticFilesDefault = PREFIX + ".staticFilesDefault.o";

	/**
	 * Configuration property:  Swagger provider class.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_swaggerProvider REST_swaggerProvider}
	 * 	<li><b>Name:</b>  <js>"RestContext.swaggerProvider.o"</js>
	 * 	<li><b>Data type:</b>  {@link org.apache.juneau.rest.SwaggerProvider}
	 * 	<li><b>Default:</b>  {@link org.apache.juneau.rest.SwaggerProvider}
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#swaggerProvider()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#swaggerProvider(Class)}
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#swaggerProvider(SwaggerProvider)}
	 * 		</ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * The swagger provider object or class.
	 * <p>
	 * This setting is inherited from the parent context.
	 */
	public static final String REST_swaggerProvider = PREFIX + ".swaggerProvider.o";

	/**
	 * Configuration property:  Resource URI authority path.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_uriAuthority REST_uriAuthority}
	 * 	<li><b>Name:</b>  <js>"RestContext.uriAuthority.s"</js>
	 * 	<li><b>Data type:</b>  <c>String</c>
	 * 	<li><b>System property:</b>  <c>RestContext.uriAuthority</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_URIAUTHORITY</c>
	 * 	<li><b>Default:</b>  <jk>null</jk>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#uriAuthority()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#uriAuthority(String)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Overrides the authority path value for this resource and any child resources.
	 *
	 * <p>
	 * Affects the following methods:
	 * <ul class='javatree'>
	 * 	<li class='jm'>{@link RestRequest#getAuthorityPath()}
	 * </ul>
	 *
	 * <p>
	 * If you do not specify the authority, it is automatically calculated via the following:
	 *
	 * <p class='bcode w800'>
	 * 	String scheme = request.getScheme();
	 * 	<jk>int</jk> port = request.getServerPort();
	 * 	StringBuilder sb = <jk>new</jk> StringBuilder(request.getScheme()).append(<js>"://"</js>).append(request.getServerName());
	 * 	<jk>if</jk> (! (port == 80 &amp;&amp; <js>"http"</js>.equals(scheme) || port == 443 &amp;&amp; <js>"https"</js>.equals(scheme)))
	 * 		sb.append(<js>':'</js>).append(port);
	 * 	authorityPath = sb.toString();
	 * </p>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation resolving to a config file setting with default value.</jc>
	 * 	<ja>@Rest</ja>(
	 * 		path=<js>"/servlet"</js>,
	 * 		uriAuthority=<js>"$C{REST/authorityPathOverride,http://localhost:10000}"</js>
	 * 	)
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder builder) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			builder.uriAuthority(<js>"http://localhost:10000"</js>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			builder.set(<jsf>REST_uriAuthority</jsf>, <js>"http://localhost:10000"</js>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			builder.uriAuthority(<js>"http://localhost:10000"</js>);
	 * 		}
	 * 	}
	 * </p>
	 */
	public static final String REST_uriAuthority = PREFIX + ".uriAuthority.s";

	/**
	 * Configuration property:  Resource URI context path.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_uriContext REST_uriContext}
	 * 	<li><b>Name:</b>  <js>"RestContext.uriContext.s"</js>
	 * 	<li><b>Data type:</b>  <c>String</c>
	 * 	<li><b>System property:</b>  <c>RestContext.uriContext</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_URICONTEXT</c>
	 * 	<li><b>Default:</b>  <jk>null</jk>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#uriContext()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#uriContext(String)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Overrides the context path value for this resource and any child resources.
	 *
	 * <p>
	 * This setting is useful if you want to use <js>"context:/child/path"</js> URLs in child resource POJOs but
	 * the context path is not actually specified on the servlet container.
	 *
	 * <p>
	 * Affects the following methods:
	 * <ul class='javatree'>
	 * 	<li class='jm'>{@link RestRequest#getContextPath()} - Returns the overridden context path for the resource.
	 * 	<li class='jm'>{@link RestRequest#getServletPath()} - Includes the overridden context path for the resource.
	 * </ul>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation resolving to a config file setting with default value.</jc>
	 * 	<ja>@Rest</ja>(
	 * 		path=<js>"/servlet"</js>,
	 * 		uriContext=<js>"$C{REST/contextPathOverride,/foo}"</js>
	 * 	)
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder builder) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			builder.uriContext(<js>"/foo"</js>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			builder.set(<jsf>REST_uriContext</jsf>, <js>"/foo"</js>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			builder.uriContext(<js>"/foo"</js>);
	 * 		}
	 * 	}
	 * </p>
	 */
	public static final String REST_uriContext = PREFIX + ".uriContext.s";

	/**
	 * Configuration property:  URI resolution relativity.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_uriRelativity REST_uriRelativity}
	 * 	<li><b>Name:</b>  <js>"RestContext.uriRelativity.s"</js>
	 * 	<li><b>Data type:</b>  <c>String</c>
	 * 	<li><b>System property:</b>  <c>RestContext.uriRelativity</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_URIRELATIVITY</c>
	 * 	<li><b>Default:</b>  <js>"RESOURCE"</js>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#uriRelativity()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#uriRelativity(String)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Specifies how relative URIs should be interpreted by serializers.
	 *
	 * <p>
	 * See {@link UriResolution} for possible values.
	 *
	 * <p>
	 * Affects the following methods:
	 * <ul class='javatree'>
	 * 	<li class='jm'>{@link RestRequest#getUriResolver()}
	 * </ul>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation resolving to a config file setting with default value.</jc>
	 * 	<ja>@Rest</ja>(
	 * 		path=<js>"/servlet"</js>,
	 * 		uriRelativity=<js>"$C{REST/uriRelativity,PATH_INFO}"</js>
	 * 	)
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder builder) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			builder.uriRelativity(<js>"PATH_INFO"</js>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			builder.set(<jsf>REST_uriRelativity</jsf>, <js>"PATH_INFO"</js>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			builder.uriRelativity(<js>"PATH_INFO"</js>);
	 * 		}
	 * 	}
	 * </p>
	 */
	public static final String REST_uriRelativity = PREFIX + ".uriRelativity.s";

	/**
	 * Configuration property:  URI resolution.
	 *
	 * <h5 class='section'>Property:</h5>
	 * <ul class='spaced-list'>
	 * 	<li><b>ID:</b>  {@link org.apache.juneau.rest.RestContext#REST_uriResolution REST_uriResolution}
	 * 	<li><b>Name:</b>  <js>"RestContext.uriResolution.s"</js>
	 * 	<li><b>Data type:</b>  <c>String</c>
	 * 	<li><b>System property:</b>  <c>RestContext.uriResolution</c>
	 * 	<li><b>Environment variable:</b>  <c>RESTCONTEXT_URIRESOLUTION</c>
	 * 	<li><b>Default:</b>  <js>"ROOT_RELATIVE"</js>
	 * 	<li><b>Session property:</b>  <jk>false</jk>
	 * 	<li><b>Annotations:</b>
	 * 		<ul>
	 * 			<li class='ja'>{@link org.apache.juneau.rest.annotation.Rest#uriResolution()}
	 * 		</ul>
	 * 	<li><b>Methods:</b>
	 * 		<ul>
	 * 			<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#uriResolution(String)}
	 * 		</ul>
	 * </ul>
	 *
	 * <h5 class='section'>Description:</h5>
	 * <p>
	 * Specifies how relative URIs should be interpreted by serializers.
	 *
	 * <p>
	 * See {@link UriResolution} for possible values.
	 *
	 * <p>
	 * Affects the following methods:
	 * <ul class='javatree'>
	 * 	<li class='jm'>{@link RestRequest#getUriResolver()}
	 * </ul>
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Option #1 - Defined via annotation resolving to a config file setting with default value.</jc>
	 * 	<ja>@Rest</ja>(
	 * 		path=<js>"/servlet"</js>,
	 * 		uriResolution=<js>"$C{REST/uriResolution,ABSOLUTE}"</js>
	 * 	)
	 * 	<jk>public class</jk> MyResource {
	 *
	 * 		<jc>// Option #2 - Defined via builder passed in through resource constructor.</jc>
	 * 		<jk>public</jk> MyResource(RestContextBuilder builder) <jk>throws</jk> Exception {
	 *
	 * 			<jc>// Using method on builder.</jc>
	 * 			builder.uriResolution(<js>"ABSOLUTE"</js>);
	 *
	 * 			<jc>// Same, but using property.</jc>
	 * 			builder.set(<jsf>REST_uriResolution</jsf>, <js>"ABSOLUTE"</js>);
	 * 		}
	 *
	 * 		<jc>// Option #3 - Defined via builder passed in through init method.</jc>
	 * 		<ja>@RestHook</ja>(<jsf>INIT</jsf>)
	 * 		<jk>public void</jk> init(RestContextBuilder builder) <jk>throws</jk> Exception {
	 * 			builder.uriResolution(<js>"ABSOLUTE"</js>);
	 * 		}
	 * 	}
	 * </p>
	 */
	public static final String REST_uriResolution = PREFIX + ".uriResolution.s";


	//-------------------------------------------------------------------------------------------------------------------
	// Static
	//-------------------------------------------------------------------------------------------------------------------

	private static final Map<Class<?>, RestContext> REGISTRY = new ConcurrentHashMap<>();

	/**
	 * Returns a registry of all created {@link RestContext} objects.
	 *
	 * @return An unmodifiable map of resource classes to {@link RestContext} objects.
	 */
	public static final Map<Class<?>, RestContext> getGlobalRegistry() {
		return Collections.unmodifiableMap(REGISTRY);
	}

	//-------------------------------------------------------------------------------------------------------------------
	// Instance
	//-------------------------------------------------------------------------------------------------------------------

	private final Supplier<?> resource;
	private final Class<?> resourceClass;

	final RestContextBuilder builder;
	private final boolean
		allowBodyParam,
		renderResponseStackTraces;
	private final Enablement debug;
	private final String
		clientVersionHeader,
		uriAuthority,
		uriContext;
	final String path, fullPath;
	final UrlPathMatcher pathMatcher;

	private final Set<String> allowedMethodParams, allowedHeaderParams, allowedMethodHeaders;

	private final Class<? extends RestParam>[] restParams, hookMethodParams;
	private final SerializerGroup serializers;
	private final ParserGroup parsers;
	private final HttpPartSerializer partSerializer;
	private final HttpPartParser partParser;
	private final JsonSchemaGenerator jsonSchemaGenerator;
	private final List<MediaType>
		consumes,
		produces;
	final org.apache.http.Header[] defaultRequestHeaders, defaultResponseHeaders;
	final NamedAttribute[] defaultRequestAttributes;
	private final ResponseHandler[] responseHandlers;
	private final Messages msgs;
	private final Config config;
	private final VarResolver varResolver;
	private final RestMethods restMethods;
	private final RestChildren restChildren;
	private final StackTraceStore stackTraceStore;
	private final Logger logger;
//	private final RestInfoProvider infoProvider;
	private final SwaggerProvider swaggerProvider;
	private final HttpException initException;
	private final RestContext parentContext;
	final BeanFactory rootBeanFactory;
	private final BeanFactory beanFactory;
	private final UriResolution uriResolution;
	private final UriRelativity uriRelativity;
	private final ConcurrentHashMap<String,MethodExecStats> methodExecStats = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Locale,Swagger> swaggerCache = new ConcurrentHashMap<>();
	private final Instant startTime;
	private final Map<Class<?>,ResponseBeanMeta> responseBeanMetas = new ConcurrentHashMap<>();

	// Lifecycle methods
	private final MethodInvoker[]
		postInitMethods,
		postInitChildFirstMethods,
		startCallMethods,
		endCallMethods,
		destroyMethods;

	private final RestMethodInvoker[]
		preCallMethods,
		postCallMethods;

	private final FileFinder fileFinder;
	private final StaticFiles staticFiles;
	private final RestLogger callLogger;

	private final ThreadLocal<RestCall> call = new ThreadLocal<>();

	private final DebugEnablementMap debugEnablementMap;

	// Gets set when postInitChildFirst() gets called.
	private final AtomicBoolean initialized = new AtomicBoolean(false);

	/**
	 * Constructor.
	 *
	 * @param resource The resource annotated with <ja>@Rest</ja>.
	 * @return A new builder object.
	 * @throws ServletException Something bad happened.
	 */
	public static RestContextBuilder create(Object resource) throws ServletException {
		return new RestContextBuilder(Optional.empty(), Optional.empty(), resource.getClass(), Optional.of(resource)).init(resource);
	}

	/**
	 * Constructor.
	 *
	 * @param parentContext The parent context, or <jk>null</jk> if there is no parent context.
	 * @param servletConfig The servlet config passed into the servlet by the servlet container.
	 * @param resourceClass The class annotated with <ja>@Rest</ja>.
	 * @return A new builder object.
	 * @throws ServletException Something bad happened.
	 */
	static RestContextBuilder create(RestContext parentContext, ServletConfig servletConfig, Class<?> resourceClass, Object resource) throws ServletException {
		return new RestContextBuilder(Optional.ofNullable(parentContext), Optional.ofNullable(servletConfig), resourceClass, Optional.ofNullable(resource));
	}

	/**
	 * Constructor.
	 *
	 * @param builder The servlet configuration object.
	 * @throws Exception If any initialization problems were encountered.
	 */
	public RestContext(RestContextBuilder builder) throws Exception {
		super(builder.getPropertyStore());

		startTime = Instant.now();

		REGISTRY.put(builder.resourceClass, this);

		HttpException _initException = null;

		try {
			this.builder = builder;

			this.resourceClass = builder.resourceClass;
			this.resource = builder.resource;
			Object r = getResource();

			parentContext = builder.parentContext;
			ClassInfo rci = ClassInfo.ofProxy(r);

			rootBeanFactory = createBeanFactory(r);

			beanFactory = BeanFactory.of(rootBeanFactory, r);
			beanFactory.addBean(BeanFactory.class, beanFactory);
			beanFactory.addBean(RestContext.class, this);
			beanFactory.addBean(Object.class, r);

			PropertyStore ps = getPropertyStore();
			beanFactory.addBean(PropertyStore.class, ps);

			logger = createLogger(r, beanFactory);
			beanFactory.addBean(Logger.class, logger);

			stackTraceStore = createStackTraceStore(r, beanFactory);
			beanFactory.addBean(StackTraceStore.class, stackTraceStore);

			varResolver = createVarResolver(r, beanFactory);
			beanFactory.addBean(VarResolver.class, varResolver);

			config = builder.config.resolving(varResolver.createSession());
			beanFactory.addBean(Config.class, config);

			responseHandlers = createResponseHandlers(r, beanFactory).asArray();
			beanFactory.addBean(ResponseHandler[].class, responseHandlers);

			callLogger = createCallLogger(r, beanFactory);
			beanFactory.addBean(RestLogger.class, callLogger);

			serializers = createSerializers(r, beanFactory, ps);
			beanFactory.addBean(SerializerGroup.class, serializers);

			parsers = createParsers(r, beanFactory, ps);
			beanFactory.addBean(ParserGroup.class, parsers);

			partSerializer = createPartSerializer(r, beanFactory);
			beanFactory.addBean(HttpPartSerializer.class, partSerializer);

			partParser = createPartParser(r, beanFactory);
			beanFactory.addBean(HttpPartParser.class, partParser);

			jsonSchemaGenerator = createJsonSchemaGenerator(r, beanFactory);
			beanFactory.addBean(JsonSchemaGenerator.class, jsonSchemaGenerator);

			fileFinder = createFileFinder(r, beanFactory);
			beanFactory.addBean(FileFinder.class, fileFinder);

			staticFiles = createStaticFiles(r, beanFactory);
			beanFactory.addBean(StaticFiles.class, staticFiles);

			defaultRequestHeaders = createDefaultRequestHeaders(r, beanFactory).asArray();
			defaultResponseHeaders = createDefaultResponseHeaders(r, beanFactory).asArray();
			defaultRequestAttributes = createDefaultRequestAttributes(r, beanFactory).asArray();

			restParams = createRestParams(r, beanFactory).asArray();
			hookMethodParams = createHookMethodParams(r, beanFactory).asArray();

			uriContext = nullIfEmpty(getStringProperty(REST_uriContext));
			uriAuthority = nullIfEmpty(getStringProperty(REST_uriAuthority));
			uriResolution = getProperty(REST_uriResolution, UriResolution.class, UriResolution.ROOT_RELATIVE);
			uriRelativity = getProperty(REST_uriRelativity, UriRelativity.class, UriRelativity.RESOURCE);

			allowBodyParam = ! getBooleanProperty(REST_disableAllowBodyParam);
			allowedHeaderParams = newUnmodifiableSortedCaseInsensitiveSet(getStringPropertyWithNone(REST_allowedHeaderParams, "Accept,Content-Type"));
			allowedMethodParams = newUnmodifiableSortedCaseInsensitiveSet(getStringPropertyWithNone(REST_allowedMethodParams, "HEAD,OPTIONS"));
			allowedMethodHeaders = newUnmodifiableSortedCaseInsensitiveSet(getStringPropertyWithNone(REST_allowedMethodHeaders, ""));
			renderResponseStackTraces = getBooleanProperty(REST_renderResponseStackTraces);
			clientVersionHeader = getStringProperty(REST_clientVersionHeader, "X-Client-Version");

			debugEnablementMap = createDebugEnablement(r).build();

			debug = debugEnablementMap.find(rci.inner(), Enablement.class).orElse(Enablement.NEVER);

			consumes = getListProperty(REST_consumes, MediaType.class, parsers.getSupportedMediaTypes());
			produces = getListProperty(REST_produces, MediaType.class, serializers.getSupportedMediaTypes());

			msgs = createMessages(r);

			fullPath = (builder.parentContext == null ? "" : (builder.parentContext.fullPath + '/')) + builder.getPath();
			path = builder.getPath();

			String p = path;
			if (! p.endsWith("/*"))
				p += "/*";
			pathMatcher = UrlPathMatcher.of(p);

			startCallMethods = createStartCallMethods(r).stream().map(this::toMethodInvoker).toArray(MethodInvoker[]::new);
			endCallMethods = createEndCallMethods(r).stream().map(this::toMethodInvoker).toArray(MethodInvoker[]::new);
			postInitMethods = createPostInitMethods(r).stream().map(this::toMethodInvoker).toArray(MethodInvoker[]::new);
			postInitChildFirstMethods = createPostInitChildFirstMethods(r).stream().map(this::toMethodInvoker).toArray(MethodInvoker[]::new);
			destroyMethods = createDestroyMethods(r).stream().map(this::toMethodInvoker).toArray(MethodInvoker[]::new);

			preCallMethods = createPreCallMethods(r).stream().map(this::toRestMethodInvoker).toArray(RestMethodInvoker[]:: new);
			postCallMethods = createPostCallMethods(r).stream().map(this::toRestMethodInvoker).toArray(RestMethodInvoker[]:: new);

			restMethods = createRestMethods(r);
			restChildren = createRestChildren(r);

			swaggerProvider = createSwaggerProvider(r, beanFactory);

		} catch (HttpException e) {
			_initException = e;
			throw e;
		} catch (Exception e) {
			_initException = new InternalServerError(e);
			throw e;
		} finally {
			initException = _initException;
		}
	}

	private MethodInvoker toMethodInvoker(Method m) {
		return new MethodInvoker(m, getMethodExecStats(m));
	}

	private MethodInvoker toRestMethodInvoker(Method m) {
		return new RestMethodInvoker(m, findHookMethodParams(m, getBeanFactory()), getMethodExecStats(m));
	}

	/**
	 * Instantiates the bean factory for this REST resource.
	 *
	 * <p>
	 * The bean factory is typically used for passing in injected beans into REST contexts and for storing beans
	 * created by the REST context.
	 *
	 * <p>
	 * Instantiates based on the following logic:
	 * <ul>
	 * 	<li>Returns the resource class itself if it's an instance of {@link BeanFactory}.
	 * 	<li>Looks for {@link #REST_beanFactory} value set via any of the following:
	 * 		<ul>
	 * 			<li>{@link RestContextBuilder#beanFactory(Class)}/{@link RestContextBuilder#beanFactory(BeanFactory)}
	 * 			<li>{@link Rest#beanFactory()}.
	 * 		</ul>
	 * 	<li>Instantiates a new {@link BeanFactory}.
	 * 		Uses the parent context's root bean factory as the parent bean factory if this is a child resource.
	 * </ul>
	 *
	 * <p>
	 * Your REST class can also implement a create method called <c>createBeanFactory()</c> to instantiate your own
	 * bean factory.
	 *
	 * <h5 class='figure'>Example:</h5>
	 * <p class='bpcode w800'>
	 * 	<ja>@Rest</ja>
	 * 	<jk>public class</jk> MyRestClass {
	 *
	 * 		<jk>public</jk> BeanFactory createBeanFactory(Optional&lt;BeanFactory&gt; <jv>parentBeanFactory</jv>) <jk>throws</jk> Exception {
	 * 			<jc>// Create your own bean factory here.</jc>
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <p>
	 * The <c>createBeanFactory()</c> method can be static or non-static can contain any of the following arguments:
	 * <ul>
	 * 	<li><c>{@link Optional}&lt;{@link BeanFactory}&gt;</c> - The parent root bean factory if this is a child resource.
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link #REST_beanFactory}
	 * </ul>
	 *
	 * @param resource The REST resource object.
	 * @return The bean factory for this REST resource.
	 * @throws Exception If bean factory could not be instantiated.
	 */
	protected BeanFactory createBeanFactory(Object resource) throws Exception {

		BeanFactory x = null;

		if (resource instanceof BeanFactory)
			x = (BeanFactory)resource;

		if (x == null && parentContext != null)
			x = parentContext.rootBeanFactory;

		if (x == null)
			x = getInstanceProperty(REST_beanFactory, BeanFactory.class, null, x);

		x = BeanFactory
			.of(x, resource)
			.addBean(BeanFactory.class, x)
			.beanCreateMethodFinder(BeanFactory.class, resource)
			.find("createBeanFactory")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the file finder for this REST resource.
	 *
	 * <p>
	 * The file finder is used to retrieve localized files from the classpath.
	 *
	 * <p>
	 * Instantiates based on the following logic:
	 * <ul>
	 * 	<li>Returns the resource class itself is an instance of {@link FileFinder}.
	 * 	<li>Looks for {@link #REST_fileFinder} value set via any of the following:
	 * 		<ul>
	 * 			<li>{@link RestContextBuilder#fileFinder(Class)}/{@link RestContextBuilder#fileFinder(FileFinder)}
	 * 			<li>{@link Rest#fileFinder()}.
	 * 		</ul>
	 * 	<li>Resolves it via the {@link #createBeanFactory(Object) bean factory} registered in this context (including Spring beans if using SpringRestServlet).
	 * 	<li>Looks for value in {@link #REST_fileFinderDefault} setting.
	 * 	<li>Instantiates via {@link #createFileFinderBuilder(Object, BeanFactory)}.
	 * </ul>
	 *
	 * <p>
	 * Your REST class can also implement a create method called <c>createFileFinder()</c> to instantiate your own
	 * file finder.
	 *
	 * <h5 class='figure'>Example:</h5>
	 * <p class='bpcode w800'>
	 * 	<ja>@Rest</ja>
	 * 	<jk>public class</jk> MyRestClass {
	 *
	 * 		<jk>public</jk> FileFinder createFileFinder() <jk>throws</jk> Exception {
	 * 			<jc>// Create your own file finder here.</jc>
	 * 		}
	 * 	}
	 * </p>
	 *
	 * <p>
	 * The <c>createFileFinder()</c> method can be static or non-static can contain any of the following arguments:
	 * <ul>
	 * 	<li>{@link FileFinder} - The file finder that would have been returned by this method.
	 * 	<li>{@link FileFinderBuilder} - The file finder returned by {@link #createFileFinderBuilder(Object,BeanFactory)}.
	 * 	<li>{@link RestContext} - This REST context.
	 * 	<li>{@link BeanFactory} - The bean factory of this REST context.
	 * 	<li>Any {@doc RestInjection injected bean} types.  Use {@link Optional} arguments for beans that may not exist.
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link #REST_fileFinder}
	 * </ul>
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @return The file finder for this REST resource.
	 * @throws Exception If file finder could not be instantiated.
	 */
	protected FileFinder createFileFinder(Object resource, BeanFactory beanFactory) throws Exception {

		FileFinder x = null;

		if (resource instanceof FileFinder)
			x = (FileFinder)resource;

		if (x == null)
			x = getInstanceProperty(REST_fileFinder, FileFinder.class, null, beanFactory);

		if (x == null)
			x = beanFactory.getBean(FileFinder.class).orElse(null);

		if (x == null)
			x = getInstanceProperty(REST_fileFinderDefault, FileFinder.class, null, beanFactory);

		if (x == null)
			x = createFileFinderBuilder(resource, beanFactory).build();

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(FileFinder.class, x)
			.beanCreateMethodFinder(FileFinder.class, resource)
			.find("createFileFinder")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the file finder builder for this REST resource.
	 *
	 * <p>
	 * Allows subclasses to intercept and modify the builder used by the {@link #createFileFinder(Object, BeanFactory)} method.
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @return The file finder builder for this REST resource.
	 * @throws Exception If file finder builder could not be instantiated.
	 */
	protected FileFinderBuilder createFileFinderBuilder(Object resource, BeanFactory beanFactory) throws Exception {

		FileFinderBuilder x = FileFinder
			.create()
			.dir("static")
			.dir("htdocs")
			.cp(getResourceClass(), "htdocs", true)
			.cp(getResourceClass(), "/htdocs", true)
			.caching(1_000_000)
			.exclude("(?i).*\\.(class|properties)");

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(FileFinderBuilder.class, x)
			.beanCreateMethodFinder(FileFinderBuilder.class, resource)
			.find("createFileFinder")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the static files finder for this REST resource.
	 *
	 * <p>
	 * Instantiates based on the following logic:
	 * <ul>
	 * 	<li>Returns the resource class itself is an instance of FileFinder.
	 * 	<li>Looks for {@link #REST_staticFiles} value set via any of the following:
	 * 		<ul>
	 * 			<li>{@link RestContextBuilder#staticFiles(Class)}/{@link RestContextBuilder#staticFiles(StaticFiles)}
	 * 			<li>{@link Rest#staticFiles()}.
	 * 		</ul>
	 * 	<li>Looks for a static or non-static <c>createStaticFiles()</> method that returns {@link StaticFiles} on the
	 * 		resource class with any of the following arguments:
	 * 		<ul>
	 * 			<li>{@link RestContext}
	 * 			<li>{@link BeanFactory}
	 * 			<li>{@link FileFinder}
	 * 			<li>Any {@doc RestInjection injected beans}.
	 * 		</ul>
	 * 	<li>Resolves it via the bean factory registered in this context.
	 * 	<li>Looks for value in {@link #REST_staticFilesDefault} setting.
	 * 	<li>Instantiates a {@link BasicStaticFiles}.
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link #REST_staticFiles}
	 * </ul>
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @return The file finder for this REST resource.
	 * @throws Exception If file finder could not be instantiated.
	 */
	protected StaticFiles createStaticFiles(Object resource, BeanFactory beanFactory) throws Exception {

		StaticFiles x = null;

		if (resource instanceof StaticFiles)
			x = (StaticFiles)resource;

		if (x == null)
			x = getInstanceProperty(REST_staticFiles, StaticFiles.class, null, beanFactory);

		if (x == null)
			x = beanFactory.getBean(StaticFiles.class).orElse(null);

		if (x == null)
			x = getInstanceProperty(REST_staticFilesDefault, StaticFiles.class, null, beanFactory);

		if (x == null)
			x = createStaticFilesBuilder(resource, beanFactory).build();

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(StaticFiles.class, x)
			.beanCreateMethodFinder(StaticFiles.class, resource)
			.find("createStaticFiles")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the static files builder for this REST resource.
	 *
	 * <p>
	 * Allows subclasses to intercept and modify the builder used by the {@link #createStaticFiles(Object, BeanFactory)} method.
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @return The static files builder for this REST resource.
	 * @throws Exception If static files builder could not be instantiated.
	 */
	protected StaticFilesBuilder createStaticFilesBuilder(Object resource, BeanFactory beanFactory) throws Exception {

		StaticFilesBuilder x = StaticFiles
			.create()
			.dir("static")
			.dir("htdocs")
			.cp(getResourceClass(), "htdocs", true)
			.cp(getResourceClass(), "/htdocs", true)
			.caching(1_000_000)
			.exclude("(?i).*\\.(class|properties)")
			.headers(CacheControl.of("max-age=86400, public"));

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(StaticFilesBuilder.class, x)
			.beanCreateMethodFinder(StaticFilesBuilder.class, resource)
			.find("createStaticFiles")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the call logger this REST resource.
	 *
	 * <p>
	 * Instantiates based on the following logic:
	 * <ul>
	 * 	<li>Returns the resource class itself is an instance of RestLogger.
	 * 	<li>Looks for {@link #REST_callLogger} value set via any of the following:
	 * 		<ul>
	 * 			<li>{@link RestContextBuilder#callLogger(Class)}/{@link RestContextBuilder#callLogger(RestLogger)}
	 * 			<li>{@link Rest#callLogger()}.
	 * 		</ul>
	 * 	<li>Looks for a static or non-static <c>createCallLogger()</> method that returns {@link RestLogger} on the
	 * 		resource class with any of the following arguments:
	 * 		<ul>
	 * 			<li>{@link RestContext}
	 * 			<li>{@link BeanFactory}
	 * 			<li>{@link FileFinder}
	 * 			<li>Any {@doc RestInjection injected beans}.
	 * 		</ul>
	 * 	<li>Resolves it via the bean factory registered in this context.
	 * 	<li>Looks for value in {@link #REST_callLoggerDefault} setting.
	 * 	<li>Instantiates a {@link BasicFileFinder}.
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link #REST_callLogger}
	 * </ul>
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @return The file finder for this REST resource.
	 * @throws Exception If file finder could not be instantiated.
	 */
	protected RestLogger createCallLogger(Object resource, BeanFactory beanFactory) throws Exception {

		RestLogger x = null;

		if (resource instanceof RestLogger)
			x = (RestLogger)resource;

		if (x == null)
			x = getInstanceProperty(REST_callLogger, RestLogger.class, null, beanFactory);

		if (x == null)
			x = beanFactory.getBean(RestLogger.class).orElse(null);

		if (x == null)
			x = getInstanceProperty(REST_callLoggerDefault, RestLogger.class, null, beanFactory);

		if (x == null)
			x = createCallLoggerBuilder(resource, beanFactory).build();

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(RestLogger.class, x)
			.beanCreateMethodFinder(RestLogger.class, resource)
			.find("createCallLogger")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the call logger builder for this REST resource.
	 *
	 * <p>
	 * Allows subclasses to intercept and modify the builder used by the {@link #createCallLogger(Object, BeanFactory)} method.
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @return The call logger builder for this REST resource.
	 * @throws Exception If call logger builder could not be instantiated.
	 */
	protected RestLoggerBuilder createCallLoggerBuilder(Object resource, BeanFactory beanFactory) throws Exception {

		RestLoggerBuilder x = RestLogger
			.create()
			.normalRules(  // Rules when debugging is not enabled.
				RestLogger.createRule()  // Log 500+ errors with status-line and header information.
					.statusFilter(a -> a >= 500)
					.level(SEVERE)
					.requestDetail(HEADER)
					.responseDetail(HEADER)
					.build(),
				RestLogger.createRule()  // Log 400-500 errors with just status-line information.
					.statusFilter(a -> a >= 400)
					.level(WARNING)
					.requestDetail(STATUS_LINE)
					.responseDetail(STATUS_LINE)
					.build()
			)
			.debugRules(  // Rules when debugging is enabled.
				RestLogger.createRule()  // Log everything with full details.
					.level(SEVERE)
					.requestDetail(ENTITY)
					.responseDetail(ENTITY)
					.build()
			)
			.logger(getLogger())
			.stackTraceStore(getStackTraceStore());

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(RestLoggerBuilder.class, x)
			.beanCreateMethodFinder(RestLoggerBuilder.class, resource)
			.find("createCallLogger")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the response handlers for this REST resource.
	 *
	 * <p>
	 * Instantiates based on the following logic:
	 * <ul>
	 * 	<li>Looks for {@link #REST_responseHandlers} value set via any of the following:
	 * 		<ul>
	 * 			<li>{@link RestContextBuilder#responseHandlers(Class...)}/{@link RestContextBuilder#responseHandlers(ResponseHandler...)}
	 * 			<li>{@link Rest#responseHandlers()}.
	 * 		</ul>
	 * 	<li>Looks for a static or non-static <c>createResponseHandlers()</> method that returns <c>{@link ResponseHandler}[]</c> on the
	 * 		resource class with any of the following arguments:
	 * 		<ul>
	 * 			<li>{@link RestContext}
	 * 			<li>{@link BeanFactory}
	 * 			<li>Any {@doc RestInjection injected beans}.
	 * 		</ul>
	 * 	<li>Resolves it via the bean factory registered in this context.
	 * 	<li>Instantiates a <c>ResponseHandler[0]</c>.
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link #REST_responseHandlers}
	 * </ul>
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @return The response handlers for this REST resource.
	 * @throws Exception If response handlers could not be instantiated.
	 */
	protected ResponseHandlerList createResponseHandlers(Object resource, BeanFactory beanFactory) throws Exception {

		ResponseHandlerList x = ResponseHandlerList.create();

		x.append(getInstanceArrayProperty(REST_responseHandlers, ResponseHandler.class, new ResponseHandler[0], beanFactory));

		if (x.isEmpty())
			x.append(beanFactory.getBean(ResponseHandlerList.class).orElse(null));

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(ResponseHandlerList.class, x)
			.beanCreateMethodFinder(ResponseHandlerList.class, resource)
			.find("createResponseHandlers")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the serializers for this REST resource.
	 *
	 * <p>
	 * Instantiates based on the following logic:
	 * <ul>
	 * 	<li>Looks for {@link #REST_serializers} value set via any of the following:
	 * 		<ul>
	 * 			<li>{@link RestContextBuilder#serializers(Class...)}/{@link RestContextBuilder#serializers(Serializer...)}
	 * 			<li>{@link Rest#serializers()}.
	 * 		</ul>
	 * 	<li>Looks for a static or non-static <c>createSerializers()</> method that returns <c>{@link Serializer}[]</c> on the
	 * 		resource class with any of the following arguments:
	 * 		<ul>
	 * 			<li>{@link RestContext}
	 * 			<li>{@link BeanFactory}
	 * 			<li>Any {@doc RestInjection injected beans}.
	 * 		</ul>
	 * 	<li>Resolves it via the bean factory registered in this context.
	 * 	<li>Instantiates a <c>Serializer[0]</c>.
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link #REST_serializers}
	 * </ul>
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @param ps The property store to apply to all serialiers.
	 * @return The serializers for this REST resource.
	 * @throws Exception If serializers could not be instantiated.
	 */
	protected SerializerGroup createSerializers(Object resource, BeanFactory beanFactory, PropertyStore ps) throws Exception {

		SerializerGroup g = beanFactory.getBean(SerializerGroup.class).orElse(null);

		if (g == null) {
			Object[] x = getArrayProperty(REST_serializers, Object.class);

			if (x == null)
				x = beanFactory.getBean(Serializer[].class).orElse(null);

			if (x == null)
				x = new Serializer[0];

			g = SerializerGroup
				.create()
				.append(x)
				.apply(ps)
				.build();
		}

		g = BeanFactory
			.of(beanFactory, resource)
			.addBean(SerializerGroup.class, g)
			.beanCreateMethodFinder(SerializerGroup.class, resource)
			.find("createSerializers")
			.withDefault(g)
			.run();

		return g;
	}

	/**
	 * Instantiates the parsers for this REST resource.
	 *
	 * <p>
	 * Instantiates based on the following logic:
	 * <ul>
	 * 	<li>Looks for {@link #REST_parsers} value set via any of the following:
	 * 		<ul>
	 * 			<li>{@link RestContextBuilder#parsers(Class...)}/{@link RestContextBuilder#parsers(Parser...)}
	 * 			<li>{@link Rest#parsers()}.
	 * 		</ul>
	 * 	<li>Looks for a static or non-static <c>createParsers()</> method that returns <c>{@link Parser}[]</c> on the
	 * 		resource class with any of the following arguments:
	 * 		<ul>
	 * 			<li>{@link RestContext}
	 * 			<li>{@link BeanFactory}
	 * 			<li>Any {@doc RestInjection injected beans}.
	 * 		</ul>
	 * 	<li>Resolves it via the bean factory registered in this context.
	 * 	<li>Instantiates a <c>Parser[0]</c>.
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link #REST_parsers}
	 * </ul>
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @param ps The property store to apply to all serialiers.
	 * @return The parsers for this REST resource.
	 * @throws Exception If parsers could not be instantiated.
	 */
	protected ParserGroup createParsers(Object resource, BeanFactory beanFactory, PropertyStore ps) throws Exception {

		ParserGroup g = beanFactory.getBean(ParserGroup.class).orElse(null);

		if (g == null) {
			Object[] x = getArrayProperty(REST_parsers, Object.class);

			if (x == null)
				x = beanFactory.getBean(Parser[].class).orElse(null);

			if (x == null)
				x = new Parser[0];

			g = ParserGroup
				.create()
				.append(x)
				.apply(ps)
				.build();
		}

		g = BeanFactory
			.of(beanFactory, resource)
			.addBean(ParserGroup.class, g)
			.beanCreateMethodFinder(ParserGroup.class, resource)
			.find("createParsers")
			.withDefault(g)
			.run();

		return g;
	}

	/**
	 * Instantiates the HTTP part serializer for this REST resource.
	 *
	 * <p>
	 * Instantiates based on the following logic:
	 * <ul>
	 * 	<li>Returns the resource class itself is an instance of {@link HttpPartSerializer}.
	 * 	<li>Looks for {@link #REST_partSerializer} value set via any of the following:
	 * 		<ul>
	 * 			<li>{@link RestContextBuilder#partSerializer(Class)}/{@link RestContextBuilder#partSerializer(HttpPartSerializer)}
	 * 			<li>{@link Rest#partSerializer()}.
	 * 		</ul>
	 * 	<li>Looks for a static or non-static <c>createPartSerializer()</> method that returns <c>{@link HttpPartSerializer}</c> on the
	 * 		resource class with any of the following arguments:
	 * 		<ul>
	 * 			<li>{@link RestContext}
	 * 			<li>{@link BeanFactory}
	 * 			<li>Any {@doc RestInjection injected beans}.
	 * 		</ul>
	 * 	<li>Resolves it via the bean factory registered in this context.
	 * 	<li>Instantiates an {@link OpenApiSerializer}.
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link #REST_partSerializer}
	 * </ul>
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @return The HTTP part serializer for this REST resource.
	 * @throws Exception If serializer could not be instantiated.
	 */
	protected HttpPartSerializer createPartSerializer(Object resource, BeanFactory beanFactory) throws Exception {

		HttpPartSerializer x = null;

		if (resource instanceof HttpPartSerializer)
			x = (HttpPartSerializer)resource;

		if (x == null)
			x = getInstanceProperty(REST_partSerializer, HttpPartSerializer.class, null, beanFactory);

		if (x == null)
			x = beanFactory.getBean(HttpPartSerializer.class).orElse(null);

		if (x == null)
			x = new OpenApiSerializer(getPropertyStore());

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(HttpPartSerializer.class, x)
			.beanCreateMethodFinder(HttpPartSerializer.class, resource)
			.find("createPartSerializer")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the HTTP part parser for this REST resource.
	 *
	 * <p>
	 * Instantiates based on the following logic:
	 * <ul>
	 * 	<li>Returns the resource class itself is an instance of {@link HttpPartParser}.
	 * 	<li>Looks for {@link #REST_partParser} value set via any of the following:
	 * 		<ul>
	 * 			<li>{@link RestContextBuilder#partParser(Class)}/{@link RestContextBuilder#partParser(HttpPartParser)}
	 * 			<li>{@link Rest#partParser()}.
	 * 		</ul>
	 * 	<li>Looks for a static or non-static <c>createPartParser()</> method that returns <c>{@link HttpPartParser}</c> on the
	 * 		resource class with any of the following arguments:
	 * 		<ul>
	 * 			<li>{@link RestContext}
	 * 			<li>{@link BeanFactory}
	 * 			<li>Any {@doc RestInjection injected beans}.
	 * 		</ul>
	 * 	<li>Resolves it via the bean factory registered in this context.
	 * 	<li>Instantiates an {@link OpenApiSerializer}.
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link #REST_partParser}
	 * </ul>
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @return The HTTP part parser for this REST resource.
	 * @throws Exception If parser could not be instantiated.
	 */
	protected HttpPartParser createPartParser(Object resource, BeanFactory beanFactory) throws Exception {

		HttpPartParser x = null;

		if (resource instanceof HttpPartParser)
			x = (HttpPartParser)resource;

		if (x == null)
			x = getInstanceProperty(REST_partParser, HttpPartParser.class, null, beanFactory);

		if (x == null)
			x = beanFactory.getBean(HttpPartParser.class).orElse(null);

		if (x == null)
			x = new OpenApiParser(getPropertyStore());

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(HttpPartParser.class, x)
			.beanCreateMethodFinder(HttpPartParser.class, resource)
			.find("createPartParser")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the REST method parameter resolvers for this REST resource.
	 *
	 * <p>
	 * Instantiates based on the following logic:
	 * <ul>
	 * 	<li>Looks for {@link #REST_restParams} value set via any of the following:
	 * 		<ul>
	 * 			<li>{@link RestContextBuilder#restParams(Class...)}/{@link RestContextBuilder#restParams(Class...)}
	 * 			<li>{@link Rest#restParams()}.
	 * 		</ul>
	 * 	<li>Looks for a static or non-static <c>createRestParams()</> method that returns <c>{@link Class}[]</c>.
	 * 	<li>Resolves it via the bean factory registered in this context.
	 * 	<li>Instantiates a default set of parameters.
	 * </ul>
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @return The REST method parameter resolvers for this REST resource.
	 * @throws Exception If parameter resolvers could not be instantiated.
	 */
	@SuppressWarnings("unchecked")
	protected RestParamList createRestParams(Object resource, BeanFactory beanFactory) throws Exception {

		RestParamList x = RestParamList.create();

		for (Class<?> c : getListProperty(REST_restParams, Class.class, AList.create()))
			x.append((Class<? extends RestParam>)c);

		x.append(
			AttributeParam.class,
			BodyParam.class,
			ConfigParam.class,
			FormDataParam.class,
			HasFormDataParam.class,
			HasQueryParam.class,
			HeaderParam.class,
			HttpServletRequestParam.class,
			HttpServletResponseParam.class,
			InputStreamParam.class,
			InputStreamParserParam.class,
			LocaleParam.class,
			MessagesParam.class,
			MethodParam.class,
			OutputStreamParam.class,
			ParserParam.class,
			PathParam.class,
			QueryParam.class,
			ReaderParam.class,
			ReaderParserParam.class,
			RequestAttributesParam.class,
			RequestBeanParam.class,
			RequestBodyParam.class,
			RequestFormDataParam.class,
			RequestHeadersParam.class,
			RequestPathParam.class,
			RequestQueryParam.class,
			ResourceBundleParam.class,
			ResponseBeanParam.class,
			ResponseHeaderParam.class,
			ResponseStatusParam.class,
			RestContextParam.class,
			RestRequestParam.class,
			ServetInputStreamParam.class,
			ServletOutputStreamParam.class,
			SwaggerParam.class,
			TimeZoneParam.class,
			UriContextParam.class,
			UriResolverParam.class,
			WriterParam.class,
			DefaultParam.class
		);

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(RestParamList.class, x)
			.beanCreateMethodFinder(RestParamList.class, resource)
			.find("createRestParams")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the hook method parameter resolvers for this REST resource.
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @return The REST method parameter resolvers for this REST resource.
	 * @throws Exception If parameter resolvers could not be instantiated.
	 */
	@SuppressWarnings("unchecked")
	protected RestParamList createHookMethodParams(Object resource, BeanFactory beanFactory) throws Exception {

		RestParamList x = RestParamList.create();

		x.append(
			ConfigParam.class,
			HeaderParam.class,
			HttpServletRequestParam.class,
			HttpServletResponseParam.class,
			InputStreamParam.class,
			LocaleParam.class,
			MessagesParam.class,
			MethodParam.class,
			OutputStreamParam.class,
			ReaderParam.class,
			ResourceBundleParam.class,
			RestContextParam.class,
			RestRequestParam.class,
			ServetInputStreamParam.class,
			ServletOutputStreamParam.class,
			TimeZoneParam.class,
			WriterParam.class,
			DefaultParam.class
		);

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(RestParamList.class, x)
			.beanCreateMethodFinder(RestParamList.class, resource)
			.find("createHookMethodParams")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates logger for this REST resource.
	 *
	 * <p>
	 * Instantiates based on the following logic:
	 * <ul>
	 * 	<li>Looks for a static or non-static <c>createLogger()</> method that returns <c>{@link Logger}</c> on the
	 * 		resource class with any of the following arguments:
	 * 		<ul>
	 * 			<li>{@link RestContext}
	 * 			<li>{@link BeanFactory}
	 * 			<li>Any {@doc RestInjection injected beans}.
	 * 		</ul>
	 * 	<li>Resolves it via the bean factory registered in this context.
	 * 	<li>Instantiates via <c>Logger.<jsm>getLogger</jsm>(<jv>resource</jv>.getClass().getName())</c>.
	 * </ul>
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @return The logger for this REST resource.
	 * @throws Exception If logger could not be instantiated.
	 */
	protected Logger createLogger(Object resource, BeanFactory beanFactory) throws Exception {

		Logger x = beanFactory.getBean(Logger.class).orElse(null);

		if (x == null)
			x = Logger.getLogger(resource.getClass().getName());

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(Logger.class, x)
			.beanCreateMethodFinder(Logger.class, resource)
			.find("createLogger")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the JSON schema generator for this REST resource.
	 *
	 * <p>
	 * Instantiates based on the following logic:
	 * <ul>
	 * 	<li>Looks for a static or non-static <c>createJsonSchemaGenerator()</> method that returns <c>{@link JsonSchemaGenerator}</c> on the
	 * 		resource class with any of the following arguments:
	 * 		<ul>
	 * 			<li>{@link RestContext}
	 * 			<li>{@link BeanFactory}
	 * 			<li>Any {@doc RestInjection injected beans}.
	 * 		</ul>
	 * 	<li>Resolves it via the bean factory registered in this context.
	 * 	<li>Instantiates a new {@link JsonSchemaGenerator} using the property store of this context..
	 * </ul>
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @return The JSON schema generator for this REST resource.
	 * @throws Exception If JSON schema generator could not be instantiated.
	 */
	protected JsonSchemaGenerator createJsonSchemaGenerator(Object resource, BeanFactory beanFactory) throws Exception {
		JsonSchemaGenerator x = beanFactory.getBean(JsonSchemaGenerator.class).orElse(null);

		if (x == null)
			x = createJsonSchemaGeneratorBuilder(resource, beanFactory).build();

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(JsonSchemaGenerator.class, x)
			.beanCreateMethodFinder(JsonSchemaGenerator.class, resource)
			.find("createJsonSchemaGenerator")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the JSON-schema generator builder for this REST resource.
	 *
	 * <p>
	 * Allows subclasses to intercept and modify the builder used by the {@link #createJsonSchemaGenerator(Object, BeanFactory)} method.
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @return The JSON-schema generator builder for this REST resource.
	 * @throws Exception If JSON-schema generator builder could not be instantiated.
	 */
	protected JsonSchemaGeneratorBuilder createJsonSchemaGeneratorBuilder(Object resource, BeanFactory beanFactory) throws Exception {
		JsonSchemaGeneratorBuilder x = JsonSchemaGenerator
			.create()
			.apply(getPropertyStore());

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(JsonSchemaGeneratorBuilder.class, x)
			.beanCreateMethodFinder(JsonSchemaGeneratorBuilder.class, resource)
			.find("createJsonSchemaGenerator")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the REST info provider for this REST resource.
	 *
	 * <p>
	 * Instantiates based on the following logic:
	 * <ul>
	 * 	<li>Returns the resource class itself is an instance of {@link SwaggerProvider}.
	 * 	<li>Looks for {@link #REST_swaggerProvider} value set via any of the following:
	 * 		<ul>
	 * 			<li>{@link RestContextBuilder#swaggerProvider(Class)}/{@link RestContextBuilder#swaggerProvider(SwaggerProvider)}
	 * 			<li>{@link Rest#swaggerProvider()}.
	 * 		</ul>
	 * 	<li>Looks for a static or non-static <c>createSwaggerProvider()</> method that returns {@link SwaggerProvider} on the
	 * 		resource class with any of the following arguments:
	 * 		<ul>
	 * 			<li>{@link RestContext}
	 * 			<li>{@link BeanFactory}
	 * 			<li>Any {@doc RestInjection injected beans}.
	 * 		</ul>
	 * 	<li>Resolves it via the bean factory registered in this context.
	 * 	<li>Instantiates a default {@link SwaggerProvider}.
	 * </ul>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link #REST_swaggerProvider}
	 * </ul>
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @return The info provider for this REST resource.
	 * @throws Exception If info provider could not be instantiated.
	 */
	protected SwaggerProvider createSwaggerProvider(Object resource, BeanFactory beanFactory) throws Exception {
		SwaggerProvider x = beanFactory.getBean(SwaggerProvider.class).orElse(null);

		Object o = getProperty(REST_swaggerProvider);
		if (o instanceof SwaggerProvider)
			x = (SwaggerProvider)o;

		if (x == null)
			x = createSwaggerProviderBuilder(resource, beanFactory).build();

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(SwaggerProvider.class, x)
			.beanCreateMethodFinder(SwaggerProvider.class, resource)
			.find("createSwaggerProvider")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the REST API builder for this REST resource.
	 *
	 * <p>
	 * Allows subclasses to intercept and modify the builder used by the {@link #createSwaggerProvider(Object, BeanFactory)} method.
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @return The REST API builder for this REST resource.
	 * @throws Exception If REST API builder could not be instantiated.
	 */
	@SuppressWarnings("unchecked")
	protected SwaggerProviderBuilder createSwaggerProviderBuilder(Object resource, BeanFactory beanFactory) throws Exception {

		Class<? extends SwaggerProvider> c = null;
		Object o = getProperty(REST_swaggerProvider);
		if (o instanceof Class)
			c = (Class<? extends SwaggerProvider>)o;

		SwaggerProviderBuilder x = SwaggerProvider
				.create()
				.beanFactory(beanFactory)
				.fileFinder(getFileFinder())
				.messages(getMessages())
				.varResolver(getVarResolver())
				.jsonSchemaGenerator(createJsonSchemaGenerator(resource, beanFactory))
				.implClass(c);

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(SwaggerProviderBuilder.class, x)
			.beanCreateMethodFinder(SwaggerProviderBuilder.class, resource)
			.find("createSwaggerProvider")
			.withDefault(x)
			.run();

		return x;

	}

	/**
	 * Instantiates the variable resolver for this REST resource.
	 *
	 * <p>
	 * Instantiates based on the following logic:
	 * <ul>
	 * 	<li>Looks for a static or non-static <c>createVarResolver()</> method that returns <c>{@link VarResolver}</c> on the
	 * 		resource class with any of the following arguments:
	 * 		<ul>
	 * 			<li>{@link RestContext}
	 * 			<li>{@link BeanFactory}
	 * 			<li>Any {@doc RestInjection injected beans}.
	 * 		</ul>
	 * 	<li>Resolves it via the bean factory registered in this context.
	 * 	<li>Instantiates a new {@link VarResolver} using the variables returned by {@link #createVars(Object,BeanFactory)}.
	 * </ul>
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @return The variable resolver for this REST resource.
	 * @throws Exception If variable resolver could not be instantiated.
	 */
	protected VarResolver createVarResolver(Object resource, BeanFactory beanFactory) throws Exception {

		VarResolver x = beanFactory.getBean(VarResolver.class).orElse(null);

		if (x == null)
			x = builder.varResolverBuilder
				.vars(createVars(resource,beanFactory))
				.contextObject("messages", getMessages())
				.build();

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(VarResolver.class, x)
			.beanCreateMethodFinder(VarResolver.class, resource)
			.find("createVarResolver")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the variable resolver variables for this REST resource.
	 *
	 * <p>
	 * Instantiates based on the following logic:
	 * <ul>
	 * 	<li>Looks for a static or non-static <c>createVars()</> method that returns <c>{@link VarList}</c> on the
	 * 		resource class with any of the following arguments:
	 * 		<ul>
	 * 			<li>{@link RestContext}
	 * 			<li>{@link BeanFactory}
	 * 			<li>Any {@doc RestInjection injected beans}.
	 * 		</ul>
	 * 	<li>Resolves it via the bean factory registered in this context.
	 * 	<li>Instantiates a new {@link VarList} using default variables.
	 * </ul>
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @return The variable resolver variables for this REST resource.
	 * @throws Exception If variable resolver variables could not be instantiated.
	 */
	@SuppressWarnings("unchecked")
	protected VarList createVars(Object resource, BeanFactory beanFactory) throws Exception {

		VarList x = beanFactory.getBean(VarList.class).orElse(null);

		if (x == null)
			x = VarList.of(
				FileVar.class,
				LocalizationVar.class,
				RequestAttributeVar.class,
				RequestFormDataVar.class,
				RequestHeaderVar.class,
				RequestPathVar.class,
				RequestQueryVar.class,
				RequestVar.class,
				RequestSwaggerVar.class,
				SerializedRequestAttrVar.class,
				ServletInitParamVar.class,
				SwaggerVar.class,
				UrlVar.class,
				UrlEncodeVar.class,
				HtmlWidgetVar.class
			);

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(VarList.class, x)
			.beanCreateMethodFinder(VarList.class, resource)
			.find("createVars")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the stack trace store for this REST resource.
	 *
	 * <p>
	 * Instantiates based on the following logic:
	 * <ul>
	 * 	<li>Looks for a static or non-static <c>createStackTraceStore()</> method that returns <c>{@link StackTraceStore}</c> on the
	 * 		resource class with any of the following arguments:
	 * 		<ul>
	 * 			<li>{@link RestContext}
	 * 			<li>{@link BeanFactory}
	 * 			<li>Any {@doc RestInjection injected beans}.
	 * 		</ul>
	 * 	<li>Resolves it via the bean factory registered in this context.
	 * 	<li>Returns {@link StackTraceStore#GLOBAL}.
	 * </ul>
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @return The stack trace store for this REST resource.
	 * @throws Exception If stack trace store could not be instantiated.
	 */
	protected StackTraceStore createStackTraceStore(Object resource, BeanFactory beanFactory) throws Exception {

		StackTraceStore x = beanFactory.getBean(StackTraceStore.class).orElse(null);

		if (x == null)
			x = StackTraceStore.GLOBAL;

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(StackTraceStore.class, x)
			.beanCreateMethodFinder(StackTraceStore.class, resource)
			.find("createStackTraceStore")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the default request headers for this REST object.
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @return The default request headers for this REST object.
	 * @throws Exception If stack trace store could not be instantiated.
	 */
	protected HeaderList createDefaultRequestHeaders(Object resource, BeanFactory beanFactory) throws Exception {

		HeaderList x = HeaderList.create();

		x.appendUnique(getInstanceArrayProperty(REST_defaultRequestHeaders, org.apache.http.Header.class, new org.apache.http.Header[0], beanFactory));

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(HeaderList.class, x)
			.beanCreateMethodFinder(HeaderList.class, resource)
			.find("createDefaultRequestHeaders")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the default response headers for this REST object.
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @return The default response headers for this REST object.
	 * @throws Exception If stack trace store could not be instantiated.
	 */
	protected HeaderList createDefaultResponseHeaders(Object resource, BeanFactory beanFactory) throws Exception {

		HeaderList x = HeaderList.create();

		x.appendUnique(getInstanceArrayProperty(REST_defaultResponseHeaders, org.apache.http.Header.class, new org.apache.http.Header[0], beanFactory));

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(HeaderList.class, x)
			.beanCreateMethodFinder(HeaderList.class, resource)
			.find("createDefaultResponseHeaders")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the default response headers for this REST object.
	 *
	 * @param resource The REST resource object.
	 * @param beanFactory The bean factory to use for retrieving and creating beans.
	 * @return The default response headers for this REST object.
	 * @throws Exception If stack trace store could not be instantiated.
	 */
	protected NamedAttributeList createDefaultRequestAttributes(Object resource, BeanFactory beanFactory) throws Exception {

		NamedAttributeList x = NamedAttributeList.create();

		x.appendUnique(getInstanceArrayProperty(REST_defaultRequestAttributes, NamedAttribute.class, new NamedAttribute[0], beanFactory));

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(NamedAttributeList.class, x)
			.beanCreateMethodFinder(NamedAttributeList.class, resource)
			.find("createDefaultRequestAttributes")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the debug enablement map builder for this REST object.
	 *
	 * @param resource The REST resource object.
	 * @return The default response headers for this REST object.
	 */
	protected DebugEnablementMapBuilder createDebugEnablement(Object resource) {

		ClassInfo rci = ClassInfo.ofProxy(resource);

		DebugEnablementMapBuilder deb = DebugEnablementMap.create();
		for (String s : split(getStringProperty(REST_debugOn, ""))) {
			s = s.trim();
			if (! s.isEmpty()) {
				int i = s.indexOf('=');
				if (i == -1)
					deb.append(s.trim(), Enablement.ALWAYS);
				else
					deb.append(s.substring(0, i).trim(), Enablement.fromString(s.substring(i+1).trim()));
			}
		}

		Enablement defaultDebug = getInstanceProperty(REST_debugDefault, Enablement.class, null);
		if (defaultDebug == null)
			defaultDebug = isDebug() ? Enablement.ALWAYS : Enablement.NEVER;

		Enablement de = getInstanceProperty(REST_debug, Enablement.class, defaultDebug);
		if (de != null)
			deb.append(rci.getFullName(), de);
		for (MethodInfo mi : rci.getPublicMethods())
			for (RestMethod a : mi.getAnnotations(RestMethod.class))
				if (a != null && ! a.debug().isEmpty())
					deb.append(mi.getFullName(), Enablement.fromString(a.debug()));

		return deb;
	}

	/**
	 * Instantiates the messages for this REST object.
	 *
	 * @param resource The REST resource object.
	 * @return The messages for this REST object.
	 * @throws Exception An error occurred.
	 */
	protected Messages createMessages(Object resource) throws Exception {

		Messages x = createMessagesBuilder(resource).build();

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(Messages.class, x)
			.beanCreateMethodFinder(Messages.class, resource)
			.find("createMessages")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the Messages builder for this REST resource.
	 *
	 * <p>
	 * Allows subclasses to intercept and modify the builder used by the {@link #createMessages(Object)} method.
	 *
	 * @param resource The REST resource object.
	 * @return The messages builder for this REST resource.
	 * @throws Exception If messages builder could not be instantiated.
	 */
	protected MessagesBuilder createMessagesBuilder(Object resource) throws Exception {

		Tuple2<Class<?>,String>[] mbl = getInstanceArrayProperty(REST_messages, Tuple2.class);
		MessagesBuilder x = null;

		for (int i = mbl.length-1; i >= 0; i--) {
			Class<?> c = firstNonNull(mbl[i].getA(), resource.getClass());
			String value = mbl[i].getB();
			if (isJsonObject(value,true)) {
				MessagesString ms = SimpleJson.DEFAULT.read(value, MessagesString.class);
				x = Messages.create(c).name(ms.name).baseNames(split(ms.baseNames, ',')).locale(ms.locale).parent(x == null ? null : x.build());
			} else {
				x = Messages.create(c).name(value).parent(x == null ? null : x.build());
			}
		}

		if (x == null)
			x = Messages.create(resource.getClass());

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(MessagesBuilder.class, x)
			.beanCreateMethodFinder(MessagesBuilder.class, resource)
			.find("createMessages")
			.withDefault(x)
			.run();

		return x;
	}

	private static class MessagesString {
		public String name;
		public String[] baseNames;
		public String locale;
	}

	/**
	 * Creates the set of {@link RestMethodContext} objects that represent the methods on this resource.
	 *
	 * @param resource The REST resource object.
	 * @return The builder for the {@link RestMethods} object.
	 * @throws Exception An error occurred.
	 */
	protected RestMethods createRestMethods(Object resource) throws Exception {

		RestMethods x = createRestMethodsBuilder(resource).build();

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(RestMethods.class, x)
			.beanCreateMethodFinder(RestMethods.class, resource)
			.find("createRestMethods")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the REST methods builder for this REST resource.
	 *
	 * <p>
	 * Allows subclasses to intercept and modify the builder used by the {@link #createRestMethods(Object)} method.
	 *
	 * @param resource The REST resource object.
	 * @return The REST methods builder for this REST resource.
	 * @throws Exception If REST methods builder could not be instantiated.
	 */
	protected RestMethodsBuilder createRestMethodsBuilder(Object resource) throws Exception {

		RestMethodsBuilder x = RestMethods
			.create()
			.beanFactory(rootBeanFactory)
			.implClass(getClassProperty(REST_restMethodsClass, RestMethods.class));

		ClassInfo rci = ClassInfo.of(resource);

		for (MethodInfo mi : rci.getPublicMethods()) {
			RestMethod a = mi.getLastAnnotation(RestMethod.class);

			// Also include methods on @Rest-annotated interfaces.
			if (a == null) {
				for (Method mi2 : mi.getMatching()) {
					Class<?> ci2 = mi2.getDeclaringClass();
					if (ci2.isInterface() && ci2.getAnnotation(Rest.class) != null) {
						a = RestMethodAnnotation.DEFAULT;
					}
				}
			}
			if (a != null) {
				try {
					if (mi.isNotPublic())
						throw new RestServletException("@RestMethod method {0}.{1} must be defined as public.", rci.inner().getName(), mi.getSimpleName());

					RestMethodContext rmc = RestMethodContext
						.create(mi.inner(), this)
						.beanFactory(rootBeanFactory)
						.implClass(getClassProperty(REST_methodContextClass, RestMethodContext.class))
						.build();

					String httpMethod = rmc.getHttpMethod();

					// RRPC is a special case where a method returns an interface that we
					// can perform REST calls against.
					// We override the CallMethod.invoke() method to insert our logic.
					if ("RRPC".equals(httpMethod)) {

						RestMethodContext smb = RestMethodContext
							.create(mi.inner(), this)
							.dotAll()
							.beanFactory(rootBeanFactory)
							.implClass(RrpcRestMethodContext.class)
							.build();
						x
							.add("GET", smb)
							.add("POST", smb);

					} else {
						x.add(rmc);
					}
				} catch (Throwable e) {
					throw new RestServletException(e, "Problem occurred trying to initialize methods on class {0}", rci.inner().getName());
				}
			}
		}

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(RestMethodsBuilder.class, x)
			.beanCreateMethodFinder(RestMethodsBuilder.class, resource)
			.find("createRestMethods")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Creates the builder for the children of this resource.
	 *
	 * @param resource The REST resource object.
	 * @return The builder for the {@link RestChildren} object.
	 * @throws Exception An error occurred.
	 */
	protected RestChildren createRestChildren(Object resource) throws Exception {

		RestChildren x = createRestChildrenBuilder(resource).build();

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(RestChildren.class, x)
			.beanCreateMethodFinder(RestChildren.class, resource)
			.find("createRestChildren")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the REST children builder for this REST resource.
	 *
	 * <p>
	 * Allows subclasses to intercept and modify the builder used by the {@link #createRestChildren(Object)} method.
	 *
	 * @param resource The REST resource object.
	 * @return The REST children builder for this REST resource.
	 * @throws Exception If REST children builder could not be instantiated.
	 */
	protected RestChildrenBuilder createRestChildrenBuilder(Object resource) throws Exception {

		RestChildrenBuilder x = RestChildren
			.create()
			.beanFactory(rootBeanFactory)
			.implClass(getClassProperty(REST_restChildrenClass, RestChildren.class));

		// Initialize our child resources.
		for (Object o : getArrayProperty(REST_children, Object.class)) {
			String path = null;

			if (o instanceof RestChild) {
				RestChild rc = (RestChild)o;
				path = rc.path;
				o = rc.resource;
			}

			RestContextBuilder cb = null;

			if (o instanceof Class) {
				Class<?> oc = (Class<?>)o;
				// Don't allow specifying yourself as a child.  Causes an infinite loop.
				if (oc == builder.resourceClass)
					continue;
				cb = RestContext.create(this, builder.inner, oc, null);
				BeanFactory bf = BeanFactory.of(beanFactory, resource).addBean(RestContextBuilder.class, cb);
				if (bf.getBean(oc).isPresent()) {
					o = (Supplier<?>)()->bf.getBean(oc).get();  // If we resolved via injection, always get it this way.
				} else {
					o = bf.createBean(oc);
				}
			} else {
				cb = RestContext.create(this, builder.inner, o.getClass(), o);
			}

			if (path != null)
				cb.path(path);

			RestContext cc = cb.init(o).build();

			MethodInfo mi = ClassInfo.of(o).getMethod("setContext", RestContext.class);
			if (mi != null)
				mi.accessible().invoke(o, cc);

			x.add(cc);
		}

		x = BeanFactory
			.of(beanFactory, resource)
			.addBean(RestChildrenBuilder.class, x)
			.beanCreateMethodFinder(RestChildrenBuilder.class, resource)
			.find("createRestChildren")
			.withDefault(x)
			.run();

		return x;
	}

	/**
	 * Instantiates the list of {@link HookEvent#START_CALL} methods.
	 *
	 * @param resource The REST resource object.
	 * @return The default response headers for this REST object.
	 */
	protected List<Method> createStartCallMethods(Object resource) {
		Map<String,Method> x = AMap.create();

		for (MethodInfo m : ClassInfo.ofProxy(resource).getAllMethodsParentFirst())
			for (RestHook h : m.getAnnotations(RestHook.class))
				if (h.value() == HookEvent.START_CALL)
					x.put(m.getSignature(), m.accessible().inner());

		return AList.of(x.values());
	}

	/**
	 * Instantiates the list of {@link HookEvent#END_CALL} methods.
	 *
	 * @param resource The REST resource object.
	 * @return The default response headers for this REST object.
	 */
	protected List<Method> createEndCallMethods(Object resource) {
		Map<String,Method> x = AMap.create();

		for (MethodInfo m : ClassInfo.ofProxy(resource).getAllMethodsParentFirst())
			for (RestHook h : m.getAnnotations(RestHook.class))
				if (h.value() == HookEvent.END_CALL)
					x.put(m.getSignature(), m.accessible().inner());

		return AList.of(x.values());
	}

	/**
	 * Instantiates the list of {@link HookEvent#POST_INIT} methods.
	 *
	 * @param resource The REST resource object.
	 * @return The default response headers for this REST object.
	 */
	protected List<Method> createPostInitMethods(Object resource) {
		Map<String,Method> x = AMap.create();

		for (MethodInfo m : ClassInfo.ofProxy(resource).getAllMethodsParentFirst())
			for (RestHook h : m.getAnnotations(RestHook.class))
				if (h.value() == HookEvent.POST_INIT)
					x.put(m.getSignature(), m.accessible().inner());

		return AList.of(x.values());
	}

	/**
	 * Instantiates the list of {@link HookEvent#POST_INIT_CHILD_FIRST} methods.
	 *
	 * @param resource The REST resource object.
	 * @return The default response headers for this REST object.
	 */
	protected List<Method> createPostInitChildFirstMethods(Object resource) {
		Map<String,Method> x = AMap.create();

		for (MethodInfo m : ClassInfo.ofProxy(resource).getAllMethodsParentFirst())
			for (RestHook h : m.getAnnotations(RestHook.class))
				if (h.value() == HookEvent.POST_INIT_CHILD_FIRST)
					x.put(m.getSignature(), m.accessible().inner());

		return AList.of(x.values());
	}

	/**
	 * Instantiates the list of {@link HookEvent#DESTROY} methods.
	 *
	 * @param resource The REST resource object.
	 * @return The default response headers for this REST object.
	 */
	protected List<Method> createDestroyMethods(Object resource) {
		Map<String,Method> x = AMap.create();

		for (MethodInfo m : ClassInfo.ofProxy(resource).getAllMethodsParentFirst())
			for (RestHook h : m.getAnnotations(RestHook.class))
				if (h.value() == HookEvent.DESTROY)
					x.put(m.getSignature(), m.accessible().inner());

		return AList.of(x.values());
	}

	/**
	 * Instantiates the list of {@link HookEvent#PRE_CALL} methods.
	 *
	 * @param resource The REST resource object.
	 * @return The default response headers for this REST object.
	 */
	protected List<Method> createPreCallMethods(Object resource) {
		Map<String,Method> x = AMap.create();

		for (MethodInfo m : ClassInfo.ofProxy(resource).getAllMethodsParentFirst())
			for (RestHook h : m.getAnnotations(RestHook.class))
				if (h.value() == HookEvent.PRE_CALL)
					x.put(m.getSignature(), m.accessible().inner());

		return AList.of(x.values());
	}

	/**
	 * Instantiates the list of {@link HookEvent#POST_CALL} methods.
	 *
	 * @param resource The REST resource object.
	 * @return The default response headers for this REST object.
	 */
	protected List<Method> createPostCallMethods(Object resource) {
		Map<String,Method> x = AMap.create();

		for (MethodInfo m : ClassInfo.ofProxy(resource).getAllMethodsParentFirst())
			for (RestHook h : m.getAnnotations(RestHook.class))
				if (h.value() == HookEvent.POST_CALL)
					x.put(m.getSignature(), m.accessible().inner());

		return AList.of(x.values());
	}

	/**
	 * Returns the bean factory associated with this context.
	 *
	 * <p>
	 * The bean factory is used for instantiating child resource classes.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link #REST_beanFactory}
	 * </ul>
	 *
	 * @return The resource resolver associated with this context.
	 */
	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	/**
	 * Returns the time statistics gatherer for the specified method.
	 *
	 * @param m The method to get statistics for.
	 * @return The cached time-stats object.
	 */
	protected MethodExecStats getMethodExecStats(Method m) {
		String n = MethodInfo.of(m).getSimpleName();
		MethodExecStats ts = methodExecStats.get(n);
		if (ts == null) {
			methodExecStats.putIfAbsent(n, new MethodExecStats(m));
			ts = methodExecStats.get(n);
		}
		return ts;
	}

	/**
	 * Returns the variable resolver for this servlet.
	 *
	 * <p>
	 * Variable resolvers are used to replace variables in property values.
	 * They can be nested arbitrarily deep.
	 * They can also return values that themselves contain other variables.
	 *
	 * <h5 class='figure'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<ja>@Rest</ja>(
	 * 		messages=<js>"nls/Messages"</js>,
	 * 		properties={
	 * 			<ja>@Property</ja>(name=<js>"title"</js>,value=<js>"$L{title}"</js>),  <jc>// Localized variable in Messages.properties</jc>
	 * 			<ja>@Property</ja>(name=<js>"javaVendor"</js>,value=<js>"$S{java.vendor,Oracle}"</js>),  <jc>// System property with default value</jc>
	 * 			<ja>@Property</ja>(name=<js>"foo"</js>,value=<js>"bar"</js>),
	 * 			<ja>@Property</ja>(name=<js>"bar"</js>,value=<js>"baz"</js>),
	 * 			<ja>@Property</ja>(name=<js>"v1"</js>,value=<js>"$R{foo}"</js>),  <jc>// Request variable.  value="bar"</jc>
	 * 			<ja>@Property</ja>(name=<js>"v1"</js>,value=<js>"$R{foo,bar}"</js>),  <jc>// Request variable.  value="bar"</jc>
	 * 		}
	 * 	)
	 * 	<jk>public class</jk> MyRestResource <jk>extends</jk> BasicRestServlet {
	 * </p>
	 *
	 * <p>
	 * A typical usage pattern involves using variables inside the {@link HtmlDocConfig @HtmlDocConfig} annotation:
	 * <p class='bcode w800'>
	 * 	<ja>@RestMethod</ja>(
	 * 		method=<jsf>GET</jsf>, path=<js>"/{name}/*"</js>
	 * 	)
	 * 	<ja>@HtmlDocConfig</ja>(
	 * 		navlinks={
	 * 			<js>"up: $R{requestParentURI}"</js>,
	 * 			<js>"api: servlet:/api"</js>,
	 * 			<js>"stats: servlet:/stats"</js>,
	 * 			<js>"editLevel: servlet:/editLevel?logger=$A{attribute.name, OFF}"</js>
	 * 		}
	 * 		header={
	 * 			<js>"&lt;h1&gt;$L{MyLocalizedPageTitle}&lt;/h1&gt;"</js>
	 * 		},
	 * 		aside={
	 * 			<js>"$F{resources/AsideText.html}"</js>
	 * 		}
	 * 	)
	 * 	<jk>public</jk> LoggerEntry getLogger(RestRequest req, <ja>@Path</ja> String name) <jk>throws</jk> Exception {
	 * </p>
	 *
	 * <ul class='seealso'>
	 * 	<li class='jm'>{@link org.apache.juneau.rest.RestContextBuilder#vars(Class...)} - For adding custom vars.
	 * 	<li class='link'>{@doc RestSvlVariables}
	 * 	<li class='link'>{@doc RestSvlVariables}
	 * </ul>
	 *
	 * @return The var resolver in use by this resource.
	 */
	public VarResolver getVarResolver() {
		return varResolver;
	}

	/**
	 * Returns the config file associated with this servlet.
	 *
	 * <p>
	 * The config file is identified via one of the following:
	 * <ul class='javatree'>
	 * 	<li class='ja'>{@link Rest#config()}
	 * 	<li class='jm'>{@link RestContextBuilder#config(Config)}
	 * </ul>
	 *
	 * @return
	 * 	The resolving config file associated with this servlet.
	 * 	<br>Never <jk>null</jk>.
	 */
	public Config getConfig() {
		return config;
	}


	/**
	 * Returns the path for this resource as defined by the {@link Rest#path() @Rest(path)} annotation or
	 * {@link RestContextBuilder#path(String)} method.
	 *
	 * <p>
	 * If path is not specified, returns <js>""</js>.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link #REST_path}
	 * </ul>
	 *
	 * @return The servlet path.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Returns the path for this resource as defined by the {@link Rest#path() @Rest(path)} annotation or
	 * {@link RestContextBuilder#path(String)} method concatenated with those on all parent classes.
	 *
	 * <p>
	 * If path is not specified, returns <js>""</js>.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link #REST_path}
	 * </ul>
	 *
	 * @return The full path.
	 */
	public String getFullPath() {
		return fullPath;
	}

	/**
	 * Returns the call logger to use for this resource.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link #REST_callLogger}
	 * </ul>
	 *
	 * @return
	 * 	The call logger to use for this resource.
	 * 	<br>Never <jk>null</jk>.
	 */
	public RestLogger getCallLogger() {
		return callLogger;
	}

	/**
	 * Returns the resource bundle used by this resource.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link #REST_messages}
	 * </ul>
	 *
	 * @return
	 * 	The resource bundle for this resource.
	 * 	<br>Never <jk>null</jk>.
	 */
	public Messages getMessages() {
		return msgs;
	}

	/**
	 * Returns the Swagger provider used by this resource.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestContext#REST_swaggerProvider}
	 * </ul>
	 *
	 * @return
	 * 	The information provider for this resource.
	 * 	<br>Never <jk>null</jk>.
	 */
	public SwaggerProvider getSwaggerProvider() {
		return swaggerProvider;
	}

	/**
	 * Returns the resource object.
	 *
	 * <p>
	 * This is the instance of the class annotated with the {@link Rest @Rest} annotation, usually
	 * an instance of {@link RestServlet}.
	 *
	 * @return
	 * 	The resource object.
	 * 	<br>Never <jk>null</jk>.
	 */
	@BeanIgnore
	public Object getResource() {
		return resource.get();
	}

	/**
	 * Returns the parent resource context (if this resource was initialized from a parent).
	 *
	 * @return The parent resource context, or <jk>null</jk> if there is no parent context.
	 */
	public RestContext getParentContext() {
		return parentContext;
	}

	/**
	 * Returns the servlet init parameter returned by {@link ServletConfig#getInitParameter(String)}.
	 *
	 * @param name The init parameter name.
	 * @return The servlet init parameter, or <jk>null</jk> if not found.
	 */
	public String getServletInitParameter(String name) {
		return builder.getInitParameter(name);
	}

	/**
	 * Returns the child resources associated with this servlet.
	 *
	 * @return
	 * 	An unmodifiable map of child resources.
	 * 	Keys are the {@link Rest#path() @Rest(path)} annotation defined on the child resource.
	 */
	public Map<String,RestContext> getChildResources() {
		return restChildren.asMap();
	}

	/**
	 * Returns whether it's safe to render stack traces in HTTP responses.
	 *
	 * @return <jk>true</jk> if setting is enabled.
	 */
	public boolean isRenderResponseStackTraces() {
		return renderResponseStackTraces;
	}

	/**
	 * Returns whether it's safe to pass the HTTP body as a <js>"body"</js> GET parameter.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestContext#REST_disableAllowBodyParam}
	 * </ul>
	 *
	 * @return <jk>true</jk> if setting is enabled.
	 */
	public boolean isAllowBodyParam() {
		return allowBodyParam;
	}

	/**
	 * Allowed header URL parameters.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestContext#REST_allowedHeaderParams}
	 * </ul>
	 *
	 * @return
	 * 	The header names allowed to be passed as URL parameters.
	 * 	<br>The set is case-insensitive ordered.
	 */
	public Set<String> getAllowedHeaderParams() {
		return allowedHeaderParams;
	}

	/**
	 * Allowed method headers.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestContext#REST_allowedMethodHeaders}
	 * </ul>
	 *
	 * @return
	 * 	The method names allowed to be passed as <c>X-Method</c> headers.
	 * 	<br>The set is case-insensitive ordered.
	 */
	public Set<String> getAllowedMethodHeaders() {
		return allowedMethodHeaders;
	}

	/**
	 * Allowed method URL parameters.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestContext#REST_allowedMethodParams}
	 * </ul>
	 *
	 * @return
	 * 	The method names allowed to be passed as <c>method</c> URL parameters.
	 * 	<br>The set is case-insensitive ordered.
	 */
	public Set<String> getAllowedMethodParams() {
		return allowedMethodParams;
	}

	/**
	 * Returns the debug setting on this context for the specified method.
	 *
	 * @param method The java method.
	 * @return The debug setting on this context or the debug value of the servlet context if not specified for this method.
	 */
	public Enablement getDebug(Method method) {
		if (method == null)
			return null;
		return debugEnablementMap.find(method).orElse(debug);
	}

	/**
	 * Returns the name of the client version header name used by this resource.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestContext#REST_clientVersionHeader}
	 * </ul>
	 *
	 * @return
	 * 	The name of the client version header used by this resource.
	 * 	<br>Never <jk>null</jk>.
	 */
	public String getClientVersionHeader() {
		return clientVersionHeader;
	}

	/**
	 * Returns the file finder associated with this context.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestContext#REST_fileFinder}
	 * </ul>
	 *
	 * @return
	 * 	The file finder for this resource.
	 * 	<br>Never <jk>null</jk>.
	 */
	public FileFinder getFileFinder() {
		return fileFinder;
	}

	/**
	 * Returns the static files associated with this context.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestContext#REST_staticFiles}
	 * </ul>
	 *
	 * @return
	 * 	The static files for this resource.
	 * 	<br>Never <jk>null</jk>.
	 */
	public StaticFiles getStaticFiles() {
		return staticFiles;
	}

	/**
	 * Returns the logger associated with this context.
	 *
	 * @return
	 * 	The logger for this resource.
	 * 	<br>Never <jk>null</jk>.
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * Returns the stack trace database associated with this context.
	 *
	 * @return
	 * 	The stack trace database for this resource.
	 * 	<br>Never <jk>null</jk>.
	 */
	public StackTraceStore getStackTraceStore() {
		return stackTraceStore;
	}

	/**
	 * Returns the HTTP-part parser associated with this resource.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestContext#REST_partParser}
	 * </ul>
	 *
	 * @return
	 * 	The HTTP-part parser associated with this resource.
	 * 	<br>Never <jk>null</jk>.
	 */
	public HttpPartParser getPartParser() {
		return partParser;
	}

	/**
	 * Returns the HTTP-part serializer associated with this resource.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestContext#REST_partSerializer}
	 * </ul>
	 *
	 * @return
	 * 	The HTTP-part serializer associated with this resource.
	 * 	<br>Never <jk>null</jk>.
	 */
	public HttpPartSerializer getPartSerializer() {
		return partSerializer;
	}

	/**
	 * Returns the JSON-Schema generator associated with this resource.
	 *
	 * @return
	 * 	The HTTP-part serializer associated with this resource.
	 * 	<br>Never <jk>null</jk>.
	 */
	public JsonSchemaGenerator getJsonSchemaGenerator() {
		return jsonSchemaGenerator;
	}

	/**
	 * Returns the explicit list of supported accept types for this resource.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestContext#REST_serializers}
	 * 	<li class='jf'>{@link RestContext#REST_produces}
	 * </ul>
	 *
	 * @return
	 * 	The supported <c>Accept</c> header values for this resource.
	 * 	<br>Never <jk>null</jk>.
	 */
	public List<MediaType> getProduces() {
		return produces;
	}

	/**
	 * Returns the explicit list of supported content types for this resource.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestContext#REST_parsers}
	 * 	<li class='jf'>{@link RestContext#REST_consumes}
	 * </ul>
	 *
	 * @return
	 * 	The supported <c>Content-Type</c> header values for this resource.
	 * 	<br>Never <jk>null</jk>.
	 */
	public List<MediaType> getConsumes() {
		return consumes;
	}

	/**
	 * Returns the default request headers for this resource.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestContext#REST_defaultRequestHeaders}
	 * </ul>
	 *
	 * @return
	 * 	The default request headers for this resource.
	 * 	<br>Never <jk>null</jk>.
	 */
	public List<org.apache.http.Header> getDefaultRequestHeaders() {
		return unmodifiableList(asList(defaultRequestHeaders));
	}

	/**
	 * Returns the default request attributes for this resource.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestContext#REST_defaultRequestAttributes}
	 * </ul>
	 *
	 * @return
	 * 	The default request headers for this resource.
	 * 	<br>Never <jk>null</jk>.
	 */
	public List<NamedAttribute> getDefaultRequestAttributes() {
		return unmodifiableList(asList(defaultRequestAttributes));
	}

	/**
	 * Returns the default response headers for this resource.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestContext#REST_defaultResponseHeaders}
	 * </ul>
	 *
	 * @return
	 * 	The default response headers for this resource.
	 * 	<br>Never <jk>null</jk>.
	 */
	public List<org.apache.http.Header> getDefaultResponseHeaders() {
		return unmodifiableList(asList(defaultResponseHeaders));
	}

	/**
	 * Returns the response handlers associated with this resource.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestContext#REST_responseHandlers}
	 * </ul>
	 *
	 * @return
	 * 	The response handlers associated with this resource.
	 * 	<br>Never <jk>null</jk>.
	 */
	protected ResponseHandler[] getResponseHandlers() {
		return responseHandlers;
	}

	/**
	 * Returns the authority path of the resource.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestContext#REST_uriAuthority}
	 * </ul>
	 *
	 * @return
	 * 	The authority path of this resource.
	 * 	<br>If not specified, returns the context path of the ascendant resource.
	 */
	public String getUriAuthority() {
		if (uriAuthority != null)
			return uriAuthority;
		if (parentContext != null)
			return parentContext.getUriAuthority();
		return null;
	}

	/**
	 * Returns the context path of the resource.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestContext#REST_uriContext}
	 * </ul>
	 *
	 * @return
	 * 	The context path of this resource.
	 * 	<br>If not specified, returns the context path of the ascendant resource.
	 */
	public String getUriContext() {
		if (uriContext != null)
			return uriContext;
		if (parentContext != null)
			return parentContext.getUriContext();
		return null;
	}

	/**
	 * Returns the setting on how relative URIs should be interpreted as relative to.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestContext#REST_uriRelativity}
	 * </ul>
	 *
	 * @return
	 * 	The URI-resolution relativity setting value.
	 * 	<br>Never <jk>null</jk>.
	 */
	public UriRelativity getUriRelativity() {
		return uriRelativity;
	}

	/**
	 * Returns the setting on how relative URIs should be resolved.
	 *
	 * <ul class='seealso'>
	 * 	<li class='jf'>{@link RestContext#REST_uriResolution}
	 * </ul>
	 *
	 * @return
	 * 	The URI-resolution setting value.
	 * 	<br>Never <jk>null</jk>.
	 */
	public UriResolution getUriResolution() {
		return uriResolution;
	}

	/**
	 * Returns the REST Java methods defined in this resource.
	 *
	 * <p>
	 * These are the methods annotated with the {@link RestMethod @RestMethod} annotation.
	 *
	 * @return
	 * 	An unmodifiable map of Java method names to call method objects.
	 */
	public List<RestMethodContext> getMethodContexts() {
		return restMethods.getMethodContexts();
	}

	/**
	 * Returns timing information on all method executions on this class.
	 *
	 * <p>
	 * Timing information is maintained for any <ja>@RestResource</ja>-annotated and hook methods.
	 *
	 * @return A list of timing statistics ordered by average execution time descending.
	 */
	public List<MethodExecStats> getMethodExecStats() {
		return methodExecStats.values().stream().sorted().collect(Collectors.toList());
	}

	/**
	 * Gives access to the internal stack trace database.
	 *
	 * @return The stack trace database.
	 */
	public RestContextStats getStats() {
		return new RestContextStats(startTime, getMethodExecStats());
	}

	/**
	 * Returns the resource class type.
	 *
	 * @return The resource class type.
	 */
	public Class<?> getResourceClass() {
		return resourceClass;
	}

	/**
	 * Returns the swagger for the REST resource.
	 *
	 * @param locale The locale of the swagger to return.
	 * @return The swagger as an {@link Optional}.  Never <jk>null</jk>.
	 */
	public Optional<Swagger> getSwagger(Locale locale) {
		Swagger s = swaggerCache.get(locale);
		if (s == null) {
			try {
				s = swaggerProvider.getSwagger(this, locale);
				if (s != null)
					swaggerCache.put(locale, s);
			} catch (Exception e) {
				throw toHttpException(e, InternalServerError.class);
			}
		}
		return Optional.ofNullable(s);
	}

	/**
	 * Returns the timing information returned by {@link #getMethodExecStats()} in a readable format.
	 *
	 * @return A report of all method execution times ordered by .
	 */
	public String getMethodExecStatsReport() {
		StringBuilder sb = new StringBuilder()
			.append(" Method                         Runs      Running   Errors   Avg          Total     \n")
			.append("------------------------------ --------- --------- -------- ------------ -----------\n");
		getMethodExecStats()
			.stream()
			.sorted(Comparator.comparingDouble(MethodExecStats::getTotalTime).reversed())
			.forEach(x -> sb.append(String.format("%30s %9d %9d %9d %10dms %10dms\n", x.getMethod(), x.getRuns(), x.getRunning(), x.getErrors(), x.getAvgTime(), x.getTotalTime())));
		return sb.toString();
	}

	/**
	 * Finds the {@link RestParam} instances to handle resolving objects on the calls to the specified Java method.
	 *
	 * @param m The Java method being called.
	 * @param beanFactory The method context bean factory.
	 * @return The array of resolvers.
	 */
	protected RestParam[] findRestMethodParams(Method m, BeanFactory beanFactory) {

		MethodInfo mi = MethodInfo.of(m);
		List<ClassInfo> pt = mi.getParamTypes();
		RestParam[] rp = new RestParam[pt.size()];

		beanFactory = BeanFactory.of(beanFactory, getResource());

		for (int i = 0; i < pt.size(); i++) {
			ParamInfo pi = mi.getParam(i);
			beanFactory.addBean(ParamInfo.class, pi);
			for (Class<? extends RestParam> c : restParams) {
				try {
					rp[i] = beanFactory.createBean(c);
					if (rp[i] != null)
						break;
				} catch (ExecutableException e) {
					throw new InternalServerError(e.unwrap(), "Could not resolve parameter {0} on method {1}.", i, mi.getFullName());
				}
			}
			if (rp[i] == null)
				throw new InternalServerError("Could not resolve parameter {0} on method {1}.", i, mi.getFullName());
		}

		return rp;
	}

	/**
	 * Finds the {@link RestParam} instances to handle resolving objects on pre-call and post-call Java methods.
	 *
	 * @param m The Java method being called.
	 * @param beanFactory The method context bean factory.
	 * @return The array of resolvers.
	 */
	protected RestParam[] findHookMethodParams(Method m, BeanFactory beanFactory) {
		MethodInfo mi = MethodInfo.of(m);
		List<ClassInfo> pt = mi.getParamTypes();
		RestParam[] rp = new RestParam[pt.size()];

		beanFactory = BeanFactory.of(beanFactory, getResource());

		for (int i = 0; i < pt.size(); i++) {
			ParamInfo pi = mi.getParam(i);
			beanFactory.addBean(ParamInfo.class, pi);
			for (Class<? extends RestParam> c : hookMethodParams) {
				try {
					rp[i] = beanFactory.createBean(c);
					if (rp[i] != null)
						break;
				} catch (ExecutableException e) {
					throw new InternalServerError(e.unwrap(), "Could not resolve parameter {0} on method {1}.", i, mi.getFullName());
				}
			}
			if (rp[i] == null)
				throw new InternalServerError("Could not resolve parameter {0} on method {1}.", i, mi.getFullName());
		}

		return rp;
	}

	//------------------------------------------------------------------------------------------------------------------
	// Call handling
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Wraps an incoming servlet request/response pair into a single {@link RestCall} object.
	 *
	 * <p>
	 * This is the first method called by {@link #execute(Object, HttpServletRequest, HttpServletResponse)}.
	 *
	 * @param resource The REST object.
	 * @param req The rest request.
	 * @param res The rest response.
	 * @return The wrapped request/response pair.
	 */
	protected RestCall createCall(Object resource, HttpServletRequest req, HttpServletResponse res) {
		return new RestCall(resource, this, req, res).logger(getCallLogger());
	}

	/**
	 * Creates a {@link RestRequest} object based on the specified incoming {@link HttpServletRequest} object.
	 *
	 * <p>
	 * This method is called immediately after {@link #startCall(RestCall)} has been called.
	 *
	 * @param call The current REST call.
	 * @return The wrapped request object.
	 * @throws ServletException If any errors occur trying to interpret the request.
	 */
	public RestRequest createRequest(RestCall call) throws ServletException {
		return new RestRequest(call);
	}

	/**
	 * Creates a {@link RestResponse} object based on the specified incoming {@link HttpServletResponse} object
	 * and the request returned by {@link #createRequest(RestCall)}.
	 *
	 * @param call The current REST call.
	 * @return The wrapped response object.
	 * @throws ServletException If any errors occur trying to interpret the request or response.
	 */
	public RestResponse createResponse(RestCall call) throws ServletException {
		return new RestResponse(call);
	}

	/**
	 * The main service method.
	 *
	 * <p>
	 * Subclasses can optionally override this method if they want to tailor the behavior of requests.
	 *
	 * @param resource The REST object.
	 * @param r1 The incoming HTTP servlet request object.
	 * @param r2 The incoming HTTP servlet response object.
	 * @throws ServletException General servlet exception.
	 * @throws IOException Thrown by underlying stream.
	 */
	public void execute(Object resource, HttpServletRequest r1, HttpServletResponse r2) throws ServletException, IOException {

		RestCall call = createCall(resource, r1, r2);

		// Must be careful not to bleed thread-locals.
		if (this.call.get() != null)
			System.err.println("WARNING:  Thread-local call object was not cleaned up from previous request.  " + this + ", thread=["+Thread.currentThread().getId()+"]");
		this.call.set(call);

		try {

			if (initException != null)
				throw initException;

			// If the resource path contains variables (e.g. @Rest(path="/f/{a}/{b}"), then we want to resolve
			// those variables and push the servletPath to include the resolved variables.  The new pathInfo will be
			// the remainder after the new servletPath.
			// Only do this for the top-level resource because the logic for child resources are processed next.
			if (pathMatcher.hasVars() && getParentContext() == null) {
				String sp = call.getServletPath();
				String pi = call.getPathInfoUndecoded();
				UrlPath upi2 = UrlPath.of(pi == null ? sp : sp + pi);
				UrlPathMatch uppm = pathMatcher.match(upi2);
				if (uppm != null && ! uppm.hasEmptyVars()) {
					call.pathVars(uppm.getVars());
					call.request(
						new OverrideableHttpServletRequest(call.getRequest())
							.pathInfo(nullIfEmpty(urlDecode(uppm.getSuffix())))
							.servletPath(uppm.getPrefix())
					);
				} else {
					call.debug(isDebug(call)).status(SC_NOT_FOUND).finish();
					return;
				}
			}

			// If this resource has child resources, try to recursively call them.
			Optional<RestChildMatch> childMatch = restChildren.findMatch(call);
			if (childMatch.isPresent()) {
				UrlPathMatch uppm = childMatch.get().getPathMatch();
				RestContext rc = childMatch.get().getChildContext();
				if (! uppm.hasEmptyVars()) {
					call.pathVars(uppm.getVars());
					HttpServletRequest childRequest = new OverrideableHttpServletRequest(call.getRequest())
						.pathInfo(nullIfEmpty(urlDecode(uppm.getSuffix())))
						.servletPath(call.getServletPath() + uppm.getPrefix());
					rc.execute(rc.getResource(), childRequest, call.getResponse());
				} else {
					call.debug(isDebug(call)).status(SC_NOT_FOUND).finish();
				}
				return;
			}

			if (isDebug(call))
				call.debug(true);

			startCall(call);

			createRequest(call);
			createResponse(call);

			// If the specified method has been defined in a subclass, invoke it.
			try {
				restMethods.findMethod(call).invoke(call);
			} catch (NotFound e) {
				if (call.getStatus() == 0)
					call.status(404);
				call.exception(e);
				handleNotFound(call);
			}

			if (call.hasOutput()) {
				// Now serialize the output if there was any.
				// Some subclasses may write to the OutputStream or Writer directly.
				handleResponse(call);
			}


		} catch (Throwable e) {
			handleError(call, convertThrowable(e));
		} finally {
			clearState();
		}

		call.finish();
		finishCall(call);
	}

	private boolean isDebug(RestCall call) {
		Enablement e = null;
		RestMethodContext mc = call.getRestMethodContext();
		if (mc != null)
			e = mc.getDebug();
		if (e == null)
			e = getDebug();
		if (e == ALWAYS)
			return true;
		if (e == NEVER)
			return false;
		if (e == CONDITIONAL)
			return "true".equalsIgnoreCase(call.getRequest().getHeader("X-Debug"));
		return false;
	}

	/**
	 * The main method for serializing POJOs passed in through the {@link RestResponse#setOutput(Object)} method or
	 * returned by the Java method.
	 *
	 * <p>
	 * Subclasses may override this method if they wish to modify the way the output is rendered or support other output
	 * formats.
	 *
	 * <p>
	 * The default implementation simply iterates through the response handlers on this resource
	 * looking for the first one whose {@link ResponseHandler#handle(RestRequest,RestResponse)} method returns
	 * <jk>true</jk>.
	 *
	 * @param call The HTTP call.
	 * @throws IOException Thrown by underlying stream.
	 * @throws HttpException Non-200 response.
	 * @throws NotImplemented No registered response handlers could handle the call.
	 */
	public void handleResponse(RestCall call) throws IOException, HttpException, NotImplemented {

		RestRequest req = call.getRestRequest();
		RestResponse res = call.getRestResponse();

		// Loop until we find the correct handler for the POJO.
		for (ResponseHandler h : getResponseHandlers())
			if (h.handle(req, res))
				return;

		Object output = res.getOutput();
		throw new NotImplemented("No response handlers found to process output of type '"+(output == null ? null : output.getClass().getName())+"'");
	}

	/**
	 * Method that can be subclassed to allow uncaught throwables to be treated as other types of throwables.
	 *
	 * <p>
	 * The default implementation looks at the throwable class name to determine whether it can be converted to another type:
	 *
	 * <ul>
	 * 	<li><js>"*AccessDenied*"</js> - Converted to {@link Unauthorized}.
	 * 	<li><js>"*Empty*"</js>,<js>"*NotFound*"</js> - Converted to {@link NotFound}.
	 * </ul>
	 *
	 * @param t The thrown object.
	 * @return The converted thrown object.
	 */
	public Throwable convertThrowable(Throwable t) {

		ClassInfo ci = ClassInfo.ofc(t);
		if (ci.is(InvocationTargetException.class)) {
			t = ((InvocationTargetException)t).getTargetException();
			ci = ClassInfo.ofc(t);
		}

		if (ci.is(HttpRuntimeException.class)) {
			t = ((HttpRuntimeException)t).getInner();
			ci = ClassInfo.ofc(t);
		}

		if (ci.hasAnnotation(Response.class))
			return t;

		if (t instanceof ParseException || t instanceof InvalidDataConversionException)
			return new BadRequest(t);

		String n = t.getClass().getName();

		if (n.contains("AccessDenied") || n.contains("Unauthorized"))
			return new Unauthorized(t);

		if (n.contains("Empty") || n.contains("NotFound"))
			return new NotFound(t);

		return t;
	}

	/**
	 * Handle the case where a matching method was not found.
	 *
	 * <p>
	 * Subclasses can override this method to provide a 2nd-chance for specifying a response.
	 * The default implementation will simply throw an exception with an appropriate message.
	 *
	 * @param call The HTTP call.
	 * @throws Exception Any exception can be thrown.
	 */
	public void handleNotFound(RestCall call) throws Exception {
		String pathInfo = call.getPathInfo();
		String methodUC = call.getMethod();
		int rc = call.getStatus();
		String onPath = pathInfo == null ? " on no pathInfo"  : String.format(" on path '%s'", pathInfo);
		if (rc == SC_NOT_FOUND)
			throw new NotFound("Method ''{0}'' not found on resource with matching pattern{1}.", methodUC, onPath);
		else if (rc == SC_PRECONDITION_FAILED)
			throw new PreconditionFailed("Method ''{0}'' not found on resource{1} with matching matcher.", methodUC, onPath);
		else if (rc == SC_METHOD_NOT_ALLOWED)
			throw new MethodNotAllowed("Method ''{0}'' not found on resource{1}.", methodUC, onPath);
		else
			throw new ServletException("Invalid method response: " + rc, call.getException());
	}

	/**
	 * Method for handling response errors.
	 *
	 * <p>
	 * Subclasses can override this method to provide their own custom error response handling.
	 *
	 * @param call The rest call.
	 * @param e The exception that occurred.
	 * @throws IOException Can be thrown if a problem occurred trying to write to the output stream.
	 */
	public synchronized void handleError(RestCall call, Throwable e) throws IOException {

		call.exception(e);

		if (call.isDebug())
			e.printStackTrace();

		int code = 500;

		ClassInfo ci = ClassInfo.ofc(e);
		Response r = ci.getLastAnnotation(Response.class);
		if (r != null)
			if (r.code().length > 0)
				code = r.code()[0];

		HttpException e2 = (e instanceof HttpException ? (HttpException)e : new HttpException(e, code));

		HttpServletRequest req = call.getRequest();
		HttpServletResponse res = call.getResponse();

		Throwable t = null;
		if (e instanceof HttpRuntimeException)
			t = ((HttpRuntimeException)e).getInner();
		if (t == null)
			t = e2.getRootCause();
		if (t != null) {
			res.setHeader("Exception-Name", stripInvalidHttpHeaderChars(t.getClass().getName()));
			res.setHeader("Exception-Message", stripInvalidHttpHeaderChars(t.getMessage()));
		}

		try {
			res.setContentType("text/plain");
			res.setHeader("Content-Encoding", "identity");
			res.setStatus(e2.getStatus());

			PrintWriter w = null;
			try {
				w = res.getWriter();
			} catch (IllegalStateException x) {
				w = new PrintWriter(new OutputStreamWriter(res.getOutputStream(), UTF8));
			}

			try (PrintWriter w2 = w) {
				String httpMessage = RestUtils.getHttpResponseText(e2.getStatus());
				if (httpMessage != null)
					w2.append("HTTP ").append(String.valueOf(e2.getStatus())).append(": ").append(httpMessage).append("\n\n");
				if (isRenderResponseStackTraces())
					e.printStackTrace(w2);
				else
					w2.append(e2.getFullStackMessage(true));
			}

		} catch (Exception e1) {
			req.setAttribute("Exception", e1);
		}
	}

	/**
	 * Returns the session objects for the specified request.
	 *
	 * <p>
	 * The default implementation simply returns a single map containing <c>{'req':req,'res',res}</c>.
	 *
	 * @param call The current REST call.
	 * @return The session objects for that request.
	 */
	public Map<String,Object> getSessionObjects(RestCall call) {
		Map<String,Object> m = new HashMap<>();
		m.put("req", call.getRequest());
		m.put("res", call.getResponse());
		return m;
	}

	/**
	 * Called at the start of a request to invoke all {@link HookEvent#START_CALL} methods.
	 *
	 * @param call The current request.
	 * @throws HttpException If thrown from call methods.
	 */
	protected void startCall(RestCall call) throws HttpException {
		for (MethodInvoker x : startCallMethods) {
			try {
				x.invokeUsingFactory(call.getBeanFactory(), call.getContext().getResource());
			} catch (ExecutableException e) {
				throw toHttpException(e.unwrap(), InternalServerError.class);
			}
		}
	}

	/**
	 * Called during a request to invoke all {@link HookEvent#PRE_CALL} methods.
	 *
	 * @param call The current request.
	 * @throws HttpException If thrown from call methods.
	 */
	protected void preCall(RestCall call) throws HttpException {
		for (RestMethodInvoker m : preCallMethods)
			m.invokeFromCall(call, getResource());
	}

	/**
	 * Called during a request to invoke all {@link HookEvent#POST_CALL} methods.
	 *
	 * @param call The current request.
	 * @throws HttpException If thrown from call methods.
	 */
	protected void postCall(RestCall call) throws HttpException {
		for (RestMethodInvoker m : postCallMethods)
			m.invokeFromCall(call, getResource());
	}

	/**
	 * Called at the end of a request to invoke all {@link HookEvent#END_CALL} methods.
	 *
	 * <p>
	 * This is the very last method called in {@link #execute(Object, HttpServletRequest, HttpServletResponse)}.
	 *
	 * @param call The current request.
	 */
	protected void finishCall(RestCall call) {
		for (MethodInvoker x : endCallMethods) {
			try {
				x.invokeUsingFactory(call.getBeanFactory(), call.getResource());
			} catch (ExecutableException e) {
				logger.log(Level.WARNING, e.unwrap(), ()->format("Error occurred invoking finish-call method ''{0}''.", x.getFullName()));
			}
		}
	}

	/**
	 * Called during servlet initialization to invoke all {@link HookEvent#POST_INIT} methods.
	 *
	 * @return This object (for method chaining).
	 * @throws ServletException Error occurred.
	 */
	public synchronized RestContext postInit() throws ServletException {
		if (initialized.get())
			return this;
		Object resource = getResource();
		MethodInfo mi = ClassInfo.of(getResource()).getMethod("setContext", RestContext.class);
		if (mi != null) {
			try {
				mi.accessible().invoke(resource, this);
			} catch (ExecutableException e) {
				throw new ServletException(e.unwrap());
			}
		}
		for (MethodInvoker x : postInitMethods) {
			try {
				x.invokeUsingFactory(beanFactory, getResource());
			} catch (ExecutableException e) {
				throw new ServletException(e.unwrap());
			}
		}
		restChildren.postInit();
		return this;
	}

	/**
	 * Called during servlet initialization to invoke all {@link HookEvent#POST_INIT_CHILD_FIRST} methods.
	 *
	 * @return This object (for method chaining).
	 * @throws ServletException Error occurred.
	 */
	public RestContext postInitChildFirst() throws ServletException {
		if (initialized.get())
			return this;
		restChildren.postInitChildFirst();
		for (MethodInvoker x : postInitChildFirstMethods) {
			try {
				x.invokeUsingFactory(beanFactory, getResource());
			} catch (ExecutableException e) {
				throw new ServletException(e.unwrap());
			}
		}
		initialized.set(true);
		return this;
	}

	/**
	 * Called during servlet destruction to invoke all {@link HookEvent#DESTROY} methods.
	 */
	protected void destroy() {
		for (MethodInvoker x : destroyMethods) {
			try {
				x.invokeUsingFactory(beanFactory, getResource());
			} catch (ExecutableException e) {
				getLogger().log(Level.WARNING, e.unwrap(), ()->format("Error occurred invoking servlet-destroy method ''{0}''.", x.getFullName()));
			}
		}

		restChildren.destroy();
	}

	/**
	 * Returns the HTTP request object for the current request.
	 *
	 * @return The HTTP request object, or <jk>null</jk> if it hasn't been created.
	 */
	@BeanIgnore
	public RestRequest getRequest() {
		return getCall().getRestRequest();
	}

	/**
	 * Returns the HTTP response object for the current request.
	 *
	 * @return The HTTP response object, or <jk>null</jk> if it hasn't been created.
	 */
	@BeanIgnore
	public RestResponse getResponse() {
		return getCall().getRestResponse();
	}

	/**
	 * Returns the HTTP call for the current request.
	 *
	 * @return The HTTP call for the current request, never <jk>null</jk>?
	 * @throws InternalServerError If no active request exists on the current thread.
	 */
	@BeanIgnore
	public RestCall getCall() {
		RestCall rc = call.get();
		if (rc == null)
			throw new InternalServerError("No active request on current thread.");
		return rc;
	}

	/**
	 * If the specified object is annotated with {@link Response}, this returns the response metadata about that object.
	 *
	 * @param o The object to check.
	 * @return The response metadata, or <jk>null</jk> if it wasn't annotated with {@link Response}.
	 */
	public ResponseBeanMeta getResponseBeanMeta(Object o) {
		if (o == null)
			return null;
		Class<?> c = o.getClass();
		ResponseBeanMeta rbm = responseBeanMetas.get(c);
		if (rbm == null) {
			rbm = ResponseBeanMeta.create(c, getPropertyStore());
			if (rbm == null)
				rbm = ResponseBeanMeta.NULL;
			responseBeanMetas.put(c, rbm);
		}
		if (rbm == ResponseBeanMeta.NULL)
			return null;
		return rbm;
	}

	Enablement getDebug() {
		return debug;
	}

	/**
	 * Clear any request state information on this context.
	 * This should always be called in a finally block in the RestServlet.
	 */
	void clearState() {
		call.remove();
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Other methods.
	//-----------------------------------------------------------------------------------------------------------------

	@Override /* Context */
	public OMap toMap() {
		return super.toMap()
			.a("RestContext", new DefaultFilteringOMap()
				.a("allowBodyParam", allowBodyParam)
				.a("allowedMethodHeader", allowedMethodHeaders)
				.a("allowedMethodParams", allowedMethodParams)
				.a("allowedHeaderParams", allowedHeaderParams)
				.a("beanFactory", beanFactory)
				.a("clientVersionHeader", clientVersionHeader)
				.a("consumes", consumes)
				.a("defaultRequestHeaders", defaultRequestHeaders)
				.a("defaultResponseHeaders", defaultResponseHeaders)
				.a("fileFinder", fileFinder)
				.a("parsers", parsers)
				.a("partParser", partParser)
				.a("partSerializer", partSerializer)
				.a("produces", produces)
				.a("renderResponseStackTraces", renderResponseStackTraces)
				.a("responseHandlers", responseHandlers)
				.a("restParams", restParams)
				.a("serializers", serializers)
				.a("staticFiles", staticFiles)
				.a("swaggerProvider", swaggerProvider)
				.a("uriAuthority", uriAuthority)
				.a("uriContext", uriContext)
				.a("uriRelativity", uriRelativity)
				.a("uriResolution", uriResolution)
			);
	}

	//-----------------------------------------------------------------------------------------------------------------
	// Helpers.
	//-----------------------------------------------------------------------------------------------------------------

}
