import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * A class for the basic operations on rankers. 
 * @author Mariam Nassar
 *
 */
public class RankerHandler {
	/**
	 * Maps the unique model Ids to the model objects.
	 */
	private LinkedHashMap<String, Model> rankerMap = new LinkedHashMap<String, Model>();
	/**
	 * Maps the unique model Ids onto the ranking of the model.
	 */
	private LinkedHashMap<String, Integer> uniqueModelIdRanking = new LinkedHashMap<String, Integer>();
	/**
	 * A list with of the sorted unique model Ids.
	 */
	private ArrayList<String> uniqueModelIDList = new ArrayList<String>();
	
	
	/**
	 * Constructor.
	 * @param rankerList A list of models.
	 */
	public RankerHandler(List<Model> rankerList){
		int count = 1;
		
		if(rankerList != null)
			for(Model model: rankerList){
				rankerMap.put(model.getUniqueVersionId(), model);
				uniqueModelIdRanking.put(model.getUniqueVersionId(), count);
				uniqueModelIDList.add(model.getUniqueVersionId());
				count++;
			}
	}
	
	/**
	 * 
	 * @return A sorted list of model.
	 */
	public ArrayList<String> getUniqueModelIDList(){
		return this.uniqueModelIDList;
	}
	
	/**
	 * 
	 * @return The number of models in the ranker.
	 */
	public int getRankerSize(){
		return this.uniqueModelIDList.size();
	}
	
	/**
	 * Search for the ranking of a model by uniqueVersionId.
	 * 
	 * @param uniqueModelID
	 * @return The ranking of the model with the given uniqueModelID if the ranker contains the model
	 * and -1 otherwise.
	 */
	public int getRankingByUniqueModelID(String uniqueModelID){
		
		if(uniqueModelIdRanking.containsKey(uniqueModelID))
			return uniqueModelIdRanking.get(uniqueModelID);
		else return -1;
	}
	
	/**
	 * Tests if the ranker contains the model by uniqueModelId.
	 * 
	 * @param uniqueModelId
	 * @return true, if the ranker contains the model, and false else.
	 */
	public boolean containsByUniqueModelID(String uniqueModelId){ 
		if(uniqueModelIdRanking.containsKey(uniqueModelId))
			return true;
		else
			return false;
	}
	
	/**
	 * Searches for the score of a model by uniqueModelId.
	 * 
	 * @param modelID
	 * @return the score of the version with the given uniqueModelId if the ranker contains the model. 
	 * And -1 otherwise.
	 */
	public float getScoreByUniqueModelID(String uniqueModelId){
		
		if(this.rankerMap.containsKey(uniqueModelId))
			return this.rankerMap.get(uniqueModelId).getScore();
		else 
			return -1;
	}
	
	/**
	 * Updates the score of a model by uniqueModelId.
	 * 
	 * @param uniqueModelId
	 * @param newScore
	 */
	public void updateScoreByModelID(String uniqueModelId, float newScore){
		if(this.rankerMap.containsKey(uniqueModelId)){
			Model model = this.rankerMap.get(uniqueModelId);
			model.setScore(newScore);
			this.rankerMap.put(uniqueModelId, model);
		}
	}
	
	/**
	 * Swaps two models in the ranker by uniqueModelIds.
	 * 
	 * @param uniqueModelId1
	 * @param uniqueModelId2
	 */
	public void swap(String uniqueModelId1, String uniqueModelId2){ 
		int rankingOfModel1 = this.uniqueModelIdRanking.get(uniqueModelId1);
		int rankingOfModel2 = this.uniqueModelIdRanking.get(uniqueModelId2);
	
		this.uniqueModelIdRanking.put(uniqueModelId1, rankingOfModel2);
		this.uniqueModelIdRanking.put(uniqueModelId2, rankingOfModel1);
		this.uniqueModelIDList.set(rankingOfModel1 - 1, uniqueModelId2);
		this.uniqueModelIDList.set(rankingOfModel2 - 1, uniqueModelId1);
	}
	
	/**
	 * Sorts the models by their score. For the score based aggregation methods.
	 */
	public void sortModelsByScore(){
		
		//Maps the score of the model into the unique model ID
		HashMap<Float, String> scoreUniqueModelID = new HashMap<Float, String>();

		for(String uniqueModelId: uniqueModelIDList){
			Model model = this.rankerMap.get(uniqueModelId);
			scoreUniqueModelID.put(model.getScore(), uniqueModelId);
		}
		Map<Float, String> sortedMap = new TreeMap<Float, String>(scoreUniqueModelID);
		uniqueModelIDList = (ArrayList<String>) sortedMap.values();
	}
	
	/**
	 * 
	 * @return A list of sorted models for the score based aggregation methods.
	 */
	public List<Model> makeResultsListScoreBased(){
		this.sortModelsByScore();
		List<Model> modelsList = new LinkedList<Model>();
		for(String uniqueModelId: uniqueModelIDList){
			Model model = this.rankerMap.get(uniqueModelId);
			modelsList.add(model);
		}
		return modelsList;
	}
	
	/**
	 * 
	 * @return A list of sorted models for the rank based aggregation methods.
	 */
	public List<Model> makeResultsListRankBased(){
		this.setScoresToNAN();
		List<Model> modelsList = new LinkedList<Model>();
		for(String uniqueModelId: uniqueModelIDList){
			Model model = this.rankerMap.get(uniqueModelId);
			modelsList.add(model);
		}
		return modelsList;
	}
	
	/**
	 * Sets all scores to -1 when score are not relevant. For not score-based aggregation methods.
	 */
	private void setScoresToNAN(){
		for(Model model: this.rankerMap.values())
			model.setScore(-1);
		
	}

}
