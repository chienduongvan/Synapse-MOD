/**
 * Author: Andrei F.
 *
 * This file is part of the "Synapse" software and is licensed under
 * under the Microsoft Reference Source License (MS-RSL).
 *
 * Please see the attached LICENSE.txt for the full license.
 */

package com.af.synapse.utils;

public class NamedRunnable implements Runnable {
    private final Runnable runnable;

    public NamedRunnable(Runnable command) {
        this.runnable = command;
    }

    public void run() {
        String originalName = Thread.currentThread().getName();

        try {
            String name = getName();
            Thread.currentThread().setName(originalName+ ": " + name);
            runnable.run();
        } finally {
            Thread.currentThread().setName(originalName);
        }
    }

    public String getName() {
        return "Unnamed";
    }
}
