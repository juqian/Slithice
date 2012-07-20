package test;

import java.util.*;

import jqian.sootex.CFGProvider;
import jqian.sootex.HammockCFGProvider;
import jqian.sootex.du.IGlobalDUQuery;
import jqian.sootex.du.IReachingDUQuery;
import jqian.sootex.ptsto.IPtsToQuery;
import jqian.sootex.ptsto.SparkPtsToQuery;
import jqian.sootex.ptsto.TypeBasedPtsToQuery;
import jqian.sootex.util.CFGEntry;
import jqian.sootex.util.CFGExit;
import jqian.sootex.util.SootUtils;

import soot.*;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;


public class DUBuilderTest implements AllTestCases {
    static void testRD(IPtsToQuery ptsto, Set<String> methodSignatures) {    	 
    	CFGProvider cfgProvider = new HammockCFGProvider();
    	IGlobalDUQuery duQuery = Test.createDUQuery(ptsto, cfgProvider, _buildAll);
       
    	for(String signature: methodSignatures){
			SootMethod m = Scene.v().getMethod(signature);
			IReachingDUQuery dq = duQuery.getRDQuery(m);		
			testDUOnMethod("RD",dq, m);

			if(_showCfg){
	        	UnitGraph cfg = cfgProvider.getCFG(m);
	            Test.showCFG(m,cfg, "jgraph");
	        }  
			
			if (_showPDG) {				 
				Test.buildUnsafePDG(m, ptsto, dq, false);
			}
    	}
    }
    
    static void testRU(IPtsToQuery ptsto, Set<String> methodSignatures) {    	 
    	CFGProvider cfgProvider = new HammockCFGProvider();
    	IGlobalDUQuery duQuery = Test.createDUQuery(ptsto, cfgProvider, _buildAll);
       
    	for(String signature: methodSignatures){
			SootMethod m = Scene.v().getMethod(signature);			
			IReachingDUQuery uq = duQuery.getRUQuery(m);		
			testDUOnMethod("RU",uq, m);

			if(_showCfg){
	        	UnitGraph cfg = cfgProvider.getCFG(m);
	            Test.showCFG(m,cfg, "jgraph");
	        }
    	}
    }
    
    static void testDUOnMethod(String hint,IReachingDUQuery query,SootMethod m){
    	Test.out.println("\n=============== "+hint+" of "+m.getName()+"===================");
       
        for (Unit n: m.getActiveBody().getUnits()) {
        	Test.out.println("\n\n[Stmt("+SootUtils.getLine(n)+")] "+n.toString());
            testInOut(query,n);               
        }
        
        Test.out.println("\n\nStmt: entry");
        testInOut(query,CFGEntry.v()); 
        
        Test.out.println("\n\nStmt: exit");
        testInOut(query,CFGExit.v());  
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	static void testInOut(IReachingDUQuery rd,Unit stmt){
    	ForwardFlowAnalysis ffa = (ForwardFlowAnalysis)rd; 
    	Object in = ffa.getFlowBefore(stmt);    
    	showDUSet("IN set",in); 
        
        //Object out = ffa.getFlowAfter(stmt);
        //showDUSet("OUT set",out);        
    }   
    
    static void showDUSet(String hind,Object set){
    	if(set==null){
    		Test.out.println(hind + ": None");
    		return;
    	} 
    	
    	int size = 0;
    	Iterator<?> it = null;
    	if(set instanceof FlowSet){
    		FlowSet fin = (FlowSet)set;
    		it = fin.iterator();
    		size = fin.size();
    	}
    	else{ 
    		Collection<?> cin = (Collection<?>)set;
    		it = cin.iterator();
    		size = cin.size();
    	}
    	
    	Test.out.println(hind+"("+size+")");
    	Test.printCollection(it,"\n");
    } 
     
    public static boolean _showCfg = true;  
    public static boolean _showPDG = true; 
    public static boolean _buildAll = true;
  
    
    public static void main(String[] args) { 
    	boolean useTypeBasedPtsToAnalysis = false;
    	//if(args.length!=1){
    	//	Test.out.println("Usage: java SideEffectTest <checked_method_signature>");
    	//	return;
    	//} 
    	 
       	Properties conf = Test.loadConfig("/test/config.xml"); 
       	//conf.put("entry_class", RD_MAIN_CLASS);
       	//conf.put("entry_class", RU.MAIN_CLASS);
       	conf.put("entry_class", "test.cases.SDG3");
    	Test.loadClasses(true);
    	
    	if(!useTypeBasedPtsToAnalysis){
    		//SootUtils.doSparkPointsToAnalysis(Collections.EMPTY_MAP);
        	Test.doFastSparkPointsToAnalysis();
        	Test.simplifyCallGraph();
    	}
    	
    	Set<String> methodSigatures = new HashSet<String>();
    	//methodSigatures.add(RD_LOCAL[0]);
    	//methodSigatures.add(RD_LOCAL[1]);
    	//methodSigatures.add(RD_LOCAL[2]);
    	//methodSigatures.add(RD_LOCAL[3]);
    	//methodSigatures.add(RD_LOCAL[4]);
    	
    	//methodSigatures.add(RD_INTERPROC[0]);
    	//methodSigatures.add(RD_INTERPROC[1]);
    	//methodSigatures.add(RD_INTERPROC[2]);
    	
    	//methodSigatures.add(RU_LOCAL[0]);
    	//methodSigatures.add(RU_LOCAL[1]);
    	//methodSigatures.add(RU_LOCAL[2]);
    	//methodSigatures.add(RU_LOCAL[3]);
    	//methodSigatures.add(RU_LOCAL[4]);
    	
    	//methodSigatures.add(RD_INTERPROC[0]);
    	//methodSigatures.add(RD_INTERPROC[1]);
    	//methodSigatures.add(RD_INTERPROC[2]);
    	methodSigatures.add("<test.cases.SDG3: void test1()>");
		
    	IPtsToQuery query;
    	if(useTypeBasedPtsToAnalysis){
    		query = new TypeBasedPtsToQuery(false);
    	}
    	else{
    		query = new SparkPtsToQuery();
    	}
    	
    	testRD(query, methodSigatures);
    	//testRU(query, methodSigatures);
    }  
}
