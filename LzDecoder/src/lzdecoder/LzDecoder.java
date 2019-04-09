/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lzdecoder;

import com.beust.jcommander.JCommander;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Arnau Vancells & Blai Ras
 */
public class LzDecoder {

    /**
     * @param argv the command line arguments
     */
    public static void main(String[] argv) {
        String[] argt = { "-i", "console"};
        
        Args args = new Args();
        JCommander.newBuilder().addObject(args).build().parse(argt);
        Scanner scanner = new Scanner(System.in);
        
        //Load input
        String inputData = "";
        if(args.input.equals("console")){
            System.out.println("Reading input from console: ");
            inputData = scanner.nextLine();
            if (inputData.matches("^[01]+$")) {
                // accept this input
                System.out.println("Input accepted.");
            }else{
                System.out.println("Input has an invalid character. Closing...");
                System.exit(0);
            }
        }else if(args.input.equals("random")){
            System.out.println("Generating random sequence...");
            for(int i = 0; i < args.randLen; i++){
                if(Math.random() < 0.5) {
                    inputData += "0";
                }else{
                    inputData += "1";
                }
            }
            
        }else{
            System.out.println("Reading from input file...");
            try {
                inputData = readFile(args.input);
            } catch (IOException ex) {
                Logger.getLogger(LzDecoder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Sequence: " + inputData);
        
        
        //Parameter checking
        if( args.mEnt % 2 != 0 || args.mDes% 2 != 0 || args.mEnt > args.mDes || args.mEnt + args.mDes > inputData.length()){
            System.out.println("Parameters used have wrong values. Check mEnt, mDes and the input data size.");
            System.exit(0);
        }
        
        //
        
        //Return result to output
        
    }
    
    
    public static String readFile(String fileName) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(fileName));
    try {
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();

        while (line != null) {
            sb.append(line);
            sb.append("\n");
            line = br.readLine();
        }
        return sb.toString();
    } finally {
        br.close();
    }
}
    
}

