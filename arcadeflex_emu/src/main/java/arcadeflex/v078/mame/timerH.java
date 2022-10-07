/*
 * ported to v0.78
 * 
 */
package arcadeflex.v078.mame;

//mame imports
import static arcadeflex.v078.mame.timer.*;

public class timerH {

    public static double TIME_IN_HZ(double hz) {
        return 1.0 / hz;
    }

    public static double TIME_IN_CYCLES(double c, int cpu) {
        return ((double) (c) * cycles_to_sec[cpu]);
    }

    public static double TIME_IN_SEC(double s) {
        return s;
    }

    public static double TIME_IN_MSEC(double ms) {
        return ((double) (ms) * (1.0 / 1000.0));
    }

    public static double TIME_IN_USEC(double us) {
        return ((double) (us) * (1.0 / 1000000.0));
    }

    public static double TIME_IN_NSEC(double us) {
        return ((double) (us) * (1.0 / 1000000000.0));
    }

    public static final double TIME_NOW = 0.0;
    public static final double TIME_NEVER = 1.0e30;

    public static int TIME_TO_CYCLES(int cpu, double t) {
        return ((int) ((t) * sec_to_cycles[cpu]));
    }
}
