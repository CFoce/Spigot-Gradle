/**
 *
 * Copyright (C) 2017  HexagonMc <https://github.com/HexagonMC>
 * Copyright (C) 2017  Zartec <zartec@mccluster.eu>
 *
 *     This file is part of Spigot-Gradle.
 *
 *     Spigot-Gradle is free software:
 *     you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Spigot-Gradle is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Spigot-Gradle.
 *     If not, see <http://www.gnu.org/licenses/>.
 */
package eu.hexagonmc.spigot.gradle.meta;

import eu.hexagonmc.spigot.annotation.meta.PluginMetadata;
import eu.hexagonmc.spigot.annotation.meta.PluginYml;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileTree;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;

public class GenerateMetadataTask extends DefaultTask {

    private Path _targetSpigot;
    private Path _targetBungee;

    private boolean _mergeMetadata = true;

    private Path _metadataFileSpigot;
    private Path _metadataFileBungee;
    private Supplier<PluginMetadata> _supplierSpigot;
    private Supplier<PluginMetadata> _supplierBungee;

    /**
     * Enables or disables the merging of available {@link PluginMetadata}
     * sources.
     * 
     * @param state True to enable false otherwise
     */
    public void setMergeMetadata(boolean state) {
        _mergeMetadata = state;
    }

    /**
     * Sets an supplier that returns an existing spigot {@link PluginMetadata}.
     * 
     * @param supplier The supplier
     */
    public void setSupplierSpigot(Supplier<PluginMetadata> supplier) {
        _supplierSpigot = supplier;
    }

    /**
     * Returns the supplied {@link PluginMetadata} from the supplier set by
     * {@link #setSupplierSpigot(Supplier)}.
     * 
     * @return The {@link PluginMetadata}
     */
    @Internal
    private PluginMetadata getMetadataSpigot() {
        return _supplierSpigot.get();
    }

    /**
     * Sets an supplier that returns an existing bungee {@link PluginMetadata}.
     * 
     * @param supplier The supplier
     */
    public void setSupplierBungee(Supplier<PluginMetadata> supplier) {
        _supplierBungee = supplier;
    }

    /**
     * Returns the supplied {@link PluginMetadata} from the supplier set by
     * {@link #setSupplierBungee(Supplier)}.
     * 
     * @return The {@link PluginMetadata}
     */
    @Internal
    private PluginMetadata getMetadataBungee() {
        return _supplierBungee.get();
    }

    /**
     * Gets the {@link Path} for the generated spigot {@link PluginMetadata}.
     * 
     * @return The {@link Path}
     */
    @Internal
    public Path getTargetSpigot() {
        if (_targetSpigot == null) {
            return _targetSpigot = getTemporaryDir().toPath().resolve(PluginYml.FILENAME_SPIGOT);
        }
        return _targetSpigot;
    }

    /**
     * Gets the {@link Path} for the generated bungee {@link PluginMetadata}.
     * 
     * @return The {@link Path}
     */
    @Internal
    public Path getTargetBungee() {
        if (_targetBungee == null) {
            return _targetBungee = getTemporaryDir().toPath().resolve(PluginYml.FILENAME_BUNGEE);
        }
        return _targetBungee;
    }

    /**
     * Gets the @{@link Path} to the spigot {@link PluginMetadata} resolved by
     * {@link #findExtraMetadataFiles(SourceSet)}.
     * 
     * @return The {@link Path}
     */
    @Internal
    public Path getMetadataFileSpigot() {
        return _metadataFileSpigot;
    }

    /**
     * Gets the @{@link Path} to the bungee {@link PluginMetadata} resolved by
     * {@link #findExtraMetadataFiles(SourceSet)}.
     * 
     * @return The {@link Path}
     */
    @Internal
    public Path getMetadataFileBungee() {
        return _metadataFileBungee;
    }

    /**
     * {@inheritDoc}.
     */
    @TaskAction
    void generateMetadata() throws IOException {
        JavaPluginConvention java = getProject().getConvention().getPlugin(JavaPluginConvention.class);
        findExtraMetadataFiles(java.getSourceSets().getByName("main"));

        PluginMetadata metaDataSpigot = getMetadataSpigot();
        if (_mergeMetadata && _metadataFileSpigot != null) {
            PluginMetadata metaData = PluginYml.read(_metadataFileSpigot);
            if (metaData.getName().equals(metaDataSpigot.getName())) {
                metaDataSpigot.accept(metaData);
            }
        }

        PluginYml.write(getTargetSpigot(), metaDataSpigot);

        PluginMetadata metaDataBungee = getMetadataBungee();
        if (_mergeMetadata && _metadataFileBungee != null) {
            PluginMetadata metaData = PluginYml.read(_metadataFileBungee);
            if (metaData.getName().equals(metaDataBungee.getName())) {
                metaDataBungee.accept(metaData);
            }
        }

        PluginYml.write(getTargetBungee(), metaDataBungee);
    }

    /**
     * Searches in projects resources for existing plugin.yml und bungee.yml
     * files.
     * 
     * @param sourceSet The {@link SourceSet} to search in
     */
    private void findExtraMetadataFiles(SourceSet sourceSet) {
        FileTree files;
        files = sourceSet.getResources()
                .matching(filterable -> filterable.include(PluginYml.FILENAME_SPIGOT));
        if (!files.isEmpty()) {
            _metadataFileSpigot = files.getSingleFile().toPath();
        }
        files = sourceSet.getResources()
                .matching(filterable -> filterable.include(PluginYml.FILENAME_BUNGEE));
        if (!files.isEmpty()) {
            _metadataFileBungee = files.getSingleFile().toPath();
        }
    }
}
