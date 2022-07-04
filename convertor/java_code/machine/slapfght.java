/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package machine;

public class slapfght
{
	
	
	unsigned char *slapfight_dpram;
	size_t slapfight_dpram_size;
	
	int slapfight_status;
	int getstar_sequence_index;
	int getstar_sh_intenabled;
	
	static int slapfight_status_state;
	extern unsigned char *getstar_e803;
	
	static unsigned char mcu_val;
	
	/* Perform basic machine initialisation */
	MACHINE_INIT( slapfight )
	{
		/* MAIN CPU */
	
		slapfight_status_state=0;
		slapfight_status = 0xc7;
	
		getstar_sequence_index = 0;
		getstar_sh_intenabled = 0;	/* disable sound cpu interrupts */
	
		/* SOUND CPU */
		cpu_set_reset_line(1,ASSERT_LINE);
	
		/* MCU */
		mcu_val = 0;
	}
	
	/* Interrupt handlers cpu & sound */
	
	public static WriteHandlerPtr slapfight_dpram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	    slapfight_dpram[offset]=data;
	} };
	
	public static ReadHandlerPtr slapfight_dpram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    return slapfight_dpram[offset];
	} };
	
	
	
	/* Slapfight CPU input/output ports
	
	  These ports seem to control memory access
	
	*/
	
	/* Reset and hold sound CPU */
	public static WriteHandlerPtr slapfight_port_00_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		cpu_set_reset_line(1,ASSERT_LINE);
		getstar_sh_intenabled = 0;
	} };
	
	/* Release reset on sound CPU */
	public static WriteHandlerPtr slapfight_port_01_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		cpu_set_reset_line(1,CLEAR_LINE);
	} };
	
	/* Disable and clear hardware interrupt */
	public static WriteHandlerPtr slapfight_port_06_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		interrupt_enable_w(0,0);
	} };
	
	/* Enable hardware interrupt */
	public static WriteHandlerPtr slapfight_port_07_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		interrupt_enable_w(0,1);
	} };
	
	public static WriteHandlerPtr slapfight_port_08_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		unsigned char *RAM = memory_region(REGION_CPU1);
	
		cpu_setbank(1,&RAM[0x10000]);
	} };
	
	public static WriteHandlerPtr slapfight_port_09_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		unsigned char *RAM = memory_region(REGION_CPU1);
	
		cpu_setbank(1,&RAM[0x14000]);
	} };
	
	
	/* Status register */
	public static ReadHandlerPtr slapfight_port_00_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int states[3]={ 0xc7, 0x55, 0x00 };
	
		slapfight_status = states[slapfight_status_state];
	
		slapfight_status_state++;
		if (slapfight_status_state > 2) slapfight_status_state = 0;
	
		return slapfight_status;
	} };
	
	
	
	/*
	 Reads at e803 expect a sequence of values such that:
	 - first value is different from successive
	 - third value is (first+5)^0x56
	 I don't know what writes to this address do (connected to port 0 reads?).
	*/
	public static ReadHandlerPtr getstar_e803_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	unsigned char seq[] = { 0, 1, ((0+5)^0x56) };
	unsigned char val;
	
		val = seq[getstar_sequence_index];
		getstar_sequence_index = (getstar_sequence_index+1)%3;
		return val;
	} };
	
	/* Enable hardware interrupt of sound cpu */
	public static WriteHandlerPtr getstar_sh_intenable_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		getstar_sh_intenabled = 1;
		logerror("cpu #1 PC=%d: %d written to a0e0\n",activecpu_get_pc(),data);
	} };
	
	
	
	/* Generate interrups only if they have been enabled */
	INTERRUPT_GEN( getstar_interrupt )
	{
		if (getstar_sh_intenabled)
			cpu_set_irq_line(1, IRQ_LINE_NMI, PULSE_LINE);
	}
	
	public static WriteHandlerPtr getstar_port_04_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	//	cpu_halt(0,0);
	} };
	
	
	/* Tiger Heli MCU */
	
	static unsigned char from_main,from_mcu;
	static int mcu_sent = 0,main_sent = 0;
	static unsigned char portA_in,portA_out,ddrA;
	static unsigned char portB_in,portB_out,ddrB;
	static unsigned char portC_in,portC_out,ddrC;
	
	public static ReadHandlerPtr tigerh_68705_portA_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return (portA_out & ddrA) | (portA_in & ~ddrA);
	} };
	
	public static WriteHandlerPtr tigerh_68705_portA_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		portA_out = data;//?
		from_mcu = portA_out;
		mcu_sent = 1;
	} };
	
	public static WriteHandlerPtr tigerh_68705_ddrA_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		ddrA = data;
	} };
	
	public static ReadHandlerPtr tigerh_68705_portB_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return (portB_out & ddrB) | (portB_in & ~ddrB);
	} };
	
	public static WriteHandlerPtr tigerh_68705_portB_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	
		if ((ddrB & 0x02) && (~data & 0x02) && (portB_out & 0x02))
		{
			portA_in = from_main;
			if (main_sent) cpu_set_irq_line(2,0,CLEAR_LINE);
			main_sent = 0;
		}
		if ((ddrB & 0x04) && (data & 0x04) && (~portB_out & 0x04))
		{
			from_mcu = portA_out;
			mcu_sent = 1;
		}
	
		portB_out = data;
	} };
	
	public static WriteHandlerPtr tigerh_68705_ddrB_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		ddrB = data;
	} };
	
	
	public static ReadHandlerPtr tigerh_68705_portC_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		portC_in = 0;
		if (main_sent == 0) portC_in |= 0x01;
		if (mcu_sent) portC_in |= 0x02;
		return (portC_out & ddrC) | (portC_in & ~ddrC);
	} };
	
	public static WriteHandlerPtr tigerh_68705_portC_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		portC_out = data;
	} };
	
	public static WriteHandlerPtr tigerh_68705_ddrC_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		ddrC = data;
	} };
	
	public static WriteHandlerPtr tigerh_mcu_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		from_main = data;
		main_sent = 1;
		mcu_sent=0;
		cpu_set_irq_line(2,0,ASSERT_LINE);
	} };
	
	public static ReadHandlerPtr tigerh_mcu_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		mcu_sent = 0;
		return from_mcu;
	} };
	
	public static ReadHandlerPtr tigerh_mcu_status_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int res = 0;
		if (main_sent == 0) res |= 0x02;
		if (mcu_sent == 0) res |= 0x04;
		return res;
	} };
	
}
