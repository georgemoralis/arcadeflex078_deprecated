/***************************************************************************

  Speed Rumbler

  Driver provided by Paul Leaman

  M6809 for game, Z80 and YM-2203 for sound.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class srumbler
{
	
	extern unsigned char *srumbler_backgroundram,*srumbler_foregroundram;
	
	
	VIDEO_START( srumbler );
	VIDEO_UPDATE( srumbler );
	VIDEO_EOF( srumbler );
	
	
	
	public static WriteHandlerPtr srumbler_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/*
		  banking is controlled by two PROMs. 0000-4fff is mapped to the same
		  address (RAM and I/O) for all banks, so we don't handle it here.
		  e000-ffff is all mapped to the same ROMs, however we do handle it
		  here anyway.
		  Note that 5000-8fff can be either ROM or RAM, so we should handle
		  that as well to be 100% accurate.
		 */
		int i;
		unsigned char *ROM = memory_region(REGION_USER1);
		unsigned char *prom1 = memory_region(REGION_PROMS) + (data & 0xf0);
		unsigned char *prom2 = memory_region(REGION_PROMS) + 0x100 + ((data & 0x0f) << 4);
	
		for (i = 0x05;i < 0x10;i++)
		{
			int bank = ((prom1[i] & 0x03) << 4) | (prom2[i] & 0x0f);
			/* bit 2 of prom1 selects ROM or RAM - not supported */
	
			cpu_setbank(i+1,&ROM[bank*0x1000]);
		}
	} };
	
	static MACHINE_INIT( srumbler )
	{
		/* initialize banked ROM pointers */
		srumbler_bankswitch_w(0,0);
	}
	
	static INTERRUPT_GEN( srumbler_interrupt )
	{
		if (cpu_getiloops()==0)
		{
			cpu_set_irq_line(0,0,HOLD_LINE);
		}
		else
		{
			cpu_set_irq_line(0,M6809_FIRQ_LINE,HOLD_LINE);
		}
	}
	
	
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x3fff, MRA_RAM ),   /* RAM (of 1 sort or another) */
		new Memory_ReadAddress( 0x4008, 0x4008, input_port_0_r ),
		new Memory_ReadAddress( 0x4009, 0x4009, input_port_1_r ),
		new Memory_ReadAddress( 0x400a, 0x400a, input_port_2_r ),
		new Memory_ReadAddress( 0x400b, 0x400b, input_port_3_r ),
		new Memory_ReadAddress( 0x400c, 0x400c, input_port_4_r ),
		new Memory_ReadAddress( 0x5000, 0x5fff, MRA_BANK6 ),	/* Banked ROM */
		new Memory_ReadAddress( 0x6000, 0x6fff, MRA_BANK7 ),	/* Banked ROM */
		new Memory_ReadAddress( 0x7000, 0x7fff, MRA_BANK8 ),	/* Banked ROM */
		new Memory_ReadAddress( 0x8000, 0x8fff, MRA_BANK9 ),	/* Banked ROM */
		new Memory_ReadAddress( 0x9000, 0x9fff, MRA_BANK10 ),	/* Banked ROM */
		new Memory_ReadAddress( 0xa000, 0xafff, MRA_BANK11 ),	/* Banked ROM */
		new Memory_ReadAddress( 0xb000, 0xbfff, MRA_BANK12 ),	/* Banked ROM */
		new Memory_ReadAddress( 0xc000, 0xcfff, MRA_BANK13 ),	/* Banked ROM */
		new Memory_ReadAddress( 0xd000, 0xdfff, MRA_BANK14 ),	/* Banked ROM */
		new Memory_ReadAddress( 0xe000, 0xefff, MRA_BANK15 ),	/* Banked ROM */
		new Memory_ReadAddress( 0xf000, 0xffff, MRA_BANK16 ),	/* Banked ROM */
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	/*
	The "scroll test" routine on the test screen appears to overflow and write
	over the control registers (0x4000-0x4080) when it clears the screen.
	
	This doesn't affect anything since it happens to write the correct value
	to the page register.
	
	Ignore the warnings about writing to unmapped memory.
	*/
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x1dff, MWA_RAM ),
		new Memory_WriteAddress( 0x1e00, 0x1fff, MWA_RAM, spriteram, spriteram_size ),
		new Memory_WriteAddress( 0x2000, 0x3fff, srumbler_background_w, srumbler_backgroundram ),
		new Memory_WriteAddress( 0x4008, 0x4008, srumbler_bankswitch_w ),
		new Memory_WriteAddress( 0x4009, 0x4009, srumbler_4009_w ),
		new Memory_WriteAddress( 0x400a, 0x400d, srumbler_scroll_w ),
		new Memory_WriteAddress( 0x400e, 0x400e, soundlatch_w ),
		new Memory_WriteAddress( 0x5000, 0x5fff, srumbler_foreground_w, srumbler_foregroundram ),
		new Memory_WriteAddress( 0x6000, 0x6fff, MWA_RAM ), /* Video RAM 2 ??? (not used) */
		new Memory_WriteAddress( 0x7000, 0x73ff, paletteram_RRRRGGGGBBBBxxxx_swap_w, paletteram ),
		new Memory_WriteAddress( 0x7400, 0xffff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_ReadAddress sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0xe000, 0xe000, soundlatch_r ),
		new Memory_ReadAddress( 0xc000, 0xc7ff, MRA_RAM ),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0xc000, 0xc7ff, MWA_RAM ),
		new Memory_WriteAddress( 0x8000, 0x8000, YM2203_control_port_0_w ),
		new Memory_WriteAddress( 0x8001, 0x8001, YM2203_write_port_0_w ),
		new Memory_WriteAddress( 0xa000, 0xa000, YM2203_control_port_1_w ),
		new Memory_WriteAddress( 0xa001, 0xa001, YM2203_write_port_1_w ),
		new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	static InputPortPtr input_ports_srumbler = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN2 );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* DSW0 */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "1C_6C") );
		PORT_SERVICE( 0x40, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "3" );
		PORT_DIPSETTING(    0x02, "4" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPSETTING(    0x00, "7" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x18, "20k 70k and every 70k" );
		PORT_DIPSETTING(    0x10, "30k 80k and every 80k" );
		PORT_DIPSETTING(    0x08, "20k 80k" );
		PORT_DIPSETTING(    0x00, "30k 80k" );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x40, "Easy" );
		PORT_DIPSETTING(    0x60, "Normal" );
		PORT_DIPSETTING(    0x20, "Hard" );
		PORT_DIPSETTING(    0x00, "Very Hard" );
		PORT_DIPNAME( 0x80, 0x80, "Allow Continue" );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Yes") );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,1),
		2,
		new int[] { 4, 0 },
		new int[] { 0, 1, 2, 3, 8+0,8+1, 8+2, 8+3 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		16*8
	);
	
	static GfxLayout tilelayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,2),
		4,
		new int[] { RGN_FRAC(1,2)+4, RGN_FRAC(1,2)+0, 4, 0 },
		new int[] { 0, 1, 2, 3, 8+0, 8+1, 8+2, 8+3,
				32*8+0, 32*8+1, 32*8+2, 32*8+3, 33*8+0, 33*8+1, 33*8+2, 33*8+3 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
				8*16, 9*16, 10*16, 11*16, 12*16, 13*16, 14*16, 15*16 },
		64*8
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,4),
		4,
		new int[] { RGN_FRAC(3,4), RGN_FRAC(2,4), RGN_FRAC(1,4), RGN_FRAC(0,4) },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,
				2*64+0, 2*64+1, 2*64+2, 2*64+3, 2*64+4, 2*64+5, 2*64+6, 2*64+7, 2*64+8 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		32*8
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,   448, 16 ), /* colors 448 - 511 */
		new GfxDecodeInfo( REGION_GFX2, 0, tilelayout,   128,  8 ), /* colors 128 - 255 */
		new GfxDecodeInfo( REGION_GFX3, 0, spritelayout, 256,  8 ), /* colors 256 - 383 */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	static struct YM2203interface ym2203_interface =
	{
		2,                      /* 2 chips */
		4000000,        /* 4.0 MHz (? hand tuned to match the real board) */
		{ YM2203_VOL(60,20), YM2203_VOL(60,20) },
		{ 0 },
		{ 0 },
		{ 0 },
		{ 0 }
	};
	
	
	
	static MACHINE_DRIVER_START( srumbler )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M6809, 1500000)        /* 1.5 MHz (?) */
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_VBLANK_INT(srumbler_interrupt,2)
	
		MDRV_CPU_ADD(Z80, 3000000)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)        /* 3 MHz ??? */
		MDRV_CPU_MEMORY(sound_readmem,sound_writemem)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,4)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		MDRV_MACHINE_INIT(srumbler)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER | VIDEO_BUFFERS_SPRITERAM)
		MDRV_SCREEN_SIZE(64*8, 32*8)
		MDRV_VISIBLE_AREA(10*8, (64-10)*8-1, 1*8, 31*8-1 )
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(512)
	
		MDRV_VIDEO_START(srumbler)
		MDRV_VIDEO_EOF(srumbler)
		MDRV_VIDEO_UPDATE(srumbler)
	
		/* sound hardware */
		MDRV_SOUND_ADD(YM2203, ym2203_interface)
	MACHINE_DRIVER_END
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_srumbler = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 ); /* 64k for code */
		/* empty, will be filled later */
	
		ROM_REGION( 0x40000, REGION_USER1, 0 );/* Paged ROMs */
		ROM_LOAD( "14e_sr04.bin", 0x00000, 0x08000, CRC(a68ce89c),SHA1(cb5dd8c47c24f9d8ac9a6135c0b7942d16002d25) )  /* RC4 */
		ROM_LOAD( "13e_sr03.bin", 0x08000, 0x08000, CRC(87bda812),SHA1(f46dcce21d78c8525a2578b73e05b7cd8a2d8745) )  /* RC3 */
		ROM_LOAD( "12e_sr02.bin", 0x10000, 0x08000, CRC(d8609cca),SHA1(893f1f1ac0aef5d31e75228252c14c4b522bff16) )  /* RC2 */
		ROM_LOAD( "11e_sr01.bin", 0x18000, 0x08000, CRC(27ec4776),SHA1(09a53fd6472888664c21f49ab78b2c5d77d2caa1) )  /* RC1 */
		ROM_LOAD( "14f_sr09.bin", 0x20000, 0x08000, CRC(2146101d),SHA1(cacd7a13d67f43a0fc624d1c5e29d9816bd6b1c7) )  /* RC9 */
		ROM_LOAD( "13f_sr08.bin", 0x28000, 0x08000, CRC(838369a6),SHA1(6fdcbe2db488d4d99453b5537cf05ed18112368e) )  /* RC8 */
		ROM_LOAD( "12f_sr07.bin", 0x30000, 0x08000, CRC(de785076),SHA1(bdb104c6c875f5362c0d1ba9a8c5dd450c9c014b) )  /* RC7 */
		ROM_LOAD( "11f_sr06.bin", 0x38000, 0x08000, CRC(a70f4fd4),SHA1(21be3865b9f7fa265f265a565bab896357d7464f) )  /* RC6 */
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* 64k for the audio CPU */
		ROM_LOAD( "2f_sr05.bin",  0x0000, 0x8000, CRC(0177cebe),SHA1(0fa94d2057f509a6fe1de210bf513efc82f1ffe7) )
	
		ROM_REGION( 0x04000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "6g_sr10.bin",  0x00000, 0x4000, CRC(adabe271),SHA1(256d6823dcda404375825103272213e1442c3320) ) /* characters */
	
		ROM_REGION( 0x40000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "11a_sr11.bin", 0x00000, 0x8000, CRC(5fa042ba),SHA1(9e03eaf22286330826501619a7b74181dc42a5fa) ) /* tiles */
		ROM_LOAD( "13a_sr12.bin", 0x08000, 0x8000, CRC(a2db64af),SHA1(35ab93397ee8172813e69edd085b36a5b98ba082) )
		ROM_LOAD( "14a_sr13.bin", 0x10000, 0x8000, CRC(f1df5499),SHA1(b1c47b35c00bc05825353474ad2b33d9669b879e) )
		ROM_LOAD( "15a_sr14.bin", 0x18000, 0x8000, CRC(b22b31b3),SHA1(7aa1a042bccf6a1117c983bb36e88ace7712e867) )
		ROM_LOAD( "11c_sr15.bin", 0x20000, 0x8000, CRC(ca3a3af3),SHA1(bcb43fc66be852acb2f93513d6b7b089b1bdd9fc) )
		ROM_LOAD( "13c_sr16.bin", 0x28000, 0x8000, CRC(c49a4a11),SHA1(4e1432e6d6a7ffc73e695c1db245ea54beee0507) )
		ROM_LOAD( "14c_sr17.bin", 0x30000, 0x8000, CRC(aa80aaab),SHA1(37a8e57e4d8ed8372bc1d7c94cf5a087a01d79ad) )
		ROM_LOAD( "15c_sr18.bin", 0x38000, 0x8000, CRC(ce67868e),SHA1(867d6bc65119fdb7a9788f7d92e6be0326756776) )
	
		ROM_REGION( 0x40000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD( "15e_sr20.bin", 0x00000, 0x8000, CRC(3924c861),SHA1(e31e0ea50823a910f87eefc969de53f1ad738629) ) /* sprites */
		ROM_LOAD( "14e_sr19.bin", 0x08000, 0x8000, CRC(ff8f9129),SHA1(8402236e297c3b03984a22b727198cc54e0c8117) )
		ROM_LOAD( "15f_sr22.bin", 0x10000, 0x8000, CRC(ab64161c),SHA1(4d8b01ba4c85a732df38db7663bd765a49c671de) )
		ROM_LOAD( "14f_sr21.bin", 0x18000, 0x8000, CRC(fd64bcd1),SHA1(4bb6c0e0027387284de1dc1320887de3231252e9) )
		ROM_LOAD( "15h_sr24.bin", 0x20000, 0x8000, CRC(c972af3e),SHA1(3aaf5fdd07f675bd29a068035f252c0136e7881e) )
		ROM_LOAD( "14h_sr23.bin", 0x28000, 0x8000, CRC(8c9abf57),SHA1(044d5c9904e89b67bd92396a7215b73d96d3965a) )
		ROM_LOAD( "15j_sr26.bin", 0x30000, 0x8000, CRC(d4f1732f),SHA1(d16b6456c73395964c9868078e378e0d5cc48ae7) )
		ROM_LOAD( "14j_sr25.bin", 0x38000, 0x8000, CRC(d2a4ea4f),SHA1(365e534bf56e08b1e727ea7bfdfb537fa274448b) )
	
		ROM_REGION( 0x0300, REGION_PROMS, 0 );
		ROM_LOAD( "63s141.12a",   0x0000, 0x0100, CRC(8421786f),SHA1(7ffe9f3cd081842d9ee38bd67421cb8836e3f7ed) )	/* ROM banking */
		ROM_LOAD( "63s141.13a",   0x0100, 0x0100, CRC(6048583f),SHA1(a0b0f560e7f52978a1bf59417da13cc852617eff) )	/* ROM banking */
		ROM_LOAD( "63s141.8j",    0x0200, 0x0100, CRC(1a89a7ff),SHA1(437160ad5d61a257b7deaf5f5e8b3d4cf56a9663) )	/* priority (not used) */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_srumblr2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 ); /* 64k for code */
		/* empty, will be filled later */
	
		ROM_REGION( 0x40000, REGION_USER1, 0 );/* Paged ROMs */
		ROM_LOAD( "14e_sr04.bin", 0x00000, 0x08000, CRC(a68ce89c),SHA1(cb5dd8c47c24f9d8ac9a6135c0b7942d16002d25) )  /* RC4 */
		ROM_LOAD( "rc03.13e",     0x08000, 0x08000, CRC(e82f78d4),SHA1(39cb5d9c18e7635d48aa29221ae99e6a500e2841) )  /* RC3 (different) */
		ROM_LOAD( "rc02.12e",     0x10000, 0x08000, CRC(009a62d8),SHA1(72b52b34186304d70214f56acdb0f3af5bed9503) )  /* RC2 (different) */
		ROM_LOAD( "rc01.11e",     0x18000, 0x08000, CRC(2ac48d1d),SHA1(9e41cddb8f8f96e55f915ae5c244c123cc4f8c9a) )  /* RC1 (different) */
		ROM_LOAD( "rc09.14f",     0x20000, 0x08000, CRC(64f23e72),SHA1(2de892f8753df0ac85389328342089bd5cc57f38) )  /* RC9 (different) */
		ROM_LOAD( "rc08.13f",     0x28000, 0x08000, CRC(74c71007),SHA1(9b7f159dc1e3add85a3aaeb697aa800a37d09f52) )  /* RC8 (different) */
		ROM_LOAD( "12f_sr07.bin", 0x30000, 0x08000, CRC(de785076),SHA1(bdb104c6c875f5362c0d1ba9a8c5dd450c9c014b) )  /* RC7 */
		ROM_LOAD( "11f_sr06.bin", 0x38000, 0x08000, CRC(a70f4fd4),SHA1(21be3865b9f7fa265f265a565bab896357d7464f) )  /* RC6 */
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* 64k for the audio CPU */
		ROM_LOAD( "rc05.2f",      0x0000, 0x8000, CRC(ea04fa07),SHA1(e29bfc3ed9e6606206ee41c90aaaeddffa26c1b4) )  /* AUDIO (different) */
	
		ROM_REGION( 0x04000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "6g_sr10.bin",  0x00000, 0x4000, CRC(adabe271),SHA1(256d6823dcda404375825103272213e1442c3320) ) /* characters */
	
		ROM_REGION( 0x40000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "11a_sr11.bin", 0x00000, 0x8000, CRC(5fa042ba),SHA1(9e03eaf22286330826501619a7b74181dc42a5fa) ) /* tiles */
		ROM_LOAD( "13a_sr12.bin", 0x08000, 0x8000, CRC(a2db64af),SHA1(35ab93397ee8172813e69edd085b36a5b98ba082) )
		ROM_LOAD( "14a_sr13.bin", 0x10000, 0x8000, CRC(f1df5499),SHA1(b1c47b35c00bc05825353474ad2b33d9669b879e) )
		ROM_LOAD( "15a_sr14.bin", 0x18000, 0x8000, CRC(b22b31b3),SHA1(7aa1a042bccf6a1117c983bb36e88ace7712e867) )
		ROM_LOAD( "11c_sr15.bin", 0x20000, 0x8000, CRC(ca3a3af3),SHA1(bcb43fc66be852acb2f93513d6b7b089b1bdd9fc) )
		ROM_LOAD( "13c_sr16.bin", 0x28000, 0x8000, CRC(c49a4a11),SHA1(4e1432e6d6a7ffc73e695c1db245ea54beee0507) )
		ROM_LOAD( "14c_sr17.bin", 0x30000, 0x8000, CRC(aa80aaab),SHA1(37a8e57e4d8ed8372bc1d7c94cf5a087a01d79ad) )
		ROM_LOAD( "15c_sr18.bin", 0x38000, 0x8000, CRC(ce67868e),SHA1(867d6bc65119fdb7a9788f7d92e6be0326756776) )
	
		ROM_REGION( 0x40000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD( "15e_sr20.bin", 0x00000, 0x8000, CRC(3924c861),SHA1(e31e0ea50823a910f87eefc969de53f1ad738629) ) /* sprites */
		ROM_LOAD( "14e_sr19.bin", 0x08000, 0x8000, CRC(ff8f9129),SHA1(8402236e297c3b03984a22b727198cc54e0c8117) )
		ROM_LOAD( "15f_sr22.bin", 0x10000, 0x8000, CRC(ab64161c),SHA1(4d8b01ba4c85a732df38db7663bd765a49c671de) )
		ROM_LOAD( "14f_sr21.bin", 0x18000, 0x8000, CRC(fd64bcd1),SHA1(4bb6c0e0027387284de1dc1320887de3231252e9) )
		ROM_LOAD( "15h_sr24.bin", 0x20000, 0x8000, CRC(c972af3e),SHA1(3aaf5fdd07f675bd29a068035f252c0136e7881e) )
		ROM_LOAD( "14h_sr23.bin", 0x28000, 0x8000, CRC(8c9abf57),SHA1(044d5c9904e89b67bd92396a7215b73d96d3965a) )
		ROM_LOAD( "15j_sr26.bin", 0x30000, 0x8000, CRC(d4f1732f),SHA1(d16b6456c73395964c9868078e378e0d5cc48ae7) )
		ROM_LOAD( "14j_sr25.bin", 0x38000, 0x8000, CRC(d2a4ea4f),SHA1(365e534bf56e08b1e727ea7bfdfb537fa274448b) )
	
		ROM_REGION( 0x0300, REGION_PROMS, 0 );
		ROM_LOAD( "63s141.12a",   0x0000, 0x0100, CRC(8421786f),SHA1(7ffe9f3cd081842d9ee38bd67421cb8836e3f7ed) )	/* ROM banking */
		ROM_LOAD( "63s141.13a",   0x0100, 0x0100, CRC(6048583f),SHA1(a0b0f560e7f52978a1bf59417da13cc852617eff) )	/* ROM banking */
		ROM_LOAD( "63s141.8j",    0x0200, 0x0100, CRC(1a89a7ff),SHA1(437160ad5d61a257b7deaf5f5e8b3d4cf56a9663) )	/* priority (not used) */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_rushcrsh = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 ); /* 64k for code */
		/* empty, will be filled later */
	
		ROM_REGION( 0x40000, REGION_USER1, 0 );/* Paged ROMs */
		ROM_LOAD( "14e_sr04.bin", 0x00000, 0x08000, CRC(a68ce89c),SHA1(cb5dd8c47c24f9d8ac9a6135c0b7942d16002d25) )  /* RC4 */
		ROM_LOAD( "rc03.bin",     0x08000, 0x08000, CRC(a49c9be0),SHA1(9aa385063a289e71fef4c2846c8c960a8adafcc0) )  /* RC3 (different) */
		ROM_LOAD( "rc02.12e",     0x10000, 0x08000, CRC(009a62d8),SHA1(72b52b34186304d70214f56acdb0f3af5bed9503) )  /* RC2 (different) */
		ROM_LOAD( "rc01.11e",     0x18000, 0x08000, CRC(2ac48d1d),SHA1(9e41cddb8f8f96e55f915ae5c244c123cc4f8c9a) )  /* RC1 (different) */
		ROM_LOAD( "rc09.14f",     0x20000, 0x08000, CRC(64f23e72),SHA1(2de892f8753df0ac85389328342089bd5cc57f38) )  /* RC9 (different) */
		ROM_LOAD( "rc08.bin",     0x28000, 0x08000, CRC(2c25874b),SHA1(7862f0af14c508f598a2f05330a61b77b86d624e) )  /* RC8 (different) */
		ROM_LOAD( "12f_sr07.bin", 0x30000, 0x08000, CRC(de785076),SHA1(bdb104c6c875f5362c0d1ba9a8c5dd450c9c014b) )  /* RC7 */
		ROM_LOAD( "11f_sr06.bin", 0x38000, 0x08000, CRC(a70f4fd4),SHA1(21be3865b9f7fa265f265a565bab896357d7464f) )  /* RC6 */
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* 64k for the audio CPU */
		ROM_LOAD( "rc05.2f",      0x0000, 0x8000, CRC(ea04fa07),SHA1(e29bfc3ed9e6606206ee41c90aaaeddffa26c1b4) )  /* AUDIO (different) */
	
		ROM_REGION( 0x04000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "rc10.bin",     0x00000, 0x4000, CRC(0a3c0b0d),SHA1(63f4daaea852c077f0ddd04d4bb4cd6333a8de7c) ) /* characters */
	
		ROM_REGION( 0x40000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "11a_sr11.bin", 0x00000, 0x8000, CRC(5fa042ba),SHA1(9e03eaf22286330826501619a7b74181dc42a5fa) ) /* tiles */
		ROM_LOAD( "13a_sr12.bin", 0x08000, 0x8000, CRC(a2db64af),SHA1(35ab93397ee8172813e69edd085b36a5b98ba082) )
		ROM_LOAD( "14a_sr13.bin", 0x10000, 0x8000, CRC(f1df5499),SHA1(b1c47b35c00bc05825353474ad2b33d9669b879e) )
		ROM_LOAD( "15a_sr14.bin", 0x18000, 0x8000, CRC(b22b31b3),SHA1(7aa1a042bccf6a1117c983bb36e88ace7712e867) )
		ROM_LOAD( "11c_sr15.bin", 0x20000, 0x8000, CRC(ca3a3af3),SHA1(bcb43fc66be852acb2f93513d6b7b089b1bdd9fc) )
		ROM_LOAD( "13c_sr16.bin", 0x28000, 0x8000, CRC(c49a4a11),SHA1(4e1432e6d6a7ffc73e695c1db245ea54beee0507) )
		ROM_LOAD( "14c_sr17.bin", 0x30000, 0x8000, CRC(aa80aaab),SHA1(37a8e57e4d8ed8372bc1d7c94cf5a087a01d79ad) )
		ROM_LOAD( "15c_sr18.bin", 0x38000, 0x8000, CRC(ce67868e),SHA1(867d6bc65119fdb7a9788f7d92e6be0326756776) )
	
		ROM_REGION( 0x40000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD( "15e_sr20.bin", 0x00000, 0x8000, CRC(3924c861),SHA1(e31e0ea50823a910f87eefc969de53f1ad738629) ) /* sprites */
		ROM_LOAD( "14e_sr19.bin", 0x08000, 0x8000, CRC(ff8f9129),SHA1(8402236e297c3b03984a22b727198cc54e0c8117) )
		ROM_LOAD( "15f_sr22.bin", 0x10000, 0x8000, CRC(ab64161c),SHA1(4d8b01ba4c85a732df38db7663bd765a49c671de) )
		ROM_LOAD( "14f_sr21.bin", 0x18000, 0x8000, CRC(fd64bcd1),SHA1(4bb6c0e0027387284de1dc1320887de3231252e9) )
		ROM_LOAD( "15h_sr24.bin", 0x20000, 0x8000, CRC(c972af3e),SHA1(3aaf5fdd07f675bd29a068035f252c0136e7881e) )
		ROM_LOAD( "14h_sr23.bin", 0x28000, 0x8000, CRC(8c9abf57),SHA1(044d5c9904e89b67bd92396a7215b73d96d3965a) )
		ROM_LOAD( "15j_sr26.bin", 0x30000, 0x8000, CRC(d4f1732f),SHA1(d16b6456c73395964c9868078e378e0d5cc48ae7) )
		ROM_LOAD( "14j_sr25.bin", 0x38000, 0x8000, CRC(d2a4ea4f),SHA1(365e534bf56e08b1e727ea7bfdfb537fa274448b) )
	
		ROM_REGION( 0x0300, REGION_PROMS, 0 );
		ROM_LOAD( "63s141.12a",   0x0000, 0x0100, CRC(8421786f),SHA1(7ffe9f3cd081842d9ee38bd67421cb8836e3f7ed) )	/* ROM banking */
		ROM_LOAD( "63s141.13a",   0x0100, 0x0100, CRC(6048583f),SHA1(a0b0f560e7f52978a1bf59417da13cc852617eff) )	/* ROM banking */
		ROM_LOAD( "63s141.8j",    0x0200, 0x0100, CRC(1a89a7ff),SHA1(437160ad5d61a257b7deaf5f5e8b3d4cf56a9663) )	/* priority (not used) */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_srumbler	   = new GameDriver("1986"	,"srumbler"	,"srumbler.java"	,rom_srumbler,null	,machine_driver_srumbler	,input_ports_srumbler	,null	,ROT270	,	"Capcom", "The Speed Rumbler (set 1)" )
	public static GameDriver driver_srumblr2	   = new GameDriver("1986"	,"srumblr2"	,"srumbler.java"	,rom_srumblr2,driver_srumbler	,machine_driver_srumbler	,input_ports_srumbler	,null	,ROT270	,	"Capcom", "The Speed Rumbler (set 2)" )
	public static GameDriver driver_rushcrsh	   = new GameDriver("1986"	,"rushcrsh"	,"srumbler.java"	,rom_rushcrsh,driver_srumbler	,machine_driver_srumbler	,input_ports_srumbler	,null	,ROT270	,	"Capcom", "Rush & Crash (Japan)" )
}
