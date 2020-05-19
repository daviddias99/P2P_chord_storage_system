package com.feup.sdis.model;

import com.feup.sdis.peer.Constants;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Store {
    private static Store storeInstance;
    final private ReplicationCounter replCount = new ReplicationCounter(Constants.peerRootFolder + "repl.ser");
    final private SerializableHashMap<BackupFileInfo> backedUpFiles = new SerializableHashMap<>(
            Constants.peerRootFolder + "backed.ser");
    final private SerializableHashMap<StoredChunkInfo> storedFiles = new SerializableHashMap<>(
            Constants.peerRootFolder + "stored.ser");
    final private Set<String> chunksSent = Collections.synchronizedSet(new HashSet<>());
    private int usedSpace = 0;

    private Store() {
    }

    public synchronized static Store instance() {
        if (storeInstance == null) {
            storeInstance = new Store();
        }
        return storeInstance;
    }

    public synchronized SerializableHashMap<BackupFileInfo> getBackedUpFiles() {
        return backedUpFiles;
    }

    public synchronized ReplicationCounter getReplCount() {
        return replCount;
    }

    public synchronized SerializableHashMap<StoredChunkInfo> getStoredFiles() {
        return storedFiles;
    }


    public synchronized int getUsedDiskSpace() {
        int total = 0;
        for (Map.Entry<String, StoredChunkInfo> entry : storedFiles.entrySet()) {
            total += entry.getValue().chunkSize;
        }
        return total;
    }

    public Set<String> getChunksSent() {
        return chunksSent;
    }

    public synchronized boolean incrementSpace(int length) {

        if(this.usedSpace + length <= Constants.MAX_OCCUPIED_DISK_SPACE_MB){
            this.usedSpace += length;
            return true;
        }

        return false;
    }

    public synchronized StoredChunkInfo popChunkFromStored(){
        if(this.storedFiles.size() == 0)
            return null;

        int maxFound = -1;
        String chunkToPop = null;
        for (Map.Entry<String,StoredChunkInfo> entry : this.storedFiles.entrySet()) {
            int chunkSize = entry.getValue().chunkSize;
            if (chunkSize == Constants.BLOCK_SIZE) { // there wont be bigger chunks
                chunkToPop = entry.getKey();
                break;
            }

            if (chunkSize > maxFound) {
                maxFound = chunkSize;
                chunkToPop = entry.getKey();
            }
        }
        
        return this.storedFiles.remove(chunkToPop);
    }

}