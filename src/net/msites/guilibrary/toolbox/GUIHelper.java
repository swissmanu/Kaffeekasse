/*
 * Created on 23.05.2005
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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;

/**
 * Sammlung aus verschiedenen GUI-spezifischen Hilfsmethoden.
 * 
 * @author Manuel Alabor
 * @version 1.5
 */
public class GUIHelper {

    /**
     * Zentriert die Component <code>toCenter</code> auf <code>owner</code>.<br>
     * Praktisch, um z.B. einen <code>JDialog</code> auf einem <code>JFrame</code>
     * zu zentrieren.
     * 
     * @param toCenter
     * @param owner
     */
    public static void centerOnOwner(Component toCenter, Component owner) {
		Dimension ownerSize = owner.getSize();
		Point ownerLocation = owner.getLocation();
		Dimension toCenterSize = toCenter.getSize();
		
		int posX = new Double(((ownerSize.getWidth() - toCenterSize.getWidth()) / 2) + ownerLocation.getX()).intValue();
		int posY = new Double(((ownerSize.getHeight() - toCenterSize.getHeight()) / 2) + ownerLocation.getY()).intValue();
		
		toCenter.setLocation(posX, posY);
    }
    
    /**
     * Zentriert die Component <code>toCenter</code> auf dem Bildschirm.<br>
     * Praktisch, um z.B. ein Fenster auf dem Bildschirm zu zentrieren.
     * 
     * @param toCenter
     */
    public static void centerOnScreen(Component toCenter) {
        Dimension paneSize = toCenter.getSize();
        Dimension screenSize = toCenter.getToolkit().getScreenSize();
        
        int posX = (screenSize.width - paneSize.width) / 2;
        int posY = (screenSize.height - paneSize.height) / 2; 
        
        toCenter.setLocation(posX, posY);
    }
    
    /**
     * Erstellt aus einem <code>BufferedImage</code> ein Systemkompatibles
     * <code>BufferedImage</code>. Performance solcher Bilder ist höher beim
     * Zeichnen.
     * 
     * @param image
     * @return
     */
    public static BufferedImage toCompatibleImage(BufferedImage image) {
        GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice d = e.getDefaultScreenDevice();
        GraphicsConfiguration c = d.getDefaultConfiguration();
        
        BufferedImage compatibleImage = c.createCompatibleImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics g = compatibleImage.getGraphics();
        
        g.drawImage(image, 0, 0, null);
        g.dispose();
        
        return compatibleImage;
    }
    
    /**
     * Aktiviert das JGoodies Looks L'n'F.<br/>
     * Benötigt das entsprechende JAR im ClassPath!
     * 
     * @see PlasticXPLookAndFeel
     */
    public static void useJGoodiesLooks() {
        try {
            UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
            UIManager.put("jgoodies.popupDropShadowEnabled", Boolean.TRUE);
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Ersetzt die GlassPane von <code>frame</code> mit <code>newGlassPane</code>.</br>
     * Wird als <code>newGlassPane</code> null übergeben, wird lediglich die
     * aktuelle GlassPane entfernt.
     * 
     * @param frame
     * @param newGlassPane
     */
    public static void replaceGlassPane(JFrame frame, Component newGlassPane) {
        Component oldGlassPane = frame.getGlassPane();
        if(oldGlassPane != null) oldGlassPane.setVisible(false);
        
        if(newGlassPane != null) {
            frame.setGlassPane(newGlassPane);
            newGlassPane.setVisible(true);
        }
    }
    
    
    public static BufferedImage createReflectedImage(BufferedImage input, int length) {
        return createReflectedImage(input, 0.4f, 1f, 1, length);
    }    
    
    public static BufferedImage createReflectedImage(BufferedImage input, float startOpacity, float endOpacity, int distance, int length) {
        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();

        BufferedImage gradient = createGradientMask(inputWidth, inputHeight, startOpacity, endOpacity, length);
        BufferedImage buffer = createReflection(input, inputWidth, inputHeight, distance, length);
        
        /* Maske drüberzeichnen: */
        Graphics2D g2 = buffer.createGraphics();
        g2.setComposite(AlphaComposite.DstOut);
        g2.drawImage(gradient, null, 0, inputHeight);
        g2.dispose();
        
        return buffer;
    }
    
    private static BufferedImage createGradientMask(int inputWidth, int inputHeight, float startOpacity, float endOpacity, int length) {
        BufferedImage gradient = new BufferedImage(inputWidth, inputHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = gradient.createGraphics();
        GradientPaint painter = new GradientPaint(0.0f, 0.0f,
                                                  new Color(1.0f, 1.0f, 1.0f, startOpacity),
                                                  0.0f, length,
                                                  new Color(1.0f, 1.0f, 1.0f, endOpacity));
        g.setPaint(painter);
        g.fill(new Rectangle2D.Double(0, 0, inputWidth, inputHeight));
        
        g.dispose();
        gradient.flush();

        return gradient;
    }
    
    private static BufferedImage createReflection(BufferedImage input, int inputWidth, int inputHeight, int distance, int length) {
        int totalHeight = inputHeight + distance + length;
        int y = (inputHeight << 1) + distance;
        
        BufferedImage buffer = createEmptyCanvas(inputWidth, totalHeight);
        Graphics2D g = buffer.createGraphics();
        
        g.drawImage(input, null, null);
        g.translate(0, y);
        
        AffineTransform reflectTransform = AffineTransform.getScaleInstance(1.0, -1.0);
        g.drawImage(input, reflectTransform, null);
        g.translate(0, -y);
        
        g.dispose();
        
        return buffer;
    }

    
    public static BufferedImage createEmptyCanvas(int width, int height) {
        GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice d = e.getDefaultScreenDevice();
        GraphicsConfiguration gc = d.getDefaultConfiguration();
        BufferedImage image = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        
        return image;
    }
    
    /**
     * Erstellt aus einer Bilddatei ein Thumbnail und verwendet den Interpolations-
     * Modus Bilinear zur Verkleinerung.
     * 
     * @param file
     * @param width
     * @param height
     * @return
     */
    public static BufferedImage createThumbnail(File file, int width, int height) {
        return createThumbnail(file, width, height, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }
    
    /**
     * Erstellt aus einer Bilddatei ein Thumbnail.
     * 
     * @param file
     * @param width
     * @param height
     * @param mode RenderingHints.KEY_INTERPOLATION
     * @return
     * @see #createThumbnail(File, int, int)
     */
    public static BufferedImage createThumbnail(File file, int width, int height, Object mode) {
        BufferedImage thumbnail = null;
        
        try {
            BufferedImage input = ImageIO.read(file);
            thumbnail = createThumbnail(input, width, height, mode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return thumbnail;
    }
    
    /**
     * Erstellt aus einem Bild ein Thumbnail und vernwendet den Interpolations-
     * Modus Bilinear zur Verkleinerung.
     * 
     * @param image
     * @param width
     * @param height
     * @return
     */
    public static BufferedImage createThumbnail(BufferedImage image, int width, int height) {
        return createThumbnail(image, width, height, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }
    
    /**
     * Erstellt aus einem Bild ein Thumbnail.
     * 
     * @param image
     * @param width
     * @param height
     * @param mode RenderingHints.KEY_INTERPOLATION
     * @return
     * @see #createThumbnail(BufferedImage, int, int)
     */
    public static BufferedImage createThumbnail(BufferedImage image, int width, int height, Object mode) {
        /* Thumbnail erstellen: */
        BufferedImage thumbnail = null;
        
        int origWidth = image.getWidth();
        int origHeight = image.getHeight();
        int thumbWidth = width;
        int thumbHeight = height;
        if(origWidth > origHeight) {
            thumbHeight = origHeight * width / origWidth;
        } else {
            thumbWidth = origWidth * height / origHeight;
        }
        
        thumbnail = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D)thumbnail.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, mode);
        
        g.drawImage(image, 0,0, thumbWidth,thumbHeight, null);
        g.dispose();
        
        return thumbnail;
    }
    
    
	public static JButton createListButton(Action action) {
		JButton btnButton = new JButton(action);
		
		btnButton.setToolTipText(btnButton.getText());
		btnButton.setText("");
		btnButton.setBorderPainted(false);
		btnButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnButton.setFocusable(false);
		btnButton.setHorizontalAlignment(SwingConstants.LEFT);
		
		return btnButton;
	}
    
    
    
    
    
    
    
    
}
