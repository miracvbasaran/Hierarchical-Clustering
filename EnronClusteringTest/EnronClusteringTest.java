package EnronClusteringTest;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Mirac Vuslat Basaran on 07-Jul-17.
 */
public class EnronClusteringTest {
    public static long startTime, endTime, runTime, overallStartTime, overallEndTime, overallRunTimeInMs, overallRunTimeInS, overallRunTimeInM;
    public static int numSamples = 76577;
    public static int numFeatures = 30109;
    public static byte[][] features;
    public static int subsetSampleStartingIndex, subsetSampleSize, subsetSampleEndingIndex;
    public static int maxOverHead;
    public static double maxOverHeadRate;
    public static double maxDocRepetition;
    public static boolean minStorage;
    public static boolean averageDistanceLinkage;
    public static boolean minSum;
    private static String mergeFileName;
    private static String clusterFileName;


    public static void main(String[] args) throws IOException{
        // Arguments In Order:
        // 0: Start Index
        // 1: Subset Size
        // 2: Overhead
        // 3: Overhead Rate
        // 4: MDR
        // 5: MS
        // 6: Average Distance Linkage
        // 7: min Sum
        // 8: outputFileName
        String fileName = "features.txt";
        int startIndex = Integer.parseInt(args[0]);
        int size = Integer.parseInt(args[1]);

        // Reading From File;
        readFeatures(fileName, startIndex, size);

        // Running the clustering Algorithm with *some* parameters
        int O = Integer.parseInt(args[2]);
        double OR = Double.parseDouble(args[3]);
        boolean showMergingMessages = true;
        double MDR = Double.parseDouble(args[4]);
        boolean MS = Integer.parseInt(args[5]) == 1;
        boolean ADL = Integer.parseInt(args[6]) == 1;
        boolean minS = Integer.parseInt(args[7]) == 1;
        String outFileName = "TestWithParameters_" + args[0] + "_" + args[1] + "_" + args[2] + "_" + args[3] + "_" +
                args[4] + "_" + args[5] + "_" + args[6] + "_" + args[7] + ".txt";
        mergeFileName = "MergingWithParameters_" + args[0] + "_" + args[1] + "_" + args[2] + "_" + args[3] + "_" +
                args[4] + "_" + args[5] + "_" + args[6] + "_" + args[7] + ".txt";
        clusterFileName = "ClusterIdsWithParameters_" + args[0] + "_" + args[1] + "_" + args[2] + "_" + args[3] + "_" +
                args[4] + "_" + args[5] + "_" + args[6] + "_" + args[7] + ".txt";

        runClustering(O, OR, MDR, MS, ADL, minS, showMergingMessages);

        // Running tests on the clusters


        runTests(outFileName, true, true, true,true);

        // Finish Program
        overallEndTime = System.currentTimeMillis();
        overallRunTimeInMs = overallEndTime - overallStartTime;
        overallRunTimeInS = overallRunTimeInMs / 1000;
        overallRunTimeInM = overallRunTimeInS / 60;
        overallRunTimeInS = overallRunTimeInS % 60;
        System.out.println();
        System.out.println("Overall running time: " + overallRunTimeInM + " minutes " + overallRunTimeInS + " s.");
    }

    public static void readFeatures(String fileName, int startIndex, int size) throws IOException{
        String enronFeatureFileName = fileName;
        BufferedReader reader = new BufferedReader(new FileReader(enronFeatureFileName));

        String line = null;
        Scanner scan = null;


        int lineNo = 0;

        subsetSampleStartingIndex = startIndex;
        subsetSampleSize = size;
        subsetSampleEndingIndex = subsetSampleStartingIndex + subsetSampleSize;

        features = new byte[subsetSampleSize][numFeatures];

        overallStartTime = System.currentTimeMillis();
        startTime = System.currentTimeMillis();
        System.out.println("Started reading from file.");
        while((line = reader.readLine()) != null && lineNo < subsetSampleEndingIndex){
            if(lineNo >= subsetSampleStartingIndex){
                int index = 0;
                scan = new Scanner(line);
                scan.useDelimiter(" ");
                while(scan.hasNext()){
                    String data = scan.next();
                    features[lineNo - subsetSampleStartingIndex][index++] = Byte.parseByte(data);
                }
            }

            lineNo++;
        }
        endTime = System.currentTimeMillis();
        runTime = endTime - startTime;
        reader.close();
        System.out.println("Finished reading from file in " + runTime + " ms.");
    }

    public static void runClustering(int O, double OR, double maxDocRep, boolean MS, boolean ADL, boolean minS, boolean showMergingMessages) throws IOException{
        maxOverHead = O;
        maxOverHeadRate = OR;
        maxDocRepetition = maxDocRep;
        minStorage = MS;
        averageDistanceLinkage = ADL;
        minSum = minS;
        startTime = System.currentTimeMillis();
        System.out.println("Started clustering.");
        HierarchicalClustering clusterer = new HierarchicalClustering(features, maxOverHead, maxOverHeadRate, maxDocRepetition, minStorage, averageDistanceLinkage, minSum, showMergingMessages, mergeFileName);
        clusterer.runClustering();
        endTime = System.currentTimeMillis();
        runTime = endTime - startTime;
        System.out.println();
        System.out.println("Finished clustering " + runTime + " ms.");
    }


    public static void runTests (String outFileName, boolean docRepTest, boolean individualDocTest, boolean overheadTest, boolean individualKeywordTest) throws IOException{
        startTime = System.currentTimeMillis();

        FileWriter fw = new FileWriter(outFileName);
        PrintWriter out = new PrintWriter(fw);

        out.println("Started running tests.");
        ArrayList<Cluster> clusters = Clusters.getClusters();
        out.println("Total Number Of Clusters: " + (clusters.size() - Clusters.getInvalidIds().size()));
        out.println();
        out.println("Testing with " + subsetSampleSize + " keywords starting from index " + subsetSampleStartingIndex +
                " with maxOverHead " + maxOverHead + ", maxOverHeadRate " + maxOverHeadRate + ", maxDocRepetition " + maxDocRepetition +
                " and minimizeStorageOverhead: " + minStorage);

        if(docRepTest) {
            int[] docsClustering = new int[numFeatures];
            int[] docsNaive = new int[numFeatures];

            for(int i = 0; i < clusters.size(); i++){
                if(!Clusters.getInvalidIds().contains(i)){

                    // Loop to calculate docsClustering
                    for(int j = 0; j < numFeatures; j++){
                        docsClustering[j] += clusters.get(i).union[j];
                    }

                    int memberSize = clusters.get(i).getMemberSize();
                    int unionSize = clusters.get(i).getUnionSize();
                    if(individualKeywordTest){
                        out.println("EnronClusteringTest.Cluster " + i + " --> Member Size: " + memberSize + " Doc Union Size: " + unionSize);
                        if(memberSize > 1){
                            out.println("Members of this cluster: ");
                            for(int j = 0; j < memberSize; j++){
                                int memberNo = clusters.get(i).getMembers().get(j);
                                int memberDocSize = Features.docSizes[memberNo];
                                out.print("\t");
                                out.println(i + ") Sample No: " + memberNo + " --> Doc Size: " + memberDocSize +
                                        " Overhead: " + (unionSize - memberDocSize) + " Overhead Rate: " +
                                        ((double)(unionSize - memberDocSize) / memberDocSize));
                            }
                    }

                    }
                }
            }

            out.println("");
            out.println("Tests on Document Repetition:");

            // Naive Test
            int numberOfUniqueRepeatedDocsNaive = 0;
            int totalRepNaive = 0;
            int documentsInSample = numFeatures;

            for(int i = 0; i < numFeatures; i++){
                docsNaive[i] = 0;
                for(int j = 0; j  < subsetSampleSize; j++){
                    docsNaive[i] += features[j][i];
                }

                if (docsNaive[i] == 0){
                    documentsInSample--;
                }
                else{
                    totalRepNaive += docsNaive[i] - 1;
                    if(1 < docsNaive[i]){
                        numberOfUniqueRepeatedDocsNaive++;
                    }
                }
            }



            // Clustering Test
            int numberOfUniqueRepeatedDocsClustering = 0;
            int totalRepClustering = 0;
            if(individualDocTest){
                out.println("Individual Document Storage Test:\n");
                out.println("\t\t\tNaive Approach\tClustering");
            }
            for(int i = 0; i < numFeatures; i++){
                if(docsClustering[i] != 0){
                    if(individualDocTest){
                        out.println("Document " + i + ":\t" + docsClustering[i] + "\t" +
                                            docsNaive[i]);
                    }
                    totalRepClustering = totalRepClustering + docsClustering[i] - 1;
                    if(docsClustering[i] > 1){
                        numberOfUniqueRepeatedDocsClustering++;
                    }
                }
            }

            out.println();
            out.println("NAIVE --> Total number of unique repeated documents is " + numberOfUniqueRepeatedDocsNaive + " out of " +
                    documentsInSample + " total documents.");
            out.println("NAIVE --> Total number of repeated documents is " + totalRepNaive + " out of " +
                    documentsInSample + " total documents.");
            out.println("NAIVE --> Document repetition overhead is " + ((double)(totalRepNaive) / documentsInSample) + " out of " +
                    documentsInSample + " total documents.");
            out.println();
            out.println("CLUSTERING --> Total number of unique repeated documents is " + numberOfUniqueRepeatedDocsClustering + " out of " +
                    documentsInSample + " total documents.");
            out.println("CLUSTERING --> Total number of repeated documents is " + totalRepClustering + " out of " +
                    documentsInSample + " total documents.");
            out.println("CLUSTERING --> Document repetition overhead is " + ((double)(totalRepClustering) / documentsInSample) + " out of " +
                    documentsInSample + " total documents.");
        }

        if(overheadTest){
            out.println("");
            out.println("Tests on Query Overhead:");

            int numKeywordsExceedingMaxO = 0;
            int numKeywordsExceedingMaxOR = 0;
            int numKeywordsExceedingBoth = 0;
            double averageDocSize = 0;
            double averageOverheadInNumDocs = 0;
            double averageOverheadRate = 0;

            for(int i = 0; i < subsetSampleSize; i++){
                int overhead = Features.overheads[i];
                double overheadRate = (double)Features.overheads[i] / Features.docSizes[i];

                if(overhead > maxOverHead){
                    numKeywordsExceedingMaxO++;
                    if(overheadRate > maxOverHeadRate){
                        numKeywordsExceedingMaxOR++;
                        numKeywordsExceedingBoth++;
                    }
                }
                else{
                    if(overheadRate > maxOverHeadRate){
                        numKeywordsExceedingMaxOR++;
                    }
                }

                averageDocSize += Features.docSizes[i];
                averageOverheadInNumDocs += overhead;
                averageOverheadRate += overheadRate;
            }
            averageDocSize = averageDocSize / subsetSampleSize;
            averageOverheadInNumDocs = averageOverheadInNumDocs / subsetSampleSize;
            averageOverheadRate = averageOverheadRate / subsetSampleSize;

            out.println("Total number of keywords: "  + subsetSampleSize);
            out.println("Total number of keywords exceeding O: " + maxOverHead +  " is " + numKeywordsExceedingMaxO + " keywords.");
            out.println("Total number of keywords exceeding OR: " + maxOverHeadRate +  " is " + numKeywordsExceedingMaxOR + " keywords.");
            out.println("Total number of keywords exceeding both O and OR is " + numKeywordsExceedingBoth + " keywords.");
            out.println("Average Query Size: " + averageDocSize);
            out.println("Average Overhead In Number Of Documents: " + averageOverheadInNumDocs);
            out.println("Average Overhead Rate: " + averageOverheadRate);

            out.close();
        }

        fw = new FileWriter(clusterFileName);
        out = new PrintWriter(fw);

        // Storing clusters
        int[] keywordToCluster = new int[subsetSampleSize];
        for(int i = 0; i < Clusters.getClusters().size(); i++){
            for(int j = 0; j < Clusters.getClusters().get(i).members.size(); j++){
                keywordToCluster[Clusters.getClusters().get(i).members.get(j)] = i;
            }
        }
        for(int i = 0; i < subsetSampleSize; i++){
            out.println((i + subsetSampleStartingIndex) + " " + keywordToCluster[i]);
        }


        endTime = System.currentTimeMillis();
        runTime = endTime - startTime;
        System.out.println("Finished running tests " + runTime + " ms.");
    }


}
