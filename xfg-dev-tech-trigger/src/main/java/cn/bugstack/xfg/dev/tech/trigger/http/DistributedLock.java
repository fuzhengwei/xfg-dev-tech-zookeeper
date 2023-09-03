package cn.bugstack.xfg.dev.tech.trigger.http;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;

public class DistributedLock {

    private CuratorFramework curatorFramework;
    private String lockPath;
    private InterProcessMutex mutex;
    private InterProcessSemaphoreMutex semaphoreMutex;
    private InterProcessReadWriteLock readWriteLock;

    public DistributedLock(CuratorFramework curatorFramework, String lockPath) {
        this.curatorFramework = curatorFramework;
        this.lockPath = lockPath;
        this.mutex = new InterProcessMutex(curatorFramework, lockPath);
        this.semaphoreMutex = new InterProcessSemaphoreMutex(curatorFramework, lockPath);
        this.readWriteLock = new InterProcessReadWriteLock(curatorFramework, lockPath);
    }

    public void lock() throws Exception {
        mutex.acquire();
    }

    public void unlock() throws Exception {
        mutex.release();
    }

    public void semaphoreLock() throws Exception {
        semaphoreMutex.acquire();
    }

    public void semaphoreUnlock() throws Exception {
        semaphoreMutex.release();
    }

    public void readLock() throws Exception {
        readWriteLock.readLock().acquire();
    }

    public void readUnlock() throws Exception {
        readWriteLock.readLock().release();
    }

    public void writeLock() throws Exception {
        readWriteLock.writeLock().acquire();
    }

    public void writeUnlock() throws Exception {
        readWriteLock.writeLock().release();
    }

}