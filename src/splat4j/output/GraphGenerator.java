/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splat4j.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import splat4j.Configuration;
import splat4j.Path;
import splat4j.Site;
import splat4j.SplatEngine;
import splat4j.Utils;

/**
 *
 * @author Jude Mukundane
 */
public class GraphGenerator {

    SplatEngine splat;
    Configuration config;

    public GraphGenerator(Configuration config, SplatEngine splat) {
        this.splat = splat;
        this.config = config;
    }

    public void GraphTerrain(Site source, Site destination, String name) {
        FileWriter profWri = null, clutterWri = null;
        try {
            int x, y, z;
            String basename="", term = "", ext = "";
            double minheight = 100000.0, maxheight = -100000.0;
            Path path = Utils.readPath(destination, source, splat, config);
            File profile = new File("profile.gp");
            profWri = new FileWriter(profile);
            File clutterFile;
            if (splat.getClutter() > 0.0) {
                clutterFile = new File("clutter.gp");
                clutterWri = new FileWriter(clutterFile);
            }
            for (x = 0; x < path.getLength(); x++) {
                if ((path.getElevation()[x] + splat.getClutter()) > maxheight) {
                    maxheight = path.getElevation()[x] + splat.getClutter();
                }

                if (path.getElevation()[x] < minheight) {
                    minheight = path.getElevation()[x];
                }

                if (splat.isMetric()) {
                    profWri.write(String.format("%f\t%f\n", config.KM_PER_MILE * path.getDistance()[x], config.METERS_PER_FOOT * path.getElevation()[x]));

                    if (clutterWri != null && x > 0 && x < path.getLength() - 2) {
                        clutterWri.write(String.format("%f\t%f\n", config.KM_PER_MILE * path.getDistance()[x], config.METERS_PER_FOOT * (path.getElevation()[x] == 0.0 ? path.getElevation()[x] : (path.getElevation()[x] + splat.getClutter()))));
                    }
                } else {
                    profWri.write(String.format("%f\t%f\n", path.getDistance()[x], path.getElevation()[x]));

                    if (clutterWri != null && x > 0 && x < path.getLength() - 2) {
                        clutterWri.write(String.format("%f\t%f\n", path.getDistance()[x], (path.getElevation()[x] == 0.0 ? path.getElevation()[x] : (path.getElevation()[x] + splat.getClutter()))));
                    }
                }
            }
            if (name.startsWith(".")) {
                /* Default filename and output file type */

                basename = "profile";
                term = "png";
                ext = "png";
            } else {
                /* Extract extension and terminal type from "name" */
                if (name.contains(".")) {
                    ext = name.substring(name.lastIndexOf("."));
                    term = name.substring(name.lastIndexOf("."));
                    basename = name.substring(0, name.lastIndexOf("."));
                }

                if (ext.isEmpty()) {
                    term = ext = "png";
                }

            }
            /* Either .ps or .postscript may be used
            as an extension for postscript output. */

            if (term == "postscript") {
                ext = "ps";
            } else if (ext == "ps") {
                term = "postscript enhanced color";
            }
            if (maxheight < 1.0) {
                maxheight = 1.0;
                /* Avoid a gnuplot y-range error */
                minheight = -1.0;
                /* over a completely sea-level path */
            } else {
                minheight -= (0.01 * maxheight);
            }

            File splatFile = new File("splat.gp");
            FileWriter splatWri = new FileWriter(splatFile);
            splatWri.write("set grid\n");
            splatWri.write(String.format("set yrange [%2.3f to %2.3f]\n", splat.isMetric() ? minheight * config.METERS_PER_FOOT : minheight, splat.isMetric() ? maxheight * config.METERS_PER_FOOT : maxheight));
            splatWri.write("set encoding iso_8859_1\n");
            splatWri.write(String.format("set term %s\n", term));
            splatWri.write(String.format("set title \"%s Terrain Profile Between %s and %s (%.2f%c Azimuth)\"\n", splat.getName(), destination.getName(), source.getName(), Utils.azimuth(destination, source, config), 176));
            if (splat.isMetric()) {
                splatWri.write(String.format("set xlabel \"Distance Between %s and %s (%.2f kilometers)\"\n", destination.getName(), source.getName(), config.KM_PER_MILE * Utils.distance(source, destination, config)));
                splatWri.write("set ylabel \"Ground Elevation Above Sea Level (meters)\"\n");
            } else {
                splatWri.write(String.format("set xlabel \"Distance Between %s and %s (%.2f miles)\"\n", destination.getName(), source.getName(), Utils.distance(source, destination, config)));
                splatWri.write("set ylabel \"Ground Elevation Above Sea Level (feet)\"\n");
            }
            splatWri.write(String.format("set output \"%s.%s\"\n", basename, ext));
            if (splat.getClutter() > 0.0) {
                if (splat.isMetric()) {
                    splatWri.write(String.format("plot \"profile.gp\" title \"Terrain Profile\" with lines, \"clutter.gp\" title \"Clutter Profile (%.2f meters)\" with lines\n", splat.getClutter() * config.METERS_PER_FOOT));
                } else {
                    splatWri.write(String.format("plot \"profile.gp\" title \"Terrain Profile\" with lines, \"clutter.gp\" title \"Clutter Profile (%.2f feet)\" with lines\n", splat.getClutter()));
                }
            } else {
                splatWri.write("plot \"profile.gp\" title \"\" with lines\n");
            }
            x = Runtime.getRuntime().exec("gnuplot splat.gp").exitValue();
            if (x != -1) {
                if (!splat.isGpsav()) {
                    new File("splat.gp").deleteOnExit();
                    new File("profile.gp").deleteOnExit();
                }

                System.out.printf("Terrain plot written to: \"%s.%s\"\n", basename, ext);
            } else {
                System.err.print("\n*** ERROR: Error occurred invoking gnuplot!\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(GraphGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                profWri.close();
            } catch (IOException ex) {
                Logger.getLogger(GraphGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void GraphElevation(Site source, Site destination, String name) {
        FileWriter profWri = null;
        try {
            int x, y, z;
            String basename=null, term=null, ext=null;
            double angle, clutter_angle = 0.0, refangle, maxangle = -90.0, minangle = 90.0, distance;
            Site remote, remote2;
            File proFile = null, clutterFile = null, referenceFile = null;
            FileWriter clutWri=null;
            Path path = Utils.readPath(destination, source, splat, config);
            /* destination=RX, source=TX */
            refangle = Utils.elevationAngle(destination, source, splat, config);
            distance = Utils.distance(source, destination, config);
            proFile = new File("profile.gp");
            profWri = new FileWriter(proFile);
            if (splat.getClutter() > 0.0) {
                clutterFile = new File("clutter.gp");
                clutWri = new FileWriter(clutterFile);
            }
            referenceFile = new File("reference.gp", "wb");
            FileWriter refWri = new FileWriter(referenceFile);
            for (x = 1; x < path.getLength() - 1; x++) {
                remote = new Site("", "", path.getLat(x), path.getLon(x), 0.0f);
//		remote.lat=path.lat[x];
//		remote.lon=path.lon[x];
//		remote.getAlt()=0.0;
                angle = Utils.elevationAngle(destination, remote, splat, config);

                if (splat.getClutter() > 0.0) {
                    remote2 = new Site("", "", path.getLat(x), path.getLon(x), (float)(path.getElevation()[x] != 0.0 ? splat.getClutter() : 0.0));
//			remote2.lat=path.lat[x];
//			remote2.lon=path.lon[x];
//
//			if (path.getElevation()[x]!=0.0)
//				remote2.getAlt()=clutter;
//			else
//				remote2.getAlt()=0.0;

                    clutter_angle = Utils.elevationAngle(destination, remote2, splat, config);
                }

                if (splat.isMetric()) {
                    profWri.write(String.format("%f\t%f\n", config.KM_PER_MILE * path.getDistance()[x], angle));

                    if (clutterFile != null) {
                        clutWri.write(String.format("%f\t%f\n", config.KM_PER_MILE * path.getDistance()[x], clutter_angle));
                    }

                    refWri.write(String.format("%f\t%f\n", config.KM_PER_MILE * path.getDistance()[x], refangle));
                } else {
                    profWri.write(String.format("%f\t%f\n", path.getDistance()[x], angle));

                    if (clutterFile != null) {
                        clutWri.write(String.format("%f\t%f\n", path.getDistance()[x], clutter_angle));
                    }

                    refWri.write(String.format("%f\t%f\n", path.getDistance()[x], refangle));
                }

                if (angle > maxangle) {
                    maxangle = angle;
                }

                if (clutter_angle > maxangle) {
                    maxangle = clutter_angle;
                }

                if (angle < minangle) {
                    minangle = angle;
                }
            }
            if (splat.isMetric()) {
                profWri.write(String.format("%f\t%f\n", config.KM_PER_MILE * path.getDistance()[path.getLength() - 1], refangle));
                refWri.write(String.format("%f\t%f\n", config.KM_PER_MILE * path.getDistance()[path.getLength() - 1], refangle));
            } else {
                profWri.write(String.format("%f\t%f\n", path.getDistance()[path.getLength() - 1], refangle));
                refWri.write(String.format("%f\t%f\n", path.getDistance()[path.getLength() - 1], refangle));
            }
            if (name.startsWith(".")) {
                /* Default filename and output file type */

                basename = "profile";
                term = "png";
                ext = "png";
            } else {
                /* Extract extension and terminal type from "name" */
                if (name.contains(".")) {
                    ext = name.substring(name.lastIndexOf("."));
                    term = name.substring(name.lastIndexOf("."));
                    basename = name.substring(0, name.lastIndexOf("."));
                }

                if (ext.isEmpty()) {
                    term = ext = "png";
                }

            }
            /* Either .ps or .postscript may be used
            as an extension for postscript output. */
            if (term == "postscript") {
                ext = "ps";
            } else if (ext == "ps") {
                term = "postscript enhanced color";
            }
            File splatFile = new File("splat.gp");
            FileWriter splatWri = new FileWriter(splatFile);
            splatWri.write("set grid\n");
            if (distance > 2.0) {
                splatWri.write(String.format("set yrange [%2.3f to %2.3f]\n", (-Math.abs(refangle) - 0.25), maxangle + 0.25));
            } else {
                splatWri.write(String.format("set yrange [%2.3f to %2.3f]\n", minangle, refangle + (-minangle / 8.0)));
            }
            splatWri.write("set encoding iso_8859_1\n");
            splatWri.write(String.format("set term %s\n", term));
            splatWri.write(String.format("set title \"%s Elevation Profile Between %s and %s (%.2f%c azimuth)\"\n", splat.getName(), destination.getName(), source.getName(), Utils.azimuth(destination, source, config), 176));
            if (splat.isMetric()) {
                splatWri.write(String.format("set xlabel \"Distance Between %s and %s (%.2f kilometers)\"\n", destination.getName(), source.getName(), config.KM_PER_MILE * distance));
            } else {
                splatWri.write(String.format("set xlabel \"Distance Between %s and %s (%.2f miles)\"\n", destination.getName(), source.getName(), distance));
            }
            splatWri.write(String.format("set ylabel \"Elevation Angle Along LOS Path Between\\n%s and %s (degrees)\"\n", destination.getName(), source.getName()));
            splatWri.write(String.format("set output \"%s.%s\"\n", basename, ext));
            if (splat.getClutter() > 0.0) {
                if (splat.isMetric()) {
                    splatWri.write(String.format("plot \"profile.gp\" title \"Real Earth Profile\" with lines, \"clutter.gp\" title \"Clutter Profile (%.2f meters)\" with lines, \"reference.gp\" title \"Line of Sight Path (%.2f%c getElevation())\" with lines\n", splat.getClutter() * config.METERS_PER_FOOT, refangle, 176));
                } else {
                    splatWri.write(String.format("plot \"profile.gp\" title \"Real Earth Profile\" with lines, \"clutter.gp\" title \"Clutter Profile (%.2f feet)\" with lines, \"reference.gp\" title \"Line of Sight Path (%.2f%c getElevation())\" with lines\n", splat.getClutter(), refangle, 176));
                }
            } else {
                splatWri.write(String.format("plot \"profile.gp\" title \"Real Earth Profile\" with lines, \"reference.gp\" title \"Line of Sight Path (%.2f%c getElevation())\" with lines\n", refangle, 176));
            }
            x = Runtime.getRuntime().exec("gnuplot splat.gp").exitValue();
            if (x != -1) {
                if (!splat.isGpsav()) {
                    splatFile.deleteOnExit();
                    proFile.deleteOnExit();
                    referenceFile.deleteOnExit();

                    if (splat.getClutter() > 0.0) {
                        clutterFile.deleteOnExit();
                    }
                }

                System.out.printf("Elevation plot written to: \"%s.%s\"\n", basename, ext);
            } else {
                System.err.print("\n*** ERROR: Error occurred invoking gnuplot!\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(GraphGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                profWri.close();
            } catch (IOException ex) {
                Logger.getLogger(GraphGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void GraphHeight(Site source, Site destination, String name, boolean fresnel_plot, boolean normalized) {
        FileWriter profWri = null;
        try {
            int x, y, z;
            String basename="", term="", ext="";
            double a, b, c, height = 0.0, refangle, cangle, maxheight = -100000.0,
                    minheight = 100000.0, lambda = 0.0, f_zone = 0.0, fpt6_zone = 0.0,
                    nm = 0.0, nb = 0.0, ed = 0.0, es = 0.0, r = 0.0, d = 0.0, d1 = 0.0,
                    terrain, azimuth, distance, dheight = 0.0
                    , minterrain = 100000.0, minearth = 100000.0, miny, maxy, min2y, max2y;
            Site remote;
            File proFile = null
                    , clutterFile = null
                    , referenceFile = null
                    , fresnelFile = null
                    , fresnel6File = null
                    , curvatureFile = null;
            Path path = Utils.readPath(destination, source, splat, config);
            /* destination=RX, source=TX */
            azimuth = Utils.azimuth(destination, source,config);
            distance = Utils.distance(destination, source, config);
            refangle = Utils.elevationAngle(destination, source, splat, config);
            b = Utils.getElevation(destination, splat, config) + destination.getAlt() + config.EARTHRADIUS;
            FileWriter clutWri = null;
            FileWriter fresWri= null;
            FileWriter fres6Wri= null;
            /* WavegetLength() and path getDistance() (great circle) in feet. */
            if (fresnel_plot) {
                lambda = 9.8425e8 / (splat.getLr().getFrq_mhz() * 1e6);
                d = 5280.0 * path.getDistance()[path.getLength() - 1];
            }   if (normalized) {
                ed = Utils.getElevation(destination, splat, config);
                es = Utils.getElevation(source, splat, config);
                nb = -destination.getAlt() - ed;
                nm = (-source.getAlt() - es - nb) / (path.getDistance()[path.getLength() - 1]);
            }   proFile = new File("profile.gp");
            profWri = new FileWriter(proFile);
            if (splat.getClutter() > 0.0) {
                clutterFile = new File("clutter.gp");
                clutWri = new FileWriter(clutterFile);
            }   referenceFile = new File("reference.gp");
            FileWriter refWri = new FileWriter(referenceFile);
            curvatureFile = new File("curvature.gp");
            FileWriter curvWri = new FileWriter(curvatureFile);
            if ((splat.getLr().getFrq_mhz() >= 20.0) && (splat.getLr().getFrq_mhz() <= 20000.0) && fresnel_plot) {
                fresnelFile = new File("fresnel.gp");
                fresWri= new FileWriter(fresnelFile);
                
                fresnel6File  = new File("fresnel_pt_6.gp");
                fres6Wri = new FileWriter(fresnel6File);
            }   for (x = 0; x < path.getLength() - 1; x++) {
                remote = new Site("", "", path.getLat(x), path.getLon(x), 0.0f);
//            remote.lat = path.lat[x];
//            remote.lon = path.lon[x];
//            remote.getAlt() = 0.0;

terrain = Utils.getElevation(remote, splat, config);

if (x == 0) {
    terrain += destination.getAlt();  /* RX antenna spike */
}

a = terrain + config.EARTHRADIUS;
cangle = 5280.0 * Utils.distance(destination, remote, config) / config.EARTHRADIUS;
c = b * Math.sin(refangle * config.DEG2RAD + config.HALFPI) / Math.sin(config.HALFPI - refangle * config.DEG2RAD - cangle);

height = a - c;

/* Per Fink and Christiansen, Electronics
* Engineers' Handbook, 1989:
*
*   H = sqrt(lamba * d1 * (d - d1)/d)
*
* where H is the getDistance() from the LOS
* path to the first Fresnel zone boundary.
*/
if ((splat.getLr().getFrq_mhz() >= 20.0) && (splat.getLr().getFrq_mhz() <= 20000.0) && fresnel_plot) {
    d1 = 5280.0 * path.getDistance()[x];
    f_zone = -1.0 * Math.sqrt(lambda * d1 * (d - d1) / d);
    fpt6_zone = f_zone * splat.getFzoneClearance();
}

if (normalized) {
    r = -(nm * path.getDistance()[x]) - nb;
    height += r;
    
    if ((splat.getLr().getFrq_mhz() >= 20.0) && (splat.getLr().getFrq_mhz() <= 20000.0) && fresnel_plot) {
        f_zone += r;
        fpt6_zone += r;
    }
} else {
    r = 0.0;
}

if (splat.isMetric()) {
    profWri.write(String.format("%f\t%f\n", config.KM_PER_MILE * path.getDistance()[x], config.METERS_PER_FOOT * height));
    
    if (clutterFile != null && x > 0 && x < path.getLength() - 2) {
        clutWri.write(String.format("%f\t%f\n", config.KM_PER_MILE * path.getDistance()[x], config.METERS_PER_FOOT * (terrain == 0.0 ? height : (height + splat.getClutter()))));
    }
    
    refWri.write(String.format("%f\t%f\n", config.KM_PER_MILE * path.getDistance()[x], config.METERS_PER_FOOT * r));
    curvWri.write(String.format("%f\t%f\n", config.KM_PER_MILE * path.getDistance()[x], config.METERS_PER_FOOT * (height - terrain)));
} else {
    profWri.write(String.format("%f\t%f\n", path.getDistance()[x], height));
    
    if (clutterFile != null && x > 0 && x < path.getLength() - 2) {
        clutWri.write(String.format("%f\t%f\n", path.getDistance()[x], (terrain == 0.0 ? height : (height + splat.getClutter()))));
    }
    
    refWri.write(String.format("%f\t%f\n", path.getDistance()[x], r));
    curvWri.write(String.format("%f\t%f\n", path.getDistance()[x], height - terrain));
}

if ((splat.getLr().getFrq_mhz() >= 20.0) && (splat.getLr().getFrq_mhz() <= 20000.0) && fresnel_plot) {
    if (splat.isMetric()) {
        fresWri.write(String.format("%f\t%f\n", config.KM_PER_MILE * path.getDistance()[x], config.METERS_PER_FOOT * f_zone));
        fres6Wri.write(String.format("%f\t%f\n", config.KM_PER_MILE * path.getDistance()[x], config.METERS_PER_FOOT * fpt6_zone));
    } else {
        fresWri.write(String.format("%f\t%f\n", path.getDistance()[x], f_zone));
        fres6Wri.write(String.format("%f\t%f\n", path.getDistance()[x], fpt6_zone));
    }
    
    if (f_zone < minheight) {
        minheight = f_zone;
    }
}

if ((height + splat.getClutter()) > maxheight) {
    maxheight = height + splat.getClutter();
}

if (height < minheight) {
    minheight = height;
}

if (r > maxheight) {
    maxheight = r;
}

if (terrain < minterrain) {
    minterrain = terrain;
}

if ((height - terrain) < minearth) {
    minearth = height - terrain;
}
            }   if (normalized) {
                r = -(nm * path.getDistance()[path.getLength() - 1]) - nb;
            } else {
                r = 0.0;
            }   if (splat.isMetric()) {
                profWri.write(String.format("%f\t%f\n", config.KM_PER_MILE * path.getDistance()[path.getLength() - 1], config.METERS_PER_FOOT * r));
                refWri.write(String.format("%f\t%f\n", config.KM_PER_MILE * path.getDistance()[path.getLength() - 1], config.METERS_PER_FOOT * r));
            } else {
                profWri.write(String.format("%f\t%f\n", path.getDistance()[path.getLength() - 1], r));
                refWri.write(String.format("%f\t%f\n", path.getDistance()[path.getLength() - 1], r));
            }   if ((splat.getLr().getFrq_mhz() >= 20.0) && (splat.getLr().getFrq_mhz() <= 20000.0) && fresnel_plot) {
                if (splat.isMetric()) {
                    fresWri.write(String.format("%f\t%f\n", config.KM_PER_MILE * path.getDistance()[path.getLength() - 1], config.METERS_PER_FOOT * r));
                    fres6Wri.write(String.format("%f\t%f\n", config.KM_PER_MILE * path.getDistance()[path.getLength() - 1], config.METERS_PER_FOOT * r));
                } else {
                    fresWri.write(String.format("%f\t%f\n", path.getDistance()[path.getLength() - 1], r));
                    fres6Wri.write(String.format("%f\t%f\n", path.getDistance()[path.getLength() - 1], r));
                }
            }   if (r > maxheight) {
                maxheight = r;
            }   if (r < minheight) {
                minheight = r;
            }   if (name.startsWith(".")) {
                /* Default filename and output file type */

                basename = "profile";
                term = "png";
                ext = "png";
            } else {
                /* Extract extension and terminal type from "name" */
                if (name.contains(".")) {
                    ext = name.substring(name.lastIndexOf("."));
                    term = name.substring(name.lastIndexOf("."));
                    basename = name.substring(0, name.lastIndexOf("."));
                }

                if (ext.isEmpty()) {
                    term = ext = "png";
                }

            }
            /* Either .ps or .postscript may be used
            as an extension for postscript output. */
            if (term == "postscript") {
                ext = "ps";
            } else if (ext == "ps") {
                term = "postscript enhanced color";
            }
            File splatFile = new File("splat.gp");
            FileWriter splatWri = new FileWriter(splatFile);
            dheight = maxheight - minheight;
            miny = minheight - 0.15 * dheight;
            maxy = maxheight + 0.05 * dheight;
            if (maxy < 20.0) {
                maxy = 20.0;
            }   dheight = maxheight - minheight;
            min2y = miny - minterrain + 0.05 * dheight;
            if (minearth < min2y) {
                miny -= min2y - minearth + 0.05 * dheight;
                min2y = minearth - 0.05 * dheight;
            }   max2y = min2y + maxy - miny;
            splatWri.write("set grid\n");
            splatWri.write(String.format("set yrange [%2.3f to %2.3f]\n", splat.isMetric() ? miny * config.METERS_PER_FOOT : miny, splat.isMetric() ? maxy * config.METERS_PER_FOOT : maxy));
            splatWri.write(String.format("set y2range [%2.3f to %2.3f]\n", splat.isMetric() ? min2y * config.METERS_PER_FOOT : min2y, splat.isMetric() ? max2y * config.METERS_PER_FOOT : max2y));
            splatWri.write(String.format("set xrange [-0.5 to %2.3f]\n", splat.isMetric() ? config.KM_PER_MILE * Math.rint(distance + 0.5) : Math.rint(distance + 0.5)));
            splatWri.write(String.format("set encoding iso_8859_1\n"));
            splatWri.write(String.format("set term %s\n", term));
            if ((splat.getLr().getFrq_mhz() >= 20.0) && (splat.getLr().getFrq_mhz() <= 20000.0) && fresnel_plot) {
                splatWri.write(String.format("set title \"%s Path Profile Between %s and %s (%.2f%c azimuth)\\nWith First Fresnel Zone\"\n", splat.getName(), destination.getName(), source.getName(), azimuth, 176));
            } else {
                splatWri.write(String.format("set title \"%s Height Profile Between %s and %s (%.2f%c azimuth)\"\n", splat.getName(), destination.getName(), source.getName(), azimuth, 176));
            }   if (splat.isMetric()) {
                splatWri.write(String.format("set xlabel \"Distance Between %s and %s (%.2f kilometers)\"\n", destination.getName(), source.getName(), config.KM_PER_MILE * Utils.distance(source, destination, config)));
            } else {
                splatWri.write(String.format("set xlabel \"Distance Between %s and %s (%.2f miles)\"\n", destination.getName(), source.getName(), Utils.distance(source, destination, config)));
            }   if (normalized) {
                if (splat.isMetric()) {
                    splatWri.write(String.format("set ylabel \"Normalized Height Referenced To LOS Path Between\\n%s and %s (meters)\"\n", destination.getName(), source.getName()));
                } else {
                    splatWri.write(String.format("set ylabel \"Normalized Height Referenced To LOS Path Between\\n%s and %s (feet)\"\n", destination.getName(), source.getName()));
                }
                
            } else {
                if (splat.isMetric()) {
                    splatWri.write(String.format("set ylabel \"Height Referenced To LOS Path Between\\n%s and %s (meters)\"\n", destination.getName(), source.getName()));
                } else {
                    splatWri.write(String.format("set ylabel \"Height Referenced To LOS Path Between\\n%s and %s (feet)\"\n", destination.getName(), source.getName()));
                }
            }   splatWri.write(String.format("set output \"%s.%s\"\n", basename, ext));
            if ((splat.getLr().getFrq_mhz() >= 20.0) && (splat.getLr().getFrq_mhz() <= 20000.0) && fresnel_plot) {
                if (splat.getClutter()> 0.0) {
                    if (splat.isMetric()) {
                        splatWri.write(String.format("plot \"profile.gp\" title \"Point-to-Point Profile\" with lines, \"clutter.gp\" title \"Ground Clutter (%.2f meters)\" with lines, \"reference.gp\" title \"Line of Sight Path\" with lines, \"curvature.gp\" axes x1y2 title \"Earth's Curvature Contour\" with lines, \"fresnel.gp\" axes x1y1 title \"First Fresnel Zone (%.3f MHz)\" with lines, \"fresnel_pt_6.gp\" title \"%.0f%% of First Fresnel Zone\" with lines\n", splat.getClutter()* config.METERS_PER_FOOT, splat.getLr().getFrq_mhz(), splat.getFzoneClearance() * 100.0));
                    } else {
                        splatWri.write(String.format("plot \"profile.gp\" title \"Point-to-Point Profile\" with lines, \"clutter.gp\" title \"Ground Clutter (%.2f feet)\" with lines, \"reference.gp\" title \"Line of Sight Path\" with lines, \"curvature.gp\" axes x1y2 title \"Earth's Curvature Contour\" with lines, \"fresnel.gp\" axes x1y1 title \"First Fresnel Zone (%.3f MHz)\" with lines, \"fresnel_pt_6.gp\" title \"%.0f%% of First Fresnel Zone\" with lines\n", splat.getClutter(), splat.getLr().getFrq_mhz(), splat.getFzoneClearance() * 100.0));
                    }
                } else {
                    splatWri.write(String.format("plot \"profile.gp\" title \"Point-to-Point Profile\" with lines, \"reference.gp\" title \"Line of Sight Path\" with lines, \"curvature.gp\" axes x1y2 title \"Earth's Curvature Contour\" with lines, \"fresnel.gp\" axes x1y1 title \"First Fresnel Zone (%.3f MHz)\" with lines, \"fresnel_pt_6.gp\" title \"%.0f%% of First Fresnel Zone\" with lines\n", splat.getLr().getFrq_mhz(), splat.getFzoneClearance() * 100.0));
                }
            } else {
                if (splat.getClutter()> 0.0) {
                    if (splat.isMetric()) {
                        splatWri.write(String.format("plot \"profile.gp\" title \"Point-to-Point Profile\" with lines, \"clutter.gp\" title \"Ground Clutter (%.2f meters)\" with lines, \"reference.gp\" title \"Line Of Sight Path\" with lines, \"curvature.gp\" axes x1y2 title \"Earth's Curvature Contour\" with lines\n", splat.getClutter()* config.METERS_PER_FOOT));
                    } else {
                        splatWri.write(String.format("plot \"profile.gp\" title \"Point-to-Point Profile\" with lines, \"clutter.gp\" title \"Ground Clutter (%.2f feet)\" with lines, \"reference.gp\" title \"Line Of Sight Path\" with lines, \"curvature.gp\" axes x1y2 title \"Earth's Curvature Contour\" with lines\n", splat.getClutter()));
                    }
                } else {
                    splatWri.write("plot \"profile.gp\" title \"Point-to-Point Profile\" with lines, \"reference.gp\" title \"Line Of Sight Path\" with lines, \"curvature.gp\" axes x1y2 title \"Earth's Curvature Contour\" with lines\n");
                }
                
            }   x = Runtime.getRuntime().exec("gnuplot splat.gp").exitValue();
            if (x != -1) {
                if (!splat.isGpsav()) {
                    splatFile.deleteOnExit();
                    proFile.deleteOnExit();
                    referenceFile.deleteOnExit();

                    if (splat.getClutter() > 0.0) {
                        clutterFile.deleteOnExit();
                    }
                }

                if ((splat.getLr().getFrq_mhz() >= 20.0) && (splat.getLr().getFrq_mhz() <= 20000.0) && fresnel_plot) {
                    fresnelFile.deleteOnExit();    
                    fresnel6File.deleteOnExit();
                }
                
                System.out.printf("Elevation plot written to: \"%s.%s\"\n", basename, ext);
            } else {
                System.err.print("\n*** ERROR: Error occurred invoking gnuplot!\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(GraphGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                profWri.close();
            } catch (IOException ex) {
                Logger.getLogger(GraphGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
