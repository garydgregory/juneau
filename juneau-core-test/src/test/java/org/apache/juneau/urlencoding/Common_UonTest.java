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
package org.apache.juneau.urlencoding;

import static org.apache.juneau.TestUtils.*;
import static org.junit.Assert.*;

import java.net.*;
import java.net.URI;
import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.annotation.*;
import org.apache.juneau.serializer.*;
import org.apache.juneau.testbeans.*;
import org.junit.*;

@SuppressWarnings({"serial","javadoc"})
public class Common_UonTest {
	UonParser p = UonParser.DEFAULT;
	UonParser pe = UonParser.DEFAULT_DECODING;

	//====================================================================================================
	// Trim nulls from beans
	//====================================================================================================
	@Test
	public void testTrimNullsFromBeans() throws Exception {
		UonSerializer s = new UonSerializer.Encoding();
		A t1 = A.create(), t2;

		s.setTrimNullProperties(false);
		String r = s.serialize(t1);
		assertEquals("(s1=null,s2=s2)", r);
		t2 = pe.parse(r, A.class);
		assertEqualObjects(t1, t2);

		s.setTrimNullProperties(true);
		r = s.serialize(t1);
		assertEquals("(s2=s2)", r);
		t2 = p.parse(r, A.class);
		assertEqualObjects(t1, t2);
	}

	public static class A {
		public String s1, s2;

		public static A create() {
			A t = new A();
			t.s2 = "s2";
			return t;
		}
	}

	//====================================================================================================
	// Trim empty maps
	//====================================================================================================
	@Test
	public void testTrimEmptyMaps() throws Exception {
		UonSerializer s = UonSerializer.DEFAULT_ENCODING.clone();
		B t1 = B.create(), t2;
		String r;

		s.setTrimEmptyMaps(false);
		r = s.serialize(t1);
		assertEquals("(f1=(),f2=(f2a=null,f2b=(s2=s2)))", r);
		t2 = pe.parse(r, B.class);
		assertEqualObjects(t1, t2);

		s.setTrimEmptyMaps(true);
		r = s.serialize(t1);
		assertEquals("(f2=(f2a=null,f2b=(s2=s2)))", r);
		t2 = pe.parse(r, B.class);
		assertNull(t2.f1);
	}

	public static class B {
		public TreeMap<String,A> f1, f2;

		public static B create() {
			B t = new B();
			t.f1 = new TreeMap<String,A>();
			t.f2 = new TreeMap<String,A>(){{put("f2a",null);put("f2b",A.create());}};
			return t;
		}
	}

	//====================================================================================================
	// Trim empty lists
	//====================================================================================================
	@Test
	public void testTrimEmptyLists() throws Exception {
		UonSerializer s = new UonSerializer.Encoding();
		C t1 = C.create(), t2;
		String r;

		s.setTrimEmptyCollections(false);
		r = s.serialize(t1);
		assertEquals("(f1=@(),f2=@(null,(s2=s2)))", r);
		t2 = pe.parse(r, C.class);
		assertEqualObjects(t1, t2);

		s.setTrimEmptyCollections(true);
		r = s.serialize(t1);
		assertEquals("(f2=@(null,(s2=s2)))", r);
		t2 = pe.parse(r, C.class);
		assertNull(t2.f1);
	}

	public static class C {
		public List<A> f1, f2;

		public static C create() {
			C t = new C();
			t.f1 = new LinkedList<A>();
			t.f2 = new LinkedList<A>(){{add(null);add(A.create());}};
			return t;
		}
	}

	//====================================================================================================
	// Trim empty arrays
	//====================================================================================================
	@Test
	public void testTrimEmptyArrays() throws Exception {
		UonSerializer s = new UonSerializer.Encoding();
		D t1 = D.create(), t2;
		String r;

		s.setTrimEmptyCollections(false);
		r = s.serialize(t1);
		assertEquals("(f1=@(),f2=@(null,(s2=s2)))", r);
		t2 = pe.parse(r, D.class);
		assertEqualObjects(t1, t2);

		s.setTrimEmptyCollections(true);
		r = s.serialize(t1);
		assertEquals("(f2=@(null,(s2=s2)))", r);
		t2 = pe.parse(r, D.class);
		assertNull(t2.f1);
	}

	public static class D {
		public A[] f1, f2;

		public static D create() {
			D t = new D();
			t.f1 = new A[]{};
			t.f2 = new A[]{null, A.create()};
			return t;
		}
	}

	//====================================================================================================
	// @BeanProperty.properties annotation.
	//====================================================================================================
	@Test
	public void testBeanPropertyProperies() throws Exception {
		UonSerializer s = UonSerializer.DEFAULT;
		String ue = s.serialize(new E1());
		assertEquals("(x1=(f1=1),x2=(f1=1),x3=@((f1=1)),x4=@((f1=1)),x5=@((f1=1)),x6=@((f1=1)))", ue);
	}

	public static class E1 {
		@BeanProperty(properties="f1") public E2 x1 = new E2();
		@BeanProperty(properties="f1") public Map<String,Integer> x2 = new LinkedHashMap<String,Integer>() {{
			put("f1",1); put("f2",2);
		}};
		@BeanProperty(properties="f1") public E2[] x3 = {new E2()};
		@BeanProperty(properties="f1") public List<E2> x4 = new LinkedList<E2>() {{
			add(new E2());
		}};
		@BeanProperty(properties="f1") public ObjectMap[] x5 = {new ObjectMap().append("f1",1).append("f2",2)};
		@BeanProperty(properties="f1") public List<ObjectMap> x6 = new LinkedList<ObjectMap>() {{
			add(new ObjectMap().append("f1",1).append("f2",2));
		}};
	}

	public static class E2 {
		public int f1 = 1;
		public int f2 = 2;
	}

	//====================================================================================================
	// @BeanProperty.properties annotation on list of beans.
	//====================================================================================================
	@Test
	public void testBeanPropertyPropertiesOnListOfBeans() throws Exception {
		UonSerializer s = UonSerializer.DEFAULT;
		List<F> l = new LinkedList<F>();
		F t = new F();
		t.x1.add(new F());
		l.add(t);
		String xml = s.serialize(l);
		assertEquals("@((x1=@((x2=2)),x2=2))", xml);
	}

	public static class F {
		@BeanProperty(properties="x2") public List<F> x1 = new LinkedList<F>();
		public int x2 = 2;
	}

	//====================================================================================================
	// Test URIAttr - Test that URLs and URIs are serialized and parsed correctly.
	//====================================================================================================
	@Test
	public void testURIAttr() throws Exception {
		UonSerializer s = UonSerializer.DEFAULT;
		UonParser p = UonParser.DEFAULT;

		G t = new G();
		t.uri = new URI("http://uri");
		t.f1 = new URI("http://f1");
		t.f2 = new URL("http://f2");

		String r = s.serialize(t);
		t = p.parse(r, G.class);
		assertEquals("http://uri", t.uri.toString());
		assertEquals("http://f1", t.f1.toString());
		assertEquals("http://f2", t.f2.toString());
	}

	public static class G {
		public URI uri;
		public URI f1;
		public URL f2;
	}

	//====================================================================================================
	// Test URIs with URI_CONTEXT and URI_AUTHORITY
	//====================================================================================================
	@Test
	public void testUris() throws Exception {
		WriterSerializer s = new UonSerializer();
		TestURI t = new TestURI();
		String r;
		String expected;

		s.setRelativeUriBase(null);
		r = s.serialize(t);
		expected = ""
			+"("
			+"f0=f0/x0,"
			+"f1=f1/x1,"
			+"f2=/f2/x2,"
			+"f3=http://www.apache.org/f3/x3,"
			+"f4=f4/x4,"
			+"f5=/f5/x5,"
			+"f6=http://www.apache.org/f6/x6,"
			+"f7=http://www.apache.org/f7/x7,"
			+"f8=f8/x8,"
			+"f9=f9/x9,"
			+"fa=http://www.apache.org/fa/xa#MY_LABEL,"
			+"fb='http://www.apache.org/fb/xb?label=MY_LABEL&foo=bar',"
			+"fc='http://www.apache.org/fc/xc?foo=bar&label=MY_LABEL',"
			+"fd='http://www.apache.org/fd/xd?label2=MY_LABEL&foo=bar',"
			+"fe='http://www.apache.org/fe/xe?foo=bar&label2=MY_LABEL'"
			+")";
		assertEquals(expected, r);

		s.setRelativeUriBase("");  // Same as null.
		r = s.serialize(t);
		assertEquals(expected, r);

		s.setRelativeUriBase("/cr");
		r = s.serialize(t);
		expected = ""
			+"("
			+"f0=/cr/f0/x0,"
			+"f1=/cr/f1/x1,"
			+"f2=/f2/x2,"
			+"f3=http://www.apache.org/f3/x3,"
			+"f4=/cr/f4/x4,"
			+"f5=/f5/x5,"
			+"f6=http://www.apache.org/f6/x6,"
			+"f7=http://www.apache.org/f7/x7,"
			+"f8=/cr/f8/x8,"
			+"f9=/cr/f9/x9,"
			+"fa=http://www.apache.org/fa/xa#MY_LABEL,"
			+"fb='http://www.apache.org/fb/xb?label=MY_LABEL&foo=bar',"
			+"fc='http://www.apache.org/fc/xc?foo=bar&label=MY_LABEL',"
			+"fd='http://www.apache.org/fd/xd?label2=MY_LABEL&foo=bar',"
			+"fe='http://www.apache.org/fe/xe?foo=bar&label2=MY_LABEL'"
			+")";
		assertEquals(expected, r);

		s.setRelativeUriBase("/cr/");  // Same as above
		r = s.serialize(t);
		assertEquals(expected, r);

		s.setRelativeUriBase("/");
		r = s.serialize(t);
		expected = ""
			+"("
			+"f0=/f0/x0,"
			+"f1=/f1/x1,"
			+"f2=/f2/x2,"
			+"f3=http://www.apache.org/f3/x3,"
			+"f4=/f4/x4,"
			+"f5=/f5/x5,"
			+"f6=http://www.apache.org/f6/x6,"
			+"f7=http://www.apache.org/f7/x7,"
			+"f8=/f8/x8,"
			+"f9=/f9/x9,"
			+"fa=http://www.apache.org/fa/xa#MY_LABEL,"
			+"fb='http://www.apache.org/fb/xb?label=MY_LABEL&foo=bar',"
			+"fc='http://www.apache.org/fc/xc?foo=bar&label=MY_LABEL',"
			+"fd='http://www.apache.org/fd/xd?label2=MY_LABEL&foo=bar',"
			+"fe='http://www.apache.org/fe/xe?foo=bar&label2=MY_LABEL'"
			+")";
		assertEquals(expected, r);

		s.setRelativeUriBase(null);

		s.setAbsolutePathUriBase("http://foo");
		r = s.serialize(t);
		expected = ""
			+"("
			+"f0=f0/x0,"
			+"f1=f1/x1,"
			+"f2=http://foo/f2/x2,"
			+"f3=http://www.apache.org/f3/x3,"
			+"f4=f4/x4,"
			+"f5=http://foo/f5/x5,"
			+"f6=http://www.apache.org/f6/x6,"
			+"f7=http://www.apache.org/f7/x7,"
			+"f8=f8/x8,"
			+"f9=f9/x9,"
			+"fa=http://www.apache.org/fa/xa#MY_LABEL,"
			+"fb='http://www.apache.org/fb/xb?label=MY_LABEL&foo=bar',"
			+"fc='http://www.apache.org/fc/xc?foo=bar&label=MY_LABEL',"
			+"fd='http://www.apache.org/fd/xd?label2=MY_LABEL&foo=bar',"
			+"fe='http://www.apache.org/fe/xe?foo=bar&label2=MY_LABEL'"
			+")";
		assertEquals(expected, r);

		s.setAbsolutePathUriBase("http://foo/");
		r = s.serialize(t);
		assertEquals(expected, r);

		s.setAbsolutePathUriBase("");  // Same as null.
		r = s.serialize(t);
		expected = ""
			+"("
			+"f0=f0/x0,"
			+"f1=f1/x1,"
			+"f2=/f2/x2,"
			+"f3=http://www.apache.org/f3/x3,"
			+"f4=f4/x4,"
			+"f5=/f5/x5,"
			+"f6=http://www.apache.org/f6/x6,"
			+"f7=http://www.apache.org/f7/x7,"
			+"f8=f8/x8,"
			+"f9=f9/x9,"
			+"fa=http://www.apache.org/fa/xa#MY_LABEL,"
			+"fb='http://www.apache.org/fb/xb?label=MY_LABEL&foo=bar',"
			+"fc='http://www.apache.org/fc/xc?foo=bar&label=MY_LABEL',"
			+"fd='http://www.apache.org/fd/xd?label2=MY_LABEL&foo=bar',"
			+"fe='http://www.apache.org/fe/xe?foo=bar&label2=MY_LABEL'"
			+")";
		assertEquals(expected, r);
	}

	//====================================================================================================
	// Validate that you cannot update properties on locked serializer.
	//====================================================================================================
	@Test
	public void testLockedSerializer() throws Exception {
		UonSerializer s = new UonSerializer().lock();
		try {
			s.setUseWhitespace(true);
			fail("Locked exception not thrown");
		} catch (LockedException e) {}
		try {
			s.setAddBeanTypeProperties(true);
			fail("Locked exception not thrown");
		} catch (LockedException e) {}
		try {
			s.setBeanMapPutReturnsOldValue(true);
			fail("Locked exception not thrown");
		} catch (LockedException e) {}
	}

	//====================================================================================================
	// Recursion
	//====================================================================================================
	@Test
	public void testRecursion() throws Exception {
		WriterSerializer s = new UonSerializer();

		R1 r1 = new R1();
		R2 r2 = new R2();
		R3 r3 = new R3();
		r1.r2 = r2;
		r2.r3 = r3;
		r3.r1 = r1;

		// No recursion detection
		try {
			s.serialize(r1);
			fail("Exception expected!");
		} catch (Exception e) {
			String msg = e.getLocalizedMessage();
			assertTrue(msg.contains("It's recommended you use the SerializerContext.SERIALIZER_detectRecursions setting to help locate the loop."));
		}

		// Recursion detection, no ignore
		s.setDetectRecursions(true);
		try {
			s.serialize(r1);
			fail("Exception expected!");
		} catch (Exception e) {
			String msg = e.getLocalizedMessage();
			assertTrue(msg.contains("[0]root:org.apache.juneau.urlencoding.Common_UonTest$R1"));
			assertTrue(msg.contains("->[1]r2:org.apache.juneau.urlencoding.Common_UonTest$R2"));
			assertTrue(msg.contains("->[2]r3:org.apache.juneau.urlencoding.Common_UonTest$R3"));
			assertTrue(msg.contains("->[3]r1:org.apache.juneau.urlencoding.Common_UonTest$R1"));
		}

		s.setIgnoreRecursions(true);
		assertEquals("(name=foo,r2=(name=bar,r3=(name=baz)))", s.serialize(r1));
	}

	public static class R1 {
		public String name = "foo";
		public R2 r2;
	}
	public static class R2 {
		public String name = "bar";
		public R3 r3;
	}
	public static class R3 {
		public String name = "baz";
		public R1 r1;
	}
}