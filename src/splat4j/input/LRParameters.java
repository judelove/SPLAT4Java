/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splat4j.input;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import splat4j.LR;
import splat4j.SplatEngine;

/**
 *
 * @author Jude Mukundane
 */
public class LRParameters {

    SplatEngine splat;
    LR lr;
    String filename;
    double eps_dielect = 15.0;
    double sgm_conductivity = 0.005;
    double eno_ns_surfref = 301.0;
    double frq_mhz = 300;
    int radio_climate = 5;
    int pol = 0;
    double conf = 0.5;
    double rel = 0.5;
    double erp = 0.0;

    private double[] azimuth = new double[361], azimuthPattern = new double[361], el_pattern = new double[10001], slant_angle = new double[361];
    double[][] elevation_pattern = new double[361][1001], antenna_pattern = new double[361][1001];
    double az, xx, elevation, amplitude, rotation, valid1, valid2, delta, tilt, mechanical_tilt = 0.0, tilt_azimuth, tilt_increment, sum;
    int[] readCount = new int[361];
    int got_azimuth_pattern = 0, got_elevation_pattern = 0;

    public LRParameters(SplatEngine splat) {
        this.splat = splat;
    }

    public void loadLRFile(String basename) {
        /* This function reads ITM parameter data for the transmitter
	   site.  The file name is the same as the txsite, except the
	   filename extension is .lrp.  If the needed file is not found,
	   then the file "splat.lrp" is read from the current working
	   directory.  Failure to load this file under a forced_read
	   condition will result in the default parameters hard coded
	   into this function to be used and written to "splat.lrp". */
        this.filename = basename + ".lrp";
        File lrFile;

        if (filename != null && !filename.isEmpty()) {
            lrFile = new File(filename);

            if (lrFile.exists()) {
                try {
                    lr = new LR();
                    Scanner scanner = new Scanner(lrFile);
                    lr.setEps_dielect(Double.parseDouble(scanner.nextLine().split(";")[0].trim()));
                    lr.setSgm_conductivity(Double.parseDouble(scanner.nextLine().split(";")[0].trim()));
                    lr.setEno_ns_surfref(Double.parseDouble(scanner.nextLine().split(";")[0].trim()));
                    this.frq_mhz = Double.parseDouble(scanner.nextLine().split(";")[0].trim());
                    lr.setFrq_mhz(splat.getForcedFreq() < 0 ? this.frq_mhz:splat.getForcedFreq());
                    lr.setRadio_climate(this.radio_climate = Integer.parseInt(scanner.nextLine().split(";")[0].trim()));
                    lr.setPol(Integer.parseInt(scanner.nextLine().split(";")[0].trim()));
                    lr.setConf(Double.parseDouble(scanner.nextLine().split(";")[0].trim()));
                    lr.setRel(Double.parseDouble(scanner.nextLine().split(";")[0].trim()));

                    String power = scanner.nextLine().split(";")[0].trim();
                    lr.setErp(Double.parseDouble(power.replaceAll("dBm|dbm", "")));
                    if (power.toLowerCase().contains("dbm")) {
                        lr.setErp((Math.pow(10.0, (lr.getErp() - 32.14) / 10.0)));
                    }


                    this.loadAzFile(filename.replace("lrp", "az"));
                    this.loadElFile(filename.replace("lrp", "el"));
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(LRParameters.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NumberFormatException ex) {
                    System.err.println("There was an error loading the specified lrp file. Default values will be used");

                }
            } else {

                lr = new LR();
                lr.setEps_dielect(this.eps_dielect);
                lr.setSgm_conductivity(this.sgm_conductivity);
                lr.setEno_ns_surfref(this.eno_ns_surfref);
                lr.setFrq_mhz(splat.getForcedFreq() < 0 ? this.frq_mhz : splat.getForcedFreq());
                lr.setRadio_climate(this.radio_climate);
                lr.setPol(this.pol);
                lr.setConf(this.conf);
                lr.setRel(this.rel);

                lr.setErp(splat.getForcedErp() < 0? Math.pow(10.0, (this.erp - 32.14) / 10.0) : splat.getForcedErp());
                lrFile = new File("splat.lrp");

                FileWriter fwr = null;
                try //write the file
                {
                    fwr = new FileWriter(lrFile);
                    fwr.write(String.format("%.3f\t; Earth Dielectric Constant (Relative permittivity)\n", this.eps_dielect));
                    fwr.write(String.format("%.3f\t; Earth Conductivity (Siemens per meter)\n", this.sgm_conductivity));
                    fwr.write(String.format("%.3f\t; Atmospheric Bending Constant (N-Units)\n", this.eno_ns_surfref));
                    fwr.write(String.format("%.3f\t; Frequency in MHz (20 MHz to 20 GHz)\n", this.frq_mhz));
                    fwr.write(String.format("%d\t; Radio Climate\n", this.radio_climate));
                    fwr.write(String.format("%d\t; Polarization (0 = Horizontal, 1 = Vertical)\n", this.pol));
                    fwr.write(String.format("%.2f\t; Fraction of Situations\n", this.conf));
                    fwr.write(String.format("%.2f\t; Fraction of Time\n", this.rel));
                    fwr.write(String.format("%.2f\t; Transmitter Effective Radiated Power in Watts or dBm (optional)\n", this.erp));
                    fwr.write(String.format("\nPlease consult SPLAT! documentation for the meaning and use of this data.\n"));
                    System.err.printf("\n\n*** There were problems reading your \"%s\" file! ***\nA \"splat.lrp\" file was written to your directory with default data.\n", filename);
                } catch (IOException ex) {
                    Logger.getLogger(LRParameters.class.getName()).log(Level.SEVERE, null, ex);
                    System.err.println("Default parameters have been assumed for this analysis.\n");
                } finally {
                    try {
                        if (fwr != null) {
                            fwr.close();
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(LRParameters.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }

            }
            this.splat.setLr(lr);
        }
    }

    void loadAzFile(String filename) {
        /* This function reads and processes antenna pattern (.az
	   and .el) files that correspond in name to previously
	   loaded SPLAT! .lrp files.  */

        int a, b, w, x, y, z, last_index, next_index, span;

        /* Load .az antenna pattern file */
        File azFile = new File(filename);

        if (azFile.exists()) {
            try {
                Scanner scanner = new Scanner(azFile);
                /* Clear azimuth pattern array */

                for (x = 0; x <= 360; x++) {
                    azimuth[x] = 0.0;
                    readCount[x] = 0;
                }

                /* Read azimuth pattern rotation
                in degrees measured clockwise
                from true North. */
                rotation = Double.parseDouble(scanner.nextLine().split(";")[0].trim());

                /* Read azimuth (degrees) and corresponding
                normalized field radiation pattern amplitude
                (0.0 to 1.0) until EOF is reached. */
                while (scanner.hasNextLine()) {
                    String azAmp = scanner.nextLine().split(";")[0];
                    az = Double.parseDouble(azAmp.split(" ")[0]);
                    amplitude = Double.parseDouble(azAmp.split(" ")[1]);

         x = (int) Math.rint(az);

                    if (x >= 0 && x <= 360) {
                        azimuth[x] += amplitude;
                        readCount[x]++;
                    }

                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(LRParameters.class.getName()).log(Level.SEVERE, null, ex);
            }
            /* Handle 0=360 degree ambiguity */
            if ((readCount[0] == 0) && (readCount[360] != 0)) {
                readCount[0] = readCount[360];
                azimuth[0] = azimuth[360];
            }

            if ((readCount[0] != 0) && (readCount[360] == 0)) {
                readCount[360] = readCount[0];
                azimuth[360] = azimuth[0];
            }

            /* Average pattern values in case more than
one was read for each degree of azimuth. */
            for (x = 0; x <= 360; x++) {
                if (readCount[x] > 1) {
                    azimuth[x] /= (float) readCount[x];
                }
            }

            /* Interpolate missing azimuths
to completely fill the array */
            last_index = -1;
            next_index = -1;

            for (x = 0; x <= 360; x++) {
                if (readCount[x] != 0) {
                    if (last_index == -1) {
                        last_index = x;
                    } else {
                        next_index = x;
                    }
                }

                if (last_index != -1 && next_index != -1) {
                    valid1 = azimuth[last_index];
                    valid2 = azimuth[next_index];

                    span = next_index - last_index;
                    delta = (valid2 - valid1) / (float) span;

                    for (y = last_index + 1; y < next_index; y++) {
                        azimuth[y] = azimuth[y - 1] + delta;
                    }

                    last_index = y;
                    next_index = -1;
                }
            }

            /* Perform azimuth pattern rotation
and load azimuth_pattern[361] with
azimuth pattern data in its final form. */
            for (x = 0; x < 360; x++) {
                y = x + (int) Math.rint(rotation);

                if (y >= 360) {
                    y -= 360;
                }

                azimuthPattern[y] = azimuth[x];
            }

            azimuthPattern[360] = azimuthPattern[0];

            lr.setGot_azimuth_pattern(true); 
                    got_azimuth_pattern = 255;

        }
    }

    void loadElFile(String filename) {

        /* Read and process .el file */
        File elFile = new File(filename);
        int a, b, w, x, y, z, last_index, next_index, span;
        if (elFile.exists()) {
            try {
                Scanner scanner = new Scanner(elFile);
                /* Clear azimuth pattern array */

                for (x = 0; x <= 10000; x++) {
                    el_pattern[x] = 0.0;
                    readCount[x] = 0;
                }

                /* Read mechanical tilt (degrees) and
                tilt azimuth in degrees measured
                clockwise from true North. */
          String mechTiltAzimuth = scanner.nextLine().split(";")[0];
                mechanical_tilt = Double.parseDouble(mechTiltAzimuth.split(" ")[0]);
                tilt_azimuth = Double.parseDouble(mechTiltAzimuth.split(" ")[1]);

//sscanf(string, "%f %f",  & mechanical_tilt,  & tilt_azimuth);

                /* Read elevation (degrees) and corresponding
normalized field radiation pattern amplitude
(0.0 to 1.0) until EOF is reached. */
                while (scanner.hasNextLine()) {
                    String azAmp = scanner.nextLine().split(";")[0];
                    elevation = Double.parseDouble(azAmp.split(" ")[0]);
                    amplitude = Double.parseDouble(azAmp.split(" ")[1]);

//                /* Read in normalized radiated field values
//			   for every 0.01 degrees of elevation between
//			   -10.0 and +90.0 degrees */

                    x = (int) Math.rint(100.0 * (elevation + 10.0));

                    if (x >= 0 && x <= 10000) {
                        el_pattern[x] += amplitude;
                        readCount[x] = readCount[x] + 1;
                    }

                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(LRParameters.class.getName()).log(Level.SEVERE, null, ex);
            }
            /* Average the field values in case more than
one was read for each 0.01 degrees of elevation. */
            for (x = 0; x <= 10000; x++) {
                if (readCount[x] > 1) {
                    el_pattern[x] = el_pattern[x] / (float) readCount[x];
                }
            }

            /* Interpolate between missing elevations (if
any) to completely fill the array and provide
radiated field values for every 0.01 degrees of
elevation. */
            last_index = -1;
            next_index = -1;

            for (x = 0; x <= 10000; x++) {
                if (readCount[x] != 0) {
                    if (last_index == -1) {
                        last_index = x;
                    } else {
                        next_index = x;
                    }
                }

                if (last_index != -1 && next_index != -1) {
                    valid1 = el_pattern[last_index];
                    valid2 = el_pattern[next_index];

                    span = next_index - last_index;
                    delta = (valid2 - valid1) / (float) span;

                    for (y = last_index + 1; y < next_index; y++) {
                        el_pattern[y] = el_pattern[y - 1] + delta;
                    }

                    last_index = y;
                    next_index = -1;
                }
            }

            /* Fill slant_angle[] array with offset angles based
on the antenna's mechanical beam tilt (if any)
and tilt direction (azimuth). */
            if (mechanical_tilt == 0.0) {
                for (x = 0; x <= 360; x++) {
                    slant_angle[x] = 0.0;
                }
            } else {
                tilt_increment = mechanical_tilt / 90.0;

                for (x = 0; x <= 360; x++) {
                    xx = (float) x;
                    y = (int) Math.rint(tilt_azimuth + xx);

                    while (y >= 360) {
                        y -= 360;
                    }

                    while (y < 0) {
                        y += 360;
                    }

                    if (x <= 180) {
                        slant_angle[y] = -(tilt_increment * (90.0 - xx));
                    }

                    if (x > 180) {
                        slant_angle[y] = -(tilt_increment * (xx - 270.0));
                    }
                }
            }

            slant_angle[360] = slant_angle[0];
            /* 360 degree wrap-around */

            for (w = 0; w <= 360; w++) {
                tilt = slant_angle[w];

                /**
                 * Convert tilt angle to an array index offset *
                 */
                y = (int) Math.rint(100.0 * tilt);

                /* Copy shifted el_pattern[10001] field
    values into elevation_pattern[361][1001]
    at the corresponding azimuth, downsampling
    (averaging) along the way in chunks of 10. */
                for (x = y, z = 0; z <= 1000; x += 10, z++) {
                    for (sum = 0.0, a = 0; a < 10; a++) {
                        b = a + x;

                        if (b >= 0 && b <= 10000) {
                            sum += el_pattern[b];
                        }
                        if (b < 0) {
                            sum += el_pattern[0];
                        }
                        if (b > 10000) {
                            sum += el_pattern[10000];
                        }
                    }

                    elevation_pattern[w][z] = sum / 10.0;
                }
            }
lr.setGot_elevation_pattern(true);
            got_elevation_pattern = 255;
        }

        for (x = 0; x <= 360; x++) {
            for (y = 0; y <= 1000; y++) {
                if (got_elevation_pattern == 255) {
                    elevation = elevation_pattern[x][y];
                } else {
                    elevation = 1.0;
                }

                if (got_azimuth_pattern == 255) {
                    az = azimuthPattern[x];
                } else {
                    az = 1.0;
                }

                lr.setAntenna_pattern(x, y, (float)(az * elevation));
            }
        }

    }
}
