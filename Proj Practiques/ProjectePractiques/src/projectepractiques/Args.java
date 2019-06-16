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
    
    @Parameter(names = { "--codedInput", "-ci" }, description = "Fitxer d’entrada a la descodificació.")
    String codedInput = "saved_images.zip";

    @Parameter(names = { "--output", "-o" }, description = " Nom del fitxer en format propi amb la seqüència d’imatges\n" +
"de sortida i la informació necessària per la descodificació.")
    String output = "saved_images.zip";
    
    @Parameter(names = { "--decodedOutput"}, description = "Fitxer de sortida del video decodificat")
    String decodedOutput = "saved_decoded_images.zip";

    @Parameter(names = { "-e", "--encode" }, description = "Argument que indica que s’haurà d’aplicar la codificació sobre el conjunt d’imatges\n" +
"d’input i guardar el resultat al lloc indicat per output. Si és 1 estarà activat, si és 0 desactivat.")
    Integer encode = 0;

    @Parameter(names = { "-d", "--decode" }, description = "Argument que indica que s’haurà d’aplicar la descodificació sobre el conjunt\n" +
"d’imatges d’input provinents d’un fitxer en format propi.  Si és 1 estarà activat, si és 0 desactivat.")
    Integer decode = 0;

    @Parameter(names = {"--fps"}, description = "nombre d’imatges per segon amb les quals és reproduirà el vídeo.") 
    Integer fps = 25;
    
    @Parameter(names = "--binaritzation", description = "filtre de binarització utilitzant el valor llindar indicat. S'espera un valor positiu.")
    int bin = -1;

    @Parameter(names = "--negative", description = " aplicació d’un filtre negatiu sobre la imatge.  Si és 1 estarà activat, si és 0 desactivat.")
    int negative = 0;
    
    @Parameter(names = "--averaging", description = "aplicació d’un filtre de promig en zones de value x value.")
    int average = 0;
    
    @Parameter(names = "--decoAvg", description = "aplicació d’un filtre de promig en zones de value x value, exclusivament al resultat de la decodificació.")
    int decoAvg = 0;
    
    @Parameter(names = "--comparator", description = "determina la funció de comparació a utilitzar.")
    int comparator = 0;
    
    @Parameter(names = "--tileSize", description = "Mida de les tesel·les.")
    int tileSize = 8;
    
    @Parameter(names = "--seekRange", description = "desplaçament màxim en la cerca de tessel·les coincidents")
    int seekRange = 0;
    
    @Parameter(names = "--quality", description = "factor de qualitat que determinarà quan dos tessel·les és consideren\n" +
"coincidents.")
    int quality = 0;
    
    @Parameter(names = "--gop", description = "Indica el numero de Group of Picture a usar")
    int gop = 0;
    
    @Parameter(names = "--batch", description = "Mode batch.  Si és 1 no es reproduïrà el video, si és 0 es reproduïrà.")
    int batch = 0;
    
    @Parameter(names = "--threshold", description = "Threshold de la comparació d'imatges.")
    int thresh = 0;
    
    @Parameter(names = "-help", description = "Mostra la descripció de les comandes.")
    int help = 0;

}
