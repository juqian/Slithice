package jqian.sootex.dependency.pdg.builder;

import java.util.*;

import jqian.sootex.location.ArrayElmt;
import jqian.sootex.location.CommonInstObject;
import jqian.sootex.location.GlobalLocation;
import jqian.sootex.location.HeapField;
import jqian.sootex.location.InvokeInfo;
import jqian.sootex.location.Location;
import jqian.sootex.location.HeapAbstraction;
import jqian.sootex.location.MethodRet;
import jqian.sootex.location.StackLocation;
import jqian.sootex.ptsto.IPtsToQuery;
import jqian.sootex.util.CFGEntry;
import jqian.sootex.util.CFGExit;
import jqian.sootex.util.callgraph.Callees;

import jqian.sootex.dependency.pdg.ActualIn;
import jqian.sootex.dependency.pdg.ActualNode;
import jqian.sootex.dependency.pdg.ActualOut;
import jqian.sootex.dependency.pdg.CallNode;
import jqian.sootex.dependency.pdg.CtrlDependenceEdge;
import jqian.sootex.dependency.pdg.DependenceEdge;
import jqian.sootex.dependency.pdg.DependenceNode;
import jqian.sootex.dependency.pdg.FormalNode;
import jqian.sootex.dependency.pdg.FormalOut;
import jqian.sootex.dependency.pdg.FormalIn;
import jqian.sootex.dependency.pdg.JimpleStmtNode;
import jqian.sootex.dependency.pdg.DepGraphOptions;
import jqian.sootex.du.IReachingDUQuery;
import soot.*;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.*;
import soot.toolkits.graph.*;

 
/**
 * A PDG builder with side-effect of the calls under consideration.
 */
public class PDGBuilder extends AbstractPDGBuilder {
    protected SDGBuilder _sdgBuilder; 
    private BindingObjectCollector _bindingCollector;
    
    public PDGBuilder(SootMethod m, UnitGraph cfg, DepGraphOptions dgOptions,
    				IPtsToQuery ptsTo, HeapAbstraction heapAbstraction, IReachingDUQuery rd,
    		          SDGBuilder sdgBuilder){ 
    	super(m, cfg, dgOptions, ptsTo, heapAbstraction, rd); 
    	
    	this._sdgBuilder = sdgBuilder;   
    	this._bindingCollector = getBindingObjectCollector(_pdgOptions.getInterfaceLocationAbstraction());
    }  
    
    /** Use a binding object to connect the formals and the actuals. */
    static abstract class BindingObjectCollector{
    	public abstract Object getBindingForActualAndFormal(Location loc);
    	
    	public Collection<Object> getBindingSet(Collection<Location> locs){
    		Set<Object> bindingSet = new HashSet<Object>();        		
    		for(Location loc: locs){         			
    			Object binding = getBindingForActualAndFormal(loc);    				
    			bindingSet.add(binding);
            } 
    		
    		return bindingSet;
    	}
    }
    
    static class FieldSensitiveBindingObjectCollector extends BindingObjectCollector{
        public Object getBindingForActualAndFormal(Location loc){        	
    		return loc;
        }
        
    	@SuppressWarnings({ "unchecked", "rawtypes" })
		public Collection<Object> getBindingSet(Collection<Location> locs){
    		return (Collection)locs;
    	}   
    }
    
    static class FieldBasedBindingObjectCollector extends BindingObjectCollector{
        public Object getBindingForActualAndFormal(Location loc){
        	Object binding = null;
        	if(loc instanceof StackLocation || loc instanceof GlobalLocation || loc instanceof MethodRet){
    			binding = loc;
    		}
        	else if(loc instanceof HeapField){        		 
    			binding = ((HeapField)loc).getField();
    		}
    		else if(loc instanceof ArrayElmt){
    			//binding = loc.getType();
    			binding = loc.getType().makeArrayType();
    		}
    		else if(loc instanceof Location.TypeLocation){
    			// can not build field-based summary edges on type-based heap abstraction
    			binding = loc;
    		}
    		else{
    			throw new RuntimeException("Unknown locations");
    		}
    		
    		return binding;
        }
    }

    static class TypeBasedBindingObjectCollector extends BindingObjectCollector{
        public Object getBindingForActualAndFormal(Location loc){
        	Object binding = null;
        	if(loc instanceof StackLocation || loc instanceof MethodRet){
    			binding = loc;
    		}
        	else if(loc instanceof GlobalLocation){
        		binding = ((GlobalLocation)loc).getSootField().getDeclaringClass().getType();
        	}
        	else if(loc instanceof HeapField){
        		HeapField f = (HeapField)loc;
        		CommonInstObject obj = (CommonInstObject)f.getWrapperObject();
        		if(obj==null){
        			// in field-based analysis
        			binding = f.getField().getDeclaringClass().getType();
        		}
        		else{
        			binding = obj.getType();
        		}    			
    		}
    		else if(loc instanceof ArrayElmt){
    			binding = loc.getType().makeArrayType();
    		}
    		else if(loc instanceof Location.TypeLocation){
    			binding = loc;
    		}
    		else{
    			throw new RuntimeException("Unknown locations");
    		}    		 
    		
    		return binding;
        }
    }
    
    static class HeapUndistinguishedBindingObjectCollector extends BindingObjectCollector{
    	Type objectType = Scene.v().getObjectType();
    	Type objectArrayType = objectType.makeArrayType();
    	
        public Object getBindingForActualAndFormal(Location loc){
        	Object binding = null;
        	if(loc instanceof StackLocation || loc instanceof MethodRet){
    			binding = loc;
    		}
        	else if(loc instanceof GlobalLocation){
        		binding = objectType;
        	}
        	else if(loc instanceof HeapField){
    			binding = objectType;
    		}
    		else if(loc instanceof ArrayElmt){
    			binding = objectArrayType;
    		}
    		else if(loc instanceof Location.TypeLocation){
    			Location.TypeLocation tloc = (Location.TypeLocation)loc;
    			Type t = tloc.getObjectType();
    			if(t instanceof ArrayType){
    				binding = objectArrayType;
    			}
    			else{
    				binding = objectType;
    			}
    		}
    		else{
    			throw new RuntimeException("Unknown locations");
    		}
    		
    		return binding;
        }
    }
    
    private BindingObjectCollector getBindingObjectCollector(HeapAbstraction locAbstraction){
    	BindingObjectCollector bindingCollector = null;
    	if(locAbstraction==HeapAbstraction.FIELD_SENSITIVE){
    		bindingCollector = new FieldSensitiveBindingObjectCollector();    	 
		}
    	else if(locAbstraction==HeapAbstraction.FIELD_BASED){
    		bindingCollector = new FieldBasedBindingObjectCollector();    		 
    	}
    	else if(locAbstraction==HeapAbstraction.TYPE_BASED){
    		bindingCollector = new TypeBasedBindingObjectCollector();
    	}
    	else if(locAbstraction==HeapAbstraction.NO_DISTINGUISH){
    		bindingCollector = new HeapUndistinguishedBindingObjectCollector();
    	}
    	
    	return bindingCollector;
    }
    
    private void buildFormalForGlobals(){
    	Collection<Location> modGlobals = _sdgBuilder.getTgtModGlobals(_method);
    	Collection<Object> bindingSet = _bindingCollector.getBindingSet(modGlobals);
    	for(Object binding: bindingSet){        	
    		FormalNode node = new FormalOut(_method,binding);
    		_pdg.addNode(node);
    	}  
    	
    	Collection<Location> useGlobals =  _sdgBuilder.getTgtUsedGlobals(_method); 
    	bindingSet = _bindingCollector.getBindingSet(useGlobals);
    	for(Object binding: bindingSet){
    		FormalNode node = new FormalIn(_method, binding, FormalIn.HEAP_INDEX);
    		_pdg.addNode(node);
    	}    	
    }
        
    private void buildFormalForHeaps(){ 
    	Collection<Location> modHeaps = _sdgBuilder.getTgtModHeapLocs(_method);
    	Collection<Location> useHeaps = _sdgBuilder.getTgtUsedHeapLocs(_method);  
    	
    	Collection<Object> bindingSet = _bindingCollector.getBindingSet(modHeaps);
        for(Object binding: bindingSet){        	 
        	FormalNode node = new FormalOut(_method,binding);
           	_pdg.addNode(node);
        } 
        	
    	bindingSet = _bindingCollector.getBindingSet(useHeaps);
        for(Object binding: bindingSet){
        	FormalNode node = new FormalIn(_method,binding,FormalIn.HEAP_INDEX);
        	_pdg.addNode(node);
        }
    }

    protected void buildFormals(){
    	// formal out for the returns
    	if(!(_method.getReturnType() instanceof VoidType)){
    		FormalNode node = new FormalOut(_method,Location.methodToRet(_method));
    		_pdg.addNode(node);
    	}    	
    	
    	//formal ins for parameters 
    	int size = _method.getParameterCount();
    	Body body = _method.getActiveBody();
    	for(int i=0; i<size;i++){   
    		Local p = body.getParameterLocal(i);               
            Location param = Location.valueToLocation(p);
    		FormalNode node = new FormalIn(_method,param,i);
    		_pdg.addNode(node);
    	}
    	
    	//formal in for the receiver
    	if(!_method.isStatic()){
    		Location loc = Location.getThisPointer(_method);
    		FormalNode node = new FormalIn(_method,loc,FormalIn.THIS_INDEX);
    		_pdg.addNode(node);    		
    	}
    	
    	buildFormalForGlobals();
    	buildFormalForHeaps();
    }
    
    protected void buildFormalInDependences(){
    	DependenceNode entry = _pdg.entry();
    	
    	Collection<FormalNode> ins = _pdg.getFormalIns();
    	for(FormalNode f: ins){    	 	                
             DependenceEdge edge = new CtrlDependenceEdge(entry, f);
			_pdg.addEdge(edge);  
    	}
    }
    
    protected void buildFormalOutDependences(){    	
    	DependenceNode entry = _pdg.entry();
    	Collection<FormalNode> outs = _pdg.getFormalOuts();
    	
    	// build control dependence
    	for(FormalNode f: outs){    	
             DependenceEdge edge = new CtrlDependenceEdge(entry, f);
			_pdg.addEdge(edge);  
    	}
    	
    	// build data-flow dependences
    	HeapAbstraction locAbstraction  = _pdgOptions.getInterfaceLocationAbstraction();
    	if(locAbstraction==HeapAbstraction.FIELD_SENSITIVE){
    		for(FormalNode f: outs){        
    			Location loc = (Location)f.getBinding();
    			buildDepForLocation(CFGExit.v(),loc,f);			
        	}
    	}
    	else{    		 
    		// dependence for method return
    		if(!(_method.getReturnType() instanceof VoidType)){
        		Location loc = Location.methodToRet(_method);
        		Object binding = _bindingCollector.getBindingForActualAndFormal(loc);
        		FormalNode f = _pdg.getBindingFormal(binding, false);
        		buildDepForLocation(CFGExit.v(),loc,f);        		
        	}
    		
    		Collection<Location> modHeaps = _sdgBuilder.getTgtModHeapLocs(_method); 
    		for(Location loc: modHeaps){ 
    			Object binding = _bindingCollector.getBindingForActualAndFormal(loc);    		 				
    			FormalNode f = _pdg.getBindingFormal(binding, false);
    			buildDepForLocation(CFGExit.v(),loc,f);   
    		}
    		
    		Collection<Location> modGlobals = _sdgBuilder.getTgtModGlobals(_method);  
    		for(Location loc: modGlobals){   
    			Object binding = _bindingCollector.getBindingForActualAndFormal(loc);  
    			FormalNode f = _pdg.getBindingFormal(binding, false);    			
    			buildDepForLocation(CFGExit.v(),loc,f);
    		}
    	}    	
    }
    
    @Override
    protected void buildNodesForCall(Unit callsite){    
    	//also build a unique node to link all possible calls
    	JimpleStmtNode node=new JimpleStmtNode(_method,callsite);
        _pdg.addNode(node);  
        
        //We do not build call nodes for callsites whose callee are discarded during call graph simplification
 	    CallGraph cg = Scene.v().getCallGraph(); 	    
 	    Callees callees = new Callees(cg,callsite);	
 	    for(SootMethod tgt: callees.explicits()){
 	    	//add call node
 	    	CallNode call = new CallNode(_method,callsite,tgt);
 	    	_pdg.addNode(call);
 	    	
 	    	//add actual nodes
 	    	buildActuals(call,tgt,callsite);
 	    	
 	    	//add control dependence
 	    	 DependenceEdge edge = new CtrlDependenceEdge(node, call);
 			_pdg.addEdge(edge);  
 	    }    
    }
    
    
    private void buildActualsForLocalParameters(CallNode call,SootMethod callee,Unit callsite, Collection<ActualNode> list){
    	boolean concrete = callee.isConcrete(); 
    	InvokeInfo info = new InvokeInfo((Stmt)callsite);
    	// if have a return and the return value is used    	
    	if(info.getRetLoc()!=null){    	 
    		Location ret = null;
    		if(concrete) 
    			ret = Location.methodToRet(callee);
    		
    		ActualOut node = new ActualOut(_method, callsite, callee, info.getRetLoc(), ret);
			_pdg.addNode(node);
			list.add(node);
    	}
    	
    	// explicit parameter node
    	Location[] args = info.getArgLocs();    	
    	int size = args.length;
    	for(int i=0;i<size;i++){
    		Location actual = args[i];
    		if(actual!=null){
    			Location formal = null;
        		if(concrete){
        			Local p = callee.getActiveBody().getParameterLocal(i);               
        	        formal = Location.valueToLocation(p);
        		} 
        		
        		ActualIn node = new ActualIn(_method,callsite,callee,actual,formal);
        		_pdg.addNode(node);
        		list.add(node);
    		}
    	}
    	
    	// this pointer
    	if(!callee.isStatic()){
    		Location formal = null;
    		if(concrete)
    			formal = Location.getThisPointer(callee);
    		 
    		Location actual = info.receiver(); 
    		ActualIn node = new ActualIn(_method,callsite,callee,actual,formal);
    		_pdg.addNode(node);
    		list.add(node);;
    	}
    }
    
    private void buildActualsForGlobals(CallNode call,SootMethod callee,Unit callsite, Collection<ActualNode> list){
    	Collection<Location> modGlobals =  _sdgBuilder.getTgtModGlobals(callee);
    	Collection<Object> bindingSet = _bindingCollector.getBindingSet(modGlobals);	
		for(Object binding: bindingSet){           
			ActualOut node = new ActualOut(_method, callsite, callee, binding,binding);
			_pdg.addNode(node);
			list.add(node);
        } 
		
		Collection<Location> useGlobals = _sdgBuilder.getTgtUsedGlobals(callee);
		bindingSet = _bindingCollector.getBindingSet(useGlobals);	
		for (Object binding: bindingSet) {				
			ActualIn node = new ActualIn(_method, callsite, callee, binding, binding);
			_pdg.addNode(node);
			list.add(node);
		}
    }
    	
    
    private void buildActualsForHeaps(CallNode call,SootMethod callee,Unit callsite, Collection<ActualNode> list){
    	Collection<Location> modHeaps = _sdgBuilder.getTgtModHeapLocs(callee);
    	Collection<Location> useHeaps = _sdgBuilder.getTgtUsedHeapLocs(callee);
    	 
    	// build actual -in/-out nodes for each binding object
		Collection<Object> bindingSet = _bindingCollector.getBindingSet(modHeaps);		
		for(Object binding: bindingSet){
			ActualOut node = new ActualOut(_method, callsite, callee, binding, binding);
			_pdg.addNode(node);
			list.add(node);
        }
		
		bindingSet = _bindingCollector.getBindingSet(useHeaps);		 
		for (Object binding: bindingSet) {
			ActualIn node = new ActualIn(_method, callsite, callee, binding, binding);
			_pdg.addNode(node);
			list.add(node);
		}
    }
    
    /** Build actual-in/-outs for method call. */
    private void buildActuals(CallNode call,SootMethod callee,Unit callsite){
    	Collection<ActualNode> list = new LinkedList<ActualNode>();    	
    	buildActualsForLocalParameters(call,callee,callsite, list);
    	
    	// TODO: need more consideration for native methods here
    	boolean concrete = callee.isConcrete(); 
    	// actual -in/-outs due to global locations and heap locations
    	if(concrete){
    		buildActualsForGlobals(call, callee, callsite, list);
    		buildActualsForHeaps(call, callee, callsite, list);        	
    	}
    	
    	//build control dependences
    	for(DependenceNode n: list){    	
    		DependenceEdge edge = new CtrlDependenceEdge(call,n);
 			_pdg.addEdge(edge);  
    	}
    }
    
    @Override
    protected Collection<DependenceNode> getDefinitionNodes(Unit stmt, Location loc){
    	Collection<DependenceNode> nodes = new LinkedList<DependenceNode>();
    	if(stmt==CFGEntry.v()){  
    		Object binding = _bindingCollector.getBindingForActualAndFormal(loc);
    		DependenceNode n = _pdg.getBindingFormal(binding, true);
    		nodes.add(n);
    	}
    	else{    		
    		if((stmt instanceof Stmt) && ((Stmt)stmt).containsInvokeExpr()){
    			CallGraph cg = Scene.v().getCallGraph(); 	    
    	 	    Callees callees = new Callees(cg,stmt);	
    	 	    
    	 	    //If all targets are tailed during call graph simplification
    	 	    if(callees.explicits().size()==0){
    	 	    	DependenceNode src = _pdg.getStmtBindingNode(stmt);
        			nodes.add(src);
    	 	    }
    	 	    else{
    	 	    	Object binding = _bindingCollector.getBindingForActualAndFormal(loc);
    	 	    	for(SootMethod tgt: callees.explicits()){        				
        				DependenceNode n=_pdg.getBindingActual(stmt,tgt, binding, false);	
        				if(n!=null)	nodes.add(n); 					
        			}  
    	 	    }
    		}
    		else{
    			DependenceNode src = _pdg.getStmtBindingNode(stmt);
    			nodes.add(src);
    		}
    	}
    	
    	return nodes;
    }
    
    /** 
     * Just treat the call as a common statement. 
     * (Can only define value by method return. No extra side-effects) 
     */
    private void buildDepForTailoredCallee(InvokeInfo invoke, DependenceNode dest){
    	Unit invokeStmt = invoke.getInvokeStmt();
    	Location[] args = invoke.getArgLocs();
    	int count = args.length;
        for(int i=0 ;i<count; i++){
            Location arg = args[i];
            if(arg!=null && arg instanceof StackLocation){	            
            	buildDepForLocation(invokeStmt,arg,dest);
            }
        }
        
        if(!invoke.getInvokeExpr().getMethod().isStatic()){	          
            Location receiver = invoke.receiver();
            if(receiver instanceof StackLocation) 
            	buildDepForLocation(invokeStmt,receiver,dest);	            	 
        }
    }
   
    
    /** 
     * Safely handle the method invocation. Consider the side effect of each callee.  
     * @param dest The node that represent the call statement
     */
    protected void buildDepForInvoke(Unit curStmt, DependenceNode dest){
    	InvokeInfo invoke = new InvokeInfo((Stmt)curStmt);
	    Unit invokeStmt=invoke.getInvokeStmt();
	    CallGraph cg = Scene.v().getCallGraph(); 		    
	    
	    //FIXED 2008-07-13: Here we only consider explicit callees
	    /*
	    Set processedTgts = new HashSet();
	    Iterator edgeIt = cg.edgesOutOf(invokeStmt);	
	    while(edgeIt.hasNext()){	    	 
	    	Edge edge = (Edge)edgeIt.next();
	    	SootMethod tgt = edge.tgt();
	    	if(processedTgts.contains(tgt))
	    		continue;
	    	
	    	processedTgts.add(tgt);
	    */
	    	
	    Callees callees = new Callees(cg,invokeStmt);	
	    
	    //If the callees are tailed during call graph simplification
	    if(callees.explicits().size()==0){	    	 
	    	buildDepForTailoredCallee(invoke, dest);	        
	        return;
	    }
	    
	    for(SootMethod tgt: callees.explicits()){
	    	// dependence for arguments
	    	int pCount = tgt.getParameterCount();
	    	Location[] args = invoke.getArgLocs();
	        for(int i=0;i<pCount;i++){
	            Location arg = args[i];
	            if(arg!=null && arg instanceof StackLocation){
	            	buildDepForActualIn(dest,curStmt,tgt,arg);
	            }
	        }
	        
	        //make sure the dependence on recevier is kept
	        if(!invoke.getInvokeExpr().getMethod().isStatic()){	          
	            Location receiver = invoke.receiver();
	            if(receiver instanceof StackLocation) 
	            	buildDepForActualIn(dest,curStmt,tgt,receiver);
	        }
	       
	        //TODO need more consideration for native method
	        if(!tgt.isConcrete()){//side effect free or we just do not want to consider	            
	           continue;	           
	        }	     
	        
	        //Get dependence for side effected heap locations 
	        //handle ordinary calls
	        Collection<Location> useHeaps = _sdgBuilder.getTgtUsedHeapLocs(tgt);	       
	        for(Location loc: useHeaps){	    	    
	    	    buildDepForActualIn(dest,curStmt,tgt,loc);
	        }
	        
	        Collection<Location> useGlobals = _sdgBuilder.getTgtUsedGlobals(tgt);
	        for(Location loc: useGlobals){	    	    
	    	    buildDepForActualIn(dest,curStmt,tgt,loc);
	        }
	    }
    }
    
    private void buildDepForActualIn(DependenceNode dest,Unit callsite,SootMethod callee,Location loc){   
    	Object binding = _bindingCollector.getBindingForActualAndFormal(loc);
    	ActualNode in = _pdg.getBindingActual(callsite, callee, binding, true);    	
        buildDepForLocation(callsite,loc,in);
    }
}
