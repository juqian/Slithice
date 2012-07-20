package jqian.sootex.dependency.pdg.builder;

import java.util.*;

import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.*;
import jqian.sootex.du.*;
import jqian.sootex.location.*;
import jqian.sootex.ptsto.IPtsToQuery;
import jqian.sootex.ptsto.PtsToHelper;
import jqian.sootex.util.CFGEntry;
import jqian.sootex.util.CFGExit;
import jqian.sootex.dependency.DependencyHelper;
import jqian.sootex.dependency.pdg.*;


/**
 * Constructing a procedural dependence graph. 
 */
public abstract class AbstractPDGBuilder { 
    protected PDG _pdg; 
    protected SootMethod _method;       
    protected HeapAbstraction _heapAbstraction;
    
    /*Temporal reference*/
    protected DepGraphOptions _pdgOptions;
    protected IPtsToQuery _ptsto;
    protected IReachingDUQuery _rd; 
    protected UnitGraph _cfg;
    
    
    /** 
     * @param cfg The CFG graph of the analyzed method. Used for flexible PDG construction.
     * @param ptsTo Used to query the points-to information
     * @param rd    Used to query the reaching definition information
     */
    public AbstractPDGBuilder(SootMethod m, UnitGraph cfg, DepGraphOptions dgOptions,
    		                  IPtsToQuery ptsTo, HeapAbstraction heapAbstraction, IReachingDUQuery rd){
    	this._method = m;
        this._pdgOptions = dgOptions;    
        this._pdg = new PDG(m);
        this._ptsto = ptsTo;       
        this._rd = rd;      
        this._cfg = cfg;     
        this._heapAbstraction = heapAbstraction;
    }    
    
    public PDG getPDG(){
        return _pdg;
    }
    
    public void build(){    	
    	Collection<Unit> stmts = collectStmts();        
        initializeNodes(stmts);  

        if(_pdgOptions.withCtrlDependence()){        	
            buildCtrlDependences(_cfg,stmts); 
        } 
        
        buildDataDependence(stmts); 
        clearTemporals();
    }  
   
	//collect statements for dependence graph construction
    protected Collection<Unit> collectStmts(){
    	// XXX: only build dependences for nodes in the CFG
    	// Collection<Unit> stmts = _method.getActiveBody().getUnits();         
    	Collection<Unit> stmts = new HashSet<Unit>();
        for(Iterator<Unit> it=_cfg.iterator();it.hasNext();){
        	Unit u = it.next();
        	
            // do not create node for CFGEntry and CFGExit
            // the PDG already has an entry, and there is no need for an exit
            if(u instanceof CFGEntry || u instanceof CFGExit){
            	continue;
            }
            
        	stmts.add(u);
        }  
        
        return stmts;
    }
    
    /**Collect the dependence graph nodes for each statement */
    private void initializeNodes(Collection<Unit> stmts){  
    	// nodes for normal statements
        for(Unit s :stmts){
            if(s instanceof IdentityStmt)
            	continue; 
            
            if(s instanceof Stmt && ((Stmt)s).containsInvokeExpr()){
            	buildNodesForCall(s);    
            }
            else{            	
            	JimpleStmtNode node = new JimpleStmtNode(_method, s);
				_pdg.addNode(node);   
            }
        } 
        
        //build formal -in/-out nodes
        buildFormals();
    }
    
    /** Any PDG builder needing formal nodes should overwrite this method. */
    protected abstract void buildFormals();
    
    /** Build nodes for call. */
    protected abstract void buildNodesForCall(Unit callsite);
    
    protected abstract void buildFormalInDependences();
    
    protected abstract void buildFormalOutDependences();
    
    protected abstract void buildDepForInvoke(Unit curStmt,DependenceNode curNode);
    
    
    protected void buildCtrlDependences(UnitGraph cfg,Collection<Unit> stmts){ 
    	Map<Unit,Collection<Unit>> unit2depends = DependencyHelper.calcCtrlDependences(cfg);
    	DependenceNode entry = _pdg.entry();
    	
		for (Unit s : stmts) {
			if(s instanceof IdentityStmt){
				continue;
			}
			//if(s instanceof CFGEntry){
			//	continue;
			//}
			
			DependenceNode to = _pdg.getStmtBindingNode(s);
			Collection<Unit> depends = unit2depends.get(s);
			for (Unit d : depends) {
				DependenceNode from;
				if (d instanceof CFGEntry) {
					from = entry;
				} else {
					from = _pdg.getStmtBindingNode(d);
				}

				DependenceEdge edge = new CtrlDependenceEdge(from, to);
				_pdg.addEdge(edge);
			}
		}
    }
    
    protected void buildDataDependence(Collection<Unit> stmts){              
        for(Unit s: stmts){     
            DependenceNode dest= _pdg.getStmtBindingNode(s);
            
            if(s instanceof Stmt && ((Stmt)s).containsInvokeExpr()){           
                //invoke_stmt or assign_stmt with invoke_expr            	
            	buildDepForInvoke(s, dest);            	           	
            }            
            else{
                //Including: assign_stmt, return_void_stmt, if_stmt, lookup_switch_stmt, 
                //           table_switch_stmt, throw_stmt, ret_stmt, return_stmt, enter_monitor_stmt   
            	//No dependence: breakpoint_stmt, goto_stmt, nop_stmt
                buildDepForNormalStmt(s, dest);
            } 
        }
        
        buildFormalInDependences();
        buildFormalOutDependences();
    }
     
    private void buildDepForNormalStmt(Unit stmt,DependenceNode dest){
    	// include use in left hand side and right hand side of the statement
    	List<ValueBox> useBoxes = stmt.getUseBoxes();	
    	for(ValueBox vb: useBoxes){
            Value use = vb.getValue();
          
            //not a direct box containing variables
            if(use instanceof Local){
            	Location loc = Location.valueToLocation(use);            
            	buildDepForLocation(stmt,loc,dest);
            }
            else if(use instanceof StaticFieldRef){
            	StaticFieldRef ref = (StaticFieldRef)use;
            	Location loc = Location.getGlobalLocation(ref.getField());          
            	buildDepForLocation(stmt,loc,dest);
            }
            else if(use instanceof InstanceFieldRef){ 
            	AccessPath ap = AccessPath.valueToAccessPath(_method, stmt, use);
            	buildDepForRef(stmt,dest,ap);            	 
            }
            else if(use instanceof ArrayRef){
            	AccessPath ap = AccessPath.valueToAccessPath(_method, stmt, use); 
                buildDepForRef(stmt,dest,ap);                
            }
            else{
            	//Ignored immediate: Constant
            	//Ignore expressions, e.g., binop_expr, instance_of_expr, unop_expr, cast_expr,
            	//new_expr, new_array_expr, new_multi_array_expr
            }
    	}
    }  
    
    /** build dependence edge from an abstract location. */
    protected void buildDepForLocation(Unit curStmt,Location loc,DependenceNode dest){               
    	 Collection<Unit> defs = _rd.getReachingDUSites(curStmt, null, loc);
         for(Unit defStmt: defs){               	
         	addDataDep(dest,defStmt,loc);
         }
    }   
    
    /**
     * Collect the dependences of instance field or array element access.
     * The dependencies of the based variable is not considered here.
     * TODO: 这里性能还有很大的优化余地
     */
    protected void buildDepForRef(Unit curStmt,DependenceNode curNode,AccessPath ap){	
    	Collection<Location> locations = PtsToHelper.getAccessedLocations(_ptsto, _heapAbstraction, curStmt, ap); 
    	Collection<Unit> defSites = _rd.getReachingDUSites(curStmt, ap, locations);
 
    	//TODO 性能问题
    	for(Unit defStmt: defSites){ 
    		Collection<Location> defs = _rd.getDULocations(defStmt);
    		for(Location loc: locations){
    			if(defs.contains(loc)){
    				addDataDep(curNode,defStmt,loc);
    			}    			
    		}
        }
    }
 
   
    /**
     * Get the dependence graph nodes corresponding to the definition of location <code>loc</code>
     * at statement <code>stmt</code>, so that dependence edges between def-use sites can be added.
     */
    protected Collection<DependenceNode> getDefinitionNodes(Unit stmt, Location loc){
    	Collection<DependenceNode> nodes = new ArrayList<DependenceNode>(1);
    	DependenceNode src = _pdg.getStmtBindingNode(stmt);
    	nodes.add(src);    	
    	return nodes;
    }
    
    /** Add a data dependence edge. */
    protected void addDataDep(DependenceNode dest,Unit srcStmt,Location loc){    	
    	Collection<DependenceNode> srcNodes = getDefinitionNodes(srcStmt,loc);
    	for(DependenceNode src: srcNodes){
        	DependenceEdge edge = null;
        	if(_pdgOptions.withDependReason()){
        		edge = new DataDependenceEdge(src,dest,loc);
        	}else{
        		if(loc instanceof StackLocation){
        			edge = new DataDependenceEdge(src,dest,loc);
        		}
        		else{
        			edge = new DataDependenceEdge(src,dest,null);
        		}        		
        	}
        	
        	_pdg.addEdge(edge);
    	}
    }
    
    protected void clearTemporals(){    
    	_cfg = null;
        _ptsto = null;
        _rd = null;
        _pdgOptions = null;
    }
}