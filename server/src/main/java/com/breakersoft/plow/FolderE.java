package com.breakersoft.plow;

import java.util.UUID;

public class FolderE implements Folder {

    private UUID projectId;
    private UUID folderId;

    @Override
    public UUID getFolderId() {
        return folderId;
    }

    @Override
    public UUID getProjectId() {
        return projectId;
    }

    public void setFolderId(UUID folderId) {
        this.folderId = folderId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public int hashCode() {
        return folderId.hashCode();
    }
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;

        Folder other = (Folder) obj;
        return folderId.equals(other.getFolderId());
    }

}
