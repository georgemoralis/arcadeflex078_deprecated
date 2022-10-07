/*
 * ported to v0.78
 * 
 */
package arcadeflex.v078.mame;

//generic imports
import static arcadeflex.v078.generic.funcPtr.*;
//mame imports
import static arcadeflex.v078.mame.cpuint.*;
import static arcadeflex.v078.mame.cpuintrf.*;
import static arcadeflex.v078.mame.cpuintrfH.*;
import static arcadeflex.v078.mame.timerH.*;
import static arcadeflex.v078.mame.timer.*;
import static arcadeflex.v078.mame.common.*;
import static arcadeflex.v078.mame.cpuexecH.*;
import static arcadeflex.v078.mame.mame.*;

public class cpuexec {

    /**
     * *************************************************************************
     *
     * cpuexec.c
     *
     * Core multi-CPU execution engine.
     *
     **************************************************************************
     */

    /*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Debug logging
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///#define VERBOSE 0
/*TODO*///
/*TODO*///#if VERBOSE
/*TODO*///#define LOG(x)	logerror x
/*TODO*///#else
/*TODO*///#define LOG(x)
/*TODO*///#endif
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Macros to help verify active CPU
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///#define VERIFY_ACTIVECPU(retval, name)						\
/*TODO*///	int activecpu = cpu_getactivecpu();						\
/*TODO*///	if (activecpu < 0)										\
/*TODO*///	{														\
/*TODO*///		logerror(#name "() called with no active cpu!\n");	\
/*TODO*///		return retval;										\
/*TODO*///	}
/*TODO*///
/*TODO*///#define VERIFY_ACTIVECPU_VOID(name)							\
/*TODO*///	int activecpu = cpu_getactivecpu();						\
/*TODO*///	if (activecpu < 0)										\
/*TODO*///	{														\
/*TODO*///		logerror(#name "() called with no active cpu!\n");	\
/*TODO*///		return;												\
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Macros to help verify executing CPU
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///#define VERIFY_EXECUTINGCPU(retval, name)					\
/*TODO*///	int activecpu = cpu_getexecutingcpu();					\
/*TODO*///	if (activecpu < 0)										\
/*TODO*///	{														\
/*TODO*///		logerror(#name "() called with no executing cpu!\n");\
/*TODO*///		return retval;										\
/*TODO*///	}
/*TODO*///
/*TODO*///#define VERIFY_EXECUTINGCPU_VOID(name)						\
/*TODO*///	int activecpu = cpu_getexecutingcpu();					
/*TODO*///	if (activecpu < 0)										
/*TODO*///	{														
/*TODO*///		logerror(#name "() called with no executing cpu!\n");\
/*TODO*///		return;												
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Macros to help verify CPU index
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///#define VERIFY_CPUNUM(retval, name)							\
/*TODO*///	if (cpunum < 0 || cpunum >= cpu_gettotalcpu())			\
/*TODO*///	{														\
/*TODO*///		logerror(#name "() called for invalid cpu num!\n");	\
/*TODO*///		return retval;										\
/*TODO*///	}
/*TODO*///
/*TODO*///#define VERIFY_CPUNUM_VOID(name)							\
/*TODO*///	if (cpunum < 0 || cpunum >= cpu_gettotalcpu())			\
/*TODO*///	{														\
/*TODO*///		logerror(#name "() called for invalid cpu num!\n");	\
/*TODO*///		return;												\
/*TODO*///	}
/*TODO*///
/*TODO*///
    /**
     * ***********************************
     *
     * Triggers for the timer system
     *
     ************************************
     */
    public static final int TRIGGER_TIMESLICE = -1000;
    public static final int TRIGGER_INT = -2000;
    public static final int TRIGGER_YIELDTIME = -3000;
    public static final int TRIGGER_SUSPENDTIME = -4000;

    /**
     * ***********************************
     *
     * Internal CPU info structure
     *
     ************************************
     */
    public static class cpuinfo {

        int suspend;/* suspend reason mask (0 = not suspended) */
        int nextsuspend;/* pending suspend reason mask */
        int eatcycles;/* true if we eat cycles while suspended */
        int nexteatcycles;/* pending value */
        int trigger;/* pending trigger to release a trigger suspension */

        int iloops;/* number of interrupts remaining this frame */

 /*TODO*/        int/*UINT64*/ totalcycles;/* total CPU cycles executed */
        double localtime;/* local time, relative to the timer system's global time */
        double clockscale;/* current active clock scale factor */

        int vblankint_countdown;/* number of vblank callbacks left until we interrupt */
        int vblankint_multiplier;/* number of vblank callbacks per interrupt */
        Object vblankint_timer;/* reference to elapsed time counter */
        double vblankint_period;/* timing period of the VBLANK interrupt */

        Object timedint_timer;/* reference to this CPU's timer */
        double timedint_period;/* timing period of the timed interrupt */

        public static cpuinfo[] create(int n) {
            cpuinfo[] a = new cpuinfo[n];
            for (int k = 0; k < n; k++) {
                a[k] = new cpuinfo();
            }
            return a;
        }
    }

    /**
     * ***********************************
     *
     * General CPU variables
     *
     ************************************
     */
    static cpuinfo[] cpu_exec = cpuinfo.create(MAX_CPU);

    static int time_to_reset;
    static int time_to_quit;

    static int vblank;
    static int current_frame;
    static int watchdog_counter;

    static int cycles_running;
    static int cycles_stolen;

    /**
     * ***********************************
     *
     * Timer variables
     *
     ************************************
     */
    static Object vblank_timer;
    static int vblank_countdown;
    static int vblank_multiplier;
    static double vblank_period;

    static Object refresh_timer;
    static double refresh_period;
    static double refresh_period_inv;

    static Object timeslice_timer;
    static double timeslice_period;

    static double scanline_period;
    static double scanline_period_inv;

    static Object interleave_boost_timer;
    static Object interleave_boost_timer_end;
    static double perfect_interleave;

    /**
     * ***********************************
     *
     * Save/load variables
     *
     ************************************
     */
    static int loadsave_schedule;
    static String loadsave_schedule_name;

    /**
     * ***********************************
     *
     * Initialize all the CPUs
     *
     ************************************
     */
    public static int cpu_init() {
        int cpunum;

        /* initialize the interfaces first */
        if (cpuintrf_init() != 0) {
            return 1;
        }

        /* loop over all our CPUs */
        for (cpunum = 0; cpunum < MAX_CPU; cpunum++) {
            int cputype = Machine.drv.cpu[cpunum].cpu_type;

            /* if this is a dummy, stop looking */
            if (cputype == CPU_DUMMY) {
                break;
            }

            /*TODO*///		/* set the save state tag */
/*TODO*///		state_save_set_current_tag(cpunum + 1);

            /* initialize the cpuinfo struct */
            //memset(&cpu[cpunum], 0, sizeof(cpu[cpunum]));
            cpu_exec[cpunum].suspend = SUSPEND_REASON_RESET;
            cpu_exec[cpunum].clockscale = cputype_get_interface(cputype).overclock;

            /* compute the cycle times */
            sec_to_cycles[cpunum] = cpu_exec[cpunum].clockscale * Machine.drv.cpu[cpunum].cpu_clock;
            cycles_to_sec[cpunum] = 1.0 / sec_to_cycles[cpunum];

            /* initialize this CPU */
            if (cpuintrf_init_cpu(cpunum, cputype) != 0) {
                return 1;
            }
        }

        /* compute the perfect interleave factor */
        compute_perfect_interleave();

        /*TODO*///	/* save some stuff in tag 0 */
/*TODO*///	state_save_set_current_tag(0);
/*TODO*///	state_save_register_INT32("cpu", 0, "watchdog count", &watchdog_counter, 1);

        /* reset the IRQ lines and save those */
        if (cpuint_init() != 0) {
            return 1;
        }

        return 0;
    }

    /**
     * ***********************************
     *
     * Prepare the system for execution
     *
     ************************************
     */
    static void cpu_pre_run() {
        int cpunum;

        logerror("Machine reset\n");

        begin_resource_tracking();

        /*TODO*///	/* read hi scores information from hiscore.dat */
/*TODO*///	hs_open(Machine->gamedrv->name);
/*TODO*///	hs_init();

        /* initialize the various timers (suspends all CPUs at startup) */
        cpu_inittimers();
        watchdog_counter = -1;

        /* reset sound chips */
        sound_reset();

        /* first pass over CPUs */
        for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
            /* enable all CPUs (except for audio CPUs if the sound is off) */
            if ((Machine.drv.cpu[cpunum].cpu_flags & CPU_AUDIO_CPU) == 0 || Machine.sample_rate != 0) {
                cpunum_resume(cpunum, SUSPEND_ANY_REASON);
            } else {
                cpunum_suspend(cpunum, SUSPEND_REASON_DISABLE, 1);
            }

            /* reset the interrupt state */
            cpuint_reset_cpu(cpunum);

            /* reset the total number of cycles */
            cpu_exec[cpunum].totalcycles = 0;
            cpu_exec[cpunum].localtime = 0;
        }

        vblank = 0;

        /* do this AFTER the above so machine_init() can use cpu_halt() to hold the */
 /* execution of some CPUs, or disable interrupts */
        if (Machine.drv.machine_init != null) {
            Machine.drv.machine_init.handler();
        }

        /* now reset each CPU */
        for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
            cpunum_reset(cpunum, Machine.drv.cpu[cpunum].reset_param, cpu_irq_callbacks[cpunum]);
        }

        /* reset the globals */
        cpu_vblankreset();
        current_frame = 0;
        /*TODO*///	state_save_dump_registry();
    }

    /**
     * ***********************************
     *
     * Finish up execution
     *
     ************************************
     */
    static void cpu_post_run() {
        /*TODO*///	/* write hi scores to disk - No scores saving if cheat */
/*TODO*///	hs_close();

        /* stop the machine */
        if (Machine.drv.machine_stop != null) {
            Machine.drv.machine_stop.handler();
        }

        end_resource_tracking();
    }

    /**
     * ***********************************
     *
     * Execute until done
     *
     ************************************
     */
    public static void cpu_run() {

        /* loop over multiple resets, until the user quits */
        time_to_quit = 0;
        while (time_to_quit == 0) {
            /* prepare everything to run */
            cpu_pre_run();

            /* loop until the user quits or resets */
            time_to_reset = 0;
            while (time_to_quit == 0 && time_to_reset == 0) {
                /*TODO*///			/* if we have a load/save scheduled, handle it */
/*TODO*///			if (loadsave_schedule != LOADSAVE_NONE)
/*TODO*///				handle_loadsave();

                /* execute CPUs */
                cpu_timeslice();

            }

            /* finish up this iteration */
            cpu_post_run();
        }

    }

    /**
     * ***********************************
     *
     * Deinitialize all the CPUs
     *
     ************************************
     */
    public static void cpu_exit() {
        int cpunum;

        /* shut down the CPU cores */
        for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
            cpuintrf_exit_cpu(cpunum);
        }
    }

    /**
     * ***********************************
     *
     * Force a reset at the end of this timeslice
     *
     ************************************
     */
    public static void machine_reset() {
        time_to_reset = 1;
    }

    /*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Handle saves at runtime
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///static void handle_save(void)
/*TODO*///{
/*TODO*///	mame_file *file;
/*TODO*///	int cpunum;
/*TODO*///
/*TODO*///	/* open the file */
/*TODO*///	file = mame_fopen(Machine->gamedrv->name, loadsave_schedule_name, FILETYPE_STATE, 1);
/*TODO*///
/*TODO*///	if (file)
/*TODO*///	{
/*TODO*///		/* write the save state */
/*TODO*///		state_save_save_begin(file);
/*TODO*///
/*TODO*///		/* write tag 0 */
/*TODO*///		state_save_set_current_tag(0);
/*TODO*///		state_save_save_continue();
/*TODO*///
/*TODO*///		/* loop over CPUs */
/*TODO*///		for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++)
/*TODO*///		{
/*TODO*///			cpuintrf_push_context(cpunum);
/*TODO*///
/*TODO*///			/* make sure banking is set */
/*TODO*///			activecpu_reset_banking();
/*TODO*///
/*TODO*///			/* save the CPU data */
/*TODO*///			state_save_set_current_tag(cpunum + 1);
/*TODO*///			state_save_save_continue();
/*TODO*///
/*TODO*///			cpuintrf_pop_context();
/*TODO*///		}
/*TODO*///
/*TODO*///		/* finish and close */
/*TODO*///		state_save_save_finish();
/*TODO*///		mame_fclose(file);
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		usrintf_showmessage("Error: Failed to save state");
/*TODO*///	}
/*TODO*///
/*TODO*///	/* unschedule the save */
/*TODO*///	cpu_loadsave_reset();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Handle loads at runtime
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///static void handle_load(void)
/*TODO*///{
/*TODO*///	mame_file *file;
/*TODO*///	int cpunum;
/*TODO*///
/*TODO*///	/* open the file */
/*TODO*///	file = mame_fopen(Machine->gamedrv->name, loadsave_schedule_name, FILETYPE_STATE, 0);
/*TODO*///
/*TODO*///	/* if successful, load it */
/*TODO*///	if (file)
/*TODO*///	{
/*TODO*///		/* start loading */
/*TODO*///		if (!state_save_load_begin(file))
/*TODO*///		{
/*TODO*///			/* read tag 0 */
/*TODO*///			state_save_set_current_tag(0);
/*TODO*///			state_save_load_continue();
/*TODO*///
/*TODO*///			/* loop over CPUs */
/*TODO*///			for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++)
/*TODO*///			{
/*TODO*///				cpuintrf_push_context(cpunum);
/*TODO*///
/*TODO*///				/* make sure banking is set */
/*TODO*///				activecpu_reset_banking();
/*TODO*///
/*TODO*///				/* load the CPU data */
/*TODO*///				state_save_set_current_tag(cpunum + 1);
/*TODO*///				state_save_load_continue();
/*TODO*///
/*TODO*///				cpuintrf_pop_context();
/*TODO*///			}
/*TODO*///
/*TODO*///			/* finish and close */
/*TODO*///			state_save_load_finish();
/*TODO*///		}
/*TODO*///		mame_fclose(file);
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		usrintf_showmessage("Error: Failed to load state");
/*TODO*///	}
/*TODO*///
/*TODO*///	/* unschedule the load */
/*TODO*///	cpu_loadsave_reset();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Handle saves & loads at runtime
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///static void handle_loadsave(void)
/*TODO*///{
/*TODO*///	/* it's one or the other */
/*TODO*///	if (loadsave_schedule == LOADSAVE_SAVE)
/*TODO*///		handle_save();
/*TODO*///	else if (loadsave_schedule == LOADSAVE_LOAD)
/*TODO*///		handle_load();
/*TODO*///
/*TODO*///	/* reset the schedule */
/*TODO*///	cpu_loadsave_reset();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Schedules a save/load for later
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///void cpu_loadsave_schedule_file(int type, const char *name)
/*TODO*///{
/*TODO*///	cpu_loadsave_reset();
/*TODO*///
/*TODO*///	loadsave_schedule_name = malloc(strlen(name) + 1);
/*TODO*///	if (loadsave_schedule_name)
/*TODO*///	{
/*TODO*///		strcpy(loadsave_schedule_name, name);
/*TODO*///		loadsave_schedule = type;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Schedules a save/load for later
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///void cpu_loadsave_schedule(int type, char id)
/*TODO*///{
/*TODO*///	char name[256];
/*TODO*///	sprintf(name, "%s-%c", Machine->gamedrv->name, id);
/*TODO*///	cpu_loadsave_schedule_file(type, name);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Unschedules any saves or loads
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///void cpu_loadsave_reset(void)
/*TODO*///{
/*TODO*///	loadsave_schedule = LOADSAVE_NONE;
/*TODO*///	if (loadsave_schedule_name)
/*TODO*///	{
/*TODO*///		free(loadsave_schedule_name);
/*TODO*///		loadsave_schedule_name = NULL;
/*TODO*///	}
/*TODO*///}
/*TODO*///
    /**
     * ***********************************
     *
     * Watchdog routines
     *
     ************************************
     */

    /*--------------------------------------------------------------

	Use these functions to initialize, and later maintain, the
	watchdog. For convenience, when the machine is reset, the
	watchdog is disabled. If you call this function, the
	watchdog is initialized, and from that point onwards, if you
	don't call it at least once every 3 seconds, the machine
	will be reset.

	The 3 seconds delay is targeted at qzshowby, which otherwise
	would reset at the start of a game.

    --------------------------------------------------------------*/
    static void watchdog_reset() {
        if (watchdog_counter == -1) {
            logerror("watchdog armed\n");
        }
        watchdog_counter = (int) (3 * Machine.drv.frames_per_second);
    }

    public static WriteHandlerPtr watchdog_reset_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            watchdog_reset();
        }
    };

    public static ReadHandlerPtr watchdog_reset_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            watchdog_reset();
            return 0xff;
        }
    };

    /*TODO*///
/*TODO*///
/*TODO*///WRITE16_HANDLER( watchdog_reset16_w )
/*TODO*///{
/*TODO*///	watchdog_reset();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///READ16_HANDLER( watchdog_reset16_r )
/*TODO*///{
/*TODO*///	watchdog_reset();
/*TODO*///	return 0xffff;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///WRITE32_HANDLER( watchdog_reset32_w )
/*TODO*///{
/*TODO*///	watchdog_reset();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///READ32_HANDLER( watchdog_reset32_r )
/*TODO*///{
/*TODO*///	watchdog_reset();
/*TODO*///	return 0xffffffff;
/*TODO*///}
/*TODO*///
/*TODO*///
    /**
     * ***********************************
     *
     * Handle reset line changes
     *
     ************************************
     */
    public static TimerCallbackHandlerPtr reset_callback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            int cpunum = param & 0xff;
            int state = param >> 8;

            /* if we're asserting the line, just halt the CPU */
            if (state == ASSERT_LINE) {
                cpunum_suspend(cpunum, SUSPEND_REASON_RESET, 1);
                return;
            }

            /* if we're clearing the line that was previously asserted, or if we're just */
 /* pulsing the line, reset the CPU */
            if ((state == CLEAR_LINE && (cpu_exec[cpunum].suspend & SUSPEND_REASON_RESET) != 0) || state == PULSE_LINE) {
                cpunum_reset(cpunum, Machine.drv.cpu[cpunum].reset_param, cpu_irq_callbacks[cpunum]);
            }

            /* if we're clearing the line, make sure the CPU is not halted */
            cpunum_resume(cpunum, SUSPEND_REASON_RESET);
        }
    };

    public static void cpunum_set_reset_line(int cpunum, int state) {
        timer_set(TIME_NOW, (cpunum & 0xff) | (state << 8), reset_callback);
    }

    /**
     * ***********************************
     *
     * Handle halt line changes
     *
     ************************************
     */
    public static TimerCallbackHandlerPtr halt_callback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            int cpunum = param & 0xff;
            int state = param >> 8;

            /* if asserting, halt the CPU */
            if (state == ASSERT_LINE) {
                cpunum_suspend(cpunum, SUSPEND_REASON_HALT, 1);
            } /* if clearing, unhalt the CPU */ else if (state == CLEAR_LINE) {
                cpunum_resume(cpunum, SUSPEND_REASON_HALT);
            }
        }
    };

    public static void cpunum_set_halt_line(int cpunum, int state) {
        timer_set(TIME_NOW, (cpunum & 0xff) | (state << 8), halt_callback);
    }

    /**
     * ***********************************
     *
     * Execute all the CPUs for one timeslice
     *
     ************************************
     */
    static void cpu_timeslice() {
        double target = timer_time_until_next_timer();
        int cpunum, ran;

        //LOG(("------------------\n"));
        //LOG(("cpu_timeslice: target = %.9f\n", target));
        /* process any pending suspends */
        for (cpunum = 0; Machine.drv.cpu[cpunum].cpu_type != CPU_DUMMY; cpunum++) {
            if (cpu_exec[cpunum].suspend != cpu_exec[cpunum].nextsuspend) {
                //LOG(("--> updated CPU%d suspend from %X to %X\n", cpunum, cpu_exec[cpunum].suspend, cpu_exec[cpunum].nextsuspend));
            }
            cpu_exec[cpunum].suspend = cpu_exec[cpunum].nextsuspend;
            cpu_exec[cpunum].eatcycles = cpu_exec[cpunum].nexteatcycles;
        }

        /* loop over CPUs */
        for (cpunum = 0; Machine.drv.cpu[cpunum].cpu_type != CPU_DUMMY; cpunum++) {
            /* only process if we're not suspended */
            if (cpu_exec[cpunum].suspend == 0) {
                /* compute how long to run */
                cycles_running = TIME_TO_CYCLES(cpunum, target - cpu_exec[cpunum].localtime);
                //LOG(("  cpu %d: %d cycles\n", cpunum, cycles_running));

                /* run for the requested number of cycles */
                if (cycles_running > 0) {
                    //profiler_mark(PROFILER_CPU1 + cpunum);
                    cycles_stolen = 0;
                    ran = cpunum_execute(cpunum, cycles_running);
                    ran -= cycles_stolen;
                    //profiler_mark(PROFILER_END);

                    /* account for these cycles */
                    cpu_exec[cpunum].totalcycles += ran;
                    cpu_exec[cpunum].localtime += TIME_IN_CYCLES(ran, cpunum);
                    //LOG(("         %d ran, %d total, time = %.9f\n", ran, (INT32) cpu[cpunum].totalcycles, cpu[cpunum].localtime));

                    /* if the new local CPU time is less than our target, move the target up */
                    if (cpu_exec[cpunum].localtime < target && cpu_exec[cpunum].localtime > 0) {
                        target = cpu_exec[cpunum].localtime;
                        //LOG(("         (new target)\n"));
                    }
                }
            }
        }

        /* update the local times of all CPUs */
        for (cpunum = 0; Machine.drv.cpu[cpunum].cpu_type != CPU_DUMMY; cpunum++) {
            /* if we're suspended and counting, process */
            if (cpu_exec[cpunum].suspend != 0 && cpu_exec[cpunum].eatcycles != 0 && cpu_exec[cpunum].localtime < target) {
                /* compute how long to run */
                cycles_running = TIME_TO_CYCLES(cpunum, target - cpu_exec[cpunum].localtime);
                //LOG(("  cpu %d: %d cycles (suspended)\n", cpunum, cycles_running));

                cpu_exec[cpunum].totalcycles += cycles_running;
                cpu_exec[cpunum].localtime += TIME_IN_CYCLES(cycles_running, cpunum);
                //LOG(("         %d skipped, %d total, time = %.9f\n", cycles_running, (INT32) cpu[cpunum].totalcycles, cpu[cpunum].localtime));
            }

            /* update the suspend state */
            if (cpu_exec[cpunum].suspend != cpu_exec[cpunum].nextsuspend) {
                //LOG(("--> updated CPU%d suspend from %X to %X\n", cpunum, cpu[cpunum].suspend, cpu[cpunum].nextsuspend));
            }
            cpu_exec[cpunum].suspend = cpu_exec[cpunum].nextsuspend;
            cpu_exec[cpunum].eatcycles = cpu_exec[cpunum].nexteatcycles;

            /* adjust to be relative to the global time */
            cpu_exec[cpunum].localtime -= target;
        }

        /* update the global time */
        timer_adjust_global_time(target);

    }

    /**
     * ***********************************
     *
     * Abort the timeslice for the active CPU
     *
     ************************************
     */
    public static void activecpu_abort_timeslice() {
        int current_icount;
        int activecpu = cpu_getexecutingcpu();
        if (activecpu < 0) {
            logerror("activecpu_abort_timeslice() called with no executing cpu!\n");
            return;
        }

        //LOG(("activecpu_abort_timeslice (CPU=%d, cycles_left=%d)\n", cpu_getexecutingcpu(), activecpu_get_icount() + 1));
        /* swallow the remaining cycles */
        current_icount = activecpu_get_icount() + 1;
        cycles_stolen += current_icount;
        cycles_running -= current_icount;
        activecpu_adjust_icount(-current_icount);
    }

    /**
     * ***********************************
     *
     * Return the current local time for a CPU, relative to the current
     * timeslice
     *
     ************************************
     */
    public static double cpunum_get_localtime(int cpunum) {
        double result;
        if (cpunum < 0 || cpunum >= cpu_gettotalcpu()) {
            logerror("cpunum_get_localtime() called for invalid cpu num!\n");
            return 0;
        }

        /* if we're active, add in the time from the current slice */
        result = cpu_exec[cpunum].localtime;
        if (cpunum == cpu_getexecutingcpu()) {
            int cycles = cycles_currently_ran();
            result += TIME_IN_CYCLES(cycles, cpunum);
        }
        return result;
    }

    /**
     * ***********************************
     *
     * Set a suspend reason for the given CPU
     *
     ************************************
     */
    public static void cpunum_suspend(int cpunum, int reason, int eatcycles) {
        if (cpunum < 0 || cpunum >= cpu_gettotalcpu()) {
            logerror("cpunum_suspend() called for invalid cpu num!\n");
            return;
        }
        //LOG(("cpunum_suspend (CPU=%d, r=%X, eat=%d)\n", cpunum, reason, eatcycles));

        /* set the pending suspend bits, and force a resync */
        cpu_exec[cpunum].nextsuspend |= reason;
        cpu_exec[cpunum].nexteatcycles = eatcycles;
        if (cpu_getexecutingcpu() >= 0) {
            activecpu_abort_timeslice();
        }
    }

    /**
     * ***********************************
     *
     * Clear a suspend reason for a given CPU
     *
     ************************************
     */
    public static void cpunum_resume(int cpunum, int reason) {
        if (cpunum < 0 || cpunum >= cpu_gettotalcpu()) {
            logerror("cpunum_resume() called for invalid cpu num!\n");
            return;
        }
        //LOG(("cpunum_resume (CPU=%d, r=%X)\n", cpunum, reason));

        /* clear the pending suspend bits, and force a resync */
        cpu_exec[cpunum].nextsuspend &= ~reason;
        if (cpu_getexecutingcpu() >= 0) {
            activecpu_abort_timeslice();
        }
    }

    /**
     * ***********************************
     *
     * Return true if a given CPU is suspended
     *
     ************************************
     */
    public static int cpunum_is_suspended(int cpunum, int reason) {
        if (cpunum < 0 || cpunum >= cpu_gettotalcpu()) {
            logerror("cpunum_suspend() called for invalid cpu num!\n");
            return 0;
        }
        return ((cpu_exec[cpunum].nextsuspend & reason) != 0) ? 1 : 0;
    }

    /**
     * ***********************************
     *
     * Returns the current scaling factor for a CPU's clock speed
     *
     ************************************
     */
    public static double cpunum_get_clockscale(int cpunum) {
        if (cpunum < 0 || cpunum >= cpu_gettotalcpu()) {
            logerror("cpunum_get_clockscale() called for invalid cpu num!\n");
            return 1.0;
        }
        return cpu_exec[cpunum].clockscale;
    }

    /**
     * ***********************************
     *
     * Sets the current scaling factor for a CPU's clock speed
     *
     ************************************
     */
    public static void cpunum_set_clockscale(int cpunum, double clockscale) {
        if (cpunum < 0 || cpunum >= cpu_gettotalcpu()) {
            logerror("cpunum_set_clockscale() called for invalid cpu num!\n");
            return;
        }

        cpu_exec[cpunum].clockscale = clockscale;
        sec_to_cycles[cpunum] = cpu_exec[cpunum].clockscale * Machine.drv.cpu[cpunum].cpu_clock;
        cycles_to_sec[cpunum] = 1.0 / sec_to_cycles[cpunum];

        /* re-compute the perfect interleave factor */
        compute_perfect_interleave();
    }

    /**
     * ***********************************
     *
     * Temporarily boosts the interleave factor
     *
     ************************************
     */
    public static void cpu_boost_interleave(double timeslice_time, double boost_duration) {
        /* if you pass 0 for the timeslice_time, it means pick something reasonable */
        if (timeslice_time < perfect_interleave) {
            timeslice_time = perfect_interleave;
        }

        //LOG(("cpu_boost_interleave(%.9f, %.9f)\n", timeslice_time, boost_duration));

        /* adjust the interleave timer */
        timer_adjust((mame_timer) interleave_boost_timer, timeslice_time, 0, timeslice_time);

        /* adjust the end timer */
        timer_adjust((mame_timer) interleave_boost_timer_end, boost_duration, 0, TIME_NEVER);
    }

    /**
     * ***********************************
     *
     * Return cycles ran this iteration
     *
     ************************************
     */
    public static int cycles_currently_ran() {
        int activecpu = cpu_getexecutingcpu();
        if (activecpu < 0) {
            logerror("cycles_currently_ran() called with no executing cpu!\n");
            return 0;
        }
        return cycles_running - activecpu_get_icount();
    }

    /**
     * ***********************************
     *
     * Return cycles remaining in this iteration
     *
     ************************************
     */
    public static int cycles_left_to_run() {
        int activecpu = cpu_getexecutingcpu();
        if (activecpu < 0) {
            logerror("cycles_left_to_run() called with no executing cpu!\n");
            return 0;
        }
        return activecpu_get_icount();
    }

    /**
     * ***********************************
     *
     * Return total number of CPU cycles for the active CPU or for a given CPU.
     *
     ************************************
     */

    /*--------------------------------------------------------------

	IMPORTANT: this value wraps around in a relatively short
	time. For example, for a 6MHz CPU, it will wrap around in
	2^32/6000000 = 716 seconds = 12 minutes.

	Make sure you don't do comparisons between values returned
	by this function, but only use the difference (which will
	be correct regardless of wraparound).

	Alternatively, use the new 64-bit variants instead.

--------------------------------------------------------------*/
    public static int /*UINT32*/ activecpu_gettotalcycles() {
        int activecpu = cpu_getexecutingcpu();
        if (activecpu < 0) {
            logerror("cpu_gettotalcycles() called with no executing cpu!\n");
            return 0;
        }
        return cpu_exec[activecpu].totalcycles + cycles_currently_ran();
    }

    public static int /*UINT32*/ cpu_gettotalcycles(int cpunum) {
        if (cpunum < 0 || cpunum >= cpu_gettotalcpu()) {
            logerror("cpu_gettotalcycles() called for invalid cpu num!\n");
            return 0;
        }
        if (cpunum == cpu_getexecutingcpu()) {
            return cpu_exec[cpunum].totalcycles + cycles_currently_ran();
        } else {
            return cpu_exec[cpunum].totalcycles;
        }
    }

    /*TODO*///
/*TODO*///UINT64 activecpu_gettotalcycles64(void)
/*TODO*///{
/*TODO*///	VERIFY_EXECUTINGCPU(0, cpu_gettotalcycles);
/*TODO*///	return cpu[activecpu].totalcycles + cycles_currently_ran();
/*TODO*///}
/*TODO*///
/*TODO*///UINT64 cpu_gettotalcycles64(int cpunum)
/*TODO*///{
/*TODO*///	VERIFY_CPUNUM(0, cpu_gettotalcycles);
/*TODO*///	if (cpunum == cpu_getexecutingcpu())
/*TODO*///		return cpu[cpunum].totalcycles + cycles_currently_ran();
/*TODO*///	else
/*TODO*///		return cpu[cpunum].totalcycles;
/*TODO*///}
/*TODO*///
    /**
     * ***********************************
     *
     * Return cycles until next interrupt handler call
     *
     ************************************
     */
    public static int activecpu_geticount() {
        int result;

        /* remove me - only used by mamedbg, m92 */
        int activecpu = cpu_getexecutingcpu();
        if (activecpu < 0) {
            logerror("cpu_geticount() called with no executing cpu!\n");
            return 0;
        }
        result = TIME_TO_CYCLES(activecpu, cpu_exec[activecpu].vblankint_period - timer_timeelapsed((mame_timer) cpu_exec[activecpu].vblankint_timer));
        return (result < 0) ? 0 : result;
    }

    /**
     * ***********************************
     *
     * Safely eats cycles so we don't cross a timeslice boundary
     *
     ************************************
     */
    public static void activecpu_eat_cycles(int cycles) {
        int cyclesleft = activecpu_get_icount();
        if (cycles > cyclesleft) {
            cycles = cyclesleft;
        }
        activecpu_adjust_icount(-cycles);
    }

    /**
     * ***********************************
     *
     * Scales a given value by the fraction of time elapsed between refreshes
     *
     ************************************
     */
    public static int cpu_scalebyfcount(int value) {
        int result = (int) ((double) value * timer_timeelapsed((mame_timer) refresh_timer) * refresh_period_inv);
        if (value >= 0) {
            return (result < value) ? result : value;
        } else {
            return (result > value) ? result : value;
        }
    }

    /**
     * ***********************************
     *
     * Creates the refresh timer
     *
     ************************************
     */
    public static void cpu_init_refresh_timer() {
        /* allocate an infinite timer to track elapsed time since the last refresh */
        refresh_period = TIME_IN_HZ(Machine.drv.frames_per_second);
        refresh_period_inv = 1.0 / refresh_period;
        refresh_timer = timer_alloc(null);

        /* while we're at it, compute the scanline times */
        cpu_compute_scanline_timing();
    }

    /**
     * ***********************************
     *
     * Computes the scanline timing
     *
     ************************************
     */
    public static void cpu_compute_scanline_timing() {
        if (Machine.drv.vblank_duration != 0) {
            scanline_period = (refresh_period - TIME_IN_USEC(Machine.drv.vblank_duration))
                    / (double) (Machine.drv.default_visible_area.max_y - Machine.drv.default_visible_area.min_y + 1);
        } else {
            scanline_period = refresh_period / (double) Machine.drv.screen_height;
        }
        scanline_period_inv = 1.0 / scanline_period;
    }

    /**
     * ***********************************
     *
     * Returns the current scanline
     *
     ************************************
     */

    /*--------------------------------------------------------------

	Note: cpu_getscanline() counts from 0, 0 being the first
	visible line. You might have to adjust this value to match
	the hardware, since in many cases the first visible line
	is >0.

    --------------------------------------------------------------*/
    public static int cpu_getscanline() {
        double result = Math.floor(timer_timeelapsed((mame_timer) refresh_timer) * scanline_period_inv);
        return (int) result;
    }

    /**
     * ***********************************
     *
     * Returns time until given scanline
     *
     ************************************
     */
    public static double cpu_getscanlinetime(int scanline) {
        double scantime = timer_starttime((mame_timer) refresh_timer) + (double) scanline * scanline_period;
        double abstime = timer_get_time();
        double result;

        /* if we're already past the computed time, count it for the next frame */
        if (abstime >= scantime) {
            scantime += TIME_IN_HZ(Machine.drv.frames_per_second);
        }

        /* compute how long from now until that time */
        result = scantime - abstime;

        /* if it's small, just count a whole frame */
        if (result < TIME_IN_NSEC(1)) {
            result += TIME_IN_HZ(Machine.drv.frames_per_second);
        }
        return result;
    }

    /**
     * ***********************************
     *
     * Returns time for one scanline
     *
     ************************************
     */
    public static double cpu_getscanlineperiod() {
        return scanline_period;
    }

    /**
     * ***********************************
     *
     * Returns a crude approximation of the horizontal position of the bream
     *
     ************************************
     */
    public static int cpu_gethorzbeampos() {
        double elapsed_time = timer_timeelapsed((mame_timer) refresh_timer);
        int scanline = (int) (elapsed_time * scanline_period_inv);
        double time_since_scanline = elapsed_time - (double) scanline * scanline_period;
        return (int) (time_since_scanline * scanline_period_inv * (double) Machine.drv.screen_width);
    }

    /**
     * ***********************************
     *
     * Returns the VBLANK state
     *
     ************************************
     */
    public static int cpu_getvblank() {
        return vblank;
    }

    /**
     * ***********************************
     *
     * Returns the current frame count
     *
     ************************************
     */
    public static int cpu_getcurrentframe() {
        return current_frame;
    }

    /**
     * ***********************************
     *
     * Generate a specific trigger
     *
     ************************************
     */
    public static TimerCallbackHandlerPtr cpu_trigger = new TimerCallbackHandlerPtr() {
        public void handler(int trigger) {
            int cpunum;

            /* cause an immediate resynchronization */
            if (cpu_getexecutingcpu() >= 0) {
                activecpu_abort_timeslice();
            }

            /* look for suspended CPUs waiting for this trigger and unsuspend them */
            for (cpunum = 0; cpunum < MAX_CPU; cpunum++) {
                /* if this is a dummy, stop looking */
                if (Machine.drv.cpu[cpunum].cpu_type == CPU_DUMMY) {
                    break;
                }

                /* see if this is a matching trigger */
                if (cpu_exec[cpunum].suspend != 0 && cpu_exec[cpunum].trigger == trigger) {
                    cpunum_resume(cpunum, SUSPEND_REASON_TRIGGER);
                    cpu_exec[cpunum].trigger = 0;
                }
            }
        }
    };

    /**
     * ***********************************
     *
     * Generate a trigger in the future
     *
     ************************************
     */
    public static void cpu_triggertime(double duration, int trigger) {
        timer_set(duration, trigger, cpu_trigger);
    }

    /**
     * ***********************************
     *
     * Generate a trigger for an int
     *
     ************************************
     */
    public static void cpu_triggerint(int cpunum) {
        cpu_trigger.handler(TRIGGER_INT + cpunum);
    }

    /**
     * ***********************************
     *
     * Burn/yield CPU cycles until a trigger
     *
     ************************************
     */
    public static void cpu_spinuntil_trigger(int trigger) {
        int cpunum = cpu_getexecutingcpu();

        int activecpu = cpu_getexecutingcpu();
        if (activecpu < 0) {
            logerror("cpu_spinuntil_trigger() called with no executing cpu!\n");
            return;
        }

        /* suspend the CPU immediately if it's not already */
        cpunum_suspend(cpunum, SUSPEND_REASON_TRIGGER, 1);

        /* set the trigger */
        cpu_exec[cpunum].trigger = trigger;
    }

    public static void cpu_yielduntil_trigger(int trigger) {
        int cpunum = cpu_getexecutingcpu();

        int activecpu = cpu_getexecutingcpu();
        if (activecpu < 0) {
            logerror("cpu_yielduntil_trigger() called with no executing cpu!\n");
            return;
        }
        /* suspend the CPU immediately if it's not already */
        cpunum_suspend(cpunum, SUSPEND_REASON_TRIGGER, 0);

        /* set the trigger */
        cpu_exec[cpunum].trigger = trigger;
    }

    /**
     * ***********************************
     *
     * Burn/yield CPU cycles until an interrupt
     *
     ************************************
     */
    public static void cpu_spinuntil_int() {
        int activecpu = cpu_getexecutingcpu();
        if (activecpu < 0) {
            logerror("cpu_spinuntil_int() called with no executing cpu!\n");
            return;
        }
        cpu_spinuntil_trigger(TRIGGER_INT + activecpu);
    }

    public static void cpu_yielduntil_int() {
        int activecpu = cpu_getexecutingcpu();
        if (activecpu < 0) {
            logerror("cpu_yielduntil_int() called with no executing cpu!\n");
            return;
        }
        cpu_yielduntil_trigger(TRIGGER_INT + activecpu);
    }

    /**
     * ***********************************
     *
     * Burn/yield CPU cycles until the end of the current timeslice
     *
     ************************************
     */
    public static void cpu_spin() {
        cpu_spinuntil_trigger(TRIGGER_TIMESLICE);
    }

    public static void cpu_yield() {
        cpu_yielduntil_trigger(TRIGGER_TIMESLICE);
    }

    /**
     * ***********************************
     *
     * Burn/yield CPU cycles for a specific period of time
     *
     ************************************
     */
    static int timetrig_spin = 0;

    public static void cpu_spinuntil_time(double duration) {

        cpu_spinuntil_trigger(TRIGGER_SUSPENDTIME + timetrig_spin);
        cpu_triggertime(duration, TRIGGER_SUSPENDTIME + timetrig_spin);
        timetrig_spin = (timetrig_spin + 1) & 255;
    }

    static int timetrig_yield = 0;

    public static void cpu_yielduntil_time(double duration) {

        cpu_yielduntil_trigger(TRIGGER_YIELDTIME + timetrig_yield);
        cpu_triggertime(duration, TRIGGER_YIELDTIME + timetrig_yield);
        timetrig_yield = (timetrig_yield + 1) & 255;
    }

    /**
     * ***********************************
     *
     * Returns the number of times the interrupt handler will be called before
     * the end of the current video frame.
     *
     ************************************
     */

    /*--------------------------------------------------------------

	This can be useful to interrupt handlers to synchronize
	their operation. If you call this from outside an interrupt
	handler, add 1 to the result, i.e. if it returns 0, it means
	that the interrupt handler will be called once.

    --------------------------------------------------------------*/
    public static int cpu_getiloops() {
        int activecpu = cpu_getactivecpu();
        if (activecpu < 0) {
            logerror("cpu_getiloops() called with no active cpu!\n");
            return 0;
        }
        return cpu_exec[activecpu].iloops;
    }

    /**
     * ***********************************
     *
     * Hook for updating things on the real VBLANK (once per frame)
     *
     ************************************
     */
    public static void cpu_vblankreset() {
        int cpunum;

        /* read hi scores from disk */
 /*TODO*///	hs_update();

        /* read keyboard & update the status of the input ports */
        update_input_ports();

        /* reset the cycle counters */
        for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
            if ((cpu_exec[cpunum].suspend & SUSPEND_REASON_DISABLE) == 0) {
                cpu_exec[cpunum].iloops = Machine.drv.cpu[cpunum].vblank_interrupts_per_frame - 1;
            } else {
                cpu_exec[cpunum].iloops = -1;
            }
        }
    }

    /**
     * ***********************************
     *
     * First-run callback for VBLANKs
     *
     ************************************
     */
    public static TimerCallbackHandlerPtr cpu_firstvblankcallback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {

            /* now that we're synced up, pulse from here on out */
            timer_adjust((mame_timer) vblank_timer, vblank_period, param, vblank_period);

            /* but we need to call the standard routine as well */
            cpu_vblankcallback.handler(param);
        }
    };

    /**
     * ***********************************
     *
     * VBLANK core handler
     *
     ************************************
     */
    public static TimerCallbackHandlerPtr cpu_vblankcallback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            int cpunum;

            if (vblank_countdown == 1) {
                vblank = 1;
            }

            /* loop over CPUs */
            for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
                /* if the interrupt multiplier is valid */
                if (cpu_exec[cpunum].vblankint_multiplier != -1) {
                    /* decrement; if we hit zero, generate the interrupt and reset the countdown */
                    if (--cpu_exec[cpunum].vblankint_countdown == 0) {
                        /* a param of -1 means don't call any callbacks */
                        if (param != -1) {
                            /* if the CPU has a VBLANK handler, call it */
                            if (Machine.drv.cpu[cpunum].vblank_interrupt != null && cpu_getstatus(cpunum) != 0) {
                                cpuintrf_push_context(cpunum);
                                Machine.drv.cpu[cpunum].vblank_interrupt.handler();
                                cpuintrf_pop_context();
                            }

                            /* update the counters */
                            cpu_exec[cpunum].iloops--;
                        }

                        /* reset the countdown and timer */
                        cpu_exec[cpunum].vblankint_countdown = cpu_exec[cpunum].vblankint_multiplier;
                        timer_adjust((mame_timer) cpu_exec[cpunum].vblankint_timer, TIME_NEVER, 0, 0);
                    }
                } /* else reset the VBLANK timer if this is going to be a real VBLANK */ else if (vblank_countdown == 1) {
                    timer_adjust((mame_timer) cpu_exec[cpunum].vblankint_timer, TIME_NEVER, 0, 0);
                }
            }

            /* is it a real VBLANK? */
            if (--vblank_countdown == 0) {
                /* do we update the screen now? */
                if ((Machine.drv.video_attributes & VIDEO_UPDATE_AFTER_VBLANK) == 0) {
                    time_to_quit = updatescreen();
                }

                /* Set the timer to update the screen */
                timer_set(TIME_IN_USEC(Machine.drv.vblank_duration), 0, cpu_updatecallback);

                /* reset the globals */
                cpu_vblankreset();

                /* reset the counter */
                vblank_countdown = vblank_multiplier;
            }

        }
    };

    /**
     * ***********************************
     *
     * End-of-VBLANK callback
     *
     ************************************
     */
    public static TimerCallbackHandlerPtr cpu_updatecallback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {

            /* update the screen if we didn't before */
            if ((Machine.drv.video_attributes & VIDEO_UPDATE_AFTER_VBLANK) != 0) {
                time_to_quit = updatescreen();
            }
            vblank = 0;

            /* update IPT_VBLANK input ports */
            inputport_vblank_end();

            /* reset partial updating */
            reset_partial_updates();

            /* check the watchdog */
            if (watchdog_counter > 0) {
                if (--watchdog_counter == 0) {
                    logerror("reset caused by the watchdog\n");
                    machine_reset();
                }
            }

            /* track total frames */
            current_frame++;

            /* reset the refresh timer */
            timer_adjust((mame_timer) refresh_timer, TIME_NEVER, 0, 0);
        }
    };

    /**
     * ***********************************
     *
     * Callback for timed interrupts (not tied to a VBLANK)
     *
     ************************************
     */
    public static TimerCallbackHandlerPtr cpu_timedintcallback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            /* bail if there is no routine */
            if (Machine.drv.cpu[param].timed_interrupt != null && cpu_getstatus(param) != 0) {
                cpuintrf_push_context(param);
                //cpu_cause_interrupt(param, Machine.drv.cpu[param].timed_interrupt.handler());
                Machine.drv.cpu[param].timed_interrupt.handler();
                cpuintrf_pop_context();
            }
        }
    };

    /**
     * ***********************************
     *
     * Converts an integral timing rate into a period
     *
     ************************************
     */

    /*--------------------------------------------------------------

            Rates can be specified as follows:

                    rate <= 0		-> 0
                    rate < 50000	-> 'rate' cycles per frame
                    rate >= 50000	-> 'rate' nanoseconds

    --------------------------------------------------------------*/
    public static double cpu_computerate(int value) {
        /* values equal to zero are zero */
        if (value <= 0) {
            return 0.0;
        }

        /* values above between 0 and 50000 are in Hz */
        if (value < 50000) {
            return TIME_IN_HZ(value);
        } /* values greater than 50000 are in nanoseconds */ else {
            return TIME_IN_NSEC(value);
        }
    }

    /**
     * ***********************************
     *
     * Callback to force a timeslice
     *
     ************************************
     */
    public static TimerCallbackHandlerPtr cpu_timeslicecallback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            cpu_trigger.handler(TRIGGER_TIMESLICE);
        }
    };

    /**
     * ***********************************
     *
     * Callback to end a temporary interleave boost
     *
     ************************************
     */
    public static TimerCallbackHandlerPtr end_interleave_boost = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            timer_adjust((mame_timer) interleave_boost_timer, TIME_NEVER, 0, TIME_NEVER);
            //LOG(("end_interleave_boost\n"));
        }
    };

    /**
     * ***********************************
     *
     * Compute the "perfect" interleave interval
     *
     ************************************
     */
    static void compute_perfect_interleave() {
        double smallest = cycles_to_sec[0];
        int cpunum;

        /* start with a huge time factor and find the 2nd smallest cycle time */
        perfect_interleave = 1.0;
        for (cpunum = 1; Machine.drv.cpu[cpunum].cpu_type != CPU_DUMMY; cpunum++) {
            /* find the 2nd smallest cycle interval */
            if (cycles_to_sec[cpunum] < smallest) {
                perfect_interleave = smallest;
                smallest = cycles_to_sec[cpunum];
            } else if (cycles_to_sec[cpunum] < perfect_interleave) {
                perfect_interleave = cycles_to_sec[cpunum];
            }
        }

        /* adjust the final value */
        if (perfect_interleave == 1.0) {
            perfect_interleave = cycles_to_sec[0];
        }

        //LOG(("Perfect interleave = %.9f, smallest = %.9f\n", perfect_interleave, smallest));
    }

    /**
     * ***********************************
     *
     * Setup all the core timers
     *
     ************************************
     */
    static void cpu_inittimers() {
        double first_time;
        int cpunum, max, ipf;

        /* allocate a dummy timer at the minimum frequency to break things up */
        ipf = Machine.drv.cpu_slices_per_frame;
        if (ipf <= 0) {
            ipf = 1;
        }
        timeslice_period = TIME_IN_HZ(Machine.drv.frames_per_second * ipf);
        timeslice_timer = timer_alloc(cpu_timeslicecallback);
        timer_adjust((mame_timer) timeslice_timer, timeslice_period, 0, timeslice_period);

        /* allocate timers to handle interleave boosts */
        interleave_boost_timer = timer_alloc(null);
        interleave_boost_timer_end = timer_alloc(end_interleave_boost);

        /*
	 *	The following code finds all the CPUs that are interrupting in sync with the VBLANK
	 *	and sets up the VBLANK timer to run at the minimum number of cycles per frame in
	 *	order to service all the synced interrupts
         */

 /* find the CPU with the maximum interrupts per frame */
        max = 1;
        for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
            ipf = Machine.drv.cpu[cpunum].vblank_interrupts_per_frame;
            if (ipf > max) {
                max = ipf;
            }
        }

        /* now find the LCD with the rest of the CPUs (brute force - these numbers aren't huge) */
        vblank_multiplier = max;
        while (true) {
            for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
                ipf = Machine.drv.cpu[cpunum].vblank_interrupts_per_frame;
                if (ipf > 0 && (vblank_multiplier % ipf) != 0) {
                    break;
                }
            }
            if (cpunum == cpu_gettotalcpu()) {
                break;
            }
            vblank_multiplier += max;
        }

        /* initialize the countdown timers and intervals */
        for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
            ipf = Machine.drv.cpu[cpunum].vblank_interrupts_per_frame;
            if (ipf > 0) {
                cpu_exec[cpunum].vblankint_countdown = cpu_exec[cpunum].vblankint_multiplier = vblank_multiplier / ipf;
            } else {
                cpu_exec[cpunum].vblankint_countdown = cpu_exec[cpunum].vblankint_multiplier = -1;
            }
        }

        /* allocate a vblank timer at the frame rate * the LCD number of interrupts per frame */
        vblank_period = TIME_IN_HZ(Machine.drv.frames_per_second * vblank_multiplier);
        vblank_timer = timer_alloc(cpu_vblankcallback);
        vblank_countdown = vblank_multiplier;

        /*
	 *		The following code creates individual timers for each CPU whose interrupts are not
	 *		synced to the VBLANK, and computes the typical number of cycles per interrupt
         */

 /* start the CPU interrupt timers */
        for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++) {
            ipf = Machine.drv.cpu[cpunum].vblank_interrupts_per_frame;

            /* compute the average number of cycles per interrupt */
            if (ipf <= 0) {
                ipf = 1;
            }
            cpu_exec[cpunum].vblankint_period = TIME_IN_HZ(Machine.drv.frames_per_second * ipf);
            cpu_exec[cpunum].vblankint_timer = timer_alloc(null);

            /* see if we need to allocate a CPU timer */
            ipf = Machine.drv.cpu[cpunum].timed_interrupts_per_second;
            if (ipf != 0) {
                cpu_exec[cpunum].timedint_period = cpu_computerate(ipf);
                cpu_exec[cpunum].timedint_timer = timer_alloc(cpu_timedintcallback);
                timer_adjust((mame_timer) cpu_exec[cpunum].timedint_timer, cpu_exec[cpunum].timedint_period, cpunum, cpu_exec[cpunum].timedint_period);
            }
        }

        /* note that since we start the first frame on the refresh, we can't pulse starting
	   immediately; instead, we back up one VBLANK period, and inch forward until we hit
	   positive time. That time will be the time of the first VBLANK timer callback */
        first_time = -TIME_IN_USEC(Machine.drv.vblank_duration) + vblank_period;
        while (first_time < 0) {
            cpu_vblankcallback.handler(-1);
            first_time += vblank_period;
        }
        timer_set(first_time, 0, cpu_firstvblankcallback);
    }

}
