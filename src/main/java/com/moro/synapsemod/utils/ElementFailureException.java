/**
 * Author: Andrei F.
 *
 * This file is part of the "Synapse" software and is licensed under
 * under the Microsoft Reference Source License (MS-RSL).
 *
 * Please see the attached LICENSE.txt for the full license.
 */

package com.moro.synapsemod.utils;

import com.moro.synapsemod.elements.BaseElement;

public class ElementFailureException extends Exception {
    private BaseElement source = null;
    private String sourceClassName = null;

    public ElementFailureException (String className, Exception exception) {
        super(exception);
        this.sourceClassName = className;
    }

    public ElementFailureException (BaseElement source, Exception exception) {
        super(exception);
        this.source = source;
        this.sourceClassName = source.getClass().getSimpleName();
    }

    public BaseElement getSource() {
        return source;
    }

    String getSourceClass() {
        return sourceClassName;
    }
}
