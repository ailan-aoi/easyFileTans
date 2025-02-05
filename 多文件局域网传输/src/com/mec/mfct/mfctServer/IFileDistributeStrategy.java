package com.mec.mfct.mfctServer;

import com.mec.mfct.file.Resource;
import com.mec.mfct.file.SenderTask;

public interface IFileDistributeStrategy {
    int DEFAULT_SECTION_SIZE = 1 << 15;

    SenderTask[] fileDistribute(Resource resource, int senderCount);
}
