package cz.cuni.mff.respefo.util;

import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import cz.cuni.mff.respefo.ReSpefo;

public class FileUtils {
	public static String fileOpenDialog(FileType fileType) {
		return fileOpenDialog(fileType, true);
	}
	
	public static String fileOpenDialog(FileType fileType, boolean saveFilterPath) {
		return fileDialog(fileType, saveFilterPath, SWT.OPEN, "Open file");
	}
	
	public static String fileSaveDialog(FileType fileType) {
		return fileSaveDialog(fileType, true);
	}
	
	public static String fileSaveDialog(FileType fileType, boolean saveFilterPath) {
		return fileDialog(fileType, saveFilterPath, SWT.SAVE, "Save file");
	}
	
	private static String fileDialog(FileType fileType, boolean saveFilterPath, int style, String text) {
		FileDialog dialog = new FileDialog(ReSpefo.getShell(), style);
		
		dialog.setText(text);
		dialog.setFilterNames(new String[] {fileType.filterNames(), "All Files"});
		dialog.setFilterExtensions(new String[] {fileType.filterExtensions(), "*"});
		
		dialog.setFilterPath(ReSpefo.getFilterPath());
		
		String fileName = dialog.open();
		
		if (saveFilterPath && fileName != null && Paths.get(fileName).getParent() != null) {
			ReSpefo.setFilterPath(Paths.get(fileName).getParent().toString());
		}
		
		return fileName;
	}
	
	public static String getFileExtension(String fileName) {
		int index = fileName.lastIndexOf('.');
		if (index >= 0) {
			return fileName.substring(index + 1);
		} else {
			return "";
		}
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
}
