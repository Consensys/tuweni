/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.tuweni.kv;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "STORE")
public class Store {

  public Store() {}

  public Store(String key, String value) {
    this.key = key;
    this.value = value;
  }

  @Id
  private String key;

  private String value;

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Store))
      return false;
    Store store = (Store) o;
    return Objects.equals(key, store.key) && Objects.equals(value, store.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, value);
  }
}
