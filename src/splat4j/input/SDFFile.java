/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splat4j.input;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import splat4j.Dem;
import splat4j.Configuration;
import splat4j.SplatEngine;

/**
 *
 * @author Jude Mukundane
 */
public class SDFFile {

    private final String filename, sdf_dir;
    private int minlat, minlon, maxlat, maxlon;
    Configuration config;

    SDFFile(String filename, String sdf_dir, SplatEngine splat, Configuration config) {
        this.filename = filename;
        this.sdf_dir = sdf_dir;
        this.config = config;
        this.loadSDF(this.filename, this.sdf_dir, splat);
    }

    private int loadSDF_SDF(String filename, String sdf_dir, SplatEngine splat) {
        /* This function reads uncompressed SPLAT Data Files (.sdf)
	   containing digital elevation model data into memory.
	   Elevation data, maximum and minimum elevations, and
	   quadrangle limits are stored in the first available
	   dem[] structure. */

        int x, y, data, indx;
        boolean found = false, free_page = false;

       /* Parse filename for minimum latitude and longitude values */
        String[] fileCoordinates = filename.split("[.]")[0].split("_");
        minlat = Integer.parseInt(fileCoordinates[0]);
        maxlat = Integer.parseInt(fileCoordinates[1]);
        minlon = Integer.parseInt(fileCoordinates[2]);
        maxlon = Integer.parseInt(fileCoordinates[3]);

        /* Is it already in memory? */
        for (indx = 0, found = false; indx < splat.getDem().length && !found; indx++) {
            if (splat.getDem()[indx] == null) {
                continue;
            }
            if (minlat == splat.getDem()[indx].getMinNorth() && minlon == splat.getDem()[indx].getMinWest() && maxlat == splat.getDem()[indx].getMaxNorth() && maxlon == splat.getDem()[indx].getMaxWest()) {
                found = true;
            }
        }

        /* Is room available to load it? */
        if (!found) {
            for (indx = 0, free_page = false; indx < splat.getDem().length && !free_page; indx++) {
                if (splat.getDem()[indx].getMaxNorth() == -90) {
                    free_page = true;
                }
            }
        }

        indx--;

        if (free_page && !found && indx >= 0 && indx < splat.getDem().length) {
            /* Search for SDF file in current working directory first */

//            strncpy(path_plus_name, sdf_file, 255);
//
//            fd = fopen(path_plus_name, "rb");
            File sdfFile = new File(filename);

            if (!sdfFile.exists()) {
                /* Next, try loading SDF file from path specified
			   in $HOME/.splat_path file or by -d argument */

//                strncpy(path_plus_name, sdf_path, 255);
//                strncat(path_plus_name, sdf_file, 254);
//
//                fd = fopen(path_plus_name, "rb");
                sdfFile = new File(sdf_dir + "/" + filename);
            }

            if (sdfFile.exists()) {
                try {
                    System.out.printf("Loading \"%s\" into page %d...", sdfFile.getAbsolutePath(), indx + 1);
                    //fflush(stdout);
                    Scanner scanner = new Scanner(sdfFile);
                    splat.setDem(indx, new Dem(Integer.parseInt(scanner.nextLine()), Integer.parseInt(scanner.nextLine()), Integer.parseInt(scanner.nextLine()), Integer.parseInt(scanner.nextLine()), splat.getIppd()));

//                fgets(line, 19, fd);
//                sscanf(line, "%d",  & dem[indx].max_west);
//
//                fgets(line, 19, fd);
//                sscanf(line, "%d",  & dem[indx].min_north);
//
//                fgets(line, 19, fd);
//                sscanf(line, "%d",  & dem[indx].min_west);
//
//                fgets(line, 19, fd);
//                sscanf(line, "%d",  & dem[indx].max_north);
                    for (x = 0; x < splat.getIppd(); x++) {
                        for (y = 0; y < splat.getIppd(); y++) {
                            //fgets(line, 19, fd);
                            data = Integer.parseInt(scanner.nextLine());

                            splat.getDem()[indx].setData(x, y, data);
                            splat.getDem()[indx].setSignal(x, y, 0);
                            splat.getDem()[indx].setMask(x, y, 0);
                            if (data > splat.getDem()[indx].getMaxEl()) {
                                splat.getDem()[indx].setMaxEl(data);
                            }

                            if (data < splat.getDem()[indx].getMinEl()) {
                                splat.getDem()[indx].setMinEl(data);
                            }
                        }
                    }

                    if (splat.getDem()[indx].getMinEl() < splat.getMinElevation()) {
                        splat.setMinElevation(splat.getDem()[indx].getMinEl());
                    }

                    if (splat.getDem()[indx].getMaxEl() > splat.getMaxElevation()) {
                        splat.setMaxElevation(splat.getDem()[indx].getMaxEl());
                    }

                    if (splat.getMaxNorth() == -90) {
                        splat.setMaxNorth(splat.getDem()[indx].getMaxNorth());
                    } else if (splat.getDem()[indx].getMaxNorth() > splat.getMaxNorth()) {
                        splat.setMaxNorth(splat.getDem()[indx].getMaxNorth());
                    }

                    if (splat.getMinNorth() == 90) {
                        splat.setMinNorth(splat.getDem()[indx].getMinNorth());
                    } else if (splat.getDem()[indx].getMinNorth() < splat.getMinNorth()) {
                        splat.setMinNorth(splat.getDem()[indx].getMinNorth());
                    }

                    if (splat.getMaxWest() == -1) {
                        splat.setMaxWest(splat.getDem()[indx].getMaxWest());
                    } else {
                        if (Math.abs(splat.getDem()[indx].getMaxWest() - splat.getMaxWest()) < 180) {
                            if (splat.getDem()[indx].getMaxWest() > splat.getMaxWest()) {
                                splat.setMaxWest(splat.getDem()[indx].getMaxWest());
                            }
                        } else {
                            if (splat.getDem()[indx].getMaxWest() < splat.getMaxWest()) {
                                splat.setMaxWest(splat.getDem()[indx].getMaxWest());
                            }
                        }
                    }

                    if (splat.getMinWest() == 360) {
                        splat.setMinWest(splat.getDem()[indx].getMinWest());
                    } else {
                        if (Math.abs(splat.getDem()[indx].getMinWest() - splat.getMinWest()) < 180.0) {
                            if (splat.getDem()[indx].getMinWest() < splat.getMinWest()) {
                                splat.setMinWest(splat.getDem()[indx].getMinWest());
                            }
                        } else {
                            if (splat.getDem()[indx].getMinWest() > splat.getMinWest()) {
                                splat.setMinWest(splat.getDem()[indx].getMinWest());
                            }
                        }
                    }

                    System.out.println(" Done!");
//fflush(stdout);

                    return 1;
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(SDFFile.class.getName()).log(Level.SEVERE, null, ex);
                    return 0;

                }
            } else {
                return -1;
            }
        } else {
            return 0;
        }
    }

    private int loadSDF(String filename, String sdf_dir, SplatEngine splat) {
        /* This function loads the requested SDF file from the filesystem.
	   It first tries to invoke the LoadSDF_SDF() function to load an
	   uncompressed SDF file (since uncompressed files load slightly
	   faster).  If that attempt fails, then it tries to load a
	   compressed SDF file by invoking the LoadSDF_BZ() function.
	   If that fails, then we can assume that no elevation data
	   exists for the region requested, and that the region
	   requested must be entirely over water. */

        int x, y, indx; // minlat, minlon, maxlat, maxlon;
        boolean found, free_page = false;
        int return_value = -1;

        /* Try to load an uncompressed SDF first. */
        return_value = loadSDF_SDF(filename, sdf_dir, splat);

//        /* If that fails, try loading a compressed SDF. */
//        if (return_value == 0 || return_value == -1) {
//            return_value = LoadSDF_BZ(name);
//        }

        /* If neither format can be found, then assume the area is water. */
        if (return_value == 0 || return_value == -1) {
            /* Parse SDF name for minimum latitude and longitude values */

            //sscanf(name, "%d:%d:%d:%d",  & minlat,  & maxlat,  & minlon,  & maxlon);
            String[] fileCoordinates = filename.split("[.]")[0].split("_");
            minlat = Integer.parseInt(fileCoordinates[0]);
            maxlat = Integer.parseInt(fileCoordinates[1]);
            minlon = Integer.parseInt(fileCoordinates[2]);
            maxlon = Integer.parseInt(fileCoordinates[3]);

            /* Is it already in memory? */
            for (indx = 0, found = false; indx < splat.getDem().length
                    && !found; indx++) {
                if (splat.getDem()[indx] == null) {
                    continue;
                }
                if (getMinlat() == splat.getDem()[indx].getMinNorth() && getMinlon() == splat.getDem()[indx].getMinWest() && getMaxlat() == splat.getDem()[indx].getMaxNorth() && getMaxlon() == splat.getDem()[indx].getMaxWest()) {
                    found = true;
                }
            }

            /* Is room available to load it? */
            if (!found) {
                for (indx = 0, free_page = false; indx < splat.getDem().length && !free_page; indx++) {
                    if (splat.getDem()[indx] == null) { //.getMaxNorth() == -90) {
                        free_page = true;
                    }
                }
            }

            indx--;

            if (free_page && !found && indx >= 0 && indx < splat.getDem().length) {
                System.out.printf("Region  \"%s\" assumed as sea-level into page %d...", filename, indx + 1);
                //fflush(stdout);
                splat.setDem(indx, new Dem(maxlon, minlat, minlon, maxlat, config.IPPD));
//                splat.getDem()[indx].setMaxWest(getMaxlon());
//                splat.getDem()[indx].setMinNorth(getMinlat());
//                splat.getDem()[indx].setMinWest(getMinlon());
//                splat.getDem()[indx].setMaxNorth(getMaxlat());

                /* Fill DEM with sea-level topography */
                for (x = 0; x < splat.getIppd(); x++) {
                    for (y = 0; y < splat.getIppd(); y++) {
                        splat.getDem()[indx].setData(x, y, 0);
                        splat.getDem()[indx].setSignal(x, y, 0);
                        splat.getDem()[indx].setMask(x, y, 0);

                        if (splat.getDem()[indx].getMinEl() > 0) {
                            splat.getDem()[indx].setMinEl(0);
                        }
                    }
                }

                if (splat.getDem()[indx].getMinEl() < splat.getMinElevation()) {
                    splat.setMinElevation(splat.getDem()[indx].getMinEl());
                }

                if (splat.getDem()[indx].getMaxEl() > splat.getMaxElevation()) {
                    splat.setMaxElevation(splat.getDem()[indx].getMaxEl());
                }

                if (splat.getMaxNorth() == -90) {
                    splat.setMaxNorth(splat.getDem()[indx].getMaxNorth());
                } else if (splat.getDem()[indx].getMaxNorth() > splat.getMaxNorth()) {
                    splat.setMaxNorth(splat.getDem()[indx].getMaxNorth());
                }

                if (splat.getMinNorth() == 90) {
                    splat.setMinNorth(splat.getDem()[indx].getMinNorth());
                } else if (splat.getDem()[indx].getMinNorth() < splat.getMinNorth()) {
                    splat.setMinNorth(splat.getDem()[indx].getMinNorth());
                }

                if (splat.getMaxWest() == -1) {
                    splat.setMaxWest(splat.getDem()[indx].getMaxWest());
                } else {
                    if (Math.abs(splat.getDem()[indx].getMaxWest() - splat.getMaxWest()) < 180) {
                        if (splat.getDem()[indx].getMaxWest() > splat.getMaxWest()) {
                            splat.setMaxWest(splat.getDem()[indx].getMaxWest());
                        }
                    } else {
                        if (splat.getDem()[indx].getMaxWest() < splat.getMaxWest()) {
                            splat.setMaxWest(splat.getDem()[indx].getMaxWest());
                        }
                    }
                }

                if (splat.getMinWest() == 360) {
                    splat.setMinWest(splat.getDem()[indx].getMinWest());
                } else {
                    if (Math.abs(splat.getDem()[indx].getMinWest() - splat.getMinWest()) < 180) {
                        if (splat.getDem()[indx].getMinWest() < splat.getMinWest()) {
                            splat.setMinWest(splat.getDem()[indx].getMinWest());
                        }
                    } else {
                        if (splat.getDem()[indx].getMinWest() > splat.getMinWest()) {
                            splat.setMinWest(splat.getDem()[indx].getMinWest());
                        }
                    }
                }

                System.out.println(" Done!");
                //fflush(stdout);

                return_value = 1;
            }
        }

        return return_value;
    }

    /**
     * @return the minlat
     */
    public int getMinlat() {
        return minlat;
    }

    /**
     * @return the minlon
     */
    public int getMinlon() {
        return minlon;
    }

    /**
     * @return the maxlat
     */
    public int getMaxlat() {
        return maxlat;
    }

    /**
     * @return the maxlon
     */
    public int getMaxlon() {
        return maxlon;
    }
}
