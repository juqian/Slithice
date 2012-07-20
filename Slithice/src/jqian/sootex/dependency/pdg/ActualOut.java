package jqian.sootex.dependency.pdg;

import jqian.sootex.location.*;
import jqian.sootex.util.SootUtils;
import soot.*;

/**
 *
 */
public class ActualOut extends ActualNode {
	public ActualOut(MethodOrMethodContext mc,Unit callsite,SootMethod callee,Object binding,Object fm) {
		super(mc,callsite,callee,binding,fm);		
	}

	public Object clone() {		
		return new ActualOut(_mc,_callsite,_callee,_binding,_formalNodeBinding);		
	}
	
	public String toString() {	
		String fmName = "";
		if(_formalNodeBinding!=null){
			if(_formalNodeBinding instanceof MethodRet){
				fmName = "RET";
			}
			else if(_binding instanceof HeapLocation || _binding instanceof GlobalLocation
					|| _binding instanceof SootField || _binding instanceof Type){
				fmName = "...";
			}
			else{
				fmName = _formalNodeBinding.toString();
			}
		}
		
		String actual = "";
		if(_binding instanceof SootField){
			actual = SootUtils.getShortFieldString((SootField)_binding);
		}
		else if(_binding instanceof Type){
			actual = _binding.toString();//+"[x]";
		}
		else{
			actual = _binding.toString();
		}
		
		String out = "#"+_id+" AO "+actual+"="+fmName;
		return out;
	}
}
