/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package components.filepreview;

import components.JFileDisplayPanel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import net.sf.image4j.codec.ico.ICODecoder;
import net.sf.image4j.codec.ico.ICOImage;

/**
 * This is a FileDetailsView that can display a preview for Microsoft icon 
 * files. This uses Image4J to load the images from icon files and tries to 
 * determine the highest quality image to display.
 * @author Milo Steier
 * @see JFileDisplayPanel
 * @see DefaultFileDetailsView
 * @see FileDetailsView
 */
public class ICOFileDetailsView extends DefaultFileDetailsView{
    /**
     * This is used to compare and sort ICOImages in order from the smallest 
     * images with the smallest color depth to the largest images with the 
     * largest color depth. This is initialized the first time this is requested 
     * by {@link #getIconImageComparator() }.
     */
    private static Comparator<ICOImage> icoCompare = null;
    /**
     * This constructs a ICOFileDetailsView.
     */
    public ICOFileDetailsView(){
        super();
    }
    /**
     * This returns the comparator used to sort ICOImages in order of color 
     * depth and size. Smaller color depths are prioritized over larger color 
     * depths and smaller images are prioritized over larger images. The color 
     * depth is prioritized over the image size. As such, if two images are the 
     * same size, but one has a color depth of 8 bits per pixel and the other 
     * has a color depth of 32 bits per pixel, then the one with 8 bits per 
     * pixel will come before the one with 32 bits per pixel. If two images have 
     * the same color depth but not the same size, then the image with the 
     * smaller width is prioritized. If both images have the same width, then 
     * the image with the smaller height is prioritized.
     * @return The comparator used to sort ICOImages.
     * @see ICOImage#getColourDepth() 
     * @see ICOImage#getImage() 
     * @see BufferedImage#getWidth() 
     * @see BufferedImage#getHeight() 
     */
    protected Comparator<ICOImage> getIconImageComparator(){
        if (icoCompare == null){    // If the comparator is not initialized
            icoCompare = new Comparator<>(){
                @Override
                public int compare(ICOImage o1, ICOImage o2) {
                        // Compare whether they are equal or at least one of 
                    Integer value = compareObjects(o1,o2);// then is null
                    if (value != null)  // If the comparison is not null
                        return value;
                        // Get the comparison between the two images
                    value = Integer.compare(o1.getColourDepth(), o2.getColourDepth());
                    if (value == 0){    // If the color depths are the same
                            // Get the image of the first ICOImage
                        BufferedImage img1 = o1.getImage();
                            // Get the image of the second ICOImage
                        BufferedImage img2 = o2.getImage();
                            // Compare the images to see if they are equal or 
                            // one of them is null
                        value = compareObjects(img1,img2);
                        if (value != null)  // If the comparison is not null
                            return value;
                        value = Integer.compare(img1.getWidth(), img2.getWidth());
                        if (value == 0) // If the widths are the same
                            value = Integer.compare(img1.getHeight(), img2.getHeight());
                    }
                    return value;
                }
                /**
                 * This compares two objects based off whether they are equal or 
                 * one of the is null. If both are equal, then this returns 0. 
                 * If the first is null, then this returns -1. If the second is 
                 * null, then this returns 1. Otherwise, this returns null.
                 * @param o1 The first object to compare.
                 * @param o2 The second object to compare.
                 * @return 0 if both are equal, -1 if {@code o1} is null, 1 if 
                 * {@code o2} is null, or null if neither are null or equal.
                 */
                private Integer compareObjects(Object o1, Object o2){
                        // If both are equal to each other
                    if (java.util.Objects.equals(o1, o2))   
                        return 0;
                    else if (o1 == null)            // If the first is null
                        return -1;
                    else if (o2 == null)            // If the second is null
                        return 1;
                    return null;
                }
            };
        }
        return icoCompare;
    }
    @Override
    public String getFileDescription(File file, JFileDisplayPanel panel){
        if (!isFileDisplayable(file,panel)) // If the file is not displayable
            return null;
        if (!file.isDirectory()){           // If the file is not a directory
                // Get the file name (in uppercase to make comparisons easier)
            String name = file.getName().toUpperCase();
                // If the file ends with the ico file extension
            if (name.endsWith(".ICO"))  
                return "Microsoft Icon File";
                // If the file ends with the cursor file extension
            else if (name.endsWith(".CUR"))
                return "Microsoft Cursor File";
        }
        return super.getFileDescription(file, panel);
    }
    /**
     * This returns an icon displaying a preview of the file. This is used when 
     * the file is not an image, but the file still can have a preview. For 
     * Microsoft icon files, this will return the highest quality image loaded 
     * from the given file. If the file is not a Microsoft icon file, then this 
     * will return null.
     * @param file {@inheritDoc }
     * @param panel {@inheritDoc }
     * @return {@inheritDoc }
     * @see #isFileDisplayable(java.io.File, components.files.preview.JFileDisplayPanel) 
     * @see #getFileImage(java.io.File, components.files.preview.JFileDisplayPanel) 
     * @see #getFileIcon(java.io.File, components.files.preview.JFileDisplayPanel) 
     * @see #getFilePreviewDetail(java.io.File, javax.swing.Icon, components.files.preview.JFileDisplayPanel) 
     * @see ICODecoder#readExt(java.io.File) 
     */
    @Override
    public Icon getFilePreview(File file, JFileDisplayPanel panel){
        if (!isFileDisplayable(file,panel)) // If the file is not displayable
            return null;
        try{    // Gets a list containing the images with metadata from the file
                // if the file is an icon file
            List<ICOImage> images = ICODecoder.readExt(file);
                // If no images were loaded
            if (images == null || images.isEmpty())
                return null;
            images.sort(getIconImageComparator());
            return new ImageIcon(images.get(images.size()-1).getImage());
        }
        catch(Exception exc){
        }
        return super.getFilePreview(file, panel);
    }
}
