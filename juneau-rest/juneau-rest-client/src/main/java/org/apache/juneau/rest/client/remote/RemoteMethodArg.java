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
package org.apache.juneau.rest.client.remote;

import static org.apache.juneau.internal.ClassUtils.*;

import static org.apache.juneau.httppart.HttpPartType.*;

import org.apache.juneau.http.annotation.*;
import org.apache.juneau.httppart.*;
import org.apache.juneau.reflect.*;

/**
 * Represents the metadata about an annotated argument of a method on a REST proxy class.
 *
 * <ul class='seealso'>
 * 	<li class='link'>{@doc juneau-rest-client.RestProxies}
 * </ul>
 */
public final class RemoteMethodArg {

	private final int index;
	private final HttpPartType partType;
	private final HttpPartSerializer serializer;
	private final HttpPartSchema schema;

	RemoteMethodArg(int index, HttpPartType partType, HttpPartSchema schema) {
		this.index = index;
		this.partType = partType;
		this.serializer = createSerializer(partType, schema);
		this.schema = schema;
	}

	private static HttpPartSerializer createSerializer(HttpPartType partType, HttpPartSchema schema) {
		return castOrCreate(HttpPartSerializer.class, schema.getSerializer());
	}

	/**
	 * Returns the name of the HTTP part.
	 *
	 * @return The name of the HTTP part.
	 */
	public String getName() {
		return schema.getName();
	}

	/**
	 * Returns whether the <c>skipIfEmpty</c> flag was found in the schema.
	 *
	 * @return <jk>true</jk> if the <c>skipIfEmpty</c> flag was found in the schema.
	 */
	public boolean isSkipIfEmpty() {
		return schema.isSkipIfEmpty();
	}

	/**
	 * Returns the method argument index.
	 *
	 * @return The method argument index.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Returns the HTTP part type.
	 *
	 * @return The HTTP part type.  Never <jk>null</jk>.
	 */
	public HttpPartType getPartType() {
		return partType;
	}

	/**
	 * Returns the HTTP part serializer to use for serializing this part.
	 *
	 * @param _default The default serializer to use if the serializer was not defined via annotations.
	 * @return The HTTP part serializer, or the default if not specified.
	 */
	public HttpPartSerializerSession getSerializer(HttpPartSerializerSession _default) {
		return serializer == null ? _default : serializer.createPartSession(null);
	}

	/**
	 * Returns the HTTP part schema information about this part.
	 *
	 * @return The HTTP part schema information, or <jk>null</jk> if not found.
	 */
	public HttpPartSchema getSchema() {
		return schema;
	}

	static RemoteMethodArg create(ParamInfo mpi) {
		int i = mpi.getIndex();
		if (mpi.hasAnnotation(Header.class)) {
			return new RemoteMethodArg(i, HEADER, HttpPartSchema.create(Header.class, mpi));
		} else if (mpi.hasAnnotation(Query.class)) {
			return new RemoteMethodArg(i, QUERY, HttpPartSchema.create(Query.class, mpi));
		} else if (mpi.hasAnnotation(FormData.class)) {
			return new RemoteMethodArg(i, FORMDATA, HttpPartSchema.create(FormData.class, mpi));
		} else if (mpi.hasAnnotation(Path.class)) {
			return new RemoteMethodArg(i, PATH, HttpPartSchema.create(Path.class, mpi));
		} else if (mpi.hasAnnotation(Body.class)) {
			return new RemoteMethodArg(i, BODY, HttpPartSchema.create(Body.class, mpi));
		}
		return null;
	}
}
