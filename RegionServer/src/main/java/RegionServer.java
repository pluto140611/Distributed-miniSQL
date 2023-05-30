import RegionManagers.RegionManager;

import javax.swing.plaf.synth.Region;

public class RegionServer {
    public static void main (String[] args) throws Exception{
        RegionManager regionManager = new RegionManager();
        regionManager.start();

    }
}
