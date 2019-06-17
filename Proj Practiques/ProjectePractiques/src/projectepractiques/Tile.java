/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projectepractiques;

import java.awt.image.BufferedImage;

/**
 * Class that represents 'a tile' with his image, his coordinates and setters/getters
 * @author Blai Ras i Arnau Vancells
 */
public class Tile {
    private int x,y;
    private BufferedImage img;

    public Tile(BufferedImage img, int x, int y) {
        this.x = x; this.y = y; this.img = img;
    }
    
    public void setImg(BufferedImage i) { this.img = i;}
    public void setX(int x) {this.x = x;}
    public void setY(int y) {this.y = y;}
    public int getX() {return x;}
    public int getY() {return y;}
    public BufferedImage getImg() {return img;}
}
    

