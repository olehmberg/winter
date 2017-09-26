/*
 * Copyright (c) 2017 Data and Web Science Group, University of Mannheim, Germany (http://dws.informatik.uni-mannheim.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes;

import java.util.regex.Pattern;

public class NumericParser {

    // from
    // https://docs.oracle.com/javase/6/docs/api/java/lang/Double.html#valueOf%28java.lang.String%29

    final static  String Digits = "(\\p{Digit}+)";
    final static  String HexDigits = "(\\p{XDigit}+)";
    // an exponent is 'e' or 'E' followed by an optionally
    // signed decimal integer.
    final static  String Exp = "[eE][+-]?" + Digits;
    final static Pattern fpRegex = Pattern.compile(("[\\x00-\\x20]*" + // Optional leading
                                               // "whitespace"
            "[+-]?(" + // Optional sign character
            "NaN|" + // "NaN" string
            "Infinity|" + // "Infinity" string

            // A decimal floating-point string representing a finite
            // positive
            // number without a leading sign has at most five basic pieces:
            // Digits . Digits ExponentPart FloatTypeSuffix
            //
            // Since this method allows integer-only strings as input
            // in addition to strings of floating-point literals, the
            // two sub-patterns below are simplifications of the grammar
            // productions from the Java Language Specification, 2nd
            // edition, section 3.10.2.

            // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
            "(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|" +

            // . Digits ExponentPart_opt FloatTypeSuffix_opt
            "(\\.(" + Digits + ")(" + Exp + ")?)|" +

            // Hexadecimal strings
            "((" +
            // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
            "(0[xX]" + HexDigits + "(\\.)?)|" +

            // 0[xX] HexDigits_opt . HexDigits BinaryExponent
            // FloatTypeSuffix_opt
            "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

            ")[pP][+-]?" + Digits + "))" + "[fFdD]?))" + "[\\x00-\\x20]*"), Pattern.CASE_INSENSITIVE);// Optional
                                                                           // trailing
                                                                           // "whitespace"
    
    public static boolean parseNumeric(String text) {
        if(canParseDouble(text)) {
            return true;
        } else {
            return parseByChar(text);
        }
    }


    private static boolean canParseDouble(String text) {
        return fpRegex.matcher(text).matches();
    }

    private static boolean parseByChar(String text) {
        // go char by char and see if it is code or some other number
        // this should be changed with units
        int nmNumbers = 0;
        int nmChars = 0;

        for (char ch : text.toCharArray()) {
            if (Character.isDigit(ch))
                nmNumbers++;
            else if (!Character.isWhitespace(ch))
                nmChars++;
        }
        if (nmNumbers >= 1.5 * nmChars)
            return true;
        return false;
    }
}
