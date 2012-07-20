package jqian.sootex.util;

import soot.AbstractUnit;
import soot.UnitPrinter;

@SuppressWarnings("unchecked")
public class CFGExit extends AbstractUnit {
	private static final long serialVersionUID = -6197401790403627819L;

	private static CFGExit _instance=new CFGExit();
    public static CFGExit v(){return _instance;}
    
    public Object clone(){       
        throw new RuntimeException("Entry clone not support.");
    }

    /** @see soot.Unit#fallsThrough() */
    public boolean fallsThrough() {        
        return true;
    }

    /** @see soot.Unit#branches() */
    public boolean branches() {       
        return false;
    }

    /** @see soot.Unit#toString(soot.UnitPrinter)  */
    public void toString(UnitPrinter up) {        
    }
    
    public String toString(){
        return "exit";
    }
}