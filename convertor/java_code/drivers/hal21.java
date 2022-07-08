/*
	Hal21
	ASO
	Alpha Mission ('p3.6d' is a bad dump)


Change Log
----------

AT08XX03:

[Common]
 - cleaned and consolidated VIDEO_UPDATE()
 - added shadows and highlights

 * A.S.O and HAL21 do a lot of palette cycling therefore
   conversion to tilemaps may be disadvantageous.
   Cocktail mode involves changing tile offsets and sprite
   coordinates and is still unsupported.

 * Manuals show both boards have noise filters to smooth out
   rings and scratches which are especially audible in HAL21.

[HAL21]
 - installed NMI scheduler to prevent music trashing

[ASO]
 - fixed music and sound effects being cut short
 - fixed service mode(hold P1 start during ROM test)
 - improved scrolling and color

 * Stage 5 boss' sky and the first half of stage 6's background
   appear to have consistent color as shown in Beep! magazine:

     http://qtq.hp.infoseek.co.jp/kouryaku/aso/aso2.jpg
     http://qtq.hp.infoseek.co.jp/kouryaku/aso/aso3.png

   Compared to MAME these areas are blacked out under pens
   0xf0-0xff. On the other hand pens 0x170-0x17f suit them
   perfectly but they are never used in the first two loops.
   (I played through the game and logged pen usage. Only four
   color codes have blue pen15 so it's not difficult to tell.)

   There are unknown bits embedded in RGB triplets and the whole
   upper half of the palette is simply unused. The fact that ASO's
   color PROMs are identical in every set dismissed bad dumps but
   increased the likelyhood of proprietary logic which is quite
   obvious in Touchdown Fever and HAL21.

[TODO]
 - find out what "really" messes up ASO's scrolling
 - verify color effects in both games
*/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class hal21
{
	
	static UINT8 *shared_ram, *shared_auxram;
	static UINT8 *hal21_vreg, *hal21_sndfifo;
	
	/**************************************************************************/
	// Test Handlers
	
	public static WriteHandlerPtr aso_scroll_sync_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (data == 0x7f && shared_auxram[0x04d2] & 1) data++;
	
		shared_auxram[0x04f8] = data;
	} };
	
	static void hal21_sound_scheduler(int mode, int data)
	{
		static int busy, hold, ffcount, ffhead, fftail;
	
		switch (mode)
		{
			case 0: // init
				fftail = ffhead = ffcount = hold = busy = 0;
			return;
	
			case 1: // cut-through or capture
				if (data & ~0x1f) busy = 1; else
				if (data && busy)
				{
					if (ffcount < 16)
					{
						ffcount++;
						hal21_sndfifo[ffhead] = data;
						ffhead = (ffhead + 1) & 15;
					}
					return;
				}
			break;
	
			case 2: // acknowledge
				if (busy) { busy = 0; hold = 4; }
			return;
	
			case 3: // release
				if (busy == 0)
				{
					if (hold) hold--; else
					if (ffcount)
					{
						ffcount--;
						data = hal21_sndfifo[fftail];
						fftail = (fftail + 1) & 15;
						break;
					}
				}
			return;
		}
	
		snk_sound_busy_bit = 0x20;
		soundlatch_w(0, data);
		cpu_set_nmi_line(2, PULSE_LINE);
	}
	
	/**************************************************************************/
	
	public static ReadHandlerPtr hal21_videoram_r  = new ReadHandlerPtr() { public int handler(int offset){ return videoram.read(offset); } };
	public static WriteHandlerPtr hal21_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){ videoram.write(offset,data); } };
	public static ReadHandlerPtr hal21_spriteram_r  = new ReadHandlerPtr() { public int handler(int offset){ return spriteram.read(offset); } };
	public static WriteHandlerPtr hal21_spriteram_w = new WriteHandlerPtr() {public void handler(int offset, int data){ spriteram.write(offset,data); } };
	
	public static WriteHandlerPtr hal21_vreg0_w = new WriteHandlerPtr() {public void handler(int offset, int data){ hal21_vreg[0] = data; } };
	public static WriteHandlerPtr hal21_vreg1_w = new WriteHandlerPtr() {public void handler(int offset, int data){ hal21_vreg[1] = data; } };
	public static WriteHandlerPtr hal21_vreg2_w = new WriteHandlerPtr() {public void handler(int offset, int data){ hal21_vreg[2] = data; } };
	public static WriteHandlerPtr hal21_vreg3_w = new WriteHandlerPtr() {public void handler(int offset, int data){ hal21_vreg[3] = data; } };
	public static WriteHandlerPtr hal21_vreg4_w = new WriteHandlerPtr() {public void handler(int offset, int data){ hal21_vreg[4] = data; } };
	public static WriteHandlerPtr hal21_vreg5_w = new WriteHandlerPtr() {public void handler(int offset, int data){ hal21_vreg[5] = data; } };
	public static WriteHandlerPtr hal21_vreg6_w = new WriteHandlerPtr() {public void handler(int offset, int data){ hal21_vreg[6] = data; } };
	public static WriteHandlerPtr hal21_vreg7_w = new WriteHandlerPtr() {public void handler(int offset, int data){ hal21_vreg[7] = data; } };
	
	
	PALETTE_INIT( aso )
	{
		int i;
		int num_colors = 1024;
	
		/*
			palette format is RRRG GGBB B??? the three unknown bits are used but
			I'm not sure how, I'm currently using them as least significant bit but
			that's most likely wrong.
		*/
		for( i=0; i<num_colors; i++ )
		{
			int bit0=0,bit1,bit2,bit3,r,g,b;
	
			bit0 = (color_prom.read(i + 2*num_colors)>> 2) & 0x01;
			bit1 = (color_prom.read(i)>> 1) & 0x01;
			bit2 = (color_prom.read(i)>> 2) & 0x01;
			bit3 = (color_prom.read(i)>> 3) & 0x01;
			r = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			bit0 = (color_prom.read(i + 2*num_colors)>> 1) & 0x01;
			bit1 = (color_prom.read(i + num_colors)>> 2) & 0x01;
			bit2 = (color_prom.read(i + num_colors)>> 3) & 0x01;
			bit3 = (color_prom.read(i)>> 0) & 0x01;
			g = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			bit0 = (color_prom.read(i + 2*num_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(i + 2*num_colors)>> 3) & 0x01;
			bit2 = (color_prom.read(i + num_colors)>> 0) & 0x01;
			bit3 = (color_prom.read(i + num_colors)>> 1) & 0x01;
			b = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			palette_set_color(i,r,g,b);
		}
	
		/* prepare shadow draw table */
		for (i=0; i<=5; i++) gfx_drawmode_table[i] = DRAWMODE_SOURCE;
	
		gfx_drawmode_table[6] = DRAWMODE_SHADOW;
		gfx_drawmode_table[7] = DRAWMODE_NONE;
	}
	
	VIDEO_START( aso )
	{
		snk_blink_parity = 0;
	
		return 0;
	}
	
	
	static void hal21_draw_background( struct mame_bitmap *bitmap, int scrollx, int scrolly, int attrs,
									const struct GfxElement *gfx )
	{
		static int color[2] = {8, 8};
		struct rectangle *cliprect;
		int bankbase, c, x, y, offsx, offsy, dx, dy, sx, sy, offs, tile_number;
	
		cliprect = &Machine->visible_area;
		bankbase = attrs<<3 & 0x100;
		c = attrs & 0x0f;
		if (c > 11) { fillbitmap(bitmap,Machine->pens[(c<<4)+8], cliprect); return; }
		if (c<8 || color[0]<14 || bankbase)
		{
			c ^= 0x08;
			color[0] = c;
			color[1] = (c & 0x08) ? c : 8;
		}
	
		offsx = ((scrollx>>3) + 0) & 0x3f;
		dx = -(scrollx & 7) + 0;
		offsy = ((scrolly>>3) + 0) & 0x3f;
		dy = -(scrolly & 7) + 0;
	
		for (x=2; x<35; x++)
			for (y=0; y<28; y++)
			{
				offs = (((offsx+x)&0x3f)<<6) + ((offsy+y)&0x3f);
				sx = (x<<3) + dx;
				sy = (y<<3) + dy;
				tile_number = bankbase + videoram.read(offs);
				c = (tile_number & ~0x3f) ? color[0] : color[1];
	
				drawgfx(bitmap, gfx,
					tile_number, c,
					0, 0,
					sx, sy,
					cliprect, TRANSPARENCY_NONE, 0);
			}
	}
	
	static void hal21_draw_sprites( struct mame_bitmap *bitmap, int scrollx, int scrolly,
									const struct GfxElement *gfx )
	{
		struct rectangle *cliprect;
		UINT8 *sprptr, *endptr;
		int attrs, tile, x, y, color, fy;
	
		cliprect = &Machine->visible_area;
		sprptr = spriteram;
		endptr = spriteram + 0x100;
	
		for (; sprptr<endptr; sprptr+=4)
		{
			if (*(UINT32*)sprptr == 0 || *(UINT32*)sprptr == -1) continue;
	
			attrs = sprptr[3];
			tile  = sprptr[1] + (attrs<<2 & 0x100);
			color = attrs & 0x0f;
			fy    = attrs & 0x20;
			y     = (sprptr[0] + (attrs<<4 & 0x100) - scrolly) & 0x1ff;
			x     = (0x100 - (sprptr[2] + (attrs<<1 & 0x100) - scrollx)) & 0x1ff;
			if (y > 512-16) y -= 512;
			if (x > 512-16) x -= 512;
	
			drawgfx(bitmap, gfx,
					tile, color,
					0, fy,
					x, y,
					cliprect, TRANSPARENCY_PEN, 7);
		}
	}
	
	static void aso_draw_background( struct mame_bitmap *bitmap, int scrollx, int scrolly, int attrs,
									const struct GfxElement *gfx )
	{
		struct rectangle *cliprect;
		int bankbase, c, x, y, offsx, offsy, dx, dy, sx, sy, offs, tile_number;
	
		cliprect = &Machine->visible_area;
		bankbase = attrs<<4 & 0x300;
		c = attrs & 0x0f;
		if (c == 7) c = 15;
	
		offsx = ((scrollx>>3) + 0) & 0x3f;
		dx = -(scrollx & 7) + 0;
		offsy = ((scrolly>>3) + 0) & 0x3f;
		dy = -(scrolly & 7) + 0;
	
		for (x=2; x<35; x++)
			for (y=0; y<28; y++)
			{
				offs = (((offsx+x)&0x3f)<<6) + ((offsy+y)&0x3f);
				sx = (x<<3) + dx;
				sy = (y<<3) + dy;
				tile_number = bankbase + videoram.read(offs);
	
				drawgfx(bitmap, gfx,
					tile_number, c,
					0, 0,
					sx, sy,
					cliprect, TRANSPARENCY_NONE, 0);
			}
	}
	
	static void aso_draw_sprites( struct mame_bitmap *bitmap, int scrollx, int scrolly,
									const struct GfxElement *gfx )
	{
		struct rectangle *cliprect;
		UINT8 *sprptr, *endptr;
		int attrs, tile, x, y, color;
	
		cliprect = &Machine->visible_area;
		sprptr = spriteram;
		endptr = spriteram + 0x100;
	
		for (; sprptr<endptr; sprptr+=4)
		{
			if (*(UINT32*)sprptr == 0 || *(UINT32*)sprptr == -1) continue;
	
			attrs = sprptr[3]; /* YBBX.CCCC */
			tile  = sprptr[1] + (attrs<<2 & 0x100) + (~attrs<<4 & 0x200);
			color = attrs & 0x0f;
			y     = (sprptr[0] + (attrs<<4 & 0x100) - scrolly) & 0x1ff;
			x     = (0x100 - (sprptr[2] + (attrs<<1 & 0x100) - scrollx)) & 0x1ff;
			if (y > 512-16) y -= 512;
			if (x > 512-16) x -= 512;
	
			drawgfx(bitmap, gfx,
					tile, color,
					0, 0,
					x, y,
					cliprect, TRANSPARENCY_PEN_TABLE, 7);
		}
	}
	
	VIDEO_UPDATE( aso )
	{
		UINT8 *ram = memory_region(REGION_CPU1);
		int attr, msbs, spsy, spsx, bgsy, bgsx, bank, i;
	
		attr = (int)hal21_vreg[0];
		msbs = (int)hal21_vreg[1];
		spsy = (int)hal21_vreg[2] + (msbs<<5 & 0x100) + 9;
		spsx = (int)hal21_vreg[3] + (msbs<<8 & 0x100) + 30;
		bgsy = (int)hal21_vreg[4] + (msbs<<4 & 0x100) - 8;
		bgsx = (int)hal21_vreg[5] - 16;
	
		if (snk_gamegroup)
		{
			hal21_draw_background(bitmap, bgsx+(msbs<<7 & 0x100), bgsy, attr, Machine->gfx[1]);
	
			attr = snk_blink_parity;
			snk_blink_parity ^= 0xdf;
			for (i=6; i<0x80; i+=8) { palette_set_color(i, attr, attr, attr); }
	
			hal21_draw_sprites(bitmap, spsx, spsy, Machine->gfx[2]);
		}
		else
		{
			aso_draw_background(bitmap, bgsx+(~msbs<<7 & 0x100), bgsy, attr, Machine->gfx[1]);
			aso_draw_sprites(bitmap, spsx, spsy, Machine->gfx[2]);
		}
	
		bank = msbs>>6 & 1;
		tnk3_draw_text(bitmap, bank, &ram[0xf800]);
		tnk3_draw_status(bitmap, bank, &ram[0xfc00]);
	}
	
	
	static InputPortPtr input_ports_hal21 = new InputPortPtr(){ public void handler() { 
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW,  IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW,  IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW,  IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* sound CPU status */
		PORT_BIT( 0x40, IP_ACTIVE_LOW,  IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN );
	
		PORT_START();  /* P1 controls */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0xc0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();  /* P2 controls */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0xc0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();   /* DSW1 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x04, "3" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x20, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0xc0, "20000 60000" );
		PORT_DIPSETTING(    0x80, "40000 90000" );
		PORT_DIPSETTING(    0x40, "50000 120000" );
		PORT_DIPSETTING(    0x00, "None" );
	
		PORT_START();   /* DSW2 */
		PORT_DIPNAME( 0x01, 0x01, "Bonus Type" );
		PORT_DIPSETTING(    0x01, "Every Bonus Set" );
		PORT_DIPSETTING(    0x00, "Second Bonus Set" );
		PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x06, "Easy" );
		PORT_DIPSETTING(    0x04, "2" );
		PORT_DIPSETTING(    0x02, "3" );
		PORT_DIPSETTING(    0x00, "4" );
		PORT_DIPNAME( 0x18, 0x18, "Special" );
		PORT_DIPSETTING(    0x18, "Normal" );
		PORT_DIPSETTING(    0x10, DEF_STR( "Demo_Sounds"));
		PORT_DIPSETTING(    0x08, "Infinite Lives" );
		PORT_DIPSETTING(    0x00, "Freeze" );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Flip_Screen") ); // 0x20 -> fe65
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	/**************************************************************************/
	
	static InputPortPtr input_ports_aso = new InputPortPtr(){ public void handler() { 
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_COIN2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW,  IPT_SERVICE1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW,  IPT_COIN1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW,  IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW,  IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* sound CPU status */
		PORT_BIT( 0x40, IP_ACTIVE_LOW,  IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON3 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_DIPNAME( 0x01, 0x01, "Allow Continue" );
		PORT_DIPSETTING(    0x01, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x04, "3" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C"));
		PORT_DIPSETTING(    0x28, DEF_STR( "3C_1C"));
		PORT_DIPSETTING(    0x30, DEF_STR( "2C_1C"));
		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C"));
		PORT_DIPSETTING(    0x18, DEF_STR( "1C_2C"));
		PORT_DIPSETTING(    0x10, DEF_STR( "1C_3C"));
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_4C"));
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C"));
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0xc0, "50k 100k" );
		PORT_DIPSETTING(    0x80, "60k 120k" );
		PORT_DIPSETTING(    0x40, "100k 200k" );
		PORT_DIPSETTING(    0x00, "None" );
	
		PORT_START(); 
		PORT_DIPNAME( 0x01, 0x01, "Bonus Occurrence" );
		PORT_DIPSETTING(    0x01, "1st & every 2nd" );
		PORT_DIPSETTING(    0x00, "1st & 2nd only" );
		PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x06, "Easy" );
		PORT_DIPSETTING(    0x04, "Normal" );
		PORT_DIPSETTING(    0x02, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BITX( 0x10,    0x10, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Cheat of some kind", IP_KEY_NONE, IP_JOY_NONE );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0xc0, 0xc0, "Start Area" );
		PORT_DIPSETTING(    0xc0, "1" );
		PORT_DIPSETTING(    0x80, "2" );
		PORT_DIPSETTING(    0x40, "3" );
		PORT_DIPSETTING(    0x00, "4" );
	INPUT_PORTS_END(); }}; 
	
	
	/**************************************************************************/
	
	static GfxLayout char256 = new GfxLayout(
		8,8,
		0x100,
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 4, 0, 12, 8, 20, 16, 28, 24},
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		256
	);
	
	static GfxLayout char1024 = new GfxLayout(
		8,8,
		0x400,
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 4, 0, 12, 8, 20, 16, 28, 24},
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		256
	);
	
	static GfxLayout sprite1024 = new GfxLayout(
		16,16,
		0x400,
		3,
		new int[] { 2*1024*256,1*1024*256,0*1024*256 },
		new int[] {
			7,6,5,4,3,2,1,0,
			15,14,13,12,11,10,9,8
		},
		new int[] {
			0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
			8*16, 9*16, 10*16, 11*16, 12*16, 13*16, 14*16, 15*16
		},
		256
	);
	
	static GfxDecodeInfo aso_gfxdecodeinfo[] =
	{
		/* colors 512-1023 are currently unused, I think they are a second bank */
		new GfxDecodeInfo( REGION_GFX1, 0, char256,    128*3,  8 ), /* colors 384..511 */
		new GfxDecodeInfo( REGION_GFX2, 0, char1024,   128*1, 16 ), /* colors 128..383 */
		new GfxDecodeInfo( REGION_GFX3, 0, sprite1024, 128*0, 16 ), /* colors   0..127 */
		new GfxDecodeInfo( -1 )
	};
	
	/**************************************************************************/
	
	public static ReadHandlerPtr shared_auxram_r  = new ReadHandlerPtr() { public int handler(int offset) { return shared_auxram[offset]; } };
	public static WriteHandlerPtr shared_auxram_w = new WriteHandlerPtr() {public void handler(int offset, int data) { shared_auxram[offset] = data; } };
	
	public static ReadHandlerPtr shared_ram_r  = new ReadHandlerPtr() { public int handler(int offset) { return shared_ram[offset]; } };
	public static WriteHandlerPtr shared_ram_w = new WriteHandlerPtr() {public void handler(int offset, int data) { shared_ram[offset] = data; } };
	
	public static ReadHandlerPtr CPUC_ready_r  = new ReadHandlerPtr() { public int handler(int offset) { snk_sound_busy_bit = 0; return 0; } };
	
	public static ReadHandlerPtr hal21_input_port_0_r  = new ReadHandlerPtr() { public int handler(int offset) { return input_port_0_r(0) | snk_sound_busy_bit; } };
	
	public static WriteHandlerPtr hal21_soundcommand_w = new WriteHandlerPtr() {public void handler(int offset, int data) { hal21_sound_scheduler(1, data); } };
	public static WriteHandlerPtr hal21_soundack_w = new WriteHandlerPtr() {public void handler(int offset, int data) { hal21_sound_scheduler(2, data); } };
	
	public static ReadHandlerPtr hal21_soundcommand_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int data = soundlatch_r(0);
		soundlatch_clear_w(0, 0);
		return data;
	} };
	
	public static WriteHandlerPtr aso_soundcommand_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		snk_sound_busy_bit = 0x20;
		soundlatch_w(0, data);
		cpu_set_irq_line( 2, 0, HOLD_LINE );
	} };
	
	static INTERRUPT_GEN( hal21_sound_interrupt )
	{
		hal21_sound_scheduler(3, 0);
	}
	
	/**************************************************************************/
	
	static struct YM3526interface ym3526_interface ={
		1,          /* number of chips */
		4000000,    /* 4 MHz? (hand tuned) */
		{ 100 }
	};
	
	public static Memory_ReadAddress aso_readmem_sound[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0xbfff, MRA_ROM ),
		new Memory_ReadAddress( 0xc000, 0xc7ff, MRA_RAM ),
		new Memory_ReadAddress( 0xd000, 0xd000, hal21_soundcommand_r ),
		new Memory_ReadAddress( 0xe000, 0xe000, CPUC_ready_r ),
		new Memory_ReadAddress( 0xf000, 0xf000, YM3526_status_port_0_r ),
		new Memory_ReadAddress( 0xf002, 0xf002, MRA_NOP ), // unknown read
		new Memory_ReadAddress( 0xf004, 0xf004, MRA_NOP ), // unknown read
		new Memory_ReadAddress( 0xf006, 0xf006, MRA_NOP ), // unknown read
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress aso_writemem_sound[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new Memory_WriteAddress( 0xc000, 0xc7ff, MWA_RAM ),
		new Memory_WriteAddress( 0xf000, 0xf000, YM3526_control_port_0_w ), /* YM3526 #1 control port? */
		new Memory_WriteAddress( 0xf001, 0xf001, YM3526_write_port_0_w ),   /* YM3526 #1 write port?  */
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	/**************************************************************************/
	
	static AY8910interface ay8910_interface = new AY8910interface(
		2, /* number of chips */
		1500000, // hand tuned
		new int[] { 25,40 },
		new ReadHandlerPtr[] { 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 }
	);
	
	public static Memory_ReadAddress hal21_readmem_sound[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new Memory_ReadAddress( 0x8000, 0x87ff, MRA_RAM ),
		new Memory_ReadAddress( 0xa000, 0xa000, hal21_soundcommand_r ),
		new Memory_ReadAddress( 0xc000, 0xc000, CPUC_ready_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress hal21_writemem_sound[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x3fff, MWA_ROM ),
		new Memory_WriteAddress( 0x8000, 0x87ff, MWA_RAM ),
		new Memory_WriteAddress( 0xe000, 0xe000, AY8910_control_port_0_w ),
		new Memory_WriteAddress( 0xe001, 0xe001, AY8910_write_port_0_w ),
		new Memory_WriteAddress( 0xe002, 0xe002, hal21_soundack_w ), // bitfielded(0-5) acknowledge write, details unknown
		new Memory_WriteAddress( 0xe008, 0xe008, AY8910_control_port_1_w ),
		new Memory_WriteAddress( 0xe009, 0xe009, AY8910_write_port_1_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static IO_ReadPort hal21_readport_sound[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x0000, 0x0000, MRA_NOP ), // external sound ROM detection?
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort hal21_writeport_sound[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x0000, 0x0000, MWA_NOP ), // external sound ROM switch?
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	/**************************** ASO/Alpha Mission *************************/
	
	public static Memory_ReadAddress aso_readmem_cpuA[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0xbfff, MRA_ROM ),
		new Memory_ReadAddress( 0xc000, 0xc000, hal21_input_port_0_r ), /* coin, start */
		new Memory_ReadAddress( 0xc100, 0xc100, input_port_1_r ), /* P1 */
		new Memory_ReadAddress( 0xc200, 0xc200, input_port_2_r ), /* P2 */
		new Memory_ReadAddress( 0xc500, 0xc500, input_port_3_r ), /* DSW1 */
		new Memory_ReadAddress( 0xc600, 0xc600, input_port_4_r ), /* DSW2 */
		new Memory_ReadAddress( 0xc700, 0xc700, snk_cpuB_nmi_trigger_r ),
		new Memory_ReadAddress( 0xd800, 0xffff, MRA_RAM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress aso_writemem_cpuA[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new Memory_WriteAddress( 0xc400, 0xc400, aso_soundcommand_w ),
		new Memory_WriteAddress( 0xc700, 0xc700, snk_cpuA_nmi_ack_w ),
		new Memory_WriteAddress( 0xc800, 0xc800, hal21_vreg1_w ),
		new Memory_WriteAddress( 0xc900, 0xc900, hal21_vreg2_w ),
		new Memory_WriteAddress( 0xca00, 0xca00, hal21_vreg3_w ),
		new Memory_WriteAddress( 0xcb00, 0xcb00, hal21_vreg4_w ),
		new Memory_WriteAddress( 0xcc00, 0xcc00, hal21_vreg5_w ),
		new Memory_WriteAddress( 0xcd00, 0xcd00, hal21_vreg6_w ),
		new Memory_WriteAddress( 0xce00, 0xce00, hal21_vreg7_w ),
		new Memory_WriteAddress( 0xcf00, 0xcf00, hal21_vreg0_w ),
		new Memory_WriteAddress( 0xdcf8, 0xdcf8, aso_scroll_sync_w ),
		new Memory_WriteAddress( 0xd800, 0xdfff, MWA_RAM, shared_auxram ),
		new Memory_WriteAddress( 0xe000, 0xe7ff, MWA_RAM, spriteram ),
		new Memory_WriteAddress( 0xe800, 0xf7ff, MWA_RAM, videoram ),
		new Memory_WriteAddress( 0xf800, 0xffff, MWA_RAM, shared_ram ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_ReadAddress aso_readmem_cpuB[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0xbfff, MRA_ROM ),
		new Memory_ReadAddress( 0xc000, 0xc000, snk_cpuA_nmi_trigger_r ),
		new Memory_ReadAddress( 0xc800, 0xe7ff, shared_auxram_r ),
		new Memory_ReadAddress( 0xe800, 0xf7ff, MRA_RAM ),
		new Memory_ReadAddress( 0xf800, 0xffff, shared_ram_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress aso_writemem_cpuB[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new Memory_WriteAddress( 0xc000, 0xc000, snk_cpuB_nmi_ack_w ),
		new Memory_WriteAddress( 0xc800, 0xd7ff, shared_auxram_w ),
		new Memory_WriteAddress( 0xd800, 0xe7ff, hal21_videoram_w ),
		new Memory_WriteAddress( 0xe800, 0xf7ff, MWA_RAM ),
		new Memory_WriteAddress( 0xf800, 0xffff, shared_ram_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	/**************************** HAL21 *************************/
	
	public static Memory_ReadAddress hal21_readmem_CPUA[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0xc000, 0xc000, hal21_input_port_0_r ), /* coin, start */
		new Memory_ReadAddress( 0xc100, 0xc100, input_port_1_r ), /* P1 */
		new Memory_ReadAddress( 0xc200, 0xc200, input_port_2_r ), /* P2 */
		new Memory_ReadAddress( 0xc400, 0xc400, input_port_3_r ), /* DSW1 */
		new Memory_ReadAddress( 0xc500, 0xc500, input_port_4_r ), /* DSW2 */
		new Memory_ReadAddress( 0xc700, 0xc700, snk_cpuB_nmi_trigger_r ),
		new Memory_ReadAddress( 0xe000, 0xefff, MRA_RAM ),
		new Memory_ReadAddress( 0xf000, 0xffff, MRA_RAM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress hal21_writemem_CPUA[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0xc300, 0xc300, hal21_soundcommand_w ),
		new Memory_WriteAddress( 0xc600, 0xc600, hal21_vreg0_w ),
		new Memory_WriteAddress( 0xc700, 0xc700, snk_cpuA_nmi_ack_w ),
		new Memory_WriteAddress( 0xd300, 0xd300, hal21_vreg1_w ),
		new Memory_WriteAddress( 0xd400, 0xd400, hal21_vreg2_w ),
		new Memory_WriteAddress( 0xd500, 0xd500, hal21_vreg3_w ),
		new Memory_WriteAddress( 0xd600, 0xd600, hal21_vreg4_w ),
		new Memory_WriteAddress( 0xd700, 0xd700, hal21_vreg5_w ),
		new Memory_WriteAddress( 0xe000, 0xefff, MWA_RAM, spriteram ),
		new Memory_WriteAddress( 0xf000, 0xffff, MWA_RAM, shared_ram ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_ReadAddress hal21_readmem_CPUB[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x9fff, MRA_ROM ),
		new Memory_ReadAddress( 0xc000, 0xcfff, hal21_spriteram_r ),
		new Memory_ReadAddress( 0xd000, 0xdfff, MRA_RAM ),
		new Memory_ReadAddress( 0xe000, 0xefff, shared_ram_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress hal21_writemem_CPUB[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x9fff, MWA_ROM ),
		new Memory_WriteAddress( 0xa000, 0xa000, snk_cpuB_nmi_ack_w ),
		new Memory_WriteAddress( 0xc000, 0xcfff, hal21_spriteram_w ),
		new Memory_WriteAddress( 0xd000, 0xdfff, MWA_RAM, videoram ),
		new Memory_WriteAddress( 0xe000, 0xefff, shared_ram_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	/**************************************************************************/
	
	DRIVER_INIT( aso )
	{
		hal21_vreg = auto_malloc(16);
		snk_gamegroup = 0;
	}
	
	DRIVER_INIT( hal21 )
	{
		hal21_vreg = auto_malloc(24);
		hal21_sndfifo = hal21_vreg + 8;
		snk_gamegroup = 1;
	}
	
	MACHINE_INIT( aso )
	{
		memset(hal21_vreg, 0, 8);
		hal21_sound_scheduler(0, 0);
		snk_sound_busy_bit = 0;
	}
	
	static MACHINE_DRIVER_START( aso )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80, 4000000)
		MDRV_CPU_MEMORY(aso_readmem_cpuA,aso_writemem_cpuA)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_CPU_ADD(Z80, 4000000)
		MDRV_CPU_MEMORY(aso_readmem_cpuB,aso_writemem_cpuB)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_CPU_ADD(Z80, 4000000)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)
		MDRV_CPU_MEMORY(aso_readmem_sound,aso_writemem_sound)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_INTERLEAVE(100)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER | VIDEO_HAS_SHADOWS)
		MDRV_SCREEN_SIZE(36*8, 28*8)
		MDRV_VISIBLE_AREA(0*8, 36*8-1, 1*8, 28*8-1)
		MDRV_GFXDECODE(aso_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_MACHINE_INIT(aso)
	
		MDRV_PALETTE_INIT(aso)
		MDRV_VIDEO_START(aso)
		MDRV_VIDEO_UPDATE(aso)
	
		/* sound hardware */
		MDRV_SOUND_ADD(YM3526, ym3526_interface)
	MACHINE_DRIVER_END
	
	static MACHINE_DRIVER_START( hal21 )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80, 4000000)
		MDRV_CPU_MEMORY(hal21_readmem_CPUA,hal21_writemem_CPUA)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_CPU_ADD(Z80, 4000000)
		MDRV_CPU_MEMORY(hal21_readmem_CPUB,hal21_writemem_CPUB)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_CPU_ADD(Z80, 4000000)
		MDRV_CPU_FLAGS(CPU_AUDIO_CPU)
		MDRV_CPU_MEMORY(hal21_readmem_sound,hal21_writemem_sound)
		MDRV_CPU_PORTS(hal21_readport_sound,hal21_writeport_sound)
		MDRV_CPU_VBLANK_INT(hal21_sound_interrupt,1)
		MDRV_CPU_PERIODIC_INT(irq0_line_hold,220) // music tempo, hand tuned
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_INTERLEAVE(100)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER | VIDEO_HAS_HIGHLIGHTS)
		MDRV_SCREEN_SIZE(36*8, 28*8)
		MDRV_VISIBLE_AREA(0*8, 36*8-1, 1*8, 28*8-1)
		MDRV_GFXDECODE(aso_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(1024)
	
		MDRV_MACHINE_INIT(aso)
	
		MDRV_PALETTE_INIT(aso)
		MDRV_VIDEO_START(aso)
		MDRV_VIDEO_UPDATE(aso)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, ay8910_interface)
	MACHINE_DRIVER_END
	
	/**************************************************************************/
	
	static RomLoadPtr rom_hal21 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );  /* 64k for CPUA code */
		ROM_LOAD( "hal21p1.bin",    0x0000, 0x2000, CRC(9d193830),SHA1(8e4e9c8bc774d7c7c0b68a5fa5cabdc6b5cfa41b) )
		ROM_LOAD( "hal21p2.bin",    0x2000, 0x2000, CRC(c1f00350),SHA1(8709455a980931565ccca60162a04c6c3133099b) )
		ROM_LOAD( "hal21p3.bin",    0x4000, 0x2000, CRC(881d22a6),SHA1(4b2a65dc18620f7f77532f791212fccfe1f0b245) )
		ROM_LOAD( "hal21p4.bin",    0x6000, 0x2000, CRC(ce692534),SHA1(e1d8e6948578ec9d0b6dc2aff17ad23b8ce46d6a) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );  /* 64k for CPUB code */
		ROM_LOAD( "hal21p5.bin",    0x0000, 0x2000, CRC(3ce0684a),SHA1(5e76770a3252d5565a8f11a79ac3a9a6c31a43e2) )
		ROM_LOAD( "hal21p6.bin",    0x2000, 0x2000, CRC(878ef798),SHA1(0aae152947c9c6733b77dd1ac14f2f6d6bfabeaa) )
		ROM_LOAD( "hal21p7.bin",    0x4000, 0x2000, CRC(72ebbe95),SHA1(b1f7dc535e7670647500391d21dfa971d5e342a2) )
		ROM_LOAD( "hal21p8.bin",    0x6000, 0x2000, CRC(17e22ad3),SHA1(0e10a3c0f2e2ec284f4e0f1055397a8ccd1ff0f7) )
		ROM_LOAD( "hal21p9.bin",    0x8000, 0x2000, CRC(b146f891),SHA1(0b2db3e14b0401a7914002c6f7c26933a1cba162) )
	
		ROM_REGION( 0x10000, REGION_CPU3, 0 );  /* 64k for sound code */
		ROM_LOAD( "hal21p10.bin",   0x0000, 0x4000, CRC(916f7ba0),SHA1(7b8bcd59d768c4cd226de96895d3b9755bb3ba79) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "hal21p12.bin", 0x0000, 0x2000, CRC(9839a7cd),SHA1(d3f9d964263a64aa3648faf5eb2e4fa532ae7852) ) /* char */
	
		ROM_REGION( 0x8000, REGION_GFX2, ROMREGION_DISPOSE );/* background tiles */
		ROM_LOAD( "hal21p11.bin", 0x0000, 0x4000, CRC(24abc57e),SHA1(1d7557a62adc059fb3fe20a09be18c2f40441581) )
	
		ROM_REGION( 0x18000, REGION_GFX3, ROMREGION_DISPOSE );/* 16x16 sprites */
		ROM_LOAD( "hal21p13.bin", 0x00000, 0x4000, CRC(052b4f4f),SHA1(032eb5771d33defce86e222f3e7aa22bc37db6db) )
		ROM_RELOAD(               0x04000, 0x4000 );
		ROM_LOAD( "hal21p14.bin", 0x08000, 0x4000, CRC(da0cb670),SHA1(1083bdd3488dfaa5094a2ef52cfc4206f35c9612) )
		ROM_RELOAD(               0x0c000, 0x4000 );
		ROM_LOAD( "hal21p15.bin", 0x10000, 0x4000, CRC(5c5ea945),SHA1(f9ce206cab4fad1f6478d731d4b096ec33e7b99f) )
		ROM_RELOAD(               0x14000, 0x4000 );
	
		ROM_REGION( 0x0c00, REGION_PROMS, 0 );
		ROM_LOAD( "hal21_3.prm",  0x000, 0x400, CRC(605afff8),SHA1(94e80ebd574b1580dac4a2aebd57e3e767890c0d) )
		ROM_LOAD( "hal21_2.prm",  0x400, 0x400, CRC(c5d84225),SHA1(cc2cd32f81ed7c1bcdd68e91d00f8081cb706ce7) )
		ROM_LOAD( "hal21_1.prm",  0x800, 0x400, CRC(195768fc),SHA1(c88bc9552d57d52fb4b030d118f48fedccf563f4) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_hal21j = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );  /* 64k for CPUA code */
		ROM_LOAD( "hal21p1.bin",    0x0000, 0x2000, CRC(9d193830),SHA1(8e4e9c8bc774d7c7c0b68a5fa5cabdc6b5cfa41b) )
		ROM_LOAD( "hal21p2.bin",    0x2000, 0x2000, CRC(c1f00350),SHA1(8709455a980931565ccca60162a04c6c3133099b) )
		ROM_LOAD( "hal21p3.bin",    0x4000, 0x2000, CRC(881d22a6),SHA1(4b2a65dc18620f7f77532f791212fccfe1f0b245) )
		ROM_LOAD( "hal21p4.bin",    0x6000, 0x2000, CRC(ce692534),SHA1(e1d8e6948578ec9d0b6dc2aff17ad23b8ce46d6a) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );  /* 64k for CPUB code */
		ROM_LOAD( "hal21p5.bin",    0x0000, 0x2000, CRC(3ce0684a),SHA1(5e76770a3252d5565a8f11a79ac3a9a6c31a43e2) )
		ROM_LOAD( "hal21p6.bin",    0x2000, 0x2000, CRC(878ef798),SHA1(0aae152947c9c6733b77dd1ac14f2f6d6bfabeaa) )
		ROM_LOAD( "hal21p7.bin",    0x4000, 0x2000, CRC(72ebbe95),SHA1(b1f7dc535e7670647500391d21dfa971d5e342a2) )
		ROM_LOAD( "hal21p8.bin",    0x6000, 0x2000, CRC(17e22ad3),SHA1(0e10a3c0f2e2ec284f4e0f1055397a8ccd1ff0f7) )
		ROM_LOAD( "hal21p9.bin",    0x8000, 0x2000, CRC(b146f891),SHA1(0b2db3e14b0401a7914002c6f7c26933a1cba162) )
	
		ROM_REGION( 0x10000, REGION_CPU3, 0 );  /* 64k for sound code */
		ROM_LOAD( "hal21-10.bin",   0x0000, 0x4000, CRC(a182b3f0),SHA1(b76eff97a58a96467e9f3a74125a0a770e7678f8) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "hal21p12.bin", 0x0000, 0x2000, CRC(9839a7cd),SHA1(d3f9d964263a64aa3648faf5eb2e4fa532ae7852) ) /* char */
	
		ROM_REGION( 0x8000, REGION_GFX2, ROMREGION_DISPOSE );/* background tiles */
		ROM_LOAD( "hal21p11.bin", 0x0000, 0x4000, CRC(24abc57e),SHA1(1d7557a62adc059fb3fe20a09be18c2f40441581) )
	
		ROM_REGION( 0x18000, REGION_GFX3, ROMREGION_DISPOSE );/* 16x16 sprites */
		ROM_LOAD( "hal21p13.bin", 0x00000, 0x4000, CRC(052b4f4f),SHA1(032eb5771d33defce86e222f3e7aa22bc37db6db) )
		ROM_RELOAD(               0x04000, 0x4000 );
		ROM_LOAD( "hal21p14.bin", 0x08000, 0x4000, CRC(da0cb670),SHA1(1083bdd3488dfaa5094a2ef52cfc4206f35c9612) )
		ROM_RELOAD(               0x0c000, 0x4000 );
		ROM_LOAD( "hal21p15.bin", 0x10000, 0x4000, CRC(5c5ea945),SHA1(f9ce206cab4fad1f6478d731d4b096ec33e7b99f) )
		ROM_RELOAD(               0x14000, 0x4000 );
	
		ROM_REGION( 0x0c00, REGION_PROMS, 0 );
		ROM_LOAD( "hal21_3.prm",  0x000, 0x400, CRC(605afff8),SHA1(94e80ebd574b1580dac4a2aebd57e3e767890c0d) )
		ROM_LOAD( "hal21_2.prm",  0x400, 0x400, CRC(c5d84225),SHA1(cc2cd32f81ed7c1bcdd68e91d00f8081cb706ce7) )
		ROM_LOAD( "hal21_1.prm",  0x800, 0x400, CRC(195768fc),SHA1(c88bc9552d57d52fb4b030d118f48fedccf563f4) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_aso = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );  /* 64k for cpuA code */
		ROM_LOAD( "aso.1",    0x0000, 0x8000, CRC(3fc9d5e4),SHA1(1318904d3d896affd5affd8e475ac9ee6929b955) )
		ROM_LOAD( "aso.3",    0x8000, 0x4000, CRC(39a666d2),SHA1(b5426520eb600d44bc5566d742d7b88194076494) )
	
		ROM_REGION( 0x10000, REGION_CPU2, 0 );  /* 64k for cpuB code */
		ROM_LOAD( "aso.4",    0x0000, 0x8000, CRC(2429792b),SHA1(674e81880f359f7e8d34d0ad9074267360afadbf) )
		ROM_LOAD( "aso.6",    0x8000, 0x4000, CRC(c0bfdf1f),SHA1(65b15ce9c2e78df79cb603c58639421d29701633) )
	
		ROM_REGION( 0x10000, REGION_CPU3, 0 );  /* 64k for sound code */
		ROM_LOAD( "aso.7",    0x0000, 0x8000, CRC(49258162),SHA1(c265b79d012be1e065389f910f7b4ce61f5b27ce) )  /* YM3526 */
		ROM_LOAD( "aso.9",    0x8000, 0x4000, CRC(aef5a4f4),SHA1(e908e79e27ff892fe75d1ba5cb0bc9dc6b7b4268) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE );/* characters */
		ROM_LOAD( "aso.14",   0x0000, 0x2000, CRC(8baa2253),SHA1(e6e4a5aa005e89744c4e2a19a080cf322edc6b52) )
	
		ROM_REGION( 0x8000, REGION_GFX2, ROMREGION_DISPOSE );/* background tiles */
		ROM_LOAD( "aso.10",   0x0000, 0x8000, CRC(00dff996),SHA1(4f6ce4c0f2da0d2a711bcbf9aa998b4e31d0d9bf) )
	
		ROM_REGION( 0x18000, REGION_GFX3, ROMREGION_DISPOSE );/* 16x16 sprites */
		ROM_LOAD( "aso.11",   0x00000, 0x8000, CRC(7feac86c),SHA1(13b81f006ec587583416c1e7432da4c3f0375924) )
		ROM_LOAD( "aso.12",   0x08000, 0x8000, CRC(6895990b),SHA1(e84554cae9a768021c3dc7183bc3d28e2dd768ee) )
		ROM_LOAD( "aso.13",   0x10000, 0x8000, CRC(87a81ce1),SHA1(28c1069e6c08ecd579f99620c1cb6df01ad1aa74) )
	
		ROM_REGION( 0x0c00, REGION_PROMS, 0 );
		ROM_LOAD( "up02_f12.rom",  0x000, 0x00400, CRC(5b0a0059),SHA1(f61e17c8959f1cd6cc12b38f2fb7c6190ebd0e0c) )
		ROM_LOAD( "up02_f13.rom",  0x400, 0x00400, CRC(37e28dd8),SHA1(681726e490872a574dd0295823a44d64ef3a7b45) )
		ROM_LOAD( "up02_f14.rom",  0x800, 0x00400, CRC(c3fd1dd3),SHA1(c48030cc458f0bebea0ffccf3d3c43260da6a7fb) )
	ROM_END(); }}; 
	
	public static GameDriver driver_aso	   = new GameDriver("1985"	,"aso"	,"hal21.java"	,rom_aso,null	,machine_driver_aso	,input_ports_aso	,init_aso	,ROT270	,	"SNK", "ASO - Armored Scrum Object", GAME_NO_COCKTAIL )
	public static GameDriver driver_hal21	   = new GameDriver("1985"	,"hal21"	,"hal21.java"	,rom_hal21,null	,machine_driver_hal21	,input_ports_hal21	,init_hal21	,ROT270	,	"SNK", "HAL21", GAME_IMPERFECT_COLORS | GAME_NO_COCKTAIL )
	public static GameDriver driver_hal21j	   = new GameDriver("1985"	,"hal21j"	,"hal21.java"	,rom_hal21j,driver_hal21	,machine_driver_hal21	,input_ports_hal21	,init_hal21	,ROT270	,	"SNK", "HAL21 (Japan)", GAME_IMPERFECT_COLORS | GAME_NO_COCKTAIL )
}
