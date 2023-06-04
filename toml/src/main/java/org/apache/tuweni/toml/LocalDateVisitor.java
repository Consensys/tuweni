// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.toml;

import org.apache.tuweni.toml.internal.TomlParser.DayContext;
import org.apache.tuweni.toml.internal.TomlParser.MonthContext;
import org.apache.tuweni.toml.internal.TomlParser.YearContext;
import org.apache.tuweni.toml.internal.TomlParserBaseVisitor;

import java.time.DateTimeException;
import java.time.LocalDate;

import org.antlr.v4.runtime.tree.ErrorNode;

final class LocalDateVisitor extends TomlParserBaseVisitor<LocalDate> {

  private static LocalDate INITIAL = LocalDate.parse("1900-01-01");
  private LocalDate date = INITIAL;

  @Override
  public LocalDate visitYear(YearContext ctx) {
    String text = ctx.getText();
    if (text.length() != 4) {
      throw new TomlParseError("Invalid year (valid range 0000..9999)", new TomlPosition(ctx));
    }
    int year;
    try {
      year = Integer.parseInt(text);
    } catch (NumberFormatException e) {
      throw new TomlParseError("Invalid year", new TomlPosition(ctx), e);
    }
    date = date.withYear(year);
    return date;
  }

  @Override
  public LocalDate visitMonth(MonthContext ctx) {
    String text = ctx.getText();
    if (text.length() != 2) {
      throw new TomlParseError("Invalid month (valid range 01..12)", new TomlPosition(ctx));
    }
    int month;
    try {
      month = Integer.parseInt(text);
    } catch (NumberFormatException e) {
      throw new TomlParseError("Invalid month", new TomlPosition(ctx), e);
    }
    if (month < 1 || month > 12) {
      throw new TomlParseError("Invalid month (valid range 01..12)", new TomlPosition(ctx));
    }
    date = date.withMonth(month);
    return date;
  }

  @Override
  public LocalDate visitDay(DayContext ctx) {
    String text = ctx.getText();
    if (text.length() != 2) {
      throw new TomlParseError("Invalid day (valid range 01..28/31)", new TomlPosition(ctx));
    }
    int day;
    try {
      day = Integer.parseInt(text);
    } catch (NumberFormatException e) {
      throw new TomlParseError("Invalid day", new TomlPosition(ctx), e);
    }
    if (day < 1 || day > 31) {
      throw new TomlParseError("Invalid day (valid range 01..28/31)", new TomlPosition(ctx));
    }
    try {
      date = date.withDayOfMonth(day);
    } catch (DateTimeException e) {
      throw new TomlParseError(e.getMessage(), new TomlPosition(ctx), e);
    }
    return date;
  }

  @Override
  public LocalDate visitErrorNode(ErrorNode node) {
    return null;
  }

  @Override
  protected LocalDate aggregateResult(LocalDate aggregate, LocalDate nextResult) {
    return aggregate == null ? null : nextResult;
  }

  @Override
  protected LocalDate defaultResult() {
    return date;
  }
}
