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
package org.apache.juneau.dto.html5;

import org.apache.juneau.annotation.*;

/**
 * DTO for an HTML {@doc ExtHTML5.embedded-content-0#the-object-element <object>}
 * element.
 *
 * <ul class='seealso'>
 * 	<li class='link'>{@doc DtoHtml5}
 * </ul>
 */
@Bean(typeName="object")
public class Object_ extends HtmlElementMixed {

	/**
	 * Creates an empty {@link Object_} element.
	 */
	public Object_() {}

	/**
	 * Creates an {@link Object_} element with the specified child nodes.
	 *
	 * @param children The child nodes.
	 */
	public Object_(Object...children) {
		children(children);
	}

	/**
	 * {@doc ExtHTML5.embedded-content-0#attr-object-data data} attribute.
	 *
	 * <p>
	 * Address of the resource.
	 *
	 * @param data The new value for this attribute.
	 * @return This object (for method chaining).
	 */
	public final Object_ data(String data) {
		attr("data", data);
		return this;
	}

	/**
	 * {@doc ExtHTML5.forms#attr-fae-form form} attribute.
	 *
	 * <p>
	 * Associates the control with a form element.
	 *
	 * @param form The new value for this attribute.
	 * @return This object (for method chaining).
	 */
	public final Object_ form(String form) {
		attr("form", form);
		return this;
	}

	/**
	 * {@doc ExtHTML5.embedded-content-0#attr-dim-height height}
	 * attribute.
	 *
	 * <p>
	 * Vertical dimension.
	 *
	 * @param height
	 * 	The new value for this attribute.
	 * 	Typically a {@link Number} or {@link String}.
	 * @return This object (for method chaining).
	 */
	public final Object_ height(Object height) {
		attr("height", height);
		return this;
	}

	/**
	 * {@doc ExtHTML5.embedded-content-0#attr-object-name name} attribute.
	 *
	 * <p>
	 * Name of nested browsing context.
	 *
	 * @param name The new value for this attribute.
	 * @return This object (for method chaining).
	 */
	public final Object_ name(String name) {
		attr("name", name);
		return this;
	}

	/**
	 * {@doc ExtHTML5.embedded-content-0#attr-object-type type} attribute.
	 *
	 * <p>
	 * Type of embedded resource.
	 *
	 * @param type The new value for this attribute.
	 * @return This object (for method chaining).
	 */
	public final Object_ type(String type) {
		attr("type", type);
		return this;
	}

	/**
	 * {@doc ExtHTML5.embedded-content-0#attr-object-typemustmatch typemustmatch}
	 * attribute.
	 *
	 * <p>
	 * Whether the type attribute and the Content-Type value need to match for the resource to be used.
	 *
	 * @param typemustmatch
	 * 	The new value for this attribute.
	 * 	Typically a {@link Boolean} or {@link String}.
	 * @return This object (for method chaining).
	 */
	public final Object_ typemustmatch(Object typemustmatch) {
		attr("typemustmatch", typemustmatch);
		return this;
	}

	/**
	 * {@doc ExtHTML5.embedded-content-0#attr-hyperlink-usemap usemap}
	 * attribute.
	 *
	 * <p>
	 * Name of image map to use.
	 *
	 * @param usemap The new value for this attribute.
	 * @return This object (for method chaining).
	 */
	public final Object_ usemap(String usemap) {
		attr("usemap", usemap);
		return this;
	}

	/**
	 * {@doc ExtHTML5.embedded-content-0#attr-dim-width width} attribute.
	 *
	 * <p>
	 * Horizontal dimension.
	 *
	 * @param width
	 * 	The new value for this attribute.
	 * 	Typically a {@link Number} or {@link String}.
	 * @return This object (for method chaining).
	 */
	public final Object_ width(Object width) {
		attr("width", width);
		return this;
	}


	//-----------------------------------------------------------------------------------------------------------------
	// Overridden methods
	//-----------------------------------------------------------------------------------------------------------------

	@Override /* HtmlElement */
	public final Object_ _class(String _class) {
		super._class(_class);
		return this;
	}

	@Override /* HtmlElement */
	public final Object_ id(String id) {
		super.id(id);
		return this;
	}

	@Override /* HtmlElement */
	public final Object_ style(String style) {
		super.style(style);
		return this;
	}

	@Override /* HtmlElementMixed */
	public Object_ children(Object...children) {
		super.children(children);
		return this;
	}

	@Override /* HtmlElementMixed */
	public Object_ child(Object child) {
		super.child(child);
		return this;
	}
}
