package EnronClusteringTest;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
    private double maxDocRepetition; // Maximum number of times a document is repeated
    private boolean showMergingMessages;
    private boolean minStorage;
    private boolean withinOverheadBounds;
    private boolean averageDistLinkage;
    private boolean minSum;
    private String mergeFileName;


    public HierarchicalClustering(byte[][] features, int maxOverHead, double maxOverHeadRate, double maxDocRepetition, boolean minStorage,
                                  boolean averageDistLinkage, boolean minSum, boolean showMergingMessages, String mergeFileName){
        Features.setFeatures(features);
        this.maxOverHead = maxOverHead;
        this.maxOverHeadRate = maxOverHeadRate;
        this.maxDocRepetition = maxDocRepetition;
        this.showMergingMessages = showMergingMessages;
        this.minStorage = minStorage;
        this.averageDistLinkage = averageDistLinkage;
        this.minSum = minSum;
        this.withinOverheadBounds = true;
        this.mergeFileName = mergeFileName;

        numSamples = features.length;
        numFeatures = features[0].length;
        distances = new int[numSamples][numFeatures];
        Clusters.setClusters(new ArrayList<Cluster>());
        Clusters.setInvalidIds(new HashSet<Integer>(numSamples));
    }

    // Main method to run the clustering algorithm
    public void runClustering () throws IOException {
        calculateInitialDistances(); // Setting initial clusters/distances
        FileWriter fw = new FileWriter(mergeFileName);
        PrintWriter out = new PrintWriter(fw);


        // Clustering Loop
        Pair closestDistancePair;
        do{
            if(averageDistLinkage){
                closestDistancePair = findAverageClosestClusters();
            }
            else{
                closestDistancePair = findClosestClusters();
            }
            if(closestDistancePair != null){
                updateDistances(closestDistancePair);
                if(showMergingMessages){
                    out.println("Merging clusters " + closestDistancePair.getNum1() + " and " + closestDistancePair.getNum2() + ".");
                }
            }

            else{
                if(!minStorage){
                    break;
                }
            }
        } while((!minStorage && closestDistancePair != null) || (minStorage && !checkDocRepetition()));
        out.close();
    }

    // Calculating initial distances between samples/clusters,
    // as well as creating the initial clusters
    private void calculateInitialDistances(){
        long startTime = System.currentTimeMillis();
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
                        if(Features.features[i][k] == 0 && Features.features[j][k] == 1){
                            dist++;
                        }
                    }
                    distances[i][j] = dist;
                }
            }
        }
        long endTime = System.currentTimeMillis();
        long runTimeInMs = endTime - startTime;
        long runTimeInS = (runTimeInMs) / 1000;
        long runTimeInM = runTimeInS / 60;
        runTimeInS = (runTimeInS) % 60;

        System.out.println("Done calculating initial distances in " + runTimeInM + " minutes and " + runTimeInS + "s.");
        System.out.println();

    }

    // Updating distances after merging two clusters
    private void updateDistances(Pair clusterIds){
        int cl1 = clusterIds.getNum1();
        int cl2 = clusterIds.getNum2();

        Clusters.getClusters().get(cl1).addMember(cl2);
        Clusters.addInvalidId(new Integer(cl2));
        for(int i = 0; i < Clusters.getClusters().size(); i++){
            if(!Clusters.getInvalidIds().contains(new Integer(i))){
                int distCl1ToI = 0;
                int distIToCl1 = 0;
                for(int k = 0; k < numFeatures; k++){
                    if(Clusters.getClusters().get(cl1).union[k] == 0 && Clusters.getClusters().get(i).union[k] == 1){
                        distCl1ToI++;
                    }
                    else if(Clusters.getClusters().get(cl1).union[k] == 1 && Clusters.getClusters().get(i).union[k] == 0){
                        distIToCl1++;
                    }
                }
                distances[cl1][i] = distCl1ToI;
                distances[i][cl1] = distIToCl1;
            }
        }
    }

    private boolean checkDocRepetition(){
        ArrayList<Cluster> clusters = Clusters.getClusters();
        int[] docsClustering = new int[numFeatures];
        int documentsInSample = numFeatures;

        for(int i = 0; i < clusters.size(); i++){
            if(!Clusters.getInvalidIds().contains(i)){

                // Loop to calculate docsClustering
                for(int j = 0; j < numFeatures; j++){
                    docsClustering[j] += clusters.get(i).union[j];
                }
            }
        }

        // Clustering Test
        int numberOfUniqueRepeatedDocsClustering = 0;
        int totalRepClustering = 0;

        for(int i = 0; i < numFeatures; i++){
            if(docsClustering[i] != 0){
                totalRepClustering = totalRepClustering + docsClustering[i] - 1;
                if(docsClustering[i] > 1){
                    numberOfUniqueRepeatedDocsClustering++;
                }
            }
            else{
                documentsInSample--;
            }
        }

        double storageOverhead = ((double)(totalRepClustering) / documentsInSample);

        return storageOverhead <= maxDocRepetition;
    }

    // Bug with only merging cluster 0
    private Pair findAverageClosestClusters(){
        if(Clusters.getInvalidIds().size() >= Clusters.getClusters().size() - 1){
            return null;
        }
        double globalMinDist;
        int globalMinDistMax;
        int globalMinSum;
        int cl1, cl2;
        double globalMinDistMaxOR = 1;
        boolean flag = false;

        // Finding initial minDist
        cl1 = 0;
        cl2 = 1;
        for(int i = 0; i < Clusters.getClusters().size(); i++){
            if(!Clusters.getInvalidIds().contains(i)){
                cl1 = i;
                break;
            }
        }
        for(int i = cl1 + 1; i < Clusters.getClusters().size(); i++){
            if(!Clusters.getInvalidIds().contains(i)){
                cl2 = i;
                break;
            }
        }

        int initialSumDistances = 0;
        int initialMax = Features.overheads[Clusters.getClusters().get(cl1).getMembers().get(0)] + distances[cl1][cl2];
        double initialMaxOR = (double) initialMax / Features.docSizes[Clusters.getClusters().get(cl1).getMembers().get(0)];

        for(int i = 0; i < Clusters.getClusters().get(cl1).getMemberSize(); i++){
            int localDist = Features.overheads[Clusters.getClusters().get(cl1).getMembers().get(i)] + distances[cl1][cl2];
            double localOR = (double) localDist / Features.docSizes[Clusters.getClusters().get(cl1).getMembers().get(i)];

            if(localDist > initialMax){
                initialMax = localDist;
            }
            if(localOR > initialMaxOR){
                initialMaxOR = localOR;
            }

            initialSumDistances += localDist;
        }
        for(int i = 0; i < Clusters.getClusters().get(cl2).getMemberSize(); i++){
            int localDist = Features.overheads[Clusters.getClusters().get(cl2).getMembers().get(i)] + distances[cl2][cl1];
            double localOR = (double) localDist / Features.docSizes[Clusters.getClusters().get(cl2).getMembers().get(i)];

            if(localDist > initialMax){
                initialMax = localDist;
            }
            if(localOR > initialMaxOR){
                initialMaxOR = localOR;
            }

            initialSumDistances += localDist;
        }
        globalMinSum = initialSumDistances;
        globalMinDist = (double) initialSumDistances / (Clusters.getClusters().get(cl1).getMemberSize() +
                Clusters.getClusters().get(cl2).getMemberSize());
        globalMinDistMax = initialMax;
        globalMinDistMaxOR = initialMaxOR;

        int similarity = 0;
        for(int k = 0; k < Clusters.getClusters().get(cl1).union.length; k++){
            if(Clusters.getClusters().get(cl1).union[k] == 1 && Clusters.getClusters().get(cl2).union[k] == 1){
                similarity++;
            }
        }


        if(0 < similarity && (minStorage || (globalMinDistMax < maxOverHead && globalMinDistMaxOR < maxOverHeadRate))){
            flag = true;
        }

        // Loop to find actual minDist
        for(int i = 0; i < Clusters.getClusters().size(); i++){
            if(!Clusters.getInvalidIds().contains(new Integer(i))){
                for(int j = i + 1; j < Clusters.getClusters().size(); j++){
                    if(!Clusters.getInvalidIds().contains(new Integer(j))){
                        Cluster clusterI = Clusters.getClusters().get(i);
                        Cluster clusterJ = Clusters.getClusters().get(j);

                        int iToJ = distances[i][j];
                        int jToI = distances[j][i];

                        int sumDistances = 0;
                        int maxDistance = Features.overheads[Clusters.getClusters().get(i).getMembers().get(0)] + iToJ;
                        double maxOR = (double) maxDistance / Features.docSizes[Clusters.getClusters().get(i).getMembers().get(0)];

                        for(int k = 1; k < clusterI.getMemberSize(); k++){
                            int localDist = Features.overheads[clusterI.getMembers().get(k)] + iToJ;
                            double localOR = (double )localDist / Features.docSizes[clusterI.getMembers().get(k)];

                            if(localDist > maxDistance){
                                maxDistance = localDist;
                            }
                            if(localOR > maxOR){
                                maxOR = localOR;
                            }

                            sumDistances += localDist;
                        }

                        for(int k = 0; k < clusterJ.getMemberSize(); k++){
                            int localDist = Features.overheads[clusterJ.getMembers().get(k)] + jToI;
                            double localOR = (double )localDist / Features.docSizes[clusterJ.getMembers().get(k)];

                            if(localDist > maxDistance){
                                maxDistance = localDist;
                            }
                            if(localOR > maxOR){
                                maxOR = localOR;
                            }

                            sumDistances += localDist;
                        }

                        double averageDist = (double) sumDistances / (clusterI.getMemberSize() + clusterJ.getMemberSize());

                        if(minSum){
                            if(sumDistances < globalMinSum){
                                similarity = 0;
                                for(int k = 0; k < clusterI.union.length; k++){
                                    if(clusterI.union[k] == 1 && clusterJ.union[k] == 1){
                                        similarity++;
                                    }
                                }

                                if(minStorage){
                                    cl1 = i;
                                    cl2 = j;
                                    globalMinDist = averageDist;
                                    globalMinSum = sumDistances;
                                    globalMinDistMax = maxDistance;
                                    globalMinDistMaxOR = maxOR;
                                    flag = true;
                                }
                                else if(0 < similarity && (maxDistance < maxOverHead || maxOR < maxOverHeadRate)){
                                    cl1 = i;
                                    cl2 = j;
                                    globalMinDist = averageDist;
                                    globalMinSum = sumDistances;
                                    globalMinDistMax = maxDistance;
                                    globalMinDistMaxOR = maxOR;
                                    flag = true;
                                }
                            }
                        }
                        else{
                            if(averageDist < globalMinDist){
                                if(minStorage){
                                    cl1 = i;
                                    cl2 = j;
                                    globalMinDist = averageDist;
                                    globalMinDistMax = maxDistance;
                                    globalMinDistMaxOR = maxOR;
                                    flag = true;
                                }
                                else if(maxDistance < maxOverHead || maxOR < maxOverHeadRate){
                                    cl1 = i;
                                    cl2 = j;
                                    globalMinDist = averageDist;
                                    globalMinDistMax = maxDistance;
                                    globalMinDistMaxOR = maxOR;
                                    flag = true;
                                }
                            }
                        }

                    }
                }
            }
        }


        if(flag){
            if(cl1 < cl2){
                return new Pair(cl1, cl2);
            }
            else{
                return new Pair(cl2, cl1);
            }
        }
        else{
            return null;
        }

    }


    private Pair findClosestClusters(){

        // Initial Minimum Distance
        int minDist, minDistMax;
        int cl1, cl2;
        double minDistMaxOR = 1;
        boolean initialFlag = true;
        boolean changeFlag = false;
        int ctr = 0;
        do{
            cl1 = ctr;
            cl2 = ctr + 1;
            for(int i = 0; i < Clusters.getClusters().size(); i++){
                if(!Clusters.getInvalidIds().contains(i)){
                    cl1 = i;
                    break;
                }
            }
            for(int i = cl1 + 1; i < Clusters.getClusters().size(); i++){
                if(!Clusters.getInvalidIds().contains(i)){
                    cl2 = i;
                    break;
                }
            }
            minDist = Features.overheads[Clusters.getClusters().get(cl1).getMembers().get(0)] + distances[cl1][cl2];
            minDistMax = Features.overheads[Clusters.getClusters().get(cl1).getMembers().get(0)] + distances[cl1][cl2];
            for(int i = 0; i < Clusters.getClusters().get(cl1).getMemberSize(); i++){
                int localDist = Features.overheads[Clusters.getClusters().get(cl1).getMembers().get(i)] + distances[cl1][cl2];
                if(localDist < minDist){
                    minDist = localDist;
                }
                if(minDistMax < localDist){
                    minDistMax = localDist;
                    minDistMaxOR = (double)localDist / Features.docSizes[Clusters.getClusters().get(cl1).getMembers().get(i)];
                }
            }
            for(int i = 0; i < Clusters.getClusters().get(cl2).getMemberSize(); i++){
                int localDist = Features.overheads[Clusters.getClusters().get(cl2).getMembers().get(i)] + distances[cl2][cl1];
                if(localDist < minDist){
                    minDist = localDist;
                }
                if(minDistMax < localDist){
                    minDistMax = localDist;
                    minDistMaxOR = (double)localDist / Features.docSizes[Clusters.getClusters().get(cl2).getMembers().get(i)];
                }
            }
            if (minStorage || minDistMax < maxOverHead || minDistMaxOR < maxOverHeadRate){
                initialFlag = false;
            }
        } while(false);




        // Finding the actual minimum distance
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
                            // OR minStorage is true
                            if((minStorage || maxMemberOverhead < maxOverHead  || ((double)maxMemberOverhead / Features.docSizes[clusterI.getMembers().get(maxK)]) < maxOverHeadRate)
                                    && (maxComplementMemberOverhead < maxOverHead  || ((double)maxComplementMemberOverhead / Features.docSizes[clusterJ.getMembers().get(maxComplementK)]) < maxOverHeadRate)){
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
                            // OR minStorage is true
                            if((minStorage || maxMemberOverhead < maxOverHead  || ((double)maxMemberOverhead / Features.docSizes[clusterJ.getMembers().get(maxK)]) < maxOverHeadRate)
                                    && (maxComplementMemberOverhead < maxOverHead  || ((double)maxComplementMemberOverhead / Features.docSizes[clusterI.getMembers().get(maxComplementK)]) < maxOverHeadRate)){
                                flag = true;
                            }
                        }
                        if(flag && minMemberOverhead < minDist){
                            minDist = minMemberOverhead;
                            minDistMax = maxComplementMemberOverhead;
                            cl1 = i;
                            cl2 = j;
                            changeFlag = true;
                        }
                    }
                }
            }
        }

        /*boolean flag = true;

        for(int i = 0; i < EnronClusteringTest.Clusters.getClusters().get(cl1).getMemberSize(); i++){
            if(EnronClusteringTest.Features.overheads[EnronClusteringTest.Clusters.getClusters().get(cl1).getMembers().get(i)] + minDist < maxOverHead ||
                    ((double)(EnronClusteringTest.Features.overheads[EnronClusteringTest.Clusters.getClusters().get(cl1).getMembers().get(i)] + minDist) /
                            EnronClusteringTest.Features.docSizes[EnronClusteringTest.Clusters.getClusters().get(cl1).getMembers().get(i)]) < maxOverHeadRate){
            }
            else{
                flag = false;
                break;
            }
        }
        if(flag){
            for(int i = 0; i < EnronClusteringTest.Clusters.getClusters().get(cl2).getMemberSize(); i++){
                if(EnronClusteringTest.Features.overheads[EnronClusteringTest.Clusters.getClusters().get(cl2).getMembers().get(i)] + minDistComplement < maxOverHead ||
                        ((double)(EnronClusteringTest.Features.overheads[EnronClusteringTest.Clusters.getClusters().get(cl2).getMembers().get(i)] + minDistComplement) /
                                EnronClusteringTest.Features.docSizes[EnronClusteringTest.Clusters.getClusters().get(cl2).getMembers().get(i)]) < maxOverHeadRate){
                }
                else{
                    flag = false;
                }
            }
        }


        if(!flag){
            return null;
        }
        else{*/
        if(!initialFlag || changeFlag){
            if(cl1 < cl2){
                return new Pair(cl1, cl2);
            }
            else{
                return new Pair(cl2, cl1);
            }
        }
        else{
            return null;
        }


        //}
    }

}
