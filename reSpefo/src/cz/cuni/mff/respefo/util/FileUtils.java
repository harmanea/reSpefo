package cz.cuni.mff.respefo.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;

import cz.cuni.mff.respefo.ReSpefo;

public class FileUtils {
	private static final Logger LOGGER = Logger.getLogger(FileUtils.class.getName());
	
	private static String filterPath;
	static {
		try {
			filterPath = System.getProperty("user.dir");
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Couldn't determine current user directory", e);
			filterPath = "";
		}
	}
	
	public static String getFilterPath() {
		return filterPath;
	}

	public static void setFilterPath(String filterPath) {
		FileUtils.filterPath = filterPath;
	}

	public static String fileOpenDialog(FileType fileType) {
		return fileOpenDialog(fileType, true);
	}
	
	public static String fileOpenDialog(FileType fileType, boolean saveFilterPath) {
		return fileDialog(fileType, saveFilterPath, SWT.OPEN, "Open file", null);
	}
	
	public static String fileOpenDialog(FileType fileType, String defaultFileName) {
		return fileDialog(fileType, true, SWT.OPEN, "Open file", defaultFileName);
	}
	
	public static String fileSaveDialog(FileType fileType) {
		return fileSaveDialog(fileType, true);
	}
	
	public static String fileSaveDialog(FileType fileType, boolean saveFilterPath) {
		return fileDialog(fileType, saveFilterPath, SWT.SAVE, "Save file", null);
	}
	
	private static String fileDialog(FileType fileType, boolean saveFilterPath, int style, String text, String defaultFileName) {
		FileDialog dialog = new FileDialog(ReSpefo.getShell(), style);
		
		dialog.setText(text);
		dialog.setFilterNames(new String[] {fileType.filterNames(), "All Files"});
		dialog.setFilterExtensions(new String[] {fileType.filterExtensions(), "*"});
		dialog.setFilterPath(getFilterPath());
		
		if (defaultFileName != null) {
			dialog.setFileName(defaultFileName);
		} else if (style == SWT.SAVE && ReSpefo.getSpectrum() != null) {
			dialog.setFileName(ReSpefo.getSpectrum().getName());
		}
		
		String fileName = dialog.open();
		
		if (saveFilterPath && fileName != null && Paths.get(fileName).getParent() != null) {
			setFilterPath(Paths.get(fileName).getParent().toString());
		}
		
		return fileName;
	}
	
	public static String directoryDialog() {
		return directoryDialog(true);
	}
	
	public static String directoryDialog(boolean saveFilterPath) {
		DirectoryDialog dialog = new DirectoryDialog(ReSpefo.getShell());
		
		dialog.setText("Choose directory");
		dialog.setFilterPath(getFilterPath());
		
		String directoryName = dialog.open();
		
		if (saveFilterPath && directoryName != null) {
			setFilterPath(Paths.get(directoryName).toString());
		}
		
		return directoryName;
	}
	
	public static List<String> multipleFilesDialog(FileType fileType) {
		return multipleFilesDialog(fileType, "Choose files");
	}
	
	public static List<String> multipleFilesDialog(FileType fileType, String text) {
		FileDialog dialog = new FileDialog(ReSpefo.getShell(), SWT.OPEN | SWT.MULTI);
		
		dialog.setText(text);
		dialog.setFilterNames(new String[] {fileType.filterNames(), "All Files"});
		dialog.setFilterExtensions(new String[] {fileType.filterExtensions(), "*"});
		dialog.setFilterPath(getFilterPath());
		
		dialog.open();
		
		return Arrays.stream(dialog.getFileNames())
			.map(fileName -> dialog.getFilterPath() + File.separator + fileName)
			.collect(Collectors.toList());
	}
	
	public static String getFileExtension(String fileName) {
		int index = fileName.lastIndexOf('.');
		if (index >= 0) {
			return fileName.substring(index + 1);
		} else {
			return "";
		}
	}
	
	public static String stripFileExtension(String fileName) {
		int index = fileName.lastIndexOf('.');
		if (index >= 0) {
			return fileName.substring(0, index);
		} else {
			return fileName;
		}
	}
	
	public static String getParentDirectory(String fileName) {
		Path parent = Paths.get(fileName).getParent();
		if (parent != null) {
			return parent.toString();
		} else {
			return "";
		}
	}
	
	public static String stripParent(String fileName) {
		return Paths.get(fileName).getFileName().toString();
	}
	
	/**
	 * Increments the last number in a file name
	 * <br/>
	 * For example: fileName001.txt --> filename002.txt
	 */
	public static String incrementFileName(String fileName) {
		String fileExtension = getFileExtension(fileName);
		if (fileExtension.length() > 0) {
			fileName = fileName.substring(0, fileName.length() - fileExtension.length());
		}
		
		Pattern pattern = Pattern.compile("[0-9]+");
		Matcher matcher = pattern.matcher(fileName);
		
		int start = 0, end = 0;
		String group = null;
		while (matcher.find()) {
			start = matcher.start();
			end = matcher.end();
			group = matcher.group();
		}
		
		if (group == null) {
			return null;
		}
		
		int number = Integer.parseInt(group);
		number += 1;
		
		group = Integer.toString(number);
		for (int i = group.length(); i < end - start; i++) {
			group = '0' + group;
		}
		
		fileName = fileName.substring(0, start) + group + fileName.substring(end);
		
		return fileName + fileExtension;
		
	}
	
	public static File firstUniqueFileName(String fileName, String extension) {
		File file = new File(fileName + "." + extension); 
		for (int num = 1; file.exists() && num < Integer.MAX_VALUE; num++) {
		    file = new File(fileName + " (" + num + ")." + extension);
		}
		
		return file;
	}
	
	public static void renameFile(String oldFileName, String newFileName) throws IOException {
		Path source = Paths.get(oldFileName);
		Files.move(source, source.resolveSibling(newFileName), StandardCopyOption.REPLACE_EXISTING);
	}
	
	public static void clearFileIfExists(File file) {
		if (file.exists()) {
			try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
				randomAccessFile.setLength(0);
				
			} catch (Exception e) {
				// ignore exception
			}
		}
	}
}
