package com.start;

public class Main {

    /**
     * @param args 创建上下文并启动
     */
    public static void main(String[] args) {

        Context context = new Context("param.properties");
        context.start();

    }


}
