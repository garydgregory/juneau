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
package org.apache.juneau.rest.client2;

import static org.apache.juneau.httppart.HttpPartType.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

import org.apache.http.*;
import org.apache.juneau.*;
import org.apache.juneau.assertions.*;
import org.apache.juneau.http.*;
import org.apache.juneau.httppart.*;
import org.apache.juneau.oapi.*;
import org.apache.juneau.parser.ParseException;
import org.apache.juneau.utils.*;

/**
 * Represents a single header on an HTTP response.
 *
 * <p>
 * An extension of an HttpClient {@link Header} that provides various support for converting the header to POJOs and
 * other convenience methods.
 *
 * <ul class='seealso'>
 * 	<li class='jc'>{@link RestClient}
 * 	<li class='link'>{@doc juneau-rest-client}
 * </ul>
 */
public class RestResponseHeader implements Header {

	static final Header NULL_HEADER = new Header() {

		@Override /* Header */
		public String getName() {
			return null;
		}

		@Override /* Header */
		public String getValue() {
			return null;
		}

		@Override /* Header */
		public HeaderElement[] getElements() throws org.apache.http.ParseException {
			return new HeaderElement[0];
		}
	};

	private final Header header;
	private final RestRequest request;
	private final RestResponse response;
	private HttpPartParserSession parser;
	private HttpPartSchema schema;

	/**
	 * Constructor.
	 *
	 * @param request The request object.
	 * @param response The response object.
	 * @param header The wrapped header.  Can be <jk>null</jk>.
	 */
	public RestResponseHeader(RestRequest request, RestResponse response, Header header) {
		this.request = request;
		this.response = response;
		this.header = header == null ? NULL_HEADER : header;
		parser(null);
	}

	//------------------------------------------------------------------------------------------------------------------
	// Setters
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Specifies the part schema for this header.
	 *
	 * <p>
	 * Used by schema-based part parsers such as {@link OpenApiParser}.
	 *
	 * @param value
	 * 	The part schema.
	 * @return This object (for method chaining).
	 */
	public RestResponseHeader schema(HttpPartSchema value) {
		this.schema = value;
		return this;
	}

	/**
	 * Specifies the part parser to use for this header.
	 *
	 * <p>
	 * If not specified, uses the part parser defined on the client by calling {@link RestClientBuilder#partParser(Class)}.
	 *
	 * @param value
	 * 	The new part parser to use for this header.
	 * 	<br>If <jk>null</jk>, {@link SimplePartParser#DEFAULT} will be used.
	 * @return This object (for method chaining).
	 */
	public RestResponseHeader parser(HttpPartParserSession value) {
		this.parser = value == null ? SimplePartParser.DEFAULT_SESSION : value;
		return this;
	}

	//------------------------------------------------------------------------------------------------------------------
	// Retrievers
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Returns <jk>true</jk> if this header exists on the response.
	 *
	 * @return <jk>true</jk> if this header exists on the response.
	 */
	public boolean exists() {
		return header != NULL_HEADER;
	}

	/**
	 * Returns the value of this header as a string.
	 *
	 * @return The value of this header as a string, or <jk>null</jk> if header was not present.
	 */
	public String asString() {
		return getValue();
	}

	/**
	 * Returns the value of this header as a CSV array header.
	 *
	 * @return The value of this header as a CSV array header, or <jk>null</jk> if header was not present.
	 */
	public BasicCsvArrayHeader asCsvArrayHeader() {
		String v = getValue();
		return v == null ? null : new BasicCsvArrayHeader(getName(), v);
	}

	/**
	 * Returns the value of this header as a date header.
	 *
	 * @return The value of this header as a date header, or <jk>null</jk> if header was not present.
	 */
	public BasicDateHeader asDateHeader() {
		String v = getValue();
		return v == null ? null : new BasicDateHeader(getName(), v);
	}

	/**
	 * Returns the value of this header as an entity validator array header.
	 *
	 * @return The value of this header as an entity validator array header, or <jk>null</jk> if header was not present.
	 */
	public BasicEntityValidatorArrayHeader asEntityValidatorArrayHeader() {
		String v = getValue();
		return v == null ? null : new BasicEntityValidatorArrayHeader(getName(), v);
	}

	/**
	 * Returns the value of this header as an integer header.
	 *
	 * @return The value of this header as an integer header, or <jk>null</jk> if header was not present.
	 */
	public BasicIntegerHeader asIntegerHeader() {
		String v = getValue();
		return v == null ? null : new BasicIntegerHeader(getName(), v);
	}

	/**
	 * Returns the value of this header as a long header.
	 *
	 * @return The value of this header as a long header, or <jk>null</jk> if header was not present.
	 */
	public BasicLongHeader asLongHeader() {
		String v = getValue();
		return v == null ? null : new BasicLongHeader(getName(), v);
	}

	/**
	 * Returns the value of this header as a range array header.
	 *
	 * @return The value of this header as a range array header, or <jk>null</jk> if header was not present.
	 */
	public BasicRangeArrayHeader asRangeArrayHeader() {
		String v = getValue();
		return v == null ? null : new BasicRangeArrayHeader(getName(), v);
	}

	/**
	 * Returns the value of this header as a string header.
	 *
	 * @return The value of this header as a string header, or <jk>null</jk> if header was not present.
	 */
	public BasicHeader asHeader() {
		String v = getValue();
		return v == null ? null : new BasicHeader(getName(), v);
	}

	/**
	 * Returns the value of this header as a URI header.
	 *
	 * @return The value of this header as a URI header, or <jk>null</jk> if header was not present.
	 */
	public BasicUriHeader asUriHeader() {
		String v = getValue();
		return v == null ? null : new BasicUriHeader(getName(), v);
	}

	/**
	 * Same as {@link #asString()} but sets the value in a mutable for fluent calls.
	 *
	 * @param m The mutable to set the header value in.
	 * @return The response object (for method chaining).
	 */
	public RestResponse asString(Mutable<String> m) {
		m.set(asString());
		return response;
	}

	/**
	 * Returns the value of this header as an {@link Optional}.
	 *
	 * @return The value of this header as an {@link Optional}, or an empty optional if header was not present.
	 */
	public Optional<String> asOptionalString() {
		return Optional.ofNullable(getValue());
	}

	/**
	 * Returns the value of this header as a string with a default value.
	 *
	 * @param def The default value.
	 * @return The value of this header as a string, or the default value if header was not present.
	 */
	public String asStringOrElse(String def) {
		return getValue() == null ? def : getValue();
	}

	/**
	 * Same as {@link #asStringOrElse(String)} but sets the value in a mutable for fluent calls.
	 *
	 * @param m The mutable to set the header value in.
	 * @param def The default value.
	 * @return The response object (for method chaining).
	 */
	public RestResponse asStringOrElse(Mutable<String> m, String def) {
		m.set(asStringOrElse(def));
		return response;
	}

	/**
	 * Converts this header to the specified type.
	 *
	 * @param <T> The type to convert to.
	 * @param type The type to convert to.
	 * @param args The type parameters.
	 * @return The converted type, or <jk>null</jk> if header is not present.
	 * @throws RestCallException If value could not be parsed.
	 */
	public <T> T as(Type type, Type...args) throws RestCallException {
		return as(request.getClassMeta(type, args));
	}

	/**
	 * Same as {@link #as(Type,Type...)} but sets the value in a mutable for fluent calls.
	 *
	 * @param m The mutable to set the parsed header value in.
	 * @param <T> The type to convert to.
	 * @param type The type to convert to.
	 * @param args The type parameters.
	 * @return The response object (for method chaining).
	 * @throws RestCallException If value could not be parsed.
	 */
	public <T> RestResponse as(Mutable<T> m, Type type, Type...args) throws RestCallException {
		m.set(as(type, args));
		return response;
	}

	/**
	 * Converts this header to the specified type.
	 *
	 * @param <T> The type to convert to.
	 * @param type The type to convert to.
	 * @return The converted type, or <jk>null</jk> if header is not present.
	 * @throws RestCallException If value could not be parsed.
	 */
	public <T> T as(Class<T> type) throws RestCallException {
		return as(request.getClassMeta(type));
	}

	/**
	 * Same as {@link #as(Class)} but sets the value in a mutable for fluent calls.
	 *
	 * @param m The mutable to set the parsed header value in.
	 * @param <T> The type to convert to.
	 * @param type The type to convert to.
	 * @return The response object (for method chaining).
	 * @throws RestCallException If value could not be parsed.
	 */
	public <T> RestResponse as(Mutable<T> m, Class<T> type) throws RestCallException {
		m.set(as(type));
		return response;
	}

	/**
	 * Converts this header to the specified type.
	 *
	 * @param <T> The type to convert to.
	 * @param type The type to convert to.
	 * @return The converted type, or <jk>null</jk> if header is not present.
	 * @throws RestCallException If value could not be parsed.
	 */
	public <T> T as(ClassMeta<T> type) throws RestCallException {
		try {
			return parser.parse(HEADER, schema, asString(), type);
		} catch (ParseException e) {
			throw new RestCallException(e);
		}
	}

	/**
	 * Same as {@link #as(ClassMeta)} but sets the value in a mutable for fluent calls.
	 *
	 * @param m The mutable to set the parsed header value in.
	 * @param <T> The type to convert to.
	 * @param type The type to convert to.
	 * @return The response object (for method chaining).
	 * @throws RestCallException If value could not be parsed.
	 */
	public <T> RestResponse as(Mutable<T> m, ClassMeta<T> type) throws RestCallException {
		m.set(as(type));
		return response;
	}

	/**
	 * Same as {@link #as(Type,Type...)} but returns the value as an {@link Optional}.
	 *
	 * @param <T> The type to convert to.
	 * @param type The type to convert to.
	 * @param args The type parameters.
	 * @return The parsed value as an {@link Optional}, or an empty optional if header was not present.
	 * @throws RestCallException If value could not be parsed.
	 */
	public <T> Optional<T> asOptional(Type type, Type...args) throws RestCallException {
		return Optional.ofNullable(as(type, args));
	}

	/**
	 * Same as {@link #asOptional(Type,Type...)} but sets the value in a mutable for fluent calls.
	 *
	 * @param m The mutable to set the parsed header value in.
	 * @param <T> The type to convert to.
	 * @param type The type to convert to.
	 * @param args The type parameters.
	 * @return The response object (for method chaining).
	 * @throws RestCallException If value could not be parsed.
	 */
	public <T> RestResponse asOptional(Mutable<Optional<T>> m, Type type, Type...args) throws RestCallException {
		m.set(asOptional(type, args));
		return response;
	}

	/**
	 * Same as {@link #as(Class)} but returns the value as an {@link Optional}.
	 *
	 * @param <T> The type to convert to.
	 * @param type The type to convert to.
	 * @return The parsed value as an {@link Optional}, or an empty optional if header was not present.
	 * @throws RestCallException If value could not be parsed.
	 */
	public <T> Optional<T> asOptional(Class<T> type) throws RestCallException {
		return Optional.ofNullable(as(type));
	}

	/**
	 * Same as {@link #asOptional(Class)} but sets the value in a mutable for fluent calls.
	 *
	 * @param m The mutable to set the parsed header value in.
	 * @param <T> The type to convert to.
	 * @param type The type to convert to.
	 * @return The response object (for method chaining).
	 * @throws RestCallException If value could not be parsed.
	 */
	public <T> RestResponse asOptional(Mutable<Optional<T>> m, Class<T> type) throws RestCallException {
		m.set(asOptional(type));
		return response;
	}

	/**
	 * Same as {@link #as(ClassMeta)} but returns the value as an {@link Optional}.
	 *
	 * @param <T> The type to convert to.
	 * @param type The type to convert to.
	 * @return The parsed value as an {@link Optional}, or an empty optional if header was not present.
	 * @throws RestCallException If value could not be parsed.
	 */
	public <T> Optional<T> asOptional(ClassMeta<T> type) throws RestCallException {
		return Optional.ofNullable(as(type));
	}

	/**
	 * Same as {@link #asOptional(ClassMeta)} but sets the value in a mutable for fluent calls.
	 *
	 * @param m The mutable to set the parsed header value in.
	 * @param <T> The type to convert to.
	 * @param type The type to convert to.
	 * @return The response object (for method chaining).
	 * @throws RestCallException If value could not be parsed.
	 */
	public <T> RestResponse asOptional(Mutable<Optional<T>> m, ClassMeta<T> type) throws RestCallException {
		m.set(asOptional(type));
		return response;
	}

	/**
	 * Matches the specified pattern against this header value.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Parse header using a regular expression.</jc>
	 * 	Matcher m = client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.run()
	 * 		.getHeader(<js>"Content-Type"</js>).asMatcher(Pattern.<jsm>compile</jsm>(<js>"application/(.*)"</js>));
	 *
	 * 	<jk>if</jk> (m.matches())
	 * 		String mediaType = m.group(1);
	 * </p>
	 *
	 * @param pattern The regular expression pattern to match.
	 * @return The matcher.
	 * @throws RestCallException If a connection error occurred.
	 */
	public Matcher asMatcher(Pattern pattern) throws RestCallException {
		return pattern.matcher(asString());
	}

	/**
	 * Same as {@link #asMatcher(Pattern)} but sets the value in a mutable for fluent calls.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Parse header using a regular expression.</jc>
	 * 	Mutable&lt;Matcher&gt; m = Mutable.create();
	 * 	Matcher m = client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.run()
	 * 		.getHeader(<js>"Content-Type"</js>).asMatcher(m, Pattern.<jsm>compile</jsm>(<js>"application/(.*)"</js>));
	 *
	 * 	<jk>if</jk> (m.get().matches())
	 * 		String mediaType = m.get().group(1);
	 * </p>
	 *
	 * @param m The mutable to set the value in.
	 * @param pattern The regular expression pattern to match.
	 * @return The response object (for method chaining).
	 * @throws RestCallException If a connection error occurred.
	 */
	public RestResponse asMatcher(Mutable<Matcher> m, Pattern pattern) throws RestCallException {
		m.set(pattern.matcher(asString()));
		return response;
	}

	/**
	 * Matches the specified pattern against this header value.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Parse header using a regular expression.</jc>
	 * 	Matcher m = client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.run()
	 * 		.getHeader(<js>"Content-Type"</js>).asMatcher(<js>"application/(.*)"</js>);
	 *
	 * 	<jk>if</jk> (m.matches())
	 * 		String mediaType = m.group(1);
	 * </p>
	 *
	 * @param regex The regular expression pattern to match.
	 * @return The matcher.
	 * @throws RestCallException If a connection error occurred.
	 */
	public Matcher asMatcher(String regex) throws RestCallException {
		return asMatcher(regex, 0);
	}

	/**
	 * Same as {@link #asMatcher(String)} but sets the value in a mutable for fluent calls.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Parse header using a regular expression.</jc>
	 * 	Mutable&lt;Matcher&gt; m = Mutable.create();
	 * 	Matcher m = client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.run()
	 * 		.getHeader(<js>"Content-Type"</js>).asMatcher(m, <js>"application/(.*)"</js>);
	 *
	 * 	<jk>if</jk> (m.get().matches())
	 * 		String mediaType = m.get().group(1);
	 * </p>
	 *
	 * @param m The mutable to set the value in.
	 * @param regex The regular expression pattern to match.
	 * @return The response object (for method chaining).
	 * @throws RestCallException If a connection error occurred.
	 */
	public RestResponse asMatcher(Mutable<Matcher> m, String regex) throws RestCallException {
		asMatcher(regex, 0);
		return response;
	}

	/**
	 * Matches the specified pattern against this header value.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Parse header using a regular expression.</jc>
	 * 	Matcher m = client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.run()
	 * 		.getHeader(<js>"Content-Type"</js>).asMatcher(<js>"application/(.*)"</js>, <jsf>CASE_INSENSITIVE</jsf>);
	 *
	 * 	<jk>if</jk> (m.matches())
	 * 		String mediaType = m.group(1);
	 * </p>
	 *
	 * @param regex The regular expression pattern to match.
	 * @param flags Pattern match flags.  See {@link Pattern#compile(String, int)}.
	 * @return The matcher.
	 * @throws RestCallException If a connection error occurred.
	 */
	public Matcher asMatcher(String regex, int flags) throws RestCallException {
		return asMatcher(Pattern.compile(regex, flags));
	}

	/**
	 * Same as {@link #asMatcher(String,int)} but sets the value in a mutable for fluent calls.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Parse header using a regular expression.</jc>
	 * 	Mutable&lt;Matcher&gt; m = Mutable.create();
	 * 	Matcher m = client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.run()
	 * 		.getHeader(<js>"Content-Type"</js>).asMatcher(m, <js>"application/(.*)"</js>, <jsf>CASE_INSENSITIVE</jsf>);
	 *
	 * 	<jk>if</jk> (m.get().matches())
	 * 		String mediaType = m.get().group(1);
	 * </p>
	 *
	 * @param m The mutable to set the value in.
	 * @param regex The regular expression pattern to match.
	 * @param flags Pattern match flags.  See {@link Pattern#compile(String, int)}.
	 * @return The response object (for method chaining).
	 * @throws RestCallException If a connection error occurred.
	 */
	public RestResponse asMatcher(Mutable<Matcher> m, String regex, int flags) throws RestCallException {
		asMatcher(Pattern.compile(regex, flags));
		return response;
	}

	/**
	 * Returns the response that created this object.
	 *
	 * @return The response that created this object.
	 */
	public RestResponse toResponse() {
		return response;
	}

	//------------------------------------------------------------------------------------------------------------------
	// Assertions.
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Provides the ability to perform fluent-style assertions on this response header.
	 *
	 * <h5 class='section'>Examples:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Validates the content type header is provided.</jc>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.run()
	 * 		.assertHeader(<js>"Content-Type"</js>).exists();
	 *
	 * 	<jc>// Validates the content type is JSON.</jc>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.run()
	 * 		.assertHeader(<js>"Content-Type"</js>).equals(<js>"application/json"</js>);
	 *
	 * 	<jc>// Validates the content type is JSON using test predicate.</jc>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.run()
	 * 		.assertHeader(<js>"Content-Type"</js>).passes(x -&gt; x.equals(<js>"application/json"</js>));
	 *
	 * 	<jc>// Validates the content type is JSON by just checking for substring.</jc>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.run()
	 * 		.assertHeader(<js>"Content-Type"</js>).contains(<js>"json"</js>);
	 *
	 * 	<jc>// Validates the content type is JSON using regular expression.</jc>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.run()
	 * 		.assertHeader(<js>"Content-Type"</js>).matches(<js>".*json.*"</js>);
	 *
	 * 	<jc>// Validates the content type is JSON using case-insensitive regular expression.</jc>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.run()
	 * 		.assertHeader(<js>"Content-Type"</js>).matches(<js>".*json.*"</js>, <jsf>CASE_INSENSITIVE</jsf>);
	 * </p>
	 *
	 * <p>
	 * The assertion test returns the original response object allowing you to chain multiple requests like so:
	 * <p class='bcode w800'>
	 * 	<jc>// Validates the header and converts it to a bean.</jc>
	 * 	MediaType mediaType = client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.run()
	 * 		.assertHeader(<js>"Content-Type"</js>).exists()
	 * 		.assertHeader(<js>"Content-Type"</js>).matches(<js>".*json.*"</js>)
	 * 		.getHeader(<js>"Content-Type"</js>).as(MediaType.<jk>class</jk>);
	 * </p>
	 *
	 * @return A new fluent assertion object.
	 * @throws RestCallException If REST call failed.
	 * @throws AssertionError If assertion failed.
	 */
	public FluentStringAssertion<RestResponse> assertThat() throws RestCallException {
		return new FluentStringAssertion<>(asString(), response);
	}

	/**
	 * Provides the ability to perform fluent-style assertions on an integer response header.
	 *
	 * <h5 class='section'>Examples:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Validates that the response content age is greather than 1.</jc>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.run()
	 * 		.assertIntegerHeader(<js>"Age"</js>).isGreaterThan(1);
	 * </p>
	 *
	 * @return A new fluent assertion object.
	 * @throws RestCallException If REST call failed.
	 */
	public FluentIntegerAssertion<RestResponse> assertThatInteger() throws RestCallException {
		BasicIntegerHeader h = asIntegerHeader();
		return new FluentIntegerAssertion<>(h == null ? -1 : h.asInt(), response);
	}

	/**
	 * Provides the ability to perform fluent-style assertions on a long response header.
	 *
	 * <h5 class='section'>Examples:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Validates that the response body is not too long.</jc>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.run()
	 * 		.assertLongHeader(<js>"Length"</js>).isLessThan(100000);
	 * </p>
	 *
	 * @return A new fluent assertion object.
	 * @throws RestCallException If REST call failed.
	 */
	public FluentLongAssertion<RestResponse> assertThatLong() throws RestCallException {
		BasicLongHeader h = asLongHeader();
		return new FluentLongAssertion<>(h == null ? -1 : h.asLong(), response);
	}

	/**
	 * Provides the ability to perform fluent-style assertions on a date response header.
	 *
	 * <h5 class='section'>Examples:</h5>
	 * <p class='bcode w800'>
	 * 	<jc>// Validates that the response content is not expired.</jc>
	 * 	client
	 * 		.get(<jsf>URL</jsf>)
	 * 		.run()
	 * 		.assertDateHeader(<js>"Expires"</js>).isAfter(<jk>new</jk> Date());
	 * </p>
	 *
	 * @return A new fluent assertion object.
	 * @throws RestCallException If REST call failed.
	 */
	public FluentDateAssertion<RestResponse> assertThatDate() throws RestCallException {
		BasicDateHeader h = asDateHeader();
		return new FluentDateAssertion<>(h == null ? null : h.asDate(), response);
	}

	//------------------------------------------------------------------------------------------------------------------
	// Header passthrough methods.
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Gets the name of this pair.
	 *
	 * @return The name of this pair, never <jk>null</jk>.
	 */
	@Override /* Header */
	public String getName() {
		return header.getName();
	}

	/**
	 * Gets the value of this pair.
	 *
	 * <ul class='notes'>
	 * 	<li>{@link #asString()} is an equivalent method and the preferred method for fluent-style coding.
	 * </ul>
	 *
	 * @return The value of this pair, may be <jk>null</jk>.
	 */
	@Override /* Header */
	public String getValue() {
		return header.getValue();
	}

	/**
	 * Parses the value.
	 *
	 * @return An array of {@link HeaderElement} entries, may be empty, but is never <jk>null</jk>.
	 * @throws org.apache.http.ParseException In case of a parsing error.
	 */
	@Override /* Header */
	public HeaderElement[] getElements() throws org.apache.http.ParseException {
		return header.getElements();
	}

	@Override /* Object */
	public String toString() {
		return getName() + ": " + getValue();
	}
}
