/**
 * Author: Andrei F.
 *
 * This file is part of the "Synapse" software and is licensed under
 * under the Microsoft Reference Source License (MS-RSL).
 *
 * Please see the attached LICENSE.txt for the full license.
 */

package com.moro.synapsemod.elements;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.moro.synapsemod.MainActivity;
import com.moro.synapsemod.Synapse;
import com.moro.synapsemod.lib.ActionNotification;
import com.moro.synapsemod.lib.ActionValueEvent;
import com.moro.synapsemod.lib.ActionValueNotifierClient;
import com.moro.synapsemod.lib.ActionValueNotifierHandler;
import com.moro.synapsemod.lib.ActivityListener;
import com.moro.synapsemod.utils.ElementFailureException;
import com.moro.synapsemod.utils.L;
import com.moro.synapsemod.R;
import com.moro.synapsemod.utils.Utils;

import net.minidev.json.JSONObject;

import java.util.ArrayDeque;

public class SButton extends BaseElement implements View.OnClickListener,
                                                    ActionValueNotifierClient, ActivityListener {
    private String command;

    public SButton(JSONObject element, LinearLayout layout,
                   MainActivity.TabSectionFragment fragment) {
        super(element, layout, fragment);

        if (element.containsKey("action"))
            this.command = element.get("action").toString();
        else
            L.w("Button without action detected!");

        ActionValueNotifierHandler.register(this);
    }

    @Override
    public View getView() {
        Button button = (Button) LayoutInflater.from(Utils.mainActivity)
                .inflate(R.layout.template_button, this.layout, false);

        if (this.element.containsKey("label"))
            button.setText(Utils.localise(this.element.get("label")));

        button.setOnClickListener(this);

        return button;
    }

    @Override
    public void onClick(View view) {
        Toast t;
        try {
            String result = Utils.runCommand(command);
            t = Toast.makeText(Utils.mainActivity, result, Toast.LENGTH_LONG);
            ActionValueNotifierHandler.propagate(this, ActionValueEvent.APPLY);
        } catch (Exception e) {
            t = Toast.makeText(Utils.mainActivity, e.getMessage(), Toast.LENGTH_LONG);
        }

        if (t.getView() instanceof ViewGroup) {
            Object tv = ((ViewGroup) t.getView()).getChildAt(0);

            if (tv instanceof TextView)
                ((TextView) tv).setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        }

        t.show();
    }

    /**
     *  ActionValueNotifierClient methods
     */

    @Override
    public String getId() {
        return command;
    }

    private ArrayDeque<ActionNotification> queue = new ArrayDeque<>();
    private boolean jobRunning = false;

    private void handleNotifications() {
        jobRunning = true;
        while (queue.size() > 0) {
            ActionNotification current = queue.removeFirst();
            switch (current.notification) {
                case APPLY:
                    onClick(null);
                    break;
            }
        }
        jobRunning = false;
    }

    private Runnable dequeJob = new Runnable() {
        @Override
        public void run() {
            handleNotifications();
        }
    };

    @Override
    public void onNotify(ActionValueNotifierClient source, ActionValueEvent notification) {
        L.d(notification.toString());
        queue.add(new ActionNotification(source, notification));

        if (queue.size() == 1 && !jobRunning)
            Synapse.handler.post(dequeJob);
    }

    /**
     *  ActionValueClient methods
     */

    @Override
    public String getLiveValue() { return null; }

    @Override
    public String getSetValue() { return null; }

    @Override
    public String getStoredValue() { return null; }

    @Override
    public void refreshValue() {}

    @Override
    public void setDefaults() {}

    @Override
    public void applyValue() {}

    @Override
    public void cancelValue() {}

    @Override
    public void onMainStart() {
        if (!Utils.mainActivity.isChangingConfigurations() && !Utils.appStarted)
            try {
                ActionValueNotifierHandler.addNotifiers(this);
            } catch (Exception e) {
                Utils.createElementErrorView(new ElementFailureException(this, e));
            }
    }

    @Override
    public void onStart() {}

    @Override
    public void onResume() {}

    @Override
    public void onPause() {}
}