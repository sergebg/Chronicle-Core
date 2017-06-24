/*
 * Copyright 2016 higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicLong;

public class ReferenceCounter {
    private final AtomicLong value = new AtomicLong(1);
    private final Runnable onRelease;

    private ReferenceCounter(Runnable onRelease) {
        this.onRelease = onRelease;
    }

    @NotNull
    public static ReferenceCounter onReleased(Runnable onRelease) {
        return new ReferenceCounter(onRelease);
    }

    public void reserve() throws IllegalStateException {
        long v =  value.getAndIncrement();
        if (v <= 0) {
            value.decrementAndGet();
            throw new IllegalStateException("Released, counter=" + v);
        }
    }

    public void release() throws IllegalStateException {
        long v = value.decrementAndGet();
        if (v < 0) {
            value.getAndIncrement();
            throw new IllegalStateException("Released, counter=" + v);
        }
        if (v == 0) {
            onRelease.run();
        }
    }

    public long get() {
        return value.get();
    }

    @NotNull
    @Override
    public String toString() {
        return Long.toString(value.get());
    }

}
