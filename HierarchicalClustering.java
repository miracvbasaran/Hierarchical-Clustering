import java.util.ArrayList;

/**
 * Created by Mirac Vuslat Basaran on 03-Jul-17.
 */
public class HierarchicalClustering {
    private int[][] distances;
    private ArrayList<Cluster> clusters;
    private int numFeatures;
    private int numSamples;
    private byte[][] features;
    private int maxOverHead, maxDocRepetition;


    public HierarchicalClustering(byte[][] features, int maxOverHead, int maxDocRepetition){
        this.features = features;
        this.maxOverHead = maxOverHead;
        this.maxDocRepetition = maxDocRepetition;

    }

    public void CalculateDistances(){

    }
}
