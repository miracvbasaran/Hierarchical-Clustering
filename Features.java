/**
 * Created by Mirac Vuslat Basaran on 06-Jul-17.
 */
public abstract class Features {
    private static byte[][] features;
    public static int[] docSizes;
    public static int[] overheads;

    public static byte[][] getFeatures() {
        return features;
    }

    public static void setFeatures(byte[][] features) {
        Features.features = features;
        docSizes = new int[features.length];
        overheads = new int[features.length];
    }
}
