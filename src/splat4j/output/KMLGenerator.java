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
public class KMLGenerator {
    Configuration config;
    SplatEngine splat;
    
    public KMLGenerator(Configuration config, SplatEngine splat)
    {
        this.config = config;
        this.splat = splat;
    }
    
    public void WriteKML(Site source, Site destination)
{
    boolean block;
    FileWriter repWri = null;
        try {
            int	x, y;
            double	distance, rx_alt, tx_alt, cos_xmtr_angle,
                    azimuth, cos_test_angle, test_alt;
            Path path = Utils.readPath(source,destination, splat, config);
            String report_name = String.format("%s-to-%s.kml",source.getName(),destination.getName());
            for (char ch : new char[]{32, 17, 92, 42, 47}) {
                report_name = report_name.replace(ch, '_');
            }   File reportFile = new File(report_name);
            repWri = new FileWriter(reportFile);
            repWri.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            repWri.write("<kml xmlns=\"http://earth.google.com/kml/2.0\">\n");
            repWri.write(String.format("<!-- Generated by %s Version %s -->\n",splat.getName(), splat.getVersion()));
            repWri.write("<Folder>\n");
            repWri.write("<name>SPLAT! Path</name>\n");
            repWri.write("<open>1</open>\n");
            repWri.write(String.format("<description>Path Between %s and %s</description>\n",source.getName(),destination.getName()));
            repWri.write("<Placemark>\n");
            repWri.write(String.format("    <name>%s</name>\n",source.getName()));
            repWri.write("    <description>\n");
            repWri.write("       Transmit Site\n");
            if (source.getLat()>=0.0)
                repWri.write(String.format("       <BR>%s North</BR>\n",Utils.dec2dms(source.getLat())));
            else
                repWri.write(String.format("       <BR>%s South</BR>\n",Utils.dec2dms(source.getLat())));
            repWri.write(String.format("       <BR>%s West</BR>\n",Utils.dec2dms(source.getLon())));
            azimuth=Utils.azimuth(source,destination, config);
            distance=Utils.distance(source,destination, config);
            if (splat.isMetric())
                repWri.write(String.format("       <BR>%.2f km",distance*config.KM_PER_MILE));
            else
                repWri.write(String.format("       <BR>%.2f miles",distance));
            repWri.write(String.format(" to %s</BR>\n       <BR>toward an azimuth of %.2f%c</BR>\n",destination.getName(),azimuth,176));
            repWri.write("    </description>\n");
            repWri.write("    <visibility>1</visibility>\n");
            repWri.write("    <Style>\n");
            repWri.write("      <IconStyle>\n");
            repWri.write("        <Icon>\n");
            repWri.write("          <href>root://icons/palette-5.png</href>\n");
            repWri.write("          <x>224</x>\n");
            repWri.write("          <y>224</y>\n");
            repWri.write("          <w>32</w>\n");
            repWri.write("          <h>32</h>\n");
            repWri.write("        </Icon>\n");
            repWri.write("      </IconStyle>\n");
            repWri.write("    </Style>\n");
            repWri.write("    <Point>\n");
            repWri.write("      <extrude>1</extrude>\n");
            repWri.write("      <altitudeMode>relativeToGround</altitudeMode>\n");
            repWri.write(String.format("      <coordinates>%f,%f,30</coordinates>\n",(source.getLon()<180.0?-source.getLon():360.0-source.getLon()),source.getLat()));
            repWri.write("    </Point>\n");
            repWri.write("</Placemark>\n");
            repWri.write("<Placemark>\n");
            repWri.write(String.format("    <name>%s</name>\n",destination.getName()));
            repWri.write("    <description>\n");
            repWri.write("       Receive Site\n");
            if (destination.getLat()>=0.0)
                repWri.write(String.format("       <BR>%s North</BR>\n",Utils.dec2dms(destination.getLat())));
            else
                repWri.write(String.format("       <BR>%s South</BR>\n",Utils.dec2dms(destination.getLat())));
            repWri.write(String.format("       <BR>%s West</BR>\n",Utils.dec2dms(destination.getLon())));
            if (splat.isMetric())
                repWri.write(String.format("       <BR>%.2f km",distance*config.KM_PER_MILE));
            else
                repWri.write(String.format("       <BR>%.2f miles",distance));
            repWri.write(String.format(" to %s</BR>\n       <BR>toward an azimuth of %.2f%c</BR>\n",source.getName(),Utils.azimuth(destination,source, config),176));
            repWri.write("    </description>\n");
            repWri.write("    <visibility>1</visibility>\n");
            repWri.write("    <Style>\n");
            repWri.write("      <IconStyle>\n");
            repWri.write("        <Icon>\n");
            repWri.write("          <href>root://icons/palette-5.png</href>\n");
            repWri.write("          <x>224</x>\n");
            repWri.write("          <y>224</y>\n");
            repWri.write("          <w>32</w>\n");
            repWri.write("          <h>32</h>\n");
            repWri.write("        </Icon>\n");
            repWri.write("      </IconStyle>\n");
            repWri.write("    </Style>\n");
            repWri.write("    <Point>\n");
            repWri.write("      <extrude>1</extrude>\n");
            repWri.write("      <altitudeMode>relativeToGround</altitudeMode>\n");
            repWri.write(String.format("      <coordinates>%f,%f,30</coordinates>\n",(destination.getLon()<180.0?-destination.getLon():360.0-destination.getLon()),destination.getLat()));
            repWri.write("    </Point>\n");
            repWri.write("</Placemark>\n");
            repWri.write("<Placemark>\n");
            repWri.write("<name>Point-to-Point Path</name>\n");
            repWri.write("  <visibility>1</visibility>\n");
            repWri.write("  <open>0</open>\n");
            repWri.write("  <Style>\n");
            repWri.write("    <LineStyle>\n");
            repWri.write("      <color>7fffffff</color>\n");
            repWri.write("    </LineStyle>\n");
            repWri.write("    <PolyStyle>\n");
            repWri.write("       <color>7fffffff</color>\n");
            repWri.write("    </PolyStyle>\n");
            repWri.write("  </Style>\n");
            repWri.write("  <LineString>\n");
            repWri.write("    <extrude>1</extrude>\n");
            repWri.write("    <tessellate>1</tessellate>\n");
            repWri.write("    <altitudeMode>relativeToGround</altitudeMode>\n");
            repWri.write("    <coordinates>\n");
            for (x=0; x<path.getLength(); x++)
                repWri.write(String.format("      %f,%f,5\n",(path.getLon(x)<180.0?-path.getLon(x):360.0-path.getLon(x)),path.getLat(x)));
            repWri.write("    </coordinates>\n");
            repWri.write("   </LineString>\n");
            repWri.write("</Placemark>\n");
            repWri.write("<Placemark>\n");
            repWri.write("<name>Line-of-Sight Path</name>\n");
            repWri.write("  <visibility>1</visibility>\n");
            repWri.write("  <open>0</open>\n");
            repWri.write("  <Style>\n");
            repWri.write("    <LineStyle>\n");
            repWri.write("      <color>ff00ff00</color>\n");
            repWri.write("    </LineStyle>\n");
            repWri.write("    <PolyStyle>\n");
            repWri.write("       <color>7f00ff00</color>\n");
            repWri.write("    </PolyStyle>\n");
            repWri.write("  </Style>\n");
            repWri.write("  <LineString>\n");
            repWri.write("    <extrude>1</extrude>\n");
            repWri.write("    <tessellate>1</tessellate>\n");
            repWri.write("    <altitudeMode>relativeToGround</altitudeMode>\n");
            repWri.write("    <coordinates>\n");
            /* Walk across the "path", indentifying obstructions along the way */
            
            for (y=0; y<path.getLength(); y++)
            {
                distance=5280.0*path.getDistance()[y];
                tx_alt=config.EARTHRADIUS+source.getAlt()+path.getElevation()[0];
                rx_alt=config.EARTHRADIUS+destination.getAlt()+path.getElevation()[y];
                
                /* Calculate the cosine of the elevation of the
                transmitter as seen at the temp rx point. */
                
                cos_xmtr_angle=((rx_alt*rx_alt)+(distance*distance)-(tx_alt*tx_alt))/(2.0*rx_alt*distance);
                
                for (x=y, block=false; x>=0 && !block; x--)
                {
                    distance=5280.0*(path.getDistance()[y]-path.getDistance()[x]);
                    test_alt=config.EARTHRADIUS+path.getElevation()[x];
                    
                    cos_test_angle=((rx_alt*rx_alt)+(distance*distance)-(test_alt*test_alt))/(2.0*rx_alt*distance);
                    
                    /* Compare these two angles to determine if
                    an obstruction exists.  Since we're comparing
                    the cosines of these angles rather than
                    the angles themselves, the following "if"
                    statement is reversed from what it would
                    be if the actual angles were compared. */
                    
                    if (cos_xmtr_angle>=cos_test_angle)
                        block=true;
                }
                
                if (block)
                    repWri.write(String.format("      %f,%f,-30\n",(path.getLon(y)<180.0?-path.getLon(y):360.0-path.getLon(y)),path.getLat(y)));
                else
                    repWri.write(String.format("      %f,%f,5\n",(path.getLon(y)<180.0?-path.getLon(y):360.0-path.getLon(y)),path.getLat(y)));
            }   repWri.write("    </coordinates>\n");
            repWri.write("  </LineString>\n");
            repWri.write("</Placemark>\n");
            repWri.write("    <LookAt>\n");
            repWri.write(String.format("      <longitude>%f</longitude>\n",(source.getLon()<180.0?-source.getLon():360.0-source.getLon())));
            repWri.write(String.format("      <latitude>%f</latitude>\n",source.getLat()));
            repWri.write("      <range>300.0</range>\n");
            repWri.write("      <tilt>45.0</tilt>\n");
            repWri.write(String.format("      <heading>%f</heading>\n",azimuth));
            repWri.write("    </LookAt>\n");
            repWri.write("</Folder>\n");
            repWri.write("</kml>\n");
            System.out.printf("\nKML file written to: \"%s\"",report_name);
        } catch (IOException ex) {
            Logger.getLogger(KMLGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                repWri.close();
            } catch (IOException ex) {
                Logger.getLogger(KMLGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

}
}
