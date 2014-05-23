// By Adam Micha³owski (c) 2013 Settlers Simulation

package com.softsquare.settlers;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
 
public class Logger {
	public enum TYPE {
		INFO, SUCCESS, 
		WARNING, ERROR,
		DEBUG, FATAL
	}
	public interface LoggerListener {
		abstract void onLog(Logger.TYPE type, String text);
	}
	
	private static Logger _logger = null;
	private final String _fileName = "error.html";
	private final boolean _listenersEnabled = true;
	private static ArrayList<LoggerListener> _listeners = new ArrayList<LoggerListener>();
	
	public static void addListener(LoggerListener listener) {
		if(_listeners.contains(listener) == false)
			_listeners.add(listener);
	}
	
	public static void removeListener(LoggerListener listener) {
		_listeners.remove(listener);
	}	
	private Logger() {
		if(!SettlersGame.isOnDesktop) return;
		String htmlHeader = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?> "
				+ "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\""
				+ " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"> <html>"
                + "<head> <title>XGine log file</title> <meta http-equiv=\"Content-Type\" "
                + "content=\"text/html; charset=utf-8\"/> <style type=\"text/css\"> body "
                + "{ background: #1a242a; color: #b4c8d2; margin-right: 20px; margin-left: "
                + "20px; font-size: 14px; font-family: Arial, sans-serif, sans; } a "
                + "{ text-decoration: none; } a:link { color: #b4c8d2; } a:active { color:"
                + " #ff9900; } a:visited { color: #b4c8d2; } a:hover { color: #ff9900; } h1"
                + " { text-align: center; } h2 { color: #ffffff; } .message, .success .warning,"
                + " .error, .debug, .fatal, .message { background-color: #080c10; color: #b4c8d2;"
                + " padding: 3px; } .success { background-color: #080c10; color: #339933; padding:"
                + " 3px; } .warning { background-color: #839ca7; color: #1a242a; padding: 3px; } "
                + ".error { background-color: #ff9933; color: #1a242a; padding: 3px; } .debug {"
                + " background-color: #080c10; color: #2255dd; padding: 3px; } .fatal {"
                + " background-color: #ffffff; color: #ff5522; padding: 3px; }</style></head><body>";
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(_fileName, false)));
			out.println(htmlHeader);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Logger getInstance() {
		if(_logger == null) _logger = new Logger();
		return _logger;
	}
	
	public void _log(Logger.TYPE type, String text) {
		if(!SettlersGame.isOnDesktop) return;
		
		if(_listenersEnabled) {
			for(LoggerListener l : _listeners)
				l.onLog(type, text);
		}
		
		PrintWriter out;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(_fileName, true)));
			switch (type) {
			case INFO:
				out.print("\n<div class=\"message\">");
				break;
			case SUCCESS:
				out.print("\n<div class=\"success\">");
				break;
			case WARNING:
				out.print("\n<div class=\"warning\">");
				break;
			case ERROR:
				out.print("\n<div class=\"error\">");
				break;
			case DEBUG:
				out.print("\n<div class=\"debug\">");
				break;
			case FATAL:
				out.print("\n<div class=\"fatal\">");
				break;
			default:
				out.print("\n<div class=\"message\">");
				break;
			}
			out.print("<b>[" + (new Date()).toString() + "]</b> " + text + "</div>");
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void log(String text) {
		Logger.getInstance()._log(Logger.TYPE.INFO, text);
	}
	
	public static void logInfo(String text) {
		Logger.getInstance()._log(Logger.TYPE.INFO, text);
	}
	
	public static void logSuccess(String text) {
		Logger.getInstance()._log(Logger.TYPE.SUCCESS, text);
	}
	
	public static void logWarning(String text) {
		Logger.getInstance()._log(Logger.TYPE.WARNING, text);
	}
 
	public static void logError(String text) {
		Logger.getInstance()._log(Logger.TYPE.ERROR, text);
	}
	
	public static void logDebug(String text) {
		Logger.getInstance()._log(Logger.TYPE.DEBUG, text);
	}
 
	public static void logFatal(String text) {
		Logger.getInstance()._log(Logger.TYPE.FATAL, text);
	}
 
}


