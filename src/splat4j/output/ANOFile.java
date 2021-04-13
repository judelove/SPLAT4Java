/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splat4j.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import splat4j.Configuration;
import splat4j.SplatEngine;
import splat4j.Utils;
import splat4j.input.SDFAgent;

/**
 *
 * @author Jude Mukundane
 */
public class ANOFile {

    SplatEngine splat;
    Configuration config;

    public ANOFile(Configuration config, SplatEngine splat) {
        this.config = config;
        this.splat = splat;
    }

    public int LoadANO(String filename) {
        /* This function reads a SPLAT! alphanumeric output 
	   file (-ani option) for analysis and/or map generation. */

        int error = 0, max_west, min_west, max_north, min_north;
        String string;
        double latitude = 0.0, longitude = 0.0, azimuth = 0.0, elevation = 0.0,
                ano = 0.0;
        File anoFile = new File(filename);

        if (anoFile.exists()) {
            try {
                Scanner scanner = new Scanner(anoFile);

                String line = scanner.nextLine().split(";")[0];
                max_west = Integer.parseInt(line.split(" ")[0]);
                min_west = Integer.parseInt(line.split(" ")[1]);

                line = scanner.nextLine().split(";")[0];
                max_north = Integer.parseInt(line.split(" ")[0]);
                min_north = Integer.parseInt(line.split(" ")[1]);

                SDFAgent sdfa = new SDFAgent(splat.getSdfPath());
                sdfa.loadTopoData(config, splat, max_west - 1, min_west, max_north - 1, min_north);

                System.out.printf("\nReading \"%s\"... ", filename);

                while (scanner.hasNextLine()) {
                    line = scanner.nextLine().split(";")[0];
                    latitude = Float.parseFloat(line.split(" ")[0]);
                    longitude = Float.parseFloat(line.split(" ")[1]);
                    azimuth = Float.parseFloat(line.split(" ")[2]);
                    elevation = Float.parseFloat(line.split(" ")[3]);
                    ano = Float.parseFloat(line.split(" ")[4]);

                    if (splat.getLr().getErp() == 0.0) {
                        /* Path loss */

                        if (splat.getContourThreshold() == 0 || (Math.abs(ano) <= (double) splat.getContourThreshold())) {
                            ano = Math.abs(ano);

                            if (ano > 255.0) {
                                ano = 255.0;
                            }

                            putSignal(latitude, longitude, (int)Math.round(ano));
                        }
                    }

                    if (splat.getLr().getErp() != 0.0 && splat.isDbm()) {
                        /* signal power level in dBm */

                        if (splat.getContourThreshold() == 0 || (ano >= (double) splat.getContourThreshold())) {
                            ano = 200.0 + Math.rint(ano);

                            if (ano < 0.0) {
                                ano = 0.0;
                            }

                            if (ano > 255.0) {
                                ano = 255.0;
                            }

                            putSignal(latitude, longitude,  (int)Math.round(ano));
                        }
                    }

                    if (splat.getLr().getErp() != 0.0 && !splat.isDbm()) {
                        /* field strength dBuV/m */

                        if (splat.getContourThreshold() == 0 || (ano >= (double) splat.getContourThreshold())) {
                            ano = 100.0 + Math.rint(ano);

                            if (ano < 0.0) {
                                ano = 0.0;
                            }

                            if (ano > 255.0) {
                                ano = 255.0;
                            }

                            putSignal(latitude, longitude, (int) Math.round(ano));
                        }
                    }

                }
                System.out.print(" Done!\n");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ANOFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            error = 1;
        }

        return error;
    }

    int putSignal(double lat, double lon, int signal) {
        /* This function writes a signal level (0-255)
	   at the specified location for later recall. */

        int x=0, y=0, indx;
        boolean found;

        for (indx = 0, found = false; indx < config.MAXPAGES && !found;) {
            x = (int) Math.rint(splat.getPpd() * (lat - splat.getDem()[indx].getMinNorth()));
            y = splat.getMpi() - (int) Math.rint(splat.getPpd() * (Utils.lonDiff(splat.getDem()[indx].getMaxWest(), lon)));

            if (x >= 0 && x <= splat.getMpi() && y >= 0 && y <= splat.getMpi()) {
                found = true;
            } else {
                indx++;
            }
        }

        if (found) {
           splat.getDem()[indx].getSignal()[x][y] = signal;
            return (splat.getDem()[indx].getSignal()[x][y]);
        } else {
            return 0;
        }
    }
}
