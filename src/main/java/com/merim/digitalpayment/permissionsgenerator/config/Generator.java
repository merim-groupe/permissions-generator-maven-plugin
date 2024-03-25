package com.merim.digitalpayment.permissionsgenerator.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Generator.
 *
 * @author Pierre Adam
 * @since 24.03.25
 */
@Getter
@Setter
@ToString
public class Generator {

    /**
     * The Source.
     */
    private Source source;

    /**
     * The Target.
     */
    private Target target;

    /**
     * Validate.
     *
     * @throws MojoExecutionException the mojo execution exception
     */
    public void validate(final String key) throws MojoExecutionException {
        if (this.source == null) {
            throw new MojoExecutionException(key + ".source is missing.");
        } else {
            this.source.validate(key + ".source");
        }

        if (this.target == null) {
            throw new MojoExecutionException(key + ".target is missing.");
        } else {
            this.target.validate(key + ".target");
        }
    }
}
