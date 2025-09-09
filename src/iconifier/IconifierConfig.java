/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iconifier;

import config.ConfigUtilities;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import utils.SwingExtendedUtilities;

/**
 *
 * @author Mosblinker
 */
public class IconifierConfig {
    /**
     * This is the configuration key for the progress display setting.
     */
    public static final String PROGRESS_DISPLAY_KEY = "DisplayProgress";
    /**
     * This is the configuration key for the bounds of the program.
     */
    public static final String PROGRAM_BOUNDS_KEY = "ProgramBounds";
    
    public static final String SHOW_PREVIEW_BORDER_KEY = "ShowPreviewBorder";
    
    public static final String SCALE_IMAGE_PREVIEW_KEY = "ScaleImagePreview";
    
    public static final String IMAGE_FORMAT_SETTING_KEY = "FormatImage";
    
    public static final String SCALE_IMAGE_SETTING_KEY = "ScaleImage";
    
    public static final String CIRCULAR_ICON_SETTING_KEY = "CircularIcon";
    
    public static final String FEATHERING_SETTING_KEY = "Feathering";
    /**
     * 
     */
    public static final String FILE_CHOOSER_SIZE_KEY = "FileChooserSize";
    /**
     * 
     */
    public static final String FILE_CHOOSER_CURRENT_DIRECTORY_KEY = 
            "CurrentDirectory";
    /**
     * 
     */
    public static final String FILE_CHOOSER_SELECTED_FILE_KEY = 
            "SelectedFile";
    /**
     * 
     */
    public static final String FILE_CHOOSER_FILE_FILTER_KEY = 
            "FileFilter";
    /**
     * This is a preference node to store the settings for this program.
     */
    private final Preferences node;
    /**
     * 
     */
    private final Map<JFileChooser, Preferences> fcNodes;
    /**
     * 
     * @param node 
     */
    public IconifierConfig(Preferences node){
        this.node = Objects.requireNonNull(node);
        fcNodes = new HashMap<>();
    }
    /**
     * 
     * @return 
     */
    public Preferences getPreferences(){
        return node;
    }
    /**
     * 
     * @return 
     */
    public Map<JFileChooser, Preferences> getFileChooserPreferenceMap(){
        return fcNodes;
    }
    /**
     * 
     * @param fc
     * @param name
     */
    public void addFileChooser(JFileChooser fc, String name){
        if (!fcNodes.containsKey(fc))
            fcNodes.put(fc, getPreferences().node(name));
    }
    /**
     * 
     * @param fc
     * @return 
     */
    public Preferences getFileChooserPreferences(JFileChooser fc){
        return fcNodes.get(fc);
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public Dimension getDimension(String key, Dimension defaultValue){
        return ConfigUtilities.getDimension(node, key, defaultValue);
    }
    /**
     * 
     * @param key
     * @return 
     */
    public Dimension getDimension(String key){
        return getDimension(key,null);
    }
    /**
     * 
     * @param key
     * @param width
     * @param height 
     */
    public void putDimension(String key, int width, int height){
        ConfigUtilities.putDimension(node, key, width, height);
    }
    /**
     * 
     * @param key
     * @param dim 
     */
    public void putDimension(String key, Dimension dim){
        ConfigUtilities.putDimension(node, key, dim);
    }
    /**
     * 
     * @param key
     * @param comp 
     */
    public void putDimension(String key, Component comp){
        ConfigUtilities.putDimension(node, key, comp);
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public Rectangle getRectangle(String key, Rectangle defaultValue){
        return ConfigUtilities.getRectangle(node, key, defaultValue);
    }
    /**
     * 
     * @param key
     * @return 
     */
    public Rectangle getRectangle(String key){
        return getRectangle(key,null);
    }
    /**
     * 
     * @param key
     * @param x
     * @param y
     * @param width
     * @param height 
     */
    public void putRectangle(String key, int x, int y, int width, int height){
        ConfigUtilities.putRectangle(node, key, x, y, width, height);
    }
    /**
     * 
     * @param key
     * @param width
     * @param height 
     */
    public void putRectangle(String key, int width, int height){
        putRectangle(key,0,0,width,height);
    }
    /**
     * 
     * @param key
     * @param value 
     */
    public void putRectangle(String key, Rectangle value){
        ConfigUtilities.putRectangle(node, key, value);
    }
    /**
     * 
     * @param key
     * @param comp 
     */
    public void putRectangle(String key, Component comp){
        ConfigUtilities.putRectangle(node, key, comp);
    }
    /**
     * 
     * @param node
     * @param key
     * @param defaultFile
     * @return 
     */
    protected File getFile(Preferences node, String key, File defaultFile){
            // Get the name of the file from the preference node, or null
        String name = node.get(key, null);
            // If there is no value set for that key
        if (name == null)
            return defaultFile;
        return new File(name);
    }
    /**
     * 
     * @param key
     * @param defaultFile
     * @return 
     */
    public File getFile(String key, File defaultFile){
        return getFile(getPreferences(),key,defaultFile);
    }
    /**
     * 
     * @param key
     * @return 
     */
    public File getFile(String key){
        return getFile(key,null);
    }
    /**
     * 
     * @param node
     * @param key
     * @param value 
     */
    protected void putFile(Preferences node, String key, File value){
        if (value == null)
            node.remove(key);
        else
            node.put(key, value.toString());
    }
    /**
     * 
     * @param key
     * @param value 
     */
    public void putFile(String key, File value){
        putFile(getPreferences(),key,value);
    }
}
