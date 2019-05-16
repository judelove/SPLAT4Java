/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splat4j.input;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import splat4j.Site;

/**
 *
 * @author Jude Mukundane
 */
public class CitiesFile {

    private String filename;
    private ArrayList<Site> sites;

    public CitiesFile(String path) {
        this.filename = path;
    }

    public void loadCities() {
        File qthFile = new File(filename);
        if (qthFile.exists()) {
            try {

                Scanner rdr = new Scanner(qthFile);
                while (rdr.hasNextLine()) {
                    String name = null;
                    double lat = 91, lon = 361;
                    String line = rdr.nextLine();
                    String[] parts = line.split(",");
                    if (parts.length >= 1) {
                        name = parts[0];
                    }
                    if (parts.length >= 2) {
                        lat = readBearing(parts[1]);
                    }
                    if (parts.length >= 3) {
                        lat = readBearing(parts[2]);
                    }
                    getSites().add(new Site(name, filename, lat, lon, 0));
                }

            } catch (Exception ex) {
                System.err.print(ex.getStackTrace());
            }
        }
        else
        {
            System.err.printf("\n*** ERROR: \"%s\": not found!", filename);
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
            bearing = Math.abs(Double.parseDouble(input.trim()));
        }

        return bearing < -360 ? 0 : (bearing > 360) ? 0 : bearing;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @return the sites
     */
    public ArrayList<Site> getSites() {
        return sites;
    }
}
