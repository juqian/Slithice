package jqian.sootex.location;

import soot.*;

public class HeapField extends HeapLocation {
	final SootField _field;
	final InstanceObject _father;
	
	public HeapField(InstanceObject hObj, SootField sfld) {
		this._father = hObj;
		this._field = sfld;
	}
    
	public InstanceObject getWrapperObject() {
		return _father;
	}

	public SootField getField() {
		return _field;
	}

	public Type getType() {
		return _field.getType();
	}

	public String toString() {
		String s = (_father!=null)? _father.toString() : "*";
		s += ".";
		s += (_field!=null)?_field.getName(): "*";		
		return  s; 
	}
}
