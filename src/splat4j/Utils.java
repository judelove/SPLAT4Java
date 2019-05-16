/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splat4j;

/**
 *
 * @author Jude Mukundane
 */
public class Utils {

    
    public static double lonDiff(double lon1, double lon2) {
        /* This function returns the short path longitudinal
	   difference between longitude1 and longitude2 
	   as an angle between -180.0 and +180.0 degrees.
	   If lon1 is west of lon2, the result is positive.
	   If lon1 is east of lon2, the result is negative. */

        double diff;

        diff = lon1 - lon2;

        if (diff <= -180.0) {
            diff += 360.0;
        }

        if (diff >= 180.0) {
            diff -= 360.0;
        }

        return diff;
    }

    public static double readBearing(String input) {
        /* This function takes numeric input in the form of a character
	   string, and returns an equivalent bearing in degrees as a
	   decimal number (double).  The input may either be expressed
	   in decimal format (40.139722) or degree, minute, second
	   format (40 08 23).  This function also safely handles
	   extra spaces found either leading, trailing, or
	   embedded within the numbers expressed in the
	   input string.  Decimal seconds are permitted. */

        double bearing = 0.0;

        if (input.trim().contains(" ")) { //Degree, Minute, Second Format (40 08 23.xx)
            String[] parts = input.trim().split(" ");
            double deg = 0, min = 0, sec = 0;
            if (parts.length >= 1) {
                deg = Math.abs(Double.parseDouble(parts[0]));
                bearing = deg;
            }
            if (parts.length >= 2) {
                min = Math.abs(Double.parseDouble(parts[1]));
                bearing += min / 60;
            }
            if (parts.length >= 3) {
                sec = Math.abs(Double.parseDouble(parts[2]));
                bearing += sec / 3600;
            }
            bearing = (deg < 0 || min < 0 || sec < 0) ? -bearing : bearing;
        } else {
            bearing = Math.abs(Double.parseDouble(input.trim()));
        }

        return bearing < -360 ? 0 : (bearing > 360) ? 0 : bearing;
    }

    public static double arccos(double x, double y, Configuration config) {
        /* This function implements the arc cosine function,
	   returning a value between 0 and TWOPI. */

        double result = 0.0;

        if (y > 0.0) {
            result = Math.acos(x / y);
        }

        if (y < 0.0) {
            result = config.PI + Math.acos(x / y);
        }

        return result;
    }

    public static int reduceAngle(double angle, Configuration config) {
        /* This function normalizes the argument to
	   an integer angle between 0 and 180 degrees */

        double temp;

        temp = Math.acos(Math.cos(angle * config.DEG2RAD));

        return (int) Math.rint(temp / config.DEG2RAD);
    }

    public static double distance(Site site1, Site site2, Configuration config) {
        /* This function returns the great circle distance
	   in miles between any two site locations. */

        double lat1, lon1, lat2, lon2, distance;

        lat1 = site1.getLat() * config.DEG2RAD;
        lon1 = site1.getLon() * config.DEG2RAD;
        lat2 = site2.getLat() * config.DEG2RAD;
        lon2 = site2.getLon() * config.DEG2RAD;

        distance = 3959.0 * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos((lon1) - (lon2)));

        return distance;
    }

    public static String dec2dms(double decimal) {
        /* Converts decimal degrees to degrees, minutes, seconds,
	   (DMS) and returns the result as a character string. */

        int sign;
        int degrees, minutes, seconds;
        double a, b, c, d;

        if (decimal < 0.0) {
            decimal = -decimal;
            sign = -1;
        } else {
            sign = 1;
        }

        a = Math.floor(decimal);
        b = 60.0 * (decimal - a);
        c = Math.floor(b);
        d = 60.0 * (b - c);

        degrees = (int) a;
        minutes = (int) c;
        seconds = (int) d;

        if (seconds < 0) {
            seconds = 0;
        }

        if (seconds > 59) {
            seconds = 59;
        }

        return String.format("%d%c %d\' %d\"", degrees * sign, 176, minutes, seconds);
    }

    public static double azimuth(Site source, Site destination, Configuration config) {
        /* This function returns the azimuth (in degrees) to the
	   destination as seen from the location of the source. */

        double dest_lat, dest_lon, src_lat, src_lon,
                beta, azimuth, diff, num, den, fraction;

        dest_lat = destination.getLat() * config.DEG2RAD;
        dest_lon = destination.getLon() * config.DEG2RAD;

        src_lat = source.getLat() * config.DEG2RAD;
        src_lon = source.getLon() * config.DEG2RAD;

        /* Calculate Surface Distance */
        beta = Math.acos(Math.sin(src_lat) * Math.sin(dest_lat) + Math.cos(src_lat) * Math.cos(dest_lat) * Math.cos(src_lon - dest_lon));

        /* Calculate Azimuth */
        num = Math.sin(dest_lat) - (Math.sin(src_lat) * Math.cos(beta));
        den = Math.cos(src_lat) * Math.sin(beta);
        fraction = num / den;

        /* Trap potential problems in acos() due to rounding */
        if (fraction >= 1.0) {
            fraction = 1.0;
        }

        if (fraction <= -1.0) {
            fraction = -1.0;
        }

        /* Calculate azimuth */
        azimuth = Math.acos(fraction);

        /* Reference it to True North */
        diff = dest_lon - src_lon;

        if (diff <= -config.PI) {
            diff += config.TWOPI;
        }

        if (diff >= config.PI) {
            diff -= config.TWOPI;
        }

        if (diff > 0.0) {
            azimuth = config.TWOPI - azimuth;
        }

        return (azimuth / config.DEG2RAD);
    }

    public static double haat(Site antenna, SplatEngine splat, Configuration config) {
        /* This function returns the antenna's Height Above Average
	   Terrain (HAAT) based on FCC Part 73.313(d).  If a critical
	   error occurs, such as a lack of SDF data to complete the
	   survey, -5000.0 is returned. */

        int azi, c;
        boolean error = false;
        double terrain, avg_terrain, haat, sum = 0.0;

        /* Calculate the average terrain between 2 and 10 miles
	   from the antenna site at azimuths of 0, 45, 90, 135,
	   180, 225, 270, and 315 degrees. */
        for (c = 0, azi = 0; azi <= 315 && !error; azi += 45) {
            terrain = averageTerrain(antenna, (double) azi, 2.0, 10.0, splat, config);

            if (terrain < -9998.0) /* SDF data is missing */ {
                error = true;
            }

            if (terrain > -4999.0) /* It's land, not water */ {
                sum += terrain;
                /* Sum of averages */
                c++;
            }
        }

        if (error) {
            return -5000.0;
        } else {
            avg_terrain = (sum / (double) c);
            haat = (antenna.getAlt() + getElevation(antenna,splat, config)) - avg_terrain;
            return haat;
        }
    }

    public static double averageTerrain(Site source, double azimuthx, double start_distance, double end_distance, SplatEngine splat, Configuration config) {
        /* This function returns the average terrain calculated in
	   the direction of "azimuth" (degrees) between "start_distance"
	   and "end_distance" (miles) from the source location.  If
	   the terrain is all water (non-critical error), -5000.0 is
	   returned.  If not enough SDF data has been loaded into
	   memory to complete the survey (critical error), then
	   -9999.0 is returned. */

        int c, samples, endpoint;
        double beta, lat1, lon1, lat2, lon2, num, den, azimuth, terrain = 0.0;
        Site destination;

        lat1 = source.getLat() * config.DEG2RAD;
        lon1 = source.getLon() * config.DEG2RAD;

        /* Generate a path of elevations between the source
	   location and the remote location provided. */
        beta = end_distance / 3959.0;

        azimuth = config.DEG2RAD * azimuthx;

        lat2 = Math.asin(Math.sin(lat1) * Math.cos(beta) + Math.cos(azimuth) * Math.sin(beta) * Math.cos(lat1));
        num = Math.cos(beta) - (Math.sin(lat1) * Math.sin(lat2));
        den = Math.cos(lat1) * Math.cos(lat2);

        if (azimuth == 0.0 && (beta > config.HALFPI - lat1)) {
            lon2 = lon1 + config.PI;
        } else if (azimuth == config.HALFPI && (beta > config.HALFPI + lat1)) {
            lon2 = lon1 + config.PI;
        } else if (Math.abs(num / den) > 1.0) {
            lon2 = lon1;
        } else {
            if ((config.PI - azimuth) >= 0.0) {
                lon2 = lon1 - arccos(num, den, config );
            } else {
                lon2 = lon1 + arccos(num, den, config);
            }
        }

        while (lon2 < 0.0) {
            lon2 += config.TWOPI;
        }

        while (lon2 > config.TWOPI) {
            lon2 -= config.TWOPI;
        }

        lat2 = lat2 / config.DEG2RAD;
        lon2 = lon2 / config.DEG2RAD;

        destination = new Site(null, null, lat2, lon2, 0);

        //destination.setLat(lat2);
        //destination.lon=lon2;

        /* If SDF data is missing for the endpoint of
	   the radial, then the average terrain cannot
	   be accurately calculated.  Return -9999.0 */
        if (getElevation(destination, splat, config) < -4999.0) {
            return (-9999.0);
        } else {
            Path path = readPath(source, destination, splat, config);

            endpoint = path.getLength();

            /* Shrink the length of the radial if the
		   outermost portion is not over U.S. land. */
            for (c = endpoint - 1; c >= 0 && path.getElevation()[c] == 0.0; c--);

            endpoint = c + 1;

            for (c = 0, samples = 0; c < endpoint; c++) {
                if (path.getDistance()[c] >= start_distance) {
                    terrain += (path.getElevation()[c] == 0.0 ? path.getElevation()[c] : path.getElevation()[c] + splat.getClutter());
                    samples++;
                }
            }

            if (samples == 0) {
                terrain = -5000.0;  /* No land */
            } else {
                terrain = (terrain / (double) samples);
            }

            return terrain;
        }
    }

    public static double elevationAngle(Site source, Site destination, SplatEngine splat, Configuration config) {
        /* This function returns the angle of elevation (in degrees)
	   of the destination as seen from the source location.
	   A positive result represents an angle of elevation (uptilt),
	   while a negative result represents an angle of depression
	   (downtilt), as referenced to a normal to the center of
	   the earth. */

        //register double a, b, dx;
        double a, b, dx;

        a = getElevation(destination, splat, config) + destination.getAlt() + config.EARTHRADIUS;
        b = getElevation(source, splat, config) + source.getAlt() + config.EARTHRADIUS;

        dx = 5280.0 * distance(source, destination, config);

        /* Apply the Law of Cosines */
        return ((180.0 * (Math.acos(((b * b) + (dx * dx) - (a * a)) / (2.0 * b * dx))) / config.PI) - 90.0);
    }

    public static double getElevation(Site location, SplatEngine splat, Configuration config) {
        /* This function returns the elevation (in feet) of any location
	   represented by the digital elevation model data in memory.
	   Function returns -5000.0 for locations not found in memory. */

        boolean found;
        int x = 0, y = 0, indx;
        double elevation;

        for (indx = 0, found = false; indx < config.MAXPAGES && !found;) {
            if(splat.getDem()[indx] != null){
            x = (int) Math.rint(splat.getPpd() * (location.getLat() - splat.getDem()[indx].getMinNorth()));
            y = splat.getMpi() - (int) Math.rint(splat.getPpd() * (lonDiff(splat.getDem()[indx].getMaxWest(), location.getLon())));

            if (x >= 0 && x <= splat.getMpi() && y >= 0 && y <= splat.getMpi()) {
                found = true;
            } else {
                indx++;
            }
        }
            else
            {
                indx++;
            }
        }

        if (found) {
            elevation = 3.28084 * splat.getDem()[indx].getData(x, y);
        } else {
            elevation = -5000.0;
        }

        return elevation;
    }

    public static double elevationAngle2(Site source, Site destination, double er, SplatEngine splat,Configuration config) {
        /* This function returns the angle of elevation (in degrees)
	   of the destination as seen from the source location, UNLESS
	   the path between the sites is obstructed, in which case, the
	   elevation angle to the first obstruction is returned instead.
	   "er" represents the earth radius. */

        int x;
        boolean block = false;
        double source_alt, destination_alt, cos_xmtr_angle,
                cos_test_angle, test_alt, elevation, distance,
                source_alt2, first_obstruction_angle = 0.0;
        Path temp;

        temp = readPath(source, destination, splat, config);

        distance = 5280.0 * distance(source, destination, config);
        source_alt = er + source.getAlt() + getElevation(source, splat, config);
        destination_alt = er + destination.getAlt() + getElevation(destination, splat, config);
        source_alt2 = source_alt * source_alt;

        /* Calculate the cosine of the elevation angle of the
	   destination (receiver) as seen by the source (transmitter). */
        cos_xmtr_angle = ((source_alt2) + (distance * distance) - (destination_alt * destination_alt)) / (2.0 * source_alt * distance);

        /* Test all points in between source and destination locations to
	   see if the angle to a topographic feature generates a higher
	   elevation angle than that produced by the destination.  Begin
	   at the source since we're interested in identifying the FIRST
	   obstruction along the path between source and destination. */
        for (x = 2, block = false; x < temp.getLength() && !block; x++) {
            distance = 5280.0 * temp.getDistance()[x];

            test_alt = splat.getEarthradius() + (temp.getElevation()[x] == 0.0 ? temp.getElevation()[x] : temp.getElevation()[x] + splat.getClutter());

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
                first_obstruction_angle = ((Math.acos(cos_test_angle)) / config.DEG2RAD) - 90.0;
            }
        }

        if (block) {
            elevation = first_obstruction_angle;
        } else {
            elevation = ((Math.acos(cos_xmtr_angle)) / config.DEG2RAD) - 90.0;
        }

        //path=temp;
        return elevation;
    }

    public static Path readPath(Site source, Site destination, SplatEngine splat, Configuration config) {
        Path path = new Path(config);
        /* This function generates a sequence of latitude and
	   longitude positions between source and destination
	   locations along a great circle path, and stores
	   elevation and distance information for points
	   along that path in the "path" structure. */

        int c;
        double azimuth, distance, lat1, lon1, beta, den, num,
                lat2, lon2, total_distance, dx, dy, path_length,
                miles_per_sample, samples_per_radian = 68755.0;
        Site tempsite;

        lat1 = source.getLat() * config.DEG2RAD;
        lon1 = source.getLon() * config.DEG2RAD;

        lat2 = destination.getLat() * config.DEG2RAD;
        lon2 = destination.getLon() * config.DEG2RAD;

        if (splat.getPpd() == 1200.0) {
            samples_per_radian = 68755.0;
        }

        if (splat.getPpd() == 3600.0) {
            samples_per_radian = 206265.0;
        }

        azimuth = azimuth(source, destination, config) * config.DEG2RAD;

        total_distance = distance(source, destination, config);

        if (total_distance > (30.0 / splat.getPpd())) /* > 0.5 pixel distance */ {
            dx = samples_per_radian * Math.acos(Math.cos(lon1 - lon2));
            dy = samples_per_radian * Math.acos(Math.cos(lat1 - lat2));

            path_length = Math.sqrt((dx * dx) + (dy * dy));
            /* Total number of samples */

            miles_per_sample = total_distance / path_length;
            /* Miles per sample */
        } else {
            c = 0;
            dx = 0.0;
            dy = 0.0;
            path_length = 0.0;
            miles_per_sample = 0.0;
            total_distance = 0.0;

            lat1 = lat1 / config.DEG2RAD;
            lon1 = lon1 / config.DEG2RAD;

            path.setLat(c, lat1);
            path.setLon(c, lon1);
            path.setElevation(c, getElevation(source, splat, config));
            path.setDistance(c, 0.0);
        }

        for (distance = 0.0, c = 0; (total_distance != 0.0 && distance <= total_distance && c < config.ARRAYSIZE); c++, distance = miles_per_sample * (double) c) {
            beta = distance / 3959.0;
            lat2 = Math.asin(Math.sin(lat1) * Math.cos(beta) + Math.cos(azimuth) * Math.sin(beta) * Math.cos(lat1));
            num = Math.cos(beta) - (Math.sin(lat1) * Math.sin(lat2));
            den = Math.cos(lat1) * Math.cos(lat2);

            if (azimuth == 0.0 && (beta > config.HALFPI - lat1)) {
                lon2 = lon1 + config.PI;
            } else if (azimuth == config.HALFPI && (beta > config.HALFPI + lat1)) {
                lon2 = lon1 + config.PI;
            } else if (Math.abs(num / den) > 1.0) {
                lon2 = lon1;
            } else {
                if ((config.PI - azimuth) >= 0.0) {
                    lon2 = lon1 - arccos(num, den, config);
                } else {
                    lon2 = lon1 + arccos(num, den, config);
                }
            }

            while (lon2 < 0.0) {
                lon2 += config.TWOPI;
            }

            while (lon2 > config.TWOPI) {
                lon2 -= config.TWOPI;
            }

            lat2 = lat2 / config.DEG2RAD;
            lon2 = lon2 / config.DEG2RAD;

            path.setLat(c, lat2);
            path.setLon(c, lon2);
            //tempsite.lat=lat2;
            //tempsite.lon=lon2;
            path.setElevation(c, getElevation(new Site(null, null, lat2, lon2, 0), splat, config));
            path.setDistance(c, distance);

        }

        /* Make sure exact destination point is recorded at path.length-1 */
        if (c < config.ARRAYSIZE) {
            path.setLat(c, destination.getLat());
            path.setLon(c, destination.getLon());
            path.setElevation(c, getElevation(destination, splat, config));
            path.setDistance(c, total_distance);
            c++;
        }

        if (c < config.ARRAYSIZE) {
            path.setLength(c);
        } else {
            path.setLength(config.ARRAYSIZE - 1);
        }
        return path;
    }

}
