/* Rotary Fighter

driver by Barry Rodewald
 based on Initial work by David Haywood

 todo:

 sound
 verify game speed if possible (related to # of interrupts)

*/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class rotaryf
{
	
	
	static PALETTE_INIT( rotaryf )
	{
		palette_set_color(0,0x00,0x00,0x00); /* black */
		palette_set_color(1,0xff,0xff,0xff); /* white */
	}
	
	INTERRUPT_GEN( rotaryf_interrupt )
	{
		if(cpu_getvblank())
			cpu_set_irq_line(0,I8085_RST55_LINE,HOLD_LINE);
		else
			cpu_set_irq_line(0,I8085_RST75_LINE,HOLD_LINE);
	
	}
	
	public static Memory_ReadAddress rotaryf_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x03ff, MRA_ROM ),
		new Memory_ReadAddress( 0x4000, 0x57ff, MRA_ROM ),
	//	new Memory_ReadAddress( 0x6ffb, 0x6ffb, random_r ), ??
	//	new Memory_ReadAddress( 0x6ffd, 0x6ffd, random_r ), ??
	//	new Memory_ReadAddress( 0x6fff, 0x6fff, random_r ), ??
		new Memory_ReadAddress( 0x7000, 0x73ff, MRA_RAM ),
		new Memory_ReadAddress( 0x8000, 0x9fff, MRA_RAM ),
		new Memory_ReadAddress( 0xa000, 0xa1ff, MRA_RAM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress rotaryf_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x03ff, MWA_ROM ),
		new Memory_WriteAddress( 0x4000, 0x57ff, MWA_ROM ),
		new Memory_WriteAddress( 0x7000, 0x73ff, MWA_RAM ), // clears to 1ff ?
		new Memory_WriteAddress( 0x8000, 0x9fff, c8080bw_videoram_w, videoram, videoram_size ),
		new Memory_WriteAddress( 0xa000, 0xa1ff, MWA_RAM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static IO_ReadPort rotaryf_readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
	//	new IO_ReadPort( 0x00, 0x00, input_port_0_r ),
		new IO_ReadPort( 0x21, 0x21, input_port_1_r ),
		new IO_ReadPort( 0x29, 0x29, input_port_2_r ),
		new IO_ReadPort( 0x26, 0x26, input_port_3_r ),
	//	new IO_ReadPort( 0x28, 0x28, c8080bw_shift_data_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort rotaryf_writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
	//	new IO_WritePort( 0x21, 0x21, c8080bw_shift_amount_w ),
	//	new IO_WritePort( 0x28, 0x28, c8080bw_shift_data_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	static InputPortPtr input_ports_rotaryf = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START();       /* IN1 */
		PORT_BIT_IMPULSE( 0x20, IP_ACTIVE_HIGH, IPT_COIN1, 1 );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START();       /* DSW0 */
		PORT_DIPNAME( 0x81, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x81, "3" );
		PORT_DIPSETTING(    0x01, "4" );
		PORT_DIPSETTING(    0x80, "5" );
		PORT_DIPSETTING(    0x00, "6" );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING( 0x00, DEF_STR( "Off") );
		PORT_DIPSETTING( 0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING( 0x04, "1000" );
		PORT_DIPSETTING( 0x00, "1500" );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING( 0x00, DEF_STR( "Off") );
		PORT_DIPSETTING( 0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING( 0x00, DEF_STR( "Off") );
		PORT_DIPSETTING( 0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING( 0x00, DEF_STR( "Off") );
		PORT_DIPSETTING( 0x40, DEF_STR( "On") );
	//	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );
	//	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER2 );
	//	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 );
	
		PORT_START(); 		/* Dummy port for cocktail mode */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
	INPUT_PORTS_END(); }}; 
	
	
	static MACHINE_DRIVER_START( rotaryf )
	
		/* basic machine hardware */
		MDRV_CPU_ADD_TAG("main",8085A,2000000) /* 8080? */ /* 2 MHz? */
		MDRV_CPU_MEMORY(rotaryf_readmem,rotaryf_writemem)
		MDRV_CPU_PORTS(rotaryf_readport,rotaryf_writeport)
		MDRV_CPU_VBLANK_INT(rotaryf_interrupt,5)
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(32*8, 32*8)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 0*8, 32*8-1)
		MDRV_PALETTE_LENGTH(2)
		MDRV_PALETTE_INIT(rotaryf)
		MDRV_VIDEO_START(generic_bitmapped)
		MDRV_VIDEO_UPDATE(8080bw)
	
		/* sound hardware */
		MDRV_SOUND_ADD(SAMPLES, invaders_samples_interface)
		MDRV_SOUND_ADD(SN76477, invaders_sn76477_interface)
	
	MACHINE_DRIVER_END
	
	static RomLoadPtr rom_rotaryf = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );    /* 64k for code */
		ROM_LOAD( "krf-1.bin",            0x0000, 0x0400, CRC(f7b2d3e6);SHA1(be7afc1a14be60cb895fc4180167353c7156fc4c) )
		ROM_RELOAD (                      0x4000, 0x0400             );
		ROM_LOAD( "krf-2.bin",			  0x4400, 0x0400, CRC(be9f047a);SHA1(e5dd2b5b4fda7f178e7f1137592ba49fbc9cc82e) )
		ROM_LOAD( "krf-3.bin",            0x4800, 0x0400, CRC(c7629eb6);SHA1(03aae964783ce4b1de77737e83fd2094483fbda4) )
		ROM_LOAD( "krf-4.bin",            0x4c00, 0x0400, CRC(b4703093);SHA1(9239d6da818049bc98a631c3bf5b962b5df5b2ea) )
		ROM_LOAD( "krf-5.bin",            0x5000, 0x0400, CRC(ae233f07);SHA1(a7bbd2ee4477ee041d170e2fc4e94c99c3b564fc) )
		ROM_LOAD( "krf-6.bin",            0x5400, 0x0400, CRC(e28b3713);SHA1(428f73891125f80c722357f1029b18fa9416bcfd) )
	ROM_END(); }}; 
	
	public static GameDriver driver_rotaryf	   = new GameDriver("19??"	,"rotaryf"	,"rotaryf.java"	,rom_rotaryf,null	,machine_driver_rotaryf	,input_ports_rotaryf	,init_8080bw	,ROT270	,	"<unknown>", "Rotary Fighter", GAME_NO_SOUND )
}
