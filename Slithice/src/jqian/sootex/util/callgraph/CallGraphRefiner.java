package jqian.sootex.util.callgraph;

import java.util.*;

import jqian.Global;
import jqian.sootex.AtomicTypes;
import jqian.sootex.util.graph.BreathFirstSearch;
import jqian.util.Utils;
import soot.*;
import soot.jimple.*;
import soot.jimple.spark.pag.*;
import soot.jimple.spark.sets.*;
import soot.jimple.toolkits.callgraph.*;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import soot.toolkits.graph.*;

/**
 * Refine call graphs, filter unnecessary edges heuristically.
 * Redirect the caller of Thread.start() directly to the actually stared threads' run() method
 * Avoid hot call graph node java.lang.Thread.run() 
 * 
 * Soot's CallGraphBuilder already redirect Thread.start() to Thread*.run(), but 
 * for threads created in new Thread(Runnable) manner. The call is redirect to Thread.run(),
 * instead of the really started threads. 
 * Besides, the soot2.2.3 version has a bug that when a user
 * overriding Thread.start(), the overrided methods are also considered as thread 
 * starts.
 */
public class CallGraphRefiner{
	private final SootClass THREAD_CLASS = Scene.v().getSootClass("java.lang.Thread");
	private final SootClass RUNNABLE_CLASS = Scene.v().getSootClass("java.lang.Runnable");
	private final SootField THREAD_TARGET_FIELD = Scene.v().getField("<java.lang.Thread: java.lang.Runnable target>");
	private final SootMethod THREAD_START_METHOD = Scene.v().getMethod("<java.lang.Thread: void start()>");
	private final SootMethod THREAD_RUN_METHOD = Scene.v().getMethod("<java.lang.Thread: void run()>");  
	private final SootMethod RUNNABLE_RUN_METHOD = Scene.v().getMethod("<java.lang.Runnable: void run()>");
	    
	////////////////////////////////////////////////////////////////////
    private PointsToAnalysis _ptsTo;
    private boolean _verbose;
    private boolean _isVTA;
    
   
    public CallGraphRefiner(PointsToAnalysis ptsTo, boolean isVTA,boolean verbose){    	
        this._ptsTo = ptsTo;
        this._verbose = verbose;
        this._isVTA = isVTA;
    }
    
    public CallGraphRefiner(PointsToAnalysis ptsTo, boolean verbose){    	
        this(ptsTo,false,verbose);
    } 
    
    public static class CallGraphFilter{
    	public Collection<SootMethod> getCallGraphEntries(){
    		return Scene.v().getEntryPoints();		
    	}
    	
    	public boolean isEdgeIgnored(Edge edge){
    		return false;
    		
    		/*Options _options = PyxisGlobal.options();
			String pkgName = cls.getPackageName();
			
    		//filter by edge types, only the implicit thread call are always kept
    		if(kind==Kind.PRIVILEGED && _options.isPrivilegedCallIngored()){
    			continue;
    		}	    		
    		if((kind==Kind.FINALIZE || kind==Kind.INVOKE_FINALIZE) 
    			&& _options.isObjectFinalierIgnored()){
    			continue;
    		}    		
    		if(kind==Kind.CLINIT){
    			if(_options.isAllClinitIgnored()){
    				continue;
    			}
    			else if(_options.isJreClinitIgnored() && pyxis.isLibPackage(pkgName)){ 		                 
                    continue;
    			}
            }
    		if(kind==Kind.NEWINSTANCE && _options.isReflectiveNewInstanceIgnored()){
    			continue;
    		}
    		
    		//filter by method type
    		
    	
            
            //Not dig into the body of a method obviously side effect free
            if(pyxis.isSideEffectFreeLib(tgt) ){
            	continue;
            }
            
            //Ignore toString() methods of library class		            
            if(_options.isLibToStringIgnored() && m.getParameterCount()==0 
               && m.getName().equals("toString") && pyxis.isLibPackage(pkgName)){
            	 continue;
            }
            
            if(_options.isNonConcreteCalleeIgnored() && !tgt.isConcrete())
            	continue;   
		}*/
    	}
    } 
    
    public static class AggressiveCallGraphFilter extends CallGraphRefiner.CallGraphFilter{
    	// ignore implicit entries
    	public Collection<SootMethod> getCallGraphEntries(){
    		return EntryPoints.v().application();
    	}
    	
    	// ignore implicit calls
    	public boolean isEdgeIgnored(Edge edge){
    		//XXX: We do not step into the body of a type that is considered atomic
    		//     Handling a call like Integer.intValue() takes a lot of time
    		SootClass cls = edge.tgt().getDeclaringClass();
            if(AtomicTypes.isAtomicType(cls))
            	return true;
            
    		Kind kind = edge.kind();
    		if(kind.isExplicit() || kind==Kind.THREAD){
    			return false;
    		} 
    		
    		return true;
    	}
    }
   
    
    /** No filtering, just adjust thread start call edges .*/
    public CallGraph refine(CallGraph cg){
    	return refine(cg, new CallGraphFilter());
    } 
	
    
    /** 
     * Refine context-insensitive call graph, heuristically ignore many methods 
     * to reduce the analysis cost. 
     */
	public CallGraph refine(CallGraph cg, CallGraphFilter filter){
		Date startTime = new Date();
		int oldSize = cg.size();
		Map<Unit,SootMethod> threadCall2Method = new HashMap<Unit,SootMethod>(); 
 
		//selectively copy to a new call graph
		CallGraph newCg = new CallGraph();	
		Stack<SootMethod> stack = new Stack<SootMethod>();
		stack.addAll(filter.getCallGraphEntries());	    
	    Set<SootMethod> processed = new HashSet<SootMethod>();
	    
	    while(!stack.isEmpty()){
	    	SootMethod m = (SootMethod)stack.pop();
	    	
	    	if(!processed.add(m)){	    		
	    		continue; //if already processed
	    	}
	    	
	    	for(Iterator<Edge> it = cg.edgesOutOf(m);it.hasNext();){
	    		Edge e = it.next();
	    		Kind kind = e.kind();
	    		SootMethod tgt = e.tgt();
	    		SootClass cls = tgt.getDeclaringClass();
	    		
	    		if(filter.isEdgeIgnored(e)){
	    			continue;
	    		}
	            
	            if(kind==Kind.THREAD){
	            	if(_verbose) 
	            		Global.v().out.println(e);
	            	
	            	//XXX check if there are abnormal usage	            
	            	if(tgt.getName().equals("start") && !cls.getName().equals("java.lang.Thread")){
	            		throw new RuntimeException("strange edge.");
	            	}            		
	            	
	            	Unit unit = e.srcUnit();
	                threadCall2Method.put(unit,e.src());
	                continue;
	            }
	            
	            //remove old Thread edge, the edge will be added in later phase
	            if(tgt.equals(THREAD_START_METHOD)){
	            	if(_verbose) 
	            		Global.v().out.println(e);
	            	continue;
	            }     
      
	            newCg.addEdge(e);  
	            
	            if(!processed.contains(tgt)){
	            	stack.add(tgt);
	            }
	    	}
	    }	
	  
	    // Add thread start relevant call edges
	    if(_verbose){
	    	Global.v().out.println("=================== Refined thread start call edges ========================");
    	}
    	
        for(Map.Entry<Unit,SootMethod> entry: threadCall2Method.entrySet()){
            Unit unit = entry.getKey();
            SootMethod m = entry.getValue();            
            Collection<SootMethod> targets = resolveTargets(unit, cg);     
            
            //FIXME 对那些不是线程启动的方法也要收集调用目标，Thread+.start() -> Thread.start()
            //这里有个bug，但幸好重载Thread.start()的情况并不多，这里问题并不是很严重            
            for(SootMethod tgt: targets){           
                Edge m2run = new Edge(m,unit,tgt,Kind.THREAD); 
                newCg.addEdge(m2run);
                
                if(_verbose){    
                	Global.v().out.println("    " + m2run);
                }
            }                
        }
        
        Date endTime = new Date();
        int newSize = newCg.size();     
        Global.v().out.println("[Call Graph] Refine call graph in " + Utils.getTimeConsumed(startTime, endTime) +	
        						", new call graph has "+newSize +" edges (oritginal :"+ oldSize+")");

        return newCg; 
    } 
	
	public void refineClinits(){
		/*//call statements and their enclosing methods, only for thread relative ones
        Map<Unit,SootMethod> unit2Method = new HashMap(); 
        
		CallGraph cg = Scene.v().getCallGraph();
		CallGraph newCg = new CallGraph();	
		
		//get the edges into Thread.start() 
		Collection edgesToThreadStart = new HashSet();	    
        for (Iterator it = cg.edgesInto(_threadStart); it.hasNext(); ){
        	edgesToThreadStart.add(it.next());
        }
				
        for(SootMethod entry: Scene.v().getEntryPoints()){
        	//collection entries
    		Stack<SootMethod> stack = new Stack();
    		stack.add(entry);
        }
        
        
        
		
    		   
        //selectively copy the call graph	    
	    Set processed = new HashSet();
	    Collection<SootClass> classes = Scene.v().getClasses();
	    int clsCount = classes.size()+1;
	    int[] clsRef = new int[clsCount];	  
	    for(SootMethod m: stack){
	    	SootClass cls = m.getDeclaringClass();
	    	int id = cls.getNumber();
	    	clsRef[id]++;
	    }
	    
	    while(!stack.isEmpty()){
	    	SootMethod m = (SootMethod)stack.pop();
	    	SootClass cls = m.getDeclaringClass();
	    	int clsId = cls.getNumber();
	    	clsRef[clsId]--;
	    	
	    	if(!processed.add(m)){	    		
	    		continue; //if already processed
	    	}
	    	
	    	for(Iterator it = cg.edgesOutOf(m);it.hasNext();){
	    		Edge e = (Edge)it.next();
	    		Kind kind = e.kind();
	    		SootMethod tgt = e.tgt();
	    		cls = tgt.getDeclaringClass();	 
	    		clsId = cls.getNumber();
	    		
	            if(kind==Kind.THREAD){
	            	if(_verbose) PyxisGlobal.out.println(e);
	            	
	            	//XXX throw exception for unhandled thread usage.	            
	            	if(tgt.getName().equals("start") && !cls.getName().equals("java.lang.Thread")){
	            		throw new RuntimeException("Unawared thread usage.");
	            	}            		
	            	
	            	Unit unit = e.srcUnit();
	                unit2Method.put(unit,e.src());
	                continue;
	            }
	            else if(kind==Kind.CLINIT){
	            	//class already in stack, no more <clinit> call take place
	            	if(clsRef[clsId]>0){
	            		continue;
	            	}
	            }
	            
	            //remove old Thread edge, the correponding edge will be added in later phase
	            if(edgesToThreadStart.contains(e)){
	            	if(_verbose) System.out.println(e);
	            	continue;
	            }
	             	             
                //else remain
	            newCg.addEdge(e);  
	            
	            if(!processed.contains(tgt)){
	            	stack.add(tgt);
	    	    	
	    	    	clsRef[clsId]++;
	            }
	    	}
	    }	
	  
	    //add thread edges
        addThreadEdges(newCg,unit2Method); 
        
        //update call graph and reachable methods
        Scene.v().setCallGraph(newCg);
        Scene.v().setReachableMethods(null);     */   
    }
	    
  
    /** Resolve the real targets of a method call. */
    private Collection<SootMethod> resolveTargets(Unit unit, CallGraph originalCallGraph){
        Collection<SootMethod> threadEntries = new LinkedList<SootMethod>(); //set of possible calling targets

        if(_ptsTo instanceof PAG){
        	InvokeStmt stmt = (InvokeStmt)unit;
            InstanceInvokeExpr expr = (InstanceInvokeExpr)stmt.getInvokeExpr();
            Local receiver = (Local)expr.getBase(); 
            
        	//collect all possible receiver thread objects  
        	PointsToSet pt2set = _ptsTo.reachingObjects(receiver);  
        	Collection<AllocNode> threadObjs = new HashSet<AllocNode>();
        	PointsToSetInternal internalSet = (PointsToSetInternal)pt2set;                              
            P2SetVisitor visitor = new ThreadCollector(threadObjs,true,expr);
            internalSet.forall(visitor);
            
            //transfer thread objects to thread classes (including classes implementing Runnable)
            Collection<SootClass> threadClasses = new HashSet<SootClass>();
            for(AllocNode n: threadObjs){
            	RefType type = (RefType)n.getType();
                threadClasses.add(type.getSootClass());	
            }
            
            //transfer thread classes to corresponding run() methods
            
            FastHierarchy hierarchy = Scene.v().getFastHierarchy();        
            for(SootClass cls: threadClasses){ 
                SootMethod entry = hierarchy.resolveConcreteDispatch(cls,RUNNABLE_RUN_METHOD);
                if(entry != THREAD_RUN_METHOD){//ignore Thread.run()            	
                	threadEntries.add(entry);
                }
            }
        }
        else if(_ptsTo instanceof DumbPointerAnalysis){
        	// Transfer thread start on Thread.run() to the actually started thread,
        	// namely the thread entries possibly called inside Thread.run()
        	Iterator<Edge> edges = originalCallGraph.edgesOutOf(unit);        	
        	for(Targets it = new Targets(edges);it.hasNext();){
        		SootMethod m = (SootMethod)it.next();
        		if(m==THREAD_START_METHOD){
        			
        		}
        		else if(m==THREAD_RUN_METHOD){        			 
        			Callees callees = new Callees(originalCallGraph,THREAD_RUN_METHOD);
        			threadEntries.addAll(callees.explicits());
        		}
        		else{
        			threadEntries.add(m);
        		}
        	}
        	
        	threadEntries.remove(THREAD_RUN_METHOD);
        }  
        
        return threadEntries;        
    }
 
    
    private boolean isThreadStartCall(AllocNode node,InvokeExpr expr){
    	if(expr instanceof SpecialInvokeExpr){
    		if(expr.getMethod() ==THREAD_START_METHOD)
        		return true;
    		else
    			return false;
    	}
    	else{
    		FastHierarchy hierarchy = Scene.v().getFastHierarchy();
    		SootClass cls = ((RefType)node.getType()).getSootClass();
    		SootMethod start = hierarchy.resolveConcreteDispatch(cls,THREAD_START_METHOD);
    		if(start == THREAD_START_METHOD)
    			return true;
    		else
    		    return false;
    	}
    }
   
    
    /** Collect all SootClass(es) of a given PointsToSet. */
    private class ThreadCollector extends P2SetVisitor{
        private Collection<AllocNode> _threadObjs;    
        private Set<AllocNode> _checkedObjects;
        private FastHierarchy _hierarchy;
        private boolean _checkStartCall;
        private InvokeExpr _invoke;
        
        public ThreadCollector(Collection<AllocNode> threadObjs,boolean checkStartCall,InvokeExpr invoke){
        	this(threadObjs, new HashSet<AllocNode>(), checkStartCall,invoke); 
        }
        
        public ThreadCollector(Collection<AllocNode> threadObjs,Set<AllocNode> checkedObjects, boolean checkStartCall,InvokeExpr invoke){
            this._threadObjs = threadObjs;  
            this._hierarchy = Scene.v().getFastHierarchy();
            this._checkStartCall = checkStartCall;
            this._invoke = invoke;
            this._checkedObjects = checkedObjects;            
        }
        
        public void visit(Node n)  {
            if(n instanceof AllocNode){
            	//To avoid recursive analysis
            	if(_checkedObjects.contains(n)){
            		return;
            	}
            	
            	AllocNode node = (AllocNode)n;
            	_checkedObjects.add(node); 
            	
                Type t = node.getType(); 
                if(!(t instanceof RefType)){
                	//XXX: Could be AnySubType
                	return;
                }
                
                RefType type = (RefType)t;
                SootClass cls = type.getSootClass();
                
                //Check if the start() call is a real call to Thread.start(). 
                //If not, no THREAD edge is added, and here we directly return.    
                if(_checkStartCall){
                	//FIXME 这里还是要加一条到Thread+.start()的边，该问题暂时没有处理
                	if(!isThreadStartCall(node,_invoke))
                       	return;
                }
                
                SootMethod run = _hierarchy.resolveConcreteDispatch(cls,RUNNABLE_RUN_METHOD);
                if(run == THREAD_RUN_METHOD){
                	//Call Thread.run() on java.lang.Thread or its sub class (subclass may not override run() method)
                	if(cls == THREAD_CLASS && !_isVTA){
                		//trace to the constructor call of Thread(Runnable r) 
                		resolveTgtsFromParam(node,_threadObjs,_checkedObjects);                 		
                	}else{
                		//searching Thread.target field for the started threads                		
                		resolveTgtsFromField(node,_threadObjs,_checkedObjects);  
                        //throw new RuntimeException("A special use of thread.");
                	}
                }else{
                	//Call run() on a sub class of java.lang.Thread overriding Thread.run()
                	_threadObjs.add(node); 
                }
            }
            else{
                throw new RuntimeException(n.getClass()+" can not occur in a points-to set");
            }        
        }
    }
    
    /** Resolve the possible started Runnable(s) from class field Thread.target.
     *  This approach can be very imprecise as this field is assigned in
     *  Thread.init(ThreadGroup,Runnable,...) which is analyzed context-insensitively.
     *  We prefer resolveTgtsFromParams instead.
     */ 
    private void resolveTgtsFromField(AllocNode node,Collection<AllocNode> threadObjs,Set<AllocNode> checkedObjects){    	
    	 AllocDotField field = node.dot(THREAD_TARGET_FIELD); 
    	 PointsToSetInternal targets = field.getP2Set();    	   	
         P2SetVisitor visitor = new ThreadCollector(threadObjs,checkedObjects,false,null);
         targets.forall(visitor); 
    }
    
    private void resolveTgtsFromParam(AllocNode node,Collection<AllocNode> threadObjs,Set<AllocNode> checkedObjects){    	
		Value allocExpr = (Value)node.getNewExpr();		
		DefinitionStmt allocStmt = null;
		Local allocLocal = null;
		Body body = node.getMethod().getActiveBody();
		
		{//Find the allocation statement and the Local carrying the new allocated object
			for (Unit unit: body.getUnits()) {
			    if(unit instanceof DefinitionStmt){
			    	DefinitionStmt d = (DefinitionStmt)unit;
				    if(d.getRightOp() == allocExpr){
				    	allocStmt = d;
				    	allocLocal = (Local)d.getLeftOp();
						break;
				    }
			    }
			}

			if (allocStmt == null)
				throw new RuntimeException("Error to find allocating Unit.");
		}
		
		{//find call to the constructors, set local to be the real thread
			UnitGraph graph = new BriefUnitGraph(body);			
			BreathFirstSearch<Unit> bsearch = new ConstructorCallSearch(graph,allocStmt,allocLocal,THREAD_CLASS);
			Collection<Unit> calls = bsearch.search();
			InvokeStmt invoke = (InvokeStmt)calls.iterator().next();
			List<?> args = invoke.getInvokeExpr().getArgs();
			int size = args.size();
			
			Type runableType = RUNNABLE_CLASS.getType();
			FastHierarchy hierarchy = Scene.v().getFastHierarchy();
			for(int i=0;i<size;i++){
				Object ag = args.get(i);
				if(ag instanceof Local){
					Local agLoc = (Local)ag;
					Type agType = agLoc.getType();
					
					if(hierarchy.canStoreType(agType, runableType)){//agType == runableType){
						allocLocal = agLoc;
					}					
				}
			}
			
			assert(allocLocal!=null);//Else can not find the real thread object.
		}
		
		//if(local!=null){//get the points-to set of Runnable type local
			PointsToSetInternal tgts =(PointsToSetInternal)_ptsTo.reachingObjects(allocLocal);	
			//If implemented like this:
			// Runnable r = new RunnableImpl();
			// Thread t0 = new Thread(r);
			// Thread t1 = new Thread(t0);
			// t1.start();
			//Then we must perform recursive analysis as the real threads holden by t0 can still be activated.
	        P2SetVisitor visitor = new ThreadCollector(threadObjs,checkedObjects,false,null);
	        tgts.forall(visitor); 
		//}		
    }  
    
    private class ConstructorCallSearch extends BreathFirstSearch<Unit>{
    	public ConstructorCallSearch(UnitGraph cfg,Unit start,Local local,SootClass cls){
    	   super(cfg,start,false);	
    	   this._local = local;
    	   this._cls = cls;
    	}
    	
    	public boolean match(Unit obj){
    		if(!(obj instanceof InvokeStmt)) 
    			return false;
    		
    		InvokeStmt stmt = (InvokeStmt)obj;
    		InvokeExpr expr = stmt.getInvokeExpr();
    		if(!(expr instanceof InstanceInvokeExpr))
    			return false;
    				
    		InstanceInvokeExpr invoke = (InstanceInvokeExpr)expr;
    		Local receiver = (Local)invoke.getBase(); 
    		if(receiver != _local)
    			return false;
    		
    		SootMethod method = invoke.getMethod();
    		if(method.getDeclaringClass()!=_cls ||!method.getName().equals("<init>"))
    			return false;    			
    		
    		return true;
    	}
    	
    	private Local _local;
    	private SootClass _cls;
    }
}
