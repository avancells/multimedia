/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projectepractiques;

import com.beust.jcommander.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author BlaiSB
 */
public class Args {
    @Parameter
    private List<String> parameters = new ArrayList<>();

    @Parameter(names = { "--input", "-i" }, description = "Fitxer d’entrada. Argument obligatori.")
    String input = "";

    @Parameter(names = { "--output", "-o" }, description = " Nom del fitxer en format propi amb la seqüència d’imatges\n" +
"de sortida i la informació necessària per la descodificació.")
    String output = "saved_images.zip";

    @Parameter(names = { "-e", "--encode" }, description = "Argument que indica que s’haurà d’aplicar la codificació sobre el conjunt d’imatges\n" +
"d’input i guardar el resultat al lloc indicat per output. ")
    Integer encode = 0;

    @Parameter(names = { "-d", "--decode" }, description = "Argument que indica que s’haurà d’aplicar la descodificació sobre el conjunt\n" +
"d’imatges d’input provinents d’un fitxer en format propi")
    Integer decode = 0;

    @Parameter(names = {"--fps"}, description = "nombre d’imatges per segon amb les quals és reproduirà el vídeo.") 
    Integer fps = 25;


    @Parameter(names = "--binaritzation", description = "filtre de binarització utilitzant el valor llindar indicat")
    int bin = -1;

    @Parameter(names = "--negative", description = " aplicació d’un filtre negatiu sobre la imatge. ")
    int negative = 0;
    
    @Parameter(names = "--averaging", description = "aplicació d’un filtre de promig en zones de value x value.")
    int average = 0;
    
    @Parameter(names = "--nTiles", description = "nombre de tessel·les en la qual dividir la imatge")
    int nTiles = 0;
    
    @Parameter(names = "--seekRange", description = "desplaçament màxim en la cerca de tessel·les coincidents")
    int seekRange = 0;
    
    @Parameter(names = "--quality", description = "factor de qualitat que determinarà quan dos tessel·les és consideren\n" +
"coincidents.")
    int quality = 0;
    
    @Parameter(names = "--batch", description = "No Window")
    int batch = 0;
    
    @Parameter(names = "-help", description = "Prompts the help.")
    int help = 0;

    
    
}
