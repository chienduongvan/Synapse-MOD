package com.af.synapse.lib;

import android.view.View;

public interface Selectable extends View.OnLongClickListener {
    void setSelectable(boolean flag);
    void select();
    void deselect();
    boolean isSelected();
}
