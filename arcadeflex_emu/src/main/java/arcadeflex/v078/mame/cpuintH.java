package arcadeflex.v078.mame;

//mame imports
import static arcadeflex.v078.mame.cpuint.*;

public class cpuintH {

    /**
     * *************************************************************************
     *
     * cpuint.h
     *
     * Core multi-CPU interrupt engine.
     *
     **************************************************************************
     */
    /**
     * ***********************************
     *
     * Interrupt constants
     *
     ************************************
     */
    public static final int INTERRUPT_NONE = 126;/* generic "none" vector */

    /**
     * ***********************************
     *
     * Interrupt handling
     *
     ************************************
     */

    /* macro for handling NMI lines */
    public static void cpu_set_nmi_line(int cpunum, int state) {
        cpu_set_irq_line(cpunum, IRQ_LINE_NMI, state);
    }
}
