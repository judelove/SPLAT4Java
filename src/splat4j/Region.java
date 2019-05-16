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
public class Region {

    

    private int[][] color = new int[32][3];
    private int[] level = new int[32];
    private int levels;
    
    public Region(){
        
    }

    /**
     * @return the color
     */
    public int[][] getColor() {
        return color;
    }

    /**
     * @return the level
     */
    public int[] getLevel() {
        return level;
    }

    /**
     * @return the levels
     */
    public int getLevels() {
        return levels;
    }

    /**
     * @param color the color to set
     */
    public void setColor(int x, int y, int color) {
        this.color[x] [y]= color;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(int x, int  level) {
        this.level[x] = level;
    }

    /**
     * @param levels the levels to set
     */
    public void setLevels(int levels) {
        this.levels = levels;
    }
}
