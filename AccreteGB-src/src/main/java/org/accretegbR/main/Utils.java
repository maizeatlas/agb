package org.accretegbR.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import rcaller.Globals;
import rcaller.RCode;

public class Utils {

    private static String WIN_R_EXE = "R.exe";
    private static String WIN_RSCRIPT_EXE = "Rscript.exe";
    private static String LINUX_R = "R";
    private static String LINUX_RSCRIPT = "Rscript";
    private static String OS_BIN = "bin";
    private static String WHICH_R = "which R";
    private static String WHICH_RSCRIPT = "which Rscript";
        
    public static String getRPath(String path) {
        // Path is null or ""
        if(StringUtils.isEmpty(path)) {
            if(SystemUtils.IS_OS_WINDOWS) {
                return StringUtils.EMPTY;
            } else {
                return getRWhichCommand();
            }
        }
        
        File rFile = new File(path);
        if(rFile.exists()) {

            if(rFile.isFile() ) {
                // path is file
                //String test = rFile.getParent() + filePathSeparator() + WIN_R_EXE;
                if(SystemUtils.IS_OS_WINDOWS) {
                    if(StringUtils.endsWith(path, WIN_RSCRIPT_EXE)  && 
                            new File(rFile.getParent() + filePathSeparator() + WIN_R_EXE).exists()) {
                        return rFile.getParent() + filePathSeparator() + WIN_RSCRIPT_EXE;
                    }
                } else {
                	String filePath = rFile.getAbsolutePath().
           	    	     substring(0,rFile.getAbsolutePath().lastIndexOf(File.separator));
                    if(StringUtils.endsWith(path, LINUX_RSCRIPT) && 
                            new File(filePath + filePathSeparator() + LINUX_R).exists() ) {
                        return filePath + filePathSeparator() + LINUX_RSCRIPT;
                    }
                }
            } else if(rFile.isDirectory()) {
                // path is directory
                String rFilePath = StringUtils.EMPTY;
                String rScriptFilePath = StringUtils.EMPTY;
                if(StringUtils.endsWith(path, OS_BIN)) {
                    if(SystemUtils.IS_OS_WINDOWS) {
                        rFilePath = path + filePathSeparator() + WIN_R_EXE;
                        rScriptFilePath = path + filePathSeparator() + WIN_RSCRIPT_EXE;
                    } else {
                        rFilePath = path + filePathSeparator() + LINUX_R;
                        rScriptFilePath = path + filePathSeparator() + LINUX_RSCRIPT;
                    }
                } else if(StringUtils.endsWith(path, OS_BIN + filePathSeparator())) {
                    if(SystemUtils.IS_OS_WINDOWS) {
                        rFilePath = path + WIN_R_EXE;
                        rScriptFilePath = path + WIN_RSCRIPT_EXE;
                    } else {
                        rFilePath = path + LINUX_R;
                        rScriptFilePath = path + LINUX_RSCRIPT;
                    }
                } else if(StringUtils.endsWith(path, filePathSeparator())) {
                    if(SystemUtils.IS_OS_WINDOWS) {
                        rFilePath = path + OS_BIN + filePathSeparator() + WIN_R_EXE;
                        rScriptFilePath = path + OS_BIN + filePathSeparator() + WIN_RSCRIPT_EXE;
                    } else {
                        rFilePath = path + OS_BIN + filePathSeparator() + LINUX_R;
                        rScriptFilePath = path + OS_BIN + filePathSeparator() + LINUX_RSCRIPT;
                    }
                } else {
                    if(SystemUtils.IS_OS_WINDOWS) {
                        rFilePath = path + filePathSeparator() + OS_BIN + filePathSeparator() + WIN_R_EXE;
                        rScriptFilePath = path + filePathSeparator() + OS_BIN + filePathSeparator() + WIN_RSCRIPT_EXE;
                    } else {
                        rFilePath = path + filePathSeparator() + OS_BIN + filePathSeparator() + LINUX_R;
                        rScriptFilePath = path + filePathSeparator() + OS_BIN + filePathSeparator() + LINUX_RSCRIPT;
                    }
                }
            
                if(new File(rFilePath).exists() && new File(rScriptFilePath).exists()) {
                    return rScriptFilePath;
                }
            }
        }
        return StringUtils.EMPTY;
    }
    
    public static String filePathSeparator() {
        if(SystemUtils.IS_OS_WINDOWS) {
            return "\\";
        } else {
            return "/";
        }
    }
    
    public static String getRWhichCommand() {
        try {
            String rPath = StringUtils.EMPTY;
            String rScriptPath = StringUtils.EMPTY;
            
            Process p = Runtime.getRuntime().exec(WHICH_R);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            if(stdInput != null) {
                rPath = stdInput.readLine();
            }
            
            p = Runtime.getRuntime().exec(WHICH_RSCRIPT);
            stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            if(stdInput != null) {
                rScriptPath = stdInput.readLine();
            }
            
            if(!StringUtils.isEmpty(rPath) && !StringUtils.isEmpty(rScriptPath) 
                    && new File(rPath).exists() && new File(rScriptPath).exists()) {
                return rScriptPath;
            }
        } catch (IOException ex) {
            return StringUtils.EMPTY;
        }
        return StringUtils.EMPTY;
    }
        
    public static RCode getRCodeHeader() {
        RCode rcode = new RCode();
        rcode.setCode(new StringBuffer());
        rcode.addRCode("packageExist<-\"Runiversal\" %in% rownames(installed.packages());\n");
        rcode.addRCode("if(!packageExist){\n");
        rcode.addRCode("install.packages(\"Runiversal\", repos=\"" + Globals.cranRepos + "\");\n");
        rcode.addRCode("}\n");
        rcode.addRCode("packageExist<-\"agricolae\" %in% rownames(installed.packages());\n");
        rcode.addRCode("if(!packageExist){\n");
        rcode.addRCode("install.packages(\"agricolae\", repos=\"" + Globals.cranRepos + "\");\n");
        rcode.addRCode("}\n");
        return rcode;

    }
        
}