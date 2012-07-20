package jqian.sootex.dependency.pdg;

import soot.*;

/**
 * 
 */
public class FormalOut extends FormalNode {   
	public FormalOut(MethodOrMethodContext mc,Object binding){
		super(mc,binding);		
	}
	
	public Object clone() {	
		return new FormalOut(_mc,_binding);
	}
	
	public String toString(){		
	  	String name = getBindingName();
	   	return "#"+_id+" FO "+name;
	}
}
