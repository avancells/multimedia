
package projectepractiques;

import com.beust.jcommander.JCommander;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
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

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author Blai Ras i Arnau Vancells
 */
public class ProjectePractiques {

    //Array with all the image names
    public static ArrayList<String> imageNames = new ArrayList<>();
    //Array with all the images ready to be zipped
    public static ArrayList<BufferedImage> imagesToZip = new ArrayList<>();
    //Diccionari ImageName - Image (bufferedImage)
    public static Map<String, BufferedImage> imageDict = new HashMap<>();
    //Array with all the tiles of every image
    public static ArrayList<Tile> allTiles = new ArrayList<>();
    //Array with all the Data needed for the decode
    public static ArrayList<CodedData> dataList = new ArrayList<>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] argv) {
        Args args = new Args();
        JCommander.newBuilder().addObject(args).build().parse(argv);
        JCommander jCommander = new JCommander(args, argv);
        
        
        if (args.help == 1) {
            // Show jcommander help
            jCommander.usage();
            return;
        }
        
        // Input arg can't be null, it is mandatory
        if (args.input == "") {
            System.out.println("Input cannot be null");
            System.exit(0);
        }
        
        // Encoding mode
        if (args.encode == 1) {
            System.out.println("- Encoding mode -");
            //First of all, read all the data to code
            try {
                System.out.println("    Reading zip file...");
                readZip(args.input, true);
            } catch (IOException ex) {
                Logger.getLogger(ProjectePractiques.class.getName()).log(Level.SEVERE, null, ex);
            }

            // for each image read
            for (int i = 0; i < imageNames.size(); i++) {
                
                // apply averaging
                if (args.average > 0) {
                    imageDict.put(imageNames.get(i), average(imageDict.get(imageNames.get(i)),args.average));
                }
                
                // apply binaritzation
                if (args.bin > -1) {
                    imageDict.put(imageNames.get(i), binaritzation(imageDict.get(imageNames.get(i)),args.bin));
                }
                
                // apply negative filter
                if (args.negative == 1){
                    imageDict.put(imageNames.get(i), negative(imageDict.get(imageNames.get(i))));
                }
            }
            
            // Start videoplayer thread
            if(args.batch == 0){
                VideoPlayer vp = new VideoPlayer(args.fps, imageNames, new HashMap<>(imageDict));
                Thread t = new Thread(vp);
                System.out.println("    Starting videoplayer thread...");
                t.start();
            }
            
            
            
            // Save images into zip in JPEG format to the specified path
            try {
                System.out.println("    Saving images to: " + args.output);
                saveToZip(args.output);
            } catch (IOException ex) {
                Logger.getLogger(ProjectePractiques.class.getName()).log(Level.SEVERE, null, ex);
            }

            //Here we compute the analytics of our code

            //Size of the original zip fle
            File original = new File(args.input);
            long original_size = original.length();
            
            //Size of the jpeg file
            File jpeg = new File(args.output);
            long jpeg_size = jpeg.length();
           	//Compute our quality factor
            double qualityFactor_jpeg = qualityFactor(imageDict.get(imageNames.get(0)), imageDict.get(imageNames.get(1)));
            String qualityFactor_jpeg_text = String.format("%.2f", qualityFactor_jpeg);
            //Compute the coding time
            double finalTime=-1;
            try {
                long codeStart = System.nanoTime();
                codeAllImages(args);
                long codeEnd = System.nanoTime();
                finalTime = ((codeEnd - codeStart) / 1000000000.0);
                System.out.println("    Coding time(seconds): " + finalTime);
            } catch (IOException ex) {
                Logger.getLogger(ProjectePractiques.class.getName()).log(Level.SEVERE, null, ex);          
            }
            
            //Size of the result zip file, coded
            File coded = new File(args.output);
            long coded_size = coded.length();
            System.out.println("    - Estadístiques de la codificació -");
            System.out.println("      Factor de qualitat original: " + qualityFactor_jpeg_text );
            System.out.println("      Original file size: " + (original_size / 1024) + "(KB)");
            System.out.println("      JPEG only file size: " + (jpeg_size / 1024) + "(KB)");
            System.out.println("      Coded file size: " + (coded_size / 1024) + "(KB)");
            float factor = (float) coded_size / jpeg_size;
            System.out.println("      Factor de compressió(respecte JPEG only): " + factor);
            
            //Mode used to check the perfomance of our code
            //It creates a .txt file with time & quality results
            //Videoplayer doesn't pop!
            if(args.batch == 1){
                addBatchResultsEncode(args, finalTime, coded_size, factor);
            }
            
            
            
        }
        //Decoding mode
        if(args.decode == 1){
            System.out.println("- Decoding mode -");
            //Start computing the decoding time
            long codeStart = System.nanoTime();
            decodeAllImages(args);
            long codeEnd = System.nanoTime();
            double finalTime = ((codeEnd - codeStart) / 1000000000.0);
            System.out.println("    Decoding time(seconds): " + finalTime);
            //Compute our quality factor
            double qualityFactor_decoded = qualityFactor(imageDict.get(imageNames.get(0)), imageDict.get(imageNames.get(1)));
            String qualityFactor_decoded_text = String.format("%.2f", qualityFactor_decoded);
            System.out.println("    Factor de qualitat decodificada: " + qualityFactor_decoded_text);
            
            try {
                System.out.println("    Saving images to: " + args.decodedOutput);
                saveToZip(args.decodedOutput);
            } catch (IOException ex) {
                Logger.getLogger(ProjectePractiques.class.getName()).log(Level.SEVERE, null, ex);
            }
            //'Smooth' filter, aka averaging filter, for better looks
            if(args.decoAvg > 0){
                // for each image read
                for (int i = 0; i < imageNames.size(); i++) {
                    // apply averaging
                    imageDict.put(imageNames.get(i), average(imageDict.get(imageNames.get(i)),args.decoAvg));
                }
            }
            
            
            // Start videoplayer thread
            if(args.batch == 0){
                VideoPlayer vp = new VideoPlayer(args.fps, imageNames, new HashMap<>(imageDict));
                Thread t = new Thread(vp);
                System.out.println("    Starting videoplayer thread...");
                t.start();
            }else{
            	//Write to the results .txt file the output of the decode data
                addBatchResultsDecode(finalTime, qualityFactor_decoded_text);
            }    
        }
    }
    
    //Unused 
    //Read image from path, and load it into a BufferedImage object
    public static BufferedImage readImage(String imagePath) throws FileNotFoundException, IOException {
        BufferedImage buffer;
        InputStream is = new BufferedInputStream(new FileInputStream(imagePath));
        buffer = ImageIO.read(is);
        return buffer;
    }
    
    // Returns the color of a pixel from a BufferedImage, given x and y
   public static int[] getPixelColor(BufferedImage image,int x,int y) {
        int colors[] = new int[3];
        int clr = 0;

        try{
            clr = image.getRGB(x,y); 
        }catch(Exception e){
            System.out.println(e);
            System.out.println(x + " " + y);
        }

        int  red   = (clr & 0x00ff0000) >> 16;
        int  green = (clr & 0x0000ff00) >> 8;
        int  blue  =  clr & 0x000000ff;
        colors[0] = red;
        colors[1] = green;
        colors[2] = blue;
        return colors;
    }
    
    // Reads a zip file and loads the images into a HashMap, plus ordering them by name
    public static void readZip(String zipPath, boolean sort) throws IOException {
        // Load zip and its entries
        ZipFile zip = new ZipFile(new File(zipPath));
        Enumeration<? extends ZipEntry> entries = zip.entries();
        // Read every entry and load it to the HashMap
        while(entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            InputStream entryStream = zip.getInputStream(zipEntry);
            //Used only for the decoding mode
            //Compressed file that contains the Coded Data
            if("codedData.gz".equals(zipEntry.getName())){
                FileInputStream in = new FileInputStream("codedData.gz");
                GZIPInputStream gis = new GZIPInputStream(in);
                ObjectInputStream ois = new ObjectInputStream(gis);
                try {
                    dataList =  (ArrayList<CodedData>) (ois.readObject());
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ProjectePractiques.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                BufferedImage image = ImageIO.read(entryStream);
                imageNames.add(zipEntry.getName());            
                imageDict.put(zipEntry.getName(), image);
            }
        }
        zip.close();
        // Sort the image names list
        if(sort){
            Collections.sort(imageNames, (String f1, String f2) -> f1.compareTo(f2)); 
        }
    }

    //Unused (moved to VideoPlayer.java)
    // Creates a new JFrame containing an image, and cicles through the list of loaded images in order to play them
    // in a sequence, while controlling the FPS its played at
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

    //Applies bin filter with a treshold in a BufferedImage
    public static BufferedImage binaritzation(BufferedImage image, int thrs) {
        //3-position array where we store R,G,B values of every pixel
        int[] colors;
        int[] black = new int[3];
        int[] white = new int[3];
        for (int i = 0; i < 3; i++) {
            black[i] = 0;
            white[i] = 255;
        }
        //We create a tesela so we can modify the image pixels
        WritableRaster bitmap = (WritableRaster) image.getData();
        WritableRaster tesela = bitmap.createWritableChild(image.getMinX(), image.getMinY(), image.getWidth(), image.getHeight(), 0,0, null);
        //We compute the mean of every pixel so we can decide if its black or white
        double mean;
        //Image iteration pixel by pixel
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
        //Create the binarized image and return it
        BufferedImage subImage = new BufferedImage(image.getColorModel(),tesela,image.isAlphaPremultiplied(),null);
        return subImage;
    }
    
    //Applies negative filter in a BufferedImage
    public static BufferedImage negative(BufferedImage image) {
        //3-position array where we store R,G,B values of every pixel
        int[] colors;
        //3-position array where we will store the new negative R,B,G values 
        int[] negat = new int[3];
        //Create a tessela form the original image so we can modify its pixels
        WritableRaster bitmap = (WritableRaster) image.getData();
        WritableRaster tesela = bitmap.createWritableChild(image.getMinX(), image.getMinY(), image.getWidth(), image.getHeight(), 0,0, null);
        //Image iteration
        for (int x = 0; x < image.getWidth(); x++){
            for (int y = 0; y < image.getHeight(); y++) {
                colors = getPixelColor(image,x,y);
                for (int i = 0; i < 3; i++) {
                    //Negative filter means doing 1 - original pixel
                    if(colors[i] == 0){
                        negat[i] = -1;
                    }else{
                        negat[i] = 1-colors[i];
                    }
                }
                bitmap.setPixel(x,y,negat);
            }
        }
        //Create the negative image and return it
        BufferedImage subImage = new BufferedImage(image.getColorModel(),tesela,image.isAlphaPremultiplied(),null);
        return subImage;
    }
    
    //Funcion that applies averaging filter in a BufferedImage, taking a window with 'value' size
    public static BufferedImage average(BufferedImage image, int value) {
        //3-position array where we store R,G,B values of every pixel
        int[] colors;
        //3-position array where we will store the mean value of every pixel of every channel
        int[] meanColor = new int[3];
        //mean values for every channel
        double meanRed, meanGreen, meanBlue;
        //Create a tessela form the original image so we can modify its pixels
        WritableRaster bitmap = (WritableRaster) image.getData();
        WritableRaster tesela = bitmap.createWritableChild(image.getMinX(), image.getMinY(), image.getWidth(), image.getHeight(), 0,0, null);
        //Image iteration
        for (int x = 0; x < image.getWidth()-1; x++){
            for (int y = 0; y < image.getHeight()-1; y++) {
                int red = 0, green=0, blue = 0;
                //Window iteration
                for (int f =-value; f <= value; f++) {
                    for (int k = -value; k <= value; k++) {
                        //Get every window coordinate, but first check if they actually exist (not out of bounds)
                        if (y+(f)>=0 && x+(k)>=0 && y+(f) < image.getHeight() && x+(k)<image.getWidth()) {
                            //Temporal store of the value of every channel so then we can compute the mean
                            colors = getPixelColor(image,x+k,y+f);
                            red += colors[0];
                            green += colors[1];
                            blue += colors[2];
                        }
                    }
                }
                //To compute the mean of every color we have to compute the 'distance'+1
                int distance = (value - (-value)+1)*(value - (-value)+1);
                //For every channel, compute the new pixel value
                meanRed = red / distance;
                meanColor[0] = (int) meanRed;   
                meanGreen = green /  distance;
                meanColor[1] = (int) meanGreen;
                meanBlue = blue /  distance;
                meanColor[2] = (int) meanBlue;
                bitmap.setPixel(x,y,meanColor);
            }
        }
        //Create the averaged image and return it
        BufferedImage subImage = new BufferedImage(image.getColorModel(),tesela,image.isAlphaPremultiplied(),null);
        return subImage;
    }
    
    //Function that stores our image array into a ZIP File
    public static void saveToZip(String path) throws FileNotFoundException, IOException {
        //File and Zip Outoput Streams 
        FileOutputStream fos = new FileOutputStream(path);
        ZipOutputStream zipOS = new ZipOutputStream(fos);
        //For every image, we create a temporally jpeg file
        //Then, with the createFileToZip function, we include it to the output zip file
        //Finally, we delete the jpeg image
        for (int i =0; i< imageNames.size(); i++) {
            File tempImage = new File("image_"+Integer.toString(i)+".jpg");
            ImageIO.write(imageDict.get(imageNames.get(i)),"jpg",tempImage);
            createFileToZip(imageDict.get(imageNames.get(i)),"image_",i,zipOS);
            tempImage.delete();
        }
        zipOS.finish(); //Good practice!
        zipOS.close();
    }

    //Function that stores into a zip file the passed image
    public static void createFileToZip(BufferedImage image, String path, int name,ZipOutputStream zipOS) throws FileNotFoundException, IOException {
        //Create the jpeg image
        File f = new File(path+Integer.toString(name)+".jpg");
        //Create the inputstream
        FileInputStream fis = new FileInputStream(f);
        //Create a zipentry for the file we are gonna include to the zip
        ZipEntry zipEntry = new ZipEntry(path+Integer.toString(name)+".jpg");
        //Store the image into the zip as an array of bytes
        zipOS.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOS.write(bytes, 0, length);
        }
        zipOS.closeEntry();
        fis.close();
    }
    
    //Function that divides an image into a concrete number of tiles
    //Returns an ArrayList where every position is a Tile (an image) 
    public static ArrayList<Tile> doTiles(BufferedImage img, int tileSize) {
        
        allTiles = new ArrayList();
        int propX = tileSize;//img.getWidth() / nTiles;
        int propY = tileSize;//img.getHeight() / nTiles;
        int nTilesX = img.getWidth() / tileSize;
        int nTilesY = img.getHeight() / tileSize;
        
        WritableRaster wr = (WritableRaster) img.getData();
        //First 2 fors: maximum number of tiles watching the image width and height
        for (int i = 0; i < nTilesX; i++) {
            for (int j = 0; j < nTilesY; j++) {
            	//Making sure we dont go out of bounds
                if (i * propX + propX <= img.getWidth()) {
                    if (j * propY + propY <= img.getHeight()) {
                    	//Create the tile image
                        WritableRaster tile = (WritableRaster)wr.createChild((i * propX), (j * propY), propX, propY, 0, 0, null);
                        BufferedImage buffTile = new BufferedImage(img.getColorModel(), tile, 
                        img.getColorModel().isAlphaPremultiplied(), null);
                        //Create our Tile object with the image and sizes                  
                        Tile t = new Tile(buffTile, i*propX, j*propY);
                        //Add the tile to the array of tiles of the image
                        allTiles.add(t);
                    }
                }
            }
        }
        return allTiles;
    }
    
    //Functions used to mesure pixel correlation between 2 images
    public static double compareImg(double x, double y, double z, double x2,double y2, double z2) {
        return  Math.sqrt(Math.pow(x-x2, 2) + Math.pow(y-y2, 2)+ Math.pow(z-z2, 2));
    }
    
    public static double compareImg2(double x, double y, double z, double x2,double y2, double z2) {
        return  10 * (Math.sqrt(x-x2) + Math.sqrt(y-y2)+ Math.sqrt(z-z2));
    }
    
    public static double compareImg3(double x, double y, double z, double x2,double y2, double z2) {
        return  (10 * ((float) (Math.abs(x-x2) / 255.0) + ((float) Math.abs(y-y2) / 255.0) + ((float) Math.abs(z-z2) / 255.0)) / 3 * 100);
    }
    
    //Function that enables the user to decide wich compare function wants to use
    public static double compareSelector(int compFunc, double x, double y, double z, double x2,double y2, double z2){
        switch(compFunc){
            case 1:
                return compareImg(x, y, z, x2, y2, z2);
            case 2:
                return compareImg2(x, y, z, x2, y2, z2);
            case 3: 
                return compareImg3(x, y, z, x2, y2, z2);
            default:
                return compareImg3(x, y, z, x2, y2, z2);
        }
    }
    
    //Function that compares the quality factor between an original image and the decoded one
    public static double qualityFactor(BufferedImage img1, BufferedImage img2){
        int imagePixels1 = img1.getWidth() * img1.getHeight();
        int[] colorsTile1;
        int red = 0, green=0, blue = 0;
        //Get the mean of each channel of all pixels of the image 1
        for (int tileX = 0; tileX < img1.getWidth(); tileX++) {
            for (int tileY = 0; tileY < img1.getHeight(); tileY++) {
                colorsTile1 = getPixelColor(img1,tileX,tileY);
                red += colorsTile1[0];
                green += colorsTile1[1];
                blue += colorsTile1[2];
            }
        }
        double meanRed1 = (double) red / imagePixels1;
        double meanGreen1 = (double) green /  imagePixels1;
        double meanBlue1 = (double) blue /  imagePixels1;
        
        int imagePixels2 = img2.getWidth() * img2.getHeight();
        int[] colorsTile2;
        int red2 = 0, green2 = 0, blue2 = 0;
        //Get the mean of each channel of all pixels of the image 2
        for (int tileX = 0; tileX < img2.getWidth(); tileX++) {
            for (int tileY = 0; tileY < img2.getHeight(); tileY++) {
                colorsTile2 = getPixelColor(img2,tileX,tileY);
                red2 += colorsTile2[0];
                green2 += colorsTile2[1];
                blue2 += colorsTile2[2];
            }
        }
        double meanRed2 = (double) red2 / imagePixels2;
        double meanGreen2 = (double) green2 /  imagePixels2;
        double meanBlue2 = (double) blue2 /  imagePixels2;
        //Use the correlation function to get a quality coeficient
        return compareImg3(meanRed1, meanGreen1, meanBlue1, meanRed2, meanGreen2, meanBlue2);
    }
    
    //Function that codes an image using movement detection & compensation
    //Creates tiles, compares pixel correlation, replaces tiles with the frame I pixels, stores the coded data for the decodification and returns the coded image
    public static BufferedImage[] createCodedImg(BufferedImage frameI, BufferedImage frameP, int thrs, int tileSize, int seekRange, int comparator) {
        //Compute the tiles of the image
        ArrayList<Tile> tilesP = doTiles(frameP,tileSize);
        //Lists that contains the important data of every tile
        ArrayList<Integer> bestTilesX = new ArrayList<>();
        ArrayList<Integer> bestTilesY = new ArrayList<>();
        ArrayList<Integer> bestOriginX = new ArrayList<>();
        ArrayList<Integer> bestOriginY = new ArrayList<>();
        int tileWidth = tileSize;
        int tileHeight = tileSize;
        
        int[] colorsTileX;
        int[] colors;
        //3-position array where we will store the mean value of every pixel of every channel
        int[] meanColorTileX = new int[3];
        int[] meanColor = new int[3];
        //mean values for every channel
        double meanRed=0, meanGreen=0, meanBlue =0;
        double meanRedX, meanGreenX, meanBlueX;
        
        double nPixels;
        double compareValue;
        
        WritableRaster bitmap = (WritableRaster) frameP.getData();
        WritableRaster bitmap2 = (WritableRaster) frameP.getData();
        WritableRaster frameP_mod = bitmap.createWritableChild(frameP.getMinX(), frameP.getMinY(), frameP.getWidth(), frameP.getHeight(), 0,0, null);
        WritableRaster ref_P = bitmap2.createWritableChild(frameP.getMinX(), frameP.getMinY(), frameP.getWidth(), frameP.getHeight(), 0,0, null);

        //For every tile
        for (Tile tile: tilesP) {

            double bestValue = 9999;
            int bestX = -1, bestY = -1;
            Tile bestTile = null;
            nPixels = tile.getImg().getWidth() * tile.getImg().getHeight();
            int red = 0, green=0, blue = 0;
            //Get the mean of every channel of every pixel of every tile
            for (int tileX = 0; tileX < tile.getImg().getWidth(); tileX++) {
                for (int tileY = 0; tileY < tile.getImg().getHeight(); tileY++) {
                    colorsTileX = getPixelColor(tile.getImg(),tileX,tileY);
                    red += colorsTileX[0];
                    green += colorsTileX[1];
                    blue += colorsTileX[2];
                }
            }
            meanRedX = (double) red / nPixels;
            meanGreenX = (double) green /  nPixels;
            meanBlueX = (double) blue /  nPixels;
        	//2 fors to iterate through the tile size + seekRange
            for (int seekX = tile.getX() - seekRange; seekX < tile.getX()+ seekRange; seekX++) {
                for (int seekY = tile.getY() - seekRange; seekY < tile.getY()+ seekRange; seekY++) {
                	//those if's correct the possible out of bounds coordinates
                    if (seekY < 0){
                        seekY = 0;
                    }
                    if (seekY > frameI.getHeight()){
                        seekY = frameI.getHeight();
                    }
                    if (seekX < 0){
                        seekX = 0;
                    }
                    if (seekX > frameI.getWidth()){
                        seekX = frameI.getWidth();
                    }
                    red = 0;
                    green = 0;
                    blue = 0;
                    //2 fors that can be understand like a window that goes through the tile size + seekRange
                    //Getting the values of every channel of every pixel so we can later compute the mean of every each of them
                    for (int f = seekX; f < seekX + tileSize; f++) {
                        for (int k = seekY; k < seekY + tileSize; k++) {
                            if((f < frameP.getWidth()) && (k < frameP.getHeight()) && (f >= 0) && (k >= 0)){
                                colors = getPixelColor(frameI,f,k);
                                red += colors[0];
                                green += colors[1];
                                blue += colors[2];
                            }
                        }
                    }
                    meanRed = (double) red / nPixels;
                    meanGreen = (double) green /  nPixels;
                    meanBlue = (double) blue /  nPixels;

                    //Now that we have the 2 means, get the correlation factor
                    compareValue = compareSelector(comparator,meanRedX,meanGreenX,meanBlueX,meanRed,meanGreen,meanBlue);

                    //If this value is over a treshold, replace the tile with the n-1 frame pixels
                    if ( compareValue < thrs && seekY <= frameP.getHeight() - tileSize &&  seekX <= frameP.getWidth() - tileSize) {
                        //Maybe there's more than one 'window' that gets over the treshold
                        //Get the best one!
                        if(compareValue < bestValue){
                            bestValue = compareValue;
                            bestTile = tile;
                            bestX = seekX;
                            bestY = seekY;
                        }
                    }

                }
            }
         	//Now that we have the tile info,
            if(bestTile != null){
            	//Iterate through it
                for (int temp_tileX = tile.getX(); temp_tileX < tile.getImg().getWidth() + tile.getX(); temp_tileX++) {
                    for (int temp_tileY = tile.getY(); temp_tileY < tile.getImg().getHeight() + tile.getY(); temp_tileY++) {
                    	//(Using the same array, but we decided to put the black color so we put zero in every pixel channel)
                        meanColor[0] = (int) 0;//meanRedX;
                        meanColor[1] = (int) 0;//meanGreenX;
                        meanColor[2] = (int) 0;//meanBlueX;
                        
                        //Replace the pixels of the tile with black color for the 'coded' image
                        frameP_mod.setPixel(temp_tileX,temp_tileY,meanColor);
                        
                        //And to the new 'frame I' image that were gonna use in the next iteration, replace the tile with the colors of the frame I image (the previous one)
                        colors = getPixelColor(frameI, bestX + (temp_tileX - tile.getX()), bestY+(temp_tileY - tile.getY()));
                        ref_P.setPixel(temp_tileX, temp_tileY, colors);
                        
                    }
                }
                //Add the info of the tiles for the decode
                bestTilesX.add(bestTile.getX());
                bestTilesY.add(bestTile.getY());
                bestOriginX.add(bestX);
                bestOriginY.add(bestY);
            }
        }
        //Create the codedData object with all the info of every tile
        CodedData codedData = new CodedData(bestTilesX, bestTilesY, bestOriginX, bestOriginY, tileWidth, tileHeight);
        dataList.add(codedData); //add it to the list
        //Create the images and return it!
        BufferedImage frameP_new = new BufferedImage(frameP.getColorModel(),frameP_mod,frameP.isAlphaPremultiplied(),null);
        BufferedImage ref_buf = new BufferedImage(frameP.getColorModel(),ref_P,frameP.isAlphaPremultiplied(),null);
        BufferedImage[] result = new BufferedImage[2];
        result[0] = frameP_new;
        result[1] = ref_buf;
        return result;
    }
    
    //Function that controlls the coding process
    public static void codeAllImages(Args args) throws FileNotFoundException, IOException{
        BufferedImage tempI=null, tempP=null;
        BufferedImage[] codeOut = null;
        FileOutputStream fos = new FileOutputStream(args.output);
        ZipOutputStream zipOS = new ZipOutputStream(fos);
        int j = 0;
        //Iterate through all the images dividing them into frameP or frameI
        for(int i= 0; i < imageNames.size(); i++){ 
            //j is a counter of the gop
            if(j >= (args.gop)){
                j=0;
            }
            if(j==0){
                // Frame I
                tempI = imageDict.get(imageNames.get(i));
                imgToZip(tempI, i, zipOS, "image_coded_");
            }else{
                //Frame P
                codeOut = createCodedImg(tempI, imageDict.get(imageNames.get(i)), args.thresh, args.tileSize, args.seekRange, args.comparator);
                imgToZip(codeOut[0], i, zipOS, "image_coded_");
                tempI = codeOut[1];
                //showImage(tempP);
            }
            j++;
        }
        //Get the gop, its always the first position of the coded data
        dataList.get(0).gop = args.gop;
        //Store into a .gz file all the info of every tile of every image, info that is stored into our dataList
        try {
            FileOutputStream out = new FileOutputStream("codedData.gz");
            GZIPOutputStream gos = new GZIPOutputStream(out);
            ObjectOutputStream oos = new ObjectOutputStream(gos);
            oos.writeObject(dataList);
            oos.flush();
            gos.flush();
            out.flush();
            
            oos.close();
            gos.close();
            out.close();
        } catch (Exception e) {
            System.out.println("Problem serializing: " + e);
        }
       
        FileInputStream fis = new FileInputStream("codedData.gz");
        ZipEntry e = new ZipEntry("codedData.gz");
        zipOS.putNextEntry(e);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOS.write(bytes, 0, length);
        }
        zipOS.closeEntry();
        fis.close();
        zipOS.finish(); //Good practice!
        zipOS.close();
    }
    
    //Function that controlls the decoding process
    public static void decodeAllImages(Args args){
       	//Now those arrays will contain 'decoded' images
       	imageNames.clear();
        imagesToZip.clear();
        imageDict.clear();
        ArrayList<CodedData> recoveredData = null;

        //Read the coded images
        try {
            System.out.println("Reading zip file...");
            readZip(args.codedInput, false);
        } catch (IOException ex) {
            Logger.getLogger(ProjectePractiques.class.getName()).log(Level.SEVERE, null, ex);
        }
        BufferedImage tempFrame=null, tempFrameI=null;
        WritableRaster tempBitmap=null;
        WritableRaster tempDecoded=null;
        CodedData tempData=null;
        int gop = dataList.get(0).gop;
        //System.out.println(gop);
        BufferedImage tempBufferedImage = null;
        int z = 0;
        //recoveredData[0] contains the info of the image 0, and so on
        int recoveredDataCounter = 0;
        //For every image,
        for(int i= 0; i < imageNames.size(); i++){
        	//z is a counter of the gop so we can decide if its a frameI or P
            if(z >= (gop)){
                z=0;
            }
            if(z == 0){//Frame I
            	//Store it
                tempFrameI = imageDict.get(imageNames.get(i));
                imageDict.put(imageNames.get(i), tempFrameI);
            }else{
                //Frame P, decode it
                tempFrame = imageDict.get(imageNames.get(i)); 
                tempBitmap = (WritableRaster) tempFrame.getData();
                tempDecoded = tempBitmap.createWritableChild(tempFrame.getMinX(), tempFrame.getMinY(), tempFrame.getWidth(), tempFrame.getHeight(), 0,0, null);
            	//Get his info
                tempData = dataList.get(recoveredDataCounter);
                recoveredDataCounter++;
                int[] tempColor;
                //Iterate through the tile and replace its pixels
                for(int k = 0; k < tempData.bestTilesX.size(); k++){
                    for (int baseX = 0; baseX < tempData.tileWidth ; baseX++) {
                        for (int baseY = 0; baseY < tempData.tileHeight; baseY++) {
                            tempColor = getPixelColor(tempFrameI, tempData.bestOriginX.get(k)+baseX, tempData.bestOriginY.get(k)+baseY);
                            tempDecoded.setPixel(tempData.bestTilesX.get(k)+baseX, tempData.bestTilesY.get(k)+baseY, tempColor);
                        }
                    }
                }
                //Store the new decoded image
                tempBufferedImage = new BufferedImage(tempFrame.getColorModel(),tempDecoded,tempFrame.isAlphaPremultiplied(),null);
                imageDict.put(imageNames.get(i), tempBufferedImage);
                tempFrameI = tempBufferedImage;
            }
            z++;
        }
    }

    //Function that takes and image and writes it into a zip (with another function) in the jpg format
    public static void imgToZip(BufferedImage image, int number, ZipOutputStream zipOS, String path){
        try {
            File tempImage = new File(path+ number +".jpg");
            ImageIO.write(image,"jpg",tempImage);
            createFileToZip(image, path, number, zipOS);
            tempImage.delete();
        } catch (IOException ex) {
            Logger.getLogger(ProjectePractiques.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //Unused, used only for debuggind and various checkings
    //Creates a JFrame with a label, containing the given image
    public static void showImage(BufferedImage image) {
        JFrame frame = new JFrame();
        JLabel label = new JLabel(new ImageIcon(image));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }
    
   	//Function that stores into a .txt file the infodata resulting of the codification
    public static void addBatchResultsEncode(Args args, double finalTime, long coded_size, float factor){
        BufferedWriter output;
        //The header is:
        // tilesize threshold seekrange gop | codeTime codedSize compressionFactor | decodeTime qualityFactor
        try {
            output = new BufferedWriter(new FileWriter("encodeBatch.txt", true));
            String result = args.tileSize + " " + args.thresh + " " + args.seekRange + " " + args.gop + " | " + finalTime + " " + coded_size + " " + factor;
            output.append(result);
            output.close();
        } catch (IOException ex) {
            Logger.getLogger(ProjectePractiques.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //Function that stores into a .txt file the infodata resulting of the decodification
    public static void addBatchResultsDecode(double finalTime, String factorQualitat){
        BufferedWriter output;
        try {
            output = new BufferedWriter(new FileWriter("encodeBatch.txt", true));
            String result = " | " + finalTime + " " + factorQualitat + "\n";
            output.append(result);
            output.newLine();
            output.close();
        } catch (IOException ex) {
            Logger.getLogger(ProjectePractiques.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}