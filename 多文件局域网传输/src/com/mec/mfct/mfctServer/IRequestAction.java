package com.mec.mfct.mfctServer;

import com.mec.mfct.file.Resource;

public interface IRequestAction {
    Resource requestResource(int resourceId);
    boolean registryResource(int resourceId, Resource resource);
    void healthCheck(int resourceId, String ip, boolean resourceIsExist);
    boolean registryResourceOwner(int resourceId, String ip);
    void revokeResource(int resourceId);
    void alterResource(int resourceId, Resource resource);
}
