package com.breakersoft.plow.event;

import com.breakersoft.plow.Folder;

public class FolderCreatedEvent implements Event {

    public Folder folder;

    public FolderCreatedEvent(Folder folder) {
        this.folder = folder;
    }

}
