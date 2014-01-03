package hr.fer.zemris.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtil {
	
	public static File zipFolder(File root) throws IOException {
		
		if (root==null)
			return null;
		if (!root.isDirectory())
			return null;
		
		File out = File.createTempFile("SHA", null);
		
		ZipOutputStream os = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(out)));
		
		addFolder("", root, os);
		os.flush();
		os.close();
		
		return out;
	}
	
	public static File zipFolder(File root, Set<String> topLevelDirFilter) throws IOException {
		
		if (root==null)
			return null;
		if (!root.isDirectory())
			return null;
		
		File out = File.createTempFile("SHA", null);
		
		ZipOutputStream os = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(out)));
		if(topLevelDirFilter==null) {
			addFolder("", root, os);
		} else {
			addFolder("", root, os, topLevelDirFilter);
		}
		os.flush();
		os.close();
		
		return out;
	}
	
	private static void addFolder(String path, File folder, ZipOutputStream os, Set<String> topLevelDirFilter) throws IOException{
		for (File f : folder.listFiles()) {
			if (f.isDirectory()) {
				if(topLevelDirFilter!=null && !topLevelDirFilter.contains(f.getName())) continue;
				if (path.equals(""))
					addFolder(f.getName(),f,os);
				else
					addFolder(path+"/"+f.getName(),f,os);
			}
			else {
				byte[] buff = new byte[32*1024];
				
				BufferedInputStream is = new BufferedInputStream(new FileInputStream(f));
				try {
					os.putNextEntry(new ZipEntry(path+"/"+f.getName()));
					int procitano = -1;
					while ((procitano=is.read(buff))!=-1) {
						os.write(buff, 0, procitano);
					}
					os.flush();
					os.closeEntry();
				} finally {
					try { is.close(); } catch(Exception ignorable) {}
				}
			}
		}
	}

	private static void addFolder(String path, File folder, ZipOutputStream os) throws IOException{
		for (File f : folder.listFiles()) {
			if (f.isDirectory()) {
				if (path.equals(""))
					addFolder(f.getName(),f,os);
				else
					addFolder(path+"/"+f.getName(),f,os);
			}
			else {
				byte[] buff = new byte[32*1024];
				
				BufferedInputStream is = new BufferedInputStream(new FileInputStream(f));
				try {
					os.putNextEntry(new ZipEntry(path+"/"+f.getName()));
					int procitano = -1;
					while ((procitano=is.read(buff))!=-1) {
						os.write(buff, 0, procitano);
					}
					os.flush();
					os.closeEntry();
				} finally {
					try { is.close(); } catch(Exception ignorable) {}
				}
			}
		}
	}

	private static Map<String,String> extensionToMimeMap;
	static {
		extensionToMimeMap = new HashMap<String, String>();
		extensionToMimeMap.put("", "application/octet-stream");
		extensionToMimeMap.put("pdf", "application/pdf");
		extensionToMimeMap.put("jpg", "image/jpg");
		extensionToMimeMap.put("gif", "image/gif");
		extensionToMimeMap.put("png", "image/png");
		extensionToMimeMap.put("doc", "application/msword");
		extensionToMimeMap.put("xls", "application/vnd.ms-excel");
		extensionToMimeMap.put("ppt", "application/vnd.ms-powerpoint");
		extensionToMimeMap.put("txt", "text/plain");
	}

	/**
	 * Vraća mime-tip pridružen ekstenziji. Popis podržanih mime-tipova nije
	 * pretjerano velik. Ako se tip ne zna, vraća se "application/octet-stream".
	 * 
	 * @param ext ekstenzija; mora biti napisana malim slovima bez početne točke
	 * @return mime-tip
	 */
	public static String findMimeTypeForExtension(String ext) {
		if(ext==null) return "application/octet-stream";
		String mime = extensionToMimeMap.get(ext);
		if(mime==null) return "application/octet-stream";
		return mime;
	}

	/**
	 * Vraća pronađenu ekstenziju datoteke, napisanu malim slovima i bez točke na početku.
	 * Ako nema ekstenzije, vraća se prazan string.
	 * 
	 * @param name naziv datoteke
	 * @return ekstenzija
	 */
	public static String findExtension(String name) {
		int p = name.lastIndexOf('.');
		if(p==-1) return "";
		return name.substring(p+1).toLowerCase();
	}

	/**
	 * Pomoćna metoda koja čita ulazni stream i kopira ga u izlazni. Metoda ne obrađuje
	 * pogreške - to se ostavlja pozivatelju.
	 * 
	 * @param in ulazni stream
	 * @param out izlazni stream
	 * @throws IOException u slučaju pogreške
	 */
	public static void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[10240];
	    int len;

	    while((len = in.read(buffer)) >= 0) {
	      out.write(buffer, 0, len);
	    }
	}

}
