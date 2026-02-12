package exec;

/**
 * La classe Mapile permet l'execution de la machine a pile virtuelle MAPILE
 *  
 * @author Girard, Masson, Perraudeau
 */

import libMapile.*;

public class Mapile {

    public static void main(String[] args) {
        if (args.length == 1) {
            ExecMapile.activer(args[0]);
        } else if (args.length == 2 && args[1].equals("--trace")) {
            ExecMapile.activer(args[0] + "*");
        } else {
            ExecMapile.activer();
        }
    }

}
