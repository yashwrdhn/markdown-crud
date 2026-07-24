package com.mvp;

public class EchoTool extends Tool {

    public EchoTool() {
        super("echo", "Echoes the input");
    }

    @Override
    public Object execute(Object input) {
        return input;
    }



}