package com.colorchains.colorchainsgl;

/**
 * Created by francisco.cnmarao on 19/04/2017.
 */

import java.util.Date;

/**
 * Created by francisco.cnmarao on 22/03/2017.
 */

public class Timer {
    public static boolean showFPS = true;
    public static long currentTime;
    public static long lastTime;
    public static long deltaTime;
    public static long totalTime = 0;
    public static long ticks = 0;
    public static int fps = 0;
    public static long dT;
    public static int timeInterval = 16;
    public static void update() {
        Date d = new Date();
        Timer.currentTime = d.getTime();
        if (Timer.lastTime == 0) Timer.lastTime = Timer.currentTime - 15;
        if (Timer.showFPS && (Timer.currentTime - Timer.lastTime) != 0) {
            Timer.totalTime += Math.round(1000 / ((Timer.currentTime - Timer.lastTime)));
            if (Timer.ticks % 30 == 0) {
                Timer.fps = (int) (Timer.totalTime / 30);
                Timer.totalTime = 0;
                Timer.dT = Timer.currentTime - Timer.lastTime;
            }
        }

        Timer.deltaTime = Math.min(Timer.currentTime - Timer.lastTime, 60);//Timer.timeInterval;//Math.floor((Math.max(this.currentTime - this.lastTime, 15) <= 30 ? this.currentTime - this.lastTime : 30) / 2) * 2;//16
        Timer.lastTime = Timer.currentTime;
        Timer.ticks++;
    }
}
