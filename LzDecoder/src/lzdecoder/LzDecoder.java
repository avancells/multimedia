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
    public static ArrayList<String> sequences = new ArrayList<>();
    public static String savedInput = "";
    public static String inputData = "";

    /**
     * @param argv the command line arguments
     */
    public static void main(String[] argv) {
        String[] argt = { "-i", "random", "-mode", "0"};
        
        Args args = new Args();
        JCommander.newBuilder().addObject(args).build().parse(argt);
        Scanner scanner = new Scanner(System.in);
        
        //Load input
        if(args.input.equals("console")){
            System.out.println("Reading input from console: ");
            inputData = scanner.nextLine();
            if (inputData.matches("^[01 ]+$")) {
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
        savedInput = inputData;
        
        
        //Parameter checking
        if( !esPotencia(args.mEnt) || !esPotencia(args.mDes) || args.mEnt > args.mDes || args.mEnt + args.mDes > inputData.length()){
            System.out.println("Parameters used have wrong values. Check mEnt, mDes and the input data size.");
            System.exit(0);
        }

        
        if(args.mode == 1){
            decodeSeq(inputData);
            
        }else{
            //code  
            System.out.println("- Coding mode -");
            String code = inputData.substring(0, args.mDes) + " ";
            //System.out.println(coded);
            while(!inputData.isEmpty() || entBuffer.size() == args.mEnt){
                code = searchBuffer(args, code);
            }
            //System.out.println(inputData);
            
            
            System.out.println("Coded result: " + code);
            
            //Check if correct
            if (savedInput.equals(decodeSeq(code))) {
                System.out.println("PERF");
            } else {
                System.out.println("FFFFFFFFFF");
            }
        }
        
        

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
    
    public static String decodeString(String decode){
        String elem1 = sequences.remove(0);
        sequences.trimToSize();
        if(sequences.isEmpty()){
            decode += elem1;
            return decode;
        }
        
        // if the element read is a 0 or a 1, it means we need to insert 
        // directly that value to the decoded sequence
        if(elem1.length() == 1){
            // Add the element to the decoded sequence
            decode += elem1;
            // Remove the first element of the buffer
            desBuffer.remove(0);
            // Add the new element to the end of it
            desBuffer.add(elem1);
            
            desBuffer.trimToSize();
            return decode;
        }else{ // if the element read is longer than 1, its part of a coded pair (L)
            // We obtain the second element, corresponding to D
            String elem2 = sequences.remove(0);
            
            int value1 = Integer.parseInt(elem1, 2);            
            // if the value is 0, its equivalent to 2^length
            if(value1 == 0){
                value1 = (int) Math.pow(2, elem1.length());
            }
            
            int value2 = Integer.parseInt(elem2, 2);
            // if the value is 0, its equivalent to 2^length
            if(value2 == 0){
                value2 = (int) Math.pow(2, elem2.length());
            }
            
                       
            desBuffer.trimToSize();
            
            String entString = "";
            String tempValue = "";
            //Taking L as number of chars to get from the buffer
            int desSize = desBuffer.size();
            for(int i = 0; i < value1; i++){
                
                tempValue = desBuffer.get(desSize - value2 + i  );
                entString += tempValue;
                desBuffer.add(tempValue);
            }
            // clear the buffer
            for(int i = 0; i < value1; i++){
                desBuffer.remove(0);
            }
            desBuffer.trimToSize();
            
            decode += entString;
            return decode;
        }
    }
    
    public static boolean esPotencia(int x){      
        return (x & (x - 1)) == 0;
    }
    
    public static String searchBuffer(Args args, String code){
        if(inputData.isEmpty()){
            System.out.println("cagarro " + entBuffer.toString());
            System.out.println("titola " + desBuffer.toString());
            entBuffer.clear();
            return code;
        }
        while(desBuffer.size() != args.mDes || entBuffer.size() != args.mEnt){ 
            if(entBuffer.size() < args.mEnt){
                if(!inputData.isEmpty()){
                    entBuffer.add(inputData.substring(0,1));
                    inputData = inputData.substring(1);
                }
                    
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
        
        //System.out.println(tempEnt.length());
        //System.out.println(desBuffer.size() - posMatch);
        
        String codedL = "";
        if(tempEnt.length() == entBuffer.size()){ // if the length is the maximum, set to 0
            codedL = String.format("%" + nEnt + "s", Integer.toBinaryString(0)).replace(' ', '0');
        }else{
            codedL = String.format("%" + nEnt + "s", Integer.toBinaryString(tempEnt.length())).replace(' ', '0');
        }
        
        String codedD = "";
        if(desBuffer.size() - posMatch == desBuffer.size()){ // if the length is the maximum, set to 0
            codedD = String.format("%" + nDes + "s", Integer.toBinaryString(0)).replace(' ', '0');
        }else{
            codedD = String.format("%" + nDes + "s", Integer.toBinaryString(desBuffer.size() - posMatch)).replace(' ', '0');
        }
        
        
        //System.out.println("L: " + codedL + " nEnt: " + nEnt);
        //System.out.println("D: " + codedD + " nDes: " + nDes);
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
                for(int i = 0; i < tempEnt.length() ; i++){
                    desBuffer.remove(0);
                    desBuffer.trimToSize();

                }
            }
            
            
        }  
        //System.out.println("Dades restants: " + inputData);    
        //System.out.println("Codificació: " + code);
        //System.out.println("---------");
        
        if(inputData.isEmpty()){
            for(int i = 0; i < args.mDes - desBuffer.size(); i++){
                entBuffer.remove(0);
            }
            for(String i: entBuffer){
                System.out.println("puta" + entBuffer.toString());
                code += i;
            }
            System.out.println("TEPUTAMERNAIE");
            System.out.println(entBuffer.toString());
            System.out.println(desBuffer.toString());
            System.out.println(inputData);
        }
               
        return code;
        
        
    }
    
    public static String decodeSeq(String toDecode) {
        //decode 
        System.out.println("- Decoding mode -");

        for(String i: toDecode.split(" ")){
            sequences.add(i);
        }

        //init decoded sequence, and fill desBuffer with that data
        String decode = sequences.remove(0);
        for(char i: decode.toCharArray()){
            desBuffer.add(String.valueOf(i));
        }

        //iterate inputData till its completely processed
        while(!sequences.isEmpty()){
            decode = decodeString(decode);
        }            
        System.out.println("Sequencia descodificada: " + decode);
        return decode;
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

