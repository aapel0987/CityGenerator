package materials;

import java.awt.Color;

final public class MaterialsCollection {
	
	//Public Members
	final public static MaterialPoint Sand = new MaterialPoint(Color.YELLOW,1);
	final public static MaterialPoint Water = new MaterialPoint(Color.BLUE,1);
	final public static MaterialPoly Grass = new MaterialPoly(Color.GREEN);
	final public static MaterialPoly Stone = new MaterialPoly(Color.GRAY);
	
	
	private MaterialsCollection(){
		
	}
}
