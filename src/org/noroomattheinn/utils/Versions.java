/*
 * Versions.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Oct 27, 2013
 */

package org.noroomattheinn.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

/**
 * Versions: Represents a list of release versions of something (like a java application). 
 * The overall list of versions has a set of release notes that are referenced
 * by a URL. There is also a Release object for each release which indicates
 * it's version number, the date on which it was released, and the URL where
 * it can be accessed.
 * 
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

@XmlRootElement(name = "versions")
public class Versions {
    @XmlElement private URL releaseNotes;
    @XmlElement(name = "release", type = Release.class)
                private List<Release> releases = new ArrayList<>();

    public static class Release {
        @XmlElement private String number;
        @XmlElement private Date date;
        @XmlElement private URL url;
        @XmlElement private URL urlForMac;
        @XmlElement private URL urlForWindows;
        @XmlElement private Boolean experimental = false;
        
        public String getReleaseNumber() { return number; }
        public Date getReleaseDate() { return date; }
        public URL getReleaseURL() { return url; }
        public URL getMacURL() { return urlForMac; }
        public URL getWindowsURL() { return urlForWindows; }
        public Boolean getExperimental() { return experimental; }
    }
    
    public List<Release> getReleases() { return releases; }
    public URL getReleaseNotes() { return releaseNotes; }
    
    public static Versions getVersionInfo(String versionsFileURL) {
        Versions versions = null;
        
        try {
            URL url = new URL(versionsFileURL);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            JAXBContext jc = JAXBContext.newInstance(Versions.class);
            Unmarshaller jaxbUnmarshaller = jc.createUnmarshaller();
            versions = (Versions)jaxbUnmarshaller.unmarshal(in);
        } catch (IOException|JAXBException ex) {
            Logger.getLogger(Versions.class.getName()).warning("Problem parsing versions file: " + ex);
        }

        return versions;
    }
    
    public static void main(String[] args) throws Exception {
        Versions versions = Versions.getVersionInfo(
              //"https://dl.dropboxusercontent.com/u/7045813/VisibleTesla/versions.xml");
                "https://dl.dropboxusercontent.com/u/7045813/VTExtras/test_versions.xml");
        
        
        if (versions == null) {
            Logger.getLogger(Versions.class.getName()).log(
                    Level.INFO, "Unable to get version information");
        } else {
            for (Release r : versions.getReleases()) {
                System.out.println("Version: " + r.getReleaseNumber());
                System.out.println("Mac URL: " + r.getMacURL());
                System.out.println("Windows URL: " + r.getWindowsURL());
                System.out.println("IsExperimental: " + r.getExperimental());
            }

            JAXBContext jc = JAXBContext.newInstance(Versions.class);
            JAXBElement<Versions> je2 = new JAXBElement<>(new QName("versions"), Versions.class, versions);
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(je2, System.out);
        }
    }
}
