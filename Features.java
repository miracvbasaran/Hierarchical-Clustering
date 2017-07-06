/**
 * Created by Mirac Vuslat Basaran on 06-Jul-17.
 */
public abstract class Features {
    private static byte[][] features;

    public static byte[][] getFeatures() {
        return features;
    }

    public static void setFeatures(byte[][] features) {
        Features.features = features;
    }
}
