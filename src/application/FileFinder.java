package application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileFinder {
		
	public static String getAppPath() {
		String appPath = null;
		try {
			appPath = new File("./photos").getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return appPath;
	}
	
	public static FileDisplay getFileDisplayTree() {
		File rootFile = new File(getAppPath());	
		FileDisplay rootFileDisplay = new FileDisplay(rootFile, findChildren(rootFile));
		setFileIcons(rootFileDisplay);
		return rootFileDisplay;
	}
	
	private static void setFileIcons(FileDisplay root) {
		for (FileDisplay child : root.getChildrenFileDisplays()) {
			
			setFileIcons(child);
			
			if (child.getFile().getName().toUpperCase().startsWith("ICON.")) {
				child.getParentFileDisplay().setImage(child.getImage());
				child.getParentFileDisplay().removeChild(child);
			}
			
		}
	}
	
	private static FileDisplay[] findChildren(File root) {
		List<File> files = search(root);
		
		FileDisplay[] children = new FileDisplay[files.size()];
		
		int index = 0;
		for (File file : files) {
			
			if (file.isDirectory()) {
				children[index] = new FileDisplay(file, findChildren(file));
			} else {
				children[index] = new FileDisplay(file);
			}
			
			index++;
		}
		
		return children;
	}
	
	public static List<File> search(File dir) {

		File[] dirsAndFiles = dir.listFiles();
		
		List<File> files = new ArrayList<>();
		
		for (File file : dirsAndFiles) {	
			String name = file.getName().toUpperCase();
			if (file.isDirectory() ||
					name.endsWith("BMP") || 
					name.endsWith("GIF") || 
					name.endsWith("JPEG") || 
					name.endsWith("JPG") || 
					name.endsWith("PNG")) {
				files.add(file);
			}
		}

		return files;
	}
	

}
