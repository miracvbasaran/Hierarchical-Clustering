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
    private int maxOverHead; // Maximum overhead for each query
    private int maxDocRepetition; // Maximum number of times a document is repeated


    public HierarchicalClustering(byte[][] features, int maxOverHead, int maxDocRepetition){
        this.features = features;
        this.maxOverHead = maxOverHead;
        this.maxDocRepetition = maxDocRepetition;
        numSamples = features.length;
        numFeatures = features[0].length;
        distances = new int[numSamples][numFeatures];
        clusters = new ArrayList<Cluster>();

    }

    // Calculating initial distances between samples/clusters,
    // as well as creating the initial clusters
    public void CalculateInitialDistances(){
        for(int i = 0; i < numSamples; i++){
            clusters.add(new Cluster());
            for(int j = 0; j < numSamples; j++){
                int dist = 0;
                for(int k = 0; k < numFeatures; k++){
                    if(features[i][k] != features[j][k]){
                        dist++;
                    }
                }
                distances[i][j] = dist;
            }
        }
    }


}
