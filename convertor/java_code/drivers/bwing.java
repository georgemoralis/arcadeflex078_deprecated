/*****************************************************************************

B-Wings  (c) 1984 Data East Corporation
Zaviga   (c) 1984 Data East Corporation

drivers by Acho A. Tang


JUL-2003

Known issues:

- The main program is responsible for sprite clipping but occational
  glitches can be seen at the top and bottom screen edges. (post rotate)

- B-Wings bosses sometimes flicker. (sync issue)

- The text layer has an unknown attribute. (needs verification)

- Zaviga's DIPs are incomplete. (manual missing)

*****************************************************************************/
// Directives

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class bwing
{
	
	#define BW_DEBUG 0
	#define BW_CHEAT 0
	
	#define MAX_SOUNDS 16
	
	//****************************************************************************
	// Imports
	
	extern struct GfxLayout bwing_tilelayout;
	
	extern extern extern extern extern extern extern VIDEO_START( bwing );
	extern VIDEO_UPDATE( bwing );
	
	//****************************************************************************
	// Local Vars
	
	static data8_t sound_fifo[MAX_SOUNDS];
	static data8_t *bwp123_membase[3], *bwp3_rombase;
	static data8_t *bwp1_sharedram1, *bwp2_sharedram1;
	static size_t bwp3_romsize;
	static int bwp3_nmimask, bwp3_u8F_d, ffcount, ffhead, fftail;
	
	//****************************************************************************
	// Interrupt Handlers
	
	INTERRUPT_GEN ( bwp1_interrupt )
	{
		static int coin = 0;
		data8_t latch_data;
	
		switch (cpu_getiloops())
		{
			case 0:
				if (ffcount)
				{
					ffcount--;
					latch_data = sound_fifo[fftail];
					fftail = (fftail + 1) & (MAX_SOUNDS - 1);
					soundlatch_w(0, latch_data);
					cpu_set_irq_line(2, DECO16_IRQ_LINE, HOLD_LINE); // SNDREQ
				}
			break;
	
			case 1:
				if (~readinputport(4) & 0x03)
					{ if (coin == 0) { coin = 1; cpu_set_nmi_line(0, ASSERT_LINE); } }
				else
					coin = 0;
			break;
	
			case 2:
				if (readinputport(5)) cpu_set_irq_line(0, M6809_FIRQ_LINE, ASSERT_LINE);
			break;
		}
	}
	
	
	INTERRUPT_GEN ( bwp3_interrupt ) { if (bwp3_nmimask == 0) cpu_set_nmi_line(2, ASSERT_LINE); }
	
	//****************************************************************************
	// Memory and I/O Handlers
	
	public static WriteHandlerPtr bwp12_sharedram1_w = new WriteHandlerPtr() {public void handler(int offset, int data) { bwp1_sharedram1[offset] = bwp2_sharedram1[offset] = data; } };
	public static WriteHandlerPtr bwp3_u8F_w = new WriteHandlerPtr() {public void handler(int offset, int data) { bwp3_u8F_d = data; } }; // prepares custom chip for various operations
	public static WriteHandlerPtr bwp3_nmiack_w = new WriteHandlerPtr() {public void handler(int offset, int data) { cpu_set_nmi_line(2, CLEAR_LINE); } };
	public static WriteHandlerPtr bwp3_nmimask_w = new WriteHandlerPtr() {public void handler(int offset, int data) { bwp3_nmimask = data & 0x80; } };
	
	
	public static ReadHandlerPtr bwp1_io_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (offset == 0) return(readinputport(0));
		if (offset == 1) return(readinputport(1));
		if (offset == 2) return(readinputport(2));
		if (offset == 3) return(readinputport(3));
		if (offset == 4) return(readinputport(4));
	
		return((bwp123_membase[0])[0x1b00 + offset]);
	} };
	
	
	public static WriteHandlerPtr bwp1_ctrl_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch (offset)
		{
			// MSSTB
			case 0: cpu_set_irq_line(1, M6809_IRQ_LINE, ASSERT_LINE); break;
	
			// IRQACK
			case 1: cpu_set_irq_line(0, M6809_IRQ_LINE, CLEAR_LINE); break;
	
			// FIRQACK
			case 2: cpu_set_irq_line(0, M6809_FIRQ_LINE, CLEAR_LINE); break;
	
			// NMIACK
			case 3: cpu_set_nmi_line(0, CLEAR_LINE); break;
	
			// SWAP(bank-swaps sprite RAM between 1800 & 1900; ignored bc. they're treated as a single chunk.)
			case 4: break;
	
			// SNDREQ
			case 5:
				if (data == 0x80) // protection trick to screw CPU1 & 3
					cpu_set_nmi_line(1, ASSERT_LINE); // SNMI
				else
				if (ffcount < MAX_SOUNDS)
				{
					ffcount++;
					sound_fifo[ffhead] = data;
					ffhead = (ffhead + 1) & (MAX_SOUNDS - 1);
				}
			break;
	
			// BANKSEL(supposed to bank-switch CPU0 4000-7fff(may also 8000-bfff) 00=bank 0, 80=bank 1, unused)
			case 6: break;
	
			// hardwired to SWAP
			case 7: break;
		}
	
		#if BW_DEBUG
			(bwp123_membase[0])[0x1c00 + offset] = data;
		#endif
	} };
	
	
	public static WriteHandlerPtr bwp2_ctrl_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch (offset)
		{
			case 0: cpu_set_irq_line(0, M6809_IRQ_LINE, ASSERT_LINE); break; // SMSTB
	
			case 1: cpu_set_irq_line(1, M6809_FIRQ_LINE, CLEAR_LINE); break;
	
			case 2: cpu_set_irq_line(1, M6809_IRQ_LINE, CLEAR_LINE); break;
	
			case 3: cpu_set_nmi_line(1, CLEAR_LINE); break;
		}
	
		#if BW_DEBUG
			(bwp123_membase[1])[0x1800 + offset] = data;
		#endif
	} };
	
	//****************************************************************************
	// CPU Memory Maps
	
	// Main CPU
	public static Memory_ReadAddress bwp1_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x1b00, 0x1b07, bwp1_io_r ),
		new Memory_ReadAddress( 0x0000, 0x1fff, MRA_RAM ),
		new Memory_ReadAddress( 0x2000, 0x3fff, bwing_scrollram_r ),
		new Memory_ReadAddress( 0x4000, 0xffff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress bwp1_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x07ff, bwp12_sharedram1_w, bwp1_sharedram1 ),
		new Memory_WriteAddress( 0x0800, 0x0fff, MWA_RAM ),
		new Memory_WriteAddress( 0x1000, 0x13ff, bwing_videoram_w, videoram ),
		new Memory_WriteAddress( 0x1800, 0x19ff, bwing_spriteram_w, buffered_spriteram ),
		new Memory_WriteAddress( 0x1a00, 0x1aff, bwing_paletteram_w, paletteram ),
		new Memory_WriteAddress( 0x1b00, 0x1b07, bwing_scrollreg_w ),
		new Memory_WriteAddress( 0x1c00, 0x1c07, bwp1_ctrl_w ),
		new Memory_WriteAddress( 0x2000, 0x3fff, bwing_scrollram_w ),
		new Memory_WriteAddress( 0x1000, 0x1fff, MWA_RAM ), // falls through
		new Memory_WriteAddress( 0x4000, 0xffff, MWA_NOP ), // "B-Wings US" writes to 9631-9632(debug?)
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	// Sub CPU
	public static Memory_ReadAddress bwp2_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x0fff, MRA_RAM ),
		new Memory_ReadAddress( 0xa000, 0xffff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress bwp2_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x07ff, bwp12_sharedram1_w, bwp2_sharedram1 ),
		new Memory_WriteAddress( 0x0800, 0x0fff, MWA_RAM ),
		new Memory_WriteAddress( 0x1800, 0x1803, bwp2_ctrl_w ),
		new Memory_WriteAddress( 0xa000, 0xffff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	// Sound CPU
	public static Memory_ReadAddress bwp3_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x01ff, MRA_RAM ),
		new Memory_ReadAddress( 0xa000, 0xa000, soundlatch_r ),
		new Memory_ReadAddress( 0xe000, 0xffff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress bwp3_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x01ff, MWA_RAM ),
		new Memory_WriteAddress( 0x0200, 0x0200, DAC_0_signed_data_w ),
		new Memory_WriteAddress( 0x1000, 0x1000, bwp3_nmiack_w ),
		new Memory_WriteAddress( 0x2000, 0x2000, AY8910_write_port_0_w ),
		new Memory_WriteAddress( 0x4000, 0x4000, AY8910_control_port_0_w ),
		new Memory_WriteAddress( 0x6000, 0x6000, AY8910_write_port_1_w ),
		new Memory_WriteAddress( 0x8000, 0x8000, AY8910_control_port_1_w ),
		new Memory_WriteAddress( 0xd000, 0xd000, bwp3_nmimask_w ),
		new Memory_WriteAddress( 0xe000, 0xffff, MWA_ROM, bwp3_rombase, bwp3_romsize ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static IO_ReadPort bwp3_readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x00, 0x00, input_port_6_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort bwp3_writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00, 0x00, bwp3_u8F_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	//****************************************************************************
	// I/O Port Maps
	
	static InputPortPtr input_ports_bwing = new InputPortPtr(){ public void handler() { 
		PORT_START(); 
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x10, 0x10, "Diag" );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Cocktail") );
	#if BW_CHEAT // undocumented and only labeled as "KEEP OFF" in B-Wings
		PORT_DIPNAME( 0x40, 0x40, "Invincibility" );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "Infinite" );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	#else
		PORT_BIT( 0xc0, IP_ACTIVE_LOW, IPT_UNKNOWN );
	#endif
	
		PORT_START(); 
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPSETTING(    0x01, "3" );
		PORT_DIPNAME( 0x06, 0x06, "Bonus" );
		PORT_DIPSETTING(    0x00, "40000" );
		PORT_DIPSETTING(    0x02, "20000 80000" );
		PORT_DIPSETTING(    0x04, "20000 60000" );
		PORT_DIPSETTING(    0x06, "20000 40000" );
		PORT_DIPNAME( 0x08, 0x08, "Enemy Crafts" );
		PORT_DIPSETTING(    0x00, "Hard" );
		PORT_DIPSETTING(    0x08, "Normal" );
		PORT_DIPNAME( 0x10, 0x10, "Enemy Missiles" );
		PORT_DIPSETTING(    0x00, "Hard" );
		PORT_DIPSETTING(    0x10, "Normal" );
		PORT_DIPNAME( 0x20, 0x20, "Freeze" );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, "Country" );
		PORT_DIPSETTING(    0x00, "Japan/US" );
		PORT_DIPSETTING(    0x40, "Japan Only" );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK );
	
		PORT_START(); 
		PORT_BIT( 0xff, IP_ACTIVE_HIGH, IPT_TILT );
	
		PORT_START(); 
		PORT_BIT( 0xff, IP_ACTIVE_HIGH, IPT_VBLANK );
	
		PORT_START();  // a matter of taste
		PORT_DIPNAME( 0x07, 0x00, "RGB" );
		PORT_DIPSETTING(    0x00, "Default" );
		PORT_DIPSETTING(    0x01, "More Red" );
		PORT_DIPSETTING(    0x02, "More Green" );
		PORT_DIPSETTING(    0x03, "More Blue" );
		PORT_DIPSETTING(    0x04, "Max" );
	INPUT_PORTS_END(); }}; 
	
	//****************************************************************************
	// Graphics Layouts
	
	static GfxLayout charlayout = new GfxLayout
	(
		8, 8,
		256,
		2,
		new int[] { 0, 0x4000 },
		new int[] { 7, 6, 5, 4, 3, 2, 1, 0 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16, 16,
		512,
		3,
		new int[] { 0x40000, 0x20000, 0 },
		new int[] { 7, 6, 5, 4, 3, 2, 1, 0, 128+7, 128+6, 128+5, 128+4, 128+3, 128+2, 128+1, 128+0 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
		  8*8, 9*8,10*8,11*8,12*8,13*8,14*8,15*8 },
		32*8
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1,  0, charlayout,       0x00, 1 ), // chars
		new GfxDecodeInfo( REGION_GFX2,  0, spritelayout,     0x20, 2 ), // sprites
		new GfxDecodeInfo( REGION_USER1, 0, bwing_tilelayout, 0x10, 2 ), // foreground tiles place holder
		new GfxDecodeInfo( REGION_USER1, 0, bwing_tilelayout, 0x30, 2 ), // background tiles place holder
		new GfxDecodeInfo( -1 )
	};
	
	//****************************************************************************
	// Hardware Definitions
	
	MACHINE_INIT( bwing )
	{
		bwp3_nmimask = 0;
		fftail = ffhead = ffcount = 0;
	}
	
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
		2,
		1500000,
		new int[] { 50, 50 },
		new ReadHandlerPtr[] { 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 }
	);
	
	static DACinterface dac_interface = new DACinterface
	(
		1,
		new int[] { 10 }
	);
	
	
	static MACHINE_DRIVER_START( bwing )
	
		// basic machine hardware
		MDRV_CPU_ADD(M6809, 2000000)
		MDRV_CPU_MEMORY(bwp1_readmem, bwp1_writemem)
		MDRV_CPU_VBLANK_INT(bwp1_interrupt, 3)
	
		MDRV_CPU_ADD(M6809, 2000000)
		MDRV_CPU_MEMORY(bwp2_readmem, bwp2_writemem)
	//	MDRV_CPU_VBLANK_INT(irq1_line_assert, 1) // vblank triggers FIRQ on CPU2 by design (unused)
	
		MDRV_CPU_ADD(DECO16, 2000000)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)
		MDRV_CPU_MEMORY(bwp3_readmem, bwp3_writemem)
		MDRV_CPU_PORTS(bwp3_readport, bwp3_writeport)
		MDRV_CPU_PERIODIC_INT(bwp3_interrupt, 1000)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(600)	// must be long enough for polling
		MDRV_INTERLEAVE(300)		// high enough?
	
		// video hardware
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER | VIDEO_UPDATE_BEFORE_VBLANK)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_SCREEN_SIZE(32*8, 32*8)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 1*8, 31*8-1)
		MDRV_PALETTE_LENGTH(64)
	
		MDRV_VIDEO_START(bwing)
		MDRV_VIDEO_UPDATE(bwing)
	
		// sound hardware
		MDRV_SOUND_ADD(AY8910, ay8910_interface)
		MDRV_SOUND_ADD(DAC, dac_interface)
	
	MACHINE_DRIVER_END
	
	//****************************************************************************
	// ROM Maps
	
	static RomLoadPtr rom_bwing = new RomLoadPtr(){ public void handler(){ 
		// Top Board(SCU-01)
		ROM_REGION( 0x10000, REGION_CPU1, 0 );// main CPU
		ROM_LOAD( "bw_bv-02.10a", 0x04000, 0x04000, CRC(5ce74ab5);SHA1(b414f0bbe1c4c5b4c810bb4b9fba16aaf86520ff) )
		ROM_LOAD( "bw_bv-01.7a",  0x08000, 0x04000, CRC(b960c707);SHA1(086cb0f22fb59922bf0369bf6b382a241d979ec3) )
		ROM_LOAD( "bw_bv-00.4a",  0x0c000, 0x04000, CRC(926bef63);SHA1(d4bd2e91fa0abc5e9472d4b684c076bdc3c29f5b) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );// sub CPU
		ROM_LOAD( "bw_bv-06.10d", 0x0a000, 0x02000, CRC(91a21a4c);SHA1(042eed60119a861f6b3ccfbe68d880f182a8a8e1) )
		ROM_LOAD( "bw_bv-05.9d",  0x0c000, 0x02000, CRC(f283f39a);SHA1(9f7f4c39d49f4dfff73fe74cd457480e8a43a3c5) )
		ROM_LOAD( "bw_bv-04.7d",  0x0e000, 0x02000, CRC(29ae75b6);SHA1(48c94e996857f2ac995bcd25f0e67b9f7c17d807) )
	
		ROM_REGION( 0x10000, REGION_CPU3, 0 );// sound CPU(encrypted)
		ROM_LOAD( "bw_bv-03.13a", 0x0e000, 0x02000, CRC(e8ac9379);SHA1(aaf5c20aa33ed05747a8a27739e9d09e094a518d) )
	
		// Bottom Board(CCU-01)
		ROM_REGION( 0x01000, REGION_GFX1, ROMREGION_DISPOSE );// chars
		ROM_LOAD( "bw_bv-10.5c",  0x00000, 0x01000, CRC(edca6901);SHA1(402c80e7519cf3a43b9fef52c9923961220a48b6) )
	
		// Middle Board(MCU-01)
		ROM_REGION( 0x0c000, REGION_GFX2, ROMREGION_DISPOSE );// sprites
		ROM_LOAD( "bw_bv-07.1l",  0x00000, 0x04000, CRC(3d5ab2be);SHA1(2b3a039914ebfcc3993da74853a67546fc22c191) )
		ROM_LOAD( "bw_bv-08.1k",  0x04000, 0x04000, CRC(7a585f1e);SHA1(99e5d947b6b1fa96b90c676a282376d67fc377f0) )
		ROM_LOAD( "bw_bv-09.1h",  0x08000, 0x04000, CRC(a14c0b57);SHA1(5033354793d77922f5ef7f268cbe212e551efadf) )
	
		// GPU Banks
		ROM_REGION( 0x08000, REGION_USER1, 0 );
		ROM_FILL(0x00000, 0x08000, 0);
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_bwings = new RomLoadPtr(){ public void handler(){ 
		// Top Board(SCU-01)
		ROM_REGION( 0x10000, REGION_CPU1, 0 );// main CPU
		ROM_LOAD( "bw_bv-02.10a", 0x04000, 0x04000, CRC(5ce74ab5);SHA1(b414f0bbe1c4c5b4c810bb4b9fba16aaf86520ff) )
		ROM_LOAD( "bv02.bin",     0x06000, 0x02000, CRC(2f84654e);SHA1(11b5343219b46d03f686ea348181c509121b9e3c) ) // only the lower 8k is different
		ROM_LOAD( "bw_bv-01.7a",  0x08000, 0x04000, CRC(b960c707);SHA1(086cb0f22fb59922bf0369bf6b382a241d979ec3) )
		ROM_LOAD( "bv00.bin",     0x0c000, 0x04000, CRC(0bbc1222);SHA1(cfdf621a423a5ce4ba44a980e683d2abf044d6b9) ) // different
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );// sub CPU
		ROM_LOAD( "bw_bv-06.10d", 0x0a000, 0x02000, CRC(91a21a4c);SHA1(042eed60119a861f6b3ccfbe68d880f182a8a8e1) )
		ROM_LOAD( "bw_bv-05.9d",  0x0c000, 0x02000, CRC(f283f39a);SHA1(9f7f4c39d49f4dfff73fe74cd457480e8a43a3c5) )
		ROM_LOAD( "bw_bv-04.7d",  0x0e000, 0x02000, CRC(29ae75b6);SHA1(48c94e996857f2ac995bcd25f0e67b9f7c17d807) )
	
		ROM_REGION( 0x10000, REGION_CPU3, 0 );// sound CPU(encrypted)
		ROM_LOAD( "bw_bv-03.13a", 0x0e000, 0x02000, CRC(e8ac9379);SHA1(aaf5c20aa33ed05747a8a27739e9d09e094a518d) )
	
		// Bottom Board(CCU-01)
		ROM_REGION( 0x01000, REGION_GFX1, ROMREGION_DISPOSE );// chars
		ROM_LOAD( "bw_bv-10.5c",  0x00000, 0x01000, CRC(edca6901);SHA1(402c80e7519cf3a43b9fef52c9923961220a48b6) )
	
		// Middle Board(MCU-01)
		ROM_REGION( 0x0c000, REGION_GFX2, ROMREGION_DISPOSE );// sprites
		ROM_LOAD( "bw_bv-07.1l",  0x00000, 0x04000, CRC(3d5ab2be);SHA1(2b3a039914ebfcc3993da74853a67546fc22c191) )
		ROM_LOAD( "bw_bv-08.1k",  0x04000, 0x04000, CRC(7a585f1e);SHA1(99e5d947b6b1fa96b90c676a282376d67fc377f0) )
		ROM_LOAD( "bw_bv-09.1h",  0x08000, 0x04000, CRC(a14c0b57);SHA1(5033354793d77922f5ef7f268cbe212e551efadf) )
	
		// GPU Banks
		ROM_REGION( 0x08000, REGION_USER1, 0 );
		ROM_FILL(0x00000, 0x08000, 0);
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_batwings = new RomLoadPtr(){ public void handler(){ 
		// Top Board(SCU-01)
		ROM_REGION( 0x10000, REGION_CPU1, 0 );// main CPU
		ROM_LOAD( "bv-02-.10a",   0x04000, 0x04000, CRC(6074a86b);SHA1(0ce1bd74450144fd3c6556787d6c5c5d4531d830) )  // different
		ROM_LOAD( "bw_bv-01.7a",  0x08000, 0x04000, CRC(b960c707);SHA1(086cb0f22fb59922bf0369bf6b382a241d979ec3) )
		ROM_LOAD( "bv-00-.4a",    0x0c000, 0x04000, CRC(1f83804c);SHA1(afd5eb0822db4fd982062945ca27e66ed9680645) )  // different
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );// sub CPU
		ROM_LOAD( "bv-06-.10d", 0x0a000, 0x02000, NO_DUMP );// bad dump filled with 0x7e, no substitute found
		ROM_LOAD( "bv-05-.9d",  0x0c000, 0x02000, CRC(1e393300);SHA1(8d847256eb5dbccf5f524ec3aa836073d70b4edc) )  // different
		ROM_LOAD( "bv-04-.7d",  0x0e000, 0x02000, CRC(6548c5bb);SHA1(d12cc8d0d5692c3de766f5c42c818dd8f685760a) )  // different
	
		ROM_REGION( 0x10000, REGION_CPU3, 0 );// sound CPU(encrypted)
		ROM_LOAD( "bw_bv-03.13a", 0x0e000, 0x02000, CRC(e8ac9379);SHA1(aaf5c20aa33ed05747a8a27739e9d09e094a518d) )
	
		// Bottom Board(CCU-01)
		ROM_REGION( 0x01000, REGION_GFX1, ROMREGION_DISPOSE );// chars
		ROM_LOAD( "bw_bv-10.5c",  0x00000, 0x01000, CRC(edca6901);SHA1(402c80e7519cf3a43b9fef52c9923961220a48b6) )
	
		// Middle Board(MCU-01)
		ROM_REGION( 0x0c000, REGION_GFX2, ROMREGION_DISPOSE );// sprites
		ROM_LOAD( "bw_bv-07.1l",  0x00000, 0x04000, CRC(3d5ab2be);SHA1(2b3a039914ebfcc3993da74853a67546fc22c191) )
		ROM_LOAD( "bw_bv-08.1k",  0x04000, 0x04000, CRC(7a585f1e);SHA1(99e5d947b6b1fa96b90c676a282376d67fc377f0) )
		ROM_LOAD( "bw_bv-09.1h",  0x08000, 0x04000, CRC(a14c0b57);SHA1(5033354793d77922f5ef7f268cbe212e551efadf) )
	
		// GPU Banks
		ROM_REGION( 0x08000, REGION_USER1, 0 );
		ROM_FILL(0x00000, 0x08000, 0);
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_zaviga = new RomLoadPtr(){ public void handler(){ 
		// Top Board(DE-0169-0)
		ROM_REGION( 0x10000, REGION_CPU1, 0 );// main CPU
		ROM_LOAD( "as04", 0x04000, 0x04000, CRC(b79f5da2);SHA1(b39748666d3f7fb1ac46d282cce09fe9531df6b1) )
		ROM_LOAD( "as02", 0x08000, 0x04000, CRC(6addd16a);SHA1(940637c49bf9f38c77176ed2ae212048e9e7fd8f) )
		ROM_LOAD( "as00", 0x0c000, 0x04000, CRC(c6ae4af0);SHA1(6f6f14385b20f9c9c312f816036c608fe8514b00) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );// sub CPU
		ROM_LOAD( "as08", 0x0a000, 0x02000, CRC(b6187b3a);SHA1(d2d7c5b185f59986f45d8ec3ddf9b95364e57d96) )
		ROM_LOAD( "as07", 0x0c000, 0x02000, CRC(dc1170e3);SHA1(c8e4d1564fd272d726d0e4ffd4f33f67f1b37cd7) )
		ROM_LOAD( "as06", 0x0e000, 0x02000, CRC(ba888f84);SHA1(f94de8553cd4704d9b3349ded881a7cc62fa9b57) )
	
		ROM_REGION( 0x10000, REGION_CPU3, 0 );// sound CPU(encrypted)
		ROM_LOAD( "as05", 0x0e000, 0x02000, CRC(afe9b0ac);SHA1(3c653cd4fff7f4e00971249900b5a810b6e74dfe) )
	
		// Bottom Board(DE-0170-0)
		ROM_REGION( 0x01000, REGION_GFX1, ROMREGION_DISPOSE );// chars
		ROM_LOAD( "as14", 0x00000, 0x01000, CRC(62132c1d);SHA1(6b101e220a440488da17de8446f4e2c8ec7c7de9) )
	
		// Middle Board(DE-0171-0)
		ROM_REGION( 0x0c000, REGION_GFX2, ROMREGION_DISPOSE );// sprites
		ROM_LOAD( "as11", 0x00000, 0x04000, CRC(aa84af24);SHA1(af4ff085dc44b3d1493ec1c8b4a8d18dccecc872) )
		ROM_LOAD( "as12", 0x04000, 0x04000, CRC(84af9041);SHA1(8fbd5995ca8e708cd7fb9cdfcdb174e12084f526) )
		ROM_LOAD( "as13", 0x08000, 0x04000, CRC(15d0922b);SHA1(b8d715a9e610531472d516c19f6035adbce93c84) )
	
		// GPU Banks
		ROM_REGION( 0x08000, REGION_USER1, 0 );
		ROM_FILL(0x00000, 0x08000, 0);
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_zavigaj = new RomLoadPtr(){ public void handler(){ 
		// Top Board(DE-0169-0)
		ROM_REGION( 0x10000, REGION_CPU1, 0 );// main CPU
		ROM_LOAD( "as04", 0x04000, 0x04000, CRC(b79f5da2);SHA1(b39748666d3f7fb1ac46d282cce09fe9531df6b1) )
		ROM_LOAD( "as02", 0x08000, 0x04000, CRC(6addd16a);SHA1(940637c49bf9f38c77176ed2ae212048e9e7fd8f) )
		ROM_LOAD( "as00", 0x0c000, 0x04000, CRC(c6ae4af0);SHA1(6f6f14385b20f9c9c312f816036c608fe8514b00) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );// sub CPU
		ROM_LOAD( "as08",     0x0a000, 0x02000, CRC(b6187b3a);SHA1(d2d7c5b185f59986f45d8ec3ddf9b95364e57d96) )
		ROM_LOAD( "as07",     0x0c000, 0x02000, CRC(dc1170e3);SHA1(c8e4d1564fd272d726d0e4ffd4f33f67f1b37cd7) )
		ROM_LOAD( "as06-.7d", 0x0e000, 0x02000, CRC(b02d270c);SHA1(beea3d44d367543b5b5075c5892580e690691e75) )  // different
	
		ROM_REGION( 0x10000, REGION_CPU3, 0 );// sound CPU(encrypted)
		ROM_LOAD( "as05", 0x0e000, 0x02000, CRC(afe9b0ac);SHA1(3c653cd4fff7f4e00971249900b5a810b6e74dfe) )
	
		// Bottom Board(DE-0170-0)
		ROM_REGION( 0x01000, REGION_GFX1, ROMREGION_DISPOSE );// chars
		ROM_LOAD( "as14", 0x00000, 0x01000, CRC(62132c1d);SHA1(6b101e220a440488da17de8446f4e2c8ec7c7de9) )
	
		// Middle Board(DE-0171-0)
		ROM_REGION( 0x0c000, REGION_GFX2, ROMREGION_DISPOSE );// sprites
		ROM_LOAD( "as11", 0x00000, 0x04000, CRC(aa84af24);SHA1(af4ff085dc44b3d1493ec1c8b4a8d18dccecc872) )
		ROM_LOAD( "as12", 0x04000, 0x04000, CRC(84af9041);SHA1(8fbd5995ca8e708cd7fb9cdfcdb174e12084f526) )
		ROM_LOAD( "as13", 0x08000, 0x04000, CRC(15d0922b);SHA1(b8d715a9e610531472d516c19f6035adbce93c84) )
	
		// GPU Banks
		ROM_REGION( 0x08000, REGION_USER1, 0 );
		ROM_FILL(0x00000, 0x08000, 0);
	ROM_END(); }}; 
	
	//****************************************************************************
	// Initializations
	
	static void fix_bwp3(void)
	{
		unsigned char *rom = bwp3_rombase;
		int i, j = bwp3_romsize;
		unsigned char ah, al;
	
		// swap nibbles
		for (i=0; i<j; i++) { ah = al = rom[i]; rom[i] = (ah >> 4) | (al << 4); }
	
		// relocate vectors
		rom[j-(0x10-0x4)] = rom[j-(0x10-0xb)] = rom[j-(0x10-0x6)];
		rom[j-(0x10-0x5)] = rom[j-(0x10-0xa)] = rom[j-(0x10-0x7)];
	}
	
	
	static DRIVER_INIT( bwing )
	{
		bwp123_membase[0] = memory_region(REGION_CPU1);
		bwp123_membase[1] = memory_region(REGION_CPU2);
		bwp123_membase[2] = memory_region(REGION_CPU3);
	
		fix_bwp3();
	}
	
	//****************************************************************************
	// Game Entries
	
	public static GameDriver driver_bwing	   = new GameDriver("1984"	,"bwing"	,"bwing.java"	,rom_bwing,null	,machine_driver_bwing	,input_ports_bwing	,init_bwing	,ROT90	,	"Data East Corporation", "B-Wings (Japan)" )
	public static GameDriver driver_bwings	   = new GameDriver("1984"	,"bwings"	,"bwing.java"	,rom_bwings,driver_bwing	,machine_driver_bwing	,input_ports_bwing	,init_bwing	,ROT90	,	"Data East Corporation", "Battle Wings" )
	public static GameDriver driver_batwings	   = new GameDriver("1984"	,"batwings"	,"bwing.java"	,rom_batwings,driver_bwing	,machine_driver_bwing	,input_ports_bwing	,init_bwing	,ROT90	,	"Data East Corporation", "Battle Wings (alt)", GAME_NOT_WORKING )
	
	public static GameDriver driver_zaviga	   = new GameDriver("1984"	,"zaviga"	,"bwing.java"	,rom_zaviga,null	,machine_driver_bwing	,input_ports_bwing	,init_bwing	,ROT90	,	"Data East Corporation", "Zaviga" )
	public static GameDriver driver_zavigaj	   = new GameDriver("1984"	,"zavigaj"	,"bwing.java"	,rom_zavigaj,driver_zaviga	,machine_driver_bwing	,input_ports_bwing	,init_bwing	,ROT90	,	"Data East Corporation", "Zaviga (Japan)" )
}
