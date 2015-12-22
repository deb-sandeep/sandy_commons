package com.sandy.common.ui.statusbar;

import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;

import org.apache.log4j.Logger;

/**
 * This status bar component shows a digital clock on the status bar, which
 * shows time at second interval. Internally, this component spawns a thread
 * which sleeps for 1000 milliseconds and changes the text of the status bar
 * accordingly.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public class ClockSBComponent extends AbstractSBComponent {

    private static final long serialVersionUID = -7087385729013001309L ;

    public static Logger logger = Logger.getLogger( ClockSBComponent.class ) ;

    private static final SimpleDateFormat DATE_TIME_FMT = 
                                     new SimpleDateFormat( "dd-MMM HH:mm:ss" ) ;

    private JLabel timeLabel = null;

    @Override
    public void initialize() {
        setUpUI();
        final Thread thread = new Thread() {
            public void run() {
                // Loop for ever
                while( true ) {
                    try {
                        Thread.sleep( 1000 );
                        ClockSBComponent.this.timeLabel
                                .setText( DATE_TIME_FMT.format( new Date() ) );
                    }
                    catch( final Throwable e ) {
                        // Harden the logic, so that this thread does not fail
                        logger.debug( "Time update failed", e );
                    }
                }
            }
        };
        thread.start();
    }

    /**
     * A private helper method to set up the UI. Note that the code for this
     * method has been generated in NetBeans and should not be changed.
     */
    private void setUpUI() {

        super.setLayout( new BorderLayout() );
        this.timeLabel = new JLabel( DATE_TIME_FMT.format( new Date() ) );
        this.timeLabel.setDoubleBuffered( true );
        super.add( this.timeLabel, BorderLayout.CENTER );
    }
}
