/*******************************************************************************

Equites           (c) 1984 Alpha Denshi Co./Sega
Bull Fighter      (c) 1984 Alpha Denshi Co./Sega
The Koukouyakyuh  (c) 1985 Alpha Denshi Co.
Splendor Blast    (c) 1985 Alpha Denshi Co.
High Voltage      (c) 1985 Alpha Denshi Co.

drivers by Acho A. Tang

Stephh's notes (based on the games M68000 code and some tests) :

0) all games

  - To enter sort of "test mode", bits 0 and 1 need to be ON when the game is reset.
    Acho said that it could be a switch (but I'm not sure of that), and that's why
    I've added a EASY_TEST_MODE compilation switch.


1) 'equites'

  - When in "test mode", press START1 to cycle through next sound, and press START2
    to directly test the inputs and the Dip Switches.
  - When the number of buttons is set to 2, you need to press BOTH BUTTON1 and
    BUTTON2 to have the same effect as BUTTON3.


2) 'bullfgtr'

  - When in "test mode", press START1 to cycle through next sound, and press START2
    to directly test the inputs and the Dip Switches.
  - I'm not sure I understand how the coinage is handled, and so it's hard to make
    a good description. Anyway, the values are correct.


3) 'kouyakyu'

  - When in "test mode", press START1 to cycle through next sound, and press START2
    to directly test the inputs and the Dip Switches.
  - Bit 1 of Dip Switch is only read in combinaison of bit 0 during P.O.S.T. to
    enter the "test mode", but it doesn't add any credit ! That's why I've patched
    the inputs, so you can enter the "test mode" by pressing COIN1 during P.O.S.T.


4) 'splndrbt'

  - When starting a 2 players game, when player 1 game is over, the game enters in
    an infinite loop on displaying the "GAME OVER" message.
  - You can test player 2 by putting 0xff instead of 0x00 at 0x040009 ($9,A6).
  - FYI, what should change the contents of $9,A6 is the routine at 0x000932,
    but I haven't found where this routine could be called 8( 8303 issue ?


5) 'hvoltage'

  - When starting a 2 players game, when player 1 game is over, the game becomes
    buggy on displaying the "GAME OVER" message and you can't start a new game
    anymore.
  - You can test player 2 by putting 0xff instead of 0x00 at 0x040009 ($9,A6).
  - FYI, what should change the contents of $9,A6 is the routine at 0x000fc4,
    but I haven't found where this routine could be called 8( 8404 issue ?

  - There is sort of "debug mode" that you can access if 0x000038.w returns 0x0000
    instead of 0xffff. To enable it, turn HVOLTAGE_HACK to 1 then enable the fake
    Dip Switch.
  - When you are in "debug mode", the Inputs and Dip Switches have special features.
    Here is IMO the full list :

      * pressing IPT_JOYSTICK_DOWN of player 2 freezes the game
      * pressing IPT_JOYSTICK_UP of player 2 unfreezes the game
      * pressing IPT_COIN1 gives invulnerability (the collision routine isn't called)
      * pressing IPT_COIN2 speeds up the game and you don't need to kill the bosses
      * when bit 2 is On, you are given invulnerability (same effect as IPT_COIN1)
      * when bit 3 is On, you don't need to kill the bosses (only the last one)
      * when bit 4 is On ("Lives" Dip Switch set to "5"), some coordonates are displayed
      * when bit 7 is On ("Coinage" Dip Switch set to "A 1/3C B 1/6C" or "A 2/1C B 3/1C"),
        a "band" is displayed at the left of the screen


TO DO :

  - support screen flipping in 'equites' and 'splndrbt'.


Hardware Deficiencies
---------------------

- Lack of 8303/8404 tech info. All MCU results are guessed.

  equites_8404rule(unsigned pc, int offset, int data) details:

	    pc: 68000 code address where the program queries the MCU
	offset: 8404 memory offset(in bytes) from where MCU data is read
	  data: fake byte-value to return (negative numbers trigger special conditions)

The following ROMs need redump:

- Bull Fighter's RGB PROMs (the ones in use are fake make-up's)
- The Koukouyakyuh's epr-6706.bin (the one in use is patched)


Emulation Deficiencies
----------------------

- Equites has sprite lag in the post-rotate X direction depends on interrupt timing.
- Scale factors in High Voltage and Splendor Blast are inaccurate. Actual values are believed
  to be in the three unknown ROM s3.8l, 1.9j and 4.7m but the equations are unknown.

- MSM5232 clock speed and capacitor values are not known.
- There seems to be a rheostat on Equites' soundboard to adjust the MSM5232's music pitch.
- It hasn't been confirmed whether music tempos are the same across all games.


* Special Thanks to:

  Jarek Burczynski for a superb MSM5232 emulation
  The Equites WIP webmasters for the vital screenshots and sound clips
  Yasuhiro Ogawa for the correct Equites ROM information


Other unemulated Alpha Denshi and SNK games that may use similar hardware:
-----------------------------------------------------------
Maker        Year Genre Name             Japanese Name
-----------------------------------------------------------
Alpha Denshi 1984 (SPT) Champion Croquet チャンピオンクロッケー
Alpha Denshi 1985 (???) Tune Pit(?)      チェーンピット
Alpha Denshi 1985 (MAJ) Perfect Janputer パーフェクトジャンピューター
SNK/Eastern  1985 (ACT) Gekisoh          激走

*******************************************************************************/
// Directives

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class equites
{
	
	#define HVOLTAGE_HACK	0
	#define EASY_TEST_MODE	1
	
	// Equites Hardware
	#define BMPAD 8
	
	/******************************************************************************/
	// Imports
	
	// Common Hardware Start
	#define EQUITES_ADD_SOUNDBOARD7 \
		MDRV_CPU_ADD(8085A, 5000000) \
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU) \
		MDRV_CPU_MEMORY(equites_s_readmem, equites_s_writemem) \
		MDRV_CPU_PORTS(0, equites_s_writeport) \
		MDRV_CPU_PERIODIC_INT(nmi_line_pulse, 4000) \
		MDRV_SOUND_ADD(MSM5232, equites_5232intf) \
		MDRV_SOUND_ADD(AY8910, equites_8910intf) \
		MDRV_SOUND_ADD(DAC, equites_dacintf)
	
	
	extern READ16_HANDLER(equites_8404_r);
	extern WRITE_HANDLER(equites_5232_w);
	extern WRITE_HANDLER(equites_8910control_w);
	extern WRITE_HANDLER(equites_8910data_w);
	extern WRITE_HANDLER(equites_dac0_w);
	extern WRITE_HANDLER(equites_dac1_w);
	
	extern data16_t *equites_8404ram;
	extern struct MSM5232interface equites_5232intf;
	extern struct AY8910interface equites_8910intf;
	extern struct DACinterface equites_dacintf;
	
	public static Memory_ReadAddress equites_s_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0xbfff, MRA_ROM ), // sound program
		new Memory_ReadAddress( 0xc000, 0xc000, soundlatch_r ),
		new Memory_ReadAddress( 0xe000, 0xe0ff, MRA_RAM ), // stack and variables
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress equites_s_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0xbfff, MWA_ROM ), // sound program
		new Memory_WriteAddress( 0xc080, 0xc08d, equites_5232_w ),
		new Memory_WriteAddress( 0xc0a0, 0xc0a0, equites_8910data_w ),
		new Memory_WriteAddress( 0xc0a1, 0xc0a1, equites_8910control_w ),
		new Memory_WriteAddress( 0xc0b0, 0xc0b0, MWA_NOP ), // INTR: sync with main melody
		new Memory_WriteAddress( 0xc0c0, 0xc0c0, MWA_NOP ), // INTR: sync with specific beats
		new Memory_WriteAddress( 0xc0d0, 0xc0d0, equites_dac0_w ),
		new Memory_WriteAddress( 0xc0e0, 0xc0e0, equites_dac1_w ),
		new Memory_WriteAddress( 0xc0f8, 0xc0fe, MWA_NOP ), // soundboard I/O, ignored
		new Memory_WriteAddress( 0xc0ff, 0xc0ff, soundlatch_clear_w ),
		new Memory_WriteAddress( 0xe000, 0xe0ff, MWA_RAM ), // stack and variables
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort equites_s_writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00e0, 0x00e5, MWA_NOP ), // soundboard I/O, ignored
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	// Common Hardware End
	
	// Equites Hardware
	extern PALETTE_INIT( equites );
	extern VIDEO_START( equites );
	extern VIDEO_UPDATE( equites );
	extern READ16_HANDLER(equites_spriteram_r);
	extern WRITE16_HANDLER(equites_charram_w);
	extern WRITE16_HANDLER(equites_scrollreg_w);
	extern WRITE16_HANDLER(equites_bgcolor_w);
	
	// Splendor Blast Hareware
	extern MACHINE_INIT( splndrbt );
	extern PALETTE_INIT( splndrbt );
	extern VIDEO_START( splndrbt );
	extern VIDEO_UPDATE( splndrbt );
	extern READ16_HANDLER(splndrbt_bankedchar_r);
	extern WRITE16_HANDLER(splndrbt_charram_w);
	extern WRITE16_HANDLER(splndrbt_bankedchar_w);
	extern WRITE16_HANDLER(splndrbt_selchar0_w);
	extern WRITE16_HANDLER(splndrbt_selchar1_w);
	extern WRITE16_HANDLER(splndrbt_scrollram_w);
	extern WRITE16_HANDLER(splndrbt_bgcolor_w);
	extern data16_t *splndrbt_scrollx, *splndrbt_scrolly;
	
	/******************************************************************************/
	// Locals
	
	// Equites Hardware
	static int disablejoyport2 = 0;
	
	/******************************************************************************/
	// Exports
	
	// Common
	data16_t *equites_workram;
	int equites_id;
	
	// Equites Hardware
	int equites_flip;
	
	// Splendor Blast Hardware
	int splndrbt_flip;
	
	/******************************************************************************/
	// Local Functions
	
	/******************************************************************************/
	// Interrupt Handlers
	
	// Equites Hardware
	static INTERRUPT_GEN( equites_interrupt )
	{
		if (cpu_getiloops())
			cpu_set_irq_line(0, 2, HOLD_LINE);
		else
			cpu_set_irq_line(0, 1, HOLD_LINE);
	}
	
	// Splendor Blast Hareware
	static INTERRUPT_GEN( splndrbt_interrupt )
	{
		if (cpu_getiloops())
			cpu_set_irq_line(0, 2, HOLD_LINE);
		else
			cpu_set_irq_line(0, 1, HOLD_LINE);
	}
	
	/******************************************************************************/
	// Main CPU Handlers
	
	// Equites Hardware
	static READ16_HANDLER(equites_joyport_r)
	{
		int data;
	
		data = readinputport(0);
		if (disablejoyport2) data = (data & 0x80ff) | (data<<8 & 0x7f00);
	
		return (data);
	}
	
	static WRITE16_HANDLER(equites_flip0_w)
	{
		if (ACCESSING_LSB) disablejoyport2 = 1;
		if (ACCESSING_MSB) equites_flip = 0;
	}
	
	static WRITE16_HANDLER(equites_flip1_w)
	{
		if (ACCESSING_LSB) disablejoyport2 = 0;
		if (ACCESSING_MSB) equites_flip = 1;
	}
	
	// Splendor Blast Hardware
	#if HVOLTAGE_HACK
	static READ16_HANDLER(hvoltage_debug_r)
	{
		return(readinputport(2));
	}
	#endif
	
	static WRITE16_HANDLER(splndrbt_flip0_w)
	{
		if (ACCESSING_LSB) splndrbt_flip = 0;
		if (ACCESSING_MSB) equites_bgcolor_w(offset, data, 0x00ff);
	}
	
	static WRITE16_HANDLER(splndrbt_flip1_w)
	{
		if (ACCESSING_LSB) splndrbt_flip = 1;
	}
	#if 0
	static WRITE16_HANDLER(log16_w)
	{
		int pc = activecpu_get_pc();
	
		usrintf_showmessage("%04x: %04x(w)\n", pc, data);
		logerror("%04x: %04x(w)\n", pc, data);
	}
	#endif
	/******************************************************************************/
	// Main CPU Memory Map
	
	// Equites Hardware
	static MEMORY_READ16_START( equites_readmem )
		{ 0x000000, 0x00ffff, MRA16_ROM }, // main program
		{ 0x040000, 0x040fff, MRA16_RAM }, // work RAM
		{ 0x080000, 0x080fff, MRA16_RAM }, // char RAM
		{ 0x0c0000, 0x0c0fff, MRA16_RAM }, // scroll RAM
		{ 0x100000, 0x100fff, equites_spriteram_r }, // sprite RAM
		{ 0x140000, 0x1407ff, equites_8404_r }, // 8404 RAM
		{ 0x180000, 0x180001, input_port_1_word_r }, // MSB: DIP switches
		{ 0x1c0000, 0x1c0001, equites_joyport_r }, // joyport[2211] (shares the same addr with scrollreg)
	MEMORY_END
	
	static MEMORY_WRITE16_START( equites_writemem )
		{ 0x000000, 0x00ffff, MWA16_NOP }, // ROM area is written several times (dev system?)
		{ 0x040000, 0x040fff, MWA16_RAM, &equites_workram },
		{ 0x080000, 0x080fff, equites_charram_w, &videoram16 },
		{ 0x0c0000, 0x0c0fff, MWA16_RAM, &spriteram16_2 },
		{ 0x100000, 0x100fff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0x140000, 0x1407ff, MWA16_RAM, &equites_8404ram },
		{ 0x180000, 0x180001, soundlatch_word_w }, // LSB: sound latch
		{ 0x184000, 0x184001, equites_flip0_w }, // [MMLL] MM: normal screen, LL: use joystick 1 only
		{ 0x188000, 0x188001, MWA16_NOP }, // 8404 control port1
		{ 0x18c000, 0x18c001, MWA16_NOP }, // 8404 control port2
		{ 0x1a4000, 0x1a4001, equites_flip1_w }, // [MMLL] MM: flip screen, LL: use both joysticks
		{ 0x1a8000, 0x1a8001, MWA16_NOP }, // 8404 control port3
		{ 0x1ac000, 0x1ac001, MWA16_NOP }, // 8404 control port4
		{ 0x1c0000, 0x1c0001, equites_scrollreg_w }, // scroll register[XXYY]
		{ 0x380000, 0x380001, equites_bgcolor_w }, // bg color register[CC--]
		{ 0x780000, 0x780001, MWA16_NOP }, // watchdog
	MEMORY_END
	
	// Splendor Blast Hardware
	static MEMORY_READ16_START( splndrbt_readmem )
		{ 0x000000, 0x00ffff, MRA16_ROM }, // main program
		{ 0x040000, 0x040fff, MRA16_RAM }, // work RAM
		{ 0x080000, 0x080001, input_port_0_word_r }, // joyport [2211]
		{ 0x0c0000, 0x0c0001, input_port_1_word_r }, // MSB: DIP switches LSB: not used
		{ 0x100000, 0x100001, MRA16_RAM }, // no read
		{ 0x1c0000, 0x1c0001, MRA16_RAM }, // LSB: watchdog
		{ 0x180000, 0x1807ff, equites_8404_r }, // 8404 RAM
		{ 0x200000, 0x200fff, MRA16_RAM }, // char page 0
		{ 0x201000, 0x201fff, splndrbt_bankedchar_r }, // banked char page 1, 2
		{ 0x400000, 0x400fff, MRA16_RAM }, // scroll RAM 0,1
		{ 0x600000, 0x6001ff, MRA16_RAM }, // sprite RAM 0,1,2
	MEMORY_END
	
	static MEMORY_WRITE16_START( splndrbt_writemem )
		{ 0x000000, 0x00ffff, MWA16_NOP },
		{ 0x040000, 0x040fff, MWA16_RAM, &equites_workram }, // work RAM
		{ 0x0c0000, 0x0c0001, splndrbt_flip0_w }, // [MMLL] MM: bg color register, LL: normal screen
		{ 0x0c4000, 0x0c4001, MWA16_NOP }, // 8404 control port1
		{ 0x0c8000, 0x0c8001, MWA16_NOP }, // 8404 control port2
		{ 0x0cc000, 0x0cc001, splndrbt_selchar0_w }, // select active char map
		{ 0x0e0000, 0x0e0001, splndrbt_flip1_w }, // [MMLL] MM: not used, LL: flip screen
		{ 0x0e4000, 0x0e4001, MWA16_NOP }, // 8404 control port3
		{ 0x0e8000, 0x0e8001, MWA16_NOP }, // 8404 control port4
		{ 0x0ec000, 0x0ec001, splndrbt_selchar1_w }, // select active char map
		{ 0x100000, 0x100001, MWA16_RAM, &splndrbt_scrollx }, // scrollx
		{ 0x140000, 0x140001, soundlatch_word_w }, // LSB: sound command
		{ 0x1c0000, 0x1c0001, MWA16_RAM, &splndrbt_scrolly }, // scrolly
		{ 0x180000, 0x1807ff, MWA16_RAM, &equites_8404ram }, // 8404 RAM
		{ 0x200000, 0x200fff, splndrbt_charram_w, &videoram16, &videoram_size }, // char RAM page 0
		{ 0x201000, 0x201fff, splndrbt_bankedchar_w }, // banked char RAM page 1,2
		{ 0x400000, 0x400fff, splndrbt_scrollram_w, &spriteram16_2 }, // scroll RAM 0,1
		{ 0x600000, 0x6001ff, MWA16_RAM, &spriteram16, &spriteram_size }, // sprite RAM 0,1,2
	MEMORY_END
	
	/******************************************************************************/
	// Common Port Map
	
	#define EQUITES_PLAYER_INPUT_LSB( button1, button2, button3, start ) \
		PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY );\
		PORT_BIT( 0x0002, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY );\
		PORT_BIT( 0x0004, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY );\
		PORT_BIT( 0x0008, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY );\
		PORT_BIT( 0x0010, IP_ACTIVE_HIGH, button1 );\
		PORT_BIT( 0x0020, IP_ACTIVE_HIGH, button2 );\
		PORT_BIT( 0x0040, IP_ACTIVE_HIGH, button3 );\
		PORT_BIT( 0x0080, IP_ACTIVE_HIGH, start );
	
	#define EQUITES_PLAYER_INPUT_MSB( button1, button2, button3, start ) \
		PORT_BIT( 0x0100, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );\
		PORT_BIT( 0x0200, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );\
		PORT_BIT( 0x0400, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );\
		PORT_BIT( 0x0800, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );\
		PORT_BIT( 0x1000, IP_ACTIVE_HIGH, button1 | IPF_COCKTAIL );\
		PORT_BIT( 0x2000, IP_ACTIVE_HIGH, button2 | IPF_COCKTAIL );\
		PORT_BIT( 0x4000, IP_ACTIVE_HIGH, button3 | IPF_COCKTAIL );\
		PORT_BIT( 0x8000, IP_ACTIVE_HIGH, start );
	
	/******************************************************************************/
	// Equites Port Map
	
	static InputPortPtr input_ports_equites = new InputPortPtr(){ public void handler() { 
		PORT_START(); 
		EQUITES_PLAYER_INPUT_LSB( IPT_BUTTON1, IPT_BUTTON2, IPT_BUTTON3, IPT_START1 )
		EQUITES_PLAYER_INPUT_MSB( IPT_BUTTON1, IPT_BUTTON2, IPT_BUTTON3, IPT_START2 )
	
		PORT_START(); 
		PORT_BIT( 0x0100, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x0200, IP_ACTIVE_HIGH, IPT_COIN2 );
	#if EASY_TEST_MODE
		PORT_SERVICE( 0x0300, IP_ACTIVE_HIGH );
	#endif
		PORT_DIPNAME( 0x0400, 0x0400, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(      0x0400, DEF_STR( "Upright") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x0800, 0x0800, "Buttons" );
		PORT_DIPSETTING(      0x0000, "2" );
		PORT_DIPSETTING(      0x0800, "3" );
		PORT_DIPNAME( 0x1000, 0x0000, DEF_STR ( Lives );
		PORT_DIPSETTING(      0x0000, "3" );
		PORT_DIPSETTING(      0x1000, "5" );
		PORT_DIPNAME( 0x2000, 0x0000, DEF_STR ( Difficulty );
		PORT_DIPSETTING(      0x0000, "Normal" );
		PORT_DIPSETTING(      0x2000, "Hard" );
		PORT_DIPNAME( 0xc000, 0x0000, DEF_STR( "Coinage") );
		PORT_DIPSETTING(      0xc000, "A 2C/1C B 3C/1C" );
		PORT_DIPSETTING(      0x0000, "A 1C/1C B 2C/1C" );
		PORT_DIPSETTING(      0x8000, "A 1C/2C B 1C/4C" );
		PORT_DIPSETTING(      0x4000, "A 1C/3C B 1C/6C" );
	INPUT_PORTS_END(); }}; 
	
	/******************************************************************************/
	// Bull Fighter Port Map
	
	static InputPortPtr input_ports_bullfgtr = new InputPortPtr(){ public void handler() { 
		PORT_START(); 
		EQUITES_PLAYER_INPUT_LSB( IPT_BUTTON1, IPT_BUTTON2, IPT_BUTTON3, IPT_START1 )
		EQUITES_PLAYER_INPUT_MSB( IPT_BUTTON1, IPT_BUTTON2, IPT_BUTTON3, IPT_START2 )
	
		PORT_START(); 
		PORT_BIT( 0x0100, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x0200, IP_ACTIVE_HIGH, IPT_COIN2 );
	#if EASY_TEST_MODE
		PORT_SERVICE( 0x0300, IP_ACTIVE_HIGH );
	#endif
		PORT_DIPNAME( 0x0c00, 0x0000, "Game Time" );
		PORT_DIPSETTING(      0x0c00, "3:00" );
		PORT_DIPSETTING(      0x0800, "2:00" );
		PORT_DIPSETTING(      0x0000, "1:30" );
		PORT_DIPSETTING(      0x0400, "1:00" );
		PORT_DIPNAME( 0x2000, 0x0000, DEF_STR ( Difficulty );
		PORT_DIPSETTING(      0x0000, "Normal" );
		PORT_DIPSETTING(      0x2000, "Hard" );
		PORT_DIPNAME( 0x4000, 0x4000, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x4000, DEF_STR( "On") );
		PORT_DIPNAME( 0x9000, 0x0000, DEF_STR( "Coinage") );
	//	PORT_DIPSETTING(      0x9000, "A 1C/1C B 1C/1C" );	// More than 1 credit per player needed
		PORT_DIPSETTING(      0x0000, "A 1C/1C B 1C/1C" );
		PORT_DIPSETTING(      0x8000, "A 1C/1C B 1C/4C" );
		PORT_DIPSETTING(      0x1000, "A 1C/2C B 1C/3C" );
	INPUT_PORTS_END(); }}; 
	
	/******************************************************************************/
	// Koukouyakyuh Port Map
	
	static InputPortPtr input_ports_kouyakyu = new InputPortPtr(){ public void handler() { 
		PORT_START(); 
		EQUITES_PLAYER_INPUT_LSB( IPT_BUTTON1, IPT_BUTTON2, IPT_BUTTON3, IPT_START1 )
		EQUITES_PLAYER_INPUT_MSB( IPT_BUTTON1, IPT_BUTTON2, IPT_BUTTON3, IPT_START2 )
	
		PORT_START(); 
	//	PORT_BIT( 0x0100, IP_ACTIVE_HIGH, IPT_COIN1 );
	//	PORT_BIT( 0x0200, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x0300, IP_ACTIVE_HIGH, IPT_COIN1 );
	#if EASY_TEST_MODE
		PORT_SERVICE( 0x0300, IP_ACTIVE_HIGH );
	#endif
		PORT_DIPNAME( 0x0c00, 0x0000, "Game Points" );
		PORT_DIPSETTING(      0x0800, "3000" );
		PORT_DIPSETTING(      0x0400, "4000" );
		PORT_DIPSETTING(      0x0000, "5000" );
		PORT_DIPSETTING(      0x0c00, "7000" );
		PORT_DIPNAME( 0x2000, 0x0000, DEF_STR( "Unused") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x2000, DEF_STR( "On") );
		PORT_DIPNAME( 0x4000, 0x4000, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x4000, DEF_STR( "On") );
		PORT_DIPNAME( 0x9000, 0x0000, DEF_STR( "Coinage") );
		PORT_DIPSETTING(      0x9000, "1C/1C (2C per player); )
		PORT_DIPSETTING(      0x0000, "1C/1C (1C per player); )
		PORT_DIPSETTING(      0x8000, "1C/1C (1C for 2 players); )
		PORT_DIPSETTING(      0x1000, "1C/3C (1C per player); )
	INPUT_PORTS_END(); }}; 
	
	/******************************************************************************/
	// Splendor Blast Port Map
	
	static InputPortPtr input_ports_splndrbt = new InputPortPtr(){ public void handler() { 
		PORT_START(); 
		EQUITES_PLAYER_INPUT_LSB( IPT_BUTTON1, IPT_BUTTON2, IPT_UNKNOWN, IPT_START1 )
		EQUITES_PLAYER_INPUT_MSB( IPT_BUTTON1, IPT_BUTTON2, IPT_UNKNOWN, IPT_START2 )
	
		PORT_START(); 
		PORT_BIT( 0x0100, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x0200, IP_ACTIVE_HIGH, IPT_COIN2 );
	#if EASY_TEST_MODE
		PORT_SERVICE( 0x0300, IP_ACTIVE_HIGH );
	#endif
		PORT_DIPNAME( 0x0c00, 0x0000, DEF_STR ( Difficulty );
		PORT_DIPSETTING(      0x0400, "Easy" );
		PORT_DIPSETTING(      0x0000, "Normal" );
		PORT_DIPSETTING(      0x0800, "Hard" );
		PORT_DIPSETTING(      0x0c00, "Hardest" );
		PORT_DIPNAME( 0x1000, 0x0000, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(      0x1000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x2000, 0x2000, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(      0x2000, DEF_STR( "Upright") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0xc000, 0x0000, DEF_STR( "Coinage") );
		PORT_DIPSETTING(      0xc000, "A 2C/1C B 3C/1C" );
		PORT_DIPSETTING(      0x0000, "A 1C/1C B 2C/1C" );
		PORT_DIPSETTING(      0x4000, "A 1C/2C B 1C/4C" );
		PORT_DIPSETTING(      0x8000, "A 1C/3C B 1C/6C" );
	INPUT_PORTS_END(); }}; 
	
	/******************************************************************************/
	// High Voltage Port Map
	
	static InputPortPtr input_ports_hvoltage = new InputPortPtr(){ public void handler() { 
		PORT_START(); 
		EQUITES_PLAYER_INPUT_LSB( IPT_BUTTON1, IPT_BUTTON2, IPT_UNKNOWN, IPT_START1 )
		EQUITES_PLAYER_INPUT_MSB( IPT_BUTTON1, IPT_BUTTON2, IPT_UNKNOWN, IPT_START2 )
	
		PORT_START(); 
		PORT_BIT( 0x0100, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x0200, IP_ACTIVE_HIGH, IPT_COIN2 );
	#if EASY_TEST_MODE
		PORT_SERVICE( 0x0300, IP_ACTIVE_HIGH );
	#endif
	#if HVOLTAGE_HACK
		PORT_DIPNAME( 0x0400, 0x0000, "Invulnerability" );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0400, DEF_STR( "On") );
		PORT_DIPNAME( 0x0800, 0x0000, "Need to kill Bosses" );
		PORT_DIPSETTING(      0x0800, DEF_STR( "No") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Yes") );
	#else
		PORT_DIPNAME( 0x0c00, 0x0000, DEF_STR ( Difficulty );
		PORT_DIPSETTING(      0x0400, "Easy" );
		PORT_DIPSETTING(      0x0000, "Normal" );
		PORT_DIPSETTING(      0x0800, "Hard" );
		PORT_DIPSETTING(      0x0c00, "Hardest" );
	#endif
		PORT_DIPNAME( 0x1000, 0x0000, DEF_STR ( Lives );			// See notes
		PORT_DIPSETTING(      0x0000, "3" );
		PORT_DIPSETTING(      0x1000, "5" );
		PORT_DIPNAME( 0x2000, 0x0000, DEF_STR ( Bonus_Life );
		PORT_DIPSETTING(      0x0000, "50k, 100k then every 100k" );
		PORT_DIPSETTING(      0x2000, "50k, 200k then every 100k" );
		PORT_DIPNAME( 0xc000, 0x0000, DEF_STR( "Coinage") );			// See notes
		PORT_DIPSETTING(      0xc000, "A 2C/1C B 3C/1C" );
		PORT_DIPSETTING(      0x0000, "A 1C/1C B 2C/1C" );
		PORT_DIPSETTING(      0x4000, "A 1C/2C B 1C/4C" );
		PORT_DIPSETTING(      0x8000, "A 1C/3C B 1C/6C" );
	
	#if HVOLTAGE_HACK
		/* Fake port to handle debug mode */
		PORT_START(); 
		PORT_DIPNAME( 0xffff, 0xffff, "Debug Mode" );
		PORT_DIPSETTING(      0xffff, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
	#endif
	INPUT_PORTS_END(); }}; 
	
	/******************************************************************************/
	// Graphics Layouts
	
	// Equites Hardware
	static GfxLayout eq_charlayout = new GfxLayout
	(
		8, 8,
		256,
		2,
		new int[] { 0, 4 },
		new int[] { 8*8+3, 8*8+2, 8*8+1, 8*8, 3, 2, 1, 0 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		16*8
	);
	
	static GfxLayout eq_tilelayout = new GfxLayout
	(
		16, 16,
		256,
		3,
		new int[] { 0, 0x4000*8, 0x4000*8+4 },
		new int[] {
		  128*1+3, 128*1+2, 128*1+1, 128*1+0,
		  128*2+3, 128*2+2, 128*2+1, 128*2+0,
		  128*3+3, 128*3+2, 128*3+1, 128*3+0,
		        3,       2,       1,       0
		},
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8, 8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		64*8
	);
	
	static GfxLayout eq_spritelayout = new GfxLayout
	(
		16, 14,
		256,
		3,
		new int[] { 0, 0x4000*8, 0x4000*8+4 },
		new int[] {
		        3,       2,       1,       0,
		  128*1+3, 128*1+2, 128*1+1, 128*1+0,
		  128*2+3, 128*2+2, 128*2+1, 128*2+0,
		  128*3+3, 128*3+2, 128*3+1, 128*3+0
		},
		new int[] { 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8, 8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8 },
		64*8
	);
	
	static GfxDecodeInfo equites_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, eq_charlayout,     0, 32 ), // chars
		new GfxDecodeInfo( REGION_GFX2, 0, eq_tilelayout,   128, 16 ), // tile set0
		new GfxDecodeInfo( REGION_GFX3, 0, eq_tilelayout,   128, 16 ), // tile set1
		new GfxDecodeInfo( REGION_GFX4, 0, eq_spritelayout, 256, 16 ), // sprite set0
		new GfxDecodeInfo( REGION_GFX5, 0, eq_spritelayout, 256, 16 ), // sprite set1
		new GfxDecodeInfo( -1 ) // end of array
	};
	
	// Splendor Blast Hardware
	static GfxLayout sp_charlayout = new GfxLayout
	(
		8, 8,
		512,
		2,
		new int[] { 0, 4 },
		new int[] { 8*8+3, 8*8+2, 8*8+1, 8*8, 3, 2, 1, 0 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		16*8
	);
	
	static GfxLayout sp_tilelayout = new GfxLayout
	(
		16,16,
		256,
		2,
		new int[] { 0, 4 },
		new int[] {
		  16*8+3, 16*8+2, 16*8+1, 16*8+0,
		  32*8+3, 32*8+2, 32*8+1, 32*8+0,
		  48*8+3, 48*8+2, 48*8+1, 48*8+0,
		       3,      2,      1,      0
		},
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8, 8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		64*8
	);
	
	static GfxLayout sp_spritelayout = new GfxLayout
	(
		32,32,
		128,
		3,
		new int[] { 0, 0x8000*8, 0x8000*8+4 },
		new int[] {
		  0*8+3, 0*8+2, 0*8+1, 0*8+0, 1*8+3, 1*8+2, 1*8+1, 1*8+0,
		  2*8+3, 2*8+2, 2*8+1, 2*8+0, 3*8+3, 3*8+2, 3*8+1, 3*8+0,
		  4*8+3, 4*8+2, 4*8+1, 4*8+0, 5*8+3, 5*8+2, 5*8+1, 5*8+0,
		  6*8+3, 6*8+2, 6*8+1, 6*8+0, 7*8+3, 7*8+2, 7*8+1, 7*8+0
		},
		new int[] {
		  0*8*8, 1*8*8, 2*8*8, 3*8*8, 4*8*8, 5*8*8, 6*8*8, 7*8*8,
		  8*8*8, 9*8*8,10*8*8,11*8*8,12*8*8,13*8*8,14*8*8,15*8*8,
		 31*8*8,30*8*8,29*8*8,28*8*8,27*8*8,26*8*8,25*8*8,24*8*8,
		 23*8*8,22*8*8,21*8*8,20*8*8,19*8*8,18*8*8,17*8*8,16*8*8
		},
		8*32*8
	);
	
	static GfxDecodeInfo splndrbt_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, sp_charlayout,     0, 256/4 ), // 512 4-color chars
		new GfxDecodeInfo( REGION_GFX2, 0, sp_tilelayout,   256, 256/4 ), // 256 4-color tiles
		new GfxDecodeInfo( REGION_GFX3, 0, sp_tilelayout,   256, 256/4 ), // 256 4-color tiles
		new GfxDecodeInfo( REGION_GFX4, 0, sp_spritelayout, 512, 256/8 ), // 256 8-color sprites
		new GfxDecodeInfo( -1 ) // end of array
	};
	
	/******************************************************************************/
	// Hardware Definitions
	
	static MACHINE_DRIVER_START( equites )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, 12000000/2) // OSC: 12Mhz
		MDRV_CPU_MEMORY(equites_readmem, equites_writemem)
		MDRV_CPU_VBLANK_INT(equites_interrupt, 2)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(600)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER | VIDEO_UPDATE_AFTER_VBLANK)
		MDRV_SCREEN_SIZE(256 +BMPAD*2, 256 +BMPAD*2)
		MDRV_VISIBLE_AREA(0 +BMPAD, 256-1 +BMPAD, 24 +BMPAD, 232-1 +BMPAD)
		MDRV_PALETTE_LENGTH(256)
		MDRV_COLORTABLE_LENGTH(384)
		MDRV_GFXDECODE(equites_gfxdecodeinfo)
	
		MDRV_PALETTE_INIT(equites)
		MDRV_VIDEO_START(equites)
		MDRV_VIDEO_UPDATE(equites)
	
		/* sound hardware */
		EQUITES_ADD_SOUNDBOARD7
	
	MACHINE_DRIVER_END
	
	static MACHINE_DRIVER_START( splndrbt )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, 12000000/2) // OSC: 12Mhz
		MDRV_CPU_MEMORY(splndrbt_readmem, splndrbt_writemem)
		MDRV_CPU_VBLANK_INT(splndrbt_interrupt, 2)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(600)
		MDRV_MACHINE_INIT(splndrbt)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER | VIDEO_UPDATE_AFTER_VBLANK)
		MDRV_SCREEN_SIZE(256, 256)
		MDRV_VISIBLE_AREA(0, 256-1, 64, 256-1)
		MDRV_PALETTE_LENGTH(256)
		MDRV_COLORTABLE_LENGTH(1536)
		MDRV_GFXDECODE(splndrbt_gfxdecodeinfo)
	
		MDRV_PALETTE_INIT(splndrbt)
		MDRV_VIDEO_START(splndrbt)
		MDRV_VIDEO_UPDATE(splndrbt)
	
		/* sound hardware */
		EQUITES_ADD_SOUNDBOARD7
	
	MACHINE_DRIVER_END
	
	/******************************************************************************/
	// Equites ROM Map
	
	static RomLoadPtr rom_equites = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );// 68000 ROMs
		ROM_LOAD16_BYTE( "ep1", 0x00001, 0x2000, CRC(6a4fe5f7),SHA1(5ff1594a2cee28cc7d59448eb57473088ac6f14b) )
		ROM_LOAD16_BYTE( "ep5", 0x00000, 0x2000, CRC(00faa3eb),SHA1(6b31d041ad4ca81eda36487f659997cc4030f23c) )
		ROM_LOAD16_BYTE( "epr-ep2.12d", 0x04001, 0x2000, CRC(0c1bc2e7),SHA1(4c3510dfeee2fb2f295a32e2fe2021c4c7f08e8a) )
		ROM_LOAD16_BYTE( "epr-ep6.12b", 0x04000, 0x2000, CRC(bbed3dcc),SHA1(46ef2c60ccfa76a187b19dc0b7e6c594050b183f) )
		ROM_LOAD16_BYTE( "epr-ep3.10d", 0x08001, 0x2000, CRC(5f2d059a),SHA1(03fe904a445cce89462788fecfd61ac53f4dd17f) )
		ROM_LOAD16_BYTE( "epr-ep7.10b", 0x08000, 0x2000, CRC(a8f6b8aa),SHA1(ee4edb54c147a95944482e998616b025642a268a) )
		ROM_LOAD16_BYTE( "ep4",  0x0c001, 0x2000, CRC(b636e086),SHA1(5fc23a86b6051ecf6ff3f95f810f0eb471a203b0) )
		ROM_LOAD16_BYTE( "ep8",  0x0c000, 0x2000, CRC(d7ee48b0),SHA1(d0398704d8e89f2b0a9ed05e18f7c644d1e047c0) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );// Z80 ROMs
		ROM_LOAD( "ev1.1m", 0x00000, 0x2000, CRC(43faaf2e),SHA1(c9aaf298d673eb70399366776474f1b242549eb4) )
		ROM_LOAD( "ev2.1l", 0x02000, 0x2000, CRC(09e6702d),SHA1(896771f73a486e5035909eeed9ef48103d81d4ae) )
		ROM_LOAD( "ev3.1k", 0x04000, 0x2000, CRC(10ff140b),SHA1(7c28f988a9c8b2a702d007096199e67b447a183c) )
		ROM_LOAD( "ev4.1h", 0x06000, 0x2000, CRC(b7917264),SHA1(e58345fda088b171fd348959de15082f3cb42514) )
	
		ROM_REGION( 0x1000, REGION_GFX1, ROMREGION_DISPOSE );// chars
		ROM_LOAD( "ep9",  0x00000, 0x1000, CRC(0325be11),SHA1(d95667b439e3d97b08efeaf08022348546a4f385) )
	
		ROM_REGION( 0x8000, REGION_GFX2, ROMREGION_DISPOSE );// tile set1
		ROM_LOAD_NIB_HIGH( "eb5.7h",  0x00000, 0x2000, CRC(cbef7da5),SHA1(c5fcd2341ce5b039a15116fbd85796bb5ddc4701) )
		ROM_LOAD         ( "eb5.7h",  0x02000, 0x2000, CRC(cbef7da5),SHA1(c5fcd2341ce5b039a15116fbd85796bb5ddc4701) )
		ROM_LOAD         ( "eb1.7f",  0x04000, 0x2000, CRC(9a236583),SHA1(fcc4da2efe904f0178bd83fdee25d4752b9cc5ce) )
		ROM_LOAD         ( "eb2.8f",  0x06000, 0x2000, CRC(f0fb6355),SHA1(3c4c009f80e648d02767b29bb8d18f4de7b26d4e) )
	
		ROM_REGION( 0x8000, REGION_GFX3, ROMREGION_DISPOSE );// tile set2
		ROM_LOAD_NIB_HIGH( "eb6.8h",  0x00000, 0x2000, CRC(1e5e5475),SHA1(80ebe9326c628685faafb259f956a98ac435c809) )
		ROM_LOAD         ( "eb6.8h",  0x02000, 0x2000, CRC(1e5e5475),SHA1(80ebe9326c628685faafb259f956a98ac435c809) )
		ROM_LOAD         ( "eb3.10f", 0x04000, 0x2000, CRC(dbd0044b),SHA1(5611517bb0f54bfb0585eeca8af21fbfc2f65b2c) )
		ROM_LOAD         ( "eb4.11f", 0x06000, 0x2000, CRC(f8f8e600),SHA1(c7c97e4dc1f7a73694c98b2b1a3d7fa9f3317a2a) )
	
		ROM_REGION( 0x8000, REGION_GFX4, ROMREGION_DISPOSE );// sprite set1
		ROM_LOAD_NIB_HIGH( "es5.5h",     0x00000, 0x2000, CRC(d5b82e6a),SHA1(956a1413426e53f8a735260e660805b04016ca8d) )
		ROM_LOAD         ( "es5.5h",     0x02000, 0x2000, CRC(d5b82e6a),SHA1(956a1413426e53f8a735260e660805b04016ca8d) )
		ROM_LOAD         ( "es1.5f",     0x04000, 0x2000, CRC(cf81a2cd),SHA1(a1b45451cafeaceabe3dfe24eb073098a33ab22b) )
		ROM_LOAD         ( "es2.4f",     0x06000, 0x2000, CRC(ae3111d8),SHA1(d63633b531339fa04af757f42e956b8eb1debc4e) )
	
		ROM_REGION( 0x8000, REGION_GFX5, ROMREGION_DISPOSE );// sprite set2
		ROM_LOAD_NIB_HIGH( "es6.4h",     0x00000, 0x2000, CRC(cb4f5da9),SHA1(5af8f6aca0a3bb6417430e0179ec97c33d4014e3) )
		ROM_LOAD         ( "es6.4h",     0x02000, 0x2000, CRC(cb4f5da9),SHA1(5af8f6aca0a3bb6417430e0179ec97c33d4014e3) )
		ROM_LOAD         ( "es3.2f",     0x04000, 0x2000, CRC(3d44f815),SHA1(1835aef280a6915acbf7cad771d65bf1074f0f98) )
		ROM_LOAD         ( "es4.1f",     0x06000, 0x2000, CRC(16e6d18a),SHA1(44f9045ad034808070cd6497a3b94c3d8cc93262) )
	
		ROM_REGION( 0x0300, REGION_PROMS, 0 );// RGB PROMs
		ROM_LOAD( "bprom.3a",  0x00000, 0x100, CRC(2fcdf217),SHA1(4acf67d37e844c2773028ecffe72a66754ed5bca) ) // R
		ROM_LOAD( "bprom.1a",  0x00100, 0x100, CRC(d7e6cd1f),SHA1(ce330e43ba8a97ab79040c053a25e46e8fe60bdb) ) // G
		ROM_LOAD( "bprom.2a",  0x00200, 0x100, CRC(e3d106e8),SHA1(6b153eb8140d36b4d194e26106a5ba5bffd1a851) ) // B
	
		ROM_REGION( 0x0100, REGION_USER1, 0 );// CLUT(same PROM x 4)
		ROM_LOAD( "bprom.6b",  0x0000, 0x100, CRC(6294cddf),SHA1(c7a2854f62e31032df2b07fae3fb3b51ac6daac2) )
		ROM_LOAD( "bprom.7b",  0x0000, 0x100, CRC(6294cddf),SHA1(c7a2854f62e31032df2b07fae3fb3b51ac6daac2) )
		ROM_LOAD( "bprom.9b",  0x0000, 0x100, CRC(6294cddf),SHA1(c7a2854f62e31032df2b07fae3fb3b51ac6daac2) )
		ROM_LOAD( "bprom.10b", 0x0000, 0x100, CRC(6294cddf),SHA1(c7a2854f62e31032df2b07fae3fb3b51ac6daac2) )
	
		ROM_REGION( 0x0020, REGION_USER2, 0 );// MSM5232 PROM?
		ROM_LOAD( "bprom.3h",  0x00000, 0x020, CRC(33b98466),SHA1(017c73cf8c17dc5047c89316ae5b45f8d22092e8) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_equitess = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );// 68000 ROMs
		ROM_LOAD16_BYTE( "epr-ep1.13d", 0x00001, 0x2000, CRC(c6edf1cd),SHA1(21dba62e692f4fdc79155ce169a48ae827bd5994) )
		ROM_LOAD16_BYTE( "epr-ep5.13b", 0x00000, 0x2000, CRC(c11f0759),SHA1(5caf2b6b777b2fdabc26ea232225be2d789e87f3) )
		ROM_LOAD16_BYTE( "epr-ep2.12d", 0x04001, 0x2000, CRC(0c1bc2e7),SHA1(4c3510dfeee2fb2f295a32e2fe2021c4c7f08e8a) )
		ROM_LOAD16_BYTE( "epr-ep6.12b", 0x04000, 0x2000, CRC(bbed3dcc),SHA1(46ef2c60ccfa76a187b19dc0b7e6c594050b183f) )
		ROM_LOAD16_BYTE( "epr-ep3.10d", 0x08001, 0x2000, CRC(5f2d059a),SHA1(03fe904a445cce89462788fecfd61ac53f4dd17f) )
		ROM_LOAD16_BYTE( "epr-ep7.10b", 0x08000, 0x2000, CRC(a8f6b8aa),SHA1(ee4edb54c147a95944482e998616b025642a268a) )
		ROM_LOAD16_BYTE( "epr-ep4.9d",  0x0c001, 0x2000, CRC(956a06bd),SHA1(a716f9aaf0c32c522968f4ff13de904d6e8c7f98) )
		ROM_LOAD16_BYTE( "epr-ep8.9b",  0x0c000, 0x2000, CRC(4c78d60d),SHA1(207a82779e2fe3e9082f4fa09b87c713a51167e6) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );// Z80 ROMs
		ROM_LOAD( "ev1.1m", 0x00000, 0x2000, CRC(43faaf2e),SHA1(c9aaf298d673eb70399366776474f1b242549eb4) )
		ROM_LOAD( "ev2.1l", 0x02000, 0x2000, CRC(09e6702d),SHA1(896771f73a486e5035909eeed9ef48103d81d4ae) )
		ROM_LOAD( "ev3.1k", 0x04000, 0x2000, CRC(10ff140b),SHA1(7c28f988a9c8b2a702d007096199e67b447a183c) )
		ROM_LOAD( "ev4.1h", 0x06000, 0x2000, CRC(b7917264),SHA1(e58345fda088b171fd348959de15082f3cb42514) )
	
		ROM_REGION( 0x1000, REGION_GFX1, ROMREGION_DISPOSE );// chars
		ROM_LOAD( "epr-ep0.3e",  0x00000, 0x1000, CRC(3f5a81c3),SHA1(8fd5bc621f483bfa46be7e40e6480b25243bdf70) )
	
		ROM_REGION( 0x8000, REGION_GFX2, ROMREGION_DISPOSE );// tile set1
		ROM_LOAD_NIB_HIGH( "eb5.7h",  0x00000, 0x2000, CRC(cbef7da5),SHA1(c5fcd2341ce5b039a15116fbd85796bb5ddc4701) )
		ROM_LOAD         ( "eb5.7h",  0x02000, 0x2000, CRC(cbef7da5),SHA1(c5fcd2341ce5b039a15116fbd85796bb5ddc4701) )
		ROM_LOAD         ( "eb1.7f",  0x04000, 0x2000, CRC(9a236583),SHA1(fcc4da2efe904f0178bd83fdee25d4752b9cc5ce) )
		ROM_LOAD         ( "eb2.8f",  0x06000, 0x2000, CRC(f0fb6355),SHA1(3c4c009f80e648d02767b29bb8d18f4de7b26d4e) )
	
		ROM_REGION( 0x8000, REGION_GFX3, ROMREGION_DISPOSE );// tile set2
		ROM_LOAD_NIB_HIGH( "eb6.8h",  0x00000, 0x2000, CRC(1e5e5475),SHA1(80ebe9326c628685faafb259f956a98ac435c809) )
		ROM_LOAD         ( "eb6.8h",  0x02000, 0x2000, CRC(1e5e5475),SHA1(80ebe9326c628685faafb259f956a98ac435c809) )
		ROM_LOAD         ( "eb3.10f", 0x04000, 0x2000, CRC(dbd0044b),SHA1(5611517bb0f54bfb0585eeca8af21fbfc2f65b2c) )
		ROM_LOAD         ( "eb4.11f", 0x06000, 0x2000, CRC(f8f8e600),SHA1(c7c97e4dc1f7a73694c98b2b1a3d7fa9f3317a2a) )
	
		ROM_REGION( 0x8000, REGION_GFX4, ROMREGION_DISPOSE );// sprite set1
		ROM_LOAD_NIB_HIGH( "es5.5h",     0x00000, 0x2000, CRC(d5b82e6a),SHA1(956a1413426e53f8a735260e660805b04016ca8d) )
		ROM_LOAD         ( "es5.5h",     0x02000, 0x2000, CRC(d5b82e6a),SHA1(956a1413426e53f8a735260e660805b04016ca8d) )
		ROM_LOAD         ( "es1.5f",     0x04000, 0x2000, CRC(cf81a2cd),SHA1(a1b45451cafeaceabe3dfe24eb073098a33ab22b) )
		ROM_LOAD         ( "es2.4f",     0x06000, 0x2000, CRC(ae3111d8),SHA1(d63633b531339fa04af757f42e956b8eb1debc4e) )
	
		ROM_REGION( 0x8000, REGION_GFX5, ROMREGION_DISPOSE );// sprite set2
		ROM_LOAD_NIB_HIGH( "es6.4h",     0x00000, 0x2000, CRC(cb4f5da9),SHA1(5af8f6aca0a3bb6417430e0179ec97c33d4014e3) )
		ROM_LOAD         ( "es6.4h",     0x02000, 0x2000, CRC(cb4f5da9),SHA1(5af8f6aca0a3bb6417430e0179ec97c33d4014e3) )
		ROM_LOAD         ( "es3.2f",     0x04000, 0x2000, CRC(3d44f815),SHA1(1835aef280a6915acbf7cad771d65bf1074f0f98) )
		ROM_LOAD         ( "es4.1f",     0x06000, 0x2000, CRC(16e6d18a),SHA1(44f9045ad034808070cd6497a3b94c3d8cc93262) )
	
		ROM_REGION( 0x0300, REGION_PROMS, 0 );// RGB PROMs
		ROM_LOAD( "bprom.3a",  0x00000, 0x100, CRC(2fcdf217),SHA1(4acf67d37e844c2773028ecffe72a66754ed5bca) ) // R
		ROM_LOAD( "bprom.1a",  0x00100, 0x100, CRC(d7e6cd1f),SHA1(ce330e43ba8a97ab79040c053a25e46e8fe60bdb) ) // G
		ROM_LOAD( "bprom.2a",  0x00200, 0x100, CRC(e3d106e8),SHA1(6b153eb8140d36b4d194e26106a5ba5bffd1a851) ) // B
	
		ROM_REGION( 0x0100, REGION_USER1, 0 );// CLUT(same PROM x 4)
		ROM_LOAD( "bprom.6b",  0x0000, 0x100, CRC(6294cddf),SHA1(c7a2854f62e31032df2b07fae3fb3b51ac6daac2) )
		ROM_LOAD( "bprom.7b",  0x0000, 0x100, CRC(6294cddf),SHA1(c7a2854f62e31032df2b07fae3fb3b51ac6daac2) )
		ROM_LOAD( "bprom.9b",  0x0000, 0x100, CRC(6294cddf),SHA1(c7a2854f62e31032df2b07fae3fb3b51ac6daac2) )
		ROM_LOAD( "bprom.10b", 0x0000, 0x100, CRC(6294cddf),SHA1(c7a2854f62e31032df2b07fae3fb3b51ac6daac2) )
	
		ROM_REGION( 0x0020, REGION_USER2, 0 );// MSM5232 PROM?
		ROM_LOAD( "bprom.3h",  0x00000, 0x020, CRC(33b98466),SHA1(017c73cf8c17dc5047c89316ae5b45f8d22092e8) )
	ROM_END(); }}; 
	
	/******************************************************************************/
	// Koukouyakyuh ROM Map
	
	static RomLoadPtr rom_kouyakyu = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 ); // 68000 ROMs
		ROM_LOAD16_BYTE( "epr-6704.bin", 0x00001, 0x2000, CRC(c7ac2292),SHA1(614bfb0949620d4c260768f14a116b076dd38438) )
		ROM_LOAD16_BYTE( "epr-6707.bin", 0x00000, 0x2000, CRC(9cb2962e),SHA1(bd1bcbc53a3346e22789f24a35ab3aa681317d02) )
		ROM_LOAD16_BYTE( "epr-6705.bin", 0x04001, 0x2000, CRC(985327cb),SHA1(86969fe763cbaa527d64de35844773b5ab1d7f83) )
		ROM_LOAD16_BYTE( "epr-6708.bin", 0x04000, 0x2000, CRC(f8863dc5),SHA1(bfdd294d51420dd70aa97942909a9b8a95ffc05c) )
		ROM_LOAD16_BYTE( "epr-6706.bin", 0x08001, 0x2000, BAD_DUMP CRC(79e94cd2),SHA1(f44c2292614b46116818fad9a7eb48cceeb3b819)  )	// was bad, manually patched
		ROM_LOAD16_BYTE( "epr-6709.bin", 0x08000, 0x2000, CRC(f41cb58c),SHA1(f0d1048e949d51432739755f985e4df65b8e918b) )
		ROM_FILL(                        0x0c000, 0x4000, 0 );
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );// Z80 ROMs
		ROM_LOAD( "epr-6703.bin", 0x00000, 0x2000, CRC(fbff3a86),SHA1(4ed2887b1e4509ded853a230f735d4d2aa475886) )
		ROM_LOAD( "epr-6702.bin", 0x02000, 0x2000, CRC(27ddf031),SHA1(2f11d3b693e46852762669ed1e35a667990edec7) )
		ROM_LOAD( "epr-6701.bin", 0x04000, 0x2000, CRC(3c83588a),SHA1(a84c813ba9d464cffc855397aaacbb9177c86fb4) )
		ROM_LOAD( "epr-6700.bin", 0x06000, 0x2000, CRC(ee579266),SHA1(94dfcf506049fc78db00084ff7031d19520d9a85) )
		ROM_LOAD( "epr-6699.bin", 0x08000, 0x2000, CRC(9bfa4a72),SHA1(8ac4d308dab0d67a26b4e3550c2e8064aaf36a74) )
		ROM_LOAD( "epr-6698.bin", 0x0a000, 0x2000, CRC(7adfd1ff),SHA1(b543dd6734a681a187dabf602bea390de663039c) )
	
		ROM_REGION( 0x1000, REGION_GFX1, ROMREGION_DISPOSE );// chars
		ROM_LOAD( "epr-6710.bin", 0x00000, 0x1000, CRC(accda190),SHA1(265d2fd92574d65e7890e48d5f305bf903a67bc8) )
	
		ROM_REGION( 0x8000, REGION_GFX2, ROMREGION_DISPOSE );// tile set1
		ROM_LOAD_NIB_HIGH( "epr-6695.bin", 0x00000, 0x2000, CRC(22bea465),SHA1(4860d7ee3c386cdacc9c608ffe74ec8bfa58edcb) )
		ROM_LOAD         ( "epr-6695.bin", 0x02000, 0x2000, CRC(22bea465),SHA1(4860d7ee3c386cdacc9c608ffe74ec8bfa58edcb) )
		ROM_LOAD         ( "epr-6689.bin", 0x04000, 0x2000, CRC(53bf7587),SHA1(0046cd04d11ce789ff69e0807700a624af96eb36) )
		ROM_LOAD         ( "epr-6688.bin", 0x06000, 0x2000, CRC(ceb76c5b),SHA1(81fa236871f10c77eb201e1c9771bd57406df15b) )
	
		ROM_REGION( 0x8000, REGION_GFX3, ROMREGION_DISPOSE );// tile set2
		ROM_LOAD_NIB_HIGH( "epr-6694.bin", 0x00000, 0x2000, CRC(51a7345e),SHA1(184c890559ed633e23cb459c313e6179cc3eb542) )
		ROM_LOAD         ( "epr-6694.bin", 0x02000, 0x2000, CRC(51a7345e),SHA1(184c890559ed633e23cb459c313e6179cc3eb542) )
		ROM_LOAD         ( "epr-6687.bin", 0x04000, 0x2000, CRC(9c1f49df),SHA1(1a5cf5278777f829d3654e838bd2bb9f4dbb57ba) )
		ROM_LOAD         ( "epr-6686.bin", 0x06000, 0x2000, CRC(3d9e516f),SHA1(498614821f87dbcc39edb1756e1af6b536044e6a) )
	
		ROM_REGION( 0x8000, REGION_GFX4, ROMREGION_DISPOSE );// sprite set1
		ROM_LOAD_NIB_HIGH( "epr-6696.bin", 0x00000, 0x2000, CRC(0625f48e),SHA1(bea09ccf37f38678fb53c55bd0a79557d6c81b3f) )
		ROM_LOAD         ( "epr-6696.bin", 0x02000, 0x2000, CRC(0625f48e),SHA1(bea09ccf37f38678fb53c55bd0a79557d6c81b3f) )
		ROM_LOAD         ( "epr-6690.bin", 0x04000, 0x2000, CRC(a142a11d),SHA1(209c7e0591622434ada4445f3f8789059c5f4f77) )
		ROM_LOAD         ( "epr-6691.bin", 0x06000, 0x2000, CRC(b640568c),SHA1(8cef1387c469abec8b488621a94cc9575d6c5fcc) )
	
		ROM_REGION( 0x8000, REGION_GFX5, ROMREGION_DISPOSE );// sprite set2
		ROM_LOAD_NIB_HIGH( "epr-6697.bin", 0x00000, 0x2000, CRC(f18afabe),SHA1(abd7f6c0bd0de145c423166a2f4e86ccdb12b1ce) )
		ROM_LOAD         ( "epr-6697.bin", 0x02000, 0x2000, CRC(f18afabe),SHA1(abd7f6c0bd0de145c423166a2f4e86ccdb12b1ce) )
		ROM_LOAD         ( "epr-6692.bin", 0x04000, 0x2000, CRC(b91d8172),SHA1(8d8f6ea78ebf652f295ce96abf19e628fe777d07) )
		ROM_LOAD         ( "epr-6693.bin", 0x06000, 0x2000, CRC(874e3acc),SHA1(29438f196811fc2c8f54b6c47f1c175e4797dd4c) )
	
		ROM_REGION( 0x0300, REGION_PROMS, 0 );// RGB PROMS
		ROM_LOAD( "pr6627.bpr",  0x00000, 0x100, CRC(5ec5480d),SHA1(f966a277539a5d257f32692cdd92ce44b08599e8) ) // R
		ROM_LOAD( "pr6629.bpr",  0x00100, 0x100, CRC(29c7a393),SHA1(67cced39c0a80655c420aad668dfe836c1d7c643) ) // G
		ROM_LOAD( "pr6628.bpr",  0x00200, 0x100, CRC(8af247a4),SHA1(01702fbce53dd4875e4825f0487e7aed9cf212fa) ) // B
	
		ROM_REGION( 0x0100, REGION_USER1, 0 );// CLUT(same PROM x 4)
		ROM_LOAD( "pr6630a.bpr", 0x0000, 0x100, CRC(d6e202da),SHA1(500ebd5c95d2d2c33535d25cf7f8f649897dc224) )
		ROM_LOAD( "pr6630b.bpr", 0x0000, 0x100, CRC(d6e202da),SHA1(500ebd5c95d2d2c33535d25cf7f8f649897dc224) )
		ROM_LOAD( "pr6630c.bpr", 0x0000, 0x100, CRC(d6e202da),SHA1(500ebd5c95d2d2c33535d25cf7f8f649897dc224) )
		ROM_LOAD( "pr6630d.bpr", 0x0000, 0x100, CRC(d6e202da),SHA1(500ebd5c95d2d2c33535d25cf7f8f649897dc224) )
	
		ROM_REGION( 0x0020, REGION_USER2, 0 );// MSM5232 PROM?(identical to bprom.3h in Equites)
		ROM_LOAD( "pr.bpr",      0x00000, 0x020, CRC(33b98466),SHA1(017c73cf8c17dc5047c89316ae5b45f8d22092e8) )
	ROM_END(); }}; 
	
	/******************************************************************************/
	// Bull Fighter ROM Map
	
	static RomLoadPtr rom_bullfgtr = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );// 68000 ROMs
		ROM_LOAD16_BYTE( "m_d13.bin",  0x00001, 0x2000, CRC(7c35dd4b),SHA1(6bd604ee32c0c5db17f90e24aa254ec7072d27dd) )
		ROM_LOAD16_BYTE( "m_b13.bin",  0x00000, 0x2000, CRC(c4adddce),SHA1(48b6ddbad52a3941d3e651642b26d9adf70f71f5) )
		ROM_LOAD16_BYTE( "m_d12.bin",  0x04001, 0x2000, CRC(5d51be2b),SHA1(55d2718479cb71ceefefbaf40c14285e5603e526) )
		ROM_LOAD16_BYTE( "m_b12.bin",  0x04000, 0x2000, CRC(d98390ef),SHA1(17006503325627055c8b22052d7ed94e474f4ef7) )
		ROM_LOAD16_BYTE( "m_dd10.bin", 0x08001, 0x2000, CRC(21875752),SHA1(016db4125b1a4584ae277af427370780d96a17c5) )
		ROM_LOAD16_BYTE( "m_b10.bin",  0x08000, 0x2000, CRC(9d84f678),SHA1(32584d54788cb570bd5210992836f28ba9c87aac) )
		ROM_FILL(                      0x0c000, 0x4000, 0 );
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );// Z80 ROMs
		ROM_LOAD( "s_m1.bin", 0x00000, 0x2000, CRC(2a8e6fcf),SHA1(866903408e05938a982ffef4c9b849203c6cc060) )
		ROM_LOAD( "s_l2.bin", 0x02000, 0x2000, CRC(026e1533),SHA1(6271869a3faaafacfac35262746e87a83c158b93) )
		ROM_LOAD( "s_k1.bin", 0x04000, 0x2000, CRC(51ee751c),SHA1(60bf848dfdfe313ab05df5a5c05819b0fa87ca50) )
		ROM_LOAD( "s_h1.bin", 0x06000, 0x2000, CRC(62c7a25b),SHA1(237d3cbdfbf45b33c2f65d30faba151380866a93) )
	
		ROM_REGION( 0x1000, REGION_GFX1, ROMREGION_DISPOSE );// chars
		ROM_LOAD( "m_e4.bin", 0x000000, 0x1000, CRC(c6894c9a),SHA1(0d5a55cded4fd833211bdc733a78c6c8423897de) )
	
		ROM_REGION( 0x8000, REGION_GFX2, ROMREGION_DISPOSE );// tile set1
		ROM_LOAD_NIB_HIGH( "l_h7.bin",  0x00000, 0x2000, CRC(6d05e9f2),SHA1(4b5c92b72bf73a08a2359fe889a327a696a45e8a) )
		ROM_LOAD         ( "l_h7.bin",  0x02000, 0x2000, CRC(6d05e9f2),SHA1(4b5c92b72bf73a08a2359fe889a327a696a45e8a) )
		ROM_LOAD         ( "l_f7.bin",  0x04000, 0x2000, CRC(4352d069),SHA1(bac687f050837b023da00cb53bb524b2a76310d4) )
		ROM_LOAD         ( "l_f9.bin",  0x06000, 0x2000, CRC(24edfd7d),SHA1(be8a40d8d5ccff06f37c1ab67341f56e41a5ea88) )
	
		ROM_REGION( 0x8000, REGION_GFX3, ROMREGION_DISPOSE );// tile set2
		ROM_LOAD_NIB_HIGH( "l_h9.bin",  0x00000, 0x2000, CRC(016340ae),SHA1(f980d39337c711a15520388967ca4503e7970e18) )
		ROM_LOAD         ( "l_h9.bin",  0x02000, 0x2000, CRC(016340ae),SHA1(f980d39337c711a15520388967ca4503e7970e18) )
		ROM_LOAD         ( "l_f10.bin", 0x04000, 0x2000, CRC(4947114e),SHA1(822dc3f14b71dc9e5b69078aefbed6b438aa0690) )
		ROM_LOAD         ( "l_f12.bin", 0x06000, 0x2000, CRC(fa296cb3),SHA1(2ba864766655cb3dd2999a6cdf96dcefd6818135) )
	
		ROM_REGION( 0x8000, REGION_GFX4, ROMREGION_DISPOSE );// sprite set1
		ROM_LOAD_NIB_HIGH( "l_h6.bin",  0x00000, 0x2000, CRC(48394389),SHA1(a5c6021b60226a775b2052909e8d21b5f79d9ec5) )
		ROM_LOAD         ( "l_h6.bin",  0x02000, 0x2000, CRC(48394389),SHA1(a5c6021b60226a775b2052909e8d21b5f79d9ec5) )
		ROM_LOAD         ( "l_f6.bin",  0x04000, 0x2000, CRC(7c69b473),SHA1(abc181b4e5b3f48c667a0bb4814c3818dfc6e9e2) )
		ROM_LOAD         ( "l_f4.bin",  0x06000, 0x2000, CRC(c3dc713f),SHA1(c2072cc71ea61e0c718c339bda1460d93343469e) )
	
		ROM_REGION( 0x8000, REGION_GFX5, ROMREGION_DISPOSE );// sprite set2
		ROM_LOAD_NIB_HIGH( "l_h4.bin",  0x00000, 0x2000, CRC(141409ec),SHA1(3cc542fa34fdbd71e392c7c22da5d5120642be86) )
		ROM_LOAD         ( "l_h4.bin",  0x02000, 0x2000, CRC(141409ec),SHA1(3cc542fa34fdbd71e392c7c22da5d5120642be86) )
		ROM_LOAD         ( "l_f3.bin",  0x04000, 0x2000, CRC(883f93fd),SHA1(a96df701f82e62582522953830049d29bcb3d458) )
		ROM_LOAD         ( "l_f1.bin",  0x06000, 0x2000, CRC(578ace7b),SHA1(933e85d49db7b27fd85e4713f0984612bc29e134) )
	
		// all color PROMs of current dump are bad and have wrong sizes
		ROM_REGION( 0x0300, REGION_PROMS, 0 );// RGB PROMs
		ROM_LOAD( "m_a3.bin", 0x00000, 0x100, BAD_DUMP CRC(8203ee60),SHA1(5bf5e54615c77945365c39d6ae95586ae187ebed)  ) // R (made up, dump was bad)
		ROM_LOAD( "m_a1.bin", 0x00100, 0x100, BAD_DUMP CRC(2eb1a3de),SHA1(80b0e0f882210f2119d7ee307deb856dcc288352)  ) // G (made up, dump was bad)
		ROM_LOAD( "m_a2.bin", 0x00200, 0x100, BAD_DUMP CRC(2e769d4c),SHA1(4de42319aed5073e88742c006b641efdd52cac80)  ) // B (made up, dump was bad)
	
		ROM_REGION( 0x0100, REGION_USER1, 0 );// CLUT(same PROM x 4)
		ROM_LOAD( "l_b6.bin",  0x0000, 0x100, CRC(8835a069),SHA1(bc8d4130d4fa0f16bb2511ac66b84d53218042a3) )
		ROM_LOAD( "l_b7.bin",  0x0000, 0x100, CRC(8835a069),SHA1(bc8d4130d4fa0f16bb2511ac66b84d53218042a3) )
		ROM_LOAD( "l_b9.bin",  0x0000, 0x100, CRC(8835a069),SHA1(bc8d4130d4fa0f16bb2511ac66b84d53218042a3) )
		ROM_LOAD( "l_b10.bin", 0x0000, 0x100, CRC(8835a069),SHA1(bc8d4130d4fa0f16bb2511ac66b84d53218042a3) )
	
		ROM_REGION( 0x0020, REGION_USER2, 0 );// MSM5232 PROMs?(identical to bprom.3h in Equites)
		ROM_LOAD( "s_h3.bin",  0x00000, 0x020, CRC(33b98466),SHA1(017c73cf8c17dc5047c89316ae5b45f8d22092e8) )
	ROM_END(); }}; 
	
	/******************************************************************************/
	// Splendor Blast ROM Map
	
	static RomLoadPtr rom_splndrbt = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );// 68000 ROMs(16k x 4)
		ROM_LOAD16_BYTE( "1.16a", 0x00001, 0x4000, CRC(4bf4b047),SHA1(ef0efffa2f49905e17e4ed3a03cac419793b26d1) )
		ROM_LOAD16_BYTE( "2.16c", 0x00000, 0x4000, CRC(27acb656),SHA1(5f2f8d05f2f1c6c92c8364e9e6831ca525cbacd0) )
		ROM_LOAD16_BYTE( "3.15a", 0x08001, 0x4000, CRC(5b182189),SHA1(50ebb1fddcb6838442e8a20261f200f3386ce8a8) )
		ROM_LOAD16_BYTE( "4.15c", 0x08000, 0x4000, CRC(cde99613),SHA1(250b59f75eee84442da3cc7c599d1e16f0294df9) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );// Z80 ROMs(8k x 4)
		ROM_LOAD( "1_v.1m", 0x00000, 0x2000, CRC(1b3a6e42),SHA1(41a4f0503c939ec0a739c8bc6bf3c8fc354912ee) )
		ROM_LOAD( "2_v.1l", 0x02000, 0x2000, CRC(2a618c72),SHA1(6ad459d94352c317150ae6344d4db9bb613938dd) )
		ROM_LOAD( "3_v.1k", 0x04000, 0x2000, CRC(bbee5346),SHA1(753cb784b04f081fa1f8590dc28056d9918f313b) )
		ROM_LOAD( "4_v.1h", 0x06000, 0x2000, CRC(10f45af4),SHA1(00fa599bad8bf3ba6deee54165f381403096e8f9) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE );// 512 chars(8k x 1)
		ROM_LOAD( "10.8c",  0x00000, 0x2000, CRC(501887d4),SHA1(3cf4401d6fddff1500066219a71ac3b30ecbdd28) )
	
		ROM_REGION( 0x4000, REGION_GFX2, ROMREGION_DISPOSE );// 256 tiles(16k x 1)
		ROM_LOAD( "8.14m",  0x00000, 0x4000, CRC(c2c86621),SHA1(a715c70ace98502f2c0d4a81539cd79d19e9b6c4) )
	
		ROM_REGION( 0x4000, REGION_GFX3, ROMREGION_DISPOSE );// 256 tiles(16k x 1)
		ROM_LOAD( "9.12m",  0x00000, 0x4000, CRC(4f7da6ff),SHA1(0516271df4a36d6ea38d1b8a5e471e1d2a79e8c1) )
	
		ROM_REGION( 0x10000, REGION_GFX4, ROMREGION_DISPOSE );// 256 sprites
		ROM_LOAD_NIB_HIGH( "6.18n", 0x00000, 0x2000, CRC(aa72237f),SHA1(0a26746a6c448a7fb853ef708e2bdeb76edd99cf) )
		ROM_CONTINUE     (          0x04000, 0x2000 );
		ROM_LOAD         ( "6.18n", 0x02000, 0x2000, CRC(aa72237f),SHA1(0a26746a6c448a7fb853ef708e2bdeb76edd99cf) )
		ROM_CONTINUE     (          0x06000, 0x2000 );
		ROM_LOAD         ( "5.18m", 0x08000, 0x4000, CRC(5f618b39),SHA1(2891067e71b8e1183ee5741487faa1561316cade) )
		ROM_LOAD         ( "7.17m", 0x0c000, 0x4000, CRC(abdd8483),SHA1(df8c8338c24fa487c49b01ce26db7eb28c8c6b85) )
	
		ROM_REGION( 0x0300, REGION_PROMS, 0 );// RGB PROMs
		ROM_LOAD( "r.3a",   0x00000, 0x100, CRC(ca1f08ce),SHA1(e46e2850d3ee3c8cbb23c10645f07d406c7ff50b) ) // R
		ROM_LOAD( "g.1a",   0x00100, 0x100, CRC(66f89177),SHA1(caa51c1bf071764d5089487342794cbf023136c0) ) // G
		ROM_LOAD( "b.2a",   0x00200, 0x100, CRC(d14318bc),SHA1(e219963b3e40eb246e608fbe10daa85dbb4c1226) ) // B
	
		ROM_REGION( 0x0500, REGION_USER1, 0 );// CLUT(256bytes x 5)
		ROM_LOAD( "2.8k",   0x00000, 0x100, CRC(e1770ad3),SHA1(e408b175b8fff934e07b0ded1ee21d7f91a9523d) )
		ROM_LOAD( "s5.15p", 0x00100, 0x100, CRC(7f6cf709),SHA1(5938faf937b682dcc83e53444cbf5e0bd7741363) )
		ROM_LOAD( "s3.8l",  0x00200, 0x100, CRC(1314b0b5),SHA1(31ef4b916110581390afc1ba90c5dca7c08c619f) ) // unknown
		ROM_LOAD( "1.9j",   0x00300, 0x100, CRC(f5b9b777),SHA1(a4ec731be77306db6baf319391c4fe78517fe43e) ) // unknown
		ROM_LOAD( "4.7m",   0x00400, 0x100, CRC(12cbcd2c),SHA1(a7946820bbf3f7e110a328b673123988af97ce7e) ) // unknown
	
		ROM_REGION( 0x2000, REGION_USER2, 0 );// unknown(8k x 1)
		ROM_LOAD( "0.8h",   0x00000, 0x2000, CRC(12681fb5),SHA1(7a0930819d4cd00475d1897128daa6ac865e07d0) )
	
		ROM_REGION( 0x0020, REGION_USER3, 0 );// MSM5232 PROMs?(identical to bprom.3h in Equites)
		ROM_LOAD( "3h.bpr", 0x00000, 0x020, CRC(33b98466),SHA1(017c73cf8c17dc5047c89316ae5b45f8d22092e8) )
	ROM_END(); }}; 
	
	/******************************************************************************/
	// High Voltage ROM Map
	
	static RomLoadPtr rom_hvoltage = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );// 68000 ROMs(16k x 4)
		ROM_LOAD16_BYTE( "1.16a", 0x00001, 0x4000, CRC(82606e3b),SHA1(25c3172928d8f1eda2c4c757d505fdfd91f21ea1) )
		ROM_LOAD16_BYTE( "2.16c", 0x00000, 0x4000, CRC(1d74fef2),SHA1(3df3dc98a78a137da8c5cddf6a8519b477824fb9) )
		ROM_LOAD16_BYTE( "3.15a", 0x08001, 0x4000, CRC(677abe14),SHA1(78b343122f9ad187c823bf49e8f001288c762586) )
		ROM_LOAD16_BYTE( "4.15c", 0x08000, 0x4000, CRC(8aab5a20),SHA1(fb90817173ad69c0e00d03814b4e10b18955c07e) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );// Z80 ROMs(8k x 3)
		ROM_LOAD( "5_v.1l", 0x00000, 0x4000, CRC(ed9bb6ea),SHA1(73b0251b86835368ec2a4e98a5f61e28e58fd234) )
		ROM_LOAD( "6_v.1h", 0x04000, 0x4000, CRC(e9542211),SHA1(482f2c90e842fe5cc31cc6a39025adf65ba47ce9) )
		ROM_LOAD( "7_v.1e", 0x08000, 0x4000, CRC(44d38554),SHA1(6765971376eafa218fda1accb1e173a7c1850cc8) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE );// 512 chars(8k x 1)
		ROM_LOAD( "5.8c",   0x00000, 0x2000, CRC(656d53cd),SHA1(9971ed7e7da0e8bf46e97e8f75a2c2201b33fc2f) )
	
		ROM_REGION( 0x4000, REGION_GFX2, ROMREGION_DISPOSE );// 256 tiles(16k x 1)
		ROM_LOAD( "9.14m",  0x00000, 0x4000, CRC(506a0989),SHA1(0e7f2c9bab5e83f06a8148f69d8d0cbfe7d55c5e) )
	
		ROM_REGION( 0x4000, REGION_GFX3, ROMREGION_DISPOSE );// 256 tiles(16k x 1)
		ROM_LOAD( "10.12m", 0x00000, 0x4000, CRC(98f87d4f),SHA1(94a7a14b0905597993595b347102436d97fc1dc9) )
	
		ROM_REGION( 0x10000, REGION_GFX4, ROMREGION_DISPOSE );// 256 sprites
		ROM_LOAD_NIB_HIGH( "8.18n", 0x00000, 0x2000, CRC(725acae5),SHA1(ba54598a087f8bb5fa7182b0e85d0e038003e622) )
		ROM_CONTINUE     (          0x04000, 0x2000 );
		ROM_LOAD         ( "8.18n", 0x02000, 0x2000, CRC(725acae5),SHA1(ba54598a087f8bb5fa7182b0e85d0e038003e622) )
		ROM_CONTINUE     (          0x06000, 0x2000 );
		ROM_LOAD         ( "6.18m", 0x08000, 0x4000, CRC(9baf2c68),SHA1(208e5ac8eb157d4bf949ab4330827da032a04235) )
		ROM_LOAD         ( "7.17m", 0x0c000, 0x4000, CRC(12d25fb1),SHA1(99f5d68bd6d6ee5f2acb7685aceacfb0894c4961) )
	
		ROM_REGION( 0x0300, REGION_PROMS, 0 );// RGB PROMs
		ROM_LOAD( "r.3a",   0x00000, 0x100, CRC(98eccbf6),SHA1(a55755e8388d3edf3020b1129a638fe1e99362b6) ) // R
		ROM_LOAD( "g.1a",   0x00100, 0x100, CRC(fab2ed23),SHA1(6f63b6a3196dda76eb9a885b17d886a14365f922) ) // G
		ROM_LOAD( "b.2a",   0x00200, 0x100, CRC(7274961b),SHA1(d13070060e216d633675a528cf0dc3de94c95ffb) ) // B
	
		ROM_REGION( 0x0500, REGION_USER1, 0 );// CLUT(256bytes x 5)
		ROM_LOAD( "2.8k",   0x00000, 0x100, CRC(685f4e44),SHA1(110cb8f5a37f22ce9d391bd0cd46dcbb8fcf66b8) )
		ROM_LOAD( "s5.15p", 0x00100, 0x100, CRC(b09bcc73),SHA1(f8139feaa9563324b69aeac5c17beccfdbfa0864) )
		ROM_LOAD( "3.8l",   0x00200, 0x100, CRC(1314b0b5),SHA1(31ef4b916110581390afc1ba90c5dca7c08c619f) ) // unknown(identical to s3.8l in Splendor Blast)
		ROM_LOAD( "1.9j",   0x00300, 0x100, CRC(f5b9b777),SHA1(a4ec731be77306db6baf319391c4fe78517fe43e) ) // unknown(identical to 1.9j in Splendor Blast)
		ROM_LOAD( "4.7m",   0x00400, 0x100, CRC(12cbcd2c),SHA1(a7946820bbf3f7e110a328b673123988af97ce7e) ) // unknown(identical to 4.7m in Splendor Blast)
	
		ROM_REGION( 0x2000, REGION_USER2, 0 );// unknown(8k x 1, identical to 0.8h in Splendor Blast )
		ROM_LOAD( "0.8h",   0x00000, 0x2000, CRC(12681fb5),SHA1(7a0930819d4cd00475d1897128daa6ac865e07d0) )
	
		ROM_REGION( 0x0020, REGION_USER3, 0 );// MSM5232 PROMs?(identical to bprom.3h in Equites)
		ROM_LOAD( "3h.bpr", 0x00000, 0x020, CRC(33b98466),SHA1(017c73cf8c17dc5047c89316ae5b45f8d22092e8) )
	ROM_END(); }}; 
	
	/******************************************************************************/
	// Initializations
	
	static void equites_init_common(void)
	{
		equites_flip = 0;
	
		equites_8404init();
	}
	
	static void splndrbt_init_common(void)
	{
		equites_8404init();
	}
	
	static DRIVER_INIT( equites )
	{
		equites_id = 0x8400;
	
		equites_init_common();
	
		equites_8404rule(0x094a, 0x007, 0x04); // 8404 test
		equites_8404rule(0x094e, 0x049, 0x00); // 8404 test
	
		equites_8404rule(0x0c76, 0x00b, 0x04); // death
		equites_8404rule(0x0c7e, 0x0cf, 0x0c); // 2nd jmp hi
		equites_8404rule(0x0c84, 0x0d1, 0xae); // 2nd jmp lo
		equites_8404rule(0x0c8c, 0x0d3, 0x0c); // 1st jmp hi
		equites_8404rule(0x0c92, 0x0d5, 0x9c); // 1st jmp lo
	
		equites_8404rule(0x92a6, 0x00f, 0x04); // respawn
		equites_8404rule(0x92b0, 0x27d,-0x40); // 2nd jmp hi
		equites_8404rule(0x92b6, 0x27f,-0x04); // 2nd jmp lo
		equites_8404rule(0x92b0, 0x281,-0x50); // 1st jmp hi
		equites_8404rule(0x92b6, 0x283,-0x05); // 1st jmp lo
	
		equites_8404rule(0x8f06, 0x013, 0x04); // ENT
		equites_8404rule(0x8f0c, 0x481,-0x10); // ENT jmpaddr hi
		equites_8404rule(0x8f12, 0x483,-0x01); // ENT jmpaddr lo
	
		equites_8404rule(0x915e, 0x017, 0x04); // EXT
		equites_8404rule(0x9164, 0x47d,-0x20); // scroll y
		equites_8404rule(0x916a, 0x47f,-0x02); // player y
		equites_8404rule(0x9170, 0x481,-0x30); // exit location hi
		equites_8404rule(0x9176, 0x483,-0x03); // exit location lo
	}
	
	static DRIVER_INIT( bullfgtr )
	{
		equites_id = 0x8401;
	
		equites_init_common();
	
		equites_8404rule(0x0e7a, 0x601, 0x00); // boot up
		equites_8404rule(0x3da4, 0x201, 0x0c); // goal in
	}
	
	static DRIVER_INIT( kouyakyu )
	{
		equites_id = 0x8500;
	
		equites_init_common();
	
		equites_8404rule(0x5582, 0x603, 0x05); // home run
	}
	
	static DRIVER_INIT( splndrbt )
	{
		equites_id = 0x8510;
	
		splndrbt_init_common();
	
		equites_8404rule(0x06f8, 0x007, 0x04); // 8404 test
		equites_8404rule(0x06fc, 0x049, 0x00); // 8404 test
	
		equites_8404rule(0x12dc, 0x01b, 0x04); // guard point
		equites_8404rule(0x12e4, 0x01f, 0x04); // guard point
	
		equites_8404rule(0x0dc2, 0x00b, 0x04); // guard point (start race)
		equites_8404rule(0x0dd4, 0x5e1, 0x00); // no. of addresses to look up - 1
		equites_8404rule(0x0dd8, 0x5e3, 0x0c); // race start/respawn addr hi
		equites_8404rule(0x0dde, 0x5e5, 0x32); // race start/respawn addr lo
	
		equites_8404rule(0x1268, 0x023, 0x04); // guard point
	
		equites_8404rule(0x1298, 0x25f,-0x0c); // stage number?
	
		// game params. (180261-18027f)->(40060-4006f)
		equites_8404rule(0x12a0, 0x261, 0x0a); // max speed hi
		equites_8404rule(0x12a0, 0x263, 0x00); // max speed lo
		equites_8404rule(0x12a0, 0x265, 0x00); // accel hi
		equites_8404rule(0x12a0, 0x267, 0x10); // accel lo
		equites_8404rule(0x12a0, 0x269, 0x0c); // max turbo speed hi
		equites_8404rule(0x12a0, 0x26b, 0x00); // max turbo speed lo
		equites_8404rule(0x12a0, 0x26d, 0x00); // turbo accel hi
		equites_8404rule(0x12a0, 0x26f, 0x20); // turbo accel lo
		equites_8404rule(0x12a0, 0x271,-0x09); // random enemy spwan list
		equites_8404rule(0x12a0, 0x273,-0x09); // .
		equites_8404rule(0x12a0, 0x275,-0x09); // .
		equites_8404rule(0x12a0, 0x277,-0x09); // .
		equites_8404rule(0x12a0, 0x279,-0x09); // .
		equites_8404rule(0x12a0, 0x27b,-0x09); // .
		equites_8404rule(0x12a0, 0x27d,-0x09); // .
		equites_8404rule(0x12a0, 0x27f,-0x09); // .
	
		equites_8404rule(0x500e, 0x281,-0x08); // power-up's (random?)
		equites_8404rule(0x500e, 0x283,-0x08); // power-up's (random?)
	
		equites_8404rule(0x132e, 0x285,-0xa0); // object spawn table addr hi
		equites_8404rule(0x1334, 0x287,-0x0a); // object spawn table addr lo
	
		equites_8404rule(0x739a, 0x289,-0xb0); // level section table addr hi
		equites_8404rule(0x73a0, 0x28b,-0x0b); // level section table addr lo
	
		equites_8404rule(0x0912, 0x017, 0x04); // guard point
		equites_8404rule(0x0b4c, 0x013, 0x04); // guard point
	
		equites_8404rule(0x0bfc, 0x00f, 0x04); // guard point (miss/no gas/end level)
		equites_8404rule(0x0c08, 0x5e1, 0x05); // no. of addresses to look up - 1
		equites_8404rule(0x0c0c, 0x5e3,-0x70); // game over/respawn addr hi
		equites_8404rule(0x0c12, 0x5e5,-0x07); // game over/respawn addr lo
		equites_8404rule(0x0c0c, 0x5e7,-0x70); // game over/respawn addr hi
		equites_8404rule(0x0c12, 0x5e9,-0x07); // game over/respawn addr lo
		equites_8404rule(0x0c0c, 0x5eb,-0x70); // game over/respawn addr hi
		equites_8404rule(0x0c12, 0x5ed,-0x07); // game over/respawn addr lo
		equites_8404rule(0x0c0c, 0x5ef,-0x70); // game over/respawn addr hi
		equites_8404rule(0x0c12, 0x5f1,-0x07); // game over/respawn addr lo
		equites_8404rule(0x0c0c, 0x5f3,-0x70); // game over/respawn addr hi
		equites_8404rule(0x0c12, 0x5f5,-0x07); // game over/respawn addr lo
		equites_8404rule(0x0c0c, 0x5f7,-0x70); // game over/respawn addr hi
		equites_8404rule(0x0c12, 0x5f9,-0x07); // game over/respawn addr lo
	}
	
	static DRIVER_INIT( hvoltage )
	{
		int i;
	
	#if HVOLTAGE_HACK
		install_mem_read16_handler(0, 0x000038, 0x000039, hvoltage_debug_r);
	#endif
	
		equites_id = 0x8511;
	
		splndrbt_init_common();
	
		equites_8404rule(0x0b18, 0x007, 0x04); // 8404 test
		equites_8404rule(0x0b1c, 0x049, 0x00); // 8404 test
	
		for(i=0x07; i<0x47; i+=4) equites_8404rule(0x0c64, i, 0xff); // checksum
	
		equites_8404rule(0x0df6, 0x00f, 0x04); // 1st gateway (init)
		equites_8404rule(0x0e02, 0x247, 0x01); // no. of addresses to look up - 1
		equites_8404rule(0x0e06, 0x249, 0x10); // addr hi
		equites_8404rule(0x0e0c, 0x24b, 0x12); // addr lo
		equites_8404rule(0x0e06, 0x24d, 0x19); // addr hi
		equites_8404rule(0x0e0c, 0x24f, 0x96); // addr lo
	
		equites_8404rule(0x10fc, 0x017, 0x04); // 2nd gateway (intro)
		equites_8404rule(0x111e, 0x6a5, 0x00); // no. of addresses to look up - 1
		equites_8404rule(0x1122, 0x6a7, 0x11); // addr hi
		equites_8404rule(0x1128, 0x6a9, 0xa4); // addr lo
	
		equites_8404rule(0x0f86, 0x013, 0x04); // 3rd gateway (miss)
		equites_8404rule(0x0f92, 0x491, 0x03); // no. of addresses to look up - 1
		equites_8404rule(0x0f96, 0x493,-0x60); // addr hi
		equites_8404rule(0x0f9c, 0x495,-0x06); // addr lo
		equites_8404rule(0x0f96, 0x497,-0x60); // addr hi
		equites_8404rule(0x0f9c, 0x499,-0x06); // addr lo
		equites_8404rule(0x0f96, 0x49b,-0x60); // addr hi
		equites_8404rule(0x0f9c, 0x49d,-0x06); // addr lo
		equites_8404rule(0x0f96, 0x49f,-0x60); // addr hi
		equites_8404rule(0x0f9c, 0x4a1,-0x06); // addr lo
	}
	
	/******************************************************************************/
	
	// Game Entries
	
	// Equites Hardware
	public static GameDriver driver_equites	   = new GameDriver("1984"	,"equites"	,"equites.java"	,rom_equites,null	,machine_driver_equites	,input_ports_equites	,init_equites	,ROT90	,	"Alpha Denshi Co.",                "Equites", GAME_UNEMULATED_PROTECTION | GAME_NO_COCKTAIL )
	public static GameDriver driver_equitess	   = new GameDriver("1984"	,"equitess"	,"equites.java"	,rom_equitess,driver_equites	,machine_driver_equites	,input_ports_equites	,init_equites	,ROT90	,	"Alpha Denshi Co. (Sega license)", "Equites (Sega)", GAME_UNEMULATED_PROTECTION | GAME_NO_COCKTAIL )
	public static GameDriver driver_bullfgtr	   = new GameDriver("1984"	,"bullfgtr"	,"equites.java"	,rom_bullfgtr,null	,machine_driver_equites	,input_ports_bullfgtr	,init_bullfgtr	,ROT90	,	"Alpha Denshi Co. (Sega license)", "Bull Fighter", GAME_UNEMULATED_PROTECTION | GAME_WRONG_COLORS )
	public static GameDriver driver_kouyakyu	   = new GameDriver("1985"	,"kouyakyu"	,"equites.java"	,rom_kouyakyu,null	,machine_driver_equites	,input_ports_kouyakyu	,init_kouyakyu	,ROT0	,	"Alpha Denshi Co.",                "The Koukouyakyuh", GAME_UNEMULATED_PROTECTION )
	
	// Splendor Blast Hardware
	public static GameDriver driver_splndrbt	   = new GameDriver("1985"	,"splndrbt"	,"equites.java"	,rom_splndrbt,null	,machine_driver_splndrbt	,input_ports_splndrbt	,init_splndrbt	,ROT0	,	"Alpha Denshi Co.", "Splendor Blast", GAME_UNEMULATED_PROTECTION | GAME_NO_COCKTAIL )
	public static GameDriver driver_hvoltage	   = new GameDriver("1985"	,"hvoltage"	,"equites.java"	,rom_hvoltage,null	,machine_driver_splndrbt	,input_ports_hvoltage	,init_hvoltage	,ROT0	,	"Alpha Denshi Co.", "High Voltage", GAME_UNEMULATED_PROTECTION )
	
	/******************************************************************************/
}
