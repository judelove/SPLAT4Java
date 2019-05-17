/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splat4j.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import splat4j.Site;
import splat4j.Configuration;
import splat4j.SplatEngine;
import splat4j.Utils;
import splat4j.Dem;
import splat4j.FontData;
import splat4j.Region;
import java.util.Scanner;

/**
 *
 * @author Jude Mukundane
 */
public class PPMGenerator {

    Configuration config;
    SplatEngine splat;
    double conversion, one_over_gamma, lat, lon,
            north, south, east, west, max_elevation, min_elevation;
    boolean found, kml, geo, ngs, cityorcounty;
    double width, height, terrain;
    int loss, signal, match, mask, indx, x, y, z, x0 = 0, y0 = 0, red = 0, green = 0, blue = 0, hundreds, tens, units;

    public PPMGenerator(Configuration config, SplatEngine splat) {
        this.config = config;
        this.splat = splat;
    }

    public void WritePPM(String baseFilename, boolean geo, boolean kml, boolean ngs, Site[] xmtr) {

        configureParameters();
        setGeoBounds();
        if (!kml && geo) {
            writeGeoFile(baseFilename, west, north, width, height, east, south);
        }
        if (kml && !geo) {
            writeKMLFile(baseFilename, xmtr, north, south, east, west, x);
        }

        FileOutputStream wri = null;
        try {
            File mapFile = new File(baseFilename + ".ppm");
            wri = new FileOutputStream(mapFile);
            wri.write(String.format("P6\n%s %s\n255\n", (int) width, (int) height).getBytes());
            System.out.printf("\nWriting \"%s\" (%sx%s pixmap image)... ", baseFilename + ".ppm", width, height);
            //fflush(stdout);
            for (y = 0, lat = north; y < (int) height; y++, lat = north - (splat.getDpp() * (double) y)) {
                for (x = 0, lon = splat.getMaxWest(); x < (int) width; x++, lon = (double) splat.getMaxWest() - (splat.getDpp() * (double) x)) {
                    if (lon < 0.0) {
                        lon += 360.0;
                    }

                    for (indx = 0, found = false; indx < config.MAXPAGES && !found;) {
                        x0 = (int) Math.rint(splat.getPpd() * (lat - (double) splat.getDem()[indx].getMinNorth()));
                        y0 = splat.getMpi() - (int) Math.rint(splat.getPpd() * (Utils.lonDiff((double) splat.getDem()[indx].getMaxWest(), lon)));

                        if (x0 >= 0 && x0 <= splat.getMpi() && y0 >= 0 && y0 <= splat.getMpi()) {
                            found = true;
                        } else {
                            indx++;
                        }
                    }

                    if (found) {
                        try {
                            mask = splat.getDem()[indx].getMask()[x0][y0];
                            if (mask > 0) {
                                int k = 1;
                            }

                            if ((mask & 2) > 0) /* Text Labels: Red */ {
                                //wri.write(String.format("%d%d%d", 255, 0, 0));
                            } else if ((mask & 4) > 0) /* County Boundaries: Light Cyan */ {
                                wri.write(new byte[]{(byte) 128, (byte) 128, (byte) 255});
                            } else {
                                int k = mask & 57;
                                switch (mask & 57) {
                                    case 1:
                                        /* TX1: Green */
                                        wri.write(new byte[]{(byte) 0, (byte) 255, (byte) 0});
                                        break;

                                    case 8:
                                        /* TX2: Cyan */
                                        wri.write(new byte[]{(byte) 0, (byte) 255, (byte) 255});
                                        break;

                                    case 9:
                                        /* TX1 + TX2: Yellow */
                                        wri.write(new byte[]{(byte) 255, (byte) 255, (byte) 0});
                                        break;

                                    case 16:
                                        /* TX3: Medium Violet */
                                        wri.write(new byte[]{(byte) 147, (byte) 112, (byte) 219});
                                        break;

                                    case 17:
                                        /* TX1 + TX3: Pink */
                                        wri.write(new byte[]{(byte) 255, (byte) 192, (byte) 203});
                                        break;

                                    case 24:
                                        /* TX2 + TX3: Orange */
                                        wri.write(new byte[]{(byte) 255, (byte) 165, (byte) 0});
                                        break;

                                    case 25:
                                        /* TX1 + TX2 + TX3: Dark Green */
                                        wri.write(new byte[]{(byte) 0, (byte) 100, (byte) 0});
                                        break;

                                    case 32:
                                        /* TX4: Sienna 1 */
                                        wri.write(new byte[]{(byte) 255, (byte) 130, (byte) 71});
                                        break;

                                    case 33:
                                        /* TX1 + TX4: Green Yellow */
                                        wri.write(new byte[]{(byte) 173, (byte) 255, (byte) 47});
                                        break;

                                    case 40:
                                        /* TX2 + TX4: Dark Sea Green 1 */
                                        wri.write(new byte[]{(byte) 193, (byte) 255, (byte) 193});
                                        break;

                                    case 41:
                                        /* TX1 + TX2 + TX4: Blanched Almond */
                                        wri.write(new byte[]{(byte) 255, (byte) 235, (byte) 205});
                                        break;

                                    case 48:
                                        /* TX3 + TX4: Dark Turquoise */
                                        wri.write(new byte[]{(byte) 0, (byte) 206, (byte) 209});
                                        break;

                                    case 49:
                                        /* TX1 + TX3 + TX4: Medium Spring Green */
                                        wri.write(new byte[]{(byte) 0, (byte) 250, (byte) 154});
                                        break;

                                    case 56:
                                        /* TX2 + TX3 + TX4: Tan */
                                        wri.write(new byte[]{(byte) 210, (byte) 180, (byte) 140});
                                        break;

                                    case 57:
                                        /* TX1 + TX2 + TX3 + TX4: Gold2 */
                                        wri.write(new byte[]{(byte) 238, (byte) 201, (byte) 0});
                                        break;

                                    default:
                                        if (ngs) /* No terrain */ {
                                            wri.write(new byte[]{(byte) 255, (byte) 255, (byte) 255});
                                        } else {
                                            /* Sea-level: Medium Blue */
                                            if (splat.getDem()[indx].getData(x0, y0) == 0) {
                                                wri.write(new byte[]{(byte) 0, (byte) 0, (byte) 170});
                                            } else {
                                                /* Elevation: Greyscale */
                                                terrain = (0.5 + Math.pow((double) (splat.getDem()[indx].getData(x0, y0) - splat.getMinElevation()), one_over_gamma) * conversion);
                                                wri.write(new byte[]{(byte) terrain, (byte) terrain, (byte) terrain});
                                            }
                                        }
                                }
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        /* We should never get here, but if */
 /* we do, display the region as black */

                        //wri.write(String.format("%d%d%d", 0, 0, 0));
                    }
                }
            }
            System.out.print("Done!\n");
        } catch (IOException ex) {
            Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                wri.close();
            } catch (IOException ex) {
                Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void configureParameters() {
        //File *fd;
        one_over_gamma = 1.0 / config.GAMMA;
        conversion = 255.0 / Math.pow((double) (splat.getMaxElevation() - splat.getMinElevation()), one_over_gamma);
        width = (splat.getIppd() * Utils.reduceAngle(splat.getMaxWest() - splat.getMinWest(), config));
        height = (splat.getIppd() * Utils.reduceAngle(splat.getMaxNorth() - splat.getMinNorth(), config));
    }

    private void setGeoBounds() {

        splat.setMinWest((int) (splat.getMinWest() + splat.getDpp()));
        if (splat.getMinWest() > 360.0) {
            splat.setMinWest((int) (splat.getMinWest() - 360.0));
        }
        north = (double) splat.getMaxNorth() - splat.getDpp();
        south = (double) splat.getMinNorth();
        east = (splat.getMinWest() < 180.0 ? -splat.getMinWest() : 360.0 - splat.getMinWest());
        west = (double) (splat.getMaxWest() < 180 ? -splat.getMaxWest() : 360 - splat.getMaxWest());
    }

    public void WritePPMLR(String baseFilename, boolean geo, boolean kml, boolean ngs, Site[] xmtr, Dem[] dem) {

        Region region = new Region();
        configureParameters();
        setGeoBounds();
        if (!kml && geo) {
            writeGeoFile(baseFilename, west, north, width, height, east, south);
        }
        if (kml && !geo) {
            writeKMLFile(baseFilename, xmtr, north, south, east, west, x);
        }

        FileOutputStream mpWri = null;
        try {
            File mapFile = new File(baseFilename + ".ppm");
            mpWri = new FileOutputStream(mapFile);
            notifyPPMFileWrite(mpWri, baseFilename);
            for (y = 0, lat = north; y < (int) height; y++, lat = north - (splat.getDpp() * (double) y)) {
                for (x = 0, lon = splat.getMaxWest(); x < (int) width; x++, lon = splat.getMaxWest() - (splat.getDpp() * (double) x)) {
                    if (lon < 0.0) {
                        lon += 360.0;
                    }

                    for (indx = 0, found = false; indx < config.MAXPAGES && !found;) {
                        x0 = (int) Math.rint(splat.getPpd() * (lat - (double) dem[indx].getMinNorth()));
                        y0 = splat.getMpi() - (int) Math.rint(splat.getPpd() * (Utils.lonDiff((double) dem[indx].getMaxWest(), lon)));

                        if (x0 >= 0 && x0 <= splat.getMpi() && y0 >= 0 && y0 <= splat.getMpi()) {
                            found = true;
                        } else {
                            indx++;
                        }
                    }

                    if (found) {
                        mask = dem[indx].getMask()[x0][y0];
                        loss = (dem[indx].getSignal()[x0][y0]);
                        cityorcounty = false;

                        match = 255;

                        red = 0;
                        green = 0;
                        blue = 0;

                        if (loss <= region.getLevel()[0]) {
                            match = 0;
                        } else {
                            for (z = 1; (z < region.getLevels() && match == 255); z++) {
                                if (loss >= region.getLevel()[z - 1] && loss < region.getLevel()[z]) {
                                    match = z;
                                }
                            }
                        }

                        if (match < region.getLevels()) {
                            if (splat.isSmoothContours() && match > 0) {
                                red = interpolate(region.getColor()[match - 1][0], region.getColor()[match][0], region.getLevel()[match - 1], region.getLevel()[match], loss);
                                green = interpolate(region.getColor()[match - 1][1], region.getColor()[match][1], region.getLevel()[match - 1], region.getLevel()[match], loss);
                                blue = interpolate(region.getColor()[match - 1][2], region.getColor()[match][2], region.getLevel()[match - 1], region.getLevel()[match], loss);
                            } else {
                                red = region.getColor()[match][0];
                                green = region.getColor()[match][1];
                                blue = region.getColor()[match][2];
                            }
                        }

                        if ((mask & 2) > 0) {
                            /* Text Labels: Red or otherwise */

                            if (red >= 180 && green <= 75 && blue <= 75 && loss != 0) {
                                mpWri.write(new byte[]{(byte) (255 ^ red), (byte) (255 ^ green), (byte) (255 ^ blue)});

                            } else {
                                mpWri.write(new byte[]{(byte) 255, (byte) 0, (byte) 0});
                                //mpWri.write(String.format("%c%c%c", 255, 0, 0));
                            }

                            cityorcounty = true;
                        } else if ((mask & 4) > 0) {
                            /* County Boundaries: Black */
                            mpWri.write(new byte[]{(byte) 0, (byte) 0, (byte) 0});

                            cityorcounty = true;
                        }

                        if (!cityorcounty) {
                            if (loss == 0 || (splat.getContourThreshold() != 0 && loss > Math.abs(splat.getContourThreshold()))) {
                                if (ngs) /* No terrain */ {
                                    mpWri.write(new byte[]{(byte) 255, (byte) 255, (byte) 255});
                                    //mpWri.write(String.format("%c%c%c", 255, 255, 255));
                                } else {
                                    /* Display land or sea elevation */

                                    if (dem[indx].getData()[x0][y0] == 0) {
                                        mpWri.write(new byte[]{(byte) 0, (byte) 0, (byte) 170});
                                        //mpWri.write(String.format("%c%c%c", 0, 0, 170));
                                    } else {
                                        terrain = (int) (0.5 + Math.pow((double) (dem[indx].getData()[x0][y0] - min_elevation), one_over_gamma) * conversion);
                                        mpWri.write(new byte[]{(byte) terrain, (byte) terrain, (byte) terrain});
                                        // mpWri.write(String.format("%s%s%s", terrain, terrain, terrain));
                                    }
                                }
                            } else {
                                /* Plot path loss in color */

                                if (red != 0 || green != 0 || blue != 0) {
                                    mpWri.write(new byte[]{(byte) red, (byte) green, (byte) blue});
                                    //mpWri.write(String.format("%c%c%c", red, green, blue));
                                } else /* terrain / sea-level */ {
                                    if (dem[indx].getData()[x0][y0] == 0) {
                                        mpWri.write(new byte[]{(byte) 0, (byte) 0, (byte) 170});
                                        //mpWri.write(String.format("%c%c%c", 0, 0, 170));
                                    } else {
                                        /* Elevation: Greyscale */
                                        terrain = (int) (0.5 + Math.pow((double) (dem[indx].getData()[x0][y0] - min_elevation), one_over_gamma) * conversion);
                                        mpWri.write(new byte[]{(byte) terrain, (byte) terrain, (byte) terrain});
                                        //mpWri.write(String.format("%c%c%c", terrain, terrain, terrain));
                                    }
                                }
                            }
                        }
                    } else {
                        /* We should never get here, but if */
 /* we do, display the region as black */

                        mpWri.write(new byte[]{(byte) 255, (byte) 0, (byte) 0});
                        //mpWri.write(String.format("%c%c%c", 0, 0, 0));
                    }
                }
            }
            if (!kml && !geo) {
                /* Display legend along bottom of image
                * if not generating .kml or .geo output.
                 */

                int colorwidth = (int) Math.rint((float) width / (float) region.getLevels());

                for (y0 = 0; y0 < 30; y0++) {
                    for (x0 = 0; x0 < (int) width; x0++) {
                        indx = x0 / colorwidth;
                        x = x0 % colorwidth;
                        int level = region.getLevel()[indx];

                        hundreds = level / 100;

                        if (hundreds > 0) {
                            level -= (hundreds * 100);
                        }

                        tens = level / 10;

                        if (tens > 0) {
                            level -= (tens * 10);
                        }

                        units = level;

                        if (y0 >= 8 && y0 <= 23) {
                            if (hundreds > 0) {
                                if (x >= 11 && x <= 18) {
                                    if ((FontData.get(16 * (hundreds + '0') + (y0 - 8)) & (128 >> (x - 11))) > 0) {
                                        indx = 255;
                                    }
                                }
                            }

                            if (tens > 0 || hundreds > 0) {
                                if (x >= 19 && x <= 26) {
                                    if ((FontData.get(16 * (tens + '0') + (y0 - 8)) & (128 >> (x - 19))) > 0) {
                                        indx = 255;
                                    }
                                }
                            }

                            if (x >= 27 && x <= 34) {
                                if ((FontData.get(16 * (units + '0') + (y0 - 8)) & (128 >> (x - 27))) > 0) {
                                    indx = 255;
                                }
                            }

                            if (x >= 42 && x <= 49) {
                                if ((FontData.get(16 * ('d') + (y0 - 8)) & (128 >> (x - 42))) > 0) {
                                    indx = 255;
                                }
                            }

                            if (x >= 50 && x <= 57) {
                                if ((FontData.get(16 * ('B') + (y0 - 8)) & (128 >> (x - 50))) > 0) {
                                    indx = 255;
                                }
                            }
                        }

                        if (indx > region.getLevels()) {
                            mpWri.write(new byte[]{(byte) 255, (byte) 0, (byte) 0});
                        } else {
                            red = region.getColor()[indx][0];
                            green = region.getColor()[indx][1];
                            blue = region.getColor()[indx][2];

                            mpWri.write(new byte[]{(byte) red, (byte) green, (byte) blue});
                            //mpWri.write(String.format("%c%c%c", red, green, blue));
                        }
                    }
                }
            }
            if (kml) {
                /* Write colorkey image file */

                File ckFile = new File(baseFilename + "-ck.ppm");
                FileOutputStream ckWri = new FileOutputStream(ckFile);

                height = 30 * region.getLevels();
                width = 100;

                ckWri.write(String.format("P6\n%s %s\n255\n", (int) width, (int) height).getBytes());

                for (y0 = 0; y0 < (int) height; y0++) {
                    for (x0 = 0; x0 < (int) width; x0++) {
                        indx = y0 / 30;
                        x = x0;
                        int level = region.getLevel()[indx];

                        hundreds = level / 100;

                        if (hundreds > 0) {
                            level -= (hundreds * 100);
                        }

                        tens = level / 10;

                        if (tens > 0) {
                            level -= (tens * 10);
                        }

                        units = level;

                        if ((y0 % 30) >= 8 && (y0 % 30) <= 23) {
                            if (hundreds > 0) {
                                if (x >= 11 && x <= 18) {
                                    if ((FontData.get(16 * (hundreds + '0') + ((y0 % 30) - 8)) & (128 >> (x - 11))) > 0) {
                                        indx = 255;
                                    }
                                }
                            }

                            if (tens > 0 || hundreds > 0) {
                                if (x >= 19 && x <= 26) {
                                    if ((FontData.get(16 * (tens + '0') + ((y0 % 30) - 8)) & (128 >> (x - 19))) > 0) {
                                        indx = 255;
                                    }
                                }
                            }

                            if (x >= 27 && x <= 34) {
                                if ((FontData.get(16 * (units + '0') + ((y0 % 30) - 8)) & (128 >> (x - 27))) > 0) {
                                    indx = 255;
                                }
                            }

                            if (x >= 42 && x <= 49) {
                                if ((FontData.get(16 * ('d') + ((y0 % 30) - 8)) & (128 >> (x - 42))) > 0) {
                                    indx = 255;
                                }
                            }

                            if (x >= 50 && x <= 57) {
                                if ((FontData.get(16 * ('B') + ((y0 % 30) - 8)) & (128 >> (x - 50))) > 0) {
                                    indx = 255;
                                }
                            }
                        }

                        if (indx > region.getLevels()) {
                            ckWri.write(new byte[]{(byte) 0, (byte) 0, (byte) 0});
                        } else {
                            red = region.getColor()[indx][0];
                            green = region.getColor()[indx][1];
                            blue = region.getColor()[indx][2];

                            ckWri.write(new byte[]{(byte) red, (byte) green, (byte) blue});
                        }
                    }
                }

            }
            System.out.print("Done!\n");
        } catch (IOException ex) {
            Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                mpWri.close();
            } catch (IOException ex) {
                Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void notifyPPMFileWrite(FileOutputStream mpWri, String baseFilename) throws IOException {
        if (this.kml || this.geo) {
            /* No bottom legend */

            mpWri.write(String.format("P6\n%s %s\n255\n", (int) width, (int) height).getBytes());
            System.out.printf("\nWriting \"%s\" (%sx%s pixmap image)... ", baseFilename + ".ppm", (int) width, (int) height);
        } else {
            /* Allow space for bottom legend */

            mpWri.write(String.format("P6\n%s %s\n255\n", (int) width, (int) height).getBytes());
            System.out.printf("\nWriting \"%s\" (%sx%s pixmap image)... ", baseFilename + ".ppm", (int) width, (int) height + 30);
        }
    }

    private void writeGeoFile(String baseFilename, double west, double north, double width, double height, double east, double south) {
        FileWriter wri = null;
        try {
            File geoFile = new File(baseFilename + ".geo");
            wri = new FileWriter(geoFile);
            wri.write(String.format("FILENAME\t%s\n", baseFilename + ".ppm"));
            wri.write("#\t\tX\tY\tLong\t\tLat\n");
            wri.write(String.format("TIEPOINT\t0\t0\t%.3f\t\t%.3f\n", west, north));
            wri.write(String.format("TIEPOINT\t%s\t%s\t%.3f\t\t%.3f\n", width - 1, height - 1, east, south));
            wri.write(String.format("IMAGESIZE\t%s\t%s\n", width, height));
            wri.write(String.format("#\n# Auto Generated by %s v%s\n#\n", splat.getName(), splat.getVersion()));
        } catch (IOException ex) {
            Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                wri.close();
            } catch (IOException ex) {
                Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void writeKMLFile(String baseFilename, Site[] xmtr, double north, double south, double east, double west, int x) {
        FileWriter wri = null;
        try {
            File kmlFile = new File(baseFilename + ".kml");
            wri = new FileWriter(kmlFile);
            wri.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            wri.write("<kml xmlns=\"http://earth.google.com/kml/2.1\">\n");
            wri.write(String.format("<!-- Generated by %s Version %s -->\n", splat.getName(), splat.getVersion()));
            wri.write("  <Folder>\n");
            wri.write(String.format("   <name>%s</name>\n", splat.getName()));
            wri.write(String.format("     <description>%s Transmitter Path Loss Overlay</description>\n", xmtr[0].getName()));
            wri.write("       <GroundOverlay>\n");
            wri.write("         <name>SPLAT! Path Loss Overlay</name>\n");
            wri.write("           <description>SPLAT! Coverage</description>\n");
            wri.write("		<Icon>\n");
            wri.write(String.format("              <href>%s</href>\n", baseFilename + ".ppm"));
            wri.write("		</Icon>\n");
            /* wri.write("            <opacity>128</opacity>\n"); */
            wri.write("            <LatLonBox>\n");
            wri.write(String.format("               <north>%.5f</north>\n", north));
            wri.write(String.format("               <south>%.5f</south>\n", south));
            wri.write(String.format("               <east>%.5f</east>\n", east));
            wri.write(String.format("               <west>%.5f</west>\n", west));
            wri.write("               <rotation>0.0</rotation>\n");
            wri.write("            </LatLonBox>\n");
            wri.write("       </GroundOverlay>\n");
            wri.write("       <ScreenOverlay>\n");
            wri.write("          <name>Color Key</name>\n");
            wri.write("		<description>Contour Color Key</description>\n");
            wri.write("          <Icon>\n");
            wri.write(String.format("            <href>%s</href>\n", baseFilename + "-ck.ppm"));
            wri.write("          </Icon>\n");
            wri.write("          <overlayXY x=\"0\" y=\"1\" xunits=\"fraction\" yunits=\"fraction\"/>\n");
            wri.write("          <screenXY x=\"0\" y=\"1\" xunits=\"fraction\" yunits=\"fraction\"/>\n");
            wri.write("          <rotationXY x=\"0\" y=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>\n");
            wri.write("          <size x=\"0\" y=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>\n");
            wri.write("       </ScreenOverlay>\n");
            for (x = 0; x < xmtr.length; x++) {
                wri.write("     <Placemark>\n");
                wri.write(String.format("       <name>%s</name>\n", xmtr[x].getName()));
                wri.write("       <visibility>1</visibility>\n");
                wri.write("       <Style>\n");
                wri.write("       <IconStyle>\n");
                wri.write("        <Icon>\n");
                wri.write("          <href>root://icons/palette-5.png</href>\n");
                wri.write("          <x>224</x>\n");
                wri.write("          <y>224</y>\n");
                wri.write("          <w>32</w>\n");
                wri.write("          <h>32</h>\n");
                wri.write("        </Icon>\n");
                wri.write("       </IconStyle>\n");
                wri.write("       </Style>\n");
                wri.write("      <Point>\n");
                wri.write("        <extrude>1</extrude>\n");
                wri.write("        <altitudeMode>relativeToGround</altitudeMode>\n");
                wri.write(String.format("        <coordinates>%f,%f,%f</coordinates>\n", (xmtr[x].getLon() < 180.0 ? -xmtr[x].getLon() : 360.0 - xmtr[x].getLon()), xmtr[x].getLat(), xmtr[x].getAlt()));
                wri.write("      </Point>\n");
                wri.write("     </Placemark>\n");
            }
            wri.write("  </Folder>\n");
            wri.write("</kml>\n");
        } catch (IOException ex) {
            Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                wri.close();
            } catch (IOException ex) {
                Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    void loadLossColors(Site xmtr, Region region) {
        int x;
        String lcfFilename;
        if (xmtr.getFilename().contains(".")) {
            lcfFilename = xmtr.getFilename().substring(0, xmtr.getFilename().lastIndexOf(".")) + ".lcf";
        } else {

            lcfFilename = xmtr.getFilename() + ".lcf";
        }

        /* Default values */
        region.setLevel(0, 80);
        region.setColor(0, 0, 255);
        region.setColor(0, 1, 0);
        region.setColor(0, 2, 0);

        region.setLevel(1, 90);
        region.setColor(1, 0, 255);
        region.setColor(1, 1, 128);
        region.setColor(1, 2, 0);

        region.setLevel(2, 100);
        region.setColor(2, 0, 255);
        region.setColor(2, 1, 165);
        region.setColor(2, 2, 0);

        region.setLevel(3, 110);
        region.setColor(3, 0, 255);
        region.setColor(3, 1, 206);
        region.setColor(3, 2, 0);

        region.setLevel(4, 120);
        region.setColor(4, 0, 255);
        region.setColor(4, 1, 255);
        region.setColor(4, 2, 0);

        region.setLevel(5, 130);
        region.setColor(5, 0, 184);
        region.setColor(5, 1, 255);
        region.setColor(5, 2, 0);

        region.setLevel(6, 140);
        region.setColor(6, 0, 0);
        region.setColor(6, 1, 255);
        region.setColor(6, 2, 0);

        region.setLevel(7, 150);
        region.setColor(7, 0, 0);
        region.setColor(7, 1, 208);
        region.setColor(7, 2, 0);

        region.setLevel(8, 160);
        region.setColor(8, 0, 0);
        region.setColor(8, 1, 196);
        region.setColor(8, 2, 196);

        region.setLevel(9, 170);
        region.setColor(9, 0, 0);
        region.setColor(9, 1, 148);
        region.setColor(9, 2, 255);

        region.setLevel(10, 180);
        region.setColor(10, 0, 80);
        region.setColor(10, 1, 80);
        region.setColor(10, 2, 255);

        region.setLevel(11, 190);
        region.setColor(11, 0, 0);
        region.setColor(11, 1, 38);
        region.setColor(11, 2, 255);

        region.setLevel(12, 200);
        region.setColor(12, 0, 142);
        region.setColor(12, 1, 63);
        region.setColor(12, 2, 255);

        region.setLevel(13, 210);
        region.setColor(13, 0, 196);
        region.setColor(13, 1, 54);
        region.setColor(13, 2, 255);

        region.setLevel(14, 220);
        region.setColor(14, 0, 255);
        region.setColor(14, 1, 0);
        region.setColor(14, 2, 255);

        region.setLevel(15, 230);
        region.setColor(15, 0, 255);
        region.setColor(15, 1, 194);
        region.setColor(15, 2, 204);

        region.setLevels(16);

        File splatColorFile = new File("splat.lcf");

        if (!splatColorFile.exists()) {
            FileWriter lcWri = null;
            try {
                File lcfFile = new File(lcfFilename);
                lcWri = new FileWriter(lcfFile);
                lcWri.write(String.format("; SPLAT! Auto-generated Path-Loss Color Definition (\"%s\") File\n", lcfFilename));
                lcWri.write(";\n; Format for the parameters held in this file is as follows:\n;\n");
                lcWri.write(";    dB: red, green, blue\n;\n");
                lcWri.write("; ...where \"dB\" is the path loss (in dB) and\n");
                lcWri.write("; \"red\", \"green\", and \"blue\" are the corresponding RGB color\n");
                lcWri.write("; definitions ranging from 0 to 255 for the region specified.\n");
                lcWri.write(";\n; The following parameters may be edited and/or expanded\n");
                lcWri.write("; for future runs of SPLAT!  A total of 32 contour regions\n");
                lcWri.write("; may be defined in this file.\n;\n;\n");
                for (x = 0; x < region.getLevels(); x++) {
                    lcWri.write(String.format("%3d: %3d, %3d, %3d\n", region.getLevel()[x], region.getColor()[x][0], region.getColor()[x][1], region.getColor()[x][2]));
                }
            } catch (IOException ex) {
                Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    lcWri.close();
                } catch (IOException ex) {
                    Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } else {
            try {
                Scanner scanner = new Scanner(splatColorFile);
                x = 0;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith(";")) {
                        continue;
                    }
                    String[] lines = line.split(" ");
                    region.setLevel(x, Integer.parseInt(lines[0].trim().replace(":", "")));
                    region.setColor(x, 0, Integer.parseInt(lines[1].trim()));
                    region.setColor(x, 1, Integer.parseInt(lines[2].trim()));
                    region.setColor(x, 2, Integer.parseInt(lines[3].trim()));
                    x++;

                }

                region.setLevels(x);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    void loadSignalColors(Site xmtr, Region region) {
        int x;
        String lcfFilename;
        if (xmtr.getFilename().contains(".")) {
            lcfFilename = xmtr.getFilename().substring(0, xmtr.getFilename().lastIndexOf(".")) + ".scf";
        } else {

            lcfFilename = xmtr.getFilename() + ".scf";
        }

        /* Default values */
        region.setLevel(0, 128);
        region.setColor(0, 0, 255);
        region.setColor(0, 1, 0);
        region.setColor(0, 2, 0);

        region.setLevel(1, 118);
        region.setColor(1, 0, 255);
        region.setColor(1, 1, 165);
        region.setColor(1, 2, 0);

        region.setLevel(2, 108);
        region.setColor(2, 0, 255);
        region.setColor(2, 1, 206);
        region.setColor(2, 2, 0);

        region.setLevel(3, 98);
        region.setColor(3, 0, 255);
        region.setColor(3, 1, 255);
        region.setColor(3, 2, 0);

        region.setLevel(4, 88);
        region.setColor(4, 0, 184);
        region.setColor(4, 1, 255);
        region.setColor(4, 2, 0);

        region.setLevel(5, 78);
        region.setColor(5, 0, 0);
        region.setColor(5, 1, 255);
        region.setColor(5, 2, 0);

        region.setLevel(6, 68);
        region.setColor(6, 0, 0);
        region.setColor(6, 1, 208);
        region.setColor(6, 2, 0);

        region.setLevel(7, 58);
        region.setColor(7, 0, 0);
        region.setColor(7, 1, 196);
        region.setColor(7, 2, 196);

        region.setLevel(8, 48);
        region.setColor(8, 0, 0);
        region.setColor(8, 1, 148);
        region.setColor(8, 2, 255);

        region.setLevel(9, 38);
        region.setColor(9, 0, 80);
        region.setColor(9, 1, 80);
        region.setColor(9, 2, 255);

        region.setLevel(10, 28);
        region.setColor(10, 0, 0);
        region.setColor(10, 1, 38);
        region.setColor(10, 2, 255);

        region.setLevel(11, 18);
        region.setColor(11, 0, 142);
        region.setColor(11, 1, 63);
        region.setColor(11, 2, 255);

        region.setLevel(12, 8);
        region.setColor(12, 0, 140);
        region.setColor(12, 1, 0);
        region.setColor(12, 2, 128);

        region.setLevels(13);

        File splatColorFile = new File("splat.scf");

        if (!splatColorFile.exists()) {
            FileWriter lcWri = null;
            try {
                File lcfFile = new File(lcfFilename);
                lcWri = new FileWriter(lcfFile);
                lcWri.write(String.format("; SPLAT! Auto-generated Path-Loss Color Definition (\"%s\") File\n", lcfFilename));
                lcWri.write(";\n; Format for the parameters held in this file is as follows:\n;\n");
                lcWri.write(";    dB: red, green, blue\n;\n");
                lcWri.write("; ...where \"dB\" is the path loss (in dB) and\n");
                lcWri.write("; \"red\", \"green\", and \"blue\" are the corresponding RGB color\n");
                lcWri.write("; definitions ranging from 0 to 255 for the region specified.\n");
                lcWri.write(";\n; The following parameters may be edited and/or expanded\n");
                lcWri.write("; for future runs of SPLAT!  A total of 32 contour regions\n");
                lcWri.write("; may be defined in this file.\n;\n;\n");
                for (x = 0; x < region.getLevels(); x++) {
                    lcWri.write(String.format("%3d: %3d, %3d, %3d\n", region.getLevel()[x], region.getColor()[x][0], region.getColor()[x][1], region.getColor()[x][2]));
                }
            } catch (IOException ex) {
                Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    lcWri.close();
                } catch (IOException ex) {
                    Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } else {
            try {
                Scanner scanner = new Scanner(splatColorFile);
                x = 0;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith(";")) {
                        continue;
                    }
                    String[] lines = line.split(" ");
                    region.setLevel(x, Integer.parseInt(lines[0].trim().replace(":", "")));
                    region.setColor(x, 0, Integer.parseInt(lines[1].trim()));
                    region.setColor(x, 1, Integer.parseInt(lines[2].trim()));
                    region.setColor(x, 2, Integer.parseInt(lines[3].trim()));
                    x++;

                }

                region.setLevels(x);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    void loadDBMColors(Site xmtr, Region region) {
        int x;
        String lcfFilename;
        if (xmtr.getFilename().contains(".")) {
            lcfFilename = xmtr.getFilename().substring(0, xmtr.getFilename().lastIndexOf(".")) + ".dcf";
        } else {

            lcfFilename = xmtr.getFilename() + ".dcf";
        }

        /* Default values */
        region.setLevel(0, 0);
        region.setColor(0, 0, 255);
        region.setColor(0, 1, 0);
        region.setColor(0, 2, 0);

        region.setLevel(1, -10);
        region.setColor(1, 0, 255);
        region.setColor(1, 1, 128);
        region.setColor(1, 2, 0);

        region.setLevel(2, -20);
        region.setColor(2, 0, 255);
        region.setColor(2, 1, 165);
        region.setColor(2, 2, 0);

        region.setLevel(3, -30);
        region.setColor(3, 0, 255);
        region.setColor(3, 1, 206);
        region.setColor(3, 2, 0);

        region.setLevel(4, -40);
        region.setColor(4, 0, 255);
        region.setColor(4, 1, 255);
        region.setColor(4, 2, 0);

        region.setLevel(5, -50);
        region.setColor(5, 0, 184);
        region.setColor(5, 1, 255);
        region.setColor(5, 2, 0);

        region.setLevel(6, -60);
        region.setColor(6, 0, 0);
        region.setColor(6, 1, 255);
        region.setColor(6, 2, 0);

        region.setLevel(7, -70);
        region.setColor(7, 0, 0);
        region.setColor(7, 1, 208);
        region.setColor(7, 2, 0);

        region.setLevel(8, -80);
        region.setColor(8, 0, 0);
        region.setColor(8, 1, 196);
        region.setColor(8, 2, 196);

        region.setLevel(9, -90);
        region.setColor(9, 0, 0);
        region.setColor(9, 1, 148);
        region.setColor(9, 2, 255);

        region.setLevel(10, -100);
        region.setColor(10, 0, 80);
        region.setColor(10, 1, 80);
        region.setColor(10, 2, 255);

        region.setLevel(11, -110);
        region.setColor(11, 0, 0);
        region.setColor(11, 1, 38);
        region.setColor(11, 2, 255);

        region.setLevel(12, -120);
        region.setColor(12, 0, 142);
        region.setColor(12, 1, 63);
        region.setColor(12, 2, 255);

        region.setLevel(13, -130);
        region.setColor(13, 0, 196);
        region.setColor(13, 1, 54);
        region.setColor(13, 2, 255);

        region.setLevel(14, -140);
        region.setColor(14, 0, 255);
        region.setColor(14, 1, 0);
        region.setColor(14, 2, 255);

        region.setLevel(15, -150);
        region.setColor(15, 0, 255);
        region.setColor(15, 1, 194);
        region.setColor(15, 2, 204);

        region.setLevels(16);

        File splatColorFile = new File("splat.dcf");

        if (!splatColorFile.exists()) {
            FileWriter lcWri = null;
            try {
                File lcfFile = new File(lcfFilename);
                lcWri = new FileWriter(lcfFile);
                lcWri.write(String.format("; SPLAT! Auto-generated DB Color Definition (\"%s\") File\n", lcfFilename));
                lcWri.write(";\n; Format for the parameters held in this file is as follows:\n;\n");
                lcWri.write(";    dB: red, green, blue\n;\n");
                lcWri.write("; ...where \"dB\" is the path loss (in dB) and\n");
                lcWri.write("; \"red\", \"green\", and \"blue\" are the corresponding RGB color\n");
                lcWri.write("; definitions ranging from 0 to 255 for the region specified.\n");
                lcWri.write(";\n; The following parameters may be edited and/or expanded\n");
                lcWri.write("; for future runs of SPLAT!  A total of 32 contour regions\n");
                lcWri.write("; may be defined in this file.\n;\n;\n");
                for (x = 0; x < region.getLevels(); x++) {
                    lcWri.write(String.format("%3d: %3d, %3d, %3d\n", region.getLevel()[x], region.getColor()[x][0], region.getColor()[x][1], region.getColor()[x][2]));
                }
            } catch (IOException ex) {
                Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    lcWri.close();
                } catch (IOException ex) {
                    Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } else {
            try {
                Scanner scanner = new Scanner(splatColorFile);
                x = 0;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith(";")) {
                        continue;
                    }
                    String[] lines = line.split(" ");
                    region.setLevel(x, Integer.parseInt(lines[0].trim().replace(":", "")));
                    region.setColor(x, 0, Integer.parseInt(lines[1].trim()));
                    region.setColor(x, 1, Integer.parseInt(lines[2].trim()));
                    region.setColor(x, 2, Integer.parseInt(lines[3].trim()));
                    x++;

                }

                region.setLevels(x);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void WritePPMSS(String baseFilename, boolean geo, boolean kml, boolean ngs, Site[] xmtr, Dem[] dem) {
        Region region = new Region();

        loadSignalColors(xmtr[0], region);

        configureParameters();
        setGeoBounds();
        if (!kml && geo) {
            writeGeoFile(baseFilename, west, north, width, height, east, south);
        }
        if (kml && !geo) {
            writeKMLFile(baseFilename, xmtr, north, south, east, west, x);
        }

        FileOutputStream mpWri = null;
        try {
            File mapFile = new File(baseFilename + ".ppm");
            mpWri = new FileOutputStream(mapFile);
            notifyPPMFileWrite(mpWri, baseFilename);

            for (y = 0, lat = north; y < (int) height; y++, lat = north - (splat.getDpp() * (double) y)) {
                for (x = 0, lon = splat.getMaxWest(); x < (int) width; x++, lon = splat.getMaxWest() - (splat.getDpp() * (double) x)) {
                    if (lon < 0.0) {
                        lon += 360.0;
                    }

                    for (indx = 0, found = false; indx < config.MAXPAGES && !found;) {
                        x0 = (int) Math.rint(splat.getPpd() * (lat - (double) dem[indx].getMinNorth()));
                        y0 = splat.getMpi() - (int) Math.rint(splat.getPpd() * (Utils.lonDiff((double) dem[indx].getMaxWest(), lon)));

                        if (x0 >= 0 && x0 <= splat.getMpi() && y0 >= 0 && y0 <= splat.getMpi()) {
                            found = true;
                        } else {
                            indx++;
                        }
                    }

                    if (found) {
                        mask = dem[indx].getMask()[x0][y0];
                        signal = (dem[indx].getSignal()[x0][y0]) - 100;
                        cityorcounty = false;

                        match = 255;

                        red = 0;
                        green = 0;
                        blue = 0;

                        if (signal >= region.getLevel()[0]) {
                            match = 0;
                        } else {
                            for (z = 1; (z < region.getLevels() && match == 255); z++) {
                                if (signal < region.getLevel()[z - 1] && signal >= region.getLevel()[z]) {
                                    match = z;
                                }
                            }
                        }

                        if (match < region.getLevels()) {
                            if (splat.isSmoothContours() && match > 0) {
                                red = interpolate(region.getColor()[match][0], region.getColor()[match - 1][0], region.getLevel()[match], region.getLevel()[match - 1], signal);
                                green = interpolate(region.getColor()[match][1], region.getColor()[match - 1][1], region.getLevel()[match], region.getLevel()[match - 1], signal);
                                blue = interpolate(region.getColor()[match][2], region.getColor()[match - 1][2], region.getLevel()[match], region.getLevel()[match - 1], signal);
                            } else {
                                red = region.getColor()[match][0];
                                green = region.getColor()[match][1];
                                blue = region.getColor()[match][2];
                            }
                        }

                        if ((mask & 2) > 0) {
                            /* Text Labels: Red or otherwise */

                            if (red >= 180 && green <= 75 && blue <= 75) {
                                mpWri.write(new byte[]{(byte) (255 ^ red), (byte) (255 ^ green), (byte) (255 ^ blue)});
                                //  mpWri.write(String.format("%c%c%c", , 255 ^ green, ));
                            } else {
                                mpWri.write(new byte[]{(byte) 255, (byte) 0, (byte) 0});
                                //mpWri.write(String.format("%c%c%c", 255, 0, 0));
                            }

                            cityorcounty = true;
                        } else if ((mask & 4) > 0) {
                            /* County Boundaries: Black */

                            mpWri.write(new byte[]{(byte) 0, (byte) 0, (byte) 0});
                            //mpWri.write(String.format("%c%c%c", 0, 0, 0));

                            cityorcounty = true;
                        }

                        if (!cityorcounty) {
                            if (splat.getContourThreshold() != 0 && signal < splat.getContourThreshold()) {
                                if (ngs) {
                                    mpWri.write(new byte[]{(byte) 255, (byte) 255, (byte) 255});
                                    //mpWri.write(String.format("%c%c%c", 255, 255, 255));
                                } else {
                                    /* Display land or sea elevation */

                                    if (dem[indx].getData(x0, y0) == 0) {
                                        mpWri.write(new byte[]{(byte) 0, (byte) 0, (byte) 170});
                                        //mpWri.write(String.format("%c%c%c", 0, 0, 170));
                                    } else {
                                        terrain = (0.5 + Math.pow((double) (dem[indx].getData(x0, y0) - min_elevation), one_over_gamma) * conversion);
                                        mpWri.write(new byte[]{(byte) terrain, (byte) terrain, (byte) terrain});
                                        //mpWri.write(String.format("%c%c%c", terrain, terrain, terrain));
                                    }
                                }
                            } else {
                                /* Plot field strength regions in color */

                                if (red != 0 || green != 0 || blue != 0) {
                                    mpWri.write(new byte[]{(byte) red, (byte) green, (byte) blue});
                                    //mpWri.write(String.format("%c%c%c", red, green, blue));
                                } else /* terrain / sea-level */ {
                                    if (ngs) {
                                        mpWri.write(new byte[]{(byte) 255, (byte) 255, (byte) 255});
                                        //mpWri.write(String.format("%c%c%c", 255, 255, 255));
                                    } else {
                                        if (dem[indx].getData(x0, y0) == 0) {
                                            mpWri.write(new byte[]{(byte) 0, (byte) 0, (byte) 170});
                                            //mpWri.write(String.format("%c%c%c", 0, 0, 170));
                                        } else {
                                            /* Elevation: Greyscale */
                                            terrain = (0.5 + Math.pow((double) (dem[indx].getData(x0, y0) - min_elevation), one_over_gamma) * conversion);
                                            mpWri.write(new byte[]{(byte) terrain, (byte) terrain, (byte) terrain});
                                            //mpWri.write(String.format("%c%c%c", terrain, terrain, terrain));
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        /* We should never get here, but if */
 /* we do, display the region as black */
                        mpWri.write(new byte[]{(byte) 0, (byte) 0, (byte) 0});
                        //mpWri.write(String.format("%c%c%c", 0, 0, 0));
                    }
                }
            }

            if (!kml && !geo) {
                /* Display legend along bottom of image
                * if not generating .kml or .geo output.
                 */

                int colorwidth = (int) Math.rint((float) width / (float) region.getLevels());

                for (y0 = 0; y0 < 30; y0++) {
                    for (x0 = 0; x0 < (int) width; x0++) {
                        indx = x0 / colorwidth;
                        x = x0 % colorwidth;
                        int level = region.getLevel()[indx];

                        hundreds = level / 100;

                        if (hundreds > 0) {
                            level -= (hundreds * 100);
                        }

                        tens = level / 10;

                        if (tens > 0) {
                            level -= (tens * 10);
                        }

                        units = level;

                        if (y0 >= 8 && y0 <= 23) {
                            if (hundreds > 0) {
                                if (x >= 5 && x <= 12) {
                                    if ((FontData.get(16 * (hundreds + '0') + (y0 - 8)
                                    ) & (128 >> (x - 5))) > 0) {
                                        indx = 255;
                                    }
                                }

                                if (tens > 0 || hundreds > 0) {
                                    if (x >= 13 && x <= 20) {
                                        if ((FontData.get(16 * (tens + '0') + (y0 - 8)
                                        ) & (128 >> (x - 13))) > 0) {
                                            indx = 255;
                                        }
                                    }

                                    if (x >= 21 && x <= 28) {
                                        if ((FontData.get(16 * (units + '0') + (y0 - 8)
                                        ) & (128 >> (x - 21))) > 0) {
                                            indx = 255;
                                        }
                                    }

                                    if (x >= 36 && x <= 43) {
                                        if ((FontData.get(16 * ('d') + (y0 - 8)
                                        ) & (128 >> (x - 36))) > 0) {
                                            indx = 255;
                                        }
                                    }

                                    if (x >= 44 && x <= 51) {
                                        if ((FontData.get(16 * ('B') + (y0 - 8)
                                        ) & (128 >> (x - 44))) > 0) {
                                            indx = 255;
                                        }
                                    }

                                    if (x >= 52 && x <= 59) {
                                        if ((FontData.get(16 * (230) + (y0 - 8)
                                        ) & (128 >> (x - 52))) > 0) {
                                            indx = 255;
                                        }
                                    }

                                    if (x >= 60 && x <= 67) {
                                        if ((FontData.get(16 * ('V') + (y0 - 8)
                                        ) & (128 >> (x - 60))) > 0) {
                                            indx = 255;
                                        }
                                    }

                                    if (x >= 68 && x <= 75) {
                                        if ((FontData.get(16 * ('/') + (y0 - 8)
                                        ) & (128 >> (x - 68))) > 0) {
                                            indx = 255;
                                        }
                                    }

                                    if (x >= 76 && x <= 83) {
                                        if ((FontData.get(16 * ('m') + (y0 - 8)
                                        ) & (128 >> (x - 76))) > 0) {
                                            indx = 255;
                                        }
                                    }

                                    if (indx > region.getLevels()) {
                                        mpWri.write(new byte[]{(byte) 0, (byte) 0, (byte) 0});
                                        //mpWri.write(String.format("%c%c%c", 0, 0, 0));
                                    } else {
                                        red = region.getColor()[indx][0];
                                        green = region.getColor()[indx][1];
                                        blue = region.getColor()[indx][2];
                                        mpWri.write(new byte[]{(byte) red, (byte) green, (byte) blue});
                                        //mpWri.write(String.format("%c%c%c", red, green, blue));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (kml) {
            FileOutputStream ckWri = null;
            try {
                File ckFile = new File(baseFilename + "-ck.ppm");
                ckWri = new FileOutputStream(ckFile);
                height = 30 * region.getLevels();
                width = 100;
                ckWri.write(String.format("P6\n%s %s\n255\n", (int) width, (int) height).getBytes());
                int k = 0;
                for (y0 = 0; y0 < (int) height; y0++) {
                    for (x0 = 0; x0 < (int) width; x0++) {
                        indx = y0 / 30;
                        x = x0;
                        int level = region.getLevel()[indx];

                        hundreds = level / 100;

                        if (hundreds > 0) {
                            level -= (hundreds * 100);
                        }

                        tens = level / 10;

                        if (tens > 0) {
                            level -= (tens * 10);
                        }

                        units = level;

                        if ((y0 % 30) >= 8 && (y0 % 30) <= 23) {
                            if (hundreds > 0) {
                                if (x >= 5 && x <= 12) {
                                    if ((FontData.get(16 * (hundreds + '0') + ((y0 % 30) - 8)) & (128 >> (x - 5))) > 0) {
                                        indx = 255;
                                    }
                                }
                            }

                            if (tens > 0 || hundreds > 0) {
                                if (x >= 13 && x <= 20) {
                                    if ((FontData.get(16 * (tens + '0') + ((y0 % 30) - 8)
                                    ) & (128 >> (x - 13))) > 0) {
                                        indx = 255;
                                    }
                                }
                            }

                            if (x >= 21 && x <= 28) {
                                if ((FontData.get(16 * (units + '0') + ((y0 % 30) - 8)
                                ) & (128 >> (x - 21))) > 0) {
                                    indx = 255;
                                }
                            }

                            if (x >= 36 && x <= 43) {
                                if ((FontData.get(16 * ('d') + ((y0 % 30) - 8)
                                ) & (128 >> (x - 36))) > 0) {
                                    indx = 255;
                                }
                            }

                            if (x >= 44 && x <= 51) {
                                if ((FontData.get(16 * ('B') + ((y0 % 30) - 8)
                                ) & (128 >> (x - 44))) > 0) {
                                    indx = 255;
                                }
                            }

                            if (x >= 52 && x <= 59) {
                                if ((FontData.get(16 * (230) + ((y0 % 30) - 8)
                                ) & (128 >> (x - 52))) > 0) {
                                    indx = 255;
                                }
                            }

                            if (x >= 60 && x <= 67) {
                                if ((FontData.get(16 * ('V') + ((y0 % 30) - 8)
                                ) & (128 >> (x - 60))) > 0) {
                                    indx = 255;
                                }
                            }

                            if (x >= 68 && x <= 75) {
                                if ((FontData.get(16 * ('/') + ((y0 % 30) - 8)
                                ) & (128 >> (x - 68))) > 0) {
                                    indx = 255;
                                }
                            }

                            if (x >= 76 && x <= 83) {
                                if ((FontData.get(16 * ('m') + ((y0 % 30) - 8)
                                ) & (128 >> (x - 76))) > 0) {
                                    indx = 255;
                                }
                            }
                        }

                        if (indx > region.getLevels()) {
                            ckWri.write(new byte[]{(byte) 0, (byte) 0, (byte) 0});
                        } else {
                            red = region.getColor()[indx][0];
                            green = region.getColor()[indx][1];
                            blue = region.getColor()[indx][2];
                            ckWri.write(new byte[]{(byte) red, (byte) green, (byte) blue});
                            //ckWri.write(String.format("%c%c%c", red, green, blue));
                        }
                    }
                    k++;

                }
                System.out.print("Done!\n");
            } catch (IOException ex) {
                Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    ckWri.close();
                } catch (IOException ex) {
                    Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void WritePPMDBM(String baseFilename, boolean geo, boolean kml, boolean ngs, Site[] xmtr, Dem[] dem) {
        Region region = new Region();

        loadDBMColors(xmtr[0], region);

        configureParameters();
        setGeoBounds();
        if (!kml && geo) {
            writeGeoFile(baseFilename, west, north, width, height, east, south);
        }
        if (kml && !geo) {
            writeKMLFile(baseFilename, xmtr, north, south, east, west, x);
        }

        FileOutputStream mpWri = null;
        try {
            File mapFile = new File(baseFilename + ".ppm");
            mpWri = new FileOutputStream(mapFile);
            notifyPPMFileWrite(mpWri, baseFilename);

            for (y = 0, lat = north; y < (int) height; y++, lat = north - (splat.getDpp() * (double) y)) {
                for (x = 0, lon = splat.getMaxWest(); x < (int) width; x++, lon = splat.getMaxWest() - (splat.getDpp() * (double) x)) {
                    if (lon < 0.0) {
                        lon += 360.0;
                    }

                    for (indx = 0, found = false; indx < config.MAXPAGES && !found;) {
                        x0 = (int) Math.rint(splat.getPpd() * (lat - (double) dem[indx].getMinNorth()));
                        y0 = splat.getMpi() - (int) Math.rint(splat.getPpd() * (Utils.lonDiff((double) dem[indx].getMaxWest(), lon)));

                        if (x0 >= 0 && x0 <= splat.getMpi() && y0 >= 0 && y0 <= splat.getMpi()) {
                            found = true;
                        } else {
                            indx++;
                        }
                    }

                    if (found) {
                        mask = dem[indx].getMask()[x0][y0];
                        int dBm = (dem[indx].getSignal()[x0][y0]) - 200;
                        cityorcounty = false;

                        match = 255;

                        red = 0;
                        green = 0;
                        blue = 0;

                        if (dBm >= region.getLevel()[0]) {
                            match = 0;
                        } else {
                            for (z = 1; (z < region.getLevels() && match == 255); z++) {
                                if (dBm < region.getLevel()[z - 1] && dBm >= region.getLevel()[z]) {
                                    match = z;
                                }
                            }
                        }

                        if (match < region.getLevels()) {
                            if (splat.isSmoothContours() && match > 0) {
                                red = interpolate(region.getColor()[match][0], region.getColor()[match - 1][0], region.getLevel()[match], region.getLevel()[match - 1], dBm);
                                green = interpolate(region.getColor()[match][1], region.getColor()[match - 1][1], region.getLevel()[match], region.getLevel()[match - 1], dBm);
                                blue = interpolate(region.getColor()[match][2], region.getColor()[match - 1][2], region.getLevel()[match], region.getLevel()[match - 1], dBm);
                            } else {
                                red = region.getColor()[match][0];
                                green = region.getColor()[match][1];
                                blue = region.getColor()[match][2];
                            }
                        }

                        if ((mask & 2) > 0) {
                            /* Text Labels: Red or otherwise */

                            if (red >= 180 && green <= 75 && blue <= 75 && dBm != 0) {
                                mpWri.write(new byte[]{(byte) (255 ^ red), (byte) (255 ^ green), (byte) (255 ^ green)});
                                //mpWri.write(String.format("%c%c%c", 255 ^ red, 255 ^ green, 255 ^ blue));
                            } else {
                                mpWri.write(new byte[]{(byte) 255, (byte) 0, (byte) 0});
                                //mpWri.write(String.format("%c%c%c", 255, 0, 0));
                            }

                            cityorcounty = true;
                        } else if ((mask & 4) > 0) {
                            /* County Boundaries: Black */

                            //mpWri.write(String.format("%c%c%c", 0, 0, 0));
                            mpWri.write(new byte[]{(byte) 0, (byte) 0, (byte) 0});

                            cityorcounty = true;
                        }

                        if (!cityorcounty) {
                            if (splat.getContourThreshold() != 0 && dBm < splat.getContourThreshold()) {
                                if (ngs) /* No terrain */ {
                                    mpWri.write(new byte[]{(byte) 255, (byte) 255, (byte) 255});
                                    //mpWri.write(String.format("%c%c%c", 255, 255, 255));
                                } else {
                                    /* Display land or sea elevation */

                                    if (dem[indx].getData(x0, y0) == 0) {
                                        mpWri.write(new byte[]{(byte) 0, (byte) 0, (byte) 170});
                                        //mpWri.write(String.format("%c%c%c", 0, 0, 170));
                                    } else {
                                        terrain = (0.5 + Math.pow((double) (dem[indx].getData(x0, y0) - min_elevation), one_over_gamma) * conversion);
                                        mpWri.write(new byte[]{(byte) terrain, (byte) terrain, (byte) terrain});
                                        //mpWri.write(String.format("%c%c%c", terrain, terrain, terrain));
                                    }
                                }
                            } else {
                                /* Plot signal power level regions in color */

                                if (red != 0 || green != 0 || blue != 0) {
                                    mpWri.write(new byte[]{(byte) red, (byte) green, (byte) blue});
                                    //mpWri.write(String.format("%c%c%c", red, green, blue));
                                } else /* terrain / sea-level */ {
                                    if (ngs) {
                                        mpWri.write(new byte[]{(byte) 255, (byte) 255, (byte) 255});
                                        //mpWri.write(String.format("%c%c%c", 255, 255, 255));
                                    } else {
                                        if (dem[indx].getData(x0, y0) == 0) {
                                            mpWri.write(new byte[]{(byte) 0, (byte) 0, (byte) 170});
                                            //mpWri.write(String.format("%c%c%c", 0, 0, 170));
                                        } else {
                                            /* Elevation: Greyscale */
                                            terrain = (0.5 + Math.pow((double) (dem[indx].getData(x0, y0) - min_elevation), one_over_gamma) * conversion);
                                            mpWri.write(new byte[]{(byte) terrain, (byte) terrain, (byte) terrain});
                                            // mpWri.write(String.format("%c%c%c", terrain, terrain, terrain));
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        /* We should never get here, but if */
 /* we do, display the region as black */
                        mpWri.write(new byte[]{(byte) 255, (byte) 255, (byte) 255});
                        //mpWri.write(String.format("%c%c%c", 0, 0, 0));
                    }
                }
            }

            if (!kml && !geo) {
                /* Display legend along bottom of image
                if not generating .kml or .geo output. */

                int colorwidth = (int) Math.rint((float) width / (float) region.getLevels());

                for (y0 = 0; y0 < 30; y0++) {
                    for (x0 = 0; x0 < (int) width; x0++) {
                        indx = x0 / colorwidth;
                        x = x0 % colorwidth;

                        int level = Math.abs(region.getLevel()[indx]);

                        hundreds = level / 100;

                        if (hundreds > 0) {
                            level -= (hundreds * 100);
                        }

                        tens = level / 10;

                        if (tens > 0) {
                            level -= (tens * 10);
                        }

                        units = level;

                        if (y0 >= 8 && y0 <= 23) {
                            if (hundreds > 0) {
                                if (region.getLevel()[indx] < 0) {
                                    if (x >= 5 && x <= 12) {
                                        if ((FontData.get(16 * ('-') + (y0 - 8)) & (128 >> (x - 5))) > 0) {
                                            indx = 255;
                                        }
                                    } else {
                                        if (x >= 5 && x <= 12) {
                                            if ((FontData.get(16 * ('+') + (y0 - 8)
                                            ) & (128 >> (x - 5))) > 0) {
                                                indx = 255;
                                            }
                                        }

                                        if (x >= 13 && x <= 20) {
                                            if ((FontData.get(16 * (hundreds + '0') + (y0 - 8)
                                            ) & (128 >> (x - 13))) > 0) {
                                                indx = 255;
                                            }
                                        }

                                        if (tens > 0 || hundreds > 0) {
                                            if (hundreds == 0) {
                                                if (region.getLevel()[indx] < 0) {
                                                    if (x >= 13 && x <= 20) {
                                                        if ((FontData.get(16 * ('-') + (y0 - 8)
                                                        ) & (128 >> (x - 13))) > 0) {
                                                            indx = 255;
                                                        }
                                                    } else {
                                                        if (x >= 13 && x <= 20) {
                                                            if ((FontData.get(16 * ('+') + (y0 - 8)
                                                            ) & (128 >> (x - 13))) > 0) {
                                                                indx = 255;
                                                            }
                                                        }
                                                    }

                                                    if (x >= 21 && x <= 28) {
                                                        if ((FontData.get(16 * (tens + '0') + (y0 - 8)
                                                        ) & (128 >> (x - 21))) > 0) {
                                                            indx = 255;
                                                        }
                                                    }

                                                    if (hundreds == 0 && tens == 0) {
                                                        if (region.getLevel()[indx] < 0) {
                                                            if (x >= 21 && x <= 28) {
                                                                if ((FontData.get(16 * ('-') + (y0 - 8)
                                                                ) & (128 >> (x - 21))) > 0) {
                                                                    indx = 255;
                                                                }
                                                            } else {
                                                                if (x >= 21 && x <= 28) {
                                                                    if ((FontData.get(16 * ('+') + (y0 - 8)
                                                                    ) & (128 >> (x - 21))) > 0) {
                                                                        indx = 255;
                                                                    }
                                                                }
                                                            }

                                                            if (x >= 29 && x <= 36) {
                                                                if ((FontData.get(16 * (units + '0') + (y0 - 8)
                                                                ) & (128 >> (x - 29))) > 0) {
                                                                    indx = 255;
                                                                }

                                                                if (x >= 37 && x <= 44) {
                                                                    if ((FontData.get(16 * ('d') + (y0 - 8)
                                                                    ) & (128 >> (x - 37))) > 0) {
                                                                        indx = 255;
                                                                    }

                                                                    if (x >= 45 && x <= 52) {
                                                                        if ((FontData.get(16 * ('B') + (y0 - 8)
                                                                        ) & (128 >> (x - 45))) > 0) {
                                                                            indx = 255;
                                                                        }

                                                                        if (x >= 53 && x <= 60) {
                                                                            if ((FontData.get(16 * ('m') + (y0 - 8)
                                                                            ) & (128 >> (x - 53))) > 0) {
                                                                                indx = 255;
                                                                            }
                                                                        }

                                                                        if (indx > region.getLevels()) {
                                                                            mpWri.write(new byte[]{(byte) 0, (byte) 0, (byte) 0});
                                                                            //mpWri.write(String.format("%c%c%c", 0, 0, 0));
                                                                        } else {
                                                                            red = region.getColor()[indx][0];
                                                                            green = region.getColor()[indx][1];
                                                                            blue = region.getColor()[indx][2];
                                                                            mpWri.write(new byte[]{(byte) red, (byte) green, (byte) blue});
                                                                            //mpWri.write(String.format("%c%c%c", red, green, blue));
                                                                        }
                                                                    }
                                                                }

                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (kml) {
            try {
                /* Write colorkey image file */
                FileOutputStream ckWri;
                File ckFile = new File(baseFilename + "-ck.ppm");
                ckWri = new FileOutputStream(ckFile);
                height = 30 * region.getLevels();
                width = 100;
                ckWri.write(String.format("P6\n%s %s\n255\n", (int) width, (int) height).getBytes());

                for (y0 = 0; y0 < (int) height; y0++) {
                    for (x0 = 0; x0 < (int) width; x0++) {
                        indx = y0 / 30;
                        x = x0;

                        int level = Math.abs(region.getLevel()[indx]);

                        hundreds = level / 100;

                        if (hundreds > 0) {
                            level -= (hundreds * 100);
                        }

                        tens = level / 10;

                        if (tens > 0) {
                            level -= (tens * 10);
                        }

                        units = level;

                        if ((y0 % 30) >= 8 && (y0 % 30) <= 23) {
                            if (hundreds > 0) {
                                if (region.getLevel()[indx] < 0) {
                                    if (x >= 5 && x <= 12) {
                                        if ((FontData.get(16 * ('-') + ((y0 % 30) - 8)
                                        ) & (128 >> (x - 5))) > 0) {
                                            indx = 255;
                                        }
                                    } else {
                                        if (x >= 5 && x <= 12) {
                                            if ((FontData.get(16 * ('+') + ((y0 % 30) - 8)
                                            ) & (128 >> (x - 5))) > 0) {
                                                indx = 255;
                                            }
                                        }

                                        if (x >= 13 && x <= 20) {
                                            if ((FontData.get(16 * (hundreds + '0') + ((y0 % 30) - 8)
                                            ) & (128 >> (x - 13))) > 0) {
                                                indx = 255;
                                            }
                                        }

                                        if (tens > 0 || hundreds > 0) {
                                            if (hundreds == 0) {
                                                if (region.getLevel()[indx] < 0) {
                                                    if (x >= 13 && x <= 20) {
                                                        if ((FontData.get(16 * ('-') + ((y0 % 30) - 8)
                                                        ) & (128 >> (x - 13))) > 0) {
                                                            indx = 255;
                                                        }
                                                    } else {
                                                        if (x >= 13 && x <= 20) {
                                                            if ((FontData.get(16 * ('+') + ((y0 % 30) - 8)
                                                            ) & (128 >> (x - 13))) > 0) {
                                                                indx = 255;
                                                            }
                                                        }
                                                    }

                                                    if (x >= 21 && x <= 28) {
                                                        if ((FontData.get(16 * (tens + '0') + ((y0 % 30) - 8)
                                                        ) & (128 >> (x - 21))) > 0) {
                                                            indx = 255;
                                                        }
                                                    }

                                                    if (hundreds == 0 && tens == 0) {
                                                        if (region.getLevel()[indx] < 0) {
                                                            if (x >= 21 && x <= 28) {
                                                                if ((FontData.get(16 * ('-') + ((y0 % 30) - 8)) & (128 >> (x - 21))) > 0) {
                                                                    indx = 255;
                                                                }
                                                            }
                                                        } else {
                                                            if (x >= 21 && x <= 28) {
                                                                if ((FontData.get(16 * ('+') + ((y0 % 30) - 8)) & (128 >> (x - 21))) > 0) {
                                                                    indx = 255;
                                                                }
                                                            }
                                                        }
                                                    }

                                                    if (x >= 29 && x <= 36) {
                                                        if ((FontData.get(16 * (units + '0') + ((y0 % 30) - 8)) & (128 >> (x - 29))) > 0) {
                                                            indx = 255;
                                                        }
                                                    }

                                                    if (x >= 37 && x <= 44) {
                                                        if ((FontData.get(16 * ('d') + ((y0 % 30) - 8)) & (128 >> (x - 37))) > 0) {
                                                            indx = 255;
                                                        }
                                                    }

                                                    if (x >= 45 && x <= 52) {
                                                        if ((FontData.get(16 * ('B') + ((y0 % 30) - 8)) & (128 >> (x - 45))) > 0) {
                                                            indx = 255;
                                                        }
                                                    }

                                                    if (x >= 53 && x <= 60) {
                                                        if ((FontData.get(16 * ('m') + ((y0 % 30) - 8)) & (128 >> (x - 53))) > 0) {
                                                            indx = 255;
                                                        }
                                                    }
                                                }

                                                if (indx > region.getLevels()) {
                                                    ckWri.write(new byte[]{(byte) 0, (byte) 0, (byte) 0});
                                                    //ckWri.write(String.format("%c%c%c", 0, 0, 0));
                                                } else {
                                                    red = region.getColor()[indx][0];
                                                    green = region.getColor()[indx][1];
                                                    blue = region.getColor()[indx][2];
                                                    ckWri.write(new byte[]{(byte) red, (byte) green, (byte) blue});
                                                    //ckWri.write(String.format("%c%c%c", red, green, blue));
                                                }
                                            }
                                        }

                                    }

                                    System.out.print("Done!\n");
                                }
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(PPMGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    int interpolate(int y0, int y1, int x0, int x1, int n) {
        /* Perform linear interpolation between quantized contour
                            levels displayed in field strength and path loss maps.
                            If signal level x0 corresponds to color level y0, signal
                            level x1 corresponds to color level y1, and signal level
                            n falls somewhere between x0 and x1, determine what
                            color value n corresponds to between y0 and y1. */

        int result = 0;
        double delta_x, delta_y;

        if (n <= x0) {
            return y0;
        }

        if (n >= x1) {
            return y1;
        }

        if (y0 == y1) {
            return y0;
        }

        if (x0 == x1) {
            return y0;

        }

        delta_y = (double) (y0 - y1);
        delta_x = (double) (x0 - x1);

        result = y0 + (int) Math.ceil((delta_y / delta_x) * (n - x0));

        return result;
    }
}
