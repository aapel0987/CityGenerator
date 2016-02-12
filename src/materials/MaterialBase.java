package materials;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;

import test.TestGUIManager;

abstract public class MaterialBase {

	protected Color color;
	
	protected MaterialBase(Color c){
		color = c;
	}
	
	abstract public void renderFill(TestGUIManager gui, Area area);
	

}
