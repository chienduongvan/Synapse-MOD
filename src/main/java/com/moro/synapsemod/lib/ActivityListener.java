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

public interface ActivityListener {
    void onMainStart() throws ElementFailureException;
    void onStart() throws ElementFailureException;
    void onResume() throws ElementFailureException;
    void onPause() throws ElementFailureException;
}
