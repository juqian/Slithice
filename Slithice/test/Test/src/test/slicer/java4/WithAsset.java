package test.slicer.java4;

public class WithAsset {
	public static void main(String[] args) {
		test1();
	}
	
	private static void test1(){
		int sum = 0;
		int i = 1;
		while(i < 11){
			assert sum>0;
			sum = A(sum, i);
		}	
	}
	
	private static int A(int x, int y){
	    x = add(x, y);
	    y = increment(y);
	    return x;
	}
	
	private static int add (int a, int b){
	    return a + b;
	}
	
	private static int increment(int z){
		return add(z, 1);
	}
}
