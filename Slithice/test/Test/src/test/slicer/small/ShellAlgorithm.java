package test.slicer.small;


/**
 * A Shellsort demonstration algorithm
 * ShellAlgorithm.java, 19.04.97
 *
 * @author   Lars Marius Garshol
 * @version  1.00 - 19.04.97
 */

class ShellAlgorithm extends SortAlgorithm {
  void sort(int a[]) throws Exception {
    int h[]={109,41,19,5,1}; //Best Sedgewick sort increment sequence
    int tmp,j;
    int incno; //Increment number

    //Find right start increment
    for (tmp=0; h[tmp]>a.length; tmp++)
      ;

    //Loop through increment sequence 
    for (incno=tmp; incno<=h.length-1; incno++) {

      for (int i=h[incno]; i<=a.length-1; i++) {
        // Invariant: a[start..i-h[incno]] h[incno]-sorted
    
        tmp=a[i];   
        for (j=i-h[incno]; j>=0 && a[j]>tmp; j=j-h[incno]) {
          if (stopRequested) {
            return;
          }

          a[j+h[incno]]=a[j];		        

          pause(j,i);
        }
 
        //Now we've found a[i]'s place
        a[j+h[incno]]=tmp;

      } //for i

    } //for incno

  } //end of sort
}

