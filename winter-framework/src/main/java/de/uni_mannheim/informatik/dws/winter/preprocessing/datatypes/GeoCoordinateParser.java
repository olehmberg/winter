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

public class GeoCoordinateParser {

    /**
     * this regex is more complex and catches all types of coordinates, but
     * often catches single double numbers
     */
    public static final String GeoCoordRegex = "([SNsn][\\s]*)?((?:[\\+-]?[0-9]*[\\.,][0-9]+)|(?:[\\+-]?[0-9]+))(?:(?:[^ms'′\"″,\\.\\dNEWnew]?)|(?:[^ms'′\"″,\\.\\dNEWnew]+((?:[\\+-]?[0-9]*[\\.,][0-9]+)|(?:[\\+-]?[0-9]+))(?:(?:[^ds°\"″,\\.\\dNEWnew]?)|(?:[^ds°\"″,\\.\\dNEWnew]+((?:[\\+-]?[0-9]*[\\.,][0-9]+)|(?:[\\+-]?[0-9]+))[^dm°'′,\\.\\dNEWnew]*))))([SNsn]?)[^\\dSNsnEWew]+([EWew][\\s]*)?((?:[\\+-]?[0-9]*[\\.,][0-9]+)|(?:[\\+-]?[0-9]+))(?:(?:[^ms'′\"″,\\.\\dNEWnew]?)|(?:[^ms'′\"″,\\.\\dNEWnew]+((?:[\\+-]?[0-9]*[\\.,][0-9]+)|(?:[\\+-]?[0-9]+))(?:(?:[^ds°\"″,\\.\\dNEWnew]?)|(?:[^ds°\"″,\\.\\dNEWnew]+((?:[\\+-]?[0-9]*[\\.,][0-9]+)|(?:[\\+-]?[0-9]+))[^dm°'′,\\.\\dNEWnew]*))))([EWew]?)";
    public static final Pattern GEO_COORD_REGEX_SIMPLE = Pattern.compile("^([-+]?\\d{1,2}([.]\\d+)?),?\\s+([-+]?\\d{1,3}([.]\\d+)?)$", Pattern.CASE_INSENSITIVE);
    public static final Pattern GEO_COORD_REGEX_SIMPLE1 = Pattern.compile("^[-+]?\\d{1,2}[.]\\d{4,8}$", Pattern.CASE_INSENSITIVE);

    public static boolean parseGeoCoordinate(String text) {
        if (GEO_COORD_REGEX_SIMPLE.matcher(text).matches()) {
            return true;
        }
        if (GEO_COORD_REGEX_SIMPLE1.matcher(text).matches()) {
            if(Double.parseDouble(text)>-180.00 && Double.parseDouble(text)<180.00 ) {
                return true;
            }
        }
        return false;
    }

}
