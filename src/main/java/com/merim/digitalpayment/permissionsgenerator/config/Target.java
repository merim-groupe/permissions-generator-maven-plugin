package com.merim.digitalpayment.permissionsgenerator.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;

/**
 * Target.
 *
 * @author Pierre Adam
 * @since 24.03.25
 */
@Getter
@Setter
@ToString
public class Target {

    /**
     * The Property.
     */
    private String className;

    /**
     * The Package name.
     */
    private String packageName;

    /**
     * The Directory.
     */
    private String directory;

    /**
     * Validate.
     *
     * @param key the key
     * @throws MojoExecutionException the mojo execution exception
     */
    public void validate(final String key) throws MojoExecutionException {
        if (this.className == null) {
            throw new MojoExecutionException(key + "className is missing.");
        }

        if (this.packageName == null) {
            throw new MojoExecutionException(key + "packageName is missing.");
        }

        if (this.directory == null) {
            throw new MojoExecutionException(key + ".directory is missing.");
        } else {
            final File file = new File(this.directory);

            if (!file.exists()) {
                throw new MojoExecutionException(key + ".directory \"" + this.directory + "\" no such file or directory.");
            }
        }
    }
}
