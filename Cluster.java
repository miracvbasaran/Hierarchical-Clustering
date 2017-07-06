import java.util.ArrayList;

/**
 * Created by Mirac Vuslat Basaran on 03-Jul-17.
 */
public class Cluster {
    ArrayList<Integer> members;
    private int memberSize;
    private int docSize;
    private byte[] union;

    public Cluster(){
        members = new ArrayList<Integer>();
        memberSize = 0;
        docSize = 0;
    }

    public Cluster(int member){
        members = new ArrayList<Integer>();
        members.add(member);
        docSize = 0;
        memberSize = 1;
        union = Features.getFeatures()[member];
        for(int i = 0; i < union.length; i++){
            if(union[i] == 1){
                docSize++;
            }
        }
    }

    public int addMember(int member){
        members.add(member);
        memberSize++;
        int diff = 0;
        byte[] featuresToAdd = Clusters.getClusters().get(member).getUnion();
        for(int i = 0; i < featuresToAdd.length; i++){
            if(featuresToAdd[i] == 1){
                if(union[i] == 0){
                    union[i] = 1;
                    diff++;
                }
            }
        }

        docSize += diff;
        return diff;
    }

    public void removeMember(int member){
        members.remove(member);
    }

    public void setDocSize(int docSize) {
        this.docSize = docSize;
    }

    public void setMembers(ArrayList<Integer> members) {
        this.members = members;
    }

    public void setMemberSize(int memberSize) {
        this.memberSize = memberSize;
    }

    public void setUnion(byte[] union) {
        this.union = union;
    }

    public ArrayList<Integer> getMembers() {
        return members;
    }

    public byte[] getUnion() {
        return union;
    }

    public int getDocSize() {
        return docSize;
    }

    public int getMemberSize() {
        return memberSize;
    }
}
