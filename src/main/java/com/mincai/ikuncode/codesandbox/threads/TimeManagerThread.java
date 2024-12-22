package com.mincai.ikuncode.codesandbox.threads;

import lombok.Getter;
import lombok.Setter;

/**
 * @author limincai
 */
@Getter
@Setter
public class TimeManagerThread extends Thread {

    private long time;

    private boolean isTimeout = false;

    private Process process;

    public TimeManagerThread(long time) {
        isTimeout = false;
        this.time = time;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (process.isAlive()) {
            process.destroy();
            isTimeout = true;
            this.stop();
        }
        super.run();
    }

}