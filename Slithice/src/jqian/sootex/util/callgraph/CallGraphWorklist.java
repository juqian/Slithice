package jqian.sootex.util.callgraph;

import java.util.*;

import jqian.sootex.util.SootUtils;
import soot.*;
import soot.jimple.toolkits.callgraph.*;


/**
 * A framework for worklist algorithm on the call graph
 */
public class CallGraphWorklist {
    private final MethodVisitor _visitor;    
    private final CallGraph _cg;
    private List<SootMethod> _topoOrder;
    
	/** 
	 * Note that when visiting a method at the first time, the return of visit() 
	 * should better be true, given the data of m is not empty. 
	 */	
    public static interface MethodVisitor{
    	/** return if the analysis result of method m has been changed. */ 
        public boolean visit(SootMethod m, Set<SootMethod> changeSources);  
    }
    
    public CallGraphWorklist(CallGraph cg, MethodVisitor visitor){
    	this._cg = cg;
    	this._visitor=visitor;
    	
    	TopologicalOrderer orderer = new TopologicalOrderer(cg);
        orderer.go();         
    	this._topoOrder = orderer.order(); 
    }
    
    public CallGraphWorklist(CallGraph cg, MethodVisitor visitor,List<SootMethod> topoOrder){
    	this._cg = cg;
    	this._visitor = visitor;
    	this._topoOrder = topoOrder;
    }
    
    public CallGraphWorklist(CallGraph cg, MethodVisitor visitor,Collection<SootMethod> entries){
        this._visitor=visitor;
        this._cg = cg;
        
	    assert(entries!=null);
        TopologicalOrderer orderer = new PartialTopologicalOrderer(cg,entries);
        orderer.go();
        _topoOrder = orderer.order();         
    }
    
    private static class MethodComparator implements Comparator<SootMethod>{
    	private int[] _order;
    	public MethodComparator(int[] order){
    		 this._order = order;
    	}
    	
    	public int compare(SootMethod m1, SootMethod m2){    		
    		 return _order[m1.getNumber()] - _order[m2.getNumber()];
    	}
    	 
        public boolean equals(Object obj){
        	if(obj==this)
    		   return true;
    		else
    		   return false; 
    	 }
    }
    
    
    @SuppressWarnings("unchecked")
	public void process(){ 
        //get an order number for each method
        int[] order = new int[SootUtils.getMethodCount()];
		int i = 1;
		for(Iterator<SootMethod> it=_topoOrder.iterator();it.hasNext();i++){
		   SootMethod m = it.next();
		   order[m.getNumber()] = i;
		}
        
		//initialize the queue
		Comparator<SootMethod> comparator = new MethodComparator(order);
		PriorityQueue<SootMethod> worklist = new PriorityQueue<SootMethod>(_topoOrder.size(), comparator); 
		worklist.addAll(_topoOrder);
		
		//set change source
        Map<SootMethod, Set<SootMethod>> inque = new HashMap<SootMethod, Set<SootMethod>>(_topoOrder.size()); 
        for(SootMethod m: _topoOrder){ 		 
 		   Callees callees = new Callees(_cg,m); 		   
 		   inque.put(m, callees.explicits());
 		}
        
        //worklist processing
        while(!worklist.isEmpty()){
        	SootMethod m = worklist.poll();
        	Set<SootMethod> changeSources = inque.get(m);
        	inque.remove(m);        	
        	
        	boolean changed = _visitor.visit(m,changeSources);
            if(changed){
                Iterator<SootMethod> it = new Sources(_cg.edgesInto(m));
                while(it.hasNext()){
                	SootMethod caller = it.next();
                	int id = caller.getNumber();
                	
                	//if originally reachable               	
                	if(order[id]>0){
                		changeSources = inque.get(caller);
                		//if not in worklist
                		if(changeSources==null){
                			worklist.offer(caller);
                			changeSources = new TreeSet<SootMethod>(comparator);
                			inque.put(caller,changeSources);
                		}
                	                		
                		changeSources.add(m);
                	}
                }
            }           
        }
    }
}
