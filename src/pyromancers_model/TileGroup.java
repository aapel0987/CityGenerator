package pyromancers_model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TileGroup extends UtilityBase {

	public static final String __type = "com.locator.logic::TileGroup";
	private String folder;
	private int x;
	private int y;
	private boolean blur;
	private boolean shadow;
	private LinkedList<MapItem> tiles;
	private String title;
	private boolean rotate;
	//Ignored values?
	private static final int tilex = 0;
	private static final int tiley = 0;

	public TileGroup(JsonElement jsonElement, Map<Integer, UtilityBase> objectMap) {
		super(jsonElement,objectMap);
		//System.out.println(jsonElement.toString());
		if(jsonElement.isJsonObject()) {
			JsonObject jsonObject = (JsonObject) jsonElement;
			folder 		= jsonObject.get("folder").getAsString();
			title 		= jsonObject.get("title").getAsString();
			x     		= jsonObject.get("x").getAsInt();
			y     		= jsonObject.get("y").getAsInt();
			shadow		= parseOnOff(jsonObject,"shadow");
			blur		= parseOnOff(jsonObject,"blur");
			rotate		= parseOnOff(jsonObject,"rotate");
			JsonArray tilesArray = jsonObject.get("tiles").getAsJsonArray();
			tiles		= new LinkedList<MapItem>();
			for(JsonElement sourceElement : tilesArray){
				if(!sourceElement.isJsonNull() && sourceElement.isJsonObject()){
					tiles.add((MapItem) instanceFactory(sourceElement,objectMap));
				}
			}
		}
	}
	
	protected boolean reset(){
		boolean wasReset = super.reset();
		if(wasReset){
			for(MapItem tile : tiles) tile.reset();
		}
		return wasReset;
	}
	
	public String get__type(){
		return __type;
	}

	protected int recursiveEnumerate(int starting_value) {
		starting_value = super.recursiveEnumerate(starting_value);
		for(MapItem tile : tiles) starting_value = tile.enumerate(starting_value);
		return starting_value;
	}
	
	public StringBuilder toJasonFull(){
		StringBuilder builder = new StringBuilder();
		builder.append("\"folder\":\"" + folder + "\",");
		builder.append("\"x\":" + x + ",");
		append__type(builder,__type);
		builder.append(",\"y\":" + y + ",");
		append__id(builder);
		builder.append(",");
		appendOnOff(builder,"blur",blur);
		builder.append(",");
		appendOnOff(builder,"shadow",shadow);
		builder.append(",\"tiles\":[");
		for(Iterator<MapItem> iter = tiles.iterator(); iter.hasNext(); ){
			builder.append("{");
			builder.append(iter.next().toJason());
			builder.append("}");
			if(iter.hasNext()) builder.append(",");
		}
		builder.append("]");
		builder.append(",\"title\":\"" + title + "\",");
		builder.append("\"tilex\":" + tilex + ",");
		appendOnOff(builder,"rotate",rotate);
		builder.append(",\"tiley\":" + tiley);
		return builder;
	}

	public MapItem getMapItem(String folderArg, int id) {
		if(!folderArg.equals(folder)) return null;
		for(MapItem tile : tiles) if(tile.getMapItemID() == id) return tile;
		return null;
	}

	public ArrayList<MapItem> getMapItems(String folderArg) {
		if(!folderArg.equals(folder)) return null;
		return new ArrayList<MapItem>(tiles);
	}
}
