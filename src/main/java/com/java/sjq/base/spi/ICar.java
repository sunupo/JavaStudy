package com.java.sjq.base.spi;

import org.apache.dubbo.common.extension.SPI;

@SPI("AudiCar")
public interface ICar {
    String getName();
}
