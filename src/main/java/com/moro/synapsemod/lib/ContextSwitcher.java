/**
 * Author: Andrei F.
 *
 * This file is part of the "Synapse" software and is licensed under
 * under the Microsoft Reference Source License (MS-RSL).
 *
 * Please see the attached LICENSE.txt for the full license.
 */

package com.moro.synapsemod.lib;

public class ContextSwitcher {
    private static final String globalContext = "global";

    public static String getContext() {
        return globalContext;
    }
}
