package jqian.sootex.dependency.pdg;

import java.util.*;
import jqian.sootex.location.GlobalLocation;
import jqian.sootex.location.HeapLocation;
import jqian.sootex.location.MethodRet;
import jqian.sootex.location.StackLocation;
import jqian.sootex.util.NumberableComparator;
import jqian.sootex.util.SootUtils;
import jqian.util.*;
import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.*;


/**
 * A context-sensitive procedure dependence graph. 
 * 
 * DependenceEdge(s) are shared in the DEPEND_BY set of the from node,
 * and the DEPEND_ON set of the to node. 
 */
public class PDG implements DependenceGraph{    
    private Collection<DependenceNode> _nodes;    
    private Map<DependenceNode,Collection<DependenceEdge>> _edgesOut;  //edges out of each node
    private Map<DependenceNode,Collection<DependenceEdge>> _edgesIn;   //edges into each node
    
    private Map<Object,DependenceNode> _obj2node;     //from the binding object to node for fast access
    private Map<Object,FormalNode> _loc2formalIn;     //from binding object to FormalIn node
    private Map<Object,FormalNode> _loc2formalOut;    //from binding object to FormalOut node
    private Map<Argument,ActualNode> _arg2actualIn;
    private Map<Argument,ActualNode> _arg2actualOut;    
  
    private int _edgeCount[] = new int[5];
    private EntryNode _entry;
    private MethodOrMethodContext _mc;
    
    
	public PDG(MethodOrMethodContext mc){
        this._mc = mc;
        
        _nodes =  new TreeSet<DependenceNode>(NumberableComparator.v());
        _loc2formalIn = new HashMap<Object,FormalNode>(); 
        _loc2formalOut = new HashMap<Object,FormalNode>();
        
        _edgesOut = new HashMap<DependenceNode,Collection<DependenceEdge>>();     
        _edgesIn = new HashMap<DependenceNode,Collection<DependenceEdge>>();     
        
        _obj2node = new HashMap<Object,DependenceNode>();
        _arg2actualIn = new HashMap<Argument,ActualNode>();
        _arg2actualOut = new HashMap<Argument,ActualNode>();  
        
        //build the entry node      
        _entry = new EntryNode(_mc);
        addNode(_entry);
    }
    
	/** Compact PDG, reduce memory consummation when the PDG becomes stable. */
    public void compact(){
    	// Use a compact node set
    	_nodes = new ArrayList<DependenceNode>(_nodes);
    	
    	// compact edge set for non interface nodes
    	// only ActualNode may update its edges later during SDG construction
    	for(Map.Entry<DependenceNode, Collection<DependenceEdge>> e: _edgesIn.entrySet()){
    		DependenceNode n = e.getKey();
    		if(!(n instanceof ActualNode)){
    			Collection<DependenceEdge> edges = e.getValue();
        		edges = new ArrayList<DependenceEdge>(edges);
        		e.setValue(edges);
    		}
    	}
    	
    	for(Map.Entry<DependenceNode, Collection<DependenceEdge>> e: _edgesOut.entrySet()){
    		DependenceNode n = e.getKey();
    		if(!(n instanceof ActualNode)){
        		Collection<DependenceEdge> edges = e.getValue();
        		edges = new ArrayList<DependenceEdge>(edges);
        		e.setValue(edges);
    		}
    	}
    }

    /**Adds a node to the graph.*/ 
    public boolean addNode(DependenceNode node){
    	Object binding = node.getBinding();    	
    	
    	if(node instanceof FormalIn){
    		_loc2formalIn.put(binding,(FormalIn)node);
    	}
    	else if(node instanceof FormalOut){
    		_loc2formalOut.put(binding,(FormalOut)node);
    	}
    	else if(node instanceof ActualIn){
    		ActualNode actual = (ActualNode)node;
    		Argument arg = new Argument(actual.getCallSite(),actual.getCallee(),binding);
    		_arg2actualIn.put(arg, (ActualIn)node);
    	}
    	else if(node instanceof ActualOut){
    		ActualNode actual = (ActualNode)node;
    		Argument arg = new Argument(actual.getCallSite(),actual.getCallee(),binding);
    		_arg2actualOut.put(arg, (ActualOut)node);
    	}
    	else{
    		//FIXME: Can be dangerous for CallNodes
    		_obj2node.put(binding,node);
    	}
    	
        return _nodes.add(node);
    }    
    
    /** Determine whether this is a concrete PDG with modeling to the method body. 
     *  Some PDG(s) only have FormalNode(s) to model the interface of the method.
     */
    public boolean containsMethodBody(){
    	return _obj2node.size()>1;
    }
      
    public DependenceNode entry(){
        return _entry;
    }
    
    public Collection<DependenceNode> getNodes(){
        return _nodes;
    }  
    
    /**
	 * Get the binding DependenceNode of an object. Used for easy access to the PDG.
     * 
     * @param stmt An unit can bind to a JimpleStmtNode
     *             A Integer line can bind to a JavaStmtNode
     */
    public DependenceNode getStmtBindingNode(Object stmt){
    	return _obj2node.get(stmt);
    }
    
    /** Get the corresponding formal nodes of the given binding object <code>binding</code>.*/
    public FormalNode getBindingFormal(Object binding, boolean isGetFormalIn){
    	if(isGetFormalIn)
    	    return _loc2formalIn.get(binding);
    	else
    		return _loc2formalOut.get(binding);
    }
    
    /** Get the corresponding actual nodes of the given information.*/
    public ActualNode getBindingActual(Unit callsite,SootMethod callee,Object loc, boolean isGetActualIn){
    	Argument arg = new Argument(callsite,callee,loc);
    	if(isGetActualIn) 
    		return _arg2actualIn.get(arg);    	
    	else    		
    		return _arg2actualOut.get(arg);
    }
    
    private Set<DependenceEdge> createEdgeSet(){
    	return new HashSet<DependenceEdge>();
    }
    
    ////////////////// Edges ///////////////////
    /**
     * @return true if there is not such edge yet.
     */
    public boolean addEdge(DependenceEdge edge) {
    	DependenceNode from = edge.getFrom();
    	DependenceNode to = edge.getTo();
    	
    	boolean exist = true;
    	Collection<DependenceEdge> outs = _edgesOut.get(from);
    	if(outs==null){
    		outs = createEdgeSet();
    		_edgesOut.put(from,outs);
    		exist = false; 
    	}
    	Collection<DependenceEdge> ins = _edgesIn.get(to);
    	if(ins==null){
    		ins = createEdgeSet();
    		_edgesIn.put(to,ins);
    		exist = false;
    	}
    	
    	exist = exist && outs.contains(edge); 
    	if(!exist){
    		outs.add(edge);
    		ins.add(edge);
    		
    		if(edge instanceof CtrlDependenceEdge){
    			_edgeCount[DependenceEdge.CTRL_DEP]++;
    		}else{
    			DataDependenceEdge dataEdge = (DataDependenceEdge)edge;
    			Object loc = dataEdge.getReason();
    			if(loc==null)
    				_edgeCount[DependenceEdge.UNDISTINGUSIHED_DATA_DEP]++;
    			else if(loc instanceof StackLocation || loc instanceof MethodRet)
    				_edgeCount[DependenceEdge.STACK_DEP]++;
    			else if(loc instanceof HeapLocation || loc instanceof SootField || loc instanceof Type)
    			    _edgeCount[DependenceEdge.HEAP_DEP]++;
    			else if(loc instanceof GlobalLocation)
    				_edgeCount[DependenceEdge.GLOBAL_DEP]++;
    			else 
    				throw new RuntimeException("Stange location");
    		}
    	}     	
    	
        return !exist;
    }   
   
    public int getEdgeCount(){
        int sum=0;
        for(int i=0;i<_edgeCount.length;i++)
        	sum += _edgeCount[i];           
        return sum;
    }
    
    public int getEdgeCount(int type){
    	return _edgeCount[type];
    }
    
    public static void updateStatistic(int edgeCount[], PDG pdg){
		int count = pdg.getEdgeCount(DependenceEdge.CTRL_DEP);
        edgeCount[DependenceEdge.CTRL_DEP] += count;
        
        count = pdg.getEdgeCount(DependenceEdge.GLOBAL_DEP);
        edgeCount[DependenceEdge.GLOBAL_DEP] += count;
        
        count = pdg.getEdgeCount(DependenceEdge.HEAP_DEP);
        edgeCount[DependenceEdge.HEAP_DEP] += count;
        
        count = pdg.getEdgeCount(DependenceEdge.STACK_DEP);
        edgeCount[DependenceEdge.STACK_DEP] += count;
        
        count = pdg.getEdgeCount(DependenceEdge.UNDISTINGUSIHED_DATA_DEP);
        edgeCount[DependenceEdge.UNDISTINGUSIHED_DATA_DEP] += count;
	}
	
	public static String statisticsToString(int edgeCount[]){
		 String ret = "ctrl "+ edgeCount[DependenceEdge.CTRL_DEP]
		              +", stack "+edgeCount[DependenceEdge.STACK_DEP] 
		              +", heap "+ edgeCount[DependenceEdge.HEAP_DEP]
		              +", global "+ edgeCount[DependenceEdge.GLOBAL_DEP]
		              +", undistinguished " + edgeCount[DependenceEdge.UNDISTINGUSIHED_DATA_DEP];
		 return ret;		                                                                    
	}
	
	public String statistcToString(){
		return statisticsToString(_edgeCount);
	}
    
    public Collection<DependenceEdge> edgesInto(DependenceNode node){
    	Collection<DependenceEdge> edges = _edgesIn.get(node);
    	if(edges!=null)
    		return edges;
    	else
    		return Collections.emptySet();
    }
    
    public Collection<DependenceEdge> edgesOutOf(DependenceNode node){
    	Collection<DependenceEdge> edges = _edgesOut.get(node);
    	if(edges!=null)
    		return edges;
    	else
    		return Collections.emptySet();
    }
    
    ///////////////////////////////////////////////////////
    public PDG toJavaStmtDepGraph(){    	
    	return toJavaStmtDepGraph(null);
    }
    
    /**
     * Change to the PDG with .java line as node. The returned dependence graph is easy for understanding,
     * but it is not supposed to be used in slicing.
     * @param  old2NewForOutVisiables  Map old node to new node. Only the out visible dependence nodes
     *         such as FormalNode, ActualNode, EntryNode, and CallNode are concerned.
     *         This parameter can be null.
     */
    public PDG toJavaStmtDepGraph(Map<DependenceNode,DependenceNode> old2NewForOutVisiables){
        PDG depGraph = new PDG(_mc);  
        
        Map<DependenceNode,DependenceNode> old2New=new HashMap<DependenceNode,DependenceNode>(_nodes.size()*2+1,0.7f);
        Map<Integer,DependenceNode> line2Node=new HashMap<Integer,DependenceNode>();
        
        //depGraph._entry =( EntryNode)_entry.clone();  
        //depGraph.addNode(depGraph._entry);        
        old2New.put(_entry, depGraph._entry);        
        
        //build node map
        for(DependenceNode node: _nodes){
            DependenceNode newNode=null;
            
            if(node instanceof JimpleStmtNode){
            	JimpleStmtNode jmpNode=(JimpleStmtNode)node;    
            	Unit unit=jmpNode.getStmt();
            	if(unit instanceof IdentityStmt || unit instanceof ReturnVoidStmt){
            		newNode = depGraph.entry();
            	}
            	else{
            	    newNode = assureJavaNode(depGraph,line2Node,SootUtils.getLine(unit));
            	}
            }
            else{
            	if(node instanceof FormalNode){
            		newNode = depGraph._entry;
            	}
            	else if(node instanceof CallNode){
                	Unit callsite = ((CallNode)node).getCallSite();
                	newNode = assureJavaNode(depGraph,line2Node,SootUtils.getLine(callsite)); 
                	JavaStmtNode javaNode = (JavaStmtNode)newNode;
                	javaNode.setCallSiteFlag(true);
                }
                else if(node instanceof ActualNode){
                	ActualNode actual = (ActualNode)node;
                	Unit callsite = actual.getCallSite();
                	newNode = assureJavaNode(depGraph,line2Node,SootUtils.getLine(callsite));
                }
                else if(node instanceof EntryNode){
                	newNode = depGraph.entry();
                }
                else{
                	newNode = (DependenceNode)node.clone();
                    depGraph.addNode(newNode);
                }                
                
                if(old2NewForOutVisiables!=null){
                	old2NewForOutVisiables.put(node, newNode);
                }
            }  
            
            old2New.put(node,newNode);           
        } 
        
        //reconstruct edge
        for(DependenceNode node: _nodes){            
            Collection<DependenceEdge> outs = _edgesOut.get(node);
            if(outs==null)
            	continue;
            
            for (DependenceEdge edge: outs) {
				// map to new edge
				DependenceNode from = (DependenceNode) old2New.get(node);
				DependenceNode to = (DependenceNode) old2New.get(edge.getTo());

				DependenceEdge newEdge = null;
				if (edge instanceof DataDependenceEdge) {
					DataDependenceEdge dataEdge = (DataDependenceEdge) edge;
					Object reason = dataEdge.getReason();

					// ignore data dependence caused by temporal variables
					if (reason == null || reason.toString().charAt(0) != '$') {
						if (edge instanceof SummaryEdge) {
							newEdge = new SummaryEdge(from, to, reason);
						} else {
							newEdge = new DataDependenceEdge(from, to, reason);
						}
					}
				} 
				else if (edge instanceof CtrlDependenceEdge && from != to) {
					newEdge = new CtrlDependenceEdge(from, to);
				}

				if (newEdge != null) {
					depGraph.addEdge(newEdge);
				}
			}
        }
        
        return depGraph;
    }
    
    private DependenceNode assureJavaNode(PDG depGraph,Map<Integer,DependenceNode> line2Node,int line){
    	DependenceNode newNode = line2Node.get(line);
		if (newNode == null) {
			newNode = new JavaStmtNode(_mc,line);
			line2Node.put(line, newNode);
			depGraph.addNode(newNode);
		}
		
		return newNode;
    }
    
    
    class PdgDirectedGraph implements DirectedGraph<DependenceNode>{    	 
    	public List<DependenceNode> getHeads() {
    		List<DependenceNode> list = new ArrayList<DependenceNode>(1);
    		list.add(_entry);
    		return list;
    	}
    	
    	/** No tails at all. */
    	public List<DependenceNode> getTails() {    	
    		return Collections.emptyList();
    	}

    	public List<DependenceNode> getPredsOf(DependenceNode s) {
    		List<DependenceNode> ins = new LinkedList<DependenceNode>();
    		Collection<DependenceEdge> edges = PDG.this._edgesIn.get(s);
    		for(DependenceEdge e: edges){    		 
    			ins.add(e.getFrom());
    		}
    		
    		return ins;
    	}

    	public List<DependenceNode> getSuccsOf(DependenceNode s) {
    		List<DependenceNode> outs = new LinkedList<DependenceNode>();
    		Collection<DependenceEdge> edges = PDG.this._edgesOut.get(s);
    		if(edges!=null){
    			for(DependenceEdge e: edges){    		 
    				outs.add(e.getTo());
    			}
    		}
    		
    		return outs;
    	}

    	public Iterator<DependenceNode> iterator() {
    		return PDG.this._nodes.iterator();
    	}

    	public int size() {
    		return PDG.this._nodes.size();
    	}  
    }
    
    /**Get a graph compatible with the DirectedGraph interface*/
    public DirectedGraph<DependenceNode> toDirectedGraph(){
        return new PdgDirectedGraph();
    }
    
    public String toString(){
        StringBuffer str = new StringBuffer("PDG of "+_mc+"\n");
        for(DependenceNode node: _nodes){
        	str.append("\n\nNode: "+node.toString());
        	str.append("\nIncomming edges:");
        	Collection<DependenceEdge> ins = _edgesIn.get(node);
        	if(ins!=null){
        		str.append("("+ins.size()+")\n");
        	    str.append(CollectionUtils.toString(ins.iterator(),"\n"));
        	}
        	str.append("\nOutcomming edges:");
        	Collection<DependenceEdge> outs = _edgesOut.get(node);
        	if(outs!=null){
        		str.append("("+outs.size()+")\n");
        	    str.append(CollectionUtils.toString(outs.iterator(),"\n"));
        	}
        }
        
        return str.toString();
    }
    
    public Collection<FormalNode> getFormalIns(){
    	return _loc2formalIn.values();
    }
    
    public Collection<FormalNode> getFormalOuts(){
    	return _loc2formalOut.values();
    }
    
    public void getActualNodes(Collection<ActualNode> out){
    	out.addAll(_arg2actualIn.values());
    	out.addAll(_arg2actualOut.values());
    }    
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void getActualIns(Collection out,Unit callsite){
    	for(Map.Entry<Argument,ActualNode> entry: _arg2actualIn.entrySet()){
    		Argument arg = entry.getKey();
    		if(arg._callsite==callsite){
    			out.add(entry.getValue());
    		}
    	}
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void getActualOuts(Collection out,Unit callsite){
    	for(Map.Entry<Argument,ActualNode> entry: _arg2actualOut.entrySet()){
    		Argument arg = entry.getKey();
    		if(arg._callsite==callsite){
    			out.add(entry.getValue());
    		}
    	}
    } 
    
    public void getActualOutsOfReturnValue(Collection<DependenceNode> out,Unit callsite){
    	for(Map.Entry<Argument,ActualNode> entry: _arg2actualOut.entrySet()){
    		Argument arg = entry.getKey();
    		ActualNode node = entry.getValue();
    		Object formalBinding = node.getFormalBinding();
    		if(arg._callsite==callsite && formalBinding instanceof MethodRet){
    			out.add(node);
    		}
    	}
    } 
    
    
    private static final class Argument{
    	//The argument location can be a Location or even a SootField which standing for a collection of Locations
    	public Argument(Unit callsite,SootMethod callee,Object loc){
    		this._callsite = callsite;
    		this._callee = callee;
    		this._loc = loc;
    	}
    	
    	public boolean equals(Object obj){
    		Argument that = (Argument)obj;
    		if(this._callsite==that._callsite &&
    		   this._callee==that._callee && this._loc==that._loc){
    			return true;
    		}
    		
    		return false;
    	}
    	
    	public int hashCode(){
    		int hash = 3*_callsite.hashCode()+7*_callee.getNumber();
    		if(_loc!=null) 
    			hash += 11*_loc.hashCode();
    		return hash;
    	}
    	
    	Unit _callsite;
    	SootMethod _callee;
    	Object _loc;
    }
}
