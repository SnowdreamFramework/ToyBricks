package com.github.snowdream.toybricks.processor;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by snowdream on 17/2/12.
 */
public class ProcessorManager implements Handler {
    private ProcessingEnvironment mProcessingEnvironment;
    private List<BaseContainerHandler> mHandlers;

    private Types mTypeUtils;
    private Elements mElementUtils;
    private Filer mFiler;
    private Messager mMessager;


    ProcessorManager(ProcessingEnvironment processingEnv) {
        mProcessingEnvironment = processingEnv;

        mTypeUtils = processingEnv.getTypeUtils();
        mElementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
        mMessager = processingEnv.getMessager();
    }

    void addHandlers(BaseContainerHandler... containerHandlers) {
        if (containerHandlers == null || containerHandlers.length <= 0) return;

        for (BaseContainerHandler handler :
                containerHandlers) {

            if (mHandlers == null) {
                mHandlers = new ArrayList<>();
            }

            if (!mHandlers.contains(handler)) {
                mHandlers.add(handler);
            }
        }
    }

    @Override
    public void handle(ProcessorManager processorManager, RoundEnvironment roundEnvironment) {
        if (mHandlers != null){
            for (BaseContainerHandler handler:
                    mHandlers ) {
                handler.handle(processorManager,roundEnvironment);
            }
        }

    }

    /**
     * Prints an error message
     *
     * @param e The element which has caused the error. Can be null
     * @param msg The error message
     */
    public void error(Element e, String msg) {
        mMessager.printMessage(Diagnostic.Kind.ERROR, msg, e);
    }

    /**
     * Prints an info message
     *
     * @param e The element which has caused the error. Can be null
     * @param msg The error message
     */
    public void info(Element e, String msg) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, msg, e);
    }

    public Types getTypeUtils(){
        return mTypeUtils;
    }

    public Elements getElementUtils(){
        return mElementUtils;
    }

    public Filer getFiler(){
        return mFiler;
    }

    public Messager getMessager(){
        return mMessager;
    }
}
