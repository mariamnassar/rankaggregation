
public class Model {
	
	private float score;
	private String uniqueId;
	
	
	public String getUniqueVersionId(){
		return this.uniqueId;
	}

	public Model(float score, String uniqueId){
		this.score = score;
		this.uniqueId = uniqueId;
	}	
	

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

}
