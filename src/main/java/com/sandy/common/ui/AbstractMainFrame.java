package com.sandy.common.ui;

import static com.sandy.common.ui.SwingUtils.setMaximized ;

import java.awt.BorderLayout ;
import java.awt.Component ;
import java.awt.Container ;
import java.awt.event.WindowAdapter ;
import java.awt.event.WindowEvent ;

import javax.swing.ImageIcon ;
import javax.swing.JFrame ;
import javax.swing.JMenuBar ;

import com.sandy.common.ui.statusbar.StatusBar ;

public abstract class AbstractMainFrame extends JFrame {

    private static final long serialVersionUID = -5612109031015976577L ;

    private ImageIcon frameIcon       = null ;
    private JMenuBar  menuBar         = null ;
    private StatusBar statusBar       = null ;
    private Component centerComponent = null ;

    public AbstractMainFrame() throws Exception {
        this( null, null ) ;
    }
    
    public AbstractMainFrame( String title ) throws Exception {
        this( title, null ) ;
    }
    
    public AbstractMainFrame( String title, ImageIcon frameIcon ) 
        throws Exception {
        
        super( title ) ;
        this.frameIcon = frameIcon ;
        
        setUpUI() ;
        setUpListeners() ;
    }
    
    private void setUpUI() throws Exception 
    {
        setMaximized( this ) ;
        
        Container contentPane = getContentPane() ;
        contentPane.setLayout( new BorderLayout() ) ;
        
        menuBar = getFrameMenu() ;
        if( menuBar != null ) {
            setJMenuBar( menuBar ) ;
        }
        
        statusBar = getStatusBar() ;
        if( statusBar != null ) {
            statusBar.initialize() ;
            contentPane.add( statusBar, BorderLayout.SOUTH ) ;
        }
        
        centerComponent = getCenterComponent() ;
        if( centerComponent != null ) {
            contentPane.add( centerComponent, BorderLayout.CENTER ) ;
        }
        
        if( frameIcon != null ) {
            setIconImage( frameIcon.getImage() ) ;
        }
    }
    
    private void setUpListeners() {
        super.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                handleWindowClosing() ;
            }
        }) ;
    }
    
    /** By default the program exits. */
    protected void handleWindowClosing() {
        // By default the program exits
        System.exit( -1 ) ;
    }
    
    protected JMenuBar getFrameMenu() {
        // By default the frame does not have a menu bar
        return null ;
    }
    
    protected StatusBar getStatusBar() {
        // By default the frame does not have a status bar
        return null ;
    }
    
    protected abstract Component getCenterComponent() ;
}
