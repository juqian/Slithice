package jqian.sootex.location;

import jqian.sootex.util.*;
import jqian.util.CollectionUtils;
import jqian.*;
import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.util.*;


/**
 * <immutable>
 * Represent abstract locations. An unified interface for Locals, SootFields, and etc, 
 * used for the sake of clearness and easy type checking.
 */
public abstract class Location implements Numberable{
	private static int COUNT;	
    private static Location[] _FIELD2LOC;
    private static MethodRet[] _METHOD2RET;
    private static Map<Value,Location> _VALUE2LOC; 	
    private static HeapLocation[] _TYPE2LOC; 	
	
	//-------------------- Static Methods -----------------------//
	protected static void reset(){
		COUNT = 0;
		 
		Scene scene = Scene.v();
		_METHOD2RET = new MethodRet[SootUtils.getMethodCount()];
		_FIELD2LOC = new Location[scene.getFieldNumberer().size() + 1];	
		_TYPE2LOC = new HeapLocation[scene.getTypeNumberer().size() + 1];
		
		_VALUE2LOC = new WeakHashMap<Value,Location>(CollectionUtils.getHashSetInitCapacity(scene.getLocalNumberer().size()));
	}
	
	static{
		reset();
		Global.v().regesiterResetableGlobals(Location.class);
	}
	
	/** Force releasing locations relevant to a given method. */
	public static void release(SootMethod m){
		if(!m.hasActiveBody())
			return;
		
		Body body = m.getActiveBody();
		Set<Local> interfaceLocals = new HashSet<Local>();
		int count = m.getParameterCount();
		for(int i=0; i<count;i++){
			interfaceLocals.add(body.getParameterLocal(i));
		}
		if(!m.isStatic()){
			interfaceLocals.add(body.getThisLocal());
		}		
		
		for(Local local: body.getLocals()){
			if(!interfaceLocals.contains(local)){
				_VALUE2LOC.remove(local);
			}
		}
	}
	
	
	/**
	 * Construct the Location from an immediate: constant or local.	
	 * @param value	   Should be of the type Constant or Local
	 */
    public static Location valueToLocation(Value value){   
    	Location loc = _VALUE2LOC.get(value);
		if (loc == null) {			      
	        if(value instanceof Local){
	        	loc = new StackLocation(value);	        
	        }        
	        else if(value instanceof NullConstant){            
	            //loc = null;  
	        	loc = NullConstLocation.v();
	        }   
	        else if(value instanceof ClassConstant){ 
	        	//loc = null;
	        	loc = new ConstLocation(value);	         
	        } 
	        else if(value instanceof StringConstant){	         
	            loc = new ConstLocation(value);		
	        }
	        else if(value instanceof NumericConstant){
	        	loc = new ConstLocation(value);		         
	        }
	        else{
	            throw new RuntimeException("Bad usage: input "+value.getClass());
	        }
	        
	        _VALUE2LOC.put(value, loc);
		}
		
		return loc;
    }    
 
    /** Get the unique return location of a method. */
    public static Location methodToRet(SootMethod m){  
    	int mId = m.getNumber();
    	MethodRet loc = _METHOD2RET[mId];
		if (loc == null) {
			loc = new MethodRet(m);
			_METHOD2RET[mId] = loc;
		}

		return loc;
    } 
    
    public static Location getThisPointer(SootMethod method){
        return valueToLocation(method.getActiveBody().getThisLocal()); 
    }    
  
	public static GlobalLocation getGlobalLocation(SootField field) {
		int fId = field.getNumber();
		GlobalLocation p = (GlobalLocation)_FIELD2LOC[fId];
		if (null == p) {
			p = new GlobalLocation(field);
			_FIELD2LOC[fId] = p;
		}
		return p;
	} 
	
	/** Used for field-based analysis. Do not distinguish instance objects. */
	public static HeapField getHeapFieldLocation(SootField field) {
		int fId = field.getNumber();
		HeapField p = (HeapField)_FIELD2LOC[fId];
		if (null == p) {
			p = new HeapField(null, field);
			_FIELD2LOC[fId] = p;
		}
		return p;
	} 
	
	/** A single heap field to model all instance fields. */
	public static HeapField getUnknownHeapField(){
		return UnknownInstObject.v().getField();
	}
	
	/** A single array element to model all runtime array elements*/ 
	public static ArrayElmt getUnknownArrayElmt(){
		return UnknownArraySpace.v().getElement();
	}
	
	public static class TypeLocation extends HeapLocation{  
		Type _type;
		
		public TypeLocation(Type t){
			this._type = t;
		}
		
		public InstanceObject getWrapperObject(){
			return null;
		}
		
		public Type getObjectType(){
			return _type;
		}
 
		@Override
		public Type getType() {			
			return Scene.v().getObjectType();
		}
		
		public String toString(){
			return _type.toString() + ".*";
		}
	} 
	
	/** A single abstract location to model all heap memory of a given type. */ 
	public static HeapLocation getLocationForType(Type t){
		int tId = t.getNumber();
		HeapLocation p = _TYPE2LOC[tId];
		if (null == p) {
			p = new TypeLocation(t);
			_TYPE2LOC[tId] = p;
		}
		return p;
	}

	 
    //------------------ Instance Methods ------------------------//
	private int _id; 
	
    protected Location(){
    	this._id = COUNT;
    	COUNT++;    	
    }
    
    public boolean isPointer(){
    	return getType() instanceof RefLikeType;
    }
   
    public int getNumber(){
    	return _id;
    }
    
    public void setNumber(int id){    	
    }
    
    public int hashCode(){
        return _id;
    }
    
    public abstract Type getType();
}
