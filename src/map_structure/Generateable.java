package map_structure;

import java.awt.Shape;
import java.awt.geom.Area;
import java.util.Random;

import test.TestGUIManager;

public interface Generateable extends Cloneable {

	static final Random random = new Random();
	
	public void render(TestGUIManager gui);
	
	public void crop(Shape s);
	
	public Generateable clone();
	
	public Area getArea();
}
