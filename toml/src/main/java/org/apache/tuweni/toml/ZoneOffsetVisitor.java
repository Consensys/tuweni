// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.apache.tuweni.toml;

import org.apache.tuweni.toml.internal.TomlParser.HourOffsetContext;
import org.apache.tuweni.toml.internal.TomlParser.MinuteOffsetContext;
import org.apache.tuweni.toml.internal.TomlParserBaseVisitor;

import java.time.DateTimeException;
import java.time.ZoneOffset;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;

final class ZoneOffsetVisitor extends TomlParserBaseVisitor<ZoneOffset> {

  private int hours = 0;
  private int minutes = 0;

  @Override
  public ZoneOffset visitHourOffset(HourOffsetContext ctx) {
    int hours;
    try {
      hours = Integer.parseInt(ctx.getText());
    } catch (NumberFormatException e) {
      throw new TomlParseError("Invalid zone offset", new TomlPosition(ctx), e);
    }
    if (hours < -18 || hours > 18) {
      throw new TomlParseError(
          "Invalid zone offset hours (valid range -18..+18)", new TomlPosition(ctx));
    }
    ZoneOffset offset = toZoneOffset(hours, minutes, ctx, 0);
    this.hours = hours;
    return offset;
  }

  @Override
  public ZoneOffset visitMinuteOffset(MinuteOffsetContext ctx) {
    int minutes;
    try {
      minutes = Integer.parseInt(ctx.getText());
    } catch (NumberFormatException e) {
      throw new TomlParseError("Invalid zone offset", new TomlPosition(ctx), e);
    }
    if (minutes < 0 || minutes > 59) {
      throw new TomlParseError(
          "Invalid zone offset minutes (valid range 0..59)", new TomlPosition(ctx));
    }
    ZoneOffset offset = toZoneOffset(hours, minutes, ctx, -4);
    this.minutes = minutes;
    return offset;
  }

  private static ZoneOffset toZoneOffset(
      int hours, int minutes, ParserRuleContext ctx, int offset) {
    try {
      return ZoneOffset.ofHoursMinutes(hours, (hours < 0) ? -minutes : minutes);
    } catch (DateTimeException e) {
      throw new TomlParseError(
          "Invalid zone offset (valid range -18:00..+18:00)", new TomlPosition(ctx, offset), e);
    }
  }

  @Override
  public ZoneOffset visitErrorNode(ErrorNode node) {
    return null;
  }

  @Override
  protected ZoneOffset aggregateResult(ZoneOffset aggregate, ZoneOffset nextResult) {
    return aggregate == null ? null : nextResult;
  }

  @Override
  protected ZoneOffset defaultResult() {
    return ZoneOffset.ofHoursMinutes(this.hours, (this.hours < 0) ? -this.minutes : this.minutes);
  }
}
