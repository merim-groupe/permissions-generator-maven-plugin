package com.merim.digitalpayment.permissionsgenerator;

import lombok.NonNull;
import org.burningwave.core.classes.ClassSourceGenerator;
import org.burningwave.core.classes.VariableSourceGenerator;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * Level.
 *
 * @author Pierre Adam
 * @since 24.03.25
 */
public enum CRUDActions {

    /**
     * Read level.
     */
    read,

    /**
     * Update level.
     */
    update(CRUDActions.read),

    /**
     * Create level.
     */
    create(CRUDActions.read, CRUDActions.update),

    /**
     * Delete level.
     */
    delete(CRUDActions.read, CRUDActions.update, CRUDActions.create);

    /**
     * The Implicit levels.
     */
    private final List<CRUDActions> implicitLevels;

    /**
     * Instantiates a new Level.
     *
     * @param implicitLevels the implicit levels
     */
    CRUDActions(final CRUDActions... implicitLevels) {
        this.implicitLevels = Arrays.asList(implicitLevels);
    }

    /**
     * Apply to.
     *
     * @param classSource the class source
     * @param hierarchy   the hierarchy
     * @param level       the level
     */
    private static void applyTo(@NonNull final ClassSourceGenerator classSource,
                                @NonNull final Stack<String> hierarchy,
                                @NonNull final CRUDActions level) {
        final String value = "\"" + String.join(Constants.HIERARCHY_SEPARATOR, hierarchy) +
                Constants.ACTION_SEPARATOR + level.name() + "\"";
        classSource.addField(
                VariableSourceGenerator.create(String.class, level.name())
                        .addModifier(Modifier.PUBLIC)
                        .addModifier(Modifier.STATIC)
                        .addModifier(Modifier.FINAL)
                        .useType(String.class)
                        .setValue(value));
    }

    /**
     * Apply to.
     *
     * @param classSource the class source
     * @param hierarchy   the hierarchy
     */
    public void applyTo(@NonNull final ClassSourceGenerator classSource,
                        @NonNull final Stack<String> hierarchy) {
        for (final CRUDActions level : this.implicitLevels) {
            CRUDActions.applyTo(classSource, hierarchy, level);
        }
        CRUDActions.applyTo(classSource, hierarchy, this);
    }
}
