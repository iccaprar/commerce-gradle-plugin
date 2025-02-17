package mpern.sap.commerce.build.util;

import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version> {
    private static final Pattern NEW_VERSION = Pattern.compile("(\\d\\d)(\\d\\d)(\\.([1-9]?\\d))?");
    private static final Pattern PREVIEW_VERSION = Pattern.compile("(\\d\\d)(\\d\\d)\\.FP([1-9]?\\d)?");
    private static final Pattern OLD_VERSION = Pattern.compile("(\\d)\\.(\\d)\\.(\\d)(\\.([1-9]?\\d))?");
    public static final int UNDEFINED_PART = Integer.MAX_VALUE;
    public static final Version UNDEFINED = new Version(UNDEFINED_PART, UNDEFINED_PART, UNDEFINED_PART, UNDEFINED_PART,
            "<undefined>");
    public static final Comparator<Version> VERSION_COMPARATOR = Comparator.comparingInt(Version::getMajor)
            .thenComparingInt(Version::getMinor).thenComparingInt(Version::getRelease)
            .thenComparingInt(Version::getPatch);

    private final int major;
    private final int minor;
    private final int release;
    private final int patch;
    private final String original;

    private final boolean preview;

    private Version(int major, int minor, int release, int patch, String original) {
        this.major = major;
        this.minor = minor;
        this.release = release;
        this.patch = patch;
        this.original = original;
        this.preview = false;
    }

    private Version(int major, int minor, boolean preview, String original) {
        this.major = major;
        this.minor = minor;
        this.preview = preview;
        this.release = 0;
        this.patch = Integer.MIN_VALUE;
        this.original = original;
    }

    public static Version parseVersion(String versionString) {
        Objects.requireNonNull(versionString);

        Matcher oldV = OLD_VERSION.matcher(versionString);
        Matcher newV = NEW_VERSION.matcher(versionString);
        Matcher previewV = PREVIEW_VERSION.matcher(versionString);

        if (previewV.matches()) {
            return new Version(Integer.parseInt(previewV.group(1)), Integer.parseInt(previewV.group(2)), true,
                    versionString);
        }
        if (newV.matches()) {
            int patch = UNDEFINED_PART;

            if (newV.groupCount() > 3 && newV.group(4) != null) {
                patch = Integer.parseInt(newV.group(4));
            }
            return new Version(Integer.parseInt(newV.group(1)), Integer.parseInt(newV.group(2)), 0, patch,
                    versionString);
        } else if (oldV.matches()) {
            int patch = UNDEFINED_PART;
            if (oldV.groupCount() > 4 && oldV.group(5) != null) {
                patch = Integer.parseInt(oldV.group(5));
            }
            return new Version(Integer.parseInt(oldV.group(1)), Integer.parseInt(oldV.group(2)),
                    Integer.parseInt(oldV.group(3)), patch, versionString);
        }
        String[] split = versionString.split("\\.");
        int major = UNDEFINED_PART, minor = UNDEFINED_PART, release = UNDEFINED_PART, patch = UNDEFINED_PART;
        switch (split.length) {
        case 4:
            patch = Integer.parseInt(split[3]);
        case 3:
            release = Integer.parseInt(split[2]);
        case 2:
            minor = Integer.parseInt(split[1]);
        case 1:
            major = Integer.parseInt(split[0]);
            break;
        default:
            throw new IllegalArgumentException("Could not parse " + versionString);
        }
        return new Version(major, minor, release, patch, versionString);
    }

    public Version withoutPatch() {
        return new Version(major, minor, release, UNDEFINED_PART, original);
    }

    @Override
    public int compareTo(Version o) {
        if ((this.isPreview() || o.isPreview()) && this.original.equalsIgnoreCase(o.original)) {
            return 0;
        }
        return VERSION_COMPARATOR.compare(this, o);
    }

    public boolean equalsIgnorePatch(Version version) {
        if (this == version) {
            return true;
        }
        if (this.isPreview() || version.isPreview()) {
            return false;
        }
        return major == version.major && minor == version.minor && release == version.release;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Version version = (Version) o;
        if ((this.isPreview() || version.isPreview()) && this.original.equalsIgnoreCase(version.original)) {
            return true;
        }
        return major == version.major && minor == version.minor && release == version.release && patch == version.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, release, patch, preview);
    }

    @Override
    public String toString() {
        return this.preview ? this.original + " (PREVIEW)" : this.original;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getRelease() {
        return release;
    }

    public int getPatch() {
        return patch;
    }

    public String getDependencyVersion() {
        String v = this.original;
        if (this.getPatch() == UNDEFINED_PART) {
            if (!v.endsWith(".")) {
                v += ".";
            }
            v += "+";
        }
        return v;
    }

    public boolean isPreview() {
        return preview;
    }
}
