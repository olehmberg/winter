package de.uni_mannheim.informatik.dws.winter.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WinterLogManager {

	private static Logger logger;

	public static Logger getLogger() {
		if (WinterLogManager.logger == null) {
			WinterLogManager.logger = LogManager.getRootLogger();
		}
		return WinterLogManager.logger;
	}

	public static Logger getLogger(String name) {
		setLogger(LogManager.getLogger(name));
		return getLogger();
	}

	public static Logger getRootLogger() {
		setLogger(LogManager.getRootLogger());
		return getLogger();
	}

	public static void setLogger(Logger logger) {
		WinterLogManager.logger = logger;
	}

}
