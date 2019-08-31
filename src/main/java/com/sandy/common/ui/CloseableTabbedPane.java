package com.sandy.common.ui ;

import java.awt.Color ;
import java.awt.Component ;
import java.awt.Cursor ;
import java.awt.FontMetrics ;
import java.awt.Graphics ;
import java.awt.Rectangle ;
import java.awt.event.ActionEvent ;
import java.awt.event.KeyAdapter ;
import java.awt.event.KeyEvent ;
import java.awt.event.MouseEvent ;
import java.awt.event.MouseListener ;
import java.awt.event.MouseMotionListener ;
import java.awt.image.BufferedImage ;
import java.util.ArrayList ;
import java.util.List ;

import javax.swing.JTabbedPane ;
import javax.swing.plaf.basic.BasicTabbedPaneUI ;
import javax.swing.plaf.metal.MetalTabbedPaneUI ;

import org.apache.log4j.Logger ;

/**
 * This is an extension of {@link JTabbedPane} that includes a closing button in
 * the tabs. The close button behavior is similar to how tabs works in Eclipse
 * editor.
 * <p> The core functionality is implemented in an private inner class.
 */
public class CloseableTabbedPane extends HighlightableTabbedPane {

    private static final long serialVersionUID = -8165762635018896928L ;
    
    public static final int TAB_CLOSING = 1 ;
    
    private static final Logger logger = 
                                 Logger.getLogger( CloseableTabbedPane.class ) ;

    private TabCloseImageUI closeUI ;
    
    /** Pixels added to each tab for accommodating close image */
    private static final int TAB_WIDTH_EXTENSION = 50 ;
    
    /**
     * This interface provides a contract protocol for all associating classes
     * which are interested in getting notified on tab close events.
     */
    public interface TabCloseListener {
        
        /** 
         * This method in invoked when a tab is about to be closed, i.e before
         * it has been removed from the tab pane but after the user has
         * pressed the close button on the tab.
         */
        public void tabClosing( ActionEvent e ) ;
    }
    
    private List<TabCloseListener> listeners = new ArrayList<>() ;
    
    /**
     * This is the default constructor. This also adds an extended UI that
     * increases the width of tabs available. However currently this approach
     * has to hard code the UI i.e. {@link MetalTabbedPaneUI} that is being 
     * extended till a better approach is found.
     */
    public CloseableTabbedPane() {
        this.closeUI = new TabCloseImageUI( this ) ;
        super.setUI( new BasicTabbedPaneUI() {
            protected int calculateTabWidth( int tabPlacement, int tabIndex, 
                                                         FontMetrics metrics ) {
                return super.calculateTabWidth( tabPlacement, 
                                     tabIndex, metrics ) + TAB_WIDTH_EXTENSION ;
            }
        }) ;
        addCtrlWListenerForTabClose() ;
        super.setForeground( Color.WHITE ) ;
    }
    
    private void addCtrlWListenerForTabClose() {
        
        this.addKeyListener( new KeyAdapter() {
            public void keyTyped( KeyEvent e ) {
                if( e.getKeyChar() == 'w' && 
                    ( e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK ) == KeyEvent.CTRL_DOWN_MASK ) {

                    int selIndex = CloseableTabbedPane.this.getSelectedIndex() ;
                    if( selIndex == -1 ) return ;
                    
                    boolean okToCloseTab = true ;
                    Component comp = getComponentAt( selIndex ) ;
                    if( comp instanceof CloseableTab ) {
                        CloseableTab tab = ( CloseableTab )comp ;
                        if( !tab.isOkToCloseTab() ) {
                            okToCloseTab = false ;
                        }
                    }
                    
                    if( okToCloseTab ) {
                        CloseableTabbedPane.this.notifyListeners( selIndex, TAB_CLOSING ) ;
                        CloseableTabbedPane.this.remove( selIndex ) ;
                    }
                }
            }
        } ) ;
    }
    
    /** Adds a tab close listener to the list of existing listeners. */
    public void addTabCloseListener( TabCloseListener l ) {
        if( !this.listeners.contains( l ) ) {
            this.listeners.add( l ) ;
        }
    }
    
    /** Removes the specified listener from the existing list of listeners. */
    public void removeTabCloseListener( TabCloseListener l ) {
        this.listeners.remove( l ) ;
    }
    
    /** Notifies all the tab close listeners. */
    private void notifyListeners( int tabIndex, int eventId ) {
        Component comp = getComponentAt( tabIndex ) ;
        ActionEvent evt = new ActionEvent( comp, eventId, null ) ;
        for( TabCloseListener l : listeners ) {
            l.tabClosing( evt ) ;
        }
    }
    
    /**
     * This method overrides the {@link JTabbedPane#paint(Graphics)} method and
     * in addition paints the close image where necessary in the tabs.
     */
    @Override
    public void paint( Graphics g ) {
        super.paint( g ) ;
        closeUI.paint( g ) ;
    }

    /**
     * This is an inner private class that does two primary functions:
     * <p>
     * 1. Paints the close image (grey or red) as necessary<br>
     * 2. Handle various mouse events for repainting and closing tabs
     */
    private class TabCloseImageUI implements MouseListener, MouseMotionListener {

        /** Close button size */
        private static final int BUTTON_SIZE = 16 ;
        /** Spacing between right margin and close button */
        private static final int SPACING = 5 ;
        
        /** Handle to the parent Tabbed Pane */
        private CloseableTabbedPane parent ;

        /** Buffered images for close buttons */
        private BufferedImage greyClose ;
        private BufferedImage redClose ;

        /** Mouse pointer coordinates */
        private int mX = 0 ;
        private int mY = 0 ;
        
        /** Mouse over tab and close buttons statuses */
        private int mouseOverTabIndex = -1 ;
        private boolean mouseOverClose = false ;
        
        /**
         * Constructor with a handle to it's parent TabbedPane. This adds the
         * mouse handlers to it's parent and buffers up the images.
         * 
         * @param parent The instance of {@link CloseableTabbedPane}
         */
        public TabCloseImageUI( CloseableTabbedPane parent ) {
            this.parent = parent ;
            this.parent.addMouseMotionListener( this );
            this.parent.addMouseListener( this );
            try {
                greyClose = SwingUtils.getIconImage( this.getClass(), "close_grey" ) ;
                redClose  = SwingUtils.getIconImage( this.getClass(), "close_red" ) ;
                            
            } catch ( Exception e ) {
                logger.error( "Error while loading close images for tab", e ) ;
            }
        }
        
        /**
         * The paint method that is called from the parent to paint the close
         * buttons.
         * 
         * @param g the instance of Graphic Context
         */
        public void paint( Graphics g ) {
            //Draw close image on all tabs
            for ( int i = 0 ; i < parent.getTabCount() ; i++ ) {
                boolean red = ( i == mouseOverTabIndex  && mouseOverClose ) ;
                drawCloseImage( g, i, red ) ;
            }
        }

        /** Unused mouse events */
        public void mouseClicked( MouseEvent e ) {}
        public void mouseEntered( MouseEvent e ) {}
        public void mouseExited( MouseEvent e )  {}
        public void mousePressed( MouseEvent e ) {}
        public void mouseDragged( MouseEvent e ) {}

        /**
         * This mouse released method is used for capturing the event of a mouse
         * release over a close button. If the mouse release is over a close
         * button then the tab is closed.
         * 
         * This method removes the utility tab if an only if the utility 
         * assures that it is ok for it to be removed.
         * <p>
         * See {@link MouseListener#mouseReleased(MouseEvent)}
         */
        public void mouseReleased( MouseEvent e ) {
            
            boolean okToCloseTab = true ;
            
            mX = e.getX() ;
            mY = e.getY() ;
            //check if mouse if released over close image
            if ( getMouseOverTabIndex() != -1  && isMouseOnClose() ) {
                int tabIndex = getMouseOverTabIndex() ;
                
                Component comp = getComponentAt( tabIndex ) ;
                if( comp instanceof CloseableTab ) {
                    CloseableTab tab = ( CloseableTab )comp ;
                    if( !tab.isOkToCloseTab() ) {
                        okToCloseTab = false ;
                    }
                }
                
                if( okToCloseTab ) {
                    parent.notifyListeners( tabIndex, TAB_CLOSING ) ;
                    parent.remove( tabIndex ) ;
                }
            }
        }
        
        /**
         * This method is used for tracking the mouse movement and if the state
         * is changed then repaint the tabs.
         */
        public void mouseMoved( MouseEvent e ) {
            mX = e.getX() ;
            mY = e.getY() ;
            //Re-paint if the status has changed
            if ( ( mouseOverTabIndex != getMouseOverTabIndex() ) 
                 || ( mouseOverClose != isMouseOnClose() )) {
                mouseOverTabIndex = getMouseOverTabIndex() ;
                mouseOverClose = isMouseOnClose() ;
                paint( parent.getGraphics() ) ;
                setCursor() ;
            }
        }   
        
        /**
         * This is a private method to change the cursor to a hand in case of
         * mouse over close button.
         */
        private void setCursor() {
            if ( mouseOverClose ) {
                parent.setCursor( new Cursor( Cursor.HAND_CURSOR ) ) ;
            } else {
                parent.setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) ) ;
            }
        }
        
        /**
         * This utility method returns the index of tab on which the mouse is
         * hovering.
         * 
         * @return Index of tab mouse of over or -1 if mouse is not over any
         *         tab.
         */
        private int getMouseOverTabIndex() {
            int index = -1 ;
            for ( int i = 0 ; i < parent.getTabCount() ; i++ ) {
                Rectangle rect = parent.getBoundsAt( i ) ;
                if ( rect.contains( mX, mY ) ) {
                    index = i ;
                    break ;
                }
            }
            return index ;
        }
        
        /**
         * This method determines if the mouse is over the close button.
         * 
         * @return True if mouse of over a close button
         */
        private boolean isMouseOnClose() {
            boolean ret = false ;
            int index = getMouseOverTabIndex() ;
            if ( index != -1 ) {
                Rectangle rect = parent.getBoundsAt( index ) ;
                int dx = rect.x + rect.width - BUTTON_SIZE - SPACING ;
                int dy = ( rect.y + ( rect.height / 2 ) )  - 6 ;
                Rectangle imgRect = new Rectangle( dx, dy, 
                                          BUTTON_SIZE, BUTTON_SIZE ) ;
                ret =  imgRect.contains( mX, mY ) ;
            }
            return ret ;
        }
        
        
        /**
         * This method draws the image at the given tab index.
         * 
         * @param g - Graphic context
         * @param index - Index of tab on which image is to be drawn 
         * @param red - If a red image is to be drawn or grey
         */
        private void drawCloseImage( Graphics g, int index, boolean red ) {
            if ( index != -1 && index < parent.getTabCount() ) {
                Rectangle rect = parent.getBoundsAt( index ) ;
                int dx = rect.x + rect.width - BUTTON_SIZE - SPACING ;
                int dy = ( rect.y + ( rect.height / 2 ) )  - 6 ;
                g.drawImage( red ? redClose : greyClose, dx, dy, null ) ;
            }
        }
    }

}
