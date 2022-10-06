/*
 * ported to v0.78
 * 
 */
package arcadeflex.v078.mame;

//generic imports
import arcadeflex.v078.generic.funcPtr.*;
//mame imports
import static arcadeflex.v078.mame.cpuintrf.*;

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
/*TODO*///	int activecpu = cpu_getexecutingcpu();					\
/*TODO*///	if (activecpu < 0)										\
/*TODO*///	{														\
/*TODO*///		logerror(#name "() called with no executing cpu!\n");\
/*TODO*///		return;												\
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
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Triggers for the timer system
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///enum
/*TODO*///{
/*TODO*///	TRIGGER_TIMESLICE 	= -1000,
/*TODO*///	TRIGGER_INT 		= -2000,
/*TODO*///	TRIGGER_YIELDTIME 	= -3000,
/*TODO*///	TRIGGER_SUSPENDTIME = -4000
/*TODO*///};
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Internal CPU info structure
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///struct cpuinfo
/*TODO*///{
/*TODO*///	int		suspend;				/* suspend reason mask (0 = not suspended) */
/*TODO*///	int		nextsuspend;			/* pending suspend reason mask */
/*TODO*///	int		eatcycles;				/* true if we eat cycles while suspended */
/*TODO*///	int		nexteatcycles;			/* pending value */
/*TODO*///	int		trigger;				/* pending trigger to release a trigger suspension */
/*TODO*///
/*TODO*///	int 	iloops; 				/* number of interrupts remaining this frame */
/*TODO*///
/*TODO*///	UINT64 	totalcycles;			/* total CPU cycles executed */
/*TODO*///	double	localtime;				/* local time, relative to the timer system's global time */
/*TODO*///	double	clockscale;				/* current active clock scale factor */
/*TODO*///	
/*TODO*///	int 	vblankint_countdown;	/* number of vblank callbacks left until we interrupt */
/*TODO*///	int 	vblankint_multiplier;	/* number of vblank callbacks per interrupt */
/*TODO*///	void *	vblankint_timer;		/* reference to elapsed time counter */
/*TODO*///	double	vblankint_period;		/* timing period of the VBLANK interrupt */
/*TODO*///	
/*TODO*///	void *	timedint_timer;			/* reference to this CPU's timer */
/*TODO*///	double	timedint_period; 		/* timing period of the timed interrupt */
/*TODO*///};
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	General CPU variables
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///static struct cpuinfo cpu[MAX_CPU];
/*TODO*///
/*TODO*///static int time_to_reset;
/*TODO*///static int time_to_quit;
/*TODO*///
/*TODO*///static int vblank;
/*TODO*///static int current_frame;
/*TODO*///static INT32 watchdog_counter;
/*TODO*///
/*TODO*///static int cycles_running;
/*TODO*///static int cycles_stolen;
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Timer variables
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///static void *vblank_timer;
/*TODO*///static int vblank_countdown;
/*TODO*///static int vblank_multiplier;
/*TODO*///static double vblank_period;
/*TODO*///
/*TODO*///static void *refresh_timer;
/*TODO*///static double refresh_period;
/*TODO*///static double refresh_period_inv;
/*TODO*///
/*TODO*///static void *timeslice_timer;
/*TODO*///static double timeslice_period;
/*TODO*///
/*TODO*///static double scanline_period;
/*TODO*///static double scanline_period_inv;
/*TODO*///
/*TODO*///static void *interleave_boost_timer;
/*TODO*///static void *interleave_boost_timer_end;
/*TODO*///static double perfect_interleave;
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Save/load variables
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///static int loadsave_schedule;
/*TODO*///static char *loadsave_schedule_name;
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Static prototypes
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///static void cpu_timeslice(void);
/*TODO*///static void cpu_inittimers(void);
/*TODO*///static void cpu_vblankreset(void);
/*TODO*///static void cpu_vblankcallback(int param);
/*TODO*///static void cpu_updatecallback(int param);
/*TODO*///static void end_interleave_boost(int param);
/*TODO*///static void compute_perfect_interleave(void);
/*TODO*///
/*TODO*///static void handle_loadsave(void);
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///#if 0
/*TODO*///#pragma mark CORE CPU
/*TODO*///#endif
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Initialize all the CPUs
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///int cpu_init(void)
/*TODO*///{
/*TODO*///	int cpunum;
/*TODO*///	
/*TODO*///	/* initialize the interfaces first */
/*TODO*///	if (cpuintrf_init())
/*TODO*///		return 1;
/*TODO*///
/*TODO*///	/* loop over all our CPUs */
/*TODO*///	for (cpunum = 0; cpunum < MAX_CPU; cpunum++)
/*TODO*///	{
/*TODO*///		int cputype = Machine->drv->cpu[cpunum].cpu_type;
/*TODO*///
/*TODO*///		/* if this is a dummy, stop looking */
/*TODO*///		if (cputype == CPU_DUMMY)
/*TODO*///			break;
/*TODO*///
/*TODO*///		/* set the save state tag */
/*TODO*///		state_save_set_current_tag(cpunum + 1);
/*TODO*///		
/*TODO*///		/* initialize the cpuinfo struct */
/*TODO*///		memset(&cpu[cpunum], 0, sizeof(cpu[cpunum]));
/*TODO*///		cpu[cpunum].suspend = SUSPEND_REASON_RESET;
/*TODO*///		cpu[cpunum].clockscale = cputype_get_interface(cputype)->overclock;
/*TODO*///
/*TODO*///		/* compute the cycle times */
/*TODO*///		sec_to_cycles[cpunum] = cpu[cpunum].clockscale * Machine->drv->cpu[cpunum].cpu_clock;
/*TODO*///		cycles_to_sec[cpunum] = 1.0 / sec_to_cycles[cpunum];
/*TODO*///
/*TODO*///		/* initialize this CPU */
/*TODO*///		if (cpuintrf_init_cpu(cpunum, cputype))
/*TODO*///			return 1;
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* compute the perfect interleave factor */
/*TODO*///	compute_perfect_interleave();
/*TODO*///
/*TODO*///	/* save some stuff in tag 0 */
/*TODO*///	state_save_set_current_tag(0);
/*TODO*///	state_save_register_INT32("cpu", 0, "watchdog count", &watchdog_counter, 1);
/*TODO*///
/*TODO*///	/* reset the IRQ lines and save those */
/*TODO*///	if (cpuint_init())
/*TODO*///		return 1;
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Prepare the system for execution
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///static void cpu_pre_run(void)
/*TODO*///{
/*TODO*///	int cpunum;
/*TODO*///
/*TODO*///	logerror("Machine reset\n");
/*TODO*///
/*TODO*///	begin_resource_tracking();
/*TODO*///
/*TODO*///	/* read hi scores information from hiscore.dat */
/*TODO*///	hs_open(Machine->gamedrv->name);
/*TODO*///	hs_init();
/*TODO*///
/*TODO*///	/* initialize the various timers (suspends all CPUs at startup) */
/*TODO*///	cpu_inittimers();
/*TODO*///	watchdog_counter = -1;
/*TODO*///
/*TODO*///	/* reset sound chips */
/*TODO*///	sound_reset();
/*TODO*///
/*TODO*///	/* first pass over CPUs */
/*TODO*///	for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++)
/*TODO*///	{
/*TODO*///		/* enable all CPUs (except for audio CPUs if the sound is off) */
/*TODO*///		if (!(Machine->drv->cpu[cpunum].cpu_flags & CPU_AUDIO_CPU) || Machine->sample_rate != 0)
/*TODO*///			cpunum_resume(cpunum, SUSPEND_ANY_REASON);
/*TODO*///		else
/*TODO*///			cpunum_suspend(cpunum, SUSPEND_REASON_DISABLE, 1);
/*TODO*///
/*TODO*///		/* reset the interrupt state */
/*TODO*///		cpuint_reset_cpu(cpunum);
/*TODO*///
/*TODO*///		/* reset the total number of cycles */
/*TODO*///		cpu[cpunum].totalcycles = 0;
/*TODO*///		cpu[cpunum].localtime = 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	vblank = 0;
/*TODO*///
/*TODO*///	/* do this AFTER the above so machine_init() can use cpu_halt() to hold the */
/*TODO*///	/* execution of some CPUs, or disable interrupts */
/*TODO*///	if (Machine->drv->machine_init)
/*TODO*///		(*Machine->drv->machine_init)();
/*TODO*///
/*TODO*///	/* now reset each CPU */
/*TODO*///	for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++)
/*TODO*///		cpunum_reset(cpunum, Machine->drv->cpu[cpunum].reset_param, cpu_irq_callbacks[cpunum]);
/*TODO*///
/*TODO*///	/* reset the globals */
/*TODO*///	cpu_vblankreset();
/*TODO*///	current_frame = 0;
/*TODO*///	state_save_dump_registry();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Finish up execution
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///static void cpu_post_run(void)
/*TODO*///{
/*TODO*///	/* write hi scores to disk - No scores saving if cheat */
/*TODO*///	hs_close();
/*TODO*///
/*TODO*///	/* stop the machine */
/*TODO*///	if (Machine->drv->machine_stop)
/*TODO*///		(*Machine->drv->machine_stop)();
/*TODO*///
/*TODO*///	end_resource_tracking();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Execute until done
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///void cpu_run(void)
/*TODO*///{
/*TODO*///#ifdef MAME_DEBUG
/*TODO*///	/* initialize the debugger */
/*TODO*///	if (mame_debug)
/*TODO*///		mame_debug_init();
/*TODO*///#endif
/*TODO*///
/*TODO*///	/* loop over multiple resets, until the user quits */
/*TODO*///	time_to_quit = 0;
/*TODO*///	while (!time_to_quit)
/*TODO*///	{
/*TODO*///		/* prepare everything to run */
/*TODO*///		cpu_pre_run();
/*TODO*///
/*TODO*///		/* loop until the user quits or resets */
/*TODO*///		time_to_reset = 0;
/*TODO*///		while (!time_to_quit && !time_to_reset)
/*TODO*///		{
/*TODO*///			profiler_mark(PROFILER_EXTRA);
/*TODO*///
/*TODO*///			/* if we have a load/save scheduled, handle it */
/*TODO*///			if (loadsave_schedule != LOADSAVE_NONE)
/*TODO*///				handle_loadsave();
/*TODO*///			
/*TODO*///			/* execute CPUs */
/*TODO*///			cpu_timeslice();
/*TODO*///
/*TODO*///			profiler_mark(PROFILER_END);
/*TODO*///		}
/*TODO*///
/*TODO*///		/* finish up this iteration */
/*TODO*///		cpu_post_run();
/*TODO*///	}
/*TODO*///
/*TODO*///#ifdef MAME_DEBUG
/*TODO*///	/* shut down the debugger */
/*TODO*///	if (mame_debug)
/*TODO*///		mame_debug_exit();
/*TODO*///#endif
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Deinitialize all the CPUs
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///void cpu_exit(void)
/*TODO*///{
/*TODO*///	int cpunum;
/*TODO*///
/*TODO*///	/* shut down the CPU cores */
/*TODO*///	for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++)
/*TODO*///		cpuintrf_exit_cpu(cpunum);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Force a reset at the end of this
/*TODO*/// *	timeslice
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///void machine_reset(void)
/*TODO*///{
/*TODO*///	time_to_reset = 1;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///#if 0
/*TODO*///#pragma mark -
/*TODO*///#pragma mark SAVE/RESTORE
/*TODO*///#endif
/*TODO*///
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
/*TODO*///
/*TODO*///#if 0
/*TODO*///#pragma mark -
/*TODO*///#pragma mark WATCHDOG
/*TODO*///#endif
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Watchdog routines
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*////*--------------------------------------------------------------
/*TODO*///
/*TODO*///	Use these functions to initialize, and later maintain, the
/*TODO*///	watchdog. For convenience, when the machine is reset, the
/*TODO*///	watchdog is disabled. If you call this function, the
/*TODO*///	watchdog is initialized, and from that point onwards, if you
/*TODO*///	don't call it at least once every 3 seconds, the machine
/*TODO*///	will be reset.
/*TODO*///
/*TODO*///	The 3 seconds delay is targeted at qzshowby, which otherwise
/*TODO*///	would reset at the start of a game.
/*TODO*///
/*TODO*///--------------------------------------------------------------*/
/*TODO*///
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
/*TODO*///
/*TODO*///#if 0
/*TODO*///#pragma mark -
/*TODO*///#pragma mark HALT/RESET
/*TODO*///#endif
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Handle reset line changes
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///static void reset_callback(int param)
/*TODO*///{
/*TODO*///	int cpunum = param & 0xff;
/*TODO*///	int state = param >> 8;
/*TODO*///
/*TODO*///	/* if we're asserting the line, just halt the CPU */
/*TODO*///	if (state == ASSERT_LINE)
/*TODO*///	{
/*TODO*///		cpunum_suspend(cpunum, SUSPEND_REASON_RESET, 1);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* if we're clearing the line that was previously asserted, or if we're just */
/*TODO*///	/* pulsing the line, reset the CPU */
/*TODO*///	if ((state == CLEAR_LINE && (cpu[cpunum].suspend & SUSPEND_REASON_RESET)) || state == PULSE_LINE)
/*TODO*///		cpunum_reset(cpunum, Machine->drv->cpu[cpunum].reset_param, cpu_irq_callbacks[cpunum]);
/*TODO*///
/*TODO*///	/* if we're clearing the line, make sure the CPU is not halted */
/*TODO*///	cpunum_resume(cpunum, SUSPEND_REASON_RESET);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void cpunum_set_reset_line(int cpunum, int state)
/*TODO*///{
/*TODO*///	timer_set(TIME_NOW, (cpunum & 0xff) | (state << 8), reset_callback);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Handle halt line changes
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///static void halt_callback(int param)
/*TODO*///{
/*TODO*///	int cpunum = param & 0xff;
/*TODO*///	int state = param >> 8;
/*TODO*///
/*TODO*///	/* if asserting, halt the CPU */
/*TODO*///	if (state == ASSERT_LINE)
/*TODO*///		cpunum_suspend(cpunum, SUSPEND_REASON_HALT, 1);
/*TODO*///
/*TODO*///	/* if clearing, unhalt the CPU */
/*TODO*///	else if (state == CLEAR_LINE)
/*TODO*///		cpunum_resume(cpunum, SUSPEND_REASON_HALT);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void cpunum_set_halt_line(int cpunum, int state)
/*TODO*///{
/*TODO*///	timer_set(TIME_NOW, (cpunum & 0xff) | (state << 8), halt_callback);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///#if 0
/*TODO*///#pragma mark -
/*TODO*///#pragma mark CPU SCHEDULING
/*TODO*///#endif
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Execute all the CPUs for one
/*TODO*/// *	timeslice
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///static void cpu_timeslice(void)
/*TODO*///{
/*TODO*///	double target = timer_time_until_next_timer();
/*TODO*///	int cpunum, ran;
/*TODO*///	
/*TODO*///	LOG(("------------------\n"));
/*TODO*///	LOG(("cpu_timeslice: target = %.9f\n", target));
/*TODO*///	
/*TODO*///	/* process any pending suspends */
/*TODO*///	for (cpunum = 0; Machine->drv->cpu[cpunum].cpu_type != CPU_DUMMY; cpunum++)
/*TODO*///	{
/*TODO*///		if (cpu[cpunum].suspend != cpu[cpunum].nextsuspend)
/*TODO*///			LOG(("--> updated CPU%d suspend from %X to %X\n", cpunum, cpu[cpunum].suspend, cpu[cpunum].nextsuspend));
/*TODO*///		cpu[cpunum].suspend = cpu[cpunum].nextsuspend;
/*TODO*///		cpu[cpunum].eatcycles = cpu[cpunum].nexteatcycles;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* loop over CPUs */
/*TODO*///	for (cpunum = 0; Machine->drv->cpu[cpunum].cpu_type != CPU_DUMMY; cpunum++)
/*TODO*///	{
/*TODO*///		/* only process if we're not suspended */
/*TODO*///		if (!cpu[cpunum].suspend)
/*TODO*///		{
/*TODO*///			/* compute how long to run */
/*TODO*///			cycles_running = TIME_TO_CYCLES(cpunum, target - cpu[cpunum].localtime);
/*TODO*///			LOG(("  cpu %d: %d cycles\n", cpunum, cycles_running));
/*TODO*///		
/*TODO*///			/* run for the requested number of cycles */
/*TODO*///			if (cycles_running > 0)
/*TODO*///			{
/*TODO*///				profiler_mark(PROFILER_CPU1 + cpunum);
/*TODO*///				cycles_stolen = 0;
/*TODO*///				ran = cpunum_execute(cpunum, cycles_running);
/*TODO*///				ran -= cycles_stolen;
/*TODO*///				profiler_mark(PROFILER_END);
/*TODO*///				
/*TODO*///				/* account for these cycles */
/*TODO*///				cpu[cpunum].totalcycles += ran;
/*TODO*///				cpu[cpunum].localtime += TIME_IN_CYCLES(ran, cpunum);
/*TODO*///				LOG(("         %d ran, %d total, time = %.9f\n", ran, (INT32)cpu[cpunum].totalcycles, cpu[cpunum].localtime));
/*TODO*///				
/*TODO*///				/* if the new local CPU time is less than our target, move the target up */
/*TODO*///				if (cpu[cpunum].localtime < target && cpu[cpunum].localtime > 0)
/*TODO*///				{
/*TODO*///					target = cpu[cpunum].localtime;
/*TODO*///					LOG(("         (new target)\n"));
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* update the local times of all CPUs */
/*TODO*///	for (cpunum = 0; Machine->drv->cpu[cpunum].cpu_type != CPU_DUMMY; cpunum++)
/*TODO*///	{
/*TODO*///		/* if we're suspended and counting, process */
/*TODO*///		if (cpu[cpunum].suspend && cpu[cpunum].eatcycles && cpu[cpunum].localtime < target)
/*TODO*///		{
/*TODO*///			/* compute how long to run */
/*TODO*///			cycles_running = TIME_TO_CYCLES(cpunum, target - cpu[cpunum].localtime);
/*TODO*///			LOG(("  cpu %d: %d cycles (suspended)\n", cpunum, cycles_running));
/*TODO*///
/*TODO*///			cpu[cpunum].totalcycles += cycles_running;
/*TODO*///			cpu[cpunum].localtime += TIME_IN_CYCLES(cycles_running, cpunum);
/*TODO*///			LOG(("         %d skipped, %d total, time = %.9f\n", cycles_running, (INT32)cpu[cpunum].totalcycles, cpu[cpunum].localtime));
/*TODO*///		}
/*TODO*///		
/*TODO*///		/* update the suspend state */
/*TODO*///		if (cpu[cpunum].suspend != cpu[cpunum].nextsuspend)
/*TODO*///			LOG(("--> updated CPU%d suspend from %X to %X\n", cpunum, cpu[cpunum].suspend, cpu[cpunum].nextsuspend));
/*TODO*///		cpu[cpunum].suspend = cpu[cpunum].nextsuspend;
/*TODO*///		cpu[cpunum].eatcycles = cpu[cpunum].nexteatcycles;
/*TODO*///
/*TODO*///		/* adjust to be relative to the global time */
/*TODO*///		cpu[cpunum].localtime -= target;
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* update the global time */
/*TODO*///	timer_adjust_global_time(target);
/*TODO*///
/*TODO*///	/* huh? something for the debugger */
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///	{
/*TODO*///		extern int debug_key_delay;
/*TODO*///		debug_key_delay = 0x7ffe;
/*TODO*///	}
/*TODO*///	#endif
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Abort the timeslice for the 
/*TODO*/// *	active CPU
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///void activecpu_abort_timeslice(void)
/*TODO*///{
/*TODO*///	int current_icount;
/*TODO*///	
/*TODO*///	VERIFY_EXECUTINGCPU_VOID(activecpu_abort_timeslice);
/*TODO*///	LOG(("activecpu_abort_timeslice (CPU=%d, cycles_left=%d)\n", cpu_getexecutingcpu(), activecpu_get_icount() + 1));
/*TODO*///	
/*TODO*///	/* swallow the remaining cycles */
/*TODO*///	current_icount = activecpu_get_icount() + 1;
/*TODO*///	cycles_stolen += current_icount;
/*TODO*///	cycles_running -= current_icount;
/*TODO*///	activecpu_adjust_icount(-current_icount);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Return the current local time for
/*TODO*/// *	a CPU, relative to the current
/*TODO*/// *	timeslice
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///double cpunum_get_localtime(int cpunum)
/*TODO*///{
/*TODO*///	double result;
/*TODO*///	
/*TODO*///	VERIFY_CPUNUM(0, cpunum_get_localtime);
/*TODO*///
/*TODO*///	/* if we're active, add in the time from the current slice */
/*TODO*///	result = cpu[cpunum].localtime;
/*TODO*///	if (cpunum == cpu_getexecutingcpu())
/*TODO*///	{
/*TODO*///		int cycles = cycles_currently_ran();
/*TODO*///		result += TIME_IN_CYCLES(cycles, cpunum);
/*TODO*///	}
/*TODO*///	return result;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Set a suspend reason for the 
/*TODO*/// *	given CPU
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///void cpunum_suspend(int cpunum, int reason, int eatcycles)
/*TODO*///{
/*TODO*///	VERIFY_CPUNUM_VOID(cpunum_suspend);
/*TODO*///	LOG(("cpunum_suspend (CPU=%d, r=%X, eat=%d)\n", cpunum, reason, eatcycles));
/*TODO*///	
/*TODO*///	/* set the pending suspend bits, and force a resync */
/*TODO*///	cpu[cpunum].nextsuspend |= reason;
/*TODO*///	cpu[cpunum].nexteatcycles = eatcycles;
/*TODO*///	if (cpu_getexecutingcpu() >= 0)
/*TODO*///		activecpu_abort_timeslice();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Clear a suspend reason for a 
/*TODO*/// *	given CPU
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///void cpunum_resume(int cpunum, int reason)
/*TODO*///{
/*TODO*///	VERIFY_CPUNUM_VOID(cpunum_resume);
/*TODO*///	LOG(("cpunum_resume (CPU=%d, r=%X)\n", cpunum, reason));
/*TODO*///
/*TODO*///	/* clear the pending suspend bits, and force a resync */
/*TODO*///	cpu[cpunum].nextsuspend &= ~reason;
/*TODO*///	if (cpu_getexecutingcpu() >= 0)
/*TODO*///		activecpu_abort_timeslice();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Return true if a given CPU is
/*TODO*/// *	suspended
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///int cpunum_is_suspended(int cpunum, int reason)
/*TODO*///{
/*TODO*///	VERIFY_CPUNUM(0, cpunum_suspend);
/*TODO*///	return ((cpu[cpunum].nextsuspend & reason) != 0);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Returns the current scaling factor 
/*TODO*/// *	for a CPU's clock speed
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///double cpunum_get_clockscale(int cpunum)
/*TODO*///{
/*TODO*///	VERIFY_CPUNUM(1.0, cpunum_get_clockscale);
/*TODO*///	return cpu[cpunum].clockscale;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Sets the current scaling factor 
/*TODO*/// *	for a CPU's clock speed
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///void cpunum_set_clockscale(int cpunum, double clockscale)
/*TODO*///{
/*TODO*///	VERIFY_CPUNUM_VOID(cpunum_set_clockscale);
/*TODO*///
/*TODO*///	cpu[cpunum].clockscale = clockscale;
/*TODO*///	sec_to_cycles[cpunum] = cpu[cpunum].clockscale * Machine->drv->cpu[cpunum].cpu_clock;
/*TODO*///	cycles_to_sec[cpunum] = 1.0 / sec_to_cycles[cpunum];
/*TODO*///
/*TODO*///	/* re-compute the perfect interleave factor */
/*TODO*///	compute_perfect_interleave();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Temporarily boosts the interleave
/*TODO*/// *	factor
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///void cpu_boost_interleave(double timeslice_time, double boost_duration)
/*TODO*///{
/*TODO*///	/* if you pass 0 for the timeslice_time, it means pick something reasonable */
/*TODO*///	if (timeslice_time < perfect_interleave)
/*TODO*///		timeslice_time = perfect_interleave;
/*TODO*///	
/*TODO*///	LOG(("cpu_boost_interleave(%.9f, %.9f)\n", timeslice_time, boost_duration));
/*TODO*///
/*TODO*///	/* adjust the interleave timer */
/*TODO*///	timer_adjust(interleave_boost_timer, timeslice_time, 0, timeslice_time);		
/*TODO*///
/*TODO*///	/* adjust the end timer */
/*TODO*///	timer_adjust(interleave_boost_timer_end, boost_duration, 0, TIME_NEVER);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///#if 0
/*TODO*///#pragma mark -
/*TODO*///#pragma mark TIMING HELPERS
/*TODO*///#endif
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Return cycles ran this iteration
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///int cycles_currently_ran(void)
/*TODO*///{
/*TODO*///	VERIFY_EXECUTINGCPU(0, cycles_currently_ran);
/*TODO*///	return cycles_running - activecpu_get_icount();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Return cycles remaining in this
/*TODO*/// *	iteration
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///int cycles_left_to_run(void)
/*TODO*///{
/*TODO*///	VERIFY_EXECUTINGCPU(0, cycles_left_to_run);
/*TODO*///	return activecpu_get_icount();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Return total number of CPU cycles
/*TODO*/// *	for the active CPU or for a given CPU.
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*////*--------------------------------------------------------------
/*TODO*///
/*TODO*///	IMPORTANT: this value wraps around in a relatively short
/*TODO*///	time. For example, for a 6MHz CPU, it will wrap around in
/*TODO*///	2^32/6000000 = 716 seconds = 12 minutes.
/*TODO*///
/*TODO*///	Make sure you don't do comparisons between values returned
/*TODO*///	by this function, but only use the difference (which will
/*TODO*///	be correct regardless of wraparound).
/*TODO*///
/*TODO*///	Alternatively, use the new 64-bit variants instead.
/*TODO*///
/*TODO*///--------------------------------------------------------------*/
/*TODO*///
/*TODO*///UINT32 activecpu_gettotalcycles(void)
/*TODO*///{
/*TODO*///	VERIFY_EXECUTINGCPU(0, cpu_gettotalcycles);
/*TODO*///	return cpu[activecpu].totalcycles + cycles_currently_ran();
/*TODO*///}
/*TODO*///
/*TODO*///UINT32 cpu_gettotalcycles(int cpunum)
/*TODO*///{
/*TODO*///	VERIFY_CPUNUM(0, cpu_gettotalcycles);
/*TODO*///	if (cpunum == cpu_getexecutingcpu())
/*TODO*///		return cpu[cpunum].totalcycles + cycles_currently_ran();
/*TODO*///	else
/*TODO*///		return cpu[cpunum].totalcycles;
/*TODO*///}
/*TODO*///
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
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Return cycles until next interrupt
/*TODO*/// *	handler call
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///int activecpu_geticount(void)
/*TODO*///{
/*TODO*///	int result;
/*TODO*///
/*TODO*////* remove me - only used by mamedbg, m92 */
/*TODO*///	VERIFY_EXECUTINGCPU(0, cpu_geticount);
/*TODO*///	result = TIME_TO_CYCLES(activecpu, cpu[activecpu].vblankint_period - timer_timeelapsed(cpu[activecpu].vblankint_timer));
/*TODO*///	return (result < 0) ? 0 : result;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Safely eats cycles so we don't 
/*TODO*/// *	cross a timeslice boundary
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///void activecpu_eat_cycles(int cycles)
/*TODO*///{
/*TODO*///	int cyclesleft = activecpu_get_icount();
/*TODO*///	if (cycles > cyclesleft)
/*TODO*///		cycles = cyclesleft;
/*TODO*///	activecpu_adjust_icount(-cycles);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Scales a given value by the fraction
/*TODO*/// *	of time elapsed between refreshes
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///int cpu_scalebyfcount(int value)
/*TODO*///{
/*TODO*///	int result = (int)((double)value * timer_timeelapsed(refresh_timer) * refresh_period_inv);
/*TODO*///	if (value >= 0)
/*TODO*///		return (result < value) ? result : value;
/*TODO*///	else
/*TODO*///		return (result > value) ? result : value;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///#if 0
/*TODO*///#pragma mark -
/*TODO*///#pragma mark VIDEO TIMING
/*TODO*///#endif
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Creates the refresh timer
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///void cpu_init_refresh_timer(void)
/*TODO*///{
/*TODO*///	/* allocate an infinite timer to track elapsed time since the last refresh */
/*TODO*///	refresh_period = TIME_IN_HZ(Machine->drv->frames_per_second);
/*TODO*///	refresh_period_inv = 1.0 / refresh_period;
/*TODO*///	refresh_timer = timer_alloc(NULL);
/*TODO*///
/*TODO*///	/* while we're at it, compute the scanline times */
/*TODO*///	cpu_compute_scanline_timing();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Computes the scanline timing
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///void cpu_compute_scanline_timing(void)
/*TODO*///{
/*TODO*///	if (Machine->drv->vblank_duration)
/*TODO*///		scanline_period = (refresh_period - TIME_IN_USEC(Machine->drv->vblank_duration)) /
/*TODO*///				(double)(Machine->drv->default_visible_area.max_y - Machine->drv->default_visible_area.min_y + 1);
/*TODO*///	else
/*TODO*///		scanline_period = refresh_period / (double)Machine->drv->screen_height;
/*TODO*///	scanline_period_inv = 1.0 / scanline_period;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Returns the current scanline
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*////*--------------------------------------------------------------
/*TODO*///
/*TODO*///	Note: cpu_getscanline() counts from 0, 0 being the first
/*TODO*///	visible line. You might have to adjust this value to match
/*TODO*///	the hardware, since in many cases the first visible line
/*TODO*///	is >0.
/*TODO*///
/*TODO*///--------------------------------------------------------------*/
/*TODO*///
/*TODO*///int cpu_getscanline(void)
/*TODO*///{
/*TODO*///	double result = floor(timer_timeelapsed(refresh_timer) * scanline_period_inv);
/*TODO*///	return (int)result;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Returns time until given scanline
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///double cpu_getscanlinetime(int scanline)
/*TODO*///{
/*TODO*///	double scantime = timer_starttime(refresh_timer) + (double)scanline * scanline_period;
/*TODO*///	double abstime = timer_get_time();
/*TODO*///	double result;
/*TODO*///
/*TODO*///	/* if we're already past the computed time, count it for the next frame */
/*TODO*///	if (abstime >= scantime)
/*TODO*///		scantime += TIME_IN_HZ(Machine->drv->frames_per_second);
/*TODO*///
/*TODO*///	/* compute how long from now until that time */
/*TODO*///	result = scantime - abstime;
/*TODO*///
/*TODO*///	/* if it's small, just count a whole frame */
/*TODO*///	if (result < TIME_IN_NSEC(1))
/*TODO*///		result += TIME_IN_HZ(Machine->drv->frames_per_second);
/*TODO*///	return result;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Returns time for one scanline
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///double cpu_getscanlineperiod(void)
/*TODO*///{
/*TODO*///	return scanline_period;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Returns a crude approximation
/*TODO*/// *	of the horizontal position of the
/*TODO*/// *	bream
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///int cpu_gethorzbeampos(void)
/*TODO*///{
/*TODO*///	double elapsed_time = timer_timeelapsed(refresh_timer);
/*TODO*///	int scanline = (int)(elapsed_time * scanline_period_inv);
/*TODO*///	double time_since_scanline = elapsed_time - (double)scanline * scanline_period;
/*TODO*///	return (int)(time_since_scanline * scanline_period_inv * (double)Machine->drv->screen_width);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Returns the VBLANK state
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///int cpu_getvblank(void)
/*TODO*///{
/*TODO*///	return vblank;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Returns the current frame count
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///int cpu_getcurrentframe(void)
/*TODO*///{
/*TODO*///	return current_frame;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///#if 0
/*TODO*///#pragma mark -
/*TODO*///#pragma mark SYNCHRONIZATION
/*TODO*///#endif
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Generate a specific trigger
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///void cpu_trigger(int trigger)
/*TODO*///{
/*TODO*///	int cpunum;
/*TODO*///	
/*TODO*///	/* cause an immediate resynchronization */
/*TODO*///	if (cpu_getexecutingcpu() >= 0)
/*TODO*///		activecpu_abort_timeslice();
/*TODO*///
/*TODO*///	/* look for suspended CPUs waiting for this trigger and unsuspend them */
/*TODO*///	for (cpunum = 0; cpunum < MAX_CPU; cpunum++)
/*TODO*///	{
/*TODO*///		/* if this is a dummy, stop looking */
/*TODO*///		if (Machine->drv->cpu[cpunum].cpu_type == CPU_DUMMY)
/*TODO*///			break;
/*TODO*///
/*TODO*///		/* see if this is a matching trigger */
/*TODO*///		if (cpu[cpunum].suspend && cpu[cpunum].trigger == trigger)
/*TODO*///		{
/*TODO*///			cpunum_resume(cpunum, SUSPEND_REASON_TRIGGER);
/*TODO*///			cpu[cpunum].trigger = 0;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Generate a trigger in the future
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///void cpu_triggertime(double duration, int trigger)
/*TODO*///{
/*TODO*///	timer_set(duration, trigger, cpu_trigger);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Generate a trigger for an int
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///void cpu_triggerint(int cpunum)
/*TODO*///{
/*TODO*///	cpu_trigger(TRIGGER_INT + cpunum);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Burn/yield CPU cycles until a trigger
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///void cpu_spinuntil_trigger(int trigger)
/*TODO*///{
/*TODO*///	int cpunum = cpu_getexecutingcpu();
/*TODO*///
/*TODO*///	VERIFY_EXECUTINGCPU_VOID(cpu_spinuntil_trigger);
/*TODO*///
/*TODO*///	/* suspend the CPU immediately if it's not already */
/*TODO*///	cpunum_suspend(cpunum, SUSPEND_REASON_TRIGGER, 1);
/*TODO*///
/*TODO*///	/* set the trigger */
/*TODO*///	cpu[cpunum].trigger = trigger;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void cpu_yielduntil_trigger(int trigger)
/*TODO*///{
/*TODO*///	int cpunum = cpu_getexecutingcpu();
/*TODO*///
/*TODO*///	VERIFY_EXECUTINGCPU_VOID(cpu_yielduntil_trigger);
/*TODO*///
/*TODO*///	/* suspend the CPU immediately if it's not already */
/*TODO*///	cpunum_suspend(cpunum, SUSPEND_REASON_TRIGGER, 0);
/*TODO*///
/*TODO*///	/* set the trigger */
/*TODO*///	cpu[cpunum].trigger = trigger;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Burn/yield CPU cycles until an
/*TODO*/// *	interrupt
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///void cpu_spinuntil_int(void)
/*TODO*///{
/*TODO*///	VERIFY_EXECUTINGCPU_VOID(cpu_spinuntil_int);
/*TODO*///	cpu_spinuntil_trigger(TRIGGER_INT + activecpu);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void cpu_yielduntil_int(void)
/*TODO*///{
/*TODO*///	VERIFY_EXECUTINGCPU_VOID(cpu_yielduntil_int);
/*TODO*///	cpu_yielduntil_trigger(TRIGGER_INT + activecpu);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Burn/yield CPU cycles until the
/*TODO*/// *	end of the current timeslice
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///void cpu_spin(void)
/*TODO*///{
/*TODO*///	cpu_spinuntil_trigger(TRIGGER_TIMESLICE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void cpu_yield(void)
/*TODO*///{
/*TODO*///	cpu_yielduntil_trigger(TRIGGER_TIMESLICE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Burn/yield CPU cycles for a
/*TODO*/// *	specific period of time
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///void cpu_spinuntil_time(double duration)
/*TODO*///{
/*TODO*///	static int timetrig = 0;
/*TODO*///
/*TODO*///	cpu_spinuntil_trigger(TRIGGER_SUSPENDTIME + timetrig);
/*TODO*///	cpu_triggertime(duration, TRIGGER_SUSPENDTIME + timetrig);
/*TODO*///	timetrig = (timetrig + 1) & 255;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void cpu_yielduntil_time(double duration)
/*TODO*///{
/*TODO*///	static int timetrig = 0;
/*TODO*///
/*TODO*///	cpu_yielduntil_trigger(TRIGGER_YIELDTIME + timetrig);
/*TODO*///	cpu_triggertime(duration, TRIGGER_YIELDTIME + timetrig);
/*TODO*///	timetrig = (timetrig + 1) & 255;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///#if 0
/*TODO*///#pragma mark -
/*TODO*///#pragma mark CORE TIMING
/*TODO*///#endif
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Returns the number of times the
/*TODO*/// *	interrupt handler will be called
/*TODO*/// *	before the end of the current
/*TODO*/// *	video frame.
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*////*--------------------------------------------------------------
/*TODO*///
/*TODO*///	This can be useful to interrupt handlers to synchronize
/*TODO*///	their operation. If you call this from outside an interrupt
/*TODO*///	handler, add 1 to the result, i.e. if it returns 0, it means
/*TODO*///	that the interrupt handler will be called once.
/*TODO*///
/*TODO*///--------------------------------------------------------------*/
/*TODO*///
/*TODO*///int cpu_getiloops(void)
/*TODO*///{
/*TODO*///	VERIFY_ACTIVECPU(0, cpu_getiloops);
/*TODO*///	return cpu[activecpu].iloops;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Hook for updating things on the
/*TODO*/// *	real VBLANK (once per frame)
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///static void cpu_vblankreset(void)
/*TODO*///{
/*TODO*///	int cpunum;
/*TODO*///
/*TODO*///	/* read hi scores from disk */
/*TODO*///	hs_update();
/*TODO*///
/*TODO*///	/* read keyboard & update the status of the input ports */
/*TODO*///	update_input_ports();
/*TODO*///
/*TODO*///	/* reset the cycle counters */
/*TODO*///	for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++)
/*TODO*///	{
/*TODO*///		if (!(cpu[cpunum].suspend & SUSPEND_REASON_DISABLE))
/*TODO*///			cpu[cpunum].iloops = Machine->drv->cpu[cpunum].vblank_interrupts_per_frame - 1;
/*TODO*///		else
/*TODO*///			cpu[cpunum].iloops = -1;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	First-run callback for VBLANKs
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///static void cpu_firstvblankcallback(int param)
/*TODO*///{
/*TODO*///	/* now that we're synced up, pulse from here on out */
/*TODO*///	timer_adjust(vblank_timer, vblank_period, param, vblank_period);
/*TODO*///
/*TODO*///	/* but we need to call the standard routine as well */
/*TODO*///	cpu_vblankcallback(param);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	VBLANK core handler
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///static void cpu_vblankcallback(int param)
/*TODO*///{
/*TODO*///	int cpunum;
/*TODO*///
/*TODO*///   if (vblank_countdown == 1)
/*TODO*///      vblank = 1;
/*TODO*///
/*TODO*///	/* loop over CPUs */
/*TODO*///	for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++)
/*TODO*///	{
/*TODO*///		/* if the interrupt multiplier is valid */
/*TODO*///		if (cpu[cpunum].vblankint_multiplier != -1)
/*TODO*///		{
/*TODO*///			/* decrement; if we hit zero, generate the interrupt and reset the countdown */
/*TODO*///			if (!--cpu[cpunum].vblankint_countdown)
/*TODO*///			{
/*TODO*///				/* a param of -1 means don't call any callbacks */
/*TODO*///				if (param != -1)
/*TODO*///				{
/*TODO*///					/* if the CPU has a VBLANK handler, call it */
/*TODO*///					if (Machine->drv->cpu[cpunum].vblank_interrupt && cpu_getstatus(cpunum))
/*TODO*///					{
/*TODO*///						cpuintrf_push_context(cpunum);
/*TODO*///						(*Machine->drv->cpu[cpunum].vblank_interrupt)();
/*TODO*///						cpuintrf_pop_context();
/*TODO*///					}
/*TODO*///
/*TODO*///					/* update the counters */
/*TODO*///					cpu[cpunum].iloops--;
/*TODO*///				}
/*TODO*///
/*TODO*///				/* reset the countdown and timer */
/*TODO*///				cpu[cpunum].vblankint_countdown = cpu[cpunum].vblankint_multiplier;
/*TODO*///				timer_adjust(cpu[cpunum].vblankint_timer, TIME_NEVER, 0, 0);
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		/* else reset the VBLANK timer if this is going to be a real VBLANK */
/*TODO*///		else if (vblank_countdown == 1)
/*TODO*///			timer_adjust(cpu[cpunum].vblankint_timer, TIME_NEVER, 0, 0);
/*TODO*///	}
/*TODO*///
/*TODO*///	/* is it a real VBLANK? */
/*TODO*///	if (!--vblank_countdown)
/*TODO*///	{
/*TODO*///		/* do we update the screen now? */
/*TODO*///		if (!(Machine->drv->video_attributes & VIDEO_UPDATE_AFTER_VBLANK))
/*TODO*///			time_to_quit = updatescreen();
/*TODO*///
/*TODO*///		/* Set the timer to update the screen */
/*TODO*///		timer_set(TIME_IN_USEC(Machine->drv->vblank_duration), 0, cpu_updatecallback);
/*TODO*///
/*TODO*///		/* reset the globals */
/*TODO*///		cpu_vblankreset();
/*TODO*///
/*TODO*///		/* reset the counter */
/*TODO*///		vblank_countdown = vblank_multiplier;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	End-of-VBLANK callback
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///static void cpu_updatecallback(int param)
/*TODO*///{
/*TODO*///	/* update the screen if we didn't before */
/*TODO*///	if (Machine->drv->video_attributes & VIDEO_UPDATE_AFTER_VBLANK)
/*TODO*///		time_to_quit = updatescreen();
/*TODO*///	vblank = 0;
/*TODO*///
/*TODO*///	/* update IPT_VBLANK input ports */
/*TODO*///	inputport_vblank_end();
/*TODO*///
/*TODO*///	/* reset partial updating */
/*TODO*///	reset_partial_updates();
/*TODO*///
/*TODO*///	/* check the watchdog */
/*TODO*///	if (watchdog_counter > 0)
/*TODO*///		if (--watchdog_counter == 0)
/*TODO*///		{
/*TODO*///			logerror("reset caused by the watchdog\n");
/*TODO*///			machine_reset();
/*TODO*///		}
/*TODO*///
/*TODO*///	/* track total frames */
/*TODO*///	current_frame++;
/*TODO*///
/*TODO*///	/* reset the refresh timer */
/*TODO*///	timer_adjust(refresh_timer, TIME_NEVER, 0, 0);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
    /**
     * ***********************************
     *
     * Callback for timed interrupts (not tied to a VBLANK)
     *
     ************************************
     */
    public static timer_callback cpu_timedintcallback = new timer_callback() {
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

    /*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Callback to force a timeslice
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///static void cpu_timeslicecallback(int param)
/*TODO*///{
/*TODO*///	cpu_trigger(TRIGGER_TIMESLICE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Callback to end a temporary
/*TODO*/// *	interleave boost
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///static void end_interleave_boost(int param)
/*TODO*///{
/*TODO*///	timer_adjust(interleave_boost_timer, TIME_NEVER, 0, TIME_NEVER);		
/*TODO*///	LOG(("end_interleave_boost\n"));
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Compute the "perfect" interleave
/*TODO*/// *	interval
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///static void compute_perfect_interleave(void)
/*TODO*///{
/*TODO*///	double smallest = cycles_to_sec[0];
/*TODO*///	int cpunum;
/*TODO*///
/*TODO*///	/* start with a huge time factor and find the 2nd smallest cycle time */
/*TODO*///	perfect_interleave = 1.0;
/*TODO*///	for (cpunum = 1; Machine->drv->cpu[cpunum].cpu_type != CPU_DUMMY; cpunum++)
/*TODO*///	{
/*TODO*///		/* find the 2nd smallest cycle interval */
/*TODO*///		if (cycles_to_sec[cpunum] < smallest)
/*TODO*///		{
/*TODO*///			perfect_interleave = smallest;
/*TODO*///			smallest = cycles_to_sec[cpunum];
/*TODO*///		}
/*TODO*///		else if (cycles_to_sec[cpunum] < perfect_interleave)
/*TODO*///			perfect_interleave = cycles_to_sec[cpunum];
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* adjust the final value */
/*TODO*///	if (perfect_interleave == 1.0)
/*TODO*///		perfect_interleave = cycles_to_sec[0];
/*TODO*///
/*TODO*///	LOG(("Perfect interleave = %.9f, smallest = %.9f\n", perfect_interleave, smallest));
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////*************************************
/*TODO*/// *
/*TODO*/// *	Setup all the core timers
/*TODO*/// *
/*TODO*/// *************************************/
/*TODO*///
/*TODO*///static void cpu_inittimers(void)
/*TODO*///{
/*TODO*///	double first_time;
/*TODO*///	int cpunum, max, ipf;
/*TODO*///
/*TODO*///	/* allocate a dummy timer at the minimum frequency to break things up */
/*TODO*///	ipf = Machine->drv->cpu_slices_per_frame;
/*TODO*///	if (ipf <= 0)
/*TODO*///		ipf = 1;
/*TODO*///	timeslice_period = TIME_IN_HZ(Machine->drv->frames_per_second * ipf);
/*TODO*///	timeslice_timer = timer_alloc(cpu_timeslicecallback);
/*TODO*///	timer_adjust(timeslice_timer, timeslice_period, 0, timeslice_period);
/*TODO*///	
/*TODO*///	/* allocate timers to handle interleave boosts */
/*TODO*///	interleave_boost_timer = timer_alloc(NULL);
/*TODO*///	interleave_boost_timer_end = timer_alloc(end_interleave_boost);
/*TODO*///
/*TODO*///	/*
/*TODO*///	 *	The following code finds all the CPUs that are interrupting in sync with the VBLANK
/*TODO*///	 *	and sets up the VBLANK timer to run at the minimum number of cycles per frame in
/*TODO*///	 *	order to service all the synced interrupts
/*TODO*///	 */
/*TODO*///
/*TODO*///	/* find the CPU with the maximum interrupts per frame */
/*TODO*///	max = 1;
/*TODO*///	for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++)
/*TODO*///	{
/*TODO*///		ipf = Machine->drv->cpu[cpunum].vblank_interrupts_per_frame;
/*TODO*///		if (ipf > max)
/*TODO*///			max = ipf;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* now find the LCD with the rest of the CPUs (brute force - these numbers aren't huge) */
/*TODO*///	vblank_multiplier = max;
/*TODO*///	while (1)
/*TODO*///	{
/*TODO*///		for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++)
/*TODO*///		{
/*TODO*///			ipf = Machine->drv->cpu[cpunum].vblank_interrupts_per_frame;
/*TODO*///			if (ipf > 0 && (vblank_multiplier % ipf) != 0)
/*TODO*///				break;
/*TODO*///		}
/*TODO*///		if (cpunum == cpu_gettotalcpu())
/*TODO*///			break;
/*TODO*///		vblank_multiplier += max;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* initialize the countdown timers and intervals */
/*TODO*///	for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++)
/*TODO*///	{
/*TODO*///		ipf = Machine->drv->cpu[cpunum].vblank_interrupts_per_frame;
/*TODO*///		if (ipf > 0)
/*TODO*///			cpu[cpunum].vblankint_countdown = cpu[cpunum].vblankint_multiplier = vblank_multiplier / ipf;
/*TODO*///		else
/*TODO*///			cpu[cpunum].vblankint_countdown = cpu[cpunum].vblankint_multiplier = -1;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* allocate a vblank timer at the frame rate * the LCD number of interrupts per frame */
/*TODO*///	vblank_period = TIME_IN_HZ(Machine->drv->frames_per_second * vblank_multiplier);
/*TODO*///	vblank_timer = timer_alloc(cpu_vblankcallback);
/*TODO*///	vblank_countdown = vblank_multiplier;
/*TODO*///
/*TODO*///	/*
/*TODO*///	 *		The following code creates individual timers for each CPU whose interrupts are not
/*TODO*///	 *		synced to the VBLANK, and computes the typical number of cycles per interrupt
/*TODO*///	 */
/*TODO*///
/*TODO*///	/* start the CPU interrupt timers */
/*TODO*///	for (cpunum = 0; cpunum < cpu_gettotalcpu(); cpunum++)
/*TODO*///	{
/*TODO*///		ipf = Machine->drv->cpu[cpunum].vblank_interrupts_per_frame;
/*TODO*///
/*TODO*///		/* compute the average number of cycles per interrupt */
/*TODO*///		if (ipf <= 0)
/*TODO*///			ipf = 1;
/*TODO*///		cpu[cpunum].vblankint_period = TIME_IN_HZ(Machine->drv->frames_per_second * ipf);
/*TODO*///		cpu[cpunum].vblankint_timer = timer_alloc(NULL);
/*TODO*///
/*TODO*///		/* see if we need to allocate a CPU timer */
/*TODO*///		ipf = Machine->drv->cpu[cpunum].timed_interrupts_per_second;
/*TODO*///		if (ipf)
/*TODO*///		{
/*TODO*///			cpu[cpunum].timedint_period = cpu_computerate(ipf);
/*TODO*///			cpu[cpunum].timedint_timer = timer_alloc(cpu_timedintcallback);
/*TODO*///			timer_adjust(cpu[cpunum].timedint_timer, cpu[cpunum].timedint_period, cpunum, cpu[cpunum].timedint_period);
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* note that since we start the first frame on the refresh, we can't pulse starting
/*TODO*///	   immediately; instead, we back up one VBLANK period, and inch forward until we hit
/*TODO*///	   positive time. That time will be the time of the first VBLANK timer callback */
/*TODO*///	first_time = -TIME_IN_USEC(Machine->drv->vblank_duration) + vblank_period;
/*TODO*///	while (first_time < 0)
/*TODO*///	{
/*TODO*///		cpu_vblankcallback(-1);
/*TODO*///		first_time += vblank_period;
/*TODO*///	}
/*TODO*///	timer_set(first_time, 0, cpu_firstvblankcallback);
/*TODO*///}
/*TODO*///
/*TODO*///    
}
