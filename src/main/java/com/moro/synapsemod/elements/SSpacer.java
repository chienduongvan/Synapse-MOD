package com.moro.synapsemod.elements;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.moro.synapsemod.MainActivity;
import com.moro.synapsemod.utils.Utils;

import net.minidev.json.JSONObject;

public class SSpacer extends BaseElement {
    public SSpacer(JSONObject element, LinearLayout layout, MainActivity.TabSectionFragment fragment) {
        super(element, layout, fragment);

        View v = layout.getChildAt(layout.getChildCount() - 1);
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

        int height = 1;
        if (element.containsKey("height"))
            height = (Integer) element.get("height");

        lp.bottomMargin += (int) (12 * height * Utils.density + 0.5f);
    }
}
