package pyromancers_model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.google.gson.*;

import materials.Material;
import materials.MaterialsCollection;

public class Packs {

	private static final HashMap<Material,ArrayList<MapItem>> staticMaterialAssociations = setupAssociations();
	private static final Random random = new Random();
	
	private static final HashMap<Material,ArrayList<MapItem>> setupAssociations(){
		HashMap<Material,ArrayList<MapItem>> materialAssociations = new HashMap<Material,ArrayList<MapItem>>();
		//Pack Creation
		TextureTree naturePack = parsePack("C:\\Users\\Alex\\Google Drive\\CodeProjects\\CityGenerator\\map_examples\\nature.rdm","Nature");
		//Association Setup
		materialAssociations.put(MaterialsCollection.Sand,naturePack.getMapItem("terrain/grownd/", 5));
		materialAssociations.put(MaterialsCollection.Water,naturePack.getMapItem("terrain/grownd/", 6));
		materialAssociations.put(MaterialsCollection.Tree,buildCustomCollectionTrees(naturePack));
		materialAssociations.put(MaterialsCollection.Grass,naturePack.getMapItem("floor/grass-open/", 1));
		
		return materialAssociations;
	}
	
	private static ArrayList<MapItem> buildCustomCollectionTrees(TextureTree pack){
		ArrayList<MapItem> customCollection = new ArrayList<MapItem>();
		customCollection.addAll(pack.getMapItem("plants/forest/",1));
		customCollection.addAll(pack.getMapItem("plants/forest/",29));
		customCollection.addAll(pack.getMapItem("plants/forest/",32));
		customCollection.addAll(pack.getMapItem("plants/forest/",33));
		ArrayList<MapItem> mostCommon = pack.getMapItem("plants/forest/",41);
		int count = 10;
		while(count-->0) customCollection.addAll(mostCommon);
		return customCollection;
	}
	
	public static MapItem getMapItem(Material mat){
		ArrayList<MapItem> list = getMapItems(mat); 
		return list.get(random.nextInt(list.size()));
	}
	
	public static ArrayList<MapItem> getMapItems(Material mat){
		return staticMaterialAssociations.get(mat);
	}
	
	private static LinkedList<TextureTree> parsePacks(String filename){
		JsonObject jsonobj = null;
		try {
			jsonobj = (JsonObject)(new JsonParser().parse(new BufferedReader(new FileReader(filename))));
		} catch (JsonIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HashMap<Integer,UtilityBase> objectMap = new HashMap<Integer,UtilityBase>(); 
		Location location = new Location(jsonobj,objectMap);
		
		return location.getPacks();
	}
	
	private static TextureTree parsePack(String filename, String packName) {
		return findPack(parsePacks(filename),packName);
	}

	private static TextureTree findPack(List<TextureTree> packs, String packName){
		for(TextureTree pack : packs) if(pack.get_title().equals(packName)) return pack;
		return null;
	}
	
	
}
