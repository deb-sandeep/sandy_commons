package com.sandy.common.util;

import java.io.File ;
import java.net.MalformedURLException ;
import java.net.URL ;

public class WorkspaceManager {

    private String appId = null ;
    private File   workspaceDir = null ;
    
    public WorkspaceManager( String appId ) throws Exception {
        
        this.appId = appId ;
        
        if( StringUtil.isEmptyOrNull( this.getAppId() ) ) {
            throw new Exception( "Application ID is not set." ) ;
        }
        
        File userHomeDir  = new File( System.getProperty( "user.home" ) ) ;
        this.workspaceDir = new File( userHomeDir, "." + getAppId() ) ;
        
        if( !this.workspaceDir.exists() ) {
            if( !this.workspaceDir.mkdirs() ) {
                throw new Exception( "Could not create workspace directory." ) ;
            }
        }
    }

    public String getAppId() {
        return appId;
    }

    public File getWorkspaceDir() {
        return this.getWorkspaceDir() ;
    }
    
    public File getFile( String relativePath ) {
        File file = new File( this.workspaceDir, relativePath ) ;
        return file ;
    }
    
    public URL getFileURL( String relativePath ) {
        
        URL url =  null ;
        File file = getFile( relativePath ) ;
        if( file.exists() ) {
            try {
                url = file.toURI().toURL() ;
            }
            catch( MalformedURLException e ) {
                // This should not happen!
                e.printStackTrace();
            }
        }
        return url ;
    }
    
    public boolean fileExists( String relativePath ) {
        return getFile( relativePath ).exists() ;
    }
    
    public boolean makeDir( String dirPath ) {
        File file = new File( this.workspaceDir, dirPath ) ;
        return file.mkdirs() ;
    }
}
