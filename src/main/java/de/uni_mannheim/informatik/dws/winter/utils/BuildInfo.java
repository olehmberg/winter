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

import java.io.File;
import java.net.JarURLConnection;

import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * 
 * Provides access to the build time and path of the executed jar file
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class BuildInfo {

    public static Long getBuildTime(Class<?> cl) {
        try {
            String rn = cl.getName().replace('.', '/') + ".class";
            JarURLConnection j = (JarURLConnection) ClassLoader.getSystemResource(rn).openConnection();
            return j.getJarFile().getEntry("META-INF/MANIFEST.MF").getTime();
        } catch (Exception e) {
            return 0L;
        }
    }
    
    public static String getBuildTimeString(Class<?> cl) {
        return DateFormatUtils.format(getBuildTime(cl), "yyyy-MM-dd HH:mm:ss");
    }
    
    public static File getJarPath(Class<?> cl) {
    	try {
    		return new File(cl.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        } catch (Exception e) {
            return null;
        }
    }
}
