/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projectepractiques;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Arnau Vancells
 */
public class CodedData implements Serializable{
    ArrayList<Integer> bestTilesX = new ArrayList<>();
    ArrayList<Integer> bestTilesY = new ArrayList<>();
    ArrayList<Integer> bestOriginX = new ArrayList<>();
    ArrayList<Integer> bestOriginY = new ArrayList<>();
    int tileWidth;
    int tileHeight;
    
    public CodedData(ArrayList<Integer> bestTilesX, ArrayList<Integer> bestTilesY, ArrayList<Integer> bestOriginX, ArrayList<Integer> bestOriginY, int tileWidth, int tileHeight){
        this.bestTilesX = bestTilesX;
        this.bestTilesY = bestTilesY;
        this.bestOriginX = bestOriginX;
        this.bestOriginY = bestOriginY;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
    }
}
