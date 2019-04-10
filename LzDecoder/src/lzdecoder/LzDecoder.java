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
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Arnau Vancells & Blai Ras
 */
public class LzDecoder {
    
    public static ArrayList<String> desBuffer = new ArrayList<>();
    public static ArrayList<String> entBuffer = new ArrayList<>();
    public static String inputData = "";

    /**
     * @param argv the command line arguments
     */
    public static void main(String[] argv) {
        String[] argt = { "-i", "input_code.txt"};
        
        Args args = new Args();
        JCommander.newBuilder().addObject(args).build().parse(argt);
        Scanner scanner = new Scanner(System.in);
        
        //Load input
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

        
        if(args.mode){
            //decode 
        }else{
            //code
            String code = inputData.substring(0, args.mDes) + " ";
            //System.out.println(coded);
            int pos = 0;
            code = searchBuffer(args, code);
            code = searchBuffer(args, code);
        }
        
        
        
        
        
        
        
        //Return result to output
        
    }
    /*
    if(entBuffer.size() < args.mEnt){
                int freeSpots = args.mEnt - entBuffer.size();
                    for(int i= 0; i < freeSpots; i++){
                        entBuffer.add(inputData.substring(0,1));
                        inputData = inputData.substring(1);
                    }           
            }            
            
            if(desBuffer.size() < args.mDes){
                int freeSpots = args.mDes - desBuffer.size();
                for(int i= 0; i < freeSpots; i++){
                    desBuffer.add(entBuffer.remove(0));
                }            
            }*/
    
    public static String searchBuffer(Args args, String code){
        while(desBuffer.size() != args.mDes || entBuffer.size() != args.mEnt){ 
            if(entBuffer.size() < args.mEnt){
                entBuffer.add(inputData.substring(0,1));
                inputData = inputData.substring(1);    
            }            
            
            if(desBuffer.size() < args.mDes){
                desBuffer.add(entBuffer.remove(0));     
                entBuffer.trimToSize();
            }
            
        }
        System.out.println(desBuffer.toString());
        System.out.println(entBuffer.toString());
        
        
        
        
        //search matching data
        String tempEnt = "";
        String tempDes = getStringBuffer(desBuffer.size(), desBuffer);
        int posMatch = -1;
        for(int i = entBuffer.size(); i != 0; i--){
            tempEnt = getStringBuffer(i, entBuffer);
            
            if(tempDes.contains(tempEnt)){
                //System.out.println("\n" + tempEnt);
                //System.out.println(tempDes.indexOf(tempEnt));
                
                posMatch = tempDes.indexOf(tempEnt);
                break;
            }   
            
        }
        
        //code substring position
        int nDes = (int) (Math.log(desBuffer.size())/Math.log(2));
        int nEnt = (int) (Math.log(entBuffer.size())/Math.log(2));
        
        String codedL = String.format("%" + nEnt + "s", Integer.toBinaryString(tempEnt.length())).replace(' ', '0');
        String codedD = String.format("%" + nDes + "s", Integer.toBinaryString(desBuffer.size() - posMatch)).replace(' ', '0');
        //System.out.println("L: " + codedL);
        //System.out.println("D: " + codedD);
        if(posMatch == -1){
            code += tempEnt.substring(0,1) + " ";
            desBuffer.remove(0);
            desBuffer.trimToSize();
            
            
        }else{
            code += codedL + " ";
            code += codedD + " ";
            if(tempEnt.length() == 1){
                desBuffer.remove(0);
                desBuffer.trimToSize();
            }else{
                for(int i = 0; i < tempEnt.length() - 1; i++){
                    desBuffer.remove(i);
                    desBuffer.trimToSize();

                    entBuffer.remove(i);
                    entBuffer.trimToSize();
                }
            }
            
            
        }  
        System.out.println("Dades restants: " + inputData);    
        System.out.println("Codificació: " + code);
        System.out.println("---------");
               
        return code;
        
        
    }

    public static String getStringBuffer(int size, ArrayList<String> buffer){
        String result = "";
        for(int i = 0; i< size; i++){
            result += buffer.get(i);
        }
        
        return result;
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

