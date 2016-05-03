package pyromancers_model;

import java.util.LinkedList;
import java.util.Map;

import com.google.gson.JsonElement;

public abstract class MapListed extends UtilityBase {

	public abstract StringBuilder toJasonFull();

	public abstract LinkedList<TextureTree> getPacks();
	
	protected boolean reset(){
		return super.reset();
	}
	
	public MapListed(JsonElement jsonElement, Map<Integer, UtilityBase> objectMap){
		super(jsonElement,objectMap);
	}
	
	public MapListed(){
		super();
	}
}
