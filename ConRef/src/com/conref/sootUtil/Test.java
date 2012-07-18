/** 
 *  
 * 
 * 	@author Ju Qian {jqian@live.com}
 * 	@date 2011-1-19
 * 	@version
 */
package com.conref.sootUtil;

import java.io.*;
import java.util.*;

import com.conref.util.CollectionUtils;
import com.conref.util.Configurator;
import com.conref.util.PathUtils;
import com.conref.util.WorkbenchHelper;


import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;


/**
 * @author bruteforce
 *
 */
public class Test {
	public static Properties _config;
	public static boolean _debug;
	public static boolean _verbose;
	public static PrintStream out = System.out; 
	
	public final static String DEFAULT_CONFIG_FILE = "./config.xml";
	
	public static Properties loadConfig(String configFile){		
		Configurator conf = new Configurator();
		File file = new File(configFile);

 			_config = conf.parse(configFile);


		_debug = Boolean.valueOf(_config.getProperty("debug"));
		_verbose = Boolean.valueOf(_config.getProperty("verbose"));
		return _config;
	}

	public static void setDefaultSootOptions(String classpaths, boolean isWholeProgramAnalysis, boolean inShimple){
//		String jdkPath = _config.getProperty("jdk_path");
//		String classpath = _config.getProperty("classpath");	
		String jdkPath = WorkbenchHelper.getJDKPath();
		String classpath =classpaths;
		String outputDir = "./output/soot"; 
		
		File file = new File(outputDir);
		file.mkdirs();
		//clearDirectory(outputDir);
		
		classpath  = PathUtils.getJreClasspath(jdkPath) + ";" + classpath;
		
		if(isWholeProgramAnalysis){
			if(inShimple){
				Options.v().set_whole_shimple(true);
				Options.v().set_via_shimple(true);
			}
			else{
				Options.v().set_whole_program(isWholeProgramAnalysis);
			}
		} 
		 
		 
		Options.v().set_verbose(false);
		Options.v().set_soot_classpath(classpath);
		Options.v().set_output_dir(outputDir);
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_keep_line_number(true);
		//Options.v().print_tags_in_output();
		Options.v().set_print_tags_in_output(true);	
		Options.v().setPhaseOption("cg", "verbose:false");
		//Options.v().setPhaseOption("cg","all-reachable:true");
		Options.v().setPhaseOption("jb", "use-original-names:true");
		PhaseOptions.v().setPhaseOption("jb", "use-original-names:true");		 
		//Options.v().set_validate(true);
	}
	public static void setDefaultSootOptions(Properties _config, boolean isWholeProgramAnalysis, boolean inShimple){
		String jdkPath = _config.getProperty("jdk_path");
		String classpath = _config.getProperty("classpath");	

		String outputDir = "./output/soot"; 
		
		File file = new File(outputDir);
		file.mkdirs();
		//clearDirectory(outputDir);
		
		classpath  = PathUtils.getJreClasspath(jdkPath) + ";" + classpath;
		
		if(isWholeProgramAnalysis){
			if(inShimple){
				Options.v().set_whole_shimple(true);
				Options.v().set_via_shimple(true);
			}
			else{
				Options.v().set_whole_program(isWholeProgramAnalysis);
			}
		} 
		 
		 
		Options.v().set_verbose(false);
		Options.v().set_soot_classpath(classpath);
		Options.v().set_output_dir(outputDir);
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_keep_line_number(true);
		//Options.v().print_tags_in_output();
		Options.v().set_print_tags_in_output(true);	
		Options.v().setPhaseOption("cg", "verbose:false");
		//Options.v().setPhaseOption("cg","all-reachable:true");
		Options.v().setPhaseOption("jb", "use-original-names:true");
		PhaseOptions.v().setPhaseOption("jb", "use-original-names:true");		 
		//Options.v().set_validate(true);
	}
	 
	public static Collection<SootClass> loadClasses(boolean isWholeProgramAnalysis){
		Date startTime = new Date();
		
		setDefaultSootOptions(_config, isWholeProgramAnalysis, false);
		
		//load classes for analysis
		String entryClass = _config.getProperty("entry_class");	
		SootUtils.loadClassesForEntry(entryClass);
	 
		Date endTime=new Date();	
		//Test.out.println("Load " + Scene.v().getClasses().size() + " soot classes in "+ Utils.getTimeConsumed(startTime,endTime));
		
		SootUtils.numberClassAndFields();
		return Scene.v().getClasses();
	}
	
	public static Collection<SootClass> loadClassesInShimple(boolean isWholeProgramAnalysis){
		Date startTime = new Date();
		
		setDefaultSootOptions(_config, isWholeProgramAnalysis, true); 
		//load classes for analysis
		String entryClass = _config.getProperty("entry_class");	
		SootUtils.loadClassesForEntry(entryClass);
	 
		Date endTime=new Date();	
		//Test.out.println("Load " + Scene.v().getClasses().size() + " soot classes in "+ Utils.getTimeConsumed(startTime,endTime));
		
		SootUtils.numberClassAndFields();
		return Scene.v().getClasses();
	}
	
	public static void printCollection(Iterator<?> first,String separator){
	    String str = CollectionUtils.toString(first,separator);
	    Test.out.print(str);
	} 
	
	public static void printBody(Body body){
		for(Unit s: body.getUnits()){	
			int line = SootUtils.getLine(s);
			Test.out.println(""+line+": " + s);
		}		
	}
	
	public static void buildCHACallGraph(){ 
 		List<SootMethod> entries = EntryPoints.v().application();
		entries = entries.subList(0, 1);
		Scene.v().setEntryPoints(entries);

		//PhaseOptions.v().setPhaseOption("cg", "implicit-entry:false");
		//PhaseOptions.v().setPhaseOption("cg.spark", "simulate-natives:false");
	 
		
	    CallGraphBuilder cg = new CallGraphBuilder( DumbPointerAnalysis.v() );
	    cg.build();
    
	
    }
	
	
	public static void doFastSparkPointsToAnalysis() {
		Map<String,String> opt = new HashMap<String,String>();
		opt.put("simulate-natives","false");   
		opt.put("implicit-entry","false");
		SootUtils.doSparkPointsToAnalysis(opt);
	}
	

	
	public static String getEntrySignature(String mainClass){
		return "<"+mainClass+": void main(java.lang.String[])>";
	}

	
    public static void doGC() {
        // Do 5 times because the garbage collector doesn't seem to always collect
        // everything on the first try.
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
    }  
}
