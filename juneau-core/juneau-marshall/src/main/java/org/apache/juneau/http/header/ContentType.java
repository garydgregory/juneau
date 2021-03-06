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
package org.apache.juneau.http.header;

import static org.apache.juneau.http.header.Constants.*;
import static org.apache.juneau.internal.StringUtils.*;

import java.util.function.*;

import org.apache.juneau.http.annotation.Header;
import org.apache.juneau.internal.*;

/**
 * Represents a parsed <l>Content-Type</l> HTTP request/response header.
 *
 * <p>
 * The MIME type of this content.
 *
 * <h5 class='figure'>Example</h5>
 * <p class='bcode w800'>
 * 	Content-Type: text/html; charset=utf-8
 * </p>
 *
 * <h5 class='topic'>RFC2616 Specification</h5>
 *
 * The Content-Type entity-header field indicates the media type of the entity-body sent to the recipient or, in the
 * case of the HEAD method, the media type that would have been sent had the request been a GET.
 * <p class='bcode w800'>
 * 	Content-Type   = "Content-Type" ":" media-type
 * </p>
 *
 * <p>
 * Media types are defined in section 3.7.
 * An example of the field is...
 * <p class='bcode w800'>
 * 	Content-Type: text/html; charset=ISO-8859-4
 * </p>
 *
 * <ul class='seealso'>
 * 	<li class='extlink'>{@doc ExtRFC2616}
 * </ul>
 */
@Header("Content-Type")
public class ContentType extends BasicMediaTypeHeader {

	private static final long serialVersionUID = 1L;

	private static Cache<String,ContentType> CACHE = new Cache<>(NOCACHE, CACHE_MAX_SIZE);

	// Constants
	@SuppressWarnings("javadoc")
	public static final ContentType
		APPLICATION_ATOM_XML = of("application/atom+xml"),
		APPLICATION_FORM_URLENCODED = of("application/x-www-form-urlencoded"),
		APPLICATION_JSON = of("application/json"),
		APPLICATION_OCTET_STREAM = of("application/octet-stream"),
		APPLICATION_SOAP_XML = of("application/soap+xml"),
		APPLICATION_SVG_XML = of("application/svg+xml"),
		APPLICATION_XHTML_XML = of("application/xhtml+xml"),
		APPLICATION_XML = of("application/xml"),
		IMAGE_BMP = of("image/bmp"),
		IMAGE_GIF = of("image/gif"),
		IMAGE_JPEG = of("image/jpeg"),
		IMAGE_PNG = of("image/png"),
		IMAGE_SVG = of("image/svg+xml"),
		IMAGE_TIFF = of("image/tiff"),
		IMAGE_WEBP = of("image/webp"),
		MULTIPART_FORM_DATA = of("multipart/form-data"),
		TEXT_HTML = of("text/html"),
		TEXT_PLAIN = of("text/plain"),
		TEXT_XML = of("text/xml"),
		WILDCARD = of("*/*");

	/**
	 * Returns a parsed and cached <c>Content-Type</c> header.
	 *
	 * @param value The <c>Content-Type</c> header string.
	 * @return The parsed <c>Content-Type</c> header, or <jk>null</jk> if the string was null.
	 */
	public static ContentType of(String value) {
		if (isEmpty(value))
			return null;
		ContentType ct = CACHE.get(value);
		if (ct == null)
			ct = CACHE.put(value, new ContentType(value));
		return ct;
	}

	/**
	 * Returns a parsed and cached <c>Content-Type</c> header.
	 *
	 * @param value The <c>Content-Type</c> header value.
	 * @return The parsed <c>Content-Type</c> header, or <jk>null</jk> if the string was null.
	 */
	public static ContentType of(MediaType value) {
		if (isEmpty(value))
			return null;
		return of(value.toString());
	}

	/**
	 * Convenience creator.
	 *
	 * @param value
	 * 	The header value.
	 * 	<br>Can be any of the following:
	 * 	<ul>
	 * 		<li>{@link String} - Converted using {@link MediaType#of(String)}.
	 * 		<li>{@link MediaType}.
	 * 		<li>Anything else - Converted to <c>String</c> using {@link Object#toString()} and then parsed.
	 * 	</ul>
	 * @return A new {@link AcceptEncoding} object.
	 */
	public static ContentType of(Object value) {
		if (isEmpty(value))
			return null;
		return new ContentType(value);
	}

	/**
	 * Convenience creator using supplier.
	 *
	 * <p>
	 * Header value is re-evaluated on each call to {@link #getValue()}.
	 *
	 * @param value
	 * 	The header value supplier.
	 * 	<br>Can be any of the following:
	 * 	<ul>
	 * 		<li>{@link String} - Converted using {@link MediaType#of(String)}.
	 * 		<li>{@link MediaType}.
	 * 		<li>Anything else - Converted to <c>String</c> using {@link Object#toString()} and then parsed.
	 * 	</ul>
	 * @return A new {@link AcceptEncoding} object.
	 */
	public static ContentType of(Supplier<?> value) {
		return new ContentType(value);
	}

	/**
	 * Constructor.
	 *
	 * @param value The value for this header.
	 */
	public ContentType(Object value) {
		super("Content-Type", value);
	}

	/**
	 * Constructor.
	 *
	 * @param value The value for this header.
	 */
	public ContentType(String value) {
		this((Object)value);
	}
}
