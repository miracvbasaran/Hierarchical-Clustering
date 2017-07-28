package FeatureExtraction;

import java.io.*;
import java.util.Scanner;

/**
 * Created by Mirac Vuslat Basaran on 06-Jul-17.
 */
public class EnronFeatureExtraction {

    public static void main(String[] args) throws IOException{
        String enronFileName = "output_inverted_index.csv";
        BufferedReader reader = new BufferedReader(new FileReader(enronFileName));

        String line = null;
        Scanner scan = null;


        int numSamples = 76577;
        int numFeatures = 30109;
        int lineNo = 0;

        byte[][] features = new byte[numSamples][numFeatures];

        while((line = reader.readLine()) != null ){
            int index = 0;
            scan = new Scanner(line);
            scan.useDelimiter(", ");
            while(scan.hasNext()){
                String data = scan.next();
                if(index != 0){
                    features[lineNo][Integer.parseInt(data) - 1] = 1;
                }
                index++;
            }
            lineNo++;
        }

        reader.close();
        FileWriter fw = new FileWriter("features.txt");
        //Writer writer = new BufferedWriter(fw);

        for(int i = 0; i < numSamples; i++){
            for(int j = 0; j < numFeatures; j++){
                if(j == numFeatures - 1){
                    if(i == numSamples - 1){
                        fw.write(features[i][j]);
                    }
                    else{
                        fw.write(features[i][j] + "\n");
                    }
                }
                else{
                    fw.write(features[i][j] + " ");
                }
            }
        }
        //writer.close();
        fw.flush();
        fw.close();

        /*for(int i = 0; i < numSamples; i++){
            System.out.print("Word " + i + ": ");
            for(int j = 0; j < numFeatures; j++){
                System.out.print(features[i][j] + " ");
            }
            System.out.print("\n");
        }*/
    }
}
