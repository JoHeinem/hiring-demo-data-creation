/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.optimize.hiring.data.generation;

import org.camunda.optimize.hiring.data.generation.dto.VariableValue;

public class VariableHelper {

  public static VariableValue createStringVariable(String value) {
    return new VariableValue(value, "String");
  }

  public static VariableValue createBooleanVariable(boolean value) {
    return new VariableValue(value, "Boolean");
  }

  public static VariableValue createLongVariable(long value) {
    return new VariableValue(value, "Long");
  }

  public static VariableValue createIntegerVariable(int value) {
    return new VariableValue(value, "Integer");
  }
}
