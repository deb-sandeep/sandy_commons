package com.sandy.common.ui.statusbar;

import java.awt.BorderLayout ;

import javax.swing.JLabel ;

import org.apache.log4j.Logger ;

public class MessageSBComponent extends AbstractSBComponent {

    private static final long serialVersionUID = -7087385729013001309L ;

    public static Logger logger = Logger.getLogger( ClockSBComponent.class ) ;

    private JLabel msgLabel = null;

    @Override
    public void initialize() {
        setUpUI();
    }

    private void setUpUI() {
        msgLabel = new JLabel() ;
        clear() ;
        super.add( this.msgLabel, BorderLayout.CENTER );
    }
    
    public void logMessage( String text ) {
        msgLabel.setText( text ) ;
    }
    
    public void clear() {
        msgLabel.setText( " " ) ;
    }
}
