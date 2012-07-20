package jqian.sootex.du;

import java.util.*;
import soot.*;
import soot.toolkits.graph.*;
import jqian.sootex.location.HeapAbstraction;
import jqian.sootex.sideeffect.ISideEffectAnalysis;
import jqian.sootex.util.SootUtils;
import jqian.sootex.CFGProvider;
import jqian.sootex.Cache;
import jqian.sootex.ptsto.IPtsToQuery;
import jqian.util.Utils;

/**
 * Context-insensitive reaching definition / use analysis
 * General steps:
 *    1.a. buildAll();         => build for all reachable methods
 *      b. buildXX(SootMethod) => Just build for a single method.
 *                                May need to take care of the build order so that the callees
 *                                are always built before their callers.
 *    
 *    3.a.  clear()             => clear all used temporal for the whole analysis  
 *      b.  clear(SootMethod)   => clear all temporal used for build the specified method 
 */
public class DUBuilder{
	private boolean _verbose;
    private RDAnalysis[] _rdAnalyses;  //Reaching definition analyzer for each method
    private RUAnalysis[] _ruAnalyses;  //Reaching use analyzer for each method  
    private HeapAbstraction _heapAbstraction;
    
    //-------------- Temporal ------------------//
    private IPtsToQuery _ptsto;
    private ISideEffectAnalysis _sideEffect;    
    private IGlobalDUQuery _query;
    private CFGProvider _cfgProvider;    
	

    public DUBuilder(CFGProvider cfgProvider, IPtsToQuery ptsto, HeapAbstraction heapAbstraction,
    		                   ISideEffectAnalysis sideEffect, boolean verbose){
		this._cfgProvider = cfgProvider;
    	this._ptsto = ptsto;
		this._sideEffect = sideEffect;
		this._verbose = verbose;	
		this._query = new GbDUQuery();
		this._heapAbstraction = heapAbstraction;
		
		int methodCount = SootUtils.getMethodCount();
		this._rdAnalyses = new RDAnalysis[methodCount];
		this._ruAnalyses = new RUAnalysis[methodCount];
    }
    
    /** Using lazy building, nothing is done at all. */
	public void buildAll() {
		Date startTime = new Date();

		List<?> rm = Cache.v().getTopologicalOrder();
		for (Iterator<?> it = rm.iterator(); it.hasNext();) {
			//SootMethod m = (SootMethod) it.next();
			//ReachingDefQuery query = gbRdQuery.getQuery(m);

			//PtsToQuery pt2Query = ptsToQuery.getMethodQuery(m);
		}

		Date endTime = new Date();
		System.out.println("Test finish in " + Utils.getTimeConsumed(startTime, endTime));
	}
    
    public void buildRD(SootMethod m){
    	int id = m.getNumber();
		RDAnalysis rdAnalysis = _rdAnalyses[id];
		if (rdAnalysis == null) {			 
			UnitGraph cfg = _cfgProvider.getCFG(m);
			rdAnalysis = new RDAnalysis(m, cfg, _ptsto, _heapAbstraction, _sideEffect, _verbose);
			rdAnalysis.build();

			_rdAnalyses[m.getNumber()] = rdAnalysis;
		}
    }
    
    public void buildRU(SootMethod m){
    	int id = m.getNumber();
		RUAnalysis ruAnalysis = _ruAnalyses[id];
		if (ruAnalysis == null) {			 
			UnitGraph cfg = _cfgProvider.getCFG(m);
			ruAnalysis = new RUAnalysis(m, cfg, _ptsto, _heapAbstraction, _sideEffect, _verbose);
			ruAnalysis.build();

			_ruAnalyses[m.getNumber()] = ruAnalysis;
		}
    }
    
    public void clear(SootMethod m){
    	
    }
    
    public void clear(){
    	_ptsto = null;
        _sideEffect = null;    
        _query = null;
        _cfgProvider = null;
    }
    
    /** The building result. */
    public IGlobalDUQuery getGlobalDUQuery(){
    	return _query;
    } 
    
    /////////////////////////////////////////////////////////////////////////////////////////
    public class GbDUQuery implements IGlobalDUQuery{   	    						
    	public IReachingDUQuery getRDQuery(MethodOrMethodContext mc){
    		 SootMethod m = mc.method();
    		 buildRD(m);
    		 
    		 int id = m.getNumber();
    		 RDAnalysis rdAnalysis = _rdAnalyses[id];
    		 return rdAnalysis;
    	}
    	
		public IReachingDUQuery getRUQuery(MethodOrMethodContext mc) {
			SootMethod m = mc.method();
   		 	buildRU(m);
   		 
   		 	int id = m.getNumber();
   		 	RUAnalysis ruAnalysis = _ruAnalyses[id];
   		 	return ruAnalysis;
		}
    	
    	public void releaseQuery(MethodOrMethodContext mc){
    		 SootMethod m = mc.method();
    		 int id = m.getNumber();
    		 _rdAnalyses[id] = null;
    		 _ruAnalyses[id] = null;
    	}
    }
}
