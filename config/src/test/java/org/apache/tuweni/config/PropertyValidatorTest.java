// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class PropertyValidatorTest {

  @Test
  void combinedValidatorEvaluatesBoth() {
    PropertyValidator<Integer> combined =
        PropertyValidator.combine(PropertyValidator.isPresent(), PropertyValidator.inRange(1, 5));
    assertTrue(combined.validate("foo", null, 2).isEmpty());
    List<ConfigurationError> whenNull = combined.validate("foo", null, null);
    assertEquals(1, whenNull.size());
    assertTrue(whenNull.get(0).getMessage().contains("is missing"));
    List<ConfigurationError> whenOutOfRange = combined.validate("foo", null, 10);
    assertEquals(1, whenOutOfRange.size());
    assertTrue(whenOutOfRange.get(0).getMessage().contains("is outside range"));
  }

  @Test
  void validatesAllElementsInList() {
    PropertyValidator<List<Integer>> allInList =
        PropertyValidator.allInList(PropertyValidator.inRange(1, 5));
    assertTrue(allInList.validate("foo", null, Arrays.asList(1, 2, 3, 4)).isEmpty());
    List<ConfigurationError> oneError = allInList.validate("foo", null, Arrays.asList(1, 10, 3, 4));
    assertEquals(1, oneError.size());
    assertTrue(oneError.get(0).getMessage().contains("is outside range"));
    List<ConfigurationError> twoErrors =
        allInList.validate("foo", null, Arrays.asList(1, 10, 30, 4));
    assertEquals(2, twoErrors.size());
    assertTrue(twoErrors.get(0).getMessage().contains("is outside range"));
    assertTrue(twoErrors.get(1).getMessage().contains("is outside range"));
  }

  @Test
  void validatesURLs() {
    PropertyValidator<String> urlValidator = PropertyValidator.isURL();
    assertTrue(urlValidator.validate("foo", null, "http://127.0.0.1:5678/bar").isEmpty());
    List<ConfigurationError> errors = urlValidator.validate("foo", null, "abcdefg");
    assertEquals(1, errors.size());
    assertTrue(errors.get(0).getMessage().contains("not a valid URL"));
  }

  @Test
  void validatesInSet() {
    PropertyValidator<String> inSetValidator = PropertyValidator.anyOf("one", "two", "three ");
    assertTrue(inSetValidator.validate("foo", null, "one").isEmpty());
    assertTrue(inSetValidator.validate("foo", null, "two").isEmpty());
    assertTrue(inSetValidator.validate("foo", null, "three ").isEmpty());
    assertEquals(1, inSetValidator.validate("foo", null, "three").size());
    List<ConfigurationError> errors = inSetValidator.validate("foo", null, "foobar");
    assertEquals(1, errors.size());
    assertEquals(
        "Value of property 'foo' should be \"one\", \"two\", or \"three \"",
        errors.get(0).getMessage());
  }

  @Test
  void validatesInSetIgnoreCase() {
    PropertyValidator<String> inSetValidator =
        PropertyValidator.anyOfIgnoreCase("one", "two", "three ");
    assertTrue(inSetValidator.validate("foo", null, "OnE").isEmpty());
    assertTrue(inSetValidator.validate("foo", null, "TWo").isEmpty());
    assertTrue(inSetValidator.validate("foo", null, "THree ").isEmpty());
    assertEquals(1, inSetValidator.validate("foo", null, "three").size());
    List<ConfigurationError> errors = inSetValidator.validate("foo", null, "foobar");
    assertEquals(1, errors.size());
    assertEquals(
        "Value of property 'foo' should be \"one\", \"two\", or \"three \"",
        errors.get(0).getMessage());
  }

  @Test
  void validatesEqualOrGreater() {
    PropertyValidator<Number> longPropertyValidator = PropertyValidator.isGreaterOrEqual(32L);
    assertTrue(longPropertyValidator.validate("foo", null, 33L).isEmpty());
    assertTrue(longPropertyValidator.validate("foo", null, 32L).isEmpty());
    assertEquals(1, longPropertyValidator.validate("foo", null, 31L).size());
  }

  @Test
  void testIsValidPort() {
    PropertyValidator<Integer> portValidator = PropertyValidator.isValidPort();
    assertTrue(portValidator.validate("foo", null, 8080).isEmpty());
    assertTrue(portValidator.validate("foo", null, 60000).isEmpty());
    assertEquals(1, portValidator.validate("foo", null, 65536).size());
    assertEquals(1, portValidator.validate("foo", null, 0).size());
  }

  @Test
  void testIsValidPortOrZero() {
    PropertyValidator<Integer> portValidator = PropertyValidator.isValidPortOrZero();
    assertTrue(portValidator.validate("foo", null, 8080).isEmpty());
    assertTrue(portValidator.validate("foo", null, 60000).isEmpty());
    assertTrue(portValidator.validate("foo", null, 0).isEmpty());
    assertEquals(1, portValidator.validate("foo", null, 65536).size());
  }
}
