package io.lightlink.output.async;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class NoOpBlockingQueue implements java.util.concurrent.BlockingQueue<QueueElement> {

    @Override
    public boolean add(QueueElement queueElement) {
        return false;
    }

    @Override
    public boolean offer(QueueElement queueElement) {
        return false;
    }

    @Override
    public void put(QueueElement queueElement) throws InterruptedException {

    }

    @Override
    public boolean offer(QueueElement queueElement, long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public QueueElement take() throws InterruptedException {
        return null;
    }

    @Override
    public QueueElement poll(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public int remainingCapacity() {
        return 0;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public int drainTo(Collection<? super QueueElement> c) {
        return 0;
    }

    @Override
    public int drainTo(Collection<? super QueueElement> c, int maxElements) {
        return 0;
    }

    @Override
    public QueueElement remove() {
        return null;
    }

    @Override
    public QueueElement poll() {
        return null;
    }

    @Override
    public QueueElement element() {
        return null;
    }

    @Override
    public QueueElement peek() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Iterator<QueueElement> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends QueueElement> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }
}
