package test.slicer.java5;

public class Varargs {
	static int vararg_callee(int... n) {  //传过来n为一个int型数组
        int tempSum=0;
        int c = n.length;
        for(int i=0; i<c; i++) {
           int option = n[i];
           tempSum += option;
        }
        
        return tempSum;
    }
    
    static void vararg(){
        vararg_callee(1);
        vararg_callee(1,2);
        vararg_callee(1,2,3,4);
    }
   
    static void printf(){       
        int x = 10;
        int y = 20;
        int sum = x + y;
        System.out.printf("%d + %d = %d",x,y,sum);
    }
    

	public static void main(String[] args) {
		vararg();
		printf();
	}

}
