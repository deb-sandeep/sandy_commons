package com.sandy.common.ui;

import java.awt.Color ;
import java.awt.Dimension ;
import java.awt.Graphics ;
import java.awt.Point ;
import java.awt.event.MouseAdapter ;
import java.awt.event.MouseEvent ;
import java.awt.event.MouseMotionListener ;
import java.awt.image.BufferedImage ;
import java.util.ArrayList ;

import javax.swing.JLabel ;

import org.apache.log4j.Logger ;

public class DrawingCanvas extends JLabel {
    
    private static final long serialVersionUID = -5864412396440506797L;
    private static final Logger log = Logger.getLogger( DrawingCanvas.class ) ;
    
    private static final int DRAG_NONE   = 0 ;
    private static final int DRAG_CREATE = 1 ;
    private static final int DRAG_RESIZE = 2 ;
    private static final int DRAG_MOVE   = 3 ;
    
    public static final int MARK_END_MODIFIER_LEFT_BTN   = 1 ;
    public static final int MARK_END_MODIFIER_CENTER_BTN = 2 ;
    public static final int MARK_END_MODIFIER_RIGHT_BTN  = 3 ;
    
    protected ArrayList<Rect> allShapes ; 
    protected Rect selectedShape ; 
    
    private BufferedImage scaledImg   = null ;
    private double scaleFactor = 1.0f ;
    private int currentMode = DRAG_CREATE ;
    private ScalableImagePanel parent = null ;
    
    public DrawingCanvas( ScalableImagePanel parent ) {
        
        this.allShapes     = new ArrayList<Rect>() ;
        this.selectedShape = null ;
        this.parent        = parent ;
        
        setBackground( Color.white ) ;
        
        CanvasMouseHandler2 handler = new CanvasMouseHandler2() ;
        addMouseListener( handler ) ;
        addMouseMotionListener( handler ) ;
    }
    
    public void setCurrentMode( int mode ) {
        this.currentMode = mode ;
    }

    public void setImage( BufferedImage newImage, double scaleFactor ) {
        this.scaledImg = newImage ;
        this.scaleFactor = scaleFactor ;
        super.setPreferredSize( new Dimension( newImage.getWidth(), 
                                               newImage.getHeight() ) );
        super.repaint() ;
    }
    
    public void paintComponent( Graphics g ){
        
        super.paintComponent( g ) ;
        g.drawImage( this.scaledImg, 0, 0, null ) ;
        for( Rect rect : allShapes ) {
            rect.draw( g, g.getClipBounds() ) ;
        }
    }

    protected void setSelectedShape( Rect shapeToSelect ) {
        
        if( selectedShape != shapeToSelect ){ 
            if( selectedShape != null ) {
                selectedShape.setSelected( false ) ;
            }
            selectedShape = shapeToSelect ; 
            if( selectedShape != null ){
                shapeToSelect.setSelected( true ) ;
            }
        }
    }

    protected Rect shapeContainingPoint( Point pt ) {
        for( Rect r : allShapes ) {
            if( r.inside( pt ) ) {
                return r ;
            }
        }
        return null ;
    }

    public void delete() {
        
        if( selectedShape != null ) {
            for( int i = allShapes.size() - 1 ; i >= 0 ; i--) {
                Rect r = (Rect) allShapes.get(i) ;
                if( r == selectedShape) {
                    allShapes.remove(i) ;
                }
            }
            
            selectedShape = null ;
            super.repaint() ;
        }
    }
    
    public double getScaleFactor() {
        return this.scaleFactor ;
    }
    
    // -------------------------------------------------------------------------
    protected class CanvasMouseHandler 
        extends MouseAdapter implements MouseMotionListener {
        
        Point dragAnchor ; 
        int dragStatus ;

        public void mousePressed( MouseEvent event ) {
            
            Rect clicked = null ;
            Point curPt = event.getPoint() ;

            if( currentMode != DRAG_CREATE ) {
                if( selectedShape != null && 
                    ( dragAnchor = selectedShape.getAnchorForResize( curPt ) ) != null) {
                    dragStatus = DRAG_RESIZE ; // drag will resize this shape
                } 
                else if( (clicked = shapeContainingPoint(curPt)) != null) { 
                    setSelectedShape( clicked ) ;
                    dragStatus = DRAG_MOVE ; 
                    dragAnchor = curPt ;
                } 
                else { 
                    setSelectedShape( null ) ;
                    dragStatus = DRAG_NONE ;
                }
            } 
            else {
                Rect newShape = new Rect( curPt, DrawingCanvas.this ) ; 
                allShapes.add( newShape ) ;
                setSelectedShape( newShape ) ;
                dragStatus = DRAG_CREATE ; 
                dragAnchor = curPt ;
            }
        }

        public void mouseDragged( MouseEvent event ) {
            
            Point curPt = event.getPoint() ;

            switch (dragStatus) {
                case DRAG_MOVE:
                    selectedShape.translate( curPt.x - dragAnchor.x, 
                                             curPt.y - dragAnchor.y) ;
                    dragAnchor = curPt ; 
                    break ;
                    
                case DRAG_CREATE:
                case DRAG_RESIZE:
                    selectedShape.resize( dragAnchor, curPt ) ;
                    break ;
            }
        }

        @Override
        public void mouseReleased( MouseEvent e ) {
            try {
                if( selectedShape != null ) {
                    parent.subImageSelected( selectedShape, e.getButton() ) ;
                    delete() ;
                }
            }
            catch( Exception e1 ) {
                log.error( "Raster exception.", e1 ) ;
            }
        }
    }

    protected class CanvasMouseHandler2 
        extends MouseAdapter implements MouseMotionListener {
        
        Point dragAnchor ; 
        int dragStatus ;
        
        boolean inMarkMode = false ;
    
        public void mousePressed( MouseEvent event ) {

            if( !inMarkMode ) {
                inMarkMode = true ;
                markStarted( event );
            }
            else {
                inMarkMode = false ;
                markEnded( event ) ;
            }
        }
        
        private void markStarted( MouseEvent event ) {
            
            Rect clicked = null ;
            Point curPt = event.getPoint() ;
    
            if( currentMode != DRAG_CREATE ) {
                if( selectedShape != null && 
                    ( dragAnchor = selectedShape.getAnchorForResize( curPt ) ) != null) {
                    dragStatus = DRAG_RESIZE ; // drag will resize this shape
                } 
                else if( (clicked = shapeContainingPoint(curPt)) != null) { 
                    setSelectedShape( clicked ) ;
                    dragStatus = DRAG_MOVE ; 
                    dragAnchor = curPt ;
                } 
                else { 
                    setSelectedShape( null ) ;
                    dragStatus = DRAG_NONE ;
                }
            } 
            else {
                Rect newShape = new Rect( curPt, DrawingCanvas.this ) ; 
                allShapes.add( newShape ) ;
                setSelectedShape( newShape ) ;
                dragStatus = DRAG_CREATE ; 
                dragAnchor = curPt ;
            }
        }
        
        private void markEnded( MouseEvent event ) {
            
            try {
                if( selectedShape != null ) {
                    parent.subImageSelected( selectedShape, event.getButton() ) ;
                    delete() ;
                }
            }
            catch( Exception e1 ) {
                log.error( "Raster exception.", e1 ) ;
            }
        }
    
        @Override
        public void mouseMoved( MouseEvent event ) {
            
            if( inMarkMode ) {
                
                Point curPt = event.getPoint() ;
                switch (dragStatus) {
                    case DRAG_MOVE:
                        selectedShape.translate( curPt.x - dragAnchor.x, 
                                                 curPt.y - dragAnchor.y) ;
                        dragAnchor = curPt ; 
                        break ;
                        
                    case DRAG_CREATE:
                    case DRAG_RESIZE:
                        selectedShape.resize( dragAnchor, curPt ) ;
                        break ;
                }
            }
        }

    }
}
