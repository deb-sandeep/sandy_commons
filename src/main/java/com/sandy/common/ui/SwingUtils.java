package com.sandy.common.ui;

import static com.sandy.common.util.ReflectionUtil.getResource ;

import java.awt.Dimension ;
import java.awt.Image ;
import java.awt.Toolkit ;
import java.awt.image.BufferedImage ;
import java.io.IOException ;
import java.net.URL ;

import javax.imageio.ImageIO ;
import javax.swing.ImageIcon ;
import javax.swing.JFrame ;
import javax.swing.UIManager ;
import javax.swing.UIManager.LookAndFeelInfo ;

public class SwingUtils {

    public static void setMaximized( JFrame frame ) {
        Dimension screenSz = Toolkit.getDefaultToolkit().getScreenSize() ; 
        frame.setBounds( 0, 0, screenSz.width, screenSz.height ) ;
    }
    
    public static int getScreenWidth() {
        Dimension screenSz = Toolkit.getDefaultToolkit().getScreenSize() ; 
        return screenSz.width ;
    }
    
    public static int getScreenHeight() {
        Dimension screenSz = Toolkit.getDefaultToolkit().getScreenSize() ; 
        return screenSz.height ;
    }
    
    public static ImageIcon getIcon( Class<?> mainClass, String iconName ) {
        URL url = getResource( mainClass, "/icons/" + iconName + ".png" ) ;
        Image image = Toolkit.getDefaultToolkit().getImage( url ) ;
        return new ImageIcon( image ) ;
    }
    
    public static BufferedImage getIconImage( Class<?> mainClass, String iconName ) {
        BufferedImage img = null ;
        try {
            URL url = getResource( mainClass, "/icons/" + iconName + ".png" ) ;
            img = ImageIO.read( url ) ;
        }
        catch( IOException e ) {
            throw new RuntimeException( e ) ;
        }
        return img ;
    }
    
    public static void setNimbusLookAndFeel() {
        try {
            for( LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
                if( "Nimbus".equals(info.getName() ) ) {
                    UIManager.setLookAndFeel( info.getClassName() ) ;
                    break;
                }
            }
        } 
        catch( Exception e ) {
        }
    }
}
