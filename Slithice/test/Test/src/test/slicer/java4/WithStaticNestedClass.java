package test.slicer.java4;

public class WithStaticNestedClass {
	public static void main(String[] args) {
		B.C[] c = new B.C[1];
		B.test1(c);
	}
	
	static class B{
		static class C{
			static int A(int x, int y){
			    x = add(x, y);
			    y = increment(y);
			    return x;
			}
		}		
		
		static int add (int a, int b){
		    return a + b;
		}		
		
		static void test1(C[] c){
			int sum = 0;
			int i = 1;
			while(i < 11){
				sum = C.A(sum, i);
			}	
		}
	}
	
	
	private static int increment(int z){
		return B.add(z, 1);
	}
}
