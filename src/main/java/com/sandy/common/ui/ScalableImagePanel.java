package com.sandy.common.ui;

import java.awt.BorderLayout ;
import java.awt.Color ;
import java.awt.Point ;
import java.awt.Rectangle ;
import java.awt.geom.AffineTransform ;
import java.awt.image.AffineTransformOp ;
import java.awt.image.BufferedImage ;
import java.awt.image.RasterFormatException ;
import java.io.File ;
import java.io.IOException ;
import java.util.ArrayList ;

import javax.imageio.ImageIO ;
import javax.swing.JLabel ;
import javax.swing.JPanel ;
import javax.swing.JScrollPane ;
import javax.swing.JSlider ;
import javax.swing.SwingConstants ;
import javax.swing.event.ChangeEvent ;
import javax.swing.event.ChangeListener ;

import org.apache.log4j.Logger ;

public class ScalableImagePanel extends JPanel implements ChangeListener {
    
    public interface ScalableImagePanelListener {
        public void subImageSelected( BufferedImage image, int selectionModifier ) ;
        public void subImageBoundResized( Point anchor, Point hook ) ;
    }

    private static final long serialVersionUID = 1L ;
    private static final double MAX_SCALE = 2 ;
    static final Logger logger = Logger.getLogger( ScalableImagePanel.class ) ;
    
    private DrawingCanvas  imgLabel ;
    private JSlider slider = null ;
    
    private BufferedImage curImg = null ;
    private File curImgFile = null ;
    
    private double scaleFactor = 1.0 ;
    
    private ArrayList<ScalableImagePanelListener> listeners = 
               new ArrayList<ScalableImagePanel.ScalableImagePanelListener>() ;

    public ScalableImagePanel() {
        super( new BorderLayout() ) ;
        setUpUI() ;
    }
    
    public void addListener( ScalableImagePanelListener listener ) {
        listeners.add( listener ) ;
    }
    
    public void removeListener( ScalableImagePanelListener listener ) {
        listeners.remove( listener ) ;
    }
    
    void subImageSelected( Rect selRect, int selectionModifier ) {
        Rectangle rect = selRect.getBounds() ;
        try {
            BufferedImage subImage = curImg.getSubimage( rect.x, rect.y, rect.width, rect.height ) ;
            for( ScalableImagePanelListener l : listeners ) {
                l.subImageSelected( subImage, selectionModifier );
            }
        }
        catch( RasterFormatException e ) {
            // Ignore
        }
    }
    
    public void subImageBoundResized( Point dragAnchor, Point curPt ) {
        try {
            for( ScalableImagePanelListener l : listeners ) {
                l.subImageBoundResized( dragAnchor, curPt );
            }
        }
        catch( RasterFormatException e ) {
            // Ignore
        }
    }
    
    private void setUpUI() {
        
        imgLabel = new DrawingCanvas( this ) ;
        imgLabel.setOpaque(true);
        imgLabel.setBackground( new Color(240, 240, 240) ) ;
        imgLabel.setHorizontalTextPosition( JLabel.LEFT ) ;
        imgLabel.setHorizontalAlignment( SwingConstants.CENTER ) ;
        
        JScrollPane displaySP = new JScrollPane( imgLabel ) ;
        displaySP.setBackground( Color.WHITE ) ;
        displaySP.getVerticalScrollBar().setUnitIncrement( 10 ) ;
        
        slider = new JSlider( JSlider.VERTICAL ) ;
        slider.addChangeListener( this ) ;
        
        add( slider, BorderLayout.WEST ) ;
        add( displaySP, BorderLayout.CENTER ) ;
    }
    
    public void setImage( File pngFile ) {
        
        try {
            BufferedImage img = ImageIO.read( pngFile ) ;
            curImgFile = pngFile ;
            curImg = img ;
            refreshImage() ;
        }
        catch( IOException e ) {
            e.printStackTrace();
        }
    }
    
    public File getCurImgFile() {
        return this.curImgFile ;
    }
    
    private void refreshImage() {
        if( curImg != null ) {
            BufferedImage scaledImg = curImg ;
            if( scaleFactor != 1.0 ) {
                scaledImg = getScaledImage() ;
            }
            imgLabel.setImage( scaledImg, scaleFactor );
        }
    }

    public void stateChanged( ChangeEvent c ) {
        
        int val = slider.getValue() ;
        double delta = Math.abs( val - 50 ) ;
        scaleFactor = 1 + ((MAX_SCALE/50)*delta) ;
        if( val < 50 ) {
            scaleFactor = 1/scaleFactor ;
        }
        refreshImage() ;
    }
    
    public void zoom( boolean zoomIn ) {
        
        int val = slider.getValue() ;
        int delta = (int)(Math.abs( val )*0.1) ;
        if( zoomIn ) {
            int newVal = val + delta ;
            if( newVal < slider.getMaximum() ) {
                slider.setValue( newVal ) ;
            }
        }
        else {
            int newVal = val - delta ;
            if( newVal > slider.getMinimum() ) {
                slider.setValue( newVal ) ;
            }
        }
    }
    
    public void setToolTipText( String text ) {
        this.imgLabel.setToolTipText( text ) ;
    }
    
    private BufferedImage getScaledImage() {
        
        BufferedImage scaledImg = null ;
        
        int w = (int)(curImg.getWidth()*scaleFactor) ;
        int h = (int)(curImg.getHeight()*scaleFactor) ;
        
        scaledImg = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB ) ;
        AffineTransform at = new AffineTransform() ;
        at.scale( this.scaleFactor, this.scaleFactor ) ;
        AffineTransformOp scaleOp = new AffineTransformOp( at, 
                                              AffineTransformOp.TYPE_BICUBIC ) ;
        scaledImg = scaleOp.filter( curImg, scaledImg ) ;
        
        return scaledImg ;
    }
}
