/***************************************************************************

	Atari Return of the Jedi hardware

	driver by Dan Boris

	Games supported:
		* Return of the Jedi

	Known bugs:
		* none at this time

****************************************************************************

	Memory map

****************************************************************************

	========================================================================
	CPU #1
	========================================================================
	0000-07FF   R/W   xxxxxxxx    Z-page Working RAM
	0800-08FF   R/W   xxxxxxxx    NVRAM
	0C00        R     xxxx-xxx    Switch inputs #1
	            R     x-------       (right coin)
	            R     -x------       (left coin)
	            R     --x-----       (aux coin)
	            R     ---x----       (self test)
	            R     -----x--       (left thumb switch)
	            R     ------x-       (fire switches)
	            R     -------x       (right thumb switch)
	0C01        R     xxx--x--    Communications
	            R     x-------       (VBLANK)
                R     -x------       (sound CPU communications latch full flag)
                R     --x-----       (sound CPU acknowledge latch flag)
                R     -----x--       (slam switch)
    1400        R     xxxxxxxx    Sound acknowledge latch
    1800        R     xxxxxxxx    Read A/D conversion
    1C00          W   --------    Enable NVRAM
    1C01          W   --------    Disable NVRAM
    1C80          W   --------    Start A/D conversion (horizontal)
    1C82          W   --------    Start A/D conversion (vertical)
    1D00          W   --------    NVRAM store
    1D80          W   --------    Watchdog clear
    1E00          W   --------    Interrupt acknowledge
    1E80          W   x-------    Left coin counter
    1E81          W   x-------    Right coin counter
    1E82          W   x-------    LED 1 (not used)
    1E83          W   x-------    LED 2 (not used)
    1E84          W   x-------    Alphanumerics bank select
    1E86          W   x-------    Sound CPU reset
    1E87          W   x-------    Video off
    1F00          W   xxxxxxxx    Sound communications latch
    1F80          W   -----xxx    Program ROM bank select
    2000-23FF   R/W   xxxxxxxx    Scrolling playfield (low 8 bits)
    2400-27FF   R/W   ----xxxx    Scrolling playfield (upper 4 bits)
    2800-2BFF   R/W   xxxxxxxx    Color RAM low
                R/W   -----xxx       (blue)
                R/W   --xxx---       (green)
                R/W   xx------       (red LSBs)
    2C00-2FFF   R/W   ----xxxx    Color RAM high
                R/W   -------x       (red MSB)
                R/W   ----xxx-       (intensity)
    3000-37BF   R/W   xxxxxxxx    Alphanumerics RAM
    37C0-37EF   R/W   xxxxxxxx    Motion object picture
    3800-382F   R/W   -xxxxxxx    Motion object flags
                R/W   -x---xx-       (picture bank)
                R/W   --x-----       (vertical flip)
                R/W   ---x----       (horizontal flip)
                R/W   ----x---       (32 pixels tall)
                R/W   -------x       (X position MSB)
    3840-386F   R/W   xxxxxxxx       (Y position)
    38C0-38EF   R/W   xxxxxxxx       (X position LSBs)
    3C00-3C01     W   xxxxxxxx    Scrolling playfield vertical position
    3D00-3D01     W   xxxxxxxx    Scrolling playfield horizontal position
    3E00-3FFF     W   xxxxxxxx    PIXI graphics expander RAM
    4000-7FFF   R     xxxxxxxx    Banked program ROM
    8000-FFFF   R     xxxxxxxx    Fixed program ROM
	========================================================================
	Interrupts:
		NMI not connected
		IRQ generated by 32V
	========================================================================


	========================================================================
	CPU #2
	========================================================================
	0000-07FF   R/W   xxxxxxxx    Z-page working RAM
	0800-083F   R/W   xxxxxxxx    Custom I/O
	1000          W   --------    Interrupt acknowledge
	1100          W   xxxxxxxx    Speech data
	1200          W   --------    Speech write strobe on
	1300          W   --------    Speech write strobe off
	1400          W   xxxxxxxx    Main CPU acknowledge latch
	1500          W   -------x    Speech chip reset
	1800        R     xxxxxxxx    Main CPU communication latch
	1C00        R     x-------    Speech chip ready
	1C01        R     xx------    Communications
	            R     x-------       (sound CPU communication latch full flag)
	            R     -x------       (sound CPU acknowledge latch full flag)
	8000-FFFF   R     xxxxxxxx    Program ROM
	========================================================================
	Interrupts:
		NMI not connected
		IRQ generated by 32V
	========================================================================

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class jedi
{
	
	
	/* constants */
	#define MAIN_CPU_OSC		10000000
	#define SOUND_CPU_OSC		12096000
	
	
	/* local variables */
	static UINT8 control_num;
	static UINT8 sound_latch;
	static UINT8 sound_ack_latch;
	static UINT8 sound_comm_stat;
	static UINT8 speech_write_buffer;
	static UINT8 speech_strobe_state;
	static UINT8 nvram_enabled;
	
	
	/*************************************
	 *
	 *	Interrupt handling
	 *
	 *************************************/
	
	static void generate_interrupt(int scanline)
	{
		/* IRQ is set by /32V */
		cpu_set_irq_line(0, M6502_IRQ_LINE, (scanline & 32) ? CLEAR_LINE : ASSERT_LINE);
		cpu_set_irq_line(1, M6502_IRQ_LINE, (scanline & 32) ? CLEAR_LINE : ASSERT_LINE);
	
		/* set up for the next */
		scanline += 32;
		if (scanline > 256)
			scanline = 32;
		timer_set(cpu_getscanlinetime(scanline), scanline, generate_interrupt);
	}
	
	
	public static WriteHandlerPtr main_irq_ack_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		cpu_set_irq_line(0, M6502_IRQ_LINE, CLEAR_LINE);
	} };
	
	
	public static WriteHandlerPtr sound_irq_ack_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		cpu_set_irq_line(1, M6502_IRQ_LINE, CLEAR_LINE);
	} };
	
	
	static MACHINE_INIT( jedi )
	{
		/* init globals */
		control_num = 0;
		sound_latch = 0;
		sound_ack_latch = 0;
		sound_comm_stat = 0;
		speech_write_buffer = 0;
		speech_strobe_state = 0;
		nvram_enabled = 0;
	
		/* set a timer to run the interrupts */
		timer_set(cpu_getscanlinetime(32), 32, generate_interrupt);
	}
	
	
	
	/*************************************
	 *
	 *	Main program ROM banking
	 *
	 *************************************/
	
	public static WriteHandlerPtr rom_banksel_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UINT8 *RAM = memory_region(REGION_CPU1);
	
	    if (data & 0x01) cpu_setbank(1, &RAM[0x10000]);
	    if (data & 0x02) cpu_setbank(1, &RAM[0x14000]);
	    if (data & 0x04) cpu_setbank(1, &RAM[0x18000]);
	} };
	
	
	
	/*************************************
	 *
	 *	Main CPU -> Sound CPU communications
	 *
	 *************************************/
	
	public static WriteHandlerPtr sound_reset_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		cpu_set_reset_line(1, (data & 1) ? CLEAR_LINE : ASSERT_LINE);
	} };
	
	
	static void delayed_sound_latch_w(int data)
	{
	    sound_latch = data;
	    sound_comm_stat |= 0x80;
	}
	
	
	public static WriteHandlerPtr sound_latch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		timer_set(TIME_NOW, data, delayed_sound_latch_w);
	} };
	
	
	public static ReadHandlerPtr sound_latch_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    sound_comm_stat &= ~0x80;
	    return sound_latch;
	} };
	
	
	
	/*************************************
	 *
	 *	Sound CPU -> Main CPU communications
	 *
	 *************************************/
	
	public static ReadHandlerPtr sound_ack_latch_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    sound_comm_stat &= ~0x40;
	    return sound_ack_latch;
	} };
	
	
	public static WriteHandlerPtr sound_ack_latch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	    sound_ack_latch = data;
	    sound_comm_stat |= 0x40;
	} };
	
	
	
	/*************************************
	 *
	 *	I/O ports
	 *
	 *************************************/
	
	public static ReadHandlerPtr a2d_data_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (control_num)
		{
			case 0:		return readinputport(2);
			case 2:		return readinputport(3);
			default:	return 0;
		}
	    return 0;
	} };
	
	
	public static ReadHandlerPtr special_port1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return readinputport(1) ^ ((sound_comm_stat >> 1) & 0x60);
	} };
	
	
	public static WriteHandlerPtr a2d_select_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	    control_num = offset;
	} };
	
	
	public static ReadHandlerPtr soundstat_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    return sound_comm_stat;
	} };
	
	
	public static WriteHandlerPtr jedi_coin_counter_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		coin_counter_w(offset, data >> 7);
	} };
	
	
	
	/*************************************
	 *
	 *	Speech access
	 *
	 *************************************/
	
	public static WriteHandlerPtr speech_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		speech_write_buffer = data;
	} };
	
	
	public static WriteHandlerPtr speech_strobe_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int state = (~offset >> 8) & 1;
	
		if ((state ^ speech_strobe_state) && state)
			tms5220_data_w(0, speech_write_buffer);
		speech_strobe_state = state;
	} };
	
	
	public static ReadHandlerPtr speech_ready_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    return (!tms5220_ready_r()) << 7;
	} };
	
	
	
	/*************************************
	 *
	 *	NVRAM
	 *
	 *************************************/
	
	public static WriteHandlerPtr nvram_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (nvram_enabled)
			generic_nvram[offset] = data;
	} };
	
	
	public static WriteHandlerPtr nvram_enable_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		nvram_enabled = ~offset & 1;
	} };
	
	
	
	/*************************************
	 *
	 *	Main CPU memory handlers
	 *
	 *************************************/
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x07ff, MRA_RAM ),
		new Memory_ReadAddress( 0x0800, 0x08ff, MRA_RAM ),
		new Memory_ReadAddress( 0x0c00, 0x0c00, input_port_0_r ),
		new Memory_ReadAddress( 0x0c01, 0x0c01, special_port1_r ),
		new Memory_ReadAddress( 0x1400, 0x1400, sound_ack_latch_r ),
		new Memory_ReadAddress( 0x1800, 0x1800, a2d_data_r ),
		new Memory_ReadAddress( 0x2000, 0x27ff, MRA_RAM ),
		new Memory_ReadAddress( 0x2800, 0x2fff, MRA_RAM ),
		new Memory_ReadAddress( 0x3000, 0x37bf, MRA_RAM ),
		new Memory_ReadAddress( 0x37c0, 0x3bff, MRA_RAM ),
		new Memory_ReadAddress( 0x4000, 0x7fff, MRA_BANK1 ),
		new Memory_ReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x07ff, MWA_RAM ),
		new Memory_WriteAddress( 0x0800, 0x08ff, nvram_data_w, generic_nvram, generic_nvram_size ),
		new Memory_WriteAddress( 0x1c00, 0x1c01, nvram_enable_w ),
		new Memory_WriteAddress( 0x1c80, 0x1c82, a2d_select_w ),
		new Memory_WriteAddress( 0x1d00, 0x1d00, MWA_NOP ),	/* NVRAM store */
		new Memory_WriteAddress( 0x1d80, 0x1d80, watchdog_reset_w ),
		new Memory_WriteAddress( 0x1e00, 0x1e00, main_irq_ack_w ),
		new Memory_WriteAddress( 0x1e80, 0x1e81, jedi_coin_counter_w ),
		new Memory_WriteAddress( 0x1e82, 0x1e83, MWA_NOP ),	/* LED control; not used */
		new Memory_WriteAddress( 0x1e84, 0x1e84, jedi_alpha_banksel_w ),
		new Memory_WriteAddress( 0x1e86, 0x1e86, sound_reset_w ),
		new Memory_WriteAddress( 0x1e87, 0x1e87, jedi_video_off_w ),
		new Memory_WriteAddress( 0x1f00, 0x1f00, sound_latch_w ),
		new Memory_WriteAddress( 0x1f80, 0x1f80, rom_banksel_w ),
		new Memory_WriteAddress( 0x2000, 0x27ff, jedi_backgroundram_w, jedi_backgroundram, jedi_backgroundram_size ),
		new Memory_WriteAddress( 0x2800, 0x2fff, jedi_paletteram_w, paletteram ),
		new Memory_WriteAddress( 0x3000, 0x37bf, videoram_w, videoram, videoram_size ),
		new Memory_WriteAddress( 0x37c0, 0x3bff, MWA_RAM, spriteram, spriteram_size ),
		new Memory_WriteAddress( 0x3c00, 0x3c01, jedi_vscroll_w ),
		new Memory_WriteAddress( 0x3d00, 0x3d01, jedi_hscroll_w ),
		new Memory_WriteAddress( 0x3e00, 0x3fff, jedi_PIXIRAM_w, jedi_PIXIRAM ),
		new Memory_WriteAddress( 0x4000, 0xffff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	
	/*************************************
	 *
	 *	Sound CPU memory handlers
	 *
	 *************************************/
	
	public static Memory_ReadAddress readmem2[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x07ff, MRA_RAM ),
		new Memory_ReadAddress( 0x0800, 0x080f, pokey1_r ),
		new Memory_ReadAddress( 0x0810, 0x081f, pokey2_r ),
		new Memory_ReadAddress( 0x0820, 0x082f, pokey3_r ),
		new Memory_ReadAddress( 0x0830, 0x083f, pokey4_r ),
		new Memory_ReadAddress( 0x1800, 0x1800, sound_latch_r ),
		new Memory_ReadAddress( 0x1c00, 0x1c00, speech_ready_r ),
		new Memory_ReadAddress( 0x1c01, 0x1c01, soundstat_r ),
		new Memory_ReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_WriteAddress writemem2[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x07ff, MWA_RAM ),
		new Memory_WriteAddress( 0x0800, 0x080f, pokey1_w ),
		new Memory_WriteAddress( 0x0810, 0x081f, pokey2_w ),
		new Memory_WriteAddress( 0x0820, 0x082f, pokey3_w ),
		new Memory_WriteAddress( 0x0830, 0x083f, pokey4_w ),
		new Memory_WriteAddress( 0x1000, 0x1000, sound_irq_ack_w ),
		new Memory_WriteAddress( 0x1100, 0x11ff, speech_data_w ),
		new Memory_WriteAddress( 0x1200, 0x13ff, speech_strobe_w ),
		new Memory_WriteAddress( 0x1400, 0x1400, sound_ack_latch_w ),
		new Memory_WriteAddress( 0x8000, 0xffff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	
	/*************************************
	 *
	 *	Port definitions
	 *
	 *************************************/
	
	static InputPortPtr input_ports_jedi = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* 0C00 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_BUTTON3 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW,  IPT_BUTTON2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW,  IPT_BUTTON1 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_SERVICE( 0x10, IP_ACTIVE_LOW );
		PORT_BIT( 0x20, IP_ACTIVE_LOW,  IPT_SERVICE1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW,  IPT_COIN2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_COIN1 );
	
		PORT_START(); 	/* 0C01 */
		PORT_BIT( 0x03, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_TILT );
		PORT_BIT( 0x18, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT( 0x60, IP_ACTIVE_HIGH, IPT_SPECIAL );/* sound comm */
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK );
	
		PORT_START(); 	/* analog Y */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_Y, 100, 10, 0, 255 );
	
		PORT_START(); 	/* analog X */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X, 100, 10, 0, 255 );
	INPUT_PORTS_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Graphics layouts
	 *
	 *************************************/
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,1),
		2,
		new int[] { 0, 1 },
		new int[] { 0, 2, 4, 6, 8, 10, 12, 14 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		16*8
	);
	
	
	static GfxLayout pflayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,2),
		4,
		new int[] { 0, 4, RGN_FRAC(1,2), RGN_FRAC(1,2)+4 },
		new int[] { 0, 1, 2, 3, 8+0, 8+1, 8+2, 8+3 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
				8*16, 9*16, 10*16, 11*16, 12*16, 13*16, 14*16, 15*16 },
		16*8
	);
	
	
	static GfxLayout spritelayout = new GfxLayout
	(
		8,16,
		RGN_FRAC(1,2),
		4,
		new int[] { 0, 4, RGN_FRAC(1,2), RGN_FRAC(1,2)+4 },
		new int[] { 0, 1, 2, 3, 8+0, 8+1, 8+2, 8+3},
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
				8*16, 9*16, 10*16, 11*16, 12*16, 13*16, 14*16, 15*16 },
		32*8
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,	  0, 1 ),
		new GfxDecodeInfo( REGION_GFX2, 0, pflayout,	  0, 1 ),
		new GfxDecodeInfo( REGION_GFX3, 0, spritelayout,  0, 1 ),
		new GfxDecodeInfo( -1 )
	};
	
	
	
	/*************************************
	 *
	 *	Sound definitions
	 *
	 *************************************/
	
	static POKEYinterface pokey_interface = new POKEYinterface
	(
		4,
		SOUND_CPU_OSC/2/4,	/* 1.5MHz */
		new int[] { 30, 30, MIXER(30,MIXER_PAN_LEFT), MIXER(30,MIXER_PAN_RIGHT) },
		/* The 8 pot handlers */
		new ReadHandlerPtr[] { 0, 0, 0, 0 },
		new ReadHandlerPtr[] { 0, 0, 0, 0 },
		new ReadHandlerPtr[] { 0, 0, 0, 0 },
		new ReadHandlerPtr[] { 0, 0, 0, 0 },
		new ReadHandlerPtr[] { 0, 0, 0, 0 },
		new ReadHandlerPtr[] { 0, 0, 0, 0 },
		new ReadHandlerPtr[] { 0, 0, 0, 0 },
		new ReadHandlerPtr[] { 0, 0, 0, 0 },
		/* The allpot handler */
		new ReadHandlerPtr[] { 0, 0, 0, 0 }
	);
	
	
	static struct TMS5220interface tms5220_interface =
	{
		SOUND_CPU_OSC/2/9,
		100,
		0
	};
	
	
	
	/*************************************
	 *
	 *	Machine driver
	 *
	 *************************************/
	
	static MACHINE_DRIVER_START( jedi )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M6502,MAIN_CPU_OSC/2/2)		/* 2.5MHz */
		MDRV_CPU_MEMORY(readmem,writemem)
	
		MDRV_CPU_ADD(M6502,SOUND_CPU_OSC/2/4)		/* 1.5MHz */
		MDRV_CPU_MEMORY(readmem2,writemem2)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_INTERLEAVE(4)
		
		MDRV_MACHINE_INIT(jedi)
		MDRV_NVRAM_HANDLER(generic_0fill)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(37*8, 30*8)
		MDRV_VISIBLE_AREA(0*8, 37*8-1, 0*8, 30*8-1)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024+1)
	
		MDRV_VIDEO_START(jedi)
		MDRV_VIDEO_UPDATE(jedi)
	
		/* sound hardware */
		MDRV_SOUND_ATTRIBUTES(SOUND_SUPPORTS_STEREO)
		MDRV_SOUND_ADD(POKEY,   pokey_interface)
		MDRV_SOUND_ADD(TMS5220, tms5220_interface)
	MACHINE_DRIVER_END
	
	
	
	/*************************************
	 *
	 *	ROM definitions
	 *
	 *************************************/
	
	static RomLoadPtr rom_jedi = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x1C000, REGION_CPU1, 0 );/* 64k for code + 48k for banked ROMs */
		ROM_LOAD( "14f_221.bin",  0x08000, 0x4000, CRC(414d05e3),SHA1(e5f5f8d85433467a13d6ca9e3889e07a62b00e52) )
		ROM_LOAD( "13f_222.bin",  0x0c000, 0x4000, CRC(7b3f21be),SHA1(8fe62401f9b78c7a3e62b544c4b705b1bfa9b8f3) )
		ROM_LOAD( "13d_123.bin",  0x10000, 0x4000, CRC(877f554a),SHA1(8b51109cabd84741b024052f892b3172fbe83223) ) /* Page 0 */
		ROM_LOAD( "13b_124.bin",  0x14000, 0x4000, CRC(e72d41db),SHA1(1b3fcdc435f1e470e8d5b7241856e398a4c3910e) ) /* Page 1 */
		ROM_LOAD( "13a_122.bin",  0x18000, 0x4000, CRC(cce7ced5),SHA1(bff031a637aefca713355dbf251dcb5c2cea0885) ) /* Page 2 */
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* space for the sound ROMs */
		ROM_LOAD( "01c_133.bin",  0x8000, 0x4000, CRC(6c601c69),SHA1(618b77800bbbb4db34a53ca974a71bdaf89b5930) )
		ROM_LOAD( "01a_134.bin",  0xC000, 0x4000, CRC(5e36c564),SHA1(4b0afceb9a1d912f1d5c1f26928d244d5b14ea4a) )
	
		ROM_REGION( 0x02000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "11t_215.bin",  0x00000, 0x2000, CRC(3e49491f),SHA1(ade5e846069c2fa6edf667469d13ce5a6a45c06d) ) /* Alphanumeric */
	
		ROM_REGION( 0x10000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "06r_126.bin",  0x00000, 0x8000, CRC(9c55ece8),SHA1(b8faa23314bb0d199ef46199bfabd9cb17510dd3) ) /* Playfield */
		ROM_LOAD( "06n_127.bin",  0x08000, 0x8000, CRC(4b09dcc5),SHA1(d46b5f4fb69c4b8d823dd9c4d92f8713badfa44a) )
	
		ROM_REGION( 0x20000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD( "01h_130.bin",  0x00000, 0x8000, CRC(2646a793),SHA1(dcb5fd50eafbb27565bce099a884be83a9d82285) ) /* Sprites */
		ROM_LOAD( "01f_131.bin",  0x08000, 0x8000, CRC(60107350),SHA1(ded03a46996d3f2349df7f59fd435a7ad6ed465e) )
		ROM_LOAD( "01m_128.bin",  0x10000, 0x8000, CRC(24663184),SHA1(5eba142ed926671ee131430944e59f21a55a5c57) )
		ROM_LOAD( "01k_129.bin",  0x18000, 0x8000, CRC(ac86b98c),SHA1(9f86c8801a7293fa46e9432f1651dd85bf00f4b9) )
	
		ROM_REGION( 0x0800, REGION_PROMS, 0 );/* background smoothing */
		ROM_LOAD( "136030.117",   0x0000, 0x0400, CRC(9831bd55),SHA1(12945ef2d1582914125b9ee591567034d71d6573) )
		ROM_LOAD( "136030.118",   0x0400, 0x0400, CRC(261fbfe7),SHA1(efc65a74a3718563a07b718e34d8a7aa23339a69) )
	ROM_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Game drivers
	 *
	 *************************************/
	
	public static GameDriver driver_jedi	   = new GameDriver("1984"	,"jedi"	,"jedi.java"	,rom_jedi,null	,machine_driver_jedi	,input_ports_jedi	,null	,ROT0	,	"Atari", "Return of the Jedi" )
}
