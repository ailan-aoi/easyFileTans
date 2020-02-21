package com.mec.mfct.mfctServer;

import com.mec.mfct.file.*;

import java.util.Iterator;

public class DefaultFileDistributeStrategy implements IFileDistributeStrategy {

    @Override
    public SenderTask[] fileDistribute(Resource resource, int senderCount) {
        SenderTask[] tasks = new SenderTask[senderCount];
        initArray(tasks);
        Iterator<ResourceFile> files = resource.getFiles();
        int senderIndex = 0;
        while (files.hasNext()) {
            ResourceFile file = files.next();
            Iterator<OffsetLen> sections = file.getSections();

            while (sections.hasNext()) {
                OffsetLen offsetLen = sections.next();
                long len = offsetLen.getLength();
                long offset = offsetLen.getOffset();

                for (long size = len; size > 0; size -= DEFAULT_SECTION_SIZE, offset += DEFAULT_SECTION_SIZE) {
                    if (size < DEFAULT_SECTION_SIZE) {
                        int count = (int) (size);
                        System.out.println("剩下的长度" + count);
                        tasks[senderIndex].addTask(new MataData(file.getFileId(), offset, count));
                        senderIndex = (senderIndex + 1) % senderCount;
                        break;
                    }
                    tasks[senderIndex].addTask(new MataData(file.getFileId(), offset, DEFAULT_SECTION_SIZE));
                    senderIndex = (senderIndex + 1) % senderCount;
                }
            }
        }

        return tasks;
    }

    private void initArray(SenderTask[] tasks) {
        for (int index = 0 ; index < tasks.length; index++) {
            tasks[index] = new SenderTask();
        }
    }

}
