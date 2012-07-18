package com.conref.sootUtil.graph;

public class GraphNode extends GraphElement{
	private String _label;
	
	public GraphNode(String label){
		this._label = label;
	}
	
	public String getLabel(){
		return _label;
	}
	
	public String getTip(){
		return "";
	}
	

}
