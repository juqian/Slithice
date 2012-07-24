package jqian.sootex.location;

import jqian.Global;
import soot.ArrayType;
import soot.Scene;

public class UnknownArraySpace extends ArraySpace{
	private static UnknownArraySpace _instance;
	 
    protected static void reset(){
    	_instance = null;
    }
    
    public static UnknownArraySpace v(){
    	if(_instance==null){
    		_instance = new UnknownArraySpace();
    		Global.v().regesiterResetableGlobals(UnknownArraySpace.class);
    	}
    	
    	return _instance;
    }
    
    
    private UnknownArraySpace() {	}

    public ArrayType getType(){
    	return Scene.v().getObjectType().makeArrayType();
    }
    
    public String toString(){
    	return "*";
    }
}