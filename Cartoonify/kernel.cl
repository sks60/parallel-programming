#define COLOUR_BITS 8
#define COLOUR_MASK 255
#define RED 2
#define GREEN 1
#define BLUE 0
#define GAUSSIAN_SUM 159.0

		
int red(int pixel) {
		return colourValue(pixel, RED);
	}

int green(int pixel) {
		return colourValue(pixel, GREEN);
	}
	
int blue(int pixel) {
		return colourValue(pixel, BLUE);
	}

int createPixel(int redValue, int greenValue, int blueValue) {
		return (redValue << (2 * COLOUR_BITS)) + (greenValue << COLOUR_BITS) + blueValue;
	}
	
int myclamp(double value) {
		int result = (int) (value + 0.5); // round to nearest integer
		if (result <= 0) {
			return 0;
		} else if (result > COLOUR_MASK) {
			return 255;
		} else {
			return result;
		}
	}
	
int colourValue(int pixel, int colour) {
		return (pixel >> (colour * COLOUR_BITS)) & COLOUR_MASK;
	}

int wrap(int pos, int size) {
		if (pos < 0) {
			pos = -1 - pos;
		} else if (pos >= size) {
			pos = (size - 1) - (pos - size);
		}
		
		return pos;
	}
	
int convolution(int xCentre, 
				int yCentre, 
				int* filter, 
				int filterLength, 
				int colour, 
				__global int* pixels, 
				int width, 
				int height) {
		
		int sum = 0;
		// find the width and height of the filter matrix, which must be square.
		int filterSize = 1;
		while (filterSize * filterSize < filterLength) {
			filterSize++;
		}
		
		int filterHalf = filterSize / 2;
		for (int filterY = 0; filterY < filterSize; filterY++) {
			int y = wrap(yCentre + filterY - filterHalf, height); 
			for (int filterX = 0; filterX < filterSize; filterX++) {
				int x = wrap(xCentre + filterX - filterHalf, width);
				int rgb = pixels[y * width + x];
				int filterVal = filter[filterY * filterSize + filterX];
				sum += colourValue(rgb, colour) * filterVal;
			}
		}
		
		return sum;
}


int quantizeColour(int colourValue, int numPerChannel) {
		
		float colour = colourValue / (COLOUR_MASK + 1.0f) * numPerChannel;
		int discrete = round(colour - 0.5f);
		if (discrete <= 0) discrete = 0;
		int newColour = (int)(discrete * COLOUR_MASK) / ((float)(numPerChannel - 1));
		
		return newColour;
	}
	
	
	
__kernel void gaussianBlur(__global int* pixels, 
							__global int* newPixels, 
							int width, 
							int height){
								
		int x = get_global_id(0);
		int y = get_global_id(1);
	
		int GAUSSIAN_FILTER[25] = { 
				2, 4, 5, 4, 2, // sum=17
				4, 9, 12, 9, 4, // sum=38
				5, 12, 15, 12, 5, // sum=49
				4, 9, 12, 9, 4, // sum=38
				2, 4, 5, 4, 2 // sum=17
		};
	
		int red = myclamp(convolution(x, y, GAUSSIAN_FILTER, 25, RED, pixels, width, height) / GAUSSIAN_SUM);
		int green = myclamp(convolution(x, y, GAUSSIAN_FILTER, 25, GREEN, pixels, width, height) / GAUSSIAN_SUM);
		int blue = myclamp(convolution(x, y, GAUSSIAN_FILTER, 25, BLUE, pixels, width, height) / GAUSSIAN_SUM);
		
		newPixels[y * width + x] = createPixel(red, green, blue);
		
}



__kernel void sobelEdgeDetect(__global int* pixels, 
								__global int* newPixels, 
								int width, 
								int height,
								int edgeThreshold) {

		int x = get_global_id(0);
		int y = get_global_id(1);
			
		int SOBEL_VERTICAL_FILTER[9] = {
				-1,  0, +1,
				-2,  0, +2,
				-1,  0, +1
		};
			
		int SOBEL_HORIZONTAL_FILTER[9] = {
				+1, +2, +1,
				0,  0,  0,
				-1, -2, -1
		};
				 
				 
	
		int redVertical = convolution(x, y, SOBEL_VERTICAL_FILTER, 9, RED, pixels, width, height);
		int greenVertical = convolution(x, y, SOBEL_VERTICAL_FILTER, 9, GREEN, pixels, width, height);
		int blueVertical = convolution(x, y, SOBEL_VERTICAL_FILTER, 9, BLUE, pixels, width, height);
		int redHorizontal = convolution(x, y, SOBEL_HORIZONTAL_FILTER, 9, RED, pixels, width, height);
		int greenHorizontal = convolution(x, y, SOBEL_HORIZONTAL_FILTER, 9, GREEN, pixels, width, height);
		int blueHorizontal = convolution(x, y, SOBEL_HORIZONTAL_FILTER, 9, BLUE, pixels, width, height);
		
		int verticalGradient = abs(redVertical) + abs(greenVertical) + abs(blueVertical);
		int horizontalGradient = abs(redHorizontal) + abs(greenHorizontal) + abs(blueHorizontal);
		
		// we could take use sqrt(vertGrad^2 + horizGrad^2), but simple addition catches most edges.
		int totalGradient = verticalGradient + horizontalGradient;
		
		if (totalGradient >= edgeThreshold ) {
			newPixels[y * width + x] = createPixel(0, 0, 0); // we colour the edges black
		} else {
			newPixels[y * width + x] = createPixel(COLOUR_MASK, COLOUR_MASK, COLOUR_MASK);
		}
				
}
	

__kernel void reduceColours(__global int* pixels, 
							__global int* newPixels, 
							int width, 
							int height,
							int numColours) {

		int x = get_global_id(0);
		int y = get_global_id(1);
	
		int rgb = pixels[y * width + x];
		int newRed = quantizeColour(red(rgb), numColours);
		int newGreen = quantizeColour(green(rgb), numColours);
		int newBlue = quantizeColour(blue(rgb), numColours);
		
		int newRGB = createPixel(newRed, newGreen, newBlue);
		newPixels[y * width + x] = newRGB;

}


__kernel void mergeMask(__global int* pixelsTwo, 
						__global int* pixelsThree, 
						__global int* newPixels, 
						int width, 
						int height) {
		
		int x = get_global_id(0);
		int y = get_global_id(1);
		int maskColour = createPixel(COLOUR_MASK, COLOUR_MASK, COLOUR_MASK);
		
		if (pixelsTwo[y * width + x] == maskColour) {
			newPixels[y * width + x] = pixelsThree[y * width + x];
		} else {
			newPixels[y * width + x] = pixelsTwo[y * width + x];
		}
		
}
	
	