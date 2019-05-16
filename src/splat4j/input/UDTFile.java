/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splat4j.input;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import splat4j.Configuration;
import splat4j.SplatEngine;
import splat4j.Dem;
import splat4j.Utils;

/**
 *
 * @author Jude Mukundane
 */
public class UDTFile {

    private final String filename;

    public UDTFile(String filename) {
        this.filename = filename;
    }

    public void load(Configuration config, SplatEngine splat) {
        /* This function reads a file containing User-Defined Terrain
	   features for their addition to the digital elevation model
	   data used by SPLAT!.  Elevations in the UDT file are evaluated
	   and then copied into a temporary file under /tmp.  Then the
	   contents of the temp file are scanned, and if found to be unique,
	   are added to the ground elevations described by the digital
	   elevation data already loaded into memory. */

        HashSet<String> lines = new HashSet(); //to eliminate duplicates ;-)
        String[] str;
        double latitude, longitude, height;
        //File fd1 = null, fd2 = null;

        //String tempname = "XXXXXX";
        File udtFile = new File(filename);

        if (udtFile.exists()) {
            try {
                //  File tempFile = File.createTempFile("temp", tempname);
                //fd2 = fopen(tempname, "w");

//		fgets(input,78,fd1);
//
//		pointer=strchr(input,';');
//
//		if (pointer!=null)
//			*pointer=0;
                System.out.printf("\nReading \"%s\"... ", filename);
                Scanner scanner = new Scanner(udtFile);
                while (scanner.hasNextLine()) {
                    /* Parse line for latitude, longitude, height */
                    String line = scanner.nextLine();
                    if (line.startsWith(";")) {
                        continue;
                    }
                    line = line.split(";")[0];
                    lines.add(line);
                }

//                for (x = 0, y = 0, z = 0; x < 78 && input[x] != 0 && z < 3; x++) {
//                    if (input[x] != ',' && y < 78) {
//                        str[z][y] = input[x];
//                        y++;
//                    } else {
//                        str[z][y] = 0;
//                        z++;
//                        y = 0;
//                    }
//                }
                for (String entry : lines) {
                    str = entry.split(",");
                    latitude = Utils.readBearing(str[0]);
                    longitude = Utils.readBearing(str[1]);

                    if (longitude < 0.0) {
                        longitude += 360.0;
                    }

                    /* Remove <CR> and/or <LF> from antenna height string */
//                for (i = 0; str[2][i] != 13 && str[2][i] != 10 && str[2][i] != 0; i++);
//
//                str[2][i] = 0;

                    /* The terrain feature may be expressed in either
feet or meters.  If the letter 'M' or 'm' is
discovered in the string, then this is an
indication that the value given is expressed
in meters.  Otherwise the height is interpreted
as being expressed in feet.  */
                    height = str[2].toLowerCase().contains("m") ? (Double.parseDouble(str[2].toLowerCase().replace("m", "")) * config.METERS_PER_FOOT) : Double.parseDouble(str[2].toLowerCase().replace("m", ""));
                    AddElevation(latitude, longitude, height, splat);
                }

//			if (height > 0.0) {
//                    fprintf(fd2, "%d, %d, %f\n", (int) rint(latitude / dpp), (int) rint(longitude / dpp), height);
//                }
//
//                fgets(input, 78, fd1);
//
//                pointer = strchr(input, ';');
//
//                if (pointer != null) {
//                     * pointer = 0;
//                }
//            }
//
//            fclose(fd1);
//            fclose(fd2);
//            close(fd);
//
//            System.out.printf(stdout, "Done!");
//
//            fd1 = fopen(tempname, "r");
//            fd2 = fopen(tempname, "r");
//
//            y = 0;
//
//            fscanf(fd1, "%d, %d, %lf",  & xpix,  & ypix,  & height);
//
//            do {
//                x = 0;
//                z = 0;
//
//                fscanf(fd2, "%d, %d, %lf",  & tempxpix,  & tempypix,  & tempheight);
//
//                do {
//                    if (x > y && xpix == tempxpix && ypix == tempypix) {
//                        z = 1;
//                        /* Dupe! */
//
//                        if (tempheight > height) {
//                            height = tempheight;
//                        }
//                    } else {
//                        fscanf(fd2, "%d, %d, %lf",  & tempxpix,  & tempypix,  & tempheight);
//                        x++;
//                    }
//
//                } while (feof(fd2) == 0 && z == 0);
//
//                if (z == 0) /* No duplicate found */ {
//                    AddElevation(xpix * dpp, ypix * dpp, height);
//                }
//
//                fscanf(fd1, "%d, %d, %lf",  & xpix,  & ypix,  & height);
//                y++;
//
//                rewind(fd2);
//
//            } while (feof(fd1) == 0);
//
//            fclose(fd1);
//            fclose(fd2);
//            unlink(tempname);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(UDTFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.printf("\n*** ERROR: \"%s\": not found!", filename);
        }

        System.out.println();
    }

    boolean AddElevation(double lat, double lon, double height, SplatEngine splat) {
        /* This function adds a user-defined terrain feature
	   (in meters AGL) to the digital elevation model data
	   in memory.  Does nothing and returns 0 for locations
	   not found in memory. */

        boolean found;
        int x = 0, y = 0, indx;

        for (indx = 0, found = false; indx < splat.getDem().length && !found;) {
            x = (int) Math.rint(splat.getPpd() * (lat - splat.getDem()[indx].getMinNorth()));
            y = splat.getMpi() - (int) Math.rint(splat.getPpd() * (Utils.lonDiff(splat.getDem()[indx].getMaxWest(), lon)));

            if (x >= 0 && x <= splat.getMpi() && y >= 0 && y <= splat.getMpi()) {
                found = true;
            } else {
                indx++;
            }
        }

        if (found) {
            splat.getDem()[indx].setData(x, y, splat.getDem()[indx].getData(x, y) + (short) Math.rint(height));
        }

        return found;
    }

}
