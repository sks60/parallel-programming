package com.celanim.cartoonify;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.celanim.cartoonify.Cartoonify;

public class CartoonifyTest {

	@Test
	public void testLoad() throws IOException {
		Cartoonify cart = new Cartoonify();
		assertEquals(0, cart.numImages());
		cart.loadPhoto("test.png");
		assertEquals(1, cart.numImages());
		assertEquals(50, cart.width());
		assertEquals(30, cart.height());
		// check the four corners
		assertEquals(0x00FF0000, cart.pixel(0, 0)); // red
		assertEquals(0x00FFFFFF, cart.pixel(0, 29)); // black
		assertEquals(0x00000000, cart.pixel(49, 29)); // white
		assertEquals(0x00808080, cart.pixel(49, 0));   // gray

		assertEquals(255, cart.red(cart.pixel(0, 0))); // red
		assertEquals(0, cart.green(cart.pixel(0, 0))); // red
		assertEquals(0, cart.blue(cart.pixel(0, 0)));  // red

		assertEquals(255, cart.green(0x0000FF00));
		assertEquals(255, cart.blue(0x000000FF));
		int funny = cart.createPixel(0x88, 0x99, 0xAB);
		assertEquals(0x008899AB, funny);
		assertEquals(0x88, cart.colourValue(funny, Cartoonify.RED));
		assertEquals(0x99, cart.colourValue(funny, Cartoonify.GREEN));
		assertEquals(0xAB, cart.colourValue(funny, Cartoonify.BLUE));
	}

	@Test
	public void testStack() throws IOException {
		Cartoonify cart = new Cartoonify();
		cart.loadPhoto("test.png");
		assertEquals(1, cart.numImages());
		cart.cloneImage(-1);
		assertEquals(2, cart.numImages());
		cart.grayscale();
		assertEquals(3, cart.numImages());
		assertEquals(0x00555555, cart.pixel(0, 0)); // was red
		int[] pixels = cart.popImage();
		assertEquals(0x00555555, pixels[0]);
		int[] pixels0 = cart.popImage();
		assertEquals(0x00FF0000, pixels0[0]); // a copy of the original photo
		assertEquals(1, cart.numImages());
		cart.gaussianBlur();
		cart.setEdgeThreshold(256);
		cart.sobelEdgeDetect();
		assertEquals(3, cart.numImages());
		cart.cloneImage(0);
		assertEquals(4, cart.numImages());
		cart.setNumColours(3);
		cart.reduceColours();
		assertEquals(5, cart.numImages());
		cart.mergeMask(2, cart.white, -1);
		assertEquals(6, cart.numImages());
		cart.loadPhoto("test_cartoon_e256_c3.png");
		int[] expected = cart.popImage();
		int[] actual = cart.popImage();
		assertEquals(5, cart.numImages());
		assertEquals(50 * 30, expected.length);
		assertEquals(50 * 30, actual.length);
		// This is quite a stringent test - it expects every pixel to be identical.
		// If we modify the edge wrapping, we should perhaps not check pixels near the edges.
		for (int i = 0; i < expected.length; i++) {
			final String msg = String.format("pixel[%d] expected 0x%x but got 0x%x", i, expected[i], actual[i]);
			assertEquals(msg, expected[i], actual[i]);
		}
		cart.clear();
		assertEquals(0, cart.numImages());
	}

	@Test
	public void testClamp() {
		Cartoonify cart = new Cartoonify();
		for (int i = 0; i < 300; i++) {
			assertEquals(0, cart.clamp(-i));
			assertEquals(255, cart.clamp(255 + i));
		}
		// test rounding
		assertEquals(1, cart.clamp(0.5));
		assertEquals(254, cart.clamp(254.49));
		assertEquals(255, cart.clamp(255.5));
		assertEquals(255, cart.clamp(255.99));
	}

	@Test
	public void testWrap() {
		Cartoonify cart = new Cartoonify();
		// Test small violations of the lower bound
		// The mirror is at the very edge.  So we should see each pixel twice.
		assertEquals(2, cart.wrap(2, 100));
		assertEquals(1, cart.wrap(1, 100));
		assertEquals(0, cart.wrap(0, 100));
		assertEquals(0, cart.wrap(-1, 100));
		assertEquals(1, cart.wrap(-2, 100));
		assertEquals(2, cart.wrap(-3, 100));

		// test small violations of the upper bound
		assertEquals(97, cart.wrap(97, 100));
		assertEquals(98, cart.wrap(98, 100));
		assertEquals(99, cart.wrap(99, 100));
		assertEquals(99, cart.wrap(100, 100));
		assertEquals(98, cart.wrap(101, 100));
		assertEquals(97, cart.wrap(102, 100));
	}

	@Test
	public void testQuantizeColour2() {
		Cartoonify cart = new Cartoonify();
		assertEquals(0, cart.quantizeColour(0, 2));
		assertEquals(0, cart.quantizeColour(127, 2));
		assertEquals(255, cart.quantizeColour(128, 2));
		assertEquals(255, cart.quantizeColour(255, 2));
	}

	@Test
	public void testQuantizeColour3() {
		Cartoonify cart = new Cartoonify();
		assertEquals(0, cart.quantizeColour(0, 3));
		assertEquals(0, cart.quantizeColour(85, 3));
		assertEquals(127, cart.quantizeColour(86, 3));
		assertEquals(127, cart.quantizeColour(170, 3));
		assertEquals(255, cart.quantizeColour(171, 3));
		assertEquals(255, cart.quantizeColour(255, 3));
	}

	@Test
	public void testQuantizeColour25() {
		Cartoonify cart = new Cartoonify();
		assertEquals(0, cart.quantizeColour(0, 26));
		assertEquals(0, cart.quantizeColour(9, 26));
		assertEquals(10, cart.quantizeColour(10, 26));
		assertEquals(10, cart.quantizeColour(19, 26));
		assertEquals(20, cart.quantizeColour(20, 26));
		assertEquals(244, cart.quantizeColour(246, 26));
		assertEquals(255, cart.quantizeColour(247, 26));
		assertEquals(255, cart.quantizeColour(255, 26));
	}

	@Test
	public void testConvolutionCentre() throws IOException {
		Cartoonify cart = new Cartoonify();
		cart.loadPhoto("test.png");
		// red pixels near a black region
		assertEquals(0, cart.convolution(2, 2, Cartoonify.GAUSSIAN_FILTER, Cartoonify.GREEN));
		assertEquals(255 * 159, cart.convolution(2, 2, Cartoonify.GAUSSIAN_FILTER, Cartoonify.RED));
		assertEquals(255 * 142, cart.convolution(2, 3, Cartoonify.GAUSSIAN_FILTER, Cartoonify.RED));
		assertEquals(255 * 104, cart.convolution(2, 4, Cartoonify.GAUSSIAN_FILTER, Cartoonify.RED));
	}

	@Test
	public void testConvolutionXAxis() throws IOException {
		Cartoonify cart = new Cartoonify();
		cart.loadPhoto("test.png");
		// go along the black line over the zero edge.
		for (int x = -3; x <= 3; x++) {
			assertEquals(255 * 55, cart.convolution(x, 24, Cartoonify.GAUSSIAN_FILTER, Cartoonify.RED));
		}
	}

	@Test
	public void testConvolutionYAxis() throws IOException {
		Cartoonify cart = new Cartoonify();
		cart.loadPhoto("test.png");
		// go along the black line over the zero edge.
		for (int y = -3; y <= 3; y++) {
			assertEquals(255 * 110, cart.convolution(29, y, Cartoonify.GAUSSIAN_FILTER, Cartoonify.RED));
		}
	}

	@Test
	public void testGaussianBlurMiddle() throws IOException {
		Cartoonify cart = new Cartoonify();
		cart.loadPhoto("test.png");
		cart.gaussianBlur();
		assertEquals(0x00FF0000, cart.pixel(0, 0));  // surrounded by pure red

		// inspect a given pixel...
		//		int pixel = cart.pixel(17, 2);
		//		System.out.println("pixel=" + String.format("%x", pixel)
		//				+ " red=" + cart.red(pixel)
		//				+ " green=" + cart.green(pixel)
		//				+ " blue=" + cart.blue(pixel));

		// we go along the top edge, checking that colours are blurred into each other correctly
		assertEquals(cart.createPixel(255,   0,   0), cart.pixel(2, 2));  // centre of red patch
		assertEquals(cart.createPixel(228,  27,   0), cart.pixel(3, 2));
		assertEquals(cart.createPixel(167,  88,   0), cart.pixel(4, 2));
		assertEquals(cart.createPixel( 88, 167,   0), cart.pixel(5, 2));
		assertEquals(cart.createPixel( 27, 228,   0), cart.pixel(6, 2));
		assertEquals(cart.createPixel(  0, 255,   0), cart.pixel(7, 2));  // centre of green patch
		assertEquals(cart.createPixel(  0, 228,  27), cart.pixel(8, 2));
		assertEquals(cart.createPixel(  0, 167,  88), cart.pixel(9, 2));
		assertEquals(cart.createPixel(  0,  88, 167), cart.pixel(10, 2));
		assertEquals(cart.createPixel(  0,  27, 228), cart.pixel(11, 2));
		assertEquals(cart.createPixel(  0,   0, 255), cart.pixel(12, 2));
		assertEquals(cart.createPixel( 24,  24, 252), cart.pixel(13, 2)); // starts touching the black diagonal line
		assertEquals(cart.createPixel( 72,  72, 239), cart.pixel(14, 2));
		assertEquals(cart.createPixel(123, 123, 212), cart.pixel(15, 2));
		assertEquals(cart.createPixel(154, 154, 181), cart.pixel(16, 2));
		assertEquals(cart.createPixel(170, 170, 170), cart.pixel(17, 2)); // white with diagonal black line
	}

	/**
	 * This tests the Gaussian blurring near the edge, to see how it works with wrapping.
	 * @throws IOException
	 */
	@Test
	public void testGaussianBlurEdge() throws IOException {
		Cartoonify cart = new Cartoonify();
		cart.loadPhoto("test.png");
		cart.gaussianBlur();
		assertEquals(cart.createPixel(  0,   0, 255), cart.pixel(12, 0));
		assertEquals(cart.createPixel( 27,  27, 255), cart.pixel(13, 0));
		assertEquals(cart.createPixel( 88,  88, 255), cart.pixel(14, 0));
		assertEquals(cart.createPixel(164, 164, 252), cart.pixel(15, 0)); // one corner touches a black pixel
		assertEquals(cart.createPixel(212, 212, 239), cart.pixel(16, 0)); // some blue, mostly white plus diagonal black line
		assertEquals(cart.createPixel(212, 212, 212), cart.pixel(17, 0)); // white with diagonal black line
	}
}
