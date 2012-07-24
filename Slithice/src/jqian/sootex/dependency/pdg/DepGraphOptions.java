package jqian.sootex.dependency.pdg;

import jqian.sootex.location.HeapAbstraction;

/**
 * Options for PDG construction
 */
public class DepGraphOptions {
	private final boolean _withCtrlDep;
    private final boolean _withDepReason;    
    private final HeapAbstraction _heapAbstraction;
    
    /**
     * @param withCtrlDep   Whether to construct control dependences
     * @param withDepReason  Whether to distinguish data dependence edges by depended heap locations
     * @param locAbstractionForInterface  The heap location abstraction used to construct formal and actual -in/-out nodes.
     */
    public DepGraphOptions(boolean withCtrlDep, boolean withDepReason, HeapAbstraction locAbstractionForInterface){
    	this._withCtrlDep = withCtrlDep;
    	this._withDepReason = withDepReason;
        this._heapAbstraction = locAbstractionForInterface;
    }
    
    /** choose whether to build the control dependence edge*/
    public final boolean withCtrlDependence(){
    	return _withCtrlDep;
    }
    
    /** choose whether to distinguish dependence reason in dependence graph construction. 
     *  Distinguishing them may result in too large PDG, but it will benefit program slicing. 
     *  XXX: all data dependences caused by stack variables will still be distinguished
     */
    public final boolean withDependReason(){
    	return _withDepReason; 
    }
    
    /** The heap location abstraction used to construct formal and actual -in/-out nodes. */
    public final HeapAbstraction getInterfaceLocationAbstraction(){
    	return  _heapAbstraction;
    }
}
