/**
 * 
 */
package test.slicer.java5;

/**
 * @author bruteforce
 *
 */
public class EnumType {
	public enum MyColors {
        red,
        black,
        blue,
        green,
        yellow
    }
    
    static void testEnum(){
        MyColors color = MyColors.red;
        
        for(MyColors option : MyColors.values()) {
            System.out.println(option);
        }    

        switch(color) {
        case red: 
        	System.out.println(MyColors.red); 
        	break;
        case black: 
        	System.out.println(MyColors.black); 
        	break;
        default:  break;
        }
    }

    static void testEnum2(MyColors color){        
            System.out.println(color);
        
    }
    
	public static void main(String[] args) {
		testEnum();
		testEnum2(null);
	}

}
