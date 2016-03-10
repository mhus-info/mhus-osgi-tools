/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.gwt.thirdparty.guava.common.hash;

import com.google.gwt.thirdparty.guava.common.annotations.Beta;

import java.io.Serializable;

/* 
 * An object which can send data from an object of type {@code T} into a {@code PrimitiveSink}.
 * Implementations for common types can be found in {@link Funnels}.
 *
 * <p>Note that serialization of {@linkplain BloomFilter bloom filters} requires the proper
 * serialization of funnels. When possible, it is recommended that funnels be implemented as a
 * single-element enum to maintain serialization guarantees. See Effective Java (2nd Edition),
 * Item 3: "Enforce the singleton property with a private constructor or an enum type". For example:
 *
 * @author Dimitris Andreou
 * @since 11.0
 */
@Beta
public interface Funnel<T> extends Serializable {

  /* 
   * Sends a stream of data from the {@code from} object into the sink {@code into}. There
   * is no requirement that this data be complete enough to fully reconstitute the object
   * later.
   *
   * @since 12.0 (in Guava 11.0, {@code PrimitiveSink} was named {@code Sink})
   */
  void funnel(T from, PrimitiveSink into);
}