package jqian.sootex.dependency.pdg;

import jqian.sootex.location.MethodRet;
import jqian.sootex.util.SootUtils;
import soot.*;

public abstract class FormalNode extends DependenceNode {
	/**
	 * @param binding   The binding object can be a location, a SootField or an ArrayType
	 */
	protected FormalNode(MethodOrMethodContext mc,Object binding){
		super(mc);
		this._binding = binding;
	}
	
	protected Object _binding;

	public Object getBinding() {
		return _binding;
	}

	public static String getBindingName(Object binding){
		if(binding instanceof MethodRet){
			return "RET";
		}
		else if(binding instanceof SootField){
			return SootUtils.getShortFieldString((SootField)binding);
		}
		else if(binding instanceof Type){
			return binding.toString();//+"[x]";
		}
		
		return binding.toString();
	}
	
	public String getBindingName(){
		return getBindingName(_binding);
	}
}