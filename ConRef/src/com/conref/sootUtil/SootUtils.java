/*
 * Created on 2005-7-14
 * @author Ju Qian{jqian@live.com}
 */

package com.conref.sootUtil;

import java.lang.reflect.Method;
import java.util.*;

import com.conref.sootUtil.callgraph.CallGraphEdgeFilter;
import com.conref.sootUtil.callgraph.CallGraphHelper;
import com.conref.sootUtil.callgraph.CallGraphNodeFilter;
import com.conref.sootUtil.callgraph.DirectedCallGraph;



import soot.jimple.*;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.spark.pag.PAG;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import soot.options.Options;
import soot.shimple.*;
import soot.*;
import soot.util.*;
import soot.tagkit.*;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.StronglyConnectedComponents;
import soot.toolkits.scalar.Pair;


/**
 * A collection of basic soot utils
 */
public class SootUtils {
    /**Get the SootClass instance set of cls's father types*/
    public static Set<SootClass> getAncestorTypes(SootClass cls){
        Set<SootClass> classes = new HashSet<SootClass>();
        Chain<SootClass> interfaces = cls.getInterfaces();
        
        for(SootClass itInterface: interfaces){
        	classes.add(itInterface);
            classes.addAll(getAncestorTypes(itInterface));
        } 
        
        if(cls.hasSuperclass()){
            SootClass superClass=cls.getSuperclass();
            classes.add(superClass);
            classes.addAll(getAncestorTypes(superClass));
        }
        return classes;        
    } 
    
    public static Set<SootField> findAllInstanceFields(SootClass cls){
        Set<SootField> fields = new TreeSet<SootField>();
        Set<SootClass> set = getAncestorTypes(cls);
        set.add(cls);
        
        for(SootClass base: set){
            try{
                Chain<SootField> baseFields = base.getFields();
                for(SootField f: baseFields){
                	if(!f.isStatic())
                   		fields.add(f);
                }
            } 
            catch(RuntimeException e){} 
            //This operation requires resolving level SIGNATURES, but the analyzed 
            //class may be resolving at level HIERARCHY. In the case, a RuntimeException 
            //will be thrown out. The exception does not exist in whole-program analysis mode                     
        }    
        
        return fields;
    }
    
    /**Check whether the statement is caught_exception_ref*/
    public static boolean isExceptionIdentityStmt(Unit s){
        if(s instanceof IdentityStmt){
            IdentityStmt idStmt=(IdentityStmt)s;
            if(idStmt.getRightOp() instanceof CaughtExceptionRef)
                return true;
        }
        return false;
    }

    
    public boolean isLibMethod(SootMethod m){
    	SootClass cls = m.getDeclaringClass(); 
    	return isLibPackage(cls.getPackageName());
    }
    
    static String[] JAVA_LIB_PACKAGES = {
    	"sun.", "sunw.", "com.sun.", "java.", "javax.",
   	 	"org.apache.", "org.ietf.jgss.", "org.omg.", "org.w3c.dom.", "org.xml.sax."	
    };
    
    public boolean isLibPackage(String name){
         int size = JAVA_LIB_PACKAGES.length;
         
         for(int i=0;i<size;i++){
         	String pkg = JAVA_LIB_PACKAGES[i];
         	if(name.startsWith(pkg)){
         		return true;
         	}
         }		
       
         return false;
    }  
	public static DirectedGraph<Collection> getSCCGraph(CallGraph cg,Collection entries){
		ReachableMethods rm = CallGraphHelper.getReachableMethod(cg, entries);
		//SootMethod m = Scene.v().getMethod("<test.dependency.cases.SDG2$A: void run()>");
		//if(rm.contains(m)){
		//	int i =0;
		//}
		
		CallGraphNodeFilter nodeFilter = CallGraphHelper.getCallGraphNodeFilter(rm);
		
		CallGraphEdgeFilter edgeFilter = new CallGraphEdgeFilter(){
			public boolean isIgnored(Edge e){
				return (e.kind()==Kind.THREAD);
			}
		};
		
		DirectedCallGraph dcg = new DirectedCallGraph(cg, entries, nodeFilter, edgeFilter);
		StronglyConnectedComponents scc = new StronglyConnectedComponents(dcg);
		return scc.getSuperGraph();	 
	}
 
    
    public static int getLine(Unit unit){
        int line=0;
        LineNumberTag tag = (LineNumberTag)unit.getTag("LineNumberTag");
        if (tag != null) {
           line=tag.getLineNumber();
        }
        return line;
    }
   
    /**get the short description of a statement or access path by use the 
     * class name instead of the one with package description.*/
    public static String toShortString(String string){
        StringBuffer str=new StringBuffer(string);
        //filter package name
        for(int i=str.length()-1;i>=0;i--){
            char ch=str.charAt(i);
            if(ch=='.'){
                char right=str.charAt(i+1);
                if(right=='<'){//the left must be a local
                    
                }else{//the left must be a package name
                    //find the first char of package name
                    for(int j=i-1;j>=0;j--){
                       char cur=str.charAt(j);
                       if(!( ('a'<=cur && cur<='z') 
                             || ('A'<=cur && cur <='Z')
                             || ('0'<=cur && cur <='9')
                             || cur=='.' || cur=='_')
                          ){
                           str.delete(j+1,i+1);                           
                           i=j; 
                           break;
                       }
                   }
                }                
            }
            /*
            if(ch=='>'){                   
                for(int j=i-1;j>=0;j--){
                    if(str.charAt(j)==' '){
                        int firstBlank=j;
                        for(int k=firstBlank-1;k>=0;k--)
                            if(str.charAt(k)==' '){
                               str.delete(k,firstBlank);                                
                               break;
                            }
                        break;
                    }                        
                }
            }*/
        }
        
        //filter field description
        /*
        for(int i=str.length()-1;i>=0;i--){
            char ch=str.charAt(i);
            if(ch=='>'){
               int firstBlank=-1;
               for(int j=i-1;j>=0;j--){
                  char cur=str.charAt(j);
                  if(cur==' ') firstBlank=j;
                  if(cur=='<' && firstBlank>0){                      
                      str.delete(j,firstBlank+1);                           
                      i=j; 
                      break;                      
                   }
                }                
            }
        }*/
        return str.toString();
    }
    
    
    public static String getValueString(Value v){    	
    	if(v instanceof Immediate){
    		return v.toString();
    	}
    	else if(v instanceof ArrayRef){
    		return v.toString();
    	}
    	else if(v instanceof StaticFieldRef){
    		StaticFieldRef ref = (StaticFieldRef)v;
    		SootField field = ref.getField();
    		SootClass cls = field.getDeclaringClass();
    		return cls.getShortName()+"."+field.getName();
    	}
    	else if(v instanceof InstanceFieldRef){
    		InstanceFieldRef ref = (InstanceFieldRef)v;
    		SootField field = ref.getField();
    		Value base = ref.getBase();
    		return base.toString()+"."+field.getName();
    	}
    	else if(v instanceof PhiExpr){
    		PhiExpr expr = (PhiExpr)v;
    		String str = "Phi"+expr.getBlockId()+"(";
    		
    		int count = expr.getArgCount();
   			for (int i = 0; i < count; i++) {
    			str += expr.getValue(i);
    			if (i != count - 1){
    				str += ',';
    			}
   			}
    		
   			str += ")";
    		return str;   
    	}
    	else if(v instanceof InvokeExpr){
    		String str = "";
    		InvokeExpr expr = (InvokeExpr)v;
    		SootMethod tgt = expr.getMethod();
    		
    		if(v instanceof InstanceInvokeExpr){
    			InstanceInvokeExpr instCall = (InstanceInvokeExpr)v;
    			str += instCall.getBase().toString();
    		}
    		else{    			
    			str += tgt.getDeclaringClass().getShortName();
    		}
    		
    		str += "." + tgt.getName() + "(";
    			
    		int count = expr.getArgCount();
   			for (int i = 0; i < count; i++) {
    			str += expr.getArg(i);
    			if (i != count - 1){
    				str += ',';
    			}
   			}
    		
   			str += ")";
    		return str;    	
    	}
    	else{
    		return v.toString();
    	}
    }
    
    public static String getShortFieldString(SootField f){
    	SootClass cls = f.getDeclaringClass();
    	
    	String s = cls.getShortName();
    	s += "."+f.getName();
    	return s;
    }
    
    public static String getStmtString(Unit stmt) {
		if (stmt instanceof DefinitionStmt) {
			DefinitionStmt def = (DefinitionStmt) stmt;
			String str =  getLine(stmt) + ":";
			str += getValueString(def.getLeftOp());
			str += "=" + getValueString(def.getRightOp());
			return str;
		}
		else if (stmt instanceof InvokeStmt){
			InvokeStmt invoke = (InvokeStmt)stmt;
			return SootUtils.getLine(stmt) + ":" + getValueString(invoke.getInvokeExpr());
		}
		else if (stmt instanceof IfStmt) {
			IfStmt ifs = (IfStmt) stmt;
			return getLine(stmt) + ":if " + ifs.getCondition() + " goto " + getLine(ifs.getTarget());
		}
		else if (stmt instanceof GotoStmt) {
			GotoStmt gotoStmt = (GotoStmt) stmt;
			return getLine(stmt) + ":goto " + getLine(gotoStmt.getTarget());
		}
		else{
			return getLine(stmt) + ":" + toShortString(stmt.toString());
		}
	}   

    public static int getMethodCount(){       
        return Scene.v().getMethodNumberer().size()+1;       
    } 
    
    
    /** Reset Soot analysis results, prepare for another time of analysis. */
    public static void resetSoot(){
      	//clean soot
    	Scene.v().releaseActiveHierarchy();
    	Scene.v().releaseCallGraph();
    	Scene.v().releaseFastHierarchy();
    	Scene.v().releasePointsToAnalysis();
    	Scene.v().releaseReachableMethods();
    	Scene.v().releaseSideEffectAnalysis();
    	
    	Object[] classes =  Scene.v().getClasses().toArray();
    	for(int i=0;i<classes.length;i++){
    		SootClass c = (SootClass)classes[i];
    		Scene.v().removeClass(c);
    	}
    	
    	soot.G.reset();
    	
    	//force garbage collection
    	System.gc();
    	System.gc();
    }
    
    
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ReachableMethods getReachableMethods(Collection entries){
		Scene scene = Scene.v();
		if(entries.equals(scene.getEntryPoints())){
			 return scene.getReachableMethods();
		}
		 else{
			 ReachableMethods rm = new ReachableMethods(scene.getCallGraph(),entries);    
			 rm.update();
			 return rm;
		 }
	}
	
	
	public static SootClass loadClassesForEntry(String entryClass){
		Scene scene = Scene.v();
		SootClass mainClass = scene.loadClassAndSupport(entryClass);		
		mainClass.setApplicationClass();		
		scene.setMainClass(mainClass);
		scene.loadNecessaryClasses();
		scene.setEntryPoints(EntryPoints.v().application());
		return mainClass;
	}
	
	/** Number classes, fields, methods, locals, and etc.
	 *  Soot does not automatically do numbering for these entities.	 */
	public static void numberClassAndFields(){
		Scene scene = Scene.v();
		ArrayNumberer fieldNumberer = scene.getFieldNumberer();
		int clsNum = 1;
		
		//Assure field numbers. SOOT only set number for fields of RefLikeType(s)
		//The later analysis may depend on the field numbers to distinguish them
		Collection<SootClass> classes = scene.getClasses();
		for(SootClass cls: classes){
			cls.setNumber(clsNum);
			clsNum++;
			Collection<SootField> fields = cls.getFields();
			for(SootField f: fields){
				if(f.getNumber()==0){
					fieldNumberer.add(f);
				}
			}
		}
	}
	
	public static void numberLocals(){
		//Assure local numbers
		ArrayNumberer localNumberer = Scene.v().getLocalNumberer();
		for(SootClass cls: Scene.v().getClasses()){				
			for(SootMethod mthd: cls.getMethods()){
				if(!mthd.isConcrete())
					continue;
	    			
				if(!mthd.hasActiveBody()){
					mthd.retrieveActiveBody();
				}
	    			
				Body body = mthd.getActiveBody();
				for(Local loc: body.getLocals()){
					//check if not numbered yet
					if(loc.getNumber()<=0){
						localNumberer.add(loc);
					}
				}
			}
		}
	}
	
	
	/** Clean unnecessary memories of PointsToAnalysis if a 'clean' method is add to class PAG.  */
	public static void cleanPAG() {
		PAG pag = (PAG) Scene.v().getPointsToAnalysis();

		// in the extended version, PAG has a clean() method
		try {
			// call: pag.clean();
			Method clean = PAG.class.getMethod("clean");
			if (clean != null)
				clean.invoke(pag);
		} catch (Exception e) {
		}
	}
	
	
	public static Pair<Integer,Integer> getLineRange(SootClass cls){
		int min = Integer.MAX_VALUE;
		int max = 0;
		for(SootMethod m: cls.getMethods()){
			Body body = m.retrieveActiveBody();
			for(Unit u: body.getUnits()){
				int line = getLine(u);
				if(line<min){
					min = line;
				}
				if(line>max){
					max = line;
				}
			}
		}
		
		Pair<Integer,Integer> p = new Pair<Integer,Integer>(min,max);
		return p;
	}
	
	public static void doSparkPointsToAnalysis(Map<String,String> opt) {
		
		HashMap<String,String> defaultOptions = new HashMap<String,String>();
		defaultOptions.put("enabled","true");
		defaultOptions.put("verbose","true");
		defaultOptions.put("ignore-types","false");          
		defaultOptions.put("force-gc","false");            
		defaultOptions.put("pre-jimplify","false");          
		defaultOptions.put("vta","false");                   
		defaultOptions.put("rta","false");                   
		defaultOptions.put("field-based","false");           
		defaultOptions.put("types-for-sites","false");        
		defaultOptions.put("merge-stringbuffer","true");   
		defaultOptions.put("string-constants","false");		
		defaultOptions.put("simple-edges-bidirectional","false");
		defaultOptions.put("on-fly-cg","true");            
		defaultOptions.put("simplify-offline","false");      // true
		defaultOptions.put("simplify-sccs","false");        
		defaultOptions.put("ignore-types-for-sccs","false");
		defaultOptions.put("propagator","worklist");
		defaultOptions.put("set-impl","double");
		defaultOptions.put("double-set-old","hybrid");         
		defaultOptions.put("double-set-new","hybrid");
		defaultOptions.put("dump-html","false");           
		defaultOptions.put("dump-pag","false");             
		defaultOptions.put("dump-solution","false");        
		defaultOptions.put("topo-sort","false");           
		defaultOptions.put("dump-types","true");             
		defaultOptions.put("class-method-var","true");     
		defaultOptions.put("dump-answer","false");          
		defaultOptions.put("add-tags","false");             
		defaultOptions.put("set-mass","false"); 	
		defaultOptions.put("trim-clinit","true"); 	
		defaultOptions.put("all-reachable","false");  
		
 		//The following configurations reduce safety, but dramatically improve performance
		defaultOptions.put("simulate-natives","true");       //false to increase speed    
		defaultOptions.put("implicit-entry","true");         //false to ignore implicit entries
		
		
		for(Map.Entry<String, String> e: opt.entrySet()){
			String name = e.getKey();
			String value = e.getValue();
			defaultOptions.put(name, value);
		}		
		         
		SparkTransformer.v().transform("",defaultOptions);
		
		
	}
	
	public static void jimplify(){
		Collection<SootClass> classes = Scene.v().getClasses();
		for(SootClass c: classes){
			for(SootMethod m: c.getMethods()){
				if(m.isConcrete())
					m.retrieveActiveBody();
			}
		}
	}      
 //2012.2.15 tao delete "private" ,add "public"
	public static void assureShimpleBody(SootMethod m){
		Body body = m.retrieveActiveBody();
		ShimpleBody sBody;
		if(body instanceof ShimpleBody){
			sBody = (ShimpleBody) body;
            if(!sBody.isSSA())
                sBody.rebuild();
        }
        else{
            sBody = Shimple.v().newBody(body);
        }
		m.setActiveBody(sBody);
	}
	
	public static void shimplifyAll(){
		Collection<SootClass> classes = Scene.v().getClasses();
		for(SootClass c: classes){
			for(SootMethod m: c.getMethods()){
				if(m.isConcrete()){
					assureShimpleBody(m);
				}
			}
		}
	}

	public static void shimplifyReachable(){
	    CallGraphBuilder cg = new CallGraphBuilder( DumbPointerAnalysis.v() );
	    cg.build();
	    Collection<SootClass> classes = Scene.v().getClasses();
		for(SootClass c: classes){
			for(SootMethod m: c.getMethods()){
				if(m.isConcrete()){
					if(m.hasActiveBody()){
						assureShimpleBody(m);
					}
				}
			}
		}
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Set toCompactSet(){
		return null;}
}
