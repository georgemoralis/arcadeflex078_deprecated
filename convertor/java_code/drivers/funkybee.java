/***************************************************************************

Funky Bee/Sky Lancer memory map (preliminary)

driver by Zsolt Vasvari

MAIN CPU:

0000-4fff ROM
8000-87ff RAM
a000-bfff video RAM (only 0x20 bytes of each 0x100 byte block is used)
					(also contains sprite RAM)
c000-dfff color RAM (only 0x20 bytes of each 0x100 byte block is used)
					(also contains sprite RAM)

read:
f000	  interrupt ACK
f800	  IN0/watchdog
f801	  IN1
f802	  IN2

write:
e000	  row scroll
e800	  flip screen
e802-e803 coin counter
e804	  ???
e805	  gfx bank select
e806	  ???
f800	  watchdog


I/0 ports:
write
00		  8910	control
01		  8910	write

AY8910 Port A = DSW


Known issues:

* skylancr fires in the same spot regardless of player position when in cocktail mode.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class funkybee
{
	
	
	extern extern extern extern extern 
	extern PALETTE_INIT( funkybee );
	extern VIDEO_START( funkybee );
	extern VIDEO_UPDATE( funkybee );
	
	
	public static ReadHandlerPtr funkybee_input_port_0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		watchdog_reset_r(0);
		return input_port_0_r(offset);
	} };
	
	public static WriteHandlerPtr funkybee_coin_counter_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		coin_counter_w(offset,data);
	} };
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x4fff, MRA_ROM ),
		new Memory_ReadAddress( 0x8000, 0x87ff, MRA_RAM ),
		new Memory_ReadAddress( 0xa000, 0xdfff, MRA_RAM ),
		new Memory_ReadAddress( 0xf000, 0xf000, MRA_NOP ),	/* IRQ Ack */
		new Memory_ReadAddress( 0xf800, 0xf800, funkybee_input_port_0_r ),
		new Memory_ReadAddress( 0xf801, 0xf801, input_port_1_r ),
		new Memory_ReadAddress( 0xf802, 0xf802, input_port_2_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x4fff, MWA_ROM ),
		new Memory_WriteAddress( 0x8000, 0x87ff, MWA_RAM ),
		new Memory_WriteAddress( 0xa000, 0xbfff, funkybee_videoram_w, videoram ),
		new Memory_WriteAddress( 0xc000, 0xdfff, funkybee_colorram_w, colorram ),
		new Memory_WriteAddress( 0xe000, 0xe000, funkybee_scroll_w ),
		new Memory_WriteAddress( 0xe800, 0xe800, funkybee_flipscreen_w ),
		new Memory_WriteAddress( 0xe802, 0xe803, funkybee_coin_counter_w ),
		new Memory_WriteAddress( 0xe805, 0xe805, funkybee_gfx_bank_w ),
		new Memory_WriteAddress( 0xf800, 0xf800, watchdog_reset_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static IO_ReadPort readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x02, 0x02, AY8910_read_port_0_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00, 0x00, AY8910_control_port_0_w ),
		new IO_WritePort( 0x01, 0x01, AY8910_write_port_0_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	
	static InputPortPtr input_ports_funkybee = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_DIPNAME( 0x20, 0x20, "Freeze" );
		PORT_DIPSETTING(	0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
	
		PORT_START(); 		/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0xe0, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 		/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0xe0, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 		/* DSW */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(	0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(	0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x0c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x30, "3" );
		PORT_DIPSETTING(	0x20, "4" );
		PORT_DIPSETTING(	0x10, "5" );
		PORT_DIPSETTING(	0x00, "6" );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(	0x80, DEF_STR( "Cocktail") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_skylancr = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_DIPNAME( 0x20, 0x20, "Freeze" );
		PORT_DIPSETTING(	0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
	
		PORT_START(); 		/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0xe0, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 		/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0xe0, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 		/* DSW */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(	0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(	0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x0c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x30, "3" );
		PORT_DIPSETTING(	0x20, "4" );
		PORT_DIPSETTING(	0x10, "5" );
		PORT_DIPSETTING(	0x00, "6" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(	0x40, "20000 50000" );
		PORT_DIPSETTING(	0x00, "40000 70000" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(	0x80, DEF_STR( "Cocktail") );
	INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		256,	/* 256 characters */
		2,		/* 2 bits per pixel */
		new int[] { 0, 4 },	/* the two bitplanes for 4 pixels are packed into one byte */
		new int[] { 0, 1, 2, 3, 8*8+0, 8*8+1, 8*8+2, 8*8+3 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		16*8	/* every char takes 16 bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		8,32,	/* 8*32 sprites */
		128,		/* 128 sprites */
		2,		/* 2 bits per pixel */
		new int[] { 0, 4 },	/* the two bitplanes for 4 pixels are packed into one byte */
		new int[] { 0, 1, 2, 3, 8*8+0, 8*8+1, 8*8+2, 8*8+3 },
		new int[] {  0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
		  16*8, 17*8, 18*8, 19*8, 20*8, 21*8, 22*8, 23*8,
		  32*8, 33*8, 34*8, 35*8, 36*8, 37*8, 38*8, 39*8,
		  48*8, 49*8, 50*8, 51*8, 52*8, 53*8, 54*8, 55*8 },
		4*16*8	/* every sprite takes 64 bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,	  0, 8 ),
		new GfxDecodeInfo( REGION_GFX2, 0, charlayout,	  0, 8 ),
		new GfxDecodeInfo( REGION_GFX1, 0, spritelayout, 16, 4 ),
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout, 16, 4 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
		1,	/* 1 chip */
		1500000,	/* 1.5 MHz?????? */
		new int[] { 50 },
		new ReadHandlerPtr[] { input_port_3_r },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 }
	);
	
	
	static MACHINE_DRIVER_START( funkybee )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80, 3072000)	/* 3.072 MHz */
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_PORTS(readport,writeport)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(32*8, 32*8)
		MDRV_VISIBLE_AREA(12, 32*8-1-12, 0*8, 28*8-1)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(32)
		MDRV_COLORTABLE_LENGTH(32)
	
		MDRV_PALETTE_INIT(funkybee)
		MDRV_VIDEO_START(funkybee)
		MDRV_VIDEO_UPDATE(funkybee)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, ay8910_interface)
	MACHINE_DRIVER_END
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_funkybee = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );/* 64k for code */
		ROM_LOAD( "funkybee.1",    0x0000, 0x1000, CRC(3372cb33);SHA1(09f2673cdeaadba8211d86a19e727aebb4d8be9d) )
		ROM_LOAD( "funkybee.3",    0x1000, 0x1000, CRC(7bf7c62f);SHA1(f8e5514c17fddb8ed95e5e18aab81ad0ebcc41af) )
		ROM_LOAD( "funkybee.2",    0x2000, 0x1000, CRC(8cc0fe8e);SHA1(416d97db0a2219ea46f2caa55787253e16a5ef32) )
		ROM_LOAD( "funkybee.4",    0x3000, 0x1000, CRC(1e1aac26);SHA1(a2974e6a8da5568f91aa44adb58941b0a60b1536) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "funkybee.5",    0x0000, 0x2000, CRC(86126655);SHA1(d91682121d7f6a70f10a946ab81b248cc29bdf8c) )
	
		ROM_REGION( 0x2000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "funkybee.6",    0x0000, 0x2000, CRC(5fffd323);SHA1(9de9c869bd1e2daab3b94275444ecbe904bcd6aa) )
	
		ROM_REGION( 0x0020, REGION_PROMS, 0 );
		ROM_LOAD( "funkybee.clr",  0x0000, 0x0020, CRC(e2cf5fe2);SHA1(50b293f48f078cbcebccb045aa779ced2fb298c8) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_skylancr = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );/* 64k for code */
		ROM_LOAD( "1.5a",          0x0000, 0x2000, CRC(82d55824);SHA1(5c457e720ac8611bea4bc7e63ba4ee1c11200471) )
		ROM_LOAD( "2.5c",          0x2000, 0x2000, CRC(dff3a682);SHA1(e3197e106c2c6d198d2769b63701222d48a196d1) )
		ROM_LOAD( "3.5d",          0x4000, 0x1000, CRC(7c006ee6);SHA1(22719d4d0ad5c4f534a1613e0d74cab73973bab7) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "4.6a",          0x0000, 0x2000, CRC(0f8ede07);SHA1(e04456fe12e2282191aee4823941f23ad8bda99d) )
	
		ROM_REGION( 0x2000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "5.6b",          0x0000, 0x2000, CRC(24cec070);SHA1(2b7977b07acbe1394765675cd469db13a3b495f2) )
	
		ROM_REGION( 0x0020, REGION_PROMS, 0 );
		ROM_LOAD( "18s030.1a",     0x0000, 0x0020, CRC(e645bacb);SHA1(5f4c299c4cf165fd229731c0e5799a34892bf28e) )
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_funkybee	   = new GameDriver("1982"	,"funkybee"	,"funkybee.java"	,rom_funkybee,null	,machine_driver_funkybee	,input_ports_funkybee	,null	,ROT90	,	"Orca", "Funky Bee" )
	public static GameDriver driver_skylancr	   = new GameDriver("1983"	,"skylancr"	,"funkybee.java"	,rom_skylancr,null	,machine_driver_funkybee	,input_ports_skylancr	,null	,ROT90	,	"Orca (Esco Trading Co license)", "Sky Lancer" )
}
