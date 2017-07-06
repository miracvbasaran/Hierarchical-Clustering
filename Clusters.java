import java.util.ArrayList;

/**
 * Created by Mirac Vuslat Basaran on 06-Jul-17.
 */
public abstract class Clusters {
    private static ArrayList<Cluster> clusters;

    public static ArrayList<Cluster> getClusters() {
        return clusters;
    }

    public static void setClusters(ArrayList<Cluster> cl) {
        clusters = cl;
    }
}
