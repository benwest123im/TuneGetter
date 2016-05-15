package objects;

import java.util.List;

/**
 * Created by jure on 15.5.2016.
 */
public class Challenge {

    private int fromUser, toUser;
    private String challengeId;
    private List<Float> pitchesByTime;

    public Challenge(int fromUser, int toUser, String challengeId, List<Float> pitchesByTime) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.challengeId = challengeId;
        this.pitchesByTime = pitchesByTime;
    }

    public int getFromUser() {
        return fromUser;
    }

    public void setFromUser(int fromUser) {
        this.fromUser = fromUser;
    }

    public int getToUser() {
        return toUser;
    }

    public void setToUser(int toUser) {
        this.toUser = toUser;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public List<Float> getPitchesByTime() {
        return pitchesByTime;
    }

    public void setPitchesByTime(List<Float> pitchesByTime) {
        this.pitchesByTime = pitchesByTime;
    }
}
