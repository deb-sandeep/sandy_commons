package com.sandy.common.util ;


import java.beans.Introspector;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class ReflectionUtil { 

    private static final Logger logger = Logger.getLogger( ReflectionUtil.class ) ;

    private static final String ARRAY_SUFFIX          = "[]" ;
    private static final char   PACKAGE_SEPARATOR     = '.' ;
    private static final char   INNER_CLASS_SEPARATOR = '$' ;
    private static final String CGLIB_CLASS_SEPARATOR = "$$" ;

    private static final Map<Class<?>,  Class<?>> primitiveWrapperTypeMap = new HashMap<Class<?>, Class<?>>( 8 ) ;
    private static final Map<String, Class<?>>    primitiveTypeNameMap    = new HashMap<String, Class<?>>( 8 ) ;

    static {
        primitiveWrapperTypeMap.put( Boolean.class,   boolean.class ) ;
        primitiveWrapperTypeMap.put( Byte.class,      byte.class    ) ;
        primitiveWrapperTypeMap.put( Character.class, char.class    ) ;
        primitiveWrapperTypeMap.put( Double.class,    double.class  ) ;
        primitiveWrapperTypeMap.put( Float.class,     float.class   ) ;
        primitiveWrapperTypeMap.put( Integer.class,   int.class     ) ;
        primitiveWrapperTypeMap.put( Long.class,      long.class    ) ;
        primitiveWrapperTypeMap.put( Short.class,     short.class   ) ;

        for ( Map.Entry<Class<?>, Class<?>> entry : primitiveWrapperTypeMap.entrySet() ) {
            
            Class<?> primitiveClass = entry.getKey() ;
            primitiveTypeNameMap.put( primitiveClass.getName(), primitiveClass ) ;
        }
    }

    private ReflectionUtil() {
        super() ;
    }

    public static Method getOnlyMethodWithName( final Class<?> clazz,
                                                final String methodName ) {

        Class<?> searchType = clazz ;
        if( getMethodCountForName( clazz, methodName ) == 1 ) {
            while ( !Object.class.equals( searchType ) && searchType != null ) {
                Method[] methods = null ;
                methods = searchType.getDeclaredMethods() ;

                for ( int i = 0; i < methods.length; i++ ) {
                    final Method method = methods[i] ;
                    
                    if ( methodName.equals( method.getName() ) ) {
                        return method ; 
                    }
                }
                searchType = searchType.getSuperclass() ;
            }
        }
        return null ;
    }
    

    public static Method findClosestMethod( final Class<?> clazz,  
                                            final String name,
                                            final Class<?>[] paramTypes ) { 

        Class<?> searchType = clazz ;
        while ( !Object.class.equals( searchType ) && searchType != null ) {

            Method[] methods = null ;

            methods = searchType.getDeclaredMethods() ;

            for ( int i = 0; i < methods.length; i++ ) {
                final Method method = methods[i] ;
                final Class<?>[] mthParamTypes = method.getParameterTypes() ;
                final int numParams = (paramTypes == null)?0:paramTypes.length ; 
                    
                if ( name.equals( method.getName() ) && 
                     mthParamTypes.length == numParams ) {
                    
                    // This is a potential method. Now check the parameters
                    boolean matchFound = true ;
                    for( int j=0; j<numParams; j++ ) {
                        if( paramTypes[j] != null && 
                            !mthParamTypes[j].isAssignableFrom( paramTypes[j] ) ) {
                            matchFound = false ;
                            break ;
                        }
                    }
                    
                    if( matchFound ) {
                        logger.debug( "Found a matching method " + method.getName() ) ;
                        return method ; 
                    }
                }
            }
            searchType = searchType.getSuperclass() ;
        }
        return null ;
    }
    
    /**
     * Attempt to find a {@link Method} on the supplied type with the supplied
     * name and parameter types. Searches all super classes up to
     * <code>Object</code>.
     * <p>
     * Returns <code>null</code> if no {@link Method} can be found.
     */
    public static Method findMethod( final Class<?> clazz, final String name,
                                     final Class<?>[] paramTypes ) {

        Class<?> searchType = clazz ;
        while ( !Object.class.equals( searchType ) && searchType != null ) {

            Method[] methods = null ;

            if ( searchType.isInterface() ) {
                methods = searchType.getMethods() ;
            }
            else {
                methods = searchType.getDeclaredMethods() ;
            }

            for ( int i = 0; i < methods.length; i++ ) {
                final Method method = methods[i] ;
                if ( name.equals( method.getName() ) && 
                     Arrays.equals( paramTypes,method.getParameterTypes() ) ) {
                    return method ; 
                }
            }
            searchType = searchType.getSuperclass() ;
        }
        return null ;
    }

    /**
     * Return the default ClassLoader to use: typically the thread context
     * ClassLoader, if available; the ClassLoader that loaded the
     * ReflectionUtil class will be used as fallback.
     * <p>
     * Call this method if you intend to use the thread context ClassLoader in a
     * scenario where you absolutely need a non-null ClassLoader reference: for
     * example, for class path resource loading (but not necessarily for
     * <code>Class.forName</code>, which accepts a <code>null</code>
     * ClassLoader reference as well).
     * 
     * @return the default ClassLoader (never <code>null</code>)
     * @see java.lang.Thread#getContextClassLoader()
     */
    public static ClassLoader getDefaultClassLoader() {

        ClassLoader clsLoader = null ;
        try {
            clsLoader = Thread.currentThread().getContextClassLoader() ;
        }
        catch ( Throwable ex ) { 
            logger.debug( "Cannot access thread context ClassLoader - " +
                          "falling back to system class loader", ex ) ;
        }

        if ( clsLoader == null ) {
            // No thread context class loader -> use class loader of this class.
            clsLoader = ReflectionUtil.class.getClassLoader() ;
        }
        return clsLoader ;
    }

    /**
     * Replacement for <code>Class.forName()</code> that also returns Class
     * instances for primitives (like "int") and array class names (like
     * "String[]").
     * <p>
     * Always uses the default class loader: that is, preferably the thread
     * context class loader, or the ClassLoader that loaded the ClassUtils class
     * as fallback.
     * 
     * @param name the name of the Class
     * @return Class instance for the supplied name
     * @throws ClassNotFoundException if the class was not found
     * @throws LinkageError if the class file could not be loaded
     * @see Class#forName(String, boolean, ClassLoader)
     * @see #getDefaultClassLoader()
     */
    public static Class<?> forName( final String name ) throws ClassNotFoundException,
            LinkageError {

        return forName( name, getDefaultClassLoader() ) ;
    }

    /**
     * Replacement for <code>Class.forName()</code> that also returns Class
     * instances for primitives (like "int") and array class names (like
     * "String[]").
     * 
     * @param name the name of the Class
     * @param classLoader the class loader to use (may be <code>null</code>,
     *        which indicates the default class loader)
     * @return Class instance for the supplied name
     * @throws ClassNotFoundException if the class was not found
     * @throws LinkageError if the class file could not be loaded
     * @see Class#forName(String, boolean, ClassLoader)
     */
    public static Class<?> forName( final String name, 
                                    final ClassLoader classLoader )
            throws ClassNotFoundException, LinkageError {

        final Class<?> clazz = resolvePrimitiveClassName( name ) ;
        
        if ( clazz != null ) {
            return clazz ; 
        }
        
        if ( name.endsWith( ARRAY_SUFFIX ) ) {
            // special handling for array class names
            final String elementClassName = name.substring( 0, name.length()
                                                     - ARRAY_SUFFIX.length() ) ;
            final Class<?> elementClass = forName( elementClassName, classLoader ) ;
            return Array.newInstance( elementClass, 0 ).getClass() ; 
        }
        
        ClassLoader classLoaderToUse = classLoader ;
        if ( classLoaderToUse == null ) {
            classLoaderToUse = getDefaultClassLoader() ;
        }
        
        return classLoaderToUse.loadClass( name ) ;
    }

    /**
     * Resolve the given class name as primitive class, if appropriate.
     * 
     * @param name the name of the potentially primitive class
     * @return the primitive class, or <code>null</code> if the name does not
     *         denote a primitive class
     */
    public static Class<?> resolvePrimitiveClassName( final String name ) {

        Class<?> result = null ;
        // Most class names will be quite long, considering that they
        // SHOULD sit in a package, so a length check is worthwhile.
        if ( name != null && name.length() <= 8 ) {
            // Could be a primitive - likely.
            result = ( Class<?> ) primitiveTypeNameMap.get( name ) ;
        }
        return result ;
    }

    /**
     * Get the class name without the qualified package name.
     * 
     * @param className the className to get the short name for
     * @return the class name of the class without the package name
     * @throws IllegalArgumentException if the className is empty
     */
    public static String getShortName( final String className ) {

        final int lastDotIndex = className.lastIndexOf( PACKAGE_SEPARATOR ) ;
        int nameEndIndex = className.indexOf( CGLIB_CLASS_SEPARATOR ) ;
        if ( nameEndIndex == -1 ) {
            nameEndIndex = className.length() ;
        }
        String shortName = className.substring( lastDotIndex + 1, nameEndIndex ) ;
        shortName = shortName.replace( INNER_CLASS_SEPARATOR, 
                                       PACKAGE_SEPARATOR ) ;
        return shortName ;
    }

    /**
     * Get the class name without the qualified package name.
     * 
     * @param clazz the class to get the short name for
     * @return the class name of the class without the package name
     */
    public static String getShortName( final Class<?> clazz ) {
        return getShortName( getQualifiedName( clazz ) ) ;
    }

    /**
     * Return the short string name of a Java class in decapitalized JavaBeans
     * property format.
     * 
     * @param clazz the class
     * @return the short name rendered in a standard JavaBeans property format
     * @see java.beans.Introspector#decapitalize(String)
     */
    public static String getShortNameAsProperty( final Class<?> clazz ) {
        return Introspector.decapitalize( getShortName( clazz ) ) ;
    }

    /**
     * Return the qualified name of the given class: usually simply the class
     * name, but component type class name + "[]" for arrays.
     * 
     * @param clazz the class
     * @return the qualified name of the class
     */
    public static String getQualifiedName( final Class<?> clazz ) {

        if ( clazz.isArray() ) {
            return getQualifiedNameForArray( clazz ) ; 
        }
        else {
            return clazz.getName() ;
        }
    }

    /**
     * Build a nice qualified name for an array: component type class name +
     * "[]".
     * 
     * @param clazz the array class
     * @return a qualified name for the array class
     */
    private static String getQualifiedNameForArray( final Class<?> clazz ) {

        final StringBuffer buffer = new StringBuffer() ;
        Class<?> classToCheck = clazz ;
        while ( classToCheck.isArray() ) {
            classToCheck = classToCheck.getComponentType() ;
            buffer.append( ARRAY_SUFFIX ) ;
        }
        buffer.insert( 0, clazz.getName() ) ;
        return buffer.toString() ;
    }

    /**
     * Determine whether the given class has a method with the given signature.
     * <p>
     * Essentially translates <code>NoSuchMethodException</code> to "false".
     * 
     * @param clazz the clazz to analyze
     * @param methodName the name of the method
     * @param paramTypes the parameter types of the method
     * @return whether the class has a corresponding method
     * @see java.lang.Class#getMethod
     */
    public static boolean hasMethod( final Class<?> clazz, final String methodName,
                                     final Class<?>[] paramTypes ) {

        return ( getMethodIfAvailable( clazz, methodName, paramTypes ) != null ) ;
    }

    /**
     * Determine whether the given class has a method with the given signature,
     * and return it if available (else return <code>null</code>).
     * <p>
     * Essentially translates <code>NoSuchMethodException</code> to
     * <code>null</code>.
     * 
     * @param clazz the clazz to analyze
     * @param methodName the name of the method
     * @param paramTypes the parameter types of the method
     * @return the method, or <code>null</code> if not found
     * @see java.lang.Class#getMethod
     */
    public static Method getMethodIfAvailable( final Class<?> clazz, 
                                               final String methodName,
                                               final Class<?>[] paramTypes ) {

        Method retVal = null ;
        try {
            retVal = clazz.getMethod( methodName, paramTypes ) ;
        }
        catch ( NoSuchMethodException ex ) {
            logger.debug( "No method found " + methodName + " on class " +
                          clazz.getName() ) ;
        }
        return retVal ;
    }

    /**
     * Return the number of methods with a given name (with any argument types),
     * for the given class and/or its superclasses. Includes non-public methods.
     * 
     * @param clazz the clazz to check
     * @param methodName the name of the method
     * @return the number of methods with the given name
     */
    public static int getMethodCountForName( final Class<?> clazz, 
                                             final String methodName ) {

        int count = 0 ;
        for ( int i = 0; i < clazz.getDeclaredMethods().length; i++ ) {
            final Method method = clazz.getDeclaredMethods()[i] ;
            if ( methodName.equals( method.getName() ) ) {
                count++ ;
            }
        }
        final Class<?>[] ifcs = clazz.getInterfaces() ;
        for ( int i = 0; i < ifcs.length; i++ ) {
            count += getMethodCountForName( ifcs[i], methodName ) ;
        }
        if ( clazz.getSuperclass() != null ) {
            count += getMethodCountForName( clazz.getSuperclass(), methodName ) ;
        }
        return count ;
    }

    /**
     * Does the given class and/or its superclasses at least have one or more
     * methods (with any argument types)? Includes non-public methods.
     * 
     * @param clazz the clazz to check
     * @param methodName the name of the method
     * @return whether there is at least one method with the given name
     */
    public static boolean hasAtLeastOneMethodWithName( final Class<?> clazz,
                                                       final String methodName ) {

        for ( int i = 0; i < clazz.getDeclaredMethods().length; i++ ) {
            final Method method = clazz.getDeclaredMethods()[i] ;
            if ( method.getName().equals( methodName ) ) {
                return true ; 
            }
        }
        final Class<?>[] ifcs = clazz.getInterfaces() ;
        for ( int i = 0; i < ifcs.length; i++ ) {
            if ( hasAtLeastOneMethodWithName( ifcs[i], methodName ) ) {
                return true ; 
            }
        }
        return ( clazz.getSuperclass() != null && 
                hasAtLeastOneMethodWithName( clazz.getSuperclass(),methodName ) ) ;
    }

    /**
     * Check if the given class represents a primitive wrapper, i.e. Boolean,
     * Byte, Character, Short, Integer, Long, Float, or Double.
     * 
     * @param clazz the class to check
     * @return whether the given class is a primitive wrapper class
     */
    public static boolean isPrimitiveWrapper( final Class<?> clazz ) {

        return primitiveWrapperTypeMap.containsKey( clazz ) ;
    }

    /**
     * Check if the given class represents a primitive (i.e. boolean, byte,
     * char, short, int, long, float, or double) or a primitive wrapper (i.e.
     * Boolean, Byte, Character, Short, Integer, Long, Float, or Double).
     * 
     * @param clazz the class to check
     * @return whether the given class is a primitive or primitive wrapper class
     */
    public static boolean isPrimitiveOrWrapper( final Class<?> clazz ) {

        return ( clazz.isPrimitive() || isPrimitiveWrapper( clazz ) ) ;
    }

    /**
     * Check if the given class represents an array of primitives, i.e. boolean,
     * byte, char, short, int, long, float, or double.
     * 
     * @param clazz the class to check
     * @return whether the given class is a primitive array class
     */
    public static boolean isPrimitiveArray( final Class<?> clazz ) {

        return ( clazz.isArray() && clazz.getComponentType().isPrimitive() ) ;
    }

    /**
     * Check if the given class represents an array of primitive wrappers, i.e.
     * Boolean, Byte, Character, Short, Integer, Long, Float, or Double.
     * 
     * @param clazz the class to check
     * @return whether the given class is a primitive wrapper array class
     */
    public static boolean isPrimitiveWrapperArray( final Class<?> clazz ) {

        return ( clazz.isArray() && isPrimitiveWrapper( clazz.getComponentType() ) ) ;
    }
    
    public static InputStream getResourceAsStream( final Class<?> refCls, final String resName ) {
        
        String qualifiedResName = addResPathToPkgPath( refCls, resName ) ;
        return refCls.getResourceAsStream( qualifiedResName ) ;
    }

    public static URL getResource( final Class<?> refCls, final String resName ) {
        
        String qualifiedResName = addResPathToPkgPath( refCls, resName ) ;
        return refCls.getResource( qualifiedResName ) ;
    }
    
    public static URL getTestConfigResource( final Class<?> testCaseCls, final String resName ) {
        
        String resPath = testCaseCls.getPackage().getName() ;
        resPath = resPath.replace( '.', '/' ) ;
        resPath = "/" + resPath + "/" + resName ;
        
        return testCaseCls.getResource( resPath ) ;
    }

    /**
     * Return a path suitable for use with <code>ClassLoader.getResource</code>
     * (also suitable for use with <code>Class.getResource</code>. Built by taking the package
     * of the specified class file, converting all dots ('.') to slashes ('/'),
     * adding a trailing slash if necesssary, and concatenating the specified
     * resource name to this. <br/>As such, this function may be used to build a
     * path suitable for loading a resource file that is in the same package as
     * a class file, although
     * {@link org.springframework.core.io.ClassPathResource} is usually even
     * more convenient.
     * 
     * @param clazz the Class whose package will be used as the base
     * @param resourceName the resource name to append. A leading slash is
     *        optional.
     * @return the built-up resource path
     * @see java.lang.ClassLoader#getResource
     * @see java.lang.Class#getResource
     */
    public static String addResPathToPkgPath( final Class<?> clazz,
                                              final String resourceName ) {

        String retVal = null ;

        if ( resourceName.charAt( 0 ) == '/' ) {
            retVal = classPackageAsResourcePath( clazz ) + resourceName ;
        }
        else {
            retVal = classPackageAsResourcePath( clazz ) + "/" + resourceName ;
        }

        return "/" + retVal ;
    }

    /**
     * Given an input class object, return a string which consists of the
     * class's package name as a pathname, i.e., all dots ('.') are replaced by
     * slashes ('/'). Neither a leading nor trailing slash is added. The result
     * could be concatenated with a slash and the name of a resource, and fed
     * directly to <code>ClassLoader.getResource()</code>. For it to be fed
     * to <code>Class.getResource</code> instead, a leading slash would also
     * have to be prepended to the returned value.
     * 
     * @param clazz the input class. A <code>null</code> value or the default
     *        (empty) package will result in an empty string ("") being
     *        returned.
     * @return a path which represents the package name
     */
    public static String classPackageAsResourcePath( final Class<?> clazz ) {

        if ( clazz == null ) {
            return "" ; 
        }
        final String className = clazz.getName() ;
        final int packageEndIndex = className.lastIndexOf( '.' ) ;
        if ( packageEndIndex == -1 ) {
            return "" ; 
        }
        final String packageName = className.substring( 0, packageEndIndex ) ;
        return packageName.replace( '.', '/' ) ;
    }

    public static Class<?> resolveClass( final String shortName, final List<String> importList ) 
        throws Exception {
        
        Class<?> clazz      = null ;
        String   className  = null ;
        
        final List<Class<?>> matches = new ArrayList<Class<?>>() ;
        
        if( shortName.indexOf( '.' ) == -1 ) {
            // This is possibly not an absolute class name. Try to match
            // it against the registered imports
            for( String importType : importList ) {
                
                if( importType.endsWith( ".*" ) ) {
                    // If this is a package import, see if this class belongs
                    importType = importType.substring( 0, importType.length()-1 ) ;
                    className  = importType + shortName ;
                    try {
                        clazz = Class.forName( className ) ;
                        matches.add( clazz ) ;
                    }
                    catch ( ClassNotFoundException e ) {
                        logger.debug( "Mismatch " + className ) ;
                    }
                }
                else if( importType.endsWith( shortName ) ) {
                    // If this is an absolute import ending with this param type
                    try {
                        clazz = Class.forName( importType ) ;
                        matches.add( clazz ) ;
                    }
                    catch ( ClassNotFoundException e ) {
                        logger.debug( "Mismatch " + className ) ;
                    }
                }
            }
        }
        
        if( matches.isEmpty() ) {
            try {
                clazz = ReflectionUtil.forName( shortName ) ;
                matches.add( clazz ) ;
            }
            catch ( Exception e ) {
                logger.debug( "Check for primitive type failed for " + shortName ) ;
            }
        }
        
        if( matches.size() > 1 ) {
            final StringBuffer buffer = new StringBuffer() ;
            buffer.append( "Multiple matches for parameter type " ) ;
            buffer.append( shortName ) ;
            buffer.append( "\n\tPotential matches : \n" ) ;
            for( Class<?> cls : matches ) {
                buffer.append( "\t->" ).append( cls.getName() ) ;
                buffer.append( '\n' ) ;
            }
            buffer.deleteCharAt( buffer.length()-1 ) ;
            logger.error( buffer.toString() ) ;
            throw new Exception( buffer.toString() ) ;
        }
        else if ( matches.isEmpty() ) {
            throw new Exception( "Multiple matches found for " + shortName ) ;
        }
        
        return matches.get( 0 ) ;
    }
    
    public static Object createInstance( final String className ) 
        throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        
        Object   retVal = null ;
        Class<?> clazz  = null ;
        
        clazz  = Class.forName( className ) ;
        retVal = clazz.newInstance() ;
        
        return retVal ;
    }    
}
