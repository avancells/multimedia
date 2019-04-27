
package projectepractiques;

import com.beust.jcommander.JCommander;
import java.awt.BorderLayout;
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
import java.util.Enumeration;
import java.util.Scanner;
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
    public static ArrayList<String> nameImage;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] argv) throws FileNotFoundException, IOException {
        Args args = new Args();
        JCommander.newBuilder().addObject(args).build().parse(argv);
        Scanner scanner = new Scanner(System.in);
        JCommander jCommander = new JCommander(args, argv);
        if (args.help == 1) {
            jCommander.usage();
            return;
        }
        
        //readImage();
        readZip();
    }
    
    
    public static void readImage() throws FileNotFoundException, IOException {
        BufferedImage buffer;
        InputStream is = new BufferedInputStream(new FileInputStream("Cubo03.png"));
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
        try {
            getPixelColor(image);
        } catch (IOException ex) {
            Logger.getLogger(ProjectePractiques.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    
    public static void readZip() throws IOException {
        ZipFile zip = new ZipFile(new File("cubo.zip"));
        
        
        Enumeration<? extends ZipEntry> entries = zip.entries();
        

        while(entries.hasMoreElements()) {
            
            ZipEntry zipEntry = entries.nextElement();
            System.out.println(zipEntry.getName());
            
            
            
            
//            while((line = bufferedReader.readLine()) != null){
//                System.out.println(line);
//            }
           
        }
        zip.close();
    }
        
        

}
