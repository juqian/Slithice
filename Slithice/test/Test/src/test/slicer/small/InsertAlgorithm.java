package test.slicer.small;


/**
 * A insertion sort demonstration algorithm
 * InsertAlgorithm.java, 19.04.97
 *
 * @author   Lars Marius Garshol
 * @version  1.00 - 19.04.97
 */
class InsertAlgorithm extends SortAlgorithm {
  void sort(int a[]) throws Exception {
    int tmp; //The number currently being sorted is stored here while we make room for it   
    int j;
 
    for (int i=1; i<=a.length; i++) {
      // Invariant: a[0..i-1] sorted

      tmp=a[i];

      for (j=i-1; j>=0 && a[j]>tmp; j--) {
        if (stopRequested) {
    	  return;
        }

        a[j+1]=a[j];		        

	pause(j,i);
      }

      //Now we've found a[i]'s place
      a[j+1]=tmp;
    }

  } //end of sort
}

