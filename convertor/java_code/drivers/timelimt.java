/***************************************************************************

Time Limit (c) 1983 Chuo
Progress   (c) 1984 Chuo

driver by Ernesto Corvi

Notes:
- Sprite colors are wrong (missing colortable?)

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class timelimt
{
	
	/* from vidhrdw */
	extern VIDEO_START( timelimt );
	extern PALETTE_INIT( timelimt );
	extern VIDEO_UPDATE( timelimt );
	
	extern extern extern extern extern 
	extern data8_t *timelimt_bg_videoram;
	extern size_t timelimt_bg_videoram_size;
	
	/***************************************************************************/
	
	static int nmi_enabled = 0;
	
	static MACHINE_INIT( timelimt )
	{
		soundlatch_setclearedvalue( 0 );
		nmi_enabled = 0;
	}
	
	public static WriteHandlerPtr nmi_enable_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		nmi_enabled = data & 1;	/* bit 0 = nmi enable */
	} };
	
	public static WriteHandlerPtr sound_reset_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ( data & 1 )
			cpu_set_reset_line( 1, PULSE_LINE );
	} };
	
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),		/* rom */
		new Memory_ReadAddress( 0x8000, 0x87ff, MRA_RAM ),		/* ram */
		new Memory_ReadAddress( 0x8800, 0x8bff, MRA_RAM ),		/* video ram */
		new Memory_ReadAddress( 0x9000, 0x97ff, MRA_RAM ),		/* background ram */
		new Memory_ReadAddress( 0x9800, 0x98ff, MRA_RAM ),		/* sprite ram */
		new Memory_ReadAddress( 0xa000, 0xa000, input_port_0_r ), /* input port */
		new Memory_ReadAddress( 0xa800, 0xa800, input_port_1_r ),	/* input port */
		new Memory_ReadAddress( 0xb000, 0xb000, input_port_2_r ),	/* DSW */
		new Memory_ReadAddress( 0xb800, 0xb800, MRA_NOP ),		/* NMI ack? */
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM ),		/* rom */
		new Memory_WriteAddress( 0x8000, 0x87ff, MWA_RAM ),		/* ram */
		new Memory_WriteAddress( 0x8800, 0x8bff, timelimt_videoram_w, videoram, videoram_size ),	/* video ram */
		new Memory_WriteAddress( 0x9000, 0x97ff, timelimt_bg_videoram_w, timelimt_bg_videoram, timelimt_bg_videoram_size ),/* background ram */
		new Memory_WriteAddress( 0x9800, 0x98ff, MWA_RAM, spriteram, spriteram_size ),	/* sprite ram */
		new Memory_WriteAddress( 0xb000, 0xb000, nmi_enable_w ),	/* nmi enable */
		new Memory_WriteAddress( 0xb003, 0xb003, sound_reset_w ),	/* sound reset ? */
		new Memory_WriteAddress( 0xb800, 0xb800, soundlatch_w ), 	/* sound write */
		new Memory_WriteAddress( 0xc800, 0xc800, timelimt_scroll_x_lsb_w ),
		new Memory_WriteAddress( 0xc801, 0xc801, timelimt_scroll_x_msb_w ),
		new Memory_WriteAddress( 0xc802, 0xc802, timelimt_scroll_y_w ),
		new Memory_WriteAddress( 0xc803, 0xc803, MWA_NOP ),		/* ???? bit 0 used only */
		new Memory_WriteAddress( 0xc804, 0xc804, MWA_NOP ),		/* ???? not used */
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static IO_ReadPort readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x00, 0x00, watchdog_reset_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	public static Memory_ReadAddress readmem_sound[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x1fff, MRA_ROM ),	/* rom */
		new Memory_ReadAddress( 0x3800, 0x3bff, MRA_RAM ),	/* ram */
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress writemem_sound[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x1fff, MWA_ROM ),	/* rom */
		new Memory_WriteAddress( 0x3800, 0x3bff, MWA_RAM ),	/* ram */
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static IO_ReadPort readport_sound[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x8c, 0x8d, AY8910_read_port_0_r ),
		new IO_ReadPort( 0x8e, 0x8f, AY8910_read_port_1_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort writeport_sound[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00, 0x00, soundlatch_clear_w ),
		new IO_WritePort( 0x8c, 0x8c, AY8910_control_port_0_w ),
		new IO_WritePort( 0x8d, 0x8d, AY8910_write_port_0_w ),
		new IO_WritePort( 0x8e, 0x8e, AY8910_control_port_1_w ),
		new IO_WritePort( 0x8f, 0x8f, AY8910_write_port_1_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	/***************************************************************************/
	
	static InputPortPtr input_ports_timelimt = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* probably unused */
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* probably unused */
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x07, 0x01, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_7C") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPSETTING(    0x10, "5" );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_BITX(    0x80, 0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invincibility", IP_KEY_NONE, IP_JOY_NONE );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_progress = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON3 );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* probably unused */
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x07, 0x01, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_7C") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPSETTING(    0x10, "5" );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	/***************************************************************************/
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,2),
		4,
		new int[] { RGN_FRAC(0,2)+0, RGN_FRAC(0,2)+4, RGN_FRAC(1,2)+0, RGN_FRAC(1,2)+4 },
		new int[] { 0, 1, 2, 3, 8+0, 8+1, 8+2, 8+3 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		16*8
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,3),
		3,
		new int[] { RGN_FRAC(0,3), RGN_FRAC(1,3), RGN_FRAC(2,3) },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,
				8*8+0, 8*8+1, 8*8+2, 8*8+3, 8*8+4, 8*8+5, 8*8+6, 8*8+7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				16*8, 17*8, 18*8, 19*8, 20*8, 21*8, 22*8, 23*8 },
		32*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,   32, 1 ),	/* seems correct */
		new GfxDecodeInfo( REGION_GFX2, 0, charlayout,    0, 1 ),	/* seems correct */
		new GfxDecodeInfo( REGION_GFX3, 0, spritelayout,  0, 8 ),	/* ?? */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo progress_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,   32, 1 ),	/* seems correct */
		new GfxDecodeInfo( REGION_GFX2, 0, charlayout,    0, 1 ),	/* seems correct */
		new GfxDecodeInfo( REGION_GFX3, 0, spritelayout, 64, 4 ),	/* seems correct */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	/***************************************************************************/
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
		2,	/* 2 chips */
		18432000/12,
		new int[] { 25, 25 },
		new ReadHandlerPtr[] { 0, soundlatch_r },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 }
	);
	
	static INTERRUPT_GEN( timelimt_irq ) {
		if ( nmi_enabled )
			cpu_set_irq_line(0, IRQ_LINE_NMI, PULSE_LINE);
	}
	
	/***************************************************************************/
	
	static MACHINE_DRIVER_START( timelimt )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80, 5000000)	/* 5.000 MHz */
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_PORTS(readport,0)
		MDRV_CPU_VBLANK_INT(timelimt_irq,1)
	
		MDRV_CPU_ADD(Z80,18432000/6)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)	/* 3.072 MHz */
		MDRV_CPU_MEMORY(readmem_sound,writemem_sound)
		MDRV_CPU_PORTS(readport_sound,writeport_sound)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1) /* ? */
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
		MDRV_INTERLEAVE(50)
	
		MDRV_MACHINE_INIT(timelimt)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(32*8, 32*8)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(64)
		MDRV_COLORTABLE_LENGTH(64)
	
		MDRV_PALETTE_INIT(timelimt)
		MDRV_VIDEO_START(timelimt)
		MDRV_VIDEO_UPDATE(timelimt)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, ay8910_interface)
	MACHINE_DRIVER_END
	
	static MACHINE_DRIVER_START( progress )
		/* basic machine hardware */
		MDRV_IMPORT_FROM(timelimt)
	
		MDRV_GFXDECODE(progress_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(96)
		MDRV_COLORTABLE_LENGTH(96)
	
	MACHINE_DRIVER_END
	
	/***************************************************************************
	
	  Game ROM(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_timelimt = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );/* ROMs */
		ROM_LOAD( "t8",     0x0000, 0x2000, CRC(006767ca),SHA1(a5d528c58cd73c0101ffa9ab783ec870668256db) )
		ROM_LOAD( "t7",     0x2000, 0x2000, CRC(cbe7cd86),SHA1(502a78c14c9717a466ea24cdc63da4c0f3bec1f9) )
		ROM_LOAD( "t6",     0x4000, 0x2000, CRC(f5f17e39),SHA1(7d78f551ce73276725c349703a790f2a63bb5503) )
		ROM_LOAD( "t9",     0x6000, 0x2000, CRC(2d72ab45),SHA1(01d4afacc01b9e7c49355123efd5f5ad4d79a9cd) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* ROMs */
		ROM_LOAD( "tl5",    0x0000, 0x1000, CRC(5b782e4a),SHA1(2f4fe2beb8efa5a636fefc1ee172d0200d1c9497) )
		ROM_LOAD( "tl4",    0x1000, 0x1000, CRC(a32883a9),SHA1(26e1725b67be87db28855672facb1504b8ac84d6) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE );/* tiles */
		ROM_LOAD( "tl11",   0x0000, 0x1000, CRC(46676307),SHA1(38fe80722972b6b3ba32705469a0dcb868fb76a9) )
		ROM_LOAD( "tl10",   0x1000, 0x1000, CRC(2336908a),SHA1(345fc209ce891cc6f8f111c6d3a9e0f65ee6d818) )
	
		ROM_REGION( 0x2000, REGION_GFX2, ROMREGION_DISPOSE );/* tiles */
		ROM_LOAD( "tl13",   0x0000, 0x1000, CRC(072e4053),SHA1(209edf7b371078e38d1c2812fa6a3d1a78193b3f) )
		ROM_LOAD( "tl12",   0x1000, 0x1000, CRC(ce960389),SHA1(57ee52cfa1b5a3832b362b38c8b7aa411dfc782b) )
	
		ROM_REGION( 0x6000, REGION_GFX3, ROMREGION_DISPOSE );/* sprites */
		ROM_LOAD( "tl3",    0x0000, 0x2000, CRC(01a9fd95),SHA1(cd1078700c97a3539c9d9447c55efbd27540a1b3) )
		ROM_LOAD( "tl2",    0x2000, 0x2000, CRC(4693b849),SHA1(fbebedde53599fb1eaedc648bd704b321ab096b5) )
		ROM_LOAD( "tl1",    0x4000, 0x2000, CRC(c4007caf),SHA1(ae05af3319545d5ca98a046bfc100138a5a3ed96) )
	
		ROM_REGION( 0x0040, REGION_PROMS, 0 );
		ROM_LOAD( "clr.35", 0x0000, 0x0020, CRC(9c9e6073),SHA1(98496175bf19a8cdb0018705bc1a2193b8a782e1) )
		ROM_LOAD( "clr.48", 0x0020, 0x0020, BAD_DUMP CRC(a0bcac59),SHA1(e5832831b21981363509b79d89766757bd9273b0)  )	/* FIXED BITS (xxxxxx1x) */
		/* missing a prom? */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_progress = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );/* ROMs */
		ROM_LOAD( "pg8.bin",     0x0000, 0x2000, CRC(e8779658),SHA1(3eca574d7328d54e544e663f58be789dbf151e77) )
		ROM_LOAD( "pg7.bin",     0x2000, 0x2000, CRC(5dcf6b6f),SHA1(550f02ff5ed2935f4c3c9055c5742fea46f42351) )
		ROM_LOAD( "pg6.bin",     0x4000, 0x2000, CRC(f21d2a08),SHA1(b2542e895d6d011895abec641b056ad8d7dc0d15) )
		ROM_LOAD( "pg9.bin",     0x6000, 0x2000, CRC(052ab4ac),SHA1(a2bfb575f2dfde862f9b1e8a4378f9b6b6200831) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* ROMs */
		ROM_LOAD( "pg4.bin",    0x0000, 0x1000, CRC(b1cc2fe8),SHA1(c9045e7b65311b052c337ad3bedadf108d1c24c3) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE );/* tiles */
		ROM_LOAD( "pg11.bin",   0x0000, 0x1000, CRC(bd8462e4),SHA1(91b1bd2d69aa1b1a84ee8e642b2c1131a7697dd9) )
		ROM_LOAD( "pg10.bin",   0x1000, 0x1000, CRC(c4bbf0b8),SHA1(d149eda9637474febdafd565a60eb2940702f162) )
	
		ROM_REGION( 0x2000, REGION_GFX2, ROMREGION_DISPOSE );/* tiles */
		ROM_LOAD( "pg13.bin",   0x0000, 0x1000, CRC(25ec45be),SHA1(1271b7a5632934a82ccae35de8c2968247a233bb) )
		ROM_LOAD( "pg12.bin",   0x1000, 0x1000, CRC(c837c5f5),SHA1(dbfc0d8afe0a8e9dd213cb4095b23b7aa8e2b6f4) )
	
		ROM_REGION( 0x6000, REGION_GFX3, ROMREGION_DISPOSE );/* sprites */
		ROM_LOAD( "pg1.bin",    0x0000, 0x2000, CRC(155c8f7f),SHA1(0d32ebceb9b2a0b3faf1f91b7a6800999889b331) )
		ROM_LOAD( "pg2.bin",    0x2000, 0x2000, CRC(a6ca4dfc),SHA1(4243c9ea98e365bf342cf928ff97cafb35cdc7b6) )
		ROM_LOAD( "pg3.bin",    0x4000, 0x2000, CRC(2b21c2fb),SHA1(8c95889a19057d32790c9ccddc0977980eddbd0e) )
	
		ROM_REGION( 0x0060, REGION_PROMS, 0 );
		ROM_LOAD( "35.bin", 0x0000, 0x0020, CRC(8c5ca730),SHA1(be2554e1aa4a74d976919e2c37bce5fb4d40352b) )
		ROM_LOAD( "48.bin", 0x0020, 0x0020, CRC(12dd62cd),SHA1(8322b02d73c3eb44b587f76daeaabe6beea58456) )
		ROM_LOAD( "57.bin", 0x0040, 0x0020, CRC(18455a79),SHA1(e4d64368560e3116a922588129f5f91a4c520f7d) )
	ROM_END(); }}; 
	
	public static GameDriver driver_timelimt	   = new GameDriver("1983"	,"timelimt"	,"timelimt.java"	,rom_timelimt,null	,machine_driver_timelimt	,input_ports_timelimt	,null	,ROT90	,	"Chuo Co. Ltd", "Time Limit", GAME_IMPERFECT_COLORS )
	public static GameDriver driver_progress	   = new GameDriver("1984"	,"progress"	,"timelimt.java"	,rom_progress,null	,machine_driver_progress	,input_ports_progress	,null	,ROT90	,	"Chuo Co. Ltd", "Progress" )
}
