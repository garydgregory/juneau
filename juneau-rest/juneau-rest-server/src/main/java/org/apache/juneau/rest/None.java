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

/**
 * Dummy class that indicates that serializers, parsers, or transforms for a Java class or method should not be inherited.
 * 
 * <h5 class='section'>Example:</h5>
 * <p class='bcode'>
 * 	<ja>@RestMethod</ja>(
 * 		
 * 		// No serializers are defined method or inherited from the class.
 * 		serializers=None.<jk>class</jk>, 
 * 
 * 		// No parsers are defined method or inherited from the class.
 * 		parsers=None.<jk>class</jk>, 
 * 
 * 		// No bean filters are defined method or inherited from the class.
 * 		beanFilters=None.<jk>class</jk>,
 * 		
 * 		// No POJO swaps are defined method or inherited from the class.
 * 		pojoSwaps=None.<jk>class</jk>
 * 	)
 * </p>
 */
public final class None {}
