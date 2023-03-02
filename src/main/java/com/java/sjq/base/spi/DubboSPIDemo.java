package com.java.sjq.base.spi;

import org.apache.dubbo.common.extension.ExtensionLoader;

public class DubboSPIDemo {
    public static void main(String[] args){
      //
        ExtensionLoader<ICar> loader = ExtensionLoader.getExtensionLoader(ICar.class);
        System.out.println();
        ICar audiCar = loader.getDefaultExtension();
        System.out.printf(audiCar.getName());
    }
}
