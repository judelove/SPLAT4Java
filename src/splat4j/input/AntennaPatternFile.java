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
import splat4j.LR;

/**
 *
 * @author Jude Mukundane
 */
public class AntennaPatternFile {

    LR lr;
    boolean got_azimuth_pattern = false, got_elevation_pattern = false;
    private double[] azimuth_pattern;
    private double[][] elevation_pattern;
    private double rotation;

    void loadAzFile(String filename) {
        /* This function reads and processes antenna pattern (.az
	   and .el) files that correspond in name to previously
	   loaded SPLAT! .lrp files.  */

        int x, y, last_index, next_index, span;
        String azfile;
        double az, amplitude, valid1, valid2, delta;
        double[] azimuth = new double[361];
        ;
        char[] read_count = new char[10001];

        if (filename.contains(".")) {
            azfile = filename.substring(0, filename.indexOf(".")) + ".az";

        } else {
            azfile = filename + ".az";
        }

        rotation = 0.0f;

        /* Load .az antenna pattern file */
        File azFile = new File(azfile);

        if (azFile.exists()) {
            try {
                Scanner scanner = new Scanner(azFile);
                /* Clear azimuth pattern array */

                for (x = 0; x <= 360; x++) {
                    azimuth[x] = 0.0f;
                    read_count[x] = 0;
                }

                rotation = Float.parseFloat(scanner.nextLine().split(";")[0].trim());
                /* Read azimuth pattern rotation
                in degrees measured clockwise
                from true North. */
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] parts = line.split(";")[0].split(" ");
                    az = Float.parseFloat(parts[0]);
                    amplitude = Float.parseFloat(parts[1]);
                    x = (int) Math.rint(az);

                    if (x >= 0 && x <= 360) {
                        azimuth[x] += amplitude;
                        read_count[x]++;
                    }


                }

                /* Handle 0=360 degree ambiguity */
                if ((read_count[0] == 0) && (read_count[360] != 0)) {
                    read_count[0] = read_count[360];
                    azimuth[0] = azimuth[360];
                }

                if ((read_count[0] != 0) && (read_count[360] == 0)) {
                    read_count[360] = read_count[0];
                    azimuth[360] = azimuth[0];
                }

                /* Average pattern values in case more than
                one was read for each degree of azimuth. */
                for (x = 0; x <= 360; x++) {
                    if (read_count[x] > 1) {
                        azimuth[x] /= (float) read_count[x];
                    }
                }

                /* Interpolate missing azimuths
                to completely fill the array */
                last_index = -1;
                next_index = -1;

                for (x = 0; x <= 360; x++) {
                    if (read_count[x] != 0) {
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

                    azimuth_pattern[y] = azimuth[x];
                }

                azimuth_pattern[360] = azimuth_pattern[0];

                got_azimuth_pattern = true;
                lr.setGot_azimuth_pattern(got_azimuth_pattern);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(AntennaPatternFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    void loadElFile(String filename) {
        /* Read and process .el file */

 /* This function reads and processes antenna pattern (.az
	   and .el) files that correspond in name to previously
	   loaded SPLAT! .lrp files.  */
        int a, b, w, x, y, z, last_index, next_index, span;
        String elfile;
        double xx, elevation, amplitude, valid1, valid2, delta, tilt, mechanical_tilt = 0.0f, tilt_azimuth, tilt_increment, sum;
        double[] el_pattern = new double[10001], slant_angle = new double[361];

        char[] read_count = new char[10001];

        if (filename.contains(".")) {
            elfile = filename.substring(0, filename.indexOf(".")) + ".el";
        } else {
            elfile = filename + ".el";
        }

        /* Load .az antenna pattern file */
        File elFile = new File(elfile);

        if (elFile.exists()) {

            try {
                Scanner scanner = new Scanner(elFile);
                for (x = 0; x <= 10000; x++) {
                    el_pattern[x] = 0.0f;
                    read_count[x] = 0;
                }

                /* Read mechanical tilt (degrees) and
                tilt azimuth in degrees measured
                clockwise from true North. */
                String line = scanner.nextLine();
                String[] parts = line.split(";")[0].split(" ");
                mechanical_tilt = Float.parseFloat(parts[0]);
                tilt_azimuth = Float.parseFloat(parts[1]);
                while (scanner.hasNextLine()) {
                    line = scanner.nextLine();
                    parts = line.split(";")[0].split(" ");
                    elevation = Double.parseDouble(parts[0]);
                    amplitude = Double.parseDouble(parts[1]);

                    x = (int) Math.rint(100.0 * (elevation + 10.0));

                    if (x >= 0 && x <= 10000) {
                        el_pattern[x] += amplitude;
                        read_count[x]++;
                    }

                }

                /* Average the field values in case more than
                one was read for each 0.01 degrees of elevation. */
                for (x = 0; x <= 10000; x++) {
                    if (read_count[x] > 1) {
                        el_pattern[x] /= (float) read_count[x];
                    }
                }

                /* Interpolate between missing elevations (if
                any) to completely fill the array and provide
                radiated field values for every 0.01 degrees of
                elevation. */
                last_index = -1;
                next_index = -1;

                for (x = 0; x <= 10000; x++) {
                    if (read_count[x] != 0) {
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
                        slant_angle[x] = 0.0f;
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

                got_elevation_pattern = true;
                lr.setGot_elevation_pattern(got_elevation_pattern);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(AntennaPatternFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    void setTopographicValues() {
        int x, y;
        double az, elevation;
        for (x = 0; x <= 360; x++) {
            for (y = 0; y <= 1000; y++) {
                if (got_elevation_pattern) {
                    elevation = elevation_pattern[x][y];
                } else {
                    elevation = 1.0;
                }

                if (got_azimuth_pattern) {
                    az = azimuth_pattern[x];
                } else {
                    az = 1.0;
                }

                lr.setAntenna_pattern(x, y, (float)(az * elevation));
            }
        }
    }
}
