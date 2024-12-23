/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package iconifier;

import components.*;
import components.debug.DebugCapable;
import components.disable.DisableGUIInput;
import static components.disable.DisableInput.beep;
import components.progress.JProgressDisplayMenu;
import files.FilesExtended;
import files.extensions.ImageExtensions;
import icons.*;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import net.coobird.thumbnailator.Thumbnailator;
import net.coobird.thumbnailator.Thumbnails;
import net.sf.image4j.codec.bmp.*;
import net.sf.image4j.codec.ico.*;
import net.sf.image4j.util.ConvertUtil;

/**
 *
 * @author Milo Steier
 */
public class Iconifier extends JFrame implements DisableGUIInput, DebugCapable{
    /**
     * This has the name of this program.
     */
    public static final String PROGRAM_NAME = "Icon Generator";
    /**
     * This has the path of the icon that this program displays.
     */
    public static final String ICON_FILE = "/images/"+PROGRAM_NAME+" Icon.png";
    
    public static final String ICO = "ico";
    
    public static final FileFilter ICON_FILTER = 
            FilesExtended.generateExtensionFilter("Microsoft Icon File", ICO);
    
//    public static final String CUR = "cur";
//    
//    public static final FileFilter CURSOR_FILTER = 
//            FilesExtended.generateExtensionFilter("Cursor File", CUR);
    
    public static final String BMP = "bmp";
    
    public static final FileFilter BITMAP_FILTER = 
            FilesExtended.generateExtensionFilter("Bitmap File",BMP);
    
    private static final Dimension[] DEFAULT_ICON_DIMENSIONS = {
        new Dimension(16,16),
        new Dimension(24,24),
        new Dimension(32,32),
        new Dimension(40,40),
        new Dimension(48,48),
        new Dimension(64,64),
        new Dimension(96,96),
        new Dimension(128,128),
        new Dimension(256,256),
        new Dimension(512,512),
        new Dimension(768,768)
    };
    
    private static final int AUTO_SHRINK_MULTIPLIER = 4;
    
    private static Dimension generateShrinkSize(){
        Dimension dim = new Dimension(DEFAULT_ICON_DIMENSIONS[DEFAULT_ICON_DIMENSIONS.length-1]);
        dim.width *= AUTO_SHRINK_MULTIPLIER;
        dim.height *= AUTO_SHRINK_MULTIPLIER;
        return dim;
    }
    
    private static final Dimension AUTO_SHRINK_SIZE = generateShrinkSize();
    
    private static final Dimension AUTO_COMPRESS_SIZE = new Dimension(256, 256);
    
    private static final int[] DEFAULT_BITS_PER_PIXEL = {/*4, 8, 24, */32};
    
    private static Set<Integer> generateAutoCompressedIndexes(){
        Set<Integer> compressed = new TreeSet<>();
        Set<Integer> offsets = new HashSet<>();
        for (int i = 0; i < DEFAULT_BITS_PER_PIXEL.length; i++){
            offsets.add(i*DEFAULT_ICON_DIMENSIONS.length);
        }
        for (int i = 0; i < DEFAULT_ICON_DIMENSIONS.length; i++){
            Dimension dim = DEFAULT_ICON_DIMENSIONS[i];
            if (dim.width >= AUTO_COMPRESS_SIZE.width || 
                    dim.height >= AUTO_COMPRESS_SIZE.height){
                for (Integer offset : offsets){
                    compressed.add(i+offset);
                }
            }
        }
        return Collections.unmodifiableSet(compressed);
    }
    
    private static final Set<Integer> DEFAULT_AUTO_COMPRESSED_INDEXES = 
            generateAutoCompressedIndexes();
    
    public static ICOImage createICOImage(BufferedImage img){
        InfoHeader info = BMPEncoder.createInfoHeader(img);
        return new ICOImage(img,info,ICOEncoder.createIconEntry(info));
    }
    
    private static final int FORMAT_IMAGE_SETTING_SCALED = 0;
    
    private static final int FORMAT_IMAGE_SETTING_CENTERED = 1;
    
    private static final int FORMAT_IMAGE_SETTING_UP_LEFT = 2;
    
    private static final int FORMAT_IMAGE_SETTING_DOWN_RIGHT = 3;
    
    private static final int FORMAT_IMAGE_SETTING_CROP_CENTER = 4;
    
    private static final int FORMAT_IMAGE_SETTING_CROP_UP_LEFT = 5;
    
    private static final int FORMAT_IMAGE_SETTING_CROP_DOWN_RIGHT = 6;
    
    private static final int FIRST_FORMAT_IMAGE_SETTING = FORMAT_IMAGE_SETTING_SCALED;
    
    private static final int LAST_FORMAT_IMAGE_SETTING = FORMAT_IMAGE_SETTING_CROP_DOWN_RIGHT;
    
    private static final int SCALE_IMAGE_SETTING_NEAREST_NEIGHBOR = 0;
    
    private static final int SCALE_IMAGE_SETTING_BILINEAR = 1;
    
    private static final int SCALE_IMAGE_SETTING_BICUBIC = 2;
    
    private static final int SCALE_IMAGE_SETTING_SMOOTH = 3;
    
    private static final int SCALE_IMAGE_SETTING_THUMBNAILATOR = 4;
    
    private static final int FIRST_SCALE_IMAGE_SETTING = SCALE_IMAGE_SETTING_NEAREST_NEIGHBOR;
    
    private static final int LAST_SCALE_IMAGE_SETTING = SCALE_IMAGE_SETTING_THUMBNAILATOR;
    
    private static final int ICONS_GENERATED_COUNT = 
            (DEFAULT_ICON_DIMENSIONS.length * (DEFAULT_BITS_PER_PIXEL.length+1) * 
            (LAST_FORMAT_IMAGE_SETTING+1) * (LAST_SCALE_IMAGE_SETTING+1)) + 
            (LAST_SCALE_IMAGE_SETTING+1);
    
    private static final int DEFAULT_BORDER_THICKNESS = 3;
    /**
     * This is the name of the preference node used to store the settings for 
     * this program.
     */
    private static final String PREFERENCE_NODE_NAME = 
            "milo/icon/gen/IconGenerator";
    /**
     * This is the key in the preference node for the progress display setting.
     */
    private static final String PROGRESS_DISPLAY_KEY = "DisplayProgress";
    /**
     * This is the key in the component-specific preference node for the widths 
     * of components.
     */
    private static final String PREFERENCE_WIDTH_KEY = "WindowWidth";
    /**
     * This is the key in the component-specific preference node for the heights 
     * of components.
     */
    private static final String PREFERENCE_HEIGHT_KEY = "WindowHeight";
    /**
     * This is the name of the child preference node used to store values 
     * related to the open file chooser.
     */
    private static final String OPEN_FILE_CHOOSER_PREFERENCE_NODE = 
            "OpenFileChooser";
    /**
     * This is the name of the child preference node used to store values 
     * related to the save file chooser.
     */
    private static final String SAVE_FILE_CHOOSER_PREFERENCE_NODE = 
            "SaveFileChooser";
    
    private static final String SHOW_PREVIEW_BORDER_KEY = "ShowPreviewBorder";
    
    private static final String SCALE_IMAGE_PREVIEW_KEY = "ScaleImagePreview";
    
    private static final String FORMAT_IMAGE_SETTING_KEY = "FormatImage";
    
    private static final String SCALE_IMAGE_SETTING_KEY = "ScaleImage";
    
    private static final String OPEN_FILE_CHOOSER_DIRECTORY_KEY = 
            "OpenFCCurrentDirectory";
    /**
     * This is the key in the preference node for the directory for the save 
     * file chooser.
     */
    private static final String SAVE_FILE_CHOOSER_DIRECTORY_KEY = 
            "SaveFCCurrentDirectory";
    
    private static final String OPEN_FILE_CHOOSER_FILE_FILTER_KEY = 
            "OpenFCFileFilter";
    
    /**
     * Creates new form Iconifier
     * @param debugMode
     */
    public Iconifier(boolean debugMode) {
        this.debugMode = debugMode;
        
        setIconImages(generateIconImages(
                new ImageIcon(this.getClass().getResource(ICON_FILE)).getImage()));
        
        imagePreviewModel = new ComboBoxModelList<>();
        
        initComponents();
        
        progressBar.addChangeListener(progressDisplay);
        progressBar.addPropertyChangeListener(progressDisplay);
        
        previewComboBox.setRenderer(new BitmapDetailsCellRenderer());
        
        for (FileFilter filter : ImageExtensions.IMAGE_FILTERS){
            openFC.addChoosableFileFilter(filter);
        }
        openFC.setFileFilter(ImageExtensions.IMAGE_FILTER);
        saveFC.setFileFilter(ICON_FILTER);
        
        try{    // Try to load the settings from the preference node
            config = Preferences.userRoot().node(PREFERENCE_NODE_NAME);
            int displaySettings = config.getInt(PROGRESS_DISPLAY_KEY, 
                    progressDisplay.getDisplaySettings());
            if (displaySettings != 0)
                progressDisplay.setDisplaySettings(displaySettings);
            showPreviewBorderToggle.setSelected(config.getBoolean(SHOW_PREVIEW_BORDER_KEY, 
                    showPreviewBorderToggle.isSelected()));
            scaleImageAlwaysToggle.setSelected(config.getBoolean(SCALE_IMAGE_PREVIEW_KEY, 
                    scaleImageAlwaysToggle.isSelected()));
            setComboIndexFromConfig(FORMAT_IMAGE_SETTING_KEY,formatImageCombo);
            setComboIndexFromConfig(SCALE_IMAGE_SETTING_KEY,scaleCombo);
            String dirName = config.get(OPEN_FILE_CHOOSER_DIRECTORY_KEY, null);
            if (dirName != null){
                File dir = new File(dirName);
                if (dir.exists())
                    openFC.setCurrentDirectory(dir);
            }
            dirName = config.get(SAVE_FILE_CHOOSER_DIRECTORY_KEY, null);
            if (dirName != null){
                File dir = new File(dirName);
                if (dir.exists())
                    saveFC.setCurrentDirectory(dir);
            }
            openFC.setPreferredSize(getPreferenceSize(OPEN_FILE_CHOOSER_PREFERENCE_NODE,
                    openFC.getPreferredSize()));
            saveFC.setPreferredSize(getPreferenceSize(SAVE_FILE_CHOOSER_PREFERENCE_NODE,
                    saveFC.getPreferredSize()));
            Dimension dim = getPreferenceSize(null,getPreferredSize());
                // Get the minimum size for the program
            Dimension min = getMinimumSize();
                // Make sure the width and height are within range
            dim.width = Math.max(dim.width, min.width);
            dim.height = Math.max(dim.height, min.height);
                // Set the size from the node
            setSize(dim);
            int filter = config.getInt(OPEN_FILE_CHOOSER_FILE_FILTER_KEY, -1);
            if (filter >= 0 && filter < openFC.getChoosableFileFilters().length)
                openFC.setFileFilter(openFC.getChoosableFileFilters()[filter]);
        } catch (SecurityException | IllegalStateException ex){
            config = null;
            System.out.println("Unable to load settings: " +ex);
        } catch (IllegalArgumentException ex){
            System.out.println("Invalid setting: " + ex);
        }
        
        previewBorders = new Border[]{
            BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK), 
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.WHITE, DEFAULT_BORDER_THICKNESS-2), 
                            BorderFactory.createLineBorder(Color.BLACK))),
            BorderFactory.createEmptyBorder(DEFAULT_BORDER_THICKNESS, 
                    DEFAULT_BORDER_THICKNESS, 
                    DEFAULT_BORDER_THICKNESS, 
                    DEFAULT_BORDER_THICKNESS)
        };
        
        previewLabel.setThumbnailBorder(getPreviewBorder());
        previewLabel.setImageAlwaysScaled(scaleImageAlwaysToggle.isSelected());
        
        debugMenu.setVisible(debugMode);
        useDebugIconToggle.setSelected(debugMode);
    }
    /**
     * 
     */
    public Iconifier(){
        this(false);
    }
    /**
     * 
     * @param image
     * @return 
     */
    private java.util.List<BufferedImage> generateIconImages(Image image){
        if (image == null)
            return null;
        BufferedImage img;
        if (image instanceof BufferedImage)
            img = (BufferedImage) image;
        else{
            img = new BufferedImage(image.getWidth(null),image.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
        }    // Create a list to get the images
        ArrayList<BufferedImage> iconImages = new ArrayList<>();
            // Go through the sizes for the images
        for (Dimension dim : DEFAULT_ICON_DIMENSIONS){
            iconImages.add(Thumbnailator.createThumbnail(img, dim.width, dim.height));
        }
        return iconImages;
    }
    /**
     * This updates the boolean value stored at the given key in the program's 
     * preference node to reflect whether the given toggle button is selected or 
     * not.
     * @param key The key for the value in the preference node to update.
     * @param toggleButton The toggle button to use to set the value in the 
     * preference node.
     */
    private void updateConfigBoolean(String key, AbstractButton toggleButton){
        if (config != null){        // If the preference node is available
            try{
                config.putBoolean(key, toggleButton.isSelected());
            } catch (IllegalStateException ex){ 
                if (isInDebug())    // If we are in debug mode
                    System.out.println("Error: " + ex);
            }
        }
    }
    
    private void setComboIndexFromConfig(String key, JComboBox comboBox){
        if (config != null){        // If the preference node is available
            try{
                comboBox.setSelectedIndex(Math.max(Math.min(config.getInt(key, 
                        comboBox.getSelectedIndex()), comboBox.getItemCount()-1), 0));
            } catch (IllegalStateException ex){ 
                if (isInDebug())    // If we are in debug mode
                    System.out.println("Error: " + ex);
            }
        }
    }
    /**
     * This updates the integer value stored at the given key in the program's 
     * preference node to reflect the selected index for the given combo box.
     * @param key The key for the value in the preference node to update.
     * @param comboBox The combo box to get the selected index for
     */
    private void updateConfigComboIndex(String key, JComboBox comboBox){
        if (config != null){        // If the preference node is available
            try{
                config.putInt(key, comboBox.getSelectedIndex());
            } catch (IllegalStateException ex){ 
                if (isInDebug())    // If we are in debug mode
                    System.out.println("Error: " + ex);
            }
        }
    }
    /**
     * This checks to see if the preference node is available, and if not, 
     * throws an IllegalStateException.
     * @throws IllegalStateException If the preference node was not available to 
     * the program at initialization.
     */
    private void checkIfPreferencesIsAvailable(){
            // If the preference node is not available
        if (config == null)
            throw new IllegalStateException("Preference node is not available");
    }
    /**
     * This returns the size mapped to the given key in the preference node, or 
     * {@code def} if no size is mapped to that key.
     * @param key The key to get the associated size for.
     * @param def The default size to use if there is no size associated with 
     * the given key.
     * @return The size mapped to the given key, or {@code def} if no size is 
     * mapped to the given key.
     * @throws IllegalStateException If the preference node is not available, 
     * either due to not being available when the program started up or due to 
     * the preference node being removed.
     * @throws IllegalArgumentException If the key contains the null control 
     * character.
     */
    private Dimension getPreferenceSize(String key, Dimension def){
            // Use an empty String if given null
        key = Objects.requireNonNullElse(key, "");
            // Check as to whether the preference node is available
        checkIfPreferencesIsAvailable();
            // Get the node used to store the size
        Preferences node = config.node(key);
            // If both the width and height have not been set
        if (node.get(PREFERENCE_WIDTH_KEY, null) == null && 
                node.get(PREFERENCE_HEIGHT_KEY, null) == null)
            return def;
           // If the default size is null
        if (def == null)
            def = new Dimension(0,0);
            // Get the width
        int width = node.getInt(PREFERENCE_WIDTH_KEY, def.width);
            // Get the height
        int height = node.getInt(PREFERENCE_HEIGHT_KEY, def.height);
        return new Dimension(width,height);
    }
    /**
     * This maps the given key to the given size in the preference node. If the 
     * given size is null, then the key will be removed.
     * @param key The key to map the size to.
     * @param size The size to map to the key.
     * @throws IllegalStateException If the preference node is not available, 
     * either due to not being available when the program started up or due to 
     * the preference node being removed.
     * @throws IllegalArgumentException If the key either contains the null 
     * control character or is too long to be stored in the preference node. 
     */
    private void setPreferenceSize(String key, Dimension size){
            // Check as to whether the preference node is available
        checkIfPreferencesIsAvailable();
            // Use an empty String if given null
        key = Objects.requireNonNullElse(key, "");
            // Get the node used to store the size
        Preferences node = config.node(key);
            // If the given size is null
        if (size == null){
                // Remove the width
            node.remove(PREFERENCE_WIDTH_KEY);
                // Remove the height
            node.remove(PREFERENCE_HEIGHT_KEY);
        } else {
                // Set the width
            node.putInt(PREFERENCE_WIDTH_KEY, size.width);
                // Set the height
            node.putInt(PREFERENCE_HEIGHT_KEY, size.height);
        }
    }
    
    private Border getPreviewBorder(){
        return previewBorders[(showPreviewBorderToggle.isSelected())?0:1];
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        openFC = new javax.swing.JFileChooser();
        saveFC = new javax.swing.JFileChooser();
        previewPanel = new components.JFileDisplayPanel();
        previewLabel = new components.JThumbnailLabel();
        previewComboBox = new javax.swing.JComboBox<>();
        openButton = new javax.swing.JButton();
        progressBar = new javax.swing.JProgressBar();
        saveButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        pngCheckBox = new javax.swing.JCheckBox();
        settingsButton = new javax.swing.JButton();
        controlPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        scaleCombo = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        scaleOverrideCombo = new javax.swing.JComboBox<>();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jLabel2 = new javax.swing.JLabel();
        formatImageCombo = new javax.swing.JComboBox<>();
        includeToggle = new javax.swing.JCheckBox();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        settingsMenuItem = new javax.swing.JMenuItem();
        optionsMenu = new javax.swing.JMenu();
        progressDisplay = new components.progress.JProgressDisplayMenu();
        showPreviewBorderToggle = new javax.swing.JCheckBoxMenuItem();
        scaleImageAlwaysToggle = new javax.swing.JCheckBoxMenuItem();
        debugMenu = new javax.swing.JMenu();
        printTestButton = new javax.swing.JMenuItem();
        activeTestToggle = new javax.swing.JCheckBoxMenuItem();
        slowTestToggle = new components.debug.SlowTestMenuItem();
        useDebugIconToggle = new javax.swing.JCheckBoxMenuItem();
        showDebugToggle = new javax.swing.JCheckBoxMenuItem();

        openFC.setAccessory(previewPanel);
        openFC.setToolTipText("Square images work best.");
        openFC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileChooserActionPerformed(evt);
            }
        });
        openFC.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                openFCPropertyChange(evt);
            }
        });

        saveFC.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        saveFC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileChooserActionPerformed(evt);
            }
        });

        previewPanel.setFileChooser(openFC);
        previewPanel.setImageAnimationEnabled(false);
        previewPanel.setFileDetailsView(new components.filepreview.ICOFileDetailsView());

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(PROGRAM_NAME);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        previewComboBox.setModel(imagePreviewModel);
        previewComboBox.setEnabled(false);
        previewComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previewComboBoxActionPerformed(evt);
            }
        });

        openButton.setText("Open Image");
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });

        saveButton.setText("Save Icon");
        saveButton.setEnabled(false);
        saveButton.setMaximumSize(openButton.getMaximumSize());
        saveButton.setMinimumSize(openButton.getMinimumSize());
        saveButton.setPreferredSize(openButton.getPreferredSize());
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Preview:");

        pngCheckBox.setText("Compressed");
        pngCheckBox.setEnabled(false);
        pngCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pngCheckBoxActionPerformed(evt);
            }
        });

        settingsButton.setText("Icon Settings");
        settingsButton.setToolTipText("Currently not in use.");
        settingsButton.setEnabled(false);

        controlPanel.setLayout(new java.awt.GridBagLayout());

        jLabel3.setText("Default Scaling Method:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        controlPanel.add(jLabel3, gridBagConstraints);

        scaleCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Nearest Neighbor", "Bilinear", "Bicubic", "Smooth", "Thumbnailator" }));
        scaleCombo.setSelectedIndex(4);
        scaleCombo.setEnabled(false);
        scaleCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scaleComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 7, 6, 7);
        controlPanel.add(scaleCombo, gridBagConstraints);

        jLabel4.setText("Current Scaling Method:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        controlPanel.add(jLabel4, gridBagConstraints);

        scaleOverrideCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Default", "Nearest Neighbor", "Bilinear", "Bicubic", "Smooth", "Thumbnailator" }));
        scaleOverrideCombo.setToolTipText("Overrides the scaling method for the current image.");
        scaleOverrideCombo.setEnabled(false);
        scaleOverrideCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scaleOverrideComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 7, 0, 7);
        controlPanel.add(scaleOverrideCombo, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        controlPanel.add(filler1, gridBagConstraints);

        jLabel2.setText("Format Image:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 7);
        controlPanel.add(jLabel2, gridBagConstraints);

        formatImageCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Scaled", "Centered", "Up/Left", "Down/Right", "Crop Centered", "Crop Up/Left", "Crop Down/Right" }));
        formatImageCombo.setSelectedIndex(1);
        formatImageCombo.setToolTipText("How to handle non-square images.");
        formatImageCombo.setEnabled(false);
        formatImageCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatImageComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        controlPanel.add(formatImageCombo, gridBagConstraints);

        includeToggle.setSelected(true);
        includeToggle.setText("Include Image in Icon");
        includeToggle.setEnabled(false);
        includeToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includeToggleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        controlPanel.add(includeToggle, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.3;
        controlPanel.add(filler2, gridBagConstraints);

        fileMenu.setText("File");

        openMenuItem.setText("Open Image");
        fileMenu.add(openMenuItem);

        saveMenuItem.setText("Save Icon");
        saveMenuItem.setEnabled(false);
        fileMenu.add(saveMenuItem);
        fileMenu.add(jSeparator1);

        settingsMenuItem.setText("Icon Settings");
        settingsMenuItem.setEnabled(false);
        fileMenu.add(settingsMenuItem);

        jMenuBar1.add(fileMenu);

        optionsMenu.setText("Options");

        progressDisplay.setProgressDisplayed(true);
        progressDisplay.setUpdateEnabled(true);
        progressDisplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                progressDisplayActionPerformed(evt);
            }
        });
        optionsMenu.add(progressDisplay);

        showPreviewBorderToggle.setSelected(true);
        showPreviewBorderToggle.setText("Show Border Around Preview");
        showPreviewBorderToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPreviewBorderToggleActionPerformed(evt);
            }
        });
        optionsMenu.add(showPreviewBorderToggle);

        scaleImageAlwaysToggle.setText("Scale Image Preview");
        scaleImageAlwaysToggle.setToolTipText("Scale the preview image to fill the preview window. Warning: This may introduce blurines that isn't actually in the image.");
        scaleImageAlwaysToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scaleImageAlwaysToggleActionPerformed(evt);
            }
        });
        optionsMenu.add(scaleImageAlwaysToggle);

        jMenuBar1.add(optionsMenu);

        debugMenu.setText("Debug");

        printTestButton.setText("Print Data");
        printTestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printTestButtonActionPerformed(evt);
            }
        });
        debugMenu.add(printTestButton);

        activeTestToggle.setSelected(true);
        activeTestToggle.setText("Input Enabled");
        activeTestToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activeTestToggleActionPerformed(evt);
            }
        });
        debugMenu.add(activeTestToggle);
        debugMenu.add(slowTestToggle);

        useDebugIconToggle.setText("Use Debug Icon");
        useDebugIconToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useDebugIconToggleActionPerformed(evt);
            }
        });
        debugMenu.add(useDebugIconToggle);

        showDebugToggle.setText("Show Debug Features");
        showDebugToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showDebugToggleActionPerformed(evt);
            }
        });
        debugMenu.add(showDebugToggle);

        jMenuBar1.add(debugMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(previewLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(previewComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(settingsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pngCheckBox))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(openButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(controlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 608, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(previewLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(settingsButton)
                    .addComponent(pngCheckBox)
                    .addComponent(jLabel1)
                    .addComponent(previewComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(controlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(openButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        File file = null;
        if (openFC.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            file = openFC.getSelectedFile();
        }
        openFC.setPreferredSize(openFC.getSize());
        if (config != null){
            setPreferenceSize(OPEN_FILE_CHOOSER_PREFERENCE_NODE,openFC.getSize());
            config.put(OPEN_FILE_CHOOSER_DIRECTORY_KEY, 
                    openFC.getCurrentDirectory().toString());
        }
        if (file != null){
            imgGen = new GenerateImages(file);
            imgGen.execute();
        }
    }//GEN-LAST:event_openButtonActionPerformed
    
    private void printTestButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printTestButtonActionPerformed
        System.out.println(sourceImage);
        if (sourceImage != null){
            System.out.println(sourceImage.getWidth() + " x " + sourceImage.getHeight());
            int max = Math.max(sourceImage.getWidth(), sourceImage.getHeight());
            System.out.println((max-sourceImage.getWidth()) + " x " + (max-sourceImage.getHeight()));
        }
        
        if (icons != null){
            for (int i = 0; i < icons.size(); i++){
                for (int j = 0; j < icons.get(i).size(); j++){
                    ICOImage img = icons.get(i).get(j);
                    System.out.printf("\t(%3d %3d) %3d: (%3d x %3d) %2d (%5b) %5b (%2d %5b %5b)%n",
                            i, j, 
                            img.getIconIndex(), img.getWidth(), img.getHeight(),
                            img.getColourDepth(), img.isIndexed(), img.isPngCompressed(),
                            scaleSettings.getOrDefault(i, -1), excludedSet.contains(i), 
                            compressedSet.contains(i));
                }
            }
            System.out.println();
        }
        
        for (int i = 0; i < imagePreviewModel.size(); i++){
            ICOImage img = imagePreviewModel.get(i);
            System.out.printf("\t(%3d) %3d: (%3d x %3d) %2d (%5b) %5b (%2d %5b %5b)%n",
                        i, 
                        img.getIconIndex(), img.getWidth(), img.getHeight(),
                        img.getColourDepth(), img.isIndexed(), img.isPngCompressed(),
                        scaleSettings.getOrDefault(i, scaleCombo.getSelectedIndex()), 
                        excludedSet.contains(i), compressedSet.contains(i));
        }
        System.out.println();
        System.out.println("Preference Node: " + config);
        if (config != null){    // If the preference node is available
            try{
                System.out.println("Preference Node Exists: " + config.nodeExists(""));
                System.out.println("Preference Keys: " + Arrays.toString(config.keys()));
                System.out.println("Preferences: ");
                    // Go through the keys in the preference node
                for (String key : config.keys()){
                    System.out.printf("\t%30s = %s%n", key,config.get(key, null));
                }
            } catch(IllegalStateException | BackingStoreException ex){
                System.out.println("Error: " + ex);
            }
        }
        System.out.println();
    }//GEN-LAST:event_printTestButtonActionPerformed

    private void activeTestToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activeTestToggleActionPerformed
        setInputEnabled(activeTestToggle.isSelected());
    }//GEN-LAST:event_activeTestToggleActionPerformed
    /**
     * This updates the progress bar when the progress display settings are 
     * changed.
     * @param evt The ActionEvent.
     */
    private void progressDisplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_progressDisplayActionPerformed
        progressDisplay.updateProgressString(progressBar);
        if (progressDisplay.isProgressDisplayed() && config != null){
            try{
                config.putInt(PROGRESS_DISPLAY_KEY, progressDisplay.getDisplaySettings());
            } catch (IllegalStateException ex){ 
                if (isInDebug())    // If we are in debug mode
                    System.out.println("Error: " + ex);
            }
        }
    }//GEN-LAST:event_progressDisplayActionPerformed

    private void formatImageComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatImageComboActionPerformed
        updateConfigComboIndex(FORMAT_IMAGE_SETTING_KEY,formatImageCombo);
        populateImagePreviews();
    }//GEN-LAST:event_formatImageComboActionPerformed

    private void showDebugToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showDebugToggleActionPerformed
        if (debugIcon != null)
            debugIcon.setDebugEnabled(showDebugToggle.isSelected());
        previewLabel.repaintThumbnail();
    }//GEN-LAST:event_showDebugToggleActionPerformed

    private void useDebugIconToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useDebugIconToggleActionPerformed
        setPreviewImage();
    }//GEN-LAST:event_useDebugIconToggleActionPerformed

    private void pngCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pngCheckBoxActionPerformed
        for (ICOImage img : icons.get(previewComboBox.getSelectedIndex())){
            img.setPngCompressed(pngCheckBox.isSelected());
        }
        previewComboBox.repaint();
    }//GEN-LAST:event_pngCheckBoxActionPerformed

    private void scaleComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaleComboActionPerformed
        updateConfigComboIndex(SCALE_IMAGE_SETTING_KEY,scaleCombo);
        populateImagePreviews();
    }//GEN-LAST:event_scaleComboActionPerformed

    private void previewComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previewComboBoxActionPerformed
        setPreviewImage();
    }//GEN-LAST:event_previewComboBoxActionPerformed

    private void includeToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includeToggleActionPerformed
        int selected = previewComboBox.getSelectedIndex();
        int index = -1;
        int diff;
        if (includeToggle.isSelected()){
            excludedSet.remove(selected);
            for (int i = selected-1; i >= 0 && index < 0; i--){
                index = imagePreviewModel.get(i).getIconIndex();
            }
            index++;
            diff = 1;
        }
        else{
            excludedSet.add(selected);
            diff = -1;
        }
        for (ICOImage img : icons.get(selected)){
            img.setIconIndex(index);
        }
        for (int i = selected+1; i < icons.size(); i++){
            for (ICOImage img : icons.get(i)){
                int j = img.getIconIndex();
                if (j >= 0)
                    img.setIconIndex(Math.max(j+diff,0));
            }
        }
        previewComboBox.repaint();
    }//GEN-LAST:event_includeToggleActionPerformed

    private void scaleOverrideComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaleOverrideComboActionPerformed
        int selected = previewComboBox.getSelectedIndex();
        int scale = scaleOverrideCombo.getSelectedIndex() - 1;
        int setScale = scaleSettings.getOrDefault(selected, -1);
        if (scale == setScale)
            return;
        if (scale < 0)
            scaleSettings.remove(selected);
        else
            scaleSettings.put(selected, scale);
        imagePreviewModel.set(selected, getIconImage(selected));
        previewComboBox.setSelectedIndex(selected);
    }//GEN-LAST:event_scaleOverrideComboActionPerformed
    
    private File openSaveFileChooser(){
        int option = saveFC.showSaveDialog(this);
        saveFC.setPreferredSize(saveFC.getSize());
        if (config != null){
            setPreferenceSize(SAVE_FILE_CHOOSER_PREFERENCE_NODE,saveFC.getSize());
            config.put(SAVE_FILE_CHOOSER_DIRECTORY_KEY, 
                    saveFC.getCurrentDirectory().toString());
        }   // If the user wants to save the file
        if (option == JFileChooser.APPROVE_OPTION)
            return saveFC.getSelectedFile();
        else
            return null;
    }
    
    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        File file;
        do{
            file = openSaveFileChooser();
            if (file == null)
                return;
            if (ICON_FILTER.equals(saveFC.getFileFilter()) && !ICON_FILTER.accept(file)){
                file = new File(file.toString()+"."+ICO);
                saveFC.setSelectedFile(file);
            }
            if (file.exists()){
                int option = JOptionPane.showConfirmDialog(this, 
                        "File already exists! Overwrite it?", 
                        "File Already Exists", 
                        JOptionPane.YES_NO_CANCEL_OPTION, 
                        JOptionPane.WARNING_MESSAGE);
                switch(option){
                    case(JOptionPane.NO_OPTION):
                        file = null;
                    case(JOptionPane.YES_OPTION):
                        break;
                    default:
                        return;
                }
            }
        } while (file == null);
        saver = new SaveIconImages(file,imagePreviewModel);
        saver.execute();
    }//GEN-LAST:event_saveButtonActionPerformed
    /**
     * This is an action performed by all the file choosers when the user 
     * approves of the selected file(s).
     * @param evt The ActionEvent.
     */
    private void fileChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileChooserActionPerformed
            // Get the file chooser that was used
        JFileChooser fc = (JFileChooser) evt.getSource();
            // If the user approved of the selection
        if (JFileChooser.APPROVE_SELECTION.equals(evt.getActionCommand())){
            File file = fc.getSelectedFile();           // Get the selected file
            if (file.toString().contains("\"")){        // If the file has quotation marks
                file = FilesExtended.removeQuotations(file);
                fc.setSelectedFile(file);
            }
        }
    }//GEN-LAST:event_fileChooserActionPerformed

    private void showPreviewBorderToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPreviewBorderToggleActionPerformed
        previewLabel.setThumbnailBorder(getPreviewBorder());
        updateConfigBoolean(SHOW_PREVIEW_BORDER_KEY, showPreviewBorderToggle);
    }//GEN-LAST:event_showPreviewBorderToggleActionPerformed

    private void scaleImageAlwaysToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaleImageAlwaysToggleActionPerformed
        previewLabel.setImageAlwaysScaled(scaleImageAlwaysToggle.isSelected());
        updateConfigBoolean(SCALE_IMAGE_PREVIEW_KEY, scaleImageAlwaysToggle);
    }//GEN-LAST:event_scaleImageAlwaysToggleActionPerformed
    /**
     * This returns whether this window is maximized in either the horizontal or 
     * vertical directions.
     * @return Whether this window is horizontally or vertically maximized.
     */
    private boolean isMaximized(){
        return (getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0;
    }
    /**
     * This updates the stored size when the window is resized.
     * @param evt The ComponentEvent to be processed.
     */
    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
            // If the window is not maximized
        if (!isMaximized()){
            try{    // Set the stored size in the preference node
                setPreferenceSize(null,getSize());
            }catch (IllegalStateException ex){ 
                if (isInDebug())    // If we are in debug mode
                    System.out.println("Error: " + ex);
            }
        }
    }//GEN-LAST:event_formComponentResized

    private void openFCPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_openFCPropertyChange
//        System.out.println(evt);
        if (config != null && 
                JFileChooser.FILE_FILTER_CHANGED_PROPERTY.equals(evt.getPropertyName())){
            FileFilter filter = openFC.getFileFilter();
            int index = -1;
            FileFilter[] filters = openFC.getChoosableFileFilters();
            for (int i = 0; i < filters.length && index < 0; i++){
                if (Objects.equals(filter, filters[i]))
                    index = i;
            }
            if (index > 0)
                config.putInt(OPEN_FILE_CHOOSER_FILE_FILTER_KEY, index);
        }
    }//GEN-LAST:event_openFCPropertyChange
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the System Look and Feel look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If System is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Iconifier.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new Iconifier(DebugCapable.checkForDebugArgument(args)).setVisible(true);
        });
    }
    
    private void setPreviewImage(Image image){
        if (useDebugIconToggle.isSelected() && image != null){
            if (debugIcon == null || !(debugIcon.getIcon() instanceof ImageIcon) 
                    || !((ImageIcon)debugIcon.getIcon()).getImage().equals(image))
                debugIcon = new DebuggingIcon(new ImageIcon(image),
                        showDebugToggle.isSelected());
            previewLabel.setIcon(debugIcon);
        } else {
            previewLabel.setImage(image);
        }
    }
    
    private void setPreviewImage(){
        int selected = previewComboBox.getSelectedIndex();
        ICOImage img = (ICOImage) imagePreviewModel.getSelectedItem();
        setPreviewImage((img != null) ? img.getImage() : null);
        pngCheckBox.setSelected((img != null) ? img.isPngCompressed() : false);
        includeToggle.setSelected((img != null) ? img.getIconIndex() >= 0 : false);
        scaleOverrideCombo.setSelectedIndex(scaleSettings.getOrDefault(selected, -1)+1);
    }
    
    private ICOImage getIconImage(int index, int scale, int format){
        return icons.get(index).get((scaleSettings.getOrDefault(index,scale)*
                (LAST_FORMAT_IMAGE_SETTING+1))+format);
    }
    
    private ICOImage getIconImage(int index){
        return getIconImage(index,scaleCombo.getSelectedIndex(),formatImageCombo.getSelectedIndex());
    }
    
    private void populateImagePreviews(){
        int selected = imagePreviewModel.indexOf(imagePreviewModel.getSelectedItem());
        imagePreviewModel.clear();
        if (icons == null)
            return;
        int scale = scaleCombo.getSelectedIndex();
        int format = formatImageCombo.getSelectedIndex();
        for (int i = 0; i < icons.size(); i++){
            imagePreviewModel.add(getIconImage(i,scale,format));
        }
        if (selected < 0)
            selected = imagePreviewModel.size()-1;
        imagePreviewModel.setSelectedItem(imagePreviewModel.get(selected));
    }
    
    private BufferedImage sourceImage = null;
    private DebuggingIcon debugIcon = null;
    private ComboBoxModelList<ICOImage> imagePreviewModel;
    /**
     * First list is the icon index, second list is the scale setting, third 
     * list is image formatting.
     */
    private ArrayList<ArrayList<ICOImage>> icons = null;
    private Map<Integer, Integer> scaleSettings = new TreeMap<>();
    private Set<Integer> excludedSet = new TreeSet<>();
    private Set<Integer> compressedSet = new TreeSet<>();
    private Border[] previewBorders = null;
    private Preferences config;
    private boolean active = true;
    private final boolean debugMode;
    private GenerateImages imgGen = null;
    private SaveIconImages saver = null;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBoxMenuItem activeTestToggle;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JMenu debugMenu;
    private javax.swing.JMenu fileMenu;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.JComboBox<String> formatImageCombo;
    private javax.swing.JCheckBox includeToggle;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JButton openButton;
    private javax.swing.JFileChooser openFC;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenu optionsMenu;
    private javax.swing.JCheckBox pngCheckBox;
    private javax.swing.JComboBox<ICOImage> previewComboBox;
    private components.JThumbnailLabel previewLabel;
    private components.JFileDisplayPanel previewPanel;
    private javax.swing.JMenuItem printTestButton;
    private javax.swing.JProgressBar progressBar;
    private components.progress.JProgressDisplayMenu progressDisplay;
    private javax.swing.JButton saveButton;
    private javax.swing.JFileChooser saveFC;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JComboBox<String> scaleCombo;
    private javax.swing.JCheckBoxMenuItem scaleImageAlwaysToggle;
    private javax.swing.JComboBox<String> scaleOverrideCombo;
    private javax.swing.JButton settingsButton;
    private javax.swing.JMenuItem settingsMenuItem;
    private javax.swing.JCheckBoxMenuItem showDebugToggle;
    private javax.swing.JCheckBoxMenuItem showPreviewBorderToggle;
    private components.debug.SlowTestMenuItem slowTestToggle;
    private javax.swing.JCheckBoxMenuItem useDebugIconToggle;
    // End of variables declaration//GEN-END:variables
    @Override
    public void setInputEnabled(boolean enabled) {
        active = enabled;
        activeTestToggle.setSelected(enabled);
        previewComboBox.setEnabled(sourceImage != null && enabled);
        formatImageCombo.setEnabled(sourceImage != null && enabled && 
                sourceImage.getWidth() != sourceImage.getHeight());
        scaleCombo.setEnabled(sourceImage != null && enabled);
        openButton.setEnabled(enabled);
        pngCheckBox.setEnabled(sourceImage != null && enabled);
        includeToggle.setEnabled(sourceImage != null && enabled);
        scaleOverrideCombo.setEnabled(sourceImage != null && enabled);
        saveButton.setEnabled(sourceImage != null && enabled);
//        settingsButton.setEnabled(sourceImage != null && enabled);
        settingsMenuItem.setEnabled(settingsButton.isEnabled());
        openMenuItem.setEnabled(openButton.isEnabled());
        saveMenuItem.setEnabled(saveButton.isEnabled());
    }
    @Override
    public boolean isInputEnabled() {
        return active;
    }
    @Override
    public JProgressBar getProgressBar() {
        return progressBar;
    }
    @Override
    public JProgressDisplayMenu getProgressDisplayMenu() {
        return progressDisplay;
    }
    @Override
    public void useWaitCursor(boolean isWaiting) {
        setCursor((isWaiting)?Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR):null);
    }
    @Override
    public boolean isInDebug() {
        return debugMode;
    }
    
    private void incrementProgress(){
        progressBar.setValue(progressBar.getValue()+1);
        slowTestToggle.runSlowTest();
    }
    
    private BufferedImage scaleImage(BufferedImage image,int x,int y,int w,int h,
            int width,int height,int interpolation){
        if (image == null)
            return null;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Image drawn = image;
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, 
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, 
                RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, 
                RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        switch(interpolation){
            case(SCALE_IMAGE_SETTING_SMOOTH):
                drawn = image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                break;
            case(SCALE_IMAGE_SETTING_THUMBNAILATOR):
                try {
                    drawn = Thumbnails.of(image).size(w, h).asBufferedImage();
                    break;
                } catch (IOException ex) {
                    if (isInDebug())
                        System.out.println("Thumbnailator Error: " + ex);
                    drawn = image;
                }
            default:
                Object interValue;
                switch(interpolation){
                    case(SCALE_IMAGE_SETTING_NEAREST_NEIGHBOR):
                        interValue = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
                        break;
                    case(SCALE_IMAGE_SETTING_BILINEAR):
                        interValue = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
                        break;
                    case(SCALE_IMAGE_SETTING_BICUBIC):
                    default:
                        interValue = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
                }
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interValue);
        }
        g.drawImage(drawn, x, y, w, h, null);
        g.dispose();
        return img;
    }
    
    private BufferedImage scaleImage(BufferedImage image,int width,int height,
            int interpolation){
        return scaleImage(image,0,0,width,height,width,height,interpolation);
    }
    
    private BufferedImage processImage(BufferedImage image, int width, int height, 
            int format, int interpolation){
        if (image == null)
            return null;
        int w = image.getWidth();
        int h = image.getHeight();
        if (w == width && h == height){
            if (image.getType() == BufferedImage.TYPE_INT_ARGB)
                return image;
            else
                return scaleImage(image,width,height,interpolation);
        }
        int x, y;
        double ratio = ((double) Math.min(w, h)) / Math.max(w, h);
        int size = Math.min(w, h);
        switch (format){
            case(FORMAT_IMAGE_SETTING_SCALED):
                return scaleImage(image,width,height,interpolation);
            case(FORMAT_IMAGE_SETTING_CROP_DOWN_RIGHT):
                x = w - size;
                y = h - size;
                break;
            case(FORMAT_IMAGE_SETTING_CROP_CENTER):
                x = Math.floorDiv(w - size, 2);
                y = Math.floorDiv(h - size, 2);
                break;
            default:
                if (w > h){
                    h = Math.min((int)Math.ceil(ratio*height),height);
                    w = width;
                } else if (h > w){
                    w = Math.min((int)Math.ceil(ratio*width),width);
                    h = height;
                } else{
                    w = width;
                    h = height;
                }
            case(FORMAT_IMAGE_SETTING_CROP_UP_LEFT):
                x = y = 0;
        }
        switch (format){
            case(FORMAT_IMAGE_SETTING_CROP_UP_LEFT):
            case(FORMAT_IMAGE_SETTING_CROP_DOWN_RIGHT):
            case(FORMAT_IMAGE_SETTING_CROP_CENTER):
                image = image.getSubimage(x, y, size, size);
                w = width;
                h = height;
            case(FORMAT_IMAGE_SETTING_UP_LEFT):
                x = y = 0;
                break;
            case(FORMAT_IMAGE_SETTING_DOWN_RIGHT):
                x = width - w;
                y = height - h;
                break;
            case(FORMAT_IMAGE_SETTING_CENTERED):
            default:
                x = Math.floorDiv(width - w, 2);
                y = Math.floorDiv(height - h, 2);
                break;
        }
//        System.out.printf("%3d %3d: (%3d, %3d) %3d x %3d (%3d x %3d) (%3d x %3d, %2.5f)%n",
//                format,interpolation,x,y,w,h,width,height,image.getWidth(),image.getHeight(),ratio);
        return scaleImage(image,x, y, w, h,width,height, interpolation);
    }
    
    private BufferedImage convertColorDepth(BufferedImage image, int bpp){
        if (bpp == 24)
            return ConvertUtil.convert24(image);
        // For bit depths less than 32, determine an alpha color and then swap 
        // that color out for transparancy. This would perferably be a color 
        // that is unused in the image. Additionally, for bit depths less than 
        // 24, perhaps this should generate a best fitting color map. 
        // Also, the alpha color and color map should be customizable by the user
        return ICOEncoder.convert(image, bpp);
    }
    
    private class GenerateImages extends SwingWorker<Void, Void>{
        
        private final File file;
        
        private BufferedImage image;
        
        private final ArrayList<ArrayList<ICOImage>> newIcons = new ArrayList<>();
        
        private IOException fileExc = null;
        
        private Exception exc = null;
        
        private volatile boolean loading = false;
        
        private volatile boolean success = false;
        
        GenerateImages(File file){
            this.file = Objects.requireNonNull(file);
            image = null;
        }
        
        GenerateImages(BufferedImage image){
            this.image = Objects.requireNonNull(image);
            file = null;
        }
        
        public synchronized boolean isLoading(){
            return loading;
        }
        
        public synchronized boolean isSuccessful(){
            return success;
        }
        @Override
        protected Void doInBackground() throws Exception {
            setInputEnabled(false);
            progressBar.setValue(0);
            progressBar.setStringPainted(true);
            loading = true;
            if (file != null){
                progressBar.setIndeterminate(true);
                progressDisplay.setString("Loading Image From File");
                try {
                    image = ImageIO.read(file);
                    if (image.getType() != BufferedImage.TYPE_INT_ARGB){
                        BufferedImage temp = image;
                        image = new BufferedImage(temp.getWidth(),temp.getHeight(),
                                BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g = image.createGraphics();
                        g.drawImage(temp, 0, 0, null);
                        g.dispose();
                    }
                } catch (IOException ex) {
                    fileExc = ex;
                    return null;
                }
            }
            progressDisplay.setString("Generating Icon Images");
            progressBar.setMaximum(ICONS_GENERATED_COUNT);
            progressBar.setIndeterminate(false);
            
            ArrayList<BufferedImage> formatImages = new ArrayList<>();
            int w = image.getWidth();
            int h = image.getHeight();
            boolean tooLarge = w >= AUTO_SHRINK_SIZE.width && h >= AUTO_SHRINK_SIZE.height;
            if (tooLarge){
                double ratio = ((double)Math.min(w, h)) / Math.max(w, h);
                if (w > h){
                    h = Math.min((int)Math.ceil(ratio*AUTO_SHRINK_SIZE.height),AUTO_SHRINK_SIZE.height);
                    w = AUTO_SHRINK_SIZE.width;
                } else if (h > w){
                    w = Math.min((int)Math.ceil(ratio*AUTO_SHRINK_SIZE.width),AUTO_SHRINK_SIZE.width);
                    h = AUTO_SHRINK_SIZE.height;
                } else{
                    w = AUTO_SHRINK_SIZE.width;
                    h = AUTO_SHRINK_SIZE.height;
                }
            }
            
            for (int s = FIRST_SCALE_IMAGE_SETTING; 
                    s <= LAST_SCALE_IMAGE_SETTING; s++){
                if (tooLarge){
                    formatImages.add(scaleImage(image,w,h,s));
                } else
                    formatImages.add(image);
                incrementProgress();
            }
            
            try{
                ArrayList<ArrayList<BufferedImage>> images = new ArrayList<>();

                for (Dimension dim : DEFAULT_ICON_DIMENSIONS) {
                    ArrayList<BufferedImage> temp = new ArrayList<>();
                    for (int s = FIRST_SCALE_IMAGE_SETTING; 
                            s <= LAST_SCALE_IMAGE_SETTING; s++){
                        BufferedImage img = formatImages.get(s);
                        for (int f = FIRST_FORMAT_IMAGE_SETTING; 
                                f <= LAST_FORMAT_IMAGE_SETTING; f++){
                            temp.add(processImage(img,dim.width,dim.height,f,s));
                            incrementProgress();
                        }
                    }
                    images.add(temp);
                }

    //            int iconIndex = 0;
                for (int d : DEFAULT_BITS_PER_PIXEL){
                    for (ArrayList<BufferedImage> imgArr : images){
                        int index = newIcons.size();
    //                    boolean included = !excludedSet.contains(index);
                        ArrayList<ICOImage> temp = new ArrayList<>();
                        for (BufferedImage img : imgArr){
                            ICOImage icon = createICOImage(convertColorDepth(img,d));
                            icon.setIconIndex(index);
    //                        if (included)
    //                            icon.setIconIndex(iconIndex);
                            icon.setPngCompressed(DEFAULT_AUTO_COMPRESSED_INDEXES
                                    .contains(index));
                            temp.add(icon);
                            incrementProgress();
                        }
                        newIcons.add(temp);
    //                    if (included)
    //                        iconIndex++;
                    }
                }
                success = true;
            } catch (Exception ex){
                exc = ex;
            }
            return null;
        }
        @Override
        protected void done(){
            loading = false;
            if (!success){
                if (fileExc != null){
                    if (isInDebug())
                        System.out.println("File Error: " + fileExc);
                    JOptionPane.showMessageDialog(Iconifier.this, 
                            "An error occurred while attempting to load the image from the file.\n\nError: "+fileExc, 
                            "Error Loading Image", JOptionPane.ERROR_MESSAGE);
                } else if (exc != null){
                    if (isInDebug())
                        System.out.println("Generation Error: " + exc);
                    JOptionPane.showMessageDialog(Iconifier.this, 
                            "An error occurred while attempting generate the icon images.\n\nError: "+exc, 
                            "Error Generating Images", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(Iconifier.this, 
                            "An unknown error occurred while attempting to load the image.",
                            "Error Loading Images", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                sourceImage = image;
                icons = newIcons;
                compressedSet.clear();
                compressedSet.addAll(DEFAULT_AUTO_COMPRESSED_INDEXES);
                excludedSet.clear();
                scaleSettings.clear();
                imagePreviewModel.setSelectedItem(null);
                populateImagePreviews();
            }
            progressBar.setIndeterminate(false);
            progressBar.setStringPainted(false);
            progressBar.setValue(0);
            setInputEnabled(true);
        }
    }
    /**
     * This attempts to create the given directories, opening an Error 
     * JOptionPane if failed, and returns whether it was successful.
     * @param dir The directory to create.
     * @param existingMessage The message to display if the given directory 
     * exists as a file.
     * @param errorMessage The message to display if an error occurs.
     * @return Whether this was successful at creating the directories.
     */
    private boolean createDirectories(File dir, String existingMessage, 
            String errorMessage) {
        String message;     // The message to display
        try {
            Files.createDirectories(dir.toPath());
            return true;
        }
        catch(FileAlreadyExistsException exc) {
            message = existingMessage+" already exists as a file.";
        }
        catch (IOException | SecurityException exc) {
            message = "An error occurred while creating the "+errorMessage;
        }
        beep();
        JOptionPane.showMessageDialog(this,message,
                "ERROR - Error Creating Directory",
            JOptionPane.ERROR_MESSAGE);
        return false;
    }
    /**
     * This attempts to create the given directories, opening an Error 
     * JOptionPane if failed, and returns whether it was successful.
     * @param dir The directory to create.
     * @param multiple If there will be multiple directories created.
     * @return Whether this was successful at creating the directories.
     */
    private boolean createDirectories(File dir, boolean multiple) {
        if (multiple){  // If multiple directories will be created
            return createDirectories(dir,"One of the directories",
                    "directories.");
        } else{
            return createDirectories(dir,"The specified directory",
                    "the specified directory.");
        }
    }
    /**
     * This attempts to create the given directory.
     * @param dir The directory to create.
     * @return Whether the directory was successfully created.
     */
    private boolean createDirectories(File dir){
            // If the directory file does not exist
        if (!dir.exists() || !dir.isDirectory()) {
            if (!createDirectories(dir,true))   // If the directory was not created
                return false;
        }
        return true;
    }
    
    private class SaveIconImages extends SwingWorker<Void, Void>{
        
        private File file;
        
        private List<ICOImage> icons;
        
        private IOException exc = null;
        
        private volatile boolean saving = false;
        
        private volatile boolean success = false;
        
        SaveIconImages(File file, List<ICOImage> icons){
            this.file = file;
            this.icons = icons;
        }
        
        public synchronized boolean isSaving(){
            return saving;
        }
        
        public synchronized boolean isSuccessful(){
            return success;
        }
        @Override
        protected Void doInBackground() throws Exception {
            saving = true;
            setInputEnabled(false);
            progressBar.setValue(0);
            progressBar.setStringPainted(true);
            progressDisplay.setString("Saving Icon Images");
            progressBar.setIndeterminate(true);
            if (!createDirectories(file.getParentFile()))
                return null;
            icons = new ArrayList<>(icons);
            icons.removeIf((ICOImage t) -> t == null || t.getIconIndex() < 0);
            progressBar.setMaximum(icons.size());
            ArrayList<BufferedImage> images = new ArrayList<>();
            int[] bpp = new int[icons.size()];
            boolean[] compress = new boolean[icons.size()];
            boolean useBPP = false;
            progressBar.setIndeterminate(false);
            for (ICOImage icon : icons){
                int index = images.size();
                BufferedImage img = icon.getImage();
                images.add(img);
                int p = -1;
                if (img.getColorModel().getPixelSize() != icon.getColourDepth()){
                    p = icon.getColourDepth();
                    useBPP = true;
                }
                bpp[index] = p;
                compress[index] = icon.isPngCompressed();
                incrementProgress();
            }
            progressBar.setIndeterminate(true);
            try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))){
                ICOEncoder.write(images, (useBPP) ? bpp : null, compress, out);
                success = true;
            } catch (IOException ex){
                exc = ex;
            }
            return null;
        }
        @Override
        protected void done(){
            saving = false;
            if (!success){
                if (exc != null){
                    if (isInDebug())
                        System.out.println("File Error: " + exc);
                    JOptionPane.showMessageDialog(Iconifier.this, 
                            "An error occurred while attempting to save the icon images to the file.\n\nError: "+exc, 
                            "Error Saving Image", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(Iconifier.this, 
                            "The icon images failed to save.", 
                            "Error Saving Image", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(Iconifier.this, 
                        "The icon images were successfully saved.", 
                        "Images Saved Successfully", JOptionPane.INFORMATION_MESSAGE);
            }
            progressBar.setIndeterminate(false);
            progressBar.setStringPainted(false);
            progressBar.setValue(0);
            setInputEnabled(true);
        }
    }
}
