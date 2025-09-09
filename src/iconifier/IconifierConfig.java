/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iconifier;

import config.ConfigUtilities;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
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
    /**
     * 
     */
    public static final String ALWAYS_SCALE_KEY = "AlwaysScale";
    
    public static final String IMAGE_FORMATTING_KEY = "ImageFormatting";
    
    public static final String DEFAULT_IMAGE_SCALING_KEY = "DefaultImageScaling";
    
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
    /**
     * 
     * @return 
     */
    public Rectangle getProgramBounds(){
        return getRectangle(PROGRAM_BOUNDS_KEY);
    }
    /**
     * 
     * @param comp
     * @return 
     */
    public Rectangle getProgramBounds(Iconifier comp){
        Rectangle rect = getProgramBounds();
        SwingExtendedUtilities.setComponentBounds(comp, rect);
        return rect;
    }
    /**
     * 
     * @param comp 
     */
    public void setProgramBounds(Iconifier comp){
        putRectangle(PROGRAM_BOUNDS_KEY,comp);
    }
    /**
     * 
     * @param fc
     * @param defaultValue
     * @return 
     */
    public File getSelectedFile(JFileChooser fc, File defaultValue){
        return getFile(getFileChooserPreferences(fc),
                FILE_CHOOSER_SELECTED_FILE_KEY,defaultValue);
    }
    /**
     * 
     * @param fc
     * @return 
     */
    public File getSelectedFile(JFileChooser fc){
        return getSelectedFile(fc,null);
    }
    /**
     * 
     * @param fc
     * @param file 
     */
    public void setSelectedFile(JFileChooser fc, File file){
        putFile(getFileChooserPreferences(fc),FILE_CHOOSER_SELECTED_FILE_KEY,file);
    }
    /**
     * 
     * @param fc 
     */
    public void setSelectedFile(JFileChooser fc){
        setSelectedFile(fc,fc.getSelectedFile());
    }
    /**
     * 
     * @param fc
     * @return 
     */
    public Dimension getFileChooserSize(JFileChooser fc){
        return ConfigUtilities.getDimension(getFileChooserPreferences(fc),
                FILE_CHOOSER_SIZE_KEY,null);
    }
    /**
     * 
     * @param fc
     */
    public void setFileChooserSize(JFileChooser fc){
        ConfigUtilities.putDimension(getFileChooserPreferences(fc),
                FILE_CHOOSER_SIZE_KEY,fc);
    }
    /**
     * 
     * @param fc
     * @return 
     */
    public File getCurrentDirectory(JFileChooser fc){
        return getFile(getFileChooserPreferences(fc),
                FILE_CHOOSER_CURRENT_DIRECTORY_KEY,null);
    }
    /**
     * 
     * @param fc 
     */
    public void setCurrentDirectory(JFileChooser fc){
        putFile(getFileChooserPreferences(fc),FILE_CHOOSER_CURRENT_DIRECTORY_KEY,
                fc.getCurrentDirectory());
    }
    /**
     * 
     * @param fc
     * @return 
     */
    public FileFilter getFileFilter(JFileChooser fc){
        return ConfigUtilities.getFileFilter(getFileChooserPreferences(fc), fc, 
                FILE_CHOOSER_FILE_FILTER_KEY, null);
    }
    /**
     * 
     * @param fc
     * @param filter 
     */
    public void setFileFilter(JFileChooser fc, FileFilter filter){
        ConfigUtilities.putFileFilter(getFileChooserPreferences(fc), fc, 
                FILE_CHOOSER_FILE_FILTER_KEY, filter);
    }
    /**
     * 
     * @param fc 
     */
    public void setFileFilter(JFileChooser fc){
        setFileFilter(fc,fc.getFileFilter());
    }
    /**
     * 
     * @param fc 
     */
    public void storeFileChooser(JFileChooser fc){
            // Put the file chooser's size in the preference node
        setFileChooserSize(fc);
            // Put the file chooser's current directory in the preference node
        setCurrentDirectory(fc);
    }
    /**
     * 
     * @param fc 
     */
    public void loadFileChooser(JFileChooser fc){
            // Get the current directory for the file chooser
        File dir = getCurrentDirectory(fc);
            // If there is a current directory for the file chooser and it exists
        if (dir != null && dir.exists()){
                // Set the file chooser's current directory
            fc.setCurrentDirectory(dir);
        }   // Get the selected file for the file chooser, or null
        File file = getSelectedFile(fc);
            // If there is a selected file for the file chooser
        if (file != null){
                // Select that file in the file chooser
            fc.setSelectedFile(file);
        }   // Load the file chooser's size from the preference node
        SwingExtendedUtilities.setComponentSize(fc,getFileChooserSize(fc));
            // Get the file filter for the file chooser
        FileFilter filter = getFileFilter(fc);
            // If there is a file filter selected
        if (filter != null)
            fc.setFileFilter(filter);
    }
    /**
     * 
     * @param value 
     */
    public void setProgressDisplaySetting(int value){
        getPreferences().putInt(PROGRESS_DISPLAY_KEY, value);
    }
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public int getProgressDisplaySetting(int defaultValue){
        return getPreferences().getInt(PROGRESS_DISPLAY_KEY, defaultValue);
    }
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public boolean isPreviewBorderShown(boolean defaultValue){
        return getPreferences().getBoolean(SHOW_PREVIEW_BORDER_KEY, defaultValue);
    }
    /**
     * 
     * @param value 
     */
    public void setPreviewBorderShown(boolean value){
        getPreferences().putBoolean(SHOW_PREVIEW_BORDER_KEY, value);
    }
    /**
     * 
     * @return 
     */
    public boolean isImageAlwaysScaled(){
        return getPreferences().getBoolean(ALWAYS_SCALE_KEY, false);
    }
    /**
     * 
     * @param value 
     */
    public void setImageAlwaysScaled(boolean value){
        getPreferences().putBoolean(ALWAYS_SCALE_KEY, value);
    }
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public int getImageFormatSetting(int defaultValue){
        return Math.min(Math.max(getPreferences().getInt(IMAGE_FORMATTING_KEY, 
                defaultValue), Iconifier.FIRST_IMAGE_FORMATTING), 
                Iconifier.LAST_IMAGE_FORMATTING);
    }
    /**
     * 
     * @return 
     */
    public int getImageFormatting(){
        return getImageFormatSetting(Iconifier.IMAGE_FORMATTING_CENTERED);
    }
    /**
     * 
     * @param value 
     */
    public void setImageFormatting(int value){
        getPreferences().putInt(IMAGE_FORMATTING_KEY, value);
    }
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public int getDefaultImageScaling(int defaultValue){
        return Math.min(Math.max(getPreferences().getInt(DEFAULT_IMAGE_SCALING_KEY,
                defaultValue), Iconifier.FIRST_IMAGE_SCALING),
                Iconifier.LAST_IMAGE_SCALING);
    }
    /**
     * 
     * @return 
     */
    public int getDefaultImageScaling(){
        return getDefaultImageScaling(Iconifier.IMAGE_SCALING_THUMBNAILATOR);
    }
    /**
     * 
     * @param value 
     */
    public void setDefaultImageScaling(int value){
        getPreferences().putInt(DEFAULT_IMAGE_SCALING_KEY, value);
    }
    /**
     * 
     */
    public class FileChooserPropertyChangeListener implements PropertyChangeListener{
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
                // If the property name is null or the source is not a file 
                // chooser
            if (evt.getPropertyName() == null || !(evt.getSource() instanceof JFileChooser))
                return;
                // Determine the property that was just changed
            switch(evt.getPropertyName()){
                    // If the file filter has changed
                case(JFileChooser.FILE_FILTER_CHANGED_PROPERTY):
                        // Store the file filter in the preference node
                    setFileFilter((JFileChooser)evt.getSource());
            }
        }
    }
}
