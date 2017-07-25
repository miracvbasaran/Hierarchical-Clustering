package EnronClusteringTest;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Mirac Vuslat Basaran on 06-Jul-17.
 */
public abstract class Clusters {
    private static ArrayList<Cluster> clusters;
    private static HashSet<Integer> invalidIds;

    public static ArrayList<Cluster> getClusters() {
        return clusters;
    }

    public static void setClusters(ArrayList<Cluster> cl) {
        clusters = cl;
    }

    public static void setInvalidIds(HashSet<Integer> invalidIds) {
        Clusters.invalidIds = invalidIds;
    }

    public static HashSet<Integer> getInvalidIds() {
        return invalidIds;
    }

    public static void addInvalidId(int id){
        invalidIds.add(id);
    }
}
