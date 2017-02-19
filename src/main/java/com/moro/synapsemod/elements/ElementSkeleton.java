/**
 * Author: Andrei F.
 *
 * This file is part of the "Synapse" software and is licensed under
 * under the Microsoft Reference Source License (MS-RSL).
 *
 * Please see the attached LICENSE.txt for the full license.
 */

package com.moro.synapsemod.elements;

import android.view.View;
import android.widget.LinearLayout;

import com.moro.synapsemod.MainActivity;
import com.moro.synapsemod.utils.ElementFailureException;

import net.minidev.json.JSONObject;

abstract class ElementSkeleton {
    public JSONObject element;
    public LinearLayout layout;
    public MainActivity.TabSectionFragment fragment;

    abstract public View getView() throws ElementFailureException;
}
