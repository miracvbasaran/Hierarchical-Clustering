package EnronClusteringTest;

import java.util.ArrayList;

/**
 * Created by Mirac Vuslat Basaran on 03-Jul-17.
 */
public class Cluster {
    ArrayList<Integer> members;
    private int memberSize;
    private int unionSize;
    public byte[] union;

    public Cluster(){
        members = new ArrayList<Integer>();
        memberSize = 0;
        unionSize = 0;
    }

    public Cluster(int member){
        members = new ArrayList<Integer>();
        members.add(member);
        unionSize = 0;
        memberSize = 1;
        union = Features.features[member].clone();
        for(int i = 0; i < union.length; i++){
            if(union[i] == 1){
                unionSize++;
            }
        }
        Features.docSizes[member] = unionSize;
        Features.overheads[member] = 0;
    }

    public int addMember(int member){
        int diff = 0;
        int diffComplement = 0;
        byte[] featuresToAdd = Clusters.getClusters().get(member).union.clone();
        for(int i = 0; i < featuresToAdd.length; i++){
            if(featuresToAdd[i] == 1){
                if(union[i] == 0){
                    union[i] = 1;
                    diff++;
                }
            }
            else{
                if(union[i] == 1){
                    diffComplement++;
                }
            }
        }

        unionSize += diff;
        for(int i = 0; i < this.getMemberSize(); i++){
            int memberId = members.get(i);
            Features.overheads[memberId] += diff;
        }
        for(int i = 0; i < Clusters.getClusters().get(member).getMemberSize(); i++){
            memberSize++;
            int idToAdd = Clusters.getClusters().get(member).getMembers().get(i);
            members.add(idToAdd);
            Features.overheads[idToAdd] += diffComplement;
        }
        return diff;
    }


    // TODO: Not sure if necessary though
    public void removeMember(int member){
        members.remove(member);
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


    public ArrayList<Integer> getMembers() {
        return members;
    }

    public int getMemberSize() {
        return memberSize;
    }


    public int getUnionSize() {
        return unionSize;
    }
}
