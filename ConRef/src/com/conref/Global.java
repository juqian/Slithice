package com.conref;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;



/**
 * Register to this class, so that the global static variables in the 
 * registered class can be automatically reset.
 */
public class Global {
	private static Set<Class<?>> _classesWithGlobal = new HashSet<Class<?>>();

	private static Global _instance;
	private Global(){
	
		out = System.out;
	}
	
	public static Global v(){
		if(_instance==null){
			_instance = new Global();			
		}
		return _instance;
	} 
	
	
	public PrintStream out; 

	public boolean isDebug;
	
    /** A resetable global is hold by a class with static method named "reset",
     *  and call reset() can achieve the reset operation.
     */
    public void regesiterResetableGlobals(Class<?> c){
    	_classesWithGlobal.add(c);
    }
    
    
    /** The reset method make reenterable. */
    public void reset(){
    	//reset all globals
    	if(_instance!=null){
    		for(Class<?> c: _classesWithGlobal){
        		try{
        			Method m = c.getDeclaredMethod("reset");
            		m.setAccessible(true);
            		m.invoke(null);
        		}
        		catch(Exception e){
        			e.printStackTrace();
        		}
        	}
    	}
    	
      	_instance = null;	 
      	
      	out = null; 

        //force garbage collection
    	System.gc();
    	System.gc();
    }
}
