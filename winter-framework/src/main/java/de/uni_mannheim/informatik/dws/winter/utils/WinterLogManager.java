/*
 * Copyright (c) 2018 Data and Web Science Group, University of Mannheim, Germany (http://dws.informatik.uni-mannheim.de/)
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Logging class for the winter framework based on log4j2.
 * The corresponding configuration can be found in: winter\winter-framework\src\main\resources\log4j2.xml
 * 
 * Trace Options/ logger names:
 * 		default: 	level INFO	- console
 * 		trace:		level TRACE - console
 * 		infoFile:	level INFO	- console/file
 * 		traceFile:	level TRACE	- console/file
 * 
 * @author Alexander Brinkmann (albrinkm@mail.uni-mannheim.de)
 * 
 */
public class WinterLogManager {

	private static Logger logger;
	
	/**
	 * Return current active logger.
	 * If none is defined, returns root (default) logger.
	 * @return  Current active logger.
	 */
	public static Logger getLogger() {
		if (WinterLogManager.logger == null) {
			WinterLogManager.logger = LogManager.getRootLogger();
		}
		return WinterLogManager.logger;
	}
	
	/**
	 * Return the logger defined in name and sets this one as the current active logger.
	 * @return  Current active logger.
	 */
	public static Logger activateLogger(String name) {
		if(name.equals("default")){
			WinterLogManager.setLogger(null);
		}
		else{
			setLogger(LogManager.getLogger(name));
		}
		return getLogger();
	}
	
	/**
	 * Return the logger defined in name.
	 * @return  explicitly defined logger.
	 */
	public static Logger getLogger(String name) {
		return LogManager.getLogger(name);
	}
	
	/**
	 * Set the specified logger as active logger.
	 * @param logger new active logger.
	 */
	public static void setLogger(Logger logger) {
		WinterLogManager.logger = logger;
	}

}
