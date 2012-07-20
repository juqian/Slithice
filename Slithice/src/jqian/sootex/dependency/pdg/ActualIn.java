package jqian.sootex.dependency.pdg;

import jqian.sootex.location.GlobalLocation;
import jqian.sootex.location.HeapLocation;
import soot.*;


public class ActualIn extends ActualNode {
	public ActualIn(MethodOrMethodContext mc,Unit callsite,SootMethod callee,
			        Object binding,Object formalBinding){
		super(mc,callsite,callee,binding,formalBinding);
	}
	
	 
	public Object clone() {			 
		return new ActualIn(_mc,_callsite,_callee,_binding,_formalNodeBinding);		 
	}
	
	public String toString() {
		String fmName = "";
		String argName = "";
		if(_formalNodeBinding!=null){
			fmName = FormalNode.getBindingName(_formalNodeBinding);	
		}
		
		if(_binding instanceof HeapLocation || _binding instanceof GlobalLocation 
		   || _binding instanceof SootField || _binding instanceof Type){
			argName = "...";
		}
		else{
			argName = _binding.toString();
		}
		
		String out = "#"+_id+" AI "+fmName+"="+argName;
		return out;
	}
}
