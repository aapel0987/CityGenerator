package materials;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import area_constructors.BasicShapeConstructor;
import test.TestGUIManager;

public class MaterialPoly extends Material {

	public MaterialPoly(Color c) {
		super(c);
	}

	@Override
	public void renderFill(TestGUIManager gui, Area area){
		gui.addShape(area, color);
		gui.addShape(BasicShapeConstructor.combineAreasParallel(StrokeLines(BasicShapeConstructor.getAreaLines(area, 0.1,true))), Color.BLACK);
	}

	private static List<Area> StrokeLines(List<Line2D> lines){
		final BasicStroke stroke = new BasicStroke(1);
		LinkedList<Area> areas = new LinkedList<Area>();
		Iterator<Line2D> iter = lines.iterator();
		while(iter.hasNext()){
			areas.add(new Area(stroke.createStrokedShape(iter.next())));
		}
		return areas;
	}
}
