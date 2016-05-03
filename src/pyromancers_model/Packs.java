package pyromancers_model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.*;

import materials.Material;
import materials.MaterialsCollection;

public class Packs {

	private static final HashMap<Material,MapItem> staticMaterialAssociations = setupAssociations(); 
	
	private static final HashMap<Material,MapItem> setupAssociations(){
		HashMap<Material,MapItem> materialAssociations = new HashMap<Material,MapItem>();
		//Pack Creation
		TextureTree naturePack = parsePack("C:\\Users\\Alex\\Google Drive\\CodeProjects\\CityGenerator\\map_examples\\nature.rdm","Nature");
		//Association Setup
		materialAssociations.put(MaterialsCollection.Sand,naturePack.getMapItem("terrain/grownd/", 5));
		materialAssociations.put(MaterialsCollection.Water,naturePack.getMapItem("terrain/grownd/", 6));
		materialAssociations.put(MaterialsCollection.Tree,naturePack.getMapItem("plants/forest/", 41));
		materialAssociations.put(MaterialsCollection.Grass,naturePack.getMapItem("floor/grass-open/", 1));
		
		return materialAssociations;
	}
	
	public static MapItem getMapItem(Material mat){
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
