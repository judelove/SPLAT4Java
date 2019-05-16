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
public class Configuration {

    public int MAXPAGES = 16;
    public boolean HD_ON;
    public final double GAMMA = 2.5;
    public final int BZBUFFER = 65536;
    public final int ARRAYSIZE, IPPD;
    public final double PI, TWOPI, HALFPI, DEG2RAD, EARTHRADIUS, METERS_PER_MILE, METERS_PER_FOOT, KM_PER_MILE, FOUR_THIRDS;

    public Configuration() {
        if (!HD_ON) {
            switch (MAXPAGES) {
                case 4:
                    ARRAYSIZE = 4950;
                    break;
                case 9:
                    ARRAYSIZE = 10870;
                    break;
                case 16:
                    ARRAYSIZE = 19240;
                    break;
                case 25:
                    ARRAYSIZE = 30025;
                    break;
                case 36:
                    ARRAYSIZE = 43217;
                    break;
                case 49:
                    ARRAYSIZE = 58813;
                    break;
                case 64:
                    ARRAYSIZE = 76810;
                    break;
                default:
                    ARRAYSIZE = 0;
            }
            IPPD = 1200;
        } else {
            switch (MAXPAGES) {
                case 1:
                    ARRAYSIZE = 5092;
                    break;
                case 4:
                    ARRAYSIZE = 14844;
                    break;
                case 9:
                    ARRAYSIZE = 32600;
                    break;
                case 16:
                    ARRAYSIZE = 57713;
                    break;
                case 25:
                    ARRAYSIZE = 90072;
                    break;
                case 36:
                    ARRAYSIZE = 129650;
                    break;
                case 49:
                    ARRAYSIZE = 176437;
                    break;
                case 64:
                    ARRAYSIZE = 230430;
                    break;
                default:
                    ARRAYSIZE = 0;
            }
            IPPD = 3600;
        }

        //MAXPAGES = 64;
        PI = 3.141592653589793;
        TWOPI = 6.283185307179586;
        HALFPI = 1.570796326794896;
        DEG2RAD = 1.74532925199e-02;
        EARTHRADIUS = 20902230.97;
        METERS_PER_MILE = 1609.344;
        METERS_PER_FOOT = 0.3048;
        KM_PER_MILE = 1.609344;
        FOUR_THIRDS = 1.3333333333333;
    }

}
