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
	public static final InstanceObject NIL = new SpecialObject("nil");
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
	
	private   int _objId;
	protected SootMethod _method;      //the allocation method
	protected Numberable _binding;

	public static InstanceObject makeInstObject(AllocNode node) {	
		InstanceObject obj = null;		 
		Object expr = node.getNewExpr();
		SootMethod method = node.getMethod();
		Type type = node.getType();

		if (expr instanceof NewExpr) {
			obj = new CommonInstObject(method, (Value) expr, type);
			obj._binding = node;
		} 
		else if (expr instanceof NewArrayExpr	|| expr instanceof NewMultiArrayExpr) {
			obj = new ArraySpace(method, (Value) expr, (ArrayType) type);
			obj._binding = node;
		} 
		else {
			// XXX constant do not affect program analysis, especially dependence analysis, so we ignore them here
			/*
			 * if(expr instanceof ClassConstant){
			 * 
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
			InstanceObject o = new ArraySpace(null,null,(ArrayType)type);  
			o._binding = type;
			return o;
		}
		else if(type instanceof RefType){
			InstanceObject o = new CommonInstObject(null,null,type);
			o._binding = type;
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
	protected InstanceObject() {
		this._objId = TOTAL;
		TOTAL++;
	}

	/** Get allocation site. */
	public Unit getAllocUnit(){
        return null;
    }
	
    /**Get the method where the object is allocated*/
    public SootMethod getAllocMethod(){
        return _method;
    }	
    
    public Numberable getBindingObject(){
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
        this._name=name;        
    }
   
    public String toString(){ 
        return _name;
    }
    
    public Type getType(){
        return Scene.v().getRefType("java.lang.Object");
    }
}

