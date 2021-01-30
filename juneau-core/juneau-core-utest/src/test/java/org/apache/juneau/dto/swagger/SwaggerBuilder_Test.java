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
package org.apache.juneau.dto.swagger;

import static org.apache.juneau.assertions.Assertions.*;
import static org.apache.juneau.dto.swagger.SwaggerBuilder.*;
import static org.junit.runners.MethodSorters.*;

import org.junit.*;

/**
 * Testcase for {@link SwaggerBuilder}.
 */
@FixMethodOrder(NAME_ASCENDING)
public class SwaggerBuilder_Test {

	@Test
	public void a01_contact() {
		Contact t = contact();
		assertObject(t).json().is("{}");

		t = contact("foo");
		assertObject(t).json().is("{name:'foo'}");

		t = contact("foo", "bar", "baz");
		assertObject(t).json().is("{name:'foo',url:'bar',email:'baz'}");
	}

	@Test
	public void a02_externalDocumentation() {
		ExternalDocumentation t = externalDocumentation();
		assertObject(t).json().is("{}");

		t = externalDocumentation("foo");
		assertObject(t).json().is("{url:'foo'}");

		t = externalDocumentation("foo", "bar");
		assertObject(t).json().is("{description:'bar',url:'foo'}");
	}

	@Test
	public void a03_headerInfo() {
		HeaderInfo t = headerInfo();
		assertObject(t).json().is("{}");

		t = headerInfo("foo");
		assertObject(t).json().is("{type:'foo'}");

		t = headerInfoStrict("string");
		assertObject(t).json().is("{type:'string'}");
		assertThrown(()->headerInfoStrict("foo")).is("Invalid value passed in to setType(String).  Value='foo', valid values=['string','number','integer','boolean','array']");
	}

	@Test
	public void a04_info() {
		Info t = info();
		assertObject(t).json().is("{}");

		t = info("foo", "bar");
		assertObject(t).json().is("{title:'foo',version:'bar'}");
	}

	@Test
	public void a05_items() {
		Items t = items();
		assertObject(t).json().is("{}");

		t = items("foo");
		assertObject(t).json().is("{type:'foo'}");

		t = itemsStrict("string");
		assertObject(t).json().is("{type:'string'}");
		assertThrown(()->itemsStrict("foo")).is("Invalid value passed in to setType(String).  Value='foo', valid values=['string','number','integer','boolean','array']");
	}

	@Test
	public void a06_license() {
		License t = license();
		assertObject(t).json().is("{}");

		t = license("foo");
		assertObject(t).json().is("{name:'foo'}");
	}

	@Test
	public void a07_operation() {
		Operation t = operation();
		assertObject(t).json().is("{}");
	}

	@Test
	public void a08_parameterInfo() {
		ParameterInfo t = parameterInfo();
		assertObject(t).json().is("{}");

		t = parameterInfo("foo", "bar");
		assertObject(t).json().is("{'in':'foo',name:'bar'}");

		t = parameterInfoStrict("query", "bar");
		assertObject(t).json().is("{'in':'query',name:'bar'}");
		assertThrown(()->parameterInfoStrict("foo", "bar")).is("Invalid value passed in to setIn(String).  Value='foo', valid values=['query','header','path','formData','body']");
	}

	@Test
	public void a09_responseInfo() {
		ResponseInfo t = responseInfo();
		assertObject(t).json().is("{}");

		t = responseInfo("foo");
		assertObject(t).json().is("{description:'foo'}");
	}

	@Test
	public void a10_schemaInfo() {
		SchemaInfo t = schemaInfo();
		assertObject(t).json().is("{}");
	}

	@Test
	public void a11_securityScheme() {
		SecurityScheme t = securityScheme();
		assertObject(t).json().is("{}");

		t = securityScheme("foo");
		assertObject(t).json().is("{type:'foo'}");

		t = securityScheme("foo");
		assertObject(t).json().is("{type:'foo'}");
	}

	@Test
	public void a12_swagger() {
		Swagger t = swagger();
		assertObject(t).json().is("{swagger:'2.0'}");

		t = swagger(info());
		assertObject(t).json().is("{swagger:'2.0',info:{}}");
	}

	@Test
	public void a13_tag() {
		Tag t = tag();
		assertObject(t).json().is("{}");

		t = tag("foo");
		assertObject(t).json().is("{name:'foo'}");
	}

	@Test
	public void a14_xml() {
		Xml t = xml();
		assertObject(t).json().is("{}");
	}
}
