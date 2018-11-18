package nz.ac.waikato.phototool;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PhotoToolTest {

	@Test
	public void testStack() {
		PhotoTool tool = new PhotoTool("Eiffel.jpg");
		assertEquals(1, tool.getStackSize());
		assertEquals(1944, tool.getWidth());
		assertEquals(2592, tool.getHeight());
		// half should add a photo that has half the width and height 
		tool.half();
		assertEquals(2, tool.getStackSize());
		assertEquals(972, tool.getWidth());
		assertEquals(1296, tool.getHeight());
		// sepia should add a photo that has the same size.
		tool.sepia();
		assertEquals(3, tool.getStackSize());
		assertEquals(972, tool.getWidth());
		assertEquals(1296, tool.getHeight());
		// pop the two photos that we added
		tool.undo();
		tool.undo();
		assertEquals(1, tool.getStackSize());
		assertEquals(1944, tool.getWidth());
		assertEquals(2592, tool.getHeight());
		// check that undo does not pop the original photo.
		tool.undo();
		assertEquals(1, tool.getStackSize());
	}

}
