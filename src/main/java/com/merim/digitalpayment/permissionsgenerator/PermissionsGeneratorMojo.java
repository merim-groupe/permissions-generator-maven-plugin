package com.merim.digitalpayment.permissionsgenerator;

import com.merim.digitalpayment.permissionsgenerator.config.Generator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

/**
 * PermissionsGeneratorMojo.
 *
 * @author Pierre Adam
 * @since 24.03.25
 */
@Mojo(name = "permissions-generator", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class PermissionsGeneratorMojo extends AbstractMojo {

    /**
     * The Generators.
     */
    @Parameter(property = "generators", required = true)
    List<Generator> generators;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (this.generators != null && !this.generators.isEmpty()) {
            int idx = 0;
            for (final Generator generatorConfig : this.generators) {
                generatorConfig.validate("Generator[" + idx + "]");
                final PermissionsGenerator permissionsGenerator = new PermissionsGenerator(this.getLog(), generatorConfig);
                permissionsGenerator.process();
                idx++;
            }
        } else {
            this.getLog().warn("Nothing to generate !");
        }
    }
}