/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projectepractiques;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

/**
 *
 * @author Arnau Vancells
 */
public class VideoPlayer extends JFrame implements Runnable {
    private int fps;
    public ArrayList<String> imageNames;
    public Map<String, BufferedImage> imageDict = new HashMap<>();
    public JLabel label;
    
    public VideoPlayer(int fps, ArrayList<String> imageNames, Map<String, BufferedImage> imageDict){
          this.fps = fps;
          this.imageNames = imageNames;
          this.imageDict = imageDict;
          label = new JLabel();
    }
    
    @Override
    public void run() {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setPreferredSize(new Dimension(imageDict.get(imageNames.get(0)).getWidth() + 50, imageDict.get(imageNames.get(0)).getHeight() + 50));
        
        label.setHorizontalAlignment(JLabel.CENTER);
        
        this.getContentPane().add(label, BorderLayout.CENTER);
        this.pack();
        this.setVisible(true);
        
        play();
        
    }
    
    public void play(){
        System.out.println("Displaying images at: " + fps + " fps");
        for(int i= 0; i < imageNames.size(); i++){
            label.setIcon(new ImageIcon(imageDict.get(imageNames.get(i))));
            try {
                TimeUnit.MILLISECONDS.sleep(1000 / fps);
            } catch (InterruptedException ex) {
                Logger.getLogger(VideoPlayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
