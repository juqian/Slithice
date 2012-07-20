package test.cases;

/** 
 * Test the process of threads. 
 * Handling multi-threaded executions can be very complex.
 * Thread.Thread() has huge side-effect set, therefore the PDG may fail to be displayed
 */
public class SDG2{
	static int result;

	
	public static void main(String[] args) {
		test1();
		test2();
	}
	
	static void test1(){
		A a = new A(result);
		a.start();
	
		result = a.val;
	}

	static class A extends Thread{	
		int val;
		
		public A(int i){
			val = i;
		}
		
		public void run(){
			int MAX = 1000;
			int sum = 0;
			for(int i=0;i<MAX;i++){
				sum += i;
			}
			
			val += sum;
		}
	}

	static void test2(){
		B run = new B(result);
		Thread s = new Thread(run);
		s.start();
		
		result -= run.b;
	}
	
	static class B implements Runnable{
		int b;
		
		public B(int i){
			b = i;
		}
		public void run(){
			int MIN = 1;
			int sum = 2000;
			for(int k=10000;k>MIN;k--){
				sum -= k;
			}
			
			b -= sum;
		}
	}
}

