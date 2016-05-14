package objects;

import java.util.List;

/**
 * Created by jure on 14.5.2016.
 */
public class UserResult {
    private int userId;
    private int challengeId;
    private float score;
    private List<Float> scoresByTime;

    public UserResult(int userId, int challengeId, float score, List<Float> scoresByTime) {
        this.userId = userId;
        this.challengeId = challengeId;
        this.score = score;
        this.scoresByTime = scoresByTime;
    }

    public int getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(int challengeId) {
        this.challengeId = challengeId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<Float> getScoresByTime() {
        return scoresByTime;
    }

    public void setScoresByTime(List<Float> scoresByTime) {
        this.scoresByTime = scoresByTime;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}
