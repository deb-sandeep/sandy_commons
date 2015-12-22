package com.sandy.common.ui.statusbar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList ;
import java.util.List ;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.sandy.common.contracts.Initializable ;

/**
 * This class represents the application console's status bar and is shown at
 * the south most component on the application console. 
 * 
 * To use this component, one or more status bar components need to be registered
 * via the {@link #addStatusBarComponent(AbstractSBComponent, Direction)} method,
 * followed by calling the {@link #initialize()} method.
 * 
 * Please note that if any of the status bar components are {@link Initializable},
 * the {@link #initialize()} method will initialize them before adding to the 
 * status bar.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class StatusBar extends JPanel implements Initializable {

    private static final long serialVersionUID = 1257815562094770997L ;

    public static final Logger logger = Logger.getLogger( StatusBar.class ) ;

    /** Enumeration to specify which panel the components will be added to. */
    public static enum Direction { WEST, EAST } ;
    
    private JPanel westPanel = new JPanel() ;
    private JPanel eastPanel = new JPanel() ;

    private List<AbstractSBComponent> westPanelComponents  = new ArrayList<>() ;
    private List<AbstractSBComponent> eastPanelComponents = new ArrayList<>() ;
    
    public void addStatusBarComponent( AbstractSBComponent comp, Direction dir ) {
        if( dir == Direction.WEST ) {
            westPanelComponents.add( comp ) ;
        }
        else {
            eastPanelComponents.add( comp ) ;
        }
    }
    
    @Override
    public void initialize() throws Exception {

        initializeChildComponents( this.westPanelComponents ) ;
        initializeChildComponents( this.eastPanelComponents ) ;
        
        setUpPanel( Direction.WEST ) ;
        setUpPanel( Direction.EAST ) ;
        
        setLayout( new BorderLayout() ) ;
        add( this.eastPanel, BorderLayout.CENTER ) ;
        add( this.westPanel, BorderLayout.WEST ) ;
    }
    
    private void initializeChildComponents( List<AbstractSBComponent> children )
        throws Exception {
        
        for( AbstractSBComponent child : children ) {
            if( child instanceof Initializable ) {
                child.initialize() ;
            }
        }
    }
    
    private void setUpPanel( Direction dir ) {
        
        JPanel panel                         = null ;
        int layoutDir                        = FlowLayout.LEFT ;
        List<AbstractSBComponent> components = null ;
        
        if( dir == Direction.WEST ) {
            panel      = this.westPanel ;
            layoutDir  = FlowLayout.LEFT ;
            components = this.westPanelComponents ;
        }
        else {
            panel      = this.eastPanel ;
            layoutDir  = FlowLayout.RIGHT ;
            components = this.eastPanelComponents ;
        }
        
        panel.setLayout( new FlowLayout( layoutDir ) );
        for( int i=0; i<components.size(); i++ ) {
            panel.add( components.get( i ) ) ;
            if( i < (components.size()-1) ) {
                panel.add( getSeparator() ) ;
            }
        }
    }

    private Component getSeparator() {
        final JPanel label = new JPanel() ;
        label.setBackground( Color.DARK_GRAY ) ;
        label.setPreferredSize( new Dimension( 2, 25 ) ) ;
        return label ;
    }
}

