package com.sandy.common.contracts;

/**
 * A market interface to identify classes which need to be called upon to
 * initialize themselves before they can be used.
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface Initializable {

    /**
     * This method should be implemented by implementing classes to encapsulate
     * the instance initialization logic.
     *
     * @throws Exception If an exception is encountered during initialization
     */
    void initialize() throws Exception ;
}
