package com.mec.mfct.file;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class ResourceFileSet implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 8253458003406553704L;
	private String absoultePathPart;
    private Map<Integer, ResourceFile> files;

    ResourceFileSet() {
        this("");
    }

    ResourceFileSet(String absoultePathPart) {
        this.absoultePathPart = absoultePathPart;
        files = new HashMap<>();
    }

    void setAbsoultePathPart(String absoultePathPart) {
        this.absoultePathPart = absoultePathPart;
    }

    boolean checkFileExist() {
        if (absoultePathPart == null) {
            System.out.println("请设置AbsoultePartPath!!!");
            return false;
        }
        return isFileExist();
    }

    private boolean isFileExist() {
        boolean isAllExist = true;
        for (ResourceFile rsFile : files.values()) {
            String absoultePath = absoultePathPart + rsFile.getFilePath();
            File file = new File(absoultePath);
            if (file.exists()) {
                if (file.length() == rsFile.getLength()) {
                    rsFile.setFinished();
                    continue;
                }
            }
            isAllExist = false;
        }

        return isAllExist;
    }

    void addFile(int fileId, long fileSize, String filePath) {
        ResourceFile file = new ResourceFile(fileSize);
        file.setFilePath(filePath);
        file.setFileId(fileId);
        files.put(fileId, file);
    }
    
    void receivedOneFileSection(int fileId, long offset, byte[] fileBlock) {
        int len = fileBlock.length;

        ResourceFile file = files.get(fileId);
        String fileAbsoultePath = absoultePathPart + file.getFilePath();

        RandomAccessFile raf = FileHandlePool.getFileHandle(fileAbsoultePath, "rw");
        file.receivedOneSection(raf, fileBlock, offset, len);

        if (file.isFinished()) {
            FileHandlePool.closeFileHandle(fileAbsoultePath);
        }
    }

    byte[] sentOneFileSection(int fileId, long offset, int len) {
        ResourceFile file = files.get(fileId);
        String fileAbsoultePath = absoultePathPart + file.getFilePath();
        RandomAccessFile raf = FileHandlePool.getFileHandle(fileAbsoultePath, "r");

        return file.readFileSection(raf, offset, len);
    }

    int getFilesCount() {
        return files.size();
    }

    Iterator<ResourceFile> getFiles() {
        return files.values().iterator();
    }

    public ResourceFile getFile(int fileId) {
        return files.get(fileId);
    }

    @Override
    public String toString() {
        return "ResourceFileSet{" +
//                "absoultePathPart='" + absoultePathPart + '\'' +
                ", files=" + files +
                '}';
    }
}
