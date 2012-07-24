package jqian.sootex.dependency.pdg;

import java.util.*;

import jqian.Global;
import jqian.sootex.location.InvokeInfo;
import jqian.sootex.location.Location;
import jqian.sootex.location.MethodRet;
import jqian.sootex.util.SootUtils;
import jqian.sootex.util.callgraph.CallGraphHelper;
import jqian.sootex.util.callgraph.CallGraphWorklist;
import jqian.sootex.util.callgraph.Callees;
import jqian.sootex.util.graph.PathTable;
import soot.*;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.*;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.PseudoTopologicalOrderer;

/**
 * SDG connected by PDGs
 * Summary edges are also supported
 */
public class SDG implements DependenceGraph{
	private Map<DependenceNode,Collection<DependenceEdge>> _node2edgesIn = new HashMap<DependenceNode,Collection<DependenceEdge>>();
	private Map<SootMethod,Collection<SummaryEdge>> _mc2summaries = new HashMap<SootMethod,Collection<SummaryEdge>>();
	private Map<MethodOrMethodContext,PDG> _mc2pdg = new HashMap<MethodOrMethodContext,PDG>();   
	    
	/** Get a procedure's dependence graph  */
    public PDG getPDG(MethodOrMethodContext mc){    	
    	return (PDG)_mc2pdg.get(mc);    	
    }
    
	public Collection<PDG> getPDGs(){    	
    	return _mc2pdg.values();   	
    }
    
	public boolean addPDG(MethodOrMethodContext mc,PDG pdg){    	
    	if(_mc2pdg.get(mc)!=null)
    		return false;
    	
    	_mc2pdg.put(mc,pdg); 	
    	return true;
    }
    
    public int countEdge(int type){
    	return 0;
    }
    
    public Collection<DependenceEdge> edgesInto(DependenceNode n){
    	MethodOrMethodContext mc = n.getMethodOrMethodContext();
    	PDG pdg = getPDG(mc);
    	
    	if(n instanceof FormalIn || n instanceof ActualOut || n instanceof EntryNode){
			Collection<DependenceEdge> extras = _node2edgesIn.get(n);
			if(extras!=null){
				Collection<DependenceEdge> edges = new LinkedList<DependenceEdge>();
				edges.addAll(pdg.edgesInto(n));	
				edges.addAll(extras);
				return edges;
			}
		}
		
		return pdg.edgesInto(n);		
    }
    
    //support the traversing
    public Collection<DependenceNode> getNodes(){
    	throw new RuntimeException("Not yet implemented");
    }
    
    public int getEdgeCount(){
    	throw new RuntimeException("Not yet implemented");
    }
    
    public Collection<DependenceEdge> edgesOutOf(DependenceNode node){
    	throw new RuntimeException("Not yet implemented");
    }   
    
    /**Get a graph compatible with the DirectedGraph interface*/
    public DirectedGraph<DependenceNode> toDirectedGraph(){
    	throw new RuntimeException("Not yet implemented");
    }
    
    /** building connection between actuals and formals. */
    public void connectPDGs(){
    	Global.v().out.print("\n[SDG connect methods]");
    	
    	//connect actuals and formals
    	Collection<ActualNode> actuals = new LinkedList<ActualNode>();
    	for(PDG pdg: _mc2pdg.values()){    	 
    		pdg.getActualNodes(actuals);
    	}
    	
    	for(ActualNode actual: actuals){
			FormalNode formal = getFormal(actual);
			if(formal==null)
				continue;			
			
			DependenceEdge edge;
			if(actual instanceof ActualIn){
				Object binding = formal.getBinding();
				edge = new DataDependenceEdge(actual,formal,binding);				
				addExtraEdge(formal,edge);
			}
			else{
				Object binding = actual.getBinding();
				edge = new DataDependenceEdge(formal,actual,binding);				
				addExtraEdge(actual,edge);
			}	
    	}
    	
    	//connect entry nodes and call nodes    	
    	for(PDG pdg: _mc2pdg.values()){    	 
    		for(DependenceNode n: pdg.getNodes()){
    			if(n instanceof CallNode){
    				CallNode call = (CallNode)n;
    				SootMethod callee = (SootMethod)call.getBinding();
    				if(callee.isConcrete()){
    					PDG calleePdg = getPDG(callee);
        				DependenceNode entry = calleePdg.entry();
        				
        				DependenceEdge edge = new CtrlDependenceEdge(call,entry);
        				addExtraEdge(entry,edge);
    				}
    			}
    		}  
    	}
    	
    	Global.v().out.print(" done.");
    }
    
    /** 
     * Build summary edges for all methods reachable from the entry method. 
     */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void buildSummaryEdges(SootMethod entryMethod){
		Global.v().out.print("\n[SDG summary]");
		
		CallGraph cg = Scene.v().getCallGraph();
    	Collection<SootMethod> entries = new LinkedList<SootMethod>();
    	entries.add(entryMethod);
    	
    	DirectedGraph graph = SootUtils.getSCCGraph(cg, entries);
		PseudoTopologicalOrderer pto = new PseudoTopologicalOrderer();
		List order = pto.newList(graph, true);
			
		int total = 0;
		for(Iterator it=order.iterator();it.hasNext();){          
	       	Collection node = (Collection) it.next();
	       	total += node.size();
		}
		
		// bottom-up phase 
        int methodNum = 0;        
        int lastPercent = 0;
        for(Iterator it=order.iterator();it.hasNext();){          
        	Collection node = (Collection) it.next();
        	
        	if(node.size()==1){
        		SootMethod m = (SootMethod)node.iterator().next();
        		findSummary(m);
        	}
        	else{
        		// need iterative computation inside each strongly-connected-component 
        		Set component = new HashSet(node);
    			CallGraph subCg = CallGraphHelper.getSubCallGraph(cg, component);
        		CallGraphWorklist worklist = new CallGraphWorklist(subCg, new MyVisitor());
            	worklist.process();
        	}
        	
        	// compact summary edge representation
        	for(Object o: node){
        		SootMethod m = (SootMethod)o;
        		compactSummaries(m);
        		
        		PDG pdg = (PDG)_mc2pdg.get(m);
        		if(pdg!=null){
        			pdg.finalizeEdgeSets();
        		}
        	}        	
        	
        	methodNum += node.size();
        	
        	int newPercent = (100*methodNum)/total;
        	if(newPercent-lastPercent>5){
        		Global.v().out.print(" " + newPercent + "%");
        		lastPercent = newPercent;
        	}        	
        }
        
        Global.v().out.print(" <done>\n");
    }    
    
    /** Compact SDG representation. */
    public void compact(){
    	_mc2summaries = null;
    }
    
    private class MyVisitor implements jqian.sootex.util.callgraph.CallGraphWorklist.MethodVisitor{
		public boolean visit(SootMethod m,Set<SootMethod> changeSources){
    		Collection<SummaryEdge> summary = getSummaries(m);
    		int oldSummaries = summary.size();    		
    		findSummary(m);    		
    		int newSummaries = summary.size();  
    		
    		return (newSummaries!=oldSummaries);
    	}
    }
  
	/**
	 * Summarize the dependence between FormalIn(s) and FormalOut(s)
	 */
	@SuppressWarnings("unused")
	private void findSummary(SootMethod m){
		//FIXME: Here do not build summary for native methods
		if(!m.isConcrete()){    			
			return;
		}
		
		PDG pdg = (PDG)_mc2pdg.get(m);
		
		// No PDG for the method (We may not construct PDG for every method, especially for the library methods)
		if(pdg==null){
			return;
		}
		
		if(pdg.containsMethodBody()){
			Body body = m.getActiveBody();   
        	
        	//add summary for callees
        	CallGraph cg = Scene.v().getCallGraph();
        	for(Iterator<?> it=body.getUnits().iterator();it.hasNext();){
        		Stmt s = (Stmt)it.next();        		
        		if(!s.containsInvokeExpr()){
        			continue;
        		}
        			
        		// no binding node, the statement may be ignored in the CFG
        		if(pdg.getStmtBindingNode(s)==null){
        			continue;
        		}
        		
        		Callees callees = new Callees(cg,s);	
        	    for(SootMethod tgt: callees.explicits()){
        	    	 addSummaryForCall(pdg,s,tgt); 
        	    }
        	}
		}
		else{
			pdg.hashCode();
		}
		
		Collection<SummaryEdge> summaries = getSummaries(m);
    	//using PathTable to find the reachablity relations in the PDG   
    	//The same objective also can be implemented by LocalSlicer
    	if(false){
    		findSummaryEdgeByPathTable(pdg,summaries);
    	}
    	else{
    		findSummaryEdgeByGraphTraverse(pdg,summaries);
    	}
    }
	
	/**XXX The path table based approach seems to time-consuming. A PDG may have 10000 nodes. */
	protected void findSummaryEdgeByPathTable(PDG pdg,Collection<SummaryEdge> summaries){
		Collection<FormalNode> fIns = pdg.getFormalIns();
    	Collection<FormalNode> fOuts = pdg.getFormalOuts();
    	
    	PathTable tbl = new PathTable(pdg.toDirectedGraph());        	
    	for(FormalNode in: fIns){        		 
    		for(FormalNode out: fOuts){            	 
        		if(tbl.hasPath(in, out)){
        			Object binding = out.getBinding();
        			SummaryEdge edge = new SummaryEdge(in,out,binding);
        			summaries.add(edge);            			
        		}
        	}
    	}
	}
	
	
	protected void findSummaryEdgeByGraphTraverse(PDG pdg,Collection<SummaryEdge> summaries){    		 
    	Collection<FormalNode> fOuts = pdg.getFormalOuts();
    	for(FormalNode out: fOuts){ 
    		Set<DependenceNode> fIns = getReachableFormals(pdg, out);
    		for(DependenceNode in: fIns){
        		Object binding = out.getBinding();
        		SummaryEdge edge = new SummaryEdge(in,out,binding);
        		summaries.add(edge);  
        	}
    	}
	}
	
	// FIXME: Here to use BitVector to speed up analysis
	private Set<DependenceNode> getReachableFormals(PDG pdg, DependenceNode dest) {
		Set<DependenceNode> reach = new HashSet<DependenceNode>();
		Set<DependenceNode> formals = new HashSet<DependenceNode>();
		Stack<DependenceNode> stack = new Stack<DependenceNode>();    
		DependenceNode entry = pdg.entry();

		Collection<DependenceEdge> edges = pdg.edgesInto(dest);
		if (edges != null){
			for (DependenceEdge e: edges) {    				 
				stack.add(e.getFrom()); 
			}
		}			
		
		while (!stack.isEmpty()) {
			DependenceNode top = stack.pop();
			
			if (!reach.add(top)) {
				continue;
			}
			
			if(top instanceof FormalNode){
				formals.add(top);
				continue;
			}

			if(top==entry){
				continue;
			}    			

			edges = pdg.edgesInto(top);
			if (edges == null)
				continue;

			for (DependenceEdge e: edges) {  
				DependenceNode from = e.getFrom();
				if (!reach.contains(from)) { 
					stack.add(from);    					 
				}
			}
		}

		return formals;
	}
	
	private void addSummaryForCall(PDG pdg,Unit callsite,SootMethod callee){
    	InvokeInfo invoke = new InvokeInfo((Stmt)callsite);
    	if(!callee.isConcrete()){
    		//just assume the return value depends on each formal-in
    		Location ret = invoke.getRetLoc();
    		if(ret!=null){
    			ActualNode out = pdg.getBindingActual(callsite, callee, ret, false);
        		
        		Location[] args = invoke.getArgLocs();
        		int count = args.length;
        		for(int i=0;i<count;i++){
        			Location arg = args[i];
        			ActualNode in = pdg.getBindingActual(callsite, callee, arg, true);
        			SummaryEdge edge = new SummaryEdge(in,out,ret);
        			pdg.addEdge(edge);
        		}
        		
        		if(!callee.isStatic()){
        			Location thiz = invoke.receiver();
        			ActualNode in = pdg.getBindingActual(callsite, callee, thiz, true);
        			SummaryEdge edge = new SummaryEdge(in,out,ret);
        			pdg.addEdge(edge);
        		}    
    		}
    		// TODO: need to use native helper to find dependence between in parameter		
    	}
    	else{
    		Collection<SummaryEdge> summaries = getSummaries(callee);
    		Location[] args = invoke.getArgLocs();
    		for(SummaryEdge edge: summaries){        		 
    			FormalIn from = (FormalIn)edge.getFrom();
    			DependenceNode to = edge.getTo();
    			
    			Object inBinding = null;
    			Object outBinding = null;
    			{
    				int paramIdx = from.getParamIndex();
					if (paramIdx >= 0) {
						inBinding = args[paramIdx];
					} else if (paramIdx == FormalIn.THIS_INDEX) {
						inBinding = invoke.receiver();
					}
					else{
						inBinding =  from.getBinding();
					}
					
					Object obj = to.getBinding();
					if(obj instanceof MethodRet){
						outBinding = invoke.getRetLoc();
						//if method return is not used, then just continue;
	        			if(outBinding==null)
	        				continue;
					}
					else{
						outBinding = obj;
					}
    			}   
    			
    			if(inBinding==null || outBinding==null){
    				throw new RuntimeException("Strange Error");
    			}
  				
    			ActualNode actualIn = pdg.getBindingActual(callsite, callee, inBinding, true);
    			ActualNode actualOut = pdg.getBindingActual(callsite, callee, outBinding, false); 
    			
    			edge = new SummaryEdge(actualIn,actualOut,outBinding);        			
        		pdg.addEdge(edge); 
    		}
    	}	
    }
  
    
    /** Get the corresponding FormalNode of a ActualNode. 
     * @return The result can be null when the callee is not concrete.
     */
    public FormalNode getFormal(ActualNode actual){    	
    	Object binding = actual.getFormalBinding();
    	if(binding==null)
    		return null;
    	
    	SootMethod callee = actual.getCallee();
    	if(!callee.isConcrete()){
    		return null;
    	}
    	
    	PDG pdg = getPDG(callee);    	
    	
    	FormalNode formal;
    	if(actual instanceof ActualIn){
    		formal = pdg.getBindingFormal(binding, true);
    	}
    	else{
    		formal = pdg.getBindingFormal(binding, false);
    	}
    	
    	return formal;
    }
    
    private Collection<SummaryEdge> getSummaries(SootMethod m){
    	Collection<SummaryEdge> summaries = _mc2summaries.get(m);
    	if(summaries==null){
    		summaries = new HashSet<SummaryEdge>();
    		_mc2summaries.put(m, summaries);
    	}
    	
    	return summaries;
    }
    
    private void compactSummaries(SootMethod m){
    	Collection<SummaryEdge> summaries = _mc2summaries.get(m);
    	if(summaries!=null){
    		summaries = new ArrayList<SummaryEdge>(summaries);
    		_mc2summaries.put(m, summaries);
    	}
    }
    
    /*public Collection getBindingActuals(FormalNode fm){
    	Collection actuals = (Collection)_formal2actuals.get(fm);
    	if(actuals==null){
    		actuals = new LinkedList();
    		_formal2actuals.put(fm, actuals);
    		
    		CallGraph cg = Scene.v().getCallGraph();
        	MethodOrMethodContext mc = fm.getMethodOrMethodContext();
        	for(Iterator it=cg.edgesInto(mc);it.hasNext();){
        		Edge edge = (Edge)it.next();
        		
        		SootMethod m = edge.src();
        		PDG pdg = getPDG(m);
        		
        		boolean isIn = (fm instanceof FormalIn);
        		ActualNode an = pdg.getBindingActual(edge.srcUnit(),(SootMethod)mc,(Location)fm.getBinding(),isIn);
        		actuals.add(an);
        	}
    	}
    	
    	return actuals;    	
    }*/
    
    private void addExtraEdge(DependenceNode node,DependenceEdge e){
    	Collection<DependenceEdge> edges = _node2edgesIn.get(node);
    	if(edges==null){
    		edges = new ArrayList<DependenceEdge>();
    		_node2edgesIn.put(node, edges);
    	}
    	
    	edges.add(e);
    }
    
    
    /**Change to the PDG with .java line as node*/
    public DependenceGraph toJavaStmtDepGraph(){
    	SDG sdg = new SDG();
    	
    	Map<DependenceNode,DependenceNode> old2New = new HashMap<DependenceNode,DependenceNode>();    	
    	for(Map.Entry<MethodOrMethodContext, PDG> entry: _mc2pdg.entrySet()){    	 
    		MethodOrMethodContext mc = entry.getKey();
    		PDG pdg = entry.getValue();    		
    		
    		PDG javaPDG = pdg.toJavaStmtDepGraph(old2New);
    		sdg.addPDG(mc, javaPDG);
    	}
    	
    	//clone entra edges
    	for(Map.Entry<DependenceNode,Collection<DependenceEdge>> entry:_node2edgesIn.entrySet()){
    		 DependenceNode node = entry.getKey();
    		 Collection<DependenceEdge> edges = entry.getValue();
    		 DependenceNode newTgt = (DependenceNode)old2New.get(node);
    		 
    		 Collection<DependenceEdge> newEdges= new LinkedList<DependenceEdge>();
    		 for(DependenceEdge e: edges){
    			 DependenceNode newFrom = (DependenceNode)old2New.get(e.getFrom());
    			 if(e instanceof CtrlDependenceEdge){
    				 e = new CtrlDependenceEdge(newFrom, newTgt);
    			 }
    			 else if(e instanceof DataDependenceEdge){
    				 DataDependenceEdge dd = (DataDependenceEdge)e;
    				 e = new DataDependenceEdge(newFrom, newTgt,dd.getReason());
    			 }    			 
    			 
    			 newEdges.add(e); 
    		 }
    		 
    		 addInEdges(sdg, newTgt, newEdges);    		 
    	}
        
        return sdg;
    } 
    
    private static void addInEdges(SDG sdg, DependenceNode node, Collection<DependenceEdge> inEdges){
    	Collection<DependenceEdge> edges = sdg._node2edgesIn.get(node);
    	if(edges==null){
    		edges = new LinkedList<DependenceEdge>();
    		sdg._node2edgesIn.put(node,edges);
    	}
    	
    	edges.addAll(inEdges);
    }
    
    Set<Map.Entry<DependenceNode,Collection<DependenceEdge>>> getExtraEdges(){
    	return _node2edgesIn.entrySet();
    } 
}
