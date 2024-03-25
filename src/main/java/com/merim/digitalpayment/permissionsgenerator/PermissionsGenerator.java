package com.merim.digitalpayment.permissionsgenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.merim.digitalpayment.permissionsgenerator.config.Generator;
import lombok.NonNull;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.burningwave.core.classes.ClassSourceGenerator;
import org.burningwave.core.classes.TypeDeclarationSourceGenerator;
import org.burningwave.core.classes.UnitSourceGenerator;
import org.burningwave.core.classes.VariableSourceGenerator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Stack;

/**
 * PermissionsGenerator.
 *
 * @author Pierre Adam
 * @since 24.03.25
 */
public class PermissionsGenerator {

    /**
     * The Log.
     */
    private final Log log;

    /**
     * The Generator.
     */
    private final Generator generator;

    /**
     * Instantiates a new Permissions generator.
     *
     * @param log       the log
     * @param generator the generator
     */
    public PermissionsGenerator(final Log log, final Generator generator) {
        this.log = log;
        this.generator = generator;
    }

    /**
     * Process.
     *
     * @throws MojoExecutionException the mojo execution exception
     * @throws MojoFailureException   the mojo failure exception
     */
    public void process() throws MojoExecutionException, MojoFailureException {
        final JsonNode jsonNode = this.extractConfig();

        this.createClass(jsonNode);
    }

    /**
     * Extract config json node.
     *
     * @return the json node
     * @throws MojoExecutionException the mojo execution exception
     * @throws MojoFailureException   the mojo failure exception
     */
    private JsonNode extractConfig() throws MojoExecutionException, MojoFailureException {
        final Path filePath = Paths.get(this.generator.getSource().getDirectory(), this.generator.getSource().getFileName());
        final File configFile = filePath.toFile();
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        mapper.findAndRegisterModules();

        if (!configFile.exists()) {
            throw new MojoExecutionException("No such file or directory : " + filePath);
        }

        final JsonNode jsonNode;
        try {
            jsonNode = mapper.readTree(configFile);
        } catch (final IOException e) {
            throw new MojoFailureException("Invalid configuration file : " + filePath, e);
        }

        return jsonNode;
    }

    /**
     * Create class.
     *
     * @param config the config
     * @throws MojoFailureException the mojo failure exception
     */
    private void createClass(@NonNull final JsonNode config) throws MojoFailureException {
        this.log.info("Generating " + this.generator.getTarget().getClassName());
        final UnitSourceGenerator unitSourceGenerator = this.createPermissions(config);

        unitSourceGenerator.storeToClassPath(this.generator.getTarget().getDirectory());
    }

    /**
     * Create permissions unit source generator.
     *
     * @param config the config
     * @return the unit source generator
     * @throws MojoFailureException the mojo failure exception
     */
    private UnitSourceGenerator createPermissions(@NonNull final JsonNode config) throws MojoFailureException {
        final UnitSourceGenerator unitGenerator = UnitSourceGenerator.create(this.generator.getTarget().getPackageName());

        unitGenerator.addClass(this.createClass(this.generator.getTarget().getClassName(), config));

        return unitGenerator;
    }

    /**
     * Create class source generator.
     *
     * @param className the class name
     * @param config    the config
     * @return the class source generator
     * @throws MojoFailureException the mojo failure exception
     */
    private ClassSourceGenerator createClass(@NonNull final String className,
                                             @NonNull final JsonNode config) throws MojoFailureException {
        final ClassSourceGenerator classSource = ClassSourceGenerator
                .create(TypeDeclarationSourceGenerator.create(className))
                .addModifier(Modifier.PUBLIC);

        if (config.isObject()) {
            for (final Map.Entry<String, JsonNode> entry : config.properties()) {
                classSource.addInnerClass(this.createClass(entry.getKey(), new Stack<>(), entry.getValue()));
            }
        } else {
            throw new MojoFailureException("Invalid configuration file " + this.generator.getSource().getSourceFile().getPath());
        }

        return classSource;
    }

    /**
     * Create class source generator.
     *
     * @param className the class name
     * @param hierarchy the hierarchy
     * @param config    the config
     * @return the class source generator
     * @throws MojoFailureException the mojo failure exception
     */
    private ClassSourceGenerator createClass(@NonNull final String className,
                                             @NonNull final Stack<String> hierarchy,
                                             @NonNull final JsonNode config) throws MojoFailureException {
        final ClassSourceGenerator classSource = ClassSourceGenerator
                .create(TypeDeclarationSourceGenerator.create(className))
                .addModifier(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);

        hierarchy.push(className);
        this.addName(classSource, hierarchy);

        if (config.isObject()) {
            for (final Map.Entry<String, JsonNode> entry : config.properties()) {
                if (Constants.SELF_REFERENCE.equals(entry.getKey())) {
                    final JsonNode value = entry.getValue();
                    if (value.isTextual()) {
                        this.applyCRUDLevel(classSource, hierarchy, value.asText());
                    } else {
                        throw new MojoFailureException("Invalid value.");
                    }
                } else {
                    classSource.addInnerClass(this.createClass(entry.getKey(), hierarchy, entry.getValue()));
                }
            }
        } else {
            this.applyCRUDLevel(classSource, hierarchy, config.asText());
        }

        hierarchy.pop();

        return classSource;
    }

    /**
     * Add name.
     *
     * @param classSource the class source
     * @param hierarchy   the hierarchy
     */
    private void addName(@NonNull final ClassSourceGenerator classSource,
                         @NonNull final Stack<String> hierarchy) {
        final String value = "\"" + String.join(Constants.HIERARCHY_SEPARATOR, hierarchy) + "\"";
        classSource.addField(
                VariableSourceGenerator.create(String.class, "name")
                        .addModifier(Modifier.PUBLIC)
                        .addModifier(Modifier.STATIC)
                        .addModifier(Modifier.FINAL)
                        .useType(String.class)
                        .setValue(value));
    }

    /**
     * Apply crud level.
     *
     * @param classSource the class source
     * @param hierarchy   the hierarchy
     * @param levelValue  the level value
     * @throws MojoFailureException the mojo failure exception
     */
    private void applyCRUDLevel(@NonNull final ClassSourceGenerator classSource,
                                @NonNull final Stack<String> hierarchy,
                                @NonNull final String levelValue) throws MojoFailureException {
        try {
            CRUDActions.valueOf(levelValue).applyTo(classSource, hierarchy);
        } catch (final IllegalArgumentException e) {
            throw new MojoFailureException("Invalid CRUD level: " + levelValue);
        }
    }
}
