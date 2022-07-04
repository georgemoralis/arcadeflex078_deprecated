/******************************************************************************

	Game Driver for Video System Mahjong series.

	Ojanko High School (Japan)
	(c)1988 Video System Co.,Ltd.

	Ojanko Yakata (Japan)
	(c)1986 Video System Co.,Ltd.

	Ojanko Yakata 2bankan (Japan)
	(c)1987 Video System Co.,Ltd.

	Chinese Casino [BET] (Japan)
	(c)1987 Video System Co.,Ltd.

	Ojanko Club (Japan)
	(c)1986 Video System Co.,Ltd.

	Driver by Takahiro Nogi <nogi@kt.rim.or.jp> 2000/06/10 -
	Driver by Uki 2001/12/10 -

******************************************************************************/
/******************************************************************************
Memo:

- Sometimes RAM check in testmode fails (reason unknown).

- The method to get matrix key data may be incorrect.
  2player's input is not supported.

******************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class ojankohs
{
	
	
	VIDEO_UPDATE( ojankohs );
	PALETTE_INIT( ojankoy );
	VIDEO_START( ojankohs );
	VIDEO_START( ojankoy );
	VIDEO_UPDATE( ojankoc );
	VIDEO_START( ojankoc );
	void ojankoc_flipscreen(int data);
	
	
	static int ojankohs_portselect;
	static int ojankohs_adpcm_reset;
	static int ojankohs_adpcm_data;
	static int ojankohs_vclk_left;
	
	
	static MACHINE_INIT( ojankohs )
	{
		ojankohs_portselect = 0;
	
		ojankohs_adpcm_reset = 0;
		ojankohs_adpcm_data = 0;
		ojankohs_vclk_left = 0;
	}
	
	public static WriteHandlerPtr ojankohs_rombank_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		unsigned char *ROM = memory_region(REGION_CPU1);
	
		cpu_setbank(1, &ROM[0x10000 + (0x4000 * (data & 0x3f))]);
	} };
	
	public static WriteHandlerPtr ojankoy_rombank_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		unsigned char *ROM = memory_region(REGION_CPU1);
	
		cpu_setbank(1, &ROM[0x10000 + (0x4000 * (data & 0x1f))]);
	
		ojankohs_adpcm_reset = ((data & 0x20) >> 5);
		if (ojankohs_adpcm_reset == 0) ojankohs_vclk_left = 0;
	
		MSM5205_reset_w(0, !ojankohs_adpcm_reset);
	} };
	
	public static WriteHandlerPtr ojankohs_adpcm_reset_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		ojankohs_adpcm_reset = (data & 0x01);
		ojankohs_vclk_left = 0;
	
		MSM5205_reset_w(0, !ojankohs_adpcm_reset);
	} };
	
	public static WriteHandlerPtr ojankohs_msm5205_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		ojankohs_adpcm_data = data;
		ojankohs_vclk_left = 2;
	} };
	
	static void ojankohs_adpcm_int(int irq)
	{
		/* skip if we're reset */
		if (ojankohs_adpcm_reset == 0)
			return;
	
		/* clock the data through */
		if (ojankohs_vclk_left) {
			MSM5205_data_w(0, (ojankohs_adpcm_data >> 4));
			ojankohs_adpcm_data <<= 4;
			ojankohs_vclk_left--;
		}
	
		/* generate an NMI if we're out of data */
		if (ojankohs_vclk_left == 0) 
			cpu_set_nmi_line(0, PULSE_LINE);
	}
	
	public static WriteHandlerPtr ojankoc_ctrl_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		data8_t *BANKROM = memory_region(REGION_USER1);
		UINT32 bank_address = (data & 0x0f) * 0x8000;
	
		cpu_setbank(1, &BANKROM[bank_address]);
	
		ojankohs_adpcm_reset = ((data & 0x10) >> 4);
		MSM5205_reset_w(0, (!(data & 0x10) >> 4));
		ojankoc_flipscreen(data);
	} };
	
	public static WriteHandlerPtr ojankohs_portselect_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		ojankohs_portselect = data;
	} };
	
	public static ReadHandlerPtr ojankohs_keymatrix_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int ret;
	
		switch (ojankohs_portselect) {
			case 0x01:	ret = readinputport(4);	break;
			case 0x02:	ret = readinputport(5); break;
			case 0x04:	ret = readinputport(6); break;
			case 0x08:	ret = readinputport(7); break;
			case 0x10:	ret = readinputport(8); break;
			case 0x20:	ret = 0xff; break;
			case 0x3f:	ret = 0xff;
						ret &= readinputport(4);
						ret &= readinputport(5);
						ret &= readinputport(6);
						ret &= readinputport(7);
						ret &= readinputport(8);
						break;
			default:	ret = 0xff;
						logerror("PC:%04X unknown %02X\n", activecpu_get_pc(), ojankohs_portselect);
						break;
		}
	
		return ret;
	} };
	
	public static ReadHandlerPtr ojankoc_keymatrix_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int i;
		int ret = 0;
	
		for (i = 0; i < 5; i++) {
			if (~ojankohs_portselect & (1 << i))
				ret |= readinputport(i + offset * 5 + 2);
		}
	
		return (ret & 0x3f) | (readinputport(12 + offset) & 0xc0);
	} };
	
	public static ReadHandlerPtr ojankohs_ay8910_0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		// DIPSW 2
		return (((readinputport(2) & 0x01) << 7) | ((readinputport(2) & 0x02) << 5) |
		        ((readinputport(2) & 0x04) << 3) | ((readinputport(2) & 0x08) << 1) |
		        ((readinputport(2) & 0x10) >> 1) | ((readinputport(2) & 0x20) >> 3) |
		        ((readinputport(2) & 0x40) >> 5) | ((readinputport(2) & 0x80) >> 7));
	} };
	
	public static ReadHandlerPtr ojankohs_ay8910_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		// DIPSW 1
		return (((readinputport(3) & 0x01) << 7) | ((readinputport(3) & 0x02) << 5) |
		        ((readinputport(3) & 0x04) << 3) | ((readinputport(3) & 0x08) << 1) |
		        ((readinputport(3) & 0x10) >> 1) | ((readinputport(3) & 0x20) >> 3) |
		        ((readinputport(3) & 0x40) >> 5) | ((readinputport(3) & 0x80) >> 7));
	} };
	
	public static ReadHandlerPtr ojankoy_ay8910_0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return readinputport(2);				// DIPSW 2
	} };
	
	public static ReadHandlerPtr ojankoy_ay8910_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return readinputport(3);				// DIPSW 1
	} };
	
	public static ReadHandlerPtr ccasino_dipsw3_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return (readinputport(9) ^ 0xff);		// DIPSW 3
	} };
	
	public static ReadHandlerPtr ccasino_dipsw4_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return (readinputport(10) ^ 0xff);		// DIPSW 4
	} };
	
	public static WriteHandlerPtr ojankoy_coinctr_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		coin_counter_w( 0, (data & 0x01));
	} };
	
	public static WriteHandlerPtr ccasino_coinctr_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		coin_counter_w(0, (data & 0x02));
	} };
	
	
	public static Memory_ReadAddress readmem_ojankohs[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0x8000, 0x8fff, ojankohs_videoram_r ),
		new Memory_ReadAddress( 0x9000, 0x9fff, ojankohs_colorram_r ),
		new Memory_ReadAddress( 0xa000, 0xb7ff, MRA_RAM ),
		new Memory_ReadAddress( 0xb800, 0xbfff, ojankohs_palette_r ),
		new Memory_ReadAddress( 0xc000, 0xffff, MRA_BANK1 ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress writemem_ojankohs[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0x8000, 0x8fff, ojankohs_videoram_w ),
		new Memory_WriteAddress( 0x9000, 0x9fff, ojankohs_colorram_w ),
		new Memory_WriteAddress( 0xa000, 0xb7ff, MWA_RAM, generic_nvram, generic_nvram_size ),
		new Memory_WriteAddress( 0xb800, 0xbfff, ojankohs_palette_w ),
		new Memory_WriteAddress( 0xc000, 0xffff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_ReadAddress readmem_ojankoy[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0x8000, 0x9fff, ojankohs_videoram_r ),
		new Memory_ReadAddress( 0xa000, 0xafff, ojankohs_colorram_r ),
		new Memory_ReadAddress( 0xb000, 0xbfff, MRA_RAM ),
		new Memory_ReadAddress( 0xc000, 0xffff, MRA_BANK1 ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress writemem_ojankoy[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0x8000, 0x9fff, ojankohs_videoram_w ),
		new Memory_WriteAddress( 0xa000, 0xafff, ojankohs_colorram_w ),
		new Memory_WriteAddress( 0xb000, 0xbfff, MWA_RAM, generic_nvram, generic_nvram_size ),
		new Memory_WriteAddress( 0xc000, 0xffff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_ReadAddress readmem_ojankoc[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x77ff, MRA_ROM ),
		new Memory_ReadAddress( 0x7800, 0x7fff, MRA_RAM ),
		new Memory_ReadAddress( 0x8000, 0xffff, MRA_BANK1 ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress writemem_ojankoc[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x77ff, MWA_ROM ),
		new Memory_WriteAddress( 0x7800, 0x7fff, MWA_RAM, generic_nvram, generic_nvram_size ),
		new Memory_WriteAddress( 0x8000, 0xffff, ojankoc_videoram_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static IO_ReadPort readport_ojankohs[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x00, 0x00, input_port_0_r ),
		new IO_ReadPort( 0x01, 0x01, ojankohs_keymatrix_r ),
		new IO_ReadPort( 0x02, 0x02, input_port_1_r ),
		new IO_ReadPort( 0x06, 0x06, AY8910_read_port_0_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort writeport_ojankohs[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00, 0x00, ojankohs_portselect_w ),
		new IO_WritePort( 0x01, 0x01, ojankohs_rombank_w ),
		new IO_WritePort( 0x02, 0x02, ojankohs_gfxreg_w ),
		new IO_WritePort( 0x03, 0x03, ojankohs_adpcm_reset_w ),
		new IO_WritePort( 0x04, 0x04, ojankohs_flipscreen_w ),
		new IO_WritePort( 0x05, 0x05, ojankohs_msm5205_w ),
		new IO_WritePort( 0x06, 0x06, AY8910_write_port_0_w ),
		new IO_WritePort( 0x07, 0x07, AY8910_control_port_0_w ),
		new IO_WritePort( 0x10, 0x10, IOWP_NOP ),				// unknown
		new IO_WritePort( 0x11, 0x11, IOWP_NOP ),				// unknown
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort writeport_ojankoy[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00, 0x00, ojankohs_portselect_w ),
		new IO_WritePort( 0x01, 0x01, ojankoy_rombank_w ),
		new IO_WritePort( 0x02, 0x02, ojankoy_coinctr_w ),
		new IO_WritePort( 0x04, 0x04, ojankohs_flipscreen_w ),
		new IO_WritePort( 0x05, 0x05, ojankohs_msm5205_w ),
		new IO_WritePort( 0x06, 0x06, AY8910_write_port_0_w ),
		new IO_WritePort( 0x07, 0x07, AY8910_control_port_0_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	public static IO_ReadPort readport_ccasino[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x00, 0x00, input_port_0_r ),
		new IO_ReadPort( 0x01, 0x01, ojankohs_keymatrix_r ),
		new IO_ReadPort( 0x02, 0x02, input_port_1_r ),
		new IO_ReadPort( 0x03, 0x03, ccasino_dipsw3_r ),
		new IO_ReadPort( 0x04, 0x04, ccasino_dipsw4_r ),
		new IO_ReadPort( 0x06, 0x06, AY8910_read_port_0_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort writeport_ccasino[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00, 0x00, ojankohs_portselect_w ),
		new IO_WritePort( 0x01, 0x01, ojankohs_rombank_w ),
		new IO_WritePort( 0x02, 0x02, ccasino_coinctr_w ),
		new IO_WritePort( 0x03, 0x03, ojankohs_adpcm_reset_w ),
		new IO_WritePort( 0x04, 0x04, ojankohs_flipscreen_w ),
		new IO_WritePort( 0x05, 0x05, ojankohs_msm5205_w ),
		new IO_WritePort( 0x06, 0x06, AY8910_write_port_0_w ),
		new IO_WritePort( 0x07, 0x07, AY8910_control_port_0_w ),
		new IO_WritePort( 0x08, 0x0f, ccasino_palette_w ),		// 16bit address access
		new IO_WritePort( 0x10, 0x10, IOWP_NOP ),
		new IO_WritePort( 0x11, 0x11, IOWP_NOP ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	public static IO_ReadPort readport_ojankoc[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0xfc, 0xfd, ojankoc_keymatrix_r ),
		new IO_ReadPort( 0xff, 0xff, AY8910_read_port_0_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort writeport_ojankoc[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00, 0x1f, ojankoc_palette_w ),
		new IO_WritePort( 0xf9, 0xf9, ojankohs_msm5205_w ),
		new IO_WritePort( 0xfb, 0xfb, ojankoc_ctrl_w ),
		new IO_WritePort( 0xfd, 0xfd, ojankohs_portselect_w ),
		new IO_WritePort( 0xfe, 0xfe, AY8910_write_port_0_w ),
		new IO_WritePort( 0xff, 0xff, AY8910_control_port_0_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	
	static InputPortPtr input_ports_ojankohs = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* (0) TEST SW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE3 );	// MEMORY RESET
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE2 );	// ANALYZER
		PORT_BITX( 0x08, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )	// TEST
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* (1) COIN SW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );		// COIN1
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* (2) DIPSW-1 */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x07, "1 (Easy); )
		PORT_DIPSETTING(    0x03, "2" );
		PORT_DIPSETTING(    0x05, "3" );
		PORT_DIPSETTING(    0x01, "4" );
		PORT_DIPSETTING(    0x06, "5" );
		PORT_DIPSETTING(    0x02, "6" );
		PORT_DIPSETTING(    0x04, "7" );
		PORT_DIPSETTING(    0x00, "8 (Hard); )
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x00, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Lives") );
		PORT_DIPSETTING (   0x20, "1" );
		PORT_DIPSETTING (   0x00, "2" );
		PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* (3) DIPSW-2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_DIPNAME( 0x02, 0x02, "Opponent's initial score" );
		PORT_DIPSETTING (   0x02, "2000" );
		PORT_DIPSETTING (   0x00, "3000" );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* (4) PORT 1-0 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 A", KEYCODE_A, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 E", KEYCODE_E, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 I", KEYCODE_I, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 M", KEYCODE_M, IP_JOY_NONE );
		PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Kan", KEYCODE_LCONTROL, IP_JOY_NONE );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* (5) PORT 1-1 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 B", KEYCODE_B, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 F", KEYCODE_F, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 J", KEYCODE_J, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 N", KEYCODE_N, IP_JOY_NONE );
		PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Reach", KEYCODE_LSHIFT, IP_JOY_NONE );
		PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P1 Bet", KEYCODE_2, IP_JOY_NONE );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* (6) PORT 1-2 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 C", KEYCODE_C, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 G", KEYCODE_G, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 K", KEYCODE_K, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Chi", KEYCODE_SPACE, IP_JOY_NONE );
		PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Ron", KEYCODE_Z, IP_JOY_NONE );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* (7) PORT 1-3 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 D", KEYCODE_D, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 H", KEYCODE_H, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 L", KEYCODE_L, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Pon", KEYCODE_LALT, IP_JOY_NONE );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* (8) PORT 1-4 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 Last Chance", KEYCODE_RALT, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 Take Score", KEYCODE_RCONTROL, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 Double Up", KEYCODE_RSHIFT, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Flip", KEYCODE_X, IP_JOY_NONE );
		PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Big", KEYCODE_ENTER, IP_JOY_NONE );
		PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P1 Small", KEYCODE_BACKSPACE, IP_JOY_NONE );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_ojankoy = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* (0) TEST SW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE3 );	// MEMORY RESET
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE2 );	// ANALYZER
		PORT_BITX( 0x08, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )	// TEST
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* (1) COIN SW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );		// COIN1
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* (2) DIPSW-1 */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x07, "1 (Easy); )
		PORT_DIPSETTING(    0x03, "2" );
		PORT_DIPSETTING(    0x05, "3" );
		PORT_DIPSETTING(    0x01, "4" );
		PORT_DIPSETTING(    0x06, "5" );
		PORT_DIPSETTING(    0x02, "6" );
		PORT_DIPSETTING(    0x04, "7" );
		PORT_DIPSETTING(    0x00, "8 (Hard); )
		PORT_DIPNAME( 0x18, 0x18, "Player's initial score" );
		PORT_DIPSETTING(    0x18, "1000" );
		PORT_DIPSETTING(    0x08, "2000" );
		PORT_DIPSETTING(    0x10, "3000" );
		PORT_DIPSETTING(    0x00, "5000" );
		PORT_DIPNAME( 0x60, 0x60, "Noten penalty after ryukyoku" );
		PORT_DIPSETTING(    0x60, "1000" );
		PORT_DIPSETTING(    0x20, "2000" );
		PORT_DIPSETTING(    0x40, "3000" );
		PORT_DIPSETTING(    0x00, "5000" );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* (3) DIPSW-2 */
		PORT_DIPNAME( 0x03, 0x02, "Number of ending chance" );
		PORT_DIPSETTING(    0x03, "0" );
		PORT_DIPSETTING(    0x01, "3" );
		PORT_DIPSETTING(    0x02, "5" );
		PORT_DIPSETTING(    0x00, "10" );
		PORT_DIPNAME( 0x04, 0x04, "Ending chance requires fee" );
		PORT_DIPSETTING (   0x04, DEF_STR( "No") );
		PORT_DIPSETTING (   0x00, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x60, 0x60, "Opponent's initial score" );
		PORT_DIPSETTING (   0x60, "3000 - 8000" );
		PORT_DIPSETTING (   0x20, "5000 - 10000" );
		PORT_DIPSETTING (   0x40, "8000" );
		PORT_DIPSETTING (   0x00, "10000" );
		PORT_DIPNAME( 0x80, 0x00, "Gal select / Continue" );
		PORT_DIPSETTING(    0x80, "Yes / No" );
		PORT_DIPSETTING(    0x00, "No / Yes" );
	
		PORT_START(); 	/* (4) PORT 1-0 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 A", KEYCODE_A, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 E", KEYCODE_E, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 I", KEYCODE_I, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 M", KEYCODE_M, IP_JOY_NONE );
		PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Kan", KEYCODE_LCONTROL, IP_JOY_NONE );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* (5) PORT 1-1 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 B", KEYCODE_B, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 F", KEYCODE_F, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 J", KEYCODE_J, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 N", KEYCODE_N, IP_JOY_NONE );
		PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Reach", KEYCODE_LSHIFT, IP_JOY_NONE );
		PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P1 Bet", KEYCODE_2, IP_JOY_NONE );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* (6) PORT 1-2 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 C", KEYCODE_C, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 G", KEYCODE_G, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 K", KEYCODE_K, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Chi", KEYCODE_SPACE, IP_JOY_NONE );
		PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Ron", KEYCODE_Z, IP_JOY_NONE );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* (7) PORT 1-3 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 D", KEYCODE_D, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 H", KEYCODE_H, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 L", KEYCODE_L, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Pon", KEYCODE_LALT, IP_JOY_NONE );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* (8) PORT 1-4 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 Last Chance", KEYCODE_RALT, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 Take Score", KEYCODE_RCONTROL, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 Double Up", KEYCODE_RSHIFT, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Flip", KEYCODE_X, IP_JOY_NONE );
		PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Big", KEYCODE_ENTER, IP_JOY_NONE );
		PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P1 Small", KEYCODE_BACKSPACE, IP_JOY_NONE );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_ccasino = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* (0) TEST SW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SERVICE3 );	// MEMORY RESET
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE2 );	// ANALYZER
		PORT_BITX( 0x08, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )	// TEST
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* (1) COIN SW */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );		// COIN1
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* (2) DIPSW-1 */
		PORT_DIPNAME( 0x01, 0x01, "DIPSW 1-1" );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "DIPSW 1-2" );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, "DIPSW 1-3" );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, "DIPSW 1-4" );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, "DIPSW 1-5" );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, "DIPSW 1-6" );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "DIPSW 1-7" );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "DIPSW 1-8" );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* (3) DIPSW-2 */
		PORT_DIPNAME( 0x01, 0x01, "DIPSW 2-1" );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "DIPSW 2-2" );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, "DIPSW 2-3" );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, "DIPSW 2-4" );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, "DIPSW 2-5" );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, "DIPSW 2-6" );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "DIPSW 2-7" );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "DIPSW 2-8" );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* (4) PORT 1-0 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 A", KEYCODE_A, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 E", KEYCODE_E, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 I", KEYCODE_I, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 M", KEYCODE_M, IP_JOY_NONE );
		PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Kan", KEYCODE_LCONTROL, IP_JOY_NONE );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* (5) PORT 1-1 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 B", KEYCODE_B, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 F", KEYCODE_F, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 J", KEYCODE_J, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 N", KEYCODE_N, IP_JOY_NONE );
		PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Reach", KEYCODE_LSHIFT, IP_JOY_NONE );
		PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P1 Bet", KEYCODE_2, IP_JOY_NONE );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* (6) PORT 1-2 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 C", KEYCODE_C, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 G", KEYCODE_G, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 K", KEYCODE_K, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Chi", KEYCODE_SPACE, IP_JOY_NONE );
		PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Ron", KEYCODE_Z, IP_JOY_NONE );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* (7) PORT 1-3 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 D", KEYCODE_D, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 H", KEYCODE_H, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 L", KEYCODE_L, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Pon", KEYCODE_LALT, IP_JOY_NONE );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* (8) PORT 1-4 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 Last Chance", KEYCODE_RALT, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 Take Score", KEYCODE_RCONTROL, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 Double Up", KEYCODE_RSHIFT, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Flip", KEYCODE_X, IP_JOY_NONE );
		PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Big", KEYCODE_ENTER, IP_JOY_NONE );
		PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P1 Small", KEYCODE_BACKSPACE, IP_JOY_NONE );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* (9) DIPSW-3 */
		PORT_DIPNAME( 0x01, 0x01, "DIPSW 3-1" );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "DIPSW 3-2" );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, "DIPSW 3-3" );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, "DIPSW 3-4" );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, "DIPSW 3-5" );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, "DIPSW 3-6" );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "DIPSW 3-7" );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "DIPSW 3-8" );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* (10) DIPSW-4 */
		PORT_DIPNAME( 0x01, 0x01, "DIPSW 4-1" );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "DIPSW 4-2" );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, "DIPSW 4-3" );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, "DIPSW 4-4" );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, "DIPSW 4-5" );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, "DIPSW 4-6" );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "DIPSW 4-7" );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "DIPSW 4-8" );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_ojankoc = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* DSW1 (0) */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "1-2" );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, "1-3" );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, "1-4" );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, "1-5" );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, "1-6" );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "1-7" );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
		PORT_START(); 	/* DSW2 (1) */
		PORT_DIPNAME( 0x01, 0x01, "2-1" );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "2-2" );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, "2-3" );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, "2-4" );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, "2-5" );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, "2-6" );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "2-7" );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "2-8" );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* (2) PORT 1-0 */
		PORT_BITX(0x01, IP_ACTIVE_HIGH, 0, "P1 A",   KEYCODE_A, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_HIGH, 0, "P1 E",   KEYCODE_E, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_HIGH, 0, "P1 I",   KEYCODE_I, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_HIGH, 0, "P1 M",   KEYCODE_M, IP_JOY_NONE );
		PORT_BITX(0x10, IP_ACTIVE_HIGH, 0, "P1 Kan", KEYCODE_LCONTROL, IP_JOY_NONE );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0xc0, IP_ACTIVE_HIGH, IPT_UNUSED );
	
		PORT_START(); 	/* (3) PORT 1-1 */
		PORT_BITX(0x01, IP_ACTIVE_HIGH, 0, "P1 B",     KEYCODE_B, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_HIGH, 0, "P1 F",     KEYCODE_F, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_HIGH, 0, "P1 J",     KEYCODE_J, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_HIGH, 0, "P1 N",     KEYCODE_N, IP_JOY_NONE );
		PORT_BITX(0x10, IP_ACTIVE_HIGH, 0, "P1 Reach", KEYCODE_LSHIFT, IP_JOY_NONE );
		PORT_BITX(0x20, IP_ACTIVE_HIGH, 0, "P1 Bet",   KEYCODE_2, IP_JOY_NONE );
		PORT_BIT( 0xc0, IP_ACTIVE_HIGH, IPT_UNUSED );
	
		PORT_START(); 	/* (4) PORT 1-2 */
		PORT_BITX(0x01, IP_ACTIVE_HIGH, 0, "P1 C",   KEYCODE_C, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_HIGH, 0, "P1 G",   KEYCODE_G, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_HIGH, 0, "P1 K",   KEYCODE_K, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_HIGH, 0, "P1 Chi", KEYCODE_SPACE, IP_JOY_NONE );
		PORT_BITX(0x10, IP_ACTIVE_HIGH, 0, "P1 Ron", KEYCODE_Z, IP_JOY_NONE );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0xc0, IP_ACTIVE_HIGH, IPT_UNUSED );
	
		PORT_START(); 	/* (5) PORT 1-3 */
		PORT_BITX(0x01, IP_ACTIVE_HIGH, 0, "P1 D",   KEYCODE_D, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_HIGH, 0, "P1 H",   KEYCODE_H, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_HIGH, 0, "P1 L",   KEYCODE_L, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_HIGH, 0, "P1 Pon", KEYCODE_LALT, IP_JOY_NONE );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0xc0, IP_ACTIVE_HIGH, IPT_UNUSED );
	
		PORT_START(); 	/* (6) PORT 1-4 */
		PORT_BITX(0x01, IP_ACTIVE_HIGH, 0, "P1 Last Chance", KEYCODE_RALT, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_HIGH, 0, "P1 Take Score",  KEYCODE_RCONTROL, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_HIGH, 0, "P1 Double Up",   KEYCODE_RSHIFT, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_HIGH, 0, "P1 Flip",        KEYCODE_X, IP_JOY_NONE );
		PORT_BITX(0x10, IP_ACTIVE_HIGH, 0, "P1 Big",         KEYCODE_ENTER, IP_JOY_NONE );
		PORT_BITX(0x20, IP_ACTIVE_HIGH, 0, "P1 Small",       KEYCODE_BACKSPACE, IP_JOY_NONE );
		PORT_BIT( 0xc0, IP_ACTIVE_HIGH, IPT_UNUSED );
	
		PORT_START(); 	/* (7) PORT 2-0 */
		PORT_BITX(0x01, IP_ACTIVE_HIGH, 0, "P2 A",   IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_HIGH, 0, "P2 E",   IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_HIGH, 0, "P2 I",   IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_HIGH, 0, "P2 M",   IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BITX(0x10, IP_ACTIVE_HIGH, 0, "P2 Kan", IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0xc0, IP_ACTIVE_HIGH, IPT_UNUSED );
	
		PORT_START(); 	/* (8) PORT 2-1 */
		PORT_BITX(0x01, IP_ACTIVE_HIGH, 0, "P2 B",     IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_HIGH, 0, "P2 F",     IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_HIGH, 0, "P2 J",     IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_HIGH, 0, "P2 N",     IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BITX(0x10, IP_ACTIVE_HIGH, 0, "P2 Reach", IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BITX(0x20, IP_ACTIVE_HIGH, 0, "P2 Bet",   IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BIT( 0xc0, IP_ACTIVE_HIGH, IPT_UNUSED );
	
		PORT_START(); 	/* (9) PORT 2-2 */
		PORT_BITX(0x01, IP_ACTIVE_HIGH, 0, "P2 C",   IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_HIGH, 0, "P2 G",   IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_HIGH, 0, "P2 K",   IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_HIGH, 0, "P2 Chi", IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BITX(0x10, IP_ACTIVE_HIGH, 0, "P2 Ron", IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0xc0, IP_ACTIVE_HIGH, IPT_UNUSED );
	
		PORT_START(); 	/* (10) PORT 2-3 */
		PORT_BITX(0x01, IP_ACTIVE_HIGH, 0, "P2 D",   IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_HIGH, 0, "P2 H",   IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_HIGH, 0, "P2 L",   IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_HIGH, 0, "P2 Pon", IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0xc0, IP_ACTIVE_HIGH, IPT_UNUSED );
	
		PORT_START(); 	/* (11) PORT 2-4 */
		PORT_BITX(0x01, IP_ACTIVE_HIGH, 0, "P2 Last Chance", IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_HIGH, 0, "P2 Take Score",  IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_HIGH, 0, "P2 Double Up",   IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BITX(0x08, IP_ACTIVE_HIGH, 0, "P2 Flip",        IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BITX(0x10, IP_ACTIVE_HIGH, 0, "P2 Big",         IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BITX(0x20, IP_ACTIVE_HIGH, 0, "P2 Small",       IP_KEY_DEFAULT, IP_JOY_NONE );
		PORT_BIT( 0xc0, IP_ACTIVE_HIGH, IPT_UNUSED );
	
		PORT_START(); 	/* IN1 (12) */ 
		PORT_BIT( 0x3f, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN2 (13) */ 
		PORT_BIT( 0x3f, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
	INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout ojankohs_bglayout = new GfxLayout
	(
		8, 4,
		RGN_FRAC(1, 1),
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 4, 0, 12, 8, 20, 16, 28, 24 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		16*8
	);
	
	static GfxDecodeInfo ojankohs_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, ojankohs_bglayout,   0, 64 ),
		new GfxDecodeInfo( -1 ) 						/* end of array */
	};
	
	
	static AY8910interface ojankohs_ay8910_interface = new AY8910interface
	(
		1,							/* 1 chip */
		12000000/6,					/* 2 MHz ? */
		new int[] { 15 },						/* volume */
		new ReadHandlerPtr[] { ojankohs_ay8910_0_r },	/* read port #0 */
		new ReadHandlerPtr[] { ojankohs_ay8910_1_r },	/* read port #1 */
		new WriteHandlerPtr[] { 0 },						/* write port #0 */
		new WriteHandlerPtr[] { 0 }						/* write port #1 */
	);
	
	static AY8910interface ojankoy_ay8910_interface = new AY8910interface
	(
		1,							/* 1 chip */
		12000000/8,					/* 1.5 MHz ? */
		new int[] { 15 },						/* volume */
		new ReadHandlerPtr[] { ojankoy_ay8910_0_r },		/* read port #0 */
		new ReadHandlerPtr[] { ojankoy_ay8910_1_r },		/* read port #1 */
		new WriteHandlerPtr[] { 0 },						/* write port #0 */
		new WriteHandlerPtr[] { 0 }						/* write port #1 */
	);
	
	static AY8910interface ojankoc_ay8910_interface = new AY8910interface
	(
		1,							/* 1 chip */
		8000000/4,					/* 2.000 MHz */
		new int[] { 15 },						/* volume */
		new ReadHandlerPtr[] { input_port_0_r },			/* read port #0 */
		new ReadHandlerPtr[] { input_port_1_r },			/* read port #1 */
		new WriteHandlerPtr[] { 0 },						/* write port #0 */
		new WriteHandlerPtr[] { 0 } 						/* write port #1 */
	);
	
	static struct MSM5205interface ojankohs_msm5205_interface =
	{
		1,							/* 1 chip */
		384000,						/* 384 KHz */
		{ ojankohs_adpcm_int },		/* IRQ handler */
		{ MSM5205_S48_4B },			/* 8 KHz */
		{ 50 }						/* volume */
	};
	
	static struct MSM5205interface ojankoc_msm5205_interface =
	{
		1,							/* 1 chip */
		8000000/22,					/* 364 KHz */
		{ ojankohs_adpcm_int },		/* IRQ handler */
		{ MSM5205_S48_4B },			/* 7.6 KHz */
		{ 50 }						/* volume */
	};
	
	
	static MACHINE_DRIVER_START( ojankohs )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80,12000000/2)		/* 6.00 MHz ? */
		MDRV_CPU_MEMORY(readmem_ojankohs,writemem_ojankohs)
		MDRV_CPU_PORTS(readport_ojankohs,writeport_ojankohs)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		MDRV_MACHINE_INIT(ojankohs)
		MDRV_NVRAM_HANDLER(generic_0fill)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(512, 512)
		MDRV_VISIBLE_AREA(0, 288-1, 0, 224-1)
		MDRV_GFXDECODE(ojankohs_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(ojankohs)
		MDRV_VIDEO_UPDATE(ojankohs)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, ojankohs_ay8910_interface)
		MDRV_SOUND_ADD(MSM5205, ojankohs_msm5205_interface)
	MACHINE_DRIVER_END
	
	static MACHINE_DRIVER_START( ojankoy )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80,12000000/2)		/* 6.00 MHz ? */
		MDRV_CPU_MEMORY(readmem_ojankoy,writemem_ojankoy)
		MDRV_CPU_PORTS(readport_ojankohs,writeport_ojankoy)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		MDRV_MACHINE_INIT(ojankohs)
		MDRV_NVRAM_HANDLER(generic_0fill)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(512, 512)
		MDRV_VISIBLE_AREA(0, 288-1, 0, 224-1)
		MDRV_GFXDECODE(ojankohs_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
		MDRV_PALETTE_INIT(ojankoy)
	
		MDRV_VIDEO_START(ojankoy)
		MDRV_VIDEO_UPDATE(ojankohs)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, ojankoy_ay8910_interface)
		MDRV_SOUND_ADD(MSM5205, ojankohs_msm5205_interface)
	MACHINE_DRIVER_END
	
	static MACHINE_DRIVER_START( ccasino )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80,12000000/2)		/* 6.00 MHz ? */
		MDRV_CPU_MEMORY(readmem_ojankoy,writemem_ojankoy)
		MDRV_CPU_PORTS(readport_ccasino,writeport_ccasino)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		MDRV_MACHINE_INIT(ojankohs)
		MDRV_NVRAM_HANDLER(generic_0fill)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(512, 512)
		MDRV_VISIBLE_AREA(0, 288-1, 0, 224-1)
		MDRV_GFXDECODE(ojankohs_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(ojankoy)
		MDRV_VIDEO_UPDATE(ojankohs)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, ojankoy_ay8910_interface)
		MDRV_SOUND_ADD(MSM5205, ojankohs_msm5205_interface)
	MACHINE_DRIVER_END
	
	static MACHINE_DRIVER_START( ojankoc )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80,8000000/2)			/* 4.00 MHz */
		MDRV_CPU_MEMORY(readmem_ojankoc,writemem_ojankoc)
		MDRV_CPU_PORTS(readport_ojankoc,writeport_ojankoc)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		MDRV_MACHINE_INIT(ojankohs)
		MDRV_NVRAM_HANDLER(generic_0fill)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(32*8, 32*8)
		MDRV_VISIBLE_AREA(0, 256-1, 8, 248-1)
		MDRV_PALETTE_LENGTH(16)
	
		MDRV_VIDEO_START(ojankoc)
		MDRV_VIDEO_UPDATE(ojankoc)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, ojankoc_ay8910_interface)
		MDRV_SOUND_ADD(MSM5205, ojankoc_msm5205_interface)
	MACHINE_DRIVER_END
	
	
	static RomLoadPtr rom_ojankohs = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x98000, REGION_CPU1, 0 );
		ROM_LOAD( "3.3c", 0x00000, 0x08000, CRC(f652db23);SHA1(7fcb4227804301f0404af4b007eb4accb0787c98) )
		ROM_LOAD( "5b",   0x10000, 0x80000, CRC(bd4fd0b6);SHA1(79e0937fdd34ec03b4b0a503efc1fa7c8f29e7cf) )
		ROM_LOAD( "6.6c", 0x90000, 0x08000, CRC(30772679);SHA1(8bc415da465faa70ec468a23b3528493849e83ee) )
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "13b",  0x00000, 0x80000, CRC(bda30bfa);SHA1(c412e573c40816735f7e2d0600dd0d78ebce91dc) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ojankoy = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x70000, REGION_CPU1, 0 );
		ROM_LOAD( "p-ic17.bin", 0x00000, 0x08000, CRC(9f149c30);SHA1(e3a8407844c0bb2d2fda83b01a187c87b3b7767a) )
		ROM_LOAD( "ic30.bin",   0x10000, 0x20000, CRC(37be3f7c);SHA1(9ef19ef1e118d75ae719623b90188d68e6faa8f2) )
		ROM_LOAD( "ic29.bin",   0x30000, 0x20000, CRC(dab7c4d8);SHA1(812f56a15545e98eb67ac46ca1c006201d432b5d) )
		ROM_LOAD( "a-ic34.bin", 0x50000, 0x08000, CRC(93c20ea3);SHA1(f9b74813132fd9cef7803568daad5ea8e8e02a04) )
		ROM_LOAD( "b-ic33.bin", 0x58000, 0x08000, CRC(ef86d711);SHA1(922f4c29e8b5f7cf034e1ed623793aec57e799b6) )
		ROM_LOAD( "c-ic32.bin", 0x60000, 0x08000, CRC(d20de9b0);SHA1(bfec453a5e16bb3e1ffa454d6dad44e113a54968) )
		ROM_LOAD( "d-ic31.bin", 0x68000, 0x08000, CRC(b78e6913);SHA1(a0ebe0b29025beabe5609a5d1adecfd2565da623) )
	
		ROM_REGION( 0x70000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "ic55.bin",   0x00000, 0x20000, CRC(586fb385);SHA1(cdf18f52ba8d25c740fc85a68505f102fe6ba208) )
		ROM_LOAD( "0-ic53.bin", 0x40000, 0x08000, CRC(db38c288);SHA1(8b98091eae9c22ade123a6f58c108f8e653d99c8) )
		ROM_LOAD( "1-ic52.bin", 0x48000, 0x08000, CRC(a8b4a10b);SHA1(fa44c52efd42a99e2d34c4785a09947523a8385a) )
		ROM_LOAD( "2-ic51.bin", 0x50000, 0x08000, CRC(5e2bb752);SHA1(39054cbb8f9cd99f815e2bce83bb82ec4a93b550) )
		ROM_LOAD( "3-ic50.bin", 0x58000, 0x08000, CRC(10c73a44);SHA1(e4ecfd0e1067eaec9e8f78f1cedac78599814556) )
		ROM_LOAD( "4-ic49.bin", 0x60000, 0x08000, CRC(31807d24);SHA1(9a2458386c1e970a47dd7bad85bbc2e28113759a) )
		ROM_LOAD( "5-ic48.bin", 0x68000, 0x08000, CRC(e116721d);SHA1(85e5b70fcdfc6ca92ce5aee8a17f1476b4f077d5) )
	
		ROM_REGION( 0x0800, REGION_PROMS, 0 );
		ROM_LOAD( "0-ic65.bin", 0x0000, 0x0400, CRC(28fde5ef);SHA1(81c645b5601ff33c6a5091e7debe99a8d6b6bd70) )
		ROM_LOAD( "1-ic64.bin", 0x0400, 0x0400, CRC(36c305c5);SHA1(43be6346e421f03a55bddb58a1570905321cf914) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ojanko2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x70000, REGION_CPU1, 0 );
		ROM_LOAD( "p-ic17.bin", 0x00000, 0x08000, CRC(4b33bd54);SHA1(be235492cf3824ea740f401201ad821bb71c6d89) )
		ROM_LOAD( "ic30.bin",   0x10000, 0x20000, CRC(37be3f7c);SHA1(9ef19ef1e118d75ae719623b90188d68e6faa8f2) )
		ROM_LOAD( "ic29.bin",   0x30000, 0x20000, CRC(dab7c4d8);SHA1(812f56a15545e98eb67ac46ca1c006201d432b5d) )
		ROM_LOAD( "a-ic34.bin", 0x50000, 0x08000, CRC(93c20ea3);SHA1(f9b74813132fd9cef7803568daad5ea8e8e02a04) )
		ROM_LOAD( "b-ic33.bin", 0x58000, 0x08000, CRC(ef86d711);SHA1(922f4c29e8b5f7cf034e1ed623793aec57e799b6) )
		ROM_LOAD( "c-ic32.bin", 0x60000, 0x08000, CRC(5453b9de);SHA1(d9758c56cd65d65d0711368054fc0dfbb4b213ae) )
		ROM_LOAD( "d-ic31.bin", 0x68000, 0x08000, CRC(44cd5348);SHA1(a73a676fbca4678aef8066ad72ea22c6c4ca4b32) )
	
		ROM_REGION( 0x70000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "ic55.bin",   0x00000, 0x20000, CRC(b058fb3d);SHA1(32b04405f218c1f9ca58f01dbadda3536df3d0b5) )
		ROM_LOAD( "0-ic53.bin", 0x40000, 0x08000, CRC(db38c288);SHA1(8b98091eae9c22ade123a6f58c108f8e653d99c8) )
		ROM_LOAD( "1-ic52.bin", 0x48000, 0x08000, CRC(49f2ca73);SHA1(387613fd886f3a4a569146aaec59ad15f13a8ea5) )
		ROM_LOAD( "2-ic51.bin", 0x50000, 0x08000, CRC(199a9bfb);SHA1(fa39aa5d97cf5b54327388d8f1668f24f2f420e4) )
		ROM_LOAD( "3-ic50.bin", 0x58000, 0x08000, CRC(f175510e);SHA1(9925d23b8cbd8bcadff1b37027899b63439ee734) )
		ROM_LOAD( "4-ic49.bin", 0x60000, 0x08000, CRC(3a6a9685);SHA1(756ed845f0b2f53b344a660961bd7e15df2a50f1) )
	
		ROM_REGION( 0x0800, REGION_PROMS, 0 );
		ROM_LOAD( "0-ic65.bin", 0x0000, 0x0400, CRC(86e19b01);SHA1(1facd72183d127aec1c5ad8f17f3450512698d94) )
		ROM_LOAD( "1-ic64.bin", 0x0400, 0x0400, CRC(e2f7093d);SHA1(428903e4fc9f05cf8dab01a5d4145a5b44faa311) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ccasino = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x68000, REGION_CPU1, 0 );
		ROM_LOAD( "p5.bin", 0x00000, 0x08000, CRC(d6cf3387);SHA1(507a40a0ace0742a8fd205c641d27d22d80da948) )
		ROM_LOAD( "l5.bin", 0x10000, 0x20000, CRC(49c9ecfb);SHA1(96005904cef9b9e4434034c9d68978ff9c431457) )
		ROM_LOAD( "f5.bin", 0x50000, 0x08000, CRC(fa71c91c);SHA1(f693f6bb0a9433fbf3f272e43472f6a728ae35ef) )
		ROM_LOAD( "g5.bin", 0x58000, 0x08000, CRC(8cfd60aa);SHA1(203789c58a9cbfbf37ad2a3dfcd86eefe406b2c7) )
		ROM_LOAD( "h5.bin", 0x60000, 0x08000, CRC(d20dfcf9);SHA1(83ca36f2e02bbada5b03734b5d92c5c860292db2) )
	
		ROM_REGION( 0x60000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "r1.bin", 0x00000, 0x20000, CRC(407f77ca);SHA1(a65e5403fa84185d67d994acee6f32051991d546) )
		ROM_LOAD( "s1.bin", 0x20000, 0x20000, CRC(8572d156);SHA1(22f73bfb1419c3d467b4cd4ffaa6f1598f4ee4fa) )
		ROM_LOAD( "e1.bin", 0x40000, 0x08000, CRC(d78c3428);SHA1(b033a7aa3029b7a9ff836c5c737c07aaad5d7456) )
		ROM_LOAD( "f1.bin", 0x48000, 0x08000, CRC(799cc0e7);SHA1(51ca991a76945235375f1c7c4db2abfa1d7ebd15) )
		ROM_LOAD( "g1.bin", 0x50000, 0x08000, CRC(3ac8ae04);SHA1(7ac3095bb2ee6e86970464746fe4644eabc769ec) )
		ROM_LOAD( "h1.bin", 0x58000, 0x08000, CRC(f0af2d38);SHA1(14f29404a10633f5c4b574fc1f34139f9fb8a8bf) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ojankoc = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );  /* CPU */
		ROM_LOAD( "c11.1p", 0x0000, 0x8000, CRC(cb3e900c);SHA1(95f0354f147e339a97368b5cc67200151cdfa0e9) )
	
		ROM_REGION( 0x80000, REGION_USER1, 0 ); /* BANK */
		ROM_LOAD( "1.1a", 0x00000, 0x8000, CRC(d40b17eb);SHA1(1e8c16e1562c112ca5150b3187a2d4aa22c1adf0) )
		ROM_LOAD( "2.1b", 0x08000, 0x8000, CRC(d181172a);SHA1(65d6710464a1f505df705c553558bbf22704359d) )
		ROM_LOAD( "3.1c", 0x10000, 0x8000, CRC(2e86d5bc);SHA1(0226eb81b31e43325f24b40ab51bce1729bf678c) )
		ROM_LOAD( "4.1e", 0x18000, 0x8000, CRC(00a780cb);SHA1(f0b4f6f0c58e9d069e0f6794243925679f220f35) )
		ROM_LOAD( "5.1f", 0x20000, 0x8000, CRC(f9885076);SHA1(ebf4c0769eab6545fd227eb9f4036af2472bcac3) )
		ROM_LOAD( "6.1h", 0x28000, 0x8000, CRC(42575d0c);SHA1(1f9c187b0c05179798cbdb28eb212202ffdc9fde) )
		ROM_LOAD( "7.1k", 0x30000, 0x8000, CRC(4d8d8928);SHA1(a5ccf4a1d84ef3a4966db01d66371de83e270701) )
		ROM_LOAD( "8.1l", 0x38000, 0x8000, CRC(534573b7);SHA1(ec53cad7d652c88508edd29c2412834920fe8ef6) )
		ROM_LOAD( "9.1m", 0x48000, 0x8000, CRC(2bf88eda);SHA1(55de96d057a0f35d9e74455444751f217aa4741e) )
		ROM_LOAD( "0.1n", 0x50000, 0x8000, CRC(5665016e);SHA1(0f7f0a8e55e93bcb3060c91d9704905a6e827250) )
	ROM_END(); }}; 
	
	
	public static GameDriver driver_ojankoc	   = new GameDriver("1986"	,"ojankoc"	,"ojankohs.java"	,rom_ojankoc,null	,machine_driver_ojankoc	,input_ports_ojankoc	,null	,ROT0	,	"V-System Co.", "Ojanko Club (Japan)" )
	public static GameDriver driver_ojankoy	   = new GameDriver("1986"	,"ojankoy"	,"ojankohs.java"	,rom_ojankoy,null	,machine_driver_ojankoy	,input_ports_ojankoy	,null	,ROT0	,	"V-System Co.", "Ojanko Yakata (Japan)" )
	public static GameDriver driver_ojanko2	   = new GameDriver("1987"	,"ojanko2"	,"ojankohs.java"	,rom_ojanko2,null	,machine_driver_ojankoy	,input_ports_ojankoy	,null	,ROT0	,	"V-System Co.", "Ojanko Yakata 2bankan (Japan)" )
	public static GameDriver driver_ccasino	   = new GameDriver("1987"	,"ccasino"	,"ojankohs.java"	,rom_ccasino,null	,machine_driver_ccasino	,input_ports_ccasino	,null	,ROT0	,	"V-System Co.", "Chinese Casino [BET] (Japan)" )
	public static GameDriver driver_ojankohs	   = new GameDriver("1988"	,"ojankohs"	,"ojankohs.java"	,rom_ojankohs,null	,machine_driver_ojankohs	,input_ports_ojankohs	,null	,ROT0	,	"V-System Co.", "Ojanko High School (Japan)" )
}
