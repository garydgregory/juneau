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
package org.apache.juneau.msgpack;

import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.msgpack.annotation.*;

/**
 * Metadata on classes specific to the MessagePack serializers and parsers pulled from the {@link MsgPack @MsgPack} annotation on
 * the class.
 */
public class MsgPackClassMeta extends ExtendedClassMeta {

	private final List<MsgPack> msgPacks;

	/**
	 * Constructor.
	 *
	 * @param cm The class that this annotation is defined on.
	 * @param mp MessagePack metadata provider (for finding information about other artifacts).
	 */
	public MsgPackClassMeta(ClassMeta<?> cm, MsgPackMetaProvider mp) {
		super(cm);
		this.msgPacks = cm.getAnnotations(MsgPack.class);
	}

	/**
	 * Returns the {@link MsgPack @MsgPack} annotations defined on the class.
	 *
	 * @return An unmodifiable list of annotations ordered parent-to-child, or an empty list if not found.
	 */
	protected List<MsgPack> getAnnotations() {
		return msgPacks;
	}
}
