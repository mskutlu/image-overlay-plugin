/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.bonitasoft.studio.maven.plugin.exception.CreateImageException;

/**
 * @author Romain Bioteau
 * @goal generate
 * @phase generate-resources
 */
@Mojo(defaultPhase = LifecyclePhase.GENERATE_RESOURCES, name = "create-image")
public class SetImageVersionMojo extends AbstractMojo {

    @Parameter(required = true)
    private String baseImgPath;

    @Parameter(required = true)
    private String outputImagePath;

    @Parameter(required = true)
    private String outputImageFormat;

    @Parameter(required = true)
    private int xLocation;

    @Parameter(required = true)
    private int yLocation;
    
    @Parameter(required = false)
    private int qualifierX;

    @Parameter(required = false)
    private int qualifierY;

    @Parameter(required = true)
    private String versionLabel;

    @Parameter(required = false)
    private String fontName;

    @Parameter(required = false)
    private String fontResourcePath;

    @Parameter(required = false)
    private float fontSize;

    @Parameter(required = false)
    private String color;

    @Parameter(required = false, defaultValue = "false")
    private boolean showQualifier;

    @Parameter(required = false, defaultValue = "true")
    private boolean bold;

    @Parameter(required = false, defaultValue = "false")
    private boolean italic;

    /*
     * (non-Javadoc)
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final SetImageVersion setImageVersion = createSetImageVersion();
        setImageVersion.setBaseImgPath(baseImgPath);
        setImageVersion.setOutputImageFormat(outputImageFormat);
        setImageVersion.setOutputImagePath(outputImagePath);

        final String formattedVersion = format(versionLabel);
        setImageVersion.setVerisonLabel(formattedVersion);
        if (showQualifier && hasQualifier(versionLabel)) {
            setImageVersion.setQualifierLabel(qualifier(versionLabel));
            setImageVersion.setQualifierX(qualifierX);
            setImageVersion.setQualifierY(qualifierY);
        }
        setImageVersion.setxLocation(xLocation);
        setImageVersion.setyLocation(yLocation);
        setImageVersion.setBold(bold);
        setImageVersion.setItalic(italic);
        if (fontName != null) {
            if (fontResourcePath == null) {
                throw new MojoFailureException("You must provide a custom font resourcefile when using a custom font name.");
            }
            setImageVersion.setFontName(fontName);
            setImageVersion.setFontResourcePath(fontResourcePath);
        }

        if (fontSize > 0) {
            setImageVersion.setSize(fontSize);
        }
        if (color != null && !color.isEmpty()) {
            if (!color.startsWith("#")) {
                color = "#" + color;
            }
            if (color.length() > 7) {
                throw new MojoFailureException("Color parameter has not the expected format eg: 0f125e");
            }
            setImageVersion.setColor(color);
        }

        getLog().info("Writing image with version " + versionLabel + " to " + outputImagePath + "...");

        try {
            setImageVersion.createImage();
        } catch (final CreateImageException e) {
            throw new MojoExecutionException("Failed to create target image", e);
        }
    }

    private String qualifier(String version) {
        if (hasQualifier(version)) {
            final String[] versions = version.split("\\.");
            return versions[3];
        }
        throw new IllegalArgumentException(String.format("Version has no qualifier: %s", version));
    }

    private boolean hasQualifier(String version) {
        final String[] versions = version.split("\\.");
        return versions.length >= 4;
    }

    private String format(String version) {
        if (version != null) {
            final String[] versions = version.split("\\.");
            if(versions.length < 3){
                throw new IllegalArgumentException(String.format("Invalid version format: %s", version));
            }
            String newVersion = versions[0] + "." + versions[1];
            String maintenanceVersion = versions[2];
            if (maintenanceVersion.endsWith("-SNAPSHOT")) {
                maintenanceVersion = maintenanceVersion.substring(0, maintenanceVersion.indexOf("-SNAPSHOT"));
            }
            //Version contains a tag id
            if (maintenanceVersion.indexOf(".") != -1) {
                final String[] splitTag = maintenanceVersion.split("\\.");
                maintenanceVersion = splitTag[0];
                if (showQualifier) {

                }
            }
            newVersion = newVersion + "." + maintenanceVersion;
            return newVersion;
        }
        return version;
    }

    public void setVersionLabel(String versionLabel) {
        this.versionLabel = versionLabel;
    }

    public void setShowQualifier(boolean showQualifier) {
        this.showQualifier = showQualifier;
    }

    protected SetImageVersion createSetImageVersion() {
        return new SetImageVersion();
    }

}
