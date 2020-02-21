package com.mec.mfct.file;

import java.io.Serializable;

import com.my.util.core.ByteAndDight;

public class MataData implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6082593081567782997L;
	private int fileId;
    private long offset;
    private int length;

    public MataData(byte[] mataByte) {
        byte[] bytes = new byte[4];
        setFileId(ByteAndDight.bytes2int(divisionArray(bytes, mataByte, 0)));
        setLength(ByteAndDight.bytes2int(divisionArray(bytes, mataByte, 4)));
        bytes = new byte[8];
        setOffset(ByteAndDight.bytes2long(divisionArray(bytes, mataByte, 8)));
    }

    public byte[] getBytes() {
        byte[] bytes = new byte[16];
        compineArray(bytes, ByteAndDight.int2bytes(fileId), 0);
        compineArray(bytes, ByteAndDight.int2bytes(length), 4);
        compineArray(bytes, ByteAndDight.long2bytes(offset), 8);
        return bytes;
    }

    public MataData(int fileId, long offset, int length) {
        this.fileId = fileId;
        this.offset = offset;
        this.length = length;
    }

    private void compineArray(byte[] target, byte[] resource, int targetOffset) {
        int size = resource.length;
        for (int index = 0; index < size; index++) {
            target[targetOffset + index] = resource[index];
        }
    }

    private byte[] divisionArray(byte[] target, byte[] resource, int resourceOffset) {
        int size = target.length;
        for (int index = 0; index < size; index++) {
            target[index] = resource[resourceOffset + index];
        }

        return target;
    }

    int getFileId() {
        return fileId;
    }

    void setFileId(int fileId) {
        this.fileId = fileId;
    }

    long getOffset() {
        return offset;
    }

    void setOffset(long offset) {
        this.offset = offset;
    }

    int getLength() {
        return length;
    }

    void setLength(int length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return "MataData{" +
                "fileId=" + fileId +
                ", offset=" + offset +
                ", length=" + length +
                '}';
    }
}
