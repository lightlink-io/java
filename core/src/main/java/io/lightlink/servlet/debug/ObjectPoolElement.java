package io.lightlink.servlet.debug;

public class ObjectPoolElement {
    private long lastAccess;
    private long generation;
    private int objectId;
    private Object object;

    ObjectPoolElement(long generation, int objectId, Object object) {
        this.generation = generation;
        this.objectId = objectId;
        this.object = object;
    }

    public long getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(long lastAccess) {
        this.lastAccess = lastAccess;
    }

    public long getGeneration() {
        return generation;
    }

    public void setGeneration(long generation) {
        this.generation = generation;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public Object getObject() {
        lastAccess = System.currentTimeMillis();
        return object;
    }

    public void setObject(Object object) {
        lastAccess = System.currentTimeMillis();
        this.object = object;
    }
}
