package com.topica.automarking;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnzipFile {
	private static final int BUFFER = 2048;
    
    public static void extract(String source, String dest){
        try {
            File root = new File(dest);
            if(!root.exists()){
                root.mkdir();
            }
//            BufferedOutputStream bos = null;
            // zipped input
            FileInputStream fis = new FileInputStream(source);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            while((entry = zis.getNextEntry()) != null) {
                String fileName = entry.getName();
                File file = new File(dest + File.separator + fileName);
                if (!entry.isDirectory()) {
                    extractFileContentFromArchive(file, zis);
                }
                else{
                    if(!file.exists()){
                        file.mkdirs();
                    }
                }
                zis.closeEntry();
               
            }
            zis.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
 
    }
    
    private static void extractFileContentFromArchive(File file, ZipInputStream zis) throws IOException{
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER);
        int len = 0;
        byte data[] = new byte[BUFFER];
        while ((len = zis.read(data, 0, BUFFER)) != -1) {
            bos.write(data, 0, len);
        }
        bos.flush();
        bos.close();
    }
}
