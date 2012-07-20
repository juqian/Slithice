package test.slicer.small;

/**
 * A Heapsort demonstration algorithm HeapAlgorithm.java, 19.04.97
 * 
 * @author Lars Marius Garshol
 * @version 1.00 - 19.04.97
 */

class HeapAlgorithm extends SortAlgorithm {
	void sort(int a[]) throws Exception {
		int i;

		// ----Step 1: Make a heap in linear time. Top of heap at top of array.
		// Largest numbers at the top
		for (i = a.length / 2; i >= 0; i--) {
			perc_down(i, a, a.length - 1);
		}

		// ----Step 2: Take out elements one by one, largest first, and put them
		// at the bottom
		for (i = a.length - 1; i >= 0; i--) {
			delete_max(i, a); // Places max element at a[i]
		}
	}

	void delete_max(int ix, int a[]) throws Exception {
		int ret;

		ret = a[0]; // Return value
		a[0] = a[ix];
		perc_down(0, a, ix - 1); // Percolate last value down
		a[ix] = ret;
	}

	// Have to go to i*2+1 and i*2+2 because we start at 0
	// lng is the last index we can touch, the end of the heap
	void perc_down(int ix, int a[], int lng) throws Exception {
		int i, tmp;

		tmp = a[ix];
		i = ix;
		while (i * 2 + 1 <= lng) {
			if (i * 2 + 1 == lng || a[i * 2 + 1] > a[i * 2 + 2]) {
				if (a[i * 2 + 1] < tmp)
					break; // Position found
				a[i] = a[i * 2 + 1];
				i = i * 2 + 1;
			} else {
				if (a[i * 2 + 2] < tmp)
					break; // Position found
				a[i] = a[i * 2 + 2];
				i = i * 2 + 2;
			}

			pause(i, ix);
		}

		a[i] = tmp;
	}
}
