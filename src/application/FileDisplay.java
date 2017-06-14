package application;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Either an image or a directory that gets a default image.
 * 
 */
public class FileDisplay extends ImageView {

	public static final Image FOLDER_IMAGE = new Image("folder.png");
	
	private File file;
	private FileDisplay parentFileDisplay;
	private List<FileDisplay> childrenFileDisplays;
	
	public FileDisplay() {
		this(null);
	}
	
	public FileDisplay(File file) {
		this(file, new FileDisplay[]{});
	}
	
	public FileDisplay(File file, FileDisplay ... children) {
		this.file = file;
		childrenFileDisplays = new LinkedList<>();
		
		for (FileDisplay child : children) {
			addChild(child);
		}
		if (file == null) {
			// no file? Maybe left/right indicator hack.
		} else if (file.isDirectory()) {
			setImage(FOLDER_IMAGE);
		} else {
			setImage(new Image(file.toURI().toString()));
			setPreserveRatio(true);
		}
	}
	
	public void addChild(FileDisplay child) {
		childrenFileDisplays.add(child);
		child.setParentFileDisplay(this);
	}
	
	public void removeChild(FileDisplay child) {
		childrenFileDisplays.remove(child);
		child.setParentFileDisplay(null);
	}
	
	public List<FileDisplay> getChildrenFileDisplays() {
		return childrenFileDisplays;
	}
	
	public File getFile() {
		return file;
	}
	
	public void setParentFileDisplay(FileDisplay parent) {
		this.parentFileDisplay = parent;
	}
	
	public FileDisplay getParentFileDisplay() {
		return parentFileDisplay;
	}
	
	public int getIndexInParent() {
		return getParentFileDisplay().getChildrenFileDisplays().indexOf(this);
	}
	
	public FileDisplay getNextChild() {
		int index = (getIndexInParent() + 1) % getParentFileDisplay().getChildrenFileDisplays().size();
		return getParentFileDisplay().getChildrenFileDisplays().get(index);
	}
	
	public FileDisplay getPrevious() {
		int size = getParentFileDisplay().getChildrenFileDisplays().size();
		// Always stay positive.
		int index = (getIndexInParent() + size - 1) % size;
		return getParentFileDisplay().getChildrenFileDisplays().get(index);
	}
	
	public int getCountCousin() {
		if (getParentFileDisplay() == null) {
			return 0;
		}
		
		return getParentFileDisplay().getChildrenFileDisplays().size() - 1;
	}
	
	public boolean isRegularFolder() {
		return getImage() == FOLDER_IMAGE;
	}
	
	public String toString() {
		String resultSoFar = " (" + file.getName() + " [";
		
		for (FileDisplay child : getChildrenFileDisplays()) {
			resultSoFar += child;
		}
	
		return resultSoFar + "])";
	}

}
