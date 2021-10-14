package com.soecode.lyf.java;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BuyBook{
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String name;

    public BuyBook() {
    }

    public BuyBook(String name) {
        this.name = name;
    }

    public void buy(){
        System.out.println("buy: book");
        logger.debug("buy: book");
    }
    public void buy2(){
        System.out.println("buy2: book");
        logger.debug("buy2:book");
    }
    public int sell(int price){

        logger.debug("sell: " + price);
        return price;
    }

    @Override
    public String toString() {
        return "BuyBook{}"+getClass().getName();
    }
}
