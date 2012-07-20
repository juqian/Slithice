package jqian.sootex.location;

import soot.Scene;
import soot.Type;
import soot.Value;


public class ConstLocation extends Location{
	private Value _value;
	
	public ConstLocation(Value value){
		this._value = value;
	}
 
	public Type getType() {		 
		return _value.getType();
	}
	
	public String toString(){
		return "constant";
	}
}

class NullConstLocation extends ConstLocation{	
	static NullConstLocation _instance = new NullConstLocation();
	static NullConstLocation v(){
		return _instance;
	}
	
	private NullConstLocation(){
		super(null);
	}
 
	public Type getType() {		 
		return Scene.v().getObjectType();
	}
	
	public String toString(){
		return "null";
	}
}

