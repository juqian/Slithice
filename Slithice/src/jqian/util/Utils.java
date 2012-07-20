package jqian.util;

import java.io.File;
import java.util.*;


/**
 *  
 */
public class Utils {
    /** Calculate the time between two events */
    public static String getTimeConsumed(Date start, Date end){
        long time = end.getTime()-start.getTime();
        return ""+time/1000+"."+(time/100)%10+"s";
    } 
	
	/** Transfer a free style string into a string that is valid as a file path. */
	public static String toValidFilepath(String str){
        StringBuffer strBuf = new StringBuffer(str);
        for (int i = 0; i < strBuf.length(); i++) {
            if (strBuf.charAt(i) == '<')
                strBuf.setCharAt(i, '_');
            if (strBuf.charAt(i) == '>')
                strBuf.setCharAt(i, '_');
        }
        return strBuf.toString();
	}
	
	/** Get entry class of the application. Use exception mechanism for implementation.*/
	public static String getEntryClass(){
		StackTraceElement stack[]  =  (new Throwable()).getStackTrace();   
		return stack[stack.length-1].getClassName();  
	} 
	
	
	static void clearFile(File file){	
		if(file.isDirectory()){
			for(File f: file.listFiles()){			
				clearFile(f);				 
			}
		}		
		
		file.delete();
	}
	
	public static void clearDirectory(String dir){
		File dirFile = new File(dir);
		File[] files = dirFile.listFiles();
		if(files!=null){
			for(File f: files){
				clearFile(f);
			}
		}		
	}
	
	public static void assureDirectory(String filepath){
		File f = new File(filepath);		
		File dir = f.getParentFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}
}
