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
package org.apache.juneau.reflect;

import java.util.*;
import java.util.function.*;

/**
 * An ordered list of annotations and the classes/methods/packages they were found on.
 */
public class AnnotationList extends ArrayList<AnnotationInfo<?>> {
	private static final long serialVersionUID = 1L;

	private final Predicate<AnnotationInfo<?>> filter;

	/**
	 * Constructor.
	 *
	 * No filtering.
	 */
	public AnnotationList() {
		this(null);
	}

	/**
	 * Constructor with optional filter.
	 *
	 * @param filter The filter to use to filter entries added to this list.
	 */
	public AnnotationList(Predicate<AnnotationInfo<?>> filter) {
		this.filter = filter;
	}

	@Override /* List */
	public boolean add(AnnotationInfo<?> ai) {
		if (filter == null || filter.test(ai)) {
			super.add(ai);
			return true;
		}
		return false;
	}

	private static final Comparator<AnnotationInfo<?>> RANK_COMPARATOR = new Comparator<AnnotationInfo<?>>() {
		@Override
		public int compare(AnnotationInfo<?> o1, AnnotationInfo<?> o2) {
			return o1.rank - o2.rank;
		}
	};

	/**
	 * Sort the annotations in this list based on rank.
	 *
	 * @return This object (for method chaining).
	 */
	public AnnotationList sort() {
		Collections.sort(this, RANK_COMPARATOR);
		return this;
	}

	/**
	 * Returns the specified values from all annotations in this list.
	 *
	 * @param type The annotation value type.
	 * @param name The annotation value name.
	 * @return A list of all values found.
	 */
	public <T> List<T> getValues(Class<T> type, String name) {
		List<T> l = new ArrayList<>();
		for (AnnotationInfo<?> ai : this) {
			Optional<T> o = ai.getValue(type, name);
			if (o.isPresent())
				l.add(o.get());
		}
		return l;
	}

	/**
	 * Filters this list using the specified test.
	 *
	 * @param test The test to use to filter this list.
	 * @return A new list containing only the filtered elements.
	 */
	public AnnotationList filter(Predicate<AnnotationInfo<?>> test) {
		AnnotationList al = new AnnotationList(null);
		stream().filter(test).forEach(x->al.add(x));
		return al;
	}
}
