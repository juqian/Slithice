package jqian.sootex.location;

import jqian.Global;

import soot.*;
import soot.jimple.*;
import soot.util.*;
import soot.jimple.spark.pag.AllocNode;

/**
 * Represent Abstract Objects.
 * <immutable>
 */
public abstract class InstanceObject implements Numberable{  
	public  static final InstanceObject NIL = new SpecialObject("nil");
	private static int TOTAL;  
	private static InstanceObject[] _type2obj;
	
	static void reset(){
		TOTAL = 0;		 
		int types = Scene.v().getTypeNumberer().size();    	
        _type2obj = new InstanceObject[types+1];
	}	
	
	static{
		reset();		
		Global.v().regesiterResetableGlobals(InstanceObject.class);		
	}	

	public static InstanceObject makeInstObject(AllocNode node) {	
		InstanceObject obj = null;		 
		Object expr = node.getNewExpr();
		Type type = node.getType();

		if (expr instanceof NewExpr) {
			obj = new CommonInstObject(node, type);
		} 
		else if (expr instanceof NewArrayExpr	|| expr instanceof NewMultiArrayExpr) {
			obj = new ArraySpace(node, (ArrayType) type);		 
		} 
		else {
			// XXX Constants do not affect analysis, especially dependence analysis, so we ignore them here
			/*
			 * if(expr instanceof ClassConstant){
			 * Type type=node.getType(); if(type==RefType.v( "java.lang.String"
			 * )){
			 *  } else if(type==RefType.v( "java.lang.Class")){
			 *  } else if(type instanceof RefType){
			 * loc=CommonHeapObject.getSparkHeapObject(node); } else if(type
			 * instanceof ArrayType){ loc=ArraySpace.getSparkHeapObject(node); }
			 */
		} 
		
		return obj;
	}	
	
	public static InstanceObject typeToObject(Type type){
		int tId = type.getNumber();
		InstanceObject o = _type2obj[tId];
		if(o==null){
			o = InstanceObject.createTypeObject(type);        		 
			_type2obj[tId] = o;
		}
		
		return o;
	}

	private static InstanceObject createTypeObject(Type type){
		if(type instanceof ArrayType){
			InstanceObject o = new ArraySpace(type,(ArrayType)type);  		 
			return o;
		}
		else if(type instanceof RefType){
			InstanceObject o = new CommonInstObject(type, type);			 
			return o;
		}
		else if(type instanceof NullType){
           	return InstanceObject.NIL;
        }
		else{//AnySubType
			throw new RuntimeException("Not a type for objects: " + type);
		}
    }  
	 
	// ---------------------- Instance members ------------------------//
	protected final int _objId;
	protected Object _binding;
	

	protected InstanceObject(Object binding) {
		this._objId = TOTAL;
		this._binding = binding;
		
		TOTAL++;
	}

	/** Get the attached object. Usually used to identify the instance object. */
    public Object getBindingObject(){
        return _binding;
    }
	
	/**Each heap object is assigned with a unique id*/
	public int getNumber() {
		return _objId;
	}
	
	public void setNumber(int i){		
	}
}


class SpecialObject extends InstanceObject{
	private String _name;
	
    SpecialObject(String name){
    	super(name);
        this._name=name;        
    }
   
    public String toString(){ 
        return _name;
    }
    
    public Type getType(){
        return Scene.v().getRefType("java.lang.Object");
    }
}

