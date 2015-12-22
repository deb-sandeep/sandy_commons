package com.sandy.common.ui.statusbar;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.sandy.common.contracts.Initializable ;

/**
 * This class defines the base class of a typical status bar component. Concrete
 * status bar components should derive from this class.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public abstract class AbstractSBComponent extends JPanel
    implements Initializable {

    private static final long serialVersionUID = -7087385729013001309L ;

    public static final Logger logger = Logger.getLogger( AbstractSBComponent.class ) ;

    public AbstractSBComponent() {
        super() ;
    }
}