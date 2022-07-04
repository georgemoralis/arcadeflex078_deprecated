/***************************************************************************

 Frogger hardware

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class frogger
{
	
	
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new Memory_ReadAddress( 0x8000, 0x87ff, MRA_RAM ),
		new Memory_ReadAddress( 0x8800, 0x8800, watchdog_reset_r ),
		new Memory_ReadAddress( 0xa800, 0xabff, MRA_RAM ),
		new Memory_ReadAddress( 0xb000, 0xb0ff, MRA_RAM ),
		new Memory_ReadAddress( 0xd000, 0xd007, frogger_ppi8255_1_r ),
		new Memory_ReadAddress( 0xe000, 0xe007, frogger_ppi8255_0_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x3fff, MWA_ROM ),
		new Memory_WriteAddress( 0x8000, 0x87ff, MWA_RAM ),
		new Memory_WriteAddress( 0xa800, 0xabff, galaxian_videoram_w, galaxian_videoram ),
		new Memory_WriteAddress( 0xb000, 0xb03f, galaxian_attributesram_w, galaxian_attributesram ),
		new Memory_WriteAddress( 0xb040, 0xb05f, MWA_RAM, galaxian_spriteram, galaxian_spriteram_size ),
		new Memory_WriteAddress( 0xb060, 0xb0ff, MWA_RAM ),
		new Memory_WriteAddress( 0xb808, 0xb808, galaxian_nmi_enable_w ),
		new Memory_WriteAddress( 0xb80c, 0xb80c, galaxian_flip_screen_y_w ),
		new Memory_WriteAddress( 0xb810, 0xb810, galaxian_flip_screen_x_w ),
		new Memory_WriteAddress( 0xb818, 0xb818, galaxian_coin_counter_0_w ),
		new Memory_WriteAddress( 0xb81c, 0xb81c, galaxian_coin_counter_1_w ),
		new Memory_WriteAddress( 0xd000, 0xd007, frogger_ppi8255_1_w ),
		new Memory_WriteAddress( 0xe000, 0xe007, frogger_ppi8255_0_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	MEMORY_READ_START( frogger_sound_readmem )
		{ 0x0000, 0x1fff, MRA_ROM },
		{ 0x4000, 0x43ff, MRA_RAM },
	MEMORY_END
	
	MEMORY_WRITE_START( frogger_sound_writemem )
		{ 0x0000, 0x1fff, MWA_ROM },
		{ 0x4000, 0x43ff, MWA_RAM },
	    { 0x6000, 0x6fff, frogger_filter_w },
	MEMORY_END
	
	
	PORT_READ_START( frogger_sound_readport )
		{ 0x40, 0x40, AY8910_read_port_0_r },
	PORT_END
	
	PORT_WRITE_START( frogger_sound_writeport )
		{ 0x40, 0x40, AY8910_write_port_0_w },
		{ 0x80, 0x80, AY8910_control_port_0_w },
	PORT_END
	
	
	
	static InputPortPtr input_ports_frogger = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );/* 1P shoot2 - unused */
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );/* 1P shoot1 - unused */
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START(); 	/* IN1 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x00, "3" );
		PORT_DIPSETTING(	0x01, "5" );
		PORT_DIPSETTING(	0x02, "7" );
		PORT_BITX( 0,		0x03, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "256", IP_KEY_NONE, IP_JOY_NONE );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );/* 2P shoot2 - unused */
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );/* 2P shoot1 - unused */
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL );
		PORT_DIPNAME( 0x06, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x02, "A 2/1 B 2/1 C 2/1" );
		PORT_DIPSETTING(	0x04, "A 2/1 B 1/3 C 2/1" );
		PORT_DIPSETTING(	0x00, "A 1/1 B 1/1 C 1/1" );
		PORT_DIPSETTING(	0x06, "A 1/1 B 1/6 C 1/1" );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(	0x08, DEF_STR( "Cocktail") );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	
	static AY8910interface frogger_ay8910_interface = new AY8910interface
	(
		1,	/* 1 chip */
		14318000/8,	/* 1.78975 MHz */
		new int[] { MIXERG(80,MIXER_GAIN_2x,MIXER_PAN_CENTER) },
		new ReadHandlerPtr[] { soundlatch_r },
		new ReadHandlerPtr[] { frogger_portB_r },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 }
	);
	
	
	static MACHINE_DRIVER_START( frogger )
	
		/* basic machine hardware */
		MDRV_IMPORT_FROM(galaxian_base)
		MDRV_CPU_MODIFY("main")
		MDRV_CPU_MEMORY(readmem,writemem)
	
		MDRV_CPU_ADD(Z80,14318000/8)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU) /* 1.78975 MHz */
		MDRV_CPU_MEMORY(frogger_sound_readmem,frogger_sound_writemem)
		MDRV_CPU_PORTS(frogger_sound_readport,frogger_sound_writeport)
	
		MDRV_MACHINE_INIT(scramble)
	
		/* video hardware */
		MDRV_PALETTE_LENGTH(32+64+2+1)	/* 32 for characters, 64 for stars, 2 for bullets, 1 for background */	\
	
		MDRV_PALETTE_INIT(frogger)
		MDRV_VIDEO_START(frogger)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, frogger_ay8910_interface)
	MACHINE_DRIVER_END
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_frogger = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );/* 64k for code */
		ROM_LOAD( "frogger.26",   0x0000, 0x1000, CRC(597696d6);SHA1(e7e021776cad00f095a1ebbef407b7c0a8f5d835) )
		ROM_LOAD( "frogger.27",   0x1000, 0x1000, CRC(b6e6fcc3);SHA1(5e8692f2b0c7f4b3642b3ee6670e1c3b20029cdc) )
		ROM_LOAD( "frsm3.7",      0x2000, 0x1000, CRC(aca22ae0);SHA1(5a99060ea2506a3ac7d61ca5876ce5cb3e493565) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* 64k for the audio CPU */
		ROM_LOAD( "frogger.608",  0x0000, 0x0800, CRC(e8ab0256);SHA1(f090afcfacf5f13cdfa0dfda8e3feb868c6ce8bc) )
		ROM_LOAD( "frogger.609",  0x0800, 0x0800, CRC(7380a48f);SHA1(75582a94b696062cbdb66a4c5cf0bc0bb94f81ee) )
		ROM_LOAD( "frogger.610",  0x1000, 0x0800, CRC(31d7eb27);SHA1(2e1d34ae4da385fd7cac94707d25eeddf4604e1a) )
	
		ROM_REGION( 0x1000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "frogger.607",  0x0000, 0x0800, CRC(05f7d883);SHA1(78831fd287da18928651a8adb7e578d291493eff) )
		ROM_LOAD( "frogger.606",  0x0800, 0x0800, CRC(f524ee30);SHA1(dd768967add61467baa08d5929001f157d6cd911) )
	
		ROM_REGION( 0x0020, REGION_PROMS, 0 );
		ROM_LOAD( "pr-91.6l",     0x0000, 0x0020, CRC(413703bf);SHA1(66648b2b28d3dcbda5bdb2605d1977428939dd3c) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_frogseg1 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );/* 64k for code */
		ROM_LOAD( "frogger.26",   0x0000, 0x1000, CRC(597696d6);SHA1(e7e021776cad00f095a1ebbef407b7c0a8f5d835) )
		ROM_LOAD( "frogger.27",   0x1000, 0x1000, CRC(b6e6fcc3);SHA1(5e8692f2b0c7f4b3642b3ee6670e1c3b20029cdc) )
		ROM_LOAD( "frogger.34",   0x2000, 0x1000, CRC(ed866bab);SHA1(24e1bbde44eb5480b7a0570fa0dc1de388cb95ba) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* 64k for the audio CPU */
		ROM_LOAD( "frogger.608",  0x0000, 0x0800, CRC(e8ab0256);SHA1(f090afcfacf5f13cdfa0dfda8e3feb868c6ce8bc) )
		ROM_LOAD( "frogger.609",  0x0800, 0x0800, CRC(7380a48f);SHA1(75582a94b696062cbdb66a4c5cf0bc0bb94f81ee) )
		ROM_LOAD( "frogger.610",  0x1000, 0x0800, CRC(31d7eb27);SHA1(2e1d34ae4da385fd7cac94707d25eeddf4604e1a) )
	
		ROM_REGION( 0x1000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "frogger.607",  0x0000, 0x0800, CRC(05f7d883);SHA1(78831fd287da18928651a8adb7e578d291493eff) )
		ROM_LOAD( "frogger.606",  0x0800, 0x0800, CRC(f524ee30);SHA1(dd768967add61467baa08d5929001f157d6cd911) )
	
		ROM_REGION( 0x0020, REGION_PROMS, 0 );
		ROM_LOAD( "pr-91.6l",     0x0000, 0x0020, CRC(413703bf);SHA1(66648b2b28d3dcbda5bdb2605d1977428939dd3c) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_frogseg2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );/* 64k for code */
		ROM_LOAD( "frogger.ic5",  0x0000, 0x1000, CRC(efab0c79);SHA1(68c99b6cdcb9396bb473739a62ffc009b4bf57d5) )
		ROM_LOAD( "frogger.ic6",  0x1000, 0x1000, CRC(aeca9c13);SHA1(cdf560adbd7f2813e86e378da7781cccf7928a44) )
		ROM_LOAD( "frogger.ic7",  0x2000, 0x1000, CRC(dd251066);SHA1(4612e1fe1ab7182a277140b1a1976cc17e0746a5) )
		ROM_LOAD( "frogger.ic8",  0x3000, 0x1000, CRC(bf293a02);SHA1(be94e9f5caa74c3de6fd95bd20928f4a9c514227) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );/* 64k for the audio CPU */
		ROM_LOAD( "frogger.608",  0x0000, 0x0800, CRC(e8ab0256);SHA1(f090afcfacf5f13cdfa0dfda8e3feb868c6ce8bc) )
		ROM_LOAD( "frogger.609",  0x0800, 0x0800, CRC(7380a48f);SHA1(75582a94b696062cbdb66a4c5cf0bc0bb94f81ee) )
		ROM_LOAD( "frogger.610",  0x1000, 0x0800, CRC(31d7eb27);SHA1(2e1d34ae4da385fd7cac94707d25eeddf4604e1a) )
	
		ROM_REGION( 0x1000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "frogger.607",  0x0000, 0x0800, CRC(05f7d883);SHA1(78831fd287da18928651a8adb7e578d291493eff) )
		ROM_LOAD( "frogger.606",  0x0800, 0x0800, CRC(f524ee30);SHA1(dd768967add61467baa08d5929001f157d6cd911) )
	
		ROM_REGION( 0x0020, REGION_PROMS, 0 );
		ROM_LOAD( "pr-91.6l",     0x0000, 0x0020, CRC(413703bf);SHA1(66648b2b28d3dcbda5bdb2605d1977428939dd3c) )
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_frogger	   = new GameDriver("1981"	,"frogger"	,"frogger.java"	,rom_frogger,null	,machine_driver_frogger	,input_ports_frogger	,init_frogger	,ROT90	,	"Konami", "Frogger" )
	public static GameDriver driver_frogseg1	   = new GameDriver("1981"	,"frogseg1"	,"frogger.java"	,rom_frogseg1,driver_frogger	,machine_driver_frogger	,input_ports_frogger	,init_frogger	,ROT90	,	"[Konami] (Sega license)", "Frogger (Sega set 1)" )
	public static GameDriver driver_frogseg2	   = new GameDriver("1981"	,"frogseg2"	,"frogger.java"	,rom_frogseg2,driver_frogger	,machine_driver_frogger	,input_ports_frogger	,init_frogger	,ROT90	,	"[Konami] (Sega license)", "Frogger (Sega set 2)" )
}
