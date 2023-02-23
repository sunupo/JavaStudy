package com.java.sjq.base.methodHandle;

import javafx.util.Pair;
import sun.misc.Lock;

import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

    public MethodHandles.Lookup getLookUp(boolean isPublic) {
        return isPublic ? MethodHandles.publicLookup() : MethodHandles.lookup();
    }

    public void jgg(){
        System.out.println("jgg");

    }




}
