package com.github.snowdream.toybricks.app.impl;

import com.github.snowdream.toybricks.annotation.Implementation;
import com.github.snowdream.toybricks.app.IText;

/**
 * Created by snowdream on 17/2/11.
 */
@Implementation(IText.class)
public class NewTextImpl implements IText {
    @Override
    public String getText() {
        return "NewTextImpl Implementation from "+ getClass().getCanonicalName();
    }
}
