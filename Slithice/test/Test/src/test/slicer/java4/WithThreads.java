package test.slicer.java4;

public class WithThreads {
	static int result;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WithThreads t = new WithThreads();
		t.start();
	}
	
	void start(){
		ThreadA a = new ThreadA(result);
		a.start();
		
		int k = a.val;
		k++;
		
		result = a.getResult();
		
		ThreadB run = new ThreadB(result);
		Thread s = new Thread(run);
		s.start();
		
		result -= run.getResult();
	}
	
	class ThreadA extends Thread{	
		int val;
		
		public ThreadA(int i){
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
		
		public int getResult(){
			return val;
		}
	}

	class ThreadB implements Runnable{
		int b;
		
		public ThreadB(int i){
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
		
		public int getResult(){
			return b;
		}
	}
}
