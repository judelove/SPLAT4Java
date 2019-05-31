/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splat4j;

/**
 *
 * @author Jude Mukundane
 */
public class Dem {

    private int minNorth;
    private int maxNorth;
    private int minWest;
    private int maxWest;
    private int maxEl = -32768;
    private int minEl = 32768;
    private int[][] data;
    private int[][] mask;
    private int[][] signal;

    public Dem(int maxWest,int minNorth, int minWest, int maxNorth, int IPPD) {
        this.maxNorth = maxNorth;
        this.minWest = minWest;
        this.maxWest = maxWest;
        this.minNorth = minNorth;
        this.data = new int[IPPD][IPPD];
        this.mask = new int[IPPD][IPPD];
        this.signal = new int[IPPD][IPPD];
    }
    
    public Dem(int maxWest,int minNorth, int minWest, int maxNorth, int minEl, int maxEl, int IPPD) {
        this.maxNorth = maxNorth;
        this.minWest = minWest;
        this.maxWest = maxWest;
        this.minEl = minEl;
        this.maxEl = maxEl;
        this.data = new int[IPPD][IPPD];
        this.mask = new int[IPPD][IPPD];
        this.signal = new int[IPPD][IPPD];
    }

    /**
     * @return the minNorth
     */
    public int getMinNorth() {
        return minNorth;
    }

    /**
     * @return the maxNorth
     */
    public int getMaxNorth() {
        return maxNorth;
    }

    /**
     * @return the minWest
     */
    public int getMinWest() {
        return minWest;
    }

    /**
     * @return the maxWest
     */
    public int getMaxWest() {
        return maxWest;
    }

    /**
     * @return the maxEl
     */
    public int getMaxEl() {
        return maxEl;
    }

    /**
     * @return the minEl
     */
    public int getMinEl() {
        return minEl;
    }

    /**
     * @return the data
     */
    public int[][] getData() {
        return data;
    }
    
    public int getData(int x, int y)
    {
        return data[x][y];
    }

    /**
     * @return the mask
     */
    public int[][] getMask() {
        return mask;
    }

    /**
     * @return the signal
     */
    public int[][] getSignal() {
        return signal;
    }

    /**
     * @param data the data to set
     */
    public void setData(int x, int y, int data) {
        this.getData()[x][y] = data;
    }

    /**
     * @param mask the mask to set
     */
    public void setMask(int x, int y, int mask) {
        this.mask[x][y] = mask;
    }

    /**
     * @param signal the signal to set
     */
    public void setSignal(int x, int y, int signal) {
        this.signal[x][y] = signal;
    }

    /**
     * @param maxEl the maxEl to set
     */
    public void setMaxEl(int maxEl) {
        this.maxEl = maxEl;
    }

    /**
     * @param minEl the minEl to set
     */
    public void setMinEl(int minEl) {
        this.minEl = minEl;
    }

    /**
     * @param data the data to set
     */
    public void setData(int[][] data) {
        this.data = data;
    }

    /**
     * @param minNorth the minNorth to set
     */
    public void setMinNorth(int minNorth) {
        this.minNorth = minNorth;
    }

    /**
     * @param maxNorth the maxNorth to set
     */
    public void setMaxNorth(int maxNorth) {
        this.maxNorth = maxNorth;
    }

    /**
     * @param minWest the minWest to set
     */
    public void setMinWest(int minWest) {
        this.minWest = minWest;
    }

    /**
     * @param maxWest the maxWest to set
     */
    public void setMaxWest(int maxWest) {
        this.maxWest = maxWest;
    }
}
