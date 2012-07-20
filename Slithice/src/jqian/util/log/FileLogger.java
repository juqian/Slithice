package jqian.util.log;

import java.io.*;
/**
 *    
 */
public class FileLogger implements Logger{
	protected PrintStream out;
	public FileLogger(){
		out = System.out;
	}
	
	public FileLogger(String path){
		try{
	       out = new PrintStream(new FileOutputStream(path));
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
    public void log(String message){
    	out.println(message);
    }
}
