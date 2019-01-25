package com.indigobyte.javautil;
/*

 * Copyright (c) 2003, 2017, Oracle and/or its affiliates. All rights reserved.

 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

 *

 * This code is free software; you can redistribute it and/or modify it

 * under the terms of the GNU General Public License version 2 only, as

 * published by the Free Software Foundation.  Oracle designates this

 * particular file as subject to the "Classpath" exception as provided

 * by Oracle in the LICENSE file that accompanied this code.

 *

 * This code is distributed in the hope that it will be useful, but WITHOUT

 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or

 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License

 * version 2 for more details (a copy is included in the LICENSE file that

 * accompanied this code).

 *

 * You should have received a copy of the GNU General Public License version

 * 2 along with this work; if not, write to the Free Software Foundation,

 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.

 *

 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA

 * or visit www.oracle.com if you need additional information or have any

 * questions.

 */


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Objects {
    @NotNull
    static <T> T requireNonNullElse(@Nullable T t, @NotNull T defaultValue) {
        if (t != null) {
            return t;
        }
        return defaultValue;
    }

    @NotNull
    static <T> T requireNonNull(@Nullable T t) {
        return java.util.Objects.requireNonNull(t);
    }

    static <T> T requireNonNull(T obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
        return obj;
    }

    static int hash(Object... o) {
        return java.util.Objects.hash(o);
    }

    static boolean equals(Object o1, Object o2) {
        return java.util.Objects.equals(o1, o2);
    }
}

/**
 * An interpreter for printf-style format strings.  This class provides support
 * <p>
 * for layout justification and alignment, common formats for numeric, string,
 * <p>
 * and date/time data, and locale-specific output.  Common Java types such as
 * <p>
 * {@code byte}, {@link BigDecimal BigDecimal}, and {@link Calendar}
 * <p>
 * are supported.  Limited formatting customization for arbitrary user types is
 * <p>
 * provided through the {@link Formattable} interface.
 *
 *
 *
 * <p> Formatters are not necessarily safe for multithreaded access.  Thread
 * <p>
 * safety is optional and is the responsibility of users of methods in this
 * <p>
 * class.
 *
 *
 *
 * <p> Formatted printing for the Java language is heavily inspired by C's
 * <p>
 * {@code printf}.  Although the format strings are similar to C, some
 * <p>
 * customizations have been made to accommodate the Java language and exploit
 * <p>
 * some of its features.  Also, Java formatting is more strict than C's; for
 * <p>
 * example, if a conversion is incompatible with a flag, an exception will be
 * <p>
 * thrown.  In C inapplicable flags are silently ignored.  The format strings
 * <p>
 * are thus intended to be recognizable to C programmers but not necessarily
 * <p>
 * completely compatible with those in C.
 *
 *
 *
 * <p> Examples of expected usage:
 *
 *
 *
 * <blockquote><pre>
 *
 *   StringBuilder sb = new StringBuilder();
 *
 *   // Send all output to the Appendable object sb
 *
 *   Formatter formatter = new Formatter(sb, Locale.US);
 *
 *
 *
 *   // Explicit argument indices may be used to re-order output.
 *
 *   formatter.format("%4$2s %3$2s %2$2s %1$2s", "a", "b", "c", "d")
 *
 *   // -&gt; " d  c  b  a"
 *
 *
 *
 *   // Optional locale as the first argument can be used to get
 *
 *   // locale-specific formatting of numbers.  The precision and width can be
 *
 *   // given to round and align the value.
 *
 *   formatter.format(Locale.FRANCE, "e = %+10.4f", Math.E);
 *
 *   // -&gt; "e =    +2,7183"
 *
 *
 *
 *   // The '(' numeric flag may be used to format negative numbers with
 *
 *   // parentheses rather than a minus sign.  Group separators are
 *
 *   // automatically inserted.
 *
 *   formatter.format("Amount gained or lost since last statement: $ %(,.2f",
 *
 *                    balanceDelta);
 *
 *   // -&gt; "Amount gained or lost since last statement: $ (6,217.58)"
 *
 * </pre></blockquote>
 *
 *
 *
 * <p> Convenience methods for common formatting requests exist as illustrated
 * <p>
 * by the following invocations:
 *
 *
 *
 * <blockquote><pre>
 *
 *   // Writes a formatted string to System.out.
 *
 *   System.out.format("Local time: %tT", Calendar.getInstance());
 *
 *   // -&gt; "Local time: 13:34:18"
 *
 *
 *
 *   // Writes formatted output to System.err.
 *
 *   System.err.printf("Unable to open file '%1$s': %2$s",
 *
 *                     fileName, exception.getMessage());
 *
 *   // -&gt; "Unable to open file 'food': No such file or directory"
 *
 * </pre></blockquote>
 *
 *
 *
 * <p> Like C's {@code sprintf(3)}, Strings may be formatted using the static
 * <p>
 * method {@link String#format(String, Object...) String.format}:
 *
 *
 *
 * <blockquote><pre>
 *
 *   // Format a string containing a date.
 *
 *   import java.util.Calendar;
 *
 *   import java.util.GregorianCalendar;
 *
 *   import static java.util.Calendar.*;
 *
 *
 *
 *   Calendar c = new GregorianCalendar(1995, MAY, 23);
 *
 *   String s = String.format("Duke's Birthday: %1$tb %1$te, %1$tY", c);
 *
 *   // -&gt; s == "Duke's Birthday: May 23, 1995"
 *
 * </pre></blockquote>
 *
 *
 *
 * <h3><a id="org">Organization</a></h3>
 *
 *
 *
 * <p> This specification is divided into two sections.  The first section, <a
 * <p>
 * href="#summary">Summary</a>, covers the basic formatting concepts.  This
 * <p>
 * section is intended for users who want to get started quickly and are
 * <p>
 * familiar with formatted printing in other programming languages.  The second
 * <p>
 * section, <a href="#detail">Details</a>, covers the specific implementation
 * <p>
 * details.  It is intended for users who want more precise specification of
 * <p>
 * formatting behavior.
 *
 *
 *
 * <h3><a id="summary">Summary</a></h3>
 *
 *
 *
 * <p> This section is intended to provide a brief overview of formatting
 * <p>
 * concepts.  For precise behavioral details, refer to the <a
 * <p>
 * href="#detail">Details</a> section.
 *
 *
 *
 * <h4><a id="syntax">Format String Syntax</a></h4>
 *
 *
 *
 * <p> Every method which produces formatted output requires a <i>format
 * <p>
 * string</i> and an <i>argument list</i>.  The format string is a {@link
 * <p>
 * String} which may contain fixed text and one or more embedded <i>format
 * <p>
 * specifiers</i>.  Consider the following example:
 *
 *
 *
 * <blockquote><pre>
 *
 *   Calendar c = ...;
 *
 *   String s = String.format("Duke's Birthday: %1$tm %1$te,%1$tY", c);
 *
 * </pre></blockquote>
 * <p>
 * <p>
 * <p>
 * This format string is the first argument to the {@code format} method.  It
 * <p>
 * contains three format specifiers "{@code %1$tm}", "{@code %1$te}", and
 * <p>
 * "{@code %1$tY}" which indicate how the arguments should be processed and
 * <p>
 * where they should be inserted in the text.  The remaining portions of the
 * <p>
 * format string are fixed text including {@code "Dukes Birthday: "} and any
 * <p>
 * other spaces or punctuation.
 * <p>
 * <p>
 * <p>
 * The argument list consists of all arguments passed to the method after the
 * <p>
 * format string.  In the above example, the argument list is of size one and
 * <p>
 * consists of the {@link Calendar Calendar} object {@code c}.
 *
 *
 *
 * <ul>
 *
 *
 *
 * <li> The format specifiers for general, character, and numeric types have
 * <p>
 * the following syntax:
 *
 *
 *
 * <blockquote><pre>
 *
 *   %[argument_index$][flags][width][.precision]conversion
 *
 * </pre></blockquote>
 *
 *
 *
 * <p> The optional <i>argument_index</i> is a decimal integer indicating the
 * <p>
 * position of the argument in the argument list.  The first argument is
 * <p>
 * referenced by "{@code 1$}", the second by "{@code 2$}", etc.
 *
 *
 *
 * <p> The optional <i>flags</i> is a set of characters that modify the output
 * <p>
 * format.  The set of valid flags depends on the conversion.
 *
 *
 *
 * <p> The optional <i>width</i> is a positive decimal integer indicating
 * <p>
 * the minimum number of characters to be written to the output.
 *
 *
 *
 * <p> The optional <i>precision</i> is a non-negative decimal integer usually
 * <p>
 * used to restrict the number of characters.  The specific behavior depends on
 * <p>
 * the conversion.
 *
 *
 *
 * <p> The required <i>conversion</i> is a character indicating how the
 * <p>
 * argument should be formatted.  The set of valid conversions for a given
 * <p>
 * argument depends on the argument's data type.
 *
 *
 *
 * <li> The format specifiers for types which are used to represents dates and
 * <p>
 * times have the following syntax:
 *
 *
 *
 * <blockquote><pre>
 *
 *   %[argument_index$][flags][width]conversion
 *
 * </pre></blockquote>
 *
 *
 *
 * <p> The optional <i>argument_index</i>, <i>flags</i> and <i>width</i> are
 * <p>
 * defined as above.
 *
 *
 *
 * <p> The required <i>conversion</i> is a two character sequence.  The first
 * <p>
 * character is {@code 't'} or {@code 'T'}.  The second character indicates
 * <p>
 * the format to be used.  These characters are similar to but not completely
 * <p>
 * identical to those defined by GNU {@code date} and POSIX
 * <p>
 * {@code strftime(3c)}.
 *
 *
 *
 * <li> The format specifiers which do not correspond to arguments have the
 * <p>
 * following syntax:
 *
 *
 *
 * <blockquote><pre>
 *
 *   %[flags][width]conversion
 *
 * </pre></blockquote>
 *
 *
 *
 * <p> The optional <i>flags</i> and <i>width</i> is defined as above.
 *
 *
 *
 * <p> The required <i>conversion</i> is a character indicating content to be
 * <p>
 * inserted in the output.
 *
 *
 *
 * </ul>
 *
 *
 *
 * <h4> Conversions </h4>
 *
 *
 *
 * <p> Conversions are divided into the following categories:
 *
 *
 *
 * <ol>
 *
 *
 *
 * <li> <b>General</b> - may be applied to any argument
 * <p>
 * type
 *
 *
 *
 * <li> <b>Character</b> - may be applied to basic types which represent
 * <p>
 * Unicode characters: {@code char}, {@link Character}, {@code byte}, {@link
 * <p>
 * Byte}, {@code short}, and {@link Short}. This conversion may also be
 * <p>
 * applied to the types {@code int} and {@link Integer} when {@link
 * <p>
 * Character#isValidCodePoint} returns {@code true}
 *
 *
 *
 * <li> <b>Numeric</b>
 *
 *
 *
 * <ol>
 *
 *
 *
 * <li> <b>Integral</b> - may be applied to Java integral types: {@code byte},
 * <p>
 * {@link Byte}, {@code short}, {@link Short}, {@code int} and {@link
 * <p>
 * Integer}, {@code long}, {@link Long}, and {@link BigInteger
 * <p>
 * BigInteger} (but not {@code char} or {@link Character})
 *
 *
 *
 * <li><b>Floating Point</b> - may be applied to Java floating-point types:
 * <p>
 * {@code float}, {@link Float}, {@code double}, {@link Double}, and {@link
 * <p>
 * java.math.BigDecimal BigDecimal}
 *
 *
 *
 * </ol>
 *
 *
 *
 * <li> <b>Date/Time</b> - may be applied to Java types which are capable of
 * <p>
 * encoding a date or time: {@code long}, {@link Long}, {@link Calendar},
 * <p>
 * {@link Date} and {@link TemporalAccessor TemporalAccessor}
 *
 *
 *
 * <li> <b>Percent</b> - produces a literal {@code '%'}
 * <p>
 * (<code>'&#92;u0025'</code>)
 *
 *
 *
 * <li> <b>Line Separator</b> - produces the platform-specific line separator
 *
 *
 *
 * </ol>
 *
 *
 *
 * <p> For category <i>General</i>, <i>Character</i>, <i>Numberic</i>,
 *
 * <i>Integral</i> and <i>Date/Time</i> conversion, unless otherwise specified,
 * <p>
 * if the argument <i>arg</i> is {@code null}, then the result is "{@code null}".
 *
 *
 *
 * <p> The following table summarizes the supported conversions.  Conversions
 * <p>
 * denoted by an upper-case character (i.e. {@code 'B'}, {@code 'H'},
 * <p>
 * {@code 'S'}, {@code 'C'}, {@code 'X'}, {@code 'E'}, {@code 'G'},
 * <p>
 * {@code 'A'}, and {@code 'T'}) are the same as those for the corresponding
 * <p>
 * lower-case conversion characters except that the result is converted to
 * <p>
 * upper case according to the rules of the prevailing {@link Locale
 * <p>
 * Locale}.  The result is equivalent to the following invocation of {@link
 * <p>
 * String#toUpperCase(Locale)}
 *
 *
 *
 * <pre>
 *
 *    out.toUpperCase(Locale.getDefault(Locale.Category.FORMAT)) </pre>
 *
 *
 *
 * <table class="striped">
 *
 * <caption style="display:none">genConv</caption>
 *
 * <thead>
 *
 * <tr><th scope="col" style="vertical-align:bottom"> Conversion
 *
 * <th scope="col" style="vertical-align:bottom"> Argument Category
 *
 * <th scope="col" style="vertical-align:bottom"> Description
 *
 * </thead>
 *
 * <tbody>
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'b'}, {@code 'B'}
 *
 * <td style="vertical-align:top"> general
 *
 * <td> If the argument <i>arg</i> is {@code null}, then the result is
 * <p>
 * "{@code false}".  If <i>arg</i> is a {@code boolean} or {@link
 * <p>
 * Boolean}, then the result is the string returned by {@link
 * <p>
 * String#valueOf(boolean) String.valueOf(arg)}.  Otherwise, the result is
 * <p>
 * "true".
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'h'}, {@code 'H'}
 *
 * <td style="vertical-align:top"> general
 *
 * <td> The result is obtained by invoking
 * <p>
 * {@code Integer.toHexString(arg.hashCode())}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 's'}, {@code 'S'}
 *
 * <td style="vertical-align:top"> general
 *
 * <td> If <i>arg</i> implements {@link Formattable}, then
 * <p>
 * {@link Formattable#formatTo arg.formatTo} is invoked. Otherwise, the
 * <p>
 * result is obtained by invoking {@code arg.toString()}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'c'}, {@code 'C'}
 *
 * <td style="vertical-align:top"> character
 *
 * <td> The result is a Unicode character
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'd'}
 *
 * <td style="vertical-align:top"> integral
 *
 * <td> The result is formatted as a decimal integer
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'o'}
 *
 * <td style="vertical-align:top"> integral
 *
 * <td> The result is formatted as an octal integer
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'x'}, {@code 'X'}
 *
 * <td style="vertical-align:top"> integral
 *
 * <td> The result is formatted as a hexadecimal integer
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'e'}, {@code 'E'}
 *
 * <td style="vertical-align:top"> floating point
 *
 * <td> The result is formatted as a decimal number in computerized
 * <p>
 * scientific notation
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'f'}
 *
 * <td style="vertical-align:top"> floating point
 *
 * <td> The result is formatted as a decimal number
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'g'}, {@code 'G'}
 *
 * <td style="vertical-align:top"> floating point
 *
 * <td> The result is formatted using computerized scientific notation or
 * <p>
 * decimal format, depending on the precision and the value after rounding.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'a'}, {@code 'A'}
 *
 * <td style="vertical-align:top"> floating point
 *
 * <td> The result is formatted as a hexadecimal floating-point number with
 * <p>
 * a significand and an exponent. This conversion is <b>not</b> supported
 * <p>
 * for the {@code BigDecimal} type despite the latter's being in the
 *
 * <i>floating point</i> argument category.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 't'}, {@code 'T'}
 *
 * <td style="vertical-align:top"> date/time
 *
 * <td> Prefix for date and time conversion characters.  See <a
 * <p>
 * href="#dt">Date/Time Conversions</a>.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code '%'}
 *
 * <td style="vertical-align:top"> percent
 *
 * <td> The result is a literal {@code '%'} (<code>'&#92;u0025'</code>)
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'n'}
 *
 * <td style="vertical-align:top"> line separator
 *
 * <td> The result is the platform-specific line separator
 *
 *
 *
 * </tbody>
 *
 * </table>
 *
 *
 *
 * <p> Any characters not explicitly defined as conversions are illegal and are
 * <p>
 * reserved for future extensions.
 *
 *
 *
 * <h4><a id="dt">Date/Time Conversions</a></h4>
 *
 *
 *
 * <p> The following date and time conversion suffix characters are defined for
 * <p>
 * the {@code 't'} and {@code 'T'} conversions.  The types are similar to but
 * <p>
 * not completely identical to those defined by GNU {@code date} and POSIX
 * <p>
 * {@code strftime(3c)}.  Additional conversion types are provided to access
 * <p>
 * Java-specific functionality (e.g. {@code 'L'} for milliseconds within the
 * <p>
 * second).
 *
 *
 *
 * <p> The following conversion characters are used for formatting times:
 *
 *
 *
 * <table class="striped">
 *
 * <caption style="display:none">time</caption>
 *
 * <tbody>
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'H'}
 *
 * <td> Hour of the day for the 24-hour clock, formatted as two digits with
 * <p>
 * a leading zero as necessary i.e. {@code 00 - 23}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'I'}
 *
 * <td> Hour for the 12-hour clock, formatted as two digits with a leading
 * <p>
 * zero as necessary, i.e.  {@code 01 - 12}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'k'}
 *
 * <td> Hour of the day for the 24-hour clock, i.e. {@code 0 - 23}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'l'}
 *
 * <td> Hour for the 12-hour clock, i.e. {@code 1 - 12}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'M'}
 *
 * <td> Minute within the hour formatted as two digits with a leading zero
 * <p>
 * as necessary, i.e.  {@code 00 - 59}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'S'}
 *
 * <td> Seconds within the minute, formatted as two digits with a leading
 * <p>
 * zero as necessary, i.e. {@code 00 - 60} ("{@code 60}" is a special
 * <p>
 * value required to support leap seconds).
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'L'}
 *
 * <td> Millisecond within the second formatted as three digits with
 * <p>
 * leading zeros as necessary, i.e. {@code 000 - 999}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'N'}
 *
 * <td> Nanosecond within the second, formatted as nine digits with leading
 * <p>
 * zeros as necessary, i.e. {@code 000000000 - 999999999}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'p'}
 *
 * <td> Locale-specific {@linkplain
 * <p>
 * java.text.DateFormatSymbols#getAmPmStrings morning or afternoon} marker
 * <p>
 * in lower case, e.g."{@code am}" or "{@code pm}". Use of the conversion
 * <p>
 * prefix {@code 'T'} forces this output to upper case.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'z'}
 *
 * <td> <a href="http://www.ietf.org/rfc/rfc0822.txt">RFC&nbsp;822</a>
 * <p>
 * style numeric time zone offset from GMT, e.g. {@code -0800}.  This
 * <p>
 * value will be adjusted as necessary for Daylight Saving Time.  For
 * <p>
 * {@code long}, {@link Long}, and {@link Date} the time zone used is
 * <p>
 * the {@linkplain TimeZone#getDefault() default time zone} for this
 * <p>
 * instance of the Java virtual machine.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'Z'}
 *
 * <td> A string representing the abbreviation for the time zone.  This
 * <p>
 * value will be adjusted as necessary for Daylight Saving Time.  For
 * <p>
 * {@code long}, {@link Long}, and {@link Date} the  time zone used is
 * <p>
 * the {@linkplain TimeZone#getDefault() default time zone} for this
 * <p>
 * instance of the Java virtual machine.  The Formatter's locale will
 * <p>
 * supersede the locale of the argument (if any).
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 's'}
 *
 * <td> Seconds since the beginning of the epoch starting at 1 January 1970
 * <p>
 * {@code 00:00:00} UTC, i.e. {@code Long.MIN_VALUE/1000} to
 * <p>
 * {@code Long.MAX_VALUE/1000}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'Q'}
 *
 * <td> Milliseconds since the beginning of the epoch starting at 1 January
 * <p>
 * 1970 {@code 00:00:00} UTC, i.e. {@code Long.MIN_VALUE} to
 * <p>
 * {@code Long.MAX_VALUE}.
 *
 *
 *
 * </tbody>
 *
 * </table>
 *
 *
 *
 * <p> The following conversion characters are used for formatting dates:
 *
 *
 *
 * <table class="striped">
 *
 * <caption style="display:none">date</caption>
 *
 * <tbody>
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'B'}
 *
 * <td> Locale-specific {@linkplain DateFormatSymbols#getMonths
 * <p>
 * full month name}, e.g. {@code "January"}, {@code "February"}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'b'}
 *
 * <td> Locale-specific {@linkplain
 * <p>
 * java.text.DateFormatSymbols#getShortMonths abbreviated month name},
 * <p>
 * e.g. {@code "Jan"}, {@code "Feb"}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'h'}
 *
 * <td> Same as {@code 'b'}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'A'}
 *
 * <td> Locale-specific full name of the {@linkplain
 * <p>
 * java.text.DateFormatSymbols#getWeekdays day of the week},
 * <p>
 * e.g. {@code "Sunday"}, {@code "Monday"}
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'a'}
 *
 * <td> Locale-specific short name of the {@linkplain
 * <p>
 * java.text.DateFormatSymbols#getShortWeekdays day of the week},
 * <p>
 * e.g. {@code "Sun"}, {@code "Mon"}
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'C'}
 *
 * <td> Four-digit year divided by {@code 100}, formatted as two digits
 * <p>
 * with leading zero as necessary, i.e. {@code 00 - 99}
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'Y'}
 *
 * <td> Year, formatted as at least four digits with leading zeros as
 * <p>
 * necessary, e.g. {@code 0092} equals {@code 92} CE for the Gregorian
 * <p>
 * calendar.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'y'}
 *
 * <td> Last two digits of the year, formatted with leading zeros as
 * <p>
 * necessary, i.e. {@code 00 - 99}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'j'}
 *
 * <td> Day of year, formatted as three digits with leading zeros as
 * <p>
 * necessary, e.g. {@code 001 - 366} for the Gregorian calendar.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'm'}
 *
 * <td> Month, formatted as two digits with leading zeros as necessary,
 * <p>
 * i.e. {@code 01 - 13}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'd'}
 *
 * <td> Day of month, formatted as two digits with leading zeros as
 * <p>
 * necessary, i.e. {@code 01 - 31}
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'e'}
 *
 * <td> Day of month, formatted as two digits, i.e. {@code 1 - 31}.
 *
 *
 *
 * </tbody>
 *
 * </table>
 *
 *
 *
 * <p> The following conversion characters are used for formatting common
 * <p>
 * date/time compositions.
 *
 *
 *
 * <table class="striped">
 *
 * <caption style="display:none">composites</caption>
 *
 * <tbody>
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'R'}
 *
 * <td> Time formatted for the 24-hour clock as {@code "%tH:%tM"}
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'T'}
 *
 * <td> Time formatted for the 24-hour clock as {@code "%tH:%tM:%tS"}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'r'}
 *
 * <td> Time formatted for the 12-hour clock as {@code "%tI:%tM:%tS %Tp"}.
 * <p>
 * The location of the morning or afternoon marker ({@code '%Tp'}) may be
 * <p>
 * locale-dependent.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'D'}
 *
 * <td> Date formatted as {@code "%tm/%td/%ty"}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'F'}
 *
 * <td> <a href="http://www.w3.org/TR/NOTE-datetime">ISO&nbsp;8601</a>
 * <p>
 * complete date formatted as {@code "%tY-%tm-%td"}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'c'}
 *
 * <td> Date and time formatted as {@code "%ta %tb %td %tT %tZ %tY"},
 * <p>
 * e.g. {@code "Sun Jul 20 16:17:00 EDT 1969"}.
 *
 *
 *
 * </tbody>
 *
 * </table>
 *
 *
 *
 * <p> Any characters not explicitly defined as date/time conversion suffixes
 * <p>
 * are illegal and are reserved for future extensions.
 *
 *
 *
 * <h4> Flags </h4>
 *
 *
 *
 * <p> The following table summarizes the supported flags.  <i>y</i> means the
 * <p>
 * flag is supported for the indicated argument types.
 *
 *
 *
 * <table class="striped">
 *
 * <caption style="display:none">genConv</caption>
 *
 * <thead>
 *
 * <tr><th scope="col" style="vertical-align:bottom"> Flag <th scope="col" style="vertical-align:bottom"> General
 *
 * <th scope="col" style="vertical-align:bottom"> Character <th scope="col" style="vertical-align:bottom"> Integral
 *
 * <th scope="col" style="vertical-align:bottom"> Floating Point
 *
 * <th scope="col" style="vertical-align:bottom"> Date/Time
 *
 * <th scope="col" style="vertical-align:bottom"> Description
 *
 * </thead>
 *
 * <tbody>
 *
 * <tr><th scope="row"> '-' <td style="text-align:center; vertical-align:top"> y
 *
 * <td style="text-align:center; vertical-align:top"> y
 *
 * <td style="text-align:center; vertical-align:top"> y
 *
 * <td style="text-align:center; vertical-align:top"> y
 *
 * <td style="text-align:center; vertical-align:top"> y
 *
 * <td> The result will be left-justified.
 *
 *
 *
 * <tr><th scope="row"> '#' <td style="text-align:center; vertical-align:top"> y<sup>1</sup>
 *
 * <td style="text-align:center; vertical-align:top"> -
 *
 * <td style="text-align:center; vertical-align:top"> y<sup>3</sup>
 *
 * <td style="text-align:center; vertical-align:top"> y
 *
 * <td style="text-align:center; vertical-align:top"> -
 *
 * <td> The result should use a conversion-dependent alternate form
 *
 *
 *
 * <tr><th scope="row"> '+' <td style="text-align:center; vertical-align:top"> -
 *
 * <td style="text-align:center; vertical-align:top"> -
 *
 * <td style="text-align:center; vertical-align:top"> y<sup>4</sup>
 *
 * <td style="text-align:center; vertical-align:top"> y
 *
 * <td style="text-align:center; vertical-align:top"> -
 *
 * <td> The result will always include a sign
 *
 *
 *
 * <tr><th scope="row"> '&nbsp;&nbsp;' <td style="text-align:center; vertical-align:top"> -
 *
 * <td style="text-align:center; vertical-align:top"> -
 *
 * <td style="text-align:center; vertical-align:top"> y<sup>4</sup>
 *
 * <td style="text-align:center; vertical-align:top"> y
 *
 * <td style="text-align:center; vertical-align:top"> -
 *
 * <td> The result will include a leading space for positive values
 *
 *
 *
 * <tr><th scope="row"> '0' <td style="text-align:center; vertical-align:top"> -
 *
 * <td style="text-align:center; vertical-align:top"> -
 *
 * <td style="text-align:center; vertical-align:top"> y
 *
 * <td style="text-align:center; vertical-align:top"> y
 *
 * <td style="text-align:center; vertical-align:top"> -
 *
 * <td> The result will be zero-padded
 *
 *
 *
 * <tr><th scope="row"> ',' <td style="text-align:center; vertical-align:top"> -
 *
 * <td style="text-align:center; vertical-align:top"> -
 *
 * <td style="text-align:center; vertical-align:top"> y<sup>2</sup>
 *
 * <td style="text-align:center; vertical-align:top"> y<sup>5</sup>
 *
 * <td style="text-align:center; vertical-align:top"> -
 *
 * <td> The result will include locale-specific {@linkplain
 * <p>
 * java.text.DecimalFormatSymbols#getGroupingSeparator grouping separators}
 *
 *
 *
 * <tr><th scope="row"> '(' <td style="text-align:center; vertical-align:top"> -
 *
 * <td style="text-align:center; vertical-align:top"> -
 *
 * <td style="text-align:center; vertical-align:top"> y<sup>4</sup>
 *
 * <td style="text-align:center; vertical-align:top"> y<sup>5</sup>
 *
 * <td style="text-align:center"> -
 *
 * <td> The result will enclose negative numbers in parentheses
 *
 *
 *
 * </tbody>
 *
 * </table>
 *
 *
 *
 * <p> <sup>1</sup> Depends on the definition of {@link Formattable}.
 *
 *
 *
 * <p> <sup>2</sup> For {@code 'd'} conversion only.
 *
 *
 *
 * <p> <sup>3</sup> For {@code 'o'}, {@code 'x'}, and {@code 'X'}
 * <p>
 * conversions only.
 *
 *
 *
 * <p> <sup>4</sup> For {@code 'd'}, {@code 'o'}, {@code 'x'}, and
 * <p>
 * {@code 'X'} conversions applied to {@link BigInteger BigInteger}
 * <p>
 * or {@code 'd'} applied to {@code byte}, {@link Byte}, {@code short}, {@link
 * <p>
 * Short}, {@code int} and {@link Integer}, {@code long}, and {@link Long}.
 *
 *
 *
 * <p> <sup>5</sup> For {@code 'e'}, {@code 'E'}, {@code 'f'},
 * <p>
 * {@code 'g'}, and {@code 'G'} conversions only.
 *
 *
 *
 * <p> Any characters not explicitly defined as flags are illegal and are
 * <p>
 * reserved for future extensions.
 *
 *
 *
 * <h4> Width </h4>
 *
 *
 *
 * <p> The width is the minimum number of characters to be written to the
 * <p>
 * output.  For the line separator conversion, width is not applicable; if it
 * <p>
 * is provided, an exception will be thrown.
 *
 *
 *
 * <h4> Precision </h4>
 *
 *
 *
 * <p> For general argument types, the precision is the maximum number of
 * <p>
 * characters to be written to the output.
 *
 *
 *
 * <p> For the floating-point conversions {@code 'a'}, {@code 'A'}, {@code 'e'},
 * <p>
 * {@code 'E'}, and {@code 'f'} the precision is the number of digits after the
 * <p>
 * radix point.  If the conversion is {@code 'g'} or {@code 'G'}, then the
 * <p>
 * precision is the total number of digits in the resulting magnitude after
 * <p>
 * rounding.
 *
 *
 *
 * <p> For character, integral, and date/time argument types and the percent
 * <p>
 * and line separator conversions, the precision is not applicable; if a
 * <p>
 * precision is provided, an exception will be thrown.
 *
 *
 *
 * <h4> Argument Index </h4>
 *
 *
 *
 * <p> The argument index is a decimal integer indicating the position of the
 * <p>
 * argument in the argument list.  The first argument is referenced by
 * <p>
 * "{@code 1$}", the second by "{@code 2$}", etc.
 *
 *
 *
 * <p> Another way to reference arguments by position is to use the
 * <p>
 * {@code '<'} (<code>'&#92;u003c'</code>) flag, which causes the argument for
 * <p>
 * the previous format specifier to be re-used.  For example, the following two
 * <p>
 * statements would produce identical strings:
 *
 *
 *
 * <blockquote><pre>
 *
 *   Calendar c = ...;
 *
 *   String s1 = String.format("Duke's Birthday: %1$tm %1$te,%1$tY", c);
 *
 *
 *
 *   String s2 = String.format("Duke's Birthday: %1$tm %&lt;te,%&lt;tY", c);
 *
 * </pre></blockquote>
 *
 *
 *
 * <hr>
 *
 * <h3><a id="detail">Details</a></h3>
 *
 *
 *
 * <p> This section is intended to provide behavioral details for formatting,
 * <p>
 * including conditions and exceptions, supported data types, localization, and
 * <p>
 * interactions between flags, conversions, and data types.  For an overview of
 * <p>
 * formatting concepts, refer to the <a href="#summary">Summary</a>
 *
 *
 *
 * <p> Any characters not explicitly defined as conversions, date/time
 * <p>
 * conversion suffixes, or flags are illegal and are reserved for
 * <p>
 * future extensions.  Use of such a character in a format string will
 * <p>
 * cause an {@link UnknownFormatConversionException} or {@link
 * <p>
 * UnknownFormatFlagsException} to be thrown.
 *
 *
 *
 * <p> If the format specifier contains a width or precision with an invalid
 * <p>
 * value or which is otherwise unsupported, then a {@link
 * <p>
 * IllegalFormatWidthException} or {@link IllegalFormatPrecisionException}
 * <p>
 * respectively will be thrown.
 *
 *
 *
 * <p> If a format specifier contains a conversion character that is not
 * <p>
 * applicable to the corresponding argument, then an {@link
 * <p>
 * IllegalFormatConversionException} will be thrown.
 *
 *
 *
 * <p> All specified exceptions may be thrown by any of the {@code format}
 * <p>
 * methods of {@code Formatter} as well as by any {@code format} convenience
 * <p>
 * methods such as {@link String#format(String, Object...) String.format} and
 * <p>
 * {@link PrintStream#printf(String, Object...) PrintStream.printf}.
 *
 *
 *
 * <p> For category <i>General</i>, <i>Character</i>, <i>Numberic</i>,
 *
 * <i>Integral</i> and <i>Date/Time</i> conversion, unless otherwise specified,
 * <p>
 * if the argument <i>arg</i> is {@code null}, then the result is "{@code null}".
 *
 *
 *
 * <p> Conversions denoted by an upper-case character (i.e. {@code 'B'},
 * <p>
 * {@code 'H'}, {@code 'S'}, {@code 'C'}, {@code 'X'}, {@code 'E'},
 * <p>
 * {@code 'G'}, {@code 'A'}, and {@code 'T'}) are the same as those for the
 * <p>
 * corresponding lower-case conversion characters except that the result is
 * <p>
 * converted to upper case according to the rules of the prevailing {@link
 * <p>
 * java.util.Locale Locale}.  The result is equivalent to the following
 * <p>
 * invocation of {@link String#toUpperCase(Locale)}
 *
 *
 *
 * <pre>
 *
 *    out.toUpperCase(Locale.getDefault(Locale.Category.FORMAT)) </pre>
 *
 *
 *
 * <h4><a id="dgen">General</a></h4>
 *
 *
 *
 * <p> The following general conversions may be applied to any argument type:
 *
 *
 *
 * <table class="striped">
 *
 * <caption style="display:none">dgConv</caption>
 *
 * <tbody>
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'b'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0062'</code>
 *
 * <td> Produces either "{@code true}" or "{@code false}" as returned by
 * <p>
 * {@link Boolean#toString(boolean)}.
 *
 *
 *
 * <p> If the argument is {@code null}, then the result is
 * <p>
 * "{@code false}".  If the argument is a {@code boolean} or {@link
 * <p>
 * Boolean}, then the result is the string returned by {@link
 * <p>
 * String#valueOf(boolean) String.valueOf()}.  Otherwise, the result is
 * <p>
 * "{@code true}".
 *
 *
 *
 * <p> If the {@code '#'} flag is given, then a {@link
 * <p>
 * FormatFlagsConversionMismatchException} will be thrown.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'B'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0042'</code>
 *
 * <td> The upper-case variant of {@code 'b'}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'h'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0068'</code>
 *
 * <td> Produces a string representing the hash code value of the object.
 *
 *
 *
 * <p> The result is obtained by invoking
 * <p>
 * {@code Integer.toHexString(arg.hashCode())}.
 *
 *
 *
 * <p> If the {@code '#'} flag is given, then a {@link
 * <p>
 * FormatFlagsConversionMismatchException} will be thrown.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'H'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0048'</code>
 *
 * <td> The upper-case variant of {@code 'h'}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 's'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0073'</code>
 *
 * <td> Produces a string.
 *
 *
 *
 * <p> If the argument implements {@link Formattable}, then
 * <p>
 * its {@link Formattable#formatTo formatTo} method is invoked.
 * <p>
 * Otherwise, the result is obtained by invoking the argument's
 * <p>
 * {@code toString()} method.
 *
 *
 *
 * <p> If the {@code '#'} flag is given and the argument is not a {@link
 * <p>
 * Formattable} , then a {@link FormatFlagsConversionMismatchException}
 * <p>
 * will be thrown.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'S'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0053'</code>
 *
 * <td> The upper-case variant of {@code 's'}.
 *
 *
 *
 * </tbody>
 *
 * </table>
 *
 *
 *
 * <p> The following <a id="dFlags">flags</a> apply to general conversions:
 *
 *
 *
 * <table class="striped">
 *
 * <caption style="display:none">dFlags</caption>
 *
 * <tbody>
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code '-'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u002d'</code>
 *
 * <td> Left justifies the output.  Spaces (<code>'&#92;u0020'</code>) will be
 * <p>
 * added at the end of the converted value as required to fill the minimum
 * <p>
 * width of the field.  If the width is not provided, then a {@link
 * <p>
 * MissingFormatWidthException} will be thrown.  If this flag is not given
 * <p>
 * then the output will be right-justified.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code '#'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0023'</code>
 *
 * <td> Requires the output use an alternate form.  The definition of the
 * <p>
 * form is specified by the conversion.
 *
 *
 *
 * </tbody>
 *
 * </table>
 *
 *
 *
 * <p> The <a id="genWidth">width</a> is the minimum number of characters to
 * <p>
 * be written to the
 * <p>
 * output.  If the length of the converted value is less than the width then
 * <p>
 * the output will be padded by <code>'&nbsp;&nbsp;'</code> (<code>'&#92;u0020'</code>)
 * <p>
 * until the total number of characters equals the width.  The padding is on
 * <p>
 * the left by default.  If the {@code '-'} flag is given, then the padding
 * <p>
 * will be on the right.  If the width is not specified then there is no
 * <p>
 * minimum.
 *
 *
 *
 * <p> The precision is the maximum number of characters to be written to the
 * <p>
 * output.  The precision is applied before the width, thus the output will be
 * <p>
 * truncated to {@code precision} characters even if the width is greater than
 * <p>
 * the precision.  If the precision is not specified then there is no explicit
 * <p>
 * limit on the number of characters.
 *
 *
 *
 * <h4><a id="dchar">Character</a></h4>
 * <p>
 * <p>
 * <p>
 * This conversion may be applied to {@code char} and {@link Character}.  It
 * <p>
 * may also be applied to the types {@code byte}, {@link Byte},
 * <p>
 * {@code short}, and {@link Short}, {@code int} and {@link Integer} when
 * <p>
 * {@link Character#isValidCodePoint} returns {@code true}.  If it returns
 * <p>
 * {@code false} then an {@link IllegalFormatCodePointException} will be
 * <p>
 * thrown.
 *
 *
 *
 * <table class="striped">
 *
 * <caption style="display:none">charConv</caption>
 *
 * <tbody>
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'c'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0063'</code>
 *
 * <td> Formats the argument as a Unicode character as described in <a
 * <p>
 * href="../lang/Character.html#unicode">Unicode Character
 * <p>
 * Representation</a>.  This may be more than one 16-bit {@code char} in
 * <p>
 * the case where the argument represents a supplementary character.
 *
 *
 *
 * <p> If the {@code '#'} flag is given, then a {@link
 * <p>
 * FormatFlagsConversionMismatchException} will be thrown.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'C'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0043'</code>
 *
 * <td> The upper-case variant of {@code 'c'}.
 *
 *
 *
 * </tbody>
 *
 * </table>
 *
 *
 *
 * <p> The {@code '-'} flag defined for <a href="#dFlags">General
 * <p>
 * conversions</a> applies.  If the {@code '#'} flag is given, then a {@link
 * <p>
 * FormatFlagsConversionMismatchException} will be thrown.
 *
 *
 *
 * <p> The width is defined as for <a href="#genWidth">General conversions</a>.
 *
 *
 *
 * <p> The precision is not applicable.  If the precision is specified then an
 * <p>
 * {@link IllegalFormatPrecisionException} will be thrown.
 *
 *
 *
 * <h4><a id="dnum">Numeric</a></h4>
 *
 *
 *
 * <p> Numeric conversions are divided into the following categories:
 *
 *
 *
 * <ol>
 *
 *
 *
 * <li> <a href="#dnint"><b>Byte, Short, Integer, and Long</b></a>
 *
 *
 *
 * <li> <a href="#dnbint"><b>BigInteger</b></a>
 *
 *
 *
 * <li> <a href="#dndec"><b>Float and Double</b></a>
 *
 *
 *
 * <li> <a href="#dnbdec"><b>BigDecimal</b></a>
 *
 *
 *
 * </ol>
 *
 *
 *
 * <p> Numeric types will be formatted according to the following algorithm:
 *
 *
 *
 * <p><b><a id="L10nAlgorithm"> Number Localization Algorithm</a></b>
 *
 *
 *
 * <p> After digits are obtained for the integer part, fractional part, and
 * <p>
 * exponent (as appropriate for the data type), the following transformation
 * <p>
 * is applied:
 *
 *
 *
 * <ol>
 *
 *
 *
 * <li> Each digit character <i>d</i> in the string is replaced by a
 * <p>
 * locale-specific digit computed relative to the current locale's
 * <p>
 * {@linkplain DecimalFormatSymbols#getZeroDigit() zero digit}
 *
 * <i>z</i>; that is <i>d&nbsp;-&nbsp;</i> {@code '0'}
 *
 * <i>&nbsp;+&nbsp;z</i>.
 *
 *
 *
 * <li> If a decimal separator is present, a locale-specific {@linkplain
 * <p>
 * java.text.DecimalFormatSymbols#getDecimalSeparator decimal separator} is
 * <p>
 * substituted.
 *
 *
 *
 * <li> If the {@code ','} (<code>'&#92;u002c'</code>)
 *
 * <a id="L10nGroup">flag</a> is given, then the locale-specific {@linkplain
 * <p>
 * java.text.DecimalFormatSymbols#getGroupingSeparator grouping separator} is
 * <p>
 * inserted by scanning the integer part of the string from least significant
 * <p>
 * to most significant digits and inserting a separator at intervals defined by
 * <p>
 * the locale's {@linkplain DecimalFormat#getGroupingSize() grouping
 * <p>
 * size}.
 *
 *
 *
 * <li> If the {@code '0'} flag is given, then the locale-specific {@linkplain
 * <p>
 * java.text.DecimalFormatSymbols#getZeroDigit() zero digits} are inserted
 * <p>
 * after the sign character, if any, and before the first non-zero digit, until
 * <p>
 * the length of the string is equal to the requested field width.
 *
 *
 *
 * <li> If the value is negative and the {@code '('} flag is given, then a
 * <p>
 * {@code '('} (<code>'&#92;u0028'</code>) is prepended and a {@code ')'}
 * <p>
 * (<code>'&#92;u0029'</code>) is appended.
 *
 *
 *
 * <li> If the value is negative (or floating-point negative zero) and
 * <p>
 * {@code '('} flag is not given, then a {@code '-'} (<code>'&#92;u002d'</code>)
 * <p>
 * is prepended.
 *
 *
 *
 * <li> If the {@code '+'} flag is given and the value is positive or zero (or
 * <p>
 * floating-point positive zero), then a {@code '+'} (<code>'&#92;u002b'</code>)
 * <p>
 * will be prepended.
 *
 *
 *
 * </ol>
 *
 *
 *
 * <p> If the value is NaN or positive infinity the literal strings "NaN" or
 * <p>
 * "Infinity" respectively, will be output.  If the value is negative infinity,
 * <p>
 * then the output will be "(Infinity)" if the {@code '('} flag is given
 * <p>
 * otherwise the output will be "-Infinity".  These values are not localized.
 *
 *
 *
 * <p><a id="dnint"><b> Byte, Short, Integer, and Long </b></a>
 *
 *
 *
 * <p> The following conversions may be applied to {@code byte}, {@link Byte},
 * <p>
 * {@code short}, {@link Short}, {@code int} and {@link Integer},
 * <p>
 * {@code long}, and {@link Long}.
 *
 *
 *
 * <table class="striped">
 *
 * <caption style="display:none">IntConv</caption>
 *
 * <tbody>
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'd'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0064'</code>
 *
 * <td> Formats the argument as a decimal integer. The <a
 * <p>
 * href="#L10nAlgorithm">localization algorithm</a> is applied.
 *
 *
 *
 * <p> If the {@code '0'} flag is given and the value is negative, then
 * <p>
 * the zero padding will occur after the sign.
 *
 *
 *
 * <p> If the {@code '#'} flag is given then a {@link
 * <p>
 * FormatFlagsConversionMismatchException} will be thrown.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'o'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u006f'</code>
 *
 * <td> Formats the argument as an integer in base eight.  No localization
 * <p>
 * is applied.
 *
 *
 *
 * <p> If <i>x</i> is negative then the result will be an unsigned value
 * <p>
 * generated by adding 2<sup>n</sup> to the value where {@code n} is the
 * <p>
 * number of bits in the type as returned by the static {@code SIZE} field
 * <p>
 * in the {@linkplain Byte#SIZE Byte}, {@linkplain Short#SIZE Short},
 * <p>
 * {@linkplain Integer#SIZE Integer}, or {@linkplain Long#SIZE Long}
 * <p>
 * classes as appropriate.
 *
 *
 *
 * <p> If the {@code '#'} flag is given then the output will always begin
 * <p>
 * with the radix indicator {@code '0'}.
 *
 *
 *
 * <p> If the {@code '0'} flag is given then the output will be padded
 * <p>
 * with leading zeros to the field width following any indication of sign.
 *
 *
 *
 * <p> If {@code '('}, {@code '+'}, '&nbsp;&nbsp;', or {@code ','} flags
 * <p>
 * are given then a {@link FormatFlagsConversionMismatchException} will be
 * <p>
 * thrown.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'x'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0078'</code>
 *
 * <td> Formats the argument as an integer in base sixteen. No
 * <p>
 * localization is applied.
 *
 *
 *
 * <p> If <i>x</i> is negative then the result will be an unsigned value
 * <p>
 * generated by adding 2<sup>n</sup> to the value where {@code n} is the
 * <p>
 * number of bits in the type as returned by the static {@code SIZE} field
 * <p>
 * in the {@linkplain Byte#SIZE Byte}, {@linkplain Short#SIZE Short},
 * <p>
 * {@linkplain Integer#SIZE Integer}, or {@linkplain Long#SIZE Long}
 * <p>
 * classes as appropriate.
 *
 *
 *
 * <p> If the {@code '#'} flag is given then the output will always begin
 * <p>
 * with the radix indicator {@code "0x"}.
 *
 *
 *
 * <p> If the {@code '0'} flag is given then the output will be padded to
 * <p>
 * the field width with leading zeros after the radix indicator or sign (if
 * <p>
 * present).
 *
 *
 *
 * <p> If {@code '('}, <code>'&nbsp;&nbsp;'</code>, {@code '+'}, or
 * <p>
 * {@code ','} flags are given then a {@link
 * <p>
 * FormatFlagsConversionMismatchException} will be thrown.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'X'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0058'</code>
 *
 * <td> The upper-case variant of {@code 'x'}.  The entire string
 * <p>
 * representing the number will be converted to {@linkplain
 * <p>
 * String#toUpperCase upper case} including the {@code 'x'} (if any) and
 * <p>
 * all hexadecimal digits {@code 'a'} - {@code 'f'}
 * <p>
 * (<code>'&#92;u0061'</code> -  <code>'&#92;u0066'</code>).
 *
 *
 *
 * </tbody>
 *
 * </table>
 *
 *
 *
 * <p> If the conversion is {@code 'o'}, {@code 'x'}, or {@code 'X'} and
 * <p>
 * both the {@code '#'} and the {@code '0'} flags are given, then result will
 * <p>
 * contain the radix indicator ({@code '0'} for octal and {@code "0x"} or
 * <p>
 * {@code "0X"} for hexadecimal), some number of zeros (based on the width),
 * <p>
 * and the value.
 *
 *
 *
 * <p> If the {@code '-'} flag is not given, then the space padding will occur
 * <p>
 * before the sign.
 *
 *
 *
 * <p> The following <a id="intFlags">flags</a> apply to numeric integral
 * <p>
 * conversions:
 *
 *
 *
 * <table class="striped">
 *
 * <caption style="display:none">intFlags</caption>
 *
 * <tbody>
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code '+'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u002b'</code>
 *
 * <td> Requires the output to include a positive sign for all positive
 * <p>
 * numbers.  If this flag is not given then only negative values will
 * <p>
 * include a sign.
 *
 *
 *
 * <p> If both the {@code '+'} and <code>'&nbsp;&nbsp;'</code> flags are given
 * <p>
 * then an {@link IllegalFormatFlagsException} will be thrown.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> <code>'&nbsp;&nbsp;'</code>
 *
 * <td style="vertical-align:top"> <code>'&#92;u0020'</code>
 *
 * <td> Requires the output to include a single extra space
 * <p>
 * (<code>'&#92;u0020'</code>) for non-negative values.
 *
 *
 *
 * <p> If both the {@code '+'} and <code>'&nbsp;&nbsp;'</code> flags are given
 * <p>
 * then an {@link IllegalFormatFlagsException} will be thrown.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code '0'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0030'</code>
 *
 * <td> Requires the output to be padded with leading {@linkplain
 * <p>
 * java.text.DecimalFormatSymbols#getZeroDigit zeros} to the minimum field
 * <p>
 * width following any sign or radix indicator except when converting NaN
 * <p>
 * or infinity.  If the width is not provided, then a {@link
 * <p>
 * MissingFormatWidthException} will be thrown.
 *
 *
 *
 * <p> If both the {@code '-'} and {@code '0'} flags are given then an
 * <p>
 * {@link IllegalFormatFlagsException} will be thrown.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code ','}
 *
 * <td style="vertical-align:top"> <code>'&#92;u002c'</code>
 *
 * <td> Requires the output to include the locale-specific {@linkplain
 * <p>
 * java.text.DecimalFormatSymbols#getGroupingSeparator group separators} as
 * <p>
 * described in the <a href="#L10nGroup">"group" section</a> of the
 * <p>
 * localization algorithm.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code '('}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0028'</code>
 *
 * <td> Requires the output to prepend a {@code '('}
 * <p>
 * (<code>'&#92;u0028'</code>) and append a {@code ')'}
 * <p>
 * (<code>'&#92;u0029'</code>) to negative values.
 *
 *
 *
 * </tbody>
 *
 * </table>
 *
 *
 *
 * <p> If no <a id="intdFlags">flags</a> are given the default formatting is
 * <p>
 * as follows:
 *
 *
 *
 * <ul>
 *
 *
 *
 * <li> The output is right-justified within the {@code width}
 *
 *
 *
 * <li> Negative numbers begin with a {@code '-'} (<code>'&#92;u002d'</code>)
 *
 *
 *
 * <li> Positive numbers and zero do not include a sign or extra leading
 * <p>
 * space
 *
 *
 *
 * <li> No grouping separators are included
 *
 *
 *
 * </ul>
 *
 *
 *
 * <p> The <a id="intWidth">width</a> is the minimum number of characters to
 * <p>
 * be written to the output.  This includes any signs, digits, grouping
 * <p>
 * separators, radix indicator, and parentheses.  If the length of the
 * <p>
 * converted value is less than the width then the output will be padded by
 * <p>
 * spaces (<code>'&#92;u0020'</code>) until the total number of characters equals
 * <p>
 * width.  The padding is on the left by default.  If {@code '-'} flag is
 * <p>
 * given then the padding will be on the right.  If width is not specified then
 * <p>
 * there is no minimum.
 *
 *
 *
 * <p> The precision is not applicable.  If precision is specified then an
 * <p>
 * {@link IllegalFormatPrecisionException} will be thrown.
 *
 *
 *
 * <p><a id="dnbint"><b> BigInteger </b></a>
 *
 *
 *
 * <p> The following conversions may be applied to {@link
 * <p>
 * java.math.BigInteger}.
 *
 *
 *
 * <table class="striped">
 *
 * <caption style="display:none">bIntConv</caption>
 *
 * <tbody>
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'd'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0064'</code>
 *
 * <td> Requires the output to be formatted as a decimal integer. The <a
 * <p>
 * href="#L10nAlgorithm">localization algorithm</a> is applied.
 *
 *
 *
 * <p> If the {@code '#'} flag is given {@link
 * <p>
 * FormatFlagsConversionMismatchException} will be thrown.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'o'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u006f'</code>
 *
 * <td> Requires the output to be formatted as an integer in base eight.
 * <p>
 * No localization is applied.
 *
 *
 *
 * <p> If <i>x</i> is negative then the result will be a signed value
 * <p>
 * beginning with {@code '-'} (<code>'&#92;u002d'</code>).  Signed output is
 * <p>
 * allowed for this type because unlike the primitive types it is not
 * <p>
 * possible to create an unsigned equivalent without assuming an explicit
 * <p>
 * data-type size.
 *
 *
 *
 * <p> If <i>x</i> is positive or zero and the {@code '+'} flag is given
 * <p>
 * then the result will begin with {@code '+'} (<code>'&#92;u002b'</code>).
 *
 *
 *
 * <p> If the {@code '#'} flag is given then the output will always begin
 * <p>
 * with {@code '0'} prefix.
 *
 *
 *
 * <p> If the {@code '0'} flag is given then the output will be padded
 * <p>
 * with leading zeros to the field width following any indication of sign.
 *
 *
 *
 * <p> If the {@code ','} flag is given then a {@link
 * <p>
 * FormatFlagsConversionMismatchException} will be thrown.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'x'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0078'</code>
 *
 * <td> Requires the output to be formatted as an integer in base
 * <p>
 * sixteen.  No localization is applied.
 *
 *
 *
 * <p> If <i>x</i> is negative then the result will be a signed value
 * <p>
 * beginning with {@code '-'} (<code>'&#92;u002d'</code>).  Signed output is
 * <p>
 * allowed for this type because unlike the primitive types it is not
 * <p>
 * possible to create an unsigned equivalent without assuming an explicit
 * <p>
 * data-type size.
 *
 *
 *
 * <p> If <i>x</i> is positive or zero and the {@code '+'} flag is given
 * <p>
 * then the result will begin with {@code '+'} (<code>'&#92;u002b'</code>).
 *
 *
 *
 * <p> If the {@code '#'} flag is given then the output will always begin
 * <p>
 * with the radix indicator {@code "0x"}.
 *
 *
 *
 * <p> If the {@code '0'} flag is given then the output will be padded to
 * <p>
 * the field width with leading zeros after the radix indicator or sign (if
 * <p>
 * present).
 *
 *
 *
 * <p> If the {@code ','} flag is given then a {@link
 * <p>
 * FormatFlagsConversionMismatchException} will be thrown.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'X'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0058'</code>
 *
 * <td> The upper-case variant of {@code 'x'}.  The entire string
 * <p>
 * representing the number will be converted to {@linkplain
 * <p>
 * String#toUpperCase upper case} including the {@code 'x'} (if any) and
 * <p>
 * all hexadecimal digits {@code 'a'} - {@code 'f'}
 * <p>
 * (<code>'&#92;u0061'</code> - <code>'&#92;u0066'</code>).
 *
 *
 *
 * </tbody>
 *
 * </table>
 *
 *
 *
 * <p> If the conversion is {@code 'o'}, {@code 'x'}, or {@code 'X'} and
 * <p>
 * both the {@code '#'} and the {@code '0'} flags are given, then result will
 * <p>
 * contain the base indicator ({@code '0'} for octal and {@code "0x"} or
 * <p>
 * {@code "0X"} for hexadecimal), some number of zeros (based on the width),
 * <p>
 * and the value.
 *
 *
 *
 * <p> If the {@code '0'} flag is given and the value is negative, then the
 * <p>
 * zero padding will occur after the sign.
 *
 *
 *
 * <p> If the {@code '-'} flag is not given, then the space padding will occur
 * <p>
 * before the sign.
 *
 *
 *
 * <p> All <a href="#intFlags">flags</a> defined for Byte, Short, Integer, and
 * <p>
 * Long apply.  The <a href="#intdFlags">default behavior</a> when no flags are
 * <p>
 * given is the same as for Byte, Short, Integer, and Long.
 *
 *
 *
 * <p> The specification of <a href="#intWidth">width</a> is the same as
 * <p>
 * defined for Byte, Short, Integer, and Long.
 *
 *
 *
 * <p> The precision is not applicable.  If precision is specified then an
 * <p>
 * {@link IllegalFormatPrecisionException} will be thrown.
 *
 *
 *
 * <p><a id="dndec"><b> Float and Double</b></a>
 *
 *
 *
 * <p> The following conversions may be applied to {@code float}, {@link
 * <p>
 * Float}, {@code double} and {@link Double}.
 *
 *
 *
 * <table class="striped">
 *
 * <caption style="display:none">floatConv</caption>
 *
 * <tbody>
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'e'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0065'</code>
 *
 * <td> Requires the output to be formatted using <a
 * <p>
 * id="scientific">computerized scientific notation</a>.  The <a
 * <p>
 * href="#L10nAlgorithm">localization algorithm</a> is applied.
 *
 *
 *
 * <p> The formatting of the magnitude <i>m</i> depends upon its value.
 *
 *
 *
 * <p> If <i>m</i> is NaN or infinite, the literal strings "NaN" or
 * <p>
 * "Infinity", respectively, will be output.  These values are not
 * <p>
 * localized.
 *
 *
 *
 * <p> If <i>m</i> is positive-zero or negative-zero, then the exponent
 * <p>
 * will be {@code "+00"}.
 *
 *
 *
 * <p> Otherwise, the result is a string that represents the sign and
 * <p>
 * magnitude (absolute value) of the argument.  The formatting of the sign
 * <p>
 * is described in the <a href="#L10nAlgorithm">localization
 * <p>
 * algorithm</a>. The formatting of the magnitude <i>m</i> depends upon its
 * <p>
 * value.
 *
 *
 *
 * <p> Let <i>n</i> be the unique integer such that 10<sup><i>n</i></sup>
 * <p>
 * &lt;= <i>m</i> &lt; 10<sup><i>n</i>+1</sup>; then let <i>a</i> be the
 * <p>
 * mathematically exact quotient of <i>m</i> and 10<sup><i>n</i></sup> so
 * <p>
 * that 1 &lt;= <i>a</i> &lt; 10. The magnitude is then represented as the
 * <p>
 * integer part of <i>a</i>, as a single decimal digit, followed by the
 * <p>
 * decimal separator followed by decimal digits representing the fractional
 * <p>
 * part of <i>a</i>, followed by the exponent symbol {@code 'e'}
 * <p>
 * (<code>'&#92;u0065'</code>), followed by the sign of the exponent, followed
 * <p>
 * by a representation of <i>n</i> as a decimal integer, as produced by the
 * <p>
 * method {@link Long#toString(long, int)}, and zero-padded to include at
 * <p>
 * least two digits.
 *
 *
 *
 * <p> The number of digits in the result for the fractional part of
 *
 * <i>m</i> or <i>a</i> is equal to the precision.  If the precision is not
 * <p>
 * specified then the default value is {@code 6}. If the precision is less
 * <p>
 * than the number of digits which would appear after the decimal point in
 * <p>
 * the string returned by {@link Float#toString(float)} or {@link
 * <p>
 * Double#toString(double)} respectively, then the value will be rounded
 * <p>
 * using the {@linkplain RoundingMode#HALF_UP round half up
 * <p>
 * algorithm}.  Otherwise, zeros may be appended to reach the precision.
 * <p>
 * For a canonical representation of the value, use {@link
 * <p>
 * Float#toString(float)} or {@link Double#toString(double)} as
 * <p>
 * appropriate.
 *
 *
 *
 * <p>If the {@code ','} flag is given, then an {@link
 * <p>
 * FormatFlagsConversionMismatchException} will be thrown.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'E'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0045'</code>
 *
 * <td> The upper-case variant of {@code 'e'}.  The exponent symbol
 * <p>
 * will be {@code 'E'} (<code>'&#92;u0045'</code>).
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'g'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0067'</code>
 *
 * <td> Requires the output to be formatted in general scientific notation
 * <p>
 * as described below. The <a href="#L10nAlgorithm">localization
 * <p>
 * algorithm</a> is applied.
 *
 *
 *
 * <p> After rounding for the precision, the formatting of the resulting
 * <p>
 * magnitude <i>m</i> depends on its value.
 *
 *
 *
 * <p> If <i>m</i> is greater than or equal to 10<sup>-4</sup> but less
 * <p>
 * than 10<sup>precision</sup> then it is represented in <i><a
 * <p>
 * href="#decimal">decimal format</a></i>.
 *
 *
 *
 * <p> If <i>m</i> is less than 10<sup>-4</sup> or greater than or equal to
 * <p>
 * 10<sup>precision</sup>, then it is represented in <i><a
 * <p>
 * href="#scientific">computerized scientific notation</a></i>.
 *
 *
 *
 * <p> The total number of significant digits in <i>m</i> is equal to the
 * <p>
 * precision.  If the precision is not specified, then the default value is
 * <p>
 * {@code 6}.  If the precision is {@code 0}, then it is taken to be
 * <p>
 * {@code 1}.
 *
 *
 *
 * <p> If the {@code '#'} flag is given then an {@link
 * <p>
 * FormatFlagsConversionMismatchException} will be thrown.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'G'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0047'</code>
 *
 * <td> The upper-case variant of {@code 'g'}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'f'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0066'</code>
 *
 * <td> Requires the output to be formatted using <a id="decimal">decimal
 * <p>
 * format</a>.  The <a href="#L10nAlgorithm">localization algorithm</a> is
 * <p>
 * applied.
 *
 *
 *
 * <p> The result is a string that represents the sign and magnitude
 * <p>
 * (absolute value) of the argument.  The formatting of the sign is
 * <p>
 * described in the <a href="#L10nAlgorithm">localization
 * <p>
 * algorithm</a>. The formatting of the magnitude <i>m</i> depends upon its
 * <p>
 * value.
 *
 *
 *
 * <p> If <i>m</i> NaN or infinite, the literal strings "NaN" or
 * <p>
 * "Infinity", respectively, will be output.  These values are not
 * <p>
 * localized.
 *
 *
 *
 * <p> The magnitude is formatted as the integer part of <i>m</i>, with no
 * <p>
 * leading zeroes, followed by the decimal separator followed by one or
 * <p>
 * more decimal digits representing the fractional part of <i>m</i>.
 *
 *
 *
 * <p> The number of digits in the result for the fractional part of
 *
 * <i>m</i> or <i>a</i> is equal to the precision.  If the precision is not
 * <p>
 * specified then the default value is {@code 6}. If the precision is less
 * <p>
 * than the number of digits which would appear after the decimal point in
 * <p>
 * the string returned by {@link Float#toString(float)} or {@link
 * <p>
 * Double#toString(double)} respectively, then the value will be rounded
 * <p>
 * using the {@linkplain RoundingMode#HALF_UP round half up
 * <p>
 * algorithm}.  Otherwise, zeros may be appended to reach the precision.
 * <p>
 * For a canonical representation of the value, use {@link
 * <p>
 * Float#toString(float)} or {@link Double#toString(double)} as
 * <p>
 * appropriate.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'a'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0061'</code>
 *
 * <td> Requires the output to be formatted in hexadecimal exponential
 * <p>
 * form.  No localization is applied.
 *
 *
 *
 * <p> The result is a string that represents the sign and magnitude
 * <p>
 * (absolute value) of the argument <i>x</i>.
 *
 *
 *
 * <p> If <i>x</i> is negative or a negative-zero value then the result
 * <p>
 * will begin with {@code '-'} (<code>'&#92;u002d'</code>).
 *
 *
 *
 * <p> If <i>x</i> is positive or a positive-zero value and the
 * <p>
 * {@code '+'} flag is given then the result will begin with {@code '+'}
 * <p>
 * (<code>'&#92;u002b'</code>).
 *
 *
 *
 * <p> The formatting of the magnitude <i>m</i> depends upon its value.
 *
 *
 *
 * <ul>
 *
 *
 *
 * <li> If the value is NaN or infinite, the literal strings "NaN" or
 * <p>
 * "Infinity", respectively, will be output.
 *
 *
 *
 * <li> If <i>m</i> is zero then it is represented by the string
 * <p>
 * {@code "0x0.0p0"}.
 *
 *
 *
 * <li> If <i>m</i> is a {@code double} value with a normalized
 * <p>
 * representation then substrings are used to represent the significand and
 * <p>
 * exponent fields.  The significand is represented by the characters
 * <p>
 * {@code "0x1."} followed by the hexadecimal representation of the rest
 * <p>
 * of the significand as a fraction.  The exponent is represented by
 * <p>
 * {@code 'p'} (<code>'&#92;u0070'</code>) followed by a decimal string of the
 * <p>
 * unbiased exponent as if produced by invoking {@link
 * <p>
 * Integer#toString(int) Integer.toString} on the exponent value.  If the
 * <p>
 * precision is specified, the value is rounded to the given number of
 * <p>
 * hexadecimal digits.
 *
 *
 *
 * <li> If <i>m</i> is a {@code double} value with a subnormal
 * <p>
 * representation then, unless the precision is specified to be in the range
 * <p>
 * 1 through 12, inclusive, the significand is represented by the characters
 * <p>
 * {@code '0x0.'} followed by the hexadecimal representation of the rest of
 * <p>
 * the significand as a fraction, and the exponent represented by
 * <p>
 * {@code 'p-1022'}.  If the precision is in the interval
 * <p>
 * [1,&nbsp;12], the subnormal value is normalized such that it
 * <p>
 * begins with the characters {@code '0x1.'}, rounded to the number of
 * <p>
 * hexadecimal digits of precision, and the exponent adjusted
 * <p>
 * accordingly.  Note that there must be at least one nonzero digit in a
 * <p>
 * subnormal significand.
 *
 *
 *
 * </ul>
 *
 *
 *
 * <p> If the {@code '('} or {@code ','} flags are given, then a {@link
 * <p>
 * FormatFlagsConversionMismatchException} will be thrown.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'A'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0041'</code>
 *
 * <td> The upper-case variant of {@code 'a'}.  The entire string
 * <p>
 * representing the number will be converted to upper case including the
 * <p>
 * {@code 'x'} (<code>'&#92;u0078'</code>) and {@code 'p'}
 * <p>
 * (<code>'&#92;u0070'</code> and all hexadecimal digits {@code 'a'} -
 * <p>
 * {@code 'f'} (<code>'&#92;u0061'</code> - <code>'&#92;u0066'</code>).
 *
 *
 *
 * </tbody>
 *
 * </table>
 *
 *
 *
 * <p> All <a href="#intFlags">flags</a> defined for Byte, Short, Integer, and
 * <p>
 * Long apply.
 *
 *
 *
 * <p> If the {@code '#'} flag is given, then the decimal separator will
 * <p>
 * always be present.
 *
 *
 *
 * <p> If no <a id="floatdFlags">flags</a> are given the default formatting
 * <p>
 * is as follows:
 *
 *
 *
 * <ul>
 *
 *
 *
 * <li> The output is right-justified within the {@code width}
 *
 *
 *
 * <li> Negative numbers begin with a {@code '-'}
 *
 *
 *
 * <li> Positive numbers and positive zero do not include a sign or extra
 * <p>
 * leading space
 *
 *
 *
 * <li> No grouping separators are included
 *
 *
 *
 * <li> The decimal separator will only appear if a digit follows it
 *
 *
 *
 * </ul>
 *
 *
 *
 * <p> The <a id="floatDWidth">width</a> is the minimum number of characters
 * <p>
 * to be written to the output.  This includes any signs, digits, grouping
 * <p>
 * separators, decimal separators, exponential symbol, radix indicator,
 * <p>
 * parentheses, and strings representing infinity and NaN as applicable.  If
 * <p>
 * the length of the converted value is less than the width then the output
 * <p>
 * will be padded by spaces (<code>'&#92;u0020'</code>) until the total number of
 * <p>
 * characters equals width.  The padding is on the left by default.  If the
 * <p>
 * {@code '-'} flag is given then the padding will be on the right.  If width
 * <p>
 * is not specified then there is no minimum.
 *
 *
 *
 * <p> If the <a id="floatDPrec">conversion</a> is {@code 'e'},
 * <p>
 * {@code 'E'} or {@code 'f'}, then the precision is the number of digits
 * <p>
 * after the decimal separator.  If the precision is not specified, then it is
 * <p>
 * assumed to be {@code 6}.
 *
 *
 *
 * <p> If the conversion is {@code 'g'} or {@code 'G'}, then the precision is
 * <p>
 * the total number of significant digits in the resulting magnitude after
 * <p>
 * rounding.  If the precision is not specified, then the default value is
 * <p>
 * {@code 6}.  If the precision is {@code 0}, then it is taken to be
 * <p>
 * {@code 1}.
 *
 *
 *
 * <p> If the conversion is {@code 'a'} or {@code 'A'}, then the precision
 * <p>
 * is the number of hexadecimal digits after the radix point.  If the
 * <p>
 * precision is not provided, then all of the digits as returned by {@link
 * <p>
 * Double#toHexString(double)} will be output.
 *
 *
 *
 * <p><a id="dnbdec"><b> BigDecimal </b></a>
 *
 *
 *
 * <p> The following conversions may be applied {@link BigDecimal
 * <p>
 * BigDecimal}.
 *
 *
 *
 * <table class="striped">
 *
 * <caption style="display:none">floatConv</caption>
 *
 * <tbody>
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'e'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0065'</code>
 *
 * <td> Requires the output to be formatted using <a
 * <p>
 * id="bscientific">computerized scientific notation</a>.  The <a
 * <p>
 * href="#L10nAlgorithm">localization algorithm</a> is applied.
 *
 *
 *
 * <p> The formatting of the magnitude <i>m</i> depends upon its value.
 *
 *
 *
 * <p> If <i>m</i> is positive-zero or negative-zero, then the exponent
 * <p>
 * will be {@code "+00"}.
 *
 *
 *
 * <p> Otherwise, the result is a string that represents the sign and
 * <p>
 * magnitude (absolute value) of the argument.  The formatting of the sign
 * <p>
 * is described in the <a href="#L10nAlgorithm">localization
 * <p>
 * algorithm</a>. The formatting of the magnitude <i>m</i> depends upon its
 * <p>
 * value.
 *
 *
 *
 * <p> Let <i>n</i> be the unique integer such that 10<sup><i>n</i></sup>
 * <p>
 * &lt;= <i>m</i> &lt; 10<sup><i>n</i>+1</sup>; then let <i>a</i> be the
 * <p>
 * mathematically exact quotient of <i>m</i> and 10<sup><i>n</i></sup> so
 * <p>
 * that 1 &lt;= <i>a</i> &lt; 10. The magnitude is then represented as the
 * <p>
 * integer part of <i>a</i>, as a single decimal digit, followed by the
 * <p>
 * decimal separator followed by decimal digits representing the fractional
 * <p>
 * part of <i>a</i>, followed by the exponent symbol {@code 'e'}
 * <p>
 * (<code>'&#92;u0065'</code>), followed by the sign of the exponent, followed
 * <p>
 * by a representation of <i>n</i> as a decimal integer, as produced by the
 * <p>
 * method {@link Long#toString(long, int)}, and zero-padded to include at
 * <p>
 * least two digits.
 *
 *
 *
 * <p> The number of digits in the result for the fractional part of
 *
 * <i>m</i> or <i>a</i> is equal to the precision.  If the precision is not
 * <p>
 * specified then the default value is {@code 6}.  If the precision is
 * <p>
 * less than the number of digits to the right of the decimal point then
 * <p>
 * the value will be rounded using the
 * <p>
 * {@linkplain RoundingMode#HALF_UP round half up
 * <p>
 * algorithm}.  Otherwise, zeros may be appended to reach the precision.
 * <p>
 * For a canonical representation of the value, use {@link
 * <p>
 * BigDecimal#toString()}.
 *
 *
 *
 * <p> If the {@code ','} flag is given, then an {@link
 * <p>
 * FormatFlagsConversionMismatchException} will be thrown.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'E'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0045'</code>
 *
 * <td> The upper-case variant of {@code 'e'}.  The exponent symbol
 * <p>
 * will be {@code 'E'} (<code>'&#92;u0045'</code>).
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'g'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0067'</code>
 *
 * <td> Requires the output to be formatted in general scientific notation
 * <p>
 * as described below. The <a href="#L10nAlgorithm">localization
 * <p>
 * algorithm</a> is applied.
 *
 *
 *
 * <p> After rounding for the precision, the formatting of the resulting
 * <p>
 * magnitude <i>m</i> depends on its value.
 *
 *
 *
 * <p> If <i>m</i> is greater than or equal to 10<sup>-4</sup> but less
 * <p>
 * than 10<sup>precision</sup> then it is represented in <i><a
 * <p>
 * href="#bdecimal">decimal format</a></i>.
 *
 *
 *
 * <p> If <i>m</i> is less than 10<sup>-4</sup> or greater than or equal to
 * <p>
 * 10<sup>precision</sup>, then it is represented in <i><a
 * <p>
 * href="#bscientific">computerized scientific notation</a></i>.
 *
 *
 *
 * <p> The total number of significant digits in <i>m</i> is equal to the
 * <p>
 * precision.  If the precision is not specified, then the default value is
 * <p>
 * {@code 6}.  If the precision is {@code 0}, then it is taken to be
 * <p>
 * {@code 1}.
 *
 *
 *
 * <p> If the {@code '#'} flag is given then an {@link
 * <p>
 * FormatFlagsConversionMismatchException} will be thrown.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'G'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0047'</code>
 *
 * <td> The upper-case variant of {@code 'g'}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'f'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0066'</code>
 *
 * <td> Requires the output to be formatted using <a id="bdecimal">decimal
 * <p>
 * format</a>.  The <a href="#L10nAlgorithm">localization algorithm</a> is
 * <p>
 * applied.
 *
 *
 *
 * <p> The result is a string that represents the sign and magnitude
 * <p>
 * (absolute value) of the argument.  The formatting of the sign is
 * <p>
 * described in the <a href="#L10nAlgorithm">localization
 * <p>
 * algorithm</a>. The formatting of the magnitude <i>m</i> depends upon its
 * <p>
 * value.
 *
 *
 *
 * <p> The magnitude is formatted as the integer part of <i>m</i>, with no
 * <p>
 * leading zeroes, followed by the decimal separator followed by one or
 * <p>
 * more decimal digits representing the fractional part of <i>m</i>.
 *
 *
 *
 * <p> The number of digits in the result for the fractional part of
 *
 * <i>m</i> or <i>a</i> is equal to the precision. If the precision is not
 * <p>
 * specified then the default value is {@code 6}.  If the precision is
 * <p>
 * less than the number of digits to the right of the decimal point
 * <p>
 * then the value will be rounded using the
 * <p>
 * {@linkplain RoundingMode#HALF_UP round half up
 * <p>
 * algorithm}.  Otherwise, zeros may be appended to reach the precision.
 * <p>
 * For a canonical representation of the value, use {@link
 * <p>
 * BigDecimal#toString()}.
 *
 *
 *
 * </tbody>
 *
 * </table>
 *
 *
 *
 * <p> All <a href="#intFlags">flags</a> defined for Byte, Short, Integer, and
 * <p>
 * Long apply.
 *
 *
 *
 * <p> If the {@code '#'} flag is given, then the decimal separator will
 * <p>
 * always be present.
 *
 *
 *
 * <p> The <a href="#floatdFlags">default behavior</a> when no flags are
 * <p>
 * given is the same as for Float and Double.
 *
 *
 *
 * <p> The specification of <a href="#floatDWidth">width</a> and <a
 * <p>
 * href="#floatDPrec">precision</a> is the same as defined for Float and
 * <p>
 * Double.
 *
 *
 *
 * <h4><a id="ddt">Date/Time</a></h4>
 *
 *
 *
 * <p> This conversion may be applied to {@code long}, {@link Long}, {@link
 * <p>
 * Calendar}, {@link Date} and {@link TemporalAccessor TemporalAccessor}
 *
 *
 *
 * <table class="striped">
 *
 * <caption style="display:none">DTConv</caption>
 *
 * <tbody>
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 't'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0074'</code>
 *
 * <td> Prefix for date and time conversion characters.
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'T'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0054'</code>
 *
 * <td> The upper-case variant of {@code 't'}.
 *
 *
 *
 * </tbody>
 *
 * </table>
 *
 *
 *
 * <p> The following date and time conversion character suffixes are defined
 * <p>
 * for the {@code 't'} and {@code 'T'} conversions.  The types are similar to
 * <p>
 * but not completely identical to those defined by GNU {@code date} and
 * <p>
 * POSIX {@code strftime(3c)}.  Additional conversion types are provided to
 * <p>
 * access Java-specific functionality (e.g. {@code 'L'} for milliseconds
 * <p>
 * within the second).
 *
 *
 *
 * <p> The following conversion characters are used for formatting times:
 *
 *
 *
 * <table class="striped">
 *
 * <caption style="display:none">time</caption>
 *
 * <tbody>
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top"> {@code 'H'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0048'</code>
 *
 * <td> Hour of the day for the 24-hour clock, formatted as two digits with
 * <p>
 * a leading zero as necessary i.e. {@code 00 - 23}. {@code 00}
 * <p>
 * corresponds to midnight.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'I'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0049'</code>
 *
 * <td> Hour for the 12-hour clock, formatted as two digits with a leading
 * <p>
 * zero as necessary, i.e.  {@code 01 - 12}.  {@code 01} corresponds to
 * <p>
 * one o'clock (either morning or afternoon).
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'k'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u006b'</code>
 *
 * <td> Hour of the day for the 24-hour clock, i.e. {@code 0 - 23}.
 * <p>
 * {@code 0} corresponds to midnight.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'l'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u006c'</code>
 *
 * <td> Hour for the 12-hour clock, i.e. {@code 1 - 12}.  {@code 1}
 * <p>
 * corresponds to one o'clock (either morning or afternoon).
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'M'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u004d'</code>
 *
 * <td> Minute within the hour formatted as two digits with a leading zero
 * <p>
 * as necessary, i.e.  {@code 00 - 59}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'S'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0053'</code>
 *
 * <td> Seconds within the minute, formatted as two digits with a leading
 * <p>
 * zero as necessary, i.e. {@code 00 - 60} ("{@code 60}" is a special
 * <p>
 * value required to support leap seconds).
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'L'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u004c'</code>
 *
 * <td> Millisecond within the second formatted as three digits with
 * <p>
 * leading zeros as necessary, i.e. {@code 000 - 999}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'N'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u004e'</code>
 *
 * <td> Nanosecond within the second, formatted as nine digits with leading
 * <p>
 * zeros as necessary, i.e. {@code 000000000 - 999999999}.  The precision
 * <p>
 * of this value is limited by the resolution of the underlying operating
 * <p>
 * system or hardware.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'p'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0070'</code>
 *
 * <td> Locale-specific {@linkplain
 * <p>
 * java.text.DateFormatSymbols#getAmPmStrings morning or afternoon} marker
 * <p>
 * in lower case, e.g."{@code am}" or "{@code pm}".  Use of the
 * <p>
 * conversion prefix {@code 'T'} forces this output to upper case.  (Note
 * <p>
 * that {@code 'p'} produces lower-case output.  This is different from
 * <p>
 * GNU {@code date} and POSIX {@code strftime(3c)} which produce
 * <p>
 * upper-case output.)
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'z'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u007a'</code>
 *
 * <td> <a href="http://www.ietf.org/rfc/rfc0822.txt">RFC&nbsp;822</a>
 * <p>
 * style numeric time zone offset from GMT, e.g. {@code -0800}.  This
 * <p>
 * value will be adjusted as necessary for Daylight Saving Time.  For
 * <p>
 * {@code long}, {@link Long}, and {@link Date} the time zone used is
 * <p>
 * the {@linkplain TimeZone#getDefault() default time zone} for this
 * <p>
 * instance of the Java virtual machine.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'Z'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u005a'</code>
 *
 * <td> A string representing the abbreviation for the time zone.  This
 * <p>
 * value will be adjusted as necessary for Daylight Saving Time.  For
 * <p>
 * {@code long}, {@link Long}, and {@link Date} the time zone used is
 * <p>
 * the {@linkplain TimeZone#getDefault() default time zone} for this
 * <p>
 * instance of the Java virtual machine.  The Formatter's locale will
 * <p>
 * supersede the locale of the argument (if any).
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 's'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0073'</code>
 *
 * <td> Seconds since the beginning of the epoch starting at 1 January 1970
 * <p>
 * {@code 00:00:00} UTC, i.e. {@code Long.MIN_VALUE/1000} to
 * <p>
 * {@code Long.MAX_VALUE/1000}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'Q'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u004f'</code>
 *
 * <td> Milliseconds since the beginning of the epoch starting at 1 January
 * <p>
 * 1970 {@code 00:00:00} UTC, i.e. {@code Long.MIN_VALUE} to
 * <p>
 * {@code Long.MAX_VALUE}. The precision of this value is limited by
 * <p>
 * the resolution of the underlying operating system or hardware.
 *
 *
 *
 * </tbody>
 *
 * </table>
 *
 *
 *
 * <p> The following conversion characters are used for formatting dates:
 *
 *
 *
 * <table class="striped">
 *
 * <caption style="display:none">date</caption>
 *
 * <tbody>
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'B'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0042'</code>
 *
 * <td> Locale-specific {@linkplain DateFormatSymbols#getMonths
 * <p>
 * full month name}, e.g. {@code "January"}, {@code "February"}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'b'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0062'</code>
 *
 * <td> Locale-specific {@linkplain
 * <p>
 * java.text.DateFormatSymbols#getShortMonths abbreviated month name},
 * <p>
 * e.g. {@code "Jan"}, {@code "Feb"}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'h'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0068'</code>
 *
 * <td> Same as {@code 'b'}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'A'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0041'</code>
 *
 * <td> Locale-specific full name of the {@linkplain
 * <p>
 * java.text.DateFormatSymbols#getWeekdays day of the week},
 * <p>
 * e.g. {@code "Sunday"}, {@code "Monday"}
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'a'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0061'</code>
 *
 * <td> Locale-specific short name of the {@linkplain
 * <p>
 * java.text.DateFormatSymbols#getShortWeekdays day of the week},
 * <p>
 * e.g. {@code "Sun"}, {@code "Mon"}
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'C'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0043'</code>
 *
 * <td> Four-digit year divided by {@code 100}, formatted as two digits
 * <p>
 * with leading zero as necessary, i.e. {@code 00 - 99}
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'Y'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0059'</code> <td> Year, formatted to at least
 * <p>
 * four digits with leading zeros as necessary, e.g. {@code 0092} equals
 * <p>
 * {@code 92} CE for the Gregorian calendar.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'y'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0079'</code>
 *
 * <td> Last two digits of the year, formatted with leading zeros as
 * <p>
 * necessary, i.e. {@code 00 - 99}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'j'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u006a'</code>
 *
 * <td> Day of year, formatted as three digits with leading zeros as
 * <p>
 * necessary, e.g. {@code 001 - 366} for the Gregorian calendar.
 * <p>
 * {@code 001} corresponds to the first day of the year.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'm'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u006d'</code>
 *
 * <td> Month, formatted as two digits with leading zeros as necessary,
 * <p>
 * i.e. {@code 01 - 13}, where "{@code 01}" is the first month of the
 * <p>
 * year and ("{@code 13}" is a special value required to support lunar
 * <p>
 * calendars).
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'd'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0064'</code>
 *
 * <td> Day of month, formatted as two digits with leading zeros as
 * <p>
 * necessary, i.e. {@code 01 - 31}, where "{@code 01}" is the first day
 * <p>
 * of the month.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'e'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0065'</code>
 *
 * <td> Day of month, formatted as two digits, i.e. {@code 1 - 31} where
 * <p>
 * "{@code 1}" is the first day of the month.
 *
 *
 *
 * </tbody>
 *
 * </table>
 *
 *
 *
 * <p> The following conversion characters are used for formatting common
 * <p>
 * date/time compositions.
 *
 *
 *
 * <table class="striped">
 *
 * <caption style="display:none">composites</caption>
 *
 * <tbody>
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'R'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0052'</code>
 *
 * <td> Time formatted for the 24-hour clock as {@code "%tH:%tM"}
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'T'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0054'</code>
 *
 * <td> Time formatted for the 24-hour clock as {@code "%tH:%tM:%tS"}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'r'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0072'</code>
 *
 * <td> Time formatted for the 12-hour clock as {@code "%tI:%tM:%tS
 * <p>
 * %Tp"}.  The location of the morning or afternoon marker
 * <p>
 * ({@code '%Tp'}) may be locale-dependent.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'D'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0044'</code>
 *
 * <td> Date formatted as {@code "%tm/%td/%ty"}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'F'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0046'</code>
 *
 * <td> <a href="http://www.w3.org/TR/NOTE-datetime">ISO&nbsp;8601</a>
 * <p>
 * complete date formatted as {@code "%tY-%tm-%td"}.
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'c'}
 *
 * <td style="vertical-align:top"> <code>'&#92;u0063'</code>
 *
 * <td> Date and time formatted as {@code "%ta %tb %td %tT %tZ %tY"},
 * <p>
 * e.g. {@code "Sun Jul 20 16:17:00 EDT 1969"}.
 *
 *
 *
 * </tbody>
 *
 * </table>
 *
 *
 *
 * <p> The {@code '-'} flag defined for <a href="#dFlags">General
 * <p>
 * conversions</a> applies.  If the {@code '#'} flag is given, then a {@link
 * <p>
 * FormatFlagsConversionMismatchException} will be thrown.
 *
 *
 *
 * <p> The width is the minimum number of characters to
 * <p>
 * be written to the output.  If the length of the converted value is less than
 * <p>
 * the {@code width} then the output will be padded by spaces
 * <p>
 * (<code>'&#92;u0020'</code>) until the total number of characters equals width.
 * <p>
 * The padding is on the left by default.  If the {@code '-'} flag is given
 * <p>
 * then the padding will be on the right.  If width is not specified then there
 * <p>
 * is no minimum.
 *
 *
 *
 * <p> The precision is not applicable.  If the precision is specified then an
 * <p>
 * {@link IllegalFormatPrecisionException} will be thrown.
 *
 *
 *
 * <h4><a id="dper">Percent</a></h4>
 *
 *
 *
 * <p> The conversion does not correspond to any argument.
 *
 *
 *
 * <table class="striped">
 *
 * <caption style="display:none">DTConv</caption>
 *
 * <tbody>
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code '%'}
 *
 * <td> The result is a literal {@code '%'} (<code>'&#92;u0025'</code>)
 *
 *
 *
 * <p> The width is the minimum number of characters to
 * <p>
 * be written to the output including the {@code '%'}.  If the length of the
 * <p>
 * converted value is less than the {@code width} then the output will be
 * <p>
 * padded by spaces (<code>'&#92;u0020'</code>) until the total number of
 * <p>
 * characters equals width.  The padding is on the left.  If width is not
 * <p>
 * specified then just the {@code '%'} is output.
 *
 *
 *
 * <p> The {@code '-'} flag defined for <a href="#dFlags">General
 * <p>
 * conversions</a> applies.  If any other flags are provided, then a
 * <p>
 * {@link FormatFlagsConversionMismatchException} will be thrown.
 *
 *
 *
 * <p> The precision is not applicable.  If the precision is specified an
 * <p>
 * {@link IllegalFormatPrecisionException} will be thrown.
 *
 *
 *
 * </tbody>
 *
 * </table>
 *
 *
 *
 * <h4><a id="dls">Line Separator</a></h4>
 *
 *
 *
 * <p> The conversion does not correspond to any argument.
 *
 *
 *
 * <table class="striped">
 *
 * <caption style="display:none">DTConv</caption>
 *
 * <tbody>
 *
 *
 *
 * <tr><th scope="row" style="vertical-align:top">{@code 'n'}
 *
 * <td> the platform-specific line separator as returned by {@link
 * <p>
 * System#lineSeparator()}.
 *
 *
 *
 * </tbody>
 *
 * </table>
 *
 *
 *
 * <p> Flags, width, and precision are not applicable.  If any are provided an
 * <p>
 * {@link IllegalFormatFlagsException}, {@link IllegalFormatWidthException},
 * <p>
 * and {@link IllegalFormatPrecisionException}, respectively will be thrown.
 *
 *
 *
 * <h4><a id="dpos">Argument Index</a></h4>
 *
 *
 *
 * <p> Format specifiers can reference arguments in three ways:
 *
 *
 *
 * <ul>
 *
 *
 *
 * <li> <i>Explicit indexing</i> is used when the format specifier contains an
 * <p>
 * argument index.  The argument index is a decimal integer indicating the
 * <p>
 * position of the argument in the argument list.  The first argument is
 * <p>
 * referenced by "{@code 1$}", the second by "{@code 2$}", etc.  An argument
 * <p>
 * may be referenced more than once.
 *
 *
 *
 * <p> For example:
 *
 *
 *
 * <blockquote><pre>
 *
 *   formatter.format("%4$s %3$s %2$s %1$s %4$s %3$s %2$s %1$s",
 *
 *                    "a", "b", "c", "d")
 *
 *   // -&gt; "d c b a d c b a"
 *
 * </pre></blockquote>
 *
 *
 *
 * <li> <i>Relative indexing</i> is used when the format specifier contains a
 * <p>
 * {@code '<'} (<code>'&#92;u003c'</code>) flag which causes the argument for
 * <p>
 * the previous format specifier to be re-used.  If there is no previous
 * <p>
 * argument, then a {@link MissingFormatArgumentException} is thrown.
 *
 *
 *
 * <blockquote><pre>
 *
 *    formatter.format("%s %s %&lt;s %&lt;s", "a", "b", "c", "d")
 *
 *    // -&gt; "a b b b"
 *
 *    // "c" and "d" are ignored because they are not referenced
 *
 * </pre></blockquote>
 *
 *
 *
 * <li> <i>Ordinary indexing</i> is used when the format specifier contains
 * <p>
 * neither an argument index nor a {@code '<'} flag.  Each format specifier
 * <p>
 * which uses ordinary indexing is assigned a sequential implicit index into
 * <p>
 * argument list which is independent of the indices used by explicit or
 * <p>
 * relative indexing.
 *
 *
 *
 * <blockquote><pre>
 *
 *   formatter.format("%s %s %s %s", "a", "b", "c", "d")
 *
 *   // -&gt; "a b c d"
 *
 * </pre></blockquote>
 *
 *
 *
 * </ul>
 *
 *
 *
 * <p> It is possible to have a format string which uses all forms of indexing,
 * <p>
 * for example:
 *
 *
 *
 * <blockquote><pre>
 *
 *   formatter.format("%2$s %s %&lt;s %s", "a", "b", "c", "d")
 *
 *   // -&gt; "b a a b"
 *
 *   // "c" and "d" are ignored because they are not referenced
 *
 * </pre></blockquote>
 *
 *
 *
 * <p> The maximum number of arguments is limited by the maximum dimension of a
 * <p>
 * Java array as defined by
 *
 * <cite>The Java&trade; Virtual Machine Specification</cite>.
 * <p>
 * If the argument index does not correspond to an
 * <p>
 * available argument, then a {@link MissingFormatArgumentException} is thrown.
 *
 *
 *
 * <p> If there are more arguments than format specifiers, the extra arguments
 * <p>
 * are ignored.
 *
 *
 *
 * <p> Unless otherwise specified, passing a {@code null} argument to any
 * <p>
 * method or constructor in this class will cause a {@link
 * <p>
 * NullPointerException} to be thrown.
 *
 * @author Iris Clark
 * @since 1.5
 */

public final class Formatter implements Closeable, Flushable {

    private static final int MAX_FD_CHARS = 30;
    /**
     * Writes a formatted string to this object's destination using the
     * <p>
     * specified locale, format string, and arguments.
     *
     * @param l      The {@linkplain Locale locale} to apply during
     * <p>
     * formatting.  If {@code l} is {@code null} then no localization
     * <p>
     * is applied.  This does not change this object's locale that was
     * <p>
     * set during construction.
     * @param format A format string as described in <a href="#syntax">Format string
     * <p>
     * syntax</a>
     * @param args   Arguments referenced by the format specifiers in the format
     * <p>
     * string.  If there are more arguments than format specifiers, the
     * <p>
     * extra arguments are ignored.  The maximum number of arguments is
     * <p>
     * limited by the maximum dimension of a Java array as defined by
     *
     * <cite>The Java&trade; Virtual Machine Specification</cite>.
     * @return This formatter
     * @throws IllegalFormatException   If a format string contains an illegal syntax, a format
     * <p>
     * specifier that is incompatible with the given arguments,
     * <p>
     * insufficient arguments given the format string, or other
     * <p>
     * illegal conditions.  For specification of all possible
     * <p>
     * formatting errors, see the <a href="#detail">Details</a>
     * <p>
     * section of the formatter class specification.
     * @throws FormatterClosedException If this formatter has been closed by invoking its {@link
     * <p>
     * #close()} method
     */

//    public Formatter format(Locale l, String format, Object... args) {
//
//        ensureOpen();
//
//
//        // index of last argument referenced
//
//        int last = -1;
//
//        // last ordinary index
//
//        int lasto = -1;
//
//
//        List<FormatString> fsa = parse(format);
//
//        for (FormatString fs : fsa) {
//
//            int index = fs.index();
//
//            try {
//
//                switch (index) {
//
//                    case -2:  // fixed string, "%n", or "%%"
//
//                        fs.print(null, l);
//
//                        break;
//
//                    case -1:  // relative index
//
//                        if (last < 0 || (args != null && last > args.length - 1)) {
//                            throw new MissingFormatArgumentException(fs.toString());
//                        }
//
//                        fs.print((args == null ? null : args[last]), l);
//
//                        break;
//
//                    case 0:  // ordinary index
//
//                        lasto++;
//
//                        last = lasto;
//
//                        if (args != null && lasto > args.length - 1) {
//                            throw new MissingFormatArgumentException(fs.toString());
//                        }
//
//                        fs.print((args == null ? null : args[lasto]), l);
//
//                        break;
//
//                    default:  // explicit index
//
//                        last = index - 1;
//
//                        if (args != null && last > args.length - 1) {
//                            throw new MissingFormatArgumentException(fs.toString());
//                        }
//
//                        fs.print((args == null ? null : args[last]), l);
//
//                        break;
//
//                }
//
//            } catch (IOException x) {
//
//                lastException = x;
//
//            }
//
//        }
//
//        return this;
//
//    }


    // %[argument_index$][flags][width][.precision][t]conversion

    private static final String formatSpecifier

            = "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])";
    private static double scaleUp;
    private static Pattern fsPattern = Pattern.compile(formatSpecifier);
    private final Locale l;


    // 1 (sign) + 19 (max # sig digits) + 1 ('.') + 1 ('e') + 1 (sign)

    // + 3 (max # exp digits) + 4 (error) = 30
    private final char zero;
    private Appendable a;
    private IOException lastException;


    /* Private constructors */

    private Formatter(Locale l, Appendable a) {

        this.a = a;

        this.l = l;

        this.zero = getZero(l);

    }


    private Formatter(Charset charset, Locale l, File file)

            throws FileNotFoundException {

        this(l,

                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset)));

    }


    /**
     * Constructs a new formatter.
     *
     *
     *
     * <p> The destination of the formatted output is a {@link StringBuilder}
     * <p>
     * which may be retrieved by invoking {@link #out out()} and whose
     * <p>
     * current content may be converted into a string by invoking {@link
     * <p>
     * #toString toString()}.  The locale used is the {@linkplain
     * <p>
     * Locale#getDefault(Locale.Category) default locale} for
     * <p>
     * {@linkplain Locale.Category#FORMAT formatting} for this instance of the Java
     * <p>
     * virtual machine.
     */

    public Formatter() {

        this(Locale.getDefault(Locale.Category.FORMAT), new StringBuilder());

    }


    /**
     * Constructs a new formatter with the specified destination.
     *
     *
     *
     * <p> The locale used is the {@linkplain
     * <p>
     * Locale#getDefault(Locale.Category) default locale} for
     * <p>
     * {@linkplain Locale.Category#FORMAT formatting} for this instance of the Java
     * <p>
     * virtual machine.
     *
     * @param a Destination for the formatted output.  If {@code a} is
     *          <p>
     *          {@code null} then a {@link StringBuilder} will be created.
     */

    public Formatter(Appendable a) {

        this(Locale.getDefault(Locale.Category.FORMAT), nonNullAppendable(a));

    }


    /**
     * Constructs a new formatter with the specified locale.
     *
     *
     *
     * <p> The destination of the formatted output is a {@link StringBuilder}
     * <p>
     * which may be retrieved by invoking {@link #out out()} and whose current
     * <p>
     * content may be converted into a string by invoking {@link #toString
     * <p>
     * toString()}.
     *
     * @param l The {@linkplain Locale locale} to apply during
     *          <p>
     *          formatting.  If {@code l} is {@code null} then no localization
     *          <p>
     *          is applied.
     */

    public Formatter(Locale l) {

        this(l, new StringBuilder());

    }


    /**
     * Constructs a new formatter with the specified destination and locale.
     *
     * @param a Destination for the formatted output.  If {@code a} is
     *          <p>
     *          {@code null} then a {@link StringBuilder} will be created.
     * @param l The {@linkplain Locale locale} to apply during
     *          <p>
     *          formatting.  If {@code l} is {@code null} then no localization
     *          <p>
     *          is applied.
     */

    public Formatter(Appendable a, Locale l) {

        this(l, nonNullAppendable(a));

    }


    /**
     * Constructs a new formatter with the specified file name.
     *
     *
     *
     * <p> The charset used is the {@linkplain
     * <p>
     * java.nio.charset.Charset#defaultCharset() default charset} for this
     * <p>
     * instance of the Java virtual machine.
     *
     *
     *
     * <p> The locale used is the {@linkplain
     * <p>
     * Locale#getDefault(Locale.Category) default locale} for
     * <p>
     * {@linkplain Locale.Category#FORMAT formatting} for this instance of the Java
     * <p>
     * virtual machine.
     *
     * @param fileName The name of the file to use as the destination of this
     *                 <p>
     *                 formatter.  If the file exists then it will be truncated to
     *                 <p>
     *                 zero size; otherwise, a new file will be created.  The output
     *                 <p>
     *                 will be written to the file and is buffered.
     * @throws SecurityException     If a security manager is present and {@link
     *                               <p>
     *                               SecurityManager#checkWrite checkWrite(fileName)} denies write
     *                               <p>
     *                               access to the file
     * @throws FileNotFoundException If the given file name does not denote an existing, writable
     *                               <p>
     *                               regular file and a new regular file of that name cannot be
     *                               <p>
     *                               created, or if some other error occurs while opening or
     *                               <p>
     *                               creating the file
     */

    public Formatter(String fileName) throws FileNotFoundException {

        this(Locale.getDefault(Locale.Category.FORMAT),

                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName))));

    }


    /**
     * Constructs a new formatter with the specified file name and charset.
     *
     *
     *
     * <p> The locale used is the {@linkplain
     * <p>
     * Locale#getDefault(Locale.Category) default locale} for
     * <p>
     * {@linkplain Locale.Category#FORMAT formatting} for this instance of the Java
     * <p>
     * virtual machine.
     *
     * @param fileName The name of the file to use as the destination of this
     *                 <p>
     *                 formatter.  If the file exists then it will be truncated to
     *                 <p>
     *                 zero size; otherwise, a new file will be created.  The output
     *                 <p>
     *                 will be written to the file and is buffered.
     * @param csn      The name of a supported {@linkplain Charset
     *                 <p>
     *                 charset}
     * @throws FileNotFoundException        If the given file name does not denote an existing, writable
     *                                      <p>
     *                                      regular file and a new regular file of that name cannot be
     *                                      <p>
     *                                      created, or if some other error occurs while opening or
     *                                      <p>
     *                                      creating the file
     * @throws SecurityException            If a security manager is present and {@link
     *                                      <p>
     *                                      SecurityManager#checkWrite checkWrite(fileName)} denies write
     *                                      <p>
     *                                      access to the file
     * @throws UnsupportedEncodingException If the named charset is not supported
     */

    public Formatter(String fileName, String csn)

            throws FileNotFoundException, UnsupportedEncodingException {

        this(fileName, csn, Locale.getDefault(Locale.Category.FORMAT));

    }


    /**
     * Constructs a new formatter with the specified file name, charset, and
     * <p>
     * locale.
     *
     * @param fileName The name of the file to use as the destination of this
     *                 <p>
     *                 formatter.  If the file exists then it will be truncated to
     *                 <p>
     *                 zero size; otherwise, a new file will be created.  The output
     *                 <p>
     *                 will be written to the file and is buffered.
     * @param csn      The name of a supported {@linkplain Charset
     *                 <p>
     *                 charset}
     * @param l        The {@linkplain Locale locale} to apply during
     *                 <p>
     *                 formatting.  If {@code l} is {@code null} then no localization
     *                 <p>
     *                 is applied.
     * @throws FileNotFoundException        If the given file name does not denote an existing, writable
     *                                      <p>
     *                                      regular file and a new regular file of that name cannot be
     *                                      <p>
     *                                      created, or if some other error occurs while opening or
     *                                      <p>
     *                                      creating the file
     * @throws SecurityException            If a security manager is present and {@link
     *                                      <p>
     *                                      SecurityManager#checkWrite checkWrite(fileName)} denies write
     *                                      <p>
     *                                      access to the file
     * @throws UnsupportedEncodingException If the named charset is not supported
     */

    public Formatter(String fileName, String csn, Locale l)

            throws FileNotFoundException, UnsupportedEncodingException {

        this(toCharset(csn), l, new File(fileName));

    }


    /**
     * Constructs a new formatter with the specified file.
     *
     *
     *
     * <p> The charset used is the {@linkplain
     * <p>
     * java.nio.charset.Charset#defaultCharset() default charset} for this
     * <p>
     * instance of the Java virtual machine.
     *
     *
     *
     * <p> The locale used is the {@linkplain
     * <p>
     * Locale#getDefault(Locale.Category) default locale} for
     * <p>
     * {@linkplain Locale.Category#FORMAT formatting} for this instance of the Java
     * <p>
     * virtual machine.
     *
     * @param file The file to use as the destination of this formatter.  If the
     *             <p>
     *             file exists then it will be truncated to zero size; otherwise,
     *             <p>
     *             a new file will be created.  The output will be written to the
     *             <p>
     *             file and is buffered.
     * @throws SecurityException     If a security manager is present and {@link
     *                               <p>
     *                               SecurityManager#checkWrite checkWrite(file.getPath())} denies
     *                               <p>
     *                               write access to the file
     * @throws FileNotFoundException If the given file object does not denote an existing, writable
     *                               <p>
     *                               regular file and a new regular file of that name cannot be
     *                               <p>
     *                               created, or if some other error occurs while opening or
     *                               <p>
     *                               creating the file
     */

    public Formatter(File file) throws FileNotFoundException {

        this(Locale.getDefault(Locale.Category.FORMAT),

                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))));

    }


    /**
     * Constructs a new formatter with the specified file and charset.
     *
     *
     *
     * <p> The locale used is the {@linkplain
     * <p>
     * Locale#getDefault(Locale.Category) default locale} for
     * <p>
     * {@linkplain Locale.Category#FORMAT formatting} for this instance of the Java
     * <p>
     * virtual machine.
     *
     * @param file The file to use as the destination of this formatter.  If the
     *             <p>
     *             file exists then it will be truncated to zero size; otherwise,
     *             <p>
     *             a new file will be created.  The output will be written to the
     *             <p>
     *             file and is buffered.
     * @param csn  The name of a supported {@linkplain Charset
     *             <p>
     *             charset}
     * @throws FileNotFoundException        If the given file object does not denote an existing, writable
     *                                      <p>
     *                                      regular file and a new regular file of that name cannot be
     *                                      <p>
     *                                      created, or if some other error occurs while opening or
     *                                      <p>
     *                                      creating the file
     * @throws SecurityException            If a security manager is present and {@link
     *                                      <p>
     *                                      SecurityManager#checkWrite checkWrite(file.getPath())} denies
     *                                      <p>
     *                                      write access to the file
     * @throws UnsupportedEncodingException If the named charset is not supported
     */

    public Formatter(File file, String csn)

            throws FileNotFoundException, UnsupportedEncodingException {

        this(file, csn, Locale.getDefault(Locale.Category.FORMAT));

    }


    /**
     * Constructs a new formatter with the specified file, charset, and
     * <p>
     * locale.
     *
     * @param file The file to use as the destination of this formatter.  If the
     *             <p>
     *             file exists then it will be truncated to zero size; otherwise,
     *             <p>
     *             a new file will be created.  The output will be written to the
     *             <p>
     *             file and is buffered.
     * @param csn  The name of a supported {@linkplain Charset
     *             <p>
     *             charset}
     * @param l    The {@linkplain Locale locale} to apply during
     *             <p>
     *             formatting.  If {@code l} is {@code null} then no localization
     *             <p>
     *             is applied.
     * @throws FileNotFoundException        If the given file object does not denote an existing, writable
     *                                      <p>
     *                                      regular file and a new regular file of that name cannot be
     *                                      <p>
     *                                      created, or if some other error occurs while opening or
     *                                      <p>
     *                                      creating the file
     * @throws SecurityException            If a security manager is present and {@link
     *                                      <p>
     *                                      SecurityManager#checkWrite checkWrite(file.getPath())} denies
     *                                      <p>
     *                                      write access to the file
     * @throws UnsupportedEncodingException If the named charset is not supported
     */

    public Formatter(File file, String csn, Locale l)

            throws FileNotFoundException, UnsupportedEncodingException {

        this(toCharset(csn), l, file);

    }


    /**
     * Constructs a new formatter with the specified print stream.
     *
     *
     *
     * <p> The locale used is the {@linkplain
     * <p>
     * Locale#getDefault(Locale.Category) default locale} for
     * <p>
     * {@linkplain Locale.Category#FORMAT formatting} for this instance of the Java
     * <p>
     * virtual machine.
     *
     *
     *
     * <p> Characters are written to the given {@link PrintStream
     * <p>
     * PrintStream} object and are therefore encoded using that object's
     * <p>
     * charset.
     *
     * @param ps The stream to use as the destination of this formatter.
     */

    public Formatter(PrintStream ps) {

        this(Locale.getDefault(Locale.Category.FORMAT),

                Objects.requireNonNull(ps));

    }


    /**
     * Constructs a new formatter with the specified output stream.
     *
     *
     *
     * <p> The charset used is the {@linkplain
     * <p>
     * java.nio.charset.Charset#defaultCharset() default charset} for this
     * <p>
     * instance of the Java virtual machine.
     *
     *
     *
     * <p> The locale used is the {@linkplain
     * <p>
     * Locale#getDefault(Locale.Category) default locale} for
     * <p>
     * {@linkplain Locale.Category#FORMAT formatting} for this instance of the Java
     * <p>
     * virtual machine.
     *
     * @param os The output stream to use as the destination of this formatter.
     *           <p>
     *           The output will be buffered.
     */

    public Formatter(OutputStream os) {

        this(Locale.getDefault(Locale.Category.FORMAT),

                new BufferedWriter(new OutputStreamWriter(os)));

    }


    /**
     * Constructs a new formatter with the specified output stream and
     * <p>
     * charset.
     *
     *
     *
     * <p> The locale used is the {@linkplain
     * <p>
     * Locale#getDefault(Locale.Category) default locale} for
     * <p>
     * {@linkplain Locale.Category#FORMAT formatting} for this instance of the Java
     * <p>
     * virtual machine.
     *
     * @param os  The output stream to use as the destination of this formatter.
     *            <p>
     *            The output will be buffered.
     * @param csn The name of a supported {@linkplain Charset
     *            <p>
     *            charset}
     * @throws UnsupportedEncodingException If the named charset is not supported
     */

    public Formatter(OutputStream os, String csn)

            throws UnsupportedEncodingException {

        this(os, csn, Locale.getDefault(Locale.Category.FORMAT));

    }


    /**
     * Constructs a new formatter with the specified output stream, charset,
     * <p>
     * and locale.
     *
     * @param os  The output stream to use as the destination of this formatter.
     *            <p>
     *            The output will be buffered.
     * @param csn The name of a supported {@linkplain Charset
     *            <p>
     *            charset}
     * @param l   The {@linkplain Locale locale} to apply during
     *            <p>
     *            formatting.  If {@code l} is {@code null} then no localization
     *            <p>
     *            is applied.
     * @throws UnsupportedEncodingException If the named charset is not supported
     */

    public Formatter(OutputStream os, String csn, Locale l)

            throws UnsupportedEncodingException {

        this(l, new BufferedWriter(new OutputStreamWriter(os, csn)));

    }

    /**
     * Returns a charset object for the given charset name.
     *
     * @throws NullPointerException         is csn is null
     * @throws UnsupportedEncodingException if the charset is not supported
     */

    private static Charset toCharset(String csn)

            throws UnsupportedEncodingException {

        Objects.requireNonNull(csn, "charsetName");

        try {

            return Charset.forName(csn);

        } catch (IllegalCharsetNameException | UnsupportedCharsetException unused) {

            // UnsupportedEncodingException should be thrown

            throw new UnsupportedEncodingException(csn);

        }

    }

    private static final Appendable nonNullAppendable(Appendable a) {

        if (a == null) {
            return new StringBuilder();
        }


        return a;

    }

    private static char getZero(Locale l) {

        if ((l != null) && !l.equals(Locale.US)) {

            DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(l);

            return dfs.getZeroDigit();

        } else {

            return '0';

        }

    }

    private static void checkText(String s, int start, int end) {

        for (int i = start; i < end; i++) {

            // Any '%' found in the region starts an invalid format specifier.

            if (s.charAt(i) == '%') {

                char c = (i == end - 1) ? '%' : s.charAt(i + 1);

                throw new UnknownFormatConversionException(String.valueOf(c));

            }

        }

    }

    /**
     * Returns the locale set by the construction of this formatter.
     *
     *
     *
     * <p> The {@link #format(Locale, String, Object...) format} method
     * <p>
     * for this object which has a locale argument does not change this value.
     *
     * @return {@code null} if no localization is applied, otherwise a
     * <p>
     * locale
     * @throws FormatterClosedException If this formatter has been closed by invoking its {@link
     *                                  <p>
     *                                  #close()} method
     */

    public Locale locale() {

        ensureOpen();

        return l;

    }

    /**
     * Returns the destination for the output.
     *
     * @return The destination for the output
     * @throws FormatterClosedException If this formatter has been closed by invoking its {@link
     *                                  <p>
     *                                  #close()} method
     */

    public Appendable out() {

        ensureOpen();

        return a;

    }

    /**
     * Returns the result of invoking {@code toString()} on the destination
     * <p>
     * for the output.  For example, the following code formats text into a
     * <p>
     * {@link StringBuilder} then retrieves the resultant string:
     *
     *
     *
     * <blockquote><pre>
     *
     *   Formatter f = new Formatter();
     *
     *   f.format("Last reboot at %tc", lastRebootDate);
     *
     *   String s = f.toString();
     *
     *   // -&gt; s == "Last reboot at Sat Jan 01 00:00:00 PST 2000"
     *
     * </pre></blockquote>
     *
     *
     *
     * <p> An invocation of this method behaves in exactly the same way as the
     * <p>
     * invocation
     *
     *
     *
     * <pre>
     *
     *     out().toString() </pre>
     *
     *
     *
     * <p> Depending on the specification of {@code toString} for the {@link
     * <p>
     * Appendable}, the returned string may or may not contain the characters
     * <p>
     * written to the destination.  For instance, buffers typically return
     * <p>
     * their contents in {@code toString()}, but streams cannot since the
     * <p>
     * data is discarded.
     *
     * @return The result of invoking {@code toString()} on the destination
     * <p>
     * for the output
     * @throws FormatterClosedException If this formatter has been closed by invoking its {@link
     *                                  <p>
     *                                  #close()} method
     */

    public String toString() {

        ensureOpen();

        return a.toString();

    }

    /**
     * Flushes this formatter.  If the destination implements the {@link
     * <p>
     * java.io.Flushable} interface, its {@code flush} method will be invoked.
     *
     *
     *
     * <p> Flushing a formatter writes any buffered output in the destination
     * <p>
     * to the underlying stream.
     *
     * @throws FormatterClosedException If this formatter has been closed by invoking its {@link
     *                                  <p>
     *                                  #close()} method
     */

    public void flush() {

        ensureOpen();

        if (a instanceof Flushable) {

            try {

                ((Flushable) a).flush();

            } catch (IOException ioe) {

                lastException = ioe;

            }

        }

    }


    /**
     * Writes a formatted string to this object's destination using the
     * <p>
     * specified format string and arguments.  The locale used is the one
     * <p>
     * defined during the construction of this formatter.
     *
     * @param format A format string as described in <a href="#syntax">Format string
     *               <p>
     *               syntax</a>.
     * @param args   Arguments referenced by the format specifiers in the format
     *               <p>
     *               string.  If there are more arguments than format specifiers, the
     *               <p>
     *               extra arguments are ignored.  The maximum number of arguments is
     *               <p>
     *               limited by the maximum dimension of a Java array as defined by
     *
     *               <cite>The Java&trade; Virtual Machine Specification</cite>.
     * @return This formatter
     * @throws IllegalFormatException   If a format string contains an illegal syntax, a format
     *                                  <p>
     *                                  specifier that is incompatible with the given arguments,
     *                                  <p>
     *                                  insufficient arguments given the format string, or other
     *                                  <p>
     *                                  illegal conditions.  For specification of all possible
     *                                  <p>
     *                                  formatting errors, see the <a href="#detail">Details</a>
     *                                  <p>
     *                                  section of the formatter class specification.
     * @throws FormatterClosedException If this formatter has been closed by invoking its {@link
     *                                  <p>
     *                                  #close()} method
     */

//    public Formatter format(String format, Object... args) {
//
//        return format(l, format, args);
//
//    }

    /**
     * Closes this formatter.  If the destination implements the {@link
     * <p>
     * java.io.Closeable} interface, its {@code close} method will be invoked.
     *
     *
     *
     * <p> Closing a formatter allows it to release resources it may be holding
     * <p>
     * (such as open files).  If the formatter is already closed, then invoking
     * <p>
     * this method has no effect.
     *
     *
     *
     * <p> Attempting to invoke any methods except {@link #ioException()} in
     * <p>
     * this formatter after it has been closed will result in a {@link
     * <p>
     * FormatterClosedException}.
     */

    public void close() {

        if (a == null) {
            return;
        }

        try {

            if (a instanceof Closeable) {
                ((Closeable) a).close();
            }

        } catch (IOException ioe) {

            lastException = ioe;

        } finally {

            a = null;

        }

    }

    private void ensureOpen() {

        if (a == null) {
            throw new FormatterClosedException();
        }

    }

    /**
     * Returns the {@code IOException} last thrown by this formatter's {@link
     * <p>
     * Appendable}.
     *
     *
     *
     * <p> If the destination's {@code append()} method never throws
     * <p>
     * {@code IOException}, then this method will always return {@code null}.
     *
     * @return The last exception thrown by the Appendable or {@code null} if
     * <p>
     * no such exception exists.
     */

    public IOException ioException() {

        return lastException;

    }

    /**
     * Finds format specifiers in the format string.
     */

    public List<FormatString> parse(String s) {

        ArrayList<FormatString> al = new ArrayList<>();

        Matcher m = fsPattern.matcher(s);

        for (int i = 0, len = s.length(); i < len; ) {

            if (m.find(i)) {

                // Anything between the start of the string and the beginning

                // of the format specifier is either fixed text or contains

                // an invalid format string.

                if (m.start() != i) {

                    // Make sure we didn't miss any invalid format specifiers

                    checkText(s, i, m.start());

                    // Assume previous characters were fixed text

                    al.add(new FixedString(s, i, m.start()));

                }


                al.add(new FormatSpecifier(s, m));

                i = m.end();

            } else {

                // No more valid format specifiers.  Check for possible invalid

                // format specifiers.

                checkText(s, i, len);

                // The rest of the string is fixed text

                al.add(new FixedString(s, i, s.length()));

                break;

            }

        }

        return al;

    }


    /**
     * Enum for {@code BigDecimal} formatting.
     */

    public enum BigDecimalLayoutForm {

        /**
         * Format the {@code BigDecimal} in computerized scientific notation.
         */

        SCIENTIFIC,


        /**
         * Format the {@code BigDecimal} as a decimal number.
         */

        DECIMAL_FLOAT

    }


    public interface FormatString {

        int index();

//        void print(Object arg, Locale l) throws IOException;

        String toString();
    }

    private static class Flags {

        static final Flags NONE = new Flags(0);      // ''
        static final Flags LEFT_JUSTIFY = new Flags(1 << 0);   // '-'


        // duplicate declarations from Formattable.java
        static final Flags UPPERCASE = new Flags(1 << 1);   // '^'
        static final Flags ALTERNATE = new Flags(1 << 2);   // '#'
        static final Flags PLUS = new Flags(1 << 3);   // '+'


        // numerics
        static final Flags LEADING_SPACE = new Flags(1 << 4);   // ' '
        static final Flags ZERO_PAD = new Flags(1 << 5);   // '0'
        static final Flags GROUP = new Flags(1 << 6);   // ','
        static final Flags PARENTHESES = new Flags(1 << 7);   // '('
        static final Flags PREVIOUS = new Flags(1 << 8);   // '<'


        // indexing
        private int flags;


        private Flags(int f) {

            flags = f;

        }

        public static Flags parse(String s, int start, int end) {

            Flags f = new Flags(0);

            for (int i = start; i < end; i++) {

                char c = s.charAt(i);

                Flags v = parse(c);

                if (f.contains(v)) {
                    throw new DuplicateFormatFlagsException(v.toString());
                }

                f.add(v);

            }

            return f;

        }

        private static Flags parse(char c) {

            switch (c) {

                case '-':
                    return LEFT_JUSTIFY;

                case '#':
                    return ALTERNATE;

                case '+':
                    return PLUS;

                case ' ':
                    return LEADING_SPACE;

                case '0':
                    return ZERO_PAD;

                case ',':
                    return GROUP;

                case '(':
                    return PARENTHESES;

                case '<':
                    return PREVIOUS;

                default:

                    throw new UnknownFormatFlagsException(String.valueOf(c));

            }

        }

        public static String toString(Flags f) {

            return f.toString();

        }

        public int valueOf() {

            return flags;

        }

        public boolean contains(Flags f) {

            return (flags & f.valueOf()) == f.valueOf();

        }

        public Flags dup() {

            return new Flags(flags);

        }


        // parse those flags which may be provided by users

        private Flags add(Flags f) {

            flags |= f.valueOf();

            return this;

        }


        // Returns a string representation of the current {@code Flags}.

        public Flags remove(Flags f) {

            flags &= ~f.valueOf();

            return this;

        }

        public String toString() {

            StringBuilder sb = new StringBuilder();

            if (contains(LEFT_JUSTIFY)) {
                sb.append('-');
            }

            if (contains(UPPERCASE)) {
                sb.append('^');
            }

            if (contains(ALTERNATE)) {
                sb.append('#');
            }

            if (contains(PLUS)) {
                sb.append('+');
            }

            if (contains(LEADING_SPACE)) {
                sb.append(' ');
            }

            if (contains(ZERO_PAD)) {
                sb.append('0');
            }

            if (contains(GROUP)) {
                sb.append(',');
            }

            if (contains(PARENTHESES)) {
                sb.append('(');
            }

            if (contains(PREVIOUS)) {
                sb.append('<');
            }

            return sb.toString();

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Flags flags1 = (Flags) o;
            return flags == flags1.flags;
        }

        @Override
        public int hashCode() {
            return Objects.hash(flags);
        }
    }

    private static class Conversion {

        // Byte, Short, Integer, Long, BigInteger

        // (and associated primitives due to autoboxing)

        static final char DECIMAL_INTEGER = 'd';

        static final char OCTAL_INTEGER = 'o';

        static final char HEXADECIMAL_INTEGER = 'x';

        static final char HEXADECIMAL_INTEGER_UPPER = 'X';


        // Float, Double, BigDecimal

        // (and associated primitives due to autoboxing)

        static final char SCIENTIFIC = 'e';

        static final char SCIENTIFIC_UPPER = 'E';

        static final char GENERAL = 'g';

        static final char GENERAL_UPPER = 'G';

        static final char DECIMAL_FLOAT = 'f';

        static final char HEXADECIMAL_FLOAT = 'a';

        static final char HEXADECIMAL_FLOAT_UPPER = 'A';


        // Character, Byte, Short, Integer

        // (and associated primitives due to autoboxing)

        static final char CHARACTER = 'c';

        static final char CHARACTER_UPPER = 'C';


        // java.util.Date, java.util.Calendar, long

        static final char DATE_TIME = 't';

        static final char DATE_TIME_UPPER = 'T';


        // if (arg.TYPE != boolean) return boolean

        // if (arg != null) return true; else return false;

        static final char BOOLEAN = 'b';

        static final char BOOLEAN_UPPER = 'B';

        // if (arg instanceof Formattable) arg.formatTo()

        // else arg.toString();

        static final char STRING = 's';

        static final char STRING_UPPER = 'S';

        // arg.hashCode()

        static final char HASHCODE = 'h';

        static final char HASHCODE_UPPER = 'H';


        static final char LINE_SEPARATOR = 'n';

        static final char PERCENT_SIGN = '%';


        static boolean isValid(char c) {

            return (isGeneral(c) || isInteger(c) || isFloat(c) || isText(c)

                    || c == 't' || isCharacter(c));

        }


        // Returns true iff the Conversion is applicable to all objects.

        static boolean isGeneral(char c) {

            switch (c) {

                case BOOLEAN:

                case BOOLEAN_UPPER:

                case STRING:

                case STRING_UPPER:

                case HASHCODE:

                case HASHCODE_UPPER:

                    return true;

                default:

                    return false;

            }

        }


        // Returns true iff the Conversion is applicable to character.

        static boolean isCharacter(char c) {

            switch (c) {

                case CHARACTER:

                case CHARACTER_UPPER:

                    return true;

                default:

                    return false;

            }

        }


        // Returns true iff the Conversion is an integer type.

        static boolean isInteger(char c) {

            switch (c) {

                case DECIMAL_INTEGER:

                case OCTAL_INTEGER:

                case HEXADECIMAL_INTEGER:

                case HEXADECIMAL_INTEGER_UPPER:

                    return true;

                default:

                    return false;

            }

        }


        // Returns true iff the Conversion is a floating-point type.

        static boolean isFloat(char c) {

            switch (c) {

                case SCIENTIFIC:

                case SCIENTIFIC_UPPER:

                case GENERAL:

                case GENERAL_UPPER:

                case DECIMAL_FLOAT:

                case HEXADECIMAL_FLOAT:

                case HEXADECIMAL_FLOAT_UPPER:

                    return true;

                default:

                    return false;

            }

        }


        // Returns true iff the Conversion does not require an argument

        static boolean isText(char c) {

            switch (c) {

                case LINE_SEPARATOR:

                case PERCENT_SIGN:

                    return true;

                default:

                    return false;

            }

        }

    }

    private static class DateTime {

        static final char HOUR_OF_DAY_0 = 'H'; // (00 - 23)

        static final char HOUR_0 = 'I'; // (01 - 12)

        static final char HOUR_OF_DAY = 'k'; // (0 - 23) -- like H

        static final char HOUR = 'l'; // (1 - 12) -- like I

        static final char MINUTE = 'M'; // (00 - 59)

        static final char NANOSECOND = 'N'; // (000000000 - 999999999)

        static final char MILLISECOND = 'L'; // jdk, not in gnu (000 - 999)

        static final char MILLISECOND_SINCE_EPOCH = 'Q'; // (0 - 99...?)

        static final char AM_PM = 'p'; // (am or pm)

        static final char SECONDS_SINCE_EPOCH = 's'; // (0 - 99...?)

        static final char SECOND = 'S'; // (00 - 60 - leap second)

        static final char TIME = 'T'; // (24 hour hh:mm:ss)

        static final char ZONE_NUMERIC = 'z'; // (-1200 - +1200) - ls minus?

        static final char ZONE = 'Z'; // (symbol)


        // Date

        static final char NAME_OF_DAY_ABBREV = 'a'; // 'a'

        static final char NAME_OF_DAY = 'A'; // 'A'

        static final char NAME_OF_MONTH_ABBREV = 'b'; // 'b'

        static final char NAME_OF_MONTH = 'B'; // 'B'

        static final char CENTURY = 'C'; // (00 - 99)

        static final char DAY_OF_MONTH_0 = 'd'; // (01 - 31)

        static final char DAY_OF_MONTH = 'e'; // (1 - 31) -- like d

// *    static final char ISO_WEEK_OF_YEAR_2    = 'g'; // cross %y %V

// *    static final char ISO_WEEK_OF_YEAR_4    = 'G'; // cross %Y %V

        static final char NAME_OF_MONTH_ABBREV_X = 'h'; // -- same b

        static final char DAY_OF_YEAR = 'j'; // (001 - 366)

        static final char MONTH = 'm'; // (01 - 12)

// *    static final char DAY_OF_WEEK_1         = 'u'; // (1 - 7) Monday

// *    static final char WEEK_OF_YEAR_SUNDAY   = 'U'; // (0 - 53) Sunday+

// *    static final char WEEK_OF_YEAR_MONDAY_01 = 'V'; // (01 - 53) Monday+

// *    static final char DAY_OF_WEEK_0         = 'w'; // (0 - 6) Sunday

// *    static final char WEEK_OF_YEAR_MONDAY   = 'W'; // (00 - 53) Monday

        static final char YEAR_2 = 'y'; // (00 - 99)

        static final char YEAR_4 = 'Y'; // (0000 - 9999)


        // Composites

        static final char TIME_12_HOUR = 'r'; // (hh:mm:ss [AP]M)

        static final char TIME_24_HOUR = 'R'; // (hh:mm same as %H:%M)

// *    static final char LOCALE_TIME   = 'X'; // (%H:%M:%S) - parse format?

        static final char DATE_TIME = 'c';

        // (Sat Nov 04 12:02:33 EST 1999)

        static final char DATE = 'D'; // (mm/dd/yy)

        static final char ISO_STANDARD_DATE = 'F'; // (%Y-%m-%d)

// *    static final char LOCALE_DATE           = 'x'; // (mm/dd/yy)


        static boolean isValid(char c) {

            switch (c) {

                case HOUR_OF_DAY_0:

                case HOUR_0:

                case HOUR_OF_DAY:

                case HOUR:

                case MINUTE:

                case NANOSECOND:

                case MILLISECOND:

                case MILLISECOND_SINCE_EPOCH:

                case AM_PM:

                case SECONDS_SINCE_EPOCH:

                case SECOND:

                case TIME:

                case ZONE_NUMERIC:

                case ZONE:


                    // Date

                case NAME_OF_DAY_ABBREV:

                case NAME_OF_DAY:

                case NAME_OF_MONTH_ABBREV:

                case NAME_OF_MONTH:

                case CENTURY:

                case DAY_OF_MONTH_0:

                case DAY_OF_MONTH:

// *        case ISO_WEEK_OF_YEAR_2:

// *        case ISO_WEEK_OF_YEAR_4:

                case NAME_OF_MONTH_ABBREV_X:

                case DAY_OF_YEAR:

                case MONTH:

// *        case DAY_OF_WEEK_1:

// *        case WEEK_OF_YEAR_SUNDAY:

// *        case WEEK_OF_YEAR_MONDAY_01:

// *        case DAY_OF_WEEK_0:

// *        case WEEK_OF_YEAR_MONDAY:

                case YEAR_2:

                case YEAR_4:


                    // Composites

                case TIME_12_HOUR:

                case TIME_24_HOUR:

// *        case LOCALE_TIME:

                case DATE_TIME:

                case DATE:

                case ISO_STANDARD_DATE:

// *        case LOCALE_DATE:

                    return true;

                default:

                    return false;

            }

        }

    }

    public class FixedString implements FormatString {

        private String s;

        private int start;

        private int end;

        FixedString(String s, int start, int end) {

            this.s = s;

            this.start = start;

            this.end = end;

        }

        public int index() {
            return -2;
        }

        public void print(Object arg, Locale l)

                throws IOException {
            a.append(s, start, end);
        }

        public String toString() {
            return s.substring(start, end);
        }
    }

    public class FormatSpecifier implements FormatString {

        private int index = -1;

        private Flags f = Flags.NONE;

        private int width;

        private int precision;

        private boolean dt = false;

        private char c;

        FormatSpecifier(String s, Matcher m) {

            index(s, m.start(1), m.end(1));

            flags(s, m.start(2), m.end(2));

            width(s, m.start(3), m.end(3));

            precision(s, m.start(4), m.end(4));


            int tTStart = m.start(5);

            if (tTStart >= 0) {

                dt = true;

                if (s.charAt(tTStart) == 'T') {

                    f.add(Flags.UPPERCASE);

                }

            }

            conversion(s.charAt(m.start(6)));


            if (dt) {
                checkDateTime();
            } else if (Conversion.isGeneral(c)) {
                checkGeneral();
            } else if (Conversion.isCharacter(c)) {
                checkCharacter();
            } else if (Conversion.isInteger(c)) {
                checkInteger();
            } else if (Conversion.isFloat(c)) {
                checkFloat();
            } else if (Conversion.isText(c)) {
                checkText();
            } else {
                throw new UnknownFormatConversionException(String.valueOf(c));
            }

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FormatSpecifier that = (FormatSpecifier) o;
            return index == that.index &&
                    width == that.width &&
                    precision == that.precision &&
                    dt == that.dt &&
                    c == that.c &&
                    Objects.equals(f, that.f);
        }

        @Override
        public int hashCode() {
            return Objects.hash(index, f, width, precision, dt, c);
        }

        private int index(String s, int start, int end) {

            if (start >= 0) {

                try {

                    // skip the trailing '$'

                    index = Integer.parseInt(s.substring(start, end - 1), 10);

                } catch (NumberFormatException x) {

                    assert (false);

                }

            } else {

                index = 0;

            }

            return index;

        }

        public int index() {

            return index;

        }

        private Flags flags(String s, int start, int end) {

            f = Flags.parse(s, start, end);

            if (f.contains(Flags.PREVIOUS)) {
                index = -1;
            }

            return f;

        }

        private int width(String s, int start, int end) {

            width = -1;

            if (start >= 0) {

                try {

                    width = Integer.parseInt(s.substring(start, end), 10);

                    if (width < 0) {
                        throw new IllegalFormatWidthException(width);
                    }

                } catch (NumberFormatException x) {

                    assert (false);

                }

            }

            return width;

        }

        private int precision(String s, int start, int end) {

            precision = -1;

            if (start >= 0) {

                try {

                    // skip the leading '.'

                    precision = Integer.parseInt(s.substring(start + 1, end), 10);

                    if (precision < 0) {
                        throw new IllegalFormatPrecisionException(precision);
                    }

                } catch (NumberFormatException x) {

                    assert (false);

                }

            }

            return precision;

        }

        private char conversion(char conv) {

            c = conv;

            if (!dt) {

                if (!Conversion.isValid(c)) {

                    throw new UnknownFormatConversionException(String.valueOf(c));

                }

                if (Character.isUpperCase(c)) {

                    f.add(Flags.UPPERCASE);

                    c = Character.toLowerCase(c);

                }

                if (Conversion.isText(c)) {

                    index = -2;

                }

            }

            return c;

        }


//        public void print(Object arg, Locale l) throws IOException {
//
//            if (dt) {
//
//                printDateTime(arg, l);
//
//                return;
//
//            }
//
//            switch (c) {
//
//                case Conversion.DECIMAL_INTEGER:
//
//                case Conversion.OCTAL_INTEGER:
//
//                case Conversion.HEXADECIMAL_INTEGER:
//
//                    printInteger(arg, l);
//
//                    break;
//
//                case Conversion.SCIENTIFIC:
//
//                case Conversion.GENERAL:
//
//                case Conversion.DECIMAL_FLOAT:
//
//                case Conversion.HEXADECIMAL_FLOAT:
//
//                    printFloat(arg, l);
//
//                    break;
//
//                case Conversion.CHARACTER:
//
//                case Conversion.CHARACTER_UPPER:
//
//                    printCharacter(arg);
//
//                    break;
//
//                case Conversion.BOOLEAN:
//
//                    printBoolean(arg);
//
//                    break;
//
//                case Conversion.STRING:
//
//                    printString(arg, l);
//
//                    break;
//
//                case Conversion.HASHCODE:
//
//                    printHashCode(arg);
//
//                    break;
//
//                case Conversion.LINE_SEPARATOR:
//
//                    a.append(System.lineSeparator());
//
//                    break;
//
//                case Conversion.PERCENT_SIGN:
//
//                    a.append('%');
//
//                    break;
//
//                default:
//
//                    assert false;
//
//            }
//
//        }

        private void printInteger(Object arg, Locale l) throws IOException {

            if (arg == null) {
                print("null");
            } else if (arg instanceof Byte) {
                print(((Byte) arg).byteValue(), l);
            } else if (arg instanceof Short) {
                print(((Short) arg).shortValue(), l);
            } else if (arg instanceof Integer) {
                print(((Integer) arg).intValue(), l);
            } else if (arg instanceof Long) {
                print(((Long) arg).longValue(), l);
            } else if (arg instanceof BigInteger) {
                print(((BigInteger) arg), l);
            } else {
                failConversion(c, arg);
            }

        }


        private void printFloat(Object arg, Locale l) throws IOException {

            if (arg == null) {
                print("null");
            } else if (arg instanceof Float) {
                print(((Float) arg).floatValue(), l);
            } else if (arg instanceof Double) {
                print(((Double) arg).doubleValue(), l);
            } else if (arg instanceof BigDecimal) {
                print(((BigDecimal) arg), l);
            } else {
                failConversion(c, arg);
            }

        }


        private void printDateTime(Object arg, Locale l) throws IOException {

            if (arg == null) {

                print("null");

                return;

            }

            Calendar cal = null;


            // Instead of Calendar.setLenient(true), perhaps we should

            // wrap the IllegalArgumentException that might be thrown?

            if (arg instanceof Long) {

                // Note that the following method uses an instance of the

                // default time zone (TimeZone.getDefaultRef().

                cal = Calendar.getInstance(l == null ? Locale.US : l);

                cal.setTimeInMillis((Long) arg);

            } else if (arg instanceof Date) {

                // Note that the following method uses an instance of the

                // default time zone (TimeZone.getDefaultRef().

                cal = Calendar.getInstance(l == null ? Locale.US : l);

                cal.setTime((Date) arg);

            } else if (arg instanceof Calendar) {

                cal = (Calendar) ((Calendar) arg).clone();

                cal.setLenient(true);

            } else if (arg instanceof TemporalAccessor) {

                print((TemporalAccessor) arg, c, l);

                return;

            } else {

                failConversion(c, arg);

            }

            // Use the provided locale so that invocations of

            // localizedMagnitude() use optimizations for null.

            print(cal, c, l);

        }


        private void printCharacter(Object arg) throws IOException {

            if (arg == null) {

                print("null");

                return;

            }

            String s = null;

            if (arg instanceof Character) {

                s = ((Character) arg).toString();

            } else if (arg instanceof Byte) {

                byte i = ((Byte) arg).byteValue();

                if (Character.isValidCodePoint(i)) {
                    s = new String(Character.toChars(i));
                } else {
                    throw new IllegalFormatCodePointException(i);
                }

            } else if (arg instanceof Short) {

                short i = ((Short) arg).shortValue();

                if (Character.isValidCodePoint(i)) {
                    s = new String(Character.toChars(i));
                } else {
                    throw new IllegalFormatCodePointException(i);
                }

            } else if (arg instanceof Integer) {

                int i = ((Integer) arg).intValue();

                if (Character.isValidCodePoint(i)) {
                    s = new String(Character.toChars(i));
                } else {
                    throw new IllegalFormatCodePointException(i);
                }

            } else {

                failConversion(c, arg);

            }

            print(s);

        }


//        private void printString(Object arg, Locale l) throws IOException {
//
//            if (arg instanceof Formattable) {
//
//                Formatter fmt = Formatter.this;
//
//                if (fmt.locale() != l) {
//                    fmt = new Formatter(fmt.out(), l);
//                }
//
//                ((Formattable) arg).formatTo(fmt, f.valueOf(), width, precision);
//
//            } else {
//
//                if (f.contains(Flags.ALTERNATE)) {
//                    failMismatch(Flags.ALTERNATE, 's');
//                }
//
//                if (arg == null) {
//                    print("null");
//                } else {
//                    print(arg.toString());
//                }
//
//            }
//
//        }


        private void printBoolean(Object arg) throws IOException {

            String s;

            if (arg != null) {
                s = ((arg instanceof Boolean)

                        ? ((Boolean) arg).toString()

                        : Boolean.toString(true));
            } else {
                s = Boolean.toString(false);
            }

            print(s);

        }


        private void printHashCode(Object arg) throws IOException {

            String s = (arg == null

                    ? "null"

                    : Integer.toHexString(arg.hashCode()));

            print(s);

        }


        private void print(String s) throws IOException {

            if (precision != -1 && precision < s.length()) {
                s = s.substring(0, precision);
            }

            if (f.contains(Flags.UPPERCASE)) {
                s = s.toUpperCase(Locale.getDefault(Locale.Category.FORMAT));
            }

            appendJustified(a, s);

        }


        private Appendable appendJustified(Appendable a, CharSequence cs) throws IOException {

            if (width == -1) {

                return a.append(cs);

            }

            boolean padRight = f.contains(Flags.LEFT_JUSTIFY);

            int sp = width - cs.length();

            if (padRight) {

                a.append(cs);

            }

            for (int i = 0; i < sp; i++) {

                a.append(' ');

            }

            if (!padRight) {

                a.append(cs);

            }

            return a;

        }


        public String toString() {

            StringBuilder sb = new StringBuilder("%");

            // Flags.UPPERCASE is set internally for legal conversions.

            Flags dupf = f.dup().remove(Flags.UPPERCASE);

            sb.append(dupf.toString());

            if (index > 0) {
                sb.append(index).append('$');
            }

            if (width != -1) {
                sb.append(width);
            }

            if (precision != -1) {
                sb.append('.').append(precision);
            }

            if (dt) {
                sb.append(f.contains(Flags.UPPERCASE) ? 'T' : 't');
            }

            sb.append(f.contains(Flags.UPPERCASE)

                    ? Character.toUpperCase(c) : c);

            return sb.toString();
        }


        private void checkGeneral() {

            if ((c == Conversion.BOOLEAN || c == Conversion.HASHCODE)

                    && f.contains(Flags.ALTERNATE)) {
                failMismatch(Flags.ALTERNATE, c);
            }

            // '-' requires a width

            if (width == -1 && f.contains(Flags.LEFT_JUSTIFY)) {
                throw new MissingFormatWidthException(toString());
            }

            checkBadFlags(Flags.PLUS, Flags.LEADING_SPACE, Flags.ZERO_PAD,

                    Flags.GROUP, Flags.PARENTHESES);

        }


        private void checkDateTime() {

            if (precision != -1) {
                throw new IllegalFormatPrecisionException(precision);
            }

            if (!DateTime.isValid(c)) {
                throw new UnknownFormatConversionException("t" + c);
            }

            checkBadFlags(Flags.ALTERNATE, Flags.PLUS, Flags.LEADING_SPACE,

                    Flags.ZERO_PAD, Flags.GROUP, Flags.PARENTHESES);

            // '-' requires a width

            if (width == -1 && f.contains(Flags.LEFT_JUSTIFY)) {
                throw new MissingFormatWidthException(toString());
            }

        }


        private void checkCharacter() {

            if (precision != -1) {
                throw new IllegalFormatPrecisionException(precision);
            }

            checkBadFlags(Flags.ALTERNATE, Flags.PLUS, Flags.LEADING_SPACE,

                    Flags.ZERO_PAD, Flags.GROUP, Flags.PARENTHESES);

            // '-' requires a width

            if (width == -1 && f.contains(Flags.LEFT_JUSTIFY)) {
                throw new MissingFormatWidthException(toString());
            }

        }


        private void checkInteger() {

            checkNumeric();

            if (precision != -1) {
                throw new IllegalFormatPrecisionException(precision);
            }


            if (c == Conversion.DECIMAL_INTEGER) {
                checkBadFlags(Flags.ALTERNATE);
            } else if (c == Conversion.OCTAL_INTEGER) {
                checkBadFlags(Flags.GROUP);
            } else {
                checkBadFlags(Flags.GROUP);
            }

        }


        private void checkBadFlags(Flags... badFlags) {

            for (Flags badFlag : badFlags) {
                if (f.contains(badFlag)) {
                    failMismatch(badFlag, c);
                }
            }

        }


        private void checkFloat() {

            checkNumeric();

            if (c == Conversion.DECIMAL_FLOAT) {

            } else if (c == Conversion.HEXADECIMAL_FLOAT) {

                checkBadFlags(Flags.PARENTHESES, Flags.GROUP);

            } else if (c == Conversion.SCIENTIFIC) {

                checkBadFlags(Flags.GROUP);

            } else if (c == Conversion.GENERAL) {

                checkBadFlags(Flags.ALTERNATE);

            }

        }


        private void checkNumeric() {

            if (width != -1 && width < 0) {
                throw new IllegalFormatWidthException(width);
            }


            if (precision != -1 && precision < 0) {
                throw new IllegalFormatPrecisionException(precision);
            }


            // '-' and '0' require a width

            if (width == -1

                    && (f.contains(Flags.LEFT_JUSTIFY) || f.contains(Flags.ZERO_PAD))) {
                throw new MissingFormatWidthException(toString());
            }


            // bad combination

            if ((f.contains(Flags.PLUS) && f.contains(Flags.LEADING_SPACE))

                    || (f.contains(Flags.LEFT_JUSTIFY) && f.contains(Flags.ZERO_PAD))) {
                throw new IllegalFormatFlagsException(f.toString());
            }

        }


        private void checkText() {

            if (precision != -1) {
                throw new IllegalFormatPrecisionException(precision);
            }

            switch (c) {

                case Conversion.PERCENT_SIGN:

                    if (f.valueOf() != Flags.LEFT_JUSTIFY.valueOf()

                            && f.valueOf() != Flags.NONE.valueOf()) {
                        throw new IllegalFormatFlagsException(f.toString());
                    }

                    // '-' requires a width

                    if (width == -1 && f.contains(Flags.LEFT_JUSTIFY)) {
                        throw new MissingFormatWidthException(toString());
                    }

                    break;

                case Conversion.LINE_SEPARATOR:

                    if (width != -1) {
                        throw new IllegalFormatWidthException(width);
                    }

                    if (f.valueOf() != Flags.NONE.valueOf()) {
                        throw new IllegalFormatFlagsException(f.toString());
                    }

                    break;

                default:

                    assert false;

            }

        }


        private void print(byte value, Locale l) throws IOException {

            long v = value;

            if (value < 0

                    && (c == Conversion.OCTAL_INTEGER

                    || c == Conversion.HEXADECIMAL_INTEGER)) {

                v += (1L << 8);

                assert v >= 0 : v;

            }

            print(v, l);

        }


        private void print(short value, Locale l) throws IOException {

            long v = value;

            if (value < 0

                    && (c == Conversion.OCTAL_INTEGER

                    || c == Conversion.HEXADECIMAL_INTEGER)) {

                v += (1L << 16);

                assert v >= 0 : v;

            }

            print(v, l);

        }


        private void print(int value, Locale l) throws IOException {

            long v = value;

            if (value < 0

                    && (c == Conversion.OCTAL_INTEGER

                    || c == Conversion.HEXADECIMAL_INTEGER)) {

                v += (1L << 32);

                assert v >= 0 : v;

            }

            print(v, l);

        }


        private void print(long value, Locale l) throws IOException {


            StringBuilder sb = new StringBuilder();


            if (c == Conversion.DECIMAL_INTEGER) {

                boolean neg = value < 0;

                String valueStr = Long.toString(value, 10);


                // leading sign indicator

                leadingSign(sb, neg);


                // the value

                localizedMagnitude(sb, valueStr, neg ? 1 : 0, f, adjustWidth(width, f, neg), l);


                // trailing sign indicator

                trailingSign(sb, neg);

            } else if (c == Conversion.OCTAL_INTEGER) {

                checkBadFlags(Flags.PARENTHESES, Flags.LEADING_SPACE,

                        Flags.PLUS);

                String s = Long.toOctalString(value);

                int len = (f.contains(Flags.ALTERNATE)

                        ? s.length() + 1

                        : s.length());


                // apply ALTERNATE (radix indicator for octal) before ZERO_PAD

                if (f.contains(Flags.ALTERNATE)) {
                    sb.append('0');
                }

                if (f.contains(Flags.ZERO_PAD)) {

                    trailingZeros(sb, width - len);

                }

                sb.append(s);

            } else if (c == Conversion.HEXADECIMAL_INTEGER) {

                checkBadFlags(Flags.PARENTHESES, Flags.LEADING_SPACE,

                        Flags.PLUS);

                String s = Long.toHexString(value);

                int len = (f.contains(Flags.ALTERNATE)

                        ? s.length() + 2

                        : s.length());


                // apply ALTERNATE (radix indicator for hex) before ZERO_PAD

                if (f.contains(Flags.ALTERNATE)) {
                    sb.append(f.contains(Flags.UPPERCASE) ? "0X" : "0x");
                }

                if (f.contains(Flags.ZERO_PAD)) {

                    trailingZeros(sb, width - len);

                }

                if (f.contains(Flags.UPPERCASE)) {
                    s = s.toUpperCase(Locale.getDefault(Locale.Category.FORMAT));
                }

                sb.append(s);

            }


            // justify based on width

            appendJustified(a, sb);

        }


        // neg := val < 0

        private StringBuilder leadingSign(StringBuilder sb, boolean neg) {

            if (!neg) {

                if (f.contains(Flags.PLUS)) {

                    sb.append('+');

                } else if (f.contains(Flags.LEADING_SPACE)) {

                    sb.append(' ');

                }

            } else {

                if (f.contains(Flags.PARENTHESES)) {
                    sb.append('(');
                } else {
                    sb.append('-');
                }

            }

            return sb;

        }


        // neg := val < 0

        private StringBuilder trailingSign(StringBuilder sb, boolean neg) {

            if (neg && f.contains(Flags.PARENTHESES)) {
                sb.append(')');
            }

            return sb;

        }


        private void print(BigInteger value, Locale l) throws IOException {

            StringBuilder sb = new StringBuilder();

            boolean neg = value.signum() == -1;

            BigInteger v = value.abs();


            // leading sign indicator

            leadingSign(sb, neg);


            // the value

            if (c == Conversion.DECIMAL_INTEGER) {

                localizedMagnitude(sb, v.toString(), 0, f, adjustWidth(width, f, neg), l);

            } else if (c == Conversion.OCTAL_INTEGER) {

                String s = v.toString(8);


                int len = s.length() + sb.length();

                if (neg && f.contains(Flags.PARENTHESES)) {
                    len++;
                }


                // apply ALTERNATE (radix indicator for octal) before ZERO_PAD

                if (f.contains(Flags.ALTERNATE)) {

                    len++;

                    sb.append('0');

                }

                if (f.contains(Flags.ZERO_PAD)) {

                    trailingZeros(sb, width - len);

                }

                sb.append(s);

            } else if (c == Conversion.HEXADECIMAL_INTEGER) {

                String s = v.toString(16);


                int len = s.length() + sb.length();

                if (neg && f.contains(Flags.PARENTHESES)) {
                    len++;
                }


                // apply ALTERNATE (radix indicator for hex) before ZERO_PAD

                if (f.contains(Flags.ALTERNATE)) {

                    len += 2;

                    sb.append(f.contains(Flags.UPPERCASE) ? "0X" : "0x");

                }

                if (f.contains(Flags.ZERO_PAD)) {

                    trailingZeros(sb, width - len);

                }

                if (f.contains(Flags.UPPERCASE)) {
                    s = s.toUpperCase(Locale.getDefault(Locale.Category.FORMAT));
                }

                sb.append(s);

            }


            // trailing sign indicator

            trailingSign(sb, (value.signum() == -1));


            // justify based on width

            appendJustified(a, sb);

        }


        private void print(float value, Locale l) throws IOException {

            print((double) value, l);

        }


        private void print(double value, Locale l) throws IOException {

            StringBuilder sb = new StringBuilder();

            boolean neg = Double.compare(value, 0.0) == -1;


            if (!Double.isNaN(value)) {

                double v = Math.abs(value);


                // leading sign indicator

                leadingSign(sb, neg);


                // the value

                if (!Double.isInfinite(v)) {
                    print(sb, v, l, f, c, precision, neg);
                } else {
                    sb.append(f.contains(Flags.UPPERCASE)

                            ? "INFINITY" : "Infinity");
                }


                // trailing sign indicator

                trailingSign(sb, neg);

            } else {

                sb.append(f.contains(Flags.UPPERCASE) ? "NAN" : "NaN");

            }


            // justify based on width

            appendJustified(a, sb);

        }


        // !Double.isInfinite(value) && !Double.isNaN(value)

        private void print(StringBuilder sb, double value, Locale l,

                           Flags f, char c, int precision, boolean neg)

                throws IOException {

            if (c == Conversion.SCIENTIFIC) {

                // Create a new FormattedFloatingDecimal with the desired

                // precision.

                int prec = (precision == -1 ? 6 : precision);


                FormattedFloatingDecimal fd

                        = FormattedFloatingDecimal.valueOf(value, prec,

                        FormattedFloatingDecimal.Form.SCIENTIFIC);


                StringBuilder mant = new StringBuilder().append(fd.getMantissa());

                addZeros(mant, prec);


                // If the precision is zero and the '#' flag is set, add the

                // requested decimal point.

                if (f.contains(Flags.ALTERNATE) && (prec == 0)) {

                    mant.append('.');

                }


                char[] exp = (value == 0.0)

                        ? new char[] {'+', '0', '0'} : fd.getExponent();


                int newW = width;

                if (width != -1) {

                    newW = adjustWidth(width - exp.length - 1, f, neg);

                }

                localizedMagnitude(sb, mant, 0, f, newW, l);


                sb.append(f.contains(Flags.UPPERCASE) ? 'E' : 'e');


                char sign = exp[0];

                assert (sign == '+' || sign == '-');

                sb.append(sign);


                localizedMagnitudeExp(sb, exp, 1, l);

            } else if (c == Conversion.DECIMAL_FLOAT) {

                // Create a new FormattedFloatingDecimal with the desired

                // precision.

                int prec = (precision == -1 ? 6 : precision);


                FormattedFloatingDecimal fd

                        = FormattedFloatingDecimal.valueOf(value, prec,

                        FormattedFloatingDecimal.Form.DECIMAL_FLOAT);


                StringBuilder mant = new StringBuilder().append(fd.getMantissa());

                addZeros(mant, prec);


                // If the precision is zero and the '#' flag is set, add the

                // requested decimal point.

                if (f.contains(Flags.ALTERNATE) && (prec == 0)) {
                    mant.append('.');
                }


                int newW = width;

                if (width != -1) {
                    newW = adjustWidth(width, f, neg);
                }

                localizedMagnitude(sb, mant, 0, f, newW, l);

            } else if (c == Conversion.GENERAL) {

                int prec = precision;

                if (precision == -1) {
                    prec = 6;
                } else if (precision == 0) {
                    prec = 1;
                }


                char[] exp;

                StringBuilder mant = new StringBuilder();

                int expRounded;

                if (value == 0.0) {

                    exp = null;

                    mant.append('0');

                    expRounded = 0;

                } else {

                    FormattedFloatingDecimal fd

                            = FormattedFloatingDecimal.valueOf(value, prec,

                            FormattedFloatingDecimal.Form.GENERAL);

                    exp = fd.getExponent();

                    mant.append(fd.getMantissa());

                    expRounded = fd.getExponentRounded();

                }


                if (exp != null) {

                    prec -= 1;

                } else {

                    prec -= expRounded + 1;

                }


                addZeros(mant, prec);

                // If the precision is zero and the '#' flag is set, add the

                // requested decimal point.

                if (f.contains(Flags.ALTERNATE) && (prec == 0)) {

                    mant.append('.');

                }


                int newW = width;

                if (width != -1) {

                    if (exp != null) {
                        newW = adjustWidth(width - exp.length - 1, f, neg);
                    } else {
                        newW = adjustWidth(width, f, neg);
                    }

                }

                localizedMagnitude(sb, mant, 0, f, newW, l);


                if (exp != null) {

                    sb.append(f.contains(Flags.UPPERCASE) ? 'E' : 'e');


                    char sign = exp[0];

                    assert (sign == '+' || sign == '-');

                    sb.append(sign);


                    localizedMagnitudeExp(sb, exp, 1, l);

                }

            } else if (c == Conversion.HEXADECIMAL_FLOAT) {

                int prec = precision;

                if (precision == -1)

                // assume that we want all of the digits

                {
                    prec = 0;
                } else if (precision == 0) {
                    prec = 1;
                }


                String s = hexDouble(value, prec);


                StringBuilder va = new StringBuilder();

                boolean upper = f.contains(Flags.UPPERCASE);

                sb.append(upper ? "0X" : "0x");


                if (f.contains(Flags.ZERO_PAD)) {

                    trailingZeros(sb, width - s.length() - 2);

                }


                int idx = s.indexOf('p');

                if (upper) {

                    String tmp = s.substring(0, idx);

                    // don't localize hex

                    tmp = tmp.toUpperCase(Locale.ROOT);

                    va.append(tmp);

                } else {

                    va.append(s, 0, idx);

                }

                if (prec != 0) {

                    addZeros(va, prec);

                }

                sb.append(va);

                sb.append(upper ? 'P' : 'p');

                sb.append(s, idx + 1, s.length());

            }

        }


        // Add zeros to the requested precision.

        private void addZeros(StringBuilder sb, int prec) {

            // Look for the dot.  If we don't find one, the we'll need to add

            // it before we add the zeros.

            int len = sb.length();

            int i;

            for (i = 0; i < len; i++) {

                if (sb.charAt(i) == '.') {

                    break;

                }

            }

            boolean needDot = false;

            if (i == len) {

                needDot = true;

            }


            // Determine existing precision.

            int outPrec = len - i - (needDot ? 0 : 1);

            assert (outPrec <= prec);

            if (outPrec == prec) {

                return;

            }


            // Add dot if previously determined to be necessary.

            if (needDot) {

                sb.append('.');

            }


            // Add zeros.

            trailingZeros(sb, prec - outPrec);

        }


        // Method assumes that d > 0.

        private String hexDouble(double d, int prec) {

            // Let Double.toHexString handle simple cases

            if (!Double.isFinite(d) || d == 0.0 || prec == 0 || prec >= 13) {

                // remove "0x"

                return Double.toHexString(d).substring(2);

            } else {

                assert (prec >= 1 && prec <= 12);


                int exponent = Math.getExponent(d);

                boolean subnormal

                        = (exponent == Double.MIN_EXPONENT - 1);


                // If this is subnormal input so normalize (could be faster to

                // do as integer operation).

                if (subnormal) {

                    scaleUp = Math.scalb(1.0, 54);

                    d *= scaleUp;

                    // Calculate the exponent.  This is not just exponent + 54

                    // since the former is not the normalized exponent.

                    exponent = Math.getExponent(d);

                    assert exponent >= Double.MIN_EXPONENT &&

                            exponent <= Double.MAX_EXPONENT : exponent;

                }


                int precision = 1 + prec * 4;

                int shiftDistance

                        = DoubleConsts.SIGNIFICAND_WIDTH - precision;

                assert (shiftDistance >= 1 && shiftDistance < DoubleConsts.SIGNIFICAND_WIDTH);


                long doppel = Double.doubleToLongBits(d);

                // Deterime the number of bits to keep.

                long newSignif

                        = (doppel & (DoubleConsts.EXP_BIT_MASK

                        | DoubleConsts.SIGNIF_BIT_MASK))

                        >> shiftDistance;

                // Bits to round away.

                long roundingBits = doppel & ~(~0L << shiftDistance);


                // To decide how to round, look at the low-order bit of the

                // working significand, the highest order discarded bit (the

                // round bit) and whether any of the lower order discarded bits

                // are nonzero (the sticky bit).


                boolean leastZero = (newSignif & 0x1L) == 0L;

                boolean round

                        = ((1L << (shiftDistance - 1)) & roundingBits) != 0L;

                boolean sticky = shiftDistance > 1 &&

                        (~(1L << (shiftDistance - 1)) & roundingBits) != 0;

                if ((leastZero && round && sticky) || (!leastZero && round)) {

                    newSignif++;

                }


                long signBit = doppel & DoubleConsts.SIGN_BIT_MASK;

                newSignif = signBit | (newSignif << shiftDistance);

                double result = Double.longBitsToDouble(newSignif);


                if (Double.isInfinite(result)) {

                    // Infinite result generated by rounding

                    return "1.0p1024";

                } else {

                    String res = Double.toHexString(result).substring(2);

                    if (!subnormal) {
                        return res;
                    } else {

                        // Create a normalized subnormal string.

                        int idx = res.indexOf('p');

                        if (idx == -1) {

                            // No 'p' character in hex string.

                            assert false;

                            return null;

                        } else {

                            // Get exponent and append at the end.

                            String exp = res.substring(idx + 1);

                            int iexp = Integer.parseInt(exp) - 54;

                            return res.substring(0, idx) + "p"

                                    + iexp;

                        }

                    }

                }

            }

        }


        private void print(BigDecimal value, Locale l) throws IOException {

            if (c == Conversion.HEXADECIMAL_FLOAT) {
                failConversion(c, value);
            }

            StringBuilder sb = new StringBuilder();

            boolean neg = value.signum() == -1;

            BigDecimal v = value.abs();

            // leading sign indicator

            leadingSign(sb, neg);


            // the value

            print(sb, v, l, f, c, precision, neg);


            // trailing sign indicator

            trailingSign(sb, neg);


            // justify based on width

            appendJustified(a, sb);

        }


        // value > 0

        private void print(StringBuilder sb, BigDecimal value, Locale l,

                           Flags f, char c, int precision, boolean neg)

                throws IOException {

            if (c == Conversion.SCIENTIFIC) {

                // Create a new BigDecimal with the desired precision.

                int prec = (precision == -1 ? 6 : precision);

                int scale = value.scale();

                int origPrec = value.precision();

                int nzeros = 0;

                int compPrec;


                if (prec > origPrec - 1) {

                    compPrec = origPrec;

                    nzeros = prec - (origPrec - 1);

                } else {

                    compPrec = prec + 1;

                }


                MathContext mc = new MathContext(compPrec);

                BigDecimal v

                        = new BigDecimal(value.unscaledValue(), scale, mc);


                BigDecimalLayout bdl

                        = new BigDecimalLayout(v.unscaledValue(), v.scale(),

                        BigDecimalLayoutForm.SCIENTIFIC);


                StringBuilder mant = bdl.mantissa();


                // Add a decimal point if necessary.  The mantissa may not

                // contain a decimal point if the scale is zero (the internal

                // representation has no fractional part) or the original

                // precision is one. Append a decimal point if '#' is set or if

                // we require zero padding to get to the requested precision.

                if ((origPrec == 1 || !bdl.hasDot())

                        && (nzeros > 0 || (f.contains(Flags.ALTERNATE)))) {

                    mant.append('.');

                }


                // Add trailing zeros in the case precision is greater than

                // the number of available digits after the decimal separator.

                trailingZeros(mant, nzeros);


                StringBuilder exp = bdl.exponent();

                int newW = width;

                if (width != -1) {

                    newW = adjustWidth(width - exp.length() - 1, f, neg);

                }

                localizedMagnitude(sb, mant, 0, f, newW, l);


                sb.append(f.contains(Flags.UPPERCASE) ? 'E' : 'e');


                Flags flags = f.dup().remove(Flags.GROUP);

                char sign = exp.charAt(0);

                assert (sign == '+' || sign == '-');

                sb.append(sign);


                sb.append(localizedMagnitude(null, exp, 1, flags, -1, l));

            } else if (c == Conversion.DECIMAL_FLOAT) {

                // Create a new BigDecimal with the desired precision.

                int prec = (precision == -1 ? 6 : precision);

                int scale = value.scale();


                if (scale > prec) {

                    // more "scale" digits than the requested "precision"

                    int compPrec = value.precision();

                    if (compPrec <= scale) {

                        // case of 0.xxxxxx

                        value = value.setScale(prec, RoundingMode.HALF_UP);

                    } else {

                        compPrec -= (scale - prec);

                        value = new BigDecimal(value.unscaledValue(),

                                scale,

                                new MathContext(compPrec));

                    }

                }

                BigDecimalLayout bdl = new BigDecimalLayout(

                        value.unscaledValue(),

                        value.scale(),

                        BigDecimalLayoutForm.DECIMAL_FLOAT);


                StringBuilder mant = bdl.mantissa();

                int nzeros = (bdl.scale() < prec ? prec - bdl.scale() : 0);


                // Add a decimal point if necessary.  The mantissa may not

                // contain a decimal point if the scale is zero (the internal

                // representation has no fractional part).  Append a decimal

                // point if '#' is set or we require zero padding to get to the

                // requested precision.

                if (bdl.scale() == 0 && (f.contains(Flags.ALTERNATE)

                        || nzeros > 0)) {

                    mant.append('.');

                }


                // Add trailing zeros if the precision is greater than the

                // number of available digits after the decimal separator.

                trailingZeros(mant, nzeros);


                localizedMagnitude(sb, mant, 0, f, adjustWidth(width, f, neg), l);

            } else if (c == Conversion.GENERAL) {

                int prec = precision;

                if (precision == -1) {
                    prec = 6;
                } else if (precision == 0) {
                    prec = 1;
                }


                BigDecimal tenToTheNegFour = BigDecimal.valueOf(1, 4);

                BigDecimal tenToThePrec = BigDecimal.valueOf(1, -prec);

                if ((value.equals(BigDecimal.ZERO))

                        || ((value.compareTo(tenToTheNegFour) != -1)

                        && (value.compareTo(tenToThePrec) == -1))) {


                    int e = -value.scale()

                            + (value.unscaledValue().toString().length() - 1);


                    // xxx.yyy

                    //   g precision (# sig digits) = #x + #y

                    //   f precision = #y

                    //   exponent = #x - 1

                    // => f precision = g precision - exponent - 1

                    // 0.000zzz

                    //   g precision (# sig digits) = #z

                    //   f precision = #0 (after '.') + #z

                    //   exponent = - #0 (after '.') - 1

                    // => f precision = g precision - exponent - 1

                    prec = prec - e - 1;


                    print(sb, value, l, f, Conversion.DECIMAL_FLOAT, prec,

                            neg);

                } else {

                    print(sb, value, l, f, Conversion.SCIENTIFIC, prec - 1, neg);

                }

            } else {
                assert c != Conversion.HEXADECIMAL_FLOAT;
            }

        }

        private int adjustWidth(int width, Flags f, boolean neg) {

            int newW = width;

            if (newW != -1 && neg && f.contains(Flags.PARENTHESES)) {
                newW--;
            }

            return newW;

        }

        private void trailingZeros(StringBuilder sb, int nzeros) {

            for (int i = 0; i < nzeros; i++) {

                sb.append('0');

            }

        }


        // Add trailing zeros

        private void print(Calendar t, char c, Locale l) throws IOException {

            StringBuilder sb = new StringBuilder();

            print(sb, t, c, l);


            // justify based on width

            if (f.contains(Flags.UPPERCASE)) {

                appendJustified(a, sb.toString().toUpperCase(Locale.getDefault(Locale.Category.FORMAT)));

            } else {

                appendJustified(a, sb);

            }

        }

        private Appendable print(StringBuilder sb, Calendar t, char c, Locale l)

                throws IOException {

            if (sb == null) {
                sb = new StringBuilder();
            }

            switch (c) {

                case DateTime.HOUR_OF_DAY_0: // 'H' (00 - 23)

                case DateTime.HOUR_0:        // 'I' (01 - 12)

                case DateTime.HOUR_OF_DAY:   // 'k' (0 - 23) -- like H

                case DateTime.HOUR: { // 'l' (1 - 12) -- like I

                    int i = t.get(Calendar.HOUR_OF_DAY);

                    if (c == DateTime.HOUR_0 || c == DateTime.HOUR) {
                        i = (i == 0 || i == 12 ? 12 : i % 12);
                    }

                    Flags flags = (c == DateTime.HOUR_OF_DAY_0

                            || c == DateTime.HOUR_0

                            ? Flags.ZERO_PAD

                            : Flags.NONE);

                    sb.append(localizedMagnitude(null, i, flags, 2, l));

                    break;

                }

                case DateTime.MINUTE: { // 'M' (00 - 59)

                    int i = t.get(Calendar.MINUTE);

                    Flags flags = Flags.ZERO_PAD;

                    sb.append(localizedMagnitude(null, i, flags, 2, l));

                    break;

                }

                case DateTime.NANOSECOND: { // 'N' (000000000 - 999999999)

                    int i = t.get(Calendar.MILLISECOND) * 1000000;

                    Flags flags = Flags.ZERO_PAD;

                    sb.append(localizedMagnitude(null, i, flags, 9, l));

                    break;

                }

                case DateTime.MILLISECOND: { // 'L' (000 - 999)

                    int i = t.get(Calendar.MILLISECOND);

                    Flags flags = Flags.ZERO_PAD;

                    sb.append(localizedMagnitude(null, i, flags, 3, l));

                    break;

                }

                case DateTime.MILLISECOND_SINCE_EPOCH: { // 'Q' (0 - 99...?)

                    long i = t.getTimeInMillis();

                    Flags flags = Flags.NONE;

                    sb.append(localizedMagnitude(null, i, flags, width, l));

                    break;

                }

                case DateTime.AM_PM: { // 'p' (am or pm)

                    // Calendar.AM = 0, Calendar.PM = 1, LocaleElements defines upper

                    String[] ampm = {"AM", "PM"};

                    if (l != null && l != Locale.US) {

                        DateFormatSymbols dfs = DateFormatSymbols.getInstance(l);

                        ampm = dfs.getAmPmStrings();

                    }

                    String s = ampm[t.get(Calendar.AM_PM)];

                    sb.append(s.toLowerCase(Objects.requireNonNullElse(l,

                            Locale.getDefault(Locale.Category.FORMAT))));

                    break;

                }

                case DateTime.SECONDS_SINCE_EPOCH: { // 's' (0 - 99...?)

                    long i = t.getTimeInMillis() / 1000;

                    Flags flags = Flags.NONE;

                    sb.append(localizedMagnitude(null, i, flags, width, l));

                    break;

                }

                case DateTime.SECOND: { // 'S' (00 - 60 - leap second)

                    int i = t.get(Calendar.SECOND);

                    Flags flags = Flags.ZERO_PAD;

                    sb.append(localizedMagnitude(null, i, flags, 2, l));

                    break;

                }

                case DateTime.ZONE_NUMERIC: { // 'z' ({-|+}####) - ls minus?

                    int i = t.get(Calendar.ZONE_OFFSET) + t.get(Calendar.DST_OFFSET);

                    boolean neg = i < 0;

                    sb.append(neg ? '-' : '+');

                    if (neg) {
                        i = -i;
                    }

                    int min = i / 60000;

                    // combine minute and hour into a single integer

                    int offset = (min / 60) * 100 + (min % 60);

                    Flags flags = Flags.ZERO_PAD;


                    sb.append(localizedMagnitude(null, offset, flags, 4, l));

                    break;

                }

                case DateTime.ZONE: { // 'Z' (symbol)

                    TimeZone tz = t.getTimeZone();

                    sb.append(tz.getDisplayName((t.get(Calendar.DST_OFFSET) != 0),

                            TimeZone.SHORT,

                            Objects.requireNonNullElse(l, Locale.US)));

                    break;

                }


                // Date

                case DateTime.NAME_OF_DAY_ABBREV:     // 'a'

                case DateTime.NAME_OF_DAY: { // 'A'

                    int i = t.get(Calendar.DAY_OF_WEEK);

                    Locale lt = Objects.requireNonNullElse(l, Locale.US);

                    DateFormatSymbols dfs = DateFormatSymbols.getInstance(lt);

                    if (c == DateTime.NAME_OF_DAY) {
                        sb.append(dfs.getWeekdays()[i]);
                    } else {
                        sb.append(dfs.getShortWeekdays()[i]);
                    }

                    break;

                }

                case DateTime.NAME_OF_MONTH_ABBREV:   // 'b'

                case DateTime.NAME_OF_MONTH_ABBREV_X: // 'h' -- same b

                case DateTime.NAME_OF_MONTH: { // 'B'

                    int i = t.get(Calendar.MONTH);

                    Locale lt = Objects.requireNonNullElse(l, Locale.US);

                    DateFormatSymbols dfs = DateFormatSymbols.getInstance(lt);

                    if (c == DateTime.NAME_OF_MONTH) {
                        sb.append(dfs.getMonths()[i]);
                    } else {
                        sb.append(dfs.getShortMonths()[i]);
                    }

                    break;

                }

                case DateTime.CENTURY:                // 'C' (00 - 99)

                case DateTime.YEAR_2:                 // 'y' (00 - 99)

                case DateTime.YEAR_4: { // 'Y' (0000 - 9999)

                    int i = t.get(Calendar.YEAR);

                    int size = 2;

                    switch (c) {

                        case DateTime.CENTURY:

                            i /= 100;

                            break;

                        case DateTime.YEAR_2:

                            i %= 100;

                            break;

                        case DateTime.YEAR_4:

                            size = 4;

                            break;

                    }

                    Flags flags = Flags.ZERO_PAD;

                    sb.append(localizedMagnitude(null, i, flags, size, l));

                    break;

                }

                case DateTime.DAY_OF_MONTH_0:         // 'd' (01 - 31)

                case DateTime.DAY_OF_MONTH: { // 'e' (1 - 31) -- like d

                    int i = t.get(Calendar.DATE);

                    Flags flags = (c == DateTime.DAY_OF_MONTH_0

                            ? Flags.ZERO_PAD

                            : Flags.NONE);

                    sb.append(localizedMagnitude(null, i, flags, 2, l));

                    break;

                }

                case DateTime.DAY_OF_YEAR: { // 'j' (001 - 366)

                    int i = t.get(Calendar.DAY_OF_YEAR);

                    Flags flags = Flags.ZERO_PAD;

                    sb.append(localizedMagnitude(null, i, flags, 3, l));

                    break;

                }

                case DateTime.MONTH: { // 'm' (01 - 12)

                    int i = t.get(Calendar.MONTH) + 1;

                    Flags flags = Flags.ZERO_PAD;

                    sb.append(localizedMagnitude(null, i, flags, 2, l));

                    break;

                }


                // Composites

                case DateTime.TIME:         // 'T' (24 hour hh:mm:ss - %tH:%tM:%tS)

                case DateTime.TIME_24_HOUR: { // 'R' (hh:mm same as %H:%M)

                    char sep = ':';

                    print(sb, t, DateTime.HOUR_OF_DAY_0, l).append(sep);

                    print(sb, t, DateTime.MINUTE, l);

                    if (c == DateTime.TIME) {

                        sb.append(sep);

                        print(sb, t, DateTime.SECOND, l);

                    }

                    break;

                }

                case DateTime.TIME_12_HOUR: { // 'r' (hh:mm:ss [AP]M)

                    char sep = ':';

                    print(sb, t, DateTime.HOUR_0, l).append(sep);

                    print(sb, t, DateTime.MINUTE, l).append(sep);

                    print(sb, t, DateTime.SECOND, l).append(' ');

                    // this may be in wrong place for some locales

                    StringBuilder tsb = new StringBuilder();

                    print(tsb, t, DateTime.AM_PM, l);


                    sb.append(tsb.toString().toUpperCase(Objects.requireNonNullElse(l,

                            Locale.getDefault(Locale.Category.FORMAT))));

                    break;

                }

                case DateTime.DATE_TIME: { // 'c' (Sat Nov 04 12:02:33 EST 1999)

                    char sep = ' ';

                    print(sb, t, DateTime.NAME_OF_DAY_ABBREV, l).append(sep);

                    print(sb, t, DateTime.NAME_OF_MONTH_ABBREV, l).append(sep);

                    print(sb, t, DateTime.DAY_OF_MONTH_0, l).append(sep);

                    print(sb, t, DateTime.TIME, l).append(sep);

                    print(sb, t, DateTime.ZONE, l).append(sep);

                    print(sb, t, DateTime.YEAR_4, l);

                    break;

                }

                case DateTime.DATE: { // 'D' (mm/dd/yy)

                    char sep = '/';

                    print(sb, t, DateTime.MONTH, l).append(sep);

                    print(sb, t, DateTime.DAY_OF_MONTH_0, l).append(sep);

                    print(sb, t, DateTime.YEAR_2, l);

                    break;

                }

                case DateTime.ISO_STANDARD_DATE: { // 'F' (%Y-%m-%d)

                    char sep = '-';

                    print(sb, t, DateTime.YEAR_4, l).append(sep);

                    print(sb, t, DateTime.MONTH, l).append(sep);

                    print(sb, t, DateTime.DAY_OF_MONTH_0, l);

                    break;

                }

                default:

                    assert false;

            }

            return sb;

        }

        private void print(TemporalAccessor t, char c, Locale l) throws IOException {

            StringBuilder sb = new StringBuilder();

            print(sb, t, c, l);

            // justify based on width

            if (f.contains(Flags.UPPERCASE)) {

                appendJustified(a, sb.toString().toUpperCase(Locale.getDefault(Locale.Category.FORMAT)));

            } else {

                appendJustified(a, sb);

            }

        }

        private Appendable print(StringBuilder sb, TemporalAccessor t, char c,

                                 Locale l) throws IOException {

            if (sb == null) {
                sb = new StringBuilder();
            }

            try {

                switch (c) {

                    case DateTime.HOUR_OF_DAY_0: {  // 'H' (00 - 23)

                        int i = t.get(ChronoField.HOUR_OF_DAY);

                        sb.append(localizedMagnitude(null, i, Flags.ZERO_PAD, 2, l));

                        break;

                    }

                    case DateTime.HOUR_OF_DAY: {   // 'k' (0 - 23) -- like H

                        int i = t.get(ChronoField.HOUR_OF_DAY);

                        sb.append(localizedMagnitude(null, i, Flags.NONE, 2, l));

                        break;

                    }

                    case DateTime.HOUR_0: {  // 'I' (01 - 12)

                        int i = t.get(ChronoField.CLOCK_HOUR_OF_AMPM);

                        sb.append(localizedMagnitude(null, i, Flags.ZERO_PAD, 2, l));

                        break;

                    }

                    case DateTime.HOUR: { // 'l' (1 - 12) -- like I

                        int i = t.get(ChronoField.CLOCK_HOUR_OF_AMPM);

                        sb.append(localizedMagnitude(null, i, Flags.NONE, 2, l));

                        break;

                    }

                    case DateTime.MINUTE: { // 'M' (00 - 59)

                        int i = t.get(ChronoField.MINUTE_OF_HOUR);

                        Flags flags = Flags.ZERO_PAD;

                        sb.append(localizedMagnitude(null, i, flags, 2, l));

                        break;

                    }

                    case DateTime.NANOSECOND: { // 'N' (000000000 - 999999999)

                        int i;

                        try {

                            i = t.get(ChronoField.NANO_OF_SECOND);

                        } catch (UnsupportedTemporalTypeException u) {

                            i = t.get(ChronoField.MILLI_OF_SECOND) * 1000000;

                        }

                        Flags flags = Flags.ZERO_PAD;

                        sb.append(localizedMagnitude(null, i, flags, 9, l));

                        break;

                    }

                    case DateTime.MILLISECOND: { // 'L' (000 - 999)

                        int i = t.get(ChronoField.MILLI_OF_SECOND);

                        Flags flags = Flags.ZERO_PAD;

                        sb.append(localizedMagnitude(null, i, flags, 3, l));

                        break;

                    }

                    case DateTime.MILLISECOND_SINCE_EPOCH: { // 'Q' (0 - 99...?)

                        long i = t.getLong(ChronoField.INSTANT_SECONDS) * 1000L +

                                t.getLong(ChronoField.MILLI_OF_SECOND);

                        Flags flags = Flags.NONE;

                        sb.append(localizedMagnitude(null, i, flags, width, l));

                        break;

                    }

                    case DateTime.AM_PM: { // 'p' (am or pm)

                        // Calendar.AM = 0, Calendar.PM = 1, LocaleElements defines upper

                        String[] ampm = {"AM", "PM"};

                        if (l != null && l != Locale.US) {

                            DateFormatSymbols dfs = DateFormatSymbols.getInstance(l);

                            ampm = dfs.getAmPmStrings();

                        }

                        String s = ampm[t.get(ChronoField.AMPM_OF_DAY)];

                        sb.append(s.toLowerCase(Objects.requireNonNullElse(l,

                                Locale.getDefault(Locale.Category.FORMAT))));

                        break;

                    }

                    case DateTime.SECONDS_SINCE_EPOCH: { // 's' (0 - 99...?)

                        long i = t.getLong(ChronoField.INSTANT_SECONDS);

                        Flags flags = Flags.NONE;

                        sb.append(localizedMagnitude(null, i, flags, width, l));

                        break;

                    }

                    case DateTime.SECOND: { // 'S' (00 - 60 - leap second)

                        int i = t.get(ChronoField.SECOND_OF_MINUTE);

                        Flags flags = Flags.ZERO_PAD;

                        sb.append(localizedMagnitude(null, i, flags, 2, l));

                        break;

                    }

                    case DateTime.ZONE_NUMERIC: { // 'z' ({-|+}####) - ls minus?

                        int i = t.get(ChronoField.OFFSET_SECONDS);

                        boolean neg = i < 0;

                        sb.append(neg ? '-' : '+');

                        if (neg) {
                            i = -i;
                        }

                        int min = i / 60;

                        // combine minute and hour into a single integer

                        int offset = (min / 60) * 100 + (min % 60);

                        Flags flags = Flags.ZERO_PAD;

                        sb.append(localizedMagnitude(null, offset, flags, 4, l));

                        break;

                    }

                    case DateTime.ZONE: { // 'Z' (symbol)

                        ZoneId zid = t.query(TemporalQueries.zone());

                        if (zid == null) {

                            throw new IllegalFormatConversionException(c, t.getClass());

                        }

                        if (!(zid instanceof ZoneOffset) &&

                                t.isSupported(ChronoField.INSTANT_SECONDS)) {

                            Instant instant = Instant.from(t);

                            sb.append(TimeZone.getTimeZone(zid.getId())

                                    .getDisplayName(zid.getRules().isDaylightSavings(instant),

                                            TimeZone.SHORT,

                                            Objects.requireNonNullElse(l, Locale.US)));

                            break;

                        }

                        sb.append(zid.getId());

                        break;

                    }

                    // Date

                    case DateTime.NAME_OF_DAY_ABBREV:     // 'a'

                    case DateTime.NAME_OF_DAY: { // 'A'

                        int i = t.get(ChronoField.DAY_OF_WEEK) % 7 + 1;

                        Locale lt = Objects.requireNonNullElse(l, Locale.US);

                        DateFormatSymbols dfs = DateFormatSymbols.getInstance(lt);

                        if (c == DateTime.NAME_OF_DAY) {
                            sb.append(dfs.getWeekdays()[i]);
                        } else {
                            sb.append(dfs.getShortWeekdays()[i]);
                        }

                        break;

                    }

                    case DateTime.NAME_OF_MONTH_ABBREV:   // 'b'

                    case DateTime.NAME_OF_MONTH_ABBREV_X: // 'h' -- same b

                    case DateTime.NAME_OF_MONTH: { // 'B'

                        int i = t.get(ChronoField.MONTH_OF_YEAR) - 1;

                        Locale lt = Objects.requireNonNullElse(l, Locale.US);

                        DateFormatSymbols dfs = DateFormatSymbols.getInstance(lt);

                        if (c == DateTime.NAME_OF_MONTH) {
                            sb.append(dfs.getMonths()[i]);
                        } else {
                            sb.append(dfs.getShortMonths()[i]);
                        }

                        break;

                    }

                    case DateTime.CENTURY:                // 'C' (00 - 99)

                    case DateTime.YEAR_2:                 // 'y' (00 - 99)

                    case DateTime.YEAR_4: { // 'Y' (0000 - 9999)

                        int i = t.get(ChronoField.YEAR_OF_ERA);

                        int size = 2;

                        switch (c) {

                            case DateTime.CENTURY:

                                i /= 100;

                                break;

                            case DateTime.YEAR_2:

                                i %= 100;

                                break;

                            case DateTime.YEAR_4:

                                size = 4;

                                break;

                        }

                        Flags flags = Flags.ZERO_PAD;

                        sb.append(localizedMagnitude(null, i, flags, size, l));

                        break;

                    }

                    case DateTime.DAY_OF_MONTH_0:         // 'd' (01 - 31)

                    case DateTime.DAY_OF_MONTH: { // 'e' (1 - 31) -- like d

                        int i = t.get(ChronoField.DAY_OF_MONTH);

                        Flags flags = (c == DateTime.DAY_OF_MONTH_0

                                ? Flags.ZERO_PAD

                                : Flags.NONE);

                        sb.append(localizedMagnitude(null, i, flags, 2, l));

                        break;

                    }

                    case DateTime.DAY_OF_YEAR: { // 'j' (001 - 366)

                        int i = t.get(ChronoField.DAY_OF_YEAR);

                        Flags flags = Flags.ZERO_PAD;

                        sb.append(localizedMagnitude(null, i, flags, 3, l));

                        break;

                    }

                    case DateTime.MONTH: { // 'm' (01 - 12)

                        int i = t.get(ChronoField.MONTH_OF_YEAR);

                        Flags flags = Flags.ZERO_PAD;

                        sb.append(localizedMagnitude(null, i, flags, 2, l));

                        break;

                    }


                    // Composites

                    case DateTime.TIME:         // 'T' (24 hour hh:mm:ss - %tH:%tM:%tS)

                    case DateTime.TIME_24_HOUR: { // 'R' (hh:mm same as %H:%M)

                        char sep = ':';

                        print(sb, t, DateTime.HOUR_OF_DAY_0, l).append(sep);

                        print(sb, t, DateTime.MINUTE, l);

                        if (c == DateTime.TIME) {

                            sb.append(sep);

                            print(sb, t, DateTime.SECOND, l);

                        }

                        break;

                    }

                    case DateTime.TIME_12_HOUR: { // 'r' (hh:mm:ss [AP]M)

                        char sep = ':';

                        print(sb, t, DateTime.HOUR_0, l).append(sep);

                        print(sb, t, DateTime.MINUTE, l).append(sep);

                        print(sb, t, DateTime.SECOND, l).append(' ');

                        // this may be in wrong place for some locales

                        StringBuilder tsb = new StringBuilder();

                        print(tsb, t, DateTime.AM_PM, l);

                        sb.append(tsb.toString().toUpperCase(Objects.requireNonNullElse(l,

                                Locale.getDefault(Locale.Category.FORMAT))));

                        break;

                    }

                    case DateTime.DATE_TIME: { // 'c' (Sat Nov 04 12:02:33 EST 1999)

                        char sep = ' ';

                        print(sb, t, DateTime.NAME_OF_DAY_ABBREV, l).append(sep);

                        print(sb, t, DateTime.NAME_OF_MONTH_ABBREV, l).append(sep);

                        print(sb, t, DateTime.DAY_OF_MONTH_0, l).append(sep);

                        print(sb, t, DateTime.TIME, l).append(sep);

                        print(sb, t, DateTime.ZONE, l).append(sep);

                        print(sb, t, DateTime.YEAR_4, l);

                        break;

                    }

                    case DateTime.DATE: { // 'D' (mm/dd/yy)

                        char sep = '/';

                        print(sb, t, DateTime.MONTH, l).append(sep);

                        print(sb, t, DateTime.DAY_OF_MONTH_0, l).append(sep);

                        print(sb, t, DateTime.YEAR_2, l);

                        break;

                    }

                    case DateTime.ISO_STANDARD_DATE: { // 'F' (%Y-%m-%d)

                        char sep = '-';

                        print(sb, t, DateTime.YEAR_4, l).append(sep);

                        print(sb, t, DateTime.MONTH, l).append(sep);

                        print(sb, t, DateTime.DAY_OF_MONTH_0, l);

                        break;

                    }

                    default:

                        assert false;

                }

            } catch (DateTimeException x) {

                throw new IllegalFormatConversionException(c, t.getClass());

            }

            return sb;

        }

        private void failMismatch(Flags f, char c) {

            String fs = f.toString();

            throw new FormatFlagsConversionMismatchException(fs, c);

        }


        // -- Methods to support throwing exceptions --

        private void failConversion(char c, Object arg) {

            throw new IllegalFormatConversionException(c, arg.getClass());

        }

        private char getZero(Locale l) {

            if ((l != null) && !l.equals(locale())) {

                DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(l);

                return dfs.getZeroDigit();

            }

            return zero;

        }

        private StringBuilder localizedMagnitude(StringBuilder sb,

                                                 long value, Flags f, int width, Locale l) {

            return localizedMagnitude(sb, Long.toString(value, 10), 0, f, width, l);

        }

        private StringBuilder localizedMagnitude(StringBuilder sb,

                                                 CharSequence value, final int offset, Flags f, int width,

                                                 Locale l) {

            if (sb == null) {

                sb = new StringBuilder();

            }

            int begin = sb.length();


            char zero = getZero(l);


            // determine localized grouping separator and size

            char grpSep = '\0';

            int grpSize = -1;

            char decSep = '\0';


            int len = value.length();

            int dot = len;

            for (int j = offset; j < len; j++) {

                if (value.charAt(j) == '.') {

                    dot = j;

                    break;

                }

            }


            if (dot < len) {

                if (l == null || l.equals(Locale.US)) {

                    decSep = '.';

                } else {

                    DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(l);

                    decSep = dfs.getDecimalSeparator();

                }

            }


            if (f.contains(Flags.GROUP)) {

                if (l == null || l.equals(Locale.US)) {

                    grpSep = ',';

                    grpSize = 3;

                } else {

                    DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(l);

                    grpSep = dfs.getGroupingSeparator();

                    DecimalFormat df = (DecimalFormat) NumberFormat.getIntegerInstance(l);

                    grpSize = df.getGroupingSize();

                }

            }


            // localize the digits inserting group separators as necessary

            for (int j = offset; j < len; j++) {

                if (j == dot) {

                    sb.append(decSep);

                    // no more group separators after the decimal separator

                    grpSep = '\0';

                    continue;

                }


                char c = value.charAt(j);

                sb.append((char) ((c - '0') + zero));

                if (grpSep != '\0' && j != dot - 1 && ((dot - j) % grpSize == 1)) {

                    sb.append(grpSep);

                }

            }


            // apply zero padding

            if (width != -1 && f.contains(Flags.ZERO_PAD)) {

                for (int k = sb.length(); k < width; k++) {

                    sb.insert(begin, zero);

                }

            }


            return sb;

        }

        private void localizedMagnitudeExp(StringBuilder sb, char[] value,

                                           final int offset, Locale l) {

            char zero = getZero(l);


            int len = value.length;

            for (int j = offset; j < len; j++) {

                char c = value[j];

                sb.append((char) ((c - '0') + zero));

            }

        }


        // Specialized localization of exponents, where the source value can only

        // contain characters '0' through '9', starting at index offset, and no

        // group separators is added for any locale.

        private class BigDecimalLayout {

            private StringBuilder mant;

            private StringBuilder exp;

            private boolean dot = false;

            private int scale;


            public BigDecimalLayout(BigInteger intVal, int scale, BigDecimalLayoutForm form) {

                layout(intVal, scale, form);

            }


            public boolean hasDot() {

                return dot;

            }


            public int scale() {

                return scale;

            }


            public StringBuilder mantissa() {

                return mant;

            }


            // The exponent will be formatted as a sign ('+' or '-') followed

            // by the exponent zero-padded to include at least two digits.

            public StringBuilder exponent() {

                return exp;

            }


            private void layout(BigInteger intVal, int scale, BigDecimalLayoutForm form) {

                String coeff = intVal.toString();

                this.scale = scale;


                // Construct a buffer, with sufficient capacity for all cases.

                // If E-notation is needed, length will be: +1 if negative, +1

                // if '.' needed, +2 for "E+", + up to 10 for adjusted

                // exponent.  Otherwise it could have +1 if negative, plus

                // leading "0.00000"

                int len = coeff.length();

                mant = new StringBuilder(len + 14);


                if (scale == 0) {

                    if (len > 1) {

                        mant.append(coeff.charAt(0));

                        if (form == BigDecimalLayoutForm.SCIENTIFIC) {

                            mant.append('.');

                            dot = true;

                            mant.append(coeff, 1, len);

                            exp = new StringBuilder("+");

                            if (len < 10) {

                                exp.append('0').append(len - 1);

                            } else {

                                exp.append(len - 1);

                            }

                        } else {

                            mant.append(coeff, 1, len);

                        }

                    } else {

                        mant.append(coeff);

                        if (form == BigDecimalLayoutForm.SCIENTIFIC) {

                            exp = new StringBuilder("+00");

                        }

                    }

                } else if (form == BigDecimalLayoutForm.DECIMAL_FLOAT) {

                    // count of padding zeros


                    if (scale >= len) {

                        // 0.xxx form

                        mant.append("0.");

                        dot = true;

                        trailingZeros(mant, scale - len);

                        mant.append(coeff);

                    } else {

                        if (scale > 0) {

                            // xx.xx form

                            int pad = len - scale;

                            mant.append(coeff, 0, pad);

                            mant.append('.');

                            dot = true;

                            mant.append(coeff, pad, len);

                        } else { // scale < 0

                            // xx form

                            mant.append(coeff, 0, len);

                            if (intVal.signum() != 0) {

                                trailingZeros(mant, -scale);

                            }

                            this.scale = 0;

                        }

                    }

                } else {

                    // x.xxx form

                    mant.append(coeff.charAt(0));

                    if (len > 1) {

                        mant.append('.');

                        dot = true;

                        mant.append(coeff, 1, len);

                    }

                    exp = new StringBuilder();

                    long adjusted = -(long) scale + (len - 1);

                    if (adjusted != 0) {

                        long abs = Math.abs(adjusted);

                        // require sign

                        exp.append(adjusted < 0 ? '-' : '+');

                        if (abs < 10) {

                            exp.append('0');

                        }

                        exp.append(abs);

                    } else {

                        exp.append("+00");

                    }

                }

            }

        }

    }

}

