
__kernel void throwDartsInt(__global int *seeds, const int repeats, __global int *output){
			
	int gid = get_global_id(0);
	long rand = seeds[gid];
	int dart = 0;
	
	for (int iter = 0; iter < repeats; iter++) {
		
		rand = 1103515245 * rand + 12345;
		int x = (rand & 0xfff);//0..2^12, square of x is 0..2^24
		rand = 1103515245 * rand + 12345;
		int y = (rand & 0xfff);// 0..2^12, square of y is 0..2^24
			
		if (x*x + y*y <=0xffffff) {// 2^24
			dart++;
		}
	}
	output[gid] = dart;
			
 }
 