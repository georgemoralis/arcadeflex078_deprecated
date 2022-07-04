/***************************************************************************

Atari Sprint 8 driver

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class sprint8
{
	
	extern VIDEO_EOF( sprint8 );
	extern VIDEO_START( sprint8 );
	extern VIDEO_UPDATE( sprint8 );
	
	extern 
	extern UINT8* sprint8_video_ram;
	extern UINT8* sprint8_pos_h_ram;
	extern UINT8* sprint8_pos_v_ram;
	extern UINT8* sprint8_pos_d_ram;
	
	static int steer_dir[8];
	static int steer_flag[8];
	
	static int collision_reset;
	static int collision_index;
	
	
	void sprint8_collision_callback(int n)
	{
		if (collision_reset == 0)
		{
			cpu_set_irq_line(0, 0, ASSERT_LINE);
	
			collision_index = n;
		}
	}
	
	
	static void input_callback(int dummy)
	{
		static UINT8 dial[8];
	
		int i;
	
		for (i = 0; i < 8; i++)
		{
			UINT8 val = readinputport(8 + i) >> 4;
	
			signed char delta = (val - dial[i]) & 15;
	
			if (delta & 8)
			{
				delta |= 0xf0; /* extend sign to 8 bits */
			}
	
			steer_flag[i] = (delta != 0);
	
			if (delta > 0)
			{
				steer_dir[i] = 0;
			}
			if (delta < 0)
			{
				steer_dir[i] = 1;
			}
	
			dial[i] = val;
		}
	}
	
	
	static void fill_palette(int team)
	{
		int i;
	
		for (i = 0; i < 16; i += 8)
		{
			if (team)
			{
				palette_set_color(i + 0, 0xff, 0x00, 0x00); /* red     */
				palette_set_color(i + 1, 0x00, 0x00, 0xff); /* blue    */
				palette_set_color(i + 2, 0xff, 0x00, 0x00); /* red     */
				palette_set_color(i + 3, 0x00, 0x00, 0xff); /* blue    */
				palette_set_color(i + 4, 0xff, 0x00, 0x00); /* red     */
				palette_set_color(i + 5, 0x00, 0x00, 0xff); /* blue    */
				palette_set_color(i + 6, 0xff, 0x00, 0x00); /* red     */
				palette_set_color(i + 7, 0x00, 0x00, 0xff); /* blue    */
			}
			else
			{
				palette_set_color(i + 0, 0xff, 0x00, 0x00); /* red     */
				palette_set_color(i + 1, 0x00, 0x00, 0xff); /* blue    */
				palette_set_color(i + 2, 0xff, 0xff, 0x00); /* yellow  */
				palette_set_color(i + 3, 0x00, 0xff, 0x00); /* green   */
				palette_set_color(i + 4, 0xff, 0x00, 0xff); /* magenta */
				palette_set_color(i + 5, 0xe0, 0xc0, 0x70); /* puce    */
				palette_set_color(i + 6, 0x00, 0xff, 0xff); /* cyan    */
				palette_set_color(i + 7, 0xff, 0xaa, 0xaa); /* pink    */
			}
		}
	
		palette_set_color(16, 0x00, 0x00, 0x00);
		palette_set_color(17, 0xff, 0xff, 0xff);
	}
	
	
	static PALETTE_INIT( sprint8 )
	{
		int i;
	
		fill_palette(0);
	
		for (i = 0; i < 16; i++)
		{
			colortable[2 * i + 0] = 16;
			colortable[2 * i + 1] = i;
		}
	
		colortable[32] = 16;
		colortable[33] = 16;
		colortable[34] = 16;
		colortable[35] = 17;
	}
	
	
	static MACHINE_INIT( sprint8 )
	{
		collision_reset = 0;
		collision_index = 0;
	
		timer_pulse(TIME_IN_HZ(60), 0, input_callback);
	}
	
	
	public static ReadHandlerPtr sprint8_collision_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return collision_index;
	} };
	
	
	public static ReadHandlerPtr sprint8_input_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT8 val = readinputport(offset);
	
		if (steer_dir[offset])
		{
			val |= 0x02;
		}
		if (steer_flag[offset])
		{
			val |= 0x04;
		}
	
		return val;
	} };
	
	
	public static WriteHandlerPtr sprint8_lockout_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		coin_lockout_w(offset, !(data & 1));
	} };
	
	
	public static WriteHandlerPtr sprint8_team_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		fill_palette(!(data & 1));
	} };
	
	
	public static WriteHandlerPtr sprint8_int_reset_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		collision_reset = !(data & 1);
	
		if (collision_reset)
		{
			cpu_set_irq_line(0, 0, CLEAR_LINE);
		}
	} };
	
	
	/* names of sound effects taken from Tank 8, might differ for Sprint 8 */
	
	public static WriteHandlerPtr sprint8_crash_w = new WriteHandlerPtr() {public void handler(int offset, int data) {} };
	public static WriteHandlerPtr sprint8_explosion_w = new WriteHandlerPtr() {public void handler(int offset, int data) {} };
	public static WriteHandlerPtr sprint8_bugle_w = new WriteHandlerPtr() {public void handler(int offset, int data) {} };
	public static WriteHandlerPtr sprint8_bug_w = new WriteHandlerPtr() {public void handler(int offset, int data) {} };
	public static WriteHandlerPtr sprint8_attract_w = new WriteHandlerPtr() {public void handler(int offset, int data) {} };
	public static WriteHandlerPtr sprint8_motor_w = new WriteHandlerPtr() {public void handler(int offset, int data) {} };
	
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x00ff, MRA_RAM ),
		new Memory_ReadAddress( 0x1c00, 0x1c00, sprint8_collision_r ),
		new Memory_ReadAddress( 0x1c01, 0x1c08, sprint8_input_r ),
		new Memory_ReadAddress( 0x1c09, 0x1c09, input_port_16_r ),
		new Memory_ReadAddress( 0x1c0a, 0x1c0a, input_port_17_r ),
		new Memory_ReadAddress( 0x1c0f, 0x1c0f, input_port_18_r ),
		new Memory_ReadAddress( 0x2000, 0x3fff, MRA_ROM ),
		new Memory_ReadAddress( 0xf800, 0xffff, MRA_ROM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x00ff, MWA_RAM ),
		new Memory_WriteAddress( 0x1800, 0x1bff, sprint8_video_ram_w, sprint8_video_ram ),
		new Memory_WriteAddress( 0x1c00, 0x1c0f, MWA_RAM, sprint8_pos_h_ram ),
		new Memory_WriteAddress( 0x1c10, 0x1c1f, MWA_RAM, sprint8_pos_v_ram ),
		new Memory_WriteAddress( 0x1c20, 0x1c2f, MWA_RAM, sprint8_pos_d_ram ),
		new Memory_WriteAddress( 0x1c30, 0x1c37, sprint8_lockout_w ),
		new Memory_WriteAddress( 0x1d00, 0x1d00, sprint8_int_reset_w ),
		new Memory_WriteAddress( 0x1d01, 0x1d01, sprint8_crash_w ),
		new Memory_WriteAddress( 0x1d02, 0x1d02, sprint8_explosion_w ),
		new Memory_WriteAddress( 0x1d03, 0x1d03, sprint8_bugle_w ),
		new Memory_WriteAddress( 0x1d04, 0x1d04, sprint8_bug_w ),
		new Memory_WriteAddress( 0x1d05, 0x1d05, sprint8_team_w ),
		new Memory_WriteAddress( 0x1d06, 0x1d06, sprint8_attract_w ),
		new Memory_WriteAddress( 0x1e00, 0x1e07, sprint8_motor_w ),
		new Memory_WriteAddress( 0x1f00, 0x1f00, MWA_NOP ), /* probably a watchdog, disabled in service mode */
		new Memory_WriteAddress( 0x2000, 0x3fff, MWA_ROM ),
		new Memory_WriteAddress( 0xf800, 0xffff, MWA_ROM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	static InputPortPtr input_ports_sprint8 = new InputPortPtr(){ public void handler() { 
	
		PORT_START(); 
		PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_COIN1 );
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER DIR P1 */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER FLAG P1 */
		PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED );
	
		PORT_START(); 
		PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_COIN2 );
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER DIR P2 */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER FLAG P2 */
		PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED );
	
		PORT_START(); 
		PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_COIN3 );
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER DIR P3 */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER FLAG P3 */
		PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER3 );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER3 );
		PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED );
	
		PORT_START(); 
		PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_COIN4 );
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER DIR P4 */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER FLAG P4 */
		PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER4 );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER4 );
		PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED );
	
		PORT_START(); 
		PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_COIN5 );
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER DIR P5 */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER FLAG P5 */
		PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER5 );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER5 );
		PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
		PORT_START(); 
		PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_COIN6 );
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER DIR P6 */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER FLAG P6 */
		PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER6 );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER6 );
		PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED );
	
		PORT_START(); 
		PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_COIN7 );
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER DIR P7 */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER FLAG P7 */
		PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER7 );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER7 );
		PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED );
	
		PORT_START(); 
		PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_COIN8 );
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER DIR P8 */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER FLAG P8 */
		PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER8 );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER8 );
		PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED );
	
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER1, 100, 10, 0, 0 );
	
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER2, 100, 10, 0, 0 );
	
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER3, 100, 10, 0, 0 );
	
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER4, 100, 10, 0, 0 );
	
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER5, 100, 10, 0, 0 );
	
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER6, 100, 10, 0, 0 );
	
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER7, 100, 10, 0, 0 );
	
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER8, 100, 10, 0, 0 );
	
		PORT_START(); 
		PORT_DIPNAME( 0x0f, 0x08, "Play Time" );
		PORT_DIPSETTING(    0x0f, "60 seconds" );
		PORT_DIPSETTING(    0x0e, "69 seconds" );
		PORT_DIPSETTING(    0x0d, "77 seconds" );
		PORT_DIPSETTING(    0x0c, "86 seconds" );
		PORT_DIPSETTING(    0x0b, "95 seconds" );
		PORT_DIPSETTING(    0x0a, "103 seconds" );
		PORT_DIPSETTING(    0x09, "112 seconds" );
		PORT_DIPSETTING(    0x08, "120 seconds" );
		PORT_DIPSETTING(    0x07, "129 seconds" );
		PORT_DIPSETTING(    0x06, "138 seconds" );
		PORT_DIPSETTING(    0x05, "146 seconds" );
		PORT_DIPSETTING(    0x04, "155 seconds" );
		PORT_DIPSETTING(    0x03, "163 seconds" );
		PORT_DIPSETTING(    0x02, "172 seconds" );
		PORT_DIPSETTING(    0x01, "181 seconds" );
		PORT_DIPSETTING(    0x00, "189 seconds" );
		PORT_BIT ( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 
		PORT_BITX( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2, "Track Select", KEYCODE_SPACE, IP_JOY_DEFAULT );
	
		PORT_START(); 
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_VBLANK );
	
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_sprint8p = new InputPortPtr(){ public void handler() { 
	
		PORT_START(); 
		PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_COIN1 );
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER DIR P1 */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER FLAG P1 */
		PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED );
	
		PORT_START(); 
		PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_COIN2 );
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER DIR P2 */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER FLAG P2 */
		PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED );
	
		PORT_START(); 
		PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_COIN3 );
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER DIR P3 */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER FLAG P3 */
		PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER3 );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER3 );
		PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED );
	
		PORT_START(); 
		PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_COIN4 );
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER DIR P4 */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER FLAG P4 */
		PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER4 );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER4 );
		PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED );
	
		PORT_START(); 
		PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_COIN5 );
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER DIR P5 */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER FLAG P5 */
		PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER5 );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER5 );
		PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
		PORT_START(); 
		PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_COIN6 );
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER DIR P6 */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER FLAG P6 */
		PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER6 );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER6 );
		PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED );
	
		PORT_START(); 
		PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_COIN7 );
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER DIR P7 */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER FLAG P7 */
		PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER7 );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER7 );
		PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED );
	
		PORT_START(); 
		PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_COIN8 );
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER DIR P8 */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SPECIAL );/* STEER FLAG P8 */
		PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER8 );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER8 );
		PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED );
	
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER1, 100, 10, 0, 0 );
	
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER2, 100, 10, 0, 0 );
	
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER3, 100, 10, 0, 0 );
	
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER4, 100, 10, 0, 0 );
	
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER5, 100, 10, 0, 0 );
	
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER6, 100, 10, 0, 0 );
	
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER7, 100, 10, 0, 0 );
	
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_PLAYER8, 100, 10, 0, 0 );
	
		PORT_START(); 
		PORT_DIPNAME( 0x03, 0x03, "Play Time" );
		PORT_DIPSETTING(    0x00, "54 seconds" );
		PORT_DIPSETTING(    0x01, "108 seconds" );
		PORT_DIPSETTING(    0x03, "216 seconds" );
		PORT_BIT ( 0xfc, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 
		PORT_DIPNAME( 0x01, 0x01, "Play Mode" );
		PORT_DIPSETTING(    0x00, "Chase" );
		PORT_DIPSETTING(    0x01, "Tag" );
	
		PORT_START(); 
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_VBLANK );
	
	INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout tile_layout_1 = new GfxLayout
	(
		16, 8,
		64,
		1,
		new int[] { 0 },
		new int[] {
			7, 7, 6, 6, 5, 5, 4, 4, 3, 3, 2, 2, 1, 1, 0, 0
		},
		new int[] {
			0x000, 0x200, 0x400, 0x600, 0x800, 0xa00, 0xc00, 0xe00
		},
		8
	);
	
	
	static GfxLayout tile_layout_2 = new GfxLayout
	(
		16, 8,
		64,
		1,
		new int[] { 0 },
		new int[] {
			0x000, 0x000, 0x200, 0x200, 0x400, 0x400, 0x600, 0x600,
			0x800, 0x800, 0xa00, 0xa00, 0xc00, 0xc00, 0xe00, 0xe00
	
		},
		new int[] {
			0, 1, 2, 3, 4, 5, 6, 7
		},
		8
	);
	
	
	static GfxLayout car_layout = new GfxLayout
	(
		16, 8,
		8,
		1,
		new int[] { 0 },
		new int[] {
			0x07, 0x06, 0x05, 0x04, 0x0f, 0x0e, 0x0d, 0x0c,
			0x17, 0x16, 0x15, 0x14, 0x1f, 0x1e, 0x1d, 0x1c
		},
		new int[] {
			0x000, 0x020, 0x040, 0x060, 0x080, 0x0a0, 0x0c0, 0x0e0
		},
		0x100
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, tile_layout_1, 0, 18 ),
		new GfxDecodeInfo( REGION_GFX1, 0, tile_layout_2, 0, 18 ),
		new GfxDecodeInfo( REGION_GFX2, 0, car_layout, 0, 16 ),
		new GfxDecodeInfo( -1 )
	};
	
	
	static MACHINE_DRIVER_START( sprint8 )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M6800, 11055000 / 11) /* ? */
		MDRV_CPU_MEMORY(readmem, writemem)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(29 * 1000000 / 15750)
		MDRV_MACHINE_INIT(sprint8)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER | VIDEO_UPDATE_AFTER_VBLANK)
		MDRV_SCREEN_SIZE(512, 261)
		MDRV_VISIBLE_AREA(0, 495, 0, 231)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(18)
		MDRV_COLORTABLE_LENGTH(36)
	
		MDRV_PALETTE_INIT(sprint8)
		MDRV_VIDEO_START(sprint8)
		MDRV_VIDEO_UPDATE(sprint8)
		MDRV_VIDEO_EOF(sprint8)
	
		/* sound hardware */
	MACHINE_DRIVER_END
	
	
	static RomLoadPtr rom_sprint8 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );
		ROM_LOAD( "7313.j1", 0x2400, 0x0800, CRC(1231f944);SHA1(d16c76da6a74513eb40811d806e0dd009f6dcafb) )
		ROM_LOAD( "7314.h1", 0x2c00, 0x0800, CRC(c77c0d49);SHA1(a57b5d340a41d02edb20fcb66875908110582bc5) )
		ROM_RELOAD(          0xf800, 0x0800 );
	
		ROM_REGION( 0x0200, REGION_GFX1, ROMREGION_DISPOSE );/* tiles */
		ROM_LOAD( "7315-01.n6", 0x0000, 0x0200, CRC(e2f603d0);SHA1(8d82b72d2f4039afa3341774000105a745caf85f) )
	
		ROM_REGION( 0x0100, REGION_GFX2, ROMREGION_DISPOSE );/* cars */
		ROM_LOAD( "7316-01.j5", 0x0000, 0x0100, CRC(32c028e3);SHA1(bfa76cf0981640d08e9c7fb15da134afe46afe31) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_sprint8a = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );
		ROM_LOAD_NIB_LOW ( "l2800s8", 0x2800, 0x0800, CRC(3926de69);SHA1(ec03d7684e393061d1d48ae73897e9dc38131c14) )
		ROM_LOAD_NIB_HIGH( "m2800s8", 0x2800, 0x0800, CRC(d009d6da);SHA1(3210806b0eb344d88d2cbcc46895f7224771c1f2) )
		ROM_LOAD_NIB_LOW ( "l3000s8", 0x3000, 0x0800, CRC(c78d9888);SHA1(a854b50b2cf0261c1f966ef1bd001084500b3545) )
		ROM_LOAD_NIB_HIGH( "m3000s8", 0x3000, 0x0800, CRC(9ebfe8f8);SHA1(9709f697a7f9cce7ff4edbdccbaf14931328a052) )
		ROM_LOAD_NIB_LOW ( "l3800s8", 0x3800, 0x0800, CRC(74a8f103);SHA1(0cc15006cbd4579feac0f07690f32b2b61f97ae9) )
		ROM_RELOAD(                   0xf800, 0x0800 );
		ROM_LOAD_NIB_HIGH( "m3800s8", 0x3800, 0x0800, CRC(90aadc75);SHA1(34ca21c37573d9a2df92d3a1e73fdc0a9885c0a0) )
		ROM_RELOAD(                   0xf800, 0x0800 );
	
		ROM_REGION( 0x0200, REGION_GFX1, ROMREGION_DISPOSE );/* tiles */
		ROM_LOAD( "s8.n6", 0x0000, 0x0200, CRC(92cf9a7e);SHA1(6bd2d396e0a299c2e731425cabd578d569c2061b) )
	
		ROM_REGION( 0x0100, REGION_GFX2, ROMREGION_DISPOSE );/* cars */
		ROM_LOAD( "s8.j5", 0x0000, 0x0100, CRC(d37fff36);SHA1(20a7a8caf2fbfe22e307fe8541d31784c8e39d1a) )
	ROM_END(); }}; 
	
	
	public static GameDriver driver_sprint8	   = new GameDriver("1977"	,"sprint8"	,"sprint8.java"	,rom_sprint8,null	,machine_driver_sprint8	,input_ports_sprint8	,null	,ROT0	,	"Atari", "Sprint 8",             GAME_NO_SOUND )
	public static GameDriver driver_sprint8a	   = new GameDriver("1977"	,"sprint8a"	,"sprint8.java"	,rom_sprint8a,driver_sprint8	,machine_driver_sprint8	,input_ports_sprint8p	,null	,ROT0	,	"Atari", "Sprint 8 (play tag & chase)", GAME_NO_SOUND )
}
