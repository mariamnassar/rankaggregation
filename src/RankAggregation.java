

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * This class contains the aggregation methods for the models lists.  
 * @author Mariam Nassar
 *
 */
public class RankAggregation {
	
	/**
	 * Aggregates a list of rankers with regard to an initial aggregate ranker using a chosen aggregate method.
	 * 
	 * @param rankersList
	 * @param initialAggregateRanker
	 * @param aggregateMethod
	 * @param rankersWeights
	 * @return Aggregate list of models
	 */
	public static List<Model> aggregate(List<List<Model>> rankersList, List<Model> initialAggregateRanker, RankAggregationType.Types aggregateMethod, int rankersWeights){

		if (initialAggregateRanker.isEmpty()) return initialAggregateRanker;
		//Build the ranker handlers
		RankerHandler aggregateRankerH = new RankerHandler(initialAggregateRanker);
		List<RankerHandler> rankersListH = new LinkedList<RankerHandler>();
		for(List<Model> ranker: rankersList){
			RankerHandler rankerH = new RankerHandler(ranker);
			rankersListH.add(rankerH);
		}
		
		HashMap<Integer, Integer> weights = new HashMap<Integer, Integer>();
		
		//Build a hashmap which has the form: <ranker number, ranker weight> (for each ranker in the rankersList)
		for(int i = 0; i < 4; i++){
			weights.put(i, rankersWeights % 100);
			rankersWeights = rankersWeights / 100;
		}
		
		switch(aggregateMethod){ 
			
		case ADJACENT_PAIRS: 
			return adj(rankersListH, aggregateRankerH); 
		case COMB_MNZ:
			return combMNZ(rankersListH, aggregateRankerH);
		case LOCAL_KEMENIZATION:
			return localKemenization(rankersListH, aggregateRankerH);
		case SUPERVISED_LOCAL_KEMENIZATION: 
			return supervisedLocalKemenization(rankersListH, aggregateRankerH, weights);
		case DEFAULT: 
			return initialAggregateRanker;
		default: 
			return initialAggregateRanker;
		}
	}

	
	/**
	 * Computes the Kendall-tau distance between two rankers.
	 * 
	 * @param aggregateRankerH The initial aggregate ranker.
	 * @param ranker_iH 
	 * @return The Kendall-tau distance between the given two rankers.
	 */
	private static int distance(RankerHandler aggregateRankerH, RankerHandler ranker_iH){
		int ranker_iLength = ranker_iH.getRankerSize();
		
		ArrayList<String> uniqueModelIDListR_i = ranker_iH.getUniqueModelIDList();
		ArrayList<String> uniqueModelIDListRA = aggregateRankerH.getUniqueModelIDList();
		
		//Counts the number of pairwise disagreements between the aggregate ranker and ranker_i
		int sumOfDisagreements = 0; 
		
		//Counts the number of pairs (m1, m2) from aggregate ranker with m1 is ranked better than m2 and 
		//ranker_i contains m2 but not m1
		if (ranker_iLength > 0){
			//The number of models from aggregate ranker which are not contained in ranker_i
			int sumOfModelsNotInR_i = 0; 
			for(String uniqueModelID : uniqueModelIDListRA){
				if (ranker_iH.containsByUniqueModelID(uniqueModelID))
					//For each model m from aggregate ranker, if m is contained in ranker_i, 
					//add the number of models ranked higher than m but not contained in ranker_i
					sumOfDisagreements += sumOfModelsNotInR_i;
				else sumOfModelsNotInR_i++;
			}
		}
			
		//Counts the number of pairs (m1, m2) from aggregate ranker with m1 is ranked higher than m2 and 
		//ranker_i ranks m2 higher than m1
		for(int i = 0; i < ranker_iLength; i++){
			String uniqueModelID1 = uniqueModelIDListR_i.get(i);
			
			for(int j = i+1; j < ranker_iLength; j++){
				String uniqueModelID2 = uniqueModelIDListR_i.get(j);
				int ranking2OfModel1 = aggregateRankerH.getRankingByUniqueModelID(uniqueModelID1);
				int ranking2OfModel2 = aggregateRankerH.getRankingByUniqueModelID(uniqueModelID2);
				
				if(ranking2OfModel1 > ranking2OfModel2)
					sumOfDisagreements++;
				}
		}
			
		return sumOfDisagreements;	
	}
	
	
	/**
	 * Computes the average distance between the aggregate ranker after two adjacent model have been swapped in it and all the rankers in a list of rankers.
	 * And updates the Kendall-tau distances between the aggregate and each ranker in the rankers list.
	 * 
	 * @param rankersListH
	 * @param aggregateRankerH
	 * @param uniqueModelID1
	 * @param uniqueModelID2
	 * @param distanceToRankers
	 * @return The average distance between the aggregate ranker and all other rankers in the rankers list.
	 */
	//After swapping model1 and model2 in aggregate ranker:
	//Returns the average distance between the aggregate ranker and all other rankers in rankersList
	//(=(sum of the distances between the aggregate ranker and each ranker in rankersList) divided by the number of rankers in rankersList )
	//and updates the distances between aggregate ranker and each ranker in the rankersList
	private static double distanceAvg(List<RankerHandler> rankersListH, RankerHandler aggregateRankerH, String uniqueModelID1, String uniqueModelID2, double[] distanceToRankers){
		int ranker_iLength = rankersListH.size();
		double sumDistance = 0; //Sum of the distances between the aggregate ranker and each ranker in the rankersList
		
		for(int i = 0; i < ranker_iLength; i++){
			RankerHandler ranker_iH = rankersListH.get(i);
			
			int rankingOfModel1 = ranker_iH.getRankingByUniqueModelID(uniqueModelID1);
			if(rankingOfModel1 == -1) //If the model is not contained in ranker_i then:
				rankingOfModel1 = Integer.MAX_VALUE; //Set the ranking of the model to max value
			
			int rankingOfModel2 = ranker_iH.getRankingByUniqueModelID(uniqueModelID2);
			if(rankingOfModel2 == -1)
				rankingOfModel2 = Integer.MAX_VALUE; 
			
			//Compare the rankings of model1 and model2 in ranker_i and increase / decrease the distance
			if (rankingOfModel1 > rankingOfModel2)
				distanceToRankers[i]++;
			else 
				distanceToRankers[i]--;
			
			sumDistance += distanceToRankers[i]; //Add the modified distance to the sum
		}
		
		if(ranker_iLength > 0)
			sumDistance = sumDistance / ranker_iLength; //Divide the sum by the number of rankers in rankersList
		return sumDistance;
	}
	
	
	/**
	 * Adjacent pairs aggregation method based on Kendall-tau distance.
	 * 
	 * @param rankersListH A list of other ranker handlers.
	 * @param aggregateRankerH An initial aggregate ranker handler.
	 * @return An aggregate list of models.
	 */
	//Adjacent pairs aggregation method based on Kendall-tau distance 
	//Swaps every two adjacent models in the initial aggregate ranker.
	//If the average distance between the aggregate ranker after swapping and the other rankers is improved
	//Permanently swap. Swap back otherwise.
	private static List<Model> adj (List<RankerHandler>rankersListH, RankerHandler aggregateRankerH){ //adjacent pairs, based on Ke-tau
		double dintanceMin = 0; //The minimal average distance so far
		int count = 0; //Counts the rounds of swapping every two adjacent models in the initial ranker
		ArrayList<String> uniqueModelIDList = aggregateRankerH.getUniqueModelIDList();
		int ranker_iLength = rankersListH.size();
		//The distance between the aggregate ranker and each other ranker in the rankersList
		double[] distanceToRankers = new double[ranker_iLength];  
		double[] tempDistanceToRankers = new double[ranker_iLength];
		
		//Compute the initial distances
		for(int i = 0; i < rankersListH.size(); i++){
			RankerHandler ranker_iH = rankersListH.get(i);
			distanceToRankers[i] = distance(aggregateRankerH, ranker_iH);
			dintanceMin += distanceToRankers[i];
		}
		
		//The initial distanceMin = the initial average distance
		dintanceMin = dintanceMin / rankersListH.size();
		
		while (count < 100){ //repeat 100 rounds
			for(int i = 0; i < aggregateRankerH.getRankerSize() - 2; i++){
				aggregateRankerH.swap(uniqueModelIDList.get(i), uniqueModelIDList.get(i+1));
				for(int r = 0; r < 4; r++)
					tempDistanceToRankers[r] = distanceToRankers[r];
				double distAvg = distanceAvg(rankersListH, aggregateRankerH, uniqueModelIDList.get(i), uniqueModelIDList.get(i+1), tempDistanceToRankers);
				if (distAvg < dintanceMin){ //If average distance has been improved after swapping
					dintanceMin = distAvg; //Update the minimal distance
					for(int r = 0; r < 4; r++) //Update the distances to the rankers
						distanceToRankers[r] = tempDistanceToRankers[r];
				}
				else{
					//swap back if distance has not been improved 
					aggregateRankerH.swap(uniqueModelIDList.get(i), uniqueModelIDList.get(i+1)); 
				}
			}
			
			count++;
		}
	
		List<Model> results = aggregateRankerH.makeResultsListRankBased(); 
		return results;
	}
	
	
	/**
	 * CombMNZ aggregation method based on the normalized Borda rank. 
	 * This is a score based aggregation method. 
	 * 
	 * @param rankersListH A list of other ranker handlers.
	 * @param aggregateRankerH An initial aggregate ranker handler.
	 * @return An aggregate list of models.
	 */
	//CombMNZ method. Based on the normalized Borda rank. 
	//The new score for each model will be:
	//(the sum of Borda rank normalization related to each ranker) * (the number of the rankers the model is contained in) 
	private static List<Model> combMNZ(List<RankerHandler>rankersListH, RankerHandler aggregateRankerH){
		int s = rankersListH.size();
		float maxPossibleScore = s * s;  //The maximum value the score could ever have
		
		for (String uniqueModelID : aggregateRankerH.getUniqueModelIDList()) {
			int h = 0; //Denotes the number of times model appears in the rankers
			float brn_sum = 0; //Borda rank normalization for the model

			for (int i = 0; i < s; i++) { //Compute h and brn_sum
				RankerHandler ranker_iH = rankersListH.get(i);
				
				int ranking = ranker_iH.getRankingByUniqueModelID(uniqueModelID); 
				if (ranking != -1){ //if 'ranker_i' contains model
					h++;
					brn_sum += 1 - ((double) (ranking - 1) / aggregateRankerH.getRankerSize());
				}
			}

			float newScore = brn_sum * h;
			float scoreProcent = newScore / maxPossibleScore;
			aggregateRankerH.updateScoreByModelID(uniqueModelID, scoreProcent); //Set new score
		}
		
		//Make a list with the models
		List<Model> results = aggregateRankerH.makeResultsListScoreBased(); 
		return results;
	}
	
	
	/**
	 * The local Kemenization aggregation method.
	 * Builds a locally Kemeny optimized aggregate ranker.
	 * 
	 * @param rankersListH A list of other ranker handlers.
	 * @param aggregateRankerH An initial aggregate ranker handler.
	 * @return An aggregate list of models.
	 */
	//Local Kemenization. Builds a locally Kemeny optimized aggregate ranker.
	private static List<Model> localKemenization(List<RankerHandler>rankersListH, RankerHandler aggregateRankerH){
		int rankersListLength = rankersListH.size();
		int aggregateRankerLength = aggregateRankerH.getRankerSize();
		ArrayList<String> uniqueModelIDList = aggregateRankerH.getUniqueModelIDList();
		
		for(int i = 1; i < aggregateRankerLength; i++){
			String uniqueModelID2 = uniqueModelIDList.get(i);
			
			for(int j = i-1; j >= 0; j--){
				int pro = 0;
				int con = 0;
				String uniqueModelID1 = uniqueModelIDList.get(j);
				
				//Compare the rankings of model1 and model2 in each ranker 
				for(int l = 0; l < rankersListLength; l++){
					RankerHandler ranker_i = rankersListH.get(l);
					
					int rankingOfModel1 = ranker_i.getRankingByUniqueModelID(uniqueModelID1);
					if(rankingOfModel1 == -1)
						rankingOfModel1 = Integer.MAX_VALUE; 
					
					int rankingOfModel2 = ranker_i.getRankingByUniqueModelID(uniqueModelID2);
					if(rankingOfModel2 == -1)
						rankingOfModel2 = Integer.MAX_VALUE; 
					
					//update pro if the ranking is the same as in the initial aggregate ranker
					//update cons otherwise
					if(rankingOfModel2 > rankingOfModel1)
						pro++;
					else if (rankingOfModel2 < rankingOfModel1)
						con++;
				}
				
				//swap model1 and model2 if the majority of the rankers prefer model2 to model1 (if ranking of model2 < ranking of model1)
				if(con > pro){
					aggregateRankerH.swap(uniqueModelID2, uniqueModelID1);
				}
				else if (pro >= con)
					break;
			}
		}
		
		List<Model> results = aggregateRankerH.makeResultsListRankBased(); 
		return results;
	}
	
	
	/**
	 * The supervised local Kemenization aggregation method.
	 * Builds a locally Kemeny optimized aggregate ranker with regard to the weights of the input rankers.
	 * 
	 * @param rankersListH A list of other ranker handlers.
	 * @param aggregateRankerH An initial aggregate ranker handler.
	 * @param weights
	 * @return An aggregate list of models.
	 */
	//Builds a locally Kemeny optimized aggregate ranker with regard to the weights of the input rankers.
	private static List<Model> supervisedLocalKemenization (List<RankerHandler>rankersListH, RankerHandler aggregateRankerH, HashMap<Integer, Integer> weights){
		
		int numberOfRankers = rankersListH.size();
		int aggregateRankerLength = aggregateRankerH.getRankerSize();
		//For each two models m1 and m2, if they are ranked correctly (with supervisedLocalKemenization)
		//Then M(m1, m2) will be true, false otherwise.
		boolean[][] M = new boolean[aggregateRankerLength][aggregateRankerLength];
	
		int rankingOfModel1;
		int rankingOfModel2;
		ArrayList<String> uniqueModelIDList = aggregateRankerH.getUniqueModelIDList();
		
		int weightsSum = 0; //The sum of the weights of all rankers in rankersList
		for(int o: weights.values())
			weightsSum += o;
	    
		//Computes a score for each pairs of models in aggregate ranker as follows:
		//Sums up the weights of the rankers which don't rank the pair different from the aggregate ranker
		//If the score is greter than weightsSum/2 then set M(this pair) to true
		for(int i = 0; i < aggregateRankerLength; i++){
			for(int j = i + 1 ; j < aggregateRankerLength; j++){
				String uniqueModelID1 = uniqueModelIDList.get(i);
				String uniqueModelID2 = uniqueModelIDList.get(j);
				int score = 0;
				
				for(int l = 0; l < numberOfRankers; l++){
					RankerHandler ranker_iH = rankersListH.get(l);
					
					rankingOfModel1 = ranker_iH.getRankingByUniqueModelID(uniqueModelID1);
					if(rankingOfModel1 == -1)
						rankingOfModel1 = Integer.MAX_VALUE; 
					
					rankingOfModel2 = ranker_iH.getRankingByUniqueModelID(uniqueModelID2);
					if(rankingOfModel2 == -1)
						rankingOfModel2 = Integer.MAX_VALUE; 
					
					if(rankingOfModel1 <= rankingOfModel2)
						score += weights.get(l);
				}
				
				if (score >= 0.5 * weightsSum){
					M[i][j] = true;
					M[j][i] = false;
				}
			}
		}
		
		//For each pair of models (m1, m2) with M(m1,m2) = false: swap m1, m2
		for(int i = 1; i < aggregateRankerLength; i++){
			String uniqueModelID2 = uniqueModelIDList.get(i);
			
			for(int j = i-1; j >= 0; j--){
				String uniqueModelID1 = uniqueModelIDList.get(j);
				
				if(M[j][i] == false)
					aggregateRankerH.swap(uniqueModelID1, uniqueModelID2);
			}
		}
		
		List<Model> results = aggregateRankerH.makeResultsListRankBased(); 
		return results;
	}
	

}
