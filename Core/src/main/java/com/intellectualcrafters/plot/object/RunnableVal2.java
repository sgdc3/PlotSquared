package com.intellectualcrafters.plot.object;

public abstract class RunnableVal2<T, U> implements Runnable {
    public T value1;
    public U value2;
    
    public RunnableVal2() {}
    
    public RunnableVal2(T value1, U value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public void run() {
        run(value1, value2);
    }
    
    public abstract void run(T value1, U value2);
}
