package map_structure;

import java.awt.Shape;

import area_constructors.Constructor;
import test.TestGUIManager;

public interface Generateable extends Cloneable {
	
	public void render(TestGUIManager gui);
	
	public void crop(Shape s);
	
	public Generateable clone();
}
