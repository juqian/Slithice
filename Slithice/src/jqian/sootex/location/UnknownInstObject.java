package jqian.sootex.location;

import jqian.Global;
import soot.Scene;
import soot.SootField;
import soot.Type;


public class UnknownInstObject extends CommonInstObject{
	// auto clean
	private static UnknownInstObject _instance;
	 
    protected static void reset(){
    	_instance = null;
    }
    
    public static UnknownInstObject v(){
    	if(_instance==null){
    		_instance = new UnknownInstObject();
    		Global.v().regesiterResetableGlobals(UnknownInstObject.class);
    	}
    	
    	return _instance;
    }    
    
	public static class UnknownHeapField extends HeapField {   
		private UnknownHeapField(CommonInstObject obj){
			super(obj, AnyField.v());
		}

		@Override
		public Type getType() {
			return Scene.v().getObjectType();
		}
	}
		
	protected final HeapField _field;
	
    private UnknownInstObject() {
		_field = new UnknownHeapField(this);
	}
    
    public Type getType(){
    	return Scene.v().getObjectType();
    }
    
    public HeapField getField(final SootField field){
		return _field;
	}
	
    public HeapField getField(){
		return _field;
	}
    
	public HeapField[] getAllFields(){
	    HeapField[] fields = new HeapField[1];
	    fields[0] = _field;
	    return fields;
	}	
	
    public String toString(){
    	return "*";
    }
}