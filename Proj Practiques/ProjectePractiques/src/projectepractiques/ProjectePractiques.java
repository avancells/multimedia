
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
import java.io.FileOutputStream;
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
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author Blai Ras i Arnau Vancells
 */
public class ProjectePractiques {
    
    public static ArrayList<String> imageNames = new ArrayList<>();
    public static ArrayList<BufferedImage> imagesToZip = new ArrayList<>();
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
        
        if (args.input == "") {
            System.out.println("Input cannot be null");
            System.exit(0);
        }
        
        if (args.encode == 1) {
            try {
                System.out.println("Reading zip file...");
                readZip(args.input);
            } catch (IOException ex) {
                Logger.getLogger(ProjectePractiques.class.getName()).log(Level.SEVERE, null, ex);
            }

            
            for (int i = 0; i < imageNames.size(); i++) {
                if (args.average > 0) {
                    imageDict.put(imageNames.get(i), average(imageDict.get(imageNames.get(i)),args.average));
                }
                if (args.bin > -1) {
                    imageDict.put(imageNames.get(i), binaritzation(imageDict.get(imageNames.get(i)),args.bin));
                }
                if (args.negative == 1){
                    imageDict.put(imageNames.get(i), negative(imageDict.get(imageNames.get(i))));
                }
            }

            try {
                playZip(args.fps);
            } catch (InterruptedException ex) {
                Logger.getLogger(ProjectePractiques.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            try {
                System.out.println("Saving images to zip...");
                saveToZip();
            } catch (IOException ex) {
                Logger.getLogger(ProjectePractiques.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
            System.exit(0);
            
        }
//        
        
        //showImage(average(imageDict.get(imageNames.get(0)),5));
        


//        imagesToZip.add(negative(imageDict.get(imageNames.get(0))));
//        imagesToZip.add(negative(imageDict.get(imageNames.get(5))));
//        
//        try {
//            saveToZip(imagesToZip);
//        } catch (IOException ex) {
//            Logger.getLogger(ProjectePractiques.class.getName()).log(Level.SEVERE, null, ex);
//        }
        
    }
    
    
    public static BufferedImage readImage(String imagePath) throws FileNotFoundException, IOException {
        BufferedImage buffer;
        InputStream is = new BufferedInputStream(new FileInputStream(imagePath));
        buffer = ImageIO.read(is);
        return buffer;
    }
    
    public static void showImage(BufferedImage image) {
        JFrame frame = new JFrame();
        JLabel label = new JLabel(new ImageIcon(image));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }
    
    public static int[] getPixelColor(BufferedImage image,int x,int y) {
       
        int colors[] = new int[3];
        int clr=  image.getRGB(x,y); 
        int  red   = (clr & 0x00ff0000) >> 16;
        int  green = (clr & 0x0000ff00) >> 8;
        int  blue  =  clr & 0x000000ff;
//        System.out.println("Red Color value = "+ red);
//        System.out.println("Green Color value = "+ green);
//        System.out.println("Blue Color value = "+ blue);
        colors[0] = red;
        colors[1] = green;
        colors[2] = blue;
        return colors;
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
    
    public static BufferedImage binaritzation(BufferedImage image, int thrs) {
        int[] colors;
        int[] black = new int[3];
        int[] white = new int[3];
        for (int i = 0; i < 3; i++) {
            black[i] = 0;
            white[i] = 255;
        }
        WritableRaster bitmap = (WritableRaster) image.getData();
        WritableRaster tesela = bitmap.createWritableChild(image.getMinX(), image.getMinY(), image.getWidth(), image.getHeight(), 0,0, null);
        double mean;
        for (int x = 0; x < image.getWidth(); x++){
            for (int y = 0; y < image.getHeight(); y++) {
                colors = getPixelColor(image,x,y);
                mean = colors[0] + colors[1] + colors[2];
                mean = mean/3;
                
                if (mean <= thrs) {
                    bitmap.setPixel(x,y,black);
                } else {
                    bitmap.setPixel(x,y,white);
                }
            }
        }
        BufferedImage subImage = new BufferedImage(image.getColorModel(),tesela,image.isAlphaPremultiplied(),null);
        return subImage;
    }
    
    public static BufferedImage negative(BufferedImage image) {
        int[] colors;
        int[] negat = new int[3];
        WritableRaster bitmap = (WritableRaster) image.getData();
        WritableRaster tesela = bitmap.createWritableChild(image.getMinX(), image.getMinY(), image.getWidth(), image.getHeight(), 0,0, null);
        for (int x = 0; x < image.getWidth(); x++){
            for (int y = 0; y < image.getHeight(); y++) {
                colors = getPixelColor(image,x,y);
                for (int i = 0; i < 3; i++) {
                    negat[i] = 1-colors[i];
                }
                bitmap.setPixel(x,y,negat);
            }
        }
        BufferedImage subImage = new BufferedImage(image.getColorModel(),tesela,image.isAlphaPremultiplied(),null);
        return subImage;
    }
    
    public static BufferedImage average(BufferedImage image, int value) {
        int[] colors;
        int[] meanColor = new int[3];
        double meanRed, meanGreen, meanBlue;
        
        WritableRaster bitmap = (WritableRaster) image.getData();
        WritableRaster tesela = bitmap.createWritableChild(image.getMinX(), image.getMinY(), image.getWidth(), image.getHeight(), 0,0, null);
        
        for (int x = 0; x < image.getWidth()-1; x++){
            for (int y = 0; y < image.getHeight()-1; y++) {
                int red = 0, green=0, blue = 0;
                for (int f =-value; f <= value; f++) {
                    for (int k = -value; k <= value; k++) {
                        if (y+(f)>=0 && x+(k)>=0 && y+(f) < image.getHeight() && x+(k)<image.getWidth()) {
                            colors = getPixelColor(image,x+k,y+f);
                            red += colors[0];
                            green += colors[1];
                            blue += colors[2];
                        }
                    }
                }
                int distance = (value - (-value)+1)*(value - (-value)+1);
                meanRed = red / distance;
                meanColor[0] = (int) meanRed;
                meanGreen = green /  distance;
                meanColor[1] = (int) meanGreen;
                meanBlue = blue /  distance;
                meanColor[2] = (int) meanBlue;
                bitmap.setPixel(x,y,meanColor);
            }
        }
        BufferedImage subImage = new BufferedImage(image.getColorModel(),tesela,image.isAlphaPremultiplied(),null);
        return subImage;
    }
    
    public static void saveToZip() throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream("savedImages.zip");
        ZipOutputStream zipOS = new ZipOutputStream(fos);
        for (int i =0; i< imageNames.size(); i++) {
            File tempImage = new File("image_"+Integer.toString(i)+".jpg");
            ImageIO.write(imageDict.get(imageNames.get(i)),"jpg",tempImage);
            createFileToZip(imageDict.get(imageNames.get(i)),i,zipOS);
            tempImage.delete();
        }
        zipOS.finish(); // good practice
        zipOS.close();
    }
    
    public static void createFileToZip(BufferedImage image,int name,ZipOutputStream zipOS) throws FileNotFoundException, IOException {
        File f = new File("image_"+Integer.toString(name)+".jpg");
        FileInputStream fis = new FileInputStream(f);
        ZipEntry zipEntry = new ZipEntry("image_"+Integer.toString(name)+".jpg");
        zipOS.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOS.write(bytes, 0, length);
        }
        zipOS.closeEntry();
        fis.close();
        
    }


    
    
        
        

}
