import java.util.ArrayList;

/**
 * Created by Mirac Vuslat Basaran on 03-Jul-17.
 */
public class Cluster {
    ArrayList<Integer> members;
    ArrayList<Integer> memberDocSizes;
    private int memberSize;
    private int unionSize;
    private byte[] union;

    public Cluster(){
        members = new ArrayList<Integer>();
        memberDocSizes = new ArrayList<Integer>();
        memberSize = 0;
        unionSize = 0;
    }

    public Cluster(int member){
        members = new ArrayList<Integer>();
        memberDocSizes = new ArrayList<Integer>();
        members.add(member);
        unionSize = 0;
        memberSize = 1;
        union = Features.getFeatures()[member];
        for(int i = 0; i < union.length; i++){
            if(union[i] == 1){
                unionSize++;
            }
        }
        memberDocSizes.add(unionSize);
    }

    public int addMember(int member){
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

        unionSize += diff;
        for(int i = 0; i < Clusters.getClusters().get(member).getMemberSize(); i++){
            memberSize++;
            memberDocSizes.add(Clusters.getClusters().get(member).getMemberDocSizes().get(i));
        }
        return diff;
    }


    // TODO: Not sure if necessary though
    public void removeMember(int member){
        members.remove(member);
    }

    public void setMemberDocSizes(ArrayList<Integer> memberDocSizes) {
        this.memberDocSizes = memberDocSizes;
    }

    public void setUnionSize(int unionSize) {
        this.unionSize = unionSize;
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

    public int getMemberSize() {
        return memberSize;
    }

    public ArrayList<Integer> getMemberDocSizes() {
        return memberDocSizes;
    }

    public int getUnionSize() {
        return unionSize;
    }
}
