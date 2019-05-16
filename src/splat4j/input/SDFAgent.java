/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splat4j.input;

import java.util.ArrayList;
import java.util.HashSet;
import splat4j.Configuration;
import splat4j.SplatEngine;
import splat4j.Utils;

/**
 *
 * @author Jude Mukundane
 */
public class SDFAgent {

   private String sdfDir;
    private HashSet<SDFFile> sdfFiles;
    
    public SDFAgent(String sdfDir)
    {
        this.sdfDir = sdfDir;
        this.sdfFiles = new HashSet();
    }
    
 public void loadTopoData(Configuration config, SplatEngine  splat, int max_lon, int min_lon, int max_lat, int min_lat) {
        /* This function loads the SDF files required
	   to cover the limits of the region specified. */

        int x, y, width, ymin, ymax;
        String string;

        width = Utils.reduceAngle(max_lon - min_lon, config);

        if ((max_lon - min_lon) <= 180.0) {
            for (y = 0; y <= width; y++) {
                for (x = min_lat; x <= max_lat; x++) {
                    ymin = (int) (min_lon + (double) y);

                    while (ymin < 0) {
                        ymin += 360;
                    }

                    while (ymin >= 360) {
                        ymin -= 360;
                    }

                    ymax = ymin + 1;

                    while (ymax < 0) {
                        ymax += 360;
                    } 

                    while (ymax >= 360) {
                        ymax -= 360;
                    }

                    if (splat.getIppd() == 3600) {
                        string = String.format("%d_%d_%d_%d-hd.sdf", x, x + 1, ymin, ymax);
                    } else {
                        string = String.format("%d_%d_%d_%d.sdf", x, x + 1, ymin, ymax);
                    }
                    SDFFile sdfFile = new SDFFile(string, this.sdfDir, splat, config);
                    this.sdfFiles.add(sdfFile);
                 }
            }
        } else {
            for (y = 0; y <= width; y++) {
                for (x = min_lat; x <= max_lat; x++) {
                    ymin = max_lon + y;

                    while (ymin < 0) {
                        ymin += 360;
                    }

                    while (ymin >= 360) {
                        ymin -= 360;
                    }

                    ymax = ymin + 1;

                    while (ymax < 0) {
                        ymax += 360;
                    }

                    while (ymax >= 360) {
                        ymax -= 360;
                    }

                    if (splat.getIppd() == 3600) {
                        string = String.format("%d_%d_%d_%d-hd", x, x + 1, ymin, ymax);
                    } else {
                        string = String.format("%d_%d_%d_%d", x, x + 1, ymin, ymax);
                    }
                    SDFFile sdfFile = new SDFFile(string, this.sdfDir, splat, config);
                    this.sdfFiles.add(sdfFile);
                }
            }
        }
    }
}
