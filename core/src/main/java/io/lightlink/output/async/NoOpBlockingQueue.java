package io.lightlink.output.async;

/*
 * #%L
 * LightLink Core
 * %%
 * Copyright (C) 2015 Vitaliy Shevchuk
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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
