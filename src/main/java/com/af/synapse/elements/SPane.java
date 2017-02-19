/**
 * Author: Andrei F.
 *
 * This file is part of the "Synapse" software and is licensed under
 * under the Microsoft Reference Source License (MS-RSL).
 *
 * Please see the attached LICENSE.txt for the full license.
 */

package com.af.synapse.elements;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.af.synapse.MainActivity;
import com.af.synapse.R;
import com.af.synapse.utils.ElementFailureException;
import com.af.synapse.utils.Utils;

import net.minidev.json.JSONObject;

public class SPane extends BaseElement {
    private static Drawable background = null;

    private static int paddingStart = Integer.MIN_VALUE;
    private static int paddingBottom = Integer.MIN_VALUE;
    private static int paddingTop = Integer.MIN_VALUE;

    public SPane(JSONObject elm, LinearLayout layout,
                 MainActivity.TabSectionFragment fragment) throws ElementFailureException {
        super(elm, layout, fragment);

        if (background == null)
            background = Utils.mainActivity.getResources().getDrawable(R.drawable.holo_gradient_red);

        if (paddingStart == Integer.MIN_VALUE)
            paddingStart = (int) (6 * Utils.density + 0.5f);

        if (paddingBottom == Integer.MIN_VALUE)
            paddingBottom = (int) (3 * Utils.density + 0.5f);

        if (paddingTop == Integer.MIN_VALUE)
            paddingTop = (int) (3 * Utils.density + 0.5f);

        if (elm.containsKey("title")) {
            TextView v;

            if (Utils.useInflater) {
                v = (TextView) LayoutInflater.from(Utils.mainActivity)
                        .inflate(R.layout.template_pane_titlebar, this.layout, false);
                assert v != null;
            } else {
                v = new TextView(Utils.mainActivity);
                v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                v.setTypeface(Typeface.DEFAULT_BOLD);
                v.setTextColor(Color.WHITE);
                v.setBackground(background);
                v.setPadding(paddingStart, paddingTop, 0, paddingBottom);
                v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            }

            v.setText(Utils.localise(element.get("title")));
            layout.addView(v);
        }

        if (elm.containsKey("description")) {
            BaseElement descriptionText = BaseElement.createObject("SDescription", elm, layout, fragment);
            layout.addView(descriptionText.getView());
        }
    }
}