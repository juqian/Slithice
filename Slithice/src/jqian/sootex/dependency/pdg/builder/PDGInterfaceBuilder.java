package jqian.sootex.dependency.pdg.builder;

import java.util.Collection;
import jqian.sootex.dependency.pdg.*;
import soot.SootMethod;


/**
 * Just build a PDG interface with FormalIn and FormalOut nodes.
 * No internal nodes are built. Such PDG can be used for conservatively analyzing library codes.
 */
public class PDGInterfaceBuilder extends PDGBuilder {
    public PDGInterfaceBuilder(SootMethod m, DepGraphOptions pdgOptions, SDGBuilder sdgBuilder){ 
    	super(m,null, pdgOptions, null, null, null, sdgBuilder); 
    }  
    
    
    public void build(){
        buildFormals();
        buildFormalInDependences();         
        buildFormalOutDependences();  
        clearTemporals();
    }  
  
    
    /** Assume each formal out depends on all formal in. */
    protected void buildFormalOutDependences(){    	
    	DependenceNode entry = _pdg.entry();
    	Collection<FormalNode> ins = _pdg.getFormalIns();
    	Collection<FormalNode> outs = _pdg.getFormalOuts();
    	
    	for(FormalNode fout: outs){    
    		//build control dependence
            DependenceEdge cd = new CtrlDependenceEdge(entry, fout);
			_pdg.addEdge(cd);  
			
			//depend on all formal in
			for(FormalNode fin: ins){       
				DependenceEdge dd = new DataDependenceEdge(fin,fout,null);
				_pdg.addEdge(dd); 
        	}
    	}
    } 
}
