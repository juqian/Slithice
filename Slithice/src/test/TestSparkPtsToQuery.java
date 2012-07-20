package test;

import java.util.*;
import jqian.sootex.ptsto.IPtsToQuery;
import jqian.sootex.ptsto.SparkPtsToQuery;
import soot.*;

public class TestSparkPtsToQuery{	
    /**
     * Usage: java CLASS <main_class> [analyzed_method]*
     * Example:
     *     test.util.cases.AccessPathCase "<test.util.cases.AccessPathCase: void test1()>"
     */
    public static void main(String[] args) {
    	Properties options = Test.loadConfig("/config.xml"); 
       	options.put("entry_class", "test.util.CallGraphTestCase");
       	
    	Test.loadClasses(true);     
    	Test.doFastSparkPointsToAnalysis();    	 
    	
    	SootMethod method = Scene.v().getMethod("");
        System.out.println("Testing method: "+method);
        IPtsToQuery query = new SparkPtsToQuery(); 
        PtsToTester.testPtsToQuery(Test.out,method,query);
    }  
}
