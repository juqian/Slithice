package jqian.sootex.dependency.pdg;

import soot.*;

/**
 *
 *	Model the method arguments
 */
public abstract class ActualNode extends DependenceNode{
	/**
	 * @param binding  The binding object can be a Location, a SootField or an ArrayType
	 */
	protected ActualNode(MethodOrMethodContext mc,Unit invoke,SootMethod callee,Object binding,Object formalNodeBinding){
		super(mc);
		this._binding = binding;
		this._callsite = invoke;
		this._callee = callee;
		this._formalNodeBinding = formalNodeBinding;
	}
	 
	
	public Unit getCallSite(){
		return _callsite;
	}
	
	public Object getFormalBinding(){
		return _formalNodeBinding;
	}

	public Object getBinding() {
		return _binding;
	}
	
	public SootMethod getCallee(){
		return _callee;
	}

	public String toString() {		
		return _binding.toString();
	}
	
	protected Object _binding;       //the binding can be a Location or a SootField
	protected Unit _callsite;        
	protected Object _formalNodeBinding;      //corresponding parameter    
	protected SootMethod _callee;
}
