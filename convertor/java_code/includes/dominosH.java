/*************************************************************************

	Atari Dominos hardware

*************************************************************************/

/*----------- defined in machine/dominos.c -----------*/


void dominos_ac_signal_flip(int dummy);

/*----------- defined in vidhrdw/dominos.c -----------*/

VIDEO_UPDATE( dominos );

extern unsigned char *dominos_sound_ram;
