/********************************************************************

Urashima Mahjong         UPL        68000 <unknown cpu> OKIM6295
Task Force Harrier       UPL        68000 Z80           YM2203 2xOKIM6295
Mustang                  UPL        68000 NMK004        YM2203 2xOKIM6295
Mustang (bootleg)        UPL        68000 Z80           YM3812 OKIM6295
Bio-ship Paladin         UPL        68000 <unknown cpu> YM2203(?) 2xOKIM6295
Vandyke                  UPL        68000 NMK004        YM2203 2xOKIM6295
Black Heart              UPL        68000 NMK004        YM2203 2xOKIM6295
Acrobat Mission          UPL        68000 NMK004        YM2203 2xOKIM6295
Strahl                   UPL        68000 <unknown cpu> YM2203 2xOKIM6295
Thunder Dragon           NMK/Tecmo  68000 <unknown cpu> YM2203 2xOKIM6295
Thunder Dragon (bootleg) NMK/Tecmo  68000 Z80           YM3812 OKIM6295
Hacha Mecha Fighter      NMK        68000 <unknown cpu> YM2203 2xOKIM6295
Macross                  Banpresto  68000 <unknown cpu> YM2203 2xOKIM6295
GunNail                  NMK/Tecmo  68000 NMK004        YM2203 2xOKIM6295
Macross II               Banpresto  68000 Z80           YM2203 2xOKIM6295
Thunder Dragon 2         NMK        68000 Z80           YM2203 2xOKIM6295
Saboten Bombers          NMK/Tecmo  68000               2xOKIM6295
Bombjack Twin            NMK        68000               2xOKIM6295
Nouryoku Koujou Iinkai   Tecmo      68000               2xOKIM6295
Many Block               Bee-Oh     68000 Z80           YM2203 2xOKIM6295
S.S. Mission             Comad      68000 Z80           OKIM6295
Rapid Hero				 NMK		68000 tmp90c841     YM2203 2xOKIM6295

driver by Mirko Buffoni, Richard Bush, Nicola Salmoria, Bryan McPhail,
          David Haywood, and R. Belmont.

The NMK004 CPU might be a Toshiba TLCS-90 class CPU with internal ROM in the
0000-3fff range.

The later games have an higher resolution (384x224 instead of 256x224)
but the hardware is pretty much the same. It's obvious that the higher
res is an afterthought, because the tilemap layout is weird (the left
8 screen columns have to be taken from the rightmost 8 columns of the
tilemap), and the games rely on mirror addresses to access the tilemap
sequentially.

TODO:
- Protection is patched in several games, it's the same chip as Macross/Task
  Force Harrier so it can probably be emulated.  I don't think it's an actual MCU,
  just some type of state machine (Bryan).
- Input ports in Bio-ship Paladin, Strahl
- Sound communication in Mustang might be incorrectly implemented
- Several games use an unknown (custom?) CPU to drive sound and for protection.
  In macross and gunnail it's easy to work around, the others are more complex.
  In hachamf it seems that the two CPUs share some RAM (fe000-fefff), and the
  main CPU fetches pointers from that shared RAM to do important operations
  like reading the input ports. Some of them are easily deduced checking for
  similarities in macross and bjtwin; however another protection check involves
  (see the routine at 01429a) writing data to the fe100-fe1ff range, and then
  jumping to subroutines in that range (most likely function pointers since
  each one is only 0x10 bytes long), and heaven knows what those should do.
  On startup, hachamf does a RAM test, then copies some stuff and jumps to
  RAM at 0xfef00, where it sits in a loop. Maybe the RAM is shared with the
  sound CPU, and used as a protection. We patch around that by replacing the
  reset vector with the "real" one.
- Cocktail mode is supported, but tilemap.c has problems with asymmetrical
  visible areas.
- Macross2 dip switches (the ones currently listed match macross)
- Macross2 background is wrong in level 2 at the end of the vertical scroll.
  The tilemap layout is probably different from the one I used, the dimensions
  should be correct but the page order is likely different.
- Music timing in nouryoku is a little off.
- DSW's in Tdragon2
- In Bioship, there's an occasional flicker of one of the sprites composing big
  ships. Increasing CPU speed from 12 to 16 MHz improved it, but it's still not
  100% fixed.

----

IRQ1 controls audio output and coin/joysticks reads
IRQ4 controls DSW reads and vblank.
IRQ2 points to RTE (not used).

----

mustang and hachamf test mode:

1)  Press player 2 buttons 1+2 during reset.  "Ready?" will appear
2)	Press player 1 button 2 14 (!) times

gunnail test mode:

1)  Press player 2 buttons 1+2 during reset.  "Ready?" will appear
2)	Press player 2 button 1 3 times

bjtwin test mode:

1)  Press player 2 buttons 1+2 during reset.  "Ready?" will appear
2)	Press player 1 buttons in this sequence:
	2,2,2, 1,1,1, 2,2,2, 1,1,1
	The release date of this program will appear.

Some code has to be patched out for this to work (see below). The program
remaps button 2 and 3 to button 1, so you can't enter the above sequence.

---

Questions / Notes
The 2nd cpu program roms .. anyone got any idea what cpu they run on
areas 0x0000 - 0x3fff in several of them look like they contain nothing
of value.. maybe this area wouldn't be accessable really and there is
ram there in that cpu's address space (shared with the main cpu ?)
.. maybe the roms are bitswapped like the gfx.. needs investigating
really..

'manybloc' :

  - There are writes to 0x080010.w and 0x080012.w (MCU ?) in code between
    0x005000 to 0x005690, but I see no call to "main" routine at 0x005504 !
  - There are writes to 0x08001c.w and 0x08001e.w but I can't tell what
    the effect is ! Could it be related to sound and/or interrupts ?

  - In the "test mode", press BOTH player 1 buttons to exit

  - When help is available, press BUTTON2 twice within the timer to "solve"

---

Sound notes for games with a Z80:

mustangb and tdragonb use the Seibu Raiden sound hardware and a modified
Z80 program (but the music is intact and recognizable).  See sndhrdw/seibu.c
for more info on this.

tharrier and manybloc share a sound board.  manybloc has some weird/missing
sound effects and unknown writes at F600 and F700.  Possibly these are some
sort of OKIM6295 bankswitch?  But there's only 2 256k sample ROMs, and the
6295 can address 512k on it's own?

tharrier doesn't do the extra writes and doesn't appear to have any trouble.
If someone could fix the protection it'd be fully playable with sound and music...

********************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class nmk16
{
	
	extern data16_t *nmk_bgvideoram,*nmk_fgvideoram,*nmk_txvideoram;
	extern data16_t *gunnail_scrollram;
	
	READ16_HANDLER( nmk_bgvideoram_r );
	WRITE16_HANDLER( nmk_bgvideoram_w );
	READ16_HANDLER( nmk_fgvideoram_r );
	WRITE16_HANDLER( nmk_fgvideoram_w );
	READ16_HANDLER( nmk_txvideoram_r );
	WRITE16_HANDLER( nmk_txvideoram_w );
	WRITE16_HANDLER( nmk_scroll_w );
	WRITE16_HANDLER( nmk_scroll_2_w );
	WRITE16_HANDLER( nmk_scroll_3_w );
	WRITE16_HANDLER( gunnail_scrollx_w );
	WRITE16_HANDLER( gunnail_scrolly_w );
	WRITE16_HANDLER( nmk_flipscreen_w );
	WRITE16_HANDLER( nmk_tilebank_w );
	WRITE16_HANDLER( bioship_scroll_w );
	WRITE16_HANDLER( bioship_bank_w );
	WRITE16_HANDLER( mustang_scroll_w );
	WRITE16_HANDLER( vandyke_scroll_w );
	
	VIDEO_START( macross );
	VIDEO_UPDATE( manybloc );
	VIDEO_START( gunnail );
	VIDEO_START( macross2 );
	VIDEO_START( tdragon2 );
	VIDEO_START( bjtwin );
	VIDEO_START( bioship );
	VIDEO_START( strahl );
	VIDEO_UPDATE( bioship );
	VIDEO_UPDATE( strahl );
	VIDEO_UPDATE( macross );
	VIDEO_UPDATE( gunnail );
	VIDEO_UPDATE( bjtwin );
	VIDEO_EOF( nmk );
	
	static int respcount; // used with mcu function
	
	static MACHINE_INIT( nmk16 )
	{
		respcount = 0;
	}
	
	static MACHINE_INIT( mustang_sound )
	{
		respcount = 0;
		machine_init_seibu_sound_1();
	}
	
	WRITE16_HANDLER ( ssmissin_sound_w )
	{
		/* maybe .. */
		if (ACCESSING_LSB)
		{
			soundlatch_w(0,data & 0xff);
			cpu_set_irq_line(1,0, ASSERT_LINE);
		}
	
		if (ACCESSING_MSB)
			if ((data >> 8) & 0x80)
				cpu_set_irq_line(1,0, CLEAR_LINE);
	}
	
	
	
	WRITE_HANDLER ( ssmissin_soundbank_w )
	{
		unsigned char *rom = memory_region(REGION_SOUND1);
		int bank;
	
		bank = data & 0x3;
	
		memcpy(rom + 0x20000,rom + 0x80000 + bank * 0x20000,0x20000);
	}
	
	
	static data16_t *ram;
	
	static WRITE16_HANDLER( macross_mcu_w )
	{
		logerror("%04x: mcu_w %04x\n",activecpu_get_pc(),data);
	
	
		/* since its starting to look more unlikely every day that we'll be
		   able to emulate the NMK004 sound cpu this provides some sound in
		   gunnail and macross, its entirely guesswork, sadly music can't be
		   simulated in a similar way :-( what is the NMK004 anyway, are
		   these really code roms we have, or does it have internal code of
		   its own, BioShip Paladin's 'Sound Program' Rom looks cleaner than
		   the others */
	
	
		/* Note: this doesn't play correct samples, it plays drums and stuff.
		   That's not quite right so I'm leaving it disabled */
	#if 0
		if (!strcmp(Machine->gamedrv->name,"gunnail")) {
			if (ACCESSING_LSB) {
				if ((data & 0xff) == 0x00) { /* unknown */
					/* ?? */
				} else if ((data & 0xff) == 0xcc) { /* unknown */
					/* ?? */
				} else if ((data & 0xff) == 0xd0) { /* fire normal? */
					OKIM6295_data_0_w(0, 0x80 | 0x01 );
					OKIM6295_data_0_w(0, 0x00 | 0x10 );
				} else if ((data & 0xff) == 0xd1) { /* fire purple? */
					OKIM6295_data_0_w(0, 0x80 | 0x03 );
					OKIM6295_data_0_w(0, 0x00 | 0x10 );
				} else if ((data & 0xff) == 0xd2) { /* fire red? */
					OKIM6295_data_0_w(0, 0x80 | 0x03 );
					OKIM6295_data_0_w(0, 0x00 | 0x10 );
				} else if ((data & 0xff) == 0xd3) { /* fire blue? */
					OKIM6295_data_0_w(0, 0x80 | 0x03 );
					OKIM6295_data_0_w(0, 0x00 | 0x10 );
				} else if ((data & 0xff) == 0xd8) { /* explosion1? */
					OKIM6295_data_0_w(0, 0x80 | 0x07 );
					OKIM6295_data_0_w(0, 0x00 | 0x20 );
				} else if ((data & 0xff) == 0xdb) { /* explosion2? */
					OKIM6295_data_0_w(0, 0x80 | 0x08 );
					OKIM6295_data_0_w(0, 0x00 | 0x20 );
				} else if ((data & 0xff) == 0xe0) { /* coin? */
					OKIM6295_data_0_w(0, 0x80 | 0x10 );
					OKIM6295_data_0_w(0, 0x00 | 0x10 );
				} else {
	//				usrintf_showmessage("%04x: mcu_w %04x\n",activecpu_get_pc(),data);
				}
			}
		}
	
	
		if (!strcmp(Machine->gamedrv->name,"macross")) {
			if (ACCESSING_LSB) {
				if ((data & 0xff) == 0xc4) { /* unknown */
					/* ?? */
				} else if ((data & 0xff) == 0xc5) { /* with bomb? */
					/* ?? */
				} else if ((data & 0xff) == 0xc6) { /* unknown */
					/* ?? */
				} else if ((data & 0xff) == 0xcc) { /* unknown */
					/* ?? */
				} else if ((data & 0xff) == 0xce) { /* green ememy die? */
					OKIM6295_data_0_w(0, 0x80 | 0x0f );
					OKIM6295_data_0_w(0, 0x00 | 0x40 );
				} else if ((data & 0xff) == 0xd0) { /* coin */
					OKIM6295_data_0_w(0, 0x80 | 0x01 );
					OKIM6295_data_0_w(0, 0x00 | 0x10 );
				} else if ((data & 0xff) == 0xd1) { /* shoot? */
					OKIM6295_data_0_w(0, 0x80 | 0x03 );
					OKIM6295_data_0_w(0, 0x00 | 0x20 );
				} else if ((data & 0xff) == 0xdb) { /* explosion? */
					OKIM6295_data_0_w(0, 0x80 | 0x10 );
					OKIM6295_data_0_w(0, 0x00 | 0x40 );
				} else if ((data & 0xff) == 0xdd) { /* player death? */
					OKIM6295_data_0_w(0, 0x80 | 0x0e );
					OKIM6295_data_0_w(0, 0x00 | 0x40 );
				} else if ((data & 0xff) == 0xde) { /* bomb? */
					OKIM6295_data_0_w(0, 0x80 | 0x0d );
					OKIM6295_data_0_w(0, 0x00 | 0x40 );
				} else if ((data & 0xff) == 0xdf) { /* some enemy fire? */
					OKIM6295_data_0_w(0, 0x80 | 0x12 );
					OKIM6295_data_0_w(0, 0x00 | 0x20 );
				} else if ((data & 0xff) == 0xe5) { /* power up? */
					OKIM6295_data_0_w(0, 0x80 | 0x16 );
					OKIM6295_data_0_w(0, 0x00 | 0x80 );
				} else {
	//				usrintf_showmessage("%04x: mcu_w %04x\n",activecpu_get_pc(),data);
				}
			}
		}
	#endif
	}
	
	
	static READ16_HANDLER( macross_mcu_r )
	{
		static int resp[] = {	0x82, 0xc7, 0x00,
								0x2c, 0x6c, 0x00,
								0x9f, 0xc7, 0x00,
								0x29, 0x69, 0x00,
								0x8b, 0xc7, 0x00 };
		int res;
	
		if (activecpu_get_pc()==0x8aa) res = (ram[0x064/2])|0x20; /* Task Force Harrier */
		else if (activecpu_get_pc()==0x8ce) res = (ram[0x064/2])|0x60; /* Task Force Harrier */
		else if (activecpu_get_pc() == 0x0332	/* Macross */
				||	activecpu_get_pc() == 0x64f4)	/* GunNail */
			res = ram[0x0f6/2];
		else
		{
			res = resp[respcount++];
			if (respcount >= sizeof(resp)/sizeof(resp[0])) respcount = 0;
		}
	
	logerror("%04x: mcu_r %02x\n",activecpu_get_pc(),res);
	
		return res;
	}
	
	static READ16_HANDLER( urashima_mcu_r )
	{
		static int resp[] = {	0x99, 0xd8, 0x00,
								0x2a, 0x6a, 0x00,
								0x9c, 0xd8, 0x00,
								0x2f, 0x6f, 0x00,
								0x22, 0x62, 0x00,
								0x25, 0x65, 0x00 };
		int res;
	
		res = resp[respcount++];
		if (respcount >= sizeof(resp)/sizeof(resp[0])) respcount = 0;
	
	logerror("%04x: mcu_r %02x\n",activecpu_get_pc(),res);
	
		return res;
	}
	
	static WRITE16_HANDLER( tharrier_mcu_control_w )
	{
	//	logerror("%04x: mcu_control_w %02x\n",activecpu_get_pc(),data);
	}
	
	static READ16_HANDLER( tharrier_mcu_r )
	{
		/* The MCU is mapped as the top byte for byte accesses only,
			all word accesses are to the input port */
		if (ACCESSING_MSB && !ACCESSING_LSB)
			return macross_mcu_r(offset,0)<<8;
		else
			return ~input_port_1_word_r(0,0);
	}
	
	static WRITE16_HANDLER( macross2_sound_command_w )
	{
		if (ACCESSING_LSB)
			soundlatch_w(0,data & 0xff);
	}
	
	static READ16_HANDLER( macross2_sound_result_r )
	{
		return soundlatch2_r(0);
	}
	
	public static WriteHandlerPtr macross2_sound_bank_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		const UINT8 *rom = memory_region(REGION_CPU2) + 0x10000;
	
		cpu_setbank(1,rom + (data & 0x07) * 0x4000);
	} };
	
	public static WriteHandlerPtr macross2_oki6295_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* The OKI6295 ROM space is divided in four banks, each one indepentently
		   controlled. The sample table at the beginning of the addressing space is
		   divided in four pages as well, banked together with the sample data. */
		#define TABLESIZE 0x100
		#define BANKSIZE 0x10000
		int chip = (offset & 4) >> 2;
		int banknum = offset & 3;
		unsigned char *rom = memory_region(REGION_SOUND1 + chip);
		int size = memory_region_length(REGION_SOUND1 + chip) - 0x40000;
		int bankaddr = (data * BANKSIZE) & (size-1);
	
		/* copy the samples */
		memcpy(rom + banknum * BANKSIZE,rom + 0x40000 + bankaddr,BANKSIZE);
	
		/* and also copy the samples address table */
		rom += banknum * TABLESIZE;
		memcpy(rom,rom + 0x40000 + bankaddr,TABLESIZE);
	} };
	
	static WRITE16_HANDLER( bjtwin_oki6295_bankswitch_w )
	{
		if (ACCESSING_LSB)
			macross2_oki6295_bankswitch_w(offset,data & 0xff);
	}
	
	static READ16_HANDLER( hachamf_protection_hack_r )
	{
		/* adresses for the input ports */
		static int pap[] = { 0x0008, 0x0000, 0x0008, 0x0002, 0x0008, 0x0008 };
	
		return pap[offset];
	}
	
	/***************************************************************************/
	
	static MEMORY_READ16_START( urashima_readmem )
		{ 0x000000, 0x03ffff, MRA16_ROM },
		{ 0x080004, 0x080005, urashima_mcu_r },
		{ 0x09e000, 0x09e7ff, nmk_txvideoram_r },
		{ 0x0f0000, 0x0f7fff, MRA16_RAM },
		{ 0x0f8000, 0x0f8fff, MRA16_RAM },
		{ 0x0f9000, 0x0fffff, MRA16_RAM },
	#if 0
		{ 0x080000, 0x080001, input_port_0_word_r },
		{ 0x080002, 0x080003, input_port_1_word_r },
		{ 0x080008, 0x080009, input_port_2_word_r },
		{ 0x08000a, 0x08000b, input_port_3_word_r },
		{ 0x088000, 0x0887ff, MRA16_RAM },
		{ 0x09e000, 0x0a1fff, nmk_bgvideoram_r },
	#endif
	MEMORY_END
	
	static MEMORY_WRITE16_START( urashima_writemem )
		{ 0x000000, 0x03ffff, MWA16_ROM },
		{ 0x080014, 0x080015, macross_mcu_w },
		{ 0x09e000, 0x09e7ff, nmk_txvideoram_w, &nmk_txvideoram },
		{ 0x0f0000, 0x0f7fff, MWA16_RAM },	/* Work RAM */
		{ 0x0f8000, 0x0f8fff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0x0f9000, 0x0fffff, MWA16_RAM, &ram },	/* Work RAM again */
	#if 0
		{ 0x080014, 0x080015, nmk_flipscreen_w },
		{ 0x080016, 0x080017, MWA16_NOP },	/* IRQ enable? */
		{ 0x080018, 0x080019, nmk_tilebank_w },
		{ 0x088000, 0x0887ff, paletteram16_RRRRGGGGBBBBRGBx_word_w, &paletteram16 },
		{ 0x08c000, 0x08c007, nmk_scroll_w },
		{ 0x09e000, 0x0a1fff, nmk_bgvideoram_w, &nmk_bgvideoram },
	#endif
	MEMORY_END
	
	
	static MEMORY_READ16_START( vandyke_readmem )
		{ 0x000000, 0x03ffff, MRA16_ROM },
		{ 0x080000, 0x080001, input_port_0_word_r },
		{ 0x080002, 0x080003, input_port_1_word_r },
		{ 0x080008, 0x080009, input_port_2_word_r },
		{ 0x08000a, 0x08000b, input_port_3_word_r },
		{ 0x08000e, 0x08000f, macross_mcu_r },
		{ 0x088000, 0x0887ff, MRA16_RAM },
		{ 0x090000, 0x093fff, nmk_bgvideoram_r },
		{ 0x09d000, 0x09d7ff, nmk_txvideoram_r },
		{ 0x0f0000, 0x0f7fff, MRA16_RAM },
		{ 0x0f8000, 0x0f8fff, MRA16_RAM },
		{ 0x0f9000, 0x0fffff, MRA16_RAM },
	MEMORY_END
	
	static MEMORY_WRITE16_START( vandyke_writemem )
		{ 0x000000, 0x03ffff, MWA16_ROM },
		{ 0x080016, 0x080017, MWA16_NOP },	/* IRQ enable? */
		{ 0x080018, 0x080019, nmk_tilebank_w },
		{ 0x08001e, 0x08001f, macross_mcu_w },
		{ 0x088000, 0x0887ff, paletteram16_RRRRGGGGBBBBRGBx_word_w, &paletteram16 },
		{ 0x08c000, 0x08c007, vandyke_scroll_w },
		{ 0x090000, 0x093fff, nmk_bgvideoram_w, &nmk_bgvideoram },
	//	{ 0x094000, 0x097fff, MWA16_RAM }, /* what is this */
		{ 0x09d000, 0x09d7ff, nmk_txvideoram_w, &nmk_txvideoram },
		{ 0x0f0000, 0x0f7fff, MWA16_RAM },
		{ 0x0f8000, 0x0f8fff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0x0f9000, 0x0fffff, MWA16_RAM }, /* not tested in tests .. hardly used probably some registers not ram */
	MEMORY_END
	
	static READ16_HANDLER(logr)
	{
	//logerror("Read input port 1 %05x\n",activecpu_get_pc());
	return ~input_port_0_word_r(0,0);
	}
	
	static MEMORY_READ16_START( manybloc_readmem )
		{ 0x000000, 0x03ffff, MRA16_ROM },
		{ 0x080000, 0x080001, input_port_0_word_r },
		{ 0x080002, 0x080003, input_port_1_word_r },
		{ 0x080004, 0x080005, input_port_2_word_r },
		{ 0x08001e, 0x08001f, soundlatch2_word_r },
		{ 0x088000, 0x0883ff, MRA16_RAM },
		{ 0x090000, 0x093fff, nmk_bgvideoram_r },
		{ 0x09c000, 0x09cfff, MRA16_RAM }, /* Scroll? */
		{ 0x09d000, 0x09d7ff, nmk_txvideoram_r },
		{ 0x0f0000, 0x0f7fff, MRA16_RAM },
		{ 0x0f8000, 0x0f8fff, MRA16_RAM },
		{ 0x0f9000, 0x0fffff, MRA16_RAM },
	MEMORY_END
	
	static MEMORY_WRITE16_START( manybloc_writemem )
		{ 0x000000, 0x03ffff, MWA16_ROM },
		{ 0x080010, 0x080011, MWA16_NOP },			/* See notes at the top of the driver */
		{ 0x080012, 0x080013, MWA16_NOP },			/* See notes at the top of the driver */
		{ 0x080014, 0x080015, nmk_flipscreen_w },
		{ 0x08001c, 0x08001d, MWA16_NOP },			/* See notes at the top of the driver */
		{ 0x08001e, 0x08001f, soundlatch_word_w },
		{ 0x088000, 0x0883ff, paletteram16_RRRRGGGGBBBBRGBx_word_w, &paletteram16 },
		{ 0x08c000, 0x08c007, nmk_scroll_w },
		{ 0x090000, 0x093fff, nmk_bgvideoram_w, &nmk_bgvideoram },
		{ 0x09c000, 0x09cfff, MWA16_RAM }, /* Scroll? */
		{ 0x09d000, 0x09d7ff, nmk_txvideoram_w, &nmk_txvideoram },
		{ 0x0f0000, 0x0f7fff, MWA16_RAM },	/* Work RAM */
		{ 0x0f8000, 0x0f8fff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0x0f9000, 0x0fffff, MWA16_RAM, &ram },
	MEMORY_END
	
	public static Memory_ReadAddress manybloc_sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0xbfff, MRA_ROM ),
		new Memory_ReadAddress( 0xc000, 0xc7ff, MRA_RAM ),
		new Memory_ReadAddress( 0xf000, 0xf000, soundlatch_r ),
		new Memory_ReadAddress( 0xf400, 0xf400, OKIM6295_status_0_r ),
		new Memory_ReadAddress( 0xf500, 0xf500, OKIM6295_status_1_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	static MEMORY_WRITE_START( manybloc_sound_writemem)
		{ 0x0000, 0xbfff, MWA_ROM },
		{ 0xc000, 0xc7ff, MWA_RAM },
		{ 0xf000, 0xf000, soundlatch2_w },
		{ 0xf400, 0xf400, OKIM6295_data_0_w },
		{ 0xf500, 0xf500, OKIM6295_data_1_w },
		{ 0xf600, 0xf600, MWA_NOP },
		{ 0xf700, 0xf700, MWA_NOP },
	MEMORY_END
	
	public static IO_ReadPort manybloc_sound_readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x00, 0x00, YM2203_status_port_0_r ),
		new IO_ReadPort( 0x01, 0x01, YM2203_read_port_0_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort manybloc_sound_writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00, 0x00, YM2203_control_port_0_w ),
		new IO_WritePort( 0x01, 0x01, YM2203_write_port_0_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	static MEMORY_READ16_START( tharrier_readmem )
		{ 0x000000, 0x03ffff, MRA16_ROM },
		{ 0x080000, 0x080001, logr },//input_port_0_word_r },
		{ 0x080002, 0x080003, tharrier_mcu_r }, //input_port_1_word_r },
	//	{ 0x080004, 0x080005, input_port_2_word_r },
		{ 0x08000e, 0x08000f, soundlatch2_word_r },	/* from Z80 */
		{ 0x088000, 0x0883ff, MRA16_RAM },
		{ 0x090000, 0x093fff, nmk_bgvideoram_r },
		{ 0x09d000, 0x09d7ff, nmk_txvideoram_r },
		{ 0x0f0000, 0x0f7fff, MRA16_RAM },
		{ 0x0f8000, 0x0f8fff, MRA16_RAM },
		{ 0x0f9000, 0x0fffff, MRA16_RAM },
	MEMORY_END
	
	static MEMORY_WRITE16_START( tharrier_writemem )
		{ 0x000000, 0x03ffff, MWA16_ROM },
		{ 0x080010, 0x080011, tharrier_mcu_control_w },
		{ 0x080012, 0x080013, macross_mcu_w },
	//	{ 0x080014, 0x080015, nmk_flipscreen_w },
	//	{ 0x080018, 0x080019, nmk_tilebank_w },
		{ 0x08001e, 0x08001f, soundlatch_word_w },
		{ 0x088000, 0x0883ff, paletteram16_RRRRGGGGBBBBRGBx_word_w, &paletteram16 },
		{ 0x08c000, 0x08c007, nmk_scroll_w },
		{ 0x090000, 0x093fff, nmk_bgvideoram_w, &nmk_bgvideoram },
		{ 0x09c000, 0x09c7ff, MWA16_NOP }, /* Unused txvideoram area? */
		{ 0x09d000, 0x09d7ff, nmk_txvideoram_w, &nmk_txvideoram },
		{ 0x0f0000, 0x0f7fff, MWA16_RAM },	/* Work RAM */
		{ 0x0f8000, 0x0f8fff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0x0f9000, 0x0fffff, MWA16_RAM, &ram },	/* Work RAM again (fe000-fefff is shared with the sound CPU) */
	MEMORY_END
	
	//Read input port 1 030c8/  BAD
	//3478  GOOD
	
	static MEMORY_READ16_START( mustang_readmem )
		{ 0x000000, 0x03ffff, MRA16_ROM },
		{ 0x080000, 0x080001, input_port_0_word_r },
		{ 0x080002, 0x080003, input_port_1_word_r },
		{ 0x080004, 0x080005, input_port_2_word_r },
		{ 0x08000e, 0x08000f, macross_mcu_r },
	//	{ 0x08000e, 0x08000f, soundlatch2_word_r },	/* from Z80 bootleg only? */
		{ 0x088000, 0x0887ff, MRA16_RAM },
		{ 0x090000, 0x093fff, nmk_bgvideoram_r },
		{ 0x09c000, 0x09c7ff, nmk_txvideoram_r },
		{ 0x0f0000, 0x0f7fff, MRA16_RAM },
		{ 0x0f8000, 0x0f8fff, MRA16_RAM },
		{ 0x0f9000, 0x0fffff, MRA16_RAM },
	MEMORY_END
	
	static MEMORY_WRITE16_START( mustang_writemem )
		{ 0x000000, 0x03ffff, MWA16_ROM },
		{ 0x08000e, 0x08000f, macross_mcu_w },
		{ 0x080014, 0x080015, nmk_flipscreen_w },
		{ 0x080016, 0x080017, MWA16_NOP },	// frame number?
		{ 0x088000, 0x0887ff, paletteram16_RRRRGGGGBBBBRGBx_word_w, &paletteram16 },
		{ 0x08c000, 0x08c007, mustang_scroll_w },
	//{ 0x08c000, 0x08c001, MWA16_NOP },
		{ 0x090000, 0x093fff, nmk_bgvideoram_w, &nmk_bgvideoram },
		{ 0x09c000, 0x09c7ff, nmk_txvideoram_w, &nmk_txvideoram },
		{ 0x0f0000, 0x0f7fff, MWA16_RAM },	/* Work RAM */
		{ 0x0f8000, 0x0f8fff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0x0f9000, 0x0fffff, MWA16_RAM, &ram },	/* Work RAM */
	MEMORY_END
	
	static MEMORY_WRITE16_START( mustangb_writemem )
		{ 0x000000, 0x03ffff, MWA16_ROM },
		{ 0x080014, 0x080015, nmk_flipscreen_w },
		{ 0x080016, 0x080017, MWA16_NOP },	// frame number?
		{ 0x08001e, 0x08001f, seibu_main_mustb_w },
		{ 0x088000, 0x0887ff, paletteram16_RRRRGGGGBBBBRGBx_word_w, &paletteram16 },
		{ 0x08c000, 0x08c007, mustang_scroll_w },
	//{ 0x08c000, 0x08c001, MWA16_NOP },
		{ 0x090000, 0x093fff, nmk_bgvideoram_w, &nmk_bgvideoram },
		{ 0x09c000, 0x09c7ff, nmk_txvideoram_w, &nmk_txvideoram },
		{ 0x0f0000, 0x0f7fff, MWA16_RAM },	/* Work RAM */
		{ 0x0f8000, 0x0f8fff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0x0f9000, 0x0fffff, MWA16_RAM, &ram },	/* Work RAM */
	MEMORY_END
	
	static MEMORY_READ16_START( acrobatm_readmem )
		{ 0x00000, 0x3ffff, MRA16_ROM },
		{ 0x80000, 0x8ffff, MRA16_RAM },
		{ 0xc0000, 0xc0001, input_port_0_word_r },
		{ 0xc0002, 0xc0003, input_port_1_word_r },
		{ 0xc0008, 0xc0009, input_port_2_word_r },
		{ 0xc000a, 0xc000b, input_port_3_word_r },
		{ 0xc4000, 0xc45ff, MRA16_RAM },
		{ 0xcc000, 0xcffff, nmk_bgvideoram_r },
		{ 0xd4000, 0xd47ff, nmk_txvideoram_r },
	MEMORY_END
	
	static MEMORY_WRITE16_START( acrobatm_writemem )
		{ 0x00000, 0x3ffff, MWA16_ROM },
		{ 0x88000, 0x88fff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0x80000, 0x8ffff, MWA16_RAM },
		{ 0xc0014, 0xc0015, nmk_flipscreen_w },
		{ 0xc4000, 0xc45ff, paletteram16_RRRRGGGGBBBBxxxx_word_w, &paletteram16 },
		{ 0xc8000, 0xc8007, nmk_scroll_w },
		{ 0xcc000, 0xcffff, nmk_bgvideoram_w, &nmk_bgvideoram },
		{ 0xd4000, 0xd47ff, nmk_txvideoram_w, &nmk_txvideoram },
	MEMORY_END
	
	static MEMORY_READ16_START( hachamf_readmem )
		{ 0x000000, 0x03ffff, MRA16_ROM },
		{ 0x080000, 0x080001, input_port_0_word_r },
		{ 0x080002, 0x080003, input_port_1_word_r },
		{ 0x080008, 0x080009, input_port_2_word_r },
		{ 0x088000, 0x0887ff, MRA16_RAM },
		{ 0x090000, 0x093fff, nmk_bgvideoram_r },
		{ 0x09c000, 0x09c7ff, nmk_txvideoram_r },
		{ 0x0f0000, 0x0f7fff, MRA16_RAM },
		{ 0x0f8000, 0x0f8fff, MRA16_RAM },
		{ 0x0fe000, 0x0fe00b, hachamf_protection_hack_r },
		{ 0x0f9000, 0x0fffff, MRA16_RAM },
	MEMORY_END
	
	static MEMORY_WRITE16_START( hachamf_writemem )
		{ 0x000000, 0x03ffff, MWA16_ROM },
		{ 0x080014, 0x080015, nmk_flipscreen_w },
		{ 0x080018, 0x080019, nmk_tilebank_w },
		{ 0x088000, 0x0887ff, paletteram16_RRRRGGGGBBBBRGBx_word_w, &paletteram16 },
		{ 0x08c000, 0x08c007, nmk_scroll_w },
		{ 0x090000, 0x093fff, nmk_bgvideoram_w, &nmk_bgvideoram },
		{ 0x09c000, 0x09c7ff, nmk_txvideoram_w, &nmk_txvideoram },
		{ 0x0f0000, 0x0f7fff, MWA16_RAM },	/* Work RAM */
		{ 0x0f8000, 0x0f8fff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0x0f9000, 0x0fffff, MWA16_RAM },	/* Work RAM again (fe000-fefff is shared with the sound CPU) */
	MEMORY_END
	
	static MEMORY_READ16_START( bioship_readmem )
		{ 0x000000, 0x03ffff, MRA16_ROM },
		{ 0x080000, 0x080001, input_port_0_word_r },
		{ 0x080002, 0x080003, input_port_1_word_r },
		{ 0x080008, 0x080009, input_port_2_word_r },
		{ 0x08000a, 0x08000b, input_port_3_word_r },
		{ 0x088000, 0x0887ff, MRA16_RAM },
		{ 0x090000, 0x093fff, nmk_bgvideoram_r },
		{ 0x09c000, 0x09c7ff, nmk_txvideoram_r },
		{ 0x0f0000, 0x0f7fff, MRA16_RAM },
		{ 0x0f8000, 0x0f8fff, MRA16_RAM },
		{ 0x0f9000, 0x0fffff, MRA16_RAM },
	MEMORY_END
	
	static MEMORY_WRITE16_START( bioship_writemem )
		{ 0x000000, 0x03ffff, MWA16_ROM },
	//	{ 0x080014, 0x080015, nmk_flipscreen_w },
		{ 0x084000, 0x084001, bioship_bank_w },
		{ 0x088000, 0x0887ff, paletteram16_RRRRGGGGBBBBRGBx_word_w, &paletteram16 },
		{ 0x08c000, 0x08c007, mustang_scroll_w },
		{ 0x08c010, 0x08c017, bioship_scroll_w },
		{ 0x090000, 0x093fff, nmk_bgvideoram_w, &nmk_bgvideoram },
		{ 0x09c000, 0x09c7ff, nmk_txvideoram_w, &nmk_txvideoram },
		{ 0x0f0000, 0x0f7fff, MWA16_RAM },	/* Work RAM */
		{ 0x0f8000, 0x0f8fff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0x0f9000, 0x0fffff, MWA16_RAM },	/* Work RAM again (fe000-fefff is shared with the sound CPU) */
	MEMORY_END
	
	static MEMORY_READ16_START( tdragon_readmem )
		{ 0x000000, 0x03ffff, MRA16_ROM },
		{ 0x044022, 0x044023, MRA16_NOP },  /* No Idea */
		{ 0x0b0000, 0x0b7fff, MRA16_RAM },	/* Work RAM */
		{ 0x0b8000, 0x0b8fff, MRA16_RAM },	/* Sprite RAM */
		{ 0x0b9000, 0x0bffff, MRA16_RAM },	/* Work RAM */
		{ 0x0c8000, 0x0c87ff, MRA16_RAM },  /* Palette RAM */
		{ 0x0c0000, 0x0c0001, input_port_0_word_r },
		{ 0x0c0002, 0x0c0003, input_port_1_word_r },
		{ 0x0c0008, 0x0c0009, input_port_2_word_r },
		{ 0x0c000a, 0x0c000b, input_port_3_word_r },
		{ 0x0cc000, 0x0cffff, nmk_bgvideoram_r },
		{ 0x0d0000, 0x0d07ff, nmk_txvideoram_r },
	MEMORY_END
	
	static MEMORY_WRITE16_START( tdragon_writemem )
		{ 0x000000, 0x03ffff, MWA16_ROM },
		{ 0x0b0000, 0x0b7fff, MWA16_RAM },	/* Work RAM */
		{ 0x0b8000, 0x0b8fff, MWA16_RAM, &spriteram16, &spriteram_size },	/* Sprite RAM */
		{ 0x0b9000, 0x0bffff, MWA16_RAM },	/* Work RAM */
		{ 0x0c0014, 0x0c0015, nmk_flipscreen_w }, /* Maybe */
		{ 0x0c0018, 0x0c0019, nmk_tilebank_w }, /* Tile Bank ? */
		{ 0x0c001e, 0x0c001f, MWA16_NOP },
		{ 0x0c4000, 0x0c4007, nmk_scroll_w },
		{ 0x0c8000, 0x0c87ff, paletteram16_RRRRGGGGBBBBRGBx_word_w, &paletteram16 },
		{ 0x0cc000, 0x0cffff, nmk_bgvideoram_w, &nmk_bgvideoram },
		{ 0x0d0000, 0x0d07ff, nmk_txvideoram_w, &nmk_txvideoram },
	MEMORY_END
	
	static MEMORY_WRITE16_START( tdragonb_writemem )
		{ 0x000000, 0x03ffff, MWA16_ROM },
		{ 0x0b0000, 0x0b7fff, MWA16_RAM },	/* Work RAM */
		{ 0x0b8000, 0x0b8fff, MWA16_RAM, &spriteram16, &spriteram_size },	/* Sprite RAM */
		{ 0x0b9000, 0x0bffff, MWA16_RAM },	/* Work RAM */
		{ 0x0c0014, 0x0c0015, nmk_flipscreen_w }, /* Maybe */
		{ 0x0c0018, 0x0c0019, nmk_tilebank_w }, /* Tile Bank ? */
		{ 0x0c001e, 0x0c001f, seibu_main_mustb_w },
		{ 0x0c4000, 0x0c4007, nmk_scroll_w },
		{ 0x0c8000, 0x0c87ff, paletteram16_RRRRGGGGBBBBRGBx_word_w, &paletteram16 },
		{ 0x0cc000, 0x0cffff, nmk_bgvideoram_w, &nmk_bgvideoram },
		{ 0x0d0000, 0x0d07ff, nmk_txvideoram_w, &nmk_txvideoram },
	MEMORY_END
	
	static MEMORY_READ16_START( ssmissin_readmem )
		{ 0x000000, 0x03ffff, MRA16_ROM },
		{ 0x0b0000, 0x0b7fff, MRA16_RAM },	/* Work RAM */
		{ 0x0b8000, 0x0b8fff, MRA16_RAM },	/* Sprite RAM */
		{ 0x0b9000, 0x0bffff, MRA16_RAM },	/* Work RAM */
		{ 0x0c8000, 0x0c87ff, MRA16_RAM },  /* Palette RAM */
		{ 0x0c0000, 0x0c0001, input_port_0_word_r },
		{ 0x0c0004, 0x0c0005, input_port_1_word_r },
		{ 0x0c0006, 0x0c0007, input_port_2_word_r },
	//	{ 0x0c000e, 0x0c000f, ?? },
		{ 0x0cc000, 0x0cffff, nmk_bgvideoram_r },
		{ 0x0d0000, 0x0d07ff, nmk_txvideoram_r },
	MEMORY_END
	
	static MEMORY_WRITE16_START( ssmissin_writemem )
		{ 0x000000, 0x03ffff, MWA16_ROM },
		{ 0x0b0000, 0x0b7fff, MWA16_RAM },	/* Work RAM */
		{ 0x0b8000, 0x0b8fff, MWA16_RAM, &spriteram16, &spriteram_size },	/* Sprite RAM */
		{ 0x0b9000, 0x0bffff, MWA16_RAM },	/* Work RAM */
		{ 0x0c0014, 0x0c0015, nmk_flipscreen_w }, /* Maybe */
		{ 0x0c0018, 0x0c0019, nmk_tilebank_w }, /* Tile Bank ? */
		{ 0x0c001e, 0x0c001f, ssmissin_sound_w },
		{ 0x0c4000, 0x0c4007, nmk_scroll_w },
		{ 0x0c8000, 0x0c87ff, paletteram16_RRRRGGGGBBBBRGBx_word_w, &paletteram16 },
		{ 0x0cc000, 0x0cffff, nmk_bgvideoram_w, &nmk_bgvideoram },
		{ 0x0d0000, 0x0d07ff, nmk_txvideoram_w, &nmk_txvideoram },
	MEMORY_END
	
	
	
	public static Memory_ReadAddress ssmissin_sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0x8000, 0x87ff, MRA_RAM ),
		new Memory_ReadAddress( 0x9800, 0x9800, OKIM6295_status_0_r ),
		new Memory_ReadAddress( 0xa000, 0xa000, soundlatch_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress ssmissin_sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0x8000, 0x87ff, MWA_RAM ),
		new Memory_WriteAddress( 0x9000, 0x9000, ssmissin_soundbank_w ),
		new Memory_WriteAddress( 0x9800, 0x9800, OKIM6295_data_0_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	static MEMORY_READ16_START( strahl_readmem )
		{ 0x00000, 0x3ffff, MRA16_ROM },
		{ 0x80000, 0x80001, input_port_0_word_r },
		{ 0x80002, 0x80003, input_port_1_word_r },
		{ 0x80008, 0x80009, input_port_2_word_r },
		{ 0x8000a, 0x8000b, input_port_3_word_r },
		{ 0x8c000, 0x8c7ff, MRA16_RAM },
		{ 0x90000, 0x93fff, nmk_bgvideoram_r },
		{ 0x94000, 0x97fff, nmk_fgvideoram_r },
		{ 0x9c000, 0x9c7ff, nmk_txvideoram_r },
		{ 0xf0000, 0xf7fff, MRA16_RAM },
		{ 0xf8000, 0xfefff, MRA16_RAM },
		{ 0xff000, 0xfffff, MRA16_RAM },
	MEMORY_END
	
	static MEMORY_WRITE16_START( strahl_writemem )
		{ 0x00000, 0x3ffff, MWA16_ROM },
		{ 0x80014, 0x80015, nmk_flipscreen_w },
		{ 0x8001e, 0x8001f, MWA16_NOP }, /* -> Sound cpu */
		{ 0x84000, 0x84007, nmk_scroll_w },
		{ 0x88000, 0x88007, nmk_scroll_2_w },
		{ 0x8c000, 0x8c7ff, paletteram16_RRRRGGGGBBBBxxxx_word_w, &paletteram16 },
		{ 0x90000, 0x93fff, nmk_bgvideoram_w, &nmk_bgvideoram },
		{ 0x94000, 0x97fff, nmk_fgvideoram_w, &nmk_fgvideoram },
		{ 0x9c000, 0x9c7ff, nmk_txvideoram_w, &nmk_txvideoram },
		{ 0xf0000, 0xf7fff, MWA16_RAM },	/* Work RAM */
		{ 0xf8000, 0xfefff, MWA16_RAM, &ram },	/* Work RAM again */
		{ 0xff000, 0xfffff, MWA16_RAM, &spriteram16, &spriteram_size },
	MEMORY_END
	
	static MEMORY_READ16_START( macross_readmem )
		{ 0x000000, 0x07ffff, MRA16_ROM },
		{ 0x080000, 0x080001, input_port_0_word_r },
		{ 0x080002, 0x080003, input_port_1_word_r },
		{ 0x080008, 0x080009, input_port_2_word_r },
		{ 0x08000a, 0x08000b, input_port_3_word_r },
		{ 0x08000e, 0x08000f, macross_mcu_r },
		{ 0x088000, 0x0887ff, MRA16_RAM },
		{ 0x090000, 0x093fff, nmk_bgvideoram_r },
		{ 0x09c000, 0x09c7ff, nmk_txvideoram_r },
		{ 0x0f0000, 0x0f7fff, MRA16_RAM },
		{ 0x0f8000, 0x0f8fff, MRA16_RAM },
		{ 0x0f9000, 0x0fffff, MRA16_RAM },
	MEMORY_END
	
	static MEMORY_WRITE16_START( macross_writemem )
		{ 0x000000, 0x07ffff, MWA16_ROM },
		{ 0x080014, 0x080015, nmk_flipscreen_w },
		{ 0x080016, 0x080017, MWA16_NOP },	/* IRQ enable? */
		{ 0x080018, 0x080019, nmk_tilebank_w },
		{ 0x08001e, 0x08001f, macross_mcu_w },
		{ 0x088000, 0x0887ff, paletteram16_RRRRGGGGBBBBRGBx_word_w, &paletteram16 },
		{ 0x08c000, 0x08c007, nmk_scroll_w },
		{ 0x090000, 0x093fff, nmk_bgvideoram_w, &nmk_bgvideoram },
		{ 0x09c000, 0x09c7ff, nmk_txvideoram_w, &nmk_txvideoram },
		{ 0x0f0000, 0x0f7fff, MWA16_RAM },	/* Work RAM */
		{ 0x0f8000, 0x0f8fff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0x0f9000, 0x0fffff, MWA16_RAM, &ram },	/* Work RAM again */
	MEMORY_END
	
	static MEMORY_READ16_START( gunnail_readmem )
		{ 0x000000, 0x07ffff, MRA16_ROM },
		{ 0x080000, 0x080001, input_port_0_word_r },
		{ 0x080002, 0x080003, input_port_1_word_r },
		{ 0x080008, 0x080009, input_port_2_word_r },
		{ 0x08000a, 0x08000b, input_port_3_word_r },
		{ 0x08000e, 0x08000f, macross_mcu_r },
		{ 0x088000, 0x0887ff, MRA16_RAM }, /* palette ram */
		{ 0x090000, 0x093fff, nmk_bgvideoram_r },
		{ 0x09c000, 0x09cfff, nmk_txvideoram_r },
		{ 0x09d000, 0x09dfff, nmk_txvideoram_r },	/* mirror */
		{ 0x0f0000, 0x0f7fff, MRA16_RAM },
		{ 0x0f8000, 0x0f8fff, MRA16_RAM },
		{ 0x0f9000, 0x0fffff, MRA16_RAM },
	MEMORY_END
	
	static MEMORY_WRITE16_START( gunnail_writemem )
		{ 0x000000, 0x07ffff, MWA16_ROM },
		{ 0x08001e, 0x08001f, macross_mcu_w },
		{ 0x080014, 0x080015, nmk_flipscreen_w },
		{ 0x080016, 0x080017, MWA16_NOP },	/* IRQ enable? */
		{ 0x080018, 0x080019, nmk_tilebank_w },
		{ 0x088000, 0x0887ff, paletteram16_RRRRGGGGBBBBRGBx_word_w, &paletteram16 },
		{ 0x08c000, 0x08c1ff, gunnail_scrollx_w, &gunnail_scrollram },
		{ 0x08c200, 0x08c201, gunnail_scrolly_w },
	//	{ 0x08c202, 0x08c7ff, MWA16_RAM },	// unknown
		{ 0x090000, 0x093fff, nmk_bgvideoram_w, &nmk_bgvideoram },
		{ 0x09c000, 0x09cfff, nmk_txvideoram_w, &nmk_txvideoram },
		{ 0x09d000, 0x09dfff, nmk_txvideoram_w },	/* mirror */
		{ 0x0f0000, 0x0f7fff, MWA16_RAM },	/* Work RAM */
		{ 0x0f8000, 0x0f8fff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0x0f9000, 0x0fffff, MWA16_RAM, &ram }, /* Work RAM again */
	MEMORY_END
	
	static MEMORY_READ16_START( macross2_readmem )
		{ 0x000000, 0x07ffff, MRA16_ROM },
		{ 0x100000, 0x100001, input_port_0_word_r },
		{ 0x100002, 0x100003, input_port_1_word_r },
		{ 0x100008, 0x100009, input_port_2_word_r },
		{ 0x10000a, 0x10000b, input_port_3_word_r },
		{ 0x10000e, 0x10000f, macross2_sound_result_r },	/* from Z80 */
		{ 0x120000, 0x1207ff, MRA16_RAM },
		{ 0x140000, 0x14ffff, nmk_bgvideoram_r },
		{ 0x170000, 0x170fff, nmk_txvideoram_r },
		{ 0x171000, 0x171fff, nmk_txvideoram_r },	/* mirror */
		{ 0x1f0000, 0x1f7fff, MRA16_RAM },
		{ 0x1f8000, 0x1f8fff, MRA16_RAM },
		{ 0x1f9000, 0x1fffff, MRA16_RAM },
	MEMORY_END
	
	static MEMORY_WRITE16_START( macross2_writemem )
		{ 0x000000, 0x07ffff, MWA16_ROM },
		{ 0x100014, 0x100015, nmk_flipscreen_w },
		{ 0x100016, 0x100017, MWA16_NOP },	/* IRQ eanble? */
		{ 0x100018, 0x100019, nmk_tilebank_w },
		{ 0x10001e, 0x10001f, macross2_sound_command_w },	/* to Z80 */
		{ 0x120000, 0x1207ff, paletteram16_RRRRGGGGBBBBRGBx_word_w, &paletteram16 },
		{ 0x130000, 0x130007, nmk_scroll_w },
		{ 0x130008, 0x1307ff, MWA16_NOP },	/* 0 only? */
		{ 0x140000, 0x14ffff, nmk_bgvideoram_w, &nmk_bgvideoram },
		{ 0x170000, 0x170fff, nmk_txvideoram_w, &nmk_txvideoram },
		{ 0x171000, 0x171fff, nmk_txvideoram_w },	/* mirror */
		{ 0x1f0000, 0x1f7fff, MWA16_RAM },	/* Work RAM */
		{ 0x1f8000, 0x1f8fff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0x1f9000, 0x1fffff, MWA16_RAM, &ram },	/* Work RAM again */
	MEMORY_END
	
	static MEMORY_WRITE16_START( raphero_writemem )
		{ 0x000000, 0x07ffff, MWA16_ROM },
		{ 0x100014, 0x100015, nmk_flipscreen_w },
		{ 0x100016, 0x100017, MWA16_NOP },	/* IRQ eanble? */
		{ 0x100018, 0x100019, nmk_tilebank_w },
		{ 0x10001e, 0x10001f, macross2_sound_command_w },	/* to Z80 */
		{ 0x120000, 0x1207ff, paletteram16_RRRRGGGGBBBBRGBx_word_w, &paletteram16 },
		{ 0x130000, 0x1307ff, nmk_scroll_3_w, &gunnail_scrollram },
	//	{ 0x130010, 0x1307ff, MWA16_RAM },	/* 0 only? */
		{ 0x140000, 0x14ffff, nmk_bgvideoram_w, &nmk_bgvideoram },
		{ 0x170000, 0x170fff, nmk_txvideoram_w, &nmk_txvideoram },
		{ 0x171000, 0x171fff, nmk_txvideoram_w },	/* mirror */
		{ 0x1f0000, 0x1f7fff, MWA16_RAM },	/* Work RAM */
		{ 0x1f8000, 0x1f8fff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0x1f9000, 0x1fffff, MWA16_RAM, &ram },	/* Work RAM again */
	MEMORY_END
	
	public static Memory_ReadAddress macross2_sound_readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0x8000, 0xbfff, MRA_BANK1 ),	/* banked ROM */
		new Memory_ReadAddress( 0xa000, 0xa000, MRA_NOP ),	/* IRQ ack? watchdog? */
		new Memory_ReadAddress( 0xc000, 0xdfff, MRA_RAM ),
		new Memory_ReadAddress( 0xf000, 0xf000, soundlatch_r ),	/* from 68000 */
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress macross2_sound_writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new Memory_WriteAddress( 0xc000, 0xdfff, MWA_RAM ),
		new Memory_WriteAddress( 0xe001, 0xe001, macross2_sound_bank_w ),
		new Memory_WriteAddress( 0xf000, 0xf000, soundlatch2_w ),	/* to 68000 */
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static IO_ReadPort macross2_sound_readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x00, 0x00, YM2203_status_port_0_r ),
		new IO_ReadPort( 0x01, 0x01, YM2203_read_port_0_r ),
		new IO_ReadPort( 0x80, 0x80, OKIM6295_status_0_r ),
		new IO_ReadPort( 0x88, 0x88, OKIM6295_status_1_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort macross2_sound_writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00, 0x00, YM2203_control_port_0_w ),
		new IO_WritePort( 0x01, 0x01, YM2203_write_port_0_w ),
		new IO_WritePort( 0x80, 0x80, OKIM6295_data_0_w ),
		new IO_WritePort( 0x88, 0x88, OKIM6295_data_1_w ),
		new IO_WritePort( 0x90, 0x97, macross2_oki6295_bankswitch_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	static MEMORY_READ16_START( bjtwin_readmem )
		{ 0x000000, 0x07ffff, MRA16_ROM },
		{ 0x080000, 0x080001, input_port_0_word_r },
		{ 0x080002, 0x080003, input_port_1_word_r },
		{ 0x080008, 0x080009, input_port_2_word_r },
		{ 0x08000a, 0x08000b, input_port_3_word_r },
		{ 0x084000, 0x084001, OKIM6295_status_0_lsb_r },
		{ 0x084010, 0x084011, OKIM6295_status_1_lsb_r },
		{ 0x088000, 0x0887ff, MRA16_RAM },
		{ 0x09c000, 0x09cfff, nmk_bgvideoram_r },
		{ 0x09d000, 0x09dfff, nmk_bgvideoram_r },	/* mirror */
		{ 0x0f0000, 0x0f7fff, MRA16_RAM },
		{ 0x0f8000, 0x0f8fff, MRA16_RAM },
		{ 0x0f9000, 0x0fffff, MRA16_RAM },
	MEMORY_END
	
	static MEMORY_WRITE16_START( bjtwin_writemem )
		{ 0x000000, 0x07ffff, MWA16_ROM },
		{ 0x080014, 0x080015, nmk_flipscreen_w },
		{ 0x084000, 0x084001, OKIM6295_data_0_lsb_w },
		{ 0x084010, 0x084011, OKIM6295_data_1_lsb_w },
		{ 0x084020, 0x08402f, bjtwin_oki6295_bankswitch_w },
		{ 0x088000, 0x0887ff, paletteram16_RRRRGGGGBBBBRGBx_word_w, &paletteram16 },
		{ 0x094000, 0x094001, nmk_tilebank_w },
		{ 0x094002, 0x094003, MWA16_NOP },	/* IRQ enable? */
		{ 0x09c000, 0x09cfff, nmk_bgvideoram_w, &nmk_bgvideoram },
		{ 0x09d000, 0x09dfff, nmk_bgvideoram_w },	/* mirror */
		{ 0x0f0000, 0x0f7fff, MWA16_RAM },	/* Work RAM */
		{ 0x0f8000, 0x0f8fff, MWA16_RAM, &spriteram16, &spriteram_size },
		{ 0x0f9000, 0x0fffff, MWA16_RAM },	/* Work RAM again */
	MEMORY_END
	
	
	static InputPortPtr input_ports_vandyke = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Maybe unused */
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Maybe unused */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Maybe unused */
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW 1 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00,  "2" );
		PORT_DIPSETTING(    0x01,  "3" );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Service_Mode") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW 2 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x1c, 0x1c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x1c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x14, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0xe0, 0xe0, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_blkheart = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Maybe unused */
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Maybe unused */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Maybe unused */
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW 1 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x02, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x04, "Normal" );
		PORT_DIPSETTING(    0x00, "Hard" );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x40,  "2" );
		PORT_DIPSETTING(    0xc0,  "3" );
		PORT_DIPSETTING(    0x80,  "4" );
		PORT_DIPSETTING(    0x00,  "5" );
	
		PORT_START(); 	/* DSW 2 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x1c, 0x1c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x1c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x14, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xe0, 0xe0, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x80, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_manybloc = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 - 0x080000 */
		PORT_BIT( 0x7fff, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW,  IPT_UNKNOWN );	// VBLANK ? Check code at 0x005640
	
		PORT_START(); 	/* IN1 - 0x080002 */
		PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x0002, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );// select fruits
		PORT_BIT( 0x0004, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1 );// help
		PORT_BIT( 0x0008, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x0010, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x0020, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x0040, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x0080, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x0100, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0x0200, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );// select fruits
		PORT_BIT( 0x0400, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 );// help
		PORT_BIT( 0x0800, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x1000, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x2000, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x4000, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x8000, IP_ACTIVE_HIGH, IPT_COIN2 );
	
		PORT_START(); 	/* DSW - 0x080004 -> 0x0f0036 */
		PORT_DIPNAME( 0x0001, 0x0000, "Slot System" );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0001, DEF_STR( "On") );
		PORT_DIPNAME( 0x0002, 0x0000, "Explanation" );
		PORT_DIPSETTING(      0x0000, "English" );
		PORT_DIPSETTING(      0x0002, "Japanese" );
		PORT_DIPNAME( 0x0004, 0x0000, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(      0x0004, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0008, 0x0008, DEF_STR( "Cabinet") );		// "Play Type"
		PORT_DIPSETTING(      0x0008, DEF_STR( "Upright") );		//   "Uplight" !
		PORT_DIPSETTING(      0x0000, DEF_STR( "Cocktail") );		//   "Table"
		PORT_SERVICE( 0x0010, IP_ACTIVE_HIGH );			// "Test Mode"
		PORT_DIPNAME( 0x0060, 0x0000, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(      0x0060, "Easy" );			//   "Level 1
		PORT_DIPSETTING(      0x0000, "Normal" );			//   "Level 2
		PORT_DIPSETTING(      0x0020, "Hard" );			//   "Level 3
		PORT_DIPSETTING(      0x0040, "Hardest" );			//   "Level 4
		PORT_DIPNAME( 0x0080, 0x0000, DEF_STR( "Flip_Screen") );	// "Display"
		PORT_DIPSETTING(      0x0000, DEF_STR( "Off") );			//   "Normal"
		PORT_DIPSETTING(      0x0080, DEF_STR( "On") );			//   "Inverse"
		PORT_DIPNAME( 0x0700, 0x0000, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(      0x0700, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(      0x0600, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0500, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x0400, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x0100, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x0200, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0300, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x3800, 0x0000, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(      0x3800, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(      0x3000, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x2800, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x2000, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x0800, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x1000, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x1800, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0xc000, 0x0000, "Plate Probability" );
		PORT_DIPSETTING(      0xc000, "Bad" );
		PORT_DIPSETTING(      0x0000, "Normal" );
		PORT_DIPSETTING(      0x4000, "Better" );
		PORT_DIPSETTING(      0x8000, "Best" );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_tharrier = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_HIGH, IPT_SPECIAL );/* Mcu status? */
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
	
		PORT_START(); 	/* DSW */
		PORT_DIPNAME( 0x0001, 0x0001, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0001, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0002, 0x0002, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0002, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0004, 0x0004, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0004, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0008, 0x0008, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0008, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0010, 0x0010, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0010, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0020, 0x0020, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0020, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0040, 0x0040, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0040, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0080, 0x0080, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0080, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0100, 0x0100, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(      0x0100, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0200, 0x0200, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0200, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0400, 0x0400, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0400, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0800, 0x0800, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0800, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x1000, 0x1000, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x1000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x2000, 0x2000, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x2000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x4000, 0x4000, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x4000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x8000, 0x8000, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x8000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_mustang = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );// TEST in service mode
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW */
		PORT_DIPNAME( 0x0001, 0x0001, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0001, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0002, 0x0002, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0002, DEF_STR( "On") );
		PORT_DIPNAME( 0x001c, 0x001c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(      0x0010, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0008, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x0018, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x001c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x000c, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x0014, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0004, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x00e0, 0x00e0, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(      0x0080, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0040, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x00c0, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x00e0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x0060, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x00a0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0020, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x0100, 0x0100, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(      0x0100, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0200, 0x0200, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0200, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0c00, 0x0c00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(      0x0400, "Easy" );
		PORT_DIPSETTING(      0x0c00, "Normal" );
		PORT_DIPSETTING(      0x0800, "Hard" );
		PORT_DIPSETTING(      0x0000, "Hardest" );
		PORT_DIPNAME( 0x1000, 0x1000, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x1000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x2000, 0x2000, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x2000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0xc000, 0xc000, DEF_STR( "Lives") );
		PORT_DIPSETTING(      0x4000, "2" );
		PORT_DIPSETTING(      0xc000, "3" );
		PORT_DIPSETTING(      0x8000, "4" );
		PORT_DIPSETTING(      0x0000, "5" );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_hachamf = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );//bryan:  test mode in some games?
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();   /* DSW A */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x08, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();   /* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "1" );
		PORT_DIPSETTING(    0x02, "2" );
		PORT_DIPSETTING(    0x03, "3" );
		PORT_DIPSETTING(    0x01, "4" );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x20, "Easy" );
		PORT_DIPSETTING(    0x30, "Normal" );
		PORT_DIPSETTING(    0x10, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x40, 0x00, "Language" );
		PORT_DIPSETTING(    0x00, "English" );
		PORT_DIPSETTING(    0x40, "Japanese" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_strahl = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );//bryan:  test mode in some games?
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();   /* DSW A */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();   /* DSW B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x02, "2" );
		PORT_DIPSETTING(    0x03, "3" );
		PORT_DIPSETTING(    0x01, "4" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x08, "Easy" );
		PORT_DIPSETTING(    0x0c, "Normal" );
		PORT_DIPSETTING(    0x04, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x40, "100k and every 200k" );
		PORT_DIPSETTING(    0x60, "200k and every 200k" );
		PORT_DIPSETTING(    0x20, "300k and every 300k" );
		PORT_DIPSETTING(    0x00, "None" );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_acrobatm = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x0001, 0x0000, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(      0x0001, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0002, 0x0002, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(      0x0002, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x001C, 0x001C, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(      0x0010, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0008, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x0018, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x001C, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x000C, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x0014, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0004, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x00E0, 0x00E0, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(      0x0080, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0040, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x00C0, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x00E0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x0060, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x00A0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0020, DEF_STR( "1C_4C") );
	
		PORT_START();  /* DSW B */
		PORT_SERVICE( 0x01, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x02, "50k and 100k" );
		PORT_DIPSETTING(    0x06, "100k and 100k" );
		PORT_DIPSETTING(    0x04, "100k and 200k" );
		PORT_DIPSETTING(    0x00, "None" );
		PORT_DIPNAME( 0x08, 0x00, "Language" );
		PORT_DIPSETTING(    0x00, "English" );
		PORT_DIPSETTING(    0x08, "Japanese" );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPSETTING(    0x10, "Easy" );
		PORT_DIPSETTING(    0x20, "Hard" );
		PORT_DIPSETTING(    0x30, "Normal" );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x40, "2" );
		PORT_DIPSETTING(    0xc0, "3" );
		PORT_DIPSETTING(    0x80, "4" );
		PORT_DIPSETTING(    0x00, "5" );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_bioship = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );//bryan:  test mode in some games?
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x0001, 0x0001, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(      0x0001, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0006, 0x0006, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(      0x0000, "Easy" );
		PORT_DIPSETTING(      0x0006, "Normal" );
		PORT_DIPSETTING(      0x0002, "Hard" );
		PORT_DIPSETTING(      0x0004, "Hardest" );
		PORT_SERVICE( 0x0008, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x0010, 0x0010, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0010, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0020, 0x0020, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0020, DEF_STR( "On") );
		PORT_DIPNAME( 0x00C0, 0x00C0, DEF_STR( "Lives") );
		PORT_DIPSETTING(      0x0000, "2" );
		PORT_DIPSETTING(      0x00C0, "3" );
		PORT_DIPSETTING(      0x0080, "4" );
		PORT_DIPSETTING(      0x0040, "5" );
	
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x0001, 0x0001, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0001, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0002, 0x0002, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0002, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x001C, 0x001C, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(      0x0010, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0008, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x0018, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x001C, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x000C, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x0014, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0004, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x00E0, 0x00E0, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(      0x0080, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0040, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x00C0, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x00E0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x0060, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x00A0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0020, DEF_STR( "1C_4C") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_tdragon = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );// TEST in service mode
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW 1 */
		PORT_DIPNAME( 0x0003, 0x0003, DEF_STR( "Lives") );
		PORT_DIPSETTING(      0x0000, "1" );
		PORT_DIPSETTING(      0x0002, "2" );
		PORT_DIPSETTING(      0x0003, "3" );
		PORT_DIPSETTING(      0x0001, "4" );
		PORT_DIPNAME( 0x0004, 0x0004, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0004, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0008, 0x0008, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0008, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0030, 0x0030, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(      0x0020, "Easy" );
		PORT_DIPSETTING(      0x0030, "Normal" );
		PORT_DIPSETTING(      0x0010, "Hard" );
		PORT_DIPSETTING(      0x0000, "Hardest" );
		PORT_DIPNAME( 0x0040, 0x0040, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0040, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0080, 0x0080, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(      0x0080, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0100, 0x0100, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0100, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0200, 0x0200, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0200, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0400, 0x0400, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0400, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0800, 0x0800, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0800, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x1000, 0x1000, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x1000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x2000, 0x2000, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x2000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x4000, 0x4000, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x4000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x8000, 0x8000, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x8000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW 2 */
		PORT_DIPNAME( 0x0007, 0x0007, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(      0x0004, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0005, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x0006, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x0007, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x0003, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x0002, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0001, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x0038, 0x0038, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(      0x0020, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0028, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x0030, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x0038, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x0018, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x0010, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0008, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x0040, 0x0040, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0040, DEF_STR( "On") );
		PORT_DIPNAME( 0x0080, 0x0080, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0080, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0100, 0x0100, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0100, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0200, 0x0200, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0200, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0400, 0x0400, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0400, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0800, 0x0800, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0800, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x1000, 0x1000, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x1000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x2000, 0x2000, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x2000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x4000, 0x4000, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x4000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x8000, 0x8000, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x8000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_ssmissin = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );	// "Servise" in "test mode"
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	// "Fire"
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	// "Bomb"
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	// "Fire"
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	// "Bomb"
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW 1 */
		PORT_DIPNAME( 0x0001, 0x0001, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(      0x0001, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0002, 0x0002, DEF_STR( "Unused") );
		PORT_DIPSETTING(      0x0002, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x000c, 0x000c, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(      0x0004, "Easy" );
		PORT_DIPSETTING(      0x000c, "Normal" );
		PORT_DIPSETTING(      0x0008, "Hard" );
		PORT_DIPSETTING(      0x0000, "Hardest" );
		PORT_DIPNAME( 0x0010, 0x0010, DEF_STR( "Unused") );
		PORT_DIPSETTING(      0x0010, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0020, 0x0020, DEF_STR( "Unused") );
		PORT_DIPSETTING(      0x0020, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x00c0, 0x00c0, DEF_STR( "Lives") );
		PORT_DIPSETTING(      0x0000, "1" );
		PORT_DIPSETTING(      0x0040, "2" );
		PORT_DIPSETTING(      0x00c0, "3" );
		PORT_DIPSETTING(      0x0080, "4" );
		PORT_DIPNAME( 0x0100, 0x0100, DEF_STR( "Unused") );
		PORT_DIPSETTING(      0x0100, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0200, 0x0200, DEF_STR( "Unused") );
		PORT_DIPSETTING(      0x0200, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
	#if 0
		PORT_DIPNAME( 0x1c00, 0x1c00, DEF_STR( "Coin_B") );	// initialised but not read back
		PORT_DIPSETTING(      0x0400, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x1400, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x0c00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x1c00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x1800, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x0800, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x1000, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Free_Play") );
	#else
		PORT_DIPNAME( 0x0400, 0x0400, DEF_STR( "Unused") );
		PORT_DIPSETTING(      0x0400, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0800, 0x0800, DEF_STR( "Unused") );
		PORT_DIPSETTING(      0x0800, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x1000, 0x1000, DEF_STR( "Unused") );
		PORT_DIPSETTING(      0x1000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
	#endif
		PORT_DIPNAME( 0xe000, 0xe000, DEF_STR( "Coinage") );
		PORT_DIPSETTING(      0x2000, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0xa000, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x6000, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0xe000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0xc000, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x4000, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x8000, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Free_Play") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_macross = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Maybe unused */
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Maybe unused */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Maybe unused */
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW A */
		PORT_SERVICE( 0x01, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, "Language" );
		PORT_DIPSETTING(    0x00, "English" );
		PORT_DIPSETTING(    0x08, "Japanese" );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x10, "Easy" );
		PORT_DIPSETTING(    0x30, "Normal" );
		PORT_DIPSETTING(    0x20, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "1" );
		PORT_DIPSETTING(    0x40, "2" );
		PORT_DIPSETTING(    0xc0, "3" );
		PORT_DIPSETTING(    0x80, "4" );
	
		PORT_START(); 	/* DSW B */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x04, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "5C_3C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x40, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_macross2 = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Maybe unused */
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Maybe unused */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Maybe unused */
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW A */
		PORT_SERVICE( 0x01, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, "Language" );
		PORT_DIPSETTING(    0x00, "English" );
		PORT_DIPSETTING(    0x08, "Japanese" );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW B */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x04, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "5C_3C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x40, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_tdragon2 = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Maybe unused */
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Maybe unused */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Maybe unused */
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW A */
		PORT_SERVICE( 0x01, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off"));
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "1" );
		PORT_DIPSETTING(    0x40, "2" );
		PORT_DIPSETTING(    0xc0, "3" );
		PORT_DIPSETTING(    0x80, "4" );
	
		PORT_START(); 	/* DSW B */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x04, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "5C_3C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x40, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_gunnail = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Maybe unused */
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Maybe unused */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Maybe unused */
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x1c, 0x1c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x1c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x14, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xe0, 0xe0, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x80, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_sabotenb = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );/* shown in service mode, but no effect */
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Maybe unused */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Maybe unused */
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Language" );
		PORT_DIPSETTING(    0x02, "Japanese" );
		PORT_DIPSETTING(    0x00, "English" );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x08, "Easy" );
		PORT_DIPSETTING(    0x0c, "Normal" );
		PORT_DIPSETTING(    0x04, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "1" );
		PORT_DIPSETTING(    0x40, "2" );
		PORT_DIPSETTING(    0xc0, "3" );
		PORT_DIPSETTING(    0x80, "4" );
	
		PORT_START(); 	/* DSW B */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x1c, 0x1c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x1c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x14, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xe0, 0xe0, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x80, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_bjtwin = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );/* shown in service mode, but no effect */
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Maybe unused */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Maybe unused */
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x0e, 0x0e, "Starting level" );
		PORT_DIPSETTING(    0x08, "Germany" );
		PORT_DIPSETTING(    0x04, "Thailand" );
		PORT_DIPSETTING(    0x0c, "Nevada" );
		PORT_DIPSETTING(    0x0e, "Japan" );
		PORT_DIPSETTING(    0x06, "Korea" );
		PORT_DIPSETTING(    0x0a, "England" );
		PORT_DIPSETTING(    0x02, "Hong Kong" );
		PORT_DIPSETTING(    0x00, "China" );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x20, "Easy" );
		PORT_DIPSETTING(    0x30, "Normal" );
		PORT_DIPSETTING(    0x10, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "1" );
		PORT_DIPSETTING(    0x40, "2" );
		PORT_DIPSETTING(    0xc0, "3" );
		PORT_DIPSETTING(    0x80, "4" );
	
		PORT_START(); 	/* DSW B */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x1c, 0x1c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x1c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x14, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xe0, 0xe0, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x80, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_nouryoku = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW A */
		PORT_DIPNAME( 0x03, 0x03, "Life Decrease Speed" );
		PORT_DIPSETTING(    0x02, "Slow" );
		PORT_DIPSETTING(    0x03, "Normal" );
		PORT_DIPSETTING(    0x01, "Fast" );
		PORT_DIPSETTING(    0x00, "Very Fast" );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x08, "Easy" );
		PORT_DIPSETTING(    0x0c, "Normal" );
		PORT_DIPSETTING(    0x04, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Free_Play") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0xe0, 0xe0, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_4C") );
	
		PORT_START(); 	/* DSW B */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,1),
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8
	);
	
	static GfxLayout tilelayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,1),
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4,
				16*32+0*4, 16*32+1*4, 16*32+2*4, 16*32+3*4, 16*32+4*4, 16*32+5*4, 16*32+6*4, 16*32+7*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32,
				8*32, 9*32, 10*32, 11*32, 12*32, 13*32, 14*32, 15*32 },
		32*32
	);
	
	static GfxDecodeInfo tharrier_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout, 0x000, 16 ),	/* color 0x200-0x2ff */
		new GfxDecodeInfo( REGION_GFX2, 0, tilelayout, 0x000, 16 ),	/* color 0x000-0x0ff */
		new GfxDecodeInfo( REGION_GFX3, 0, tilelayout, 0x100, 16 ),	/* color 0x100-0x1ff */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo macross_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout, 0x200, 16 ),	/* color 0x200-0x2ff */
		new GfxDecodeInfo( REGION_GFX2, 0, tilelayout, 0x000, 16 ),	/* color 0x000-0x0ff */
		new GfxDecodeInfo( REGION_GFX3, 0, tilelayout, 0x100, 16 ),	/* color 0x100-0x1ff */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo macross2_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout, 0x300, 16 ),	/* color 0x300-0x3ff */
		new GfxDecodeInfo( REGION_GFX2, 0, tilelayout, 0x000, 16 ),	/* color 0x000-0x0ff */
		new GfxDecodeInfo( REGION_GFX3, 0, tilelayout, 0x100, 32 ),	/* color 0x100-0x2ff */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo bjtwin_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout, 0x000, 16 ),	/* color 0x000-0x0ff */
		new GfxDecodeInfo( REGION_GFX2, 0, charlayout, 0x000, 16 ),	/* color 0x000-0x0ff */
		new GfxDecodeInfo( REGION_GFX3, 0, tilelayout, 0x100, 16 ),	/* color 0x100-0x1ff */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo bioship_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout, 0x300, 16 ),	/* color 0x300-0x3ff */
		new GfxDecodeInfo( REGION_GFX2, 0, tilelayout, 0x100, 16 ),	/* color 0x100-0x1ff */
		new GfxDecodeInfo( REGION_GFX3, 0, tilelayout, 0x200, 16 ),	/* color 0x200-0x2ff */
		new GfxDecodeInfo( REGION_GFX4, 0, tilelayout, 0x000, 16 ),	/* color 0x000-0x0ff */
		new GfxDecodeInfo( -1 )
	};
	
	static GfxDecodeInfo strahl_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout, 0x000, 16 ),	/* color 0x000-0x0ff */
		new GfxDecodeInfo( REGION_GFX2, 0, tilelayout, 0x300, 16 ),	/* color 0x300-0x3ff */
		new GfxDecodeInfo( REGION_GFX3, 0, tilelayout, 0x100, 16 ),	/* color 0x100-0x1ff */
		new GfxDecodeInfo( REGION_GFX4, 0, tilelayout, 0x200, 16 ),	/* color 0x200-0x2ff */
		new GfxDecodeInfo( -1 )
	};
	
	
	static void ym2203_irqhandler(int irq)
	{
		cpu_set_irq_line(1,0,irq ? ASSERT_LINE : CLEAR_LINE);
	}
	
	static struct YM2203interface ym2203_interface_15 =
	{
		1,			/* 1 chip */
		1500000,	/* 2 MHz ??? */
		{ YM2203_VOL(90,90) },
		{ 0 },
		{ 0 },
		{ 0 },
		{ 0 },
		{ ym2203_irqhandler }
	};
	
	static struct OKIM6295interface okim6295_interface_dual =
	{
		2,              					/* 2 chips */
		{ 16000000/4/165, 16000000/4/165 },	/* 24242Hz frequency? */
		{ REGION_SOUND1, REGION_SOUND2 },	/* memory region */
		{ 40, 40 }							/* volume */
	};
	
	static struct OKIM6295interface okim6295_interface_ssmissin =
	{
		1,              	/* 1 chip */
		{ 8000000/4/165 },	/* ? unknown */
		{ REGION_SOUND1 },	/* memory region */
		{ 100 }				/* volume */
	};
	
	static INTERRUPT_GEN( nmk_interrupt )
	{
		if (cpu_getiloops() == 0) cpu_set_irq_line(0, 4, HOLD_LINE);
		else cpu_set_irq_line(0, 2, HOLD_LINE);
	}
	
	/* Parameters: YM3812 frequency, Oki frequency, Oki memory region */
	SEIBU_SOUND_SYSTEM_YM3812_HARDWARE(14318180/4, 8000, REGION_SOUND1);
	
	static MACHINE_DRIVER_START( urashima )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, 10000000) /* 10 MHz ? */
		MDRV_CPU_MEMORY(urashima_readmem,urashima_writemem)
	//	MDRV_CPU_PERIODIC_INT(irq1_line_hold,112)	/* ???????? */
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_MACHINE_INIT(nmk16)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(256, 256)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(macross_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(macross)
		MDRV_VIDEO_EOF(nmk)
		MDRV_VIDEO_UPDATE(macross)
	
		/* sound hardware */
		MDRV_SOUND_ADD(OKIM6295, okim6295_interface_dual)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( vandyke )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, 10000000) /* 10 MHz ? */
		MDRV_CPU_MEMORY(vandyke_readmem,vandyke_writemem)
		MDRV_CPU_VBLANK_INT(nmk_interrupt,2)
		MDRV_CPU_PERIODIC_INT(irq1_line_hold,112)/* ???????? */
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_MACHINE_INIT(nmk16)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(256, 256)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(macross_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(macross)
		MDRV_VIDEO_EOF(nmk)
		MDRV_VIDEO_UPDATE(macross)
	
		/* sound hardware */
		/* there's also a YM2203 */
		MDRV_SOUND_ADD(OKIM6295, okim6295_interface_dual)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( tharrier )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, 10000000) /* 10 MHz */
		MDRV_CPU_MEMORY(tharrier_readmem,tharrier_writemem)
		MDRV_CPU_VBLANK_INT(nmk_interrupt,2)
		MDRV_CPU_PERIODIC_INT(irq1_line_hold,112)/* ???????? */
	
		MDRV_CPU_ADD(Z80, 3000000)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)
		MDRV_CPU_MEMORY(manybloc_sound_readmem,manybloc_sound_writemem)
		MDRV_CPU_PORTS(manybloc_sound_readport,manybloc_sound_writeport)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_MACHINE_INIT(mustang_sound)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(256, 256)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(tharrier_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(512)
	
		MDRV_VIDEO_START(macross)
		MDRV_VIDEO_EOF(nmk)
		MDRV_VIDEO_UPDATE(macross)
	
		/* sound hardware */
		MDRV_SOUND_ADD(YM2203, ym2203_interface_15)
		MDRV_SOUND_ADD(OKIM6295, okim6295_interface_dual)
	MACHINE_DRIVER_END
	
	static MACHINE_DRIVER_START( manybloc )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, 10000000) /* 10? MHz - check */
		MDRV_CPU_MEMORY(manybloc_readmem,manybloc_writemem)
		MDRV_CPU_VBLANK_INT(nmk_interrupt,2)
		MDRV_CPU_PERIODIC_INT(irq1_line_hold,60)/* is this is too high it breaks the game on this one, too low sprites flicker */
	
		MDRV_CPU_ADD(Z80, 3000000)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)
		MDRV_CPU_MEMORY(manybloc_sound_readmem,manybloc_sound_writemem)
		MDRV_CPU_PORTS(manybloc_sound_readport,manybloc_sound_writeport)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(256, 256)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 1*8, 31*8-1)
		MDRV_GFXDECODE(tharrier_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(512)
	
		MDRV_VIDEO_START(macross)
	//	MDRV_VIDEO_EOF(nmk)
		MDRV_VIDEO_UPDATE(manybloc)
	
		/* sound hardware */
		MDRV_SOUND_ADD(YM2203, ym2203_interface_15)
		MDRV_SOUND_ADD(OKIM6295, okim6295_interface_dual)
	MACHINE_DRIVER_END
	
	static MACHINE_DRIVER_START( mustang )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, 10000000) /* 10 MHz ? */
		MDRV_CPU_MEMORY(mustang_readmem,mustang_writemem)
		MDRV_CPU_VBLANK_INT(nmk_interrupt,2)
		MDRV_CPU_PERIODIC_INT(irq1_line_hold,112)/* ???????? */
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_MACHINE_INIT(nmk16)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(256, 256)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(macross_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(macross)
		MDRV_VIDEO_EOF(nmk)
		MDRV_VIDEO_UPDATE(macross)
	
	
		/* sound hardware */
		MDRV_SOUND_ADD(YM2203, ym2203_interface_15)
		MDRV_SOUND_ADD(OKIM6295, okim6295_interface_dual)
	MACHINE_DRIVER_END
	
	static MACHINE_DRIVER_START( mustangb )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, 10000000) /* 10 MHz ? */
		MDRV_CPU_MEMORY(mustang_readmem,mustangb_writemem)
		MDRV_CPU_VBLANK_INT(nmk_interrupt,2)
		MDRV_CPU_PERIODIC_INT(irq1_line_hold,112)/* ???????? */
	
		SEIBU_SOUND_SYSTEM_CPU(14318180/4)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_MACHINE_INIT(mustang_sound)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(256, 256)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(macross_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(macross)
		MDRV_VIDEO_EOF(nmk)
		MDRV_VIDEO_UPDATE(macross)
	
		/* sound hardware */
		SEIBU_SOUND_SYSTEM_YM3812_INTERFACE
	
	MACHINE_DRIVER_END
	
	static MACHINE_DRIVER_START( acrobatm )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, 10000000) /* 10 MHz ? 12 MHz? */
		MDRV_CPU_MEMORY(acrobatm_readmem,acrobatm_writemem)
		MDRV_CPU_VBLANK_INT(nmk_interrupt,2)
		MDRV_CPU_PERIODIC_INT(irq1_line_hold,112)/* ???????? */
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_MACHINE_INIT(nmk16)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(256, 256)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(macross_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(macross)
		MDRV_VIDEO_EOF(nmk)
		MDRV_VIDEO_UPDATE(macross)
	
		/* sound hardware */
		/* there's also a YM2203? */
		MDRV_SOUND_ADD(OKIM6295, okim6295_interface_dual)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( bioship )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, 16000000) /* 16 MHz ? */
		MDRV_CPU_MEMORY(bioship_readmem,bioship_writemem)
		MDRV_CPU_VBLANK_INT(nmk_interrupt,2)
		MDRV_CPU_PERIODIC_INT(irq1_line_hold,112)/* ???????? */
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_MACHINE_INIT(nmk16)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(256, 256)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(bioship_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(bioship)
		MDRV_VIDEO_EOF(nmk)
		MDRV_VIDEO_UPDATE(bioship)
	
		/* sound hardware */
		/* there's also a YM2203 */
		MDRV_SOUND_ADD(OKIM6295, okim6295_interface_dual)
	MACHINE_DRIVER_END
	
	
	/* bootleg using Raiden sound hardware */
	static MACHINE_DRIVER_START( tdragonb )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, 10000000)
		MDRV_CPU_MEMORY(tdragon_readmem,tdragonb_writemem)
		MDRV_CPU_VBLANK_INT(irq4_line_hold,1)
		MDRV_CPU_PERIODIC_INT(irq1_line_hold,112)/* ?? drives music */
	
		SEIBU_SOUND_SYSTEM_CPU(14318180/4)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_MACHINE_INIT(mustang_sound)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(256, 256)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(macross_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(macross)
		MDRV_VIDEO_EOF(nmk)
		MDRV_VIDEO_UPDATE(macross)
	
		/* sound hardware */
		SEIBU_SOUND_SYSTEM_YM3812_INTERFACE
	MACHINE_DRIVER_END
	
	static MACHINE_DRIVER_START( tdragon )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, 10000000)
		MDRV_CPU_MEMORY(tdragon_readmem,tdragon_writemem)
		MDRV_CPU_VBLANK_INT(irq4_line_hold,1)
		MDRV_CPU_PERIODIC_INT(irq1_line_hold,112)/* ?? drives music */
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_MACHINE_INIT(nmk16)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(256, 256)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(macross_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(macross)
		MDRV_VIDEO_EOF(nmk)
		MDRV_VIDEO_UPDATE(macross)
	
		/* sound hardware */
	MACHINE_DRIVER_END
	
	static MACHINE_DRIVER_START( ssmissin )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, 10000000)
		MDRV_CPU_MEMORY(ssmissin_readmem,ssmissin_writemem)
		MDRV_CPU_VBLANK_INT(irq4_line_hold,1)
		MDRV_CPU_PERIODIC_INT(irq1_line_hold,112) /* input related */
	
		MDRV_CPU_ADD(Z80, 4000000)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU) /* 4 MHz ? */
		MDRV_CPU_MEMORY(ssmissin_sound_readmem,ssmissin_sound_writemem)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_MACHINE_INIT(nmk16)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(256, 256)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(macross_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(macross)
		MDRV_VIDEO_EOF(nmk)
		MDRV_VIDEO_UPDATE(macross)
	
		/* sound hardware */
		MDRV_SOUND_ADD(OKIM6295, okim6295_interface_ssmissin)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( strahl )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, 12000000) /* 12 MHz ? */
		MDRV_CPU_MEMORY(strahl_readmem,strahl_writemem)
		MDRV_CPU_VBLANK_INT(nmk_interrupt,2)
		MDRV_CPU_PERIODIC_INT(irq1_line_hold,112)/* ???????? */
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_MACHINE_INIT(nmk16)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(256, 256)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(strahl_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(strahl)
		MDRV_VIDEO_EOF(nmk)
		MDRV_VIDEO_UPDATE(strahl)
	
		/* sound hardware */
		/* there's also a YM2203 */
		MDRV_SOUND_ADD(OKIM6295, okim6295_interface_dual)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( hachamf )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, 10000000) /* 10 MHz ? */
		MDRV_CPU_MEMORY(hachamf_readmem,hachamf_writemem)
		MDRV_CPU_VBLANK_INT(irq4_line_hold,1)
		MDRV_CPU_PERIODIC_INT(irq1_line_hold,112)/* ???????? */
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_MACHINE_INIT(nmk16)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(256, 256)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(macross_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(macross)
		MDRV_VIDEO_EOF(nmk)
		MDRV_VIDEO_UPDATE(macross)
	
		/* sound hardware */
		/* there's also a YM2203 */
		MDRV_SOUND_ADD(OKIM6295, okim6295_interface_dual)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( macross )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, 10000000) /* 10 MHz ? */
		MDRV_CPU_MEMORY(macross_readmem,macross_writemem)
		MDRV_CPU_VBLANK_INT(irq4_line_hold,1)
		MDRV_CPU_PERIODIC_INT(irq1_line_hold,112)/* ???????? */
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_MACHINE_INIT(nmk16)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(256, 256)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(macross_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(macross)
		MDRV_VIDEO_EOF(nmk)
		MDRV_VIDEO_UPDATE(macross)
	
		/* sound hardware */
		/* there's also a YM2203 */
		MDRV_SOUND_ADD(OKIM6295, okim6295_interface_dual)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( gunnail )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, 10000000) /* 10 MHz? */
		MDRV_CPU_MEMORY(gunnail_readmem,gunnail_writemem)
		MDRV_CPU_VBLANK_INT(irq4_line_hold,1)
		MDRV_CPU_PERIODIC_INT(irq1_line_hold,112)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_MACHINE_INIT(nmk16)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(512, 256)
		MDRV_VISIBLE_AREA(0*8, 48*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(macross_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(gunnail)
		MDRV_VIDEO_EOF(nmk)
		MDRV_VIDEO_UPDATE(gunnail)
	
		/* sound hardware */
		MDRV_SOUND_ADD(OKIM6295, okim6295_interface_dual)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( macross2 )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, 10000000) /* 10 MHz ? */
		MDRV_CPU_MEMORY(macross2_readmem,macross2_writemem)
		MDRV_CPU_VBLANK_INT(irq4_line_hold,1)
		MDRV_CPU_PERIODIC_INT(irq1_line_hold,112)/* ???????? */
	
		MDRV_CPU_ADD(Z80, 4000000)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU) /* 4 MHz ? */
		MDRV_CPU_MEMORY(macross2_sound_readmem,macross2_sound_writemem)
		MDRV_CPU_PORTS(macross2_sound_readport,macross2_sound_writeport)
	
		MDRV_FRAMES_PER_SECOND(57)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_MACHINE_INIT(nmk16)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(512, 256)
		MDRV_VISIBLE_AREA(0*8, 48*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(macross2_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(macross2)
		MDRV_VIDEO_EOF(nmk)
		MDRV_VIDEO_UPDATE(macross)
	
		/* sound hardware */
		MDRV_SOUND_ADD(YM2203, ym2203_interface_15)
		MDRV_SOUND_ADD(OKIM6295, okim6295_interface_dual)
	MACHINE_DRIVER_END
	
	static MACHINE_DRIVER_START( tdragon2 )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, 10000000) /* 10 MHz ? */
		MDRV_CPU_MEMORY(macross2_readmem,macross2_writemem)
		MDRV_CPU_VBLANK_INT(irq4_line_hold,1)
		MDRV_CPU_PERIODIC_INT(irq1_line_hold,112)/* ???????? */
	
		MDRV_CPU_ADD(Z80, 4000000)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU) /* 4 MHz ? */
		MDRV_CPU_MEMORY(macross2_sound_readmem,macross2_sound_writemem)
		MDRV_CPU_PORTS(macross2_sound_readport,macross2_sound_writeport)
	
		MDRV_FRAMES_PER_SECOND(57)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_MACHINE_INIT(nmk16)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(512, 256)
		MDRV_VISIBLE_AREA(0*8, 48*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(macross2_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(tdragon2)
		MDRV_VIDEO_EOF(nmk)
		MDRV_VIDEO_UPDATE(macross)
	
		/* sound hardware */
		MDRV_SOUND_ADD(YM2203, ym2203_interface_15)
		MDRV_SOUND_ADD(OKIM6295, okim6295_interface_dual)
	MACHINE_DRIVER_END
	
	static MACHINE_DRIVER_START( raphero )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, 14000000) /* 14 MHz measured */
		MDRV_CPU_MEMORY(macross2_readmem,raphero_writemem)
		MDRV_CPU_VBLANK_INT(irq4_line_hold,1)
		MDRV_CPU_PERIODIC_INT(irq1_line_hold,112)/* ???????? */
	
	//	MDRV_CPU_ADD(Z80, 4000000) // tmp90c841 ?
	//<ianpatt> looks like the tmp90c841 is a microcontroller from toshiba compatible with the z80 instruction set
	//<ianpatt> and luckily it isn't one of the versions with embedded ROM
	//	MDRV_CPU_FLAGS(CPU_AUDIO_CPU) /* 4 MHz ? */
	//	MDRV_CPU_MEMORY(macross2_sound_readmem,macross2_sound_writemem)
	//	MDRV_CPU_PORTS(macross2_sound_readport,macross2_sound_writeport)
	
		MDRV_FRAMES_PER_SECOND(56) // measured
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_MACHINE_INIT(nmk16)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(512, 256)
		MDRV_VISIBLE_AREA(0*8, 48*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(macross2_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(macross2)
		MDRV_VIDEO_EOF(nmk)
		MDRV_VIDEO_UPDATE(macross)
	
		/* sound hardware */
		MDRV_SOUND_ADD(YM2203, ym2203_interface_15)
		MDRV_SOUND_ADD(OKIM6295, okim6295_interface_dual)
	MACHINE_DRIVER_END
	
	static MACHINE_DRIVER_START( bjtwin )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(M68000, 10000000) /* 10 MHz? It's a P12, but xtals are 10MHz and 16MHz */
		MDRV_CPU_MEMORY(bjtwin_readmem,bjtwin_writemem)
		MDRV_CPU_VBLANK_INT(irq4_line_hold,1)
		MDRV_CPU_PERIODIC_INT(irq1_line_hold,112)/* ?? drives music */
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_MACHINE_INIT(nmk16)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(512, 256)
		MDRV_VISIBLE_AREA(0*8, 48*8-1, 2*8, 30*8-1)
		MDRV_GFXDECODE(bjtwin_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_VIDEO_START(bjtwin)
		MDRV_VIDEO_EOF(nmk)
		MDRV_VIDEO_UPDATE(bjtwin)
	
		/* sound hardware */
		MDRV_SOUND_ADD(OKIM6295, okim6295_interface_dual)
	MACHINE_DRIVER_END
	
	
	
	ROM_START ( urashima )
		ROM_REGION( 0x40000, REGION_CPU1, 0 );	/* 68000 code */
		ROM_LOAD16_BYTE( "um-2.15d",  0x00000, 0x20000, CRC(a90a47e3),SHA1(2f912001e9177cce8c3795f3d299115b80fdca4e) )
		ROM_LOAD16_BYTE( "um-1.15c",  0x00001, 0x20000, CRC(5f5c8f39),SHA1(cef663965c3112f87788d6a871e609c0b10ef9a2) )
	
		ROM_REGION( 0x020000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "um-5.22j",		0x000000, 0x020000, CRC(991776a2),SHA1(56740553d7d26aaeb9bec8557727030950bb01f7) )	/* 8x8 tiles */
	
		ROM_REGION( 0x080000, REGION_GFX2, ROMREGION_DISPOSE );/* 16x16 Tiles */
		ROM_LOAD( "um-7.4l",	0x000000, 0x080000, CRC(d2a68cfb),SHA1(eb6cb1fad306b697b2035a31ad48e8996722a032) )
	
		ROM_REGION( 0x080000, REGION_GFX3, ROMREGION_DISPOSE );/* Maybe there are no Sprites? */
		ROM_LOAD( "um-6.2l",	0x000000, 0x080000, CRC(076be5b5),SHA1(77444025f149a960137d3c79abecf9b30defa341) )
	
		ROM_REGION( 0x0240, REGION_PROMS, 0 );
		ROM_LOAD( "um-10.2b",      0x0000, 0x0100, CRC(cfdbb86c),SHA1(588822f6308a860937349c9106c2b4b1a75823ec) )	/* unknown */
		ROM_LOAD( "um-11.2c",      0x0100, 0x0100, CRC(ff5660cf),SHA1(a4635dcf9d6dd637ea4f36f1ad233db0bd039731) )	/* unknown */
		ROM_LOAD( "um-12.20c",     0x0200, 0x0020, CRC(bdb66b02),SHA1(8755244de638d7e835e35e08c62b0612958e6ca5) )	/* unknown */
		ROM_LOAD( "um-13.10l",     0x0220, 0x0020, CRC(4ce07ec0),SHA1(5f5744ddc7f258307f036fde4c0a8e6271b2d1f9) )	/* unknown */
	
		ROM_REGION( 0x080000, REGION_SOUND1, 0 );/* OKIM6295 samples */
		ROM_LOAD( "um-3.22c",		0x000000, 0x080000, CRC(9fd8c8fa),SHA1(0346f74c03a4daa7a84b64c9edf0e54297c82fd9) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_vandyke = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1, 0 );	/* 68000 code */
		ROM_LOAD16_BYTE( "vdk-1.16",  0x00000, 0x20000, CRC(c1d01c59),SHA1(04a7fd31ca4d87d078070390660edf08bf1d96b5) )
		ROM_LOAD16_BYTE( "vdk-2.15",  0x00001, 0x20000, CRC(9d741cc2),SHA1(2d101044fba5fc5b7d63869a0a053c42fdc2598b) )
	
		ROM_REGION(0x10000, REGION_CPU2, 0 );/* 64k for sound cpu code */
		ROM_LOAD( "vdk-4.127",    0x00000, 0x10000, CRC(eba544f0),SHA1(36f6d048d15a392542a9220a244d8a7049aaff8b) )
	
		ROM_REGION( 0x010000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "vdk-3.222",		0x000000, 0x010000, CRC(5a547c1b),SHA1(2d61f51ce2f91ebf0053ce3a00911d1bcbaba816) )	/* 8x8 tiles */
	
		ROM_REGION( 0x080000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "vdk-01.13",		0x000000, 0x080000, CRC(195a24be),SHA1(3a20dd746a87efc5c1fdc5025b709efeff82e05e) )	/* 16x16 tiles */
	
		ROM_REGION( 0x200000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD16_BYTE( "vdk-07.202",	0x000000, 0x080000, CRC(42d41f06),SHA1(69fd1d38187b8081f65acea2424bc1a0d455d90c) )	/* Sprites */
		ROM_LOAD16_BYTE( "vdk-06.203",	0x000001, 0x080000, CRC(d54722a8),SHA1(47f8e97b29ae0ff1a1d7d50734e4219a87a2ed57) )	/* Sprites */
		ROM_LOAD16_BYTE( "vdk-04.2-1",	0x100000, 0x080000, CRC(0a730547),SHA1(afac0549eb86d1fab5ca8ae2a0dad14144f55c02) )	/* Sprites */
		ROM_LOAD16_BYTE( "vdk-05.3-1",	0x100001, 0x080000, CRC(ba456d27),SHA1(5485a560ae2c2c8b6fdec314393c02a3de758ef3) )	/* Sprites */
	
		ROM_REGION( 0x080000, REGION_SOUND1, 0 );/* OKIM6295 samples */
		ROM_LOAD( "vdk-02.126",     0x000000, 0x080000, CRC(b2103274),SHA1(6bbdc912393607cd5306be946327c5ea0178c7a6) )	/* all banked */
	
		ROM_REGION( 0x080000, REGION_SOUND2, 0 );/* OKIM6295 samples */
		ROM_LOAD( "vdk-03.165",     0x000000, 0x080000, CRC(631776d3),SHA1(ffd76e5b03130252c55eaa6ae7edfee5632dae73) )	/* all banked */
	
		ROM_REGION( 0x0200, REGION_PROMS, 0 );
		ROM_LOAD( "ic100.bpr", 0x0000, 0x0100, CRC(98ed1c97),SHA1(f125ad05c3cbd1b1ab356161f9b1d814781d4c3b) )	/* V-sync hw (unused) */
		ROM_LOAD( "ic101.bpr", 0x0100, 0x0100, CRC(cfdbb86c),SHA1(588822f6308a860937349c9106c2b4b1a75823ec) )	/* H-sync hw (unused) */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_vandyjal = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1, 0 ); /* 68000 code */
		ROM_LOAD16_BYTE( "vdk-1.16",   0x00000, 0x20000, CRC(c1d01c59),SHA1(04a7fd31ca4d87d078070390660edf08bf1d96b5) )
		ROM_LOAD16_BYTE( "jaleco2.15", 0x00001, 0x20000, CRC(170e4d2e),SHA1(6009d19d30e345fea93e039d165061e2b20ff058) )
	
		ROM_REGION(0x10000, REGION_CPU2, 0 );/* 64k for sound cpu code */
		ROM_LOAD( "vdk-4.127",    0x00000, 0x10000, CRC(eba544f0),SHA1(36f6d048d15a392542a9220a244d8a7049aaff8b) )
	
		ROM_REGION( 0x010000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "vdk-3.222",		0x000000, 0x010000, CRC(5a547c1b),SHA1(2d61f51ce2f91ebf0053ce3a00911d1bcbaba816) )	/* 8x8 tiles */
	
		ROM_REGION( 0x080000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "vdk-01.13",		0x000000, 0x080000, CRC(195a24be),SHA1(3a20dd746a87efc5c1fdc5025b709efeff82e05e) )	/* 16x16 tiles */
	
		ROM_REGION( 0x200000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD16_BYTE( "vdk-07.202",	0x000000, 0x080000, CRC(42d41f06),SHA1(69fd1d38187b8081f65acea2424bc1a0d455d90c) )	/* Sprites */
		ROM_LOAD16_BYTE( "vdk-06.203",	0x000001, 0x080000, CRC(d54722a8),SHA1(47f8e97b29ae0ff1a1d7d50734e4219a87a2ed57) )	/* Sprites */
		ROM_LOAD16_BYTE( "vdk-04.2-1",	0x100000, 0x080000, CRC(0a730547),SHA1(afac0549eb86d1fab5ca8ae2a0dad14144f55c02) )	/* Sprites */
		ROM_LOAD16_BYTE( "vdk-05.3-1",	0x100001, 0x080000, CRC(ba456d27),SHA1(5485a560ae2c2c8b6fdec314393c02a3de758ef3) )	/* Sprites */
	
		ROM_REGION( 0x080000, REGION_SOUND1, 0 );/* OKIM6295 samples */
		ROM_LOAD( "vdk-02.126",     0x000000, 0x080000, CRC(b2103274),SHA1(6bbdc912393607cd5306be946327c5ea0178c7a6) )	/* all banked */
	
		ROM_REGION( 0x080000, REGION_SOUND2, 0 );/* OKIM6295 samples */
		ROM_LOAD( "vdk-03.165",     0x000000, 0x080000, CRC(631776d3),SHA1(ffd76e5b03130252c55eaa6ae7edfee5632dae73) )	/* all banked */
	
		ROM_REGION( 0x0200, REGION_PROMS, 0 );
		ROM_LOAD( "ic100.bpr", 0x0000, 0x0100, CRC(98ed1c97),SHA1(f125ad05c3cbd1b1ab356161f9b1d814781d4c3b) )	/* V-sync hw (unused) */
		ROM_LOAD( "ic101.bpr", 0x0100, 0x0100, CRC(cfdbb86c),SHA1(588822f6308a860937349c9106c2b4b1a75823ec) )	/* H-sync hw (unused) */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_tharrier = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1, 0 );
		ROM_LOAD16_BYTE( "2" ,   0x00000, 0x20000, CRC(78923aaa),SHA1(28338f49581180604403e1bd200f524fc4cb8b9f) )
		ROM_LOAD16_BYTE( "3" ,   0x00001, 0x20000, CRC(99cea259),SHA1(75abfb08b2358dd13809ade5a2dfffeb8b8df82c) )
	
		ROM_REGION( 0x010000, REGION_CPU2, 0 );
		ROM_LOAD( "12" ,   0x00000, 0x10000, CRC(b959f837),SHA1(073b14935e7d5b0cad19a3471fd26e9e3a363827) )
	
		ROM_REGION( 0x020000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "1" ,        0x000000, 0x10000, CRC(c7402e4a),SHA1(25cade2f8d4784887f0f51beb48b1e6b695629c2) )
	
		ROM_REGION( 0x080000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "89050-4" ,  0x000000, 0x80000, CRC(64d7d687),SHA1(dcfeac71fd577439e31cc1186b720388fbdc6ca0) )
	
		ROM_REGION( 0x100000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD16_BYTE( "89050-13",	0x000000, 0x80000, CRC(24db3fa4),SHA1(e0d76c479dfcacf03c04ec4760caecf3fd1e2ff7) )	/* Sprites */
		ROM_LOAD16_BYTE( "89050-17",	0x000001, 0x80000, CRC(7f715421),SHA1(bde5e0e1e22519e51ca0fd806909e90cc5b1c5b8) )
	
		ROM_REGION(0x80000, REGION_SOUND1, 0 );/* Oki sample data */
		ROM_LOAD( "89050-8",   0x00000, 0x80000, CRC(11ee4c39),SHA1(163295c385cff963a5bf87dc3e7bef6019e10ba8) )
	
		ROM_REGION(0x80000, REGION_SOUND2, 0 );/* Oki sample data */
		ROM_LOAD( "89050-10",  0x00000, 0x80000, CRC(893552ab),SHA1(b0a34291f4e482858ed295203ae031b17c2dbabc) )
	
		ROM_REGION( 0x140, REGION_PROMS, 0 );
		ROM_LOAD( "21.bpr",  0x00000, 0x100, CRC(fcd5efea),SHA1(cbda6b14127dabd1788cc256743cf62efaa5e8c4) )
		ROM_LOAD( "22.bpr",  0x00000, 0x100, CRC(cfdbb86c),SHA1(588822f6308a860937349c9106c2b4b1a75823ec) )
		ROM_LOAD( "23.bpr",  0x00000, 0x020, CRC(fc3569f4),SHA1(e1c498085e4ae9d0a995c94530544b0a5b760fbf) )
		ROM_LOAD( "24.bpr",  0x00000, 0x100, CRC(e0a009fe),SHA1(a66a27bb405d4ff8e4c0062273ee9b11e76ee520) )
		ROM_LOAD( "25.bpr",  0x00000, 0x100, CRC(e0a009fe),SHA1(a66a27bb405d4ff8e4c0062273ee9b11e76ee520) ) /* same as 24.bin */
		ROM_LOAD( "26.bpr",  0x00120, 0x020, CRC(0cbfb33e),SHA1(5dfee031a0a14bcd667fe2af2fa9cdfac3941d22) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_tharierj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1, 0 );
		ROM_LOAD16_BYTE( "2.bin" ,   0x00000, 0x20000, CRC(f3887a44),SHA1(4e5b660d33ba1d1e00263030efa67e2db376a234) )
		ROM_LOAD16_BYTE( "3.bin" ,   0x00001, 0x20000, CRC(65c247f6),SHA1(9f35f2b6f54814b4c4d23e2d78db8043e678fef2) )
	
		ROM_REGION( 0x010000, REGION_CPU2, 0 );
		ROM_LOAD( "12" ,   0x00000, 0x10000, CRC(b959f837),SHA1(073b14935e7d5b0cad19a3471fd26e9e3a363827) )
	
		ROM_REGION( 0x020000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "1.bin" ,        0x000000, 0x10000, CRC(005c26c3),SHA1(ee88d8f956b9b0a8ba5fb49c5c05f6ed6f01729c) )
	
		ROM_REGION( 0x080000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "89050-4" ,  0x000000, 0x80000, CRC(64d7d687),SHA1(dcfeac71fd577439e31cc1186b720388fbdc6ca0) )
	
		ROM_REGION( 0x100000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD16_BYTE( "89050-13",	0x000000, 0x80000, CRC(24db3fa4),SHA1(e0d76c479dfcacf03c04ec4760caecf3fd1e2ff7) )	/* Sprites */
		ROM_LOAD16_BYTE( "89050-17",	0x000001, 0x80000, CRC(7f715421),SHA1(bde5e0e1e22519e51ca0fd806909e90cc5b1c5b8) )
	
		ROM_REGION(0x80000, REGION_SOUND1, 0 );/* Oki sample data */
		ROM_LOAD( "89050-8",   0x00000, 0x80000, CRC(11ee4c39),SHA1(163295c385cff963a5bf87dc3e7bef6019e10ba8) )
	
		ROM_REGION(0x80000, REGION_SOUND2, 0 );/* Oki sample data */
		ROM_LOAD( "89050-10",  0x00000, 0x80000, CRC(893552ab),SHA1(b0a34291f4e482858ed295203ae031b17c2dbabc) )
	
		ROM_REGION( 0x140, REGION_PROMS, 0 );
		ROM_LOAD( "21.bpr",  0x00000, 0x100, CRC(fcd5efea),SHA1(cbda6b14127dabd1788cc256743cf62efaa5e8c4) )
		ROM_LOAD( "22.bpr",  0x00000, 0x100, CRC(cfdbb86c),SHA1(588822f6308a860937349c9106c2b4b1a75823ec) )
		ROM_LOAD( "23.bpr",  0x00000, 0x020, CRC(fc3569f4),SHA1(e1c498085e4ae9d0a995c94530544b0a5b760fbf) )
		ROM_LOAD( "24.bpr",  0x00000, 0x100, CRC(e0a009fe),SHA1(a66a27bb405d4ff8e4c0062273ee9b11e76ee520) )
		ROM_LOAD( "25.bpr",  0x00000, 0x100, CRC(e0a009fe),SHA1(a66a27bb405d4ff8e4c0062273ee9b11e76ee520) ) /* same as 24.bin */
		ROM_LOAD( "26.bpr",  0x00120, 0x020, CRC(0cbfb33e),SHA1(5dfee031a0a14bcd667fe2af2fa9cdfac3941d22) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_mustang = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1, 0 );
		ROM_LOAD16_BYTE( "2.bin",    0x00000, 0x20000, CRC(bd9f7c89),SHA1(a0af46a8ff82b90bece2515e1bd74e7a7ddf5379) )
		ROM_LOAD16_BYTE( "3.bin",    0x00001, 0x20000, CRC(0eec36a5),SHA1(c549fbcd3e2741a6d0f2633ded6a85909d37f633) )
	
		ROM_REGION(0x10000, REGION_CPU2, 0 );/* 64k for sound cpu code */
		ROM_LOAD( "90058-7",    0x00000, 0x10000, CRC(920a93c8),SHA1(7660ca419e2fd98848ae7f5994994eaed023151e) )
	
		ROM_REGION( 0x020000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "90058-1",    0x00000, 0x20000, CRC(81ccfcad),SHA1(70a0f769c0d4588f6f17bd52cc86a745f30e9f00) )
	
		ROM_REGION( 0x080000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "90058-4",    0x000000, 0x80000, CRC(a07a2002),SHA1(55720d84a251c33c52ae8c33aa41ff8ac9727941) )
	
		ROM_REGION( 0x100000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD16_BYTE( "90058-8",    0x00000, 0x80000, CRC(560bff04),SHA1(b005642adc81d878971ecbdead8ef5e604c90ae2) )
		ROM_LOAD16_BYTE( "90058-9",    0x00001, 0x80000, CRC(b9d72a03),SHA1(43ee9def1b6c491c6832562d66c1af54d81d9b3c) )
	
		ROM_REGION( 0x080000, REGION_SOUND1, 0 );/* OKIM6295 samples */
		ROM_LOAD( "90058-5",    0x000000, 0x80000, CRC(c60c883e),SHA1(8a01950cad820b2e781ec81cd12737829edc4f19) )
	
		ROM_REGION( 0x080000, REGION_SOUND2, 0 );/* OKIM6295 samples */
		ROM_LOAD( "90058-6",    0x000000, 0x80000, CRC(233c1776),SHA1(7010a2f914611698a65bf4f22bc1753a9ed26277) )
	
		ROM_REGION( 0x200, REGION_PROMS, 0 );
		ROM_LOAD( "10.bpr",  0x00000, 0x100, CRC(633ab1c9),SHA1(acd99fcca41eaab7948ca84988352f1d7d519c61) ) /* unknown */
		ROM_LOAD( "90058-11",  0x00100, 0x100, CRC(cfdbb86c),SHA1(588822f6308a860937349c9106c2b4b1a75823ec) ) /* unknown */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_mustangs = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1, 0 );
		ROM_LOAD16_BYTE( "90058-2",    0x00000, 0x20000, CRC(833aa458),SHA1(a9924f7044397e3a36c674b064173ffae80a79ec) )
		ROM_LOAD16_BYTE( "90058-3",    0x00001, 0x20000, CRC(e4b80f06),SHA1(ce589cebb5ea85c89eb44796b821a4bd0c44b9a8) )
	
		ROM_REGION(0x10000, REGION_CPU2, 0 );/* 64k for sound cpu code */
		ROM_LOAD( "90058-7",    0x00000, 0x10000, CRC(920a93c8),SHA1(7660ca419e2fd98848ae7f5994994eaed023151e) )
	
		ROM_REGION( 0x020000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "90058-1",    0x00000, 0x20000, CRC(81ccfcad),SHA1(70a0f769c0d4588f6f17bd52cc86a745f30e9f00) )
	
		ROM_REGION( 0x080000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "90058-4",    0x000000, 0x80000, CRC(a07a2002),SHA1(55720d84a251c33c52ae8c33aa41ff8ac9727941) )
	
		ROM_REGION( 0x100000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD16_BYTE( "90058-8",    0x00000, 0x80000, CRC(560bff04),SHA1(b005642adc81d878971ecbdead8ef5e604c90ae2) )
		ROM_LOAD16_BYTE( "90058-9",    0x00001, 0x80000, CRC(b9d72a03),SHA1(43ee9def1b6c491c6832562d66c1af54d81d9b3c) )
	
		ROM_REGION( 0x080000, REGION_SOUND1, 0 );/* OKIM6295 samples */
		ROM_LOAD( "90058-5",    0x000000, 0x80000, CRC(c60c883e),SHA1(8a01950cad820b2e781ec81cd12737829edc4f19) )
	
		ROM_REGION( 0x080000, REGION_SOUND2, 0 );/* OKIM6295 samples */
		ROM_LOAD( "90058-6",    0x000000, 0x80000, CRC(233c1776),SHA1(7010a2f914611698a65bf4f22bc1753a9ed26277) )
	
		ROM_REGION( 0x200, REGION_PROMS, 0 );
		ROM_LOAD( "90058-10",  0x00000, 0x100, CRC(de156d99),SHA1(07b70deca74e23bab7c13e5e9aee32d0dbb06509) ) /* unknown */
		ROM_LOAD( "90058-11",  0x00100, 0x100, CRC(cfdbb86c),SHA1(588822f6308a860937349c9106c2b4b1a75823ec) ) /* unknown */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_mustangb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1, 0 );
		ROM_LOAD16_BYTE( "mustang.14",    0x00000, 0x20000, CRC(13c6363b),SHA1(e2c1985d1c8ec9751c47cd7e1b85e007f3aeb6fd) )
		ROM_LOAD16_BYTE( "mustang.13",    0x00001, 0x20000, CRC(d8ccce31),SHA1(e8e3e34a480fcd298f11833c6c968c5df77c0e2a) )
	
		ROM_REGION(0x20000, REGION_CPU2, 0 );/* 64k for sound cpu code */
		ROM_LOAD( "mustang.16",    0x00000, 0x8000, CRC(99ee7505),SHA1(b97c8ee5e26e8554b5de506fba3b32cc2fde53c9) )
		ROM_CONTINUE(             0x010000, 0x08000 );
		ROM_COPY( REGION_CPU2, 0, 0x018000, 0x08000 );
	
		ROM_REGION( 0x020000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "90058-1",    0x00000, 0x20000, CRC(81ccfcad),SHA1(70a0f769c0d4588f6f17bd52cc86a745f30e9f00) )
	
		ROM_REGION( 0x080000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "90058-4",    0x000000, 0x80000, CRC(a07a2002),SHA1(55720d84a251c33c52ae8c33aa41ff8ac9727941) )
	
		ROM_REGION( 0x100000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD16_BYTE( "90058-8",    0x00000, 0x80000, CRC(560bff04),SHA1(b005642adc81d878971ecbdead8ef5e604c90ae2) )
		ROM_LOAD16_BYTE( "90058-9",    0x00001, 0x80000, CRC(b9d72a03),SHA1(43ee9def1b6c491c6832562d66c1af54d81d9b3c) )
	
		ROM_REGION( 0x010000, REGION_SOUND1, 0 );/* OKIM6295 samples */
		ROM_LOAD( "mustang.17",    0x00000, 0x10000, CRC(f6f6c4bf),SHA1(ea4cf74d968e254ae47c16c2f4c2f4bc1a528808) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_acrobatm = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1, 0 );
		ROM_LOAD16_BYTE( "02_ic100.bin",    0x00000, 0x20000, CRC(3fe487f4),SHA1(29aba5debcfddff14e584a1c7c5a403e85fc6ec0) )
		ROM_LOAD16_BYTE( "01_ic101.bin",    0x00001, 0x20000, CRC(17175753),SHA1(738865744badb78a0414ff650a94b97e516d0ea0) )
	
		ROM_REGION( 0x20000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "03_ic79.bin",   0x000000, 0x10000, CRC(d86c186e),SHA1(2e263d4780f2ba7acc7faa88472c85216fbae6a3) ) /* Characters */
	
		ROM_REGION( 0x100000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "09_ic8.bin",  0x000000, 0x100000, CRC(7c12afed),SHA1(ae793e41599355a126cbcce91cd2c9f212d21853) ) /* Foreground */
	
		ROM_REGION( 0x180000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD( "07_ic42.bin",  0x000000, 0x100000, CRC(5672bdaa),SHA1(5401a104d72904de19b73125451767bc63d36809) ) /* Sprites */
		ROM_LOAD( "08_ic29.bin",  0x100000, 0x080000, CRC(b4c0ace3),SHA1(5d638781d588cfbf4025d002d5a2309049fe1ee5) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );
		ROM_LOAD( "04_ic74.bin",    0x00000, 0x10000, CRC(176905fb),SHA1(135a184f44bedd93b293b9124fa0bd725e0ee93b) )
	
		ROM_REGION( 0x80000, REGION_SOUND1, 0 );/* OKIM6295 samples */
		ROM_LOAD( "05_ic54.bin",    0x00000, 0x80000, CRC(3b8c2b0e),SHA1(72491da32512823540b67dc5027f21c74af08c7d) )
	
		ROM_REGION( 0x80000, REGION_SOUND2, 0 );/* OKIM6295 samples */
		ROM_LOAD( "06_ic53.bin",    0x00000, 0x80000, CRC(c1517cd4),SHA1(5a91ddc608c7a6fbdd9f93e503d39eac02ef04a4) )
	
		ROM_REGION( 0x0200, REGION_PROMS, 0 );
		ROM_LOAD( "10_ic81.bin",    0x0000, 0x0100, CRC(cfdbb86c),SHA1(588822f6308a860937349c9106c2b4b1a75823ec) )	/* unknown */
		ROM_LOAD( "11_ic80.bin",    0x0100, 0x0100, CRC(633ab1c9),SHA1(acd99fcca41eaab7948ca84988352f1d7d519c61) )	/* unknown */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_bioship = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1, 0 );
		ROM_LOAD16_BYTE( "2",    0x00000, 0x20000, CRC(acf56afb),SHA1(0e8ec494ab406cfee24cf586059878332265de75) )
		ROM_LOAD16_BYTE( "1",    0x00001, 0x20000, CRC(820ef303),SHA1(d2ef29557b05abf8ae79a2c7ce0d15a91b36eeff) )
	
		ROM_REGION( 0x20000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "7",         0x000000, 0x10000, CRC(2f3f5a10),SHA1(c1006eb755eec75f69dc7972d78d0c59088eb140) ) /* Characters */
	
		ROM_REGION( 0x80000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "sbs-g.01",  0x000000, 0x80000, CRC(21302e78),SHA1(a17939c0529c8e9ec2a4edd5e6be4bcb67f86787) ) /* Foreground */
	
		ROM_REGION( 0x80000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD( "sbs-g.03",  0x000000, 0x80000, CRC(60e00d7b),SHA1(36fd02a7842ce1e79b8c4cfbe9c97052bef4aa62) ) /* Sprites */
	
		ROM_REGION( 0x80000, REGION_GFX4, ROMREGION_DISPOSE );
		ROM_LOAD( "sbs-g.02",  0x000000, 0x80000, CRC(f31eb668),SHA1(67d6d56ea203edfbae4db658399bf61f14134206) ) /* Background */
	
		ROM_REGION16_BE(0x20000, REGION_GFX5, 0 );/* Background tilemaps (used at runtime) */
		ROM_LOAD16_BYTE( "8",    0x00000, 0x10000, CRC(75a46fea),SHA1(3d78cfc482b42779bb5aedb722c4a39cbc71bd10) )
		ROM_LOAD16_BYTE( "9",    0x00001, 0x10000, CRC(d91448ee),SHA1(7f84ca3605edcab4bf226dab8dd7218cd5c3e5a4) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );
		ROM_LOAD( "6",    0x00000, 0x10000, CRC(5f39a980),SHA1(2a440f86685249f9c317634cad8cdedc8a8f1491) )
	
		ROM_REGION(0x80000, REGION_SOUND1, 0 );/* Oki sample data */
		ROM_LOAD( "sbs-g.04",    0x00000, 0x80000, CRC(7c74cc4e),SHA1(92097b372eacabdb9e8e261b0bc4223821ff9273) )
	
		ROM_REGION(0x80000, REGION_SOUND2, 0 );/* Oki sample data */
		ROM_LOAD( "sbs-g.05",    0x00000, 0x80000, CRC(f0a782e3),SHA1(d572226b8e597f1c34d246cb284e047a6e2d9290) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_blkheart = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );	/* 68000 code */
		ROM_LOAD16_BYTE( "blkhrt.7",  0x00000, 0x20000, CRC(5bd248c0),SHA1(0649f4f8682404aeb3fc80643fcabc2d7836bb23) )
		ROM_LOAD16_BYTE( "blkhrt.6",  0x00001, 0x20000, CRC(6449e50d),SHA1(d8cd126d921c95478346da96c20da01212395d77) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );	/* Code for (unknown?) CPU */
		ROM_LOAD( "4.bin",      0x00000, 0x10000, CRC(7cefa295),SHA1(408f46613b3620cee31dec43281688d231b47ddd) )
	
		ROM_REGION( 0x020000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "3.bin",    0x000000, 0x020000, CRC(a1ab3a16),SHA1(3fb57c9d2ef94ee188cbadd70378ae6f4407e71d) )	/* 8x8 tiles */
	
		ROM_REGION( 0x100000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "90068-5.bin", 0x000000, 0x100000, CRC(a1ab4f24),SHA1(b9f8104d53eda87ccd4000d049ee74ac9aa20b3e) )	/* 16x16 tiles */
	
		ROM_REGION( 0x100000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD16_WORD_SWAP( "90068-8.bin", 0x000000, 0x100000, CRC(9d3204b2) SHA1(b37a246ad37f9ce092b371f01122ddf2bc8b2db6) )	/* Sprites */
	
		ROM_REGION( 0x080000, REGION_SOUND1, 0 );/* OKIM6295 samples */
		ROM_LOAD( "90068-1.bin", 0x000000, 0x080000, CRC(e7af69d2),SHA1(da050880e186954bcf0e0adf00750dd5a371551b) )	/* all banked */
	
		ROM_REGION( 0x080000, REGION_SOUND2, 0 );/* OKIM6295 samples */
		ROM_LOAD( "90068-2.bin", 0x000000, 0x080000, CRC(3a583184),SHA1(9226f1ea7725e4b48bb055d1c17389cf960d75f8) )	/* all banked */
	
		ROM_REGION( 0x0200, REGION_PROMS, 0 );
		ROM_LOAD( "9.bpr",      0x0000, 0x0100, CRC(98ed1c97),SHA1(f125ad05c3cbd1b1ab356161f9b1d814781d4c3b) )	/* unknown */
		ROM_LOAD( "10.bpr",     0x0100, 0x0100, CRC(cfdbb86c),SHA1(588822f6308a860937349c9106c2b4b1a75823ec) )	/* unknown */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_blkhearj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );	/* 68000 code */
		ROM_LOAD16_BYTE( "7.bin",  0x00000, 0x20000, CRC(e0a5c667),SHA1(3ef39b2dc1f7ffdddf586f0b3080ecd1f362ec37) )
		ROM_LOAD16_BYTE( "6.bin",  0x00001, 0x20000, CRC(7cce45e8),SHA1(72491e30d1f9be2eede21fdde5a7484d4f65cfbf) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );	/* Code for (unknown?) CPU */
		ROM_LOAD( "4.bin",      0x00000, 0x10000, CRC(7cefa295),SHA1(408f46613b3620cee31dec43281688d231b47ddd) )
	
		ROM_REGION( 0x020000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "3.bin",    0x000000, 0x020000, CRC(a1ab3a16),SHA1(3fb57c9d2ef94ee188cbadd70378ae6f4407e71d) )	/* 8x8 tiles */
	
		ROM_REGION( 0x100000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "90068-5.bin", 0x000000, 0x100000, CRC(a1ab4f24),SHA1(b9f8104d53eda87ccd4000d049ee74ac9aa20b3e) )	/* 16x16 tiles */
	
		ROM_REGION( 0x100000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD16_WORD_SWAP( "90068-8.bin", 0x000000, 0x100000, CRC(9d3204b2) SHA1(b37a246ad37f9ce092b371f01122ddf2bc8b2db6) )	/* Sprites */
	
		ROM_REGION( 0x080000, REGION_SOUND1, 0 );/* OKIM6295 samples */
		ROM_LOAD( "90068-1.bin", 0x000000, 0x080000, CRC(e7af69d2),SHA1(da050880e186954bcf0e0adf00750dd5a371551b) )	/* all banked */
	
		ROM_REGION( 0x080000, REGION_SOUND2, 0 );/* OKIM6295 samples */
		ROM_LOAD( "90068-2.bin", 0x000000, 0x080000, CRC(3a583184),SHA1(9226f1ea7725e4b48bb055d1c17389cf960d75f8) )	/* all banked */
	
		ROM_REGION( 0x0200, REGION_PROMS, 0 );
		ROM_LOAD( "9.bpr",      0x0000, 0x0100, CRC(98ed1c97),SHA1(f125ad05c3cbd1b1ab356161f9b1d814781d4c3b) )	/* unknown */
		ROM_LOAD( "10.bpr",     0x0100, 0x0100, CRC(cfdbb86c),SHA1(588822f6308a860937349c9106c2b4b1a75823ec) )	/* unknown */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_tdragon = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );	/* 68000 code -bitswapped- */
		ROM_LOAD16_BYTE( "thund.8",  0x00000, 0x20000, CRC(edd02831),SHA1(d6bc8d2c37707768a8bf666090f33eea12dda336) )
		ROM_LOAD16_BYTE( "thund.7",  0x00001, 0x20000, CRC(52192fe5),SHA1(9afef197410e7feb71dc48003e181fbbaf5c99b2) )
	
		ROM_REGION( 0x020000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "thund.6",		0x000000, 0x20000, CRC(fe365920),SHA1(7581931cb95cd5a8ed40e4f5385b533e3d19af22) )	/* 8x8 tiles */
	
		ROM_REGION( 0x100000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "thund.5",		0x000000, 0x100000, CRC(d0bde826),SHA1(3b74d5fc88a4a9329e101ee72f393608d327d816) )	/* 16x16 tiles */
	
		ROM_REGION( 0x100000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD16_WORD_SWAP( "thund.4",	0x000000, 0x100000, CRC(3eedc2fe) SHA1(9f48986c231a8fbc07f2b39b2017d1e967b2ed3c) )	/* Sprites */
	
		ROM_REGION( 0x010000, REGION_CPU2, 0 );	/* Code for (unknown?) CPU */
		ROM_LOAD( "thund.1",      0x00000, 0x10000, CRC(bf493d74),SHA1(6f8f5eff4b71fb6cabda10075cfa88a3f607859e) )
	
		ROM_REGION( 0x100000, REGION_SOUND1, 0 );/* OKIM6295 samples? */
		ROM_LOAD( "thund.2",     0x00000, 0x80000, CRC(ecfea43e),SHA1(d664dfa6698fec8e602523bdae16068f1ff6547b) )
		ROM_LOAD( "thund.3",     0x80000, 0x80000, CRC(ae6875a8),SHA1(bfdb350b3d3fce2bead1ac60875beafe427765ed) )
	
		ROM_REGION( 0x0200, REGION_PROMS, 0 );
		ROM_LOAD( "9.bin",  0x0000, 0x0100, CRC(cfdbb86c),SHA1(588822f6308a860937349c9106c2b4b1a75823ec) )	/* unknown */
		ROM_LOAD( "10.bin", 0x0100, 0x0100, CRC(e6ead349),SHA1(6d81b1c0233580aa48f9718bade42d640e5ef3dd) )	/* unknown */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_tdragonb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );	/* 68000 code -bitswapped- */
		ROM_LOAD16_BYTE( "td_04.bin",  0x00000, 0x20000, CRC(e8a62d3e),SHA1(dd221bcd80149fffb1bdddfd3d394996bd2f8ec5) )
		ROM_LOAD16_BYTE( "td_03.bin",  0x00001, 0x20000, CRC(2fa1aa04),SHA1(ddf2b2ff179c31a1677d15d0403b00d77f9f0a6c) )
	
		ROM_REGION(0x20000, REGION_CPU2, 0 );/* 64k for sound cpu code */
		ROM_LOAD( "td_02.bin",    0x00000, 0x8000, CRC(99ee7505),SHA1(b97c8ee5e26e8554b5de506fba3b32cc2fde53c9) )
		ROM_CONTINUE(             0x010000, 0x08000 );
		ROM_COPY( REGION_CPU2, 0, 0x018000, 0x08000 );
	
		ROM_REGION( 0x020000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "td_08.bin",		0x000000, 0x20000, CRC(5144dc69),SHA1(e64d88dc0e7672f811868621f74ec209aeafbc6f) )	/* 8x8 tiles */
	
		ROM_REGION( 0x100000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "td_06.bin",		0x000000, 0x80000, CRC(c1be8a4d),SHA1(6269fd7fccf1546a01bab755d8b6b7dcffc1166e) )	/* 16x16 tiles */
		ROM_LOAD( "td_07.bin",		0x080000, 0x80000, CRC(2c3e371f),SHA1(77956425661f4f81c370fff63845d42057fcaec3) )	/* 16x16 tiles */
	
		ROM_REGION( 0x100000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD16_BYTE( "td_10.bin",	0x000000, 0x080000, CRC(bfd0ec5d),SHA1(7983661f74e8695f56e45c6e5c278d7d86431052) )	/* Sprites */
		ROM_LOAD16_BYTE( "td_09.bin",	0x000001, 0x080000, CRC(b6e074eb),SHA1(bdde068f03415391b5edaa42f1389df0f7eef899) )	/* Sprites */
	
		ROM_REGION( 0x010000, REGION_SOUND1, 0 );/* OKIM6295 samples */
		ROM_LOAD( "td_01.bin",     0x00000, 0x10000, CRC(f6f6c4bf),SHA1(ea4cf74d968e254ae47c16c2f4c2f4bc1a528808) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ssmissin = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1, 0 );
		ROM_LOAD16_BYTE( "ssm14.165",    0x00001, 0x20000, CRC(eda61b74),SHA1(6247682c27d2be7dff1fad407ccf86fe2a25f11c) )
		ROM_LOAD16_BYTE( "ssm15.166",    0x00000, 0x20000, CRC(aff15927),SHA1(258c2722ac7ca50360bfefa7b4e621373975a835) )
	
		ROM_REGION( 0x020000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "ssm16.172",		0x000000, 0x20000, CRC(5cf6eb1f),SHA1(d406b11cf06ae1afc57a50685689e358e5677a45) )	/* 8x8 tiles */
	
		ROM_REGION( 0x100000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "ssm17.147",		0x000000, 0x080000, CRC(c9c28455),SHA1(6a3e754aff3f368bde0e8905c33074084ad6ac30) )	/* 16x16 tiles */
		ROM_LOAD( "ssm18.148",		0x080000, 0x080000, CRC(ebfdaad6),SHA1(0814cdfe83f36a7dd7b5416f9d0478192733dac0) )	/* 16x16 tiles */
	
		ROM_REGION( 0x100000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD16_BYTE( "ssm20.34",		0x000001, 0x080000, CRC(a0c16c4d),SHA1(e198f69b4d8660e33851a2631b5411611b1b2ea6) )	/* 16x16 tiles */
		ROM_LOAD16_BYTE( "ssm19.33",		0x000000, 0x080000, CRC(b1943657),SHA1(97c05483b634315af338434bd2f565cc151a7283) )	/* 16x16 tiles */
	
		ROM_REGION( 0x010000, REGION_CPU2, 0 );	/* Code for Sound CPU */
		ROM_LOAD( "ssm11.188",      0x00000, 0x08000, CRC(8be6dce3),SHA1(d9a235c36e0bc44025c291247d6b0b753e4bc0c8) )
	
		ROM_REGION( 0x100000, REGION_SOUND1, 0 );/* OKIM6295 samples? */
		ROM_LOAD( "ssm13.190",     0x00000, 0x20000, CRC(618f66f0),SHA1(97637a03d9fd82305e872e9bfa489862c974bb6c) )
		ROM_LOAD( "ssm12.189",     0x80000, 0x80000, CRC(e8219c83),SHA1(68673d071a58ca2bfd2de344a830417d10bc5757) ) /* banked */
	
		ROM_REGION( 0x0300, REGION_PROMS, 0 );
		ROM_LOAD( "ssm-pr2.113",  0x0000, 0x0100, CRC(cfdbb86c),SHA1(588822f6308a860937349c9106c2b4b1a75823ec) )	/* unknown */
		ROM_LOAD( "ssm-pr1.114",  0x0100, 0x0200, CRC(ed0bd072),SHA1(66a6d435d8587c82ae96dd09c39ed5749fe00e24) )	/* unknown */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_strahl = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1, 0 );
		ROM_LOAD16_BYTE( "strahl-2.82", 0x00000, 0x20000, CRC(c9d008ae),SHA1(e9218a3143d5887e702df051354a9083a806c69c) )
		ROM_LOAD16_BYTE( "strahl-1.83", 0x00001, 0x20000, CRC(afc3c4d6),SHA1(ab3dd7db692eb01e3a87f4216d322a702f3beaad) )
	
		ROM_REGION( 0x20000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "strahl-3.73",  0x000000, 0x10000, CRC(2273b33e),SHA1(fa53e91b80dfea3f8b2c1f0ce66e5c6920c4960f) ) /* Characters */
	
		ROM_REGION( 0x40000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "str7b2r0.275", 0x000000, 0x40000, CRC(5769e3e1),SHA1(7d7a16b11027d0a7618df1ec1e3484224b772e90) ) /* Tiles */
	
		ROM_REGION( 0x180000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD( "strl3-01.32",  0x000000, 0x80000, CRC(d8337f15),SHA1(4df23fff2506b66a94dae4e0cf7d25499936b942) ) /* Sprites */
		ROM_LOAD( "strl4-02.57",  0x080000, 0x80000, CRC(2a38552b),SHA1(82335fc6aa3de9145dd84952e5ed423493bf7141) )
		ROM_LOAD( "strl5-03.58",  0x100000, 0x80000, CRC(a0e7d210),SHA1(96a762a3a1cdeaa91bde50429e0ac665fb81190b) )
	
		ROM_REGION( 0x80000, REGION_GFX4, ROMREGION_DISPOSE );
		ROM_LOAD( "str6b1w1.776", 0x000000, 0x80000, CRC(bb1bb155),SHA1(83a02e89180e15f0e7817e0e92b4bf4e209bb69a) ) /* Tiles */
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );
		ROM_LOAD( "strahl-4.66",    0x00000, 0x10000, CRC(60a799c4),SHA1(8ade3cf827a389f7cb4080957dc4d67077ea4166) )
	
		ROM_REGION( 0x80000, REGION_SOUND1, 0 );/* Oki sample data */
		ROM_LOAD( "str8pmw1.540",    0x00000, 0x80000, CRC(01d6bb6a),SHA1(b157f6f921483ed8067a7e13e370f73fdb60d136) )
	
		ROM_REGION( 0x80000, REGION_SOUND2, 0 );/* Oki sample data */
		ROM_LOAD( "str9pew1.639",    0x00000, 0x80000, CRC(6bb3eb9f),SHA1(9c1394df4f8a08f9098c85eb3d38fb862d6eabbb) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_strahla = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1, 0 );
		ROM_LOAD16_BYTE( "rom2", 0x00000, 0x20000, CRC(f80a22ef),SHA1(22099eb0bbb445702e0276713c3e48d60de60c30) )
		ROM_LOAD16_BYTE( "rom1", 0x00001, 0x20000, CRC(802ecbfc),SHA1(cc776023c7bd6b6d6af9659a0c822a2887e50199) )
	
		ROM_REGION( 0x20000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "strahl-3.73",  0x000000, 0x10000, CRC(2273b33e),SHA1(fa53e91b80dfea3f8b2c1f0ce66e5c6920c4960f) ) /* Characters */
	
		ROM_REGION( 0x40000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "str7b2r0.275", 0x000000, 0x40000, CRC(5769e3e1),SHA1(7d7a16b11027d0a7618df1ec1e3484224b772e90) ) /* Tiles */
	
		ROM_REGION( 0x180000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD( "strl3-01.32",  0x000000, 0x80000, CRC(d8337f15),SHA1(4df23fff2506b66a94dae4e0cf7d25499936b942) ) /* Sprites */
		ROM_LOAD( "strl4-02.57",  0x080000, 0x80000, CRC(2a38552b),SHA1(82335fc6aa3de9145dd84952e5ed423493bf7141) )
		ROM_LOAD( "strl5-03.58",  0x100000, 0x80000, CRC(a0e7d210),SHA1(96a762a3a1cdeaa91bde50429e0ac665fb81190b) )
	
		ROM_REGION( 0x80000, REGION_GFX4, ROMREGION_DISPOSE );
		ROM_LOAD( "str6b1w1.776", 0x000000, 0x80000, CRC(bb1bb155),SHA1(83a02e89180e15f0e7817e0e92b4bf4e209bb69a) ) /* Tiles */
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );
		ROM_LOAD( "strahl-4.66",    0x00000, 0x10000, CRC(60a799c4),SHA1(8ade3cf827a389f7cb4080957dc4d67077ea4166) )
	
		ROM_REGION( 0x80000, REGION_SOUND1, 0 );/* Oki sample data */
		ROM_LOAD( "str8pmw1.540",    0x00000, 0x80000, CRC(01d6bb6a),SHA1(b157f6f921483ed8067a7e13e370f73fdb60d136) )
	
		ROM_REGION( 0x80000, REGION_SOUND2, 0 );/* Oki sample data */
		ROM_LOAD( "str9pew1.639",    0x00000, 0x80000, CRC(6bb3eb9f),SHA1(9c1394df4f8a08f9098c85eb3d38fb862d6eabbb) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_hachamf = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1, 0 );	/* 68000 code */
		ROM_LOAD16_BYTE( "hmf_07.rom",  0x00000, 0x20000, CRC(9d847c31),SHA1(1d370d8db9cadadb9c2cb213e32f681947d81b7f) )
		ROM_LOAD16_BYTE( "hmf_06.rom",  0x00001, 0x20000, CRC(de6408a0),SHA1(2df77fecd44d2d8b0444abd4545923213ed76b2d) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );	/* unknown  - sound cpu ?????? */
		ROM_LOAD( "hmf_01.rom",  0x00000, 0x10000, CRC(9e6f48fc),SHA1(aeb5bfecc025b5478f6de874792fc0f7f54932be) )
	
		ROM_REGION( 0x020000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "hmf_05.rom",  0x000000, 0x020000, CRC(29fb04a2),SHA1(9654b90a66d0e2a0f9cd369cab29cdd0c6f77869) )	/* 8x8 tiles */
	
		ROM_REGION( 0x080000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "hmf_04.rom",  0x000000, 0x080000, CRC(05a624e3),SHA1(e1b686b36c0adedfddf70eeb6411671bbcd897d8) )	/* 16x16 tiles */
	
		ROM_REGION( 0x100000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD16_WORD_SWAP( "hmf_08.rom",  0x000000, 0x100000, CRC(7fd0f556) SHA1(d1b4bec0946869d3d7bcb870d9ae3bd17395a231) )	/* Sprites */
	
		ROM_REGION( 0x080000, REGION_SOUND1, 0 );/* OKIM6295 samples */
		ROM_LOAD( "hmf_02.rom",  0x000000, 0x080000, CRC(3f1e67f2),SHA1(413e78587d8a043a0eb94447313ba1b3c5b35be5) )
	
		ROM_REGION( 0x080000, REGION_SOUND2, 0 );/* OKIM6295 samples */
		ROM_LOAD( "hmf_03.rom",  0x000000, 0x080000, CRC(b25ed93b),SHA1(d7bc686bbccf982f40420a11158aa8e5dd4207c5) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_macross = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );	/* 68000 code */
		ROM_LOAD16_WORD_SWAP( "921a03",        0x00000, 0x80000, CRC(33318d55) SHA1(c99f85e09bd334dc8ce138b08cbed2331b0d67dd) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );	/* sound program (unknown CPU) */
		ROM_LOAD( "921a02",      0x00000, 0x10000, CRC(77c082c7),SHA1(be07aa14d0116f830f98e11a19f1debb48a5230e) )
	
		ROM_REGION( 0x020000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "921a01",      0x000000, 0x020000, CRC(bbd8242d),SHA1(7cf4897be1278e1190f499f00bc78384817a5160) )	/* 8x8 tiles */
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "921a04",      0x000000, 0x200000, CRC(4002e4bb),SHA1(281433d798ac85c84d4f1f3751a3032e8a3b5cd4) )	/* 16x16 tiles */
	
		ROM_REGION( 0x200000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD16_WORD_SWAP( "921a07",      0x000000, 0x200000, CRC(7d2bf112) SHA1(1997c99c2d3998096842abd1cee89e0e6ab43a47) )	/* Sprites */
	
		ROM_REGION( 0x80000, REGION_SOUND1, 0 );/* OKIM6295 samples */
		ROM_LOAD( "921a05",      0x000000, 0x080000, CRC(d5a1eddd),SHA1(42b5b255f02b9c6d856b1578af9a5dfc51ea6ebb) )
	
		ROM_REGION( 0x80000, REGION_SOUND2, 0 );/* OKIM6295 samples */
		ROM_LOAD( "921a06",      0x000000, 0x080000, CRC(89461d0f),SHA1(b7d27d0ee0b7ab44c20ab710b567f64fc3afb90c) )
	
		ROM_REGION( 0x0220, REGION_PROMS, 0 );
		ROM_LOAD( "921a08",      0x0000, 0x0100, CRC(cfdbb86c),SHA1(588822f6308a860937349c9106c2b4b1a75823ec) )	/* unknown */
		ROM_LOAD( "921a09",      0x0100, 0x0100, CRC(633ab1c9),SHA1(acd99fcca41eaab7948ca84988352f1d7d519c61) )	/* unknown */
		ROM_LOAD( "921a10",      0x0200, 0x0020, CRC(8371e42d),SHA1(6cfd70dfa00e85ec1df8832d41df331cc3e3733a) )	/* unknown */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_gunnail = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );	/* 68000 code */
		ROM_LOAD16_BYTE( "3e.bin",  0x00000, 0x40000, CRC(61d985b2),SHA1(96daca603f18accb47f98a3e584b2c84fc5a2ca4) )
		ROM_LOAD16_BYTE( "3o.bin",  0x00001, 0x40000, CRC(f114e89c),SHA1(a12f5278167f446bb5277e87289c41b5aa365c86) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );	/* Code for (unknown?) CPU */
		ROM_LOAD( "92077_2.bin",      0x00000, 0x10000, CRC(cd4e55f8),SHA1(92182767ca0ec37ec4949bd1a88c2efdcdcb60ed) )
	
		ROM_REGION( 0x020000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "1.bin",    0x000000, 0x020000, CRC(3d00a9f4),SHA1(91a82e3e74c8774d7f8b2adceb228b97010facfd) )	/* 8x8 tiles */
	
		ROM_REGION( 0x100000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "92077-4.bin", 0x000000, 0x100000, CRC(a9ea2804),SHA1(14dbdb3c7986db5e44dc7c5be6fcf39f3d1e50b0) )	/* 16x16 tiles */
	
		ROM_REGION( 0x200000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD16_WORD_SWAP( "92077-7.bin", 0x000000, 0x200000, CRC(d49169b3) SHA1(565ff7725dd6ace79b55706114132d8d867e81a9) )	/* Sprites */
	
		ROM_REGION( 0x080000, REGION_SOUND1, 0 );/* OKIM6295 samples */
		ROM_LOAD( "92077-6.bin", 0x000000, 0x080000, CRC(6d133f0d),SHA1(8a5e6e27a297196f20e4de0d060f1188115809bb) )	/* all banked */
	
		ROM_REGION( 0x080000, REGION_SOUND2, 0 );/* OKIM6295 samples */
		ROM_LOAD( "92077-5.bin", 0x000000, 0x080000, CRC(feb83c73),SHA1(b44e9d20b4af02e218c4bc875d66a7d6b8551cae) )	/* all banked */
	
		ROM_REGION( 0x0220, REGION_PROMS, 0 );
		ROM_LOAD( "8.bpr",      0x0000, 0x0100, CRC(4299776e),SHA1(683d14d2ace14965f0fcfe0f0540c1b77d2cece5) )	/* unknown */
		ROM_LOAD( "9.bpr",      0x0100, 0x0100, CRC(633ab1c9),SHA1(acd99fcca41eaab7948ca84988352f1d7d519c61) )	/* unknown */
		ROM_LOAD( "10.bpr",     0x0200, 0x0020, CRC(c60103c8),SHA1(dfb05b704bb5e1f75f5aaa4fa36e8ddcc905f8b6) )	/* unknown */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_macross2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );	/* 68000 code */
		ROM_LOAD16_WORD_SWAP( "mcrs2j.3",      0x00000, 0x80000, CRC(36a618fe) SHA1(56fdb2bcb4a39888cfbaf9692d66335524a6ac0c) )
	
		ROM_REGION( 0x30000, REGION_CPU2, 0 );	/* Z80 code */
		ROM_LOAD( "mcrs2j.2",    0x00000, 0x20000, CRC(b4aa8ac7),SHA1(73a6de56cbfb468450d9b39fcbae0362f242f37b) )
		ROM_RELOAD(              0x10000, 0x20000 );			/* banked */
	
		ROM_REGION( 0x020000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "mcrs2j.1",    0x000000, 0x020000, CRC(c7417410),SHA1(41431d8f1ff4d66baf1a8518a0b0c0125d1d71d4) )	/* 8x8 tiles */
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "bp932an.a04", 0x000000, 0x200000, CRC(c4d77ff0),SHA1(aca60a3f5f89265e7e3799e5d80ea8196fb11ff3) )	/* 16x16 tiles */
	
		ROM_REGION( 0x400000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD16_WORD_SWAP( "bp932an.a07", 0x000000, 0x200000, CRC(aa1b21b9) SHA1(133822e3d8628aa4eb3e62fbd054956799423b98) )	/* Sprites */
		ROM_LOAD16_WORD_SWAP( "bp932an.a08", 0x200000, 0x200000, CRC(67eb2901) SHA1(25e0f9fda1a8c0c2b59616dd153cb6dcb459d2d9) )
	
		ROM_REGION( 0x240000, REGION_SOUND1, 0 );/* OKIM6295 samples */
		ROM_LOAD( "bp932an.a06", 0x040000, 0x200000, CRC(ef0ffec0),SHA1(fd72cc77e02d1a00bf27e77a33d7dab5f6ba1cb4) )	/* all banked */
	
		ROM_REGION( 0x140000, REGION_SOUND2, 0 );/* OKIM6295 samples */
		ROM_LOAD( "bp932an.a05", 0x040000, 0x100000, CRC(b5335abb),SHA1(f4eaf4e465eeca31741d432ee46ed39ffcd92cca) )	/* all banked */
	
		ROM_REGION( 0x0200, REGION_PROMS, 0 );
		ROM_LOAD( "mcrs2bpr.9",  0x0000, 0x0100, CRC(435653a2),SHA1(575b4a46ea65179de3042614da438d2f6d8b572e) )	/* unknown */
		ROM_LOAD( "mcrs2bpr.10", 0x0100, 0x0100, CRC(e6ead349),SHA1(6d81b1c0233580aa48f9718bade42d640e5ef3dd) )	/* unknown */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_tdragon2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );	/* 68000 code */
		ROM_LOAD16_WORD_SWAP( "6.bin",      0x00000, 0x80000, CRC(310d6bca) SHA1(f46ad1d13cf5014aef1f0e8862b369ab31c22866) )
	
		ROM_REGION( 0x30000, REGION_CPU2, 0 );	/* Z80 code */
		ROM_LOAD( "5.bin",    0x00000, 0x20000, CRC(b870be61),SHA1(ea5d45c3a3ab805e55806967f00167cf6366212e) )
		ROM_RELOAD(              0x10000, 0x20000 );			/* banked */
	
		ROM_REGION( 0x020000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "1.bin",    0x000000, 0x020000, CRC(d488aafa),SHA1(4d05e7ca075b638dd90ae4c9f224817a8a3ae9f3) )	/* 8x8 tiles */
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "ww930914.2", 0x000000, 0x200000, CRC(f968c65d),SHA1(fd6d21bba53f945b1597d7d0735bc62dd44d5498) )	/* 16x16 tiles */
	
		ROM_REGION( 0x400000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD16_WORD_SWAP( "ww930917.7", 0x000000, 0x200000, CRC(b98873cb) SHA1(cc19200865176e940ff68e12de81f029b51c2084) )	/* Sprites */
		ROM_LOAD16_WORD_SWAP( "ww930918.8", 0x200000, 0x200000, CRC(baee84b2) SHA1(b325b00e6147266dbdc840e03556004531dc2038) )
	
		ROM_REGION( 0x240000, REGION_SOUND1, 0 );/* OKIM6295 samples */
		ROM_LOAD( "ww930916.4", 0x040000, 0x200000, CRC(07c35fe6),SHA1(33547bd88764704310f2ef8cf3bfe21ceb56d5b7) )	/* all banked */
	
		ROM_REGION( 0x240000, REGION_SOUND2, 0 );/* OKIM6295 samples */
		ROM_LOAD( "ww930915.3", 0x040000, 0x200000, CRC(82025bab),SHA1(ac6053700326ea730d00ec08193e2c8a2a019f0b) )	/* all banked */
	
		ROM_REGION( 0x0200, REGION_PROMS, 0 );
		ROM_LOAD( "9.bpr",  0x0000, 0x0100, CRC(435653a2),SHA1(575b4a46ea65179de3042614da438d2f6d8b572e) )	/* unknown */
		ROM_LOAD( "10.bpr", 0x0100, 0x0100, CRC(e6ead349),SHA1(6d81b1c0233580aa48f9718bade42d640e5ef3dd) )	/* unknown */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_bigbang = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );	/* 68000 code */
		ROM_LOAD16_WORD_SWAP( "eprom.3",      0x00000, 0x80000, CRC(28e5957a) SHA1(fe4f870a9c2235cc02b4e036a2a4116f071d59ad) )
	
		ROM_REGION( 0x30000, REGION_CPU2, 0 );	/* Z80 code */
		ROM_LOAD( "5.bin",    0x00000, 0x20000, CRC(b870be61),SHA1(ea5d45c3a3ab805e55806967f00167cf6366212e) )
		ROM_RELOAD(              0x10000, 0x20000 );			/* banked */
	
		ROM_REGION( 0x020000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "1.bin",    0x000000, 0x020000, CRC(d488aafa),SHA1(4d05e7ca075b638dd90ae4c9f224817a8a3ae9f3) )	/* 8x8 tiles */
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "ww930914.2", 0x000000, 0x200000, CRC(f968c65d),SHA1(fd6d21bba53f945b1597d7d0735bc62dd44d5498) )	/* 16x16 tiles */
	
		ROM_REGION( 0x400000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD16_WORD_SWAP( "ww930917.7", 0x000000, 0x200000, CRC(b98873cb) SHA1(cc19200865176e940ff68e12de81f029b51c2084) )	/* Sprites */
		ROM_LOAD16_WORD_SWAP( "ww930918.8", 0x200000, 0x200000, CRC(baee84b2) SHA1(b325b00e6147266dbdc840e03556004531dc2038) )
	
		ROM_REGION( 0x240000, REGION_SOUND1, 0 );/* OKIM6295 samples */
		ROM_LOAD( "ww930916.4", 0x040000, 0x200000, CRC(07c35fe6),SHA1(33547bd88764704310f2ef8cf3bfe21ceb56d5b7) )	/* all banked */
	
		ROM_REGION( 0x240000, REGION_SOUND2, 0 );/* OKIM6295 samples */
		ROM_LOAD( "ww930915.3", 0x040000, 0x200000, CRC(82025bab),SHA1(ac6053700326ea730d00ec08193e2c8a2a019f0b) )	/* all banked */
	
		ROM_REGION( 0x0200, REGION_PROMS, 0 );
		ROM_LOAD( "9.bpr",  0x0000, 0x0100, CRC(435653a2),SHA1(575b4a46ea65179de3042614da438d2f6d8b572e) )	/* unknown */
		ROM_LOAD( "10.bpr", 0x0100, 0x0100, CRC(e6ead349),SHA1(6d81b1c0233580aa48f9718bade42d640e5ef3dd) )	/* unknown */
	ROM_END(); }}; 
	
	/*
	
	Rapid Hero
	NMK, 1994
	
	The main board has no ROMs at all except 3 PROMs. There is a plug-in daughter
	board that holds all the ROMs. It has the capacity for 3 socketed EPROMS and 7x
	16M MASK ROMs total.
	
	
	PCB Layout (Main board)
	-----------------------
	
	AWA94099
	-----------------------------------------------------------------------
	|    YM2203  TMP90C841 6264 DSW2(8) 62256 62256 6116 62256 62256 6116 |
	|   6295 NMK112      12MHz     DSW1(8)                                |
	|  YM3014B 6295      16MHz NMK005   62256 62256 6116 62256 62256 6116 |
	|J       -----------------                                            |
	|A       |               |                                            |
	|M       -----------------                                            |
	|M                   PROM3                           NMK009  NMK009   |
	|A   NMK111   6116                    6116  NMK008                    |
	|       |-|   6116                    6116                            |
	|6116   | | NMK902   -----------------                                |
	|6116   | |          |               |                                |
	|PROM1  | |          -----------------                                |
	|       | |                                                           |
	|NMK111 | | NMK903                                                    |
	|       | | NMK903       PROM2                                        |
	|NMK111 | |                                                           |
	|       |-|                   6116                  TMP68HC000P-16    |
	| 62256     NMK901            6116                             14MHz  |
	| 62256                                                               |
	-----------------------------------------------------------------------
	
	Notes:
	      68k clock: 14.00MHz
	          VSync: 56Hz
	          HSync: 15.35kHz
	   90c841 clock: 8.000MHz
	
	
	PCB Layout (Daughter board)
	---------------------------
	
	AWA94099-ROME
	--------------------------
	| 2      6   7   5    3  |
	|                        |
	| 1                      |
	|                        |
	|                        |
	|                        |
	| 4           8   9   10 |
	|                        |
	|                        |
	|                        |
	--------------------------
	
	*/
	
	static RomLoadPtr rom_raphero = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );	/* 68000 code */
		ROM_LOAD16_WORD_SWAP( "rhp94099.3",      0x00000, 0x80000, CRC(ec9b4f05) SHA1(e5bd797620dc449fd78b41d87e9ba5a764eb8b44) )
	
		ROM_REGION( 0x20000, REGION_CPU2, 0 );	/* tmp90c841 ??? sound code/data */
		ROM_LOAD( "rhp94099.2",    0x00000, 0x20000, CRC(fe01ece1),SHA1(c469fb79f2774089848c814f92ddd3c9e384050f) )
	
		ROM_REGION( 0x020000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "rhp94099.1",    0x000000, 0x020000, CRC(55a7a011),SHA1(87ded56bfdd38cbf8d3bd8b3789831f768550a12) )	/* 8x8 tiles */
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "rhp94099.4", 0x000000, 0x200000,  CRC(076eee7b),SHA1(7c315fe33d0fcd92e0ce2f274996c8059228b005) )	/* 16x16 tiles */
	
		ROM_REGION( 0x600000, REGION_GFX3, ROMREGION_DISPOSE );/* sprites */
		ROM_LOAD16_WORD_SWAP( "rhp94099.8", 0x000000, 0x200000, CRC(49892f07) SHA1(2f5d20cd193cffcba9041aa11d6665adebeffffa) )	/* 16x16 tiles */
		ROM_LOAD16_WORD_SWAP( "rhp94099.9", 0x200000, 0x200000, CRC(ea2e47f0) SHA1(97dfa8f95f27b36deb5ce1c80e3d727bad24e52b) )	/* 16x16 tiles */
		ROM_LOAD16_WORD_SWAP( "rhp94099.10",0x400000, 0x200000, CRC(512cb839) SHA1(4a2c5ac88e4bf8a6f07c703277c4d33e649fd192) )	/* 16x16 tiles */
	
		ROM_REGION( 0x400000, REGION_SOUND1, 0 );/* OKIM6295 samples */
		ROM_LOAD( "rhp94099.5", 0x000000, 0x200000, CRC(515eba93),SHA1(c35cb5f31f4bc7327be5777624af168f9fb364a5) )	/* all banked */
		ROM_LOAD( "rhp94099.6", 0x200000, 0x200000, CRC(f1a80e5a),SHA1(218bd7b0c3d8b283bf96b95bf888228810699370) )	/* all banked */
	
		ROM_REGION( 0x240000, REGION_SOUND2, 0 );/* OKIM6295 samples */
		ROM_LOAD( "rhp94099.7", 0x040000, 0x200000, CRC(0d99547e),SHA1(2d9630bd55d27010f9d1d2dbdbd07ac265e8ebe6) )	/* all banked */
	
		ROM_REGION( 0x0300, REGION_PROMS, 0 );
		ROM_LOAD( "prom1.u19",      0x0000, 0x0100, CRC(4299776e),SHA1(683d14d2ace14965f0fcfe0f0540c1b77d2cece5) ) /* unknown */
		ROM_LOAD( "prom2.u53",      0x0100, 0x0100, CRC(e6ead349),SHA1(6d81b1c0233580aa48f9718bade42d640e5ef3dd) ) /* unknown */
		ROM_LOAD( "prom3.u60",      0x0200, 0x0100, CRC(304f98c6),SHA1(8dfd9bf719087ec30c83efe95c4561666c7d1801) ) /* unknown */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_sabotenb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );	/* 68000 code */
		ROM_LOAD16_BYTE( "ic76.sb1",  0x00000, 0x40000, CRC(b2b0b2cf),SHA1(219f1cefdb107d8404f4f8bfa0700fd3218d9320) )
		ROM_LOAD16_BYTE( "ic75.sb2",  0x00001, 0x40000, CRC(367e87b7),SHA1(c950041529b5117686e4bb1ae77db82fe758c1d0) )
	
		ROM_REGION( 0x010000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "ic35.sb3",		0x000000, 0x010000, CRC(eb7bc99d),SHA1(b3063afd58025a441d4750c22483e9129da402e7) )	/* 8x8 tiles */
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "ic32.sb4",		0x000000, 0x200000, CRC(24c62205),SHA1(3ab0ca5d7c698328d91421ccf6f7dafc20df3c8d) )	/* 16x16 tiles */
	
		ROM_REGION( 0x200000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD16_WORD_SWAP( "ic100.sb5",	0x000000, 0x200000, CRC(b20f166e) SHA1(074d770fd6d233040a80a92f4467d81f961c650b) )	/* Sprites */
	
		ROM_REGION( 0x140000, REGION_SOUND1, 0 );/* OKIM6295 samples */
		ROM_LOAD( "ic30.sb6",    0x040000, 0x100000, CRC(288407af),SHA1(78c08fae031337222681c593dc86a08df6a34a4b) )	/* all banked */
	
		ROM_REGION( 0x140000, REGION_SOUND2, 0 );/* OKIM6295 samples */
		ROM_LOAD( "ic27.sb7",    0x040000, 0x100000, CRC(43e33a7e),SHA1(51068b63f4415712eaa25dcf1ee6b0cc2850974e) )	/* all banked */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_bjtwin = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 ); /* 68000 code */
		ROM_LOAD16_BYTE( "93087-1.bin",  0x00000, 0x20000, CRC(93c84e2d),SHA1(ad0755cabfef78e7e689856379d6f8c88a9b27c1) )
		ROM_LOAD16_BYTE( "93087-2.bin",  0x00001, 0x20000, CRC(30ff678a),SHA1(aa3ce4905e448e371e254545ef9ed7edb00b1cc3) )
	
		ROM_REGION( 0x010000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "93087-3.bin",  0x000000, 0x010000, CRC(aa13df7c),SHA1(162d4f12364c68028e86fe97ee75c262daa4c699) ) /* 8x8 tiles */
	
		ROM_REGION( 0x100000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "93087-4.bin",  0x000000, 0x100000, CRC(8a4f26d0),SHA1(be057a2b6d28c623ac1f16cf02ddbe12ca430b4a) ) /* 16x16 tiles */
	
		ROM_REGION( 0x100000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD16_WORD_SWAP( "93087-5.bin", 0x000000, 0x100000, CRC(bb06245d) SHA1(c91e2284d95370b8ef2eb1b9d6305fdd6cde23a0) ) /* Sprites */
	
		ROM_REGION( 0x140000, REGION_SOUND1, 0 );/* OKIM6295 samples */
		ROM_LOAD( "93087-6.bin",    0x040000, 0x100000, CRC(372d46dd),SHA1(18f44e777241af50787730652fa018c51b65ea15) ) /* all banked */
	
		ROM_REGION( 0x140000, REGION_SOUND2, 0 );/* OKIM6295 samples */
		ROM_LOAD( "93087-7.bin",    0x040000, 0x100000, CRC(8da67808),SHA1(f042574c097f5a8c2684fcc23f2c817c168254ef) ) /* all banked */
	
		ROM_REGION( 0x0200, REGION_PROMS, 0 );
		ROM_LOAD( "8.bpr",      0x0000, 0x0100, CRC(633ab1c9),SHA1(acd99fcca41eaab7948ca84988352f1d7d519c61) ) /* unknown */
		ROM_LOAD( "9.bpr",      0x0000, 0x0100, CRC(435653a2),SHA1(575b4a46ea65179de3042614da438d2f6d8b572e) ) /* unknown */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_nouryoku = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1, 0 );	/* 68000 code */
		ROM_LOAD16_BYTE( "ic76.1",  0x00000, 0x40000, CRC(26075988),SHA1(c3d0eef0417be3f78008c026915fd7e2fd589563) )
		ROM_LOAD16_BYTE( "ic75.2",  0x00001, 0x40000, CRC(75ab82cd),SHA1(fb828f87eebbe9d61766535efc18de9dfded110c) )
	
		ROM_REGION( 0x010000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "ic35.3",		0x000000, 0x010000, CRC(03d0c3b1),SHA1(4d5427c324e2141d0a953cc5133d10b327827e0b) )	/* 8x8 tiles */
	
		ROM_REGION( 0x200000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "ic32.4",		0x000000, 0x200000, CRC(88d454fd),SHA1(c79c48d9b3602266499a5dd0b15fd2fb032809be) )	/* 16x16 tiles */
	
		ROM_REGION( 0x200000, REGION_GFX3, ROMREGION_DISPOSE );
		ROM_LOAD16_WORD_SWAP( "ic100.5",	0x000000, 0x200000, CRC(24d3e24e) SHA1(71e38637953ec98bf308824aaef5628803aead21) )	/* Sprites */
	
		ROM_REGION( 0x140000, REGION_SOUND1, 0 );/* OKIM6295 samples */
		ROM_LOAD( "ic30.6",     0x040000, 0x100000, CRC(feea34f4),SHA1(bee467e74dbad497c6f5f6b38b7e52001e767012) )	/* all banked */
	
		ROM_REGION( 0x140000, REGION_SOUND2, 0 );/* OKIM6295 samples */
		ROM_LOAD( "ic27.7",     0x040000, 0x100000, CRC(8a69fded),SHA1(ee73f1789bcc672232606a4b3b28087fea1c5c69) )	/* all banked */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_manybloc = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x200000, REGION_CPU1, 0 );	/* 68000 code */
		ROM_LOAD16_BYTE( "1-u33.bin",  0x00001, 0x20000, CRC(07473154),SHA1(e67f637e74dfe5f1be558f963c0b3225254afe33) )
		ROM_LOAD16_BYTE( "2-u35.bin",  0x00000, 0x20000, CRC(04acd8c1),SHA1(3ef329e8d25565c7f7166f12137f4df5a057022f) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );	/* Z80? CPU */
		ROM_LOAD( "3-u146.bin",      0x00000, 0x10000, CRC(7bf5fafa),SHA1(d17feca628775860d6c7019a9725bd40fbc5b7d7) )
	
		ROM_REGION( 0x80000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "12-u39.bin",    0x000000, 0x10000, CRC(413b5438),SHA1(af366ce998ebe0d25255cc0cb1cd81689d3696ec) )	/* 8x8 tiles */
	
		ROM_REGION( 0x080000, REGION_GFX3, ROMREGION_DISPOSE );/* 16x16 sprite tiles */
		ROM_LOAD16_BYTE( "8-u54b.bin",  0x000000, 0x20000, CRC(03eede77),SHA1(2476a488bb0d39790b2cc7f261ddb973378022ff) )
		ROM_LOAD16_BYTE( "10-u86b.bin", 0x000001, 0x20000, CRC(9eab216f),SHA1(616f3ee2d06aa7151af634773a5e8633bff9588e) )
		ROM_LOAD16_BYTE( "9-u53b.bin",  0x040000, 0x20000, CRC(dfcfa040),SHA1(f1561defe9746afdb1a5327d0a4435a6f3e87a77) )
		ROM_LOAD16_BYTE( "11-u85b.bin", 0x040001, 0x20000, CRC(fe747dd5),SHA1(6ba57a45f4d77e2574de95d4a2f0718c601e7214) )
	
		ROM_REGION( 0x80000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "4-u96.bin", 0x000000, 0x40000, CRC(28af2640),SHA1(08fa57de66cf58fe2256455538261c2d05d27e1e) )
		ROM_LOAD( "5-u97.bin", 0x040000, 0x40000, CRC(536699e6),SHA1(13ec233f5e4f2a65ac7bc55511e988508269acd5) )
	
		ROM_REGION( 0x40000, REGION_SOUND1, 0 );/* OKIM6295 samples */
		ROM_LOAD( "6-u131.bin", 0x000000, 0x40000, CRC(79a4ae75),SHA1(f7609d0ca18b4af8c5f37daa1795a7a6c6d768ae) )
	
		ROM_REGION( 0x40000, REGION_SOUND2, 0 );/* OKIM6295 samples */
		ROM_LOAD( "7-u132.bin", 0x000000, 0x40000, CRC(21db875e),SHA1(e1d96155b6d8825f7c449f276d02f9769258345d) )
	
		ROM_REGION( 0x20220, REGION_PROMS, 0 );
		ROM_LOAD( "u7.bpr",      0x0000, 0x0100, CRC(cfdbb86c),SHA1(588822f6308a860937349c9106c2b4b1a75823ec) )	/* unknown */
		ROM_LOAD( "u120.bpr",      0x0100, 0x0100, CRC(576c5984),SHA1(6e9b7f30de0d91cb766a62abc5888ec9af085a27) )	/* unknown */
		ROM_LOAD( "u200.bpr",      0x0200, 0x0020, CRC(1823600b),SHA1(7011156ebcb815b176856bd67898ce655ea1b5ab) )	/* unknown */
		ROM_LOAD( "u10.bpr",      0x0100, 0x0200, CRC(8e9b569a),SHA1(1d8d633fbeb72d5e55ad4b282df02e9ca5e240eb) )	/* unknown */
	ROM_END(); }}; 
	
	static unsigned char decode_byte(unsigned char src, unsigned char *bitp)
	{
		unsigned char ret, i;
	
		ret = 0;
		for (i=0; i<8; i++)
			ret |= (((src >> bitp[i]) & 1) << (7-i));
	
		return ret;
	}
	
	static unsigned long bjtwin_address_map_bg0(unsigned long addr)
	{
	   return ((addr&0x00004)>> 2) | ((addr&0x00800)>> 10) | ((addr&0x40000)>>16);
	}
	
	
	static unsigned short decode_word(unsigned short src, unsigned char *bitp)
	{
		unsigned short ret, i;
	
		ret=0;
		for (i=0; i<16; i++)
			ret |= (((src >> bitp[i]) & 1) << (15-i));
	
		return ret;
	}
	
	
	static unsigned long bjtwin_address_map_sprites(unsigned long addr)
	{
	   return ((addr&0x00010)>> 4) | ((addr&0x20000)>>16) | ((addr&0x100000)>>18);
	}
	
	
	static void decode_gfx(void)
	{
		/* GFX are scrambled.  We decode them here.  (BIG Thanks to Antiriad for descrambling info) */
		unsigned char *rom;
		int A;
	
		static unsigned char decode_data_bg[8][8] =
		{
			{0x3,0x0,0x7,0x2,0x5,0x1,0x4,0x6},
			{0x1,0x2,0x6,0x5,0x4,0x0,0x3,0x7},
			{0x7,0x6,0x5,0x4,0x3,0x2,0x1,0x0},
			{0x7,0x6,0x5,0x0,0x1,0x4,0x3,0x2},
			{0x2,0x0,0x1,0x4,0x3,0x5,0x7,0x6},
			{0x5,0x3,0x7,0x0,0x4,0x6,0x2,0x1},
			{0x2,0x7,0x0,0x6,0x5,0x3,0x1,0x4},
			{0x3,0x4,0x7,0x6,0x2,0x0,0x5,0x1},
		};
	
		static unsigned char decode_data_sprite[8][16] =
		{
			{0x9,0x3,0x4,0x5,0x7,0x1,0xb,0x8,0x0,0xd,0x2,0xc,0xe,0x6,0xf,0xa},
			{0x1,0x3,0xc,0x4,0x0,0xf,0xb,0xa,0x8,0x5,0xe,0x6,0xd,0x2,0x7,0x9},
			{0xf,0xe,0xd,0xc,0xb,0xa,0x9,0x8,0x7,0x6,0x5,0x4,0x3,0x2,0x1,0x0},
			{0xf,0xe,0xc,0x6,0xa,0xb,0x7,0x8,0x9,0x2,0x3,0x4,0x5,0xd,0x1,0x0},
	
			{0x1,0x6,0x2,0x5,0xf,0x7,0xb,0x9,0xa,0x3,0xd,0xe,0xc,0x4,0x0,0x8}, /* Haze 20/07/00 */
			{0x7,0x5,0xd,0xe,0xb,0xa,0x0,0x1,0x9,0x6,0xc,0x2,0x3,0x4,0x8,0xf}, /* Haze 20/07/00 */
			{0x0,0x5,0x6,0x3,0x9,0xb,0xa,0x7,0x1,0xd,0x2,0xe,0x4,0xc,0x8,0xf}, /* Antiriad, Corrected by Haze 20/07/00 */
			{0x9,0xc,0x4,0x2,0xf,0x0,0xb,0x8,0xa,0xd,0x3,0x6,0x5,0xe,0x1,0x7}, /* Antiriad, Corrected by Haze 20/07/00 */
		};
	
	
		/* background */
		rom = memory_region(REGION_GFX2);
		for (A = 0;A < memory_region_length(REGION_GFX2);A++)
		{
			rom[A] = decode_byte( rom[A], decode_data_bg[bjtwin_address_map_bg0(A)]);
		}
	
		/* sprites */
		rom = memory_region(REGION_GFX3);
		for (A = 0;A < memory_region_length(REGION_GFX3);A += 2)
		{
			unsigned short tmp = decode_word( rom[A+1]*256 + rom[A], decode_data_sprite[bjtwin_address_map_sprites(A)]);
			rom[A+1] = tmp >> 8;
			rom[A] = tmp & 0xff;
		}
	}
	
	static void decode_tdragonb(void)
	{
		/* Descrambling Info Again Taken from Raine, Huge Thanks to Antiriad and the Raine Team for
		   going Open Source, best of luck in future development. */
	
		unsigned char *rom;
		int A;
	
		/* The Main 68k Program of the Bootleg is Bitswapped */
		static unsigned char decode_data_tdragonb[1][16] =
		{
			{0xe,0xc,0xa,0x8,0x7,0x5,0x3,0x1,0xf,0xd,0xb,0x9,0x6,0x4,0x2,0x0},
		};
	
		/* Graphic Roms Could Also Do With Rearranging to make things simpler */
		static unsigned char decode_data_tdragonbgfx[1][8] =
		{
			{0x7,0x6,0x5,0x3,0x4,0x2,0x1,0x0},
		};
	
		rom = memory_region(REGION_CPU1);
		for (A = 0;A < memory_region_length(REGION_CPU1);A += 2)
		{
	#ifdef LSB_FIRST
			unsigned short tmp = decode_word( rom[A+1]*256 + rom[A], decode_data_tdragonb[0]);
			rom[A+1] = tmp >> 8;
			rom[A] = tmp & 0xff;
	#else
			unsigned short tmp = decode_word( rom[A]*256 + rom[A+1], decode_data_tdragonb[0]);
			rom[A] = tmp >> 8;
			rom[A+1] = tmp & 0xff;
	#endif
		}
	
		rom = memory_region(REGION_GFX2);
		for (A = 0;A < memory_region_length(REGION_GFX2);A++)
		{
			rom[A] = decode_byte( rom[A], decode_data_tdragonbgfx[0]);
		}
	
		rom = memory_region(REGION_GFX3);
		for (A = 0;A < memory_region_length(REGION_GFX3);A++)
		{
			rom[A] = decode_byte( rom[A], decode_data_tdragonbgfx[0]);
		}
	}
	
	static void decode_ssmissin(void)
	{
		/* Like Thunder Dragon Bootleg without the Program Rom Swapping */
		unsigned char *rom;
		int A;
	
		/* Graphic Roms Could Also Do With Rearranging to make things simpler */
		static unsigned char decode_data_tdragonbgfx[1][8] =
		{
			{0x7,0x6,0x5,0x3,0x4,0x2,0x1,0x0},
		};
	
		rom = memory_region(REGION_GFX2);
		for (A = 0;A < memory_region_length(REGION_GFX2);A++)
		{
			rom[A] = decode_byte( rom[A], decode_data_tdragonbgfx[0]);
		}
	
		rom = memory_region(REGION_GFX3);
		for (A = 0;A < memory_region_length(REGION_GFX3);A++)
		{
			rom[A] = decode_byte( rom[A], decode_data_tdragonbgfx[0]);
		}
	}
	
	
	static DRIVER_INIT( nmk )
	{
		decode_gfx();
	}
	
	static DRIVER_INIT( hachamf )
	{
		data16_t *rom = (data16_t *)memory_region(REGION_CPU1);
	
		rom[0x0006/2] = 0x7dc2;	/* replace reset vector with the "real" one */
	}
	
	static DRIVER_INIT( acrobatm )
	{
		data16_t *RAM = (data16_t *)memory_region(REGION_CPU1);
	
		RAM[0x724/2] = 0x4e71; /* Protection */
		RAM[0x726/2] = 0x4e71;
		RAM[0x728/2] = 0x4e71;
	
		RAM[0x9d8/2] = 0x3e3C; /* Checksum */
		RAM[0x9da/2] = 0x0;
		RAM[0x97c/2] = 0x3e3C;
		RAM[0x97e/2] = 0x0;
	}
	
	static DRIVER_INIT( tdragonb )
	{
		data16_t *ROM = (data16_t *)memory_region(REGION_CPU1);
	
		decode_tdragonb();
	
		/* The Following Patch is taken from Raine, Otherwise the game has no Sprites in Attract Mode or After Level 1
		   which is rather odd considering its a bootleg.. */
		ROM[0x00308/2] = 0x4e71; /* Sprite Problem */
	}
	
	static DRIVER_INIT( tdragon )
	{
		data16_t *RAM = (data16_t *)memory_region(REGION_CPU1);
	
		RAM[0x94b0/2] = 0; /* Patch out JMP to shared memory (protection) */
		RAM[0x94b2/2] = 0x92f4;
	}
	
	static DRIVER_INIT( ssmissin )
	{
		decode_ssmissin();
	}
	
	
	static DRIVER_INIT( strahl )
	{
		data16_t *RAM = (data16_t *)memory_region(REGION_CPU1);
	
		RAM[0x79e/2] = 0x4e71; /* Protection */
		RAM[0x7a0/2] = 0x4e71;
		RAM[0x7a2/2] = 0x4e71;
	
		RAM[0x968/2] = 0x4e71; /* Checksum error */
		RAM[0x96a/2] = 0x4e71;
		RAM[0x8e0/2] = 0x4e71; /* Checksum error */
		RAM[0x8e2/2] = 0x4e71;
	}
	
	static DRIVER_INIT( bioship )
	{
		data16_t *RAM = (data16_t *)memory_region(REGION_CPU1);
	
		RAM[0xe78a/2] = 0x4e71; /* Protection */
		RAM[0xe78c/2] = 0x4e71;
	
		RAM[0xe798/2] = 0x4e71; /* Checksum */
		RAM[0xe79a/2] = 0x4e71;
	}
	
	
	int is_blkheart;
	
	// see raine's games/nmk.c
	
	static WRITE16_HANDLER ( test_2a_w )
	{
		data = data >> 8;
	
		ram[0x2a/2] = (data << 8) | data;
	
		if (data == 1 || data == 2) {
			ram[0x68/2] = 11;
			ram[0x6a/2] = 0;
		}
	}
	
	static WRITE16_HANDLER ( test_2a_mustang_w )
	{
		data = data >> 8;
	
		ram[0x2a/2] = (data << 8) | data;
	
		if (data == 1 || data == 2) {
			ram[0x2e/2] = 10;
			ram[0x2c/2] = 0;
		}
	
	}
	
	static DRIVER_INIT( blkheart )
	{
		data16_t *RAM = (data16_t *)memory_region(REGION_CPU1);
	
		is_blkheart = 1; // sprite enable is different?
	
	   // see raine's games/nmk.c
		RAM[0x872/2] = 0x4e71;
	   	RAM[0x874/2] = 0x4e71;
	   	RAM[0x876/2] = 0x4e71;
	   	RAM[0x8d6/2] = 0x0300;
	   	RAM[0xe1c/2] = 0x0300;
	   	RAM[0x23dc/2] = 0x0300;
	   	RAM[0x3dea/2] = 0x0300;
	
		install_mem_write16_handler(0, 0xf902a, 0xf902b, test_2a_w );
	}
	
	static DRIVER_INIT( mustang )
	{
		data16_t *RAM = (data16_t *)memory_region(REGION_CPU1);
	
		is_blkheart = 1; // sprite enable is different?
	
	   // see raine's games/nmk.c
		RAM[0x85c/2] = 0x4e71;
		RAM[0x85e/2] = 0x4e71;
		RAM[0x860/2] = 0x4e71;
		RAM[0x8c0/2] = 0x0300;
	  	RAM[0xc00/2] = 0x0300;
	  	RAM[0x30b2/2] = 0x0300;
	
		install_mem_write16_handler(0, 0xf902a, 0xf902b, test_2a_mustang_w );
	}
	
	static DRIVER_INIT( bjtwin )
	{
		init_nmk();
	
		/* Patch rom to enable test mode */
	
	/*	008F54: 33F9 0008 0000 000F FFFC move.w  $80000.l, $ffffc.l
	 *	008F5E: 3639 0008 0002           move.w  $80002.l, D3
	 *	008F64: 3003                     move.w  D3, D0				\
	 *	008F66: 3203                     move.w  D3, D1				|	This code remaps
	 *	008F68: 0041 BFBF                ori.w   #-$4041, D1		|   buttons 2 and 3 to
	 *	008F6C: E441                     asr.w   #2, D1				|   button 1, so
	 *	008F6E: 0040 DFDF                ori.w   #-$2021, D0		|   you can't enter
	 *	008F72: E240                     asr.w   #1, D0				|   service mode7
	 *	008F74: C640                     and.w   D0, D3				|
	 *	008F76: C641                     and.w   D1, D3				/
	 *	008F78: 33C3 000F FFFE           move.w  D3, $ffffe.l
	 *	008F7E: 207C 000F 9000           movea.l #$f9000, A0
	 */
	
	//	data 16_t *rom = (data16_t *)memory_region(REGION_CPU1);
	//	rom[0x09172/2] = 0x6006;	/* patch checksum error */
	//	rom[0x08f74/2] = 0x4e71);
	}
	
	
	
	public static GameDriver driver_urashima	   = new GameDriver("1989"	,"urashima"	,"nmk16.java"	,rom_urashima,null	,machine_driver_urashima	,input_ports_macross	,null	,ROT0	,	"UPL",							"Urashima Mahjong", GAME_UNEMULATED_PROTECTION | GAME_NOT_WORKING ) /* Similar Hardware? */
	public static GameDriver driver_tharrier	   = new GameDriver("1989"	,"tharrier"	,"nmk16.java"	,rom_tharrier,null	,machine_driver_tharrier	,input_ports_tharrier	,null	,ROT270	,	"UPL (American Sammy license)",	"Task Force Harrier", GAME_UNEMULATED_PROTECTION | GAME_NOT_WORKING )
	public static GameDriver driver_tharierj	   = new GameDriver("1989"	,"tharierj"	,"nmk16.java"	,rom_tharierj,driver_tharrier	,machine_driver_tharrier	,input_ports_tharrier	,null	,ROT270	,	"UPL",	                        "Task Force Harrier (Japan)", GAME_UNEMULATED_PROTECTION | GAME_NOT_WORKING )
	public static GameDriver driver_mustang	   = new GameDriver("1990"	,"mustang"	,"nmk16.java"	,rom_mustang,null	,machine_driver_mustang	,input_ports_mustang	,init_mustang	,ROT0	,	"UPL",							"US AAF Mustang (Japan)", GAME_UNEMULATED_PROTECTION | GAME_NO_SOUND) // Playable but there are Still Protection Problems
	public static GameDriver driver_mustangs	   = new GameDriver("1990"	,"mustangs"	,"nmk16.java"	,rom_mustangs,driver_mustang	,machine_driver_mustang	,input_ports_mustang	,init_mustang	,ROT0	,	"UPL (Seoul Trading license)",	"US AAF Mustang (Seoul Trading)", GAME_UNEMULATED_PROTECTION | GAME_NO_SOUND ) // Playable but there are Still Protection Problems
	public static GameDriver driver_mustangb	   = new GameDriver("1990"	,"mustangb"	,"nmk16.java"	,rom_mustangb,driver_mustang	,machine_driver_mustangb	,input_ports_mustang	,init_mustang	,ROT0	,	"bootleg",						"US AAF Mustang (bootleg)", GAME_UNEMULATED_PROTECTION ) // Playable but there are Still Protection Problems
	public static GameDriver driver_bioship	   = new GameDriver("1990"	,"bioship"	,"nmk16.java"	,rom_bioship,null	,machine_driver_bioship	,input_ports_bioship	,init_bioship	,ROT0	,	"UPL (American Sammy license)",	"Bio-ship Paladin", GAME_NO_SOUND )
	public static GameDriver driver_vandyke	   = new GameDriver("1990"	,"vandyke"	,"nmk16.java"	,rom_vandyke,null	,machine_driver_vandyke	,input_ports_vandyke	,null	,ROT270	,	"UPL",							"Vandyke (Japan)",  GAME_NO_SOUND )
	public static GameDriver driver_vandyjal	   = new GameDriver("1990"	,"vandyjal"	,"nmk16.java"	,rom_vandyjal,driver_vandyke	,machine_driver_vandyke	,input_ports_vandyke	,null	,ROT270	,	"UPL (Jaleco license)",           "Vandyke (Jaleco)",  GAME_NO_SOUND )
	public static GameDriver driver_manybloc	   = new GameDriver("1991"	,"manybloc"	,"nmk16.java"	,rom_manybloc,null	,machine_driver_manybloc	,input_ports_manybloc	,null	,ROT270	,	"Bee-Oh",                         "Many Block", GAME_NO_COCKTAIL | GAME_IMPERFECT_GRAPHICS | GAME_IMPERFECT_SOUND )
	public static GameDriver driver_blkheart	   = new GameDriver("1991"	,"blkheart"	,"nmk16.java"	,rom_blkheart,null	,machine_driver_macross	,input_ports_blkheart	,init_blkheart	,ROT0	,	"UPL",							"Black Heart", GAME_UNEMULATED_PROTECTION | GAME_NO_SOUND  ) // Playable but there are Still Protection Problems
	public static GameDriver driver_blkhearj	   = new GameDriver("1991"	,"blkhearj"	,"nmk16.java"	,rom_blkhearj,driver_blkheart	,machine_driver_macross	,input_ports_blkheart	,init_blkheart	,ROT0	,	"UPL",							"Black Heart (Japan)", GAME_UNEMULATED_PROTECTION | GAME_NO_SOUND ) // Playable but there are Still Protection Problems
	public static GameDriver driver_acrobatm	   = new GameDriver("1991"	,"acrobatm"	,"nmk16.java"	,rom_acrobatm,null	,machine_driver_acrobatm	,input_ports_acrobatm	,init_acrobatm	,ROT270	,	"UPL (Taito license)",			"Acrobat Mission", GAME_NO_SOUND )
	public static GameDriver driver_strahl	   = new GameDriver("1992"	,"strahl"	,"nmk16.java"	,rom_strahl,null	,machine_driver_strahl	,input_ports_strahl	,init_strahl	,ROT0	,	"UPL",							"Koutetsu Yousai Strahl (Japan set 1)", GAME_NO_SOUND )
	public static GameDriver driver_strahla	   = new GameDriver("1992"	,"strahla"	,"nmk16.java"	,rom_strahla,driver_strahl	,machine_driver_strahl	,input_ports_strahl	,init_strahl	,ROT0	,	"UPL",							"Koutetsu Yousai Strahl (Japan set 2)", GAME_NO_SOUND )
	public static GameDriver driver_tdragon	   = new GameDriver("1991"	,"tdragon"	,"nmk16.java"	,rom_tdragon,null	,machine_driver_tdragon	,input_ports_tdragon	,init_tdragon	,ROT270	,	"NMK / Tecmo",					"Thunder Dragon", GAME_UNEMULATED_PROTECTION | GAME_NOT_WORKING | GAME_NO_SOUND )
	public static GameDriver driver_tdragonb	   = new GameDriver("1991"	,"tdragonb"	,"nmk16.java"	,rom_tdragonb,driver_tdragon	,machine_driver_tdragonb	,input_ports_tdragon	,init_tdragonb	,ROT270	,	"NMK / Tecmo",					"Thunder Dragon (Bootleg)" )
	public static GameDriver driver_ssmissin	   = new GameDriver("1992"	,"ssmissin"	,"nmk16.java"	,rom_ssmissin,null	,machine_driver_ssmissin	,input_ports_ssmissin	,init_ssmissin	,ROT270	,	"Comad",				            "S.S. Mission", GAME_NO_COCKTAIL )
	public static GameDriver driver_hachamf	   = new GameDriver("1991"	,"hachamf"	,"nmk16.java"	,rom_hachamf,null	,machine_driver_hachamf	,input_ports_hachamf	,init_hachamf	,ROT0	,	"NMK",							"Hacha Mecha Fighter", GAME_UNEMULATED_PROTECTION | GAME_NO_SOUND | GAME_NOT_WORKING )
	public static GameDriver driver_macross	   = new GameDriver("1992"	,"macross"	,"nmk16.java"	,rom_macross,null	,machine_driver_macross	,input_ports_macross	,init_nmk	,ROT270	,	"Banpresto",						"Super Spacefortress Macross / Chou-Jikuu Yousai Macross", GAME_NO_SOUND )
	public static GameDriver driver_gunnail	   = new GameDriver("1993"	,"gunnail"	,"nmk16.java"	,rom_gunnail,null	,machine_driver_gunnail	,input_ports_gunnail	,init_nmk	,ROT270	,	"NMK / Tecmo",					"GunNail", GAME_NO_SOUND )
	public static GameDriver driver_macross2	   = new GameDriver("1993"	,"macross2"	,"nmk16.java"	,rom_macross2,null	,machine_driver_macross2	,input_ports_macross2	,null	,ROT0	,	"Banpresto",						"Super Spacefortress Macross II / Chou-Jikuu Yousai Macross II", GAME_NO_COCKTAIL )
	public static GameDriver driver_tdragon2	   = new GameDriver("1993"	,"tdragon2"	,"nmk16.java"	,rom_tdragon2,null	,machine_driver_tdragon2	,input_ports_tdragon2	,null	,ROT270	,	"NMK",				         	"Thunder Dragon 2", GAME_NO_COCKTAIL )
	public static GameDriver driver_bigbang	   = new GameDriver("1993"	,"bigbang"	,"nmk16.java"	,rom_bigbang,driver_tdragon2	,machine_driver_tdragon2	,input_ports_tdragon2	,null	,ROT270	,	"NMK",				         	"Big Bang", GAME_NO_COCKTAIL )
	public static GameDriver driver_raphero	   = new GameDriver("1994"	,"raphero"	,"nmk16.java"	,rom_raphero,null	,machine_driver_raphero	,input_ports_tdragon2	,null	,ROT270	,	"Media Trading Corp",             "Rapid Hero (Japan?)", GAME_NO_SOUND ) // 23rd July 1993 in test mode, (c)1994 on title screen
	
	public static GameDriver driver_sabotenb	   = new GameDriver("1992"	,"sabotenb"	,"nmk16.java"	,rom_sabotenb,null	,machine_driver_bjtwin	,input_ports_sabotenb	,init_nmk	,ROT0	,	"NMK / Tecmo",					"Saboten Bombers", GAME_NO_COCKTAIL )
	public static GameDriver driver_bjtwin	   = new GameDriver("1993"	,"bjtwin"	,"nmk16.java"	,rom_bjtwin,null	,machine_driver_bjtwin	,input_ports_bjtwin	,init_bjtwin	,ROT270	,	"NMK",							"Bombjack Twin", GAME_NO_COCKTAIL )
	public static GameDriver driver_nouryoku	   = new GameDriver("1995"	,"nouryoku"	,"nmk16.java"	,rom_nouryoku,null	,machine_driver_bjtwin	,input_ports_nouryoku	,init_nmk	,ROT0	,	"Tecmo",							"Nouryoku Koujou Iinkai", GAME_NO_COCKTAIL )
}