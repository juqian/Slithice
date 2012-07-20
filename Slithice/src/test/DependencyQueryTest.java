package test;

import java.util.Collection;
import java.util.Properties;

import soot.Body;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.DominatorTree;
import soot.toolkits.graph.MHGPostDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import test.Test;
import jqian.sootex.CFGProvider;
import jqian.sootex.HammockCFGProvider;
import jqian.sootex.dependency.DependencyQuery;
import jqian.sootex.du.IGlobalDUQuery;
import jqian.sootex.ptsto.IPtsToQuery;
import jqian.sootex.ptsto.SparkPtsToQuery;
import jqian.sootex.ptsto.TypeBasedPtsToQuery;
import jqian.sootex.util.SootUtils;
import jqian.sootex.util.graph.DominatorTreeGraph;
import jqian.sootex.util.graph.GraphHelper;
import jqian.util.graph.Graph;

/**
 *
 */
public class DependencyQueryTest implements AllTestCases{
	static void test(String pta, String methodSignature){
		IPtsToQuery ptsto;
    	if(pta.equals("type-based")){
    		ptsto = new TypeBasedPtsToQuery(false);
    	}
    	else{
    		ptsto = new SparkPtsToQuery();
    	}  
	 
    	CFGProvider cfgProvider = new HammockCFGProvider();
    	IGlobalDUQuery duQuery = Test.createDUQuery(ptsto, cfgProvider, true);    	
		DependencyQuery depQuery = new DependencyQuery(duQuery, cfgProvider);
		
		SootMethod m = Scene.v().getMethod(methodSignature);
		Test.out.println("Testing DependencyQuery for method: " + m);
		
		UnitGraph cfg = cfgProvider.getCFG(m);
        Test.showCFG(m,cfg, "jgraph");        
        
        if(_showDominatorTree){
        	MHGPostDominatorsFinder postdomFinder = new MHGPostDominatorsFinder(cfg);
    		DominatorTree postdomTree = new DominatorTree(postdomFinder);
    		DominatorTreeGraph graph = new DominatorTreeGraph(postdomTree);
    	    Graph displayedCallgraph = GraphHelper.toDisplayGraph(graph, "Dominator Tree Graph");    	 
    	    Test.showGraph(displayedCallgraph, "jgraph");	
        }        
   
		Body body = m.retrieveActiveBody();
		for(Unit u: body.getUnits()){
			Collection<Unit> cd = depQuery.getCtrlDependences(m, u);
			Collection<Unit> rwd = depQuery.getRWDependences(m, u);
			Collection<Unit> wrd = depQuery.getWRDependences(m, u);
			Collection<Unit> wwd = depQuery.getWWDependences(m, u);
			 
			Test.out.println("\n[Stmt] " + SootUtils.getStmtString(u));
			printDependences("Control dependences:", cd);
			printDependences("Flow dependences", wrd);
			printDependences("Anti Dependences", rwd);
			printDependences("Output Dependences", wwd);			
		}
	}
	
	static void printDependences(String hint, Collection<Unit> dep){
		Test.out.println();
		Test.out.println(hint);
		for(Unit u: dep){
			Test.out.println(" -> " + SootUtils.getStmtString(u));
		}
	} 
	
	static boolean _showDominatorTree = false;
	
	public static void main(String[] args) { 
    	String pta = "spark";
       	Properties conf = Test.loadConfig("/test/dependency/config.xml"); 
       	conf.put("entry_class", DepQuery.MAIN_CLASS);
    	Test.loadClasses(true);
    	
    	if(!pta.equals("type-based")){
    		//SootUtils.doSparkPointsToAnalysis(Collections.EMPTY_MAP);
        	Test.doFastSparkPointsToAnalysis();
        	Test.simplifyCallGraph();
    	} 
    	
    	//test(pta, CTRL_DEPENDENCE_QUERY[1]);
    	//test(pta, FLOW_DEPENDENCE_QUERY[4]);
    	//test(pta, ANTI_DEPENDENCE_QUERY[2]);
    	test(pta, DepQuery.OUTPUT_DEPENDENCE_QUERY[1]);
    }  
}
