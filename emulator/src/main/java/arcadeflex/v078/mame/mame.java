/***************************************************************************

	mame.c

	Controls execution of the core MAME system.

****************************************************************************

	Since there has been confusion in the past over the order of
	initialization and other such things, here it is, all spelled out
	as of May, 2002:

	main()
		- does platform-specific init
		- calls run_game()

		run_game()
			- constructs the machine driver
			- calls init_game_options()

			init_game_options()
				- determines color depth from the options
				- computes orientation from the options

			- initializes the savegame system
			- calls osd_init() to do platform-specific initialization
			- calls init_machine()

			init_machine()
				- initializes the localized strings
				- initializes the input system
				- parses and allocates the game's input ports
				- initializes the hard disk system
				- loads the game's ROMs
				- resets the timer system
				- starts the refresh timer
				- initializes the CPUs
				- loads the configuration file
				- initializes the memory system for the game
				- calls the driver's DRIVER_INIT callback

			- calls run_machine()

			run_machine()
				- calls vh_open()

				vh_open()
					- allocates the palette
					- decodes the graphics
					- computes vector game resolution
					- sets up the artwork
					- calls osd_create_display() to init the display
					- allocates the scrbitmap
					- sets the initial visible_area
					- sets up buffered spriteram
					- creates the user interface font
					- creates the debugger bitmap and font
					- finishes palette initialization

				- initializes the tilemap system
				- calls the driver's VIDEO_START callback
				- starts the audio system
				- disposes of regions marked as disposable
				- calls run_machine_core()

				run_machine_core()
					- shows the copyright screen
					- shows the game warnings
					- initializes the user interface
					- initializes the cheat system
					- calls the driver's NVRAM_HANDLER

	--------------( at this point, we're up and running )---------------------------

					- calls the driver's NVRAM_HANDLER
					- tears down the cheat system
					- saves the game's configuration

				- stops the audio system
				- calls the driver's VIDEO_STOP callback
				- tears down the tilemap system
				- calls vh_close()

				vh_close()
					- frees the decoded graphics
					- frees the fonts
					- calls osd_close_display() to shut down the display
					- tears down the artwork
					- tears down the palette system

			- calls shutdown_machine()

			shutdown_machine()
				- tears down the memory system
				- frees all the memory regions
				- tears down the hard disks
				- tears down the CPU system
				- releases the input ports
				- tears down the input system
				- tears down the localized strings
				- resets the saved state system

			- calls osd_exit() to do platform-specific cleanup

		- exits the program

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package arcadeflex.v078.mame;

import static arcadeflex.v078.mame.mameH.*;
import static common.libc.cstdio.printf;

public class mame
{
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///	
/*TODO*///		Constants
/*TODO*///	
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	#define FRAMES_PER_FPS_UPDATE		12
	
	
	
	/***************************************************************************
	
		Global variables
	
	***************************************************************************/
	
	/* handy globals for other parts of the system */
/*TODO*///	void *record;	/* for -record */
/*TODO*///	void *playback; /* for -playback */
/*TODO*///	int mame_debug; /* !0 when -debug option is specified */
	public static int bailing;	/* set to 1 if the startup is aborted to prevent multiple error messages */
	
	/* the active machine */
	public static RunningMachine active_machine;
	public static RunningMachine Machine = active_machine;

/*TODO*///	/* the active game driver */
/*TODO*///	static const struct GameDriver *gamedrv;
/*TODO*///	static struct InternalMachineDriver internal_drv;
/*TODO*///	
/*TODO*///	/* various game options filled in by the OSD */
/*TODO*///	struct GameOptions options;
/*TODO*///	
/*TODO*///	/* the active video display */
/*TODO*///	static struct mame_display current_display;
/*TODO*///	static UINT8 visible_area_changed;
/*TODO*///	
/*TODO*///	/* video updating */
/*TODO*///	static UINT8 full_refresh_pending;
/*TODO*///	static int last_partial_scanline;
/*TODO*///	
/*TODO*///	/* speed computation */
/*TODO*///	static cycles_t last_fps_time;
/*TODO*///	static int frames_since_last_fps;
/*TODO*///	static int rendered_frames_since_last_fps;
/*TODO*///	static int vfcount;
/*TODO*///	static struct performance_info performance;
/*TODO*///	
/*TODO*///	/* misc other statics */
/*TODO*///	static int settingsloaded;
/*TODO*///	static int leds_status;
/*TODO*///	
/*TODO*///	/* artwork callbacks */
/*TODO*///	#ifndef MESS
/*TODO*///	static struct artwork_callbacks mame_artwork_callbacks =
/*TODO*///	{
/*TODO*///		NULL,
/*TODO*///		artwork_load_artwork_file
/*TODO*///	};
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///	
/*TODO*///		Hard disk interface prototype
/*TODO*///	
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	static struct chd_interface_file *mame_chd_open(const char *filename, const char *mode);
/*TODO*///	static void mame_chd_close(struct chd_interface_file *file);
/*TODO*///	static UINT32 mame_chd_read(struct chd_interface_file *file, UINT64 offset, UINT32 count, void *buffer);
/*TODO*///	static UINT32 mame_chd_write(struct chd_interface_file *file, UINT64 offset, UINT32 count, const void *buffer);
/*TODO*///	static UINT64 mame_chd_length(struct chd_interface_file *file);
/*TODO*///	
/*TODO*///	static struct chd_interface mame_chd_interface =
/*TODO*///	{
/*TODO*///		mame_chd_open,
/*TODO*///		mame_chd_close,
/*TODO*///		mame_chd_read,
/*TODO*///		mame_chd_write,
/*TODO*///		mame_chd_length
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///	
/*TODO*///		Other function prototypes
/*TODO*///	
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	static static static static 
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///	static #endif
/*TODO*///	
/*TODO*///	static void recompute_fps(int skipped_it);
/*TODO*///	static static static static int decode_graphics(const struct GfxDecodeInfo *gfxdecodeinfo);
/*TODO*///	static void compute_aspect_ratio(const struct InternalMachineDriver *drv, int *aspect_x, int *aspect_y);
/*TODO*///	static void scale_vectorgames(int gfx_width, int gfx_height, int *width, int *height);
/*TODO*///	static 
/*TODO*///	#ifdef MESS
/*TODO*///	#define handle_user_interface	handle_mess_user_interface
/*TODO*///	#endif
	
	
	/***************************************************************************
	
		Inline functions
     * @param message	
	***************************************************************************/
	
	/*-------------------------------------------------
		bail_and_print - set the bailing flag and
		print a message if one hasn't already been
		printed
	-------------------------------------------------*/
	
	public static void bail_and_print(String message)
	{
		if (bailing == 0)
		{
			bailing = 1;
			printf("%s\n", message);
		}
	}
	
	
	
	
	/***************************************************************************
	
		Core system management
     * @param game	
     * @return 	
	***************************************************************************/
	
	/*-------------------------------------------------
		run_game - run the given game in a session
	-------------------------------------------------*/
	
	public static int run_game(int game)
	{
		int err = 1;
	
/*TODO*///		begin_resource_tracking();
/*TODO*///	
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///		/* validity checks -- debug build only */
/*TODO*///		if (validitychecks())
/*TODO*///			return 1;
/*TODO*///		#ifdef MESS
/*TODO*///		if (messvaliditychecks()) return 1;
/*TODO*///		#endif
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		/* first give the machine a good cleaning */
/*TODO*///		memset(Machine, 0, sizeof(Machine));
/*TODO*///	
/*TODO*///		/* initialize the driver-related variables in the Machine */
/*TODO*///		Machine->gamedrv = gamedrv = drivers[game];
/*TODO*///		expand_machine_driver(gamedrv->drv, &internal_drv);
/*TODO*///		Machine->drv = &internal_drv;
/*TODO*///	
/*TODO*///		/* initialize the game options */
/*TODO*///		if (init_game_options())
/*TODO*///			return 1;
/*TODO*///	
/*TODO*///		/* if we're coming in with a savegame request, process it now */
/*TODO*///		if (options.savegame)
/*TODO*///			cpu_loadsave_schedule(LOADSAVE_LOAD, options.savegame);
/*TODO*///		else
/*TODO*///			cpu_loadsave_reset();
	
		/* here's the meat of it all */
		bailing = 0;
	
/*TODO*///		/* let the OSD layer start up first */
/*TODO*///		if (osd_init())
/*TODO*///			bail_and_print("Unable to initialize system");
/*TODO*///		else
/*TODO*///		{
/*TODO*///			begin_resource_tracking();
/*TODO*///	
/*TODO*///			/* then finish setting up our local machine */
/*TODO*///			if (init_machine())
/*TODO*///				bail_and_print("Unable to initialize machine emulation");
/*TODO*///			else
/*TODO*///			{
/*TODO*///				/* then run it */
/*TODO*///				if (run_machine())
/*TODO*///					bail_and_print("Unable to start machine emulation");
/*TODO*///				else
					err = 0;
/*TODO*///	
/*TODO*///				/* shutdown the local machine */
/*TODO*///				shutdown_machine();
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* stop tracking resources and exit the OSD layer */
/*TODO*///			end_resource_tracking();
/*TODO*///			osd_exit();
/*TODO*///		}
/*TODO*///	
/*TODO*///		end_resource_tracking();
		return err;
	}
	
	
	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		init_machine - initialize the emulated machine
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	static int init_machine(void)
/*TODO*///	{
/*TODO*///		/* load the localization file */
/*TODO*///		if (uistring_init(options.language_file) != 0)
/*TODO*///		{
/*TODO*///			logerror("uistring_init failed\n");
/*TODO*///			goto cant_load_language_file;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* initialize the input system */
/*TODO*///		if (code_init() != 0)
/*TODO*///		{
/*TODO*///			logerror("code_init failed\n");
/*TODO*///			goto cant_init_input;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* if we have inputs, process them now */
/*TODO*///		if (gamedrv->input_ports)
/*TODO*///		{
/*TODO*///			/* allocate input ports */
/*TODO*///			Machine->input_ports = input_port_allocate(gamedrv->input_ports);
/*TODO*///			if (!Machine->input_ports)
/*TODO*///			{
/*TODO*///				logerror("could not allocate Machine->input_ports\n");
/*TODO*///				goto cant_allocate_input_ports;
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* allocate default input ports */
/*TODO*///			Machine->input_ports_default = input_port_allocate(gamedrv->input_ports);
/*TODO*///			if (!Machine->input_ports_default)
/*TODO*///			{
/*TODO*///				logerror("could not allocate Machine->input_ports_default\n");
/*TODO*///				goto cant_allocate_input_ports_default;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* init the hard drive interface now, before attempting to load */
/*TODO*///		chd_set_interface(&mame_chd_interface);
/*TODO*///	
/*TODO*///		/* load the ROMs if we have some */
/*TODO*///		if (gamedrv->rom && rom_load(gamedrv->rom) != 0)
/*TODO*///		{
/*TODO*///			logerror("readroms failed\n");
/*TODO*///			goto cant_load_roms;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* first init the timers; some CPUs have built-in timers and will need */
/*TODO*///		/* to allocate them up front */
/*TODO*///		timer_init();
/*TODO*///		cpu_init_refresh_timer();
/*TODO*///	
/*TODO*///		/* now set up all the CPUs */
/*TODO*///		cpu_init();
/*TODO*///	
/*TODO*///	#ifdef MESS
/*TODO*///		/* initialize the devices */
/*TODO*///		if (devices_init(gamedrv) || devices_initialload(gamedrv, TRUE))
/*TODO*///		{
/*TODO*///			logerror("devices_init failed\n");
/*TODO*///			goto cant_load_roms;
/*TODO*///		}
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		/* load input ports settings (keys, dip switches, and so on) */
/*TODO*///		settingsloaded = load_input_port_settings();
/*TODO*///	
/*TODO*///		/* multi-session safety - set spriteram size to zero before memory map is set up */
/*TODO*///		spriteram_size = spriteram_2_size = 0;
/*TODO*///	
/*TODO*///		/* initialize the memory system for this game */
/*TODO*///		if (!memory_init())
/*TODO*///		{
/*TODO*///			logerror("memory_init failed\n");
/*TODO*///			goto cant_init_memory;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* call the game driver's init function */
/*TODO*///		if (gamedrv->driver_init)
/*TODO*///			(*gamedrv->driver_init)();
/*TODO*///	
/*TODO*///	#ifdef MESS
/*TODO*///		/* initialize the devices */
/*TODO*///		if (devices_initialload(gamedrv, FALSE))
/*TODO*///		{
/*TODO*///			logerror("devices_initialload failed\n");
/*TODO*///			goto cant_load_roms;
/*TODO*///		}
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		return 0;
/*TODO*///	
/*TODO*///	cant_init_memory:
/*TODO*///	cant_load_roms:
/*TODO*///		input_port_free(Machine->input_ports_default);
/*TODO*///		Machine->input_ports_default = 0;
/*TODO*///	cant_allocate_input_ports_default:
/*TODO*///		input_port_free(Machine->input_ports);
/*TODO*///		Machine->input_ports = 0;
/*TODO*///	cant_allocate_input_ports:
/*TODO*///		code_close();
/*TODO*///	cant_init_input:
/*TODO*///	cant_load_language_file:
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		run_machine - start the various subsystems
/*TODO*///		and the CPU emulation; returns non zero in
/*TODO*///		case of error
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	static int run_machine(void)
/*TODO*///	{
/*TODO*///		int res = 1;
/*TODO*///	
/*TODO*///		/* start the video hardware */
/*TODO*///		if (vh_open())
/*TODO*///			bail_and_print("Unable to start video emulation");
/*TODO*///		else
/*TODO*///		{
/*TODO*///			/* initialize tilemaps */
/*TODO*///			tilemap_init();
/*TODO*///	
/*TODO*///			/* start up the driver's video */
/*TODO*///			if (Machine->drv->video_start && (*Machine->drv->video_start)())
/*TODO*///				bail_and_print("Unable to start video emulation");
/*TODO*///			else
/*TODO*///			{
/*TODO*///				/* start the audio system */
/*TODO*///				if (sound_start())
/*TODO*///					bail_and_print("Unable to start audio emulation");
/*TODO*///				else
/*TODO*///				{
/*TODO*///					int region;
/*TODO*///	
/*TODO*///					/* free memory regions allocated with REGIONFLAG_DISPOSE (typically gfx roms) */
/*TODO*///					for (region = 0; region < MAX_MEMORY_REGIONS; region++)
/*TODO*///						if (Machine->memory_region[region].flags & ROMREGION_DISPOSE)
/*TODO*///						{
/*TODO*///							int i;
/*TODO*///	
/*TODO*///							/* invalidate contents to avoid subtle bugs */
/*TODO*///							for (i = 0; i < memory_region_length(region); i++)
/*TODO*///								memory_region(region)[i] = rand();
/*TODO*///							free(Machine->memory_region[region].base);
/*TODO*///							Machine->memory_region[region].base = 0;
/*TODO*///						}
/*TODO*///	
/*TODO*///					/* now do the core execution */
/*TODO*///					run_machine_core();
/*TODO*///					res = 0;
/*TODO*///	
/*TODO*///					/* store the sound system */
/*TODO*///					sound_stop();
/*TODO*///				}
/*TODO*///	
/*TODO*///				/* shut down the driver's video and kill and artwork */
/*TODO*///				if (Machine->drv->video_stop)
/*TODO*///					(*Machine->drv->video_stop)();
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* close down the tilemap and video systems */
/*TODO*///			tilemap_close();
/*TODO*///			vh_close();
/*TODO*///		}
/*TODO*///	
/*TODO*///		return res;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		run_machine_core - core execution loop
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	void run_machine_core(void)
/*TODO*///	{
/*TODO*///		/* disable artwork for the start */
/*TODO*///		artwork_enable(0);
/*TODO*///	
/*TODO*///		/* if we didn't find a settings file, show the disclaimer */
/*TODO*///		if (settingsloaded || options.skip_disclaimer || showcopyright(artwork_get_ui_bitmap()) == 0)
/*TODO*///		{
/*TODO*///			/* show info about incorrect behaviour (wrong colors etc.) */
/*TODO*///			if (showgamewarnings(artwork_get_ui_bitmap()) == 0)
/*TODO*///			{
/*TODO*///				/* show info about the game */
/*TODO*///				if (options.skip_gameinfo || showgameinfo(artwork_get_ui_bitmap()) == 0)
/*TODO*///				{
/*TODO*///					init_user_interface();
/*TODO*///	
/*TODO*///					/* enable artwork now */
/*TODO*///					artwork_enable(1);
/*TODO*///	
/*TODO*///					/* disable cheat if no roms */
/*TODO*///					if (!gamedrv->rom)
/*TODO*///						options.cheat = 0;
/*TODO*///	
/*TODO*///					/* start the cheat engine */
/*TODO*///					if (options.cheat)
/*TODO*///						InitCheat();
/*TODO*///	
/*TODO*///					/* load the NVRAM now */
/*TODO*///					if (Machine->drv->nvram_handler)
/*TODO*///					{
/*TODO*///						mame_file *nvram_file = mame_fopen(Machine->gamedrv->name, 0, FILETYPE_NVRAM, 0);
/*TODO*///						(*Machine->drv->nvram_handler)(nvram_file, 0);
/*TODO*///						if (nvram_file)
/*TODO*///							mame_fclose(nvram_file);
/*TODO*///					}
/*TODO*///	
/*TODO*///					/* run the emulation! */
/*TODO*///					cpu_run();
/*TODO*///	
/*TODO*///					/* save the NVRAM */
/*TODO*///					if (Machine->drv->nvram_handler)
/*TODO*///					{
/*TODO*///						mame_file *nvram_file = mame_fopen(Machine->gamedrv->name, 0, FILETYPE_NVRAM, 1);
/*TODO*///						if (nvram_file != NULL)
/*TODO*///						{
/*TODO*///							(*Machine->drv->nvram_handler)(nvram_file, 1);
/*TODO*///							mame_fclose(nvram_file);
/*TODO*///						}
/*TODO*///					}
/*TODO*///	
/*TODO*///					/* stop the cheat engine */
/*TODO*///					if (options.cheat)
/*TODO*///						StopCheat();
/*TODO*///	
/*TODO*///					/* save input ports settings */
/*TODO*///					save_input_port_settings();
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		shutdown_machine - tear down the emulated
/*TODO*///		machine
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void shutdown_machine(void)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///	#ifdef MESS
/*TODO*///		/* close down any devices */
/*TODO*///		devices_exit();
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		/* release any allocated memory */
/*TODO*///		memory_shutdown();
/*TODO*///	
/*TODO*///		/* free the memory allocated for various regions */
/*TODO*///		for (i = 0; i < MAX_MEMORY_REGIONS; i++)
/*TODO*///			free_memory_region(i);
/*TODO*///	
/*TODO*///		/* close all hard drives */
/*TODO*///		chd_close_all();
/*TODO*///	
/*TODO*///		/* reset the CPU system */
/*TODO*///		cpu_exit();
/*TODO*///	
/*TODO*///		/* free the memory allocated for input ports definition */
/*TODO*///		input_port_free(Machine->input_ports);
/*TODO*///		input_port_free(Machine->input_ports_default);
/*TODO*///	
/*TODO*///		/* close down the input system */
/*TODO*///		code_close();
/*TODO*///	
/*TODO*///		/* reset the saved states */
/*TODO*///		state_save_reset();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		mame_pause - pause or resume the system
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	void mame_pause(int pause)
/*TODO*///	{
/*TODO*///		osd_pause(pause);
/*TODO*///		osd_sound_enable(!pause);
/*TODO*///		palette_set_global_brightness_adjust(pause ? options.pause_bright : 1.00);
/*TODO*///		schedule_full_refresh();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		expand_machine_driver - construct a machine
/*TODO*///		driver from the macroized state
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	void expand_machine_driver(void (*constructor)(struct InternalMachineDriver *), struct InternalMachineDriver *output)
/*TODO*///	{
/*TODO*///		/* keeping this function allows us to pre-init the driver before constructing it */
/*TODO*///		memset(output, 0, sizeof(*output));
/*TODO*///		(*constructor)(output);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		vh_open - start up the video system
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	static int vh_open(void)
/*TODO*///	{
/*TODO*///		struct osd_create_params params;
/*TODO*///		struct artwork_callbacks *artcallbacks;
/*TODO*///		int bmwidth = Machine->drv->screen_width;
/*TODO*///		int bmheight = Machine->drv->screen_height;
/*TODO*///	
/*TODO*///		/* first allocate the necessary palette structures */
/*TODO*///		if (palette_start())
/*TODO*///			goto cant_start_palette;
/*TODO*///	
/*TODO*///		/* convert the gfx ROMs into character sets. This is done BEFORE calling the driver's */
/*TODO*///		/* palette_init() routine because it might need to check the Machine->gfx[] data */
/*TODO*///		if (Machine->drv->gfxdecodeinfo)
/*TODO*///			if (decode_graphics(Machine->drv->gfxdecodeinfo))
/*TODO*///				goto cant_decode_graphics;
/*TODO*///	
/*TODO*///		/* if we're a vector game, override the screen width and height */
/*TODO*///		if (Machine->drv->video_attributes & VIDEO_TYPE_VECTOR)
/*TODO*///			scale_vectorgames(options.vector_width, options.vector_height, &bmwidth, &bmheight);
/*TODO*///	
/*TODO*///		/* compute the visible area for raster games */
/*TODO*///		if (!(Machine->drv->video_attributes & VIDEO_TYPE_VECTOR))
/*TODO*///		{
/*TODO*///			params.width = Machine->drv->default_visible_area.max_x - Machine->drv->default_visible_area.min_x + 1;
/*TODO*///			params.height = Machine->drv->default_visible_area.max_y - Machine->drv->default_visible_area.min_y + 1;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			params.width = bmwidth;
/*TODO*///			params.height = bmheight;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* fill in the rest of the display parameters */
/*TODO*///		compute_aspect_ratio(Machine->drv, &params.aspect_x, &params.aspect_y);
/*TODO*///		params.depth = Machine->color_depth;
/*TODO*///		params.colors = palette_get_total_colors_with_ui();
/*TODO*///		params.fps = Machine->drv->frames_per_second;
/*TODO*///		params.video_attributes = Machine->drv->video_attributes;
/*TODO*///		params.orientation = Machine->orientation;
/*TODO*///	
/*TODO*///	#ifdef MESS
/*TODO*///		artcallbacks = &mess_artwork_callbacks;
/*TODO*///	#else
/*TODO*///		artcallbacks = &mame_artwork_callbacks;
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		/* initialize the display through the artwork (and eventually the OSD) layer */
/*TODO*///		if (artwork_create_display(&params, direct_rgb_components, artcallbacks))
/*TODO*///			goto cant_create_display;
/*TODO*///	
/*TODO*///		/* the create display process may update the vector width/height, so recompute */
/*TODO*///		if (Machine->drv->video_attributes & VIDEO_TYPE_VECTOR)
/*TODO*///			scale_vectorgames(options.vector_width, options.vector_height, &bmwidth, &bmheight);
/*TODO*///	
/*TODO*///		/* now allocate the screen bitmap */
/*TODO*///		Machine->scrbitmap = auto_bitmap_alloc_depth(bmwidth, bmheight, Machine->color_depth);
/*TODO*///		if (!Machine->scrbitmap)
/*TODO*///			goto cant_create_scrbitmap;
/*TODO*///	
/*TODO*///		/* set the default visible area */
/*TODO*///		set_visible_area(0,1,0,1);	// make sure everything is recalculated on multiple runs
/*TODO*///		set_visible_area(
/*TODO*///				Machine->drv->default_visible_area.min_x,
/*TODO*///				Machine->drv->default_visible_area.max_x,
/*TODO*///				Machine->drv->default_visible_area.min_y,
/*TODO*///				Machine->drv->default_visible_area.max_y);
/*TODO*///	
/*TODO*///		/* create spriteram buffers if necessary */
/*TODO*///		if (Machine->drv->video_attributes & VIDEO_BUFFERS_SPRITERAM)
/*TODO*///			if (init_buffered_spriteram())
/*TODO*///				goto cant_init_buffered_spriteram;
/*TODO*///	
/*TODO*///		/* build our private user interface font */
/*TODO*///		/* This must be done AFTER osd_create_display() so the function knows the */
/*TODO*///		/* resolution we are running at and can pick a different font depending on it. */
/*TODO*///		/* It must be done BEFORE palette_init() because that will also initialize */
/*TODO*///		/* (through osd_allocate_colors()) the uifont colortable. */
/*TODO*///		Machine->uifont = builduifont();
/*TODO*///		if (Machine->uifont == NULL)
/*TODO*///			goto cant_build_uifont;
/*TODO*///	
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///		/* if the debugger is enabled, initialize its bitmap and font */
/*TODO*///		if (mame_debug)
/*TODO*///		{
/*TODO*///			int depth = options.debug_depth ? options.debug_depth : Machine->color_depth;
/*TODO*///	
/*TODO*///			/* first allocate the debugger bitmap */
/*TODO*///			Machine->debug_bitmap = auto_bitmap_alloc_depth(options.debug_width, options.debug_height, depth);
/*TODO*///			if (!Machine->debug_bitmap)
/*TODO*///				goto cant_create_debug_bitmap;
/*TODO*///	
/*TODO*///			/* then create the debugger font */
/*TODO*///			Machine->debugger_font = build_debugger_font();
/*TODO*///			if (Machine->debugger_font == NULL)
/*TODO*///				goto cant_build_debugger_font;
/*TODO*///		}
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		/* initialize the palette - must be done after osd_create_display() */
/*TODO*///		if (palette_init())
/*TODO*///			goto cant_init_palette;
/*TODO*///	
/*TODO*///		/* force the first update to be full */
/*TODO*///		set_vh_global_attribute(NULL, 0);
/*TODO*///	
/*TODO*///		/* reset performance data */
/*TODO*///		last_fps_time = osd_cycles();
/*TODO*///		rendered_frames_since_last_fps = frames_since_last_fps = 0;
/*TODO*///		performance.game_speed_percent = 100;
/*TODO*///		performance.frames_per_second = Machine->drv->frames_per_second;
/*TODO*///		performance.vector_updates_last_second = 0;
/*TODO*///	
/*TODO*///		/* reset video statics and get out of here */
/*TODO*///		pdrawgfx_shadow_lowpri = 0;
/*TODO*///		leds_status = 0;
/*TODO*///	
/*TODO*///		return 0;
/*TODO*///	
/*TODO*///	cant_init_palette:
/*TODO*///	
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///	cant_build_debugger_font:
/*TODO*///	cant_create_debug_bitmap:
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	cant_build_uifont:
/*TODO*///	cant_init_buffered_spriteram:
/*TODO*///	cant_create_scrbitmap:
/*TODO*///	cant_create_display:
/*TODO*///	cant_decode_graphics:
/*TODO*///	cant_start_palette:
/*TODO*///		vh_close();
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		vh_close - close down the video system
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void vh_close(void)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		/* free all the graphics elements */
/*TODO*///		for (i = 0; i < MAX_GFX_ELEMENTS; i++)
/*TODO*///		{
/*TODO*///			freegfx(Machine->gfx[i]);
/*TODO*///			Machine->gfx[i] = 0;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* free the font elements */
/*TODO*///		if (Machine->uifont)
/*TODO*///		{
/*TODO*///			freegfx(Machine->uifont);
/*TODO*///			Machine->uifont = NULL;
/*TODO*///		}
/*TODO*///		if (Machine->debugger_font)
/*TODO*///		{
/*TODO*///			freegfx(Machine->debugger_font);
/*TODO*///			Machine->debugger_font = NULL;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* close down the OSD layer's display */
/*TODO*///		osd_close_display();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		compute_aspect_ratio - determine the aspect
/*TODO*///		ratio encoded in the video attributes
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void compute_aspect_ratio(const struct InternalMachineDriver *drv, int *aspect_x, int *aspect_y)
/*TODO*///	{
/*TODO*///		/* if it's explicitly specified, use it */
/*TODO*///		if (drv->aspect_x && drv->aspect_y)
/*TODO*///		{
/*TODO*///			*aspect_x = drv->aspect_x;
/*TODO*///			*aspect_y = drv->aspect_y;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* otherwise, attempt to deduce the result */
/*TODO*///		else if (!(drv->video_attributes & VIDEO_DUAL_MONITOR))
/*TODO*///		{
/*TODO*///			*aspect_x = 4;
/*TODO*///			*aspect_y = (drv->video_attributes & VIDEO_DUAL_MONITOR) ? 6 : 3;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		init_game_options - initialize the various
/*TODO*///		game options
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	static int init_game_options(void)
/*TODO*///	{
/*TODO*///		/* copy some settings into easier-to-handle variables */
/*TODO*///		record	   = options.record;
/*TODO*///		playback   = options.playback;
/*TODO*///		mame_debug = options.mame_debug;
/*TODO*///	
/*TODO*///		/* determine the color depth */
/*TODO*///		Machine->color_depth = 16;
/*TODO*///		alpha_active = 0;
/*TODO*///		if (Machine->drv->video_attributes & VIDEO_RGB_DIRECT)
/*TODO*///		{
/*TODO*///			/* first pick a default */
/*TODO*///			if (Machine->drv->video_attributes & VIDEO_NEEDS_6BITS_PER_GUN)
/*TODO*///				Machine->color_depth = 32;
/*TODO*///			else
/*TODO*///				Machine->color_depth = 15;
/*TODO*///	
/*TODO*///			/* now allow overrides */
/*TODO*///			if (options.color_depth == 15 || options.color_depth == 32)
/*TODO*///				Machine->color_depth = options.color_depth;
/*TODO*///	
/*TODO*///			/* enable alpha for direct video modes */
/*TODO*///			alpha_active = 1;
/*TODO*///			alpha_init();
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* update the vector width/height with defaults */
/*TODO*///		if (options.vector_width == 0) options.vector_width = 640;
/*TODO*///		if (options.vector_height == 0) options.vector_height = 480;
/*TODO*///	
/*TODO*///		/* initialize the samplerate */
/*TODO*///		Machine->sample_rate = options.samplerate;
/*TODO*///	
/*TODO*///		/* get orientation right */
/*TODO*///		Machine->orientation = ROT0;
/*TODO*///		Machine->ui_orientation = options.ui_orientation;
/*TODO*///	
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		decode_graphics - decode the graphics
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	static int decode_graphics(const struct GfxDecodeInfo *gfxdecodeinfo)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		/* loop over all elements */
/*TODO*///		for (i = 0; i < MAX_GFX_ELEMENTS && gfxdecodeinfo[i].memory_region != -1; i++)
/*TODO*///		{
/*TODO*///			int region_length = 8 * memory_region_length(gfxdecodeinfo[i].memory_region);
/*TODO*///			UINT8 *region_base = memory_region(gfxdecodeinfo[i].memory_region);
/*TODO*///			struct GfxLayout glcopy;
/*TODO*///			int j;
/*TODO*///	
/*TODO*///			/* make a copy of the layout */
/*TODO*///			glcopy = *gfxdecodeinfo[i].gfxlayout;
/*TODO*///	
/*TODO*///			/* if the character count is a region fraction, compute the effective total */
/*TODO*///			if (IS_FRAC(glcopy.total))
/*TODO*///				glcopy.total = region_length / glcopy.charincrement * FRAC_NUM(glcopy.total) / FRAC_DEN(glcopy.total);
/*TODO*///	
/*TODO*///			/* loop over all the planes, converting fractions */
/*TODO*///			for (j = 0; j < MAX_GFX_PLANES; j++)
/*TODO*///			{
/*TODO*///				int value = glcopy.planeoffset[j];
/*TODO*///				if (IS_FRAC(value))
/*TODO*///					glcopy.planeoffset[j] = FRAC_OFFSET(value) + region_length * FRAC_NUM(value) / FRAC_DEN(value);
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* loop over all the X/Y offsets, converting fractions */
/*TODO*///			for (j = 0; j < MAX_GFX_SIZE; j++)
/*TODO*///			{
/*TODO*///				int value = glcopy.xoffset[j];
/*TODO*///				if (IS_FRAC(value))
/*TODO*///					glcopy.xoffset[j] = FRAC_OFFSET(value) + region_length * FRAC_NUM(value) / FRAC_DEN(value);
/*TODO*///	
/*TODO*///				value = glcopy.yoffset[j];
/*TODO*///				if (IS_FRAC(value))
/*TODO*///					glcopy.yoffset[j] = FRAC_OFFSET(value) + region_length * FRAC_NUM(value) / FRAC_DEN(value);
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* some games increment on partial tile boundaries; to handle this without reading */
/*TODO*///			/* past the end of the region, we may need to truncate the count */
/*TODO*///			/* an example is the games in metro.c */
/*TODO*///			if (glcopy.planeoffset[0] == GFX_RAW)
/*TODO*///			{
/*TODO*///				int base = gfxdecodeinfo[i].start;
/*TODO*///				int end = region_length/8;
/*TODO*///				while (glcopy.total > 0)
/*TODO*///				{
/*TODO*///					int elementbase = base + (glcopy.total - 1) * glcopy.charincrement / 8;
/*TODO*///					int lastpixelbase = elementbase + glcopy.height * glcopy.yoffset[0] / 8 - 1;
/*TODO*///					if (lastpixelbase < end)
/*TODO*///						break;
/*TODO*///					glcopy.total--;
/*TODO*///				}
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* now decode the actual graphics */
/*TODO*///			if ((Machine->gfx[i] = decodegfx(region_base + gfxdecodeinfo[i].start, &glcopy)) == 0)
/*TODO*///			{
/*TODO*///				bailing = 1;
/*TODO*///				printf("Out of memory decoding gfx\n");
/*TODO*///				return 1;
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* if we have a remapped colortable, point our local colortable to it */
/*TODO*///			if (Machine->remapped_colortable)
/*TODO*///				Machine->gfx[i]->colortable = &Machine->remapped_colortable[gfxdecodeinfo[i].color_codes_start];
/*TODO*///			Machine->gfx[i]->total_colors = gfxdecodeinfo[i].total_color_codes;
/*TODO*///		}
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		scale_vectorgames - scale the vector games
/*TODO*///		to a given resolution
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void scale_vectorgames(int gfx_width, int gfx_height, int *width, int *height)
/*TODO*///	{
/*TODO*///		double x_scale, y_scale, scale;
/*TODO*///	
/*TODO*///		/* compute the scale values */
/*TODO*///		x_scale = (double)gfx_width / (double)(*width);
/*TODO*///		y_scale = (double)gfx_height / (double)(*height);
/*TODO*///	
/*TODO*///		/* pick the smaller scale factor */
/*TODO*///		scale = (x_scale < y_scale) ? x_scale : y_scale;
/*TODO*///	
/*TODO*///		/* compute the new size */
/*TODO*///		*width = (int)((double)*width * scale);
/*TODO*///		*height = (int)((double)*height * scale);
/*TODO*///	
/*TODO*///		/* round to the nearest 4 pixel value */
/*TODO*///		*width &= ~3;
/*TODO*///		*height &= ~3;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		init_buffered_spriteram - initialize the
/*TODO*///		double-buffered spriteram
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	static int init_buffered_spriteram(void)
/*TODO*///	{
/*TODO*///		/* make sure we have a valid size */
/*TODO*///		if (spriteram_size == 0)
/*TODO*///		{
/*TODO*///			logerror("vh_open():  Video buffers spriteram but spriteram_size is 0\n");
/*TODO*///			return 0;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* allocate memory for the back buffer */
/*TODO*///		buffered_spriteram = auto_malloc(spriteram_size);
/*TODO*///		if (buffered_spriteram == 0)
/*TODO*///			return 1;
/*TODO*///	
/*TODO*///		/* register for saving it */
/*TODO*///		state_save_register_UINT8("generic_video", 0, "buffered_spriteram", buffered_spriteram, spriteram_size);
/*TODO*///	
/*TODO*///		/* do the same for the secon back buffer, if present */
/*TODO*///		if (spriteram_2_size)
/*TODO*///		{
/*TODO*///			/* allocate memory */
/*TODO*///			buffered_spriteram_2 = auto_malloc(spriteram_2_size);
/*TODO*///			if (buffered_spriteram_2 == 0)
/*TODO*///				return 1;
/*TODO*///	
/*TODO*///			/* register for saving it */
/*TODO*///			state_save_register_UINT8("generic_video", 0, "buffered_spriteram_2", buffered_spriteram_2, spriteram_2_size);
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* make 16-bit and 32-bit pointer variants */
/*TODO*///		buffered_spriteram16 = (data16_t *)buffered_spriteram;
/*TODO*///		buffered_spriteram32 = (data32_t *)buffered_spriteram;
/*TODO*///		buffered_spriteram16_2 = (data16_t *)buffered_spriteram_2;
/*TODO*///		buffered_spriteram32_2 = (data32_t *)buffered_spriteram_2;
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///	
/*TODO*///		Screen rendering and management.
/*TODO*///	
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		set_visible_area - adjusts the visible portion
/*TODO*///		of the bitmap area dynamically
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	void set_visible_area(int min_x, int max_x, int min_y, int max_y)
/*TODO*///	{
/*TODO*///		if (       Machine->visible_area.min_x == min_x
/*TODO*///				&& Machine->visible_area.max_x == max_x
/*TODO*///				&& Machine->visible_area.min_y == min_y
/*TODO*///				&& Machine->visible_area.max_y == max_y)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* "dirty" the area for the next display update */
/*TODO*///		visible_area_changed = 1;
/*TODO*///	
/*TODO*///		/* set the new values in the Machine struct */
/*TODO*///		Machine->visible_area.min_x = min_x;
/*TODO*///		Machine->visible_area.max_x = max_x;
/*TODO*///		Machine->visible_area.min_y = min_y;
/*TODO*///		Machine->visible_area.max_y = max_y;
/*TODO*///	
/*TODO*///		/* vector games always use the whole bitmap */
/*TODO*///		if (Machine->drv->video_attributes & VIDEO_TYPE_VECTOR)
/*TODO*///		{
/*TODO*///			Machine->absolute_visible_area.min_x = 0;
/*TODO*///			Machine->absolute_visible_area.max_x = Machine->scrbitmap->width - 1;
/*TODO*///			Machine->absolute_visible_area.min_y = 0;
/*TODO*///			Machine->absolute_visible_area.max_y = Machine->scrbitmap->height - 1;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* raster games need to use the visible area */
/*TODO*///		else
/*TODO*///			Machine->absolute_visible_area = Machine->visible_area;
/*TODO*///	
/*TODO*///		/* recompute scanline timing */
/*TODO*///		cpu_compute_scanline_timing();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		schedule_full_refresh - force a full erase
/*TODO*///		and refresh the next frame
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	void schedule_full_refresh(void)
/*TODO*///	{
/*TODO*///		full_refresh_pending = 1;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		reset_partial_updates - reset the partial
/*TODO*///		updating mechanism for a new frame
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	void reset_partial_updates(void)
/*TODO*///	{
/*TODO*///		last_partial_scanline = 0;
/*TODO*///		performance.partial_updates_this_frame = 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		force_partial_update - perform a partial
/*TODO*///		update from the last scanline up to and
/*TODO*///		including the specified scanline
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	void force_partial_update(int scanline)
/*TODO*///	{
/*TODO*///		struct rectangle clip = Machine->visible_area;
/*TODO*///	
/*TODO*///		/* if skipping this frame, bail */
/*TODO*///		if (osd_skip_this_frame())
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* skip if less than the lowest so far */
/*TODO*///		if (scanline < last_partial_scanline)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* if there's a dirty bitmap and we didn't do any partial updates yet, handle it now */
/*TODO*///		if (full_refresh_pending && last_partial_scanline == 0)
/*TODO*///		{
/*TODO*///			fillbitmap(Machine->scrbitmap, get_black_pen(), NULL);
/*TODO*///			full_refresh_pending = 0;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* set the start/end scanlines */
/*TODO*///		if (last_partial_scanline > clip.min_y)
/*TODO*///			clip.min_y = last_partial_scanline;
/*TODO*///		if (scanline < clip.max_y)
/*TODO*///			clip.max_y = scanline;
/*TODO*///	
/*TODO*///		/* render if necessary */
/*TODO*///		if (clip.min_y <= clip.max_y)
/*TODO*///		{
/*TODO*///			profiler_mark(PROFILER_VIDEO);
/*TODO*///			(*Machine->drv->video_update)(Machine->scrbitmap, &clip);
/*TODO*///			performance.partial_updates_this_frame++;
/*TODO*///			profiler_mark(PROFILER_END);
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* remember where we left off */
/*TODO*///		last_partial_scanline = scanline + 1;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		draw_screen - render the final screen bitmap
/*TODO*///		and update any artwork
/*TODO*///	-------------------------------------------------*/
/*TODO*///	int gbPriorityBitmapIsDirty;
/*TODO*///	
/*TODO*///	void draw_screen(void)
/*TODO*///	{
/*TODO*///		/* finish updating the screen */
/*TODO*///		force_partial_update(Machine->visible_area.max_y);
/*TODO*///		if( gbPriorityBitmapIsDirty )
/*TODO*///		{
/*TODO*///			fillbitmap( priority_bitmap, 0x00, NULL );
/*TODO*///			gbPriorityBitmapIsDirty = 0;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		update_video_and_audio - actually call the
/*TODO*///		OSD layer to perform an update
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	void update_video_and_audio(void)
/*TODO*///	{
/*TODO*///		int skipped_it = osd_skip_this_frame();
/*TODO*///	
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///		debug_trace_delay = 0;
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		/* fill in our portion of the display */
/*TODO*///		current_display.changed_flags = 0;
/*TODO*///	
/*TODO*///		/* set the main game bitmap */
/*TODO*///		current_display.game_bitmap = Machine->scrbitmap;
/*TODO*///		current_display.game_bitmap_update = Machine->absolute_visible_area;
/*TODO*///		if (skipped_it == 0)
/*TODO*///			current_display.changed_flags |= GAME_BITMAP_CHANGED;
/*TODO*///	
/*TODO*///		/* set the visible area */
/*TODO*///		current_display.game_visible_area = Machine->absolute_visible_area;
/*TODO*///		if (visible_area_changed)
/*TODO*///			current_display.changed_flags |= GAME_VISIBLE_AREA_CHANGED;
/*TODO*///	
/*TODO*///		/* set the vector dirty list */
/*TODO*///		if (Machine->drv->video_attributes & VIDEO_TYPE_VECTOR)
/*TODO*///			if (!full_refresh_pending && !ui_dirty && !skipped_it)
/*TODO*///			{
/*TODO*///				current_display.vector_dirty_pixels = vector_dirty_list;
/*TODO*///				current_display.changed_flags |= VECTOR_PIXELS_CHANGED;
/*TODO*///			}
/*TODO*///	
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///		/* set the debugger bitmap */
/*TODO*///		current_display.debug_bitmap = Machine->debug_bitmap;
/*TODO*///		if (debugger_bitmap_changed)
/*TODO*///			current_display.changed_flags |= DEBUG_BITMAP_CHANGED;
/*TODO*///		debugger_bitmap_changed = 0;
/*TODO*///	
/*TODO*///		/* adjust the debugger focus */
/*TODO*///		if (debugger_focus != current_display.debug_focus)
/*TODO*///		{
/*TODO*///			current_display.debug_focus = debugger_focus;
/*TODO*///			current_display.changed_flags |= DEBUG_FOCUS_CHANGED;
/*TODO*///		}
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		/* set the LED status */
/*TODO*///		if (leds_status != current_display.led_state)
/*TODO*///		{
/*TODO*///			current_display.led_state = leds_status;
/*TODO*///			current_display.changed_flags |= LED_STATE_CHANGED;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* update with data from other parts of the system */
/*TODO*///		palette_update_display(&current_display);
/*TODO*///	
/*TODO*///		/* render */
/*TODO*///		artwork_update_video_and_audio(&current_display);
/*TODO*///	
/*TODO*///		/* update FPS */
/*TODO*///		recompute_fps(skipped_it);
/*TODO*///	
/*TODO*///		/* reset dirty flags */
/*TODO*///		visible_area_changed = 0;
/*TODO*///		if (ui_dirty) ui_dirty--;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		recompute_fps - recompute the frame rate
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void recompute_fps(int skipped_it)
/*TODO*///	{
/*TODO*///		/* increment the frame counters */
/*TODO*///		frames_since_last_fps++;
/*TODO*///		if (skipped_it == 0)
/*TODO*///			rendered_frames_since_last_fps++;
/*TODO*///	
/*TODO*///		/* if we didn't skip this frame, we may be able to compute a new FPS */
/*TODO*///		if (!skipped_it && frames_since_last_fps >= FRAMES_PER_FPS_UPDATE)
/*TODO*///		{
/*TODO*///			cycles_t cps = osd_cycles_per_second();
/*TODO*///			cycles_t curr = osd_cycles();
/*TODO*///			double seconds_elapsed = (double)(curr - last_fps_time) * (1.0 / (double)cps);
/*TODO*///			double frames_per_sec = (double)frames_since_last_fps / seconds_elapsed;
/*TODO*///	
/*TODO*///			/* compute the performance data */
/*TODO*///			performance.game_speed_percent = 100.0 * frames_per_sec / Machine->drv->frames_per_second;
/*TODO*///			performance.frames_per_second = (double)rendered_frames_since_last_fps / seconds_elapsed;
/*TODO*///	
/*TODO*///			/* reset the info */
/*TODO*///			last_fps_time = curr;
/*TODO*///			frames_since_last_fps = 0;
/*TODO*///			rendered_frames_since_last_fps = 0;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* for vector games, compute the vector update count once/second */
/*TODO*///		vfcount++;
/*TODO*///		if (vfcount >= (int)Machine->drv->frames_per_second)
/*TODO*///		{
/*TODO*///	#ifndef MESS
/*TODO*///			/* from vidhrdw/avgdvg.c */
/*TODO*///			
/*TODO*///			performance.vector_updates_last_second = vector_updates;
/*TODO*///			vector_updates = 0;
/*TODO*///	#endif
/*TODO*///			vfcount -= (int)Machine->drv->frames_per_second;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		updatescreen - handle frameskipping and UI,
/*TODO*///		plus updating the screen during normal
/*TODO*///		operations
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	int updatescreen(void)
/*TODO*///	{
/*TODO*///		/* update sound */
/*TODO*///		sound_update();
/*TODO*///	
/*TODO*///		/* if we're not skipping this frame, draw the screen */
/*TODO*///		if (osd_skip_this_frame() == 0)
/*TODO*///		{
/*TODO*///			profiler_mark(PROFILER_VIDEO);
/*TODO*///			draw_screen();
/*TODO*///			profiler_mark(PROFILER_END);
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* the user interface must be called between vh_update() and osd_update_video_and_audio(), */
/*TODO*///		/* to allow it to overlay things on the game display. We must call it even */
/*TODO*///		/* if the frame is skipped, to keep a consistent timing. */
/*TODO*///		if (handle_user_interface(artwork_get_ui_bitmap()))
/*TODO*///			/* quit if the user asked to */
/*TODO*///			return 1;
/*TODO*///	
/*TODO*///		/* blit to the screen */
/*TODO*///		update_video_and_audio();
/*TODO*///	
/*TODO*///		/* call the end-of-frame callback */
/*TODO*///		if (Machine->drv->video_eof)
/*TODO*///		{
/*TODO*///			profiler_mark(PROFILER_VIDEO);
/*TODO*///			(*Machine->drv->video_eof)();
/*TODO*///			profiler_mark(PROFILER_END);
/*TODO*///		}
/*TODO*///	
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///	
/*TODO*///		Miscellaneous bits & pieces
/*TODO*///	
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		mame_highscore_enabled - return 1 if high
/*TODO*///		scores are enabled
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	int mame_highscore_enabled(void)
/*TODO*///	{
/*TODO*///		/* disable high score when record/playback is on */
/*TODO*///		if (record != 0 || playback != 0)
/*TODO*///			return 0;
/*TODO*///	
/*TODO*///		/* disable high score when cheats are used */
/*TODO*///		if (he_did_cheat != 0)
/*TODO*///			return 0;
/*TODO*///	
/*TODO*///		/* disable high score when playing network game */
/*TODO*///		/* (this forces all networked machines to start from the same state!) */
/*TODO*///	#ifdef MAME_NET
/*TODO*///		if (net_active())
/*TODO*///			return 0;
/*TODO*///	#elif defined XMAME_NET
/*TODO*///		if (osd_net_active())
/*TODO*///			return 0;
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		set_led_status - set the state of a given LED
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	void set_led_status(int num, int on)
/*TODO*///	{
/*TODO*///		if (on)
/*TODO*///			leds_status |=	(1 << num);
/*TODO*///		else
/*TODO*///			leds_status &= ~(1 << num);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		mame_get_performance_info - return performance
/*TODO*///		info
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	const struct performance_info *mame_get_performance_info(void)
/*TODO*///	{
/*TODO*///		return &performance;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		mame_find_cpu_index - return the index of the
/*TODO*///		given CPU, or -1 if not found
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	int mame_find_cpu_index(const char *tag)
/*TODO*///	{
/*TODO*///		int cpunum;
/*TODO*///	
/*TODO*///		for (cpunum = 0; cpunum < MAX_CPU; cpunum++)
/*TODO*///			if (Machine->drv->cpu[cpunum].tag && strcmp(Machine->drv->cpu[cpunum].tag, tag) == 0)
/*TODO*///				return cpunum;
/*TODO*///	
/*TODO*///		return -1;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		machine_add_cpu - add a CPU during machine
/*TODO*///		driver expansion
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	struct MachineCPU *machine_add_cpu(struct InternalMachineDriver *machine, const char *tag, int type, int cpuclock)
/*TODO*///	{
/*TODO*///		int cpunum;
/*TODO*///	
/*TODO*///		for (cpunum = 0; cpunum < MAX_CPU; cpunum++)
/*TODO*///			if (machine->cpu[cpunum].cpu_type == 0)
/*TODO*///			{
/*TODO*///				machine->cpu[cpunum].tag = tag;
/*TODO*///				machine->cpu[cpunum].cpu_type = type;
/*TODO*///				machine->cpu[cpunum].cpu_clock = cpuclock;
/*TODO*///				return &machine->cpu[cpunum];
/*TODO*///			}
/*TODO*///	
/*TODO*///		logerror("Out of CPU's!\n");
/*TODO*///		return NULL;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		machine_find_cpu - find a tagged CPU during
/*TODO*///		machine driver expansion
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	struct MachineCPU *machine_find_cpu(struct InternalMachineDriver *machine, const char *tag)
/*TODO*///	{
/*TODO*///		int cpunum;
/*TODO*///	
/*TODO*///		for (cpunum = 0; cpunum < MAX_CPU; cpunum++)
/*TODO*///			if (machine->cpu[cpunum].tag && strcmp(machine->cpu[cpunum].tag, tag) == 0)
/*TODO*///				return &machine->cpu[cpunum];
/*TODO*///	
/*TODO*///		logerror("Can't find CPU '%s'!\n", tag);
/*TODO*///		return NULL;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		machine_remove_cpu - remove a tagged CPU
/*TODO*///		during machine driver expansion
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	void machine_remove_cpu(struct InternalMachineDriver *machine, const char *tag)
/*TODO*///	{
/*TODO*///		int cpunum;
/*TODO*///	
/*TODO*///		for (cpunum = 0; cpunum < MAX_CPU; cpunum++)
/*TODO*///			if (machine->cpu[cpunum].tag && strcmp(machine->cpu[cpunum].tag, tag) == 0)
/*TODO*///			{
/*TODO*///				memmove(&machine->cpu[cpunum], &machine->cpu[cpunum + 1], sizeof(machine->cpu[0]) * (MAX_CPU - cpunum - 1));
/*TODO*///				memset(&machine->cpu[MAX_CPU - 1], 0, sizeof(machine->cpu[0]));
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///		logerror("Can't find CPU '%s'!\n", tag);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		machine_add_sound - add a sound system during
/*TODO*///		machine driver expansion
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	struct MachineSound *machine_add_sound(struct InternalMachineDriver *machine, const char *tag, int type, void *sndintf)
/*TODO*///	{
/*TODO*///		int soundnum;
/*TODO*///	
/*TODO*///		for (soundnum = 0; soundnum < MAX_SOUND; soundnum++)
/*TODO*///			if (machine->sound[soundnum].sound_type == 0)
/*TODO*///			{
/*TODO*///				machine->sound[soundnum].tag = tag;
/*TODO*///				machine->sound[soundnum].sound_type = type;
/*TODO*///				machine->sound[soundnum].sound_interface = sndintf;
/*TODO*///				return &machine->sound[soundnum];
/*TODO*///			}
/*TODO*///	
/*TODO*///		logerror("Out of sounds!\n");
/*TODO*///		return NULL;
/*TODO*///	
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		machine_find_sound - find a tagged sound
/*TODO*///		system during machine driver expansion
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	struct MachineSound *machine_find_sound(struct InternalMachineDriver *machine, const char *tag)
/*TODO*///	{
/*TODO*///		int soundnum;
/*TODO*///	
/*TODO*///		for (soundnum = 0; soundnum < MAX_SOUND; soundnum++)
/*TODO*///			if (machine->sound[soundnum].tag && strcmp(machine->sound[soundnum].tag, tag) == 0)
/*TODO*///				return &machine->sound[soundnum];
/*TODO*///	
/*TODO*///		logerror("Can't find sound '%s'!\n", tag);
/*TODO*///		return NULL;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		machine_remove_sound - remove a tagged sound
/*TODO*///		system during machine driver expansion
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	void machine_remove_sound(struct InternalMachineDriver *machine, const char *tag)
/*TODO*///	{
/*TODO*///		int soundnum;
/*TODO*///	
/*TODO*///		for (soundnum = 0; soundnum < MAX_SOUND; soundnum++)
/*TODO*///			if (machine->sound[soundnum].tag && strcmp(machine->sound[soundnum].tag, tag) == 0)
/*TODO*///			{
/*TODO*///				memmove(&machine->sound[soundnum], &machine->sound[soundnum + 1], sizeof(machine->sound[0]) * (MAX_SOUND - soundnum - 1));
/*TODO*///				memset(&machine->sound[MAX_SOUND - 1], 0, sizeof(machine->sound[0]));
/*TODO*///				return;
/*TODO*///			}
/*TODO*///	
/*TODO*///		logerror("Can't find sound '%s'!\n", tag);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		mame_chd_open - interface for opening
/*TODO*///		a hard disk image
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	struct chd_interface_file *mame_chd_open(const char *filename, const char *mode)
/*TODO*///	{
/*TODO*///		/* look for read-only drives first in the ROM path */
/*TODO*///		if (mode[0] == 'r' && !strchr(mode, '+'))
/*TODO*///		{
/*TODO*///			const struct GameDriver *drv;
/*TODO*///	
/*TODO*///			/* attempt reading up the chain through the parents */
/*TODO*///			for (drv = Machine->gamedrv; drv != NULL; drv = drv->clone_of)
/*TODO*///			{
/*TODO*///				void* file = mame_fopen(drv->name, filename, FILETYPE_IMAGE, 0);
/*TODO*///	
/*TODO*///				if (file != NULL)
/*TODO*///					return file;
/*TODO*///			}
/*TODO*///	
/*TODO*///			return NULL;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* look for read/write drives in the diff area */
/*TODO*///		return (struct chd_interface_file *)mame_fopen(NULL, filename, FILETYPE_IMAGE_DIFF, 1);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		mame_chd_close - interface for closing
/*TODO*///		a hard disk image
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	void mame_chd_close(struct chd_interface_file *file)
/*TODO*///	{
/*TODO*///		mame_fclose((mame_file *)file);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		mame_chd_read - interface for reading
/*TODO*///		from a hard disk image
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	UINT32 mame_chd_read(struct chd_interface_file *file, UINT64 offset, UINT32 count, void *buffer)
/*TODO*///	{
/*TODO*///		mame_fseek((mame_file *)file, offset, SEEK_SET);
/*TODO*///		return mame_fread((mame_file *)file, buffer, count);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		mame_chd_write - interface for writing
/*TODO*///		to a hard disk image
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	UINT32 mame_chd_write(struct chd_interface_file *file, UINT64 offset, UINT32 count, const void *buffer)
/*TODO*///	{
/*TODO*///		mame_fseek((mame_file *)file, offset, SEEK_SET);
/*TODO*///		return mame_fwrite((mame_file *)file, buffer, count);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*-------------------------------------------------
/*TODO*///		mame_chd_length - interface for getting
/*TODO*///		the length a hard disk image
/*TODO*///	-------------------------------------------------*/
/*TODO*///	
/*TODO*///	UINT64 mame_chd_length(struct chd_interface_file *file)
/*TODO*///	{
/*TODO*///		return mame_fsize((mame_file *)file);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///	
/*TODO*///		Huge bunch of validity checks for the debug build
/*TODO*///	
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///	
/*TODO*///	INLINE int my_stricmp(const char *dst, const char *src)
/*TODO*///	{
/*TODO*///		while (*src && *dst)
/*TODO*///		{
/*TODO*///			if (tolower(*src) != tolower(*dst))
/*TODO*///				return *dst - *src;
/*TODO*///			src++;
/*TODO*///			dst++;
/*TODO*///		}
/*TODO*///		return *dst - *src;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static int validitychecks(void)
/*TODO*///	{
/*TODO*///		int i,j,cpu;
/*TODO*///		UINT8 a,b;
/*TODO*///		int error = 0;
/*TODO*///	
/*TODO*///	
/*TODO*///		a = 0xff;
/*TODO*///		b = a + 1;
/*TODO*///		if (b > a)	{ printf("UINT8 must be 8 bits\n"); error = 1; }
/*TODO*///	
/*TODO*///		if (sizeof(INT8)   != 1)	{ printf("INT8 must be 8 bits\n"); error = 1; }
/*TODO*///		if (sizeof(UINT8)  != 1)	{ printf("UINT8 must be 8 bits\n"); error = 1; }
/*TODO*///		if (sizeof(INT16)  != 2)	{ printf("INT16 must be 16 bits\n"); error = 1; }
/*TODO*///		if (sizeof(UINT16) != 2)	{ printf("UINT16 must be 16 bits\n"); error = 1; }
/*TODO*///		if (sizeof(INT32)  != 4)	{ printf("INT32 must be 32 bits\n"); error = 1; }
/*TODO*///		if (sizeof(UINT32) != 4)	{ printf("UINT32 must be 32 bits\n"); error = 1; }
/*TODO*///		if (sizeof(INT64)  != 8)	{ printf("INT64 must be 64 bits\n"); error = 1; }
/*TODO*///		if (sizeof(UINT64) != 8)	{ printf("UINT64 must be 64 bits\n"); error = 1; }
/*TODO*///	
/*TODO*///		for (i = 0;drivers[i];i++)
/*TODO*///		{
/*TODO*///			struct InternalMachineDriver drv;
/*TODO*///			const struct RomModule *romp;
/*TODO*///			const struct InputPortTiny *inp;
/*TODO*///	
/*TODO*///			expand_machine_driver(drivers[i]->drv, &drv);
/*TODO*///	
/*TODO*///			if (drivers[i]->clone_of == drivers[i])
/*TODO*///			{
/*TODO*///				printf("%s: %s is set as a clone of itself\n",drivers[i]->source_file,drivers[i]->name);
/*TODO*///				error = 1;
/*TODO*///			}
/*TODO*///	
/*TODO*///			if (drivers[i]->clone_of && drivers[i]->clone_of->clone_of)
/*TODO*///			{
/*TODO*///				if ((drivers[i]->clone_of->clone_of->flags & NOT_A_DRIVER) == 0)
/*TODO*///				{
/*TODO*///					printf("%s: %s is a clone of a clone\n",drivers[i]->source_file,drivers[i]->name);
/*TODO*///					error = 1;
/*TODO*///				}
/*TODO*///			}
/*TODO*///	
/*TODO*///	#if 0
/*TODO*///	//		if (drivers[i]->drv->color_table_len == drivers[i]->drv->total_colors &&
/*TODO*///			if (drivers[i]->drv->color_table_len && drivers[i]->drv->total_colors &&
/*TODO*///					drivers[i]->drv->vh_init_palette == 0)
/*TODO*///			{
/*TODO*///				printf("%s: %s could use color_table_len = 0\n",drivers[i]->source_file,drivers[i]->name);
/*TODO*///				error = 1;
/*TODO*///			}
/*TODO*///	#endif
/*TODO*///	
/*TODO*///			for (j = i+1;drivers[j];j++)
/*TODO*///			{
/*TODO*///				if (!strcmp(drivers[i]->name,drivers[j]->name))
/*TODO*///				{
/*TODO*///					printf("%s: %s is a duplicate name (%s, %s)\n",drivers[i]->source_file,drivers[i]->name,drivers[i]->source_file,drivers[j]->source_file);
/*TODO*///					error = 1;
/*TODO*///				}
/*TODO*///				if (!strcmp(drivers[i]->description,drivers[j]->description))
/*TODO*///				{
/*TODO*///					printf("%s: %s is a duplicate description (%s, %s)\n",drivers[i]->description,drivers[i]->source_file,drivers[i]->name,drivers[j]->name);
/*TODO*///					error = 1;
/*TODO*///				}
/*TODO*///	#ifndef MESS
/*TODO*///				if (drivers[i]->rom && drivers[i]->rom == drivers[j]->rom
/*TODO*///						&& (drivers[i]->flags & NOT_A_DRIVER) == 0
/*TODO*///						&& (drivers[j]->flags & NOT_A_DRIVER) == 0)
/*TODO*///				{
/*TODO*///					printf("%s: %s and %s use the same ROM set\n",drivers[i]->source_file,drivers[i]->name,drivers[j]->name);
/*TODO*///					error = 1;
/*TODO*///				}
/*TODO*///	#endif
/*TODO*///			}
/*TODO*///	
/*TODO*///	#ifndef MESS
/*TODO*///			if ((drivers[i]->flags & NOT_A_DRIVER) == 0)
/*TODO*///			{
/*TODO*///				if (drv.sound[0].sound_type == 0 && (drivers[i]->flags & GAME_NO_SOUND) == 0 &&
/*TODO*///						strcmp(drivers[i]->name,"minivadr"))
/*TODO*///				{
/*TODO*///					printf("%s: %s missing GAME_NO_SOUND flag\n",drivers[i]->source_file,drivers[i]->name);
/*TODO*///					error = 1;
/*TODO*///				}
/*TODO*///			}
/*TODO*///	#endif
/*TODO*///	
/*TODO*///			romp = drivers[i]->rom;
/*TODO*///	
/*TODO*///			if (romp)
/*TODO*///			{
/*TODO*///				int region_type_used[REGION_MAX];
/*TODO*///				int region_length[REGION_MAX];
/*TODO*///				const char *last_name = 0;
/*TODO*///				int count = -1;
/*TODO*///	
/*TODO*///				for (j = 0;j < REGION_MAX;j++)
/*TODO*///				{
/*TODO*///					region_type_used[j] = 0;
/*TODO*///					region_length[j] = 0;
/*TODO*///				}
/*TODO*///	
/*TODO*///				while (!ROMENTRY_ISEND(romp))
/*TODO*///				{
/*TODO*///					const char *c;
/*TODO*///	
/*TODO*///					if (ROMENTRY_ISREGION(romp))
/*TODO*///					{
/*TODO*///						int type = ROMREGION_GETTYPE(romp);
/*TODO*///	
/*TODO*///						count++;
/*TODO*///						if (type && (type >= REGION_MAX || type <= REGION_INVALID))
/*TODO*///						{
/*TODO*///							printf("%s: %s has invalid ROM_REGION type %x\n",drivers[i]->source_file,drivers[i]->name,type);
/*TODO*///							error = 1;
/*TODO*///						}
/*TODO*///	
/*TODO*///						region_type_used[type]++;
/*TODO*///						region_length[type] = region_length[count] = ROMREGION_GETLENGTH(romp);
/*TODO*///					}
/*TODO*///					if (ROMENTRY_ISFILE(romp))
/*TODO*///					{
/*TODO*///						const char *hash;
/*TODO*///	
/*TODO*///						last_name = c = ROM_GETNAME(romp);
/*TODO*///						while (*c)
/*TODO*///						{
/*TODO*///							if (tolower(*c) != *c)
/*TODO*///							{
/*TODO*///								printf("%s: %s has upper case ROM name %s\n",drivers[i]->source_file,drivers[i]->name,ROM_GETNAME(romp));
/*TODO*///								error = 1;
/*TODO*///							}
/*TODO*///							c++;
/*TODO*///						}
/*TODO*///	
/*TODO*///						hash = ROM_GETHASHDATA(romp);
/*TODO*///						if (!hash_verify_string(hash))
/*TODO*///						{
/*TODO*///							printf("%s: rom '%s' has an invalid hash string '%s'\n", drivers[i]->name, ROM_GETNAME(romp), hash);
/*TODO*///							error = 1;
/*TODO*///						}
/*TODO*///					}
/*TODO*///					if (!ROMENTRY_ISREGIONEND(romp))						/* ROM_LOAD_XXX() */
/*TODO*///					{
/*TODO*///						if (ROM_GETOFFSET(romp) + ROM_GETLENGTH(romp) > region_length[count])
/*TODO*///						{
/*TODO*///							printf("%s: %s has ROM %s extending past the defined memory region\n",drivers[i]->source_file,drivers[i]->name,last_name);
/*TODO*///							error = 1;
/*TODO*///						}
/*TODO*///					}
/*TODO*///					romp++;
/*TODO*///				}
/*TODO*///	
/*TODO*///				for (j = 1;j < REGION_MAX;j++)
/*TODO*///				{
/*TODO*///					if (region_type_used[j] > 1)
/*TODO*///					{
/*TODO*///						printf("%s: %s has duplicated ROM_REGION type %x\n",drivers[i]->source_file,drivers[i]->name,j);
/*TODO*///						error = 1;
/*TODO*///					}
/*TODO*///				}
/*TODO*///	
/*TODO*///	
/*TODO*///				for (cpu = 0;cpu < MAX_CPU;cpu++)
/*TODO*///				{
/*TODO*///					if (drv.cpu[cpu].cpu_type)
/*TODO*///					{
/*TODO*///						int alignunit,databus_width;
/*TODO*///	
/*TODO*///	
/*TODO*///						alignunit = cputype_align_unit(drv.cpu[cpu].cpu_type);
/*TODO*///						databus_width = cputype_databus_width(drv.cpu[cpu].cpu_type);
/*TODO*///	
/*TODO*///						if (drv.cpu[cpu].memory_read)
/*TODO*///						{
/*TODO*///							const struct Memory_ReadAddress *mra = drv.cpu[cpu].memory_read;
/*TODO*///	
/*TODO*///							if (!IS_MEMPORT_MARKER(mra) || (mra->end & MEMPORT_DIRECTION_MASK) != MEMPORT_DIRECTION_READ)
/*TODO*///							{
/*TODO*///								printf("%s: %s wrong MEMPORT_READ_START\n",drivers[i]->source_file,drivers[i]->name);
/*TODO*///								error = 1;
/*TODO*///							}
/*TODO*///	
/*TODO*///							switch (databus_width)
/*TODO*///							{
/*TODO*///								case 8:
/*TODO*///									if ((mra->end & MEMPORT_WIDTH_MASK) != MEMPORT_WIDTH_8)
/*TODO*///									{
/*TODO*///										printf("%s: %s cpu #%d uses wrong data width memory handlers! (width = %d, memory = %08x)\n",drivers[i]->source_file,drivers[i]->name,cpu,databus_width,mra->end);
/*TODO*///										error = 1;
/*TODO*///									}
/*TODO*///									break;
/*TODO*///								case 16:
/*TODO*///									if ((mra->end & MEMPORT_WIDTH_MASK) != MEMPORT_WIDTH_16)
/*TODO*///									{
/*TODO*///										printf("%s: %s cpu #%d uses wrong data width memory handlers! (width = %d, memory = %08x)\n",drivers[i]->source_file,drivers[i]->name,cpu,databus_width,mra->end);
/*TODO*///										error = 1;
/*TODO*///									}
/*TODO*///									break;
/*TODO*///								case 32:
/*TODO*///									if ((mra->end & MEMPORT_WIDTH_MASK) != MEMPORT_WIDTH_32)
/*TODO*///									{
/*TODO*///										printf("%s: %s cpu #%d uses wrong data width memory handlers! (width = %d, memory = %08x)\n",drivers[i]->source_file,drivers[i]->name,cpu,databus_width,mra->end);
/*TODO*///										error = 1;
/*TODO*///									}
/*TODO*///									break;
/*TODO*///							}
/*TODO*///	
/*TODO*///							while (!IS_MEMPORT_END(mra))
/*TODO*///							{
/*TODO*///								if (!IS_MEMPORT_MARKER(mra))
/*TODO*///								{
/*TODO*///									if (mra->end < mra->start)
/*TODO*///									{
/*TODO*///										printf("%s: %s wrong memory read handler start = %08x > end = %08x\n",drivers[i]->source_file,drivers[i]->name,mra->start,mra->end);
/*TODO*///										error = 1;
/*TODO*///									}
/*TODO*///									if ((mra->start & (alignunit-1)) != 0 || (mra->end & (alignunit-1)) != (alignunit-1))
/*TODO*///									{
/*TODO*///										printf("%s: %s wrong memory read handler start = %08x, end = %08x ALIGN = %d\n",drivers[i]->source_file,drivers[i]->name,mra->start,mra->end,alignunit);
/*TODO*///										error = 1;
/*TODO*///									}
/*TODO*///								}
/*TODO*///								mra++;
/*TODO*///							}
/*TODO*///						}
/*TODO*///						if (drv.cpu[cpu].memory_write)
/*TODO*///						{
/*TODO*///							const struct Memory_WriteAddress *mwa = drv.cpu[cpu].memory_write;
/*TODO*///	
/*TODO*///							if (mwa->start != MEMPORT_MARKER ||
/*TODO*///									(mwa->end & MEMPORT_DIRECTION_MASK) != MEMPORT_DIRECTION_WRITE)
/*TODO*///							{
/*TODO*///								printf("%s: %s wrong MEMPORT_WRITE_START\n",drivers[i]->source_file,drivers[i]->name);
/*TODO*///								error = 1;
/*TODO*///							}
/*TODO*///	
/*TODO*///							switch (databus_width)
/*TODO*///							{
/*TODO*///								case 8:
/*TODO*///									if ((mwa->end & MEMPORT_WIDTH_MASK) != MEMPORT_WIDTH_8)
/*TODO*///									{
/*TODO*///										printf("%s: %s cpu #%d uses wrong data width memory handlers! (width = %d, memory = %08x)\n",drivers[i]->source_file,drivers[i]->name,cpu,databus_width,mwa->end);
/*TODO*///										error = 1;
/*TODO*///									}
/*TODO*///									break;
/*TODO*///								case 16:
/*TODO*///									if ((mwa->end & MEMPORT_WIDTH_MASK) != MEMPORT_WIDTH_16)
/*TODO*///									{
/*TODO*///										printf("%s: %s cpu #%d uses wrong data width memory handlers! (width = %d, memory = %08x)\n",drivers[i]->source_file,drivers[i]->name,cpu,databus_width,mwa->end);
/*TODO*///										error = 1;
/*TODO*///									}
/*TODO*///									break;
/*TODO*///								case 32:
/*TODO*///									if ((mwa->end & MEMPORT_WIDTH_MASK) != MEMPORT_WIDTH_32)
/*TODO*///									{
/*TODO*///										printf("%s: %s cpu #%d uses wrong data width memory handlers! (width = %d, memory = %08x)\n",drivers[i]->source_file,drivers[i]->name,cpu,databus_width,mwa->end);
/*TODO*///										error = 1;
/*TODO*///									}
/*TODO*///									break;
/*TODO*///							}
/*TODO*///	
/*TODO*///							while (!IS_MEMPORT_END(mwa))
/*TODO*///							{
/*TODO*///								if (!IS_MEMPORT_MARKER(mwa))
/*TODO*///								{
/*TODO*///									if (mwa->end < mwa->start)
/*TODO*///									{
/*TODO*///										printf("%s: %s wrong memory write handler start = %08x > end = %08x\n",drivers[i]->source_file,drivers[i]->name,mwa->start,mwa->end);
/*TODO*///										error = 1;
/*TODO*///									}
/*TODO*///									if ((mwa->start & (alignunit-1)) != 0 || (mwa->end & (alignunit-1)) != (alignunit-1))
/*TODO*///									{
/*TODO*///										printf("%s: %s wrong memory write handler start = %08x, end = %08x ALIGN = %d\n",drivers[i]->source_file,drivers[i]->name,mwa->start,mwa->end,alignunit);
/*TODO*///										error = 1;
/*TODO*///									}
/*TODO*///								}
/*TODO*///								mwa++;
/*TODO*///							}
/*TODO*///						}
/*TODO*///	
/*TODO*///						if (drv.cpu[cpu].port_read)
/*TODO*///						{
/*TODO*///							const struct IO_ReadPort *pra = drv.cpu[cpu].port_read;
/*TODO*///	
/*TODO*///							if (!IS_MEMPORT_MARKER(pra) || (pra->end & MEMPORT_DIRECTION_MASK) != MEMPORT_DIRECTION_READ)
/*TODO*///							{
/*TODO*///								printf("%s: %s wrong PORT_READ_START\n",drivers[i]->source_file,drivers[i]->name);
/*TODO*///								error = 1;
/*TODO*///							}
/*TODO*///	
/*TODO*///							switch (databus_width)
/*TODO*///							{
/*TODO*///								case 8:
/*TODO*///									if ((pra->end & MEMPORT_WIDTH_MASK) != MEMPORT_WIDTH_8)
/*TODO*///									{
/*TODO*///										printf("%s: %s cpu #%d uses wrong data width port handlers! (width = %d, port = %08x)\n",drivers[i]->source_file,drivers[i]->name,cpu,databus_width,pra->end);
/*TODO*///										error = 1;
/*TODO*///									}
/*TODO*///									break;
/*TODO*///								case 16:
/*TODO*///									if ((pra->end & MEMPORT_WIDTH_MASK) != MEMPORT_WIDTH_16)
/*TODO*///									{
/*TODO*///										printf("%s: %s cpu #%d uses wrong data width port handlers! (width = %d, port = %08x)\n",drivers[i]->source_file,drivers[i]->name,cpu,databus_width,pra->end);
/*TODO*///										error = 1;
/*TODO*///									}
/*TODO*///									break;
/*TODO*///								case 32:
/*TODO*///									if ((pra->end & MEMPORT_WIDTH_MASK) != MEMPORT_WIDTH_32)
/*TODO*///									{
/*TODO*///										printf("%s: %s cpu #%d uses wrong data width port handlers! (width = %d, port = %08x)\n",drivers[i]->source_file,drivers[i]->name,cpu,databus_width,pra->end);
/*TODO*///										error = 1;
/*TODO*///									}
/*TODO*///									break;
/*TODO*///							}
/*TODO*///	
/*TODO*///							while (!IS_MEMPORT_END(pra))
/*TODO*///							{
/*TODO*///								if (!IS_MEMPORT_MARKER(pra))
/*TODO*///								{
/*TODO*///									if (pra->end < pra->start)
/*TODO*///									{
/*TODO*///										printf("%s: %s wrong port read handler start = %08x > end = %08x\n",drivers[i]->source_file,drivers[i]->name,pra->start,pra->end);
/*TODO*///										error = 1;
/*TODO*///									}
/*TODO*///									if ((pra->start & (alignunit-1)) != 0 || (pra->end & (alignunit-1)) != (alignunit-1))
/*TODO*///									{
/*TODO*///										printf("%s: %s wrong port read handler start = %08x, end = %08x ALIGN = %d\n",drivers[i]->source_file,drivers[i]->name,pra->start,pra->end,alignunit);
/*TODO*///										error = 1;
/*TODO*///									}
/*TODO*///	
/*TODO*///								}
/*TODO*///								pra++;
/*TODO*///							}
/*TODO*///						}
/*TODO*///	
/*TODO*///						if (drv.cpu[cpu].port_write)
/*TODO*///						{
/*TODO*///							const struct IO_WritePort *pwa = drv.cpu[cpu].port_write;
/*TODO*///	
/*TODO*///							if (pwa->start != MEMPORT_MARKER ||
/*TODO*///									(pwa->end & MEMPORT_DIRECTION_MASK) != MEMPORT_DIRECTION_WRITE)
/*TODO*///							{
/*TODO*///								printf("%s: %s wrong PORT_WRITE_START\n",drivers[i]->source_file,drivers[i]->name);
/*TODO*///								error = 1;
/*TODO*///							}
/*TODO*///	
/*TODO*///							switch (databus_width)
/*TODO*///							{
/*TODO*///								case 8:
/*TODO*///									if ((pwa->end & MEMPORT_WIDTH_MASK) != MEMPORT_WIDTH_8)
/*TODO*///									{
/*TODO*///										printf("%s: %s cpu #%d uses wrong data width port handlers! (width = %d, port = %08x)\n",drivers[i]->source_file,drivers[i]->name,cpu,databus_width,pwa->end);
/*TODO*///										error = 1;
/*TODO*///									}
/*TODO*///									break;
/*TODO*///								case 16:
/*TODO*///									if ((pwa->end & MEMPORT_WIDTH_MASK) != MEMPORT_WIDTH_16)
/*TODO*///									{
/*TODO*///										printf("%s: %s cpu #%d uses wrong data width port handlers! (width = %d, port = %08x)\n",drivers[i]->source_file,drivers[i]->name,cpu,databus_width,pwa->end);
/*TODO*///										error = 1;
/*TODO*///									}
/*TODO*///									break;
/*TODO*///								case 32:
/*TODO*///									if ((pwa->end & MEMPORT_WIDTH_MASK) != MEMPORT_WIDTH_32)
/*TODO*///									{
/*TODO*///										printf("%s: %s cpu #%d uses wrong data width port handlers! (width = %d, port = %08x)\n",drivers[i]->source_file,drivers[i]->name,cpu,databus_width,pwa->end);
/*TODO*///										error = 1;
/*TODO*///									}
/*TODO*///									break;
/*TODO*///							}
/*TODO*///	
/*TODO*///							while (!IS_MEMPORT_END(pwa))
/*TODO*///							{
/*TODO*///								if (!IS_MEMPORT_MARKER(pwa))
/*TODO*///								{
/*TODO*///									if (pwa->end < pwa->start)
/*TODO*///									{
/*TODO*///										printf("%s: %s wrong port write handler start = %08x > end = %08x\n",drivers[i]->source_file,drivers[i]->name,pwa->start,pwa->end);
/*TODO*///										error = 1;
/*TODO*///									}
/*TODO*///									if ((pwa->start & (alignunit-1)) != 0 || (pwa->end & (alignunit-1)) != (alignunit-1))
/*TODO*///									{
/*TODO*///										printf("%s: %s wrong port write handler start = %08x, end = %08x ALIGN = %d\n",drivers[i]->source_file,drivers[i]->name,pwa->start,pwa->end,alignunit);
/*TODO*///										error = 1;
/*TODO*///									}
/*TODO*///	
/*TODO*///								}
/*TODO*///								pwa++;
/*TODO*///							}
/*TODO*///						}
/*TODO*///	
/*TODO*///					}
/*TODO*///				}
/*TODO*///	
/*TODO*///	
/*TODO*///				if (drv.gfxdecodeinfo)
/*TODO*///				{
/*TODO*///					for (j = 0;j < MAX_GFX_ELEMENTS && drv.gfxdecodeinfo[j].memory_region != -1;j++)
/*TODO*///					{
/*TODO*///						int len,avail,k,start;
/*TODO*///						int type = drv.gfxdecodeinfo[j].memory_region;
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///						if (type && (type >= REGION_MAX || type <= REGION_INVALID))
/*TODO*///						{
/*TODO*///							printf("%s: %s has invalid memory region for gfx[%d]\n",drivers[i]->source_file,drivers[i]->name,j);
/*TODO*///							error = 1;
/*TODO*///						}
/*TODO*///	*/
/*TODO*///	
/*TODO*///						if (!IS_FRAC(drv.gfxdecodeinfo[j].gfxlayout->total))
/*TODO*///						{
/*TODO*///							start = 0;
/*TODO*///							for (k = 0;k < MAX_GFX_PLANES;k++)
/*TODO*///							{
/*TODO*///								if (drv.gfxdecodeinfo[j].gfxlayout->planeoffset[k] > start)
/*TODO*///									start = drv.gfxdecodeinfo[j].gfxlayout->planeoffset[k];
/*TODO*///							}
/*TODO*///							start &= ~(drv.gfxdecodeinfo[j].gfxlayout->charincrement-1);
/*TODO*///							len = drv.gfxdecodeinfo[j].gfxlayout->total *
/*TODO*///									drv.gfxdecodeinfo[j].gfxlayout->charincrement;
/*TODO*///							avail = region_length[type]
/*TODO*///									- (drv.gfxdecodeinfo[j].start & ~(drv.gfxdecodeinfo[j].gfxlayout->charincrement/8-1));
/*TODO*///							if ((start + len) / 8 > avail)
/*TODO*///							{
/*TODO*///								printf("%s: %s has gfx[%d] extending past allocated memory\n",drivers[i]->source_file,drivers[i]->name,j);
/*TODO*///								error = 1;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///	
/*TODO*///	
/*TODO*///			inp = drivers[i]->input_ports;
/*TODO*///	
/*TODO*///			if (inp)
/*TODO*///			{
/*TODO*///				while (inp->type != IPT_END)
/*TODO*///				{
/*TODO*///					if (inp->name && inp->name != IP_NAME_DEFAULT)
/*TODO*///					{
/*TODO*///						j = 0;
/*TODO*///	
/*TODO*///						for (j = 0;j < STR_TOTAL;j++)
/*TODO*///						{
/*TODO*///							if (inp->name == ipdn_defaultstrings[j]) break;
/*TODO*///							else if (!my_stricmp(inp->name,ipdn_defaultstrings[j]))
/*TODO*///							{
/*TODO*///								printf("%s: %s must use DEF_STR( %s )\n",drivers[i]->source_file,drivers[i]->name,inp->name);
/*TODO*///								error = 1;
/*TODO*///							}
/*TODO*///						}
/*TODO*///	
/*TODO*///						if (inp->name == DEF_STR( On ) && (inp+1)->name == DEF_STR( Off ))
/*TODO*///						{
/*TODO*///							printf("%s: %s has inverted Off/On dipswitch order\n",drivers[i]->source_file,drivers[i]->name);
/*TODO*///							error = 1;
/*TODO*///						}
/*TODO*///	
/*TODO*///						if (inp->name == DEF_STR( Yes ) && (inp+1)->name == DEF_STR( No ))
/*TODO*///						{
/*TODO*///							printf("%s: %s has inverted No/Yes dipswitch order\n",drivers[i]->source_file,drivers[i]->name);
/*TODO*///							error = 1;
/*TODO*///						}
/*TODO*///	
/*TODO*///						if (!my_stricmp(inp->name,"table"))
/*TODO*///						{
/*TODO*///							printf("%s: %s must use DEF_STR( Cocktail ), not %s\n",drivers[i]->source_file,drivers[i]->name,inp->name);
/*TODO*///							error = 1;
/*TODO*///						}
/*TODO*///	
/*TODO*///						if (inp->name == DEF_STR( Cabinet ) && (inp+1)->name == DEF_STR( Upright )
/*TODO*///								&& inp->default_value != (inp+1)->default_value)
/*TODO*///						{
/*TODO*///							printf("%s: %s Cabinet must default to Upright\n",drivers[i]->source_file,drivers[i]->name);
/*TODO*///							error = 1;
/*TODO*///						}
/*TODO*///	
/*TODO*///						if (inp->name == DEF_STR( Cocktail ) && (inp+1)->name == DEF_STR( Upright ))
/*TODO*///						{
/*TODO*///							printf("%s: %s has inverted Upright/Cocktail dipswitch order\n",drivers[i]->source_file,drivers[i]->name);
/*TODO*///							error = 1;
/*TODO*///						}
/*TODO*///	
/*TODO*///						if (inp->name >= DEF_STR( 9C_1C ) && inp->name <= DEF_STR( Free_Play )
/*TODO*///								&& (inp+1)->name >= DEF_STR( 9C_1C ) && (inp+1)->name <= DEF_STR( Free_Play )
/*TODO*///								&& inp->name >= (inp+1)->name)
/*TODO*///						{
/*TODO*///							printf("%s: %s has unsorted coinage %s > %s\n",drivers[i]->source_file,drivers[i]->name,inp->name,(inp+1)->name);
/*TODO*///							error = 1;
/*TODO*///						}
/*TODO*///	
/*TODO*///						if (inp->name == DEF_STR( Flip_Screen ) && (inp+1)->name != DEF_STR( Off ))
/*TODO*///						{
/*TODO*///							printf("%s: %s has wrong Flip Screen option %s\n",drivers[i]->source_file,drivers[i]->name,(inp+1)->name);
/*TODO*///							error = 1;
/*TODO*///						}
/*TODO*///	
/*TODO*///						if (inp->name == DEF_STR( Demo_Sounds ) && (inp+2)->name == DEF_STR( On )
/*TODO*///								&& inp->default_value != (inp+2)->default_value)
/*TODO*///						{
/*TODO*///							printf("%s: %s Demo Sounds must default to On\n",drivers[i]->source_file,drivers[i]->name);
/*TODO*///							error = 1;
/*TODO*///						}
/*TODO*///	
/*TODO*///						if (inp->name == DEF_STR( Demo_Sounds ) && (inp+1)->name == DEF_STR( No ))
/*TODO*///						{
/*TODO*///							printf("%s: %s has wrong Demo Sounds option No instead of Off\n",drivers[i]->source_file,drivers[i]->name);
/*TODO*///							error = 1;
/*TODO*///						}
/*TODO*///					}
/*TODO*///	
/*TODO*///					inp++;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		return error;
/*TODO*///	}
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	
/*TODO*///	
}
