package materials;

import java.awt.Color;

final public class MaterialsCollection {
	
	//Public Members
	// Great website for picking colors: http://www.rapidtables.com/web/color/RGB_Color.htm
	final public static MaterialPoint Sand = new MaterialPoint(Color.YELLOW,1);
	final public static MaterialPoint Water = new MaterialPoint(Color.BLUE,1);
	final public static MaterialPoint Tree = new MaterialPoint(new Color((float)0.0,(float)0.392,(float)0.0),2);
	final public static MaterialPoint Brush = new MaterialPoint(new Color((float)0.0,(float)0.7,(float)0.0),1);
	final public static MaterialPoly Grass = new MaterialPoly(Color.GREEN);
	final public static MaterialPoly Stone = new MaterialPoly(Color.GRAY);
	final public static MaterialPoly Mud = new MaterialPoly(new Color((float)(51.0/256.0),(float)(25/256.0),(float)(0.0/256.0)));
	final public static MaterialPoly GrassyStone = new MaterialPoly(new Color((float)(75.0/256.0),(float)(97/256.0),(float)(75.0/256.0)));
	final public static MaterialPoly MuddyGrass = new MaterialPoly(new Color((float)(51.0/256.0),(float)(80/256.0),(float)0.0));
		
	private MaterialsCollection(){
		
	}
}
