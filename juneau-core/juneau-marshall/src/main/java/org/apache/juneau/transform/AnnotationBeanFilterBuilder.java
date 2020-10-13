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
package org.apache.juneau.transform;

import static org.apache.juneau.internal.StringUtils.*;

import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.annotation.*;

/**
 * Bean filter builder initialized from the contents of a {@link Bean @Bean} annotation found on a class.
 *
 * <p>
 * <b>*** Internal class - Not intended for external use ***</b>
 *
 * @param <T> Annotated bean class.
 */
public final class AnnotationBeanFilterBuilder<T> extends BeanFilterBuilder<T> {

	/**
	 * Constructor.
	 *
	 * @param annotatedClass The class found to have a {@link Bean @Bean} annotation.
	 * @param annotations
	 * 	The {@link Bean @Bean} annotations found on the class and all parent classes in child-to-parent order.
	 * @throws Exception Thrown from property namer constructor.
	 */
	public AnnotationBeanFilterBuilder(Class<T> annotatedClass, List<Bean> annotations) throws Exception {
		super(annotatedClass);

		for (Bean b : annotations) {

			if (! b.bpi().isEmpty())
				bpi(split(b.bpi()));

			if (! b.typeName().isEmpty())
				typeName(b.typeName());

			if (b.sort())
				sortProperties(true);

			if (b.fluentSetters())
				fluentSetters(true);

			if (! b.bpx().isEmpty())
				bpx(split(b.bpx()));

			if (! b.bpro().isEmpty())
				bpro(split(b.bpro()));

			if (! b.bpwo().isEmpty())
				bpwo(split(b.bpwo()));

			if (b.propertyNamer() != PropertyNamerDefault.class)
				propertyNamer(b.propertyNamer());

			if (b.interfaceClass() != Object.class)
				interfaceClass(b.interfaceClass());

			if (b.stopClass() != Object.class)
				stopClass(b.stopClass());

			if (b.dictionary().length > 0)
				dictionary(b.dictionary());

			if (b.interceptor() != BeanInterceptor.Default.class)
				interceptor(b.interceptor());
		}
	}
}
