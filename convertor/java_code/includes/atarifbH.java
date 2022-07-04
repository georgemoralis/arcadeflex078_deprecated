/*************************************************************************

	Atari Football hardware

*************************************************************************/

#define GAME_IS_FOOTBALL   (atarifb_game == 1)
#define GAME_IS_FOOTBALL4  (atarifb_game == 2)
#define GAME_IS_BASEBALL   (atarifb_game == 3)
#define GAME_IS_SOCCER     (atarifb_game == 4)


/*----------- defined in drivers/atarifb.c -----------*/



/*----------- defined in machine/atarifb.c -----------*/



/*----------- defined in vidhrdw/atarifb.c -----------*/

extern size_t atarifb_alphap1_vram_size;
extern size_t atarifb_alphap2_vram_size;
extern unsigned char *atarifb_alphap1_vram;
extern unsigned char *atarifb_alphap2_vram;
extern unsigned char *atarifb_scroll_register;


VIDEO_START( atarifb );
VIDEO_UPDATE( atarifb );
