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

import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/**
 * 
 * Super class for executable classes that provides command-line parsing
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 *
 */
public class Executable {

    @Parameter
    protected List<String> params;
    
    public List<String> getParams() {
		return params;
	}
    
    private boolean silent = false;
    /**
	 * @param silent the silent to set
	 */
	public void setSilent(boolean silent) {
		this.silent = silent;
	}
    
    protected boolean parseCommandLine(Class<?> cls, String... args) {
        try {
        	if(!silent) {
	        	System.err.println(BuildInfo.getJarPath(getClass()));
	        	System.err.println(String.format("%s version %s", cls.getName(), BuildInfo.getBuildTimeString(cls)));
        		System.err.println("		 __      __.___        __                     ");
        		System.err.println("		 /  \\    /  \\   | _____/  |_  ____     _______ ");
        		System.err.println("		 \\   \\/\\/   /   |/    \\   __\\/ __ \\    \\_  __ \\");
        		System.err.println("		  \\        /|   |   |  \\  | \\  ___/     |  | \\/");
        		System.err.println("		   \\__/\\  / |___|___|  /__|  \\___  > /\\ |__|   ");
        		System.err.println("		        \\/           \\/          \\/  \\/        ");
        	}
        	
            @SuppressWarnings("unused")
			JCommander cmd = new JCommander(this, args);
            
            return true;
        } catch(Exception e) {
        	System.err.println(e.getMessage());
        	usage(args);
            return false;
        }
    }
    
    protected void usage(String... args) {
    	System.out.println(StringUtils.join(args, " "));
        JCommander cmd = new JCommander(this);
        cmd.usage();
    }
	
}
