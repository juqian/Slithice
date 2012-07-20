package jqian.sootex.location;

import soot.*;

public class ArrayElmt extends HeapLocation{ 
	private ArraySpace _array;
	
    ArrayElmt(ArraySpace array) {
        this._array=array;
    }   
 
    public Type getType() {
        return _array.getType().getArrayElementType();
    }
    
    public InstanceObject getWrapperObject(){
        return _array;
    }

    public String toString(){
    	String s = (_array!=null)?_array.toString() : "N/A";
        return s + "[]";
    }
}
