package com.trongthang.welcometomyworld;

public class RunAfter {
    public Runnable functionToRun;
    public int runAfterInTick;

    public RunAfter(Runnable functionToRun, int runAfterTick){
        this.functionToRun = functionToRun;
        this.runAfterInTick = runAfterTick;
    }
}