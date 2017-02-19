/**
 * Author: Andrei F.
 *
 * This file is part of the "Synapse" software and is licensed under
 * under the Microsoft Reference Source License (MS-RSL).
 *
 * Please see the attached LICENSE.txt for the full license.
 */

package com.moro.synapsemod.elements;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.moro.synapsemod.MainActivity;
import com.moro.synapsemod.R;
import com.moro.synapsemod.utils.Utils;

import net.minidev.json.JSONObject;

public class STitleBar extends BaseElement {
    private static Drawable background = null;
    private static int paddingStart = Integer.MIN_VALUE;
    private static int paddingBottom = Integer.MIN_VALUE;
    private static int paddingTop = Integer.MIN_VALUE;

    public STitleBar(JSONObject element, LinearLayout layout, MainActivity.TabSectionFragment fragment) {
        super(element, layout, fragment);

        if (background == null)
            background = Utils.mainActivity.getResources().getDrawable(R.drawable.holo_gradient_red);

        if (paddingStart == Integer.MIN_VALUE)
            paddingStart = (int) (6 * Utils.density + 0.5f);

        if (paddingBottom == Integer.MIN_VALUE)
            paddingBottom = (int) (3 * Utils.density + 0.5f);

        if (paddingTop == Integer.MIN_VALUE)
            paddingTop = (int) (3 * Utils.density + 0.5f);
    }

    @Override
    public View getView() {
        TextView v;

        if (Utils.useInflater) {
            v = (TextView) LayoutInflater.from(Utils.mainActivity)
                    .inflate(R.layout.template_titlebar, this.layout, false);
            assert v != null;
        } else {
            v = new TextView(Utils.mainActivity);
            v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            v.setTypeface(Typeface.DEFAULT_BOLD);
            v.setTextColor(Color.WHITE);
            v.setBackground(background);
            v.setPadding(paddingStart, paddingTop, 0, paddingBottom);
        }

        if (element.containsKey("background"))
            v.setBackground(null);

        Object title = element.get("title");
        v.setText(Utils.localise(title));

        return v;
    }
}
