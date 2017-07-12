import java.util.*;

/**
 * Created by Mirac Vuslat Basaran on 03-Jul-17.
 */
public class HierarchicalClustering {
    private int[][] distances;
    private int numFeatures;
    private int numSamples;
    private int maxOverHead; // Maximum overhead for each query
    private double maxOverHeadRate;
    private int maxDocRepetition; // Maximum number of times a document is repeated



    public HierarchicalClustering(byte[][] features, int maxOverHead, double maxOverHeadRate, int maxDocRepetition){
        Features.setFeatures(features);
        this.maxOverHead = maxOverHead;
        this.maxOverHeadRate = maxOverHeadRate;
        this.maxDocRepetition = maxDocRepetition;
        numSamples = features.length;
        numFeatures = features[0].length;
        distances = new int[numSamples][numFeatures];
        Clusters.setClusters(new ArrayList<Cluster>());
        Clusters.setInvalidIds(new HashSet<Integer>(numSamples));
    }

    // Main method to run the clustering algorithm
    public void runClustering(){
        calculateInitialDistances(); // Setting initial clusters/distances

        // Clustering Loop
        while(Clusters.getInvalidIds().size() < Clusters.getClusters().size() - 1){
            Pair closestDistancePair = findClosestClusters();
            if(closestDistancePair != null){
                updateDistances(closestDistancePair);
                System.out.println("Merging clusters " + closestDistancePair.getNum1() + " and " + closestDistancePair.getNum2() + ".");
            }

            else{
                break;
            }
        }
    }

    // Calculating initial distances between samples/clusters,
    // as well as creating the initial clusters
    private void calculateInitialDistances(){
        System.out.println("Started calculating initial distances.");
        for(int i = 0; i < numSamples; i++){
            Clusters.getClusters().add(new Cluster(i));

            for(int j = 0; j < numSamples; j++){
                if(i == j){
                    distances[i][i] = 0;
                }
                else{
                    int dist = 0;
                    for(int k = 0; k < numFeatures; k++){
                        if(Features.getFeatures()[i][k] == 0 && Features.getFeatures()[j][k] == 1){
                            dist++;
                        }
                    }
                    distances[i][j] = dist;
                }
            }
        }
        System.out.println("Done calculating initial distances.");
    }

    // Updating distances after merging two clusters
    private void updateDistances(Pair clusterIds){
        int cl1 = clusterIds.getNum1();
        int cl2 = clusterIds.getNum2();

        Clusters.getClusters().get(cl1).addMember(cl2);

        for(int i = 0; i < Clusters.getClusters().size(); i++){
            if(!Clusters.getInvalidIds().contains(new Integer(i))){
                int distCl1ToI = 0;
                int distIToCl1 = 0;
                for(int k = 0; k < numFeatures; k++){
                    if(Clusters.getClusters().get(cl1).getUnion()[k] == 0 && Clusters.getClusters().get(i).getUnion()[k] == 1){
                        distCl1ToI++;
                    }
                    else if(Clusters.getClusters().get(cl1).getUnion()[k] == 1 && Clusters.getClusters().get(i).getUnion()[k] == 0){
                        distIToCl1++;
                    }
                }
                distances[cl1][i] = distCl1ToI;
                distances[i][cl1] = distIToCl1;
            }
        }

        Clusters.addInvalidId(new Integer(cl2));
    }

    // TODO: Finished?
    // TODO: Overhead Check
    private Pair findClosestClusters(){
        int cl1, cl2;
        int minDist = distances[0][1];
        int minDistComplement = distances[1][0];
        cl1 = 0;
        cl2 = 1;
        for(int i = 0; i < Clusters.getClusters().size(); i++){
            if(!Clusters.getInvalidIds().contains(new Integer(i))){
                for(int j = i + 1; j < Clusters.getClusters().size(); j++){
                    if(!Clusters.getInvalidIds().contains(new Integer(j))){
                        int clusterDistance = distances[i][j];
                        int clusterDistanceComplement = distances[j][i];
                        boolean flag = false;

                        int minMemberOverhead = 0;
                        int maxMemberOverhead = 0;
                        int maxComplementMemberOverhead = 0;
                        int maxComplementK = 0;
                        int minK = 0;
                        int maxK = 0;

                        // I to J is smaller
                        if(clusterDistance < clusterDistanceComplement){
                            Cluster clusterI = Clusters.getClusters().get(i);

                            // Finding minDist
                            for(int k = 0; k < clusterI.getMemberSize(); k++){
                                int memberId = clusterI.getMembers().get(k);
                                int memberOverhead = Features.overheads[memberId] + clusterDistance;
                                if (k == 0){
                                    minMemberOverhead = memberOverhead;
                                    maxMemberOverhead = memberOverhead;
                                }
                                else{
                                    if(memberOverhead < minMemberOverhead){
                                        minMemberOverhead = minMemberOverhead;
                                        minK = k;
                                    }

                                    if(maxMemberOverhead < memberOverhead){
                                        maxMemberOverhead = memberOverhead;
                                        maxK = k;
                                    }
                                }
                            }

                            // Finding distance complement
                            Cluster clusterJ = Clusters.getClusters().get(j);
                            for(int k = 0; k < clusterJ.getMemberSize(); k++){
                                int memberId = clusterJ.getMembers().get(k);
                                int memberOverhead = Features.overheads[memberId] + clusterDistanceComplement;
                                if (k == 0){
                                    maxComplementMemberOverhead = memberOverhead;
                                }
                                else{
                                    if(maxComplementMemberOverhead < memberOverhead){
                                        maxComplementMemberOverhead = memberOverhead;
                                        maxComplementK = k;
                                    }
                                }
                            }

                            // Checking if maxMemberOverhead and maxComplementMemberOverhead is "OK"
                            if((maxMemberOverhead < maxOverHead  || ((double)maxMemberOverhead / Features.overheads[clusterI.getMembers().get(maxK)]) < maxOverHeadRate)
                                    && (maxComplementMemberOverhead < maxOverHead  || ((double)maxComplementMemberOverhead / Features.overheads[clusterJ.getMembers().get(maxComplementK)]) < maxOverHeadRate)){
                                flag = true;
                            }
                        }
                        // J to I is smaller
                        else{
                            Cluster clusterJ = Clusters.getClusters().get(j);

                            // Finding minDist
                            for(int k = 0; k < clusterJ.getMemberSize(); k++){
                                int memberId = clusterJ.getMembers().get(k);
                                int memberOverhead = Features.overheads[memberId] + clusterDistance;
                                if (k == 0){
                                    minMemberOverhead = memberOverhead;
                                    maxMemberOverhead = memberOverhead;
                                }
                                else{
                                    if(memberOverhead < minMemberOverhead){
                                        minMemberOverhead = minMemberOverhead;
                                        minK = k;
                                    }

                                    if(maxMemberOverhead < memberOverhead){
                                        maxMemberOverhead = memberOverhead;
                                        maxK = k;
                                    }
                                }
                            }

                            // Finding distance complement
                            Cluster clusterI = Clusters.getClusters().get(i);
                            for(int k = 0; k < clusterI.getMemberSize(); k++){
                                int memberId = clusterI.getMembers().get(k);
                                int memberOverhead = Features.overheads[memberId] + clusterDistanceComplement;
                                if (k == 0){
                                    maxComplementMemberOverhead = memberOverhead;
                                }
                                else{
                                    if(maxComplementMemberOverhead < memberOverhead){
                                        maxComplementMemberOverhead = memberOverhead;
                                        maxComplementK = k;
                                    }
                                }
                            }

                            // Checking if maxMemberOverhead and maxComplementMemberOverhead is "OK"
                            if((maxMemberOverhead < maxOverHead  || ((double)maxMemberOverhead / Features.overheads[clusterJ.getMembers().get(maxK)]) < maxOverHeadRate)
                                    && (maxComplementMemberOverhead < maxOverHead  || ((double)maxComplementMemberOverhead / Features.overheads[clusterI.getMembers().get(maxComplementK)]) < maxOverHeadRate)){
                                flag = true;
                            }
                        }
                        if(flag && minMemberOverhead < minDist){
                            minDist = minMemberOverhead;
                            minDistComplement = maxComplementMemberOverhead;
                            cl1 = i;
                            cl2 = j;
                        }
                    }
                }
            }
        }

        boolean flag = true;

        for(int i = 0; i < Clusters.getClusters().get(cl1).getMemberSize(); i++){
            if(Features.overheads[Clusters.getClusters().get(cl1).getMembers().get(i)] + minDist < maxOverHead ||
                    ((double)(Features.overheads[Clusters.getClusters().get(cl1).getMembers().get(i)] + minDist) /
                            Features.docSizes[Clusters.getClusters().get(cl1).getMembers().get(i)]) < maxOverHeadRate){
            }
            else{
                flag = false;
                break;
            }
        }
        if(flag){
            for(int i = 0; i < Clusters.getClusters().get(cl2).getMemberSize(); i++){
                if(Features.overheads[Clusters.getClusters().get(cl2).getMembers().get(i)] + minDistComplement < maxOverHead ||
                        ((double)(Features.overheads[Clusters.getClusters().get(cl2).getMembers().get(i)] + minDistComplement) /
                                Features.docSizes[Clusters.getClusters().get(cl2).getMembers().get(i)]) < maxOverHeadRate){
                }
                else{
                    flag = false;
                }
            }
        }


        if(!flag){
            return null;
        }
        else{
            if(cl1 < cl2){
                return new Pair(cl1, cl2);
            }
            else{
                return new Pair(cl2, cl1);
            }
        }
    }

}
