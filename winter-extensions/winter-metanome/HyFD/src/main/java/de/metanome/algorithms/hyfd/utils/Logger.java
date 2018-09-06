package de.metanome.algorithms.hyfd.utils;

public class Logger {

	private static Logger instance = null;
	
	private StringBuilder log = new StringBuilder();
	
	private Logger() {
	}
	
	public static Logger getInstance() {
		if (instance == null)
			instance = new Logger();
		return instance;
	}
	
	public void write(String message) {
		this.log.append(message);
		System.out.print(message);
	}
	
	public void writeln(String message) {
		this.log.append(message + "\r\n");
		System.out.println(message);
	}
	
	public void write(Object message) {
		this.write(message.toString());;
	}
	
	public void writeln(Object message) {
		this.writeln(message.toString());;
	}
	
	public String read() {
		return this.log.toString();
	}
}
