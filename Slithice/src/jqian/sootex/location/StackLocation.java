package jqian.sootex.location;

import soot.*;

/**
 * <immutable>
 */
public class StackLocation extends Location {
	protected Value _value;
	
	StackLocation(Value lc){
		this._value = lc;
	}	 
	
    public Value getValue(){
    	return _value;
    }
   
    public Type getType(){
        return _value.getType();
    }   

    public String toString(){
        return _value.toString();
    }
}
