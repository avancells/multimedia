
package projectepractiques;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Classe que representa un objecte amb la informacio essencial d'una tile + el GOP. No necesita getters/setters
 * @author Arnau Vancells i Blai Ras
 */
public class CodedData implements Serializable{
    ArrayList<Integer> bestTilesX = new ArrayList<>();
    ArrayList<Integer> bestTilesY = new ArrayList<>();
    ArrayList<Integer> bestOriginX = new ArrayList<>();
    ArrayList<Integer> bestOriginY = new ArrayList<>();
    int tileWidth;
    int tileHeight;
    int gop;
    
    public CodedData(ArrayList<Integer> bestTilesX, ArrayList<Integer> bestTilesY, ArrayList<Integer> bestOriginX, ArrayList<Integer> bestOriginY, int tileWidth, int tileHeight){
        this.bestTilesX = bestTilesX;
        this.bestTilesY = bestTilesY;
        this.bestOriginX = bestOriginX;
        this.bestOriginY = bestOriginY;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
    }
}
