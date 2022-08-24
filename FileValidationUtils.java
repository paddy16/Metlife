/*********************************************************** {COPYRIGHT-TOP} ***
 * IBM Confidential
 * OCO Source Materials
 * Tivoli Workload Analytics
 *
 * (C) Copyright IBM Corp. 2012.  All Rights Reserved.
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 ************************************************************ {COPYRIGHT-END} **/
package com.ibm.tivoli.unity.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class FileValidationUtils {
		 
	 public static final String isValidPath(String rootdir, String childdir){
		   
			//try {
			//	File root = new File(rootdir);
			//	File child = new File(rootdir+childdir);
			//	if(!child.getCanonicalPath().startsWith(root.getCanonicalPath())){
			//		childdir = null;				
			//	}
			//} catch (IOException io) {
			//	childdir = null;			
	        //}
			//return childdir;
			
			if (!FileValidationUtils.pathStartsWith(rootdir, rootdir+childdir)) {
			    childdir = null;
			}
			
			return childdir;
	 }
	 
	 public static final boolean pathStartsWith(String pathPrefix, String pathToCheck) {
		 boolean result = false;
	     try {
	    	 Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrwx--x");
		     FileAttribute<Set<PosixFilePermission>> fileAttributes = PosixFilePermissions.asFileAttribute(perms);
             File root = new File(pathPrefix);
             
             Path childPath = null;
             try{
            	childPath = Files.createFile(Paths.get(pathToCheck),fileAttributes);
             }catch(FileAlreadyExistsException fae){
            	//fae.printStackTrace(); // Not to be bothered as path is valid
            	childPath = Paths.get(pathToCheck);
             }
             File child = childPath.toFile();
             result = child.getCanonicalPath().startsWith(root.getCanonicalPath());
         }catch (IOException ex) {
        	 ex.printStackTrace();
             return false;
         }
	     return result;
	 }
	 	 
}
