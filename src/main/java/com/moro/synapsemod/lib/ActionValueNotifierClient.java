/**
 * Author: Andrei F.
 *
 * This file is part of the "Synapse" software and is licensed under
 * under the Microsoft Reference Source License (MS-RSL).
 *
 * Please see the attached LICENSE.txt for the full license.
 */

package com.moro.synapsemod.lib;

public interface ActionValueNotifierClient extends ActionValueClient {
    String getId();
    void onNotify(ActionValueNotifierClient source, ActionValueEvent notification);
}
