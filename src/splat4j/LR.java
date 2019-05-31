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
public class LR {
                private double eps_dielect; 
		private double sgm_conductivity; 
		private double eno_ns_surfref;
		private double frq_mhz; 
		private double conf; 
		private double rel;
		private double erp;
		private int radio_climate;  
		private int pol;
                private boolean got_azimuth_pattern = false, got_elevation_pattern = false;
		private float[][] antenna_pattern = new float[361][1001];
                private double[][] azimuth_pattern = new double[361][1001];
                private double[][] elevation_pattern = new double[361][1001];
                
                public LR()
                {}

    /**
     * @return the eps_dielect
     */
    public double getEps_dielect() {
        return eps_dielect;
    }

    /**
     * @return the sgm_conductivity
     */
    public double getSgm_conductivity() {
        return sgm_conductivity;
    }

    /**
     * @return the eno_ns_surfref
     */
    public double getEno_ns_surfref() {
        return eno_ns_surfref;
    }

    /**
     * @return the frq_mhz
     */
    public double getFrq_mhz() {
        return frq_mhz;
    }

    /**
     * @return the conf
     */
    public double getConf() {
        return conf;
    }

    /**
     * @return the rel
     */
    public double getRel() {
        return rel;
    }

    /**
     * @return the erp
     */
    public double getErp() {
        return erp;
    }

    /**
     * @return the radio_climate
     */
    public int getRadio_climate() {
        return radio_climate;
    }

    /**
     * @return the pol
     */
    public int getPol() {
        return pol;
    }

    /**
     * @return the antenna_pattern
     */
    public float[][] getAntenna_pattern() {
        return antenna_pattern;
    }

    /**
     * @param antenna_pattern the antenna_pattern to set
     */
    public void setAntenna_pattern(int x, int y, float antenna_pattern) {
        this.antenna_pattern[x][y] = antenna_pattern;
    }

    /**
     * @param eps_dielect the eps_dielect to set
     */
    public void setEps_dielect(double eps_dielect) {
        this.eps_dielect = eps_dielect;
    }

    /**
     * @param sgm_conductivity the sgm_conductivity to set
     */
    public void setSgm_conductivity(double sgm_conductivity) {
        this.sgm_conductivity = sgm_conductivity;
    }

    /**
     * @param eno_ns_surfref the eno_ns_surfref to set
     */
    public void setEno_ns_surfref(double eno_ns_surfref) {
        this.eno_ns_surfref = eno_ns_surfref;
    }

    /**
     * @param frq_mhz the frq_mhz to set
     */
    public void setFrq_mhz(double frq_mhz) {
        this.frq_mhz = frq_mhz;
    }

    /**
     * @param conf the conf to set
     */
    public void setConf(double conf) {
        this.conf = conf;
    }

    /**
     * @param rel the rel to set
     */
    public void setRel(double rel) {
        this.rel = rel;
    }

    /**
     * @param erp the erp to set
     */
    public void setErp(double erp) {
        this.erp = erp;
    }

    /**
     * @param radio_climate the radio_climate to set
     */
    public void setRadio_climate(int radio_climate) {
        this.radio_climate = radio_climate;
    }

    /**
     * @param pol the pol to set
     */
    public void setPol(int pol) {
        this.pol = pol;
    }

    /**
     * @return the got_azimuth_pattern
     */
    public boolean isGot_azimuth_pattern() {
        return got_azimuth_pattern;
    }

    /**
     * @param got_azimuth_pattern the got_azimuth_pattern to set
     */
    public void setGot_azimuth_pattern(boolean got_azimuth_pattern) {
        this.got_azimuth_pattern = got_azimuth_pattern;
    }

    /**
     * @return the got_elevation_pattern
     */
    public boolean isGot_elevation_pattern() {
        return got_elevation_pattern;
    }

    /**
     * @param got_elevation_pattern the got_elevation_pattern to set
     */
    public void setGot_elevation_pattern(boolean got_elevation_pattern) {
        this.got_elevation_pattern = got_elevation_pattern;
    }

    
    /**
     * @return the elevation_pattern
     */
    public double[][] getElevation_pattern() {
        return elevation_pattern;
    }

    /**
     * @param elevation_pattern the elevation_pattern to set
     */
    public void setElevation_pattern(int x, int y, double elevation_pattern) {
        this.elevation_pattern[x][y] = elevation_pattern;
    }
                
}
