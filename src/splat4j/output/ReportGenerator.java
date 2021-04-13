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
import splat4j.LR;
import splat4j.Path;
import splat4j.Site;
import splat4j.Utils;
import splat4j.Dem;
import splat4j.ITWOM.ITWOM3;
import splat4j.ITWOM.ITWOM3.ITWOMResult;
import splat4j.SplatEngine;

/**
 *
 * @author Jude Mukundane
 */
public class ReportGenerator {

    SplatEngine splat;
    Configuration config;
    
    public ReportGenerator(Configuration config, SplatEngine splat)
    {
        this.splat = splat;
        this.config = config;
    }

    public void obstructionAnalysis(Site xmtr, Site rcvr, double f, FileWriter writ) {
        /* Perform an obstruction analysis along the
	   path between receiver and transmitter. */

        int x;
        Site site_x;
        double h_r, h_t, h_x, h_r_orig, cos_tx_angle, cos_test_angle,
                cos_tx_angle_f1, cos_tx_angle_fpt6, d_tx, d_x,
                h_r_f1, h_r_fpt6, h_f, h_los, lambda = 0.0;
        String string = null, string_fpt6 = null, string_f1 = null;

        Path path = Utils.readPath(xmtr, rcvr, splat, config);
        h_r = Utils.getElevation(rcvr, splat, config) + rcvr.getAlt() + config.EARTHRADIUS;
        h_r_f1 = h_r;
        h_r_fpt6 = h_r;
        h_r_orig = h_r;
        h_t = Utils.getElevation(xmtr, splat, config) + xmtr.getAlt() + config.EARTHRADIUS;
        d_tx = 5280.0 * Utils.distance(rcvr, xmtr, config);
        cos_tx_angle = ((h_r * h_r) + (d_tx * d_tx) - (h_t * h_t)) / (2.0 * h_r * d_tx);
        cos_tx_angle_f1 = cos_tx_angle;
        cos_tx_angle_fpt6 = cos_tx_angle;

        if (f > 0) {
            lambda = 9.8425e8 / (f * 1e6);
        }

        if (splat.getClutter() > 0.0) {
            try {
                writ.write("Terrain has been raised by");

                if (splat.isMetric()) {
                    writ.write(String.format(" %.2f meters", config.METERS_PER_FOOT * splat.getClutter()));
                } else {
                    writ.write(String.format(" %.2f feet", splat.getClutter()));
                }

                writ.write(" to account for ground clutter.\n\n");

                /* At each point along the path calculate the cosine
	   of a sort of "inverse elevation angle" at the receiver.
	   From the antenna, 0 deg. looks at the ground, and 90 deg.
	   is parallel to the ground.

	   Start at the receiver.  If this is the lowest antenna,
	   then terrain obstructions will be nearest to it.  (Plus,
	   that's the way SPLAT!'s original los() did it.)

	   Calculate cosines only.  That's sufficient to compare
	   angles and it saves the extra computational burden of
	   acos().  However, note the inverted comparison: if
	   acos(A) > acos(B), then B > A. */
                for (x = path.getLength() - 1; x > 0; x--) {
                    site_x = new Site(null, null, path.getLat(x), path.getLon(x), 0.0f);


                    h_x = Utils.getElevation(site_x,  splat, config) + config.EARTHRADIUS + splat.getClutter();
                    d_x = 5280.0 * Utils.distance(rcvr, site_x, config);

                    /* Deal with the LOS path first. */
                    cos_test_angle = ((h_r * h_r) + (d_x * d_x) - (h_x * h_x)) / (2.0 * h_r * d_x);

                    if (cos_tx_angle > cos_test_angle) {
                        if (h_r == h_r_orig) {
                            writ.write(String.format("Between %s and %s, %s detected obstructions at:\n\n", rcvr.getName(), xmtr.getName(), splat.getName()));
                        }

                        if (site_x.getLat() >= 0.0) {
                            if (splat.isMetric()) {
                                writ.write(String.format("   %8.4f N,%9.4f W, %5.2f kilometers, %6.2f meters AMSL\n", site_x.getLat(), site_x.getLon(), config.KM_PER_MILE * (d_x / 5280.0), config.METERS_PER_FOOT * (h_x - config.EARTHRADIUS)));
                            } else {
                                writ.write(String.format("   %8.4f N,%9.4f W, %5.2f miles, %6.2f feet AMSL\n", site_x.getLat(), site_x.getLon(), d_x / 5280.0, h_x - config.EARTHRADIUS));
                            }
                        } else {
                            if (splat.isMetric()) {
                                writ.write(String.format("   %8.4f S,%9.4f W, %5.2f kilometers, %6.2f meters AMSL\n", -site_x.getLat(), site_x.getLon(), config.KM_PER_MILE * (d_x / 5280.0), config.METERS_PER_FOOT * (h_x - config.EARTHRADIUS)));
                            } else {
                                writ.write(String.format("   %8.4f S,%9.4f W, %5.2f miles, %6.2f feet AMSL\n", -site_x.getLat(), site_x.getLon(), d_x / 5280.0, h_x - config.EARTHRADIUS));
                            }
                        }
                    }

                    while (cos_tx_angle > cos_test_angle) {
                        h_r += 1;
                        cos_test_angle = ((h_r * h_r) + (d_x * d_x) - (h_x * h_x)) / (2.0 * h_r * d_x);
                        cos_tx_angle = ((h_r * h_r) + (d_tx * d_tx) - (h_t * h_t)) / (2.0 * h_r * d_tx);
                    }

                    if (f > 0) {
                        /* Now clear the first Fresnel zone... */

                        cos_tx_angle_f1 = ((h_r_f1 * h_r_f1) + (d_tx * d_tx) - (h_t * h_t)) / (2.0 * h_r_f1 * d_tx);
                        h_los = Math.sqrt(h_r_f1 * h_r_f1 + d_x * d_x - 2 * h_r_f1 * d_x * cos_tx_angle_f1);
                        h_f = h_los - Math.sqrt(lambda * d_x * (d_tx - d_x) / d_tx);

                        while (h_f < h_x) {
                            h_r_f1 += 1;
                            cos_tx_angle_f1 = ((h_r_f1 * h_r_f1) + (d_tx * d_tx) - (h_t * h_t)) / (2.0 * h_r_f1 * d_tx);
                            h_los = Math.sqrt(h_r_f1 * h_r_f1 + d_x * d_x - 2 * h_r_f1 * d_x * cos_tx_angle_f1);
                            h_f = h_los - Math.sqrt(lambda * d_x * (d_tx - d_x) / d_tx);
                        }

                        /* and clear the 60% F1 zone. */
                        cos_tx_angle_fpt6 = ((h_r_fpt6 * h_r_fpt6) + (d_tx * d_tx) - (h_t * h_t)) / (2.0 * h_r_fpt6 * d_tx);
                        h_los = Math.sqrt(h_r_fpt6 * h_r_fpt6 + d_x * d_x - 2 * h_r_fpt6 * d_x * cos_tx_angle_fpt6);
                        h_f = h_los - splat.getFzoneClearance() * Math.sqrt(lambda * d_x * (d_tx - d_x) / d_tx);

                        while (h_f < h_x) {
                            h_r_fpt6 += 1;
                            cos_tx_angle_fpt6 = ((h_r_fpt6 * h_r_fpt6) + (d_tx * d_tx) - (h_t * h_t)) / (2.0 * h_r_fpt6 * d_tx);
                            h_los = Math.sqrt(h_r_fpt6 * h_r_fpt6 + d_x * d_x - 2 * h_r_fpt6 * d_x * cos_tx_angle_fpt6);
                            h_f = h_los - splat.getFzoneClearance() * Math.sqrt(lambda * d_x * (d_tx - d_x) / d_tx);
                        }
                    }
                }

                if (h_r > h_r_orig) {
                    if (splat.isMetric()) {
                        string = String.format("\nAntenna at %s must be raised to at least %.2f meters AGL\nto clear all obstructions detected by %s.\n", rcvr.getName(), config.METERS_PER_FOOT * (h_r - Utils.getElevation(rcvr, splat, config) - config.EARTHRADIUS), splat.getName());
                    } else {
                        string = String.format("\nAntenna at %s must be raised to at least %.2f feet AGL\nto clear all obstructions detected by %s.\n", rcvr.getName(), h_r - Utils.getElevation(rcvr, splat, config) - config.EARTHRADIUS, splat.getName());
                    }
                } else {
                    string = String.format("\nNo obstructions to LOS path due to terrain were detected by %s\n", splat.getName());
                }

                if (f > 0) {
                    if (h_r_fpt6 > h_r_orig) {
                        if (splat.isMetric()) {
                            string_fpt6 = String.format("\nAntenna at %s must be raised to at least %.2f meters AGL\nto clear %.0f%c of the first Fresnel zone.\n", rcvr.getName(), config.METERS_PER_FOOT * (h_r_fpt6 - Utils.getElevation(rcvr, splat, config) - config.EARTHRADIUS), splat.getFzoneClearance() * 100.0, 37);
                        } else {
                            string_fpt6 = String.format("\nAntenna at %s must be raised to at least %.2f feet AGL\nto clear %.0f%c of the first Fresnel zone.\n", rcvr.getName(), h_r_fpt6 - Utils.getElevation(rcvr, splat, config) - config.EARTHRADIUS, splat.getFzoneClearance() * 100.0, 37);
                        }
                    } else {
                        string_fpt6 = String.format("\n%.0f%c of the first Fresnel zone is clear.\n", splat.getFzoneClearance() * 100.0, 37);
                    }

                    if (h_r_f1 > h_r_orig) {
                        if (splat.isMetric()) {
                            string_f1 = String.format("\nAntenna at %s must be raised to at least %.2f meters AGL\nto clear the first Fresnel zone.\n", rcvr.getName(), config.METERS_PER_FOOT * (h_r_f1 - Utils.getElevation(rcvr, splat, config) - config.EARTHRADIUS));
                        } else {
                            string_f1 = String.format("\nAntenna at %s must be raised to at least %.2f feet AGL\nto clear the first Fresnel zone.\n", rcvr.getName(), h_r_f1 - Utils.getElevation(rcvr, splat, config) - config.EARTHRADIUS);
                        }

                    } else {
                        string_f1 = String.format("\nThe first Fresnel zone is clear.\n");
                    }
                }

                writ.write(String.format("%s", string));

                if (f > 0) {
                    writ.write(String.format("%s", string_f1));
                    writ.write(String.format("%s", string_fpt6));

                }
            } catch (IOException ex) {
                Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    } 

    public void PathReport(Site source, Site destination, String name, boolean graph_it) { //, double loss, int errnum, String strmode) {

        int x, y, z;
        String basename, term, ext, report_name;
        double maxloss = -100000.0, minloss = 100000.0, haavt,
                angle1, angle2, azimuth, pattern = 1.0, patterndB = 0.0,
                total_loss = 0.0, cos_xmtr_angle, cos_test_angle = 0.0,
                source_alt, test_alt, dest_alt, source_alt2, dest_alt2,
                distance, elevation, four_thirds_earth, field_strength,
                free_space_loss = 0.0, eirp = 0.0, voltage, rxp, dBm,
                power_density;
        double[] elev;
        boolean block = false;

        report_name = String.format("%s-to-%s.txt", source.getName(), destination.getName());
        four_thirds_earth = config.FOUR_THIRDS * config.EARTHRADIUS;

        for (char ch : new char[]{32, 17, 92, 42, 47}) {
            report_name = report_name.replace(ch, '_');
        }

        FileWriter repWri = null;
        try {
            File reportFile = new File(report_name);
            repWri = new FileWriter(reportFile);
            repWri.write(String.format("\n\t\t--==[ %s v%s Path Analysis ]==--\n\n", splat.getName(), splat.getVersion()));
            repWri.write(String.format("%s\n\n", splat.getDashes()));
            repWri.write(String.format("Transmitter site: %s\n", source.getName()));
            if (source.getLat() >= 0.0) {
                repWri.write(String.format("Site location: %.4f North / %.4f West", source.getLat(), source.getLon()));
                repWri.write(String.format(" (%s N / ", Utils.dec2dms(source.getLat())));
            } else {

                repWri.write(String.format("Site location: %.4f South / %.4f West", -source.getLat(), source.getLon()));
                repWri.write(String.format(" (%s S / ", Utils.dec2dms(source.getLat())));
            }
            repWri.write(String.format("%s W)\n", Utils.dec2dms(source.getLon())));
            if (splat.isMetric()) {
                repWri.write(String.format("Ground elevation: %.2f meters AMSL\n", config.METERS_PER_FOOT * Utils.getElevation(source, splat, config)));
                repWri.write(String.format("Antenna height: %.2f meters AGL / %.2f meters AMSL\n", config.METERS_PER_FOOT * source.getAlt(), config.METERS_PER_FOOT * (source.getAlt() + Utils.getElevation(source, splat, config))));
            } else {
                repWri.write(String.format("Ground elevation: %.2f feet AMSL\n", Utils.getElevation(source, splat, config)));
                repWri.write(String.format("Antenna height: %.2f feet AGL / %.2f feet AMSL\n", source.getAlt(), source.getAlt() + Utils.getElevation(source, splat, config)));
            }
            haavt = Utils.haat(source,splat, config);
            if (haavt > -4999.0) {
                if (splat.isMetric()) {
                    repWri.write(String.format("Antenna height above average terrain: %.2f meters\n", config.METERS_PER_FOOT * haavt));
                } else {
                    repWri.write(String.format("Antenna height above average terrain: %.2f feet\n", haavt));
                }
            }
            azimuth = Utils.azimuth(source, destination, config);
            angle1 = Utils.elevationAngle(source, destination, splat, config);
            angle2 = Utils.elevationAngle2(source, destination, config.EARTHRADIUS, splat, config);
            if (splat.getLr().isGot_azimuth_pattern() || splat.getLr().isGot_elevation_pattern() ) {
                x = (int) Math.rint(10.0 * (10.0 - angle2));

                if (x >= 0 && x <= 1000) {
                    pattern = (double) splat.getLr().getAntenna_pattern()[(int) Math.rint(azimuth)][x];
                }

                patterndB = 20.0 * Math.log10(pattern);
            }
            if (splat.isMetric()) {
                repWri.write(String.format(String.format("Distance to %s: %.2f kilometers\n", destination.getName(), config.KM_PER_MILE * Utils.distance(source, destination, config))));
            } else {
                repWri.write(String.format(String.format("Distance to %s: %.2f miles\n", destination.getName(), Utils.distance(source, destination, config))));
            }
            repWri.write(String.format(String.format("Azimuth to %s: %.2f degrees\n", destination.getName(), azimuth)));
            if (angle1 >= 0.0) {
                repWri.write(String.format(String.format("Elevation angle to %s: %+.4f degrees\n", destination.getName(), angle1)));
            } else {
                repWri.write(String.format(String.format("Depression angle to %s: %+.4f degrees\n", destination.getName(), angle1)));
            }
            if ((angle2 - angle1) > 0.0001) {
                if (angle2 < 0.0) {
                    repWri.write("Depression");
                } else {
                    repWri.write("Elevation");
                }

                repWri.write(String.format(String.format(" angle to the first obstruction: %+.4f degrees\n", angle2)));
            }
            repWri.write(String.format("\n%s\n\n", splat.getDashes()));
            /* Receiver */

            repWri.write(String.format(String.format("Receiver site: %s\n", destination.getName())));
            if (destination.getLat() >= 0.0) {
                repWri.write(String.format(String.format("Site location: %.4f North / %.4f West", destination.getLat(), destination.getLon())));
                repWri.write(String.format(String.format(" (%s N / ", Utils.dec2dms(destination.getLat()))));
            } else {
                repWri.write(String.format(String.format("Site location: %.4f South / %.4f West", -destination.getLat(), destination.getLon())));
                repWri.write(String.format(" (%s S / ", Utils.dec2dms(destination.getLat())));
            }
            repWri.write(String.format("%s W)\n", Utils.dec2dms(destination.getLon())));
            if (splat.isMetric()) {
                repWri.write(String.format("Ground elevation: %.2f meters AMSL\n", config.METERS_PER_FOOT * Utils.getElevation(destination, splat, config)));
                repWri.write(String.format("Antenna height: %.2f meters AGL / %.2f meters AMSL\n", config.METERS_PER_FOOT * destination.getAlt(), config.METERS_PER_FOOT * (destination.getAlt() + Utils.getElevation(destination, splat, config))));
            } else {
                repWri.write(String.format("Ground elevation: %.2f feet AMSL\n", Utils.getElevation(destination, splat, config)));
                repWri.write(String.format("Antenna height: %.2f feet AGL / %.2f feet AMSL\n", destination.getAlt(), destination.getAlt() + Utils.getElevation(destination, splat, config)));
            }
            haavt = Utils.haat(destination, splat, config);
            if (haavt > -4999.0) {
                if (splat.isMetric()) {
                    repWri.write(String.format("Antenna height above average terrain: %.2f meters\n", config.METERS_PER_FOOT * haavt));
                } else {
                    repWri.write(String.format("Antenna height above average terrain: %.2f feet\n", haavt));
                }
            }
            if (splat.isMetric()) {
                repWri.write(String.format("Distance to %s: %.2f kilometers\n", source.getName(), config.KM_PER_MILE * Utils.distance(source, destination, config)));
            } else {
                repWri.write(String.format("Distance to %s: %.2f miles\n", source.getName(), Utils.distance(source, destination, config)));
            }
            azimuth = Utils.azimuth(destination, source, config);
            angle1 = Utils.elevationAngle(destination, source, splat, config);
            angle2 = Utils.elevationAngle2(destination, source, config.EARTHRADIUS, splat, config);
            repWri.write(String.format("Azimuth to %s: %.2f degrees\n", source.getName(), azimuth));
            if (angle1 >= 0.0) {
                repWri.write(String.format("Elevation angle to %s: %+.4f degrees\n", source.getName(), angle1));
            } else {
                repWri.write(String.format("Depression angle to %s: %+.4f degrees\n", source.getName(), angle1));
            }
            if ((angle2 - angle1) > 0.0001) {
                if (angle2 < 0.0) {
                    repWri.write(String.format("Depression"));
                } else {
                    repWri.write(String.format("Elevation"));
                }

                repWri.write(String.format(" angle to the first obstruction: %+.4f degrees\n", angle2));
            }
            repWri.write(String.format("\n%s\n\n", splat.getDashes()));
            if (splat.getLr().getFrq_mhz() > 0.0) {
                if (splat.isOlditm()) {
                    repWri.write(String.format("Longley-Rice Parameters Used In This Analysis:\n\n"));
                } else {
                    repWri.write(String.format("ITWOM Version %.1f Parameters Used In This Analysis:\n\n", "3.0"));
                }

                repWri.write(String.format("Earth's Dielectric Constant: %d\n", splat.getLr().getEps_dielect()));
                repWri.write(String.format("Earth's Conductivity: %.3lf Siemens/meter\n", splat.getLr().getSgm_conductivity()));
                repWri.write(String.format("Atmospheric Bending Constant (N-units): %.3lf ppm\n", splat.getLr().getEno_ns_surfref()));
                repWri.write(String.format("Frequency: %.3lf MHz\n", splat.getLr().getFrq_mhz()));
                repWri.write(String.format("Radio Climate: %d (", splat.getLr().getRadio_climate()));

                switch (splat.getLr().getRadio_climate()) {
                    case 1:
                        repWri.write(String.format("Equatorial"));
                        break;

                    case 2:
                        repWri.write(String.format("Continental Subtropical"));
                        break;

                    case 3:
                        repWri.write(String.format("Maritime Subtropical"));
                        break;

                    case 4:
                        repWri.write(String.format("Desert"));
                        break;

                    case 5:
                        repWri.write(String.format("Continental Temperate"));
                        break;

                    case 6:
                        repWri.write(String.format("Martitime Temperate, Over Land"));
                        break;

                    case 7:
                        repWri.write(String.format("Maritime Temperate, Over Sea"));
                        break;

                    default:
                        repWri.write(String.format("Unknown"));
                }

                repWri.write(String.format(")\nPolarization: %d (", splat.getLr().getPol()));

                if (splat.getLr().getPol() == 0) {
                    repWri.write(String.format("Horizontal"));
                }

                if (splat.getLr().getPol() == 1) {
                    repWri.write(String.format("Vertical"));
                }

                repWri.write(String.format(")\nFraction of Situations: %.1lf%c\n", splat.getLr().getConf() * 100.0, 37));
                repWri.write(String.format("Fraction of Time: %.1lf%c\n", splat.getLr().getRel() * 100.0, 37));

                if (splat.getLr().getErp() != 0.0) {
                    repWri.write(String.format("Transmitter ERP: "));

                    if (splat.getLr().getErp() < 1.0) {
                        repWri.write(String.format("%.1lf milliwatts", 1000.0 * splat.getLr().getErp()));
                    }

                    if (splat.getLr().getErp() >= 1.0 && splat.getLr().getErp() < 10.0) {
                        repWri.write(String.format("%.1lf Watts", splat.getLr().getErp()));
                    }

                    if (splat.getLr().getErp() >= 10.0 && splat.getLr().getErp() < 10.0e3) {
                        repWri.write(String.format("%.0lf Watts", splat.getLr().getErp()));
                    }

                    if (splat.getLr().getErp() >= 10.0e3) {
                        repWri.write(String.format("%.3lf kilowatts", splat.getLr().getErp() / 1.0e3));
                    }

                    dBm = 10.0 * (Math.log10(splat.getLr().getErp() * 1000.0));
                    repWri.write(String.format(" (%+.2f dBm)\n", dBm));

                    /* EIRP = ERP + 2.14 dB */
                    repWri.write(String.format("Transmitter EIRP: "));

                    eirp = splat.getLr().getErp() * 1.636816521;

                    if (eirp < 1.0) {
                        repWri.write(String.format("%.1lf milliwatts", 1000.0 * eirp));
                    }

                    if (eirp >= 1.0 && eirp < 10.0) {
                        repWri.write(String.format("%.1lf Watts", eirp));
                    }

                    if (eirp >= 10.0 && eirp < 10.0e3) {
                        repWri.write(String.format("%.0lf Watts", eirp));
                    }

                    if (eirp >= 10.0e3) {
                        repWri.write(String.format("%.3lf kilowatts", eirp / 1.0e3));
                    }

                    dBm = 10.0 * (Math.log10(eirp * 1000.0));
                    repWri.write(String.format(" (%+.2f dBm)\n", dBm));
                }

                repWri.write(String.format("\n%s\n\n", "---------------dashes"));

                repWri.write(String.format("Summary For The Link Between %s and %s:\n\n", source.getName(), destination.getName()));

                if (patterndB != 0.0) {
                    repWri.write(String.format("%s antenna pattern towards %s: %.3f (%.2f dB)\n", source.getName(), destination.getName(), pattern, patterndB));
                }

                Path path = Utils.readPath(source, destination, splat, config);
                /* source=TX, destination=RX */

 /* Copy elevations plus clutter along
                        path into the elev[] array. */
                elev = new double[path.getLength() + 2];
                for (x = 1; x < path.getLength() - 1; x++) {
                    elev[x + 2] = config.METERS_PER_FOOT * (path.getElevation()[x] == 0.0 ? path.getElevation()[x] : (splat.getClutter() + path.getElevation()[x]));
                }

                /* Copy ending points without clutter */
                elev[2] = path.getElevation()[0] * config.METERS_PER_FOOT;
                elev[path.getLength() + 1] = path.getElevation()[path.getLength() - 1] * config.METERS_PER_FOOT;

                azimuth = Math.rint(Utils.azimuth(source, destination,config));
ITWOMResult result = null;
                for (y = 2; y < (path.getLength() - 1); y++) /* path.getLength()-1 avoids lr error */ {
                    distance = 5280.0 * path.getDistance()[y];
                    source_alt = four_thirds_earth + source.getAlt() + path.getElevation()[0];
                    dest_alt = four_thirds_earth + destination.getAlt() + path.getElevation()[y];
                    dest_alt2 = dest_alt * dest_alt;
                    source_alt2 = source_alt * source_alt;

                    /* Calculate the cosine of the elevation of
                            the receiver as seen by the transmitter. */
                    cos_xmtr_angle = ((source_alt2) + (distance * distance) - (dest_alt2)) / (2.0 * source_alt * distance);

                    if (splat.getLr().isGot_elevation_pattern()) {
                        /* If an antenna elevation pattern is available, the
                                following code determines the elevation angle to
                                the first obstruction along the path. */

                        for (x = 2, block = false; x < y && !block; x++) {
                            distance = 5280.0 * (path.getDistance()[y] - path.getDistance()[x]);
                            test_alt = four_thirds_earth + path.getElevation()[x];

                            /* Calculate the cosine of the elevation
                                    angle of the terrain (test point)
                                    as seen by the transmitter. */
                            cos_test_angle = ((source_alt2) + (distance * distance) - (test_alt * test_alt)) / (2.0 * source_alt * distance);

                            /* Compare these two angles to determine if
                                    an obstruction exists.  Since we're comparing
                                    the cosines of these angles rather than
                                    the angles themselves, the sense of the
                                    following "if" statement is reversed from
                                    what it would be if the angles themselves
                                    were compared. */
                            if (cos_xmtr_angle >= cos_test_angle) {
                                block = true;
                            }
                        }

                        /* At this point, we have the elevation angle
                                to the first obstruction (if it exists). */
                    }

                    /* Determine path loss for each point along
                            the path using ITWOM's point_to_point mode
                            starting at x=2 (number_of_points = 1), the
                            shortest distance terrain can play a role in
                            path loss. */
                    elev[0] = y - 1;
                    /* (number of points - 1) */

 /* Distance between elevation samples */
                    elev[1] = config.METERS_PER_MILE * (path.getDistance()[y] - path.getDistance()[y - 1]);

                    if (splat.isOlditm()) {
                        result = new ITWOM3().point_to_point_ITM(elev, source.getAlt() * config.METERS_PER_FOOT,
                                destination.getAlt() * config.METERS_PER_FOOT, splat.getLr().getEps_dielect(), splat.getLr().getSgm_conductivity(), splat.getLr().getEno_ns_surfref(), splat.getLr().getFrq_mhz(), splat.getLr().getRadio_climate(), splat.getLr().getPol(), splat.getLr().getConf(), splat.getLr().getRel());
                    } else {
                        result = new ITWOM3().point_to_point(elev, source.getAlt() * config.METERS_PER_FOOT,
                                destination.getAlt() * config.METERS_PER_FOOT, splat.getLr().getEps_dielect(), splat.getLr().getSgm_conductivity(), splat.getLr().getEno_ns_surfref(), splat.getLr().getFrq_mhz(), splat.getLr().getRadio_climate(), splat.getLr().getPol(), splat.getLr().getConf(), splat.getLr().getRel());
                    }

                    if (block) {
                        elevation = ((Math.acos(cos_test_angle)) / config.DEG2RAD) - 90.0;
                    } else {
                        elevation = ((Math.acos(cos_xmtr_angle)) / config.DEG2RAD) - 90.0;
                    }

                    /* Integrate the antenna's radiation
                            pattern into the overall path loss. */
                    x = (int) Math.rint(10.0 * (10.0 - elevation));

                    if (x >= 0 && x <= 1000) {
                        pattern = (double) splat.getLr().getAntenna_pattern()[(int) azimuth][x];

                        if (pattern != 0.0) {
                            patterndB = 20.0 * Math.log10(pattern);
                        }
                    } else {
                        patterndB = 0.0;
                    }

                    total_loss = result.getLoss() - patterndB;

                    try {
                        File profileFile = new File("profile.gp");
                        FileWriter prfWrit = new FileWriter(profileFile);
                        if (splat.isMetric()) {
                            prfWrit.write(String.format("%f\t%f\n", config.KM_PER_MILE * path.getDistance()[y], total_loss));
                        } else {
                            prfWrit.write(String.format("%f\t%f\n", path.getDistance()[y], total_loss));
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if (total_loss > maxloss) {
                        maxloss = total_loss;
                    }

                    if (total_loss < minloss) {
                        minloss = total_loss;
                    }
                }

                distance = Utils.distance(source, destination, config);

                if (distance != 0.0) {
                    free_space_loss = 36.6 + (20.0 * Math.log10(splat.getLr().getFrq_mhz())) + (20.0 * Math.log10(distance));

                    repWri.write(String.format("Free space path loss: %.2f dB\n", free_space_loss));
                }

                if (splat.isOlditm()) {
                    repWri.write(String.format("Longley-Rice path loss: %.2f dB\n", result.getLoss()));
                } else {
                    repWri.write(String.format("ITWOM Version %.1f path loss: %.2f dB\n", "3.0", result.getLoss()));
                }

                if (free_space_loss != 0.0) {
                    repWri.write(String.format("Attenuation due to terrain shielding: %.2f dB\n", result.getLoss() - free_space_loss));
                }

                if (patterndB != 0.0) {
                    repWri.write(String.format("Total path loss including %s antenna pattern: %.2f dB\n", source.getName(), total_loss));
                }

                if (splat.getLr().getErp() != 0.0) {
                    field_strength = (139.4 + (20.0 * Math.log10(splat.getLr().getFrq_mhz())) - total_loss) + (10.0 * Math.log10(splat.getLr().getErp() / 1000.0));

                    /* dBm is referenced to EIRP */
                    rxp = eirp / (Math.pow(10.0, (total_loss / 10.0)));
                    dBm = 10.0 * (Math.log10(rxp * 1000.0));
                    power_density = (eirp / (Math.pow(10.0, (total_loss - free_space_loss) / 10.0)));
                    /* divide by 4*PI*distance_in_meters squared */
                    power_density /= (4.0 * config.PI * distance * distance * 2589988.11);

                    repWri.write(String.format("Field strength at %s: %.2f dBuV/meter\n", destination.getName(), field_strength));
                    repWri.write(String.format("Signal power level at %s: %+.2f dBm\n", destination.getName(), dBm));
                    repWri.write(String.format("Signal power density at %s: %+.2f dBW per square meter\n", destination.getName(), 10.0 * Math.log10(power_density)));
                    voltage = 1.0e6 * Math.sqrt(50.0 * (eirp / (Math.pow(10.0, (total_loss - 2.14) / 10.0))));
                    repWri.write(String.format("Voltage across a 50 ohm dipole at %s: %.2f uV (%.2f dBuV)\n", destination.getName(), voltage, 20.0 * Math.log10(voltage)));

                    voltage = 1.0e6 * Math.sqrt(75.0 * (eirp / (Math.pow(10.0, (total_loss - 2.14) / 10.0))));
                    repWri.write(String.format("Voltage across a 75 ohm dipole at %s: %.2f uV (%.2f dBuV)\n", destination.getName(), voltage, 20.0 * Math.log10(voltage)));
                }

                repWri.write("Mode of propagation: ");

                if (splat.isOlditm()) {
                    repWri.write(String.format("%s\n", result.getStrmode()));
                    repWri.write(String.format("Longley-Rice model error number: %d", result.getErrnum()));
                } else {
                    if (result.getStrmode().equals("L-o-S")) {
                        repWri.write("Line of Sight\n");
                    }

                    if (result.getStrmode().contains("1_Hrzn")) {
                        repWri.write("Single Horizon ");
                    }

                    if (result.getStrmode().contains("2_Hrzn")) {
                        repWri.write("Double Horizon ");
                    }

                    y = result.getStrmode().length();

                    if (y > 19) {
                        y = 19;
                    }

                   
                    if (result.getStrmode().contains("_Diff")) {
                        repWri.write("Diffraction Dominant\n");
                    }

                    if (result.getStrmode().contains("_Tropo")) {
                        repWri.write("Troposcatter Dominant\n");
                    }

                    if (result.getStrmode().contains("_Peak")) {
                        repWri.write("RX at Peak Terrain Along Path\n");
                    }

                    repWri.write(String.format("ITWOM error number: %d", result.getErrnum()));
                }

                switch (result.getErrnum()) {
                    case 0:
                        repWri.write(" (No error)\n");
                        break;

                    case 1:
                        repWri.write("\n  Warning: Some parameters are nearly out of range.\n");
                        repWri.write("  Results should be used with caution.\n");
                        break;

                    case 2:
                        repWri.write("\n  Note: Default parameters have been substituted for impossible ones.\n");
                        break;

                    case 3:
                        repWri.write("\n  Warning: A combination of parameters is out of range.\n");
                        repWri.write("  Results are probably invalid.\n");
                        break;

                    default:
                        repWri.write("\n  Warning: Some parameters are out of range.\n");
                        repWri.write("  Results are probably invalid.\n");
                }

                repWri.write(String.format("\n%s\n\n", "-------------------------------dashes"));
            }
            System.out.printf("\nPath Loss Report written to: \"%s\"\n", report_name);
            obstructionAnalysis(source, destination, splat.getLr().getFrq_mhz(), repWri);
            /* Skip plotting the graph if ONLY a path-loss report is needed. */

            if (graph_it) {
                if (name.startsWith(".")) {
                    /* Default filename and output file type */

                    basename = "profile";
                    term = "png";
                    ext = "png";

                } else {
                    /* Extract extension and terminal type from "name" */

                    basename = name;
                    //strncpy(basename, name, 254);
                    if (name.contains(".")) {
                        ext = term = name.substring(name.lastIndexOf("."));
                    } else {
                        term = ext = "png";
                    }

                    /* Either .ps or .postscript may be used
                        as an extension for postscript output. */
                    if (term.contains("postscript")) {
                        ext = "ps";
                    } else if (ext.contains("ps")) {
                        term = "postscript enhanced color";
                    }

                    File gpFile = new File("splat.gp");
                    FileWriter wri;
                    try {
                        wri = new FileWriter(gpFile);
                    
                        wri.write("set grid\n");
                        wri.write(String.format("set yrange [%2.3f to %2.3f]\n", minloss, maxloss));
                        wri.write("set encoding iso_8859_1\n");
                        wri.write(String.format("set term %s\n", term));
                        wri.write(String.format("set title \"%s Loss Profile Along Path Between %s and %s (%.2f%c azimuth)\"\n", splat.getName(), destination.getName(), source.getName(), Utils.azimuth(destination, source, config), 176));

                        if (splat.isMetric()) {
                            wri.write(String.format("set xlabel \"Distance Between %s and %s (%.2f kilometers)\"\n", destination.getName(), source.getName(), config.KM_PER_MILE * Utils.distance(destination, source, config)));
                        } else {
                            wri.write(String.format("set xlabel \"Distance Between %s and %s (%.2f miles)\"\n", destination.getName(), source.getName(), Utils.distance(destination, source, config)));
                        }

                        if (splat.getLr().isGot_azimuth_pattern() || splat.getLr().isGot_elevation_pattern()) {
                            wri.write(String.format("set ylabel \"Total Path Loss (including TX antenna pattern) (dB)"));
                        } else {
                            if (splat.isOlditm()) {
                                wri.write("set ylabel \"Longley-Rice Path Loss (dB)");
                            } else {
                                wri.write(String.format("set ylabel \"ITWOM Version %.1f Path Loss (dB)", "3.0")); //ITWOMVersion()));
                            }
                        }

                        wri.write(String.format("\"\nset output \"%s.%s\"\n", basename, ext));
                        wri.write("plot \"profile.gp\" title \"Path Loss\" with lines\n");
                    } catch (IOException ex) {
                        Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    Process proc = Runtime.getRuntime().exec("gnuplot splat.gp");
                    x = proc.exitValue();

                    if (x != -1) {
                        if (!splat.isGpsav()) {
                            new File("splat.gp").delete();
                            new File("profile.gp").delete();
                            new File("reference.gp").delete();
                        }

                        System.out.printf("Path loss plot written to: \"%s.%s\"\n", basename, ext);
                    } else {
                        System.err.print("\n*** ERROR: Error occurred invoking gnuplot!\n");
                    }
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                repWri.close();
            } catch (IOException ex) {
                Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void SiteReport(Site xmtr) {
        FileWriter wri = null;
        try {
            String report_name;
            double terrain;
            int x, azi;
            report_name = String.format("%s-site_report.txt", xmtr.getName());
            for (char ch : new char[]{32, 17, 92, 42, 47}) {
                report_name = report_name.replace(ch, '_');
            }
            File siteReportFile = new File(report_name);
            wri = new FileWriter(siteReportFile);
            wri.write(String.format("\n\t--==[ %s v%s Site Analysis Report For: %s ]==--\n\n", splat.getName(), splat.getVersion(), xmtr.getName()));
            wri.write(String.format("%s\n\n", splat.getDashes()));
            if (xmtr.getLat() >= 0.0) {
                wri.write(String.format("Site location: %.4f North / %.4f West", xmtr.getLat(), xmtr.getLon()));
                wri.write(String.format(" (%s N / ", Utils.dec2dms(xmtr.getLat())));
            } else {
                wri.write(String.format("Site location: %.4f South / %.4f West", -xmtr.getLat(), xmtr.getLon()));
                wri.write(String.format(" (%s S / ", Utils.dec2dms(xmtr.getLat())));
            }
            wri.write(String.format("%s W)\n", Utils.dec2dms(xmtr.getLon())));
            if (splat.isMetric()) {
                wri.write(String.format("Ground elevation: %.2f meters AMSL\n", config.METERS_PER_FOOT * Utils.getElevation(xmtr, splat, config)));
                wri.write(String.format("Antenna height: %.2f meters AGL / %.2f meters AMSL\n", config.METERS_PER_FOOT * xmtr.getAlt(), config.METERS_PER_FOOT * (xmtr.getAlt() + Utils.getElevation(xmtr, splat, config))));
            } else {
                wri.write(String.format("Ground elevation: %.2f feet AMSL\n", Utils.getElevation(xmtr, splat, config)));
                wri.write(String.format("Antenna height: %.2f feet AGL / %.2f feet AMSL\n", xmtr.getAlt(), xmtr.getAlt() + Utils.getElevation(xmtr, splat, config)));
            }
            terrain = Utils.haat(xmtr, splat, config);
            if (terrain > -4999.0) {
                if (splat.isMetric()) {
                    wri.write(String.format("Antenna height above average terrain: %.2f meters\n\n", config.METERS_PER_FOOT * terrain));
                } else {
                    wri.write(String.format("Antenna height above average terrain: %.2f feet\n\n", terrain));
                }

                /* Display the average terrain between 2 and 10 miles
                from the transmitter site at azimuths of 0, 45, 90,
                135, 180, 225, 270, and 315 degrees. */
                for (azi = 0; azi <= 315; azi += 45) {
                    wri.write(String.format("Average terrain at %3d degrees azimuth: ", azi));
                    terrain = Utils.averageTerrain(xmtr, (double) azi, 2.0, 10.0, splat, config);

                    if (terrain > -4999.0) {
                        if (splat.isMetric()) {
                            wri.write(String.format("%.2f meters AMSL\n", config.METERS_PER_FOOT * terrain));
                        } else {
                            wri.write(String.format("%.2f feet AMSL\n", terrain));
                        }
                    } else {
                        wri.write(String.format("No terrain\n"));
                    }
                }
            }
            wri.write(String.format("\n%s\n\n", splat.getDashes()));
            System.out.printf("\nSite analysis report written to: \"%s\"\n", report_name);
        } catch (IOException ex) {
            Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                wri.close();
            } catch (IOException ex) {
                Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
