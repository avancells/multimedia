/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lzdecoder;
import com.beust.jcommander.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Arnau Vancells & Blai Ras
 */


public class Args {
  @Parameter
  private List<String> parameters = new ArrayList<>();

  @Parameter(names = { "-input", "-i" }, description = "Input .txt file path, if null console input will be prompted. 'random' can be used to use a random sequence as input.")
   String input = "console";
  
  @Parameter(names = { "-output", "-o" }, description = "Output .txt file path, if null console output will be used.")
   String output = "console";
  
  @Parameter(names = { "-mEnt", "-ent" }, description = "Change input window size. Default is 4.")
   Integer mEnt = 4;
  
  @Parameter(names = { "-mDes", "-des" }, description = "Change sliding window size. Default is 8")
   Integer mDes = 8;

  @Parameter(names = { "-randSize", "-randLen"}, description = "Change the random sequence size. Default is 25")
   Integer randLen = 25;
  
  
  @Parameter(names = "-groups", description = "Comma-separated list of group names to be run")
  private String groups;

  @Parameter(names = "-mode", description = "Use 0 to enable coding, 1 to enable decoding, 2 to enable checking mode(code-decode-compare). Default is coding mode.")
   int mode = 0;
}