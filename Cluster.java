import java.util.ArrayList;

/**
 * Created by Mirac Vuslat Basaran on 03-Jul-17.
 */
public class Cluster {
    ArrayList<Integer> members;

    public Cluster(){
        members = new ArrayList<Integer>();
    }

    public Cluster(int member){
        members = new ArrayList<Integer>();
        members.add(member);
    }
}
