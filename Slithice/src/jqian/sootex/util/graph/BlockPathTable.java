package jqian.sootex.util.graph;

import java.util.*;

import jqian.sootex.util.CFGEntry;
import jqian.sootex.util.CFGExit;
import soot.*;
import soot.util.*;
import soot.toolkits.graph.*;
/**
 * Check path relation using basic block graph 
 * TODO A Block already has an index with it in Soot, so a more effecient version
 *      may be derived using such information  
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class BlockPathTable implements IPathQuery {
    public BlockPathTable(UnitGraph graph){      
        BlockGraph blockGraph=null;
        if(graph instanceof BriefUnitGraph){
            blockGraph=new BriefBlockGraph((BriefUnitGraph)graph);
        }else{
            throw new RuntimeException(graph.getClass().toString()
                                       +" is currently not supported!");
        }
        
        this._blkNum=blockGraph.size();
        Collection heads=blockGraph.getHeads();
        this._entrys=new int[heads.size()];
        int headCount=0;
        for(Iterator it=heads.iterator();it.hasNext();headCount++){
            Block blk=(Block)it.next();  
            _entrys[headCount]=blk.getIndexInMethod();
        }
        
        this._pathTbl=new BitVector[_blkNum];
        this._unit2Id=new HashMap(graph.size()*2+ 1, 0.7f);  
        for(Iterator it=blockGraph.iterator();it.hasNext();){
            Block blk=(Block)it.next();            
            int blkId=blk.getIndexInMethod();
            
            //init statement to index map
            int i=0;
            for(Iterator blkIt=blk.iterator();blkIt.hasNext();i++){
                Unit unit=(Unit)blkIt.next();                
                _unit2Id.put(unit,new Id(blkId,i));               
            }
            //init path table
            _pathTbl[blkId]=new BitVector(_blkNum);
            List succs = blockGraph.getSuccsOf(blk);
            for (Iterator succIt = succs.iterator(); succIt.hasNext();) {
                Block toBlk=(Block)succIt.next(); 
                int toBlkId=toBlk.getIndexInMethod();
                _pathTbl[blkId].set(toBlkId);
            }
        }
        
        //Use floyd-warshall algortihm to build a path table        
        for (int i = 0; i <_blkNum; i++){
            for (int j = 0; j <_blkNum; j++) {
                if (_pathTbl[j].get(i)) {
                    _pathTbl[j].or(_pathTbl[i]);
                }
            }
        }
    }
            
    /**
     * Checking whether there exist at least a path from a node to another
     * @param src
     * @param dest
     * @return
     */
    public boolean hasPath(Object src,Object dest){
        Unit entry=CFGEntry.v(),exit=CFGExit.v();
        if(src==entry && dest==exit){
            return true;
        }
        else if(src==exit || dest==entry){
        	return false;
        }       
        else if(src==entry){
            Id id=(Id)_unit2Id.get(dest);
            return reachableFromHead(id._blkId);
        } 
        else if(dest==exit){    
            Id id=(Id)_unit2Id.get(src);            
            return reachableFromHead(id._blkId);
        }
        
        Id srcId=(Id)_unit2Id.get(src); 
        Id destId=(Id)_unit2Id.get(dest);    
        
        //if in the same block
        if(src!=dest && srcId._blkId==destId._blkId){            
            return srcId._inBlkId<destId._inBlkId;
        }
        
        return _pathTbl[srcId._blkId].get(destId._blkId);
    }   
    
    public String toString(){    
        String str="\n--------------------Path Table----------------------\n  ";
    	for(int i=0;i<_blkNum;i++)	{
           str+=" "+formatString(2,i);
    	}
    	for(int i=0;i<_blkNum;i++){
    		str+="\n"+formatString(2,i);
    		for(int j=0;j<_blkNum;j++){
    			char c=_pathTbl[i].get(j)? 'X':' ';
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
   
    private boolean reachableFromHead(int nodeId){  
        int entryNum=_entrys.length;
        for(int i=0;i<entryNum;i++){    
            if(nodeId==i)  return true;                
            if(_pathTbl[i].get(nodeId))  return true;             
        }
        return false;
    }
    /////////////////////////////////////////////////////////
    private BitVector _pathTbl[];  
    private int _blkNum;
    private Map/*<Unit,Integer>*/ _unit2Id;    
    private int[] _entrys;
   
    private class Id{
        public Id(int blkId,int inBlkId){
            this._blkId=blkId;
            this._inBlkId=inBlkId;
        }
        public int _blkId;
        public int _inBlkId;
    }
               
       
}
