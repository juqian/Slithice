package jqian.sootex.dependency.pdg.builder;

import java.util.*;

import jqian.Global;
import jqian.sootex.CFGProvider;
import jqian.sootex.dependency.pdg.PDG;
import jqian.sootex.dependency.pdg.DepGraphOptions;
import jqian.sootex.dependency.pdg.SDG;
import jqian.sootex.du.IGlobalDUQuery;
import jqian.sootex.du.IReachingDUQuery;
import jqian.sootex.location.HeapAbstraction;
import jqian.sootex.location.Location;
import jqian.sootex.ptsto.IPtsToQuery;
import jqian.sootex.sideeffect.ISideEffectAnalysis;
import jqian.sootex.util.SootUtils;
import jqian.sootex.util.callgraph.CallGraphHelper;
import jqian.util.Utils;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.toolkits.graph.UnitGraph;

/**
 */
public class SDGBuilder {
	protected SDG _sdg;
    private boolean _verbose;
	
	//--------------------- temporals --------------------------//
    @SuppressWarnings("rawtypes")
	private Collection _entries;
    private DepGraphOptions _dgOptions;
	protected IPtsToQuery _ptsto;
    protected IGlobalDUQuery _gbRdQuery;
    protected ISideEffectAnalysis _sideEffect;
    protected CFGProvider _cfgProvider;
    protected int _javaLibDepth;
    protected CallGraph _restrictedCallGraph;
    protected HeapAbstraction _heapAbstraction;
    
	/** Build a context-insensitive SDG constructor.
	 * @param options  A set of options for PDG construction  
	 * @param rd       The Reaching Definition query should cover all methods reachable from the specified entries
	 *                 Or else there may be runtime exceptions.
	 * @param javaLibDepth Analysis depth for Java library methods
	 */
	public SDGBuilder(Collection<?> entries,DepGraphOptions options,
				CFGProvider cfgProvider, IPtsToQuery ptsTo, HeapAbstraction heapAbstraction,
			    IGlobalDUQuery rd,ISideEffectAnalysis sideEffect,boolean verbose, int javaLibDepth){
		this._entries = entries;
		this._verbose = verbose;
		this._dgOptions = options;
		this._ptsto = ptsTo;
		this._gbRdQuery = rd;
		this._sideEffect = sideEffect;		
		this._cfgProvider = cfgProvider;
		this._javaLibDepth = javaLibDepth;
		this._heapAbstraction = heapAbstraction;
		this._sdg = new SDG();
	}
	
	public SDG getSDG(){
		return _sdg;
	}
	
	public Collection<Location> getTgtUsedHeapLocs(SootMethod callee){
		return _sideEffect.getUseHeapLocs(callee);
	}
	
	public Collection<Location> getTgtUsedGlobals(SootMethod callee){
		return _sideEffect.getUseGlobals(callee);
	}
	
	public Collection<Location> getTgtModHeapLocs(SootMethod callee){
		return _sideEffect.getModHeapLocs(callee);
	}
	
	public Collection<Location> getTgtModGlobals(SootMethod callee){
		return _sideEffect.getModGlobals(callee);
	}
	
	public void buildMethodPDG(SootMethod m){
		Date startTime=null;
        if(_verbose){
            startTime=new Date();           
        }
        
	    int edgeCount[] = new int[5];		
	    int nodeCount = buildForMethod(m,edgeCount);
		
		Date endTime=null;
        if(_verbose){
            endTime=new Date();  
            Global.v().out.println("[Method PDG build] Complete in "+ Utils.getTimeConsumed(startTime,endTime));             
            Global.v().out.println("[Dependence Count] nodes "+nodeCount+", "+PDG.statisticsToString(edgeCount));   
        }  
	}
	
	/** This method should be called before buidling any dependence graph fragment. */
	public void preBuild(){		 	 
	}
	
	private int buildForMethod(SootMethod m,int edgeCount[]){
		if( !m.isConcrete() ||  !m.hasActiveBody()) 
			return 0;  
     
		Date startTime=null;
        if(_verbose){
            startTime=new Date();           
        }
        
		PDGBuilder builder;
		CallGraph cg = Scene.v().getCallGraph();
		if(_javaLibDepth>=0 && !_restrictedCallGraph.edgesOutOf(m).hasNext() && cg.edgesOutOf(m).hasNext()){
			builder = new PDGInterfaceBuilder(m,_dgOptions, this);
		}
		else{
			UnitGraph cfg = _cfgProvider.getCFG(m);
			IReachingDUQuery rd = _gbRdQuery.getRDQuery(m);
			builder = new PDGBuilder(m,cfg,_dgOptions, _ptsto, _heapAbstraction, rd,this);
		}
       
		builder.build();
		
		PDG pdg = builder.getPDG();
		pdg.compact();
        _sdg.addPDG(m, pdg);
        
        PDG.updateStatistic(edgeCount,pdg);  
        int nodeCount = pdg.getNodes().size();
        
        //Release rd query
        _gbRdQuery.releaseQuery(m);
        
        Date endTime=null;
        if(_verbose){
            endTime=new Date();  
            Global.v().out.println("[PDG] " + m + " -- " + Utils.getTimeConsumed(startTime,endTime) + ", " + nodeCount +" nodes");             
            //Global.v().out.println("[Dependence Count] nodes "+nodeCount+", "+PDG.statisticsToString(edgeCount));   
        }  
        
        return nodeCount;
	}
	
    @SuppressWarnings("unchecked")
	public void buildAll(){
    	Date startTime=null;
        if(_verbose){
            startTime=new Date();           
        }
		
		int edgeCount[] = new int[5];
		int nodeCount = 0;

		ReachableMethods reachables = null;
		if(_javaLibDepth<0){
			// no restriction on library methods
			reachables = SootUtils.getReachableMethods(_entries);   
		}
		else{
			// only limited depth of library methods are considered
			CallGraph cg = CallGraphHelper.tailorLibMethods(Scene.v().getCallGraph(),_entries, _javaLibDepth);
			reachables = new ReachableMethods(cg,_entries);    
			reachables.update();
			_restrictedCallGraph = cg;
		}
		     
		for(Iterator<?> it = reachables.listener();it.hasNext();){
			SootMethod m =(SootMethod) it.next();
			nodeCount += buildForMethod(m,edgeCount);
		}
		
		Date endTime=null;
        if(_verbose){
            endTime=new Date();  
            Global.v().out.println("[Context-insensitive SDG] "+ reachables.size() +" methods analyzed in "+
        		         Utils.getTimeConsumed(startTime,endTime)); 
            Global.v().out.println("[Dependence count] nodes "+ nodeCount+", "+PDG.statisticsToString(edgeCount));
        } 
    }   
    
    /** This method should be called after the whole building to release resources. */
    public void postBuild(){
    	_restrictedCallGraph = null;
    	
    	//clear temporals to save space
    	_entries = null;
        _dgOptions = null;
        _ptsto = null;
        _gbRdQuery = null;
        _sideEffect = null;   
    }
}
