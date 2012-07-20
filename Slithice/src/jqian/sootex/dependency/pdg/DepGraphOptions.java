package jqian.sootex.dependency.pdg;

import jqian.sootex.location.HeapAbstraction;

/**
 * Options for PDG construction
 */
public class DepGraphOptions {
	private final boolean _withCtrlDep;
    private final boolean _withDepReason;    
    private final HeapAbstraction _locAbstraction;
    
    /**
     * 
     * @param withCtrlDep   Whether to include control dependence
     * @param withDepReason  Whether to distinguish dependence edge by depended heap location
     * @param locAbstractionForInterface  The heap location abstraction used to construct formal and actual -in/-out nodes.
     */
    public DepGraphOptions(boolean withCtrlDep, boolean withDepReason, HeapAbstraction locAbstractionForInterface){
    	this._withCtrlDep = withCtrlDep;
    	this._withDepReason = withDepReason;
        this._locAbstraction = locAbstractionForInterface;
    }
    
    /** choose whether to build the control dependence edge*/
    public boolean withCtrlDependence(){
    	return _withCtrlDep;
    }
    
    /** choose whether to distinguish dependence reason in dependence graph construction. 
     *  Distinguishing them may result in too large PDG, but it will benefit program slicing. 
     *  XXX: all data dependences caused by stack variables will still be distinguished
     */
    public boolean withDependReason(){
    	return _withDepReason; 
    }
    
    /** The heap location abstraction used to construct formal and actual -in/-out nodes. */
    public HeapAbstraction getInterfaceLocationAbstraction(){
    	return  _locAbstraction;
    }
}
