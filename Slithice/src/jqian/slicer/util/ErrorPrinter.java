package jqian.slicer.util;

import jqian.slicer.plugin.*;
import jqian.util.eclipse.ConsoleUtil;


public class ErrorPrinter {
	public static void printError(String msg){
		ConsoleUtil.printError(ID.CONSOLE, msg);
	}
	
	public static void printError(Exception e){
		ConsoleUtil.printError(ID.CONSOLE, e.getMessage());
	}
}
