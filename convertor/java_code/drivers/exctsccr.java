/***************************************************************************

Exciting Soccer - (c) 1983 Alpha Denshi Co.

Supported sets:
Exciting Soccer - Alpha Denshi
Exciting Soccer (bootleg) - Kazutomi


Preliminary driver by:
Ernesto Corvi
ernesto@imagina.com

Jarek Parchanski
jpdev@friko6.onet.pl


NOTES:
The game supports Coin 2, but the dip switches used for it are the same
as Coin 1. Basically, this allowed to select an alternative coin table
based on wich Coin input was connected.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class exctsccr
{
	
	/* from vidhrdw */
	extern extern extern extern 
	extern PALETTE_INIT( exctsccr );
	extern VIDEO_START( exctsccr );
	extern VIDEO_UPDATE( exctsccr );
	
	/* from machine */
	extern UINT8 *exctsccr_mcu_ram;
	extern extern 
	
	public static WriteHandlerPtr exctsccr_DAC_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		DAC_signed_data_w(offset,data << 2);
	} };
	
	
	/***************************************************************************
	
		Memory definition(s)
	
	***************************************************************************/
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x5fff, MRA_ROM ),
		new Memory_ReadAddress( 0x6000, 0x63ff, MRA_RAM ), /* Alpha mcu (protection) */
		new Memory_ReadAddress( 0x7c00, 0x7fff, MRA_RAM ), /* work ram */
		new Memory_ReadAddress( 0x8000, 0x83ff, MRA_RAM ),
		new Memory_ReadAddress( 0x8400, 0x87ff, MRA_RAM ),
		new Memory_ReadAddress( 0x8800, 0x8bff, MRA_RAM ), /* ??? */
		new Memory_ReadAddress( 0xa000, 0xa000, input_port_0_r ),
		new Memory_ReadAddress( 0xa040, 0xa040, input_port_1_r ),
		new Memory_ReadAddress( 0xa080, 0xa080, input_port_3_r ),
		new Memory_ReadAddress( 0xa0c0, 0xa0c0, input_port_2_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x5fff, MWA_ROM ),
		new Memory_WriteAddress( 0x6000, 0x63ff, exctsccr_mcu_w, exctsccr_mcu_ram ), /* Alpha mcu (protection) */
		new Memory_WriteAddress( 0x7c00, 0x7fff, MWA_RAM ), /* work ram */
		new Memory_WriteAddress( 0x8000, 0x83ff, exctsccr_videoram_w, videoram ),
		new Memory_WriteAddress( 0x8400, 0x87ff, exctsccr_colorram_w, colorram ),
		new Memory_WriteAddress( 0x8800, 0x8bff, MWA_RAM ), /* ??? */
		new Memory_WriteAddress( 0xa000, 0xa000, MWA_NOP ), /* ??? */
		new Memory_WriteAddress( 0xa001, 0xa001, MWA_NOP ), /* ??? */
		new Memory_WriteAddress( 0xa002, 0xa002, exctsccr_gfx_bank_w ),
		new Memory_WriteAddress( 0xa003, 0xa003, exctsccr_flipscreen_w ), /* Cocktail mode ( 0xff = flip screen, 0x00 = normal ) */
		new Memory_WriteAddress( 0xa006, 0xa006, exctsccr_mcu_control_w ), /* MCU control */
		new Memory_WriteAddress( 0xa007, 0xa007, MWA_NOP ), /* This is also MCU control, but i dont need it */
		new Memory_WriteAddress( 0xa040, 0xa06f, MWA_RAM, spriteram ), /* Sprite pos */
		new Memory_WriteAddress( 0xa080, 0xa080, soundlatch_w ),
		new Memory_WriteAddress( 0xa0c0, 0xa0c0, watchdog_reset_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_ReadAddress sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x8fff, MRA_ROM ),
		new Memory_ReadAddress( 0xa000, 0xa7ff, MRA_RAM ),
		new Memory_ReadAddress( 0xc00d, 0xc00d, soundlatch_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x8fff, MWA_ROM ),
		new Memory_WriteAddress( 0xa000, 0xa7ff, MWA_RAM ),
		new Memory_WriteAddress( 0xc008, 0xc009, exctsccr_DAC_data_w ),
		new Memory_WriteAddress( 0xc00c, 0xc00c, soundlatch_w ), /* used to clear the latch */
		new Memory_WriteAddress( 0xc00f, 0xc00f, MWA_NOP ), /* ??? */
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort sound_writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x82, 0x82, AY8910_write_port_0_w ),
		new IO_WritePort( 0x83, 0x83, AY8910_control_port_0_w ),
		new IO_WritePort( 0x86, 0x86, AY8910_write_port_1_w ),
		new IO_WritePort( 0x87, 0x87, AY8910_control_port_1_w ),
		new IO_WritePort( 0x8a, 0x8a, AY8910_write_port_2_w ),
		new IO_WritePort( 0x8b, 0x8b, AY8910_control_port_2_w ),
		new IO_WritePort( 0x8e, 0x8e, AY8910_write_port_3_w ),
		new IO_WritePort( 0x8f, 0x8f, AY8910_control_port_3_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	/* Bootleg */
	public static Memory_ReadAddress bl_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x5fff, MRA_ROM ),
		new Memory_ReadAddress( 0x8000, 0x83ff, MRA_RAM ),
		new Memory_ReadAddress( 0x8400, 0x87ff, MRA_RAM ),
		new Memory_ReadAddress( 0x8800, 0x8fff, MRA_RAM ), /* ??? */
		new Memory_ReadAddress( 0xa000, 0xa000, input_port_0_r ),
		new Memory_ReadAddress( 0xa040, 0xa040, input_port_1_r ),
		new Memory_ReadAddress( 0xa080, 0xa080, input_port_3_r ),
		new Memory_ReadAddress( 0xa0c0, 0xa0c0, input_port_2_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress bl_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x5fff, MWA_ROM ),
		new Memory_WriteAddress( 0x7000, 0x7000, AY8910_write_port_0_w ),
		new Memory_WriteAddress( 0x7001, 0x7001, AY8910_control_port_0_w ),
		new Memory_WriteAddress( 0x8000, 0x83ff, exctsccr_videoram_w, videoram ),
		new Memory_WriteAddress( 0x8400, 0x87ff, exctsccr_colorram_w, colorram ),
		new Memory_WriteAddress( 0x8800, 0x8fff, MWA_RAM ), /* ??? */
		new Memory_WriteAddress( 0xa000, 0xa000, MWA_NOP ), /* ??? */
		new Memory_WriteAddress( 0xa001, 0xa001, MWA_NOP ), /* ??? */
		new Memory_WriteAddress( 0xa002, 0xa002, exctsccr_gfx_bank_w ), /* ??? */
		new Memory_WriteAddress( 0xa003, 0xa003, exctsccr_flipscreen_w ), /* Cocktail mode ( 0xff = flip screen, 0x00 = normal ) */
		new Memory_WriteAddress( 0xa006, 0xa006, MWA_NOP ), /* no MCU, but some leftover code still writes here */
		new Memory_WriteAddress( 0xa007, 0xa007, MWA_NOP ), /* no MCU, but some leftover code still writes here */
		new Memory_WriteAddress( 0xa040, 0xa06f, MWA_RAM, spriteram ), /* Sprite Pos */
		new Memory_WriteAddress( 0xa080, 0xa080, soundlatch_w ),
		new Memory_WriteAddress( 0xa0c0, 0xa0c0, watchdog_reset_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_ReadAddress bl_sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x5fff, MRA_ROM ),
		new Memory_ReadAddress( 0x6000, 0x6000, soundlatch_r ),
		new Memory_ReadAddress( 0xe000, 0xe3ff, MRA_RAM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress bl_sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x5fff, MWA_ROM ),
		new Memory_WriteAddress( 0x8000, 0x8000, MWA_NOP ), /* 0 = DAC sound off, 1 = DAC sound on */
		new Memory_WriteAddress( 0xa000, 0xa000, soundlatch_w ), /* used to clear the latch */
		new Memory_WriteAddress( 0xc000, 0xc000, exctsccr_DAC_data_w ),
		new Memory_WriteAddress( 0xe000, 0xe3ff, MWA_RAM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	/***************************************************************************
	
		Input port(s)
	
	***************************************************************************/
	
	static InputPortPtr input_ports_exctsccr = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN   | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT| IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* DSW0 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, "A 1C/1C B 3C/1C" );
		PORT_DIPSETTING(    0x01, "A 1C/2C B 1C/4C" );
		PORT_DIPSETTING(    0x00, "A 1C/3C B 1C/6C" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x10, "Easy" );
		PORT_DIPSETTING(    0x00, "Hard" );
		PORT_DIPNAME( 0x60, 0x00, "Game Time" );
		PORT_DIPSETTING(    0x20, "1 Min." );
		PORT_DIPSETTING(    0x00, "2 Min." );
		PORT_DIPSETTING(    0x60, "3 Min." );
		PORT_DIPSETTING(    0x40, "4 Min." );
		PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* Has to be 0 */
	INPUT_PORTS_END(); }}; 
	
	/***************************************************************************
	
		Graphic(s) decoding
	
	***************************************************************************/
	
	static GfxLayout charlayout1 = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		256,	/* 256 characters */
		3,		/* 3 bits per pixel */
		new int[] { 0x4000*8+4, 0, 4 },	/* plane offset */
		new int[] { 8*8+0, 8*8+1, 8*8+2, 8*8+3, 0, 1, 2, 3 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		16*8	/* every char takes 16 consecutive bytes */
	);
	
	static GfxLayout charlayout2 = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		256,	/* 256 characters */
		3,		/* 3 bits per pixel */
		new int[] { 0x2000*8, 0, 4 },	/* plane offset */
		new int[] { 8*8+0, 8*8+1, 8*8+2, 8*8+3, 0, 1, 2, 3 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		16*8	/* every char takes 16 consecutive bytes */
	);
	
	static GfxLayout spritelayout1 = new GfxLayout
	(
		16,16,	    /* 16*16 sprites */
		64,	        /* 64 sprites */
		3,	        /* 3 bits per pixel */
		new int[] { 0x4000*8+4, 0, 4 },	/* plane offset */
		new int[] { 8*8, 8*8+1, 8*8+2, 8*8+3, 16*8+0, 16*8+1, 16*8+2, 16*8+3,
				24*8+0, 24*8+1, 24*8+2, 24*8+3, 0, 1, 2, 3  },
		new int[] { 0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
				32 * 8, 33 * 8, 34 * 8, 35 * 8, 36 * 8, 37 * 8, 38 * 8, 39 * 8 },
		64*8	/* every sprite takes 64 bytes */
	);
	
	static GfxLayout spritelayout2 = new GfxLayout
	(
		16,16,	    /* 16*16 sprites */
		64,         /* 64 sprites */
		3,	        /* 3 bits per pixel */
		new int[] { 0x2000*8, 0, 4 },	/* plane offset */
		new int[] { 8*8, 8*8+1, 8*8+2, 8*8+3, 16*8+0, 16*8+1, 16*8+2, 16*8+3,
				24*8+0, 24*8+1, 24*8+2, 24*8+3, 0, 1, 2, 3  },
		new int[] { 0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
				32 * 8, 33 * 8, 34 * 8, 35 * 8, 36 * 8, 37 * 8, 38 * 8, 39 * 8 },
		64*8	/* every sprite takes 64 bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,		/* 16*16 sprites */
		64,	    	/* 64 sprites */
		3,	    	/* 2 bits per pixel */
		new int[] { 0x1000*8+4, 0, 4 },	/* plane offset */
		new int[] { 8*8, 8*8+1, 8*8+2, 8*8+3, 16*8+0, 16*8+1, 16*8+2, 16*8+3,
				24*8+0, 24*8+1, 24*8+2, 24*8+3, 0, 1, 2, 3  },
		new int[] { 0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
				32 * 8, 33 * 8, 34 * 8, 35 * 8, 36 * 8, 37 * 8, 38 * 8, 39 * 8 },
		64*8	/* every sprite takes 64 bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x0000, charlayout1,      0, 32 ), /* chars */
		new GfxDecodeInfo( REGION_GFX1, 0x2000, charlayout2,      0, 32 ), /* chars */
		new GfxDecodeInfo( REGION_GFX1, 0x1000, spritelayout1, 16*8, 32 ), /* sprites */
		new GfxDecodeInfo( REGION_GFX1, 0x3000, spritelayout2, 16*8, 32 ), /* sprites */
		new GfxDecodeInfo( REGION_GFX2, 0x0000, spritelayout,  16*8, 32 ), /* sprites */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	/***************************************************************************
	
		Sound interface(s)
	
	***************************************************************************/
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
		4,	/* 4 chips */
		1500000,	/* 1.5 MHz ? */
		new int[] { 15, 15, 15, 15 }, /* volume */
		new ReadHandlerPtr[] { 0, 0, 0, 0 },
		new ReadHandlerPtr[] { 0, 0, 0, 0 },
		new WriteHandlerPtr[] { 0, 0, 0, 0 }, /* it writes 0s thru port A, no clue what for */
		{ 0, 0, 0, 0 }
	);
	
	static DACinterface dac_interface = new DACinterface
	(
		2,
		new int[] { 50, 50 }
	);
	
	/* Bootleg */
	static AY8910interface bl_ay8910_interface = new AY8910interface
	(
		1,	/* 1 chip */
		1500000,	/* 1.5 MHz ? */
		new int[] { 50 }, /* volume */
		new ReadHandlerPtr[] { 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 }
	);
	
	static DACinterface bl_dac_interface = new DACinterface
	(
		1,
		new int[] { 100 }
	);
	
	/***************************************************************************
	
		Machine driver(s)
	
	***************************************************************************/
	
	static MACHINE_DRIVER_START( exctsccr )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80, 4000000)	/* 4.0 MHz (?) */
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_CPU_ADD(Z80, 4123456)	/* ??? with 4 MHz, nested NMIs might happen */
		MDRV_CPU_MEMORY(sound_readmem,sound_writemem)
		MDRV_CPU_PORTS(0,sound_writeport)
		MDRV_CPU_PERIODIC_INT(nmi_line_pulse,4000) /* 4 kHz, updates the dac */
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(32*8, 32*8)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(32)
		MDRV_COLORTABLE_LENGTH(64*8)
	
		MDRV_PALETTE_INIT(exctsccr)
		MDRV_VIDEO_START(exctsccr)
		MDRV_VIDEO_UPDATE(exctsccr)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, ay8910_interface)
		MDRV_SOUND_ADD(DAC, dac_interface)
	MACHINE_DRIVER_END
	
	/* Bootleg */
	static MACHINE_DRIVER_START( exctsccb )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80, 4000000)	/* 4.0 MHz (?) */
		MDRV_CPU_MEMORY(bl_readmem,bl_writemem)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_CPU_ADD(Z80, 3072000)	/* 3.072 MHz ? */
		MDRV_CPU_MEMORY(bl_sound_readmem,bl_sound_writemem)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(32*8, 32*8)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(32)
		MDRV_COLORTABLE_LENGTH(64*8)
	
		MDRV_PALETTE_INIT(exctsccr)
		MDRV_VIDEO_START(exctsccr)
		MDRV_VIDEO_UPDATE(exctsccr)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, bl_ay8910_interface)
		MDRV_SOUND_ADD(DAC, bl_dac_interface)
	MACHINE_DRIVER_END
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_exctsccr = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );    /* 64k for code */
		ROM_LOAD( "1_g10.bin",    0x0000, 0x2000, CRC(aa68df66);SHA1(f10cac5a4c5aad1e1eb8835174dc8d517bb2921a) )
		ROM_LOAD( "2_h10.bin",    0x2000, 0x2000, CRC(2d8f8326);SHA1(8809e7b081fa2a1966cb51ac969fd7b468d35be0) )
		ROM_LOAD( "3_j10.bin",    0x4000, 0x2000, CRC(dce4a04d);SHA1(9c015e4597ec8921bea213d9841fc69c776a4e6d) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );    /* 64k for code */
		ROM_LOAD( "0_h6.bin",     0x0000, 0x2000, CRC(3babbd6b);SHA1(b81bd47c4449f4f21f2d55d01eb9cb6db10664c7) )
		ROM_LOAD( "9_f6.bin",     0x2000, 0x2000, CRC(639998f5);SHA1(c4ff5e5e75d53dea38449f323186d08d5b57bf90) )
		ROM_LOAD( "8_d6.bin",     0x4000, 0x2000, CRC(88651ee1);SHA1(2052e1b3f9784439369f464e31f4a2b0d1bb0565) )
		ROM_LOAD( "7_c6.bin",     0x6000, 0x2000, CRC(6d51521e);SHA1(2809bd2e61f40dcd31d43c62520982bdcfb0a865) )
		ROM_LOAD( "1_a6.bin",     0x8000, 0x1000, CRC(20f2207e);SHA1(b1ed2237d0bd50ddbe593fd2fbff9f1d67c1eb11) )
	
		ROM_REGION( 0x06000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "4_a5.bin",     0x0000, 0x2000, CRC(c342229b);SHA1(a989d6c12521c77882a7e17d4d80afe7eae05906) )
		ROM_LOAD( "5_b5.bin",     0x2000, 0x2000, CRC(35f4f8c9);SHA1(cdf5bbfea9abdd338938e5f4499d2d71ce3c6237) )
		ROM_LOAD( "6_c5.bin",     0x4000, 0x2000, CRC(eda40e32);SHA1(6c08fd4f4fb35fd354d02e04548e960c545f6a88) )
	
		ROM_REGION( 0x02000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "2_k5.bin",     0x0000, 0x1000, CRC(7f9cace2);SHA1(bf05a31716f3ca1c2fd1034cd1f39e2d21cdaed3) )
		ROM_LOAD( "3_l5.bin",     0x1000, 0x1000, CRC(db2d9e0d);SHA1(6ec09a47f7aea6bf31eb0ee78f44012f4d92de8a) )
	
		ROM_REGION( 0x0220, REGION_PROMS, 0 );
		ROM_LOAD( "prom1.e1",     0x0000, 0x0020, CRC(d9b10bf0);SHA1(bc1263331968f4bf37eb70ec4f56a8cb763c29d2) ) /* palette */
		ROM_LOAD( "prom2.8r",     0x0020, 0x0100, CRC(8a9c0edf);SHA1(8aad387e9409cff0eeb42eeb57e9ea88770a8c9a) ) /* lookup table */
		ROM_LOAD( "prom3.k5",     0x0120, 0x0100, CRC(b5db1c2c);SHA1(900aaaac6b674a9c5c7b7804a4b0c3d5cce761aa) ) /* lookup table */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_exctscca = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );    /* 64k for code */
		ROM_LOAD( "1_g10.bin",    0x0000, 0x2000, CRC(aa68df66);SHA1(f10cac5a4c5aad1e1eb8835174dc8d517bb2921a) )
		ROM_LOAD( "2_h10.bin",    0x2000, 0x2000, CRC(2d8f8326);SHA1(8809e7b081fa2a1966cb51ac969fd7b468d35be0) )
		ROM_LOAD( "3_j10.bin",    0x4000, 0x2000, CRC(dce4a04d);SHA1(9c015e4597ec8921bea213d9841fc69c776a4e6d) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );    /* 64k for code */
		ROM_LOAD( "exctsccc.000", 0x0000, 0x2000, CRC(642fc42f);SHA1(cfc849d18e347e3e23fc31c1ce7f2580d5d9b2b0) )
		ROM_LOAD( "exctsccc.009", 0x2000, 0x2000, CRC(d88b3236);SHA1(80f083fb15243e9e68978677caed8aee8e3109a0) )
		ROM_LOAD( "8_d6.bin",     0x4000, 0x2000, CRC(88651ee1);SHA1(2052e1b3f9784439369f464e31f4a2b0d1bb0565) )
		ROM_LOAD( "7_c6.bin",     0x6000, 0x2000, CRC(6d51521e);SHA1(2809bd2e61f40dcd31d43c62520982bdcfb0a865) )
		ROM_LOAD( "1_a6.bin",     0x8000, 0x1000, CRC(20f2207e);SHA1(b1ed2237d0bd50ddbe593fd2fbff9f1d67c1eb11) )
	
		ROM_REGION( 0x06000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "4_a5.bin",     0x0000, 0x2000, CRC(c342229b);SHA1(a989d6c12521c77882a7e17d4d80afe7eae05906) )
		ROM_LOAD( "5_b5.bin",     0x2000, 0x2000, CRC(35f4f8c9);SHA1(cdf5bbfea9abdd338938e5f4499d2d71ce3c6237) )
		ROM_LOAD( "6_c5.bin",     0x4000, 0x2000, CRC(eda40e32);SHA1(6c08fd4f4fb35fd354d02e04548e960c545f6a88) )
	
		ROM_REGION( 0x02000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "2_k5.bin",     0x0000, 0x1000, CRC(7f9cace2);SHA1(bf05a31716f3ca1c2fd1034cd1f39e2d21cdaed3) )
		ROM_LOAD( "3_l5.bin",     0x1000, 0x1000, CRC(db2d9e0d);SHA1(6ec09a47f7aea6bf31eb0ee78f44012f4d92de8a) )
	
		ROM_REGION( 0x0220, REGION_PROMS, 0 );
		ROM_LOAD( "prom1.e1",     0x0000, 0x0020, CRC(d9b10bf0);SHA1(bc1263331968f4bf37eb70ec4f56a8cb763c29d2) ) /* palette */
		ROM_LOAD( "prom2.8r",     0x0020, 0x0100, CRC(8a9c0edf);SHA1(8aad387e9409cff0eeb42eeb57e9ea88770a8c9a) ) /* lookup table */
		ROM_LOAD( "prom3.k5",     0x0120, 0x0100, CRC(b5db1c2c);SHA1(900aaaac6b674a9c5c7b7804a4b0c3d5cce761aa) ) /* lookup table */
	ROM_END(); }}; 
	
	/* Bootleg */
	static RomLoadPtr rom_exctsccb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );/* 64k for code */
		ROM_LOAD( "es-1.e2",      0x0000, 0x2000, CRC(997c6a82);SHA1(60fe27a12eedd22c775b7e65c5ba692cfcf5ac74) )
		ROM_LOAD( "es-2.g2",      0x2000, 0x2000, CRC(5c66e792);SHA1(f7a7f32806965fa926261217cee3159ccd198d49) )
		ROM_LOAD( "es-3.h2",      0x4000, 0x2000, CRC(e0d504c0);SHA1(d9a9f37b3a44a05a3f3389aa9617c419a2cee661) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* sound */
		ROM_LOAD( "es-a.k2",      0x0000, 0x2000, CRC(99e87b78);SHA1(f12006ff3f6f3c706e06288c97a1446141373432) )
		ROM_LOAD( "es-b.l2",      0x2000, 0x2000, CRC(8b3db794);SHA1(dbfed2357c7631bfca6bbd63a23617bc3abf6ca3) )
		ROM_LOAD( "es-c.m2",      0x4000, 0x2000, CRC(7bed2f81);SHA1(cbbb0480519cc04a99e8983228b18c9e49a9985d) )
	
		ROM_REGION( 0x06000, REGION_GFX1, ROMREGION_DISPOSE );
		/* I'm using the ROMs from exctscc2, national flags are wrong (ITA replaces USA) */
		ROM_LOAD( "vr.5a",        0x0000, 0x2000, BAD_DUMP CRC(4ff1783d);SHA1(c45074864c3a4bcbf3a87d164027ae16dca53d9c)  )
		ROM_LOAD( "vr.5b",        0x2000, 0x2000, BAD_DUMP CRC(5605b60b);SHA1(19d5909896ae4a3d7552225c369d30475c56793b)  )
		ROM_LOAD( "vr.5c",        0x4000, 0x2000, BAD_DUMP CRC(1fb84ee6);SHA1(56ceb86c509be783f806403ac21e7c9684760d5f)  )
	
		ROM_REGION( 0x02000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "vr.5k",        0x0000, 0x1000, BAD_DUMP CRC(1d37edfa);SHA1(184fa6dd7b1b3fff4c5fc19b42301ccb7979ac84)  )
		ROM_LOAD( "vr.5l",        0x1000, 0x1000, BAD_DUMP CRC(b97f396c);SHA1(4ffe512acf047230bd593911a615fc0ef66b481d)  )
	
		ROM_REGION( 0x0220, REGION_PROMS, 0 );
		ROM_LOAD( "prom1.e1",     0x0000, 0x0020, CRC(d9b10bf0);SHA1(bc1263331968f4bf37eb70ec4f56a8cb763c29d2) ) /* palette */
		ROM_LOAD( "prom2.8r",     0x0020, 0x0100, CRC(8a9c0edf);SHA1(8aad387e9409cff0eeb42eeb57e9ea88770a8c9a) ) /* lookup table */
		ROM_LOAD( "prom3.k5",     0x0120, 0x0100, CRC(b5db1c2c);SHA1(900aaaac6b674a9c5c7b7804a4b0c3d5cce761aa) ) /* lookup table */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_exctscc2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );/* 64k for code */
		ROM_LOAD( "vr.3j",        0x0000, 0x2000, CRC(c6115362);SHA1(6a258631abd72ef6b8d7968bb4b2bc88e89e597d) )
		ROM_LOAD( "vr.3k",        0x2000, 0x2000, CRC(de36ba00);SHA1(0a0d92e710b8c749f145571bc8a204609456d19d) )
		ROM_LOAD( "vr.3l",        0x4000, 0x2000, CRC(1ddfdf65);SHA1(313d0a7f13fc2de15aa32492c38a59fbafad9f01) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );    /* 64k for code */
		ROM_LOAD( "vr.7d",        0x0000, 0x2000, CRC(2c675a43);SHA1(aa0a8dbcae955e3da92c435202f2a1ed238c377e) )
		ROM_LOAD( "vr.7e",        0x2000, 0x2000, CRC(e571873d);SHA1(2dfff24f5dac86e92612f40cf3642005c7f36ad3) )
		ROM_LOAD( "8_d6.bin",     0x4000, 0x2000, CRC(88651ee1);SHA1(2052e1b3f9784439369f464e31f4a2b0d1bb0565) )	/* vr.7f */
		ROM_LOAD( "7_c6.bin",     0x6000, 0x2000, CRC(6d51521e);SHA1(2809bd2e61f40dcd31d43c62520982bdcfb0a865) )	/* vr.7h */
		ROM_LOAD( "1_a6.bin",     0x8000, 0x1000, CRC(20f2207e);SHA1(b1ed2237d0bd50ddbe593fd2fbff9f1d67c1eb11) )	/* vr.7k */
	
		ROM_REGION( 0x06000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "vr.5a",        0x0000, 0x2000, CRC(4ff1783d);SHA1(c45074864c3a4bcbf3a87d164027ae16dca53d9c) )
		ROM_LOAD( "vr.5b",        0x2000, 0x2000, CRC(5605b60b);SHA1(19d5909896ae4a3d7552225c369d30475c56793b) )
		ROM_LOAD( "vr.5c",        0x4000, 0x2000, CRC(1fb84ee6);SHA1(56ceb86c509be783f806403ac21e7c9684760d5f) )
	
		ROM_REGION( 0x02000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "vr.5k",        0x0000, 0x1000, CRC(1d37edfa);SHA1(184fa6dd7b1b3fff4c5fc19b42301ccb7979ac84) )
		ROM_LOAD( "vr.5l",        0x1000, 0x1000, CRC(b97f396c);SHA1(4ffe512acf047230bd593911a615fc0ef66b481d) )
	
		ROM_REGION( 0x0220, REGION_PROMS, 0 );
		ROM_LOAD( "prom1.e1",     0x0000, 0x0020, CRC(d9b10bf0);SHA1(bc1263331968f4bf37eb70ec4f56a8cb763c29d2) ) /* palette */
		ROM_LOAD( "prom2.8r",     0x0020, 0x0100, CRC(8a9c0edf);SHA1(8aad387e9409cff0eeb42eeb57e9ea88770a8c9a) ) /* lookup table */
		ROM_LOAD( "prom3.k5",     0x0120, 0x0100, CRC(b5db1c2c);SHA1(900aaaac6b674a9c5c7b7804a4b0c3d5cce761aa) ) /* lookup table */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_exctsccr	   = new GameDriver("1983"	,"exctsccr"	,"exctsccr.java"	,rom_exctsccr,null	,machine_driver_exctsccr	,input_ports_exctsccr	,null	,ROT90	,	"Alpha Denshi Co.", "Exciting Soccer" )
	public static GameDriver driver_exctscca	   = new GameDriver("1983"	,"exctscca"	,"exctsccr.java"	,rom_exctscca,driver_exctsccr	,machine_driver_exctsccr	,input_ports_exctsccr	,null	,ROT90	,	"Alpha Denshi Co.", "Exciting Soccer (alternate music)" )
	public static GameDriver driver_exctsccb	   = new GameDriver("1983"	,"exctsccb"	,"exctsccr.java"	,rom_exctsccb,driver_exctsccr	,machine_driver_exctsccb	,input_ports_exctsccr	,null	,ROT90	,	"bootleg", "Exciting Soccer (bootleg)" )
	public static GameDriver driver_exctscc2	   = new GameDriver("1984"	,"exctscc2"	,"exctsccr.java"	,rom_exctscc2,driver_exctsccr	,machine_driver_exctsccr	,input_ports_exctsccr	,null	,ROT90	,	"Alpha Denshi Co.", "Exciting Soccer II", GAME_NOT_WORKING )
}
