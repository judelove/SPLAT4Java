/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splat4j;

import splat4j.input.QTHFile;

/**
 *
 * @author Jude Mukundane
 */
public class Site {

    private double lat;
    private double lon;
    private float alt;
    private String name;
    private String filename; 
    
    public Site(QTHFile file)
    {
        this.lat = file.getLat();
        this.lon = file.getLon();
        this.name = file.getName();
        this.filename = file.getFilename();
        this.alt = file.getAlt();
    }
    
    public Site(String name, String filename, double lat, double lon, float alt)
    {
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.filename = filename;
        this.alt = alt;
    }
    /**
     * @return the lat
     */
    public double getLat() {
        return lat;
    }

    /**
     * @return the lon
     */
    public double getLon() {
        return lon;
    }

    /**
     * @return the alt
     */
    public double getAlt() {
        return alt;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

   
}
