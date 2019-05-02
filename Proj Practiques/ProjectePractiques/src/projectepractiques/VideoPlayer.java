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
 * @author Arnau Vancells i Blai Ras 
 */

//Class that reproduces a video (JLabel) in a separated thread (parallel to the main code) 
public class VideoPlayer extends JFrame implements Runnable {
    private int fps;
    //The image array to reproduce
    public ArrayList<String> imageNames;
    //The image dictionary (String name, BufferedImage image) to locate the images
    public Map<String, BufferedImage> imageDict = new HashMap<>();
    public JLabel label;
    
    //Constructor.
    public VideoPlayer(int fps, ArrayList<String> imageNames, Map<String, BufferedImage> imageDict){
          this.fps = fps;
          this.imageNames = imageNames;
          this.imageDict = imageDict;
          //Create tge window
          label = new JLabel();
    }

    //Function that runs in parallel. In this case we reproduce the video
    @Override
    public void run() {
        //JLabel properties
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setPreferredSize(new Dimension(imageDict.get(imageNames.get(0)).getWidth() + 50, imageDict.get(imageNames.get(0)).getHeight() + 50));
        label.setHorizontalAlignment(JLabel.CENTER);
        this.getContentPane().add(label, BorderLayout.CENTER);
        this.pack();
        this.setVisible(true);
        play();
        this.dispose(); //Close
    }
    //Function that reproduces the video (images)
    public void play(){
        System.out.println("Displaying images at: " + fps + " fps");
        //For every image, show it with a certain interval (aka fps)
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
