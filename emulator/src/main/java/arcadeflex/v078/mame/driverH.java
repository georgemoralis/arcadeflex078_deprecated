/*
 * ported to v0.78
 * 
 */
package arcadeflex.v078.mame;

import static arcadeflex.v078.mame.commonH.*;
import arcadeflex.v078.mame.drawgfxH.rectangle;
import static arcadeflex.v078.mame.mameH.machine;
import static common.FuncPtr.*;
import common.PtrLib.UBytePtr;
import java.lang.reflect.Method;

public class driverH {

    /*TODO*///	/***************************************************************************
/*TODO*///
/*TODO*///		driver.h
/*TODO*///
/*TODO*///		Include this with all MAME files. Includes all the core system pieces.
/*TODO*///
/*TODO*///	***************************************************************************/
/*TODO*///
/*TODO*///	#ifndef DRIVER_H
/*TODO*///	#define DRIVER_H
/*TODO*///
/*TODO*///
/*TODO*///	/***************************************************************************
/*TODO*///
/*TODO*///		Macros for declaring common callbacks
/*TODO*///
/*TODO*///	***************************************************************************/
/*TODO*///
/*TODO*///	#define DRIVER_INIT(name)		public static InitDriverPtr init_##name = new InitDriverPtr() { public void handler() 
/*TODO*///
/*TODO*///	#define INTERRUPT_GEN(func)		void func(void)
/*TODO*///
/*TODO*///	#define MACHINE_INIT(name)		public static InitMachinePtr machine_init_##name = new InitMachinePtr() { public void handler() 
/*TODO*///	#define MACHINE_STOP(name)		void machine_stop_##name(void)
/*TODO*///
/*TODO*///	#define NVRAM_HANDLER(name)		void nvram_handler_##name(mame_file *file, int read_or_write)
/*TODO*///
/*TODO*///	#define PALETTE_INIT(name)		void palette_init_##name(UINT16 *colortable, const UINT8 *color_prom)
/*TODO*///
/*TODO*///	#define VIDEO_START(name)		int video_start_##name(void)
/*TODO*///	#define VIDEO_STOP(name)		void video_stop_##name(void)
/*TODO*///	#define VIDEO_EOF(name)			void video_eof_##name(void)
/*TODO*///	#define VIDEO_UPDATE(name)		void video_update_##name(struct mame_bitmap *bitmap, const struct rectangle *cliprect)
/*TODO*///
/*TODO*///	/* NULL versions */
/*TODO*///	#define init_NULL				NULL
/*TODO*///	#define machine_init_NULL 		NULL
/*TODO*///	#define nvram_handler_NULL 		NULL
/*TODO*///	#define palette_init_NULL		NULL
/*TODO*///	#define video_start_NULL 		NULL
/*TODO*///	#define video_stop_NULL 		NULL
/*TODO*///	#define video_eof_NULL 			NULL
/*TODO*///	#define video_update_NULL 		NULL
/*TODO*///	
/*TODO*///	#ifdef MESS
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#ifdef MAME_NET
/*TODO*///	#endif /* MAME_NET */
/*TODO*///	
/*TODO*///	#ifdef MMSND
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///	
/*TODO*///		Macros for building machine drivers
/*TODO*///	
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	/* use this to declare external references to a machine driver */
/*TODO*///	#define MACHINE_DRIVER_EXTERN(game)										\
/*TODO*///		void construct_##game(struct InternalMachineDriver *machine)		\
/*TODO*///	
/*TODO*///	
/*TODO*///	/* start/end tags for the machine driver */
/*TODO*///	#define MACHINE_DRIVER_START(game) 										\
/*TODO*///		void construct_##game(struct InternalMachineDriver *machine)		\
/*TODO*///		{																	\
/*TODO*///			struct MachineCPU *cpu = NULL;									\
/*TODO*///			(void)cpu;														\
/*TODO*///	
/*TODO*///	#define MACHINE_DRIVER_END 												\
/*TODO*///		} };																	\
/*TODO*///	
/*TODO*///	
/*TODO*///	/* importing data from other machine drivers */
/*TODO*///	#define MDRV_IMPORT_FROM(game) 											\
/*TODO*///		construct_##game(machine); 											\
/*TODO*///	
/*TODO*///	
/*TODO*///	/* add/modify/remove/replace CPUs */
/*TODO*///	#define MDRV_CPU_ADD_TAG(tag, type, clock)								\
/*TODO*///		cpu = machine_add_cpu(machine, (tag), CPU_##type, (clock));			\
/*TODO*///	
/*TODO*///	#define MDRV_CPU_ADD(type, clock)										\
/*TODO*///		MDRV_CPU_ADD_TAG(NULL, type, clock)									\
/*TODO*///	
/*TODO*///	#define MDRV_CPU_MODIFY(tag)											\
/*TODO*///		cpu = machine_find_cpu(machine, tag);								\
/*TODO*///	
/*TODO*///	#define MDRV_CPU_REMOVE(tag)											\
/*TODO*///		machine_remove_cpu(machine, tag);									\
/*TODO*///		cpu = NULL;															\
/*TODO*///	
/*TODO*///	#define MDRV_CPU_REPLACE(tag, type, clock)								\
/*TODO*///		cpu = machine_find_cpu(machine, tag);								\
/*TODO*///		if (cpu)															\
/*TODO*///		{																	\
/*TODO*///			cpu->cpu_type = (CPU_##type);									\
/*TODO*///			cpu->cpu_clock = (clock);										\
/*TODO*///		}																	\
/*TODO*///	
/*TODO*///	
/*TODO*///	/* CPU parameters */
/*TODO*///	#define MDRV_CPU_FLAGS(flags)											\
/*TODO*///		if (cpu)															\
/*TODO*///			cpu->cpu_flags = (flags);										\
/*TODO*///	
/*TODO*///	#define MDRV_CPU_CONFIG(config)											\
/*TODO*///		if (cpu)															\
/*TODO*///			cpu->reset_param = &(config);									\
/*TODO*///	
/*TODO*///	#define MDRV_CPU_MEMORY(readmem, writemem)								\
/*TODO*///		if (cpu)															\
/*TODO*///		{																	\
/*TODO*///			cpu->memory_read = (readmem);									\
/*TODO*///			cpu->memory_write = (writemem);									\
/*TODO*///		}																	\
/*TODO*///	
/*TODO*///	#define MDRV_CPU_PORTS(readport, writeport)								\
/*TODO*///		if (cpu)															\
/*TODO*///		{																	\
/*TODO*///			cpu->port_read = (readport);									\
/*TODO*///			cpu->port_write = (writeport);									\
/*TODO*///		}																	\
/*TODO*///	
/*TODO*///	#define MDRV_CPU_VBLANK_INT(func, rate)									\
/*TODO*///		if (cpu)															\
/*TODO*///		{																	\
/*TODO*///			cpu->vblank_interrupt = func;									\
/*TODO*///			cpu->vblank_interrupts_per_frame = (rate);						\
/*TODO*///		}																	\
/*TODO*///	
/*TODO*///	#define MDRV_CPU_PERIODIC_INT(func, rate)								\
/*TODO*///		if (cpu)															\
/*TODO*///		{																	\
/*TODO*///			cpu->timed_interrupt = func;									\
/*TODO*///			cpu->timed_interrupts_per_second = (rate);						\
/*TODO*///		}																	\
/*TODO*///	
	
	/* core parameters */
	public static void MDRV_FRAMES_PER_SECOND(float rate) {
		machine.frames_per_second = (rate);
        }
	
	public static void MDRV_VBLANK_DURATION(int duration) {
		machine.vblank_duration = (duration);
        }
	
/*TODO*///	#define MDRV_INTERLEAVE(interleave)										\
/*TODO*///		machine->cpu_slices_per_frame = (interleave);						\
/*TODO*///	
/*TODO*///	
/*TODO*///	/* core functions */
/*TODO*///	#define MDRV_MACHINE_INIT(name)											\
/*TODO*///		machine->machine_init = machine_init_##name;						\
/*TODO*///	
/*TODO*///	#define MDRV_MACHINE_STOP(name)											\
/*TODO*///		machine->machine_stop = machine_stop_##name;						\
/*TODO*///	
/*TODO*///	#define MDRV_NVRAM_HANDLER(name)										\
/*TODO*///		machine->nvram_handler = nvram_handler_##name;						\
	
	
	/* core video parameters */
	public static void MDRV_VIDEO_ATTRIBUTES(int flags) {
		machine.video_attributes = (flags);
        }
	
/*TODO*///	#define MDRV_ASPECT_RATIO(num, den)										\
/*TODO*///		machine->aspect_x = (num);											\
/*TODO*///		machine->aspect_y = (den);											\
	
	public static void MDRV_SCREEN_SIZE(int width, int height) {
		machine.screen_width = (width);
		machine.screen_height = (height);
        }
	
	public static void MDRV_VISIBLE_AREA(int minx, int maxx, int miny, int maxy) {
		machine.default_visible_area.min_x = (minx);
		machine.default_visible_area.max_x = (maxx);
		machine.default_visible_area.min_y = (miny);
		machine.default_visible_area.max_y = (maxy);
        }
	
/*TODO*///	#define MDRV_GFXDECODE(gfx)												\
/*TODO*///		machine->gfxdecodeinfo = (gfx);										\
	
	public static void MDRV_PALETTE_LENGTH(int length) {
		machine.total_colors = (length);
        }
	
/*TODO*///	#define MDRV_COLORTABLE_LENGTH(length)									\
/*TODO*///		machine->color_table_len = (length);								\
/*TODO*///	

        public static VhPaletteInitPtr GET_palette_init(String name) {
            VhPaletteInitPtr _palOut = null;
            
            try {
                Class _cl = Class.forName("arcadeflex.v078.vidhrdw."+name+".palette_init_"+name);
                
                _palOut = (VhPaletteInitPtr) _cl.newInstance();
               
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
            
            return _palOut;
        }
	/* core video functions */
	public static void MDRV_PALETTE_INIT(String name) {
		machine.init_palette = GET_palette_init(name);
        }

/*TODO*///	#define MDRV_VIDEO_START(name)											\
/*TODO*///		machine->video_start = video_start_##name;							\
/*TODO*///	
/*TODO*///	#define MDRV_VIDEO_STOP(name)											\
/*TODO*///		machine->video_stop = video_stop_##name;							\
/*TODO*///	
/*TODO*///	#define MDRV_VIDEO_EOF(name)											\
/*TODO*///		machine->video_eof = video_eof_##name;								\
/*TODO*///	
/*TODO*///	#define MDRV_VIDEO_UPDATE(name)											\
/*TODO*///		machine->video_update = video_update_##name;						\
/*TODO*///	
/*TODO*///	
/*TODO*///	/* core sound parameters */
/*TODO*///	#define MDRV_SOUND_ATTRIBUTES(flags)									\
/*TODO*///		machine->sound_attributes = (flags);								\
/*TODO*///	
/*TODO*///	
/*TODO*///	/* add/remove/replace sounds */
/*TODO*///	#define MDRV_SOUND_ADD_TAG(tag, type, interface)						\
/*TODO*///		machine_add_sound(machine, (tag), SOUND_##type, &(interface));		\
/*TODO*///	
/*TODO*///	#define MDRV_SOUND_ADD(type, interface)									\
/*TODO*///		MDRV_SOUND_ADD_TAG(NULL, type, interface)							\
/*TODO*///	
/*TODO*///	#define MDRV_SOUND_REMOVE(tag)											\
/*TODO*///		machine_remove_sound(machine, tag);									\
/*TODO*///	
/*TODO*///	#define MDRV_SOUND_REPLACE(tag, type, interface)						\
/*TODO*///		{																	\
/*TODO*///			struct MachineSound *sound = machine_find_sound(machine, tag);	\
/*TODO*///			if (sound)														\
/*TODO*///			{																\
/*TODO*///				sound->sound_type = SOUND_##type;							\
/*TODO*///				sound->sound_interface = &(interface);						\
/*TODO*///			}																\
/*TODO*///		}																	\
/*TODO*///	
/*TODO*///	
/*TODO*///	struct MachineCPU *machine_add_cpu(struct InternalMachineDriver *machine, const char *tag, int type, int cpuclock);
/*TODO*///	struct MachineCPU *machine_find_cpu(struct InternalMachineDriver *machine, const char *tag);
/*TODO*///	void machine_remove_cpu(struct InternalMachineDriver *machine, const char *tag);
/*TODO*///	
/*TODO*///	struct MachineSound *machine_add_sound(struct InternalMachineDriver *machine, const char *tag, int type, void *sndintf);
/*TODO*///	struct MachineSound *machine_find_sound(struct InternalMachineDriver *machine, const char *tag);
/*TODO*///	void machine_remove_sound(struct InternalMachineDriver *machine, const char *tag);
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///	
/*TODO*///		Internal representation of a machine driver, built from the constructor
/*TODO*///	
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	#define MAX_CPU 8	/* MAX_CPU is the maximum number of CPUs which cpuintrf.c */
/*TODO*///						/* can run at the same time. Currently, 8 is enough. */
/*TODO*///	
/*TODO*///	#define MAX_SOUND 5	/* MAX_SOUND is the maximum number of sound subsystems */
/*TODO*///						/* which can run at the same time. Currently, 5 is enough. */
    public static abstract interface MachinePtr {

        public abstract void handler(InternalMachineDriver machine);
    }

    public static class InternalMachineDriver {
        /*TODO*///		struct MachineCPU cpu[MAX_CPU];
		float frames_per_second;
		int vblank_duration;
/*TODO*///		UINT32 cpu_slices_per_frame;
/*TODO*///	
/*TODO*///		void (*machine_init)(void);
/*TODO*///		void (*machine_stop)(void);
/*TODO*///		void (*nvram_handler)(mame_file *file, int read_or_write);
/*TODO*///	
		int /*UINT32*/ video_attributes;
/*TODO*///		UINT32 aspect_x, aspect_y;
		int screen_width,screen_height;
		rectangle default_visible_area = new rectangle();
/*TODO*///		struct GfxDecodeInfo *gfxdecodeinfo;
		int /*UINT32*/ total_colors;
/*TODO*///		UINT32 color_table_len;
	
		VhPaletteInitPtr init_palette;
/*TODO*///		int (*video_start)(void);
/*TODO*///		void (*video_stop)(void);
/*TODO*///		void (*video_eof)(void);
/*TODO*///		void (*video_update)(struct mame_bitmap *bitmap,const struct rectangle *cliprect);
/*TODO*///	
/*TODO*///		UINT32 sound_attributes;
/*TODO*///		struct MachineSound sound[MAX_SOUND];
    };

    	/***************************************************************************
	
		Machine driver constants and flags
	
	***************************************************************************/
	
	/* VBlank is the period when the video beam is outside of the visible area and */
	/* returns from the bottom to the top of the screen to prepare for a new video frame. */
	/* VBlank duration is an important factor in how the game renders itself. MAME */
	/* generates the vblank_interrupt, lets the game run for vblank_duration microseconds, */
	/* and then updates the screen. This faithfully reproduces the behaviour of the real */
	/* hardware. In many cases, the game does video related operations both in its vblank */
	/* interrupt, and in the normal game code; it is therefore important to set up */
	/* vblank_duration accurately to have everything properly in sync. An example of this */
	/* is Commando: if you set vblank_duration to 0, therefore redrawing the screen BEFORE */
	/* the vblank interrupt is executed, sprites will be misaligned when the screen scrolls. */
	
	/* Here are some predefined, TOTALLY ARBITRARY values for vblank_duration, which should */
	/* be OK for most cases. I have NO IDEA how accurate they are compared to the real */
	/* hardware, they could be completely wrong. */
	public static final int DEFAULT_60HZ_VBLANK_DURATION  = 0;
/*TODO*///	#define DEFAULT_30HZ_VBLANK_DURATION 0
/*TODO*///	/* If you use IPT_VBLANK, you need a duration different from 0. */
/*TODO*///	#define DEFAULT_REAL_60HZ_VBLANK_DURATION 2500
/*TODO*///	#define DEFAULT_REAL_30HZ_VBLANK_DURATION 2500
	
	
	/* ----- flags for video_attributes ----- */
	
	/* bit 0 of the video attributes indicates raster or vector video hardware */
	public static final int	VIDEO_TYPE_RASTER   = 0x0000;
	public static final int	VIDEO_TYPE_VECTOR   = 0x0001;
	
/*TODO*///	/* bit 3 of the video attributes indicates that the game's palette has 6 or more bits */
/*TODO*///	/*       per gun, and would therefore require a 24-bit display. This is entirely up to */
/*TODO*///	/*       the OS dependant layer, the bitmap will still be 16-bit. */
/*TODO*///	#define VIDEO_NEEDS_6BITS_PER_GUN	0x0008
/*TODO*///	
/*TODO*///	/* ASG 980417 - added: */
/*TODO*///	/* bit 4 of the video attributes indicates that the driver wants its refresh after */
/*TODO*///	/*       the VBLANK instead of before. */
/*TODO*///	#define	VIDEO_UPDATE_BEFORE_VBLANK	0x0000
/*TODO*///	#define	VIDEO_UPDATE_AFTER_VBLANK	0x0010
/*TODO*///	
/*TODO*///	/* In most cases we assume pixels are square (1:1 aspect ratio) but some games need */
/*TODO*///	/* different proportions, e.g. 1:2 for Blasteroids */
/*TODO*///	#define VIDEO_PIXEL_ASPECT_RATIO_MASK 0x0060
/*TODO*///	#define VIDEO_PIXEL_ASPECT_RATIO_1_1 0x0000
/*TODO*///	#define VIDEO_PIXEL_ASPECT_RATIO_1_2 0x0020
/*TODO*///	#define VIDEO_PIXEL_ASPECT_RATIO_2_1 0x0040
/*TODO*///	
/*TODO*///	#define VIDEO_DUAL_MONITOR			0x0080
/*TODO*///	
/*TODO*///	/* Mish 181099:  See comments in vidhrdw/generic.c for details */
/*TODO*///	#define VIDEO_BUFFERS_SPRITERAM		0x0100
/*TODO*///	
/*TODO*///	/* game wants to use a hicolor or truecolor bitmap (e.g. for alpha blending) */
/*TODO*///	#define VIDEO_RGB_DIRECT 			0x0200
/*TODO*///	
/*TODO*///	/* automatically extend the palette creating a darker copy for shadows */
/*TODO*///	#define VIDEO_HAS_SHADOWS			0x0400
/*TODO*///	
/*TODO*///	/* automatically extend the palette creating a brighter copy for highlights */
/*TODO*///	#define VIDEO_HAS_HIGHLIGHTS		0x0800
/*TODO*///	
/*TODO*///	
/*TODO*///	/* ----- flags for sound_attributes ----- */
/*TODO*///	#define	SOUND_SUPPORTS_STEREO		0x0001
    /**
     * *************************************************************************
     *
     * Game driver structure
     *
     **************************************************************************
     */
    public static class GameDriver {

        public GameDriver(String year, String name, String source,
                RomLoadPtr romload,
                GameDriver parent,
                MachinePtr drv, InputPortPtr input, InitDriverPtr init,
                int monitor, String manufacture, String fullname) {
            this.year = year;
            this.source_file = source;
            this.clone_of = parent;
            this.name = name;
            this.description = fullname;
            this.manufacturer = manufacture;
            //TODO            this.drv = drv;
            //TODO           this.driver_init = init;
            romload.handler();//load the rom
            //TODO            input.handler();//load input
            //TODO            this.input_ports = input_macro;//copy input macro to input ports
            this.rom = rommodule_macro; //copy rommodule_macro to rom
            this.flags = monitor;
        }

        public String source_file;/* set this to __FILE__ */
        public GameDriver clone_of;/*if this is a clone, point to the main version of the game */
        public String name;
        /*TODO*///		const struct SystemBios *bios;	/* if this system has alternate bios roms use this */
/*TODO*///										/* structure to list names and ROM_BIOSFLAGS. */            
        public String description;
        public String year;
        public String manufacturer;
        public InternalMachineDriver drv;
//TODO*/// 	const struct InputPortTiny *input_ports;
/*TODO*///		void (*driver_init)(void);	/* optional function to be called during initialization */
/*TODO*///									/* This is called ONCE, unlike Machine->init_machine */
/*TODO*///									/* which is called every time the game is reset. */
/*TODO*///	
        public RomModule[] rom;
        /*TODO*///	#ifdef MESS
/*TODO*///		void (*sysconfig_ctor)(struct SystemConfigurationParamBlock *cfg);
/*TODO*///		const struct GameDriver *compatible_with;
/*TODO*///	#endif
/*TODO*///
        public int flags;/* orientation and other flags; see defines below */

    };

    /*TODO*///	/***************************************************************************
/*TODO*///	
/*TODO*///		Game driver flags
/*TODO*///	
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	/* ----- values for the flags field ----- */
/*TODO*///	
/*TODO*///	#define ORIENTATION_MASK        	0x0007
/*TODO*///	#define	ORIENTATION_FLIP_X			0x0001	/* mirror everything in the X direction */
/*TODO*///	#define	ORIENTATION_FLIP_Y			0x0002	/* mirror everything in the Y direction */
/*TODO*///	#define ORIENTATION_SWAP_XY			0x0004	/* mirror along the top-left/bottom-right diagonal */
	
    public static final int GAME_NOT_WORKING            = 0x0008;
    public static final int GAME_UNEMULATED_PROTECTION	= 0x0010;	/* game's protection not fully emulated */
    public static final int GAME_WRONG_COLORS           = 0x0020;	/* colors are totally wrong */
    public static final int GAME_IMPERFECT_COLORS       = 0x0040;	/* colors are not 100% accurate, but close */
    public static final int GAME_IMPERFECT_GRAPHICS	= 0x0080;	/* graphics are wrong/incomplete */
    public static final int GAME_NO_COCKTAIL		= 0x0100;	/* screen flip support is missing */
    public static final int GAME_NO_SOUND		= 0x0200;	/* sound is missing */
    public static final int GAME_IMPERFECT_SOUND	= 0x0400;	/* sound is known to be wrong */
    public static final int NOT_A_DRIVER                = 0x4000;
    /* set by the fake "root" driver_0 and by "containers" */
 /*TODO*///												/* e.g. driver_neogeo. */
/*TODO*///	#ifdef MESS
/*TODO*///	#define GAME_COMPUTER               0x8000  /* Driver is a computer (needs full keyboard) */
/*TODO*///	#define GAME_COMPUTER_MODIFIED      0x0800	/* Official? Hack */
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///	
/*TODO*///		Macros for building game drivers
/*TODO*///	
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	#define public static GameDriver driver_NAME	   = new GameDriver("YEAR"	,"NAME"	,"driverH.java"	,rom_NAME,driver_PARENT	,machine_driver_MACHINE	,input_ports_INPUT	,init_INIT	,MONITOR	,	COMPANY,FULLNAME)	\
/*TODO*///	extern const struct GameDriver driver_##PARENT;	\
/*TODO*///	const struct GameDriver driver_##NAME =		\
/*TODO*///	{											\
/*TODO*///		__FILE__,								\
/*TODO*///		&driver_##PARENT,						\
/*TODO*///		#NAME,									\
/*TODO*///		system_bios_0,							\
/*TODO*///		FULLNAME,								\
/*TODO*///		#YEAR,									\
/*TODO*///		COMPANY,								\
/*TODO*///		construct_##MACHINE,					\
/*TODO*///		input_ports_##INPUT,					\
/*TODO*///		init_##INIT,							\
/*TODO*///		rom_##NAME,								\
/*TODO*///		MONITOR									\
/*TODO*///	};
/*TODO*///	
/*TODO*///	#define public static GameDriver driver_NAME	   = new GameDriver("YEAR"	,"NAME"	,"driverH.java"	,rom_NAME,driver_PARENT	,machine_driver_MACHINE	,input_ports_INPUT	,init_INIT	,MONITOR	,	COMPANY,FULLNAME,FLAGS)	\
/*TODO*///	extern const struct GameDriver driver_##PARENT;	\
/*TODO*///	const struct GameDriver driver_##NAME =		\
/*TODO*///	{											\
/*TODO*///		__FILE__,								\
/*TODO*///		&driver_##PARENT,						\
/*TODO*///		#NAME,									\
/*TODO*///		system_bios_0,							\
/*TODO*///		FULLNAME,								\
/*TODO*///		#YEAR,									\
/*TODO*///		COMPANY,								\
/*TODO*///		construct_##MACHINE,					\
/*TODO*///		input_ports_##INPUT,					\
/*TODO*///		init_##INIT,							\
/*TODO*///		rom_##NAME,								\
/*TODO*///		(MONITOR)|(FLAGS)						\
/*TODO*///	};
/*TODO*///	
/*TODO*///	#define GAMEB(YEAR,NAME,PARENT,BIOS,MACHINE,INPUT,INIT,MONITOR,COMPANY,FULLNAME)	\
/*TODO*///	extern const struct GameDriver driver_##PARENT;	\
/*TODO*///	const struct GameDriver driver_##NAME =		\
/*TODO*///	{											\
/*TODO*///		__FILE__,								\
/*TODO*///		&driver_##PARENT,						\
/*TODO*///		#NAME,									\
/*TODO*///		system_bios_##BIOS,						\
/*TODO*///		FULLNAME,								\
/*TODO*///		#YEAR,									\
/*TODO*///		COMPANY,								\
/*TODO*///		construct_##MACHINE,					\
/*TODO*///		input_ports_##INPUT,					\
/*TODO*///		init_##INIT,							\
/*TODO*///		rom_##NAME,								\
/*TODO*///		MONITOR									\
/*TODO*///	};
/*TODO*///	
/*TODO*///	#define GAMEBX(YEAR,NAME,PARENT,BIOS,MACHINE,INPUT,INIT,MONITOR,COMPANY,FULLNAME,FLAGS)	\
/*TODO*///	extern const struct GameDriver driver_##PARENT;	\
/*TODO*///	const struct GameDriver driver_##NAME =		\
/*TODO*///	{											\
/*TODO*///		__FILE__,								\
/*TODO*///		&driver_##PARENT,						\
/*TODO*///		#NAME,									\
/*TODO*///		system_bios_##BIOS,						\
/*TODO*///		FULLNAME,								\
/*TODO*///		#YEAR,									\
/*TODO*///		COMPANY,								\
/*TODO*///		construct_##MACHINE,					\
/*TODO*///		input_ports_##INPUT,					\
/*TODO*///		init_##INIT,							\
/*TODO*///		rom_##NAME,								\
/*TODO*///		(MONITOR)|(FLAGS)						\
/*TODO*///	};

    /* monitor parameters to be used with the GAME() macro */
    public static final int ROT0 = 0;
    /*TODO*///	#define	ROT90	(ORIENTATION_SWAP_XY|ORIENTATION_FLIP_X)	/* rotate clockwise 90 degrees */
/*TODO*///	#define	ROT180	(ORIENTATION_FLIP_X|ORIENTATION_FLIP_Y)		/* rotate 180 degrees */
/*TODO*///	#define	ROT270	(ORIENTATION_SWAP_XY|ORIENTATION_FLIP_Y)	/* rotate counter-clockwise 90 degrees */
/*TODO*///	
/*TODO*///	/* this allows to leave the INIT field empty in the GAME() macro call */
/*TODO*///	#define init_0 0
/*TODO*///	
/*TODO*///	/* this allows to leave the BIOS field empty in the GAMEB() macro call */
/*TODO*///	#define system_bios_0 0
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///	
/*TODO*///		Global variables
/*TODO*///	
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	extern const struct GameDriver *drivers[];
/*TODO*///	extern const struct GameDriver *test_drivers[];
/*TODO*///	
/*TODO*///	#endif
}
