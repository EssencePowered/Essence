/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.io;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import io.github.nucleuspowered.nucleus.services.interfaces.INucleusTextTemplateFactory;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * Handles loading and reading text files.
 */
public final class TextFileController {

    private static final TextComponent padding = TextComponent.of("-", NamedTextColor.GOLD);

    private static final List<Charset> characterSetsToTest = Lists.newArrayList(
        StandardCharsets.UTF_8,
        StandardCharsets.ISO_8859_1,
        StandardCharsets.US_ASCII,
        StandardCharsets.UTF_16
    );

    /**
     * The internal {@link Asset} that represents the default file.
     */
    @Nullable private final Asset asset;

    /**
     * Holds the file location.
     */
    private final Path fileLocation;

    /**
     * Holds the file information.
     */
    private final List<String> fileContents = Lists.newArrayList();

    /**
     * Holds the {@link NucleusTextTemplateImpl} information.
     */
    private final List<NucleusTextTemplateImpl> textTemplates = Lists.newArrayList();
    private final boolean getTitle;
    private final INucleusTextTemplateFactory textTemplateFactory;

    private long fileTimeStamp = 0;
    @Nullable private NucleusTextTemplate title;

    public TextFileController(final INucleusTextTemplateFactory textTemplateFactory, final Path fileLocation, final boolean getTitle) throws IOException {
        this(textTemplateFactory, null, fileLocation, getTitle);
    }

    public TextFileController(final INucleusTextTemplateFactory textTemplateFactory, @Nullable final Asset asset, final Path fileLocation) throws IOException {
        this(textTemplateFactory, asset, fileLocation, false);
    }

    private TextFileController(
            final INucleusTextTemplateFactory textTemplateFactory, @Nullable final Asset asset, final Path fileLocation, final boolean getTitle) throws IOException {
        this.textTemplateFactory = textTemplateFactory;
        this.asset = asset;
        this.fileLocation = fileLocation;
        this.getTitle = getTitle;
        this.load();
    }

    /**
     * Loads the file and refreshes the contents of the file in memory.
     *
     * @throws IOException Thrown if there is an issue getting the file.
     */
    public void load() throws IOException {
        if (this.asset != null && !Files.exists(this.fileLocation)) {
            // Create the file
            this.asset.copyToFile(this.fileLocation);
        }

        final List<String> fileContents = Lists.newArrayList();

        // Load the file into the list.
        MalformedInputException exception = null;
        for (final Charset charset : characterSetsToTest) {
            try {
                fileContents.addAll(Files.readAllLines(this.fileLocation, charset));
                exception = null;
                break;
            } catch (final MalformedInputException ex) {
                exception = ex;
            }
        }

        // Rethrow exception if it doesn't work.
        if (exception != null) {
            throw exception;
        }

        this.fileTimeStamp = Files.getLastModifiedTime(this.fileLocation).toMillis();
        this.fileContents.clear();
        this.fileContents.addAll(fileContents);
        this.textTemplates.clear();
    }

    public Optional<TextComponent> getTitle(final Audience source) {
        if (this.getTitle && this.textTemplates.isEmpty() && !this.fileContents.isEmpty()) {
            // Initialisation!
            this.getFileContentsAsText();
        }

        if (this.title != null) {
            return Optional.of(this.title.getForSource(source));
        }

        return Optional.empty();
    }

    public List<TextComponent> getTextFromNucleusTextTemplates(final Audience source) {
        return this.getFileContentsAsText().stream().map(x -> x.getForObject(source)).collect(Collectors.toList());
    }

    public void sendToPlayer(final CommandSource src, final TextComponent title) {

        final PaginationList.Builder pb = Util.getPaginationBuilder(src).contents(this.getTextFromNucleusTextTemplates(src));

        if (title != null && !title.isEmpty()) {
            pb.title(title).padding(padding);
        } else {
            pb.padding(Util.SPACE);
        }

        pb.sendTo(src);
    }

    /**
     * Gets the contents of the file.
     *
     * @return An {@link ImmutableList} that contains the file contents.
     */
    private ImmutableList<NucleusTextTemplateImpl> getFileContentsAsText() {
        this.checkFileStamp();
        if (this.textTemplates.isEmpty()) {
            final List<String> contents = Lists.newArrayList(this.fileContents);
            if (this.getTitle) {
                this.title = this.getTitleFromStrings(contents);

                if (this.title != null) {
                    contents.remove(0);

                    final Iterator<String> i = contents.iterator();
                    while (i.hasNext()) {
                        final String n = i.next();
                        if (n.isEmpty() || n.matches("^\\s+$")) {
                            i.remove();
                        } else {
                            break;
                        }
                    }
                }
            }

            contents.forEach(x -> this.textTemplates.add(this.textTemplateFactory.createFromAmpersandString(x)));
        }

        return ImmutableList.copyOf(this.textTemplates);
    }

    @Nullable private NucleusTextTemplate getTitleFromStrings(final List<String> info) {
        if (!info.isEmpty()) {
            String sec1 = info.get(0);
            if (sec1.startsWith("#")) {
                // Get rid of the # and spaces, then limit to 50 characters.
                sec1 = sec1.replaceFirst("#\\s*", "");
                if (sec1.length() > 50) {
                    sec1 = sec1.substring(0, 50);
                }

                return this.textTemplateFactory.createFromAmpersandString(sec1);
            }
        }

        return null;
    }

    private void checkFileStamp() {
        try {
            if (this.fileContents.isEmpty() || Files.getLastModifiedTime(this.fileLocation).toMillis() > this.fileTimeStamp) {
                this.load();
            }
        } catch (final IOException e) {
            // ignored
        }
    }
}
