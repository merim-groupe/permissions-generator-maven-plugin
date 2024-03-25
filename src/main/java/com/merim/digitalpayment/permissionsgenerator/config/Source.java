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
public class Source {

    /**
     * The Package name.
     */
    private String fileName;

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
        if (this.directory == null) {
            throw new MojoExecutionException(key + ".directory is missing.");
        } else {
            final File file = new File(this.directory);

            if (!file.exists()) {
                throw new MojoExecutionException(key + ".directory \"" + this.directory + "\" no such file or directory.");
            }
        }

        if (this.fileName == null) {
            throw new MojoExecutionException(key + ".fileName is missing.");
        } else {
            final File file = this.getSourceFile();

            if (!file.exists()) {
                throw new MojoExecutionException(key + ".fileName \"" + this.fileName + "\" no such file or directory.");
            }
        }
    }

    /**
     * Gets source file.
     *
     * @return the source file
     */
    public File getSourceFile() {
        return new File(this.directory, this.fileName);
    }
}
