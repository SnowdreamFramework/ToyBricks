package com.github.snowdream.toybricks.app.impl;

import com.github.snowdream.toybricks.app.IText;

/**
 * Created by snowdream on 17/2/11.
 */
public class TextImpl implements IText {
    @Override
    public String getText() {
        return "Defalt Implementation from "+ getClass().getCanonicalName();
    }
}
