/*
 * Created on 31.05.2004
 * 
 * Copyright (c) 2006, Manuel Alabor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary 
 * form must reproduce the above copyright notice, this list of conditions and 
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of Manuel Alabor nor the names 
 * of its contributors may be used to endorse or promote products derived from 
 * this software without specific prior written permission.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.msites.guilibrary.toolbox;

import java.awt.Canvas;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Der GUIImageManager verwaltet alle GUI-spezifischen Bilder.<br>
 * Er stellt sicher dass kein Bild zweimal geladen wird, indem er intern einen
 * Cache verwaltet.<br>
 * <br>
 * Der GUIImageManager implementiert (zum grössten Teil) das Singleton-Design-
 * Pattern. Per <code>getInstance</code> wird die aktuelle Instanz angefordert.<br>
 * Bevor dies jedoch getan werden kann, muss der GUIImageManager per
 * <code>loadImageManager(String)</code> initialisiert werden. Mit dem Parameter
 * "homePath" wird dem GUIImageManager das Verzeichnis angegeben, in welchem
 * sich alle Bilder der Applikation befinden.<br>
 * Somit kann beim codieren später lediglich der Dateiname des gewünschten Bildes
 * angegeben werden, und es ist nicht nötig, den gesamten, absoluten Pfad anzugeben.<br>
 * Trotzdem ist der GUIImageManager aber in der Lage, absolute Pfade zu verarbeiten.
 * <br>
 * <br>
 * Version 5.0: Arbeitet jetzt mit CompatibleImages.<br>
 * Version 5.2: Auf BufferedImages umgestellt; getURL() kann Fehler printen<br>
 * <br>
 * Beispiele:<br>
 *  GUIImageManager.loadImageManager("/gui/core/images/");<br>
 *  Image relativeImage = GUIImageManager.getInstance().getImage("logo.png");<br>
 *  Image absolutImage = GUIImageManager.getInstance().getImage("/manu/gui/components/icon.png"); 
 * 
 * @author Manuel Alabor
 * @version 5.2
 */
public class GUIImageManager {

    /* Instanz: */
    private static GUIImageManager imageManager = null;
    
    /* Eigenschaften: */
    private String homePath;
    
    /* Objekte: */
	private Hashtable<String, BufferedImage> imageCache;
	
	
	// Singleton-Implementierung -----------------------------------------------
	/**
	 * Liefert die interne GUIImageManager-Instanz.
	 */
	public static GUIImageManager getInstance() {
	    return imageManager;
	}
    
	
	// Methoden zum Laden von Bildern ------------------------------------------
	/**
	 * Standartkonstruktor
	 * 
	 * @param homePath Pfad des Ordners, in welchem sich alle Bilder der Applikation befinden
	 */
	private GUIImageManager(String homePath) {
		/* homePath prüfen: */
	    // Sicherstellen, dass sich am Ende von homePath ein "/" befindet:
		if(homePath.charAt(homePath.length()-1) != '/') {
		    homePath = homePath + '/';
		}
	    
		/* Setzen: */
	    this.imageCache = new Hashtable<String, BufferedImage>();
		this.homePath = homePath;
	}
	
	/**
	 * Lädt ein Bild und liefert dieses zurück.<br>
	 * Wurde ein Bild bereits geladen (es befindet sich dann in der Hashtable
	 * images), wird dieses nicht erneut geladen, sondern das entsprechende Bild
	 * wird aus der Hashtable gelesen und zurückgegeben.
	 * 
	 * @param imageName
	 * @return Image
	 */
	public BufferedImage getImage(String imageName) {
	    /* Absoluter Pfad?: */
	    // Ist zu Beginn des Bildnamens ein "/" angegeben, so handelt es sich um
	    // einen absoluten Pfad.
	    // Fehlt dieses Zeichen, so wird relativePath dem Bildnamen hinzugefügt.
	    String path = imageName;
	    if(path.charAt(0) != '/') {
	        path = homePath + imageName;  // relativer Pfad
	    }
	    
	    /* Bild laden: */
	    if(imageCache.containsKey(path)) {
			return imageCache.get(path);
		} else {
            BufferedImage image = loadImage(getURL(path));
            
			imageCache.put(path, image);  // Bild speichern
			return image;				 // Bild zurückgeben
		}
	}
    
    
	
	/**
	 * Liefert ein Bild.<br>
	 * Im Gegensatz zu den anderen getImage*-Methoden speichert diese Version
	 * hier das Bild anschliessend nicht in den Cache.
	 * 
	 * @param imageName
	 * @return
	 */
	public BufferedImage getUnchachedImage(String imageName) {
	    String path = imageName;
	    if(path.charAt(0) != '/') {
	        path = homePath + imageName;  // relativer Pfad
	    }
	    
	    if(imageCache.containsKey(path)) {
	        return imageCache.get(path);
	    } else {
	        BufferedImage image = loadImage(getURL(path));
	        return image;
	    }
	}

	/**
	 * Benutzt die Methode getImage(...) um ein Bild zu laden/holen.<br>
	 * Das Bild wird jedoch nicht als normales Image-Objekt zurückgegeben, sondern
	 * wird als ImageIcon ausgeliefert.<br>
	 * <br>
	 * ACHTUNG! imageName ist relativ zum Standort von GUIImageManager!
	 * 
	 * @param imageName
	 * @return ImageIcon
	 */
	public ImageIcon getImageIcon(String imageName) {
		Image image = getImage(imageName);
		ImageIcon icon;
		
		try {
		    icon = new ImageIcon(image);
		} catch(Exception e) {
		    icon = null;
		    e.printStackTrace();
		}
		
		return icon;
	}
	
	/**
	 * Diese Überladung von getImage ermöglicht während dem Laden des Bildes einen
	 * MediaTracker zu verwenden.<br>
	 * Hiermit wird sichergestellt dass das Bild nicht erst vor dem ersten anzeigen
	 * nachgeladen wird, sondern sofort im Speicher zur Verfügung steht.<br>
	 * <br>
	 * ACHTUNG! imageName ist relativ zum Standort von GUIImageManager!
	 * 
	 * @param imageName
	 * @param boolean Tracker verwenden?
	 * @return Image
	 */
	public Image getImage(String imageName, boolean useTracker) {
	    /* Prüfung: */
	    // Wenn das gewünschte Bild bereits geladen ist, den Tracker NIEMALS
	    // benutzen (auch wenn dies aufgrund des übergebenen Parameters ge-
	    // wünscht sein sollte).
	    if(imageCache.containsKey(imageName)) {
			useTracker = false;
		}
	    
	    /* Bild holen: */
	    Image image = getImage(imageName);
	    
		/* Tracker: */
		if(useTracker) {
			// Per MediaTracker laden:
			MediaTracker tracker = new MediaTracker(new Canvas());
			tracker.addImage(image, 1);
			try {
				tracker.waitForAll();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		/* Bild zurückgeben: */
		return image;
	}
    
    
    
    
	
    
    // Hilfsmethoden -----------------------------------------------------------
	/**
     * Lädt Bilder in den Cache, ohne irgendetwas zurückzugeben.<br>
     * Die Pfade werden in einem <code>String</code>-Array übergeben.
     * 
     * @param images
     * @see #preloadImage(String)
     * @see #getImage(String, boolean)
	 */
    public void preloadImages(String[] images) {
        for(int i = 0, l = images.length; i < l; i++) {
            // Im folgenden Aufruf muss der Tracker verwendet werden, da das
            // Image ansonsten nicht "vorgeladen" wird.
            getInstance().getImage(images[i], true);
        }
    }
    
    /**
     * Analog zu <code>preloadImages(String[])</code> lädt diese Methode nur
     * ein Bild in den Cache, anstatt einer ganzen Sammlung.
     * 
     * @param image
     * @see #preloadImages(String)
     */
    public void preloadImage(String image) {
        preloadImages(new String[]{image});
    }
    
    /**
	 * Liefert einen URL welcher sich aus der Position des GUIImageManagers sowie
	 * dem Namen eines zu ladenden Bildes zusammensetzt.
	 * 
	 * @param imageName
	 * @return
	 */
	private URL getURL(String imageName) {
	    // Uncomment following line for debugging:
//	     System.out.println(" -> try to get \"" + imageName + "\" as URL");
        URL url = this.getClass().getResource(imageName);
        if(url == null) System.err.println("GUIImageManager: \"Imagefile [" + imageName + "] not found\"");
        
	    return url;
	}
    
    /**
     * Lädt die interne GUIImageManager-Instanz.<br>
     * Beispiel: /net/msites/xbmcconfig/gui/res/
     * 
     * @param homePath
     */
    public static void loadImageManager(String homePath) {
        imageManager = new GUIImageManager(homePath);
    }
    
    
    
    private static BufferedImage loadImage(URL url) {
        BufferedImage tmp = null;
        try {
            tmp = ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedImage compatibleImage = GUIHelper.toCompatibleImage(tmp);
        
        return compatibleImage;
    }
    
    // Getter-Methoden ---------------------------------------------------------
    /**
     * Prüft ob die Methode <code>loadImageManager</code> aufgerufen, und somit
     * die interne GUIImageManager-Instanz geladen wurde.
     * 
     * @return
     */
    public static boolean isLoaded() {
        if(imageManager == null) {
            return false;
        } else {
            return true;
        }

    }
		
}
