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

package de.uni_mannheim.informatik.dws.winter.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * 
 * Collection of utility functions for files.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class FileUtils {

    public static long countLines(String path) {
        
        long lines=0;
        
        try {
            BufferedReader r = new BufferedReader(getReader(path));
            
            
            while(r.readLine()!=null)
            {
                lines++;
            }
            
            r.close();
        } catch (IOException e) {
            e.printStackTrace();
            lines=-1;
        }
        
        return lines;
    }
    
    public static Reader getReader(String path) throws FileNotFoundException, IOException {
        if (path.endsWith(".gz")) {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(path));
            return new InputStreamReader(gzip, "UTF-8");
        } else {
            return new InputStreamReader(new FileInputStream(path), "UTF-8");
        }
    }
    
    /**
     * @param root
     * @return returns a list of all files (including those in sub-directories)
     */
    public static List<File> listAllFiles(File root) {
		LinkedList<File> files = new LinkedList<>();
		
		if(root.isDirectory()) {
			LinkedList<File> toList = new LinkedList<>(Arrays.asList(root.listFiles()));
			
			while(!toList.isEmpty()) {
				File f = toList.poll();
				
				if(f.isDirectory()) {
					toList.addAll(Arrays.asList(f.listFiles()));
				} else {
					files.add(f);
				}
			}
		} else {
			files.add(root);
		}
		
		return files;
    }
}
