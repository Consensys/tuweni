// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.toml;

import org.apache.tuweni.toml.internal.TomlLexer;

import java.util.Arrays;
import java.util.BitSet;
import java.util.stream.Stream;

enum TokenName {
  // Ordered by display preference
  LOWER_ALPHA("a-z", TomlLexer.UnquotedKey),
  UPPER_ALPHA("A-Z", TomlLexer.UnquotedKey),
  DIGITS("0-9", TomlLexer.UnquotedKey),
  ARRAY_END("]", TomlLexer.ArrayEnd, TomlLexer.TableKeyEnd),
  ARRAY_TABLE_END("]]", TomlLexer.ArrayTableKeyEnd),
  INLINE_TABLE_END("}", TomlLexer.InlineTableEnd),
  DOT(".", TomlLexer.Dot),
  DASH("-", TomlLexer.Dash),
  PLUS("+", TomlLexer.Plus),
  COLON(":", TomlLexer.Colon),
  EQUALS("=", TomlLexer.Equals),
  COMMA("a comma", TomlLexer.Comma),
  Z("Z", TomlLexer.Z),
  APOSTROPHE("'", TomlLexer.Apostrophe, TomlLexer.MLLiteralStringEnd),
  QUOTATION_MARK("\"", TomlLexer.QuotationMark, TomlLexer.MLBasicStringEnd),
  TRIPLE_APOSTROPHE("'''", TomlLexer.TripleApostrophe),
  TRIPLE_QUOTATION_MARK("\"\"\"", TomlLexer.TripleQuotationMark),
  CHARACTER("a character", TomlLexer.EscapeSequence, TomlLexer.StringChar),
  NUMBER("a number", TomlLexer.DecimalInteger, TomlLexer.BinaryInteger, TomlLexer.OctalInteger, TomlLexer.HexInteger,
      TomlLexer.FloatingPoint, TomlLexer.FloatingPointInf, TomlLexer.FloatingPointNaN),
  BOOLEAN("a boolean", TomlLexer.TrueBoolean, TomlLexer.FalseBoolean),
  DATETIME("a date/time", TomlLexer.DateDigits),
  TIME("a time", TomlLexer.TimeDelimiter),
  ARRAY("an array", TomlLexer.ArrayStart),
  INLINE_TABLE("a table", TomlLexer.InlineTableStart),
  TABLE("a table key", TomlLexer.TableKeyStart, TomlLexer.ArrayTableKeyStart),
  NEWLINE("a newline", TomlLexer.NewLine),
  EOF("end-of-input", TomlLexer.EOF),
  NULL("NULL", 0, TomlLexer.WS, TomlLexer.Comment, TomlLexer.Error);

  private final String displayName;
  @SuppressWarnings("ImmutableEnumChecker")
  private final BitSet tokenTypes;

  TokenName(String displayName, int... tokenTypes) {
    this.displayName = displayName;
    // offset by 1 to account for EOF being -1 (moves it to zero)
    this.tokenTypes = new BitSet(TomlLexer.VOCABULARY.getMaxTokenType() + 1);
    for (int type : tokenTypes) {
      this.tokenTypes.set(type + 1);
    }
  }

  static Stream<TokenName> namesForToken(int tokenType) {
    return Arrays.stream(TokenName.values()).filter(n -> n.tokenTypes.get(tokenType + 1));
  }

  public String displayName() {
    return displayName;
  }
}
