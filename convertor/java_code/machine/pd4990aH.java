/*
 *	Header file for the PD4990A Serial I/O calendar & clock.
 */


struct pd4990a_s
{
	int seconds;
	int minutes;
	int hours;
	int days;
	int month;
	int year;
	int weekday;
};

extern struct pd4990a_s pd4990a;

void pd4990a_control_w(unsigned short);
WRITE16_HANDLER( pd4990a_control_16_w );

