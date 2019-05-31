/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splat4j.input;

import java.io.File;
import java.util.Scanner;

/**
 *
 * @author Jude Mukundane
 */
public class QTHFile {
    
    private double lat = 91, lon = 361;
    private float alt = 0;
    private String name, filename;
    
    public QTHFile(String path) {
        this.loadQTH(path);
    }
    
    private void loadQTH(String filename) {
        File qthFile = new File(filename);
        if (qthFile.exists()) {
            this.filename = filename;
            try {
                Scanner rdr = new Scanner(qthFile);
                name = rdr.nextLine();
                lat = readBearing(rdr.nextLine());
                lon = readBearing(rdr.nextLine());
                if (getLon() < 0) {
                    lon += 360;
                }
                alt = getAlt(rdr.nextLine());
                
            } catch (Exception ex) {
                System.err.print(ex.getStackTrace());
            }
        }
        
    }
    
    private double readBearing(String input) {
        /* This function takes numeric input in the form of a character
	   string, and returns an equivalent bearing in degrees as a
	   decimal number (double).  The input may either be expressed
	   in decimal format (40.139722) or degree, minute, second
	   format (40 08 23).  This function also safely handles
	   extra spaces found either leading, trailing, or
	   embedded within the numbers expressed in the
	   input string.  Decimal seconds are permitted. */
        
        double bearing = 0.0;
        
        if (input.trim().contains(" ")) { //Degree, Minute, Second Format (40 08 23.xx)
            String[] parts = input.trim().split(" ");
            double deg = 0, min = 0, sec = 0;
            if (parts.length >= 1) {
                deg = Math.abs(Double.parseDouble(parts[0]));
                bearing = deg;
            }
            if (parts.length >= 2) {
                min = Math.abs(Double.parseDouble(parts[1]));
                bearing += min / 60;
            }
            if (parts.length >= 3) {
                sec = Math.abs(Double.parseDouble(parts[2]));
                bearing += sec / 3600;
            }
            bearing = (deg < 0 || min < 0 || sec < 0) ? -bearing : bearing;
        } else {
            bearing = Double.parseDouble(input.trim());
        }
        
        return bearing < -360 ? 0 : (bearing > 360) ? 0 : bearing;
    }
    
    private float getAlt(String nextLine) {
        if (nextLine.contains("m") || nextLine.contains("M")) {
            return (float)(Float.parseFloat(nextLine.replace("m", "").replace("M", "").trim()) * 3.28084);
        } else {
            return Float.parseFloat(nextLine.trim());
        }
    }

    /**
     * @return the lat
     */
    public double getLat() {
        return lat;
    }

    /**
     * @return the lon
     */
    public double getLon() {
        return lon;
    }

    /**
     * @return the alt
     */
    public float getAlt() {
        return alt;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }
    
}
