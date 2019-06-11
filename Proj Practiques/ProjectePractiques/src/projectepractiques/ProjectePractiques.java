
package projectepractiques;

import com.beust.jcommander.JCommander;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    public static ArrayList<Tile> allTiles = new ArrayList<>();
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
            System.out.println("Encoding mode (work in progress)");
            try {
                System.out.println("Reading zip file...");
                readZip(args.input);
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
                System.out.println("Starting videoplayer thread...");
                t.start();
            }
            
            
            
            // Save images into zip in JPEG format to the specified path
            try {
                System.out.println("Saving images to: " + args.output);
                saveToZip(args.output);
            } catch (IOException ex) {
                Logger.getLogger(ProjectePractiques.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            try {
                codeAllImages(args);
            } catch (IOException ex) {
                Logger.getLogger(ProjectePractiques.class.getName()).log(Level.SEVERE, null, ex);          
            }
            
        }
        if(args.decode == 1){
            decodeAllImages(args);
            
            // Start videoplayer thread
            if(args.batch == 0){
                VideoPlayer vp = new VideoPlayer(args.fps, imageNames, new HashMap<>(imageDict));
                Thread t = new Thread(vp);
                System.out.println("Starting videoplayer thread...");
                t.start();
            }
            
            
        }
        
    }
    
    // Read image from path, and load it into a BufferedImage object
    public static BufferedImage readImage(String imagePath) throws FileNotFoundException, IOException {
        BufferedImage buffer;
        InputStream is = new BufferedInputStream(new FileInputStream(imagePath));
        buffer = ImageIO.read(is);
        return buffer;
    }
    
    //Unused
    // Creates a JFrame with a label, containing the given image
    public static void showImage(BufferedImage image) {
        JFrame frame = new JFrame();
        JLabel label = new JLabel(new ImageIcon(image));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }
    
    // Returns the color of a pixel from a BufferedImage, given x and y
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
    
    //Unused
    // Testing function used to trim the image using a WritableRaster
    public static void subImage(BufferedImage image) {
        WritableRaster bitmap = (WritableRaster) image.getData();
        WritableRaster tesela = bitmap.createWritableChild(image.getMinX(), image.getMinY(), 100, 50, 0,0, null);
        BufferedImage subImage = new BufferedImage(image.getColorModel(),tesela,image.isAlphaPremultiplied(),null);
        showImage(subImage);
    }
    
    // Reads a zip file and loads the images into a HashMap, plus ordering them by name
    public static void readZip(String zipPath) throws IOException {

        // Load zip and its entries
        ZipFile zip = new ZipFile(new File(zipPath));
        
        Enumeration<? extends ZipEntry> entries = zip.entries();
        
        // Read every entry and load it to the HashMap
        while(entries.hasMoreElements()) {
            
            ZipEntry zipEntry = entries.nextElement();
            InputStream entryStream = zip.getInputStream(zipEntry);
            BufferedImage image = ImageIO.read(entryStream);
            imageNames.add(zipEntry.getName());            
            imageDict.put(zipEntry.getName(), image);

        }
        zip.close();
        // Sort the image names list
        //Collections.sort(imageNames, (String f1, String f2) -> f1.compareTo(f2)); 
        
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
    
    public static void startEncode(int nTiles, int gop, int thrs) {
        BufferedImage frameI;
        
        for (String imgName: imageNames) {
            for (int i = 0; i < gop; i++) {
                if (i == 0) {
                    frameI = imageDict.get(imgName);
                } else {
                    
                }
                    
            }
        }
        

        
    }
    
    public static ArrayList<Tile> doTiles(BufferedImage img, int tileSize) {
        allTiles = new ArrayList();
        
        int propX = tileSize;//img.getWidth() / nTiles;
        int propY = tileSize;//img.getHeight() / nTiles;
        
        int nTilesX = img.getWidth() / tileSize;
        int nTilesY = img.getHeight() / tileSize;
                
        
        WritableRaster wr = (WritableRaster) img.getData();
        

        for (int i = 0; i < nTilesX; i++) {
            for (int j = 0; j < nTilesY; j++) {
                if (i * propX + propX < img.getWidth()) {
                    if (j * propY + propY < img.getHeight()) {
                        WritableRaster tile = (WritableRaster)wr.createChild((i * propX), (j * propY), propX, propY, 0, 0, null);
                        BufferedImage buffTile = new BufferedImage(img.getColorModel(), tile, 
                        img.getColorModel().isAlphaPremultiplied(), null);
                        Tile t = new Tile(buffTile, i*propX, j*propY);
                        allTiles.add(t);
                    }
                }
            }
        }
       
        return allTiles;
    }
    
    public static double compareImg(double x, double y, double z, double x2,double y2, double z2) {
        return  Math.sqrt(Math.pow(x-x2, 2) + Math.pow(y-y2, 2)+ Math.pow(z-z2, 2));
    }
    
    public static double compareImg2(double x, double y, double z, double x2,double y2, double z2) {
        return  10 * (Math.sqrt(x-x2) + Math.sqrt(y-y2)+ Math.sqrt(z-z2));
    }
    
    public static BufferedImage createCodedImg(BufferedImage frameI, BufferedImage frameP, int thrs, int tileSize, int seekRange) {
        ArrayList<Tile> tilesP = doTiles(frameP,tileSize);
        ArrayList<Integer> bestTilesX = new ArrayList<>();
        ArrayList<Integer> bestTilesY = new ArrayList<>();
        ArrayList<Integer> bestOriginX = new ArrayList<>();
        ArrayList<Integer> bestOriginY = new ArrayList<>();
        int tileWidth = tilesP.get(0).getImg().getWidth();
        int tileHeight = tilesP.get(0).getImg().getHeight();
        
        int[] colorsTileX;
        int[] colors;
        //3-position array where we will store the mean value of every pixel of every channel
        int[] meanColorTileX = new int[3];
        int[] meanColor = new int[3];
        //mean values for every channel
        double meanRed=0, meanGreen=0, meanBlue =0;
        double meanRedX, meanGreenX, meanBlueX;
        
        int nPixels;
        
        double compareValue;
        
        WritableRaster bitmap = (WritableRaster) frameP.getData();
        WritableRaster frameP_mod = bitmap.createWritableChild(frameP.getMinX(), frameP.getMinY(), frameP.getWidth(), frameP.getHeight(), 0,0, null);

        for (Tile tile: tilesP) {
            
            double bestValue = 9999;
            int bestX = -1, bestY = -1;
            Tile bestTile = null;
            
            nPixels = tile.getImg().getWidth() * tile.getImg().getHeight();
            int red = 0, green=0, blue = 0;
            for (int tileX = 0; tileX < tile.getImg().getWidth(); tileX++) {
                for (int tileY = 0; tileY < tile.getImg().getHeight(); tileY++) {
                    colorsTileX = getPixelColor(tile.getImg(),tileX,tileY);
                    red += colorsTileX[0];
                    green += colorsTileX[1];
                    blue += colorsTileX[2];
                }
            }
            meanRedX = red / nPixels;

            meanGreenX = green /  nPixels;

            meanBlueX = blue /  nPixels;
            
            for (int seekX = tile.getX() - seekRange; seekX < tile.getX()+ seekRange; seekX++) {
                for (int seekY = tile.getY() - seekRange; seekY < tile.getY()+ seekRange; seekY++) {
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
                    //System.out.println(seekX + " " + " Y: " + seekY + " " );
                    for (int f = seekX; f < seekX + tile.getImg().getWidth(); f++) {
                        for (int k = seekY; k < seekY + tile.getImg().getHeight(); k++) {
                            
                            //System.out.println((seekX + k) + " " + (seekY + f));
                            if (seekY + f >= 0 && seekX + k >= 0 && seekY + f < frameI.getHeight() && seekX + k < frameI.getWidth()) {
                                colors = getPixelColor(frameI,seekX+k,seekY+f);
                                red += colors[0];
                                green += colors[1];
                                blue += colors[2];
                            }
                        }
                    }
                    
                    meanRed = red / nPixels;
                    meanGreen = green /  nPixels;
                    meanBlue = blue /  nPixels;
                    
                    red = 0;
                    green = 0;
                    blue = 0;
                    
                    compareValue = compareImg(meanRedX,meanGreenX,meanBlueX,meanRed,meanGreen,meanBlue);
                    //System.out.println(compareValue);
                    if ( compareValue < thrs && compareValue != 0) {
                        //see you tmrrw;
                        // guardar top tile
                        if(compareValue < bestValue){
                            //System.out.println(compareValue);
                            bestValue = compareValue;
                            bestTile = tile;
                            bestX = seekX;
                            bestY = seekY;
                        }
                    }

                }
            }
            if(bestTile != null){
                for (int temp_tileX = bestTile.getX(); temp_tileX < bestTile.getImg().getWidth() + bestTile.getX(); temp_tileX++) {
                    for (int temp_tileY = bestTile.getY(); temp_tileY < bestTile.getImg().getHeight() + bestTile.getY(); temp_tileY++) {
                        meanColor[0] = (int) meanRedX;
                        meanColor[1] = (int) meanGreenX;
                        meanColor[2] = (int) meanBlueX;

                        frameP_mod.setPixel(temp_tileX,temp_tileY,meanColor);
                    }
                }
                bestTilesX.add(bestTile.getX());
                bestTilesY.add(bestTile.getY());
                bestOriginX.add(bestX);
                bestOriginY.add(bestY);
                
            }
        }
        
        CodedData codedData = new CodedData(bestTilesX, bestTilesY, bestOriginX, bestOriginY, tileWidth, tileHeight);
        dataList.add(codedData);
        
        BufferedImage frameP_new = new BufferedImage(frameP.getColorModel(),frameP_mod,frameP.isAlphaPremultiplied(),null);
        return frameP_new;
        
        
    }
    
//    public static void decodeImg(){
//        //save image
//        try{
//            
//            
//            File tempImage = new File("image_"+ 0 +".jpg");
//            ImageIO.write(frameI,"jpg",tempImage);
//            createFileToZip(frameI, 0, zipOS);
//            
//            
//            tempImage = new File("image_"+ Integer.toString(number) +".jpg");
//            ImageIO.write(frameP_new,"jpg",tempImage);
//
//            createFileToZip(frameP_new, number, zipOS);
//
//
//        }catch(Exception e){
//            System.out.println("Teputamare");
//        }
//
//        //save info 
//        CodedData codedData = new CodedData(bestTilesX, bestTilesY, bestOriginX, bestOriginY, tileWidth, tileHeight);
//        try {
//            FileOutputStream out = new FileOutputStream("codedData.out");
//            ObjectOutputStream oos = new ObjectOutputStream(out);
//            oos.writeObject(codedData);
//            oos.flush();
//        } catch (Exception e) {
//            System.out.println("Problem serializing: " + e);
//        }
//        
//        
//        ////// decode
//        // recuperar imatge
//        //llegir coded data
//        CodedData recoveredData = null;
//
//        try {
//          FileInputStream in = new FileInputStream("codedData.out");
//          ObjectInputStream ois = new ObjectInputStream(in);
//          recoveredData = (CodedData) (ois.readObject());
//        } catch (Exception e) {
//          System.out.println("Problem serializing: " + e);
//        }
//
//        
//        WritableRaster bitmap2 = (WritableRaster) frameP.getData();
//        WritableRaster decodedP = bitmap2.createWritableChild(frameP.getMinX(), frameP.getMinY(), frameP.getWidth(), frameP.getHeight(), 0,0, null);
//        
//        int[] tempColor;
//        for(int i = 0; i < recoveredData.bestTilesX.size(); i++){
//            for (int baseX = 0; baseX < recoveredData.tileWidth ; baseX++) {
//                for (int baseY = 0; baseY < recoveredData.tileHeight; baseY++) {
//                    tempColor = getPixelColor(frameI, recoveredData.bestOriginX.get(i)+baseX, recoveredData.bestOriginY.get(i)+baseY);
//                    
//                    decodedP.setPixel(recoveredData.bestTilesX.get(i)+baseX, recoveredData.bestTilesY.get(i)+baseY, tempColor);
//                }
//            }
//        }
//        
//        BufferedImage final_decodedP = new BufferedImage(frameP.getColorModel(),decodedP,frameP.isAlphaPremultiplied(),null);
//        showImage(final_decodedP);
//
//        
//    }
    
    public static void codeAllImages(Args args) throws FileNotFoundException, IOException{
        BufferedImage tempI=null, tempP=null;
        FileOutputStream fos = new FileOutputStream("test.zip");
        ZipOutputStream zipOS = new ZipOutputStream(fos);
        int j = 0;
        for(int i= 0; i < imageNames.size(); i++){ 
            if(j >= (imageNames.size()/args.gop)){
                j=0;
            }
            if(j==0){
                // Frame I
                tempI = imageDict.get(imageNames.get(i));
                imgToZip(tempI, i, zipOS, "image_coded_");
            }else{
                //Frame P
                tempP = createCodedImg(tempI, imageDict.get(imageNames.get(i)), args.thresh, args.tileSize, args.seekRange);
                imgToZip(tempP, i, zipOS, "image_coded_");
                //showImage(tempP);
            }
            j++;
                
            
            //imageDict.get(imageNames.get(i))
        }
        zipOS.finish(); //Good practice!
        zipOS.close();
        
        try {
            FileOutputStream out = new FileOutputStream("codedData.out");
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(dataList);
            oos.flush();
        } catch (Exception e) {
            System.out.println("Problem serializing: " + e);
        }
    }
    
    public static void decodeAllImages(Args args){
        imageNames.clear();
        imagesToZip.clear();
        imageDict.clear();
        
        // llegir coded data
        ArrayList<CodedData> recoveredData = null;

        try {
          Boolean empty = false;
          FileInputStream in = new FileInputStream("codedData.out");
          ObjectInputStream ois = new ObjectInputStream(in);
          recoveredData =  (ArrayList<CodedData>) (ois.readObject());
        } catch (Exception e) {
          System.out.println("Problem serializing: " + e);
        }
        
        // llegir imatges del zip
        try {
            System.out.println("Reading zip file...");
            readZip(args.codedInput);
        } catch (IOException ex) {
            Logger.getLogger(ProjectePractiques.class.getName()).log(Level.SEVERE, null, ex);
        }
        BufferedImage tempFrame=null, tempFrameI=null;
        WritableRaster tempBitmap=null;
        WritableRaster tempDecoded=null;
        CodedData tempData=null;
        
        BufferedImage tempBufferedImage = null;
        
        
        int z = 0;
        int recoveredDataCounter = 0;
        // per cada imatge
        for(int i= 0; i < imageNames.size(); i++){
            if(z >= (imageNames.size()/args.gop)){
                z=0;
            }
            if(z == 0){
                tempFrameI = imageDict.get(imageNames.get(i));
                imageDict.put(imageNames.get(i), tempFrameI);
            }else{
                 // recuperar imatge
                tempFrame = imageDict.get(imageNames.get(i)); 
                tempBitmap = (WritableRaster) tempFrame.getData();
                tempDecoded = tempBitmap.createWritableChild(tempFrame.getMinX(), tempFrame.getMinY(), tempFrame.getWidth(), tempFrame.getHeight(), 0,0, null);

                tempData = recoveredData.get(recoveredDataCounter);
                recoveredDataCounter++;

                //System.out.println(tempData.bestTilesX);
                //System.out.println(tempData.tileWidth);
                //System.out.println(tempData.tileHeight);
                int[] tempColor;
                //if (seekY + f >= 0 && seekX + k >= 0 && seekY + f < frameI.getHeight() && seekX + k < frameI.getWidth())
                
                for(int k = 0; k < tempData.bestTilesX.size(); k++){
                    for (int baseX = 0; baseX < tempData.tileWidth ; baseX++) {
                        for (int baseY = 0; baseY < tempData.tileHeight; baseY++) {
                            tempColor = getPixelColor(tempFrameI, tempData.bestOriginX.get(k)+baseX, tempData.bestOriginY.get(k)+baseY);
                            tempDecoded.setPixel(tempData.bestTilesX.get(k)+baseX, tempData.bestTilesY.get(k)+baseY, tempColor);
                        }
                    }
                }
                
                // guardar-la
                tempBufferedImage = new BufferedImage(tempFrame.getColorModel(),tempDecoded,tempFrame.isAlphaPremultiplied(),null);
                imageDict.put(imageNames.get(i), tempBufferedImage);
            }
            z++;
        }

    }
    
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
}