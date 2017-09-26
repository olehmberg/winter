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

package de.uni_mannheim.informatik.dws.winter.index.io;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilterFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;

public class StringTokeniser {
   
    public static List<String> tokenise(String s, boolean useStemmer) {
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
        List<String> result = new ArrayList<String>();

        try {

            Map<String, String> args = new HashMap<String, String>();
            args.put("generateWordParts", "1");
            args.put("generateNumberParts", "1");
            args.put("catenateNumbers", "0");
            args.put("splitOnCaseChange", "1");
            WordDelimiterFilterFactory fact = new WordDelimiterFilterFactory(args);

            // resolve non unicode chars
            s = StringEscapeUtils.unescapeJava(s);
            
            // remove brackets (but keep content)
                s = s.replaceAll("[\\(\\)]", "");
                
            // tokenise
            TokenStream stream = fact.create(new WhitespaceTokenizer(
                    Version.LUCENE_46, new StringReader(s)));
            stream.reset();

            if(useStemmer) {
                // use stemmer if requested
                stream = new PorterStemFilter(stream);
            }
            
            // lower case all tokens
            stream = new LowerCaseFilter(Version.LUCENE_46, stream);
            
            // remove stop words
            stream = new StopFilter(Version.LUCENE_46, stream,
                    ((StopwordAnalyzerBase) analyzer).getStopwordSet());
            
            // enumerate tokens
            while (stream.incrementToken()) {
                result.add(stream.getAttribute(CharTermAttribute.class)
                        .toString());
            }
            stream.close();
        } catch (IOException e) {
            // not thrown b/c we're using a string reader...
        }
        
        analyzer.close();
        
        return result;
    }
}
