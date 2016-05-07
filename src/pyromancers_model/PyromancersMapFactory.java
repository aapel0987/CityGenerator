package pyromancers_model;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import map_structure.AreaLayer;
import map_structure.Generateable;
import map_structure.Group;
import materials.Material;
import materials.MaterialPoint;
import materials.MaterialPoly;

public final class PyromancersMapFactory {

	private final static Random random = new Random();
	
	public static void writeToFile(Group map, String filename){
		buildMap(map).writeToFile(filename);
	}
	
	public static Location buildMap(Group map){
		return new Location(buildBunch(map));
	}
	
	private static Bunch buildBunch(Group group){
		Bunch bunch = new Bunch(group.getName());
		List<Generateable> localMembers = new LinkedList<Generateable>(group.getMembers());
		Collections.reverse(localMembers); 
		for(Generateable member : localMembers){
			MapListed mapListed = null;
			if(member instanceof Group){
				mapListed = buildBunch((Group) member);
			} else if(member instanceof AreaLayer){
				mapListed = buildLayer((AreaLayer) member);
			} else{
				throw new IllegalArgumentException("Cannot process Generateable Type: " + member.getClass().getName());
			}
			bunch.addSource(mapListed);
		}
		return bunch;
	}
	
	private static MapListed buildLayer(AreaLayer layer){
		Material material = layer.getMaterial(); 
		if(material instanceof MaterialPoint){
			ArrayList<MapItem> mapItems = Packs.getMapItems(layer.getMaterial());
			return buildBrushLayer(((MaterialPoint) material).getFillPoints(layer),mapItems);
		} else if(material instanceof MaterialPoly){
				MapItem mapItem = Packs.getMapItem(layer.getMaterial());
				return buildPolygonLayer(layer,mapItem);
		} else {
			throw new IllegalArgumentException("Cannot process Material Type: " + material.getClass().getName());
		}
	}
	
	private static MapListed buildBrushLayer(ArrayList<Point2D> points, ArrayList<MapItem> mapItems){
		
		if(mapItems.size() == 1) {
			return new Layer("buildBrushLayer_0", new Brush(mapItems.get(0),points));
		} else {
			ArrayList<MapListed> mapList = new ArrayList<MapListed>();
			ArrayList<Point2D> pointsClone = new ArrayList<Point2D>(points);
			while(!pointsClone.isEmpty()){
				Point2D nextPoint = pointsClone.remove(random.nextInt(pointsClone.size()));
				mapList.add(new Layer("buildBrushLayer_" + nextPoint.toString(), new Obstical(mapItems.get(random.nextInt(mapItems.size())),nextPoint)));
			}
			return new Bunch("buildLayer_Bunch",mapList);
		}
	}
	
	private static Layer buildPolygonLayer(AreaLayer layer, MapItem mapItem){
		return new Layer("buildPolygonLayer",new Polygon(layer,mapItem));
	}
}
