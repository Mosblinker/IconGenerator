/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package components;

import java.awt.Component;
import java.awt.image.BufferedImage;
import javax.swing.JList;
import net.sf.image4j.codec.bmp.BMPImage;
import net.sf.image4j.codec.ico.ICOImage;

/**
 * 
 * @author Milo Steier
 */
public class BitmapDetailsCellRenderer extends ImageSizeCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList list, Object value, 
            int index, boolean isSelected, boolean cellHasFocus){
        if (value instanceof BMPImage){
            BMPImage img = (BMPImage) value;
            BufferedImage buffImg = img.getImage();
            String text = dimensionToString(buffImg.getWidth(),buffImg.getHeight());
            if (img.getColourDepth() != -1 && !img.isIndexed())
                text += ", " + img.getColourDepth() + "-bit color";
            else if (img.getColourCount() != -1)
                text += ", " + img.getColourCount() + " color" + 
                        ((img.getColourCount() > 1) ? "s" : "");
            if (img instanceof ICOImage){
                ICOImage ico = (ICOImage) img;
                text = ico.getIconIndex() + ": " + text;
                if (ico.isPngCompressed())
                    text += " (PNG)";
            }
            value = text;
        }
        
        return super.getListCellRendererComponent(list,value,index,isSelected,
            cellHasFocus);
    }
}
