
__kernel void throwDartsFloat(__global int *seeds, const int repeats, __global int *output){
					
	int gid = get_global_id(0);
	long rand = seeds[gid];
	int dart = 0;
					
	for (int iter = 0; iter < repeats; iter++) {
		
		rand = 1103515245 * rand + 12345;
		float x = ((float) (rand & 0xffffff)) / 0x1000000; // extract 24 bits for x
		rand = 1103515245 * rand + 12345;
		float y = ((float) (rand & 0xffffff)) / 0x1000000; // extract 24 bits for y
					
		if (x*x + y*y <=1.0) {
			dart++;
		}
	}
	output[gid] = dart;

}
		