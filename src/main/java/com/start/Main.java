package com.start;

public class Main {

    /**
     * @param args 创建上下文并启动
     */
    public static void main(String[] args) {
        Environment environment = new Environment("param.properties");
        Context context = new Context(environment, 1);
        context.start();


    }


}
