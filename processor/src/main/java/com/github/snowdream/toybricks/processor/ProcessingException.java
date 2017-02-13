package com.github.snowdream.toybricks.processor;

import javax.lang.model.element.Element;

/**
 * Created by snowdream on 17/2/12.
 */
public class ProcessingException extends Exception {

  Element element;

  public ProcessingException(Element element, String msg, Object... args) {
    super(String.format(msg, args));
    this.element = element;
  }

  public Element getElement() {
    return element;
  }
}
