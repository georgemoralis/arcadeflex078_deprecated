/*
 * ported to v0.78
 * 
 */
package arcadeflex.v078.mame;

//generic imports
import static arcadeflex.v078.generic.funcPtr.*;

public class cpuint {

    /**
     * *************************************************************************
     *
     * cpuint.c
     *
     * Core multi-CPU interrupt engine.
     *
     **************************************************************************
     */
    /**
     * ***********************************
     *
     * Debug logging
     *
     ************************************
     */
    /*TODO*///
/*TODO*///#define VERBOSE 0
/*TODO*///
/*TODO*///#if VERBOSE
/*TODO*///#define LOG(x)	logerror x
/*TODO*///#else
/*TODO*///#define LOG(x)
/*TODO*///#endif
    /**
     * ***********************************
     *
     * CPU interrupt variables
     *
     ************************************
     */

    /* current states for each CPU */
    static int[]/*UINT8*/ interrupt_enable = new int[MAX_CPU];
    static int[][] interrupt_vector = new int[MAX_CPU][MAX_IRQ_LINES];

    /* deferred states written in callbacks */
    static int[][]/*UINT8*/ irq_line_state = new int[MAX_CPU][MAX_IRQ_LINES];
    static int[][] irq_line_vector = new int[MAX_CPU][MAX_IRQ_LINES];

    /* ick, interrupt event queues */
    public static final int MAX_IRQ_EVENTS = 256;
    static int[][] irq_event_queue = new int[MAX_CPU][MAX_IRQ_EVENTS];
    static int[] irq_event_index = new int[MAX_CPU];

    /**
     * ***********************************
     *
     * Initialize a CPU's interrupt states
     *
     ************************************
     */
    public static int cpuint_init() {
        int cpunum;
        int irqline;

        /* loop over all CPUs */
        for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
            /* reset the IRQ lines */
            for (irqline = 0; irqline < MAX_IRQ_LINES; irqline++) {
                irq_line_state[cpunum][irqline] = CLEAR_LINE;
                interrupt_vector[cpunum][irqline]
                        = irq_line_vector[cpunum][irqline] = cpunum_default_irq_vector(cpunum);
            }

            /* reset the IRQ event queues */
            irq_event_index[cpunum] = 0;
        }

        /*TODO*///	/* set up some stuff to save */
/*TODO*///	state_save_set_current_tag(0);
/*TODO*///	state_save_register_UINT8("cpu", 0, "irq enable",     interrupt_enable,  cpu_gettotalcpu());
/*TODO*///	state_save_register_INT32("cpu", 0, "irq vector",     &interrupt_vector[0][0],cpu_gettotalcpu() * MAX_IRQ_LINES);
/*TODO*///	state_save_register_UINT8("cpu", 0, "irqline state",  &irq_line_state[0][0],  cpu_gettotalcpu() * MAX_IRQ_LINES);
/*TODO*///	state_save_register_INT32("cpu", 0, "irqline vector", &irq_line_vector[0][0], cpu_gettotalcpu() * MAX_IRQ_LINES);
        return 0;
    }

    /**
     * ***********************************
     *
     * Reset a CPU's interrupt states
     *
     ************************************
     */
    public static void cpuint_reset_cpu(int cpunum) {
        int irqline;

        /* start with interrupts enabled, so the generic routine will work even if */
 /* the machine doesn't have an interrupt enable port */
        interrupt_enable[cpunum] = 1;
        for (irqline = 0; irqline < MAX_IRQ_LINES; irqline++) {
            interrupt_vector[cpunum][irqline] = cpunum_default_irq_vector(cpunum);
            irq_event_index[cpunum] = 0;
        }

        /* reset any driver hooks into the IRQ acknowledge callbacks */
        drv_irq_callbacks[cpunum] = null;
    }

    /**
     * ***********************************
     *
     * Set IRQ callback for drivers
     *
     ************************************
     */
    public static void cpu_set_irq_callback(int cpunum, IrqcallbackPtr callback) {
        drv_irq_callbacks[cpunum] = callback;
    }

    /**
     * ***********************************
     *
     * Internal IRQ callbacks
     *
     ************************************
     */
    public static int cpu_irq_callback(int cpunum, int irqline) {
        int vector = irq_line_vector[cpunum][irqline];

        //LOG(("cpu_%d_irq_callback(%d) $%04x\n", cpunum, irqline, vector));

        /* if the IRQ state is HOLD_LINE, clear it */
        if (irq_line_state[cpunum][irqline] == HOLD_LINE) {
            //LOG(("->set_irq_line(%d,%d,%d)\n", cpunum, irqline, CLEAR_LINE));
            activecpu_set_irq_line(irqline, INTERNAL_CLEAR_LINE);
            irq_line_state[cpunum][irqline] = CLEAR_LINE;
        }

        /* if there's a driver callback, run it */
        if (drv_irq_callbacks[cpunum] != null) {
            vector = (drv_irq_callbacks[cpunum]).handler(irqline);
        }

        /* otherwise, just return the current vector */
        return vector;
    }

    public static IrqcallbackPtr cpu_0_irq_callback = new IrqcallbackPtr() {
        public int handler(int irqline) {
            return cpu_irq_callback(0, irqline);
        }
    };
    public static IrqcallbackPtr cpu_1_irq_callback = new IrqcallbackPtr() {
        public int handler(int irqline) {
            return cpu_irq_callback(1, irqline);
        }
    };
    public static IrqcallbackPtr cpu_2_irq_callback = new IrqcallbackPtr() {
        public int handler(int irqline) {
            return cpu_irq_callback(2, irqline);
        }
    };
    public static IrqcallbackPtr cpu_3_irq_callback = new IrqcallbackPtr() {
        public int handler(int irqline) {
            return cpu_irq_callback(3, irqline);
        }
    };
    public static IrqcallbackPtr cpu_4_irq_callback = new IrqcallbackPtr() {
        public int handler(int irqline) {
            return cpu_irq_callback(4, irqline);
        }
    };
    public static IrqcallbackPtr cpu_5_irq_callback = new IrqcallbackPtr() {
        public int handler(int irqline) {
            return cpu_irq_callback(5, irqline);
        }
    };
    public static IrqcallbackPtr cpu_6_irq_callback = new IrqcallbackPtr() {
        public int handler(int irqline) {
            return cpu_irq_callback(6, irqline);
        }
    };
    public static IrqcallbackPtr cpu_7_irq_callback = new IrqcallbackPtr() {
        public int handler(int irqline) {
            return cpu_irq_callback(7, irqline);
        }
    };

    /**
     * ***********************************
     *
     * IRQ acknowledge callbacks
     *
     ************************************
     */
    public static IrqcallbackPtr[] cpu_irq_callbacks = {
        cpu_0_irq_callback, cpu_1_irq_callback, cpu_2_irq_callback, cpu_3_irq_callback,
        cpu_4_irq_callback, cpu_5_irq_callback, cpu_6_irq_callback, cpu_7_irq_callback
    };

    public static IrqcallbackPtr[] drv_irq_callbacks = new IrqcallbackPtr[MAX_CPU];

    /**
     * ***********************************
     *
     * Set the IRQ vector for a given IRQ line on a CPU
     *
     ************************************
     */
    public static void cpu_irq_line_vector_w(int cpunum, int irqline, int vector) {
        if (cpunum < cpu_gettotalcpu() && irqline >= 0 && irqline < MAX_IRQ_LINES) {
            //LOG(("cpu_irq_line_vector_w(%d,%d,$%04x)\n",cpunum,irqline,vector));
            interrupt_vector[cpunum][irqline] = vector;
            return;
        }
        //LOG(("cpu_irq_line_vector_w CPU#%d irqline %d > max irq lines\n", cpunum, irqline));
    }

    /**
     * ***********************************
     *
     * Generate a IRQ interrupt
     *
     ************************************
     */
    public static timer_callback cpu_empty_event_queue = new timer_callback() {
        public void handler(int cpunum) {
            int i;

            /* swap to the CPU's context */
            cpuintrf_push_context(cpunum);

            /* loop over all events */
            for (i = 0; i < irq_event_index[cpunum]; i++) {
                int irq_event = irq_event_queue[cpunum][i];
                int state = irq_event & 0xff;
                int irqline = (irq_event >> 8) & 0xff;
                int vector = irq_event >> 16;

                //LOG(("cpu_empty_event_queue %d,%d,%d\n",cpunum,irqline,state));

                /* set the IRQ line state and vector */
                if (irqline >= 0 && irqline < MAX_IRQ_LINES) {
                    irq_line_state[cpunum][irqline] = state;
                    irq_line_vector[cpunum][irqline] = vector;
                }

                /* switch off the requested state */
                switch (state) {
                    case PULSE_LINE:
                        activecpu_set_irq_line(irqline, INTERNAL_ASSERT_LINE);
                        activecpu_set_irq_line(irqline, INTERNAL_CLEAR_LINE);
                        break;

                    case HOLD_LINE:
                    case ASSERT_LINE:
                        activecpu_set_irq_line(irqline, INTERNAL_ASSERT_LINE);
                        break;

                    case CLEAR_LINE:
                        activecpu_set_irq_line(irqline, INTERNAL_CLEAR_LINE);
                        break;

                    default:
                        logerror("cpu_manualirqcallback cpu #%d, line %d, unknown state %d\n", cpunum, irqline, state);
                }

                /* generate a trigger to unsuspend any CPUs waiting on the interrupt */
                if (state != CLEAR_LINE) {
                    cpu_triggerint(cpunum);
                }
            }

            /* swap back */
            cpuintrf_pop_context();

            /* reset counter */
            irq_event_index[cpunum] = 0;
        }
    };

    public static void cpu_set_irq_line(int cpunum, int irqline, int state) {
        int vector = (irqline >= 0 && irqline < MAX_IRQ_LINES) ? interrupt_vector[cpunum][irqline] : 0xff;
        cpu_set_irq_line_and_vector(cpunum, irqline, state, vector);
    }

    public static void cpu_set_irq_line_and_vector(int cpunum, int irqline, int state, int vector) {
        int irq_event = (state & 0xff) | ((irqline & 0xff) << 8) | (vector << 16);
        int event_index = irq_event_index[cpunum]++;

        //LOG(("cpu_set_irq_line(%d,%d,%d,%02x)\n", cpunum, irqline, state, vector));

        /* enqueue the event */
        if (event_index < MAX_IRQ_EVENTS) {
            irq_event_queue[cpunum][event_index] = irq_event;

            /* if this is the first one, set the timer */
            if (event_index == 0) {
                timer_set(TIME_NOW, cpunum, cpu_empty_event_queue);
            }
        } else {
            logerror("Exceeded pending IRQ event queue on CPU %d!\n", cpunum);
        }
    }

    /**
     * ***********************************
     *
     * NMI interrupt generation
     *
     ************************************
     */
    public static InterruptHandlerPtr nmi_line_pulse = new InterruptHandlerPtr() {
        public void handler() {
            int cpunum = cpu_getactivecpu();
            if (interrupt_enable[cpunum] != 0) {
                cpu_set_irq_line(cpunum, IRQ_LINE_NMI, PULSE_LINE);
            }
        }
    };

    public static InterruptHandlerPtr nmi_line_assert = new InterruptHandlerPtr() {
        public void handler() {
            int cpunum = cpu_getactivecpu();
            if (interrupt_enable[cpunum] != 0) {
                cpu_set_irq_line(cpunum, IRQ_LINE_NMI, ASSERT_LINE);
            }
        }
    };

    /**
     * ***********************************
     *
     * IRQ n interrupt generation
     *
     ************************************
     */
    static void irqn_line_hold(int irqline) {
        int cpunum = cpu_getactivecpu();
        if (interrupt_enable[cpunum] != 0) {
            int vector = (irqline >= 0 && irqline < MAX_IRQ_LINES) ? interrupt_vector[cpunum][irqline] : 0xff;
            cpu_set_irq_line_and_vector(cpunum, irqline, HOLD_LINE, vector);
        }
    }

    static void irqn_line_pulse(int irqline) {
        int cpunum = cpu_getactivecpu();
        if (interrupt_enable[cpunum] != 0) {
            int vector = (irqline >= 0 && irqline < MAX_IRQ_LINES) ? interrupt_vector[cpunum][irqline] : 0xff;
            cpu_set_irq_line_and_vector(cpunum, irqline, PULSE_LINE, vector);
        }
    }

    static void irqn_line_assert(int irqline) {
        int cpunum = cpu_getactivecpu();
        if (interrupt_enable[cpunum] != 0) {
            int vector = (irqline >= 0 && irqline < MAX_IRQ_LINES) ? interrupt_vector[cpunum][irqline] : 0xff;
            cpu_set_irq_line_and_vector(cpunum, irqline, ASSERT_LINE, vector);
        }
    }

    /**
     * ***********************************
     *
     * IRQ interrupt generation
     *
     ************************************
     */
    public static InterruptHandlerPtr irq0_line_hold = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_hold(0);
        }
    };
    public static InterruptHandlerPtr irq0_line_pulse = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_pulse(0);
        }
    };
    public static InterruptHandlerPtr irq0_line_assert = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_assert(0);
        }
    };

    public static InterruptHandlerPtr irq1_line_hold = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_hold(1);
        }
    };
    public static InterruptHandlerPtr irq1_line_pulse = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_pulse(1);
        }
    };
    public static InterruptHandlerPtr irq1_line_assert = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_assert(1);
        }
    };

    public static InterruptHandlerPtr irq2_line_hold = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_hold(2);
        }
    };
    public static InterruptHandlerPtr irq2_line_pulse = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_pulse(2);
        }
    };
    public static InterruptHandlerPtr irq2_line_assert = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_assert(2);
        }
    };

    public static InterruptHandlerPtr irq3_line_hold = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_hold(3);
        }
    };
    public static InterruptHandlerPtr irq3_line_pulse = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_pulse(3);
        }
    };
    public static InterruptHandlerPtr irq3_line_assert = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_assert(3);
        }
    };

    public static InterruptHandlerPtr irq4_line_hold = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_hold(4);
        }
    };
    public static InterruptHandlerPtr irq4_line_pulse = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_pulse(4);
        }
    };
    public static InterruptHandlerPtr irq4_line_assert = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_assert(4);
        }
    };

    public static InterruptHandlerPtr irq5_line_hold = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_hold(5);
        }
    };
    public static InterruptHandlerPtr irq5_line_pulse = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_pulse(5);
        }
    };
    public static InterruptHandlerPtr irq5_line_assert = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_assert(5);
        }
    };

    public static InterruptHandlerPtr irq6_line_hold = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_hold(6);
        }
    };
    public static InterruptHandlerPtr irq6_line_pulse = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_pulse(6);
        }
    };
    public static InterruptHandlerPtr irq6_line_assert = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_assert(6);
        }
    };

    public static InterruptHandlerPtr irq7_line_hold = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_hold(7);
        }
    };
    public static InterruptHandlerPtr irq7_line_pulse = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_pulse(7);
        }
    };
    public static InterruptHandlerPtr irq7_line_assert = new InterruptHandlerPtr() {
        public void handler() {
            irqn_line_assert(7);
        }
    };

    /**
     * ***********************************
     *
     * Interrupt enabling
     *
     ************************************
     */
    public static timer_callback cpu_clearintcallback = new timer_callback() {
        public void handler(int cpunum) {
            int irqcount = cputype_get_interface(Machine.drv.cpu[cpunum].cpu_type).num_irqs;
            int irqline;

            cpuintrf_push_context(cpunum);

            /* clear NMI and all IRQs */
            activecpu_set_irq_line(IRQ_LINE_NMI, INTERNAL_CLEAR_LINE);
            for (irqline = 0; irqline < irqcount; irqline++) {
                activecpu_set_irq_line(irqline, INTERNAL_CLEAR_LINE);
            }

            cpuintrf_pop_context();
        }
    };

    public static void cpu_interrupt_enable(int cpunum, int enabled) {
        interrupt_enable[cpunum] = enabled;

//LOG(("CPU#%d interrupt_enable=%d\n", cpunum, enabled));

        /* make sure there are no queued interrupts */
        if (enabled == 0) {
            timer_set(TIME_NOW, cpunum, cpu_clearintcallback);
        }
    }

    public static WriteHandlerPtr interrupt_enable_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int activecpu = cpu_getactivecpu();
            if (activecpu < 0) {
                logerror("interrupt_enable_w() called with no active cpu!\n");
                return;
            }
            cpu_interrupt_enable(activecpu, data);
        }
    };

    public static ReadHandlerPtr interrupt_enable_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int activecpu = cpu_getactivecpu();
            if (activecpu < 0) {
                logerror("interrupt_enable_r() called with no active cpu!\n");
                return 1;
            }
            return interrupt_enable[activecpu];
        }
    };

    public static WriteHandlerPtr interrupt_vector_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int activecpu = cpu_getactivecpu();
            if (activecpu < 0) {
                logerror("interrupt_vector_w() called with no active cpu!\n");
                return;
            }
            if (interrupt_vector[activecpu][0] != data) {
                //LOG(("CPU#%d interrupt_vector_w $%02x\n", activecpu, data));
                interrupt_vector[activecpu][0] = data;

                /* make sure there are no queued interrupts */
                timer_set(TIME_NOW, activecpu, cpu_clearintcallback);
            }
        }
    };

}
