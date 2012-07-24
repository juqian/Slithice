package jqian.sootex.location;

import soot.*;
import soot.jimple.spark.pag.AllocNode;

/**
 * An ArraySpace is used to represent the element space of a array.
 */
public class ArraySpace extends InstanceObject{ 
	protected ArrayType _type;
	protected ArrayElmt _elmt;
	 
    ArraySpace(Object binding, ArrayType type){   
    	super(binding);
 
		this._type=type;
		this._elmt=new ArrayElmt(this);
	}
    
    /** Only used for override. */
    ArraySpace(){
    	super(null);
    	this._elmt = new ArrayElmt(this);
    }
    
    public ArrayElmt getElement(){
        return _elmt;
    }
      
    public String toString(){
    	Object alloc = null;
    	SootMethod method = null;
    	if(_binding instanceof AllocNode){
    		AllocNode node = (AllocNode)_binding;
    		alloc = node.getNewExpr();
    		method = node.getMethod();    		 
    	}
    	
        String str="(";
        str += (alloc!=null)? "N": "T";
        str += getNumber()+"#"+_type.toString();
        
        if(alloc!=null){
        	str += "@";
        	if(method!=null)
            	str += method.getName();//+_method.getSignature();
        	//if(getAllocUnit()!=null)
            //	str+=SootUtils.getLine(getAllocUnit());
        }        
        str+=")";
        return str;
    }
    
    public ArrayType getType(){
        return _type;
    }
}
