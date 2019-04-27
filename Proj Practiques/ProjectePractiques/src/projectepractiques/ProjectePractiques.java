
package projectepractiques;

import com.beust.jcommander.JCommander;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author Blai Ras i Arnau Vancells
 */
public class ProjectePractiques {
    public static ArrayList<BufferedImage> images = new ArrayList<>();
    public static ArrayList<String> imageNames = new ArrayList<>();
    public static Map<String, BufferedImage> imageDict = new HashMap<>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] argv) {
        Args args = new Args();
        JCommander.newBuilder().addObject(args).build().parse(argv);
        Scanner scanner = new Scanner(System.in);
        JCommander jCommander = new JCommander(args, argv);
        if (args.help == 1) {
            jCommander.usage();
            return;
        }
        
        try {
            //readImage();
            readZip("zips/Cubo.zip");
        } catch (IOException ex) {
            Logger.getLogger(ProjectePractiques.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            playZip(args.fps);
        } catch (InterruptedException ex) {
            Logger.getLogger(ProjectePractiques.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
    }
    
    
    public static void readImage(String imagePath) throws FileNotFoundException, IOException {
        BufferedImage buffer;
        InputStream is = new BufferedInputStream(new FileInputStream(imagePath));
        buffer = ImageIO.read(is);
        showImage(buffer);
        subImage(buffer);
    }
    
    public static void showImage(BufferedImage image) {
        JFrame frame = new JFrame();
        JLabel label = new JLabel(new ImageIcon(image));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }
    
    public static void getPixelColor(BufferedImage image) throws FileNotFoundException, IOException {
        int clr=  image.getRGB(0,0); 
        int  red   = (clr & 0x00ff0000) >> 16;
        int  green = (clr & 0x0000ff00) >> 8;
        int  blue  =  clr & 0x000000ff;
        System.out.println("Red Color value = "+ red);
        System.out.println("Green Color value = "+ green);
        System.out.println("Blue Color value = "+ blue);
    }
    
    public static void subImage(BufferedImage image) {
        WritableRaster bitmap = (WritableRaster) image.getData();
        WritableRaster tesela = bitmap.createWritableChild(image.getMinX(), image.getMinY(), 100, 50, 0,0, null);
        BufferedImage subImage = new BufferedImage(image.getColorModel(),tesela,image.isAlphaPremultiplied(),null);
        showImage(subImage);
    }
    
    public static void readZip(String zipPath) throws IOException {

        ZipFile zip = new ZipFile(new File(zipPath));
        
        Enumeration<? extends ZipEntry> entries = zip.entries();
        
        
        while(entries.hasMoreElements()) {
            
            ZipEntry zipEntry = entries.nextElement();
            InputStream entryStream = zip.getInputStream(zipEntry);
            BufferedImage image = ImageIO.read(entryStream);
            imageNames.add(zipEntry.getName());            
            //images.add(image); 
            
            imageDict.put(zipEntry.getName(), image);
            
        }
        zip.close();
        Collections.sort(imageNames);
        
    }
    
    public static void playZip(int fps) throws InterruptedException {
        JFrame frame = new JFrame();
        frame.setPreferredSize(new Dimension(imageDict.get(imageNames.get(0)).getWidth() + 50, imageDict.get(imageNames.get(0)).getHeight() + 50));
        JLabel label = new JLabel();
        label.setHorizontalAlignment(JLabel.CENTER);
        
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        
        System.out.println("Displaying images at: " + fps + " fps");
        for(int i= 0; i < imageNames.size(); i++){
            
            label.setIcon(new ImageIcon(imageDict.get(imageNames.get(i))));
            TimeUnit.MILLISECONDS.sleep(1000 / fps);
        }

    }
        
        

}