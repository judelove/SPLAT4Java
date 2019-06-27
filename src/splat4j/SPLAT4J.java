/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splat4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.System.getenv;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import static splat4j.SPLAT4J.config;
import splat4j.input.CitiesFile;
import splat4j.input.LRParameters;
import splat4j.output.ANOFile;
import splat4j.input.QTHFile;
import splat4j.input.SDFAgent;
import splat4j.input.UDTFile;
import splat4j.output.GraphGenerator;
import splat4j.output.ImageGenerator;
import splat4j.output.KMLGenerator;
import splat4j.output.ReportGenerator;


/**
 *****************************************************************************\
 * SPLAT!: An RF Signal Path Loss And Terrain Analysis Tool (Java Version) *
 * *****************************************************************************
 * based on SPLAT by John A. Magliacane *
 * *****************************************************************************
 * * This program is free software; you can redistribute it and/or modify it *
 * under the terms of the GNU General Public License as published by the * Free
 * Software Foundation; either version 2 of the License or any later * version.
 * * * This program is distributed in the hope that it will useful, but WITHOUT
 * * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or *
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License * for
 * more details.	
 * * @author Jude Mukundane
 */
public class SPLAT4J {

    static Configuration config;
    static SplatEngine splat;
    //private Dem[] dems;
    static QTHFile[] txFiles, RxFiles;
    static CitiesFile[] citiesFiles;
    static ArrayList<ANOFile> anoFiles;
    static ArrayList<Site> txSites;
    static ArrayList<Site> rxSites;
    static int x, y, z = 0, min_lat, min_lon, max_lat, max_lon,
            rxlat, rxlon, txlat, txlon;

    static int cities = 0, bfs = 0, txsites = 0, max_txsites = 30,
            nositereports = 0;

    static ArrayList<String> city_file, boundary_file;

    static int rxsite = 0;

    static String mapfile, header,
            elevation_file, height_file,
            longley_file, terrain_file,
            string, rxfilenames,
            txfileNames,
            udt_file, ani_filename,
            ano_filename, ext, logfile="log.splt", sdf_path;

//        char[] mapfile = new char[255], header = new char[80],
//                elevation_file = new char[255], height_file = new char[255],
//                longley_file = new char[255], terrain_file = new char[255],
//                string = new char[255], rxfile = new char[255],
//                txfile = new char[255],
//                udt_file = new char[255], ani_filename = new char[255],
//                ano_filename = new char[255], ext = new char[20], logfile = new char[255];
    static double altitude = 0.0, altitudeLR = 0.0, tx_range = 0.0, max_range,
            rx_range = 0.0, deg_range = 0.0, deg_limit = 0.0,
            deg_range_lon, er_mult, contour_threshold, LRaltitude;

    static File fd;

    static String env, splat_version = "1.4.2 (Java)";
    static String splat_name, dashes;
    private static ArrayList<Site> boundSites;

    /**
     * @param argv the command line arguments
     */
    public static void main(String[] argv) //(int argc, char argv[])
    {
        config = new Configuration();
        splat = new SplatEngine(config);

        if (config.HD_ON) {
            splat_name = "SPLAT! HD";
        } else {
            splat_name = "SPLAT!";
        }

        dashes = "---------------------------------------------------------------------------";

        if (argv.length == 0) {
            reportLaunchError();
            return;
        }

        y = argv.length;

         splat.setClutter(0.0);
       splat.setForcedErp(-1.0);
        splat.setForcedFreq(0.0);

        max_txsites = 30;

        splat.setSmoothContours(false);
        splat.setEarthradius(config.EARTHRADIUS);

        splat.setIppd(config.IPPD);
        /* pixels per degree (integer) */
        splat.setPpd(splat.getIppd());
        /* pixels per degree (double)  */
        splat.setDpp(1.0 / splat.getPpd());
        /* degrees per pixel */
        splat.setMpi(splat.getIppd() - 1);
        /* maximum pixel index per degree */

        header = String.format("\n\t\t--==[ Welcome To %s v%s ]==--\n\n", splat_name, splat_version);
        System.out.println(String.join(" ", argv));
        processInput(argv);
        metricise(splat.isMetric());
        configure();
        
        loadReceiverSite();
        if(!locateSDFFiles())
        {
            System.out.println("SDF path not found");
            return;
        }
        
        
        plot();
        generateGraphs();
        writeLog(argv);
        //splat.loadFiles(txSites, rxSites, max_txsites);
    }

    private static void reportLaunchError() {
        System.out.printf("\n\t\t --==[ %s v%s Available Options... ]==--\n\n", splat_name, splat_version);

        System.out.printf("       -t txsite(s).qth (max of 4 with -c, max of 30 with -L)\n");
        System.out.printf("       -r rxsite.qth\n");
        System.out.printf("       -c plot LOS coverage of TX(s) with an RX antenna at X feet/meters AGL\n");
        System.out.printf("       -L plot path loss map of TX based on an RX at X feet/meters AGL\n");
        System.out.printf("       -s filename(s) of city/site file(s) to import (5 max)\n");
        System.out.printf("       -b filename(s) of cartographic boundary file(s) to import (5 max)\n");
        System.out.printf("       -p filename of terrain profile graph to plot\n");
        System.out.printf("       -e filename of terrain elevation graph to plot\n");
        System.out.printf("       -h filename of terrain height graph to plot\n");
        System.out.printf("       -H filename of normalized terrain height graph to plot\n");
        System.out.printf("       -l filename of path loss graph to plot\n");
        System.out.printf("       -o filename of topographic map to generate (.ppm)\n");
        System.out.printf("       -u filename of user-defined terrain file to import\n");
        System.out.printf("       -d sdf file directory path (overrides path in ~/.splat_path file)\n");
        System.out.printf("       -m earth radius multiplier\n");
        System.out.printf("       -n do not plot LOS paths in .ppm maps\n");
        System.out.printf("       -N do not produce unnecessary site or obstruction reports\n");
        System.out.printf("       -f frequency for Fresnel zone calculation (MHz)\n");
        System.out.printf("       -R modify default range for -c or -L (miles/kilometers)\n");
        System.out.printf("      -sc display smooth rather than quantized contour levels\n");
        System.out.printf("      -db threshold beyond which contours will not be displayed\n");
        System.out.printf("      -nf do not plot Fresnel zones in height plots\n");
        System.out.printf("      -fz Fresnel zone clearance percentage (default = 60)\n");
        System.out.printf("      -gc ground clutter height (feet/meters)\n");
        System.out.printf("     -ngs display greyscale topography as white in image files\n");
        System.out.printf("     -erp override ERP in .lrp file (Watts)\n");
        System.out.printf("     -ano name of alphanumeric output file\n");
        System.out.printf("     -ani name of alphanumeric input file\n");
        System.out.printf("     -udt name of user defined terrain input file\n");
        System.out.printf("     -kml generate Google Earth (.kml) compatible output\n");
        System.out.printf("     -geo generate an Xastir .geo georeference file (with .ppm output)\n");
        System.out.printf("     -dbm plot signal power level contours rather than field strength\n");
        System.out.printf("     -log copy command line string to this output file\n");
        System.out.printf("   -gpsav preserve gnuplot temporary working files after SPLAT! execution\n");
        System.out.printf("  -metric employ metric rather than imperial units for all user I/O\n");
        System.out.printf("  -olditm invoke Longley-Rice rather than the default ITWOM model\n\n");
        System.out.printf("  -png generate PNG image instead of PPM image format \n\n");
        System.out.printf("  -png_tr generate PNG image instead of PPM image format, but with transparency where no signal \n\n");
        System.out.printf("If that flew by too fast, consider piping the output through 'less':\n");

        if (!config.HD_ON) {
            System.out.printf("\n\tsplat | less\n\n");
        } else {
            System.out.printf("\n\tsplat-hd | less\n\n");
        }

        System.out.printf("Type 'man splat', or see the documentation for more details.\n\n");

        y = (int) Math.sqrt((int) config.MAXPAGES);

        System.out.printf("This compilation of %s supports analysis over a region of %d square\n", splat_name, y);

        if (y == 1) {
            System.out.printf("degree");
        } else {
            System.out.printf("degrees");
        }

        System.out.printf(" of terrain, and computes signal levels using ITWOM Version %s.\n\n", "3.0"); //ITWOMVersion());
    }
    
    static void setBounds()
    {
        boundSites = new ArrayList<Site>();
         for (x = 0; x < 4; x++) {
            boundSites.add(x, new Site(null, null, 91.0, 361.0, 0));
        }
    }

    static void configure() {
      //
//        for (x = 0; x < 4; x++) {
//            txSites.add(x, new Site(null, null, 91.0, 361.0, 0));
//        }
//

if(splat.isMap() && (mapfile == null || mapfile.isEmpty()))
{
    mapfile = "output_map";
}
        x = 0;
        y = 0;

        min_lat = 90;
        max_lat = -90;

        min_lon = (int) Math.floor(txSites.get(0).getLon());
        max_lon = (int) Math.floor(txSites.get(0).getLon());

        for (y = 0, z = 0; z < txsites && z < max_txsites; z++) {
            txlat = (int) Math.floor(txSites.get(z).getLat());
            txlon = (int) Math.floor(txSites.get(z).getLon());

            if (txlat < min_lat) {
                min_lat = txlat;
            }

            if (txlat > max_lat) {
                max_lat = txlat;
            }

            if (Utils.lonDiff(txlon, min_lon) < 0.0) {
                min_lon = txlon;
            }

            if (Utils.lonDiff(txlon, max_lon) >= 0.0) {
                max_lon = txlon;
            }
        }

        /* Load the required SDF files */
        SDFAgent sdfAgent = new SDFAgent(splat.getSdfPath());
        sdfAgent.loadTopoData(config, splat, max_lon, min_lon, max_lat, min_lat);
        //LoadTopoData(max_lon, min_lon, max_lat, min_lat);

        if (splat.isArea_mode() || splat.isTopomap()) {
            for (z = 0; z < txsites && z < max_txsites; z++) {
                /* "Ball park" estimates used to load any additional
			   SDF files required to conduct this analysis. */
                double al = txSites.get(z).getAlt();
                double el = Utils.getElevation(txSites.get(z), splat, config);

                tx_range = Math.sqrt(1.5 * (al + el));

                if (splat.isLRmap()) {
                    rx_range = Math.sqrt(1.5 * splat.getLRaltitude());
                } else {
                    rx_range = Math.sqrt(1.5 * splat.getAltitude());
                }

                /* deg_range determines the maximum
			   amount of topo data we read */
                deg_range = (tx_range + rx_range) / 57.0;

                /* max_range regulates the size of the
			   analysis.  A small, non-zero amount can
			   be used to shrink the size of the analysis
			   and limit the amount of topo data read by
			   SPLAT!  A large number will increase the
			   width of the analysis and the size of
			   the map. */
                if (splat.getMaxRange() == 0.0) {
                    splat.setMaxRange(tx_range + rx_range);
                }

                deg_range = splat.getMaxRange() / 57.0;

                /* Prevent the demand for a really wide coverage
			   from allocating more "pages" than are available
			   in memory. */
                switch (config.MAXPAGES) {
                    case 1:
                        deg_limit = 0.125;
                        break;

                    case 2:
                        deg_limit = 0.25;
                        break;

                    case 4:
                        deg_limit = 0.5;
                        break;

                    case 9:
                        deg_limit = 1.0;
                        break;

                    case 16:
                        deg_limit = 1.5;
                        /* WAS 2.0 */
                        break;

                    case 25:
                        deg_limit = 2.0;
                        /* WAS 3.0 */
                        break;

                    case 36:
                        deg_limit = 2.5;
                        /* New! */
                        break;

                    case 49:
                        deg_limit = 3.0;
                        /* New! */
                        break;

                    case 64:
                        deg_limit = 3.5;
                        /* New! */
                        break;
                }

                if (Math.abs(txSites.get(z).getLat()) < 70.0) {
                    deg_range_lon = deg_range / Math.cos(config.DEG2RAD * txSites.get(z).getLat());
                } else {
                    deg_range_lon = deg_range / Math.cos(config.DEG2RAD * 70.0);
                }

                /* Correct for squares in degrees not being square in miles */
                if (deg_range > deg_limit) {
                    deg_range = deg_limit;
                }

                if (deg_range_lon > deg_limit) {
                    deg_range_lon = deg_limit;
                }

                splat.setMinNorth((int) Math.floor(txSites.get(z).getLat() - deg_range));
                splat.setMaxNorth((int) Math.floor(txSites.get(z).getLat() + deg_range));
                splat.setMinWest((int) Math.floor(txSites.get(z).getLon() - deg_range_lon));
                
//                north_min = (int) Math.floor(txSites.get(z).getLat() - deg_range);
//                north_max = (int) Math.floor(txSites.get(z).getLat() + deg_range);
//
//                west_min = (int) Math.floor(txSites.get(z).getLon() - deg_range_lon);

                while (splat.getMinWest() < 0) {
                    splat.setMinWest(splat.getMinWest() + 360);
                }

                while (splat.getMinWest() >= 360) {
                    splat.setMinWest(splat.getMinWest() - 360);
                }

                splat.setMaxWest((int) Math.floor(txSites.get(z).getLon() + deg_range_lon));

                while (splat.getMaxWest() < 0) {
                    splat.setMaxWest(splat.getMaxWest() + 360);
                }

                while (splat.getMaxWest() >= 360) {
                    splat.setMaxWest(splat.getMaxWest() - 360);
                }

                if (splat.getMinNorth() < min_lat) {
                    min_lat = splat.getMinNorth();
                }

                if (splat.getMaxNorth() > max_lat) {
                    max_lat = splat.getMaxNorth();
                }

                if (Utils.lonDiff(splat.getMinWest(), min_lon) < 0.0) {
                    min_lon = splat.getMinWest();
                }

                if (Utils.lonDiff(splat.getMaxWest(), max_lon) >= 0.0) {
                    max_lon = splat.getMaxWest();
                }
            }

            /* Load any additional SDF files, if required */
            sdfAgent.loadTopoData(config, splat, max_lon, min_lon, max_lat, min_lat);
        }

        if (udt_file != null) {
            UDTFile udtFile = new UDTFile(udt_file);

            udtFile.load(config, splat);
        }

    }

    static void plot() {
        /**
         * *** Let the SPLATting begin! ****
         */
        if (splat.isPt2pt_mode()) {
            System.out.print("is p2p");
            splat.PlaceMarker(rxSites.get(0));

            if (splat.isTopomap()) {
                /* Extract extension (if present)
			   from "terrain_file" */

                if (terrain_file.contains(".")) {
                    ext = terrain_file.substring(terrain_file.lastIndexOf("."));
                    terrain_file = terrain_file.substring(0, terrain_file.indexOf("."));
                } else {
                    ext = "png";
                }

//                for (x = y - 1; x > 0 && terrain_file[x] != '.'; x--);
//
//                if (x > 0) /* Extension found */ {
//                    for (z = x + 1; z <= y && (z - (x + 1)) < 10; z++) {
//                        ext[z - (x + 1)] = tolower(terrain_file[z]);
//                    }
//
//                    ext[z - (x + 1)] = 0;
//                    /* Ensure an ending 0 */
//                    terrain_file[x] = 0;
//                    /* Chop off extension */
//                } else {
//                    strncpy(ext, "png\0", 4);
//                }
            }

            if (splat.isElevation_plot()) {
                /* Extract extension (if present)
			   from "elevation_file" */

                if (elevation_file.contains(".")) {
                    ext = elevation_file.substring(elevation_file.lastIndexOf("."));
                    elevation_file = elevation_file.substring(0, elevation_file.indexOf("."));
                } else {
                    ext = "png";
                }
            }

            if (splat.isHeight_plot()) {
                /* Extract extension (if present)
			   from "height_file" */
                if (height_file.contains(".")) {
                    ext = height_file.substring(height_file.lastIndexOf("."));
                    height_file = height_file.substring(0, height_file.indexOf("."));
                } else {
                    ext = "png";
                }
            }

            if (splat.isLongley_plot()) {
                /* Extract extension (if present)
			   from "longley_file" */

                if (longley_file.contains(".")) {
                    ext = longley_file.substring(longley_file.lastIndexOf("."));
                    longley_file = longley_file.substring(0, longley_file.indexOf("."));
                } else {
                    ext = "png";
                }
            }

            for (x = 0; x < txsites && x < 4; x++) {
                splat.PlaceMarker(txSites.get(x));

                if (!splat.isNolospath()) {
                    switch (x) {
                        case 0:
                            splat.PlotPath(txSites.get(x), rxSites.get(0), 1);
                            break;

                        case 1:
                            splat.PlotPath(txSites.get(x), rxSites.get(0), 8);
                            break;

                        case 2:
                            splat.PlotPath(txSites.get(x), rxSites.get(0), 16);
                            break;

                        case 3:
                            splat.PlotPath(txSites.get(x), rxSites.get(0), 32);
                    }
                }

                if (!splat.isNoSitesReport()) {
                    ReportGenerator reporter = new ReportGenerator(config, splat);
                    reporter.SiteReport(txSites.get(x));
                    //SiteReport(tx_site[x]);
                }

                if (splat.isKml()) {
                    KMLGenerator kmler = new KMLGenerator(config, splat);
                    kmler.WriteKML(txSites.get(x), rxSites.get(0));

                    if (txsites > 1) {
                        string = String.format("%s-%s.%s%c", longley_file, '1' + x, ext, 0);
                    } else {
                        string = String.format("%s.%s%c", longley_file, ext, 0);
                    }

                    //TODO: validate the below for use of PathReport last parameter
                    if (!splat.isNorm()) 
                        if (longley_file != null) {
                            LRParameters lrp = new LRParameters(splat);
                            lrp.loadLRFile(longley_file);
                            new ReportGenerator(config, splat).PathReport(txSites.get(x), rxSites.get(0),"profile", true); //destination, header, true, true, true, altitude, rxlat, string, string, true);
                            //ReadLRParm(tx_site[x], 0);
                            //PathReport(tx_site[x], rx_site, string, 0);
                        } else {
                            LRParameters lrp = new LRParameters(splat);
                            lrp.loadLRFile("");
                            //ReadLRParm(tx_site[x], 1);
                            new ReportGenerator(config, splat).PathReport(txSites.get(x), rxSites.get(0),"profile",  false);
                            //PathReport(tx_site[x], rx_site, string, longley_file[0]);
                        }
                    }

                    if (splat.isTerrain_plot()) {
                        if (txsites > 1) {
                            string = String.format("%s-%s.%s%c", terrain_file, '1' + x, ext, 0);
                        } else {
                            string = String.format("%s.%s%c", terrain_file, ext, 0);
                        }

                        new GraphGenerator(config, splat).GraphTerrain(txSites.get(x), rxSites.get(0), string);
                    }

                    if (splat.isElevation_plot()) {
                        if (txsites > 1) {
                            string = String.format("%s-%s.%s%c", elevation_file, '1' + x, ext, 0);
                        } else {
                            string = String.format("%s.%s%c", elevation_file, ext, 0);
                        }

                        new GraphGenerator(config, splat).GraphElevation(txSites.get(x), rxSites.get(0), string);
                    }

                    if (splat.isHeight_plot()) {
                        if (txsites > 1) {
                            string = String.format("%s-%s.%s%c", height_file, '1' + x, ext, 0);
                        } else {
                            string = String.format("%s.%s%c", height_file, ext, 0);
                        }

                        new GraphGenerator(config, splat).GraphHeight(txSites.get(x), rxSites.get(0), string, splat.isFresnel_plot(), splat.isNorm());
                    }
                }
            }

            if (splat.isArea_mode() && !splat.isTopomap()) {
                System.out.print("is area");
                for (x = 0; x < txsites && x < max_txsites; x++) {
                    if (splat.isCoverage()) {
                        System.out.print("is coverage");
                        splat.PlotLOSMap(txSites.get(txSites.size() -1), splat.getAltitude());
                    } else{
                        new LRParameters(splat).loadLRFile(txSites.get(x).getName());
                        splat.PlotLRMap(txSites.get(x), splat.getLRaltitude(), ano_filename);
                    }

                     new ReportGenerator(config, splat).SiteReport(txSites.get(x)); //SiteReport(tx_site[x]);
                }
            }

            if (splat.isMap() || splat.isTopomap()) {
                /* Label the map */

                if (!splat.isKml()) {
                    for (x = 0; x < txsites && x < max_txsites; x++) {
                        splat.PlaceMarker(txSites.get(x));
                    }
                }

//            if (cities) {
//
//                for (y = 0; y < cities; y++) {
//                    LoadCities(city_file[y]);
//                }
//
//                System.out.printf("\n");
//                fflush(stdout);
//            }
//
//            /* Load city and county boundary data files */
//            if (bfs) {
//                for (y = 0; y < bfs; y++) {
//                    LoadBoundaries(boundary_file[y]);
//                }
//
//                System.out.printf("\n");
//                fflush(stdout);
//            }
                //generateGraphs();
                /* Plot the map */

            }

            /* That's all, folks! */
            return;
        }
    

    static void writeLog(String[] args) {
        if (splat.isCommand_line_log() && logfile != null) {
            FileWriter fwr = null;
            try {
                File log = new File(logfile);
                fwr = new FileWriter(log);
                for (x = 0; x < args.length; x++) {
                    fwr.write(String.format("%s ", args[x]));
                }
                fwr.write("\n");
                System.out.printf("\nCommand-line parameter log written to: \"%s\"\n", logfile);
            } catch (IOException ex) {
                Logger.getLogger(SPLAT4J.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    fwr.close();
                } catch (IOException ex) {
                    Logger.getLogger(SPLAT4J.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }

    

    static void metricise(boolean metric) {
        /* Adjust input parameters if -metric option is used */
        if (metric) {
            splat.setLRaltitude(splat.getLRaltitude() / config.METERS_PER_FOOT);
            /* meters --> feet */
            splat.setMaxRange(splat.getMaxRange() / config.KM_PER_MILE);
            /* kilometers --> miles */
            splat.setAltitude(splat.getAltitude() / config.METERS_PER_FOOT);
            /* meters --> feet */
            splat.setClutter(splat.getClutter() / config.METERS_PER_FOOT);
            /* meters --> feet */
        }
    }

    

    static boolean processInput(String[] args) {
        /* Scan for command line arguments */
        txSites = rxSites = new ArrayList();
        for (int x = 0; x < args.length; x++) {
            switch (args[x]) {
                case "-HD":
                    config.HD_ON = true;
                    break;
                case "-R":
                    try {
                        splat.setMaxRange(Math.max(0, Math.min(1000, Double.parseDouble(args[x + 1]))));
                    } catch (Exception ex) {
                        return false;
                    }
                    break;
                case "-m":
                    try {
                        er_mult = Math.max(1, Math.min(1.0e6, Double.parseDouble(args[x + 1])));
                        splat.setEarthradius(splat.getEarthradius() * er_mult);
                    } catch (Exception ex) {
                        return false;
                    }
                    break;
                case "-gc":
                    try {
                        splat.setClutter(Math.max(0, Double.parseDouble(args[x + 1])));
                    } catch (Exception ex) {
                        return false;
                    }
                    break;
                case "-fz":
                    try {
                        double fzClearance = Double.parseDouble(args[x + 1]);
                        splat.setFzoneClearance((fzClearance < 0 || fzClearance > 100) ? 60 / 100 : fzClearance / 100);
                    } catch (Exception ex) {
                        return false;
                    }
                    break;
                case "-o":
                    try {
                        if(!args[x+1].startsWith("-"))
                        {
                        mapfile = args[x + 1];
                        splat.setMap(true);
                        }
                        else
                        {
                            System.err.println("Please specify a file name for the output map file");
                            return false;
                        }
                    } catch (Exception ex) {
                        return false;
                    }
                    break;
                case "-log":
                    try {
                        logfile = args[x + 1];
                        splat.setCommand_line_log(true);
                    } catch (Exception ex) {
                        return false;
                    }
                    break;
                case "-udt":
                    try {
                        udt_file = args[x + 1];
                    } catch (Exception ex) {
                        return false;
                    }
                case "-c":
                    try{
                    if(splat.isLRmap())
                    {
                        System.err.println("-c and -L are exclusive. ignoring -c option");
                    }
                    else
                    {
                        splat.setAltitude(Double.parseDouble(args[x + 1]));
                        splat.setMap(true);
                        splat.setCoverage(true);
                        splat.setArea_mode(true);
                        max_txsites = 4;
                    }} catch (Exception ex) {
                        System.err.println("Wrong argument supplied for -c or -L option. Height expected");
                        return false;
                    }
                    break;
                case "-db":
                case "-dB":
                    try {
                        splat.setContourThreshold(Integer.parseInt(args[x + 1]));
                    } catch (Exception ex) {
                        return false;
                    }
                    break;
                case "-p":
                    terrain_file = args[x + 1];
                    splat.setTerrain_plot(true);
                    splat.setPt2pt_mode(true);
                    break;
                case "-e":
                    elevation_file = args[x + 1];
                    splat.setElevation_plot(true);
                    splat.setPt2pt_mode(true);
                    break;
                case "-H":
                    height_file = args[x + 1];
                    splat.setHeight_plot(true);
                    splat.setPt2pt_mode(true);
                    splat.setNorm(true);
                    break;
                case "-h":
                    height_file = args[x + 1];
                    splat.setHeight_plot(true);
                    splat.setPt2pt_mode(true);
                    splat.setNorm(false);
                    break;
                case "-metric":
                    splat.setMetric(true);
                    break;
                case "-gpsav":
                    splat.setGpsav(true);
                    break;
                case "-geo":
                    splat.setGeo(true);
                    break;

                case "-kml":
                    splat.setKml(true);
                    break;

                case "-nf":
                    splat.setFresnel_plot(false);
                    break;
                case "-ngs":
                    splat.setNgs(true);
                    break;

                case "-n":
                    splat.setNolospath(true);
                    break;

                case "-dbm":
                    splat.setDbm(true);
                    break;

                case "-sc":
                    splat.setSmoothContours(true);
                    break;

                case "-olditm":
                    splat.setOlditm(true);
                    break;

                case "-N":
                    splat.setNolospath(true);
                    splat.setNoSitesReport(true);
                    break;
                    
                case "-ppm":
                    if(splat.isTransparentPng())
                    {
                        System.err.println("This option can not be specified with the option for transparent PNG (-png_tr)");
                        return false;
                    }
                    splat.setGeneratePpm(true);
                    break;
                    
                case "-png_tr":
                    if(splat.isGeneratePpm())
                    {
                        System.err.println("This option can not be specified with the option to generate PPM (-ppm)");
                        return false;
                    }
                    splat.setTransparentPng(true);
                    break;

                case "-d":
                    splat.setSdfPath(args[x+1]);
                    //sdf_path = args[x + 1];
                    break;
                case "-t":
                    txsites = 0;
                    /* Read Transmitter Location */
                    while (txsites < 30 && !args[x + 1 + txsites].startsWith("-")) {
                        txSites.add(new Site(new QTHFile(args[x + 1+ txsites])));
                        //tx_site[txsites] = LoadQTH(txfile);
                        txsites++;
                    }
                    if (txsites == 0) {
                        System.err.println("\n%c*** ERROR: No transmitter site(s) specified!\n\n");
                        return false;
                    }
                    break;
                case "-L":
                    
                    if (splat.isCoverage()) {
                        System.out.printf("c and L are exclusive options, ignoring L.\n");
                    }
                    else
                    {
                        splat.setLRaltitude(Double.parseDouble(args[x + 1]));
                    splat.setMap(true);
                    splat.setLRmap(true);
                    splat.setArea_mode(true);

                    }
                    break;
                case "-l":
                    longley_file = args[x + 1];
                    splat.setLongley_plot(true);
                    splat.setPt2pt_mode(true);
                    break;
                case "-r":
                    /* Read receiver Location */
                    rxSites.add(new Site(new QTHFile(args[x + 1])));
                    splat.setPt2pt_mode(true);
                    break;
                case "-s":
                    cities = 0;
                    /* Read Transmitter Location */
                    while (cities < 5 && !args[x + cities].startsWith("-")) {
                        citiesFiles[cities] = new CitiesFile(args[x + cities]); //dd(new Site());
                        cities++;
                    }
                    break;
//                case "-b":
//                    bfs = 0;
//                    /* Read Transmitter Location */
//                    while (bfs < 5 && !args[x + bfs].startsWith("-")) {
//                        boundary_file[bfs] = args[x + bfs]; //dd(new Site());
//
//                        bfs++;
//                    }
//                    break;
                case "-f":
                    try {
                        double forced_freq = Double.parseDouble(args[x + 1]);
                        splat.setForcedFreq(Math.min(20.0e3, forced_freq < 20 ? 0 : forced_freq));
                    } catch (Exception ex) {
                        return false;
                    }
                    break;
                case "-erp":
                    try {
                        double forcedErp = Double.parseDouble(args[x + 1]);
                        splat.setForcedErp(forcedErp > 0 ? -1 : forcedErp);
                    } catch (Exception ex) {
                        return false;
                    }
                    break;
                case "-ano":
                    ano_filename = args[x + 1];
                    break;
                case "-ani":
                    ani_filename = args[x + 1];
                    break;
            }
        }
        if (!(splat.isCoverage() || splat.isLRmap() || !(ani_filename == null || ani_filename.isEmpty()))) { // && rx_site.lat == 91.0 && rx_site.lon == 361.0) {
            if (max_range != 0.0 && txsites != 0) {
                /* Plot topographic map of radius "max_range" */

                splat.setMap(false);
                splat.setTopomap(true);
            } else {
                System.err.println("\n%c*** ERROR: No receiver site found or specified!\n\n");
                return false;
            }
        }
        return true;
    }

//replace below with error checking on loading qth sites, check for atleast one qth site for tx
//    for (x  = 0, y = 0;
//    x< txsites ;
//    x
//
//    
//        ++) {
//            if (tx_site[x].lat == 91.0 && tx_site[x].lon == 361.0) {
//            fprintf(stderr, "\n*** ERROR: Transmitter site #%d not found!", x + 1);
//            y++;
//        }
//    }
//
//    if (y
//
//    
//        ) {
//            fprintf(stderr, "%c\n\n", 7);
//        exit(-1);
//    }
    /* No major errors were detected.  Whew!  :-) */
  static   boolean locateSDFFiles() {
        /* If no SDF path was specified on the command line (-d), check
	   for a path specified in the $HOME/.splat_path file.  If the
	   file is not found, then sdf_path[] remains NULL, and the
	   current working directory is assumed to contain the SDF
	   files. */
        if (splat.getSdfPath() == null || splat.getSdfPath().isEmpty()) {
            env = getenv("HOME");
            splat.setSdfPath(String.format("%s/.splat_path", env));
            fd = new File(splat.getSdfPath());

            if (!fd.exists()) {
                splat.setSdfPath("");
                
                return false;
            } else {
                // splat.setSdfPath(sdf_path);
                System.out.printf("found SDF path: %s", splat.getSdfPath());
                return true;
            }
            
        }
        else
        {
            return true;
        }
        
    }

    static void loadReceiverSite() {
        if (rxSites.size() > 0) {
            rxlat = (int) Math.floor(rxSites.get(0).getLat());
            rxlon = (int) Math.floor(rxSites.get(0).getLon());

            if (rxlat < min_lat) {
                min_lat = rxlat;
            }

            if (rxlat > max_lat) {
                max_lat = rxlat;
            }

            if (Utils.lonDiff(rxlon, min_lon) < 0.0) {
                min_lon = rxlon;
            }

            if (Utils.lonDiff(rxlon, max_lon) >= 0.0) {
                max_lon = rxlon;
            }

        }
    }

    

    static void generateGraphs() {
        ImageGenerator gen = new ImageGenerator(config, splat);
        if (splat.isCoverage() || splat.isPt2pt_mode() || splat.isTopomap()) {
            gen.WriteImage(mapfile, splat.isGeo(), splat.isKml(), splat.isNgs(), txSites.toArray(new Site[txSites.size()]));
        } else {
            if (splat.getLr() == null || splat.getLr().getErp() == 0.0) {
                gen.WriteLRImage(mapfile, splat.isGeo(), splat.isKml(), splat.isNgs(), txSites.toArray(new Site[txSites.size()]), splat.getDem());
            } else {
                if (splat.isDbm()) {
                    gen.WriteDBMImage(mapfile, splat.isGeo(), splat.isKml(), splat.isNgs(), txSites.toArray(new Site[txSites.size()]));
                } else {
                    gen.WriteSSImage(mapfile, splat.isGeo(), splat.isKml(), splat.isNgs(), txSites.toArray(new Site[txSites.size()]), splat.getDem());
                }
            }
        }
        System.exit(0);
    }
}
