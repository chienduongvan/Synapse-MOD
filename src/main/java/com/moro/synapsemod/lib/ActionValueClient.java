/**
 * Author: Andrei F.
 *
 * This file is part of the "Synapse" software and is licensed under
 * under the Microsoft Reference Source License (MS-RSL).
 *
 * Please see the attached LICENSE.txt for the full license.
 */

package com.moro.synapsemod.lib;

import com.moro.synapsemod.utils.ElementFailureException;

public interface ActionValueClient {
    String getLiveValue() throws ElementFailureException;
    String getSetValue();
    String getStoredValue();

    void refreshValue() throws ElementFailureException;
    void setDefaults();
    void applyValue() throws ElementFailureException;
    void cancelValue() throws ElementFailureException;
}
