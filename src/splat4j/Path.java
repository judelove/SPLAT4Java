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
public class Path {

    private Configuration config;
    private double[] lat, lon, elevation, distance;
    private int length;

    Path(Configuration config) {
        this.config = config;
        lat = new double[config.ARRAYSIZE];
        lon = new double[config.ARRAYSIZE];
        elevation = new double[config.ARRAYSIZE];
        distance = new double[config.ARRAYSIZE];
    }

    /**
     * @return the config
     */
    public Configuration getConfig() {
        return config;
    }

    /**
     * @return the lat
     */
    public double getLat(int index) {
        return lat[index];
    }

    /**
     * @return the lon
     */
    public double getLon(int index) {
        return lon[index];
    }

    /**
     * @return the elevation
     */
    public double[] getElevation() {
        return elevation;
    }

    /**
     * @return the distance
     */
    public double[] getDistance() {
        return distance;
    }

    /**
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(Configuration config) {
        this.config = config;
    }

    /**
     * @param lat the lat to set
     */
    public void setLat(int index, double lat) {
        this.lat[index] = lat;
    }

    /**
     * @param lon the lon to set
     */
    public void setLon(int index, double lon) {
        this.lon[index] = lon;
    }

    /**
     * @param elevation the elevation to set
     */
    public void setElevation(int index, double elevation) {
        this.elevation[index] = elevation;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(int index, double distance) {
        this.distance[index] = distance;
    }

    /**
     * @param length the length to set
     */
    public void setLength(int length) {
        this.length = length;
    }

}
