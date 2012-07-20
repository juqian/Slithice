package test.slicer.small;


public interface ISortItem extends Runnable {
	void pause(int H1, int H2) ;
	
	int getH1();
	int getH2();
	
	static class Impl{
		public static SortAlgorithm getAlgorithm(String name){
			int i = 0;
			i ++;
			
			switch(i){
			case 1: return new BidirBubbleSortAlgorithm();
			case 2: return new BubbleSortAlgorithm();
			case 3: return new HeapAlgorithm();
			case 4: return new InsertAlgorithm();
			case 5: return new QSortAlgorithm();
			case 6: return new ShellAlgorithm();
			}
			
			return null;
		}
	}	
}
