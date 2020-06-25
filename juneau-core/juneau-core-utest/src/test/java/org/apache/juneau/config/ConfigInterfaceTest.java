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
package org.apache.juneau.config;

import static org.apache.juneau.assertions.ObjectAssertion.*;
import static org.apache.juneau.testutils.TestUtils.*;
import static org.junit.Assert.*;
import static org.junit.runners.MethodSorters.*;

import java.util.*;

import org.apache.juneau.collections.*;
import org.apache.juneau.json.*;
import org.apache.juneau.testutils.pojos.*;
import org.junit.*;

@FixMethodOrder(NAME_ASCENDING)
public class ConfigInterfaceTest {

	Config cf;
	ConfigInterface proxy;

	public ConfigInterfaceTest() throws Exception {
		cf = Config.create().serializer(SimpleJsonSerializer.DEFAULT.builder().addBeanTypes().addRootType().build()).build();
		proxy = cf.getSectionAsInterface("A", ConfigInterface.class);
	}


	//====================================================================================================
	// getSectionAsInterface(String,Class)
	//====================================================================================================

	@Test
	public void testString() throws Exception {
		proxy.setString("foo");
		assertEquals("foo", proxy.getString());
		assertEquals("foo", cf.get("A/string"));
	}

	@Test
	public void testInt() throws Exception {
		proxy.setInt(1);
		assertEquals(1, proxy.getInt());
		assertEquals("1", cf.get("A/int"));
	}

	@Test
	public void testInteger() throws Exception {
		proxy.setInteger(2);
		assertEquals(2, proxy.getInteger().intValue());
		assertEquals("2", cf.get("A/integer"));
		assertInstanceOf(Integer.class, proxy.getInteger());
	}

	@Test
	public void testBoolean() throws Exception {
		proxy.setBoolean(true);
		assertEquals(true, proxy.isBoolean());
		assertEquals("true", cf.get("A/boolean"));
	}

	@Test
	public void testBooleanObject() throws Exception {
		proxy.setBooleanObject(true);
		assertEquals(true, proxy.getBooleanObject().booleanValue());
		assertEquals("true", cf.get("A/booleanObject"));
		assertInstanceOf(Boolean.class, proxy.getBooleanObject());
	}

	@Test
	public void testFloat() throws Exception {
		proxy.setFloat(1f);
		assertTrue(1f == proxy.getFloat());
		assertEquals("1.0", cf.get("A/float"));
	}

	@Test
	public void testFloatObject() throws Exception {
		proxy.setFloatObject(1f);
		assertTrue(1f == proxy.getFloatObject().floatValue());
		assertEquals("1.0", cf.get("A/floatObject"));
		assertInstanceOf(Float.class, proxy.getFloatObject());
	}

	@Test
	public void testInt3dArray() throws Exception {
		proxy.setInt3dArray(new int[][][]{{{1,2},null},null});
		assertEquals("[[[1,2],null],null]", cf.get("A/int3dArray"));
		assertObject(proxy.getInt3dArray()).json().is("[[[1,2],null],null]");
		assertInstanceOf(int[][][].class, proxy.getInt3dArray());
	}

	@Test
	public void testInteger3dArray() throws Exception {
		proxy.setInteger3dArray(new Integer[][][]{{{1,null},null},null});
		assertObject(proxy.getInteger3dArray()).json().is("[[[1,null],null],null]");
		assertEquals("[[[1,null],null],null]", cf.get("A/integer3dArray"));
		assertInstanceOf(Integer.class, proxy.getInteger3dArray()[0][0][0]);
	}

	@Test
	public void testString3dArray() throws Exception {
		proxy.setString3dArray(new String[][][]{{{"foo",null},null},null});
		assertObject(proxy.getString3dArray()).json().is("[[['foo',null],null],null]");
		assertEquals("[[['foo',null],null],null]", cf.get("A/string3dArray"));
	}

	@Test
	public void testIntegerList() throws Exception {
		proxy.setIntegerList(AList.of(1,null));
		assertObject(proxy.getIntegerList()).json().is("[1,null]");
		assertEquals("[1,null]", cf.get("A/integerList"));
		assertInstanceOf(Integer.class, proxy.getIntegerList().get(0));
	}

	@Test
	public void testInteger3dList() throws Exception {
		proxy.setInteger3dList(AList.of(AList.of(AList.of(1,null),null),null));
		assertObject(proxy.getInteger3dList()).json().is("[[[1,null],null],null]");
		assertEquals("[[[1,null],null],null]", cf.get("A/integer3dList"));
		assertInstanceOf(Integer.class, proxy.getInteger3dList().get(0).get(0).get(0));
	}

	@Test
	public void testInteger1d3dList() throws Exception {
		proxy.setInteger1d3dList(AList.of(new Integer[][][]{{{1,null},null},null},null));
		assertObject(proxy.getInteger1d3dList()).json().is("[[[[1,null],null],null],null]");
		assertEquals("[[[[1,null],null],null],null]", cf.get("A/integer1d3dList"));
		assertInstanceOf(Integer.class, proxy.getInteger1d3dList().get(0)[0][0][0]);
	}

	@Test
	public void testInt1d3dList() throws Exception {
		proxy.setInt1d3dList(AList.of(new int[][][]{{{1,2},null},null},null));
		assertObject(proxy.getInt1d3dList()).json().is("[[[[1,2],null],null],null]");
		assertEquals("[[[[1,2],null],null],null]", cf.get("A/int1d3dList"));
		assertInstanceOf(int[][][].class, proxy.getInt1d3dList().get(0));
	}

	@Test
	public void testStringList() throws Exception {
		proxy.setStringList(Arrays.asList("foo","bar",null));
		assertObject(proxy.getStringList()).json().is("['foo','bar',null]");
		assertEquals("['foo','bar',null]", cf.get("A/stringList"));
	}

	// Beans

	@Test
	public void testBean() throws Exception {
		proxy.setBean(new ABean().init());
		assertObject(proxy.getBean()).json().is("{a:1,b:'foo'}");
		assertEquals("{a:1,b:'foo'}", cf.get("A/bean"));
		assertInstanceOf(ABean.class, proxy.getBean());
	}

	@Test
	public void testBean3dArray() throws Exception {
		proxy.setBean3dArray(new ABean[][][]{{{new ABean().init(),null},null},null});
		assertObject(proxy.getBean3dArray()).json().is("[[[{a:1,b:'foo'},null],null],null]");
		assertEquals("[[[{a:1,b:'foo'},null],null],null]", cf.get("A/bean3dArray"));
		assertInstanceOf(ABean.class, proxy.getBean3dArray()[0][0][0]);
	}

	@Test
	public void testBeanList() throws Exception {
		proxy.setBeanList(Arrays.asList(new ABean().init()));
		assertObject(proxy.getBeanList()).json().is("[{a:1,b:'foo'}]");
		assertEquals("[{a:1,b:'foo'}]", cf.get("A/beanList"));
		assertInstanceOf(ABean.class, proxy.getBeanList().get(0));
	}

	@Test
	public void testBean1d3dList() throws Exception {
		proxy.setBean1d3dList(AList.of(new ABean[][][]{{{new ABean().init(),null},null},null},null));
		assertObject(proxy.getBean1d3dList()).json().is("[[[[{a:1,b:'foo'},null],null],null],null]");
		assertEquals("[[[[{a:1,b:'foo'},null],null],null],null]", cf.get("A/bean1d3dList"));
		assertInstanceOf(ABean.class, proxy.getBean1d3dList().get(0)[0][0][0]);
	}

	@Test
	public void testBeanMap() throws Exception {
		proxy.setBeanMap(AMap.of("foo",new ABean().init()));
		assertObject(proxy.getBeanMap()).json().is("{foo:{a:1,b:'foo'}}");
		assertEquals("{foo:{a:1,b:'foo'}}", cf.get("A/beanMap"));
		assertInstanceOf(ABean.class, proxy.getBeanMap().get("foo"));
	}

	@Test
	public void testBeanListMap() throws Exception {
		proxy.setBeanListMap(AMap.of("foo",Arrays.asList(new ABean().init())));
		assertObject(proxy.getBeanListMap()).json().is("{foo:[{a:1,b:'foo'}]}");
		assertEquals("{foo:[{a:1,b:'foo'}]}", cf.get("A/beanListMap"));
		assertInstanceOf(ABean.class, proxy.getBeanListMap().get("foo").get(0));
	}

	@Test
	public void testBean1d3dListMap() throws Exception {
		proxy.setBean1d3dListMap(AMap.of("foo",AList.of(new ABean[][][]{{{new ABean().init(),null},null},null},null)));
		assertObject(proxy.getBean1d3dListMap()).json().is("{foo:[[[[{a:1,b:'foo'},null],null],null],null]}");
		assertEquals("{foo:[[[[{a:1,b:'foo'},null],null],null],null]}", cf.get("A/bean1d3dListMap"));
		assertInstanceOf(ABean.class, proxy.getBean1d3dListMap().get("foo").get(0)[0][0][0]);
	}

	@Test
	public void testBeanListMapIntegerKeys() throws Exception {
		proxy.setBeanListMapIntegerKeys(AMap.of(1,Arrays.asList(new ABean().init())));
		assertObject(proxy.getBeanListMapIntegerKeys()).json().is("{'1':[{a:1,b:'foo'}]}");
		assertEquals("{'1':[{a:1,b:'foo'}]}", cf.get("A/beanListMapIntegerKeys"));
		assertInstanceOf(ABean.class, proxy.getBeanListMapIntegerKeys().get(1).get(0));
	}

	// Typed beans

	@Test
	public void testTypedBean() throws Exception {
		proxy.setTypedBean(new TypedBeanImpl().init());
		assertObject(proxy.getTypedBean()).json().is("{_type:'TypedBeanImpl',a:1,b:'foo'}");
		assertEquals("{_type:'TypedBeanImpl',a:1,b:'foo'}", cf.get("A/typedBean"));
		assertInstanceOf(TypedBeanImpl.class, proxy.getTypedBean());
	}

	@Test
	public void testTypedBean3dArray() throws Exception {
		proxy.setTypedBean3dArray(new TypedBean[][][]{{{new TypedBeanImpl().init(),null},null},null});
		assertObject(proxy.getTypedBean3dArray()).json().is("[[[{_type:'TypedBeanImpl',a:1,b:'foo'},null],null],null]");
		assertEquals("[[[{_type:'TypedBeanImpl',a:1,b:'foo'},null],null],null]", cf.get("A/typedBean3dArray"));
		assertInstanceOf(TypedBeanImpl.class, proxy.getTypedBean3dArray()[0][0][0]);
	}

	@Test
	public void testTypedBeanList() throws Exception {
		proxy.setTypedBeanList(Arrays.asList((TypedBean)new TypedBeanImpl().init()));
		assertObject(proxy.getTypedBeanList()).json().is("[{_type:'TypedBeanImpl',a:1,b:'foo'}]");
		assertEquals("[{_type:'TypedBeanImpl',a:1,b:'foo'}]", cf.get("A/typedBeanList"));
		assertInstanceOf(TypedBeanImpl.class, proxy.getTypedBeanList().get(0));
	}

	@Test
	public void testTypedBean1d3dList() throws Exception {
		proxy.setTypedBean1d3dList(AList.of(new TypedBean[][][]{{{new TypedBeanImpl().init(),null},null},null},null));
		assertObject(proxy.getTypedBean1d3dList()).json().is("[[[[{_type:'TypedBeanImpl',a:1,b:'foo'},null],null],null],null]");
		assertEquals("[[[[{_type:'TypedBeanImpl',a:1,b:'foo'},null],null],null],null]", cf.get("A/typedBean1d3dList"));
		assertInstanceOf(TypedBeanImpl.class, proxy.getTypedBean1d3dList().get(0)[0][0][0]);
	}

	@Test
	public void testTypedBeanMap() throws Exception {
		proxy.setTypedBeanMap(AMap.of("foo",new TypedBeanImpl().init()));
		assertObject(proxy.getTypedBeanMap()).json().is("{foo:{_type:'TypedBeanImpl',a:1,b:'foo'}}");
		assertEquals("{foo:{_type:'TypedBeanImpl',a:1,b:'foo'}}", cf.get("A/typedBeanMap"));
		assertInstanceOf(TypedBeanImpl.class, proxy.getTypedBeanMap().get("foo"));
	}

	@Test
	public void testTypedBeanListMap() throws Exception {
		proxy.setTypedBeanListMap(AMap.of("foo",Arrays.asList((TypedBean)new TypedBeanImpl().init())));
		assertObject(proxy.getTypedBeanListMap()).json().is("{foo:[{_type:'TypedBeanImpl',a:1,b:'foo'}]}");
		assertEquals("{foo:[{_type:'TypedBeanImpl',a:1,b:'foo'}]}", cf.get("A/typedBeanListMap"));
		assertInstanceOf(TypedBeanImpl.class, proxy.getTypedBeanListMap().get("foo").get(0));
	}

	@Test
	public void testTypedBean1d3dListMap() throws Exception {
		proxy.setTypedBean1d3dListMap(AMap.of("foo",AList.of(new TypedBean[][][]{{{new TypedBeanImpl().init(),null},null},null},null)));
		assertObject(proxy.getTypedBean1d3dListMap()).json().is("{foo:[[[[{_type:'TypedBeanImpl',a:1,b:'foo'},null],null],null],null]}");
		assertEquals("{foo:[[[[{_type:'TypedBeanImpl',a:1,b:'foo'},null],null],null],null]}", cf.get("A/typedBean1d3dListMap"));
		assertInstanceOf(TypedBeanImpl.class, proxy.getTypedBean1d3dListMap().get("foo").get(0)[0][0][0]);
	}

	@Test
	public void testTypedBeanListMapIntegerKeys() throws Exception {
		proxy.setTypedBeanListMapIntegerKeys(AMap.of(1,Arrays.asList((TypedBean)new TypedBeanImpl().init())));
		assertObject(proxy.getTypedBeanListMapIntegerKeys()).json().is("{'1':[{_type:'TypedBeanImpl',a:1,b:'foo'}]}");
		assertEquals("{'1':[{_type:'TypedBeanImpl',a:1,b:'foo'}]}", cf.get("A/typedBeanListMapIntegerKeys"));
		assertInstanceOf(TypedBeanImpl.class, proxy.getTypedBeanListMapIntegerKeys().get(1).get(0));
	}

	// Swapped POJOs

	@Test
	public void testSwappedPojo() throws Exception {
		proxy.setSwappedPojo(new SwappedPojo());
		assertObject(proxy.getSwappedPojo()).json().is("'swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/'");
		assertEquals("swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/", cf.get("A/swappedPojo"));
		assertInstanceOf(SwappedPojo.class, proxy.getSwappedPojo());
	}

	@Test
	public void testSwappedPojo3dArray() throws Exception {
		proxy.setSwappedPojo3dArray(new SwappedPojo[][][]{{{new SwappedPojo(),null},null},null});
		assertObject(proxy.getSwappedPojo3dArray()).json().is("[[['swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/',null],null],null]");
		assertEquals("[[['swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/',null],null],null]", cf.get("A/swappedPojo3dArray"));
		assertInstanceOf(SwappedPojo.class, proxy.getSwappedPojo3dArray()[0][0][0]);
	}

	@Test
	public void testSwappedPojoMap() throws Exception {
		proxy.setSwappedPojoMap(AMap.of(new SwappedPojo(), new SwappedPojo()));
		assertObject(proxy.getSwappedPojoMap()).json().is("{'swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/':'swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/'}");
		assertEquals("{'swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/':'swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/'}", cf.get("A/swappedPojoMap"));
		assertInstanceOf(SwappedPojo.class, proxy.getSwappedPojoMap().keySet().iterator().next());
		assertInstanceOf(SwappedPojo.class, proxy.getSwappedPojoMap().values().iterator().next());
	}

	@Test
	public void testSwappedPojo3dMap() throws Exception {
		proxy.setSwappedPojo3dMap(AMap.of(new SwappedPojo(), new SwappedPojo[][][]{{{new SwappedPojo(),null},null},null}));
		assertObject(proxy.getSwappedPojo3dMap()).json().is("{'swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/':[[['swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/',null],null],null]}");
		assertEquals("{'swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/':[[['swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/',null],null],null]}", cf.get("A/swappedPojo3dMap"));
		assertInstanceOf(SwappedPojo.class, proxy.getSwappedPojo3dMap().keySet().iterator().next());
		assertInstanceOf(SwappedPojo.class, proxy.getSwappedPojo3dMap().values().iterator().next()[0][0][0]);
	}

	// Implicit swapped POJOs

	@Test
	public void testImplicitSwappedPojo() throws Exception {
		proxy.setImplicitSwappedPojo(new ImplicitSwappedPojo());
		assertObject(proxy.getImplicitSwappedPojo()).json().is("'swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/'");
		assertEquals("swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/", cf.get("A/implicitSwappedPojo"));
		assertInstanceOf(ImplicitSwappedPojo.class, proxy.getImplicitSwappedPojo());
	}

	@Test
	public void testImplicitSwappedPojo3dArray() throws Exception {
		proxy.setImplicitSwappedPojo3dArray(new ImplicitSwappedPojo[][][]{{{new ImplicitSwappedPojo(),null},null},null});
		assertObject(proxy.getImplicitSwappedPojo3dArray()).json().is("[[['swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/',null],null],null]");
		assertEquals("[[['swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/',null],null],null]", cf.get("A/implicitSwappedPojo3dArray"));
		assertInstanceOf(ImplicitSwappedPojo.class, proxy.getImplicitSwappedPojo3dArray()[0][0][0]);
	}

	@Test
	public void testImplicitSwappedPojoMap() throws Exception {
		proxy.setImplicitSwappedPojoMap(AMap.of(new ImplicitSwappedPojo(), new ImplicitSwappedPojo()));
		assertObject(proxy.getImplicitSwappedPojoMap()).json().is("{'swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/':'swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/'}");
		assertEquals("{'swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/':'swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/'}", cf.get("A/implicitSwappedPojoMap"));
		assertInstanceOf(ImplicitSwappedPojo.class, proxy.getImplicitSwappedPojoMap().keySet().iterator().next());
		assertInstanceOf(ImplicitSwappedPojo.class, proxy.getImplicitSwappedPojoMap().values().iterator().next());
	}

	@Test
	public void testImplicitSwappedPojo3dMap() throws Exception {
		proxy.setImplicitSwappedPojo3dMap(AMap.of(new ImplicitSwappedPojo(), new ImplicitSwappedPojo[][][]{{{new ImplicitSwappedPojo(),null},null},null}));
		assertObject(proxy.getImplicitSwappedPojo3dMap()).json().is("{'swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/':[[['swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/',null],null],null]}");
		assertEquals("{'swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/':[[['swap-~!@#$%^&*()_+`-={}[]|:;\"<,>.?/',null],null],null]}", cf.get("A/implicitSwappedPojo3dMap"));
		assertInstanceOf(ImplicitSwappedPojo.class, proxy.getImplicitSwappedPojo3dMap().keySet().iterator().next());
		assertInstanceOf(ImplicitSwappedPojo.class, proxy.getImplicitSwappedPojo3dMap().values().iterator().next()[0][0][0]);
	}

	// Enums

	@Test
	public void testEnum() throws Exception {
		proxy.setEnum(TestEnum.TWO);
		assertObject(proxy.getEnum()).json().is("'TWO'");
		assertEquals("TWO", cf.get("A/enum"));
		assertInstanceOf(TestEnum.class, proxy.getEnum());
	}

	@Test
	public void testEnum3d() throws Exception {
		proxy.setEnum3d(new TestEnum[][][]{{{TestEnum.TWO,null},null},null});
		assertObject(proxy.getEnum3d()).json().is("[[['TWO',null],null],null]");
		assertEquals("[[['TWO',null],null],null]", cf.get("A/enum3d"));
		assertInstanceOf(TestEnum.class, proxy.getEnum3d()[0][0][0]);
	}

	@Test
	public void testEnumList() throws Exception {
		proxy.setEnumList(AList.of(TestEnum.TWO,null));
		assertObject(proxy.getEnumList()).json().is("['TWO',null]");
		assertEquals("['TWO',null]", cf.get("A/enumList"));
		assertInstanceOf(TestEnum.class, proxy.getEnumList().get(0));
	}

	@Test
	public void testEnum3dList() throws Exception {
		proxy.setEnum3dList(AList.of(AList.of(AList.of(TestEnum.TWO,null),null),null));
		assertObject(proxy.getEnum3dList()).json().is("[[['TWO',null],null],null]");
		assertEquals("[[['TWO',null],null],null]", cf.get("A/enum3dList"));
		assertInstanceOf(TestEnum.class, proxy.getEnum3dList().get(0).get(0).get(0));
	}

	@Test
	public void testEnum1d3dList() throws Exception {
		proxy.setEnum1d3dList(AList.of(new TestEnum[][][]{{{TestEnum.TWO,null},null},null},null));
		assertObject(proxy.getEnum1d3dList()).json().is("[[[['TWO',null],null],null],null]");
		assertEquals("[[[['TWO',null],null],null],null]", cf.get("A/enum1d3dList"));
		assertInstanceOf(TestEnum.class, proxy.getEnum1d3dList().get(0)[0][0][0]);
	}

	@Test
	public void testEnumMap() throws Exception {
		proxy.setEnumMap(AMap.of(TestEnum.ONE,TestEnum.TWO));
		assertObject(proxy.getEnumMap()).json().is("{ONE:'TWO'}");
		assertEquals("{ONE:'TWO'}", cf.get("A/enumMap"));
		assertInstanceOf(TestEnum.class, proxy.getEnumMap().keySet().iterator().next());
		assertInstanceOf(TestEnum.class, proxy.getEnumMap().values().iterator().next());
	}

	@Test
	public void testEnum3dArrayMap() throws Exception {
		proxy.setEnum3dArrayMap(AMap.of(TestEnum.ONE,new TestEnum[][][]{{{TestEnum.TWO,null},null},null}));
		assertObject(proxy.getEnum3dArrayMap()).json().is("{ONE:[[['TWO',null],null],null]}");
		assertEquals("{ONE:[[['TWO',null],null],null]}", cf.get("A/enum3dArrayMap"));
		assertInstanceOf(TestEnum.class, proxy.getEnum3dArrayMap().keySet().iterator().next());
		assertInstanceOf(TestEnum.class, proxy.getEnum3dArrayMap().values().iterator().next()[0][0][0]);
	}

	@Test
	public void testEnum1d3dListMap() throws Exception {
		proxy.setEnum1d3dListMap(AMap.of(TestEnum.ONE,AList.of(new TestEnum[][][]{{{TestEnum.TWO,null},null},null},null)));
		assertObject(proxy.getEnum1d3dListMap()).json().is("{ONE:[[[['TWO',null],null],null],null]}");
		assertEquals("{ONE:[[[['TWO',null],null],null],null]}", cf.get("A/enum1d3dListMap"));
		assertInstanceOf(TestEnum.class, proxy.getEnum1d3dListMap().keySet().iterator().next());
		assertInstanceOf(TestEnum.class, proxy.getEnum1d3dListMap().values().iterator().next().get(0)[0][0][0]);
	}

	public static interface ConfigInterface {

		// Various primitives

		public String getString();
		public void setString(String x);

		public int getInt();
		public void setInt(int x);

		public Integer getInteger();
		public void setInteger(Integer x);

		public boolean isBoolean();
		public void setBoolean(boolean x);

		public Boolean getBooleanObject();
		public void setBooleanObject(Boolean x);

		public float getFloat();
		public void setFloat(float x);

		public Float getFloatObject();
		public void setFloatObject(Float x);

		public int[][][] getInt3dArray();
		public void setInt3dArray(int[][][] x);

		public Integer[][][] getInteger3dArray();
		public void setInteger3dArray(Integer[][][] x);

		public String[][][] getString3dArray();
		public void setString3dArray(String[][][] x);

		public List<Integer> getIntegerList();
		public void setIntegerList(List<Integer> x);

		public List<List<List<Integer>>> getInteger3dList();
		public void setInteger3dList(List<List<List<Integer>>> x);

		public List<Integer[][][]> getInteger1d3dList();
		public void setInteger1d3dList(List<Integer[][][]> x);

		public List<int[][][]> getInt1d3dList();
		public void setInt1d3dList(List<int[][][]> x);

		public List<String> getStringList();
		public void setStringList(List<String> x);

		// Beans

		public ABean getBean();
		public void setBean(ABean x);

		public ABean[][][] getBean3dArray();
		public void setBean3dArray(ABean[][][] x);

		public List<ABean> getBeanList();
		public void setBeanList(List<ABean> x);

		public List<ABean[][][]> getBean1d3dList();
		public void setBean1d3dList(List<ABean[][][]> x);

		public Map<String,ABean> getBeanMap();
		public void setBeanMap(Map<String,ABean> x);

		public Map<String,List<ABean>> getBeanListMap();
		public void setBeanListMap(Map<String,List<ABean>> x);

		public Map<String,List<ABean[][][]>> getBean1d3dListMap();
		public void setBean1d3dListMap(Map<String,List<ABean[][][]>> x);

		public Map<Integer,List<ABean>> getBeanListMapIntegerKeys();
		public void setBeanListMapIntegerKeys(Map<Integer,List<ABean>> x);

		// Typed beans

		public TypedBean getTypedBean();
		public void setTypedBean(TypedBean x);

		public TypedBean[][][] getTypedBean3dArray();
		public void setTypedBean3dArray(TypedBean[][][] x);

		public List<TypedBean> getTypedBeanList();
		public void setTypedBeanList(List<TypedBean> x);

		public List<TypedBean[][][]> getTypedBean1d3dList();
		public void setTypedBean1d3dList(List<TypedBean[][][]> x);

		public Map<String,TypedBean> getTypedBeanMap();
		public void setTypedBeanMap(Map<String,TypedBean> x);

		public Map<String,List<TypedBean>> getTypedBeanListMap();
		public void setTypedBeanListMap(Map<String,List<TypedBean>> x);

		public Map<String,List<TypedBean[][][]>> getTypedBean1d3dListMap();
		public void setTypedBean1d3dListMap(Map<String,List<TypedBean[][][]>> x);

		public Map<Integer,List<TypedBean>> getTypedBeanListMapIntegerKeys();
		public void setTypedBeanListMapIntegerKeys(Map<Integer,List<TypedBean>> x);

		// Swapped POJOs

		public SwappedPojo getSwappedPojo();
		public void setSwappedPojo(SwappedPojo x);

		public SwappedPojo[][][] getSwappedPojo3dArray();
		public void setSwappedPojo3dArray(SwappedPojo[][][] x);

		public Map<SwappedPojo,SwappedPojo> getSwappedPojoMap();
		public void setSwappedPojoMap(Map<SwappedPojo,SwappedPojo> x);

		public Map<SwappedPojo,SwappedPojo[][][]> getSwappedPojo3dMap();
		public void setSwappedPojo3dMap(Map<SwappedPojo,SwappedPojo[][][]> x);

		// Implicit swapped POJOs

		public ImplicitSwappedPojo getImplicitSwappedPojo();
		public void setImplicitSwappedPojo(ImplicitSwappedPojo x);

		public ImplicitSwappedPojo[][][] getImplicitSwappedPojo3dArray();
		public void setImplicitSwappedPojo3dArray(ImplicitSwappedPojo[][][] x);

		public Map<ImplicitSwappedPojo,ImplicitSwappedPojo> getImplicitSwappedPojoMap();
		public void setImplicitSwappedPojoMap(Map<ImplicitSwappedPojo,ImplicitSwappedPojo> x);

		public Map<ImplicitSwappedPojo,ImplicitSwappedPojo[][][]> getImplicitSwappedPojo3dMap();
		public void setImplicitSwappedPojo3dMap(Map<ImplicitSwappedPojo,ImplicitSwappedPojo[][][]> x);

		// Enums

		public TestEnum getEnum();
		public void setEnum(TestEnum x);

		public TestEnum[][][] getEnum3d();
		public void setEnum3d(TestEnum[][][] x);

		public List<TestEnum> getEnumList();
		public void setEnumList(List<TestEnum> x);

		public List<List<List<TestEnum>>> getEnum3dList();
		public void setEnum3dList(List<List<List<TestEnum>>> x);

		public List<TestEnum[][][]> getEnum1d3dList();
		public void setEnum1d3dList(List<TestEnum[][][]> x);

		public Map<TestEnum,TestEnum> getEnumMap();
		public void setEnumMap(Map<TestEnum,TestEnum> x);

		public Map<TestEnum,TestEnum[][][]> getEnum3dArrayMap();
		public void setEnum3dArrayMap(Map<TestEnum,TestEnum[][][]> x);

		public Map<TestEnum,List<TestEnum[][][]>> getEnum1d3dListMap();
		public void setEnum1d3dListMap(Map<TestEnum,List<TestEnum[][][]>> x);
	}
}