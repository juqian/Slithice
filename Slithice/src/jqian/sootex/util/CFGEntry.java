package jqian.sootex.util;

import soot.AbstractUnit;
import soot.UnitPrinter;

@SuppressWarnings("unchecked")
public class CFGEntry extends AbstractUnit {
	private static final long serialVersionUID = -5838338765092377994L;

	private static CFGEntry _entry = new CFGEntry();
    public static CFGEntry v(){return _entry;}
    
	private CFGEntry(){}
	
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
        return "entry";
    }
}