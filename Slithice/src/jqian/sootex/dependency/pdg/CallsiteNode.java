package jqian.sootex.dependency.pdg;

import java.util.ArrayList;
import java.util.Collection;

import soot.MethodOrMethodContext;
import soot.Unit;

/**
 * A unique node to model a callsite. Each callee is modeled by a CallNode
 */
public class CallsiteNode extends JimpleStmtNode {
	protected final Collection<CallNode> _calleeNodes;
	
	public CallsiteNode(MethodOrMethodContext mc, Unit callsite) {
		super(mc, callsite);

		_calleeNodes = new ArrayList<CallNode>();
	}
	
	public void addCalleeNode(CallNode calleeNode){
		_calleeNodes.add(calleeNode);
	}

	public Collection<CallNode> getCalleeNodes(){
		return _calleeNodes;
	}
	
	// XXX: here miss callee nodes. Currently no problem, but may cause bugs in the future
	public Object clone(){
		CallsiteNode node = new CallsiteNode(_mc, _stmt);
		return node;
	}
}
