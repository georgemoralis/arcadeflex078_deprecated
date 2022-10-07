/*
 * ported to v0.78
 * 
 */
package arcadeflex.v078.mame;

//generic imports
import static arcadeflex.v078.generic.funcPtr.*;
//mame imports
import static arcadeflex.v078.mame.cpuexec.*;

public class cpuexecH {

    /**
     * *************************************************************************
     *
     * cpuexec.h
     *
     * Core multi-CPU execution engine.
     *
     **************************************************************************
     */

    /*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	CPU description for drivers
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///struct MachineCPU
/*TODO*///{
/*TODO*///	int			cpu_type;					/* index for the CPU type */
/*TODO*///	int			cpu_flags;					/* flags; see #defines below */
/*TODO*///	int			cpu_clock;					/* in Hertz */
/*TODO*///	const void *memory_read;				/* struct Memory_ReadAddress */
/*TODO*///	const void *memory_write;				/* struct Memory_WriteAddress */
/*TODO*///	const void *port_read;
/*TODO*///	const void *port_write;
/*TODO*///	void 		(*vblank_interrupt)(void);	/* for interrupts tied to VBLANK */
/*TODO*///	int 		vblank_interrupts_per_frame;/* usually 1 */
/*TODO*///	void 		(*timed_interrupt)(void);	/* for interrupts not tied to VBLANK */
/*TODO*///	int 		timed_interrupts_per_second;
/*TODO*///	void *		reset_param;				/* parameter for cpu_reset */
/*TODO*///	const char *tag;
/*TODO*///};
/*TODO*///
/*TODO*///
    /**
     * ***********************************
     *
     * CPU flag constants
     *
     ************************************
     */

    /* set this if the CPU is used as a slave for audio. It will not be emulated if */
 /* sound is disabled, therefore speeding up a lot the emulation. */
    public static final int CPU_AUDIO_CPU = 0x0002;

    /* the Z80 can be wired to use 16 bit addressing for I/O ports */
    public static final int CPU_16BIT_PORT = 0x0001;

    /**
     * ***********************************
     *
     * Save/restore
     *
     ************************************
     */

    /* Load or save the game state */
    public static final int LOADSAVE_NONE = 0;
    public static final int LOADSAVE_SAVE = 1;
    public static final int LOADSAVE_LOAD = 2;

    /**
     * ***********************************
     *
     * CPU halt/reset lines
     *
     ************************************
     */

    /* Backwards compatibility */
    public static void cpu_set_reset_line(int cpunum, int state) {
        cpunum_set_reset_line(cpunum, state);
    }
    /*TODO*///#define cpu_set_halt_line 		cpunum_set_halt_line
    /**
     * ***********************************
     *
     * CPU scheduling
     *
     ************************************
     */

    /* Suspension reasons */
    public static final int SUSPEND_REASON_HALT = 0x0001;
    public static final int SUSPEND_REASON_RESET = 0x0002;
    public static final int SUSPEND_REASON_SPIN = 0x0004;
    public static final int SUSPEND_REASON_TRIGGER = 0x0008;
    public static final int SUSPEND_REASON_DISABLE = 0x0010;
    public static final int SUSPEND_ANY_REASON = ~0; //should be ok?? (shadow note)

    /*TODO*////* Backwards compatibility */
/*TODO*///#define timer_suspendcpu(cpunum, suspend, reason)	do { if (suspend) cpunum_suspend(cpunum, reason, 1); else cpunum_resume(cpunum, reason); } while (0)
/*TODO*///#define timer_holdcpu(cpunum, suspend, reason)		do { if (suspend) cpunum_suspend(cpunum, reason, 0); else cpunum_resume(cpunum, reason); } while (0)
    public static int cpu_getstatus(int cpunum) {
        return (cpunum_is_suspended(cpunum, SUSPEND_REASON_HALT | SUSPEND_REASON_RESET | SUSPEND_REASON_DISABLE) == 0) ? 1 : 0;
    }

    /*TODO*///#define timer_get_overclock(cpunum)					cpunum_get_clockscale(cpunum)
/*TODO*///#define timer_set_overclock(cpunum, overclock)		cpunum_set_clockscale(cpunum, overclock)
/*TODO*///
/*TODO*///
    /**
     * ***********************************
     *
     * Timing helpers
     *
     ************************************
     */
    /*TODO*////* Backwards compatibility */
/*TODO*///#define cpu_gettotalcycles cpunum_gettotalcycles
/*TODO*///#define cpu_gettotalcycles64 cpunum_gettotalcycles64
/*TODO*///
    /**
     * ***********************************
     *
     * Z80 daisy chain
     *
     ************************************
     */

    /* daisy-chain link */
    public static class Z80_DaisyChain {

        public DaisyChainResetPtr reset;/* reset callback     */
        public DaisyChainInterruptEntryPtr interrupt_entry;/* entry callback     */
        public DaisyChainInterruptRetiPtr interrupt_reti;/* reti callback      */
        public int irq_param;

        /* callback paramater */
        public Z80_DaisyChain(DaisyChainResetPtr reset, DaisyChainInterruptEntryPtr interrupt_entry, DaisyChainInterruptRetiPtr interrupt_reti, int irq_param) {
            this.reset = reset;
            this.interrupt_entry = interrupt_entry;
            this.interrupt_reti = interrupt_reti;
            this.irq_param = irq_param;
        }
    }

    public static final int Z80_MAXDAISY = 4;/* maximum of daisy chan device */

    public static final int Z80_INT_REQ = 0x01;/* interrupt request mask       */
    public static final int Z80_INT_IEO = 0x02;/* interrupt disable mask(IEO)  */

    public static int Z80_VECTOR(int device, int state) {
        return (((device) << 8) & 0xFF | (state) & 0xFF);
    }
}
