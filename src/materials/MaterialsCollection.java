package materials;

import java.awt.Color;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

final public class MaterialsCollection {
	
	//Public Members
	final public static MaterialPoint Sand = new MaterialPoint(Color.YELLOW,1);
	final public static MaterialPoint Water = new MaterialPoint(Color.BLUE,1);
	final public static MaterialPoint Tree = new MaterialPoint(new Color((float)0.0,(float)0.392,(float)0.0),2);
	final public static MaterialPoint Brush = new MaterialPoint(new Color((float)0.0,(float)0.7,(float)0.0),1);
	final public static MaterialPoly Grass = new MaterialPoly(Color.GREEN);
	final public static MaterialPoly Stone = new MaterialPoly(Color.GRAY);
		
	private MaterialsCollection(){
		
	}
}
