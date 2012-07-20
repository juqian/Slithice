package jqian.sootex.location;

import jqian.sootex.util.*;
import soot.*;

/**
 * An ArraySpace is used to represent the element space of a array.
 */
public class ArraySpace extends InstanceObject{ 
	protected ArrayType _type;
	protected ArrayElmt _elmt;
	protected Value _alloc;  //allocation site
	 
    ArraySpace(SootMethod method,Value alloc,ArrayType type){        
	    this._alloc=alloc;
	    this._method=method;
		this._type=type;
		this._elmt=new ArrayElmt(this);
	}
    
    /** Only used for override. */
    public ArraySpace(){
    	this._elmt = new ArrayElmt(this);
    }
    
    public ArrayElmt getElement(){
        return _elmt;
    }
      
    public String toString(){
        String str="(";
        str += (_alloc!=null)? "N": "T";
        str += getNumber()+"#"+_type.toString();
        
        if(_alloc!=null){
        	str += "@";
        	if(_method!=null)
            	str+=_method.getName();//+_method.getSignature();
        	if(getAllocUnit()!=null)
            	str+=SootUtils.getLine(getAllocUnit());
        }        
        str+=")";
        return str;
    }
    
    public ArrayType getType(){
        return _type;
    }
}
