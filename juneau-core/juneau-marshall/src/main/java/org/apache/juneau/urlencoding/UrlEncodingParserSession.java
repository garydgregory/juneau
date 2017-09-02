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

import java.lang.reflect.*;
import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.parser.*;
import org.apache.juneau.transform.*;
import org.apache.juneau.uon.*;

/**
 * Session object that lives for the duration of a single use of {@link UrlEncodingParser}.
 *
 * <p>
 * This class is NOT thread safe.
 * It is typically discarded after one-time use although it can be reused against multiple inputs.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class UrlEncodingParserSession extends UonParserSession {

	private final boolean expandedParams;

	/**
	 * Create a new session using properties specified in the context.
	 *
	 * @param ctx
	 * 	The context creating this session object.
	 * 	The context contains all the configuration settings for this object.
	 * @param args
	 * 	Runtime session arguments.
	 */
	protected UrlEncodingParserSession(UrlEncodingParserContext ctx, ParserSessionArgs args) {
		super(ctx, args);
		ObjectMap p = getProperties();
		if (p.isEmpty()) {
			expandedParams = ctx.expandedParams;
		} else {
			expandedParams = p.getBoolean(UrlEncodingContext.URLENC_expandedParams, false);
		}
	}

	/**
	 * Returns <jk>true</jk> if the specified bean property should be expanded as multiple key-value pairs.
	 *
	 * @param pMeta The metadata on the bean property.
	 * @return <jk>true</jk> if the specified bean property should be expanded as multiple key-value pairs.
	 */
	public final boolean shouldUseExpandedParams(BeanPropertyMeta pMeta) {
		ClassMeta<?> cm = pMeta.getClassMeta();
		if (cm.isCollectionOrArray()) {
			if (expandedParams)
				return true;
			if (pMeta.getBeanMeta().getClassMeta().getExtendedMeta(UrlEncodingClassMeta.class).isExpandedParams())
				return true;
		}
		return false;
	}

	@Override /* ParserSession */
	protected <T> T doParse(ParserPipe pipe, ClassMeta<T> type) throws Exception {
		UonReader r = getUonReader(pipe, true);
		T o = parseAnything(type, r, getOuter());
		return o;
	}

	@Override /* ReaderParserSession */
	protected <K,V> Map<K,V> doParseIntoMap(ParserPipe pipe, Map<K,V> m, Type keyType, Type valueType) throws Exception {
		UonReader r = getUonReader(pipe, true);
		if (r.peekSkipWs() == '?')
			r.read();
		m = parseIntoMap2(r, m, getClassMeta(Map.class, keyType, valueType), null);
		return m;
	}

	private <T> T parseAnything(ClassMeta<T> eType, UonReader r, Object outer) throws Exception {

		if (eType == null)
			eType = (ClassMeta<T>)object();
		PojoSwap<T,Object> transform = (PojoSwap<T,Object>)eType.getPojoSwap();
		ClassMeta<?> sType = eType.getSerializedClassMeta();

		int c = r.peekSkipWs();
		if (c == '?')
			r.read();

		Object o;

		if (sType.isObject()) {
			ObjectMap m = new ObjectMap(this);
			parseIntoMap2(r, m, getClassMeta(Map.class, String.class, Object.class), outer);
			if (m.containsKey("_value"))
				o = m.get("_value");
			else
				o = cast(m, null, eType);
		} else if (sType.isMap()) {
			Map m = (sType.canCreateNewInstance() ? (Map)sType.newInstance() : new ObjectMap(this));
			o = parseIntoMap2(r, m, sType, m);
		} else if (sType.canCreateNewBean(outer)) {
			BeanMap m = newBeanMap(outer, sType.getInnerClass());
			m = parseIntoBeanMap(r, m);
			o = m == null ? null : m.getBean();
		} else if (sType.isCollection() || sType.isArray() || sType.isArgs()) {
			// ?1=foo&2=bar...
			Collection c2 = ((sType.isArray() || sType.isArgs()) || ! sType.canCreateNewInstance(outer)) ? new ObjectList(this) : (Collection)sType.newInstance();
			Map<Integer,Object> m = new TreeMap<Integer,Object>();
			parseIntoMap2(r, m, sType, c2);
			c2.addAll(m.values());
			if (sType.isArray())
				o = ArrayUtils.toArray(c2, sType.getElementType().getInnerClass());
			else if (sType.isArgs())
				o = c2.toArray(new Object[c2.size()]);
			else
				o = c2;
		} else {
			// It could be a non-bean with _type attribute.
			ObjectMap m = new ObjectMap(this);
			parseIntoMap2(r, m, getClassMeta(Map.class, String.class, Object.class), outer);
			if (m.containsKey(getBeanTypePropertyName(eType)))
				o = cast(m, null, eType);
			else if (m.containsKey("_value")) {
				o = convertToType(m.get("_value"), sType);
			} else {
				if (sType.getNotABeanReason() != null)
					throw new ParseException(loc(r), "Class ''{0}'' could not be instantiated as application/x-www-form-urlencoded.  Reason: ''{1}''", sType, sType.getNotABeanReason());
				throw new ParseException(loc(r), "Malformed application/x-www-form-urlencoded input for class ''{0}''.", sType);
			}
		}

		if (transform != null && o != null)
			o = transform.unswap(this, o, eType);

		if (outer != null)
			setParent(eType, o, outer);

		return (T)o;
	}

	private <K,V> Map<K,V> parseIntoMap2(UonReader r, Map<K,V> m, ClassMeta<?> type, Object outer) throws Exception {

		ClassMeta<K> keyType = (ClassMeta<K>)(type.isArgs() || type.isCollectionOrArray() ? getClassMeta(Integer.class) : type.getKeyType());

		int c = r.peekSkipWs();
		if (c == -1)
			return m;

		final int S1=1; // Looking for attrName start.
		final int S2=2; // Found attrName end, looking for =.
		final int S3=3; // Found =, looking for valStart.
		final int S4=4; // Looking for & or end.
		boolean isInEscape = false;

		int state = S1;
		int argIndex = 0;
		K currAttr = null;
		while (c != -1) {
			c = r.read();
			if (! isInEscape) {
				if (state == S1) {
					if (c == -1)
						return m;
					r.unread();
					Object attr = parseAttr(r, true);
					currAttr = attr == null ? null : convertAttrToType(m, trim(attr.toString()), keyType);
					state = S2;
					c = 0; // Avoid isInEscape if c was '\'
				} else if (state == S2) {
					if (c == '\u0002')
						state = S3;
					else if (c == -1 || c == '\u0001') {
						m.put(currAttr, null);
						if (c == -1)
							return m;
						state = S1;
					}
				} else if (state == S3) {
					if (c == -1 || c == '\u0001') {
						ClassMeta<V> valueType = (ClassMeta<V>)(type.isArgs() ? type.getArg(argIndex++) : type.isCollectionOrArray() ? type.getElementType() : type.getValueType());
						V value = convertAttrToType(m, "", valueType);
						m.put(currAttr, value);
						if (c == -1)
							return m;
						state = S1;
					} else  {
						// For performance, we bypass parseAnything for string values.
						ClassMeta<V> valueType = (ClassMeta<V>)(type.isArgs() ? type.getArg(argIndex++) : type.isCollectionOrArray() ? type.getElementType() : type.getValueType());
						V value = (V)(valueType.isString() ? super.parseString(r.unread(), true) : super.parseAnything(valueType, r.unread(), outer, true, null));

						// If we already encountered this parameter, turn it into a list.
						if (m.containsKey(currAttr) && valueType.isObject()) {
							Object v2 = m.get(currAttr);
							if (! (v2 instanceof ObjectList)) {
								v2 = new ObjectList(v2).setBeanSession(this);
								m.put(currAttr, (V)v2);
							}
							((ObjectList)v2).add(value);
						} else {
							m.put(currAttr, value);
						}
						state = S4;
						c = 0; // Avoid isInEscape if c was '\'
					}
				} else if (state == S4) {
					if (c == '\u0001')
						state = S1;
					else if (c == -1) {
						return m;
					}
				}
			}
			isInEscape = (c == '\\' && ! isInEscape);
		}
		if (state == S1)
			throw new ParseException(loc(r), "Could not find attribute name on object.");
		if (state == S2)
			throw new ParseException(loc(r), "Could not find '=' following attribute name on object.");
		if (state == S3)
			throw new ParseException(loc(r), "Dangling '=' found in object entry");
		if (state == S4)
			throw new ParseException(loc(r), "Could not find end of object.");

		return null; // Unreachable.
	}

	private <T> BeanMap<T> parseIntoBeanMap(UonReader r, BeanMap<T> m) throws Exception {

		int c = r.peekSkipWs();
		if (c == -1)
			return m;

		final int S1=1; // Looking for attrName start.
		final int S2=2; // Found attrName end, looking for =.
		final int S3=3; // Found =, looking for valStart.
		final int S4=4; // Looking for , or }
		boolean isInEscape = false;

		int state = S1;
		String currAttr = "";
		int currAttrLine = -1, currAttrCol = -1;
		while (c != -1) {
			c = r.read();
			if (! isInEscape) {
				if (state == S1) {
					if (c == -1) {
						return m;
					}
					r.unread();
					currAttrLine= r.getLine();
					currAttrCol = r.getColumn();
					currAttr = parseAttrName(r, true);
					if (currAttr == null)  // Value was '%00'
						return null;
					state = S2;
				} else if (state == S2) {
					if (c == '\u0002')
						state = S3;
					else if (c == -1 || c == '\u0001') {
						m.put(currAttr, null);
						if (c == -1)
							return m;
						state = S1;
					}
				} else if (state == S3) {
					if (c == -1 || c == '\u0001') {
						if (! currAttr.equals(getBeanTypePropertyName(m.getClassMeta()))) {
							BeanPropertyMeta pMeta = m.getPropertyMeta(currAttr);
							if (pMeta == null) {
								onUnknownProperty(r.getPipe(), currAttr, m, currAttrLine, currAttrCol);
							} else {
								setCurrentProperty(pMeta);
								// In cases of "&foo=", create an empty instance of the value if createable.
								// Otherwise, leave it null.
								ClassMeta<?> cm = pMeta.getClassMeta();
								if (cm.canCreateNewInstance())
									pMeta.set(m, currAttr, cm.newInstance());
								setCurrentProperty(null);
							}
						}
						if (c == -1)
							return m;
						state = S1;
					} else {
						if (! currAttr.equals(getBeanTypePropertyName(m.getClassMeta()))) {
							BeanPropertyMeta pMeta = m.getPropertyMeta(currAttr);
							if (pMeta == null) {
								onUnknownProperty(r.getPipe(), currAttr, m, currAttrLine, currAttrCol);
								parseAnything(object(), r.unread(), m.getBean(false), true, null); // Read content anyway to ignore it
							} else {
								setCurrentProperty(pMeta);
								if (shouldUseExpandedParams(pMeta)) {
									ClassMeta et = pMeta.getClassMeta().getElementType();
									Object value = parseAnything(et, r.unread(), m.getBean(false), true, pMeta);
									setName(et, value, currAttr);
									pMeta.add(m, currAttr, value);
								} else {
									ClassMeta<?> cm = pMeta.getClassMeta();
									Object value = parseAnything(cm, r.unread(), m.getBean(false), true, pMeta);
									setName(cm, value, currAttr);
									pMeta.set(m, currAttr, value);
								}
								setCurrentProperty(null);
							}
						}
						state = S4;
					}
				} else if (state == S4) {
					if (c == '\u0001')
						state = S1;
					else if (c == -1) {
						return m;
					}
				}
			}
			isInEscape = (c == '\\' && ! isInEscape);
		}
		if (state == S1)
			throw new ParseException(loc(r), "Could not find attribute name on object.");
		if (state == S2)
			throw new ParseException(loc(r), "Could not find '=' following attribute name on object.");
		if (state == S3)
			throw new ParseException(loc(r), "Could not find value following '=' on object.");
		if (state == S4)
			throw new ParseException(loc(r), "Could not find end of object.");

		return null; // Unreachable.
	}
}