package jqian.sootex.util.graph;

import java.util.*;
import soot.toolkits.graph.*;


/**
 * Floyd-Warshall path table 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class PathTable implements IPathQuery{   
	public PathTable(DirectedGraph graph){      
        //establish the map between an Unit and its index
        int nodeNum=graph.size();   
        _entrys.addAll(graph.getHeads());
        
        //the hash map is initialized as examples in soot 2.2.1 tuturial
        _node2Int=new HashMap(nodeNum*2 + 1, 0.7f);   
        _id2Node=new Object[nodeNum];
        int i=0;
        for (Iterator it =graph.iterator(); it.hasNext(); i++) {
            Object node = it.next();
            _node2Int.put(node, new Integer(i));
            _id2Node[i] = node;
        }
        
        //Use floyd-warshall algortihm to build a path table
        //init the path matrix
        _pathTbl = new boolean[nodeNum][nodeNum];
        for (i = 0; i < nodeNum; i++) {
            Object node = _id2Node[i];
            List succs = graph.getSuccsOf(node);
            for (Iterator it = succs.iterator(); it.hasNext();) {
                Object to = it.next();
                int toIndex =((Integer)_node2Int.get(to)).intValue();
                _pathTbl[i][toIndex] = true;
            }
        }

        //Floyd-WarShall
        for (i = 0; i < nodeNum; i++){
            for (int j = 0; j < nodeNum; j++) {
                if (_pathTbl[j][i]) {
                    for (int k = 0; k < nodeNum; k++)
                        _pathTbl[j][k] = _pathTbl[j][k] || _pathTbl[i][k];
                }
            }
        }
    }    
    
    public int getNodeId(Object obj){
        return ((Integer)_node2Int.get(obj)).intValue();        
    }
    
    public boolean hasPath(Object src,Object dest){
        int srcIndex=((Integer)_node2Int.get(src)).intValue();
        int destIndex=((Integer)_node2Int.get(dest)).intValue();
        return _pathTbl[srcIndex][destIndex];
    }
    
    public boolean hasPath(int src,int dest){       
        return _pathTbl[src][dest];
    }
    
    public boolean reachableFromHead(int nodeId){       
        for(Iterator it=_entrys.iterator();it.hasNext();){
            Object entry= it.next();
            int entryId=((Integer)_node2Int.get(entry)).intValue();
            if(entryId==nodeId) return true;
            if(hasPath(entryId,nodeId))  return true;             
        }
        return false;
    }
       
    public String toString(){
        String str="\n----------------------Nodes--------------------------";
        for(int i=0;i<_id2Node.length;i++){
            str+="\n["+i+"] ";
            if(_id2Node[i]!=null)
                str+=_id2Node[i].hashCode()+": "+_id2Node[i].toString();
        }       
        str+="\n--------------------Path Table----------------------\n  ";
    	for(int i=0;i<_id2Node.length;i++)	{
           str+=" "+formatString(2,i);
    	}
    	for(int i=0;i<_id2Node.length;i++){
    		str+="\n"+formatString(2,i);
    		for(int j=0;j<_id2Node.length;j++){
    			char c=_pathTbl[i][j]? 'X':' ';
    			str+="  "+c;
    		}
    	}
    	str+="\n--------------------End Table-----------------------";
    	return str;
    }
    
    private String formatString(int setw,int val){
        String str=String.valueOf(val);
        while(str.length()<setw){
            str=" "+str;  //fill the left with blank
        }
        return str;
    }
    
    /////////////////////////////////////////////////////////
    private boolean _pathTbl[][];  
    private	Map/*<Object,Integer>*/ _node2Int;
    private	Object[] _id2Node;
    private List _entrys=new LinkedList();
}
